package com.guohuai.ams.acct.books.document.entry;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import lombok.Data;

@Data
public class DocumentEntryResp {

	public DocumentEntryResp(DocumentEntry e) {
		this.documentOid = e.getDocument().getOid();
		this.documentAcctDate = e.getDocument().getAcctDate();
		this.documentDocword = e.getDocument().getDocword().getDicword();
		this.documentAcctSn = e.getDocument().getAcctSn();
		this.documentUpdateTime = e.getDocument().getUpdateTime();
		this.documentCreateTime = e.getDocument().getCreateTime();

		this.documentEntryDigest = e.getDigest();
		this.documentEntryAccountOid = e.getBook().getAccount().getOid();
		this.documentEntryAccountCode = e.getBook().getAccount().getCode();
		this.documentEntryAccountName = e.getBook().getAccount().getName();
		this.documentEntryDrAmount = e.getDrAmount();
		this.documentEntryCrAmount = e.getCrAmount();

	}

	private String documentOid;
	private Date documentAcctDate;
	private String documentDocword;
	private int documentAcctSn;
	private Timestamp documentCreateTime;
	private Timestamp documentUpdateTime;

	private String documentEntryDigest;
	private String documentEntryAccountOid;
	private String documentEntryAccountCode;
	private String documentEntryAccountName;
	private BigDecimal documentEntryDrAmount;
	private BigDecimal documentEntryCrAmount;

	private boolean master;
	private int rowspan;
	private int index;

}
