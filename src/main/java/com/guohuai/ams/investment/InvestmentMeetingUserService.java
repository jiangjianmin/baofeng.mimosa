package com.guohuai.ams.investment;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class InvestmentMeetingUserService {

	@Autowired
	private InvestmentMeetingUserDao investmentMeetingUserDao;

	/**
	 * 获得投资标的过会人员列表
	 * 
	 * @param spec
	 * @param pageable
	 * @return
	 */
	public Page<InvestmentMeetingUser> getMeetingUserList(Specification<InvestmentMeetingUser> spec,
			Pageable pageable) {
		return investmentMeetingUserDao.findAll(spec, pageable);
	}

	/**
	 * 获得投资标的过会人员详情
	 * 
	 * @param oid
	 * @return
	 */
	public InvestmentMeetingUser getMeetingUserDet(String oid) {
		return investmentMeetingUserDao.findOne(oid);
	}

	public InvestmentMeetingUser getMeetingUserDetByUserAndMeeting(String participantOid, InvestmentMeeting meeting) {
		return investmentMeetingUserDao.findByParticipantOidAndInvestmentMeeting(participantOid, meeting);
	}

	/**
	 * 新建投资标的过会人员
	 * 
	 * @param entity
	 * @param operator
	 * @return
	 */
	public InvestmentMeetingUser saveOrUpdateInvestment(InvestmentMeetingUser entity, String operator) {
		return this.investmentMeetingUserDao.save(entity);
	}

	/**
	 * 根据会议查询参会人列表
	 * 
	 * @param meeting
	 * @return
	 */
	public List<InvestmentMeetingUser> getMeetingUserByMeeting(InvestmentMeeting meeting) {
		return investmentMeetingUserDao.findByInvestmentMeeting(meeting);
	}

	/**
	 * 根据会议查询参会人列表
	 * 
	 * @param meeting
	 * @return
	 */
	public List<InvestmentMeetingUser> getMeetingUserByUser(String participantOid) {
		return investmentMeetingUserDao.findByParticipantOid(participantOid);
	}

}
