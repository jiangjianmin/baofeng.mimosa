package com.guohuai.ams.supplement.mechanism;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.component.persist.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**补单机构
 * @author qiuliang
 *
 */
@Entity
@Table(name = "t_money_supplement_mechanism")
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
public class Mechanism  extends UUID {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3035434926013041918L;

	/**
	 * 已启用
	 */
	public static final String Enable = "enable";
	
	private String fullName;//机构全称
	private String shotName;//机构简称
	private String contactMan;//联系人
	private String contactPhone;//联系方式
	private String account;//账户
	private String bankName;//银行名
	private String accountBank;//开户行
	private String remark;//备注
	private String status;//状态
	private String operator;//操作人
	@Temporal(TemporalType.TIMESTAMP)
	private Date updateTime;//更新时间
	@Temporal(TemporalType.TIMESTAMP)
	private Date createTime;//创建时间
	
	
	
	

}
