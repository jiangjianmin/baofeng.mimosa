package com.guohuai.ams.investment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface InvestmentMeetingDao extends JpaRepository<InvestmentMeeting, String>, JpaSpecificationExecutor<InvestmentMeeting> {

	/**
	 * 根据会议编号查询会议
	 * 
	 * @param meetingSn
	 * @return
	 */
	public InvestmentMeeting findBySn(String meetingSn);
}
