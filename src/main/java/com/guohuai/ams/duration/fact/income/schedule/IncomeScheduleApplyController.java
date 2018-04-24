package com.guohuai.ams.duration.fact.income.schedule;

import java.text.ParseException;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.web.view.BaseResp;
import com.guohuai.component.web.view.PageResp;

@RestController
@RequestMapping(value = "/mimosa/duration/income/schedule/apply", produces = "application/json;charset=utf-8")
public class IncomeScheduleApplyController extends BaseController {
	
	@Autowired
	private IncomeScheduleApplyService incomeScheduleApplyService;
	
	
	/**
	 * 收益分配排期申请
	 * @param form
	 * @return
	 */
	@RequestMapping(value = "/add", name = "资产池-收益分配排期申请", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseResp> add(@Valid IncomeScheduleApplyForm form){
		CheckUitls.checkIncomeDistrDate(form);
//		String operator = super.getLoginUser();
		String operator = super.getLoginUserName();
		BaseResp resp = this.incomeScheduleApplyService.add(form, operator);
		return new ResponseEntity<BaseResp>(resp, HttpStatus.OK);
	}
	
	/**
	 * 收益分配排期修改申请
	 * @param form
	 * @return
	 */
	@RequestMapping(value = "/update", name = "资产池-收益分配排期修改申请", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseResp> update(@Valid IncomeScheduleUpdateForm form){
//		String operator = super.getLoginUser();
		String operator = super.getLoginUserName();
		BaseResp resp = this.incomeScheduleApplyService.update(form, operator);
		return new ResponseEntity<BaseResp>(resp, HttpStatus.OK);
	}
	
	/**
	 * 收益分配排期删除申请
	 * @param form
	 * @return
	 */
	@RequestMapping(value = "/delete", name = "资产池-收益分配排期修改申请", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseResp> delete(@RequestParam String oid){
//		String operator = super.getLoginUser();
		String operator = super.getLoginUserName();
		BaseResp resp = this.incomeScheduleApplyService.delete(oid, operator);
		return new ResponseEntity<BaseResp>(resp, HttpStatus.OK);
	}
	
	
	/**
	 * 收益分配排期申请详情
	 * @param oid  收益分配表oid
	 * @return
	 */
	@RequestMapping(value = "/detail", name = "资产池-收益分配排期申请详情", method = {RequestMethod.POST,RequestMethod.GET})
	@ResponseBody
	public ResponseEntity<IncomeScheduleApplyResp> detail(@RequestParam String oid) {
		IncomeScheduleApplyResp pr = this.incomeScheduleApplyService.detail(oid);
		return new ResponseEntity<IncomeScheduleApplyResp>(pr, HttpStatus.OK);
	}
	
	/**
	 * 收益分配排期申请列表
	 * @param request
	 * @param assetPoolOid 资产池id
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping(value = "/list", name = "资产池-收益分配排期申请列表", method = {RequestMethod.POST,RequestMethod.GET})
	@ResponseBody
	public ResponseEntity<PageResp<IncomeScheduleApplyResp>> list(HttpServletRequest request,
			@RequestParam String assetPoolOid,
			@RequestParam int page, 
			@RequestParam int rows,
			@RequestParam(required = false,defaultValue="desc") String sort,
			@RequestParam(required = false,defaultValue="createTime") String sortField
			) {
		
		if (page < 1) {
			page = 1;
		}
		if (rows < 1) {
			rows = 10;
		}
		Direction sortDirection = Direction.DESC;
		if (!"desc".equals(sort)) {
			sortDirection = Direction.ASC;
		}
		
		Specification<IncomeScheduleApply> spec = new Specification<IncomeScheduleApply>() {
			@Override
			public Predicate toPredicate(Root<IncomeScheduleApply> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				Predicate a = cb.equal(root.get("assetPool").get("oid").as(String.class), assetPoolOid);
				Predicate b = cb.notEqual(root.get("status").as(String.class), IncomeScheduleApply.STATUS_delete);
				query.where(cb.and(a, b));
				return query.getRestriction();
			}
		};
				
//		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(Direction.DESC, "status"), new Order(Direction.DESC, "basicDate")));
		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(sortDirection, sortField)));
		PageResp<IncomeScheduleApplyResp> rep = this.incomeScheduleApplyService.queryPage(spec, pageable);
		return new ResponseEntity<PageResp<IncomeScheduleApplyResp>>(rep, HttpStatus.OK);
	}
	
	/**
	 * 审核通过
	 * @param oid
	 * @return
	 */
	@RequestMapping(value = "/pass", name = "资产池-收益分配排期审核通过", method = {RequestMethod.POST,RequestMethod.GET})
	@ResponseBody
	public ResponseEntity<BaseResp> pass(@RequestParam String oid) throws ParseException {
//		String operator = super.getLoginUser();
		String operator = super.getLoginUserName();
		BaseResp repponse = this.incomeScheduleApplyService.approve(oid, operator, IncomeScheduleApply.STATUS_pass);
		return new ResponseEntity<BaseResp>(repponse, HttpStatus.OK);
	}
	
	/**
	 * 审核不通过
	 * @param oid
	 * @return
	 */
	@RequestMapping(value = "/reject", name = "资产池-收益分配排期审核不通过", method = {RequestMethod.POST,RequestMethod.GET})
	@ResponseBody
	public ResponseEntity<BaseResp> reject(@RequestParam String oid) throws ParseException {
//		String operator = super.getLoginUser();
		String operator = super.getLoginUserName();
		BaseResp repponse = this.incomeScheduleApplyService.approve(oid, operator, IncomeScheduleApply.STATUS_reject);
		return new ResponseEntity<BaseResp>(repponse, HttpStatus.OK);
	}
	
	/**
	 * 审核删除
	 * @param oid
	 * @return
	 */
	@RequestMapping(value = "/approveDelete", name = "资产池-收益分配排期审核删除", method = {RequestMethod.POST,RequestMethod.DELETE})
	@ResponseBody
	public ResponseEntity<BaseResp> approveDelete(@RequestParam String oid) {
//		String operator = super.getLoginUser();
		String operator = super.getLoginUserName();
		BaseResp repponse = this.incomeScheduleApplyService.approveDelete(oid, operator);
		return new ResponseEntity<BaseResp>(repponse, HttpStatus.OK);
	}
	
}
