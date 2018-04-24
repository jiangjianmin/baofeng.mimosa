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

import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.util.CheckUtil;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = "/mimosa/boot/bfsms", produces = "application/json")
@Slf4j
public class BfSMSBootController extends BaseController {

	@Autowired
	private BfSMSUtils bfSMSUtils;
	
	/**
	 * 获取短信验证码
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "getvc", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BfSMSVeriCodeRep> getVc(@Valid @RequestBody BfSMSReq req) {
		CheckUtil.isMobileNO(req.getPhone());
		BfSMSVeriCodeRep rep = this.bfSMSUtils.getVeriCode(req);
		log.info("后台获取手机号：{}的验证码。", req.getPhone());
		return new ResponseEntity<BfSMSVeriCodeRep>(rep, HttpStatus.OK);
	}
}
