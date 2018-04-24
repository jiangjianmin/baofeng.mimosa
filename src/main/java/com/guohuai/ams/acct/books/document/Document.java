package com.guohuai.ams.acct.books.document;

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

import com.guohuai.ams.acct.doc.word.DocWord;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "T_ACCT_DOCUMENT")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
public class Document implements Serializable {

	private static final long serialVersionUID = 4672944310612688105L;

	// 现金管理工具 调仓
	public static final String TYPE_CASHTOOL_WAREHOUSE = "CASHTOOL_WAREHOUSE";
	// 信托计划 申购
	public static final String TYPE_TARGET_PURCHASE = "TARGET_PURCHASE";
	// 信托计划 转让
	public static final String TYPE_TARGET_TRANSFER = "TARGET_TRANSFER";
	// 信托计划 本息兑付
	public static final String TYPE_TARGET_REDEMPTION = "TARGET_REDEMPTION";

	// 收益确认 银行存款
	public static final String TYPE_INCOME_DEPOSIT = "DEPOSIT_INCOME";
	// 收益确认 投资标的
	public static final String TYPE_INCOME_TARGET = "TARGET_INCOME";
	// 收益确认 现金管理工具
	public static final String TYPE_INCOME_CASHTOOL = "CASHTOOL_INCOME";
	// 收益确认 应收勾销
	public static final String TYPE_INCOME_RECEIVE = "RECEIVE_INCOME";

	// 收益分配
	public static final String TYPE_ASSETPOOL_ALLOCATE = "ASSETPOOL_ALLOCATE";

	// 费金计提
	public static final String TYPE_ACCRUED_CHARGES = "ACCRUED_CHARGES";
	// 费金提取
	public static final String TYPE_EXTRACTION_COST = "EXTRACTION_COST";

	// 清算
	public static final String TYPE_CLEARING = "CLEARING";
	// 结算
	public static final String TYPE_SETTLEMENT = "SETTLEMENT";

	// 备付金
	public static final String TYPE_EXESS_RESERVE = "EXESS_RESERVE";

	// 股指校准 - 投资标的
	public static final String TYPE_ESTIMATE_CORRECT_TARGET = "TARGET_ESTIMATE_CORRECT";
	// 股指校准 - 现金管理工具
	public static final String TYPE_ESTIMATE_CORRECT_CASHTOOL = "CASHTOOL_ESTIMATE_CORRECT";

	// SPV交易
	public static final String TYPE_SPV_TRADE = "SPV_TRADE";
	// SPV收益确认/资管计划收益确认
	public static final String TYPE_SPV_INCOME_CONFIRM = "SPV_INCOME_CONFIRM";
	// SPV收益发放
	public static final String TYPE_SPV_INCOME_ALLOCATE = "SPV_INCOME_ALLOCATE";
	// SPV份额交收
	public static final String TYPE_SPV_TRADE_CONFIRM = "SPV_TRADE_CONFIRM";

	@Id
	private String oid;
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "docwordOid", referencedColumnName = "oid")
	private DocWord docword;
	private String type;
	private String relative;
	private String ticket;
	private int acctSn;
	private Date acctDate;
	private int invoiceNum;
	private BigDecimal drAmount;
	private BigDecimal crAmount;
	private Timestamp createTime;
	private Timestamp updateTime;

}
