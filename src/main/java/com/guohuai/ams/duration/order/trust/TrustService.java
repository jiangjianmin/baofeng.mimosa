package com.guohuai.ams.duration.order.trust;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.guohuai.ams.duration.capital.CapitalEntity;
import com.guohuai.ams.duration.capital.CapitalService;
import com.guohuai.ams.duration.order.OrderService;
import com.guohuai.ams.investment.Investment;
import com.guohuai.component.util.BigDecimalUtil;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.web.view.PageResp;

@Service
public class TrustService {

	@Autowired
	private TrustDao trustDao;
	@Autowired
	private TrustOrderDao trustPurchaseDao;
	@Autowired
	private TrustIncomeDao trustIncomeDao;
	@Autowired
	private TrustTransDao trustTransDao;
	@Autowired
	private TrustAuditDao trustAuditDao;
	@Autowired
	private CapitalService capitalService;
	
	Logger logger = Logger.getLogger(TrustService.class);
	
	/**
	 * 录入申购订单
	 * @param entity
	 */
	@Transactional
	public void save(TrustOrderEntity entity) {
		trustPurchaseDao.save(entity);
	}
	
	/**
	 * 录入持仓信息
	 * @param entity
	 */
	@Transactional
	public void save(TrustEntity entity) {
		trustDao.save(entity);
	}
	
	@Transactional
	public void save(List<TrustEntity> list) {
		trustDao.save(list);
	}
	
	/**
	 * 录入本息兑付订单
	 * @param entity
	 */
	@Transactional
	public void save(TrustIncomeEntity entity) {
		trustIncomeDao.save(entity);
	}
	
	/**
	 * 录入转让订单
	 * @param entity
	 */
	@Transactional
	public void save(TrustTransEntity entity) {
		trustTransDao.save(entity);
	}
	
	/**
	 * 录入审核订单
	 * @param entity
	 */
	@Transactional
	public void save(TrustAuditEntity entity) {
		trustAuditDao.save(entity);
	}
	
	/**
	 * 获取持仓信息
	 * @param oid
	 * @return
	 */
	@Transactional
	public TrustEntity getTrustByOid(String oid) {
		TrustEntity entity = trustDao.findOne(oid);
		
		return entity;
	}
	
	/**
	 * 获取当前资产池对此标的的持仓信息
	 * @param pid
	 * @param targetOid
	 * @return
	 */
	public TrustEntity getFundByPidAndTargetOid(final String pid, final String targetOid, final String profitType) {
		Specification<TrustEntity> spec = new Specification<TrustEntity>() {
			
			@Override
			public Predicate toPredicate(Root<TrustEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				// TODO Auto-generated method stub
				return cb.and(cb.equal(root.get("assetPoolOid").as(String.class), pid),
						cb.equal(root.get("target").get("oid").as(String.class), targetOid),
						cb.equal(root.get("state").as(String.class), "0"),
						cb.equal(root.get("profitType").as(String.class), profitType));
			}
		};
		
		TrustEntity entity = trustDao.findOne(spec);
		
		return entity;
	}
	
	/**
	 * 获取持仓信息
	 * @param oid
	 * @return
	 */
	public PageResp<JSONObject> getDataByTargetOid(String oid, Pageable pageable) {
		PageResp<JSONObject> rep = new PageResp<JSONObject>();
		List<JSONObject> objList = Lists.newArrayList();
		int total = 0;
		if (StringUtils.isNotBlank(oid)) {
			List<Object> list = trustDao.findByTargetOid(oid, pageable.getPageNumber(), pageable.getPageSize());
			if (null != list && !list.isEmpty()) {
				total = trustDao.findCountByTargetOid(oid);
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
	 * 根据订单oid获取 申购 的信托（计划）订单
	 * @param oid
	 * @return
	 */
	@Transactional
	public TrustOrderEntity getTrustOrderByOid(String oid) {
		TrustOrderEntity entity = trustPurchaseDao.findOne(oid);
		
		return entity;
	}
	
	/**
	 * 根据订单oid获取 本息兑付 的信托（计划）订单
	 * @param oid
	 * @return
	 */
	@Transactional
	public TrustIncomeEntity getTrustIncomeOrderByOid(String oid) {
		TrustIncomeEntity entity = trustIncomeDao.findOne(oid);
		
		return entity;
	}
	
	/**
	 * 根据订单oid获取 转让 的信托（计划）订单
	 * @param oid
	 * @return
	 */
	@Transactional
	public TrustTransEntity getTrustTransOrderByOid(String oid) {
		TrustTransEntity entity = trustTransDao.findOne(oid);
		
		return entity;
	}
	
	/**
	 * 获取预约中的申购订单
	 * @param pid
	 * @return
	 */
	@Transactional
	public List<TrustOrderEntity> findPurchaseByPidForAppointment(String pid) {
		List<TrustOrderEntity> list = trustPurchaseDao.findPurchaseByPidForAppointment(pid);
		
		return list;
	}
	
	/**
	 * 获取预约中的本息兑付订单
	 * @param pid
	 * @return
	 */
	@Transactional
	public List<TrustIncomeEntity> findIncomeByPidForAppointment(String pid) {
		List<TrustIncomeEntity> list = trustIncomeDao.findIncomeByPidForAppointment(pid);
		
		return list;
	}
	
	/**
	 * 获取预约中的转让订单
	 * @param pid
	 * @return
	 */
	@Transactional
	public List<TrustTransEntity> findTransByPidForAppointment(String pid) {
		List<TrustTransEntity> list = trustTransDao.findTransByPidForAppointment(pid);
		
		return list;
	}
	
	/**
	 * 获取已确认的申购订单
	 * @param pid
	 * @return
	 */
	@Transactional
	public List<TrustOrderEntity> findPurchaseByPidForConfirm(String pid) {
		List<TrustOrderEntity> list = trustPurchaseDao.findPurchaseByPidForConfirm(pid);
		
		return list;
	}
	
	/**
	 * 获取已确认的本息兑付订单
	 * @param pid
	 * @return
	 */
	@Transactional
	public List<TrustIncomeEntity> findIncomeByPidForConfirm(String pid) {
		List<TrustIncomeEntity> list = trustIncomeDao.findIncomeByPidForConfirm(pid);
		
		return list;
	}
	
	/**
	 * 获取已确认的转让订单
	 * @param pid
	 * @return
	 */
	@Transactional
	public List<TrustTransEntity> findTransByPidForConfirm(String pid) {
		List<TrustTransEntity> list = trustTransDao.findTransByPidForConfirm(pid);
		
		return list;
	}
	
	/**
	 * 获取已确认订单
	 * @param pid
	 * @return
	 */
	@Transactional
	public Page<TrustEntity> findByPidForConfirm(String pid, Pageable pageable) {
		return trustDao.findByPidForConfirm(pid, pageable);
	}
	
	/**
	 * 获取全平台的持仓列表
	 * @return
	 */
	@Transactional
	public List<TrustEntity> findTargetList() {
		List<TrustEntity> list = trustDao.findAll();
		
		return list;
	}
	
	/**
	 * 获取资产池的持仓列表
	 * @param pid
	 * @return
	 */
	@Transactional
	public List<TrustEntity> findTargetListByPid(String pid) {
		List<TrustEntity> list = trustDao.findFundListByPid(pid);
		
		return list;
	}
	
	/**
	 * 逻辑删除订单
	 * @param oid
	 * @param operation
	 * 					订单类型
	 * @param operator
	 * 					操作人
	 */
	@Transactional
	public void updateOrder(String oid, String operation, String operator) {
		if (CapitalEntity.APPLY.equals(operation) || CapitalEntity.TRANS.equals(operation)) {
			trustPurchaseDao.updateOrder(oid, operator);
		} else if (CapitalEntity.INCOME.equals(operation)
				|| CapitalEntity.BACK.equals(operation)) {
			trustIncomeDao.updateOrder(oid, operator);
		} else {
			trustTransDao.updateOrder(oid, operator);
		}
	}
	
	/**
	 * 逻辑作废订单--坏账核销
	 * @param oid
	 * @return
	 */
	@Transactional
	public void cancelOrder(String oid) {
		trustDao.updateOrder(oid);
	}
	
	/**
	 * 获取本息兑付的期数
	 * @param pid
	 * @param oid
	 * @return
	 */
	public int getSeqByIncome(String oid) {
		List<TrustIncomeEntity> list = trustIncomeDao.findSeqByPidAndOidForIncome(oid);
		if (null != list && list.size() > 0) {
			return list.size();
		}
		
		return 0;
	}
	
	/**
	 * 根据标的oid查询
	 * @param targetOid
	 * @return
	 */
	public List<TrustEntity> getDataByTargetOid(final String targetOid) {
		Specification<TrustEntity> spec = new Specification<TrustEntity>() {
			
			@Override
			public Predicate toPredicate(Root<TrustEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				// TODO Auto-generated method stub
				return root.get("target").get("oid").in(targetOid);
			}
		};
		
		return trustDao.findAll(spec);
	}
	
	/**
	 * 计算当前资产池当日投资标的的净申购额
	 * @param pid
	 * @param date
	 * @return
	 */
	@Transactional
	public BigDecimal navData(final String pid, final Date date) {
		BigDecimal navData = BigDecimalUtil.init0;
		
		Specification<TrustOrderEntity> spec = new Specification<TrustOrderEntity>() {
			
			@Override
			public Predicate toPredicate(Root<TrustOrderEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				// TODO Auto-generated method stub
				return cb.and(cb.equal(root.get("assetPoolOid").as(String.class), pid),
						cb.equal(root.get("confirmDate").as(Date.class), date));
			}
		};
		// 申购单
		List<TrustOrderEntity> purchaseList = trustPurchaseDao.findAll(spec);
		if (null != purchaseList && !purchaseList.isEmpty()) {
			for (TrustOrderEntity entity : purchaseList) {
				navData.add(entity.getInvestCash()).setScale(4, BigDecimal.ROUND_HALF_UP);
			}
		}
		// 赎回单（转让单）
		List<TrustTransEntity> redeemList = trustTransDao.findByPidAndConfirmDate(pid, date);
		if (null != redeemList && !redeemList.isEmpty()) {
			for (TrustTransEntity entity : redeemList) {
				navData.subtract(entity.getInvestCash()).setScale(4, BigDecimal.ROUND_HALF_UP);
			}
		}
		
		return navData;
	}
	
	/**
	 * 统计资产池对此标的的持仓数据，不区分收益方式
	 * @param targetOid
	 * @return
	 */
	@Transactional
	public List<Object> findByTargetOidForObj(String targetOid) {
		return trustDao.findByTargetOidForObj(targetOid);
	}
	
	/**
	 * 统计资产池对此标的的持仓数据，不区分收益方式
	 * @param targetOid
	 * @return
	 */
	@Transactional
	public List<TrustEntity> findByTargetOid(String targetOid) {
		return trustDao.findByTargetOid(targetOid);
	}
	
	/**
	 * 计算转让的标的理论市值
	 * @param entity
	 * 				信托标的对象
	 * @param volume
	 * 				转让份额
	 * @param date
	 * 				转让日期
	 * @return
	 */
	public BigDecimal calcTargetValuation(TrustEntity entity, BigDecimal volume, java.util.Date date) {
		BigDecimal profit = BigDecimal.ZERO;
		// 判断收益方式（amortized_cost：摊余成本法；book_value：账面价值法）
		if ("amortized_cost".equals(entity.getProfitType())) {
			// 当日收益
			BigDecimal dayProfit = BigDecimal.ZERO;
			// 计息天数
			int days = 0;
			// 募集期收益
			if (null == entity.getTarget().getCollectIncomeRate()) {
				logger.error("收益率为空！");
			}
			if (null == entity.getTarget().getCollectEndDate()) {
				logger.error("募集期日期不合理！");
			} else {
				days = DateUtil.getDaysBetweenTwoDate(entity.getInvestDate(), entity.getTarget().getCollectEndDate());
				if (days < 0) {
					days = 0;
				}
			}
			dayProfit = volume.multiply(entity.getTarget().getCollectIncomeRate())
					.divide(new BigDecimal(entity.getTarget().getContractDays()), 4, BigDecimal.ROUND_HALF_UP);
			profit = profit.add(dayProfit.multiply(new BigDecimal(days)).setScale(4, BigDecimal.ROUND_HALF_UP));
			
			// 存续期收益
			// 计息开始日
			Date sdate = entity.getInvestDate();
			// 计息截止日
			Date edate = new Date(date.getTime());
			if (DateUtil.compare_date(entity.getTarget().getIncomeStartDate(), entity.getInvestDate()) > 0) {
				sdate = entity.getTarget().getIncomeStartDate();
			}
			if (DateUtil.compare_date(entity.getTarget().getIncomeEndDate(), entity.getInvestDate()) < 0) {
				edate = entity.getTarget().getIncomeEndDate();
			}
			int tmp = DateUtil.getDaysBetweenTwoDate(sdate, edate);
			if (tmp > 0) {
				days = days + tmp;
			}
			dayProfit = volume.multiply(entity.getTarget().getExpAror())
					.divide(new BigDecimal(entity.getTarget().getContractDays()), 4, BigDecimal.ROUND_HALF_UP);
			profit = profit.add(dayProfit.multiply(new BigDecimal(days)).setScale(4, BigDecimal.ROUND_HALF_UP));
			
			// 逾期收益
			if (Investment.INVESTMENT_STATUS_OVERDUE_TRANSFER.equals(entity.getTarget().getState())) {
				tmp = DateUtil.getDaysBetweenTwoDate(entity.getTarget().getOverdueDay(), new Date(date.getTime()));
				if (tmp > 0) {
					days = days + tmp;
				}
			}
			dayProfit = volume.multiply(entity.getTarget().getOverdueRate())
					.divide(new BigDecimal(entity.getTarget().getContractDays()), 4, BigDecimal.ROUND_HALF_UP);
			profit = profit.add(dayProfit.multiply(new BigDecimal(days)).setScale(4, BigDecimal.ROUND_HALF_UP));
		}
		
		return volume.add(profit).setScale(4, BigDecimal.ROUND_HALF_UP);
	}
	
	/**
	 * 根据持仓oid获取订单的明细信息
	 * @param trustOid
	 * @return
	 */
	public List<TrustTransEntity> findOrderByTargetOid(final String trustOid) {
		Specification<TrustTransEntity> spec = new Specification<TrustTransEntity>() {
			
			@Override
			public Predicate toPredicate(Root<TrustTransEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				// TODO Auto-generated method stub
				return cb.and(cb.equal(root.get("trustEntity").get("oid").as(String.class), trustOid),
						cb.notEqual(root.get("state").as(String.class), "-1"));
			}
		};
		
		return trustTransDao.findAll(spec);
	}
	
	/**
	 * 当标的进入兑付期时，销毁之前未确认的赎回（转让）订单
	 * 并且回归持仓额度，重置冻结，在途资金
	 * @param targetOid
	 */
	public void updateTargetByLifeState(String targetOid) {
		List<TrustEntity> list = this.findByTargetOid(targetOid);
		if (null != list && !list.isEmpty()) {
			BigDecimal sumCash = BigDecimalUtil.init0;
			BigDecimal tempCash = BigDecimalUtil.init0;
			List<TrustTransEntity> orderList = Lists.newArrayList();
			for (TrustEntity entity : list) {
				if (entity.getTransOutVolume().compareTo(BigDecimalUtil.init0) > 0) {
					entity.setInvestVolume(entity.getInvestVolume()
							.add(entity.getTransOutVolume())
							.setScale(4, BigDecimal.ROUND_HALF_UP));
					entity.setTransOutVolume(BigDecimalUtil.init0);
					orderList = this.findOrderByTargetOid(entity.getOid());
					sumCash = BigDecimalUtil.init0;
					if (null != orderList && !orderList.isEmpty()) {
						for (TrustTransEntity order : orderList) {
							tempCash = order.getAuditCash().compareTo(BigDecimalUtil.init0) == 0 ?
									order.getTranCash() : order.getAuditCash();
							sumCash = sumCash.add(tempCash).setScale(4, BigDecimal.ROUND_HALF_UP);
							order.setState(OrderService.INVALID);
							order.setUpdateTime(DateUtil.getSqlCurrentDate());
							
							// 资金变动记录
							capitalService.capitalFlow(order.getTrustEntity().getAssetPoolOid(), order.getTrustEntity().getTarget().getOid(), 
									order.getOid(), OrderService.TRUST, BigDecimalUtil.init0, tempCash, "transfer", "confirm", null, "-1");
						}
						trustTransDao.save(orderList);
					}
					entity.setTransOutFee(entity.getTransOutFee()
							.subtract(sumCash).setScale(4, BigDecimal.ROUND_HALF_UP));
					entity.setUpdateTime(DateUtil.getSqlCurrentDate());
				}
				
			}
			trustDao.save(list);
		}
	}
}
