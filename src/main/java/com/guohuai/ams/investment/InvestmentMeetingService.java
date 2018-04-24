package com.guohuai.ams.investment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.guohuai.ams.enums.TargetEventType;
import com.guohuai.ams.investment.log.InvestmentLogService;
import com.guohuai.ams.investment.meeting.AddInvestmentMeetingForm;
import com.guohuai.ams.investment.meeting.MeetingFinishForm;
import com.guohuai.ams.investment.meeting.MeetingInvestmentDetResp;
import com.guohuai.ams.investment.meeting.SummaryDetResp;
import com.guohuai.ams.investment.meeting.SummaryFileDet;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.StringUtil;
import com.guohuai.file.File;
import com.guohuai.file.FileService;
import com.guohuai.file.SaveFileForm;
import com.guohuai.operate.api.AdminSdk;
import com.guohuai.operate.api.objs.admin.AdminObj;

@Service
@Transactional
public class InvestmentMeetingService {

	@Autowired
	private InvestmentMeetingDao investmentMeetingDao;

	@Autowired
	private InvestmentMeetingAssetDao investmentMeetingAssetDao;

	@Autowired
	private InvestmentMeetingUserDao investmentMeetingUserDao;

	@Autowired
	private InvestmentService investmentService;

	@Autowired
	private FileService fileService;

	@Autowired
	private InvestmentMeetingCheckService investmentMeetingCheckService;

	@Autowired
	private InvestmentLogService investmentLogService;

	@Autowired
	private AdminSdk adminSdk;

//	@Autowired
//	private WorkflowAssetService workflowAssetService;

	/**
	 * 获得投资标的过会列表
	 * 
	 * @param spec
	 * @param pageable
	 * @return
	 */
	public Page<InvestmentMeeting> getMeetingList(Specification<InvestmentMeeting> spec, Pageable pageable) {
		return investmentMeetingDao.findAll(spec, pageable);
	}

	/**
	 * 获得投资标的过会详情
	 * 
	 * @param oid
	 * @return
	 */
	public InvestmentMeeting getMeetingDet(String oid) {
		return investmentMeetingDao.findOne(oid);
	}

	/**
	 * 新建标的会议
	 * 
	 * @param entity
	 * @param operator
	 * @return
	 */
	public InvestmentMeeting saveMeeting(InvestmentMeeting entity, String operator) {
		entity.setCreateTime(DateUtil.getSqlCurrentDate());
		entity.setUpdateTime(DateUtil.getSqlCurrentDate());
		entity.setOperator(operator);
		return this.investmentMeetingDao.save(entity);
	}

	/**
	 * 修改投资标的
	 * 
	 * @param entity
	 * @param operator
	 * @return
	 */
	public InvestmentMeeting updateMeeting(InvestmentMeeting entity, String operator) {
		entity.setUpdateTime(DateUtil.getSqlCurrentDate());
		entity.setOperator(operator);
		return this.investmentMeetingDao.save(entity);
	}

	/**
	 * 创建过会信息
	 * 
	 * @param form
	 * @param operator
	 * @return
	 */
	@Transactional
	public InvestmentMeeting addMeeting(AddInvestmentMeetingForm form, String operator) {
		try {
			InvestmentMeeting temp = investmentMeetingDao.findBySn(form.getSn());
			if (null != temp)
				throw new RuntimeException("会议编号重复");
			InvestmentMeeting entity = InvestmentMeeting.builder().conferenceTime(form.getConferenceTime())
					.creator(operator).createTime(DateUtil.getSqlCurrentDate()).sn(form.getSn())
					.state(InvestmentMeeting.MEETING_STATE_NOTOPEN).title(form.getTitle()).build();
			InvestmentMeeting meeting = this.saveMeeting(entity, operator);
			this.addTargetStr(form.getTarget(), meeting, operator);
			this.addParticipant(form.getParticipant(), meeting);
			// 工作流
//			for (String oid : targets) {
//				workflowAssetService.complete(operator, oid, WorkflowConstant.NODEID_COMITMEETING, "pass",
//						participants);
//			}
			return meeting;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	/**
	 * 添加参会人
	 * 
	 * @param parStr
	 * @param meeting
	 * @return
	 * @throws Exception
	 */
	private Set<String> addParticipant(String parStr, InvestmentMeeting meeting) throws Exception {
		Set<String> res = new HashSet<String>();
		String[] arr = parStr.split(",");
		for (String oid : arr) {
			InvestmentMeetingUser entity = InvestmentMeetingUser.builder().investmentMeeting(meeting)
					.participantOid(oid).build();
			investmentMeetingUserDao.save(entity);
			res.add(oid);
		}
		return res;
	}

	private Set<String> addTargetStr(String targetStr, InvestmentMeeting meeting, String operator) throws Exception {
		Set<String> res = new HashSet<String>();
		String[] arr = targetStr.split(",");
		for (String oid : arr) {
			Investment temp = investmentService.getInvestmentDet(oid);
			InvestmentMeetingAsset entity = InvestmentMeetingAsset.builder().investment(temp).investmentMeeting(meeting)
					.build();
			entity.setInvestment(temp);
			entity.setInvestmentMeeting(meeting);
			investmentMeetingAssetDao.save(entity);
			temp.setState(Investment.INVESTMENT_STATUS_metting);
			investmentService.updateInvestment(temp, operator);
			res.add(temp.getOid());
		}
		return res;
	}

	/**
	 * 获得过会纪要列表
	 * 
	 * @param oid
	 */
	public List<SummaryDetResp> getSummary(String oid) {
		List<SummaryDetResp> resps = new ArrayList<SummaryDetResp>();
		InvestmentMeeting meeting = this.getMeetingDet(oid);
		if (null == meeting.getFkey()) {
			return resps;
		}
		List<File> files = fileService.list(meeting.getFkey(),1);
		if (null == files || 0 == files.size())
			return resps;
		for (File file : files) {
			SummaryDetResp resp = new SummaryDetResp();
			resp.setOid(file.getOid());
			resp.setName(file.getName());
			resp.setFkey(file.getFkey());
			resp.setFurl(file.getFurl());
			AdminObj adminObj = adminSdk.getAdmin(file.getOperator());
			if(null != adminObj)
				resp.setOperator(adminObj.getName());
			resp.setUpdateTime(file.getUpdateTime());
			resps.add(resp);
		}
		return resps;
	}

	/**
	 * 上传过会纪要
	 */
	public void summaryUp(String oid, String filesStr, String operator) {
		InvestmentMeeting meeting = this.getMeetingDet(oid);
		if (null == meeting) {
			throw new RuntimeException();
		}
		String fkey = meeting.getFkey();
		if (StringUtils.isEmpty(meeting.getFkey())) {
			// 之前未上传纪要,创建一个uuid
			fkey = StringUtil.uuid();
			meeting.setFkey(fkey);
			this.updateMeeting(meeting, operator);
		}
		List<SummaryFileDet> lists = JSONArray.parseArray(filesStr, SummaryFileDet.class);
		List<SaveFileForm> forms = new ArrayList<SaveFileForm>();
		for (SummaryFileDet file : lists) {
			SaveFileForm form = new SaveFileForm();
			form.setName(file.getName());
			form.setFurl(file.getUrl());
			form.setOid(StringUtil.uuid());
			form.setSize(file.getSize());
			forms.add(form);
		}
		fileService.merge(forms, fkey, File.CATE_User, operator);
	}

	/**
	 * 删除过会纪要
	 * 
	 * @param oid
	 * @param operator
	 */
	public void deleteSummary(String oid, String operator) {
		fileService.delete(oid, operator);
	}

	/**
	 * 启动过会
	 * 
	 * @param oid
	 * @param operator
	 * @return
	 */
	public InvestmentMeeting openMeeting(String oid, String operator) {
		InvestmentMeeting meeting = this.getMeetingDet(oid);
		if (!InvestmentMeeting.MEETING_STATE_NOTOPEN.equals(meeting.getState())
				&& !InvestmentMeeting.MEETING_STATE_STOP.equals(meeting.getState())) {
			throw new RuntimeException();
		}
		meeting.setState(InvestmentMeeting.MEETING_STATE_OPENING);
		this.updateMeeting(meeting, operator);
		return meeting;
	}

	/**
	 * 
	 * 暂停过会
	 * 
	 * @param oid
	 * @param operator
	 * @return
	 */
	public InvestmentMeeting stopMeeting(String oid, String operator) {
		InvestmentMeeting meeting = this.getMeetingDet(oid);
		if (!InvestmentMeeting.MEETING_STATE_OPENING.equals(meeting.getState())) {
			throw new RuntimeException();
		}
		meeting.setState(InvestmentMeeting.MEETING_STATE_STOP);
		this.updateMeeting(meeting, operator);
		return meeting;
	}

	/**
	 * 过会完成
	 * 
	 * @param form
	 * @param operator
	 */
	public void finishMeeting(MeetingFinishForm form, String operator) {
		InvestmentMeeting meeting = this.getMeetingDet(form.getOid());
		if (InvestmentMeeting.MEETING_STATE_NOTOPEN.equals(meeting.getState())
				|| InvestmentMeeting.MEETING_STATE_FINISH.equals(meeting.getState())) {
			// 当会议状态为完成或者未启动
			throw new RuntimeException();
		}
		List<MeetingInvestmentDetResp> list = JSONArray.parseArray(form.getTargets(), MeetingInvestmentDetResp.class);
		for (MeetingInvestmentDetResp req : list) {
//			String flowState = "reject";
			TargetEventType logType = null;
			Investment investment = investmentService.getInvestment(req.getOid());
			if (InvestmentMeetingVote.VOTE_STATUS_approve.equals(req.getVoteStatus())) {
//				flowState = "pass";
				investment.setState(Investment.INVESTMENT_STATUS_metted);
				investment.setLifeState(Investment.INVESTMENT_LIFESTATUS_PREPARE);
				String[] checkList = req.getCheckConditions();
				// 添加检查项
				for (String checkName : checkList) {
					InvestmentMeetingCheck check = InvestmentMeetingCheck.builder().investment(investment)
							.InvestmentMeeting(meeting).title(checkName)
							.state(InvestmentMeetingCheck.MEETINGCHEC_STATUS_notcheck).build();
					investmentMeetingCheckService.saveOrUpdateMeetingCheck(check);
				}
				logType = TargetEventType.collecting;
			} else if (InvestmentMeetingVote.VOTE_STATUS_notapprove.equals(req.getVoteStatus())) {
				investment.setState(Investment.INVESTMENT_STATUS_reject);
				investment.setRejectDesc(req.getRejectComment());
				logType = TargetEventType.checkreject;
			} else {
				continue;
			}
			investmentService.updateInvestment(investment, operator);
			investmentLogService.saveInvestmentLog(investment, logType, operator);
			// 工作流
//			workflowAssetService.complete(operator, investment.getOid(), WorkflowConstant.NODEID_MEETINGFINISH, flowState);
		}
		meeting.setState(InvestmentMeeting.MEETING_STATE_FINISH);
		this.updateMeeting(meeting, operator);

	}

}
