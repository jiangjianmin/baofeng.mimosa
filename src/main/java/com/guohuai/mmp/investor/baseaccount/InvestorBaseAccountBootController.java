package com.guohuai.mmp.investor.baseaccount;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.component.web.view.PagesRep;

import lombok.extern.slf4j.Slf4j;
import net.kaczmarzyk.spring.data.jpa.domain.DateAfterInclusive;
import net.kaczmarzyk.spring.data.jpa.domain.DateBeforeInclusive;
import net.kaczmarzyk.spring.data.jpa.domain.In;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;

@RestController
@RequestMapping(value = "/mimosa/boot/investor/baseaccount", produces = "application/json")
@Slf4j
public class InvestorBaseAccountBootController extends BaseController {

	@Autowired
	private InvestorBaseAccountService investorBaseAccountService;
	

	@RequestMapping(value = "query", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<PagesRep<InvestorBaseAccountQueryRep>> channelQuery(HttpServletRequest request,
			@And({ @Spec(params = "phoneNum", path = "phoneNum", spec = Like.class),
					@Spec(params = "status", path = "status", spec = In.class),
					@Spec(params = "owner", path = "owner", spec = In.class),
					@Spec(params = "createTimeBegin", path = "createTime", spec = DateAfterInclusive.class, config = DateUtil.fullDatePattern),
					@Spec(params = "createTimeEnd", path = "createTime", spec = DateBeforeInclusive.class, config = DateUtil.fullDatePattern),
					@Spec(params = "realName", path = "realName", spec = Like.class)}) Specification<InvestorBaseAccountEntity> spec,
			@RequestParam int page, @RequestParam int rows) {
		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(Direction.DESC, "createTime")));
		PagesRep<InvestorBaseAccountQueryRep> rep = this.investorBaseAccountService.accountQuery(spec, pageable);

		return new ResponseEntity<PagesRep<InvestorBaseAccountQueryRep>>(rep, HttpStatus.OK);
	}
	
	/**
	 * SINA资金明细
	 */
//	@RequestMapping(value = "qsinadetails", method = {RequestMethod.POST, RequestMethod.GET})
//	@ResponseBody
//	public ResponseEntity<QueryAccountDetailsResp> queryAccountDetails(@Valid QueryAccountDetailsReq req) {
//		QueryAccountDetailsResp rep = investorBaseAccountService.queryAccountDetails(req);
//
//		return new ResponseEntity<QueryAccountDetailsResp>(rep, HttpStatus.OK);
//	}
	
	/**
	 *超级用户信息 
	 */
	@RequestMapping(value = "sman", method = {RequestMethod.POST, RequestMethod.GET})
	@ResponseBody
	public ResponseEntity<BaseAccountRep> supermanInfo() {
		this.getLoginUser();
		BaseAccountRep rep = this.investorBaseAccountService.supermanInfo();
		return new ResponseEntity<BaseAccountRep>(rep, HttpStatus.OK);
	}

	/**
	 * 同步用户中心数据，来源于前端用户注册信息
	 * @param uid 用户中心OID
	 * @param mid 会员OID
	 * @param uacc 用户手机号
	 * @param racc 推荐人手机号
	 * @param sceneid 邀请码
	 * @return
	 */
	@RequestMapping(value = "syncuc", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseRep> syncUserCenter(@RequestParam String uid,
			@RequestParam String mid,
			@RequestParam String uacc,
			@RequestParam(required = false) String racc,
			@RequestParam String sceneid,
			@RequestParam String registerChannelId) {
		log.info("uid={}, mid={}", uid, mid);
		BaseRep rep = this.investorBaseAccountService.initBaseAccount(uid, mid, uacc, racc, sceneid, registerChannelId);
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	@RequestMapping(value = "resendcoupon", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseRep> reSendCoupon(@RequestParam(required = true) String phone) {
		BaseRep rep = this.investorBaseAccountService.reSendCoupon(phone);
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	@RequestMapping(value = "compensateRefereeCoupon", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseRep> compensateRefereeCoupon(){
		BaseRep rep = this.investorBaseAccountService.compensateRefereeCoupon();
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	/**
	 * 推荐人投资
	 * @param uid
	 * @return
	 */
	@RequestMapping(value = "refereeInvest", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseRep> refereeInvest(@RequestParam String uid,@RequestParam String refereeId) {
		BaseRep rep = this.investorBaseAccountService.addBank(uid,refereeId);
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}

	/**
	 * 从用户中心同步真实姓名
	 * 
	 * @param uid
	 * @param name
	 * @return
	 */
	@RequestMapping(value = "syncUserName", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseRep> syncUserRealName(@RequestParam String uid, @RequestParam String name, @RequestParam String idNum) {
		log.info("userOid={}, name={}, idNum", uid, name, idNum);
		BaseRep rep = this.investorBaseAccountService.syncucUserName(uid, name, idNum);
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	/**
	 * 锁定与解锁用户
	 * 
	 * @param uid
	 * @param islock
	 *            is/not
	 * @return
	 */
	@RequestMapping(value = "lockuser", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseRep> lockUser(@RequestParam(required = true) String uoid,
			@RequestParam(required = true) String islock) {

		BaseRep rep = this.investorBaseAccountService.lockUser(uoid, islock);
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}

	/**
	 * 用户信息
	 * 
	 * @param uid
	 * @return
	 */
	@RequestMapping(value = "userinfo", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<InvestorBaseAccountInfoRep> userInfo(@RequestParam(required = true) String uoid) {

		InvestorBaseAccountInfoRep rep = this.investorBaseAccountService.getUserInfo(uoid);
		return new ResponseEntity<InvestorBaseAccountInfoRep>(rep, HttpStatus.OK);
	}

	/**
	 * 获取用户资金信息
	 * 
	 * @param uoid
	 * @return
	 */
	@RequestMapping(value = "cashuserinfo", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseAccountRep> getCashUserinfo(@RequestParam(required = true) String uoid) {

		BaseAccountRep rep = this.investorBaseAccountService.userInfoPc(uoid);
		return new ResponseEntity<BaseAccountRep>(rep, HttpStatus.OK);
	}
	
//	/**
//	 * 解锁登录锁定
//	 * @param investorOid 用户OID
//	 * @return
//	 */
//	@RequestMapping(value = "/cancelloginlock", method = RequestMethod.POST)
//	@ResponseBody
//	public ResponseEntity<BaseRep> cancelLoginLock(@RequestParam(required = true) String investorOid) {
//		
//		this.investorBaseAccountService.cancelLoginLock(investorOid);
//		
//		return new ResponseEntity<BaseRep>(new BaseRep(), HttpStatus.OK);
//	}

	/**
	 * 更改用户手机号，仅测试使用
	 * 
	 * @param phoneNum
	 * @return
	 */
	@RequestMapping(value = "chanphone", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseRep> changePhone(@RequestParam(required = true) String phoneNum) {

		this.investorBaseAccountService.changeAccPhoneNum(phoneNum);
		return new ResponseEntity<BaseRep>(new BaseRep(), HttpStatus.OK);
	}
}
