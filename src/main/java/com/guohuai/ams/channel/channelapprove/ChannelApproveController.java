package com.guohuai.ams.channel.channelapprove;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

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
import com.guohuai.component.web.view.BaseResp;
import com.guohuai.component.web.view.PageResp;

import net.kaczmarzyk.spring.data.jpa.domain.DateAfterInclusive;
import net.kaczmarzyk.spring.data.jpa.domain.DateBeforeInclusive;
import net.kaczmarzyk.spring.data.jpa.domain.In;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
@RestController
@RequestMapping(value = "/mimosa/channelapprove", produces = "application/json")
public class ChannelApproveController extends BaseController {

	@Autowired
	private ChannelApproveService serviceChannelApprove;

	@RequestMapping(value = "query", name="获取渠道列表", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<PageResp<ChannelApproveQueryRep>> channelApproveQuery(HttpServletRequest request,
			@And({  @Spec(params = "channelName", path = "channelName", spec = Like.class),
//					@Spec(params = "requester", path = "requester", spec = Like.class),
					@Spec(params = "channelApprovelCode", path = "channelApprovelCode", spec = Like.class),
					@Spec(params = "requestType", path = "requestType", spec = In.class),					
					@Spec(params = "approvelResult", path = "approveStatus", spec = In.class),
					@Spec(params = "reqTimeBegin", path = "requestTime", spec = DateAfterInclusive.class),
					@Spec(params = "reqTimeEnd", path = "requestTime", spec = DateBeforeInclusive.class),}) 
	         		Specification<ChannelApprove> spec,
			@RequestParam int page, 
			@RequestParam int rows) {		
		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(Direction.DESC, "createTime")));		
		PageResp<ChannelApproveQueryRep> rep = this.serviceChannelApprove.channelApproveQuery(spec, pageable);
		
		return new ResponseEntity<PageResp<ChannelApproveQueryRep>>(rep, HttpStatus.OK);
	}
	
	/**
	 * 渠道申请开启/关闭处理
	 * @param req 
	 * @return
	 */
	@RequestMapping(value = "dealapply", name="渠道申请开启/关闭处理", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseResp> delApply(@Valid ChannelApproveOpeReq req) {
		String operator = super.getLoginUser();
		BaseResp rep = this.serviceChannelApprove.dealApply(req, operator);		
		return new ResponseEntity<BaseResp>(rep, HttpStatus.OK);
	}
	
}
