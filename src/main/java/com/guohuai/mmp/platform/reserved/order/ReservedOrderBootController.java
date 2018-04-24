package com.guohuai.mmp.platform.reserved.order;

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
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;


@RestController
@RequestMapping(value = "/mimosa/boot/reservedorder", produces = "application/json")
public class ReservedOrderBootController extends BaseController {

	@Autowired
	ReservedOrderService  reservedOrderService;
	
	
	@RequestMapping(value = "mng", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<PagesRep<ReservedOrderQueryRep>> mng(HttpServletRequest request,
			@And({@Spec(params = "orderType", path = "orderType", spec = In.class),
				@Spec(params = "orderCode", path = "orderCode", spec = Equal.class),
				@Spec(params = "createTimeBegin", path = "createTime", spec = DateAfterInclusive.class),
				@Spec(params = "createTimeEnd", path = "createTime", spec = DateBeforeInclusive.class)}) Specification<ReservedOrderEntity> spec,
			@RequestParam int page, 
			@RequestParam int rows,
			@RequestParam(required = false, defaultValue = "createTime") String sort,
			@RequestParam(required = false, defaultValue = "desc") String order) {
		Direction sortDirection = Direction.DESC;
		if (!"desc".equals(order)) {
			sortDirection = Direction.ASC;
		}
		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(sortDirection, sort)));
		PagesRep<ReservedOrderQueryRep> rep = this.reservedOrderService.mng(spec, pageable);
		return new ResponseEntity<PagesRep<ReservedOrderQueryRep>>(rep, HttpStatus.OK);
	}
	
	

	
}
