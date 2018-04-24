package com.guohuai.cardvo.service;

import java.util.List;
import java.util.Map;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.guohuai.basic.cardvo.req.userInfoReq.MUAllReq;
import com.guohuai.cardvo.dao.UserInfoDao;

/**
 * 用户查询
 * 
 * @author huyong
 * @date 2017.5.25
 */
@Service
@Transactional
public class UserInfoService {
	@Autowired
	private UserInfoDao userInfoDao;
	
	/**
	 * @desc 根据用户oid查询用户明细
	 * @author hy
	 * @date 2017.5.25
	 */
	public List<Map<String, Object>> getUserListByOids(MUAllReq mUAllReq) {
		return userInfoDao.getUserListByOids(mUAllReq);
	}
	
}
