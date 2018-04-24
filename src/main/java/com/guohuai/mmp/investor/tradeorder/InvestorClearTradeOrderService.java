package com.guohuai.mmp.investor.tradeorder;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductService;
import com.guohuai.basic.common.SeqGenerator;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.DecimalUtil;
import com.guohuai.mmp.job.lock.JobLockEntity;
import com.guohuai.mmp.job.lock.JobLockService;
import com.guohuai.mmp.job.log.JobLogEntity;
import com.guohuai.mmp.job.log.JobLogFactory;
import com.guohuai.mmp.job.log.JobLogService;
import com.guohuai.mmp.platform.investor.offset.InvestorOffsetService;
import com.guohuai.mmp.platform.publisher.offset.PublisherOffsetService;
import com.guohuai.mmp.platform.publisher.product.offset.ProductOffsetService;
import com.guohuai.mmp.publisher.hold.PublisherHoldEntity;
import com.guohuai.mmp.publisher.hold.PublisherHoldService;
import com.guohuai.mmp.sys.CodeConstants;
@Service
@Transactional
public class InvestorClearTradeOrderService {
	
	Logger logger = LoggerFactory.getLogger(InvestorClearTradeOrderService.class);
	@Autowired
	private ProductService productService;
	@Autowired
	private PublisherHoldService publisherHoldService;
	@Autowired
	private InvestorTradeOrderDao investorTradeOrderDao;
	@Autowired
	private InvestorRedeemTradeOrderService investorRedeemTradeOrderService;
	@Autowired
	private SeqGenerator seqGenerator;
	@Autowired
	private InvestorOffsetService investorOffsetService;
	@Autowired
	private PublisherOffsetService publisherOffsetService;
	@Autowired
	private ProductOffsetService productOffsetService;
	@Autowired
	private JobLockService jobLockService;
	@Autowired
	private JobLogService jobLogService;
	@Autowired
	private OrderDateService orderDateService;
	
	
	public void clear() {
		if (this.jobLockService.getRunPrivilege(JobLockEntity.JOB_jobId_clear)) {
			clearLog();
		}
	}
	
	private void clearLog() {
		JobLogEntity jobLog = JobLogFactory.getInstance(JobLockEntity.JOB_jobId_clear);
		try {
			
			clearDo();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			jobLog.setJobMessage(AMPException.getStacktrace(e));
			jobLog.setJobStatus(JobLogEntity.JOB_jobStatus_failed);
		}
		jobLog.setBatchEndTime(DateUtil.getSqlCurrentDate());
		this.jobLogService.saveEntity(jobLog);
		this.jobLockService.resetJob(JobLockEntity.JOB_jobId_clear);
	}

	/**
	 *清盘 
	 */
	public void clearDo() {
		List<Product> pList = this.productService.findByState(Product.STATE_Clearing);
		for (Product product : pList) {
			String lastOid = "0";
			while (true) {
				List<PublisherHoldEntity> hList = this.publisherHoldService.clearingHold(product.getOid(), PublisherHoldEntity.PUBLISHER_accountType_INVESTOR, lastOid);
				if (hList.isEmpty()) {
					break;
				}
				List<InvestorTradeOrderEntity> orderList = new ArrayList<InvestorTradeOrderEntity>();
				for (PublisherHoldEntity hold : hList) {
					
					InvestorTradeOrderEntity orderEntity = this.buildClearTradeOrder(hold);
					//this.publisherHoldService.redeemLock(orderEntity);
					orderList.add(orderEntity);
					lastOid = orderEntity.getOid();
				}
				this.investorTradeOrderDao.save(orderList);
				
				
			}
		}
	}
	
	private InvestorTradeOrderEntity buildClearTradeOrder(PublisherHoldEntity hold) {
		Timestamp orderTime = new Timestamp(System.currentTimeMillis());
		InvestorTradeOrderEntity tradeOrder = new InvestorTradeOrderEntity();
		tradeOrder.setInvestorBaseAccount(hold.getInvestorBaseAccount());
		tradeOrder.setProduct(hold.getProduct());
		tradeOrder.setPublisherBaseAccount(hold.getPublisherBaseAccount());
		tradeOrder.setOrderCode(this.seqGenerator.next(CodeConstants.PAYMENT_clearRedeem));
		tradeOrder.setOrderType(InvestorTradeOrderEntity.TRADEORDER_orderType_clearRedeem);
		tradeOrder.setOrderAmount(hold.getRedeemableHoldVolume().multiply(hold.getProduct().getNetUnitShare()));
		tradeOrder.setOrderVolume(tradeOrder.getOrderAmount().divide(hold.getProduct().getNetUnitShare(), DecimalUtil.scale, DecimalUtil.roundMode));
		
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
	
	
	
	
	

}
