package com.guohuai.mmp.publisher.baseaccount.loginacc;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountEntity;

public interface PublisherLoginAccDao extends JpaRepository<PublisherLoginAccEntity, String>, JpaSpecificationExecutor<PublisherLoginAccEntity> {

	PublisherLoginAccEntity findByLoginAcc(String uid);
	
	@Query(value = "select loginAcc from T_MONEY_PUBLISHER_BASEACCOUNT_LOGINACC ", nativeQuery = true)
	List<String> findAllAcc();

	List<PublisherLoginAccEntity> findByPublisherBaseAccount(PublisherBaseAccountEntity baseAccount);



}
