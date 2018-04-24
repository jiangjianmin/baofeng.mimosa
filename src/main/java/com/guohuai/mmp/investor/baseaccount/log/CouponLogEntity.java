package com.guohuai.mmp.investor.baseaccount.log;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Table;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import com.guohuai.component.persist.UUID;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "T_MONEY_COUPON_LOG")
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@lombok.Data
@EqualsAndHashCode(callSuper = true)
public class CouponLogEntity extends UUID{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4364428130988103276L;
	public static final String STATUS_SUCCESS="SUCCESS";
	public static final String STATUS_FAILED="FAILED";
	public static final String TYPE_REGISTER="REGISTER";
	public static final String TYPE_REFEREE="REFEREE";
	public static final String TYPE_INVEST="INVEST";
	
	/** 状态 SUCCESS:FAILED */
	private String status;
	
	/** 类型，REGISTER:注册,REFEREE:推荐人 */
	private String type;

	/** 已发送次数 */
	private Integer sendedTimes;
	
	/** 最多发送次数 */
	private Integer limitSendTimes;
	
	/** 下次调用时间 */
	private Timestamp nextNotifyTime;

	/** 用户ID */
	private String userOid;
	/** 渠道CID */
	private String channelCid;

	private Timestamp createTime;

	private Timestamp updateTime;

	
	
}
