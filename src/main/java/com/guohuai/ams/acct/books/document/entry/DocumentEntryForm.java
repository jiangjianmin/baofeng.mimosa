package com.guohuai.ams.acct.books.document.entry;

import java.sql.Date;

import lombok.Data;

@Data
public class DocumentEntryForm {

	private String relative;
	private Date startDate;
	private Date endDate;
	private int page;
	private int size;

}
