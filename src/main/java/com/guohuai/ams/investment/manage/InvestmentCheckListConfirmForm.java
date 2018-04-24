package com.guohuai.ams.investment.manage;

import lombok.Data;
/**
 * 确认检查项form
 * 
 * @author lirong
 *
 */
@Data
public class InvestmentCheckListConfirmForm {

	private String id;
	private String text;
	private String state;
	private String remark;
	private String file;
	private String fileName;
	private String fileSize;
	private Boolean checked;
}
