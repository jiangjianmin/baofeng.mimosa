package com.guohuai.cardvo;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.guohuai.basic.cardvo.req.userInfoReq.MUAllReq;
import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.cardvo.service.MimosaNewService;
import com.guohuai.usercenter.api.UserCenterSdk;

import lombok.extern.slf4j.Slf4j;


/**
 * 用户信息
 * @author yujianlong
 *
 */
@Slf4j
@RestController
@RequestMapping(value = "/mimosa/cardvo/mimosaUCNew/",produces = "application/json")
public class MimosaNewController extends BaseController {
	@Autowired
	private MimosaNewService mimosaNewService;
	@Autowired
	UserCenterSdk userCenterSdk;
	
	
	/**
	 * 计算数量
	 * @param mUAllReq
	 * @return
	 */
	@RequestMapping(value = "/countAll", method = RequestMethod.POST)
	public @ResponseBody Long countAll(@RequestBody @Valid MUAllReq mUAllReq) {
		return mimosaNewService.countAll(mUAllReq);
	}
	/**
	 * 计算所有锁定用户的数量
	 * @param mUAllReq
	 * @return
	 */
	@RequestMapping(value = "/countAllLocked", method = RequestMethod.POST)
	public @ResponseBody Long countAllLocked(@RequestBody @Valid MUAllReq mUAllReq) {
		return mimosaNewService.countAllLocked(mUAllReq);
	}
	/**
	 * 获取全部userid列表
	 * @param mUAllReq
	 * @return
	 */
	@RequestMapping(value = "/queryIdList", method = RequestMethod.POST)
	public @ResponseBody List<String> queryIdList(@RequestBody @Valid MUAllReq mUAllReq) {
		return mimosaNewService.queryIdList(mUAllReq);
	}
	/**
	 * 获取全部锁定userid列表
	 * @param mUAllReq
	 * @return
	 */
	@RequestMapping(value = "/queryIdListOnlocked", method = RequestMethod.POST)
	public @ResponseBody List<String> queryIdListOnlocked(@RequestBody @Valid MUAllReq mUAllReq) {
		return mimosaNewService.queryIdListOnlocked(mUAllReq);
	}
	/**查询部分字段
	 * @param mUAllReq
	 * @return
	 */
	@RequestMapping(value = "/query2MapsSome", method = RequestMethod.POST)
	public @ResponseBody List<Map<String, Object>> query2MapsSome(@RequestBody @Valid MUAllReq mUAllReq) {
		return mimosaNewService.query2MapsSome(mUAllReq);

	}
	/**查询锁定用户部分字段
	 * @param mUAllReq
	 * @return
	 */
	@RequestMapping(value = "/query2MapsSomeOnlocked", method = RequestMethod.POST)
	public @ResponseBody List<Map<String, Object>> query2MapsSomeOnlocked(@RequestBody @Valid MUAllReq mUAllReq) {
		return mimosaNewService.query2MapsSomeOnlocked(mUAllReq);
		
	}
	/**查全部字段
	 * @param mUAllReq
	 * @return
	 */
	@RequestMapping(value = "/query2MapsAll", method = RequestMethod.POST)
	public @ResponseBody List<Map<String, Object>> query2MapsAll(@RequestBody @Valid MUAllReq mUAllReq) {
		return mimosaNewService.query2MapsAll(mUAllReq);
		
	}
	/**查锁定用户全部字段
	 * @param mUAllReq
	 * @return
	 */
	@RequestMapping(value = "/query2MapsAllOnlocked", method = RequestMethod.POST)
	public @ResponseBody List<Map<String, Object>> query2MapsAllOnlocked(@RequestBody @Valid MUAllReq mUAllReq) {
		return mimosaNewService.query2MapsAllOnlocked(mUAllReq);
		
	}
	/**
	 * 持仓表单独查询。
	 * @param mUAllReq
	 * @return
	 */
	@RequestMapping(value = "/query2MapsOnPublishHoldByUserIds", method = RequestMethod.POST)
	public @ResponseBody List<Map<String, Object>> query2MapsOnPublishHoldByUserIds(@RequestBody @Valid MUAllReq mUAllReq) {
		return mimosaNewService.query2MapsOnPublishHoldByUserIds(mUAllReq);
		
	}
	/**
	 * 中间表单独查询
	 * @param mUAllReq
	 * @return
	 */
	@RequestMapping(value = "/query2MapsOnTradeStasticsByUserIds", method = RequestMethod.POST)
	public @ResponseBody List<Map<String, Object>> query2MapsOnTradeStasticsByUserIds(@RequestBody @Valid MUAllReq mUAllReq) {
		return mimosaNewService.query2MapsOnTradeStasticsByUserIds(mUAllReq);
		
	}



}
