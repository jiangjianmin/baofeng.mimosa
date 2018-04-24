package com.guohuai.moonBox.dao;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.guohuai.moonBox.entity.ProtocalEntity;

@Repository
public interface FamilyInvestPlanDao extends JpaRepository<ProtocalEntity, String>, JpaSpecificationExecutor<ProtocalEntity>{

	@Query(value="select oid,investOid,productOid,protocalName,protocalAmount,protocalDate,protocalStatus,NextInvestDate,LastPayDate,"
			+ " protocalLabel,CreateTime,UpdateTime from T_MONEY_PROTOCOL "
			+ " where investOid=?1 and updateTime>=?2 order by CreateTime desc",nativeQuery=true)
	public List<ProtocalEntity> findByInvestOidAndUpdateTime(String investorOid,String beginMonth);
 
	@Query(value="select oid,investOid,productOid,protocalName,protocalAmount,protocalDate,protocalStatus,NextInvestDate,LastPayDate,"
			+ " protocalLabel,CreateTime,UpdateTime from T_MONEY_PROTOCOL "
			+ "  where investOid=?1   and if (?2=2,1=1,protocalStatus=?2)",nativeQuery=true)
	public List<ProtocalEntity> findByInvestOid(String investorOid,String status);
	
	@Query(value="update T_MONEY_PROTOCOL set updateTime=now(),NextInvestDate==?2 where investOid=?1   and protocalStatus=0",nativeQuery=true)
	public int updateByInvesterOid(String investorOid,String  nextInvestDate);
	
}
