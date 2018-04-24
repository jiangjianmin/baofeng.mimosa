package com.guohuai.ams.system.config.risk.indicate.collect;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.guohuai.basic.component.ext.web.BaseController;


@RestController
@RequestMapping(value = "/mimosa/system/ccr/indicate/collect", produces = "application/json;charset=utf-8")
public class RiskIndicateCollectController extends BaseController {

	@Autowired
	private RiskIndicateCollectService riskIndicateCollectService;

	@RequestMapping(value = "/save", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<List<RiskIndicateCollectResp>> save(@RequestBody RiskIndicateCollectForm form) {
		super.getLoginUser();
		List<RiskIndicateCollectResp> list = this.riskIndicateCollectService.save(form);
		return new ResponseEntity<List<RiskIndicateCollectResp>>(list, HttpStatus.OK);
	}

	@RequestMapping(value = "/preUpdate", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<List<RiskIndicateCollectResp>> preUpdate(@RequestParam String relative) {
		super.getLoginUser();
		List<RiskIndicateCollectResp> list = this.riskIndicateCollectService.preUpdate(relative);
		return new ResponseEntity<List<RiskIndicateCollectResp>>(list, HttpStatus.OK);
	}
}
