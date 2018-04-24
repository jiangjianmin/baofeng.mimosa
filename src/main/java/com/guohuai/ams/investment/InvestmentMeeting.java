package com.guohuai.ams.investment;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Entity;
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
 * 投资标的过会-会议实体类
 * 
 * @author Administrator
 *
 */

@Entity
@Table(name = "T_GAM_CONFERENCE")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@DynamicInsert
@DynamicUpdate
public class InvestmentMeeting extends UUID implements Serializable {

	private static final long serialVersionUID = -248787174503909861L;

	/**
	 * 过会状态
	 */
	public static final String MEETING_STATE_NOTOPEN = "notopen"; // 未举行
	public static final String MEETING_STATE_OPENING = "opening"; // 举行中
	public static final String MEETING_STATE_STOP = "stop"; // 暂停
	public static final String MEETING_STATE_WAITENTER = "waitenter"; //待确认
	public static final String MEETING_STATE_FINISH = "finish"; // 完成

	private String sn;
	private String title;
	private String state;
	private Timestamp conferenceTime;
	private String creator;
	private String operator;
	private Timestamp createTime;
	private Timestamp updateTime;
	public String fkey;
}
