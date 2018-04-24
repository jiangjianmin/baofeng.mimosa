package com.guohuai.ams.acct.account;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AccountDao extends JpaRepository<Account, String> {

	@Query("from Account a where a.state = 'ENABLE' order by a.oid asc")
	public List<Account> search();
	
	@Query(value="select B.name,B.idNumb,B.cardNumb,B.phoneNo from gh_bf_mimosa.t_fund_baseaccount A left join gh_bf_uc.t_wfd_user_bank B on A.useroid=B.useroid where A.oid=?1",nativeQuery=true)
	public List<Object[]> selectFourElement(String uid);
	
	@Query(value="select oid from gh_bf_mimosa.t_fund_baseaccount where userOid=?1",nativeQuery=true)
	public List<String> selectYiLuOid(String investorOid);
}
