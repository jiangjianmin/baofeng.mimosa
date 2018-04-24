package com.guohuai.ams.acct.doc.template;

import lombok.Data;

@Data
public class DocTemplateResp {

	public DocTemplateResp(DocTemplate t) {
		this.oid = t.getOid();
		this.name = t.getName();
	}

	private String oid;
	private String name;

}
