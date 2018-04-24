package com.guohuai.mmp.publisher.product.rewardincomepractice;

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

import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.reward.ProductIncomeReward;
import com.guohuai.component.persist.UUID;
import com.guohuai.mmp.sys.SysConstant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 发行人-产品-奖励收益预算
 * 
 * @author yuechao
 *
 */
@Entity
@Table(name = "T_MONEY_PUBLISHER_PRODUCT_REWARDINCOMEPRACTICE")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Builder
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
public class PracticeEntity extends UUID {
	/**
	* 
	*/
	private static final long serialVersionUID = 8787981684536508116L;
	

	
	/**
	 * 所属产品
	 */
	@JoinColumn(name = "productOid", referencedColumnName = "oid")
	@ManyToOne(fetch = FetchType.LAZY)
	Product product;
	
	/**
	 * 所属奖励规则
	 */
	@JoinColumn(name = "rewardRuleOid", referencedColumnName = "oid")
	@ManyToOne(fetch = FetchType.LAZY)
	ProductIncomeReward reward;
	
	/**
	 * 持有人总份额
	 */
	BigDecimal totalHoldVolume = SysConstant.BIGDECIMAL_defaultValue;
	/**
	 * 奖励收益
	 */
	BigDecimal totalRewardIncome = SysConstant.BIGDECIMAL_defaultValue;
	
	/**
	 * t日
	 */
	Date tDate;
	
	Timestamp updateTime, createTime;
}
