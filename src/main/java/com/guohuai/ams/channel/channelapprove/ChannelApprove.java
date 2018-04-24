package com.guohuai.ams.channel.channelapprove;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.ams.channel.Channel;
import com.guohuai.component.persist.UUID;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
@Entity
@Table(name = "T_MONEY_PLATFORM_CHANNEL_APPROVAL")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class ChannelApprove extends UUID implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 渠道审批申请类型
	 */
	public static final String CHANAPPROVE_REQUESTTYPE_ON = "on"; //申请开启
	public static final String CHANAPPROVE_REQUESTTYPE_OFF = "off"; //申请关闭
	
	/**
	 * 渠道审批结果
	 */
	public static final String CHANAPPROVE_APPROVERESULT_PASS = "pass"; //通过
	public static final String CHANAPPROVE_APPROVERESULT_REFUSED = "refused"; //驳回
	public static final String CHANAPPROVE_APPROVESTATUS_toApprove = "toApprove"; //待审核
	/**
	 * 所属渠道表
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "channelOid", referencedColumnName = "oid")
	private Channel channel;
	/**
	 * 渠道名称
	 */
	private String channelName;
	/**
	 * 渠道审批编号
	 */
	private String channelApproveCode;
	/**
	 * 申请类型
	 */
	private String requestType;
	/**
	 * 申请人
	 */
	private String requester;
	/**
	 * 申请时间
	 */
	private Timestamp requestTime;	
	/**
	 * 审批人
	 */
	private String approveMan;
	/**
	 * 审批结果
	 */
	private String approveStatus;
	/**
	 * 评论
	 */
	private String remark;
	/**
	 * 更新时间
	 */
	private Timestamp updateTime;
	/**
	 * 创建时间
	 */
	private Timestamp createTime;

}
