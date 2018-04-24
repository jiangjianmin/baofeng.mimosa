package com.guohuai.mmp.investor.tradeorder.check;

import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductService;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.mmp.investor.orderlog.OrderLogEntity;
import com.guohuai.mmp.investor.orderlog.OrderLogService;
import com.guohuai.mmp.investor.tradeorder.InvestorRedeemTradeOrderService;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderDao;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderService;
import com.guohuai.mmp.investor.tradeorder.OrderDateService;
import com.guohuai.mmp.investor.tradeorder.RefuseRep;
import com.guohuai.mmp.platform.finance.modifyorder.ModifyOrderNewService;
import com.guohuai.mmp.platform.investor.offset.InvestorOffsetService;
import com.guohuai.mmp.platform.publisher.offset.PublisherOffsetService;
import com.guohuai.mmp.platform.publisher.product.offset.ProductOffsetService;
import com.guohuai.mmp.publisher.hold.PublisherHoldService;
import com.guohuai.mmp.serialtask.AbandonParams;
import com.guohuai.mmp.serialtask.SerialTaskEntity;
import com.guohuai.mmp.serialtask.SerialTaskReq;
import com.guohuai.mmp.serialtask.SerialTaskRequireNewService;
import com.guohuai.mmp.serialtask.SerialTaskService;

@Service
@Transactional
public class InvestorAbandonTradeOrderService  {
	Logger logger = LoggerFactory.getLogger(InvestorAbandonTradeOrderService.class);
	@Autowired
	private InvestorTradeOrderDao investorTradeOrderDao;
	@Autowired
	private PublisherHoldService publisherHoldService;
	@Autowired
	private OrderLogService orderLogService;
	@Autowired
	private PublisherOffsetService publisherOffsetService;
	@Autowired
	private ProductOffsetService productOffsetService;
	@Autowired
	private ProductService productService;
	@Autowired
	private InvestorTradeOrderService investorTradeOrderService;
	@Autowired
	private InvestorOffsetService investorOffsetService;
	@Autowired
	private InvestorRedeemTradeOrderService investorRedeemTradeOrderService;
	@Autowired
	private InvestorAbandonTradeOrderRequireNewService investorAbandonTradeOrderRequireNewService;
	@Autowired
	private SerialTaskService serialTaskService;
	@Autowired
	private SerialTaskRequireNewService serialTaskRequireNewService;
	@Autowired
	private ModifyOrderNewService modifyOrderNewService;
	@Autowired
	private OrderDateService orderDateService;
	
	public BaseRep abandon(AbandonReq req) {

		abandonOrder(req);
		AbandonParams params = new AbandonParams();
		params.setOrderCode(req.getOrderCode());
		params.setOrderAmount(req.getOrderAmount());
		SerialTaskReq<AbandonParams> sreq = new SerialTaskReq<AbandonParams>();
		sreq.setTaskCode(SerialTaskEntity.TASK_taskCode_abandon);
		sreq.setTaskParams(params);

		serialTaskService.createSerialTask(sreq);

		return new BaseRep();
	}
	
	public void abandonOrder(AbandonReq req) {
		
		InvestorTradeOrderEntity orderEntity = investorTradeOrderService.findByOrderCode(req.getOrderCode());
		if (null != req.getOrderAmount() && orderEntity.getOrderAmount().compareTo(req.getOrderAmount()) < 0) {
			// error.define[30080]=退款金额不能大于原有订单金额(CODE:30080)
			throw new AMPException(30080);
		}
		
		int  i = 0;
		if (Product.TYPE_Producttype_01.equals(orderEntity.getProduct().getType().getOid())) {
			if (Product.STATE_Cleared.equals(orderEntity.getProduct().getState()) ||
					Product.STATE_Durationend.equals(orderEntity.getProduct().getState())) {
				// error.define[30081]=存续期结束或已清盘产品不能废单(CODE:30081)
				throw new AMPException(30081);
			}
			i = this.investorTradeOrderDao.abandonTnOrder(req.getOrderCode());
		} else {
			i = this.investorTradeOrderDao.abandonT0Order(req.getOrderCode());
		}
		if (i < 1) {
			// error.define[30070]=废单失败(CODE:30070)
			throw new AMPException(30070);
		}
		
		modifyOrderNewService.updateDealStatusDealingByOrderCode(req.getOrderCode());
	}
	
	
	public void abandonDo(AbandonParams params, String taskOid) {
		BaseRep rep = new BaseRep();

		InvestorTradeOrderEntity orderEntity = investorTradeOrderService.findByOrderCode(params.getOrderCode());
		
		try {
			if (Product.TYPE_Producttype_01.equals(orderEntity.getProduct().getType().getOid())) {
				investorAbandonTradeOrderRequireNewService.abandonTnOrder(params);
			} else {
				investorAbandonTradeOrderRequireNewService.abandonT0Order(params);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			rep.setErrorCode(BaseRep.ERROR_CODE);
			rep.setErrorMessage(AMPException.getStacktrace(e));
		} 
		OrderLogEntity orderLog = new OrderLogEntity();
		orderLog.setOrderType(orderEntity.getOrderType());
		orderLog.setTradeOrderOid(orderEntity.getOrderCode());
		orderLog.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_abandoned);
		orderLog.setErrorCode(rep.getErrorCode());
		orderLog.setErrorMessage(rep.getErrorMessage());
		this.orderLogService.create(orderLog);
		
		serialTaskRequireNewService.updateTime(taskOid);
	}
	
	
	public RefuseRep refuse(String tradeOrderOid) {
		logger.info("refuse tradeOrderOid:{}", tradeOrderOid);
		RefuseRep rep = new RefuseRep();
		
		try {
			
			InvestorTradeOrderEntity tradeOrder = investorTradeOrderService.findByOrderCode(tradeOrderOid);
			
			int i = this.investorTradeOrderDao.refuseOrder(tradeOrderOid);
			if (i > 0) {
				// 产品单日赎回上限 产品处理(非当日的订单，不扣份额)
				if (DateUtil.isEqualDay(tradeOrder.getOrderTime())) {
					this.productService.update4RedeemRefuse(tradeOrder.getProduct(), tradeOrder.getOrderVolume());
				}
				// 仓位处理
				this.publisherHoldService.redeem4Refuse(tradeOrder);
				// 当日赎回累计
				if (DateUtil.isEqualDay(tradeOrder.getOrderTime())) {
					this.publisherHoldService.redeem4RefuseOfDayRedeemVolume(tradeOrder);
				}

				publisherOffsetService.getLatestOffset(tradeOrder,
						orderDateService.getRedeemDate(tradeOrder.getProduct(), tradeOrder.getOrderTime()), false);
				productOffsetService.offset(tradeOrder.getPublisherBaseAccount(), tradeOrder, false);
				this.investorOffsetService.getLatestNormalOffset(tradeOrder, false);
				rep.setSuccess(true);
			} 
			
		} catch (AMPException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			rep.setSuccess(false);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			rep.setSuccess(false);
		}
		InvestorTradeOrderEntity tradeOrder = investorTradeOrderService.findByOrderCode(tradeOrderOid);
		if (rep.isSuccess()) {
			OrderLogEntity orderLog = new OrderLogEntity();
			orderLog.setOrderType(tradeOrder.getOrderType());
			orderLog.setTradeOrderOid(tradeOrder.getOrderCode());
			orderLog.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_refused);
			this.orderLogService.create(orderLog);
		} else {
			// error.define[20018]=废单状态异常(CODE:20018)
			throw AMPException.getException(20018);
		}
		return rep;
	}
	
	public void isOpenRedeemConfirm(Product product) {
		logger.info("isOpenRedeemConfirm productOid:{}", product.getOid());
		if (Product.NO.equals(product.getIsOpenRedeemConfirm())) {
			String lastOid = "0";
			while (true) {
				List<InvestorTradeOrderEntity> orderList = this.investorTradeOrderDao.findByProduct(product.getOid(), lastOid);
				if (orderList.isEmpty()) {
					break;
				}
				for (InvestorTradeOrderEntity order : orderList) {
					
					refuse(order.getOrderCode());
					lastOid = order.getOid();
				}
			}
		} else {
			logger.info("打开赎回确认，不作任何处理.");
		}
		
		
	}


	
}
