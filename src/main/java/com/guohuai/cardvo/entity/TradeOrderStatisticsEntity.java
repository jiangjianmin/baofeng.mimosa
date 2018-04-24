package com.guohuai.cardvo.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.DynamicUpdate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Desc: 订单统计表
 * @author huyong
 * @date 2017.10.24
 */
@Data
@Entity
@Table(name = "t_money_investor_tradeorder_statistics")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@DynamicUpdate
public class TradeOrderStatisticsEntity implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4297510926373741092L;
	
	/**
	 * oid
	 */
	@Id
	private	String	oid;
	
	/**
	 * 投资人id（用户oid）
	 */
	private	String 	investorOid;
	
	/**
	 * 累计投资金额
	 */
	private	BigDecimal 	totalInvestAmount;
	
	/**
	 * 累计投资笔数
	 */
	private	Integer		totalInvestCount;
	
	/**
	 * 末次交易时间
	 */
	private Timestamp 	lastTradeTime;
	
	/**
	 * 创建时间
	 */
	private Timestamp	createTime;
	
	/**
	 * 更新时间
	 */
	private Timestamp	updateTime;
}
