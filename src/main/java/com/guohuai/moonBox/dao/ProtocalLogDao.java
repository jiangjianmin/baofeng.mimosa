package com.guohuai.moonBox.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.guohuai.moonBox.entity.ProtocalLogEntity;
@Repository
public interface ProtocalLogDao extends JpaRepository<ProtocalLogEntity, String>, JpaSpecificationExecutor<ProtocalLogEntity>{

	@Query(value="select oid,investOid,protocalOid,ProductOid,protocalName,operateStatus,protocalAmount,protocalDate,operateStatus,NextInvestDate,"
			+ " protocalLabel,CreateTime from t_money_protocol_log "
			+ " where investOid=?1   order by CreateTime desc limit ?2,?3 ",nativeQuery=true)
	public List<ProtocalLogEntity> findByInvestOid(String investorOid,int size1,int size2);
}

