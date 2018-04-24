package com.guohuai.ams.acct.books.document;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.guohuai.basic.component.ext.web.BaseController;

@RestController
@RequestMapping(value = "/mimosa/acct/books/document/warehouse", produces = "application/json;charset=utf-8")
public class WarehouseDocumentController extends BaseController {

	@Autowired
	private WarehouseDocumentService warehouseDocumentService;

	@RequestMapping(value = "/cashtoolPurchase", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<DocumentResp> cashtoolPurchase(DocumentForm form) {
		super.getLoginUser();
		Document d = this.warehouseDocumentService.cashtoolPurchase(form.getRelative(), form.getTicket(), form.getAmount());
		return new ResponseEntity<DocumentResp>(new DocumentResp(d), HttpStatus.OK);
	}

	@RequestMapping(value = "/cashtoolRedemption", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<DocumentResp> cashtoolRedemption(DocumentForm form) {
		super.getLoginUser();
		Document d = this.warehouseDocumentService.cashtoolRedemption(form.getRelative(), form.getTicket(), form.getAmount());
		return new ResponseEntity<DocumentResp>(new DocumentResp(d), HttpStatus.OK);
	}

	@RequestMapping(value = "/targetPurchase", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<DocumentResp> targetPurchase(DocumentForm form) {
		super.getLoginUser();
		Document d = this.warehouseDocumentService.targetPurchase(form.getRelative(), form.getTicket(), form.getAmount());
		return new ResponseEntity<DocumentResp>(new DocumentResp(d), HttpStatus.OK);
	}

	@RequestMapping(value = "/targetPremiumsTransOut", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<DocumentResp> targetPremiumsTransOut(DocumentForm form) {
		super.getLoginUser();
		Document d = this.warehouseDocumentService.targetPremiumsTransOut(form.getRelative(), form.getTicket(), form.getPrincipal(), form.getEstimate(), form.getRevenue());
		return new ResponseEntity<DocumentResp>(new DocumentResp(d), HttpStatus.OK);
	}

}
