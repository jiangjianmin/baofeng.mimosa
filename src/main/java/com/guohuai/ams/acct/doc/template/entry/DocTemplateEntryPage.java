package com.guohuai.ams.acct.doc.template.entry;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class DocTemplateEntryPage {

	private long total;
	private long number;

	private List<DocTemplateEntryResp> rows = new ArrayList<DocTemplateEntryResp>();

}
