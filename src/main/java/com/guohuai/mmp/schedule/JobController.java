package com.guohuai.mmp.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.mmp.publisher.product.agreement.ProductAgreementService;

@RestController
@RequestMapping(value = "/mimosa/boot/job", produces = "application/json")
public class JobController extends BaseController {
	private Logger logger = LoggerFactory.getLogger(JobController.class);

	@Autowired
	private ProductAgreementService productAgreementService;
	
	@RequestMapping(value = "createhtml", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<BaseRep> createhtml() {
		BaseRep rep = new BaseRep();
		logger.info("<<-----create html start----->>");
		try {
			this.productAgreementService.makeContract();
		} catch (Throwable e) {
			logger.error("<<-----create html fail----->>");
			e.printStackTrace();
		}
		logger.info("<<-----pdf success----->>");
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	@RequestMapping(value = "uploadpdf", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<BaseRep> uploadPDF() {
		BaseRep rep = new BaseRep();
		
		try {
			productAgreementService.uploadPDF();
		} catch (Throwable e) {
			logger.error("<<----- fail----->>");
			e.printStackTrace();
		}
		
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	
	
}
