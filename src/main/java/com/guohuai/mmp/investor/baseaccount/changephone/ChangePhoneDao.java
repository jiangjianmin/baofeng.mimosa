package com.guohuai.mmp.investor.baseaccount.changephone;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ChangePhoneDao extends JpaRepository<ChangePhoneEntity, String>{
	
	@Modifying
	@Query(value="update T_MONEY_BASEACCOUNT_PHONE_CHANGELOG set status = ?2 where oid=?1", nativeQuery = true)
	public int updateStatusByOid(String oid, int status);

}
