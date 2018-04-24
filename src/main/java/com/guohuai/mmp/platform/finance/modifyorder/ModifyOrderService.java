package com.guohuai.mmp.platform.finance.modifyorder;

import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.guohuai.ams.product.ProductDao;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.component.web.view.PagesRep;
import com.guohuai.mmp.investor.tradeorder.InvestorInvestTradeOrderExtService;
import com.guohuai.mmp.investor.tradeorder.check.AbandonReq;
import com.guohuai.mmp.investor.tradeorder.check.CheckOrderReq;
import com.guohuai.mmp.investor.tradeorder.check.InvestorAbandonTradeOrderService;
import com.guohuai.mmp.investor.tradeorder.check.InvestorRefundTradeOrderService;
import com.guohuai.mmp.investor.tradeorder.check.RefundTradeOrderReq;
import com.guohuai.mmp.platform.finance.check.PlatformFinanceCheckDao;
import com.guohuai.mmp.platform.finance.result.PlatformFinanceCompareDataResultEntity;
import com.guohuai.mmp.platform.finance.result.PlatformFinanceCompareDataResultNewService;


@Service
@Transactional
public class ModifyOrderService {
	Logger logger = LoggerFactory.getLogger(ModifyOrderService.class);
	@Autowired
	ModifyOrderNewService modifyOrderNewService;
	@Autowired
	ModifyOrderDao modifyOrderDao;
	@Autowired
	PlatformFinanceCheckDao platformFinanceCheckDao;
	@Autowired
	ProductDao productDao;
	@Autowired
	PlatformFinanceCompareDataResultNewService platformFinanceCompareDataResultNewService;
	@Autowired
	private InvestorAbandonTradeOrderService investorAbandonTradeOrderService;
	@Autowired
	private InvestorRefundTradeOrderService investorRefundTradeOrderService;
	@Autowired
	private InvestorInvestTradeOrderExtService investorInvestTradeOrderExtService;
	
	public PagesRep<ModifyOrderRep> modifyOrderList(Specification<ModifyOrderEntity> spec, Pageable pageable) {
		Page<ModifyOrderEntity> list = this.modifyOrderDao.findAll(spec, pageable);
		PagesRep<ModifyOrderRep> pagesRep = new PagesRep<ModifyOrderRep>();
		for (ModifyOrderEntity entity : list) {
			ModifyOrderRep rep = new ModifyOrderRep();
			rep.setOid(entity.getOid());
			rep.setCheckCode(entity.getFinanceCheck().getCheckCode());
			rep.setApproveStatus(entity.getApproveStatus());
			rep.setInvestorOid(entity.getInvestorOid());
			rep.setOpType(entity.getOpType());
			rep.setOrderAmount(entity.getOrderAmount());
			rep.setOrderCode(entity.getOrderCode());
			rep.setPostmodifyStatus(entity.getPostmodifyStatus());
			rep.setPremodifyStatus(entity.getPremodifyStatus());
			rep.setDealStatus(entity.getDealStatus());
			rep.setResultOid(entity.getResultOid());
			rep.setProductOid(entity.getProductOid());
			if(null != entity.getProductOid()){
				rep.setProductName(productDao.findOne(entity.getProductOid()).getName());
			}
			
			rep.setTradeType(entity.getTradeType());
			rep.setReason(entity.getReason());
			rep.setOperator(entity.getOperator());
			rep.setCreateTime(entity.getCreateTime());
			rep.setUpdateTime(entity.getUpdateTime());
			pagesRep.add(rep);
		}
		pagesRep.setTotal(list.getTotalElements());
		return pagesRep;
	}
	public BaseRep saveModifyOrder(ModifyOrderReq req) {
		BaseRep rep=new BaseRep();
		try{
			ModifyOrderEntity entity =new ModifyOrderEntity();
			entity.setOperator(req.getOperator());
			entity.setOrderAmount(req.getOrderAmount());
			entity.setFinanceCheck(platformFinanceCheckDao.findOne(req.getCheckOid()));
			entity.setInvestorOid(req.getInvestorOid());
			entity.setOpType(req.getOpType());
			entity.setTradeType(req.getTradeType());
			entity.setOrderCode(req.getOrderCode());
			entity.setProductOid(req.getProductOid());
			entity.setApproveStatus(ModifyOrderEntity.APPROVESTATUS_TOAPPROVE);
			entity.setDealStatus(ModifyOrderEntity.DEALSTATUS_TODEAL);
			entity.setResultOid(req.getResultOid());
			entity.setPremodifyStatus(req.getPremodifyStatus());
			entity.setCreateTime(DateUtil.getSqlCurrentDate());
			entity.setUpdateTime(DateUtil.getSqlCurrentDate());
			modifyOrderDao.save(entity);
			platformFinanceCompareDataResultNewService.updateDealStatusByOid(req.getResultOid(), PlatformFinanceCompareDataResultEntity.DEALSTATUS_DEALING);
		}catch(AMPException e){
			e.printStackTrace();
			rep.setErrorCode(-1);
			rep.setErrorMessage("新增失败！");
		}
		return rep;
	}
	public BaseRep modifyOrderApprove(String oid,String resultOid, String approveStatus, String operator) {
		BaseRep rep=new BaseRep();
		try{
			modifyOrderNewService.modifyOrderApprove(oid,approveStatus,operator);
			if(ModifyOrderEntity.APPROVESTATUS_REFUSED.equals(approveStatus)){
				platformFinanceCompareDataResultNewService.updateDealStatusByOid(resultOid, PlatformFinanceCompareDataResultEntity.DEALSTATUS_DEALT);
			}else{
				ModifyOrderEntity entity=modifyOrderDao.findOne(oid);
				//补帐
				if(entity.getOpType().equals(ModifyOrderEntity.OPTYPE_FIXORDER)){
					//补投资单
					if(entity.getTradeType().equals(ModifyOrderEntity.TRADETYPE_INVEST)){
						CheckOrderReq req=new CheckOrderReq();
						req.setProductOid(entity.getProductOid());
						req.setMoneyVolume(entity.getOrderAmount());
						req.setOrderCode(entity.getOrderCode());
						req.setMemberId(entity.getInvestorOid());
						investorInvestTradeOrderExtService.resumitInvestOrder(req);
					}else{//补赎回单
						CheckOrderReq req=new CheckOrderReq();
						req.setMemberId(entity.getInvestorOid());
						req.setProductOid(entity.getProductOid());
						req.setOrderCode(entity.getOrderCode());
						req.setMoneyVolume(entity.getOrderAmount());
						investorInvestTradeOrderExtService.resumitRedeemOrder(req);
					}
				}else if(entity.getOpType().equals(ModifyOrderEntity.OPTYPE_REFUND)){//退款
					RefundTradeOrderReq req =new RefundTradeOrderReq();
					req.setOrderAmount(entity.getOrderAmount());
					req.setOrderCode(entity.getOrderCode());
					investorRefundTradeOrderService.refund(req);
				}else if(entity.getOpType().equals(ModifyOrderEntity.OPTYPE_DISCARDHOLD) || 
						entity.getOpType().equals(ModifyOrderEntity.OPTYPE_DISCARDREFUND)){//平仓或者平仓退款
					AbandonReq req =new AbandonReq();
					req.setOrderAmount(entity.getOrderAmount());
					req.setOrderCode(entity.getOrderCode());
					investorAbandonTradeOrderService.abandon(req);
				}
				platformFinanceCompareDataResultNewService.updateDealStatusByOid(resultOid, PlatformFinanceCompareDataResultEntity.DEALSTATUS_DEALT);
			}
		}catch(AMPException e){
			e.printStackTrace();
			rep.setErrorCode(-1);
			rep.setErrorMessage("审核操作失败！");
		}
		return rep;
	}
	public BaseRep modifyOrderBatchApprove(List<String> oids, String operator) {
		List<ModifyOrderEntity> queryList=modifyOrderDao.findModifyOrderByOids(oids);
		for(ModifyOrderEntity entity : queryList){
			//修改审核状态
			modifyOrderDao.modifyOrderApprove(entity.getOid(),ModifyOrderEntity.APPROVESTATUS_PASS,operator);
			//补帐
			if(entity.getOpType().equals(ModifyOrderEntity.OPTYPE_FIXORDER)){
				//补投资单
				if(entity.getTradeType().equals(ModifyOrderEntity.TRADETYPE_INVEST)){
					CheckOrderReq req=new CheckOrderReq();
					req.setProductOid(entity.getProductOid());
					req.setMoneyVolume(entity.getOrderAmount());
					req.setOrderCode(entity.getOrderCode());
					req.setMemberId(entity.getInvestorOid());
					investorInvestTradeOrderExtService.resumitInvestOrder(req);
				}else{//补赎回单
					CheckOrderReq req=new CheckOrderReq();
					req.setMemberId(entity.getInvestorOid());
					req.setProductOid(entity.getProductOid());
					req.setOrderCode(entity.getOrderCode());
					req.setMoneyVolume(entity.getOrderAmount());
					investorInvestTradeOrderExtService.resumitRedeemOrder(req);
				}
			}else if(entity.getOpType().equals(ModifyOrderEntity.OPTYPE_REFUND)){//退款
				RefundTradeOrderReq req =new RefundTradeOrderReq();
				req.setOrderAmount(entity.getOrderAmount());
				req.setOrderCode(entity.getOrderCode());
				investorRefundTradeOrderService.refund(req);
			}else if(entity.getOpType().equals(ModifyOrderEntity.OPTYPE_DISCARDHOLD) || 
					entity.getOpType().equals(ModifyOrderEntity.OPTYPE_DISCARDREFUND)){//平仓或者平仓退款
				AbandonReq req =new AbandonReq();
				req.setOrderAmount(entity.getOrderAmount());
				req.setOrderCode(entity.getOrderCode());
				investorAbandonTradeOrderService.abandon(req);
			}
			platformFinanceCompareDataResultNewService.updateDealStatusByOid(entity.getResultOid(), PlatformFinanceCompareDataResultEntity.DEALSTATUS_DEALT);
		}
		return new BaseRep();
	}
}
