package com.guohuai.ams.label;

import java.io.Serializable;
import java.sql.Timestamp;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 平台-标签
 * 
 * @author wangyan
 *
 */
@Entity
@Table(name = "T_MONEY_PLATFORM_LABEL")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
public class LabelEntity implements Serializable {

	private static final long serialVersionUID = -3945312811927237200L;
	
	/**
	 * isOk：
	 */
	public static final String isOk_yes = "yes";// 可用
	public static final String isOk_no = "no";// 不可用
	
	/**
	 * isOk：
	 */
	public static final String labelType_general = "general";//基本
	public static final String labelType_extend = "extend";//扩展
	
	@Id
	private String oid;
	private String labelCode;//标签代码
	private String labelName;//标签名称
	private String labelType;//标签类型
	private String isOk;//是否可用
	private String labelDesc;//标签描述
	private Timestamp updateTime;
	private Timestamp createTime;

}
