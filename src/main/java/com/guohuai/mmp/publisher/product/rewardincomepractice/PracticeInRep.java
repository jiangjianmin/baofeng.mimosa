package com.guohuai.mmp.publisher.product.rewardincomepractice;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import com.guohuai.component.web.view.BaseRep;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@lombok.Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PracticeInRep extends BaseRep {

	/**
	 * 起始天数
	 */
	Integer startDate;
	/**
	 * 截止天数
	 */
	Integer endDate;

	/**
	 * 所属奖励规则
	 */
	BigDecimal rewardRatio;
	
	/**
	 * 奖励规则OID
	 */
	String rewardOid;
	
	/**
	 * 产品OID
	 */
	String productOid;

	/**
	 * 持有人总份额
	 */
	BigDecimal totalHoldVolume;
	/**
	 * 奖励收益
	 */
	BigDecimal totalRewardIncome;
	/**
	 * 市值
	 */
	long value;

	/**
	 * 奖励收益阶段
	 */
	String level;
	/**
	 * t日
	 */
	Date tDate;

	Timestamp updateTime, createTime;
}
