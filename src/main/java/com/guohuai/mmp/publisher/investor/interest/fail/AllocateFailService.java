package com.guohuai.mmp.publisher.investor.interest.fail;

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
public class AllocateFailService {
	private static final Logger logger = LoggerFactory.getLogger(AllocateFailService.class);
	
	@Autowired
	AllocateFailDao allocateFailDao;

	

	public AllocateFailEntity saveEntity(AllocateFailEntity result) {
		return this.allocateFailDao.save(result);
	}

	
	
	


	

	
	
	
}
