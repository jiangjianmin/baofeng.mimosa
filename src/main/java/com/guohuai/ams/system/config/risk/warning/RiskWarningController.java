/*   
 * Copyright © 2015 guohuaigroup All Rights Reserved.   
 *   
 * This software is the confidential and proprietary information of   
 * Founder. You shall not disclose such Confidential Information   
 * and shall use it only in accordance with the terms of the agreements   
 * you entered into with Founder.   
 *   
 */
package com.guohuai.ams.system.config.risk.warning;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.guohuai.ams.system.config.risk.cate.RiskCateService;
import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.web.view.BaseResp;

@RestController
@RequestMapping(value = "/mimosa/system/ccr/warning", produces = "application/json;charset=utf-8")
public class RiskWarningController extends BaseController {
	@Autowired
	RiskWarningService riskWarningService;
	@Autowired
	RiskCateService riskCateService;

//	@Autowired
//	private RiskIndicateService riskIndicateService;

	@RequestMapping(value = "/search", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<List<RiskWarningResp>> search(@RequestParam(defaultValue = "") String type, @RequestParam(defaultValue = "") String keyword) {
		super.getLoginUser();
		List<RiskWarningResp> list = this.riskWarningService.search(type, keyword);
		return new ResponseEntity<List<RiskWarningResp>>(list, HttpStatus.OK);
	}

	@RequestMapping(value = "/save", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<RiskWarningResp> save(RiskWarningForm form) {
		super.getLoginUser();
		RiskWarning i = this.riskWarningService.save(form);
		return new ResponseEntity<RiskWarningResp>(new RiskWarningResp(i), HttpStatus.OK);
	}

	@RequestMapping(value = "/delete", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<BaseResp> delete(@RequestParam String oid) {
		super.getLoginUser();
		this.riskWarningService.delete(oid);
		return new ResponseEntity<BaseResp>(new BaseResp(), HttpStatus.OK);
	}

	/*
	@RequestMapping(value = "/options", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<List<RiskWarningOptions>> options(@RequestParam String type) {
		super.getLoginUser();
		List<RiskWarningOptions> list = this.riskWarningService.options(type);
		return new ResponseEntity<List<RiskWarningOptions>>(list, HttpStatus.OK);
	}
*/
	
	/**
	 * title是否唯一
	 * @Title: validateTitle 
	 * @author vania
	 * @version 1.0
	 * @see: 
	 * @param warningTitle
	 * @param id
	 * @return ResponseEntity<BaseResp>    返回类型
	 */
	@RequestMapping(value = "/validateTitle", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<BaseResp> validateTitle(@RequestParam String warningTitle, @RequestParam(required = false) String id) {
		BaseResp pr = new BaseResp();
		long single = this.riskWarningService.validateSingle("title", warningTitle, id);
		return new ResponseEntity<BaseResp>(pr, single > 0 ? HttpStatus.CONFLICT : HttpStatus.OK);
	}

}
