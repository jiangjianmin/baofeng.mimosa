package com.guohuai.mmp.platform.finance.check;

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
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.component.web.view.PagesRep;

import net.kaczmarzyk.spring.data.jpa.domain.DateAfterInclusive;
import net.kaczmarzyk.spring.data.jpa.domain.DateBeforeInclusive;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;

@RestController
@RequestMapping(value = "/mimosa/platform/finance/check", produces = "application/json")
public class PlatformFinanceCheckBootController extends BaseController{
	@Autowired
	PlatformFinanceCheckService platformFinanceCheckService;
	
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
	@RequestMapping(value = "checkDataList", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<PagesRep<PlatformFinanceCheckRep>> checkDataList(HttpServletRequest request,
			@And({ @Spec(params = "checkCode", path = "checkCode", spec = Like.class),
					@Spec(params = "status", path = "checkStatus", spec = Equal.class),
					@Spec(params = "begin", path = "checkDate", spec = DateAfterInclusive.class),
					@Spec(params = "end", path = "checkDate", spec = DateBeforeInclusive.class) }) Specification<PlatformFinanceCheckEntity> spec,
			@RequestParam int page, @RequestParam int rows,
			@RequestParam(required = false, defaultValue = "createTime") String sort,
			@RequestParam(required = false, defaultValue = "desc") String order) {
		Direction sortDirection = Direction.DESC;
		if (!"desc".equals(order)) {
			sortDirection = Direction.ASC;
		}
		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(sortDirection, sort)));
		PagesRep<PlatformFinanceCheckRep> rep = this.platformFinanceCheckService.checkDataList(spec, pageable);

		return new ResponseEntity<PagesRep<PlatformFinanceCheckRep>>(rep, HttpStatus.OK);
	}

	@RequestMapping(value = "checkOrder", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseRep> checkOrder(@RequestParam String checkOid, @RequestParam String checkDate) {
//		String operator=this.getLoginUser();
		String operator=this.getLoginUserName();
		BaseRep rep = this.platformFinanceCheckService.checkOrder(checkOid, checkDate, operator);
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	@RequestMapping(value = "checkDataConfirm", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseRep> checkDataConfirm(@RequestParam String oid) {
		String operator=this.getLoginUser();
		BaseRep rep = this.platformFinanceCheckService.checkDataConfirm(oid,operator);
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}

}
