package com.guohuai.ams.acct.doc.type;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DocTypeDao extends JpaRepository<DocType, String> {

	@Query("from DocType t order by t.oid asc")
	public List<DocType> search();

}
