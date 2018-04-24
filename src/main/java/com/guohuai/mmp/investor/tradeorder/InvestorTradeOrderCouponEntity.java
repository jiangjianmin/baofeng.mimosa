package com.guohuai.mmp.investor.tradeorder;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

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
 * 加息券订单关联表
 * @author sunjian
 *
 */
@Entity
@Table(name = "T_MONEY_INVESTOR_TRADEORDER_COUPON")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class InvestorTradeOrderCouponEntity extends UUID {
	private static final long serialVersionUID = 526126676097740361L;

	private String orderOid;
	private BigDecimal interest;
	private Integer interestDays;
}
