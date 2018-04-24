package com.guohuai.mmp.publisher.bankorder;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.component.web.view.PagesRep;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountEntity;
import com.guohuai.mmp.publisher.baseaccount.loginacc.PublisherLoginAccService;

import net.kaczmarzyk.spring.data.jpa.domain.In;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;

@RestController
@RequestMapping(value = "/mimosa/boot/publisher/bankorder", produces = "application/json")
public class PublisherBankOrderBootController extends BaseController {
	
	@Autowired
	private PublisherDepositBankOrderService publisherDepositBankOrderService;
	
	@Autowired
	private PublisherWithdrawBankOrderService publisherWithdrawBankOrderService;
	
	@Autowired
	private PublisherBankOrderService publisherBankOrderService;
	
	@Autowired
	private PublisherLoginAccService publisherLoginAccService;
	
	@RequestMapping(value = "deposit", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseRep> deposit(@Valid BankOrderReq bankOrderReq) {
		String uid = this.getLoginUser();
		bankOrderReq.setUid(uid);
		BaseRep rep = this.publisherDepositBankOrderService.deposit(bankOrderReq);
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	@RequestMapping(value = "depositdup", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseRep> depositDressUp(@RequestParam(required = true) String orderOid) {
		this.getLoginUser();
		
		BaseRep rep = this.publisherDepositBankOrderService.depositDressUp(orderOid);
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	
	@RequestMapping(value = "withdraw", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseRep> withdraw(@Valid BankOrderReq bankOrderReq) {
		String uid = this.getLoginUser();


		bankOrderReq.setUid(uid);
		BaseRep rep = this.publisherWithdrawBankOrderService.withdraw(bankOrderReq);
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	@RequestMapping(value = "withdrawdup", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseRep> withdrawDressUp(@RequestParam(required = true) String orderOid) {
		this.getLoginUser();

		BaseRep rep = this.publisherWithdrawBankOrderService.withdrawDressUp(orderOid);
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	
	@RequestMapping(value = "mng", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<PagesRep<PublisherBankOrderQueryRep>> mng(HttpServletRequest request,
			@And({@Spec(params = "orderType", path = "orderType", spec = In.class),
				  @Spec(params = "orderStatus", path = "orderStatus", spec = In.class)}) Specification<PublisherBankOrderEntity> spec,
			@RequestParam int page, 
			@RequestParam int rows,
			@RequestParam(required = false, defaultValue = "createTime") String sort,
			@RequestParam(required = false, defaultValue = "desc") String order) {
		Direction sortDirection = Direction.DESC;
		if (!"desc".equals(order)) {
			sortDirection = Direction.ASC;
		}
		final String uid = this.getLoginUser();
		final PublisherBaseAccountEntity baseAccount = publisherLoginAccService.findByLoginAcc(uid);
		Specification<PublisherBankOrderEntity> speci = new Specification<PublisherBankOrderEntity>() {
			@Override
			public Predicate toPredicate(Root<PublisherBankOrderEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				Predicate tmp = cb.equal(root.get("publisherBaseAccount"), baseAccount);
				return tmp;
			}
		};
		spec = Specifications.where(spec).and(speci);
		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(sortDirection, sort)));
		PagesRep<PublisherBankOrderQueryRep> rep = this.publisherBankOrderService.publisherBankOrderMng(spec, pageable);
		return new ResponseEntity<PagesRep<PublisherBankOrderQueryRep>>(rep, HttpStatus.OK);
	}

	
}
