

package com.guohuai.mmp.publisher.product.rewardincomepractice;

import java.sql.Date;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@Transactional
public class PracticeServiceRequireNew {
	
	@Autowired
	private PracticeDao practiceDao;

	@Transactional(value = TxType.REQUIRES_NEW)
	public void delByProductAndTDate(String productOid, Date sqlDate){
		this.practiceDao.delByProductAndTDate(productOid, sqlDate);
	}
}
