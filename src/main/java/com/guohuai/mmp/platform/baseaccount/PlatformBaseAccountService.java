package com.guohuai.mmp.platform.baseaccount;

import java.math.BigDecimal;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.basic.common.SeqGenerator;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountService;
import com.guohuai.mmp.platform.baseaccount.statistics.PlatformStatisticsEntity;
import com.guohuai.mmp.platform.baseaccount.statistics.PlatformStatisticsService;
import com.guohuai.mmp.platform.superacc.order.PlatformSuperaccOrderEntity;
import com.guohuai.mmp.platform.superacc.order.PlatformSuperaccOrderService;
import com.guohuai.mmp.sys.CodeConstants;

@Service
@Transactional
public class PlatformBaseAccountService {

	Logger logger = LoggerFactory.getLogger(PlatformBaseAccountService.class);

	@Autowired
	private PlatformBaseAccountDao platformBaseAccountDao;
	@Autowired
	private PlatformStatisticsService platformStatisticsService;
	@Autowired
	private InvestorBaseAccountService investorBaseAccountService;
	@Autowired
	private SeqGenerator seqGenerator;
	@Autowired
	private PlatformSuperaccOrderService platformSuperaccOrderService;

	
	public PlatformBaseAccountEntity getPlatfromBaseAccount() {
		PlatformBaseAccountEntity entity = this.platformBaseAccountDao.findByStatus(PlatformBaseAccountEntity.PLATFORMBASE_accountStatus_normal);
		if (null == entity) {
			throw new AMPException("处于正常状态的平台账户不存在");
		}
		return entity;
	}
	
	
	public BaseRep payMoney(BigDecimal amount) {
		this.borrowFromSuperman(amount);
		investorBaseAccountService.payToPlatform(amount);
		
		PlatformSuperaccOrderEntity orderEntity = new PlatformSuperaccOrderEntity();
		orderEntity.setPlatformBaseAccount(this.getPlatfromBaseAccount());
		orderEntity.setInvestorBaseAccount(this.investorBaseAccountService.getSuperInvestor());
		orderEntity.setOrderCode(this.seqGenerator.next(CodeConstants.Superacc_order));
		orderEntity.setOrderType(PlatformSuperaccOrderEntity.ORDER_orderType_return);
		orderEntity.setOrderAmount(amount);
		orderEntity.setOrderStatus(PlatformSuperaccOrderEntity.ORDER_orderStatus_paySuccess);
		orderEntity.setCompleteTime(DateUtil.getSqlCurrentDate());
		orderEntity.setRelatedAcc(PlatformSuperaccOrderEntity.ORDER_relatedAcc_superAcc);
		platformSuperaccOrderService.saveEntity(orderEntity);
		return new BaseRep();
	}
	
	
	public BaseRep borrowMoney(BigDecimal amount) {
		
		investorBaseAccountService.borrowFromPlatform(amount);
		this.payToSuperman(amount);
		
		PlatformSuperaccOrderEntity orderEntity = new PlatformSuperaccOrderEntity();
		orderEntity.setPlatformBaseAccount(this.getPlatfromBaseAccount());
		orderEntity.setInvestorBaseAccount(this.investorBaseAccountService.getSuperInvestor());
		orderEntity.setOrderCode(this.seqGenerator.next(CodeConstants.Superacc_order));
		orderEntity.setOrderType(PlatformSuperaccOrderEntity.ORDER_orderType_borrow);
		orderEntity.setOrderAmount(amount);
		orderEntity.setOrderStatus(PlatformSuperaccOrderEntity.ORDER_orderStatus_paySuccess);
		orderEntity.setCompleteTime(DateUtil.getSqlCurrentDate());
		orderEntity.setRelatedAcc(PlatformSuperaccOrderEntity.ORDER_relatedAcc_superAcc);
		platformSuperaccOrderService.saveEntity(orderEntity);
		
		return new BaseRep();
	}
	
	public int borrowFromSuperman(BigDecimal amount) {
		int i = this.platformBaseAccountDao.borrowFromSuperman(amount);
		if (i < 1) {
			//error.define[30052]=借款失败(CODE:30051)
			throw new AMPException(30052);
		}
		return i;
	}
	
	public int payToSuperman(BigDecimal amount) {
		int i = this.platformBaseAccountDao.payToSuperman(amount);
		if (i < 1) {
			//error.define[30053]=还款失败(CODE:30053)
			throw new AMPException(30053);
		}
		return i;
	}


	public PlatformBaseAccountRep deta() {
		PlatformBaseAccountEntity entity = this.getPlatfromBaseAccount();
		PlatformStatisticsEntity st = this.platformStatisticsService.findByPlatformBaseAccount();

		PlatformBaseAccountRep rep = PlatformBaseAccountRep.builder()
				.balance(entity.getBalance()) //余额
				.superAccBorrowAmount(entity.getSuperAccBorrowAmount())
				.status(entity.getStatus())
				.statusDisp(statusEn2Ch(entity.getStatus()))
				.totalTradeAmount(st.getTotalTradeAmount()) //累计交易总额
				.totalLoanAmount(st.getTotalLoanAmount()) //累计借款总额
				.totalReturnAmount(st.getTotalReturnAmount()) //累计还款总额
				.totalInterestAmount(st.getTotalInterestAmount()) //累计付息总额
				.investorTotalDepositAmount(st.getInvestorTotalDepositAmount()) //投资人充值总额
				.investorTotalWithdrawAmount(st.getInvestorTotalWithdrawAmount()) //投资人提现总额
				.publisherTotalDepositAmount(st.getPublisherTotalDepositAmount())
				.publisherTotalWithdrawAmount(st.getPublisherTotalWithdrawAmount())
				
				.registerAmount(st.getRegisterAmount()) //注册投资人数
				.investorAmount(st.getInvestorAmount()) //投资人数
				.investorHoldAmount(st.getInvestorHoldAmount()) //持仓人数
				.overdueTimes(st.getOverdueTimes()) //逾期次数
				.productAmount(st.getProductAmount()) //发行产品数
				.closedProductAmount(st.getClosedProductAmount()) //已结算产品数
				.toCloseProductAmount(st.getToCloseProductAmount()) //待结算产品数
				.onSaleProductAmount(st.getOnSaleProductAmount()) //在售产品数
				.publisherAmount(st.getPublisherAmount()) //发行人数
				.verifiedInvestorAmount(st.getVerifiedInvestorAmount()) //实名投资人数
				.activeInvestorAmount(st.getActiveInvestorAmount()) //活跃投资人数
				.updateTime(st.getUpdateTime())
				.createTime(st.getCreateTime())
				.build();
		return rep;
	}

	private String statusEn2Ch(String status) {
		if (PlatformBaseAccountEntity.PLATFORMBASE_accountStatus_normal.equals(status)) {
			return "正常";
		}
		if (PlatformBaseAccountEntity.PLATFORMBASE_accountStatus_forbiden.equals(status)) {
			return "禁用";
		}
		return status;
	}

	
	public int updateBalanceMinusMinus(BigDecimal orderAmount) {
		return this.platformBaseAccountDao.updateBalanceMinusMinus(orderAmount);
	}
	
	
	public int updateBalancePlusPlus(BigDecimal orderAmount) {
		return this.platformBaseAccountDao.updateBalancePlusPlus(orderAmount);
		
	}


	public PlatformBaseAccountEntity saveEntity(PlatformBaseAccountEntity entity) {
		return this.platformBaseAccountDao.save(entity);
	}


}
