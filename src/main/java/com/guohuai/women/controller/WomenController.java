package com.guohuai.women.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.guohuai.women.service.WomenService;
import com.guohuai.women.to.WomenReq;
import com.guohuai.women.to.WomenRes;


@RestController
@RequestMapping(value = "/mimosa/women", produces = "application/json")
public class WomenController {
	
	@Autowired
	WomenService womenService;
	
	@RequestMapping(value = "/checkinvest")
	public ResponseEntity<WomenRes> checkinvest(@Valid @RequestBody WomenReq req) {
		WomenRes res = new WomenRes();
		womenService.checkInvest(req,res);
		return new ResponseEntity<WomenRes>(res, HttpStatus.OK);
	}

}
