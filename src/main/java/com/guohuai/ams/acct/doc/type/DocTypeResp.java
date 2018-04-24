package com.guohuai.ams.acct.doc.type;

import lombok.Data;

@Data
public class DocTypeResp {

	public DocTypeResp(DocType t) {
		this.oid = t.getOid();
		this.name = t.getName();
	}

	private String oid;
	private String name;
}
