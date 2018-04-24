package com.guohuai.ams.acct.doc.type;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.guohuai.basic.component.ext.web.BaseController;

@RestController
@RequestMapping(value = "/mimosa/acct/doc/type", produces = "application/json;charset=utf-8")
public class DocTypeController extends BaseController {

	@Autowired
	private DocTypeService docTypeService;

	@RequestMapping(value = "/search", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<List<DocTypeResp>> search() {
		super.getLoginUser();
		List<DocType> list = this.docTypeService.search();
		List<DocTypeResp> view = new ArrayList<DocTypeResp>();
		for (DocType a : list) {
			view.add(new DocTypeResp(a));
		}
		return new ResponseEntity<List<DocTypeResp>>(view, HttpStatus.OK);
	}

}
