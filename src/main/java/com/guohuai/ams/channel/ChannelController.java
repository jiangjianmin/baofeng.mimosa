
package com.guohuai.ams.channel;

import java.util.List;

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

import com.alibaba.fastjson.JSONObject;
import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.web.view.BaseResp;
import com.guohuai.component.web.view.PageResp;
import com.guohuai.component.web.view.Response;

import net.kaczmarzyk.spring.data.jpa.domain.In;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
@RestController
@RequestMapping(value = "/mimosa/channel", produces = "application/json")
public class ChannelController extends BaseController {

	@Autowired
	private ChannelService serviceChannel;

	@RequestMapping(value = "query", name="获取渠道列表", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<PageResp<ChannelQueryRep>> channelQuery(HttpServletRequest request,
			@And({  @Spec(params = "channelName", path = "channelName", spec = Like.class),
					@Spec(params = "channelCode", path = "channelCode", spec = Like.class),
					@Spec(params = "partner", path = "partner", spec = Like.class),
					@Spec(params = "contactName", path = "channelContactName", spec = Like.class),
					@Spec(params = "joinType", path = "joinType", spec = In.class),
					@Spec(params = "channelStatus", path = "channelStatus", spec = In.class),
					@Spec(params = "delStatus", path = "deleteStatus", spec = In.class)}) 
	         		Specification<Channel> spec,
			@RequestParam int page, 
			@RequestParam int rows) {		
		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(Direction.DESC, "createTime")));		
		PageResp<ChannelQueryRep> rep = this.serviceChannel.channelQuery(spec, pageable);
		
		return new ResponseEntity<PageResp<ChannelQueryRep>>(rep, HttpStatus.OK);
	}
	
	/**
	 * 获取渠道详情
	 * @param oid
	 * @return
	 */
	@RequestMapping(value = "channelinfo", name="获取渠道详情", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<ChannelInfoRep> getInfo(@RequestParam String oid) {		
		ChannelInfoRep rep = this.serviceChannel.getChannelInfo(oid);
		return new ResponseEntity<ChannelInfoRep>(rep, HttpStatus.OK);
	}
	
	/**
	 * 新增渠道
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "add", name="新增渠道", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseResp> addChanInfo(@Valid ChannelAddReq req) {		
		BaseResp rep = new BaseResp();
		this.serviceChannel.addChannel(req);
		return new ResponseEntity<BaseResp>(rep, HttpStatus.OK);		
	}
	
	/**
	 * 修改渠道
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "edit", name="修改渠道", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseResp> editChanInfo(@Valid ChannelAddReq req) {		
		BaseResp rep = new BaseResp();
		this.serviceChannel.addChannel(req);
		return new ResponseEntity<BaseResp>(rep, HttpStatus.OK);		
	}
	
	/**
	 * 删除渠道
	 * @param oid
	 * @return
	 */
	@RequestMapping(value = "delete", name="删除渠道", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseResp> delChanInfo(@RequestParam String oid) {		
		BaseResp rep = this.serviceChannel.delChannel(oid);
		return new ResponseEntity<BaseResp>(rep, HttpStatus.OK);		
	}
	
	/**
	 * 获取一个渠道信息
	 * @return
	 */
	@RequestMapping(value = "onechannel", name="获取一个渠道信息", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<ChannelInfoRep> getOneChannel() {		
		ChannelInfoRep rep = this.serviceChannel.getOneChannel();
		return new ResponseEntity<ChannelInfoRep>(rep, HttpStatus.OK);		
	}
	
	/**
	 * 渠道申请处理
	 * @param oid
	 * @param requestType 申请类型 on/off
	 * @return
	 */
	@RequestMapping(value = "setapply", name="渠道申请处理", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseResp> setApply(@RequestParam String oid, @RequestParam String requestType) {
		String operator = super.getLoginUser();
		BaseResp rep = this.serviceChannel.setApply(oid, requestType, operator);
		return new ResponseEntity<BaseResp>(rep, HttpStatus.OK);
	}
	
	/**
	 * 获取审批表中审核意见列表
	 * @param oid
	 * @return
	 */
	@RequestMapping(value = "remarksquery", name="获取审批表中审核意见列表", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<PageResp<ChannelRemarksRep>> remarksQuery(@RequestParam String oid) {		
		
		PageResp<ChannelRemarksRep> rep = this.serviceChannel.remarksQuery(oid);
		
		return new ResponseEntity<PageResp<ChannelRemarksRep>>(rep, HttpStatus.OK);
	}
	

	@RequestMapping(value = "options", name="获取可以选渠道列表", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<List<ChannelOptions>> options() {		
		
		List<ChannelOptions> rep = this.serviceChannel.getOptions();
		
		return new ResponseEntity<List<ChannelOptions>>(rep, HttpStatus.OK);
	}
	
	/**
	 * 验证cid是否唯一
	 * 
	 * @param name
	 * @param oid
	 * @return
	 */
	@RequestMapping(value = "/validateCid", name="验证cid是否唯一", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<BaseResp> validateCid(@RequestParam String cid, @RequestParam(required = false) String id) {
		BaseResp pr = new BaseResp();
		long single = this.serviceChannel.validateSingle("cid", cid, id);
		return new ResponseEntity<BaseResp>(pr, single > 0 ? HttpStatus.CONFLICT : HttpStatus.OK);
	}
	
	/**
	 * 验证ckey是否唯一
	 * 
	 * @param name
	 * @param oid
	 * @return
	 */
	@RequestMapping(value = "/validateCkey", name="验证ckey是否唯一", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<BaseResp> validateCode(@RequestParam String ckey, @RequestParam(required = false) String id) {
		BaseResp pr = new BaseResp();
		long single = this.serviceChannel.validateSingle("ckey", ckey, id);
		return new ResponseEntity<BaseResp>(pr, single > 0 ? HttpStatus.CONFLICT : HttpStatus.OK);
	}
	
	@RequestMapping(value = "/getChannelNames", name = "新加编辑产品包可以选择的渠道名称列表", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> getChannelNames() {
		List<JSONObject> jsonList = serviceChannel.getChannelNames();
		Response r = new Response();
		r.with("rows", jsonList);
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	
	
	
}
