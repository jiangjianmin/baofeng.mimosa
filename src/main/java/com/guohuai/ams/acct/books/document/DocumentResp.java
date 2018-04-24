package com.guohuai.ams.acct.books.document;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import lombok.Data;

@Data
public class DocumentResp {

	public DocumentResp(Document d) {
		this.oid = d.getOid();
		this.docword = d.getDocword().getOid();
		this.type = d.getType();
		this.relative = d.getRelative();
		this.ticket = d.getTicket();
		this.acctSn = d.getAcctSn();
		this.acctDate = d.getAcctDate();
		this.invoiceNum = d.getInvoiceNum();
		this.drAmount = d.getDrAmount();
		this.crAmount = d.getCrAmount();
		this.createTime = d.getCreateTime();
		this.updateTime = d.getUpdateTime();
	}

	private String oid;
	private String docword;
	private String type;
	private String relative;
	private String ticket;
	private int acctSn;
	private Date acctDate;
	private int invoiceNum;
	private BigDecimal drAmount;
	private BigDecimal crAmount;
	private Timestamp createTime;
	private Timestamp updateTime;

}
