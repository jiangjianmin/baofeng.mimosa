package com.guohuai.mmp.publisher.investor.interest.result;

import java.sql.Date;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.ams.duration.fact.income.IncomeAllocate;
import com.guohuai.ams.duration.fact.income.IncomeDistributionService;
import com.guohuai.ams.product.Product;




@Service
@Transactional
public class InterestResultService {
	private static final Logger logger = LoggerFactory.getLogger(InterestResultService.class);
	@Autowired
	IncomeDistributionService incomeDistributionService;
	@Autowired
	InterestResultDao InterestResultDao;

	public InterestResultEntity createEntity(Product product, IncomeAllocate incomeAllocate, Date incomeDate) {
		InterestResultEntity entity = new InterestResultEntity();
		entity.setProduct(product);
		entity.setIncomeAllocate(incomeAllocate);
		entity.setAllocateDate(incomeDate);
		return entity;
	}

	public InterestResultEntity saveEntity(InterestResultEntity result) {
		return this.InterestResultDao.save(result);
	}

	public void send(InterestResultEntity result) {
		try {
			incomeDistributionService.allocateIncome(result);
		} catch (Exception e) {
			logger.error("处理收益发放发生异常", e);
		}
	}
	
	


	

	
	
	
}
