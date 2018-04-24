package com.guohuai.ams.system.config.risk.indicate;

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
@RequestMapping(value = "/mimosa/system/ccr/indicate", produces = "application/json;charset=utf-8")
public class RiskIndicateController extends BaseController {

	@Autowired
	private RiskIndicateService riskIndicateService;

	@RequestMapping(value = "/search", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<List<RiskIndicateResp>> search(@RequestParam(defaultValue = "") String type, @RequestParam(defaultValue = "") String keyword) {
		super.getLoginUser();
		List<RiskIndicateResp> list = this.riskIndicateService.search(type, keyword);
		return new ResponseEntity<List<RiskIndicateResp>>(list, HttpStatus.OK);
	}

	@RequestMapping(value = "/save", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<RiskIndicateResp> save(@Valid RiskIndicateForm form) {
		super.getLoginUser();
		RiskIndicate i = this.riskIndicateService.save(form);
		return new ResponseEntity<RiskIndicateResp>(new RiskIndicateResp(i), HttpStatus.OK);
	}

	@RequestMapping(value = "/enable", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<RiskIndicateResp> enable(@RequestParam String oid) {
		super.getLoginUser();
		RiskIndicate i = this.riskIndicateService.enable(oid);
		return new ResponseEntity<RiskIndicateResp>(new RiskIndicateResp(i), HttpStatus.OK);
	}

	@RequestMapping(value = "/disable", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<RiskIndicateResp> disable(@RequestParam String oid) {
		super.getLoginUser();
		RiskIndicate i = this.riskIndicateService.disable(oid);
		return new ResponseEntity<RiskIndicateResp>(new RiskIndicateResp(i), HttpStatus.OK);
	}

	@RequestMapping(value = "/delete", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<RiskIndicateResp> delete(@RequestParam String oid) {
		super.getLoginUser();
		RiskIndicate i = this.riskIndicateService.delete(oid);
		return new ResponseEntity<RiskIndicateResp>(new RiskIndicateResp(i), HttpStatus.OK);
	}

	@RequestMapping(value = "/options", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<List<RiskIndicateOption>> options(@RequestParam String type) {
		super.getLoginUser();
		List<RiskIndicateOption> list = this.riskIndicateService.options(type);
		return new ResponseEntity<List<RiskIndicateOption>>(list, HttpStatus.OK);
	}

	/**
	 * title是否唯一
	 * @Title: validateTitle 
	 * @author vania
	 * @version 1.0
	 * @see: 
	 * @param indicateTitle
	 * @param id
	 * @return ResponseEntity<BaseResp>    返回类型
	 */
	@RequestMapping(value = "/validateTitle", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<BaseResp> validateTitle(@RequestParam String indicateTitle, @RequestParam(required = false) String id) {
		BaseResp pr = new BaseResp();
		long single = this.riskIndicateService.validateSingle("title", indicateTitle, id);
		return new ResponseEntity<BaseResp>(pr, single > 0 ? HttpStatus.CONFLICT : HttpStatus.OK);
	}
	
}
