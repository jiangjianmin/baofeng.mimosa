package com.guohuai.bfsms;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.guohuai.basic.common.StringUtil;
import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.util.CheckUtil;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.mmp.captcha.CaptchaService;

@RestController
@RequestMapping(value = "/mimosa/client/bfsms", produces = "application/json")
public class BfSMSClientController extends BaseController {

	@Autowired
	private BfSMSUtils bfSMSUtils;
	@Autowired
	private CaptchaService captchaService;
	
	/**
	 * 发送短信 现金风暴修改
	 * @param BfSMSReq
	 * @return
	 */
	@RequestMapping(value = "sendvc", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseRep> sendVc(@Valid @RequestBody BfSMSReq req) {
		CheckUtil.isMobileNO(req.getPhone());
		if(!BfSMSTypeEnum.smstypeEnum.xjfb_regist.equals(req.getSmsType())&&!BfSMSTypeEnum.smstypeEnum.xjfb_resetPW.equals(req.getSmsType())){//现金风暴不校验图像验证码
			// 图形验证码校验
			if (!StringUtil.isEmpty(req.getImgvc())) {
				this.captchaService.checkImgVc(super.session.getId(), req.getImgvc());
			}
		}
		BaseRep rep = this.bfSMSUtils.sendByType(req.getPhone(), req.getSmsType(), req.getValues());
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	/**
	 * 校验短信 现金风暴修改
	 * @param BfSMSReq
	 * @return
	 */
	@RequestMapping(value = "checkvc", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseRep> checkVc(@Valid @RequestBody BfSMSReq req) {
		CheckUtil.isMobileNO(req.getPhone());
		this.bfSMSUtils.checkVeriCode(req.getPhone(), req.getSmsType(), req.getVeriCode());

		return new ResponseEntity<BaseRep>(new BaseRep(), HttpStatus.OK);
	}
}
