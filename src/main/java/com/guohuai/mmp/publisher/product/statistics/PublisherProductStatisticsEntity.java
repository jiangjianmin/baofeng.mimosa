package com.guohuai.mmp.publisher.product.statistics;

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

import com.guohuai.ams.product.Product;
import com.guohuai.component.persist.UUID;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountEntity;
import com.guohuai.mmp.sys.SysConstant;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 发行人-产品-统计
 * 
 * @author wanglei
 *
 */
@Entity
@Table(name = "T_MONEY_PUBLISHER_PRODUCT_STATISTICS")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@lombok.Builder
@DynamicInsert
@DynamicUpdate
public class PublisherProductStatisticsEntity extends UUID {

	private static final long serialVersionUID = -3840803257302759632L;

	/**
	 * 所属发行人
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "publisherOid", referencedColumnName = "oid")
	PublisherBaseAccountEntity publisherBaseAccount;

	/**
	 * 所属产品
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "productOid", referencedColumnName = "oid")
	Product product;

	/** 当日投资金额 */
	private BigDecimal investAmount = SysConstant.BIGDECIMAL_defaultValue;

	/** 当日投资排名 */
	private Integer investRank = SysConstant.INTEGER_defaultValue;
	
	/** 累计投资金额 */
	private BigDecimal totalInvestAmount = SysConstant.BIGDECIMAL_defaultValue;

	/** 累计投资排名 */
	private Integer totalInvestRank = SysConstant.INTEGER_defaultValue;

	/** 投资日 */
	private Date investDate;

	private Timestamp updateTime;

	private Timestamp createTime;

}
