package com.guohuai.ams.acct.books.document.entry;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class DocumentEntryPage {
	private long total;
	private long number;

	private List<DocumentEntryResp> rows = new ArrayList<DocumentEntryResp>();
}
