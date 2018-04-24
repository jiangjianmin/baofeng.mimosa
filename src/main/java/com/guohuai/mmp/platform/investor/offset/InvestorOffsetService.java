package com.guohuai.mmp.platform.investor.offset;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.component.web.view.PagesRep;
import com.guohuai.mmp.investor.tradeorder.InvestorRedeemTradeOrderService;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity;
import com.guohuai.mmp.investor.tradeorder.OrderDateService;
import com.guohuai.mmp.platform.baseaccount.PlatformBaseAccountEntity;
import com.guohuai.mmp.platform.baseaccount.PlatformBaseAccountService;

/**
 * 平台对投资的轧差
 * 
 * @author Jeffrey Wong
 *
 */
@Service
@Transactional
public class InvestorOffsetService {

	Logger logger = LoggerFactory.getLogger(InvestorOffsetService.class);
	
	
	@Autowired
	private InvestorOffsetDao investorOffsetDao;
	@Autowired
	private PlatformBaseAccountService platformBaseAccountService;
	@Autowired
	private InvestorOffsetExtService investorOffsetExtService;
	@Autowired
	private InvestorRedeemTradeOrderService investorRedeemTradeOrderService;
	@Autowired
	private OrderDateService orderDateService;

	public InvestorOffsetEntity updateEntity(InvestorOffsetEntity offset) {
		return this.investorOffsetDao.save(offset);
	}

	/**
	 * 创建轧差批次
	 * 
	 * @return
	 */
	private InvestorOffsetEntity createEntity(InvestorOffsetEntity offset) {
	
		PlatformBaseAccountEntity platform = platformBaseAccountService.getPlatfromBaseAccount();
		offset.setPlatformBaseAccount(platform);
	
		
		offset.setClearStatus(InvestorOffsetEntity.OFFSET_clearStatus_toClear);
		offset.setCloseStatus(InvestorOffsetEntity.OFFSET_closeStatus_toClose);
		return this.updateEntity(offset);
	}

	/**
	 * 创建快速轧差批次
	 * 
	 * @return
	 */
	private InvestorOffsetEntity createFastOffset() {
		InvestorOffsetEntity fastOffset = new InvestorOffsetEntity();
		fastOffset.setOffsetFrequency(InvestorOffsetEntity.OFFSET_offsetFrequency_fast);
		fastOffset.setOffsetCode(DateUtil.defaultDateHourFormat());
		fastOffset.setOffsetDate(DateUtil.getSqlDate());
		this.createEntity(fastOffset);
		return fastOffset;
	}

	/**
	 * 创建普通轧差批次
	 * 
	 * @return
	 */
	private InvestorOffsetEntity createNormalOffset(Date confirmDate) {
		InvestorOffsetEntity normalOffset = new InvestorOffsetEntity();
		normalOffset.setOffsetFrequency(InvestorOffsetEntity.OFFSET_offsetFrequency_normal);
		normalOffset.setOffsetCode(DateUtil.format(confirmDate, "yyyyMMdd"));
		normalOffset.setOffsetDate(confirmDate);
		this.createEntity(normalOffset);
		return normalOffset;
	}

	/**
	 * 获取最新的快速轧差批次
	 * 
	 * @return
	 */
	public InvestorOffsetEntity getLatestFastOffset(InvestorTradeOrderEntity tradeOrder) {
		InvestorOffsetEntity offset = this.getLatestFastOffset(orderDateService.getRedeemDate(tradeOrder.getProduct(), tradeOrder.getOrderTime()));
		if (tradeOrder.getOrderType().equals(InvestorTradeOrderEntity.TRADEORDER_orderType_fastRedeem)) {
			this.increaseRedeem(offset, tradeOrder.getOrderAmount(), 1);
		}
		return offset;
	}

	/**
	 * 获取投资者快速轧差批次
	 * 
	 * @param confirmDate
	 * @return
	 */
	private InvestorOffsetEntity getLatestFastOffset(Date confirmDate) {
		InvestorOffsetEntity offset = this.investorOffsetDao.getLatestFastOffset(DateUtil.defaultDateHourFormat());
		if (offset == null) {
			offset= this.createFastOffset();
		}
		return offset;
	}
	
	public InvestorOffsetEntity getLatestNormalOffset(InvestorTradeOrderEntity tradeOrder) {
		return this.getLatestNormalOffset(tradeOrder, true);
	}

	/**
	 * 获取最新的普通轧差批次
	 * 
	 * @return
	 */
	public InvestorOffsetEntity getLatestNormalOffset(InvestorTradeOrderEntity tradeOrder, boolean isPositive) {
		BigDecimal orderAmount = tradeOrder.getOrderAmount();
		int step = 1;
		if (!isPositive) {
			orderAmount = orderAmount.negate();
			step = -1;
		}
		InvestorOffsetEntity offset = this.getLatestNormalOffset(orderDateService.getRedeemDate(tradeOrder.getProduct(), tradeOrder.getOrderTime()));
		if (tradeOrder.getOrderType().equals(InvestorTradeOrderEntity.TRADEORDER_orderType_normalRedeem)
				|| tradeOrder.getOrderType().equals(InvestorTradeOrderEntity.TRADEORDER_orderType_incrementRedeem)
				|| tradeOrder.getOrderType().equals(InvestorTradeOrderEntity.TRADEORDER_orderType_cash)
				|| tradeOrder.getOrderType().equals(InvestorTradeOrderEntity.TRADEORDER_orderType_refund)
		        || tradeOrder.getOrderType().equals(InvestorTradeOrderEntity.TRADEORDER_orderType_clearRedeem)) {
			this.increaseRedeem(offset, orderAmount, step);
		}
		return offset;
	}

	/**
	 * 获取投资者普通轧差批次
	 * 
	 * @param confirmDate
	 * @return
	 */
	private InvestorOffsetEntity getLatestNormalOffset(Date confirmDate) {
		InvestorOffsetEntity offset = this.investorOffsetDao.getLatestNormalOffset(DateUtil.defaultFormat(confirmDate));
		if (offset == null){
			offset = this.createNormalOffset(confirmDate);
		}
		
		if (!InvestorOffsetEntity.OFFSET_clearStatus_toClear.equals(offset.getClearStatus())) {
			// error.define[30025]=轧差批次状态异常，非待清算(CODE:30025)
			throw new AMPException(30025);
		}
		return offset;
	}

	/**
	 * 快速轧差，平台对投资者的，只包含投资单和买卖单
	 */
	public void fastOffset() {
		List<InvestorOffsetEntity> offsets = this.investorOffsetDao.getToClearOffset();
		for (InvestorOffsetEntity investorOffsetEntity : offsets) {
			// 清算
			this.investorOffsetExtService.clearDo(investorOffsetEntity);
			// 结算
			this.close(investorOffsetEntity);
		}
	}
	
	public BaseRep closeFastOffset(InvestorOffsetEntity offset) {
		
		this.logger.info("offsetCode={}, offsetType={}, RedeemAmount={}", offset.getOffsetCode(), offset.getOffsetFrequency(), offset.getRedeemAmount());
		if (this.investorOffsetExtService.updateCloseStatus4Lock(offset.getOid(), 
				InvestorOffsetEntity.OFFSET_closeStatus_closing, InvestorOffsetEntity.OFFSET_closeMan_platform) <= 0) {
			throw AMPException.getException(20024);
		}
		BaseRep rep = this.investorOffsetExtService.closeDo(offset);
		return rep;
	}

	/**
	 * 结算，平台对投资者的
	 */
	public BaseRep close(InvestorOffsetEntity offset) {
		this.logger.info("offsetCode={}, offsetType={}, RedeemAmount={}", offset.getOffsetCode(), offset.getOffsetFrequency(), offset.getRedeemAmount());
		if (this.investorOffsetExtService.updateCloseStatus4Lock(offset.getOid(), 
				InvestorOffsetEntity.OFFSET_closeStatus_closing, InvestorOffsetEntity.OFFSET_closeMan_platform) <= 0) {
			throw AMPException.getException(20024);
		}
		BaseRep rep = this.investorOffsetExtService.closeDo(offset);
		return rep;
	}


	/**
	 * 投资人批量代付-回调，普通赎回、快速赎回、还本、付息
	 * 
	 * @param btss
	 * @param btsList
	 */
//	public void batchPayCallback(BatchTradeStatusSync btss, List<BatchTradeStatus> btsList) {
//		for (BatchTradeStatus tradeStatus : btsList) {
//			this.investorOffsetExtService.handleCallbackOrder(tradeStatus);
//		}
//	}

	/**
	 * 增加待清算赎回
	 * 
	 * @param offset
	 * @param investAmount
	 */
	public void increaseRedeem(InvestorOffsetEntity offset, BigDecimal investAmount, int step) {
		this.investorOffsetDao.increaseRedeem(offset.getOid(), investAmount, step);
	}

	public PagesRep<InvestorOffsetQueryRep> mng(Specification<InvestorOffsetEntity> spec, Pageable pageable) {
		Page<InvestorOffsetEntity> cas = this.investorOffsetDao.findAll(spec, pageable);
		PagesRep<InvestorOffsetQueryRep> pagesRep = new PagesRep<InvestorOffsetQueryRep>();
		if (cas != null && cas.getContent() != null && cas.getTotalElements() > 0) {
			for (InvestorOffsetEntity offset : cas) {
				InvestorOffsetQueryRep queryRep = new InvestorOffsetQueryRep();
				queryRep.setOffsetOid(offset.getOid()); // 轧差OID
				queryRep.setOffsetDate(offset.getOffsetDate()); // 轧差日期
				queryRep.setOffsetCode(offset.getOffsetCode()); // 轧差批次
				queryRep.setClearStatus(offset.getClearStatus()); // 清算状态
				queryRep.setClearStatusDisp(offsetStatusEn2Ch(offset.getClearStatus())); // 清算状态Disp
				if (DateUtil.daysBetween(DateUtil.getSqlDate(), offset.getOffsetDate()) < 0) {
					queryRep.setClearTimeArr(false);
				} else {
					queryRep.setClearTimeArr(true);
				}
				queryRep.setCloseStatus(offset.getCloseStatus()); // 结算状态
				queryRep.setCloseStatusDisp(offsetStatusEn2Ch(offset.getCloseStatus())); // 结算状态Disp
				queryRep.setRedeemAmount(offset.getRedeemAmount()); // 赎回金额
				queryRep.setCloseMan(offset.getCloseMan()); // 结算人
				queryRep.setCreateTime(offset.getCreateTime());
				queryRep.setUpdateTime(offset.getUpdateTime());
				pagesRep.add(queryRep);
			}
		}
		pagesRep.setTotal(cas.getTotalElements());
		return pagesRep;
	}

	public InvestorOffsetDetailRep detail(String offsetOid) {
		InvestorOffsetEntity offset = this.findByOid(offsetOid);
		InvestorOffsetDetailRep rep = new InvestorOffsetDetailRep();
		rep.setOffsetDate(offset.getOffsetDate()); // 轧差日期
		rep.setOffsetCode(offset.getOffsetCode()); // 轧差批次
		rep.setClearStatus(offset.getClearStatus()); // 清算状态
		rep.setClearStatusDisp(offsetStatusEn2Ch(offset.getClearStatus())); // 清算状态Disp
		rep.setCloseStatus(offset.getCloseStatus()); // 结算状态
		rep.setCloseStatusDisp(offsetStatusEn2Ch(offset.getCloseStatus())); // 结算状态Disp
		rep.setRedeemAmount(offset.getRedeemAmount()); // 赎回金额
		rep.setCloseMan(offset.getCloseMan()); // 结算人
		rep.setCreateTime(offset.getCreateTime());
		rep.setUpdateTime(offset.getUpdateTime());
		return rep;
	}

	public InvestorOffsetEntity findByOid(String offsetOid) {
		InvestorOffsetEntity offset = this.investorOffsetDao.findOne(offsetOid);
		if (null == offset) {
			throw new AMPException("投资人轧差不存在");
		}
		return offset;
	}

	private String offsetStatusEn2Ch(String offsetStatus) {
		if (InvestorOffsetEntity.OFFSET_clearStatus_toClear.equals(offsetStatus)) {
			return "待清算";
		}else if (InvestorOffsetEntity.OFFSET_clearStatus_clearing.equals(offsetStatus)) {
			return "清算中";
		} else if (InvestorOffsetEntity.OFFSET_clearStatus_cleared.equals(offsetStatus)) {
			return "已清算";
		}else if (InvestorOffsetEntity.OFFSET_closeStatus_toClose.equals(offsetStatus)) {
			return "待结算";
		} else if (InvestorOffsetEntity.OFFSET_closeStatus_closing.equals(offsetStatus)) {
			return "结算中";
		} else if (InvestorOffsetEntity.OFFSET_closeStatus_closeSubmitFailed.equals(offsetStatus)) {
			return "结算申请失败";
		}else if (InvestorOffsetEntity.OFFSET_closeStatus_closePayFailed.equals(offsetStatus)) {
			return "结算支付失败";
		} else if (InvestorOffsetEntity.OFFSET_closeStatus_closed.equals(offsetStatus)) {
			return "已结算";
		}
		return "--";
	}

	public List<InvestorOffsetEntity> getOverdueInvestorOffset(Date curDate) {
		return this.investorOffsetDao.getOverdueInvestorOffset(curDate);
	}

	public void batchUpdate(List<InvestorOffsetEntity> iOffsetList) {
		investorOffsetDao.save(iOffsetList);
		
	}

	public void clear(InvestorOffsetEntity offset) {
		if (!offset.getOffsetFrequency().equals(InvestorOffsetEntity.OFFSET_offsetFrequency_normal)) {
			// error.define[20021]=清算状态异常(CODE:20021)
			throw AMPException.getException(20021);
		}
		
		if (DateUtil.daysBetween(DateUtil.getSqlDate(), offset.getOffsetDate()) < 0) {
			// error.define[30062]=清算时间异常(CODE:30062)
			throw AMPException.getException(30062);
		}
		
		if (0 != this.investorOffsetDao.beforeOffsetDate(offset.getOffsetDate())) {
			// error.define[30063]=请优先处理之前的轧差批次(CODE:30063)
			throw AMPException.getException(30063);
		}
		this.investorOffsetExtService.clearDo(offset);
		
	}
}
