package com.guohuai.mmp.publisher.investor.holdincome;

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

import net.kaczmarzyk.spring.data.jpa.domain.DateAfterInclusive;
import net.kaczmarzyk.spring.data.jpa.domain.DateBeforeInclusive;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.In;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
@RestController
@RequestMapping(value = "/mimosa/boot/investor/income", produces = "application/json")
public class InvestorIncomeBootController extends BaseController {

	@Autowired
	private InvestorIncomeService serviceInvestorIncome;
	
	@RequestMapping(value = "query", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<PagesRep<InvestorIncomeQueryRep>> superMng(HttpServletRequest request,
			@And({@Spec(params = "investorOid", path = "investorBaseAccount.oid", spec = Equal.class),
				  @Spec(params = "phoneNum", path = "investorBaseAccount.phoneNum", spec = Like.class),
				  @Spec(params = "productOid", path = "product.oid", spec = In.class),
				  @Spec(params = "confirmDateBegin", path = "confirmDate", spec = DateAfterInclusive.class),
				  @Spec(params = "confirmDateEnd", path = "confirmDate", spec = DateBeforeInclusive.class)}) 
				Specification<InvestorIncomeEntity> spec,
			@RequestParam int page, 
			@RequestParam int rows) {		

		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(Direction.DESC, "createTime")));
		PagesRep<InvestorIncomeQueryRep> rep = this.serviceInvestorIncome.investorIncomeQuery(spec, pageable);
		return new ResponseEntity<PagesRep<InvestorIncomeQueryRep>>(rep, HttpStatus.OK);
	}
	
}
