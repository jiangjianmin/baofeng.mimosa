package com.guohuai.ams.system.config.project.warrantyLevel;

import java.util.List;

import javax.validation.Valid;

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
import com.guohuai.component.web.view.BaseResp;

@RestController
@RequestMapping(value = "/mimosa/system/ccp/warrantyLevel", produces = "application/json;charset=utf-8")
public class CCPWarrantyLevelController extends BaseController {

	@Autowired
	private CCPWarrantyLevelService CCPWarrantyLevelService;

	@RequestMapping(value = "/save", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<CCPWarrantyLevel> create(@Valid CCPWarrantyLevelForm form) {
		super.getLoginUser();
		CCPWarrantyLevel warrantyExpire = this.CCPWarrantyLevelService.save(form);
		return new ResponseEntity<CCPWarrantyLevel>(warrantyExpire, HttpStatus.OK);
	}

	@RequestMapping(value = "/saveList", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<List<CCPWarrantyLevel>> saveList(@Valid @RequestBody CCPWarrantyLevelOptionsForm form) {
		super.getLoginUser();
		List<CCPWarrantyLevel> entityList = this.CCPWarrantyLevelService.save(form);
		return new ResponseEntity<List<CCPWarrantyLevel>>(entityList, HttpStatus.OK);
	}

	@RequestMapping(value = "/delete", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<BaseResp> delete(@RequestParam String oid) {
		super.getLoginUser();
		this.CCPWarrantyLevelService.delete(oid);
		return new ResponseEntity<BaseResp>(new BaseResp(), HttpStatus.OK);
	}

	@RequestMapping(value = "/search", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<List<CCPWarrantyLevel>> search() {
		super.getLoginUser();
		List<CCPWarrantyLevel> list = this.CCPWarrantyLevelService.search();
		return new ResponseEntity<List<CCPWarrantyLevel>>(list, HttpStatus.OK);
	}

}
