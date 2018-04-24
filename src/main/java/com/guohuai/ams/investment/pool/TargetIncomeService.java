package com.guohuai.ams.investment.pool;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

/**
 * 
 * <p>Title: TargetIncomeService.java</p>    
 * <p>本息兑付Service </p>   
 * @author vania      
 * @version 1.0    
 * @created 2016年5月18日 下午3:31:20
 */
@Service
@Transactional
public class TargetIncomeService {
	@Autowired
	TargetIncomeDao targetIncomeDao;

	
	
	/**
	 * 分页查询本息兑付
	 * 
	 * @Title: getTargetIncomeList
	 * @author vania
	 * @version 1.0
	 * @see: TODO
	 * @param spec
	 * @param pageable
	 * @return
	 * @return Page<TargetIncome> 返回类型
	 */
	public Page<TargetIncome> getTargetIncomeList(Specification<TargetIncome> spec, Pageable pageable) {
		return targetIncomeDao.findAll(spec, pageable);
	}
	
	/**
	 * 根据标的id查询所有的本息兑付数据
	 * @Title: findByTargetOidOrderBySeq 
	 * @author vania
	 * @version 1.0
	 * @see: 
	 * @param targetOid
	 * @return List<TargetIncome>    返回类型
	 */
	public List<TargetIncome> findByTargetOidOrderBySeq(String targetOid) {
		return this.targetIncomeDao.findByTargetOidOrderBySeq(targetOid);
	}
}
