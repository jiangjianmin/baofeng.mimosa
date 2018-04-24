package com.guohuai.mmp.platform.publisher.offsetlog;

import java.math.BigDecimal;
import java.sql.Timestamp;

import com.guohuai.component.persist.UUID;
import com.guohuai.mmp.sys.SysConstant;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 平台-发行人-轧差
 * 
 * @author yuechao
 *
 */
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class PublisherOffsetLogEntity extends UUID {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6648276057796947507L;
	
	Integer successPeopleNum = SysConstant.INTEGER_defaultValue;
	Integer failurePeopleNum = SysConstant.INTEGER_defaultValue;
	
	BigDecimal investAmount = SysConstant.BIGDECIMAL_defaultValue, redeemAmount = SysConstant.BIGDECIMAL_defaultValue;

	private Timestamp updateTime;
	private Timestamp createTime;
}
