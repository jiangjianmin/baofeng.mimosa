package com.guohuai.ams.duration.fact.income.schedule;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import com.guohuai.basic.component.ext.web.BaseResp;

import lombok.EqualsAndHashCode;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@lombok.Builder
public class IncomeScheduleResp extends BaseResp{
	
	private String oid;
	private String assetPoolOid;	// 资产池oid
	private Date basicDate; 	// 排期日期
	private BigDecimal annualizedRate;//年化收益率
	private String errorMes;	//失败原因
	private String status;		// 状态    toApprove待审核，reject驳回，pass待执行，finish已完成，fail失败
	private Timestamp updateTime; 
	
	private String shouldDate;
}
