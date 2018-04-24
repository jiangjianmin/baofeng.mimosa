package com.guohuai.mmp.publisher.investor;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.ams.duration.fact.income.IncomeAllocate;
import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductService;
import com.guohuai.ams.product.reward.ProductIncomeRewardCacheService;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.mmp.job.lock.JobLockEntity;
import com.guohuai.mmp.job.lock.JobLockService;
import com.guohuai.mmp.job.log.JobLogEntity;
import com.guohuai.mmp.job.log.JobLogFactory;
import com.guohuai.mmp.job.log.JobLogService;
import com.guohuai.mmp.publisher.product.rewardincomepractice.PracticeService;
import com.guohuai.mmp.publisher.product.rewardincomepractice.RewardIsNullRep;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class InterestTnRaise {
	
	
	
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
	private JobLogService jobLogService;
	@Autowired
	private ProductIncomeRewardCacheService productIncomeRewardCacheService;
	
	
	public void interestTnRaise() {

		if (this.jobLockService.getRunPrivilege(JobLockEntity.JOB_jobId_interestTnRaise)) {
			this.interestTnRaiseLog();
		}
	}

	public void interestTnRaiseLog() {
		JobLogEntity jobLog =  JobLogFactory.getInstance(JobLockEntity.JOB_jobId_interestTnRaise);
		try {
			this.interestTnRaiseDo();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			jobLog.setJobMessage(AMPException.getStacktrace(e));
			jobLog.setJobStatus(JobLogEntity.JOB_jobStatus_failed);
		}
		jobLog.setBatchEndTime(DateUtil.getSqlCurrentDate());
		this.jobLogService.saveEntity(jobLog);
		this.jobLockService.resetJob(JobLockEntity.JOB_jobId_interestTnRaise);
	}
	
	/**
	 * 定期产品募集期计息
	 */
	public void interestTnRaiseDo() {
		List<Product> productList = this.productService.findProductTn4Interest();
		Date incomeDate = DateUtil.getBeforeDate();
		for (Product product : productList) {
			log.info("productCode={}, interest start", product.getCode());
			RewardIsNullRep rewardIsNullRep = null;
			if(productIncomeRewardCacheService.hasRewardIncome(product.getOid())) {
				rewardIsNullRep = this.practiceService.rewardIsNotNullRep(product, incomeDate);
			} else {
				rewardIsNullRep = this.practiceService.rewardIsNullRep(product, incomeDate);
			}
			// 待分配收益
			BigDecimal incomeAmount = InterestFormula.compound(rewardIsNullRep.getTotalHoldVolume(), product.getRecPeriodExpAnYield(), product.getIncomeCalcBasis());
			
			InterestReq ireq = new InterestReq();
			ireq.setProduct(product);
			ireq.setTotalInterestedVolume(rewardIsNullRep.getTotalHoldVolume());
			ireq.setIncomeAmount(incomeAmount);
			ireq.setRatio(product.getRecPeriodExpAnYield());
			ireq.setIncomeDate(incomeDate);
			ireq.setIncomeType(IncomeAllocate.ALLOCATE_INCOME_TYPE_raiseIncome);
			
			IncomeAllocate incomeAllocate = this.interestRequireNew.newAllocate(ireq);

			this.interestRateMethodService.interest(incomeAllocate.getOid(), incomeAllocate.getProduct().getOid());
		}
	}
}
