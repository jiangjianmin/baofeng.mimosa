package com.guohuai.ams.investment;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface InvestmentMeetingUserDao extends JpaRepository<InvestmentMeetingUser, String>, JpaSpecificationExecutor<InvestmentMeetingUser> {

	/**
	 * 根据会议查询参会人列表
	 * 
	 * @param investmentMeeting
	 * @return
	 */
	public List<InvestmentMeetingUser> findByInvestmentMeeting(InvestmentMeeting investmentMeeting);

	/**
	 * 根据会议查询参会人列表
	 * 
	 * @param investmentMeeting
	 * @return
	 */
	public List<InvestmentMeetingUser> findByParticipantOid(String participantOid);

	/**
	 * 根据参会人Id和会议查找到参会人信息
	 * 
	 * @param participantOid
	 * @param investmentMeeting
	 * @return
	 */
	public InvestmentMeetingUser findByParticipantOidAndInvestmentMeeting(String participantOid, InvestmentMeeting investmentMeeting);
}
