package com.guohuai.mmp.investor.baseaccount;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.guohuai.ams.product.reward.ProductIncomeRewardCacheService;
import com.guohuai.basic.common.StringUtil;
import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.basic.messageBody.annotations.DeserializedField;
import com.guohuai.basic.messageBody.annotations.SerializedField;
import com.guohuai.component.util.CheckUtil;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.mmp.investor.baseaccount.changephone.ChangePhoneForm;
import com.guohuai.mmp.investor.baseaccount.statistics.InvestorStatisticsService;
import com.guohuai.mmp.investor.baseaccount.statistics.MyCaptialNewRep;
import com.guohuai.mmp.investor.baseaccount.statistics.MyCaptialQueryRep;
import com.guohuai.mmp.investor.baseaccount.statistics.MyHomeQueryRep;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = "/mimosa/client/investor/baseaccount", produces = "application/json")
@Slf4j
public class InvestorBaseAccountClientController extends BaseController {
	
	@Autowired
	InvestorBaseAccountService investorBaseAccountService;
	@Autowired
	InvestorStatisticsService investorStatisticsService;
	@Autowired
	private ProductIncomeRewardCacheService incomeRewardCacheService;
	
//	/**
//	 * 登录（新）
//	 * @param req
//	 * @return
//	 */
//	@RequestMapping(value = "login", method = RequestMethod.POST)
//	@ResponseBody
//	public ResponseEntity<BaseRep> login(@Valid @RequestBody InvestorBaseAccountLoginReq req) {
//		
//		CheckUtil.isMobileNO(req.getUserAcc());
//		
//		String accountOid = StringUtil.EMPTY;
//		if (!StringUtil.isEmpty(req.getUserPwd())) {
//			accountOid = this.investorBaseAccountService.login(req);
//		} else if (!StringUtil.isEmpty(req.getVericode())) {
//			// 快速登录
//			accountOid = this.investorBaseAccountService.fastLogin(req);
//		} else {
//			throw new GHException("用户登录失败！");
//		}
//
//		if (!StringUtil.isEmpty(accountOid)) {
//			super.setLoginUser(accountOid, new String[]{req.getPlatform()});
//		}
//		return new ResponseEntity<BaseRep>(new BaseRep(), HttpStatus.OK);
//	}
//	
//	/**
//	 * 退出
//	 * @return
//	 */
//	@RequestMapping(value = "logout", method = RequestMethod.POST)
//	@ResponseBody
//	public ResponseEntity<BaseRep> logout() {
//		this.setLogoutUser();
//		return new ResponseEntity<BaseRep>(new BaseRep(), HttpStatus.OK);
//	}
//	
//	/**
//	 * 判断用户的锁定状态
//	 * @param userAcc 手机号/账号
//	 * @return
//	 */
//	@RequestMapping(value = "/checklock", method = RequestMethod.POST)
//	@ResponseBody
//	public ResponseEntity<BaseRep> checkLockState(@RequestParam(required = true) String userAcc) {
//		CheckUtil.isMobileNO(userAcc);
//		
//		this.investorBaseAccountService.checkLockState(userAcc);
//		
//		return new ResponseEntity<BaseRep>(new BaseRep(), HttpStatus.OK);
//	}
	
	/**
	 * 是否登录状态
	 * @return
	 */
	@RequestMapping(value = "islogin", method ={RequestMethod.GET,RequestMethod.POST})
	@ResponseBody
	public ResponseEntity<InvestorBaseAccountIsLoginRep> checkLogin() {
		InvestorBaseAccountIsLoginRep rep = new InvestorBaseAccountIsLoginRep();
		if (StringUtil.isEmpty(super.isLogin())) {
			rep.setIslogin(false);
			log.info("当前用户未登录，或会话超时。");
		} else {
			rep.setIslogin(true);
		}
		return new ResponseEntity<InvestorBaseAccountIsLoginRep>(rep, HttpStatus.OK);
	}
	
	/**
	 * 用户信息
	 * @return
	 */
	@RequestMapping(value = "userinfo", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseAccountRep> userinfo() {
		String uid = this.getLoginUser();
		BaseAccountRep rep = this.investorBaseAccountService.userInfo(uid, true);
		return new ResponseEntity<BaseAccountRep>(rep, HttpStatus.OK);
	}
	
	/**
	 * 我的
	 * @author yuechao
	 */
	@RequestMapping(value = "myhome", method = RequestMethod.POST )
	@ResponseBody
	public ResponseEntity<MyHomeQueryRep> myHomeQry() {
		String uid = this.getLoginUser();
		MyHomeQueryRep rep = this.investorStatisticsService.myHomeInfo(uid);
		return new ResponseEntity<MyHomeQueryRep>(rep, HttpStatus.OK);
	}
	
	////////////////
	/**
	* 我的
	* @author yuechao
	*/
	@RequestMapping(value = "userhome", method = RequestMethod.POST )
	@ResponseBody
	public ResponseEntity<MyHomeQueryRep> userhome(@RequestParam String uid) {
		log.info("<------------------进入mimosa   userhome------------------>");
		MyHomeQueryRep rep = this.investorStatisticsService.myHomeInfo(uid);
		return new ResponseEntity<MyHomeQueryRep>(rep, HttpStatus.OK);
	}
///////////////////////////

	/**
	 * 我的资产
	 * @author yuechao
	 */
	@RequestMapping(value = "mycaptial", method =  RequestMethod.POST )
	@ResponseBody
	public ResponseEntity<MyCaptialQueryRep> myCaptialQry() {
		String uid = this.getLoginUser();
		MyCaptialQueryRep rep = this.investorStatisticsService.myCaptial(uid);
		return new ResponseEntity<MyCaptialQueryRep>(rep, HttpStatus.OK);
	}
	
	/**
	 * 新版我的奖励
	 * @return
	 */
	@RequestMapping(value = "mycaptialNew", method =  RequestMethod.POST )
	@ResponseBody
	public ResponseEntity<MyCaptialNewRep> myCaptialNew() {
		String uid = this.getLoginUser();
//		String uid = "ef474154efa74c029a3775dc03195424";
		MyCaptialNewRep rep = this.investorStatisticsService.myCaptialNew(uid);
		return new ResponseEntity<MyCaptialNewRep>(rep, HttpStatus.OK);
	}
	
	/**
	 * 判断手机号是否已经注册
	 * 前端调用
	 */
	@RequestMapping(value = "isregist", method =  RequestMethod.POST )
	@ResponseBody
	public ResponseEntity<InvestorBaseAccountIsRegistRep> isRegist(@RequestParam String phoneNum) {
		CheckUtil.checkParams(phoneNum, "手机号不能为空！");
		CheckUtil.isMobileNO(phoneNum);
		InvestorBaseAccountIsRegistRep rep = this.investorBaseAccountService.isRegist(phoneNum);
		return new ResponseEntity<InvestorBaseAccountIsRegistRep>(rep, HttpStatus.OK);
	}
	
	/**
	 * 判断手机号是否已经注册
	 * 后端调用
	 */
	@RequestMapping(value = "isregistSDK", method =  RequestMethod.POST )
	@ResponseBody
	public ResponseEntity<InvestorBaseAccountIsRegistRep> isregistSDK(@RequestParam String phoneNum) {
		CheckUtil.checkParams(phoneNum, "手机号不能为空！");
		CheckUtil.isMobileNO(phoneNum);
		InvestorBaseAccountIsRegistRep rep = this.investorBaseAccountService.isRegistSDK(phoneNum);
		return new ResponseEntity<InvestorBaseAccountIsRegistRep>(rep, HttpStatus.OK);
	}
	
	/**
	 * 判断用户是否已经被锁（仅用作修改手机号功能）
	 */
	@RequestMapping(value = "isLock", method =  RequestMethod.POST )
	@ResponseBody
	public ResponseEntity<BaseRep> isLock(@RequestBody ChangePhoneForm form) {
		String userOid=this.getLoginUser();
		this.investorBaseAccountService.isLock(userOid, form.getType());
		return new ResponseEntity<BaseRep>(new BaseRep(), HttpStatus.OK);
	}
	/**
	 * 原手机号可用-第一步验证-原手机号短信验证
	 * 1.8.0加解密过渡接口，1.8.0版本后废弃处理（verify transition）
	 */
	@RequestMapping(value = "checkOldPhoneVc", method =  RequestMethod.POST )
	@ResponseBody
	public ResponseEntity<BaseRep> checkOldPhoneVc(@RequestBody ChangePhoneForm form) {
		String userOid = this.getLoginUser();
		this.investorBaseAccountService.checkOldPhoneAccount(userOid, form.getVericodes());
		return new ResponseEntity<BaseRep>(new BaseRep(), HttpStatus.OK);
	}
	/**
	 * 原手机号可用-第一步验证-原手机号短信验证
	 * 1.8.0加密接口（手机号加密处理）
	 * @author：huyong
	 * @date：2018.1.2
	 */
	@RequestMapping(value = "verifyCheckOldPhoneVc", method =  RequestMethod.POST )
	@ResponseBody
	@DeserializedField(decrys= {"phoneNum"})
	public ResponseEntity<BaseRep> verifyCheckOldPhoneVc(@RequestBody ChangePhoneForm form) {
		String userOid = this.getLoginUser();
		this.investorBaseAccountService.checkOldPhoneAccount(userOid, form.getVericodes());
		return new ResponseEntity<BaseRep>(new BaseRep(), HttpStatus.OK);
	}
	/**
	 * 原手机号可用(原手机号不可用)-验证短信验证码并且注册手机号修改
	 * 1.8.0加解密过渡接口，1.8.0版本后废弃处理（verify transition）
	 */
	@RequestMapping(value = "changePhone", method =  RequestMethod.POST )
	@ResponseBody
	public ResponseEntity<BaseRep> changePhone(@RequestBody ChangePhoneForm form) {
		String userOid=this.getLoginUser();
		this.investorBaseAccountService.changePhone(form.getPhoneNum(), form.getVericodes(),userOid, form.getType());
		this.setLogoutUser();
		return new ResponseEntity<BaseRep>(new BaseRep(), HttpStatus.OK);
	}
	/**
	 * 原手机号可用(原手机号不可用)-验证短信验证码并且注册手机号修改
	 * 1.8.0加密接口（手机号加密处理）
	 * @author：huyong
	 * @date：2018.1.2
	 */
	@RequestMapping(value = "verifyChangePhone", method =  RequestMethod.POST )
	@ResponseBody
	@DeserializedField(decrys= {"phoneNum"})
	public ResponseEntity<BaseRep> verifyChangePhone(@RequestBody ChangePhoneForm form) {
		String userOid=this.getLoginUser();
		this.investorBaseAccountService.changePhone(form.getPhoneNum(), form.getVericodes(),userOid, form.getType());
		this.setLogoutUser();
		return new ResponseEntity<BaseRep>(new BaseRep(), HttpStatus.OK);
	}
	/**
	 * 原手机号不可用-第一步-校验交易密码是否正确
	 * 1.8.0加解密过渡接口，1.8.0版本后废弃处理（verify transition）
	 */
	@RequestMapping(value = "checkPayPwd", method =  RequestMethod.POST )
	@ResponseBody
	public ResponseEntity<BaseRep> checkPayPwd(@RequestBody ChangePhoneForm form) {
		String userOid=this.getLoginUser();
		this.investorBaseAccountService.checkPayPwd(form.getPayPassWord(),userOid);
		return new ResponseEntity<BaseRep>(new BaseRep(), HttpStatus.OK);
	}
	/**
	 * 原手机号不可用-第一步-校验交易密码是否正确
	 * 1.8.0加密接口（交易密码加密处理）
	 * @author：huyong
	 * @date：2018.1.2
	 */
	@RequestMapping(value = "verifyCheckPayPwd", method =  RequestMethod.POST )
	@ResponseBody
	@DeserializedField(decrys= {"payPassWord"})
	public ResponseEntity<BaseRep> verifyCheckPayPwd(@RequestBody ChangePhoneForm form) {
		String userOid=this.getLoginUser();
		this.investorBaseAccountService.checkPayPwd(form.getPayPassWord(),userOid);
		return new ResponseEntity<BaseRep>(new BaseRep(), HttpStatus.OK);
	}
	/**
	 *  原手机号不可用-第二步-校验用户实名信息
	 * 1.8.0加解密过渡接口，1.8.0版本后废弃处理（verify transition）
	 */
	@RequestMapping(value = "checkUserInfo", method =  RequestMethod.POST )
	@ResponseBody
	public ResponseEntity<BaseRep> checkUserInfo(@RequestBody ChangePhoneForm form) {
		String userOid=this.getLoginUser();
		this.investorBaseAccountService.checkUserInfo(form.getRealName(),form.getIdCardNo(),userOid);
		return new ResponseEntity<BaseRep>(new BaseRep(), HttpStatus.OK);
	}
	/**
	 *  原手机号不可用-第二步-校验用户实名信息
	 * 1.8.0加密接口（用户姓名、身份证号加密处理）
	 * @author：huyong
	 * @date：2018.1.2
	 */
	@RequestMapping(value = "verifyCheckUserInfo", method =  RequestMethod.POST )
	@ResponseBody
	@DeserializedField(decrys= {"RealName","IdCardNo"})
	public ResponseEntity<BaseRep> verifyCheckUserInfo(@RequestBody ChangePhoneForm form) {
		String userOid=this.getLoginUser();
		this.investorBaseAccountService.checkUserInfo(form.getRealName(),form.getIdCardNo(),userOid);
		return new ResponseEntity<BaseRep>(new BaseRep(), HttpStatus.OK);
	}
//	/**
//	 * 注册（新）
//	 * @param req
//	 * @return
//	 */
//	@RequestMapping(value = "regist", method =  RequestMethod.POST )
//	@ResponseBody
//	public ResponseEntity<BaseRep> regist(@Valid @RequestBody InvestorBaseAccountAddReq req) {
//		CheckUtil.isMobileNO(req.getUserAcc());
//		BaseRep rep = this.investorBaseAccountService.addBaseAccount(req);
//		// 登陆
//		super.setLoginUser(req.getInvestorOid(), new String[]{req.getPlatform()});
//		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
//	}
//	
//	/**
//	 * 校验输入的原登录密码是否正确（新）
//	 * @param req
//	 * @return
//	 */
//	@RequestMapping(value = "checkloginpwd", method =  RequestMethod.POST )
//	@ResponseBody
//	public ResponseEntity<BaseRep> checkLoginPassword(@Valid @RequestBody InvestorBaseAccountPasswordReq req) {
//		req.setInvestorOid(super.getLoginUser());
//		BaseRep rep = this.investorBaseAccountService.checkLoginPassword(req);
//		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
//	}
//	
//	/**
//	 * 设置/修改登录密码（新）
//	 */
//	@RequestMapping(value = "editloginpwd", method =  RequestMethod.POST )
//	@ResponseBody
//	public ResponseEntity<BaseRep> editLoginPassword(@Valid @RequestBody InvestorBaseAccountPasswordReq req) {
//		req.setInvestorOid(super.getLoginUser());
//		BaseRep rep = this.investorBaseAccountService.editLoginPassword(req);
//		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
//	}
//	
//	/**
//	 * 忘记登录密码（新）
//	 */
//	@RequestMapping(value = "forgetloginpwd", method =  RequestMethod.POST )
//	@ResponseBody
//	public ResponseEntity<BaseRep> forgetLoginPassword(@Valid @RequestBody InvestorBaseAccountPasswordReq req) {
//		CheckUtil.isMobileNO(req.getUserAcc());
//		CheckUtil.checkParams(req.getVericode(), "验证码不能为空！");
//		CheckUtil.checkVeriCode(req.getVericode());
//		BaseRep rep = this.investorBaseAccountService.forgetLoginPassword(req);
//		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
//	}
//	
//	/**
//	 * 校验支付密码是否正确（新）
//	 * @param req
//	 * @return
//	 */
//	@RequestMapping(value = "checkpaypwd", method =  RequestMethod.POST )
//	@ResponseBody
//	public ResponseEntity<BaseRep> checkPayPwd(@Valid @RequestBody InvestorBaseAccountPayPwdReq req) {
//		req.setInvestorOid(super.getLoginUser());
//		BaseRep rep = this.investorBaseAccountService.checkPayPwd(req);
//		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
//	}
//	
//	/**
//	 * 设置/修改支付密码（新）
//	 */
//	@RequestMapping(value = "editpaypwd", method =  RequestMethod.POST )
//	@ResponseBody
//	public ResponseEntity<BaseRep> editPayPwd(@Valid @RequestBody InvestorBaseAccountPayPwdReq req) {
//		req.setInvestorOid(super.getLoginUser());
//		BaseRep rep = this.investorBaseAccountService.editPayPwd(req);
//		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
//	}
//	
//	/**
//	 * 用户信息（新）
//	 */
//	@RequestMapping(value = "accountinfo", method =  RequestMethod.POST )
//	@ResponseBody
//	public ResponseEntity<BaseAccountInfoRep> getAccountInfo() {
//		BaseAccountInfoRep rep = this.investorBaseAccountService.getAccountInfo(super.getLoginUser());
//		return new ResponseEntity<BaseAccountInfoRep>(rep, HttpStatus.OK);
//	}
}
