package com.guohuai.mmp.publisher.investor;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.ams.duration.fact.income.IncomeAllocate;
import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductService;
import com.guohuai.ams.product.reward.ProductIncomeRewardCacheService;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.DecimalUtil;
import com.guohuai.mmp.job.lock.JobLockEntity;
import com.guohuai.mmp.job.lock.JobLockService;
import com.guohuai.mmp.job.lock.JobRep;
import com.guohuai.mmp.publisher.product.rewardincomepractice.PracticeService;
import com.guohuai.mmp.publisher.product.rewardincomepractice.RewardIsNullRep;

@Service
@Transactional
public class InterestT0Duration {
	
	Logger logger = LoggerFactory.getLogger(InterestT0Duration.class);
	
	@Autowired
	private ProductService productService;
	@Autowired
	private InterestRateMethodService interestRateMethodService;
	@Autowired
	private InterestRequireNew interestRequireNew;
	@Autowired
	private JobLockService jobLockService;
	@Autowired
	private PracticeService practiceService;
	@Autowired
	private ProductIncomeRewardCacheService productIncomeRewardCacheService;
	
	
	public void interestT0Duration() {
		JobLockEntity jobLock = null;
		// 收益快照是否已经成功结束
//		String batchCode = DateUtil.format(DateUtil.getSqlDate(), "yyyyMMdd");
//		
//		try {
//			this.jobLockService.isSnapshotVolume(batchCode, JobLockEntity.JOB_jobId_snapshot);
//		} catch (Exception e) {
//			jobLock = new JobLockEntity();
////			jobLock.setJobId(JobLockEntity.JOB_jobId_interest4T0Duration);
//			jobLock.setBatchCode(batchCode);
//			jobLock.setBatchStartTime(DateUtil.getSqlCurrentDate());
////			jobLock.setJobStatus(JobLockEntity.JOB_jobStatus_fail);
//			jobLock.setJobMessage(AMPException.getStacktrace(e));
//			jobLock.setBatchEndTime(DateUtil.getSqlCurrentDate());
//			this.jobLockService.save(jobLock);
//			return;
//		}
		
//		try {
//			jobLock = jobLockService.findByBatchCodeAndJobId(batchCode, JobLockEntity.JOB_jobId_interest4T0Duration);
//		} catch (Exception e) {
//			logger.error(e.getMessage(), e);
//			return;
//		}
//		JobRep jobRep = 
				this.interestT0DurationLock();
//		jobLock.setJobStatus(jobRep.getJobStatus());
//		jobLock.setJobMessage(jobRep.getJobMessage());
//		jobLock.setBatchEndTime(DateUtil.getSqlCurrentDate());
//		this.jobLockService.save(jobLock);
	}

	public JobRep interestT0DurationLock() {
		JobRep jobRep = new JobRep();
//		jobRep.setJobStatus(JobLockEntity.JOB_jobStatus_done);
		jobRep.setJobMessage("OK");
		try {
			this.interestTnDurationDo();
		} catch (Exception e) {
			e.printStackTrace();
			jobRep.setJobMessage(AMPException.getStacktrace(e));
//			jobRep.setJobStatus(JobLockEntity.JOB_jobStatus_fail);
		}
		return jobRep;
	}
	
	/**
	 * 定期产品募集期计息
	 */
	public void interestTnDurationDo() {
		List<Product> productList = this.productService.findProductTn4Snapshot();
		Date incomeDate = DateUtil.getBeforeDate();
		for (Product product : productList) {	
			
			if(Product.NO.equals(product.getIsAutoAssignIncome())) {
				continue;
			}
			
			RewardIsNullRep rewardIsNullRep = null;
			if(productIncomeRewardCacheService.hasRewardIncome(product.getOid())) {
				rewardIsNullRep = this.practiceService.rewardIsNotNullRep(product, incomeDate);
			} else {
				rewardIsNullRep = this.practiceService.rewardIsNullRep(product, incomeDate);
			}
			// 待分配收益
			BigDecimal incomeAmount = product.getExpAror().multiply(rewardIsNullRep.getTotalHoldVolume())
					.divide(new BigDecimal(product.getIncomeCalcBasis()), DecimalUtil.scale, DecimalUtil.roundMode);
			
			InterestReq ireq = new InterestReq();
			ireq.setProduct(product);
			ireq.setTotalInterestedVolume(rewardIsNullRep.getTotalHoldVolume());
			ireq.setIncomeAmount(incomeAmount);
			ireq.setRatio(product.getExpAror());
			ireq.setIncomeDate(incomeDate);
			ireq.setIncomeType(IncomeAllocate.ALLOCATE_INCOME_TYPE_durationIncome);
			
			IncomeAllocate incomeAllocate = this.interestRequireNew.newAllocate(ireq);

			this.interestRateMethodService.interest(incomeAllocate.getOid(), incomeAllocate.getProduct().getOid());
		}
	}
}
