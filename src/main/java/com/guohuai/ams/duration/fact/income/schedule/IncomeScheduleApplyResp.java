package com.guohuai.ams.duration.fact.income.schedule;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import com.guohuai.basic.component.ext.web.BaseResp;

import lombok.EqualsAndHashCode;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@lombok.Builder
public class IncomeScheduleApplyResp extends BaseResp{
	
	private String oid;
	private Date basicDate; 	// 排期日期
	private BigDecimal annualizedRate;//年化收益率
	private String creator;// 申请人
	private Timestamp createTime;  // 申请时间
	private String approver;  // 审批人
	private Timestamp approverTime; // 审批时间
	private String status;// 状态    toApprove待审核,pass通过,reject驳回,delete删除
	private String type;//操作类型      new新建，update修改，delete删除
}
