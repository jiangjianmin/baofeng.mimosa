package com.guohuai.mmp.investor.tradeorder.check;

import java.math.BigDecimal;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.mmp.investor.abandonlog.AbandonLogService;
import com.guohuai.mmp.investor.tradeorder.InvestorRedeemTradeOrderService;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderService;
import com.guohuai.mmp.investor.tradeorder.RedeemTradeOrderReq;
import com.guohuai.mmp.platform.finance.modifyorder.ModifyOrderNewService;
import com.guohuai.mmp.platform.finance.result.PlatformFinanceCompareDataResultNewService;
import com.guohuai.mmp.platform.payment.OrderNotifyReq;
import com.guohuai.mmp.platform.payment.PaymentServiceImpl;
import com.guohuai.mmp.platform.payment.RedeemPayRequest;

@Service
@Transactional
public class InvestorRefundTradeOrderService {
	Logger logger = LoggerFactory.getLogger(InvestorRefundTradeOrderService.class);
	@Autowired
	private InvestorTradeOrderService investorTradeOrderService;
	@Autowired
	private PaymentServiceImpl paymentServiceImpl;
	@Autowired
	private InvestorRedeemTradeOrderService investorRedeemTradeOrderService;
	@Autowired
	private PlatformFinanceCompareDataResultNewService platformFinanceCompareDataResultNewService;
	@Autowired
	private AbandonLogService abandonLogService;
	@Autowired
	private InvestorRefundTradeOrderRequireNewService investorRefundTradeOrderRequireNewService;
	@Autowired
	private ModifyOrderNewService modifyOrderNewService;
	public void refundPay(InvestorTradeOrderEntity orderEntity) {

		RedeemPayRequest req = new RedeemPayRequest();
		req.setUserOid(orderEntity.getInvestorBaseAccount().getMemberId());
		req.setOrderNo(orderEntity.getOrderCode());
		req.setAmount(orderEntity.getOrderAmount());
		req.setFee(BigDecimal.ZERO);
		req.setProvince(orderEntity.getProvince());
		req.setCity(orderEntity.getCity());
		req.setOrderTime(DateUtil.format(orderEntity.getOrderTime(), DateUtil.fullDatePattern));
		// 支付
		this.paymentServiceImpl.redeemPay(req);
	}

	public boolean refundCallback(OrderNotifyReq ireq) {
		
		String originalOrderCode = abandonLogService.getOriginalOrderCodeByRefundOrderCode(ireq.getOrderCode());
		
		investorTradeOrderService.updateOrderStatus4Abandon(originalOrderCode);
		
		investorTradeOrderService.updateOrderStatus4Refund(ireq.getOrderCode());
		
		platformFinanceCompareDataResultNewService.updateDealStatusDealtByOrderCode(originalOrderCode);
		modifyOrderNewService.updateDealStatusDealtByOrderCode(originalOrderCode);
		return true;
	}

	public BaseRep refund(RefundTradeOrderReq req) {
		BaseRep rep = new BaseRep();
		
		InvestorTradeOrderEntity orderEntity = this.investorTradeOrderService.findByOrderCode(req.getOrderCode());
		
		
		
		investorRefundTradeOrderRequireNewService.refundOrder(req.getOrderCode());
		
		try {
			RedeemTradeOrderReq refundReq = new RedeemTradeOrderReq();
			refundReq.setUid(orderEntity.getInvestorBaseAccount().getUserOid());
			refundReq.setProductOid(orderEntity.getProduct().getOid());
			refundReq.setOrderAmount(req.getOrderAmount());
			
			InvestorTradeOrderEntity refundOrderEntity = investorRedeemTradeOrderService.createRefundTradeOrder(refundReq);
			refundOrderEntity.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_refunding);
			refundOrderEntity.setRelateOid(orderEntity.getOid());
			this.investorTradeOrderService.saveEntity(refundOrderEntity);
			
			this.refundPay(refundOrderEntity);
			
			this.abandonLogService.create(orderEntity, refundOrderEntity);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			rep.setErrorCode(BaseRep.ERROR_CODE);
			rep.setErrorMessage(AMPException.getStacktrace(e));
		}
		
		return rep;
	}

}
