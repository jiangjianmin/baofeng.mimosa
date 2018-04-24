package com.guohuai.ams.acct.account;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "T_ACCT_ACCOUNT")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
public class Account implements Serializable {

	private static final long serialVersionUID = -3216928216493655570L;

	// 科目类别: 资产
	public static final String TYPE_Assets = "ASSETS";
	// 科目类别: 负债
	public static final String TYPE_Liability = "LIABILITY";
	// 科目类别: 权益
	public static final String TYPE_Equity = "EQUITY";

	// 余额方向: 借
	public static final String DIRECTION_Dr = "Dr";
	// 余额方向: 贷
	public static final String DIRECTION_Cr = "Cr";

	// 状态: 启用
	public static final String STATE_Enable = "ENABLE";
	// 状态: 禁用
	public static final String STATE_Disable = "DISABLE";

	@Id
	private String oid;
	private String type;
	private String code;
	private String name;
	private String direction;
	private String state;
	private String parent;
}
