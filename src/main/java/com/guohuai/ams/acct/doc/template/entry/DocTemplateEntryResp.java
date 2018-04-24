package com.guohuai.ams.acct.doc.template.entry;

import lombok.Data;

@Data
public class DocTemplateEntryResp {

	public DocTemplateEntryResp(DocTemplateEntry e) {

		this.entryOid = e.getOid();
		this.entryDigest = e.getDigest();
		this.entryDirection = e.getDirection();

		this.typeOid = e.getTemplate().getType().getOid();
		this.typeName = e.getTemplate().getType().getName();

		this.templateOid = e.getTemplate().getOid();
		this.templateName = e.getTemplate().getName();

		this.accountOid = e.getAccount().getOid();
		this.accountName = e.getAccount().getName();
	}

	public DocTemplateEntryResp(DocTemplateEntry e, boolean master) {
		this(e);
		this.master = master;
	}

	private String entryOid;
	private String entryDigest;
	private String entryDirection;

	private String typeOid;
	private String typeName;

	private String templateOid;
	private String templateName;

	private String accountOid;
	private String accountName;

	private boolean master;
	private int rowspan;
	private int index;

}
