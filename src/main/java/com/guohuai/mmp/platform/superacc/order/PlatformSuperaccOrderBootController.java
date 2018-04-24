package com.guohuai.mmp.platform.superacc.order;

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
import net.kaczmarzyk.spring.data.jpa.domain.In;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;

@RestController
@RequestMapping(value = "/mimosa/boot/platform/superacc/order", produces = "application/json")
public class PlatformSuperaccOrderBootController extends BaseController {
	
	@Autowired
	private PlatformSuperaccOrderService platformSuperaccOrderService;
	
	
	@RequestMapping(value = "query", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<PagesRep<PlatformSuperaccOrderQueryRep>> channelQuery(HttpServletRequest request,
			@And({ @Spec(params = "orderType", path = "orderType", spec = In.class),
					@Spec(params = "orderStatus", path = "orderStatus", spec = In.class),
					@Spec(params = "owner", path = "owner", spec = In.class),
					@Spec(params = "createTimeBegin", path = "createTime", spec = DateAfterInclusive.class, config = DateUtil.fullDatePattern),
					@Spec(params = "createTimeEnd", path = "createTime", spec = DateBeforeInclusive.class, config = DateUtil.fullDatePattern) })
			Specification<PlatformSuperaccOrderEntity> spec,
			@RequestParam int page, @RequestParam int rows) {
		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(Direction.DESC, "createTime")));
		PagesRep<PlatformSuperaccOrderQueryRep> rep = this.platformSuperaccOrderService.mng(spec, pageable);

		return new ResponseEntity<PagesRep<PlatformSuperaccOrderQueryRep>>(rep, HttpStatus.OK);
	}

	
	
	
}
