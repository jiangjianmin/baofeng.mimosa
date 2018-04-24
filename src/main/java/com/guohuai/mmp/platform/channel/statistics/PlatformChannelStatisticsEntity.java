package com.guohuai.mmp.platform.channel.statistics;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.ams.channel.Channel;
import com.guohuai.component.persist.UUID;
import com.guohuai.mmp.sys.SysConstant;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 资管-渠道-统计
 * 
 * @author wanglei
 *
 */
@Entity
@Table(name = "T_MONEY_PLATFORM_CHANNEL_STATISTICS")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class PlatformChannelStatisticsEntity extends UUID implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4962497564579911579L;

	/**
	 * 所属渠道
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "channelOid", referencedColumnName = "oid")
	private Channel channel;

	/**
	 * 当日投资金额
	 */
	private BigDecimal todayInvestAmount = SysConstant.BIGDECIMAL_defaultValue;

	/**
	 * 当日赎回金额
	 */
	private BigDecimal todayRedeemAmount = SysConstant.BIGDECIMAL_defaultValue;

	/**
	 * 当日还本付息金额
	 */
	private BigDecimal todayCashAmount = SysConstant.BIGDECIMAL_defaultValue;

	/**
	 * 累计投资金额
	 */
	private BigDecimal totalInvestAmount = SysConstant.BIGDECIMAL_defaultValue;

	/**
	 * 累计赎回金额
	 */
	private BigDecimal totalRedeemAmount = SysConstant.BIGDECIMAL_defaultValue;

	/**
	 * 累计还本付息金额
	 */
	private BigDecimal totalCashAmount = SysConstant.BIGDECIMAL_defaultValue;

	/** 投资日期 */
	private Date investDate;

	private Timestamp updateTime;
	private Timestamp createTime;
}
