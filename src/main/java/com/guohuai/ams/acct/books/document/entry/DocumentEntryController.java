package com.guohuai.ams.acct.books.document.entry;

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
@RequestMapping(value = "/mimosa/acct/books/document/entry", produces = "application/json;charset=utf-8")
public class DocumentEntryController extends BaseController {

	@Autowired
	private DocumentEntryService documentEntryService;

	@RequestMapping(value = "/search", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<DocumentEntryPage> search(DocumentEntryForm form) {
		super.getLoginUser();

		DocumentEntryPage ep = this.documentEntryService.search(form.getRelative(), form.getStartDate(), form.getEndDate(), form.getPage() - 1, form.getSize());

		return new ResponseEntity<DocumentEntryPage>(ep, HttpStatus.OK);
	}

	@RequestMapping(value = "/detail", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<DocumentEntryDetail> detail(@RequestParam String documentOid) {
		super.getLoginUser();
		DocumentEntryDetail d = this.documentEntryService.detail(documentOid);
		return new ResponseEntity<DocumentEntryDetail>(d, HttpStatus.OK);
	}

}
