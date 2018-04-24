package com.guohuai.ams.investment.pool;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;

/**
 * 投资标的库本兮兑付控制器
 * <p>
 * Title: TargetIncomeController.java
 * </p>
 * <p>
 * Description: 描述
 * </p>
 * 
 * @author vania
 * @version 1.0
 * @created 2016年5月17日 上午9:24:14
 */
@RestController
@RequestMapping("/mimosa/boot/targetIncome")
//@Api("投资标的库本兮兑付操作相关接口")
public class TargetIncomeController extends BaseController {
	@Autowired
	InvestmentPoolService investmentPoolService;
	@Autowired
	TargetIncomeService targetIncomeService;

	/**
	 * 投资标的本息兑付列表
	 * 
	 * @Title: investmentTargetIncomeList
	 * @author vania
	 * @version 1.0
	 * @see:
	 * @param request
	 * @param spec
	 * @param page
	 * @param size
	 * @param sortDirection
	 * @param sortField
	 * @return ResponseEntity<TargetIncomeListResp> 返回类型
	 */
	@RequestMapping(value = { "investmentTargetIncomeList" }, method = { RequestMethod.POST, RequestMethod.GET })
//	@ApiOperation(value = "投资标的本息兑付列表")
	public @ResponseBody ResponseEntity<TargetIncomeListResp> investmentTargetIncomeList(HttpServletRequest request,
			@And({ @Spec(params = "targetOid", path = "investment.oid", spec = Equal.class), @Spec(params = "seq", path = "seq", spec = Equal.class) }) Specification<TargetIncome> spec,
			@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "50") int rows, @RequestParam(defaultValue = "desc") String sortDirection,
			@RequestParam(defaultValue = "seq") String sortField) {

		if (page < 1) {
			page = 1;
		}
		if (rows <= 0) {
			rows = 50;
		}
		Order order = new Order(Direction.valueOf(sortDirection.toUpperCase()), sortField);
		Pageable pageable = new PageRequest(page - 1, rows, new Sort(order));

		Page<TargetIncome> pageData = targetIncomeService.getTargetIncomeList(spec, pageable);

		TargetIncomeListResp resp = new TargetIncomeListResp(pageData);
		return new ResponseEntity<TargetIncomeListResp>(resp, HttpStatus.OK);
	}

}
