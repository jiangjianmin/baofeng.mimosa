package com.guohuai.mmp.platform.errorlog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.web.view.BaseRep;

@RestController
@RequestMapping(value = "/mimosa/client/platform/errorlog", produces = "application/json")
public class PlatformErrorLogClientController extends BaseController {
	
	@Autowired
	private PlatformErrorLogService platformErrorLogService;
	
	@RequestMapping(value = "slog", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseRep> saveErrorLog(@RequestBody ErrorLogReq req) {
		String uid = this.getLoginUser();
		BaseRep rep = this.platformErrorLogService.saveErrorLog(req, uid);
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	@RequestMapping(value="appstart", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseRep> saveAppStartErrorLog(@RequestBody ErrorLogReq req) {
		String uid = "appStartErrorLog";
		BaseRep rep = this.platformErrorLogService.saveErrorLog(req, uid);
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
}
