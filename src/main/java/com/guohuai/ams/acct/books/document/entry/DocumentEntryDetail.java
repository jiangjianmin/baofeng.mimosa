package com.guohuai.ams.acct.books.document.entry;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.guohuai.ams.acct.books.document.Document;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.NumberToCN;

import lombok.Data;

@Data
public class DocumentEntryDetail {

	public DocumentEntryDetail(Document d) {
		this.oid = d.getOid();
		this.docWord = d.getDocword().getDicword();
		this.docTitle = d.getDocword().getTitle();
		this.acctSn = d.getAcctSn();
		this.acctDate = d.getAcctDate();
		this.docPeriod = DateUtil.format(d.getAcctDate(), "yyyy年第M期");
		this.invoiceNum = d.getInvoiceNum();
		this.createTime = d.getCreateTime();
		this.dr = amounts(d.getDrAmount());
		this.cr = amounts(d.getCrAmount());
		this.drAmount = d.getDrAmount();
		this.crAmount = d.getCrAmount();
		this.amountCN = NumberToCN.number2CNMontrayUnit(d.getDrAmount());
	}

	private String oid;
	private String docWord;
	private String docTitle;
	private int acctSn;
	private Date acctDate;
	private String docPeriod;
	private int invoiceNum;
	private Timestamp createTime;
	private String[] dr;
	private String[] cr;
	private BigDecimal drAmount;
	private BigDecimal crAmount;
	private String amountCN;

	private List<Detail> details = new ArrayList<Detail>();

	@Data
	public static class Detail {

		public Detail(DocumentEntry e) {
			this.oid = e.getOid();
			this.digest = e.getDigest();
			this.accountCode = e.getBook().getAccount().getCode();
			this.accountName = e.getBook().getAccount().getName();
			this.drAmount = e.getDrAmount();
			this.crAmount = e.getCrAmount();
			this.dr = amounts(e.getDrAmount());
			this.cr = amounts(e.getCrAmount());
		}

		private String oid;
		private String digest;
		private String accountCode;
		private String accountName;
		private String[] dr;
		private String[] cr;
		private BigDecimal drAmount;
		private BigDecimal crAmount;
	}

	public static String[] amounts(BigDecimal d) {
		long x = d.multiply(new BigDecimal("100")).longValue();
		String s = "           ";
		if (x != 0) {
			s += String.valueOf(x);
			s = s.substring(s.length() - 11, s.length());
		}
		return s.split("");
	}

}
