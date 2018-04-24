package com.guohuai.mmp.investor.bank;

import com.guohuai.basic.component.ext.web.BaseController;

//@RestController
//@RequestMapping(value = "/mimosa/boot/investor/bank", produces = "application/json")
public class BankBootController extends BaseController {

//	@Autowired
//	private BankService bankService;
	
//	/**
//	 * 绑卡的时候验证4要素
//	 * @param req
//	 * @return
//	 */
//	@RequestMapping(value = "valid4ele", method = RequestMethod.POST)
//	@ResponseBody
//	public ResponseEntity<BaseRep> valid4Ele(@RequestBody @Valid BankValid4EleRequest req) {
//		String userOid = super.getLoginUser();
//		BaseRep rep = this.bankService.valid4Ele(req, userOid);
//		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
//	}
//	
//	/**
//	 * 申请代扣协议
//	 * @param req
//	 * @return
//	 */
//	@RequestMapping(value = "validagree", method = RequestMethod.POST)
//	@ResponseBody
//	public ResponseEntity<BankAgreeRep> validAgree(@RequestBody BankAgreeRequest req) {
//		String userOid = super.getLoginUser();
//		BankAgreeRep rep = this.bankService.validAgreement(req, userOid);
//		return new ResponseEntity<BankAgreeRep>(rep, HttpStatus.OK);
//	}
//	
//	/**
//	 * 确认签约代扣协议
//	 * @param req
//	 * @return
//	 */
//	@RequestMapping(value = "confirmAgree", method = RequestMethod.POST)
//	@ResponseBody
//	public ResponseEntity<BaseRep> confirmAgree(@RequestBody BankAgreeRequest req) {
//		String userOid = super.getLoginUser();
//		BaseRep rep = this.bankService.confirmAgreement(req, userOid);
//		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
//	}
//	
//	/**
//	 * 用户绑卡
//	 * @param req
//	 * @return
//	 */
//	@RequestMapping(value = "bind", method = RequestMethod.POST)
//	@ResponseBody
//	public ResponseEntity<BaseRep> binkBank(@RequestBody BankBindRequest req) {
//		String userOid = super.getLoginUser();
//		BaseRep rep = this.bankService.bindBank(req, userOid);
//		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
//	}
}
