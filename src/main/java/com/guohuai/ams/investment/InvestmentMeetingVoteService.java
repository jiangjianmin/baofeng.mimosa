package com.guohuai.ams.investment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.guohuai.ams.investment.meeting.MeetingInvestmentDetResp;
import com.guohuai.ams.investment.meeting.VoteDetResp;
import com.guohuai.ams.investment.vote.MeetingVoteForm;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.StringUtil;
import com.guohuai.file.File;
import com.guohuai.file.FileService;
import com.guohuai.file.SaveFileForm;
import com.guohuai.operate.api.AdminSdk;
import com.guohuai.operate.api.objs.admin.AdminObj;

@Service
@Transactional
public class InvestmentMeetingVoteService {

	@Autowired
	private InvestmentMeetingVoteDao investmentMeetingVoteDao;

	@Autowired
	private InvestmentMeetingService investmentMeetingService;

	@Autowired
	private InvestmentService investmentService;

	@Autowired
	private AdminSdk adminSdk;

	@Autowired
	private InvestmentMeetingUserService investmentMeetingUserService;

	@Autowired
	private InvestmentMeetingAssetService investmentMeetingAssetService;

	@Autowired
	private FileService fileService;

	/**
	 * 获得投资标的过会表决列表
	 * 
	 * @param spec
	 * @param pageable
	 * @return
	 */
	public Page<InvestmentMeetingVote> getMeetingVoteList(Specification<InvestmentMeetingVote> spec,
			Pageable pageable) {
		return investmentMeetingVoteDao.findAll(spec, pageable);
	}

	/**
	 * 获得投资标的过会表决详情
	 * 
	 * @param oid
	 * @return
	 */
	public InvestmentMeetingVote getMeetingVoteDet(String oid) {
		return investmentMeetingVoteDao.findOne(oid);
	}

	/**
	 * 新建或更新投资标的过会表决
	 * 
	 * @param entity
	 * @param operator
	 * @return
	 */
	public InvestmentMeetingVote saveOrUpdateMeetingVote(InvestmentMeetingVote entity) {
		return this.investmentMeetingVoteDao.save(entity);
	}

	/**
	 * 根据与会人查询标的列表
	 * 
	 * @param participantOid
	 * @param pageabl
	 * @return
	 */
	public List<MeetingInvestmentDetResp> getInvestmentByParticipant(String participantOid) {
		List<InvestmentMeetingUser> participantList = investmentMeetingUserService.getMeetingUserByUser(participantOid);
		List<MeetingInvestmentDetResp> res = new ArrayList<MeetingInvestmentDetResp>();
		for (InvestmentMeetingUser userTemp : participantList) {
			if (InvestmentMeeting.MEETING_STATE_OPENING.equals(userTemp.getInvestmentMeeting().getState())) {
				// 过会中的会议
				List<MeetingInvestmentDetResp> temp = investmentMeetingAssetService
						.getInvestmentByMeeting(userTemp.getInvestmentMeeting().getOid());
				for (MeetingInvestmentDetResp resp : temp) {
					InvestmentMeetingVote voteTemp = investmentMeetingVoteDao
							.findByInvestmentMeetingAndInvestmentAndInvestmentMeetingUser(
									userTemp.getInvestmentMeeting(), resp, userTemp);
					if (voteTemp != null) {
						resp.setVoteStatus(voteTemp.getState());
					} else {
						resp.setVoteStatus(InvestmentMeetingVote.VOTE_STATUS_notvote);
					}
					res.add(resp);
				}
			}
		}
		Collections.sort(res, new meetingTimeComparator());
		return res;
	}

	static class meetingTimeComparator implements Comparator {
		public int compare(Object object1, Object object2) {// 实现接口中的方法
			MeetingInvestmentDetResp p1 = (MeetingInvestmentDetResp) object1; // 强制转换
			MeetingInvestmentDetResp p2 = (MeetingInvestmentDetResp) object2;
			try {
				Date d1 = p1.getMeetingTime();
				Date d2 = p2.getMeetingTime();
				if (d1.getTime() - d2.getTime() > 0) {
					return -1;
				} else if (d1.getTime() - d2.getTime() < 0) {
					return 1;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			return 0;
		}
	}

	// private List<MeetingInvestmentDetResp>
	// orderByMeeting(List<MeetingInvestmentDetResp> list){
	// List<MeetingInvestmentDetResp> res = new
	// ArrayList<MeetingInvestmentDetResp>();
	// for (int i = 0; i < list.size(); i++) {
	// for (int j = 0; j < list.size()-1; j++) {
	// MeetingInvestmentDetResp temp;
	// if(){
	//
	// }
	// }
	// }
	// }

	/**
	 * 根据会议和标的查询投票详情
	 * 
	 * @param meetingOid
	 * @param investmentOid
	 */
	public List<VoteDetResp> getVoteDetByMeetingAndInvestment(String meetingOid, String investmentOid) {
		List<VoteDetResp> resps = new ArrayList<VoteDetResp>();
		InvestmentMeeting meeting = investmentMeetingService.getMeetingDet(meetingOid);
		Investment investment = investmentService.getInvestmentDet(investmentOid);
		List<InvestmentMeetingUser> userList = investmentMeetingUserService.getMeetingUserByMeeting(meeting);
		for (InvestmentMeetingUser user : userList) {
			InvestmentMeetingVote vote = investmentMeetingVoteDao
					.findByInvestmentMeetingAndInvestmentAndInvestmentMeetingUser(meeting, investment, user);
			AdminObj userInfo = adminSdk.getAdmin(user.getParticipantOid());
			VoteDetResp resp = new VoteDetResp();
			if (vote != null) {
				resp.setVoteStatus(vote.getState());
				resp.setTime(vote.getVoteTime());
				// resp.setFile(vote.getFile());
				if (!StringUtils.isEmpty(vote.getFile())) {
					List<File> files = fileService.list(vote.getFile(), 1);
					if (files != null && files.size() > 0) {
						resp.setFile(files.get(0).getFurl());
						resp.setFileName(files.get(0).getName());
					}
				}
			} else {
				resp.setVoteStatus(InvestmentMeetingVote.VOTE_STATUS_notvote);
			}
			resp.setName(userInfo.getName());
			resps.add(resp);
		}
		return resps;
	}

	/**
	 * 标的过会表决
	 * 
	 * @param form
	 * @param operator
	 */
	public void doMeetingVote(MeetingVoteForm form, String operator) {
		InvestmentMeeting meeting = investmentMeetingService.getMeetingDet(form.getMeetingOid());
		if (null == meeting) {
			throw new RuntimeException();
		}
		Investment asset = investmentService.getInvestment(form.getTargetOid());
		if (null == asset) {
			throw new RuntimeException();
		}
		InvestmentMeetingUser user = investmentMeetingUserService.getMeetingUserDetByUserAndMeeting(operator, meeting);
		if (null == user) {
			throw new RuntimeException();
		}
		String voteStatus = null;
		switch (form.getVoteState()) {
		case "yes":
			voteStatus = InvestmentMeetingVote.VOTE_STATUS_approve;
			break;
		case "no":
			voteStatus = InvestmentMeetingVote.VOTE_STATUS_notapprove;
			break;
		default:
			throw new RuntimeException();
		}
		InvestmentMeetingVote hisVote = investmentMeetingVoteDao
				.findByInvestmentMeetingAndInvestmentAndInvestmentMeetingUser(meeting, asset, user);
		InvestmentMeetingVote temp = null;
		if (null != hisVote) {
			temp = hisVote;
		} else {
			temp = new InvestmentMeetingVote();
			temp.setInvestment(asset);
			temp.setInvestmentMeeting(meeting);
			temp.setInvestmentMeetingUser(user);
		}
		if (!StringUtils.isEmpty(form.getSuggest())) {
			temp.setSuggest(form.getSuggest());
		}
		if (!StringUtils.isEmpty(form.getFile())) {
			String fkey = null;
			if (null != hisVote && null != hisVote.getFile()) {
				fkey = hisVote.getFile();
				List<File> files = fileService.list(fkey, 1);
				if (files != null && files.size() > 0) {
					// 删除历史附件
					for (File hisFile : files) {
						fileService.delete(hisFile, operator);
					}
				}
			} else {
				fkey = StringUtil.uuid();
			}
			List<SaveFileForm> fileForms = new ArrayList<SaveFileForm>();
			SaveFileForm fileform = new SaveFileForm();
			fileform.setFurl(form.getFile());
			fileform.setName(form.getFileName());
			fileform.setSize(Long.valueOf(form.getFileSize()));
			fileForms.add(fileform);
			fileService.save(fileForms, fkey, File.CATE_User, operator);
			temp.setFile(fkey);
		}
		temp.setVoteTime(DateUtil.getSqlCurrentDate());
		temp.setState(voteStatus);
		this.saveOrUpdateMeetingVote(temp);
		// 工作流
		// workflowAssetService.complete(operator, asset.getOid(),
		// WorkflowConstant.NODEID_DOMEETING, "pass");
	}

}
