package com.guohuai.mmp.schedule;

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
import com.guohuai.mmp.job.lock.JobLockEntity;
import com.guohuai.mmp.job.lock.JobLockService;
import com.guohuai.mmp.job.log.JobLogEntity;
import com.guohuai.mmp.job.log.JobLogFactory;
import com.guohuai.mmp.job.log.JobLogService;
import com.guohuai.mmp.platform.baseaccount.statistics.PlatformStatisticsService;
import com.guohuai.mmp.platform.investor.offset.InvestorOffsetEntity;
import com.guohuai.mmp.platform.investor.offset.InvestorOffsetService;
import com.guohuai.mmp.platform.publisher.offset.PublisherOffsetEntity;
import com.guohuai.mmp.platform.publisher.offset.PublisherOffsetService;
import com.guohuai.mmp.publisher.baseaccount.statistics.PublisherStatisticsService;

/**
 * 
 * @author yuechao
 *
 */
@Service
@Transactional
public class OverdueTimesService {
	
	Logger logger = LoggerFactory.getLogger(OverdueTimesService.class);
	@Autowired
	private PublisherOffsetService publisherOffsetService;
	@Autowired
	private InvestorOffsetService investorOffsetService;
	@Autowired
	private PlatformStatisticsService platformStatisticsService;
	@Autowired
	private PublisherStatisticsService publisherStatisticsService;
	@Autowired
	private JobLockService jobLockService;
	@Autowired
	private ProductService productService;
	@Autowired
	private JobLogService jobLogService;
	
	/**
	 * 逾期次数
	 */
	public void overdueTimes() {

		if (this.jobLockService.getRunPrivilege(JobLockEntity.JOB_jobId_overdueTimes)) {
			this.overdueTimesLog();
		}
	}

	private void overdueTimesLog() {
		JobLogEntity jobLog =  JobLogFactory.getInstance(JobLockEntity.JOB_jobId_overdueTimes);
		
		try {
			this.overdueTimesDo();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			jobLog.setJobMessage(AMPException.getStacktrace(e));
			jobLog.setJobStatus(JobLogEntity.JOB_jobStatus_failed);
		}
		jobLog.setBatchEndTime(DateUtil.getSqlCurrentDate());
		this.jobLogService.saveEntity(jobLog);
		this.jobLockService.resetJob(JobLockEntity.JOB_jobId_overdueTimes);
		
	}
	
	private void overdueTimesDo() {
		/** 清结算逾期 */
		int overdueTimes = 0;
		List<PublisherOffsetEntity> pOffsetList = this.publisherOffsetService.getOverdueOffset(DateUtil.getSqlDate());
		for (PublisherOffsetEntity poffsetEntity : pOffsetList) {
			publisherStatisticsService.increaseOverdueTimes(poffsetEntity.getPublisherBaseAccount());
			poffsetEntity.setOverdueStatus(PublisherOffsetEntity.OFFSET_overdueStatus_yes);
		}
		this.publisherOffsetService.batchUpdate(pOffsetList);
		
		
		
		List<InvestorOffsetEntity> iOffsetList = investorOffsetService.getOverdueInvestorOffset(DateUtil.getSqlDate());
		for (InvestorOffsetEntity iOffsetEntity : iOffsetList) {
			iOffsetEntity.setOverdueStatus(InvestorOffsetEntity.OFFSET_overdueStatus_yes);
		}
		investorOffsetService.batchUpdate(iOffsetList);
		
		/** 还本付息逾期 */
		List<Product> pList = this.productService.getOverdueProduct(DateUtil.getSqlDate());
		for (Product product : pList) {
			publisherStatisticsService.increaseOverdueTimes(product.getPublisherBaseAccount());
			product.setOverdueStatus(PublisherOffsetEntity.OFFSET_overdueStatus_yes);
		}
		this.productService.batchUpdate(pList);
		
		overdueTimes += pOffsetList.size();
		overdueTimes += iOffsetList.size();
		overdueTimes += pList.size();
		platformStatisticsService.increaseOverdueTimes(overdueTimes);
	}
}
