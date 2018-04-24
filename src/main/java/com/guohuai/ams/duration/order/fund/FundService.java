package com.guohuai.ams.duration.order.fund;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.guohuai.component.util.BigDecimalUtil;
import com.guohuai.component.web.view.PageResp;

@Service
public class FundService {

	@Autowired
	private FundDao fundDao;
	@Autowired
	private FundOrderDao fundOrderDao;
	@Autowired
	private FundAuditDao fundAuditDao;
	
	/**
	 * 录入申购订单
	 * @param entity
	 */
	@Transactional
	public void save(FundOrderEntity entity) {
		fundOrderDao.save(entity);
	}
	
	/**
	 * 录入持仓信息
	 * @param entity
	 */
	@Transactional
	public void save(FundEntity entity) {
		fundDao.save(entity);
	}
	
	@Transactional
	public void save(List<FundEntity> list) {
		fundDao.save(list);
	}
	
	/**
	 * 录入审核订单
	 * @param entity
	 */
	@Transactional
	public void save(FundAuditEntity entity) {
		fundAuditDao.save(entity);
	}
	
	/**
	 * 获取 现金管理工具 的申购列表
	 * @param cashtoolOid
	 * @return
	 */
	@Transactional
	public PageResp<JSONObject> getDataByCashtoolOid(String oid, Pageable pageable) {
		PageResp<JSONObject> rep = new PageResp<JSONObject>();
		List<JSONObject> objList = Lists.newArrayList();
		int total = 0;
		if (StringUtils.isNotBlank(oid)) {
			List<Object> list = fundDao.findByCashtoolOid(oid, pageable.getPageNumber(), pageable.getPageSize());
			if (null != list && !list.isEmpty()) {
				total = fundDao.findCountByCashtoolOid(oid);
				JSONObject obj = null;
				Object[] o = null;
				for (Object entity : list) {
					obj = new JSONObject();
					o = (Object[]) entity;
					obj.put("assetPoolOid", String.valueOf(o[0]));
					obj.put("assetPoolName", String.valueOf(o[1]));
					obj.put("volume", new BigDecimal(String.valueOf(o[2])));
					objList.add(obj);
				}

			}
		}
		rep.setTotal(total);
		rep.setRows(objList);
		return rep;
	}
	
	/**
	 * 获取持仓信息
	 * @param oid
	 * @return
	 */
	@Transactional
	public FundEntity getFundByOid(String oid) {
		FundEntity entity = fundDao.findOne(oid);
		
		return entity;
	}
	
	/**
	 * 获取当前资产池对此标的的持仓信息
	 * @param pid
	 * @param cashtoolOid
	 * @return
	 */
	public FundEntity getFundByPidAndCashtoolOid(final String pid, final String cashtoolOid) {
		Specification<FundEntity> spec = new Specification<FundEntity>() {
			
			@Override
			public Predicate toPredicate(Root<FundEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				// TODO Auto-generated method stub
				return cb.and(cb.equal(root.get("assetPoolOid").as(String.class), pid),
						cb.equal(root.get("cashTool").get("oid").as(String.class), cashtoolOid),
						cb.equal(root.get("state").as(String.class), "0"));
			}
		};
		
		return fundDao.findOne(spec);
	}

	
	/**
	 * 根据订单oid获取 申购 的信托（计划）订单
	 * @param oid
	 * @return
	 */
	@Transactional
	public FundOrderEntity getFundOrderByOid(String oid) {
		FundOrderEntity entity = fundOrderDao.findOne(oid);
		
		return entity;
	}
	
	/**
	 * 获取预约中订单
	 * @param pid
	 * @return
	 */
	@Transactional
	public PageResp<FundOrderEntity> findByPidForAppointment(String pid, Pageable pageable) {
		int page = pageable.getPageNumber();
		int rows = pageable.getPageSize();
		int sNo = page * rows;
		int eNo = (page + 1) * rows;
		List<FundOrderEntity> list = fundOrderDao.findByPidForAppointment(pid, sNo, eNo);
		Integer count = fundOrderDao.findCountForFundAppointment(pid);
		PageResp<FundOrderEntity> resp = new PageResp<FundOrderEntity>();
		resp.setTotal(count);
		resp.setRows(list);
		
		return resp;
	}
	
	/**
	 * 获取已确认订单
	 * @param pid
	 * @return
	 */
	@Transactional
	public Page<FundEntity> findByPidForConfirm(String pid, Pageable pageable) {
		return fundDao.findByPidForConfirm(pid, pageable);
	}
	
	/**
	 * 获取全平台的持仓列表
	 * @return
	 */
	@Transactional
	public List<FundEntity> findFundList() {
		List<FundEntity> list = fundDao.findAll();
		
		return list;
	}
	
	/**
	 * 获取资产池的持仓列表
	 * @param pid
	 * @return
	 */
	@Transactional
	public List<FundEntity> findFundListByPid(String pid) {
		List<FundEntity> list = fundDao.findFundListByPid(pid);
		
		return list;
	}
	
	/**
	 * 逻辑删除订单
	 * @param oid
	 * @return
	 */
	@Transactional
	public void updateOrder(String oid, String operator) {
		fundOrderDao.updateOrder(oid, operator);
	}
	
	/**
	 * 计算当前资产池当日现金类管理工具的净申购额
	 * @param pid
	 * @param date
	 * @return
	 */
	@Transactional
	public BigDecimal navData(String pid, Date date) {
		BigDecimal navData = BigDecimalUtil.init0;
		
		List<FundOrderEntity> list = fundOrderDao.findByPidAndConfirmDate(pid, date);
		if (null != list && !list.isEmpty()) {
			for (FundOrderEntity entity : list) {
				if ("purchase".equals(entity.getOptType())) {
					navData.add(entity.getInvestVolume()).setScale(4, BigDecimal.ROUND_HALF_UP);
				} else {
					navData.subtract(entity.getInvestVolume()).setScale(4, BigDecimal.ROUND_HALF_UP);
				}
			}
		}
		
		return navData;
	}
}
