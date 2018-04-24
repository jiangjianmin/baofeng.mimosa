package com.guohuai.ams.investment.manage;

import java.sql.Timestamp;

import com.guohuai.ams.investment.InvestmentMeetingCheck;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 检查项列表详情返回域
 * 
 * @author lirong
 *
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InvestmentCheckDetResp {

	public InvestmentCheckDetResp(InvestmentMeetingCheck entity) {
		this.id = entity.getOid();
		this.text = entity.getTitle();
		this.state = entity.getState();
		this.remark = entity.getCheckDesc();
		this.file = entity.getCheckFile();
		this.time = entity.getCheckTime();
		this.checker = entity.getChecker();
	}

	private String id;
	private String text;
	private String state;
	private String remark;
	private String file;
	private Timestamp time;
	private String checker;

}
