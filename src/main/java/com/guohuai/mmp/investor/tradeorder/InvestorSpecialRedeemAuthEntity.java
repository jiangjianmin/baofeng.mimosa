package com.guohuai.mmp.investor.tradeorder;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.ams.channel.Channel;
import com.guohuai.ams.product.Product;
import com.guohuai.component.persist.UUID;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountEntity;
import com.guohuai.mmp.platform.finance.check.PlatformFinanceCheckEntity;
import com.guohuai.mmp.platform.investor.offset.InvestorOffsetEntity;
import com.guohuai.mmp.platform.publisher.offset.PublisherOffsetEntity;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountEntity;
import com.guohuai.mmp.publisher.hold.PublisherHoldEntity;
import com.guohuai.mmp.sys.SysConstant;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 投资人-交易委托单
 * 
 * @author yuechao
 *
 */
@Entity
@Table(name = "T_MONEY_INVESTOR_SPECIALREDEEM_AUTH")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class InvestorSpecialRedeemAuthEntity extends UUID {
	
	private static final long serialVersionUID = 4333179226422640561L;

	/**
	 * 用户id
	 */
	private String userId;

	/**
	 * 订单类型
	 */
	private String orderType;

	/**
	 * 授权金额
	 */
	private BigDecimal authAmount = SysConstant.BIGDECIMAL_defaultValue;

	/**
	 * 订单状态
	 */
	private String operateStatus;

	/**
	 * 授权开始时间
	 */
	@Temporal(TemporalType.TIMESTAMP)
	private Date startTime;
	/**
	 * 授权结束时间
	 */
	@Temporal(TemporalType.TIMESTAMP)
	private Date endTime;

	private Timestamp createTime;

	private Timestamp updateTime;

}
