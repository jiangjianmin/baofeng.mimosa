package com.guohuai.mmp.platform.inform;

import java.sql.Timestamp;



import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@lombok.Builder
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class InformQueryRep {
	
	/**
	 * 通知OID
	 */
	String informOid;
	
	/**
	 * 通知编号
	 */
	String informCode;
	/**
	 * 通知类型
	 */
	String informType;
	String informTypeDisp;
	/**
	 * 通知内容
	 */
	String informContent;
	Timestamp updateTime;
	Timestamp createTime;
}
