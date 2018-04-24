package com.guohuai.ams.investment;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.component.persist.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 过会检查项实体类
 * 
 * @author lirong
 *
 */
@Entity
@Table(name = "T_GAM_TARGET_CHECKLIST")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@DynamicInsert
@DynamicUpdate
public class InvestmentMeetingCheck extends UUID implements Serializable {

	private static final long serialVersionUID = -4023827249470662421L;

	/**
	 * 过会检查项状态 check 已检查 notcheck 未检查
	 * 
	 */
	public static final String MEETINGCHEC_STATUS_check = "check";
	public static final String MEETINGCHEC_STATUS_notcheck = "notcheck";

	/**
	 * 投资标的
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "targetOid", referencedColumnName = "oid")
	private Investment investment;
	/**
	 * 会议
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "conferenceOid", referencedColumnName = "oid")
	private InvestmentMeeting InvestmentMeeting;
	/**
	 * 检查项名称
	 */
	private String title;
	/**
	 * 检查状态
	 */
	private String state;
	/**
	 * 检查成功说明
	 */
	private String checkDesc;
	/**
	 * 检查成功附件
	 */
	private String checkFile;
	/**
	 * 检查人
	 */
	private String checker;
	/**
	 * 检查时间
	 */
	private Timestamp checkTime;

}
