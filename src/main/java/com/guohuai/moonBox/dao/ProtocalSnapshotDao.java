package com.guohuai.moonBox.dao;

import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.guohuai.moonBox.entity.ProtocalSnapshotEntity;

public interface ProtocalSnapshotDao extends JpaRepository<ProtocalSnapshotEntity, String>, JpaSpecificationExecutor<ProtocalSnapshotEntity>{

	@Query(value=" select count(1) from T_MONEY_PROTOCOL_SNAPSHOT  where investOid=?1 and protocalOid=?2  ",nativeQuery=true)
	public long findSnapshotByInvestOid(String investorOid,String protocalOid);

	/**
	 *理财计划快照
	 */
	@Modifying
	@Query(value = "insert into T_MONEY_PROTOCOL_SNAPSHOT (oid,protocalOid, investOid, ProductOid, protocalName, protocalAmount) "
			+ " select REPLACE(uuid(), '-', ''), oid, investOid, productOid, protocalName,protocalAmount"
			+ " from T_MONEY_PROTOCOL "
			+ " where protocalStatus=0 and NextInvestDate=?1 "
			+ " and  (LastPayDate is null or LastPayDate <?1 )", nativeQuery = true)
	int snapshotPlan(Date AutoDate);
	
//	@Query(value="select oid,protocalOid, investOid, ProductOid, protocalName, protocalAmount from T_MONEY_PROTOCOL_SNAPSHOT  where NextInvestDate=?1 ",nativeQuery=true)
//	public List<ProtocalSnapshotEntity> findByNextInvestDate(Date InvestDate);
	@Modifying
	@Query(value="delete from T_MONEY_PROTOCOL_SNAPSHOT  where investOid=?1 ",nativeQuery=true)
	public int deleteByinvestOid (String investOid);
	
	@Modifying
	@Query(value="delete from T_MONEY_PROTOCOL_SNAPSHOT where 1=1",nativeQuery=true)
	public void deleteShot();
	
	@Query(value = "select oid,investOid,ProductOid,protocalOid,protocalName,protocalAmount from T_MONEY_PROTOCOL_SNAPSHOT where oid>?1 order by oid asc limit 200",nativeQuery=true)
	public List<ProtocalSnapshotEntity> findSnapShotAll(String lastOid);
}

