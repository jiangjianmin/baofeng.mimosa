package com.guohuai.mmp.platform.inform;

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

import com.guohuai.component.util.DateUtil;
import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.web.view.PagesRep;

import net.kaczmarzyk.spring.data.jpa.domain.DateAfterInclusive;
import net.kaczmarzyk.spring.data.jpa.domain.DateBeforeInclusive;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.In;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;

@RestController
@RequestMapping(value = "/mmp/boot/platform/inform", produces = "application/json")
public class InformBootController extends BaseController {
	
	@Autowired
	InformService informService;
	
	
	@RequestMapping(value = "deta", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<InformDetailRep> detail(@RequestParam String informOid) {
		this.getLoginUser();
		InformDetailRep rep = this.informService.detail(informOid);
		return new ResponseEntity<InformDetailRep>(rep, HttpStatus.OK);
	}
	
	
	
	/**
	 * 查询
	 * @param request
	 * @param spec
	 * @param page
	 * @param rows
	 * @param sort
	 * @param order
	 * @return
	 */
	@RequestMapping(value = "query", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<PagesRep<InformQueryRep>> query(HttpServletRequest request,
			@And({@Spec(params = "informCode", path = "informCode", spec = Equal.class),
				@Spec(params = "informType", path = "informType", spec = In.class),
					@Spec(params = "createTimeBegin", path = "createTime", spec = DateAfterInclusive.class, config = DateUtil.fullDatePattern),
					@Spec(params = "createTimeEnd", path = "createTime", spec = DateBeforeInclusive.class, config = DateUtil.fullDatePattern)}) Specification<InformEntity> spec,
		@RequestParam int page, 
		@RequestParam int rows,
		@RequestParam(required = false, defaultValue = "createTime") String sort,
		@RequestParam(required = false, defaultValue = "desc") String order) {
		Direction sortDirection = Direction.DESC;
		if (!"desc".equals(order)) {
			sortDirection = Direction.ASC;
		}
		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(sortDirection, sort)));
		PagesRep<InformQueryRep> rep = this.informService.query(spec, pageable);
		
		return new ResponseEntity<PagesRep<InformQueryRep>>(rep, HttpStatus.OK);
	}
	
	
	
}
