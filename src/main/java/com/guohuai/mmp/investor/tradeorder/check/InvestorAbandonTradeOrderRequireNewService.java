package com.guohuai.mmp.investor.tradeorder.check;

import java.math.BigDecimal;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.ams.product.ProductService;
import com.guohuai.component.util.DateUtil;
import com.guohuai.mmp.investor.abandonlog.AbandonLogService;
import com.guohuai.mmp.investor.tradeorder.InvestorRedeemTradeOrderService;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderService;
import com.guohuai.mmp.investor.tradeorder.OrderDateService;
import com.guohuai.mmp.investor.tradeorder.RedeemTradeOrderReq;
import com.guohuai.mmp.platform.finance.modifyorder.ModifyOrderNewService;
import com.guohuai.mmp.platform.finance.result.PlatformFinanceCompareDataResultNewService;
import com.guohuai.mmp.platform.publisher.offset.PublisherOffsetService;
import com.guohuai.mmp.platform.publisher.product.offset.ProductOffsetService;
import com.guohuai.mmp.publisher.hold.PublisherHoldService;
import com.guohuai.mmp.serialtask.AbandonParams;

@Service
@Transactional
public class InvestorAbandonTradeOrderRequireNewService  {
	Logger logger = LoggerFactory.getLogger(InvestorAbandonTradeOrderRequireNewService.class);
	@Autowired
	private AbandonLogService abandonLogService;
	@Autowired
	private PublisherHoldService publisherHoldService;
	@Autowired
	private PublisherOffsetService publisherOffsetService;
	@Autowired
	private ProductOffsetService productOffsetService;
	@Autowired
	private ProductService productService;
	@Autowired
	private InvestorTradeOrderService investorTradeOrderService;
	@Autowired
	private InvestorRedeemTradeOrderService investorRedeemTradeOrderService;
	@Autowired
	private OrderDateService orderDateService;
	@Autowired
	private InvestorRefundTradeOrderService investorRefundTradeOrderService;
	@Autowired
	private PlatformFinanceCompareDataResultNewService platformFinanceCompareDataResultNewService;
	@Autowired
	private ModifyOrderNewService modifyOrderNewService;
	
	@Transactional(value = TxType.REQUIRES_NEW)
	public void abandonT0Order(AbandonParams req) {
		InvestorTradeOrderEntity orderEntity = investorTradeOrderService.findByOrderCode(req.getOrderCode());

		/** 解锁产品锁定已募份额 */
		this.productService.update4T0InvestAbandon(orderEntity);
		/** 解锁SPV锁定份额 */
		this.publisherHoldService.updateSpvHold4T0InvestAbandon(orderEntity);
		/** 扣除投资人最大持仓份额 */
		this.publisherHoldService.updateMaxHold4InvestAbandon(orderEntity.getInvestorBaseAccount(),
				orderEntity.getProduct(), orderEntity.getOrderVolume());
		// SPV轧差
		publisherOffsetService.getLatestOffset(orderEntity, this.orderDateService.getConfirmDate(orderEntity), false);
		// 产品轧差
		productOffsetService.offset(orderEntity.getPublisherBaseAccount(), orderEntity, false);

		// 合仓处理
		this.publisherHoldService.abandon4T0Invest(orderEntity);

		// 当日投资累计
		if (DateUtil.isEqualDay(orderEntity.getOrderTime())) {
			this.publisherHoldService.invest4AbandonOfDayInvestVolume(orderEntity);
		}
		
		refund(req, orderEntity);
	}
	
	
	@Transactional(value = TxType.REQUIRES_NEW)
	public void abandonTnOrder(AbandonParams req) {
		InvestorTradeOrderEntity orderEntity = investorTradeOrderService.findByOrderCode(req.getOrderCode());

		/** 解锁产品锁定已募份额 */
		this.productService.update4TnInvestAbandon(orderEntity);
		/** 解锁SPV锁定份额 */
		this.publisherHoldService.updateSpvHold4TnInvestAbandon(orderEntity);
		/** 扣除投资人最大持仓份额 */
		this.publisherHoldService.updateMaxHold4InvestAbandon(orderEntity.getInvestorBaseAccount(),
				orderEntity.getProduct(), orderEntity.getOrderVolume());
		// 合仓处理
		this.publisherHoldService.abandon4TnInvest(orderEntity);

		// 当日投资累计
		if (DateUtil.isEqualDay(orderEntity.getOrderTime())) {
			this.publisherHoldService.invest4AbandonOfDayInvestVolume(orderEntity);
		}
		
		refund(req, orderEntity);
		
	}


	public void refund(AbandonParams req, InvestorTradeOrderEntity orderEntity) {
		if (null != req.getOrderAmount() && req.getOrderAmount().compareTo(BigDecimal.ZERO) > 0) {
			RedeemTradeOrderReq refundReq = new RedeemTradeOrderReq();
			refundReq.setUid(orderEntity.getInvestorBaseAccount().getUserOid());
			refundReq.setProductOid(orderEntity.getProduct().getOid());
			refundReq.setOrderAmount(req.getOrderAmount());
			
			InvestorTradeOrderEntity refundOrderEntity = investorRedeemTradeOrderService.createRefundTradeOrder(refundReq);
			refundOrderEntity.setRelateOid(orderEntity.getOid());
			refundOrderEntity.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_refundStatus_refunding);
			this.investorTradeOrderService.saveEntity(refundOrderEntity);
			
			investorRefundTradeOrderService.refundPay(refundOrderEntity);
			this.abandonLogService.create(orderEntity, refundOrderEntity);
		} else {
			platformFinanceCompareDataResultNewService.updateDealStatusDealtByOrderCode(req.getOrderCode());
			modifyOrderNewService.updateDealStatusDealtByOrderCode(req.getOrderCode());
			this.abandonLogService.create(orderEntity);
			orderEntity.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_abandoned);
			this.investorTradeOrderService.saveEntity(orderEntity);
		}
	}
	
	
	
	
	
}
