package com.guohuai.mmp.investor.tradeorder;

import java.sql.Timestamp;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductService;
import com.guohuai.basic.common.SeqGenerator;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DecimalUtil;
import com.guohuai.mmp.platform.investor.offset.InvestorOffsetService;
import com.guohuai.mmp.platform.publisher.offset.PublisherOffsetService;
import com.guohuai.mmp.platform.publisher.product.offset.ProductOffsetService;
import com.guohuai.mmp.publisher.hold.PublisherHoldEntity;
import com.guohuai.mmp.publisher.hold.PublisherHoldService;
import com.guohuai.mmp.sys.CodeConstants;

@Service
@Transactional
public class InvestorRepayInterestTradeOrderService {
	@Autowired
	private InvestorTradeOrderDao investorTradeOrderDao;
	@Autowired
	private ProductService productService;
	@Autowired
	private SeqGenerator seqGenerator;
	@Autowired
	private PublisherHoldService publisherHoldService;
	@Autowired
	private InvestorOffsetService investorOffsetService;
	@Autowired
	private PublisherOffsetService publisherOffsetService;
	@Autowired
	private InvestorRedeemTradeOrderService investorRedeemTradeOrderService;
	@Autowired
	private ProductOffsetService productOffsetService;
	@Autowired
	private OrderDateService orderDateService;
	/**
	 * 构建付息订单
	 * @param hold 持有人
	 * @return
	 */
	private InvestorTradeOrderEntity buildRepayInterestTradeOrder(PublisherHoldEntity hold) {
		Timestamp orderTime = new Timestamp(System.currentTimeMillis());
		InvestorTradeOrderEntity tradeOrder = new InvestorTradeOrderEntity();
		tradeOrder.setInvestorBaseAccount(hold.getInvestorBaseAccount());
		tradeOrder.setProduct(hold.getProduct());
		tradeOrder.setPublisherBaseAccount(hold.getPublisherBaseAccount());
		tradeOrder.setOrderCode(this.seqGenerator.next(CodeConstants.Publisher_repayInterest));
		tradeOrder.setOrderType(InvestorTradeOrderEntity.TRADEORDER_orderType_repayInterest);
		tradeOrder.setOrderAmount(hold.getRedeemableIncome());
		tradeOrder.setOrderVolume(hold.getRedeemableIncome().divide(hold.getProduct().getNetUnitShare(), DecimalUtil.scale, DecimalUtil.roundMode));
		tradeOrder.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_accepted);
		
		tradeOrder.setCheckStatus(InvestorTradeOrderEntity.TRADEORDER_checkStatus_no);
		tradeOrder.setInvestorClearStatus(InvestorTradeOrderEntity.TRADEORDER_investorClearStatus_toClear);
		tradeOrder.setInvestorCloseStatus(InvestorTradeOrderEntity.TRADEORDER_investorCloseStatus_toClose);
		tradeOrder.setPublisherClearStatus(InvestorTradeOrderEntity.TRADEORDER_publisherClearStatus_toClear);
		tradeOrder.setPublisherConfirmStatus(InvestorTradeOrderEntity.TRADEORDER_publisherConfirmStatus_toConfirm);
		tradeOrder.setPublisherCloseStatus(InvestorTradeOrderEntity.TRADEORDER_publisherCloseStatus_toClose);
		
		tradeOrder.setCreateMan(InvestorTradeOrderEntity.TRADEORDER_createMan_platform);
		tradeOrder.setOrderTime(orderTime);
		tradeOrder.setPublisherOffset(this.publisherOffsetService.getLatestOffset(
				tradeOrder, orderDateService.getRedeemDate(tradeOrder.getProduct(), tradeOrder.getOrderTime()), true));
		tradeOrder.setInvestorOffset(this.investorOffsetService.getLatestNormalOffset(tradeOrder));
		productOffsetService.offset(tradeOrder.getPublisherBaseAccount(), tradeOrder, true);
		return tradeOrder;
	}

	
	
	public void repayInterestAll() {
		List<Product> products = this.productService.getRepayInterestProduct();
		for (Product product : products) {
			this.repayInterest(product);
		}
	}
	
	public void repayInterestAlone(String productOid) {
		Product product = this.productService.findByOid(productOid);
		if (!Product.STATE_Durationend.equals(product.getState()) && !Product.PRODUCT_repayInterestStatus_toRepay.equals(product.getRepayLoanStatus())) {
			// error.define[30049]=还本付息日未到(CODE:30049)
			throw AMPException.getException(30049);
		}
		this.repayInterest(product);
	}
	
	/**
	 * 付息
	 */
	private void repayInterest(Product product) {
//		product.setRepayInterestStatus(Product.PRODUCT_repayInterestStatus_repaying);
//		String lastOid = "0";
//		while (true) {
//			List<PublisherHoldEntity> holds = this.publisherHoldService.findByProductPaged(
//					product, PublisherHoldEntity.PUBLISHER_accountType_INVESTOR, lastOid);
//			if (holds.isEmpty()) {
//				break;
//			}
//			List<InvestorTradeOrderEntity> orderList = new ArrayList<InvestorTradeOrderEntity>();
//			for (PublisherHoldEntity publisherHoldEntity : holds) {
//				// 创建付息订单
//				InvestorTradeOrderEntity orderEntity = this.buildRepayInterestTradeOrder(publisherHoldEntity);
//				orderList.add(orderEntity);
//				this.publisherHoldService.repayInterestLock(orderEntity);
//				lastOid = publisherHoldEntity.getOid();
//			}
//			this.investorTradeOrderDao.save(orderList);
//		}
//		this.productService.saveEntity(product);
	}
	
	
	
	
	
}
