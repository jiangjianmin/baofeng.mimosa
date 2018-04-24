package com.guohuai.mmp.lx.serfee;

import java.sql.Date;

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

import com.alibaba.fastjson.JSON;
import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.component.web.view.PageResp;
import com.guohuai.component.web.view.PagesRep;
import com.guohuai.mmp.platform.accountingnotify.AccountingNotifyRep;
import com.guohuai.mmp.platform.accountingnotify.AccountingNotifyService;

@RestController
@RequestMapping(value = "/mimosa/boot/lx/serfee", produces = "application/json")
public class SerFeeController extends BaseController {
	
	@Autowired
	SerFeeService serFeeService;
	
	@Autowired
	AccountingNotifyService accountingNotifyService;
	
	
	
	/**
	 * 获取渠道费用合计
	 * @param channelOid
	 * @return
	 */
	@RequestMapping(value = "channelSumFee", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<SerFeeQueryRep> channelSumFee(@RequestParam String channelOid) {
		SerFeeQueryRep serRep = this.serFeeService.channelSumAccruedFee(channelOid);
		System.out.println(JSON.toJSONString(serRep));
		return new ResponseEntity<SerFeeQueryRep>(serRep, HttpStatus.OK);
	}
	
	/**
	 * 获取某日产品计提费用
	 * @param productOid
	 * @param tDay
	 * @return
	 */
	@RequestMapping(value = "findFeeByDate", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<SerFeeQueryRep> findFeeByDate(@RequestParam String productOid,@RequestParam Date tDay) {
		SerFeeQueryRep serRep = this.serFeeService.findFeeByDate(productOid,tDay);
		return new ResponseEntity<SerFeeQueryRep>(serRep, HttpStatus.OK);
	}
	
	/**
	 * 获取产品计提费用明细
	 * @param productOid
	 * @param type
	 * @param page
	 * @param rows
	 * @param sortField
	 * @param sort
	 * @return
	 */
	@RequestMapping(value = "/getAccruedFeeListByOid", method = { RequestMethod.GET,RequestMethod.POST })
	public @ResponseBody ResponseEntity<PageResp<SerFeeQueryRep>> getAccruedFeeListByOid(
			@RequestParam String productOid,
			@RequestParam(required = false, defaultValue = "1") int page,
			@RequestParam(required = false, defaultValue = "15") int rows,
			@RequestParam(required = false, defaultValue = "createTime") String sortField,
			@RequestParam(required = false, defaultValue = "desc") String sort) {
		Direction sortDirection = Direction.DESC;
		if (!"desc".equals(sort)) {
			sortDirection = Direction.ASC;
		}
		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(sortDirection, sortField)));
		PageResp<SerFeeQueryRep> rep = serFeeService.getAccruedFeeListByOid(productOid, pageable);
		return new ResponseEntity<PageResp<SerFeeQueryRep>>(rep, HttpStatus.OK);
	}
	
	/**
	 * 获取产品实际支付费用明细
	 * @param productOid
	 * @param type
	 * @param page
	 * @param rows
	 * @param sortField
	 * @param sort
	 * @return
	 */
	@RequestMapping(value = "/getFeeListByOid", method = { RequestMethod.GET,RequestMethod.POST })
	public @ResponseBody ResponseEntity<PagesRep<AccountingNotifyRep>> getFeeListByOid(
			@RequestParam String productOid,
			@RequestParam(required = false, defaultValue = "1") int page,
			@RequestParam(required = false, defaultValue = "15") int rows,
			@RequestParam(required = false, defaultValue = "createTime") String sortField,
			@RequestParam(required = false, defaultValue = "desc") String sort) {
		Direction sortDirection = Direction.DESC;
		if (!"desc".equals(sort)) {
			sortDirection = Direction.ASC;
		}
		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(sortDirection, sortField)));
		PagesRep<AccountingNotifyRep> rep = accountingNotifyService.getFeeListByOid(productOid, pageable);
		return new ResponseEntity<PagesRep<AccountingNotifyRep>>(rep, HttpStatus.OK);
	}
}
