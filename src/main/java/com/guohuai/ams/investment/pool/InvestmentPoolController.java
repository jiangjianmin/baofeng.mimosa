package com.guohuai.ams.investment.pool;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
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
import com.guohuai.ams.investment.InvestmentListResp;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.Section;
import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.web.view.BaseResp;
import com.guohuai.component.web.view.Response;

//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;

/**
 * 投资标的库控制器
 * <p>
 * Title: InvestmentPoolController.java
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
@RequestMapping(value = "/mimosa/boot/investmentPool", produces = "application/json;charset=UTF-8")
//@Api("投资标的库操作相关接口")
@Slf4j
public class InvestmentPoolController extends BaseController {
	@Autowired
	InvestmentPoolService investmentPoolService;
	@Autowired
	TargetIncomeService targetIncomeService;

	/**
	 * 投资标的库管理列表
	 * 
	 * @Title: investmentPoolList
	 * @author vania
	 * @version 1.0
	 * @see:
	 * @param request
	 * @param spec
	 * @param page
	 * @param size
	 * @param sortDirection
	 * @param sortField
	 * @return ResponseEntity<InvestmentListResp> 返回类型
	 */
	@RequestMapping(value = { "list"}, method = { RequestMethod.POST, RequestMethod.GET })
//	@ApiOperation(value = "标的库列表")
	public @ResponseBody ResponseEntity<InvestmentListResp> list(HttpServletRequest request,
			@RequestParam() final String op,
			@And({	
				@Spec(params = "name", path = "name", spec = Like.class), 
				@Spec(params = "type", path = "type", spec = Equal.class) 
			}) Specification<Investment> spec,
			@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "50") int rows, @RequestParam(defaultValue = "desc") String sortDirection,
			@RequestParam(defaultValue = "updateTime") String sortField) {
		log.info("标的库查询op=" + op);

		if (page < 1) {
			page = 1;
		}
		if (rows <= 0) {
			rows = 50;
		}
		Order order = new Order(Direction.valueOf(sortDirection.toUpperCase()), sortField);
		Pageable pageable = new PageRequest(page - 1, rows, new Sort(order));
		
		// 根据不同的操作拼接条件
		spec = Specifications.where(spec).and(new Specification<Investment>() {
			@Override
			public Predicate toPredicate(Root<Investment> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> predicate = new ArrayList<>();
				if (op.equals("storageList")) { // 投资标的备选库
					Expression<String> exp_state = root.get("state").as(String.class);					
					predicate.add(exp_state.in(new Object[] { Investment.INVESTMENT_STATUS_collecting, 
							Investment.INVESTMENT_STATUS_meetingpass,
							Investment.INVESTMENT_STATUS_NORMAL_INCOME,
							Investment.INVESTMENT_STATUS_OVERDUE_INCOME}));
					Expression<String> exp_lifeState = root.get("lifeState").as(String.class);					
					predicate.add(exp_lifeState.in(new Object[] { Investment.INVESTMENT_LIFESTATUS_PREPARE, 
							Investment.INVESTMENT_LIFESTATUS_STAND_UP, 
							Investment.INVESTMENT_LIFESTATUS_OVER_TIME, 
							Investment.INVESTMENT_LIFESTATUS_PAY_BACK,
							Investment.INVESTMENT_LIFESTATUS_RETURN_BACK }));
				} else if (op.equals("holdList")) { // 已持有列表
					Expression<String> exp_state = root.get("state").as(String.class);					
					predicate.add(cb.not(exp_state.in(new Object[] { Investment.INVESTMENT_STATUS_invalid })));
					
					Expression<BigDecimal> expHa = root.get("holdAmount").as(BigDecimal.class);
					Predicate p = cb.gt(expHa, new BigDecimal(0)); //持有金额大于0: holdAmount > 0 		
					predicate.add(p);					
				} else if (op.equals("notHoldList")) { // 未持有列表
					Expression<String> exp_state = root.get("state").as(String.class);					
					predicate.add(cb.not(exp_state.in(new Object[] { Investment.INVESTMENT_STATUS_invalid,
							Investment.INVESTMENT_STATUS_waitPretrial,
							Investment.INVESTMENT_STATUS_pretrial,
							Investment.INVESTMENT_STATUS_waitMeeting,
							Investment.INVESTMENT_STATUS_metting,
							Investment.INVESTMENT_STATUS_metted})));
					
					Expression<BigDecimal> exp = root.get("holdAmount").as(BigDecimal.class);
					Predicate p = cb.or(cb.isNull(exp), cb.le(exp, new BigDecimal(0))); //持有金额为空或者大于0: holdAmount is null or holdAmount < 0
					predicate.add(p); 
				} else if (op.equals("historyList")) { // 历史列表
					Expression<String> exp = root.get("state").as(String.class);	
					Predicate pst = exp.in(new Object[] { Investment.INVESTMENT_STATUS_invalid });
					
					Expression<String> exp_lifeState = root.get("lifeState").as(String.class);	
					Predicate plst = exp_lifeState.in(new Object[] { Investment.INVESTMENT_LIFESTATUS_STAND_FAIL, Investment.INVESTMENT_LIFESTATUS_CLOSE });// 未成立,已结束
					
					Predicate p = cb.or(pst, plst);
					predicate.add(p);
				} else{
					throw AMPException.getException("未知的操作类型[" + op + "]"); 
				}
				Predicate[] pre = new Predicate[predicate.size()];
				return query.where(predicate.toArray(pre)).getRestriction();
			}
		});
		
		String raiseScope = request.getParameter("raiseScope");
		spec = this.buildSpec(spec, "raiseScope", raiseScope);		
		
		String holdAmount = request.getParameter("holdAmount");
		spec = this.buildSpec(spec, "holdAmount", holdAmount);		
		
		String applyAmount = request.getParameter("applyAmount");
		spec = this.buildSpec(spec, "applyAmount", applyAmount);		

		String lifed = request.getParameter("lifed");
		spec = this.buildSpec(spec, "lifed", lifed);		

		String expAror = request.getParameter("expAror");
		spec = this.buildSpec(spec, "expAror", expAror);
		
		Page<Investment> pageData = investmentPoolService.getInvestmentList(spec, pageable);

		InvestmentListResp resp = new InvestmentListResp(pageData);
		return new ResponseEntity<InvestmentListResp>(resp, HttpStatus.OK);
	}

	/**
	 * 构建范围查询条件
	 * @Title: buildSpec 
	 * @author vania
	 * @version 1.0
	 * @see: 
	 * @param spec
	 * @param attr
	 * @param value
	 * @return Specification<Investment>    返回类型
	 */
	private Specification<Investment> buildSpec(Specification<Investment> spec, final String attr, final String value) {
		if (StringUtils.isNotBlank(value)) {
			spec = Specifications.where(spec).and(new Specification<Investment>() {
				@Override
				public Predicate toPredicate(Root<Investment> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
					Section<Investment> section = new Section<Investment>(value);
					return section.build(root, cb, attr);
				}
			});
		}
		return spec;
	}	
	
	/**
	 * 标的成立
	 * @Title: establish 
	 * @author vania
	 * @version 1.0
	 * @see: 
	 * @param form
	 * @return ResponseEntity<BaseResp>    返回类型
	 */
	@RequestMapping("establish")
//	@ApiOperation(value = "标的成立")
	public @ResponseBody ResponseEntity<BaseResp> establish(@Valid EstablishForm form) {
		log.debug("投资标的成立接口!!!");
		String loginId = null; 
		try {
			loginId = super.getLoginUser();
		} catch (Exception e) {
			
		}
		form.setOperator(loginId);
		this.investmentPoolService.establish(form);
		return new ResponseEntity<BaseResp>(new BaseResp(), HttpStatus.OK);
	}

	/**
	 * 标的不成立
	 * 
	 * @Title: unEstablish
	 * @author vania
	 * @version 1.0
	 * @see: @return CommonResp 返回类型 @throws
	 */
	@RequestMapping("unEstablish")
//	@ApiOperation(value = "标的不成立")
	public @ResponseBody ResponseEntity<BaseResp> unEstablish(@Valid UnEstablishForm form) {
		log.debug("投资标的成立接口!!!");
		String loginId = null;
		try {
			loginId = super.getLoginUser();
		} catch (Exception e) {

		}
		form.setOperator(loginId);
		this.investmentPoolService.unEstablish(form);
		return new ResponseEntity<BaseResp>(new BaseResp(), HttpStatus.OK);
	}

	/**
	 * 投资标的本息兑付
	 * 
	 * @Title: interestSave
	 * @author vania
	 * @version 1.0
	 * @see: 
	 * @param interest
	 * @return CommonResp 返回类型
	 */
	@RequestMapping("incomeSave")
//	@ApiOperation(value = "投资标的本息兑付")
	public @ResponseBody ResponseEntity<BaseResp> incomeSave(@Valid TargetIncomeForm form) {
		String loginId = null;
		try {
			loginId = super.getLoginUser();
		} catch (Exception e) {
			log.error("获取操作员失败, 原因: " + e.getMessage());
		}
		form.setOperator(loginId);
		investmentPoolService.targetIncome(form);
		return new ResponseEntity<BaseResp>(new BaseResp(), HttpStatus.OK);
	}
	/** -------------------- add by star.zhu target start --------------------- **/
	@RequestMapping("overdueN")
//	@ApiOperation(value = "标的逾期")
	public @ResponseBody ResponseEntity<Response> overdue(@Valid String oid) {
		this.investmentPoolService.overdue(oid, super.getLoginUser());
		Response r = new Response();
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	@RequestMapping("incomeSaveN")
//	@ApiOperation(value = "投资标的本息正常兑付")
	public @ResponseBody ResponseEntity<Response> incomeSaveN(@Valid String oid) {
		investmentPoolService.targetIncomeN(oid, super.getLoginUser());
		Response r = new Response();
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	@RequestMapping("incomeSaveD")
//	@ApiOperation(value = "投资标的本息逾期兑付")
	public @ResponseBody ResponseEntity<Response> incomeSaveD(@Valid String oid) {
		investmentPoolService.targetIncomeD(oid, super.getLoginUser());
		Response r = new Response();
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	@RequestMapping("targetCancel")
//	@ApiOperation(value = "投资标的坏账核销")
	public @ResponseBody ResponseEntity<Response> targetCancel(@Valid String oid) {
		investmentPoolService.targetCancel(oid, super.getLoginUser());
		Response r = new Response();
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	@RequestMapping("overdueTransfer")
//	@ApiOperation(value = "投资标的本息逾期转让")
	public @ResponseBody ResponseEntity<Response> overdueTransfer(@Valid String oid) {
		investmentPoolService.overdueTransfer(oid, super.getLoginUser());
		Response r = new Response();
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	/** -------------------- add by star.zhu for income end --------------------- **/

	/**
	 * 标的逾期
	 * 
	 * @Title: overdue
	 * @author vania
	 * @version 1.0
	 * @see: 
	 * @param days
	 * @param rate
	 * @param overdueFine
	 * @return ResponseEntity<BaseResp> 返回类型
	 */
	@RequestMapping("overdue")
//	@ApiOperation(value = "标的逾期")
	public @ResponseBody ResponseEntity<BaseResp> overdue(@Valid TargetOverdueForm form) {
		String loginId = null;
		try {
			loginId = super.getLoginUser();
		} catch (Exception e) {
			log.error("获取操作员失败, 原因: " + e.getMessage());
		}
		form.setOperator(loginId);
		this.investmentPoolService.overdue(form);
		return new ResponseEntity<BaseResp>(new BaseResp(), HttpStatus.OK);
	}
	
	/**
	 * 结束标的
	 * @Title: close 
	 * @author vania
	 * @version 1.0
	 * @see: 
	 * @param oid
	 * @return ResponseEntity<BaseResp>    返回类型
	 */
	@RequestMapping("close")
//	@ApiOperation(value = "标的逾期")
	public @ResponseBody ResponseEntity<BaseResp> close(@RequestParam() String oid) {
		String loginId = null;
		try {
			loginId = super.getLoginUser();
		} catch (Exception e) {
			log.error("获取操作员失败, 原因: " + e.getMessage());
		}
		this.investmentPoolService.close(oid, loginId);
		return new ResponseEntity<BaseResp>(new BaseResp(), HttpStatus.OK);
	}

	/**
	 * 查询正在募集期的标的列表
	 * @Title: getRecruitment 
	 * @author vania
	 * @version 1.0
	 * @see: 
	 * @return ResponseEntity<InvestmentListResp>    返回类型
	 */
	@RequestMapping("getRecruitment")
//	@ApiOperation(value = "查询正在募集期的标的列表")
	public @ResponseBody ResponseEntity<InvestmentListResp> getRecruitment() {	
		List<Investment> list = this.investmentPoolService.getCollecting(new Date(System.currentTimeMillis()));
		return new ResponseEntity<InvestmentListResp>(new InvestmentListResp(list), HttpStatus.OK);
	}

	/**
	 * 根据标的id查询所有的本息兑付数据
	 * @Title: getTargetIncome 
	 * @author vania
	 * @version 1.0
	 * @see: 
	 * @param targetOid
	 * @return ResponseEntity<TargetIncomeListResp>    返回类型
	 */
	@RequestMapping("getTargetIncome")
//	@ApiOperation(value = "根据标的id查询所有的本息兑付数据")
	public @ResponseBody ResponseEntity<TargetIncomeListResp> getTargetIncome(@RequestParam() String targetOid) {
		List<TargetIncome> list = investmentPoolService.getTargetIncome(targetOid);
		return new ResponseEntity<TargetIncomeListResp>(new TargetIncomeListResp(list), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/updateLifeStateSchedule", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> updateLifeStateSchedule() {
		investmentPoolService.updateLifeStateSchedule();;
		Response r = new Response();
		r.with("result", "SUCCESS");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	
}
