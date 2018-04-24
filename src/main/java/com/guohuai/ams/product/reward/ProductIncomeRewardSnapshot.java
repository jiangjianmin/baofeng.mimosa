package com.guohuai.ams.product.reward;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.ams.product.Product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "T_GAM_INCOME_REWARD_SNAPSHOT")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
public class ProductIncomeRewardSnapshot implements Serializable {
	
	private static final long serialVersionUID = 6518480407983911105L;

	@Id
	private String oid;// 序号
	
	private String productOid;//关联产品
	
	private String level;//阶梯名称
	private Integer startDate = 0;//起始天数	
	private Integer endDate = 0;//截止天数
	private BigDecimal ratio = new BigDecimal(0);//奖励收益率
	private BigDecimal dratio = new BigDecimal(0); // 日收益率
	private Date snapshotDate;
}
