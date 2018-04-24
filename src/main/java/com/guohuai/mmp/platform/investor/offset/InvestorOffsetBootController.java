package com.guohuai.mmp.platform.investor.offset;

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
import net.kaczmarzyk.spring.data.jpa.domain.In;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;


@RestController
@RequestMapping(value = "/mimosa/boot/investoroffset", produces = "application/json")
public class InvestorOffsetBootController extends BaseController {

	@Autowired
	private InvestorOffsetService  investorOffsetService;
	
	@RequestMapping(value = "imng", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<PagesRep<InvestorOffsetQueryRep>> mng(HttpServletRequest request,
			@And({@Spec(params = "offsetCode", path = "offsetCode", spec = Equal.class),
				@Spec(params = "closeStatus", path = "closeStatus", spec = In.class),
				@Spec(params = "clearStatus", path = "clearStatus", spec = In.class),
				@Spec(params = "offsetFrequency", path = "offsetFrequency", spec = Equal.class),
				@Spec(params = "offsetDateBegin", path = "offsetDate", spec = DateAfterInclusive.class),
				@Spec(params = "offsetDateEnd", path = "offsetDate", spec = DateBeforeInclusive.class)}) Specification<InvestorOffsetEntity> spec,
			@RequestParam int page, 
			@RequestParam int rows,
			@RequestParam(required = false, defaultValue = "createTime") String sort,
			@RequestParam(required = false, defaultValue = "desc") String order) {
		Direction sortDirection = Direction.DESC;
		if (!"desc".equals(order)) {
			sortDirection = Direction.ASC;
		}
		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(sortDirection, sort)));
		PagesRep<InvestorOffsetQueryRep> rep = this.investorOffsetService.mng(spec, pageable);
		return new ResponseEntity<PagesRep<InvestorOffsetQueryRep>>(rep, HttpStatus.OK);
	}
	
	@RequestMapping(value = "deta", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<InvestorOffsetDetailRep> detail(@RequestParam(required = true) String offsetOid){
		InvestorOffsetDetailRep detailRep = this.investorOffsetService.detail(offsetOid);
		return new ResponseEntity<InvestorOffsetDetailRep>(detailRep, HttpStatus.OK);
	}
	
	@RequestMapping(value = "iclear", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<BaseRep> iclear(@RequestParam String iOffsetOid) {
		
		investorOffsetService.clear(this.investorOffsetService.findByOid(iOffsetOid));
		return new ResponseEntity<BaseRep>(new BaseRep(),HttpStatus.OK);
	}
	
	@RequestMapping(value = "iclose", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<BaseRep> iclose(@RequestParam String iOffsetOid) {
		this.investorOffsetService.close(this.investorOffsetService.findByOid(iOffsetOid));
		return new ResponseEntity<BaseRep>(new BaseRep(),HttpStatus.OK);
	}
	
	@RequestMapping(value = "fclose", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<BaseRep> fclose(@RequestParam String iOffsetOid) {
		this.investorOffsetService.closeFastOffset(this.investorOffsetService.findByOid(iOffsetOid));
		return new ResponseEntity<BaseRep>(new BaseRep(),HttpStatus.OK);
	}
	
	
	
	
	@RequestMapping(value = "foffset", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<BaseRep> foffset() {
		this.investorOffsetService.fastOffset();
		return new ResponseEntity<BaseRep>(new BaseRep(),HttpStatus.OK);
	}
	
	
	
}
