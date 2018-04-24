package com.guohuai.mmp.platform.baseaccount.statistics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.web.view.BaseRep;

@RestController
@RequestMapping(value = "/mimosa/boot/platform/baseaccount/statistics", produces = "application/json")
public class PlatformStatisticsController extends BaseController {

	@Autowired
	PlatformStatisticsService patformStatisticsService;

	@RequestMapping(value = "home", method = { RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<BaseRep> home() {
		this.getLoginUser();
		BaseRep rep = this.patformStatisticsService.qryHome();
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	
	@RequestMapping(value = "syncaia", method = { RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<BaseRep> syncActiveInvestorAmount(@RequestParam int activeInvestorAmount) {


		int i = this.patformStatisticsService.syncActiveInvestorAmount(activeInvestorAmount);
		BaseRep rep = new BaseRep();
		if (i < 1) {
			rep.setErrorCode(BaseRep.ERROR_CODE);
		}
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	/**
	 * 
	 * @author yihonglei
	 * @Title: homeStat
	 * @Description:app 和 pc首页显示累计投资人数，累计投资金额查询
	 * @return ResponseEntity<PlatformHomeQueryRep>
	 * @date 2017年5月31日 上午11:59:13
	 * @since  1.0.0
	 */
	@RequestMapping(value = "homeStat", method = { RequestMethod.POST,RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<PlatformHomeQueryRep> homeStat() {
		PlatformHomeQueryRep rep = this.patformStatisticsService.queryAppAndPcHome();
		return new ResponseEntity<PlatformHomeQueryRep>(rep, HttpStatus.OK);
	}

}
