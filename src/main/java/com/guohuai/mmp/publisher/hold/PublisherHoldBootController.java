package com.guohuai.mmp.publisher.hold;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;

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

import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.web.view.PagesRep;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountEntity;

import net.kaczmarzyk.spring.data.jpa.domain.DateAfterInclusive;
import net.kaczmarzyk.spring.data.jpa.domain.DateBeforeInclusive;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.In;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;


@RestController
@RequestMapping(value = "/mimosa/boot/holdconfirm", produces = "application/json")
public class PublisherHoldBootController extends BaseController {

	@Autowired
	PublisherHoldService  publisherHoldService;
	
	
	
	@RequestMapping(value = "superMng", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<PagesRep<HoldQueryRep>> superMng(HttpServletRequest request,
			@And({@Spec(params = "holdStatus", path = "holdStatus", spec = In.class),
				@Spec(params = "createTimeBegin", path = "createTime", spec = DateAfterInclusive.class),
				@Spec(params = "createTimeEnd", path = "createTime", spec = DateBeforeInclusive.class)}) Specification<PublisherHoldEntity> spec,
			@RequestParam int page, 
			@RequestParam int rows,
			@RequestParam(required = false, defaultValue = "createTime") String sort,
			@RequestParam(required = false, defaultValue = "desc") String order) {
		Direction sortDirection = Direction.DESC;
		if (!"desc".equals(order)) {
			sortDirection = Direction.ASC;
		}
		
		Specification<PublisherHoldEntity> ownerSpec = new Specification<PublisherHoldEntity>() {
			@Override
			public Predicate toPredicate(Root<PublisherHoldEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

				return cb.equal(root.get("investorBaseAccount").get("owner").as(String.class),
						InvestorBaseAccountEntity.BASEACCOUNT_owner_platform);
			}

		};
		spec = Specifications.where(spec).and(ownerSpec);
		
		
		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(sortDirection, sort)));
		PagesRep<HoldQueryRep> rep = this.publisherHoldService.holdMng(spec, pageable);
		return new ResponseEntity<PagesRep<HoldQueryRep>>(rep, HttpStatus.OK);
	}
	
	
	@RequestMapping(value = "pmng", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<PagesRep<HoldQueryRep>> mng(HttpServletRequest request,
			@And({@Spec(params = "holdStatus", path = "holdStatus", spec = Equal.class),
				@Spec(params = "productCode", path = "product.code", spec = Equal.class),
				@Spec(params = "productName", path = "product.name", spec = Equal.class),
				@Spec(params = "productType", path = "product.type.oid", spec = In.class),
				  @Spec(params = "investorBaseAccountOid", path = "investorBaseAccount.oid", spec = Equal.class)}) Specification<PublisherHoldEntity> spec,
			@RequestParam int page, 
			@RequestParam int rows,
			@RequestParam(required = false, defaultValue = "createTime") String sort,
			@RequestParam(required = false, defaultValue = "desc") String order) {
		Direction sortDirection = Direction.DESC;
		if (!"desc".equals(order)) {
			sortDirection = Direction.ASC;
		}
		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(sortDirection, sort)));
		PagesRep<HoldQueryRep> rep = this.publisherHoldService.holdMng(spec, pageable);
		return new ResponseEntity<PagesRep<HoldQueryRep>>(rep, HttpStatus.OK);
	}
	
	
	@RequestMapping(value = "deta", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<HoldDetailRep> close(@RequestParam(required = true) String holdOid){
		HoldDetailRep detailRep = this.publisherHoldService.detail(holdOid);
		return new ResponseEntity<HoldDetailRep>(detailRep, HttpStatus.OK);
	}
	
	/**
	 * 根据资产池和资产池对应的hold
	 * @param assetPoolOid
	 * @return
	 */
	@RequestMapping(value = "getHoldByAssetPoolOid", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<HoldDetailRep> getHoldByAssetPoolOid(@RequestParam(required = true) String assetPoolOid, @RequestParam(required = false) String productOid){
		HoldDetailRep detailRep = this.publisherHoldService.getHoldByAssetPoolOid(assetPoolOid, productOid);
		return new ResponseEntity<HoldDetailRep>(detailRep, HttpStatus.OK);
	}
	
}
