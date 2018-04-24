package com.guohuai.ams.system.config.project.warrantyMode;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.web.view.BaseResp;

@RestController
@RequestMapping(value = "/mimosa/system/ccp/warrantyMode", produces = "application/json;charset=utf-8")
public class CCPWarrantyModeController extends BaseController {

	@Autowired
	private CCPWarrantyModeService ccpWarrantyModeService;

	@RequestMapping(value = "/create", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<CCPWarrantyMode> create(@Valid CCPWarrantyModeForm form) {
		super.getLoginUser();
		CCPWarrantyMode warrantyMode = this.ccpWarrantyModeService.create(form);
		return new ResponseEntity<CCPWarrantyMode>(warrantyMode, HttpStatus.OK);
	}

	@RequestMapping(value = "/update", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<CCPWarrantyMode> update(@Valid CCPWarrantyModeForm form) {
		super.getLoginUser();
		CCPWarrantyMode warrantyMode = this.ccpWarrantyModeService.update(form);
		return new ResponseEntity<CCPWarrantyMode>(warrantyMode, HttpStatus.OK);
	}

	@RequestMapping(value = "/delete", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<BaseResp> delete(@RequestParam String oid) {
		super.getLoginUser();
		this.ccpWarrantyModeService.delete(oid);
		return new ResponseEntity<BaseResp>(new BaseResp(), HttpStatus.OK);
	}

	@RequestMapping(value = "/search", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<List<CCPWarrantyModeResp>> search() {
		super.getLoginUser();
		List<CCPWarrantyModeResp> list = this.ccpWarrantyModeService.search();
		return new ResponseEntity<List<CCPWarrantyModeResp>>(list, HttpStatus.OK);
	}

}
