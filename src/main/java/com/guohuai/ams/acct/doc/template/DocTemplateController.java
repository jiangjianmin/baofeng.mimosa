package com.guohuai.ams.acct.doc.template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.guohuai.basic.component.ext.web.BaseController;

@RestController
@RequestMapping(value = "/mimosa/acct/doc/template", produces = "application/json;charset=utf-8")
public class DocTemplateController extends BaseController {

	@Autowired
	private DocTemplateService docTemplateService;

	@RequestMapping(value = "/search", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<Map<String, Object>> search(@RequestParam(defaultValue = "") String type, @RequestParam String name, @RequestParam int page, @RequestParam int size) {
		super.getLoginUser();
		Page<DocTemplate> list = this.docTemplateService.search(type, name, page, size);
		List<DocTemplateResp> view = new ArrayList<DocTemplateResp>();
		for (DocTemplate a : list) {
			view.add(new DocTemplateResp(a));
		}

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("total", list.getTotalElements());
		map.put("rows", view);

		return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
	}

}
