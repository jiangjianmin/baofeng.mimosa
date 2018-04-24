package com.guohuai.ams.duration.fact.calibration;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import lombok.Data;

/**
 * 市值校准
 * 
 * @author star.zhu
 *         2016年6月16日
 */
@Data
public class MarketAdjustForm implements Serializable {

	private static final long serialVersionUID = 1L;

	private String oid;
	private String assetpoolOid;
	// 上一个校准日
	private Date lastBaseDate;
	// 校准日
	private Date baseDate;
	// 份额
	private BigDecimal shares;
	// 单位净值
	private BigDecimal nav;
	// 净申购
	private BigDecimal purchase;
	// 净赎回
	private BigDecimal redemption;
	// 昨日净申赎
	private BigDecimal lastOrders;
	// 昨日份额
	private BigDecimal lastShares;
	// 昨日单位净值
	private BigDecimal lastNav;
	// 净收益
	private BigDecimal profit;
	// 收益率
	private BigDecimal ratio;
	// 申请人
	private String creator;
	// 申请时间
	private Timestamp createTime;
	// 审批人
	private String auditor;
	// 审批时间
	private Timestamp auditTime;
	// 状态 (待审核: CREATE;通过: PASS;驳回: FAIL;已删除: DELETE)
	private String status;
	
	private String aversion;
}
