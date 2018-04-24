package com.guohuai.mmp.platform.finance.modifyorder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;


@RestController
@RequestMapping(value = "/mimosa/platform/finance/modifyorder", produces = "application/json")
public class ModifyOrderBootController extends BaseController{
	@Autowired
	ModifyOrderService modifyOrderService;
	/**
	 * 查询
	 * @param request
	 * @param spec
	 * @param page
	 * @param rows
	 * @param sort
	 * @param order
	 * @return
	 */
	@RequestMapping(value = "modifyOrderList", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<PagesRep<ModifyOrderRep>> modifyOrderList(HttpServletRequest request,
			@And({ @Spec(params = "orderCode", path = "orderCode", spec = Like.class),
					@Spec(params = "opType", path = "opType", spec = Equal.class)}) Specification<ModifyOrderEntity> spec,
			@RequestParam int page, @RequestParam int rows,
			@RequestParam String checkCode,
			@RequestParam(required = false, defaultValue = "createTime") String sort,
			@RequestParam(required = false, defaultValue = "desc") String order) {
		Direction sortDirection = Direction.DESC;
		if (!"desc".equals(order)) {
			sortDirection = Direction.ASC;
		}
		Specification<ModifyOrderEntity> stateSpec = new Specification<ModifyOrderEntity>() {
			@Override
			public Predicate toPredicate(Root<ModifyOrderEntity> root, CriteriaQuery<?> query,
					CriteriaBuilder cb) {
				return cb.and(cb.like(root.get("financeCheck").get("checkCode").as(String.class), "%"+checkCode+"%")
				);
			}
		};
		spec = Specifications.where(spec).and(stateSpec);
		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(sortDirection, sort)));
		PagesRep<ModifyOrderRep> rep = this.modifyOrderService.modifyOrderList(spec, pageable);

		return new ResponseEntity<PagesRep<ModifyOrderRep>>(rep, HttpStatus.OK);
	}
	@RequestMapping(value = "saveModifyOrder", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseRep> saveModifyOrder(@Valid ModifyOrderReq req) {
		String operator=this.getLoginUser();
		req.setOperator(operator);
		BaseRep rep = this.modifyOrderService.saveModifyOrder(req);
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	@RequestMapping(value = "modifyOrderApprove", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseRep> modifyOrderApprove(@RequestParam String oid,@RequestParam String resultOid,@RequestParam String approveStatus) {
		String operator=this.getLoginUser();
		BaseRep rep = this.modifyOrderService.modifyOrderApprove(oid,resultOid,approveStatus,operator);
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	@RequestMapping(value = "modifyOrderBatchApprove", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseRep> modifyOrderBatchApprove(@RequestParam String oids) {
		String operator=this.getLoginUser();
		List<String> oidList=Arrays.asList(oids.split(","));
		BaseRep rep = this.modifyOrderService.modifyOrderBatchApprove(oidList,operator);
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
}
