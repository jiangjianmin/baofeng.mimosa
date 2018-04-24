package com.guohuai.ams.system.config.risk.warning.collect;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
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

import com.guohuai.ams.investment.Investment;
import com.guohuai.component.web.view.BaseResp;

import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;

/**
 * 风险预警
 * 
 * @author lirong
 *
 */
@RestController
@RequestMapping(value = "/mimosa/system/ccr/warning/collect", produces = "application/json;charset=utf-8")
public class RiskWarningCollectController {

	@Autowired
	RiskWarningCollectService riskWarningCollectService;

	/**
	 * 风控预警列表
	 * 
	 * @param request
	 * @param spec
	 * @param page
	 * @param rows
	 * @param sortDirection
	 * @param sortField
	 * @return
	 */
	@RequestMapping(value = "list", method = { RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody ResponseEntity<RiskWarningCollectListResp> list(HttpServletRequest request,
			@And({ @Spec(params = "sn", path = "sn", spec = Like.class),
					@Spec(params = "name", path = "name", spec = Like.class),
					@Spec(params = "state", path = "lifeState", spec = Equal.class) }) Specification<Investment> spec,
			@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "50") int rows,
			@RequestParam(defaultValue = "desc") String sortDirection,
			@RequestParam(defaultValue = "updateTime") String sortField) {
		if (page < 1) {
			page = 1;
		}
		if (rows <= 0) {
			rows = 50;
		}
		Order order = new Order(Direction.valueOf(sortDirection.toUpperCase()), sortField);
		Pageable pageable = new PageRequest(page - 1, rows, new Sort(order));

		spec = Specifications.where(spec).and(new Specification<Investment>() {
			@Override
			public Predicate toPredicate(Root<Investment> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> predicate = new ArrayList<>();
				Expression<String> exp = root.get("state").as(String.class);
				predicate.add(exp.in(new Object[] { Investment.INVESTMENT_STATUS_collecting,
						Investment.INVESTMENT_STATUS_meetingpass }));
				Expression<String> exp_lifeState = root.get("lifeState").as(String.class);
				predicate.add(exp_lifeState.in(new Object[] { Investment.INVESTMENT_LIFESTATUS_PREPARE,
						Investment.INVESTMENT_LIFESTATUS_STAND_UP, Investment.INVESTMENT_LIFESTATUS_OVER_TIME }));
				Predicate[] pre = new Predicate[predicate.size()];
				return query.where(predicate.toArray(pre)).getRestriction();
			}
		});
		return new ResponseEntity<RiskWarningCollectListResp>(riskWarningCollectService.list(spec, pageable), HttpStatus.OK);
	}

	/**
	 * 风控预警详情
	 * 
	 * @param oid
	 * @return
	 */
	@RequestMapping(value = "detail", method = { RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody ResponseEntity<List<RiskWarningCollectDetResp>> detail(
			@RequestParam(required = true) String oid) {
		List<RiskWarningCollectDetResp> res = riskWarningCollectService.detail(oid);
		return new ResponseEntity<List<RiskWarningCollectDetResp>>(res, HttpStatus.OK);
	}

	/**
	 * 风控数据采集配置项
	 * 
	 * @return
	 */
	@RequestMapping(value = "collectOption", method = { RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody ResponseEntity<RiskWaringCollectOptionResp> collectList() {
		RiskWaringCollectOptionResp res = riskWarningCollectService.collectOption();
		return new ResponseEntity<RiskWaringCollectOptionResp>(res, HttpStatus.OK);
	}

	/**
	 * 风控数据采集
	 * 
	 * @param form
	 * @return
	 */
	@RequestMapping(value = "add", method = { RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody ResponseEntity<BaseResp> collect(@Valid RiskWarningCollectForm form) {
		riskWarningCollectService.collect(form);
		return new ResponseEntity<BaseResp>(new BaseResp(), HttpStatus.OK);
	}
}
