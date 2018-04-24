package com.guohuai.mmp.investor.tradeorder;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import com.guohuai.mmp.sys.SysConstant;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@lombok.Data
@lombok.Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlatWareRep {
	
	/**
	 * 开始计息日
	 */
	private Date beginAccuralDate;
	
	/**
	 * 持有份额
	 */
	private BigDecimal holdVolume = SysConstant.BIGDECIMAL_defaultValue;
	
	/**
	 * 计息状态
	 */
	private String accrualStatus;
	
	private Timestamp completeTime;
}
