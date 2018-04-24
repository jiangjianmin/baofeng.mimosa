package com.guohuai.cardvo.service;

import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.basic.cardvo.req.userInfoReq.MUAllReq;
import com.guohuai.cardvo.dao.MimosaNewDao;

/**
 * 最新获取用户信息形式
 * @author yujianlong
 *
 */
@Service
@Transactional
public class MimosaNewService {
	@Autowired
	private MimosaNewDao mimosaNewDao;

	/**
	 * 计算数量
	 * @param mUAllReq
	 * @return
	 */
	public Long countAll(MUAllReq mUAllReq) {
		return mimosaNewDao.countAll(mUAllReq);
	}
	/**
	 * 计算所有锁定用户的数量
	 * @param mUAllReq
	 * @return
	 */
	public Long countAllLocked(MUAllReq mUAllReq) {
		return mimosaNewDao.countAllLocked(mUAllReq);
	}
	
	/**
	 * 获取全部userid列表
	 * @param mUAllReq
	 * @return
	 */
	public List<String> queryIdList(MUAllReq mUAllReq) {
		return mimosaNewDao.queryIdList(mUAllReq);
	}
	/**
	 * 获取全部锁定userid列表
	 * @param mUAllReq
	 * @return
	 */
	public List<String> queryIdListOnlocked(MUAllReq mUAllReq) {
		return mimosaNewDao.queryIdListOnlocked(mUAllReq);
	}

	/**查询部分字段
	 * @param mUAllReq
	 * @return
	 */
	public List<Map<String, Object>> query2MapsSome(MUAllReq mUAllReq) {
		return mimosaNewDao.query2MapsSome(mUAllReq);

	}
	/**查询锁定用户部分字段
	 * @param mUAllReq
	 * @return
	 */
	public List<Map<String, Object>> query2MapsSomeOnlocked(MUAllReq mUAllReq) {
		return mimosaNewDao.query2MapsSomeOnlocked(mUAllReq);
		
	}
	/**查全部字段
	 * @param mUAllReq
	 * @return
	 */
	public List<Map<String, Object>> query2MapsAll(MUAllReq mUAllReq) {
		return mimosaNewDao.query2MapsAll(mUAllReq);
		
	}
	/**查锁定用户全部字段
	 * @param mUAllReq
	 * @return
	 */
	public List<Map<String, Object>> query2MapsAllOnlocked(MUAllReq mUAllReq) {
		return mimosaNewDao.query2MapsAllOnlocked(mUAllReq);
		
	}
	/**
	 * 持仓表单独查询。
	 * @param mUAllReq
	 * @return
	 */
	public List<Map<String, Object>> query2MapsOnPublishHoldByUserIds(MUAllReq mUAllReq) {
		return mimosaNewDao.query2MapsOnPublishHoldByUserIds(mUAllReq);
		
	}
	/**
	 * 中间表单独查询
	 * @param mUAllReq
	 * @return
	 */
	public List<Map<String, Object>> query2MapsOnTradeStasticsByUserIds(MUAllReq mUAllReq) {
		return mimosaNewDao.query2MapsOnTradeStasticsByUserIds(mUAllReq);
		
	}

	
}
