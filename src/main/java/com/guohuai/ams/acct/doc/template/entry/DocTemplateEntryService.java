package com.guohuai.ams.acct.doc.template.entry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.guohuai.ams.acct.doc.template.DocTemplate;
import com.guohuai.ams.acct.doc.template.DocTemplateService;

@Service
public class DocTemplateEntryService {

	@Autowired
	private DocTemplateEntryDao docTemplateEntryDao;
	@Autowired
	private DocTemplateService docTemplateService;

	public DocTemplateEntryPage search(String templateType, String templateName, int page, int size) {
		Page<DocTemplate> templates = this.docTemplateService.search(templateType, templateName, page, size);

		DocTemplateEntryPage ep = new DocTemplateEntryPage();

		if (null != templates.getContent() && templates.getContent().size() > 0) {
			ep.setTotal(templates.getTotalElements());
			ep.setNumber(templates.getContent().size());
			List<DocTemplateEntry> entries = this.docTemplateEntryDao.search(templates.getContent());

			LinkedHashMap<String, List<DocTemplateEntry>> maps = new LinkedHashMap<String, List<DocTemplateEntry>>();

			for (DocTemplateEntry e : entries) {
				if (!maps.containsKey(e.getTemplate().getOid())) {
					maps.put(e.getTemplate().getOid(), new ArrayList<DocTemplateEntry>());
				}
				maps.get(e.getTemplate().getOid()).add(e);
			}

			int index = 0;
			for (String key : maps.keySet()) {
				for (int i = 0; i < maps.get(key).size(); i++) {
					DocTemplateEntryResp r = new DocTemplateEntryResp(maps.get(key).get(i));
					if (i == 0) {
						r.setMaster(true);
						r.setRowspan(maps.get(key).size());
					}
					r.setIndex(index);
					ep.getRows().add(r);
				}
				index++;
			}
		}

		return ep;
	}

	@Transactional
	public Map<String, DocTemplateEntry> entryMap(DocTemplate template) {
		List<DocTemplateEntry> entries = this.docTemplateEntryDao.findByTemplate(template);
		Map<String, DocTemplateEntry> maps = new HashMap<String, DocTemplateEntry>();
		for (DocTemplateEntry e : entries) {
			maps.put(e.getOid(), e);
		}
		return maps;
	}

}
