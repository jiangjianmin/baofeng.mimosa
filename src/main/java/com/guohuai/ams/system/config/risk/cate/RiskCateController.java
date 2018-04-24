package com.guohuai.ams.system.config.risk.cate;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.guohuai.component.util.StringUtil;
import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.web.view.BaseResp;

@RestController
@RequestMapping(value = "/mimosa/system/ccr/cate", produces = "application/json;charset=utf-8")
public class RiskCateController extends BaseController {

	@Autowired
	private RiskCateService riskCateService;

	@RequestMapping(value = "/options", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<Object> options(@RequestParam(required = false) String type) {
		super.getLoginUser();

		if (StringUtil.isEmpty(type)) {
			Map<String, List<RiskCate>> map = this.riskCateService.options();
			return new ResponseEntity<Object>(map, HttpStatus.OK);
		} else {
			List<RiskCate> list = this.riskCateService.options(type);
			return new ResponseEntity<Object>(list, HttpStatus.OK);
		}

	}
	

	/**
	 * title是否唯一
	 * @Title: validateTitle 
	 * @author vania
	 * @version 1.0
	 * @see: 
	 * @param cateTitle
	 * @param id
	 * @return ResponseEntity<BaseResp>    返回类型
	 */
	@RequestMapping(value = "/validateTitle", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<BaseResp> validateTitle(@RequestParam String cateTitle, @RequestParam(required = false) String id) {
		BaseResp pr = new BaseResp();
		long single = this.riskCateService.validateSingle("title", cateTitle, id);
		return new ResponseEntity<BaseResp>(pr, single > 0 ? HttpStatus.CONFLICT : HttpStatus.OK);
	}

}
