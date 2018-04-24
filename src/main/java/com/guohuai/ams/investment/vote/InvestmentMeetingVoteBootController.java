package com.guohuai.ams.investment.vote;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.guohuai.ams.investment.InvestmentMeetingVoteService;
import com.guohuai.ams.investment.meeting.MeetingInvestmentDetResp;
import com.guohuai.ams.investment.meeting.MeetingInvestmentListResp;
import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.web.view.BaseResp;

/**
 * 投资标的过会表决
 * 
 * @author lirong
 *
 */
@RestController
@RequestMapping(value = "/mimosa/target/targetVote", produces = "application/json;charset=UTF-8")
public class InvestmentMeetingVoteBootController extends BaseController {

	@Autowired
	private InvestmentMeetingVoteService investmentMeetingVoteService;

	@RequestMapping(value = "list", method = { RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody ResponseEntity<MeetingInvestmentListResp> list() {
		String operator = super.getLoginUser();
		List<MeetingInvestmentDetResp> list = investmentMeetingVoteService.getInvestmentByParticipant(operator);
		MeetingInvestmentListResp resps = new MeetingInvestmentListResp(list);
		return new ResponseEntity<MeetingInvestmentListResp>(resps, HttpStatus.OK);
	}

	@RequestMapping(value = "vote", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseEntity<BaseResp> agree(MeetingVoteForm form) {
		String operator = super.getLoginUser();
		investmentMeetingVoteService.doMeetingVote(form, operator);
		return new ResponseEntity<BaseResp>(new BaseResp(), HttpStatus.OK);
	}
}
