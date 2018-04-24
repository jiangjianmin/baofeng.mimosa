package com.guohuai.ams.acct.doc.template.entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.guohuai.basic.component.ext.web.BaseController;

@RestController
@RequestMapping(value = "/mimosa/acct/doc/template/entry", produces = "application/json;charset=utf-8")
public class DocTemplateEntryController extends BaseController {

	@Autowired
	private DocTemplateEntryService docTemplateEntryService;

	@RequestMapping(value = "/search", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<DocTemplateEntryPage> search(@RequestParam(defaultValue = "") String templateType, @RequestParam String templateName, @RequestParam int page,
			@RequestParam int size) {
		super.getLoginUser();

		DocTemplateEntryPage ep = this.docTemplateEntryService.search(templateType, templateName, page - 1, size);

		return new ResponseEntity<DocTemplateEntryPage>(ep, HttpStatus.OK);
	}

}
