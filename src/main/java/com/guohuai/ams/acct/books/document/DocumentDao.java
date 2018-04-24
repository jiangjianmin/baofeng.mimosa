package com.guohuai.ams.acct.books.document;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DocumentDao extends JpaRepository<Document, String>, JpaSpecificationExecutor<Document> {

}
