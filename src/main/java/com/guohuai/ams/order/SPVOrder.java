package com.guohuai.ams.order;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.ams.duration.assetPool.AssetPoolEntity;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountEntity;
import com.guohuai.mmp.publisher.hold.PublisherHoldEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 投资人-交易委托单
 * 
 * @author wangyan
 *
 */
@Entity
@Table(name = "T_GAM_SPV_ORDER")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
public class SPVOrder implements Serializable {

	private static final long serialVersionUID = 6889208404035299069L;

	/**
	 * 交易类型orderType：
	 */
	public static final String ORDER_TYPE_Invest = "INVEST";// 申购
	public static final String ORDER_TYPE_Redeem = "REDEEM";// 赎回
	public static final String ORDER_TYPE_BuyIn = "BUY_IN";// 买入
	public static final String ORDER_TYPE_PartSellOut = "PART_SELL_OUT";// 部分卖出
	public static final String ORDER_TYPE_FullSellOut = "FULL_SELL_OUT";// 全部卖出

	/**
	 * 订单类型orderCate：
	 */
	public static final String ORDER_CATE_Trade = "TRADE";// 交易订单
	public static final String ORDER_CATE_Strike = "STRIKE";// 冲账订单

	/**
	 * 订单状态orderStatus：
	 */
	public static final String STATUS_Submit = "SUBMIT";// 未确认
	public static final String STATUS_Confirm = "CONFIRM";// 确认
	public static final String STATUS_Disable = "DISABLE";// 失效
	public static final String STATUS_Calcing = "CALCING";// 清算中

	/**
	 * 订单入账状态entryStatus：
	 */
	public static final String ENTRY_STATUS_No = "NO";// 未入账
	public static final String ENTRY_STATUS_Yes = "YES";// 已入账

	@Id
	private String oid;// 序号
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "spvOid", referencedColumnName = "oid")
	private PublisherBaseAccountEntity spv;//关联发行人
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assetPoolOid", referencedColumnName = "oid")
	private AssetPoolEntity assetPool;//关联资产池

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "holdOid", referencedColumnName = "oid")
	private PublisherHoldEntity hold;//关联持有人手册

	private String orderCode;// 订单号
	private String orderType;// 交易类型
	private String orderCate;// 订单类型
	private BigDecimal orderAmount = new BigDecimal(0);// 订单金额(申请金额)
	private Date orderDate;// 订单日期
	private BigDecimal orderVolume = new BigDecimal(0);// 订单份额
	private String orderStatus;// 订单状态
	private String entryStatus;// 订单入账状态
	private BigDecimal payFee = new BigDecimal(0);// 手续费
	private String creater;// 订单创建人
	private Timestamp createTime;// 订单创建时间
	private String auditor;// 订单审核人
	private Timestamp completeTime;// 订单完成时间
	private Timestamp updateTime;// 订单修改时间

}
