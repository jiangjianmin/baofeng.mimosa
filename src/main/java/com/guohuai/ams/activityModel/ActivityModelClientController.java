package com.guohuai.ams.activityModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.guohuai.basic.component.ext.web.BaseController;

@RestController
@RequestMapping(value = "/mimosa/activityModel/client", produces = "application/json")
public class ActivityModelClientController  extends BaseController{
	
	@Autowired
	private ActivityModelService activityModelService;
	
	@RequestMapping(value = "/query", method = { RequestMethod.GET,RequestMethod.POST })
	public ResponseEntity<QueryActivityModelRep> query(@RequestBody ActivityModelReq req) {
		return new ResponseEntity<>(activityModelService.queryActivityModel(req), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/placeProducts", method = { RequestMethod.GET,RequestMethod.POST })
	public ResponseEntity<ActivityModelRep<Object>> placeProducts(@RequestBody ActivityModelReq req) {
		return new ResponseEntity<>(activityModelService.placeProducts(req), HttpStatus.OK);
	}
}
