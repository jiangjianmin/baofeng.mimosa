package com.guohuai.ams.investment.pool;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * 
 * <p>Title: TargetIncomeDao.java</p>    
 * <p>本息兑付Dao</p>   
 * @author vania      
 * @version 1.0    
 * @created 2016年5月18日 下午3:31:40
 */
public interface TargetIncomeDao
		extends JpaRepository<TargetIncome, String>, JpaSpecificationExecutor<TargetIncome> {
	/**
	 * 根据投资标的id查询本兮兑付记录
	 * @Title: findByTargetOid 
	 * @author vania
	 * @version 1.0
	 * @see: 
	 * @param targetOid
	 * @return
	 * @return List<Interest>    返回类型 
	 */
	@Query(value = "from TargetIncome p where p.investment.oid = ?1")
	public List<TargetIncome> findByTargetOid(String targetOid);
	
	/**
	 * 根据标的id查询所有的本息兑付数据并且按照seq升序
	 * @Title: findByTargetOidOrderBySeq 
	 * @author vania
	 * @version 1.0
	 * @see: 
	 * @param targetOid 投资标的id
	 * @return List<TargetIncome>    返回类型
	 */
	@Query(value = "from TargetIncome p where p.investment.oid = ?1 order by p.seq asc")
	public List<TargetIncome> findByTargetOidOrderBySeq(String targetOid);

	/**
	 * 删除标的某一期本兮兑付的数据
	 * @Title: deleteByTargetOidAndSeq 
	 * @author vania
	 * @version 1.0
	 * @see:
	 * @param targetOid 投资标的id
	 * @param seq void    返回类型
	 */
	@Modifying
	@Query(value = "delete from TargetIncome p where p.investment.oid = ?1 and p.seq = ?2")
	public void deleteByTargetOidAndSeq(String targetOid, int seq);
}
