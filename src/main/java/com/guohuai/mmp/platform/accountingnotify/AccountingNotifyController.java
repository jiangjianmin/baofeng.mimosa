package com.guohuai.mmp.platform.accountingnotify;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.guohuai.component.config.AccountingDefineConfig;
import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.web.view.PagesRep;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountService;
import com.guohuai.mmp.publisher.baseaccount.PublisherDetailRep;

@RestController
@RequestMapping(value = "/mimosa/boot/accounting", produces = "application/json")
public class AccountingNotifyController extends BaseController {

	@Autowired
	private PublisherBaseAccountService spvService;
	@Autowired
	private AccountingNotifyService accountingNotifyService;
	
	/**
	 * 获取spv和关联方信息
	 * @return
	 */
	@RequestMapping(value = "findSPVAndCustomer", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<SPVCustomerRep> findSPVAndCustomer() {
		List<PublisherDetailRep> SPVList = spvService.options();
		LeAccounting la = new LeAccounting();
		la.setCustomer_id(AccountingDefineConfig.define.get(AccountingNotifyEntity.PLATFORM_CUSTOMER_ID));
		la.setCustomer_account(AccountingDefineConfig.define.get(AccountingNotifyEntity.PLATFORM_CUSTOMER_ACCOUNT));
		SPVCustomerRep rep = new SPVCustomerRep();
		rep.setCustomer(la);
		rep.setSPVList(SPVList);
		return new ResponseEntity<SPVCustomerRep>(rep, HttpStatus.OK);
	}
	
	
	/**
	 * 获取某一日录入轧差费用明细
	 * @param productOid
	 * @param busDay
	 * @return
	 */
	@RequestMapping(value = "findOffsetFee", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<PagesRep<AccountingNotifyRep>> findOffsetFee(
			@RequestParam String productOid,
			@RequestParam String busDay,
			@RequestParam(required = false, defaultValue = "1") int page,
			@RequestParam(required = false, defaultValue = "20") int rows,
			@RequestParam(required = false, defaultValue = "createTime") String sortField,
			@RequestParam(required = false, defaultValue = "desc") String sort
			) {
		Direction sortDirection = Direction.DESC;
		if (!"desc".equals(sort)) {
			sortDirection = Direction.ASC;
		}
		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(sortDirection, sortField)));
		PagesRep<AccountingNotifyRep> rep = accountingNotifyService.findOffsetFee(productOid,busDay, pageable);
		return new ResponseEntity<PagesRep<AccountingNotifyRep> >(rep, HttpStatus.OK);
	}
	
}
