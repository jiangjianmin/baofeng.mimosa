package com.guohuai.mmp.platform.statistics;

import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.guohuai.plugin.PageVo;

import lombok.extern.slf4j.Slf4j;
/**
 * 
 * @ClassName: UserBehaviorController
 * @Description: 用户注册，绑卡，投资行为统计
 * @author yihonglei
 * @date 2017年5月8日 下午5:26:48
 * @version 1.0.0
 */
@RestController
@RequestMapping(value = "/mimosa/boot/userBehaviorStatistics", produces = "application/json")
@Slf4j
public class UserBehaviorController {
	
	@Autowired
	private UserBehaviorStatisticsService userBehaviorStatisticsService;
	/**
	 * 
	 * @author yihonglei
	 * @Title: queryUserStatistics
	 * @Description:查询用户注册，绑卡，投资统计信息
	 * @param req
	 * @return ResponseEntity<UserBehaviorStatisticsRes>
	 * @date 2017年5月8日 下午5:27:24
	 * @since  1.0.0
	 */
	@RequestMapping(value = "queryUserStatistics", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<PageVo<Map<String,Object>>> queryUserStatistics(@Valid @RequestBody UserBehaviorStatisticsReq req) {
		log.info("用户行为统计查询参数:{}",JSONObject.toJSONString(req));		
		PageVo<Map<String,Object>> res = userBehaviorStatisticsService.queryUserStatistics(req);
		return new ResponseEntity<PageVo<Map<String,Object>>>(res, HttpStatus.OK);
	}
	
	/**
	 * 
	 * @author yihonglei
	 * @Title: queryUserRegisterChannelOid
	 * @Description: 用户注册渠道列表
	 * @return ResponseEntity<PageVo<Map<String,Object>>>
	 * @date 2017年6月27日 下午11:58:46
	 * @since  1.0.0
	 */
	@RequestMapping(value = "queryUserRegisterChannelOid", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<PageVo<Map<String,Object>>> queryUserRegisterChannelOid() {
		PageVo<Map<String,Object>> res = userBehaviorStatisticsService.queryUserRegisterChannelOid();
		return new ResponseEntity<PageVo<Map<String,Object>>>(res, HttpStatus.OK);
	}
	
}
