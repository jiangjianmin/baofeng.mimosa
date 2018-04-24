package com.guohuai.mmp.investor.baseaccount;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.mmp.sys.SysConstant;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 投资人-基本账户
 * 
 * @author yuechao
 *
 */
@Entity
@Table(name = "T_MONEY_INVESTOR_BASEACCOUNT")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class InvestorBaseAccountEntity implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = -3103110921829885852L;

	/** 用户类型--投资者T1 */
	public static final String BASEACCOUNT_userType_T1 = "T1";
	
	/** 状态--正常 */
	public static final String BASEACCOUNT_status_normal = "normal";
	/** 状态--禁用 */
	public static final String BASEACCOUNT_status_forbidden = "forbidden";

	/** 账户所有者--投资者 */
	public static final String BASEACCOUNT_owner_investor = "investor";
	/** 账户所有者--平台 */
	public static final String BASEACCOUNT_owner_platform = "platform";
	
	/** 是新手 */
	public static final String BASEACCOUNT_isFreshMan_yes = "yes";
	/** 非新手 */
	public static final String BASEACCOUNT_isFreshMan_no = "no";
	
	/** 注册来源--后台添加 */
	public static final String BASEACCOUNT_SOURCE_backEnd = "backEnd";
	/** 注册来源--前台注册 */
	public static final String BASEACCOUNT_SOURCE_frontEnd = "frontEnd";

	/** 来源系统类型--mimosa */
	public static final String BASEACCOUNT_systemSource_mimosa = "mimosa";

	/**超级用户状态*/
	public static final String SUPER_MAN_STATUS = "bfsuperaccount";
	
	@Id
	protected String oid;
	
	/**
	 * 用户OID
	 */
	private String userOid;
	
	/**
	 * 会员OID
	 */
	private String memberId;

	/**
	 * 手机号
	 */
	private String phoneNum;

	/**
	 * 真实姓名
	 */
	private String realName;
	
	/**
	 * 身份证号
	 */
	private String idNum;
	
	/**
	 * 邀请用户标识
	 */
	private String uid;

	/**
	 * 状态
	 */
	private String status;
	
	/**
	 * 新手否
	 */
	private String isFreshMan;
	
//	/**登录密码 */
//	private String userPwd;
//	
//	/**随机密钥 */
//	private String salt;
//	
//	/**支付密码 */
//	private String payPwd;
//	
//	/**支付密码随机密钥 */
//	private String paySalt;
//	
//	/**注册来源 */
//	private String source;
//	
	/**注册渠道 */
	private String registerChannelId;

	/**
	 * 余额
	 */
	private BigDecimal balance = SysConstant.BIGDECIMAL_defaultValue;

	/**
	 * 账户所有者
	 */
	private String owner;

	private Timestamp updateTime;

	private Timestamp createTime;

}
