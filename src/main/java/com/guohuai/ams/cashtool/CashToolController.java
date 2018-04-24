/*   
 * Copyright © 2015 guohuaigroup All Rights Reserved.   
 *   
 * This software is the confidential and proprietary information of   
 * Founder. You shall not disclose such Confidential Information   
 * and shall use it only in accordance with the terms of the agreements   
 * you entered into with Founder.   
 *   
 */
package com.guohuai.ams.cashtool;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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

import com.guohuai.ams.investment.Investment;
import com.guohuai.component.util.DateUtil;
import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.web.view.BaseResp;

//import io.swagger.annotations.Api;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;

/**
 * <p>
 * Title: CashToolController.java
 * </p>
 * <p>
 * 现金管理工具操作相关接口
 * </p>
 * 
 * @author vania
 * @version 1.0
 * @created 2016年5月17日 下午2:39:04
 */
@RestController
@RequestMapping(value = "/mimosa/boot/cashTool", produces = "application/json;charset=UTF-8")
//@Api("现金管理工具操作相关接口")
public class CashToolController extends BaseController {

	@Autowired
	CashToolService cashToolService;

	@RequestMapping(name = "现金管理工具列表", value = "list", method = { RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody ResponseEntity<CashToolListResp> list(HttpServletRequest request,
			@And({ @Spec(params = "ticker", path = "ticker", spec = Equal.class),
					@Spec(params = "secShortName", path = "secShortName", spec = Like.class),
					@Spec(params = "etfLof", path = "etfLof", spec = Equal.class),
					@Spec(params = "state", path = "state", spec = Equal.class) }) Specification<CashTool> spec,
			@RequestParam(required = false, defaultValue = "1") int page,
			@RequestParam(required = false, defaultValue = "10") int rows,
			@RequestParam(required = false, defaultValue = "updateTime") String sortField,
			@RequestParam(required = false, defaultValue = "desc") String sort) {

		Direction sortDirection = Direction.DESC;
		if (!"desc".equals(sort)) {
			sortDirection = Direction.ASC;
		}
		Specification<CashTool> stateSpec = new Specification<CashTool>() {
			@Override
			public Predicate toPredicate(Root<CashTool> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.and(cb.notEqual(root.get("state"), CashTool.CASHTOOL_STATE_invalid),
						cb.notEqual(root.get("state"), CashTool.CASHTOOL_STATE_collecting));
			}
		};
		spec = Specifications.where(spec).and(stateSpec);
		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(sortDirection, sortField)));
		Page<CashTool> entitys = cashToolService.getCashToolList(spec, pageable);
		CashToolListResp resps = new CashToolListResp(entitys);
		return new ResponseEntity<CashToolListResp>(resps, HttpStatus.OK);
	}

	@RequestMapping(name = "现金管理工具详情", value = "detail", method = { RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody ResponseEntity<CashToolDetResp> detail(String oid) {
		CashTool entity = cashToolService.findByOid(oid);
		CashToolDetResp resp = new CashToolDetResp(entity);
		return new ResponseEntity<CashToolDetResp>(resp, HttpStatus.OK);
	}

	@RequestMapping(name = "新增现金管理工具", value = "add", method = { RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody ResponseEntity<BaseResp> add(@Valid CashToolManageForm form) {
		String operator = super.getLoginUser();
		CashTool cashTool = cashToolService.createInvestment(form);
		cashTool.setState(CashTool.CASHTOOL_STATE_waitPretrial);
		cashTool.setOperator(operator);
		cashTool.setCreateTime(DateUtil.getSqlCurrentDate());
		cashTool.setUpdateTime(DateUtil.getSqlCurrentDate());
		cashTool = cashToolService.save(cashTool);
		return new ResponseEntity<BaseResp>(new BaseResp(), HttpStatus.OK);
	}

	@RequestMapping(name = "现金管理类工具提交审核", value = "examine", method = { RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody ResponseEntity<BaseResp> examine(@RequestParam(required = true) String oid) {
		String operator = super.getLoginUser();
		CashTool entity = cashToolService.findByOid(oid);
		if (!CashTool.CASHTOOL_STATE_waitPretrial.equals(entity.getState())
				&& !CashTool.CASHTOOL_STATE_reject.equals(entity.getState())) {
			// 标的状态不是待预审或驳回不能提交预审
			throw new RuntimeException();
		}
		entity.setState(Investment.INVESTMENT_STATUS_pretrial);
		entity.setOperator(operator);
		entity.setUpdateTime(DateUtil.getSqlCurrentDate());
		cashToolService.save(entity);
		return new ResponseEntity<BaseResp>(new BaseResp(), HttpStatus.OK);
	}

	@RequestMapping(name = "编辑现金管理工具", value = "edit", method = { RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody ResponseEntity<BaseResp> edit(@Valid CashToolManageForm form) {
		String operator = super.getLoginUser();
		CashTool entity = cashToolService.findByOid(form.getOid());
		if (!CashTool.CASHTOOL_STATE_waitPretrial.equals(entity.getState())
				&& !CashTool.CASHTOOL_STATE_reject.equals(entity.getState())) {
			throw new RuntimeException();
		}
		CashTool temp = cashToolService.createInvestment(form);
		temp.setState(entity.getState());
		temp.setCreator(entity.getCreator());
		temp.setCreateTime(entity.getCreateTime());
		temp.setUpdateTime(DateUtil.getSqlCurrentDate());
		temp.setOperator(operator);
		cashToolService.save(temp);
		return new ResponseEntity<BaseResp>(new BaseResp(), HttpStatus.OK);
	}

	/**
	 * 作废
	 * 
	 * @param investment
	 * @return
	 */
	@RequestMapping(name = "现金管理类工具作废", value = "invalid", method = { RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody ResponseEntity<BaseResp> invalid(@RequestParam(required = true) String oid) {
		String operator = super.getLoginUser();
		CashTool entity = cashToolService.findByOid(oid);
		entity.setState(CashTool.CASHTOOL_STATE_invalid);
		entity.setUpdateTime(DateUtil.getSqlCurrentDate());
		entity.setOperator(operator);
		cashToolService.save(entity);
		return new ResponseEntity<BaseResp>(new BaseResp(), HttpStatus.OK);
	}

	@RequestMapping(name = "现金管理类工具审核列表", value = "accessList", method = { RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody ResponseEntity<CashToolListResp> accessList(HttpServletRequest request,
			@RequestParam(required = false, defaultValue = "1") int page,
			@RequestParam(required = false, defaultValue = "10") int rows,
			@RequestParam(required = false, defaultValue = "updateTime") String sortField,
			@RequestParam(required = false, defaultValue = "desc") String sort) {
		Direction sortDirection = Direction.DESC;
		if (!"desc".equals(sort)) {
			sortDirection = Direction.ASC;
		}
		Specification<CashTool> spec = new Specification<CashTool>() {
			@Override
			public Predicate toPredicate(Root<CashTool> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.equal(root.get("state").as(String.class), CashTool.CASHTOOL_STATE_pretrial);
			}
		};
		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(sortDirection, sortField)));
		Page<CashTool> entitys = cashToolService.getCashToolList(spec, pageable);
		CashToolListResp resps = new CashToolListResp(entitys);
		return new ResponseEntity<CashToolListResp>(resps, HttpStatus.OK);
	}

	/**
	 * 预审通过
	 * 
	 * @param oid
	 * @return
	 */
	@RequestMapping(name = "现金管理类工具审核通过", value = "checkpass", method = { RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody ResponseEntity<BaseResp> checkPass(@RequestParam(required = true) String oid) {
		String operator = super.getLoginUser();
		cashToolService.check(oid, CashTool.CASHTOOL_STATE_collecting, operator);
		return new ResponseEntity<BaseResp>(new BaseResp(), HttpStatus.OK);
	}

	/**
	 * 预审驳回
	 * 
	 * @param oid
	 * @return
	 */
	@RequestMapping(name = "现金管理类工具审核驳回", value = "checkreject", method = { RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody ResponseEntity<BaseResp> checkReject(@RequestParam(required = true) String oid, String suggest) {
		String operator = super.getLoginUser();
		cashToolService.check(oid, CashTool.CASHTOOL_STATE_reject, operator);
		return new ResponseEntity<BaseResp>(new BaseResp(), HttpStatus.OK);
	}

}
