package com.guohuai.mmp.publisher.baseaccount.statistics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.web.view.BaseRep;

@RestController
@RequestMapping(value = "/mimosa/boot/publisher/baseaccount/statistics/", produces = "application/json")
public class PublisherStatisticsBootController extends BaseController {

	@Autowired
	PublisherStatisticsService publisherStatisticsService;

	/** 发行人首页 */
	@RequestMapping(value = "publisherhome", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseRep> publisherhome() {
		
		//发行人登录账户
		String loginAcc = this.getLoginUser();
		
		BaseRep rep = this.publisherStatisticsService.publisherHome(loginAcc);

		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
}
