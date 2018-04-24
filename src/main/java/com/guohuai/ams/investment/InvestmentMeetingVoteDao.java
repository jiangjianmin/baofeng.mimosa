package com.guohuai.ams.investment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface InvestmentMeetingVoteDao extends JpaRepository<InvestmentMeetingVote, String>, JpaSpecificationExecutor<InvestmentMeetingVote> {

	/**
	 * 根据会议、投资标的和投票人查询投票结果
	 * 
	 * @param meetingOid
	 * @param Investment
	 * @return
	 */
	public InvestmentMeetingVote findByInvestmentMeetingAndInvestmentAndInvestmentMeetingUser(InvestmentMeeting investmentMeeting, Investment investment, InvestmentMeetingUser investmentMeetingUser);
}
