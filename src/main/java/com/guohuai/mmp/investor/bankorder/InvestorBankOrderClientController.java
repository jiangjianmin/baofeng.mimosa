package com.guohuai.mmp.investor.bankorder;

import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.RemoteUtil;
import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.component.web.view.PagesRep;
import com.guohuai.mmp.platform.payment.PaymentServiceImpl;

@RestController
@RequestMapping(value = "/mimosa/client/investor/bankorder", produces = "application/json")
public class InvestorBankOrderClientController extends BaseController {
	
	
	@Autowired
	private InvestorBankOrderExtService investorBankOrderExtService;
	@Autowired
	private InvestorBankOrderService investorBankOrderService;
	@Autowired
	private PaymentServiceImpl paymentServiceImpl;
	
	@RequestMapping(value = "deposit", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BankOrderRep> deposit(@RequestBody @Valid BankOrderReq bankOrderReq) {
		String uid = this.getLoginUser();
		bankOrderReq.setIp(RemoteUtil.getRemoteAddr(request));
		bankOrderReq.setUid(uid);
		BankOrderRep rep = this.investorBankOrderExtService.deposit(bankOrderReq);
		return new ResponseEntity<BankOrderRep>(rep, HttpStatus.OK);
	}
	
//	@RequestMapping(value = "depositcallbak", method = RequestMethod.POST)
//	@ResponseBody
//	public ResponseEntity<BaseRep> depositCallBack(@RequestParam String orderCode, @RequestParam String notifyId) {
//		
//		DepositStatusSync depositStatus = new DepositStatusSync();
//		depositStatus.setOuter_trade_no(orderCode);
//		depositStatus.setNotify_id(notifyId);
//		depositStatus.setDeposit_status(InvestorBankOrderService.PAYMENT_success);
//		this.paymentServiceImpl.onDepositStatus(depositStatus);
//		BaseRep rep =  new BaseRep();
//		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
//	}
//	
//	@RequestMapping(value = "withdraw", method = RequestMethod.POST)
//	@ResponseBody
//	public ResponseEntity<BankOrderRep> withdraw(@RequestBody @Valid BankOrderReq bankOrderReq) {
//		String uid = this.getLoginUser();
//		bankOrderReq.setIp(RemoteUtil.getRemoteAddr(request));
//		bankOrderReq.setUid(uid);
//		BankOrderRep rep = this.investorBankOrderExtService.withdraw(bankOrderReq);
//		return new ResponseEntity<BankOrderRep>(rep, HttpStatus.OK);
//	}
	
//	@RequestMapping(value = "withdrawcallback", method = RequestMethod.POST)
//	@ResponseBody
//	public ResponseEntity<BaseRep> withdrawCallBack(@RequestParam String orderCode, @RequestParam String notifyId) {
//		WithdrawStatusSync withdrawStatus = new WithdrawStatusSync();
//		withdrawStatus.setOuter_trade_no(orderCode);
//		withdrawStatus.setNotify_id(notifyId);
//		withdrawStatus.setWithdraw_status(InvestorBankOrderService.PAYMENT_success);
//		this.paymentServiceImpl.onWithdrawStatus(withdrawStatus);
//		BaseRep rep = new BaseRep();
//		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
//	}
//	
	@RequestMapping(value = "isdone", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseRep> isDone(@RequestBody @Valid BankOrderIsDoneReq isDone) {
		this.getLoginUser();
		
		BaseRep rep = this.investorBankOrderService.isDone(isDone);
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}

	/**
	 * 我的充值提现记录
	 * 
	 * @param sDate
	 * @param eDate
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping(value = "myquery", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<PagesRep<MyBankOrderRep>> myquery(@RequestParam Date sDate, @RequestParam Date eDate,
			@RequestParam int page, @RequestParam int rows) {

		final String uid = this.getLoginUser();
		page = page < 1 ? 1 : page;
		rows = rows < 1 ? 1 : rows;
		
		final Timestamp sTime = DateUtil.getTimestampZeroOfDate(sDate);// 开始日期的0点
		final Timestamp eTime = DateUtil.getTimestampLastOfDate(eDate);// 结束日期的23:59:59.999

		Specification<InvestorBankOrderEntity> spec = new Specification<InvestorBankOrderEntity>() {
			@Override
			public Predicate toPredicate(Root<InvestorBankOrderEntity> root, CriteriaQuery<?> query,
					CriteriaBuilder cb) {
				return cb.and(cb.equal(root.get("investorBaseAccount").get("userOid").as(String.class), uid), // 投资者ID
						cb.between(root.get("createTime").as(Timestamp.class), sTime, eTime)
				);
			}
		};

		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(Direction.DESC, "createTime")));
		PagesRep<MyBankOrderRep> pages = this.investorBankOrderService.myquery(spec, pageable);

		return new ResponseEntity<PagesRep<MyBankOrderRep>>(pages, HttpStatus.OK);
	}
}
