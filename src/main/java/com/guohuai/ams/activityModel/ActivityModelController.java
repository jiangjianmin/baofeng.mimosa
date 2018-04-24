package com.guohuai.ams.activityModel;

import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.basic.component.ext.web.BaseResp;

@RestController
@RequestMapping(value = "/mimosa/activityModel", produces = "application/json")
public class ActivityModelController  extends BaseController{
	
	@Autowired
	private ActivityModelService activityModelService;
	
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public ResponseEntity<BaseResp> saveActivityModel(@Valid @RequestBody AddActivityModelReq req) {
		BaseResp baseResp = activityModelService.saveActivityModel(req, this.getLoginUser());
		return new ResponseEntity<>(baseResp, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/query", method = { RequestMethod.GET,RequestMethod.POST })
	public ResponseEntity<QueryActivityModelRep> query(@RequestBody ActivityModelReq req) {
		return new ResponseEntity<>(activityModelService.queryActivityModel(req), HttpStatus.OK);
	}
}
