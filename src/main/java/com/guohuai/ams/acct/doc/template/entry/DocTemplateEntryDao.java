package com.guohuai.ams.acct.doc.template.entry;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.guohuai.ams.acct.doc.template.DocTemplate;

public interface DocTemplateEntryDao extends JpaRepository<DocTemplateEntry, String> {

	@Query("from DocTemplateEntry e where e.template in ?1 order by e.template.oid asc, e.seq asc")
	public List<DocTemplateEntry> search(List<DocTemplate> templates);

	public List<DocTemplateEntry> findByTemplate(DocTemplate template);

}
