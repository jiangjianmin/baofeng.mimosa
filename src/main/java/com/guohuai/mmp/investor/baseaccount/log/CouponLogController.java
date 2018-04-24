package com.guohuai.mmp.investor.baseaccount.log;

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
import com.guohuai.basic.component.ext.web.PageResp;
import net.kaczmarzyk.spring.data.jpa.domain.DateAfterInclusive;
import net.kaczmarzyk.spring.data.jpa.domain.DateBeforeInclusive;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;

@RestController
@RequestMapping(value="/mimosa/boot/couponLog",produces="application/json")
public class CouponLogController {
	
	@Autowired
	private CouponLogService couponLogService;
	
	@RequestMapping(value = "findCouponLog", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<PageResp<CouponLogPojo>> findCouponLog(HttpServletRequest request,
			@And({
				@Spec(params = "begin", path = "createTime", spec = DateAfterInclusive.class),
				@Spec(params = "end", path = "createTime", spec = DateBeforeInclusive.class) })Specification<CouponLogEntity> spec,
			@RequestParam Integer page, 
			@RequestParam Integer rows) {
		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(Direction.DESC, "createTime")));
		PageResp<CouponLogPojo> rep =this.couponLogService.findCouponLog(spec, pageable);
		return new ResponseEntity<PageResp<CouponLogPojo>>(rep, HttpStatus.OK);
	}
}
