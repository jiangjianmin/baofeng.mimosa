package com.guohuai.ams.investment;

import java.io.Serializable;

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
 * 标的过会-参会人员实体类
 * 
 * @author lirong
 *
 */
@Entity
@Table(name = "T_GAM_CONFERENCE_PARTICIPANT")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@DynamicInsert
@DynamicUpdate
public class InvestmentMeetingUser extends UUID implements Serializable {

	private static final long serialVersionUID = 995306725255235254L;
	/**
	 * 会议
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "conferenceOid", referencedColumnName = "oid")
	private InvestmentMeeting investmentMeeting;
	/**
	 * 参会人员
	 */
	private String participantOid;
}
