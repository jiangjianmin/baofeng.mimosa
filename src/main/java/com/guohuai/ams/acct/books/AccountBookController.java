package com.guohuai.ams.acct.books;

import java.util.ArrayList;
import java.util.List;

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
@RequestMapping(value = "/mimosa/acct/books", produces = "application/json;charset=utf-8")
public class AccountBookController extends BaseController {

	@Autowired
	private AccountBookService accountBookService;

	@RequestMapping(value = "/init", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<List<AccountBookResp>> init(@RequestParam String relative) {
		super.getLoginUser();
		List<AccountBook> books = this.accountBookService.init(relative);
		List<AccountBookResp> view = new ArrayList<AccountBookResp>();
		for (AccountBook book : books) {
			view.add(new AccountBookResp(book));
		}
		return new ResponseEntity<List<AccountBookResp>>(view, HttpStatus.OK);
	}

	@RequestMapping(value = "/balance", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<List<AccountBookBalance>> balance(@RequestParam(defaultValue = "") String relative) {
		super.getLoginUser();
		List<AccountBookBalance> view = this.accountBookService.balance(relative);
		return new ResponseEntity<List<AccountBookBalance>>(view, HttpStatus.OK);
	}

}
