package com.guohuai.mmp.investor.tradeorder;

import java.sql.Timestamp;
import java.util.List;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductService;
import com.guohuai.basic.common.SeqGenerator;
import com.guohuai.component.exception.AMPException;
import com.guohuai.mmp.platform.investor.offset.InvestorOffsetService;
import com.guohuai.mmp.platform.publisher.offset.PublisherOffsetService;
import com.guohuai.mmp.platform.publisher.product.offset.ProductOffsetService;
import com.guohuai.mmp.publisher.hold.PublisherHoldEntity;
import com.guohuai.mmp.publisher.hold.PublisherHoldService;
import com.guohuai.mmp.sys.CodeConstants;

@Service
@Transactional
public class InvestorRepayLoanTradeOrderService {
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
	 * 构建还本订单
	 * @param hold 持有人
	 * @return
	 */
	private InvestorTradeOrderEntity buildRepayLoanTradeOrder(PublisherHoldEntity hold) {
		Timestamp orderTime = new Timestamp(System.currentTimeMillis());
		InvestorTradeOrderEntity tradeOrder = new InvestorTradeOrderEntity();
		tradeOrder.setInvestorBaseAccount(hold.getInvestorBaseAccount());
		tradeOrder.setProduct(hold.getProduct());
		tradeOrder.setPublisherBaseAccount(hold.getPublisherBaseAccount());
		tradeOrder.setOrderCode(this.seqGenerator.next(CodeConstants.Publisher_repayLoan));
		tradeOrder.setOrderType(InvestorTradeOrderEntity.TRADEORDER_orderType_repayLoan);
		tradeOrder.setOrderAmount(hold.getRedeemableHoldVolume().multiply(hold.getProduct().getNetUnitShare()));
		tradeOrder.setOrderVolume(hold.getRedeemableHoldVolume());
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

	/**
	 * 还本
	 */
	public void repayLoanAll() {
		List<Product> products = this.productService.getRepayLoanProduct();
		for (Product product : products) {
			this.repayLoan(product);
		}
	}
	
	public void repayLoanAlone(String productOid) {
		
		Product product = this.productService.findByOid(productOid);
		if (!Product.STATE_Durationend.equals(product.getState()) && !Product.PRODUCT_repayLoanStatus_toRepay.equals(product.getRepayLoanStatus())) {
			// error.define[30049]=还本付息日未到(CODE:30049)
			throw AMPException.getException(30049);
		}
		this.repayLoan(product);
		
	}
	
	/**
	 * 生成还本订单
	 * @param product 需要生成还本订单的产品
	 */
	@Transactional(value = TxType.REQUIRES_NEW)
	private void repayLoan(Product product) {
		
//		product.setRepayLoanStatus(Product.PRODUCT_repayLoanStatus_repaying);
//		String lastOid = "0";
//		while (true) {
//			List<PublisherHoldEntity> holds = this.publisherHoldService.findByProductPaged(
//					product, PublisherHoldEntity.PUBLISHER_accountType_INVESTOR, lastOid);
//			if (holds.isEmpty()) {
//				break;
//			}
//			List<InvestorTradeOrderEntity> orderList = new ArrayList<InvestorTradeOrderEntity>();
//			for (PublisherHoldEntity publisherHoldEntity : holds) {
//				
//				InvestorTradeOrderEntity orderEntity = this.buildRepayLoanTradeOrder(publisherHoldEntity);
//				orderList.add(orderEntity);
//				
////				this.publisherHoldService.redeemLock(orderEntity);
//				
//				lastOid = publisherHoldEntity.getOid();
//			}
//			this.investorTradeOrderDao.save(orderList);
//		}
//		this.productService.saveEntity(product);
	}
	

}
