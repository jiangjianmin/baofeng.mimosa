package com.guohuai.ams.investment.check;

import java.math.BigDecimal;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
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

import com.guohuai.ams.enums.TargetEventType;
import com.guohuai.ams.investment.Investment;
import com.guohuai.ams.investment.InvestmentListResp;
import com.guohuai.ams.investment.InvestmentService;
import com.guohuai.ams.investment.log.InvestmentLogService;
import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.web.view.BaseResp;

/**
 * 投资标的预审管理
 * 
 * @author lirong
 *
 */
@RestController
@RequestMapping(value = "/mimosa/target/targetCheck", produces = "application/json;charset=UTF-8")
public class InvestmentCheckBootController extends BaseController {

	@Autowired
	private InvestmentService investmentService;
	@Autowired
	private InvestmentLogService investmentLogService;

	/**
	 * 投资标的预审列表
	 * 
	 * @param request
	 * @param page
	 * @param rows
	 * @param sortField
	 * @param sort
	 * @return
	 */
	@RequestMapping(name = "投资标的预审列表", value = "list", method = { RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody ResponseEntity<InvestmentListResp> list(HttpServletRequest request,
			@RequestParam(required = false, defaultValue = "1") int page,
			@RequestParam(required = false, defaultValue = "10") int rows,
			@RequestParam(required = false, defaultValue = "updateTime") String sortField,
			@RequestParam(required = false, defaultValue = "desc") String sort) {
		Direction sortDirection = Direction.DESC;
		if (!"desc".equals(sort)) {
			sortDirection = Direction.ASC;
		}
		Specification<Investment> spec = new Specification<Investment>() {
			@Override
			public Predicate toPredicate(Root<Investment> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.equal(root.get("state").as(String.class), Investment.INVESTMENT_STATUS_pretrial);
			}
		};
		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(sortDirection, sortField)));
		Page<Investment> entitys = investmentService.getInvestmentList(spec, pageable);
		InvestmentListResp resps = new InvestmentListResp(entitys);
		return new ResponseEntity<InvestmentListResp>(resps, HttpStatus.OK);
	}

	/**
	 * 预审通过
	 * 
	 * @param oid
	 * @return
	 */
	@RequestMapping(name = "投资标的预审通过", value = "checkpass", method = { RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody ResponseEntity<BaseResp> checkPass(@RequestParam(required = true) String oid, @RequestParam(required = true) BigDecimal trustAmount) {
		String operator = super.getLoginUser();
//		Investment investment = investmentService.precheck(oid, Investment.INVESTMENT_STATUS_waitMeeting, operator, trustAmount,
//				null);
		Investment investment = investmentService.precheck(oid, Investment.INVESTMENT_STATUS_metted, operator, trustAmount,
				null);  // 公共版本 跳过会签审查,过会环节
		investmentLogService.saveInvestmentLog(investment, TargetEventType.checkpass, operator);
		return new ResponseEntity<BaseResp>(new BaseResp(), HttpStatus.OK);
	}

	/**
	 * 预审驳回
	 * 
	 * @param oid
	 * @return
	 */
	@RequestMapping(name = "投资标的预审驳回", value = "checkreject", method = { RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody ResponseEntity<BaseResp> checkReject(@RequestParam(required = true) String oid,@RequestParam(required = false, defaultValue = "0") BigDecimal trustAmount,
			@RequestParam String suggest) {
		String operator = super.getLoginUser();
		investmentService.precheck(oid, Investment.INVESTMENT_STATUS_reject, operator, trustAmount, suggest);
		investmentLogService.saveInvestmentLog(oid, TargetEventType.checkreject, operator);
		return new ResponseEntity<BaseResp>(new BaseResp(), HttpStatus.OK);
	}

}
