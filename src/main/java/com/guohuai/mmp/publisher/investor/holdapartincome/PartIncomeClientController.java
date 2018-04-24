package com.guohuai.mmp.publisher.investor.holdapartincome;

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
import com.guohuai.component.web.view.PagesRep;

import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;

@RestController
@RequestMapping(value = "/mimosa/client/investor/holdapartincome", produces = "application/json")
public class PartIncomeClientController extends BaseController {

	@Autowired
	PartIncomeService partIncomeService;

	/** 我的活期奖励收益详情页 */
	@RequestMapping(value = "rewardinfo", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<PagesRep<MyLevelHoldIncomeRep>> mxxxng(@RequestParam final String productOid,
			@RequestParam final String level, @RequestParam int page, @RequestParam int rows) {

		final String uid = this.getLoginUser();

		page = page < 1 ? 1 : page;
		rows = rows < 1 ? 1 : rows;

		int startLine = (page - 1) * rows;// 开始行

		startLine = startLine < 0 ? 0 : startLine;

		PagesRep<MyLevelHoldIncomeRep> pages = this.partIncomeService.queryHoldApartIncomeAndLevel(uid, productOid,
				level, startLine, rows);

		return new ResponseEntity<PagesRep<MyLevelHoldIncomeRep>>(pages, HttpStatus.OK);
	}
	
	@RequestMapping(value = "rewardinfoxxxx", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<PagesRep<HoldApartIncomeQueryRep>> mng(HttpServletRequest request,
			@And({@Spec(params = "incomeOid", path = "incomeAllocate.oid", spec = Equal.class)}) Specification<PartIncomeEntity> spec,
			@RequestParam int page, 
			@RequestParam int rows,
			@RequestParam(required = false, defaultValue = "createTime") String sort,
			@RequestParam(required = false, defaultValue = "desc") String order) {
		Direction sortDirection = Direction.DESC;
		if (!"desc".equals(order)) {
			sortDirection = Direction.ASC;
		}
		
		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(sortDirection, sort)));
		PagesRep<HoldApartIncomeQueryRep> rep = this.partIncomeService.mng(spec, pageable);
		return new ResponseEntity<PagesRep<HoldApartIncomeQueryRep>>(rep, HttpStatus.OK);
	}
	
}
