package com.guohuai.ams.investment;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.guohuai.ams.investment.meeting.MeetingInvestmentDetResp;

@Service
@Transactional
public class InvestmentMeetingAssetService {

	@Autowired
	private InvestmentService investmentService;

	@Autowired
	private InvestmentMeetingAssetDao investmentMeetingAssetDao;

	@Autowired
	private InvestmentMeetingService investmentMeetingService;

	/**
	 * 获得过会投资标的列表
	 * 
	 * @param spec
	 * @param pageable
	 * @return
	 */
	public Page<InvestmentMeetingAsset> getMeetingAssetList(Specification<InvestmentMeetingAsset> spec, Pageable pageable) {
		return investmentMeetingAssetDao.findAll(spec, pageable);
	}

	/**
	 * 获得过会投资标的详情
	 * 
	 * @param oid
	 * @return
	 */
	public InvestmentMeetingAsset getMeetingAssetDet(String oid) {
		return investmentMeetingAssetDao.findOne(oid);
	}

	/**
	 * 新建或更新过会投资标的
	 * 
	 * @param entity
	 * @param operator
	 * @return
	 */
	public InvestmentMeetingAsset saveOrUpdateMeetingAsset(InvestmentMeetingAsset entity, String operator) {

		return this.investmentMeetingAssetDao.save(entity);
	}

	/**
	 * 根据会议获得标的
	 * 
	 * @param meeting
	 * @return
	 */
	public List<InvestmentMeetingAsset> getMeetingAssetListByMeeting(InvestmentMeeting meeting) {
		return investmentMeetingAssetDao.findByInvestmentMeeting(meeting);
	}

	/**
	 * 根据批量会议获得标的
	 * 
	 * @param meeting
	 * @return
	 */
	public List<InvestmentMeetingAsset> getMeetingAssetListByBathcMeeting(InvestmentMeeting[] meeting) {
		return investmentMeetingAssetDao.findByInvestmentMeetingIn(meeting);
	}

	/**
	 * 根据会议获得标的列表
	 * 
	 * @param meeting
	 * @return
	 */
	public List<MeetingInvestmentDetResp> getInvestmentByMeeting(String meetingOid) {
		InvestmentMeeting meeting = investmentMeetingService.getMeetingDet(meetingOid);
		List<InvestmentMeetingAsset> lists = this.getMeetingAssetListByMeeting(meeting);
		List<MeetingInvestmentDetResp> investments = new ArrayList<MeetingInvestmentDetResp>();
		for (InvestmentMeetingAsset temp : lists) {
			MeetingInvestmentDetResp entity = new MeetingInvestmentDetResp();
			BeanUtils.copyProperties(temp.getInvestment(), entity);
			entity.setMeetingOid(temp.getInvestmentMeeting().getOid());
			entity.setMeetingTitle(temp.getInvestmentMeeting().getTitle());
			entity.setMeetingTime(temp.getInvestmentMeeting().getConferenceTime());
			entity.setMeetingState(temp.getInvestmentMeeting().getState());
			investments.add(entity);
		}
		return investments;
	}

	public InvestmentMeeting getNewMeetingByInvestment(String investmentOid) {
		InvestmentMeeting res = new InvestmentMeeting();
		final Investment investment = investmentService.getInvestment(investmentOid);
		if (null == investment) {
			throw new RuntimeException();
		}
		Specification<InvestmentMeetingAsset> spec = new Specification<InvestmentMeetingAsset>() {
			@Override
			public Predicate toPredicate(Root<InvestmentMeetingAsset> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.equal(root.get("investment").as(Investment.class), investment);
			}
		};
		Direction sortDirection = Direction.DESC;
		Pageable pageable = new PageRequest(0, 1, new Sort(new Order(sortDirection, "investmentMeeting.createTime")));
		Page<InvestmentMeetingAsset> pageData = this.getMeetingAssetList(spec, pageable);
		if (null != pageData && pageData.getContent().size() > 0) {
			InvestmentMeetingAsset asset = pageData.getContent().get(0);
			BeanUtils.copyProperties(asset.getInvestmentMeeting(), res);
		}
		return res;
	}

}
