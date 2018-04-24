package com.guohuai.ams.investment.log;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.ams.investment.Investment;
import com.guohuai.component.persist.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
/**
 * 标的变动记录
 * 
 * @author Administrator
 *
 */
@Entity
@Table(name = "T_GAM_TARGET_LOG")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@DynamicInsert
@DynamicUpdate
public class InvestmentLog extends UUID{

	
	private static final long serialVersionUID = -607148389431213811L;
	/**
	 * 标的变动类型
	 * create 新建
	 * edit 编辑
	 * check 提交预审
	 * checkpass 预审通过
	 * checkreject 预审驳回
	 * invalid 作废
	 */
	public static final String INVESTMENT_LOG_TYPE_create = "create";
	public static final String INVESTMENT_LOG_TYPE_edit = "edit";
	public static final String INVESTMENT_LOG_TYPE_check = "check";
	public static final String INVESTMENT_LOG_TYPE_checkpass = "checkpass";
	public static final String INVESTMENT_LOG_TYPE_checkreject = "checkreject";
	public static final String INVESTMENT_LOG_TYPE_invalid = "invalid";
	
	/**
	 * 投资标的
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "targetOid", referencedColumnName = "oid")
	private Investment investment;
	/**
	 * 变动时间
	 */
	private Timestamp eventTime;
	/**
	 * 操作员
	 */
	private String operator;
	/**
	 * 变动类型
	 */
	private String eventType;
}
