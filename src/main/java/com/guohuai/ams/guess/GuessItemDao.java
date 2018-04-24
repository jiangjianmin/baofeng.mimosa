package com.guohuai.ams.guess;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface GuessItemDao  extends JpaRepository<GuessItemEntity, String>, JpaSpecificationExecutor<GuessItemEntity> {
	@Query(value = "from GuessItemEntity where guessOid = ?1 ORDER BY percent DESC")
	List<GuessItemEntity> findByGuessOid(String oid);
	
	@Query(value="select content from T_GAM_GUESS_ITEM where guessOid = ?1",nativeQuery=true)
	List<Object[]> findContentByGuessOid(String oid);
	

}
