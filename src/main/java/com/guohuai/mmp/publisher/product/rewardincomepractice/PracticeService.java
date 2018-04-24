

package com.guohuai.mmp.publisher.product.rewardincomepractice;

import java.sql.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductDao;
import com.guohuai.ams.product.ProductService;
import com.guohuai.ams.product.reward.ProductIncomeRewardCacheService;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.StringUtil;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.component.web.view.RowsRep;
import com.guohuai.mmp.job.lock.JobLockEntity;
import com.guohuai.mmp.job.lock.JobLockService;
import com.guohuai.mmp.job.log.JobLogEntity;
import com.guohuai.mmp.job.log.JobLogFactory;
import com.guohuai.mmp.job.log.JobLogService;
import com.guohuai.mmp.platform.StaticProperties;


@Service
@Transactional
public class PracticeService {
	@Autowired
	PracticeDao practiceDao;
	
	private static final Logger logger = LoggerFactory.getLogger(PracticeService.class);
	
	@Autowired
	private ProductService productService;
	@Autowired
	private JobLockService jobLockService;
	@Autowired
	private JobLogService jobLogService;
	@Autowired
	private PracticeInterestService practiceInterestService;
	@Autowired
	private ProductIncomeRewardCacheService productIncomeRewardCacheService;
	
	public void practice() {
		if (this.jobLockService.getRunPrivilege(JobLockEntity.JOB_jobId_practice)) {
			practiceLog();
		}
	}
	
	public void practiceLog() {

		JobLogEntity jobLog = JobLogFactory.getInstance(JobLockEntity.JOB_jobId_practice);

		try {
			practiceDo();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			jobLog.setJobMessage(AMPException.getStacktrace(e));
			jobLog.setJobStatus(JobLogEntity.JOB_jobStatus_failed);
		}
		jobLog.setBatchEndTime(DateUtil.getSqlCurrentDate());
		this.jobLogService.saveEntity(jobLog);
		this.jobLockService.resetJob(JobLockEntity.JOB_jobId_practice);
	}
	
	/**
	 * 活期产品收益试算
	 */
	private void practiceDo() {

		Date incomeDate = StaticProperties.isIs24() ? DateUtil.getBeforeDate() : DateUtil.getSqlDate();
		// T0
		List<Product> productList = this.productService.findProductT04Snapshot();
		for (Product product : productList) {
			processOneItem(product.getOid(), incomeDate);
		}

		// Tn
		productList = productService.findProductTn4Snapshot();
		for (Product product : productList) {
			processOneItem(product.getOid(), incomeDate);
		}
	}
	
	public void processOneItem(String productOid, Date incomeDate) {
		logger.info("productOid={}, incomeDate={} practice start", productOid, incomeDate);
		this.practiceInterestService.practiceInterest(productOid, incomeDate);
		logger.info("practice.productOid={}, incomeDate={} practice end", productOid, incomeDate);
	}

	public PracticeEntity saveEntity(PracticeEntity entity) {
		return this.practiceDao.save(entity);
	}
	
	public BaseRep detail(String productOid) {
		DetailRep rep = new DetailRep();
		Product product = this.productService.findByOid(productOid);
		RewardIsNullRep rewardIsNullRep = null;
		if(productIncomeRewardCacheService.hasRewardIncome(product.getOid())) {
			rewardIsNullRep = this.rewardIsNotNullRep(product, null);
		} else {
			rewardIsNullRep = this.rewardIsNullRep(product, null);
		}
		rep.setTotalHoldVolume(rewardIsNullRep.getTotalHoldVolume());
		rep.setBuyVolume(product.getCollectedVolume());
		return rep;
	}
	
	/**
	 * 某天某个产品的收益是否已试算
	 * true yes 
	 * false no
	 */
	public boolean isPractice(Product product, Date tDate) {
		if(productIncomeRewardCacheService.hasRewardIncome(product.getOid())) {
			return !this.practiceDao.findByProductAndTDate(product, tDate == null ? practiceDao.findMaxTDate(product) : tDate).isEmpty();
		} else {
			return this.practiceDao.findRewardIsNull(product, tDate) != null;
		}
	}
	
	public RewardIsNullRep rewardIsNullRep(Product product, Date tDate) {
		RewardIsNullRep rep = new RewardIsNullRep();
		
		PracticeEntity entity = null;
		if (null != tDate) {
			entity = this.practiceDao.findRewardIsNull(product, tDate);
		} else {
			entity = this.practiceDao.findRewardIsNull(product.getOid());
		}
		if (null != entity) {
			rep.setProduct(product);
			rep.setTotalHoldVolume(entity.getTotalHoldVolume());
			rep.setTotalRewardIncome(entity.getTotalRewardIncome());
			rep.setTDate(entity.getTDate());
		} 
		return rep;
	}
	
	public RewardIsNullRep rewardIsNotNullRep(Product product, Date tDate) {
		RewardIsNullRep rep = new RewardIsNullRep();
		
		List<PracticeEntity> entityList = this.practiceDao.findByProductAndTDate(product, tDate == null ? practiceDao.findMaxTDate(product) : tDate);
		for(PracticeEntity entity : entityList) {
			rep.setProduct(product);
			rep.setTotalHoldVolume(rep.getTotalHoldVolume().add(entity.getTotalHoldVolume()));
			rep.setTotalRewardIncome(rep.getTotalRewardIncome().add(entity.getTotalRewardIncome()));
			rep.setTDate(entity.getTDate());
		} 
		return rep;
	}
	
	public List<PracticeEntity> findByPrductAfterInterest(Product product, Date incomeDate) {
		return this.practiceDao.findByPrductAfterInterest(product, incomeDate);
	}
	
	
	/**
	 * 产品--数据分布
	 */
	public RowsRep<PracticeInRep> findByProduct(String productOid, Date tDate) {
		if (StringUtil.isEmpty(productOid)) {
			return new RowsRep<PracticeInRep>();
		}
		Product product = this.productService.findByOid(productOid);
		return this.findByProduct(product, tDate);
	}
	
	/**
	 * 产品--数据分布
	 */
	public RowsRep<PracticeInRep> findByProduct(Product product, Date tDate) {
		
		List<PracticeEntity> list = null;
		if (null != tDate) {
			list = this.practiceDao.findByProductAndTDate(product, tDate);
		} else {
			Date maxDate = this.practiceDao.findMaxTDate(product);
			list = this.practiceDao.findByProductAndTDate(product, maxDate);
		}
		RowsRep<PracticeInRep> rowsRep = new RowsRep<PracticeInRep>();
		if (null != list && !list.isEmpty()) {
			for (PracticeEntity entity : list) {
				PracticeInRep rep = new PracticeInRep();
				
				rep.setTotalHoldVolume(entity.getTotalHoldVolume());
				rep.setValue((long)(Math.floor(entity.getTotalHoldVolume().doubleValue() * product.getNetUnitShare().doubleValue() * 100)));
				rep.setTotalRewardIncome(entity.getTotalRewardIncome());
				rep.setTDate(entity.getTDate());
				rep.setCreateTime(entity.getCreateTime());
				rep.setUpdateTime(entity.getUpdateTime());
				rep.setProductOid(entity.getProduct().getOid());
				if (null != entity.getReward()) {
					rep.setRewardRatio(entity.getReward().getRatio());
					rep.setStartDate(entity.getReward().getStartDate());
					rep.setEndDate(entity.getReward().getEndDate());
					rep.setRewardOid(entity.getReward().getOid());
					rep.setLevel(entity.getReward().getLevel());
				}
						
				rowsRep.add(rep);
			}
		}
		
		return rowsRep;
	}

	public void delete(List<PracticeEntity> entities) {
		this.practiceDao.delete(entities);
	}
}
