package com.guohuai.mmp.publisher.investor;

import java.math.BigDecimal;
import java.sql.Date;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.ams.duration.fact.income.IncomeAllocate;
import com.guohuai.ams.product.Product;
import com.guohuai.component.util.StringUtil;
import com.guohuai.mmp.publisher.hold.PublisherHoldEntity;


@Service
@Transactional
public class InterestRateMethodServiceNew {
	private static final Logger logger = LoggerFactory.getLogger(InterestRateMethodServiceNew.class);


	@Autowired
	private InterestRateMethodProcess interestRateMethodProcess;
	/**
	 * 
	 * @param product
	 * @param incomeDate
	 * @param hold
	 * @param netUnitAmount
	 * @param fpRate 年化利率 0.05 已经除以100
	 * @return
	 */
	public InterestRep processOneItem(Product product, Date incomeDate,
			PublisherHoldEntity hold, BigDecimal netUnitAmount,
			BigDecimal fpRate, IncomeAllocate incomeAllocate) {
		String batchNo = "interest:" + StringUtil.uuid();
		InterestRep iRep = new InterestRep();
		try {
			interestRateMethodProcess.process(product, incomeDate, hold, netUnitAmount, fpRate, iRep, incomeAllocate, batchNo);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			iRep.setResult(false);
		}
		
		return iRep;
	}
	
	
}
