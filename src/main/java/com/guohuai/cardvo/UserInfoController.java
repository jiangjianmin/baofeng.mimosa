package com.guohuai.cardvo;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.guohuai.basic.cardvo.req.userInfoReq.MUAllReq;
import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.cardvo.service.UserInfoService;

/**
 * 用户信息
 * @author huyong
 * @date 2017.5.25
 */
@RestController
@RequestMapping(value = "/mimosa/cardvo/",produces = "application/json")
public class UserInfoController extends BaseController {
	@Autowired
	private UserInfoService userInfoService;
	
	
	/**
	 * @desc 根据用户oid查询用户明细
	 * @author hy
	 * @date 2017.5.25
	 */
	@RequestMapping(value = "/getUserListByOids", method = RequestMethod.POST)
	@ResponseBody
	public List<Map<String, Object>> getUserListByOids(@RequestBody MUAllReq mUAllReq) {
		return this.userInfoService.getUserListByOids(mUAllReq);
	}
}
