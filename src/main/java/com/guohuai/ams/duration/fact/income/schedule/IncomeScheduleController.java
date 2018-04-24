package com.guohuai.ams.duration.fact.income.schedule;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;

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
import com.guohuai.component.web.view.PageResp;

@RestController
@RequestMapping(value = "/mimosa/duration/income/schedule", produces = "application/json;charset=utf-8")
public class IncomeScheduleController extends BaseController {
	
	@Autowired
	private IncomeScheduleService incomeScheduleService;
	
	
	/**
	 * 收益分配排期申请详情
	 * @param oid  收益分配表oid
	 * @return
	 */
	@RequestMapping(value = "/detail", name = "资产池-收益分配排期申请详情", method = {RequestMethod.POST,RequestMethod.GET})
	@ResponseBody
	public ResponseEntity<IncomeScheduleResp> detail(@RequestParam String oid) {
		IncomeScheduleResp pr = this.incomeScheduleService.detail(oid);
		return new ResponseEntity<IncomeScheduleResp>(pr, HttpStatus.OK);
	}
	
	/**
	 * 收益分配排期申请列表
	 * @param request
	 * @param assetPoolOid 资产池id
	 * @param page
	 * @param rows
	 * @param sort
	 * @param order
	 * @return
	 */
	@RequestMapping(value = "/list", name = "资产池-收益分配排期申请列表", method = {RequestMethod.POST,RequestMethod.GET})
	@ResponseBody
	public ResponseEntity<PageResp<IncomeScheduleResp>> list(HttpServletRequest request,
			@RequestParam String assetPoolOid,
			@RequestParam int page, 
			@RequestParam int rows,
			@RequestParam(required = false, defaultValue = "basicDate") String sort,
			@RequestParam(required = false, defaultValue = "desc") String order) {
		
		if (page < 1) {
			page = 1;
		}
		if (rows < 1) {
			rows = 10;
		}
		
		Direction sortDirection = Direction.DESC;
		if (!"desc".equals(order)) {
			sortDirection = Direction.ASC;
		}
		Specification<IncomeSchedule> spec = new Specification<IncomeSchedule>() {
			@Override
			public Predicate toPredicate(Root<IncomeSchedule> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				Predicate a = cb.equal(root.get("assetPool").get("oid").as(String.class), assetPoolOid);
				query.where(a);
				return query.getRestriction();
			}
		};
				
		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(sortDirection, sort)));
		PageResp<IncomeScheduleResp> rep = this.incomeScheduleService.queryPage(spec, pageable);
		return new ResponseEntity<PageResp<IncomeScheduleResp>>(rep, HttpStatus.OK);
	}
	
	/**
	 * 获取资产池最大已分配收益排期时间+1
	 * @param oid
	 * @return
	 */
	@RequestMapping(value = "/getBaseDate", name = "资产池-收益分配排期获取应排期日期", method = {RequestMethod.POST,RequestMethod.DELETE})
	@ResponseBody
	public ResponseEntity<IncomeScheduleResp> getBaseDate(@RequestParam String oid) {
		IncomeScheduleResp repponse = this.incomeScheduleService.getBaseDate(oid);
		return new ResponseEntity<IncomeScheduleResp>(repponse, HttpStatus.OK);
	}
}
