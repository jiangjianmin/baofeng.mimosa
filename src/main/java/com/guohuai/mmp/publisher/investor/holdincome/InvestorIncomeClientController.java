package com.guohuai.mmp.publisher.investor.holdincome;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.ParseException;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

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

import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.StringUtil;
import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.web.view.PagesRep;

@RestController
@RequestMapping(value = "/mimosa/client/investor/holdincome", produces = "application/json")
public class InvestorIncomeClientController extends BaseController {

	@Autowired
	private InvestorIncomeService serviceInvestorIncome;

	/** 
	 * 针对 产品的收益明细、累计收益 
	 */
	@RequestMapping(value = "qryincome2", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<MyInvestorIncomeRep> queryAloneHoldIncome(@RequestParam(required = true) final String productOid, @RequestParam int page,
			@RequestParam int rows) {

		final String uid = this.getLoginUser();

		page = page < 1 ? 1 : page;
		rows = rows < 1 ? 1 : rows;

		Specification<InvestorIncomeEntity> spec = new Specification<InvestorIncomeEntity>() {
			@Override
			public Predicate toPredicate(Root<InvestorIncomeEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.and(cb.equal(root.get("investorBaseAccount").get("userOid").as(String.class), uid), // 投资者ID
						cb.equal(root.get("product").get("oid").as(String.class), productOid) ,// 产品ID
						cb.notEqual(root.get("incomeAmount").as(BigDecimal.class),BigDecimal.ZERO)//过滤收益为0的记录
				);
			}
		};

		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(Direction.DESC, "confirmDate")));

		MyInvestorIncomeRep rep = this.serviceInvestorIncome.queryAloneHoldIncome(spec, pageable);

		return new ResponseEntity<MyInvestorIncomeRep>(rep, HttpStatus.OK);
	}

	/**
	 * 查询 用户收益 (可根据产品OID查询)
	 */
	@RequestMapping(value = "qryincome", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<PagesRep<InvestorIncomeRep>> queryInvestorIncome(
			@RequestParam(required = false) final String productOid, @RequestParam int page, @RequestParam int rows) {

		final String uid = this.getLoginUser();

		Specification<InvestorIncomeEntity> spec = new Specification<InvestorIncomeEntity>() {
			@Override
			public Predicate toPredicate(Root<InvestorIncomeEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				Predicate p1 = cb.equal(root.get("investorBaseAccount").get("userOid").as(String.class), uid);
				Predicate p3 = cb.notEqual(root.get("incomeAmount").as(BigDecimal.class),BigDecimal.ZERO);//过滤收益为0的记录
				if (!StringUtil.isEmpty(productOid)) {
					Predicate p2 = cb.equal(root.get("product").get("oid").as(String.class), productOid);
					return cb.and(p3,p2, p1);
					
				} else {
					return cb.and(p3,p1);
				}
			}
		};

		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(Direction.DESC, "confirmDate")));
		PagesRep<InvestorIncomeRep> pages = this.serviceInvestorIncome.queryInvestorIncome(spec, pageable);

		return new ResponseEntity<PagesRep<InvestorIncomeRep>>(pages, HttpStatus.OK);
	}

	/** 我的收益明细 */
	@RequestMapping(value = "mydetail", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<PagesRep<MyInvestorIncomeDetailsRep>> queryMyIncome(@RequestParam int page,
			@RequestParam int rows, @RequestParam int year, @RequestParam int month) {

		if (month < 1 || month > 12) {
			// error.define[10001]=非法的请求参数(CODE:10001)
			throw new AMPException(10001);
		}
		final Date sDate;
		final Date eDate;
		try {
			sDate = DateUtil.fetchSqlDate(DateUtil.getFirstDayZeroTimeOfMonth(year, month, "yyyy-MM-dd"));
			eDate = DateUtil.fetchSqlDate(DateUtil.getLastDayLastTimeOfMonth(year, month, "yyyy-MM-dd"));
		} catch (ParseException e) {
			// error.define[10001]=非法的请求参数(CODE:10001)
			throw new AMPException(10001);
		}
		final String uid = this.getLoginUser();

		page = page < 1 ? 1 : page;
		rows = rows < 1 ? 1 : rows;

		// 查询收益明细
		Specification<InvestorIncomeEntity> spec = new Specification<InvestorIncomeEntity>() {
			@Override
			public Predicate toPredicate(Root<InvestorIncomeEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.and(cb.equal(root.get("investorBaseAccount").get("userOid").as(String.class), uid), // 投资者ID
						cb.between(root.get("confirmDate").as(Date.class), sDate, eDate)// 收益确认日
				);
			}
		};
		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(Direction.DESC, "confirmDate")));
		PagesRep<MyInvestorIncomeDetailsRep> pages = this.serviceInvestorIncome.findMyIncomeByPages(spec, pageable);

		return new ResponseEntity<PagesRep<MyInvestorIncomeDetailsRep>>(pages, HttpStatus.OK);
	}

	/**
	 * 累计 收益 页面
	 */
	@RequestMapping(value = "mydatedetail", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<IncomeRep> queryMyTotalIncomeByDate(@RequestParam int year,
			@RequestParam int month) {

		if (month < 1 || month > 12) {
			// error.define[10001]=非法的请求参数(CODE:10001)
			throw new AMPException(10001);
		}
		String uid = this.getLoginUser();
		
		String yMonth = year + (month < 10 ? "0" + month : "" + month);
		IncomeRep rep = this.serviceInvestorIncome.queryMyTotalIncomeByDate(uid, yMonth);

		return new ResponseEntity<IncomeRep>(rep, HttpStatus.OK);
	}
	
	/**
	 * 累计 收益 页面
	 */
	@RequestMapping(value = "myjjgdetail", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<IncomeRep> queryMyJjgIncomeByPage(@RequestParam int pageNo,
			@RequestParam int pageSize) {
		if (pageNo < 1) {
			// error.define[10001]=非法的请求参数(CODE:10001)
			throw new AMPException(10001);
		}
		String uid = this.getLoginUser();
		
		IncomeRep rep = this.serviceInvestorIncome.queryMyJjgIncomeByDate(uid, pageNo, pageSize);

		return new ResponseEntity<IncomeRep>(rep, HttpStatus.OK);
	}
}
