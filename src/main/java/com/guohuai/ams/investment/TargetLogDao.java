package com.guohuai.ams.investment;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.guohuai.ams.investment.pool.TargetIncome;

/**
 * 
 * <p>
 * Title: TargetLogDao.java
 * </p>
 * <p>
 * 标的操作日志Dao
 * </p>
 * 
 * @author vania
 * @version 1.0
 * @created 2016年5月18日 下午3:31:40
 */
public interface TargetLogDao extends JpaRepository<TargetLog, String>, JpaSpecificationExecutor<TargetLog> {
	/**
	 * 根据投资标的id查询本兮兑付记录
	 * 
	 * @Title: findByTargetOid
	 * @author vania
	 * @version 1.0
	 * @see:
	 * @param targetOid
	 * @return List<TargetIncome> 返回类型
	 */
	@Query(value = "from TargetLog p where p.investment.oid = ?1")
	public List<TargetIncome> findByTargetOid(String targetOid);
}
