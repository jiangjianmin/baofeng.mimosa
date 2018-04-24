package com.guohuai.mmp.platform.statistics;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "T_USER_INVEST_STATISTICS")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class UserInvestStatisticsEntity implements Serializable {
	
	private static final long serialVersionUID = 4997163132288510008L;

	@Id
	protected String oid;
	
	private String channelOid;
	
	private int investorPeopleNum;
	
	private BigDecimal orderAmount;
	
	private int investorPenNum;
	
	private int investorPeopleGoldNum;
	
	private BigDecimal orderAmountGold;
	
	private int investorPenGoldNum;
	
	private Date sortTime;
	
	private Date createTime;
}
