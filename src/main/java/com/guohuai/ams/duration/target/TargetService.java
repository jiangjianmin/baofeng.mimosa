package com.guohuai.ams.duration.target;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.guohuai.ams.cashtool.CashTool;
import com.guohuai.ams.cashtool.CashToolService;
import com.guohuai.ams.cashtool.pool.CashtoolPoolService;
import com.guohuai.ams.duration.assetPool.AssetPoolService;
import com.guohuai.ams.duration.order.FundForm;
import com.guohuai.ams.duration.order.TransForm;
import com.guohuai.ams.duration.order.TrustForm;
import com.guohuai.ams.duration.order.trust.TrustEntity;
import com.guohuai.ams.duration.order.trust.TrustIncomeForm;
import com.guohuai.ams.duration.order.trust.TrustService;
import com.guohuai.ams.investment.Investment;
import com.guohuai.ams.investment.InvestmentService;
import com.guohuai.ams.investment.pool.InvestmentPoolService;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.BigDecimalUtil;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.StringUtil;
import com.guohuai.component.web.view.PageResp;

/**
 * 存续期--产品服务接口
 * @author star.zhu
 * 2016年5月17日
 */
@Service
public class TargetService {
	
	@Autowired
	private RepaymentScheduleDao repaymentDao;
	
	@Autowired
	private InvestmentService investmentService;
	@Autowired
	private CashToolService cashToolService;
	@Autowired
	private InvestmentPoolService investmentPoolService;
	@Autowired
	private CashtoolPoolService cashtoolPoolService;
	@Autowired
	private TrustService trustService;
	@Autowired
	private AssetPoolService poolService;
	
//	@Autowired
//	private TargetOverdueService overdueService;
	
	/**
	 * 获取可申购的现金标的
	 * @param projectTypes
	 * @return
	 */
	@Transactional
	public List<FundForm> getFundListByScopes(String[] scopes) {
		List<FundForm> formList = Lists.newArrayList();
		List<CashTool> list = cashtoolPoolService.getCollecting(scopes);
		if (!list.isEmpty()) {
			FundForm form = null;
			for (CashTool ct : list) {
				form = new FundForm();
				form.setCashtoolOid(ct.getOid());
				form.setCashtoolName(ct.getSecShortName());
				form.setCashtoolType(ct.getEtfLof());
				form.setNetRevenue(ct.getDailyProfit());
				form.setYearYield7(ct.getWeeklyYield());
				form.setRiskLevel(ct.getRiskLevel());
				form.setDividendType(ct.getDividendType());
				form.setCirculationShares(ct.getCirculationShares());
				
				formList.add(form);
			}
		}
		
		return formList;
	}
	
	/**
	 * 获取可申购的信托标的
	 * @param projectTypes
	 * @return
	 */
	@Transactional
	public List<TrustForm> getTrustListByScopes(String[] scopes) {
		List<TrustForm> formList = Lists.newArrayList();
		List<Investment> list = investmentPoolService.getNotEstablishTarget(scopes);
		if (!list.isEmpty()) {
			TrustForm form = null;
			for (Investment inv : list) {
				form = new TrustForm();
				form.setTargetOid(inv.getOid());
				form.setTargetName(inv.getName());
				form.setTargetType(inv.getType());
				form.setExpSetDate(inv.getExpSetDate());
				form.setExpAror(inv.getExpAror());
				form.setAccrualType(inv.getAccrualType());
				form.setArorFirstDate(inv.getArorFirstDate());
				form.setAccrualDate(inv.getAccrualDate());
				form.setContractDays(inv.getContractDays());
				form.setSubjectRating(inv.getSubjectRating());
				form.setCollectStartDate(inv.getCollectStartDate());
				form.setCollectEndDate(inv.getCollectEndDate());
				form.setCollectIncomeRate(inv.getCollectIncomeRate());
				form.setFloorVolume(inv.getFloorVolume());
				form.setIncome(inv.getExpIncome());
				form.setSetDate(inv.getSetDate());
				form.setRaiseScope(inv.getRaiseScope());
				form.setTrustAmount(inv.getTrustAmount());
				form.setRestTrustAmount(inv.getRestTrustAmount());
				form.setLife(inv.getLife());
				form.setLifeUnit(inv.getLifeUnit());
				form.setState(inv.getState());
				form.setSellScope(inv.getRaiseScope()
						.subtract(inv.getHoldAmount())
						.subtract(inv.getApplyAmount())
						.setScale(4, BigDecimal.ROUND_HALF_UP));
				
				formList.add(form);
			}
		}
		
		return formList;
	}
	
	/**
	 * 获取可转入的信托标的
	 * @param projectTypes
	 * @return
	 */
	@Transactional
	public List<TransForm> getTransListByScopes(String[] scopes) {
		List<TransForm> formList = Lists.newArrayList();
		List<Investment> list = investmentPoolService.getEstablishTarget(scopes);
		if (!list.isEmpty()) {
			TransForm form = null;
			for (Investment inv : list) {
				form = new TransForm();
				form.setT_targetOid(inv.getOid());
				form.setT_targetName(inv.getName());
				form.setT_targetType(inv.getType());
				form.setYield(inv.getExpAror());
				form.setT_accrualType(inv.getAccrualType());
				form.setT_arorFirstDate(inv.getArorFirstDate());
				form.setT_accrualDate(inv.getAccrualDate());
				form.setT_contractDays(inv.getContractDays());
				form.setT_subjectRating(inv.getSubjectRating());
				form.setT_floorVolume(inv.getFloorVolume());
				form.setIncome(inv.getExpIncome());
				form.setT_setDate(inv.getSetDate());
				form.setT_raiseScope(inv.getRaiseScope());
				form.setT_trustAmount(inv.getTrustAmount());
				form.setT_restTrustAmount(inv.getRestTrustAmount());
				form.setT_life(inv.getLife());
				form.setT_lifeUnit(inv.getLifeUnit());
				form.setState(inv.getState());
				form.setT_sellScope(inv.getRaiseScope()
						.subtract(inv.getHoldAmount())
						.subtract(inv.getApplyAmount())
						.setScale(4, BigDecimal.ROUND_HALF_UP));
				
				formList.add(form);
			}
		}
		
		return formList;
	}

	/**
	 * 根据 oid 获取 货币基金（现金类管理工具） 详情
	 * @param oid
	 * @return
	 */
	@Transactional
	public CashTool getCashToolByOid(String oid) {
		
		return cashToolService.findByOid(oid);
	}

	/**
	 * 根据 oid 获取 货币基金（现金类管理工具） 详情
	 * @param oid
	 * @return
	 */
	@Transactional
	public FundForm getFundByOid(String oid) {
		FundForm form = new FundForm();
		
		CashTool entity = this.getCashToolByOid(oid);
		if (null != entity) {
			form.setCashtoolOid(entity.getTicker());
			form.setCashtoolName(entity.getSecShortName());
			form.setCashtoolType(entity.getEtfLof());
			form.setYearYield7(entity.getWeeklyYield());
			form.setNetRevenue(entity.getDailyProfit());
			form.setRiskLevel(entity.getRiskLevel());
			form.setDividendType(entity.getDividendType());
			form.setCirculationShares(entity.getCirculationShares());
		}
		
		return form;
	}

	/**
	 * 根据 oid 获取 信托（计划） 详情
	 * @param oid
	 * @return
	 */
	@Transactional
	public Investment getInvestmentByOid(String oid) {
		
		return investmentService.getInvestmentDet(oid);
	}

	/**
	 * 根据 oid 获取 信托（计划） 详情
	 * @param oid
	 * @return
	 */
	@Transactional
	public TrustForm getTrustByOid(String oid) {
		TrustForm form = new TrustForm();
		
		Investment entity = this.getInvestmentByOid(oid);
		if (null != entity) {
			form.setTargetOid(entity.getSn());
			form.setTargetName(entity.getName());
			form.setTargetType(entity.getType());
			form.setIncomeRate(entity.getExpAror());
			form.setSubjectRating(entity.getSubjectRating());
			form.setExpSetDate(entity.getExpSetDate());
			form.setExpAror(entity.getExpAror());
			form.setAccrualType(entity.getAccrualType());
			form.setArorFirstDate(entity.getArorFirstDate());
			form.setContractDays(entity.getContractDays());
			form.setRaiseScope(entity.getRaiseScope());
			form.setLife(entity.getLife());
			form.setFloorVolume(entity.getFloorVolume());
			form.setCollectStartDate(entity.getCollectStartDate());
			form.setCollectEndDate(entity.getCollectEndDate());
			form.setCollectIncomeRate(entity.getCollectIncomeRate());
			form.setState(entity.getState());
		}
		
		return form;
	}

	/**
	 * 根据 标的名称 获取 信托（计划） 列表（支持模糊查询）
	 * @param oid
	 * @return
	 */
	@Transactional
	public List<FundForm> getTrustListByName(String name) {
		List<FundForm> formList = Lists.newArrayList();
		
//		CashTool entity = cashToolService.findByOid(name);
//		if (null != entity) {
//			form.setAssetPoolCashtoolOid(entity.getTicker());
//			form.setName(entity.getSecShortName());
//			form.setType(entity.getEtfLof());
//			form.setYearYield7(entity.getWeeklyYield());
//			form.setNetRevenue(entity.getDailyProfit());
//		}
		
		return formList;
	}
	
	/**
	 * 本息兑付数据
	 * @param target
	 * 				标的对象
	 * @param targetOid
	 * @param entity
	 * 				持仓对象
	 * @param no
	 * 				期数
	 * @return
	 */
	public TrustIncomeForm getIncomeData(Investment target, TrustEntity entity, int no) {
		List<TrustIncomeForm> formList = Lists.newArrayList();
//		List<TargetIncome> list = investmentPoolService.getTargetIncome(target.getOid());
//		Map<Integer, BigDecimal> map = new HashMap<>();
//		if (null != list && list.size() > 0) {
//			for (TargetIncome in : list) {
//				map.put(in.getSeq(), in.getIncomeRate());
//			}
//		}
		// 日利息 算法：利息=本金×收益率%÷365天(有时是360天)
		BigDecimal day_yield = BigDecimalUtil.init0;
		TrustIncomeForm form = null;
		if (target.getContractDays() != 0)
			day_yield = entity.getInvestVolume()
					.multiply(entity.getTarget().getExpAror())
					.divide(new BigDecimal(target.getContractDays()), 4, BigDecimal.ROUND_HALF_UP);
		else
			throw AMPException.getException("缺少合同年天数");
		
		// 逾期收益
		BigDecimal over_profit = BigDecimalUtil.init0;
//		TargetOverdue overdue = overdueService.findByTargetOid(target.getOid());
		if (null != target && target.getOverdueDays() > 0) {
			over_profit = target.getOverdueRate().multiply(entity.getInvestVolume())
					.divide(new BigDecimal(target.getContractDays()), 4, BigDecimal.ROUND_HALF_UP)
					.multiply(new BigDecimal(target.getOverdueDays()))
					.setScale(4, BigDecimal.ROUND_HALF_UP);
		}
//		System.out.println("逾期收益" + over_profit);
		
		// 募集期收益
		BigDecimal collect_profit = this.collectingProfit(target, entity.getInvestVolume(), new Date(entity.getInvestDate().getTime()));

		// 获取付息方式 OTP:一次性；CYCLE:周期
		String pay_mode = target.getAccrualType();
		if ("ACCRUALTYPE_05".equals(pay_mode)) {
			form = new TrustIncomeForm();
			// 付息日
			form.setIncomeDate(target.getIncomeEndDate());
			// 本金
			form.setCapital(entity.getInvestVolume());
			if (over_profit.compareTo(BigDecimal.ZERO) != 0) {
				// 预期利益	算法：日利息*实际存续天数
				form.setExpIncome(new BigDecimal(target.getLifed()).multiply(day_yield)
						.add(entity.getInvestVolume())
						.add(collect_profit)
						.add(over_profit).setScale(4, BigDecimal.ROUND_HALF_UP));
				form.setExpIncomeRate(target.getOverdueRate());
			} else {
				// 预期利益	算法：日利息*实际存续天数
				form.setExpIncome(new BigDecimal(target.getLifed()).multiply(day_yield)
						.add(entity.getInvestVolume())
						.add(collect_profit)
						.setScale(4, BigDecimal.ROUND_HALF_UP));
				form.setExpIncomeRate(target.getExpAror());
			}
			// 实际值
			/*if (map.containsKey(1)) {
				if (target.getContractDays() != 0) {
					day_yield = entity.getInvestVolume()
							.multiply(map.get(1))
							.divide(new BigDecimal(target.getContractDays()), 4, BigDecimal.ROUND_HALF_UP);
				} else {
					day_yield = BigDecimalUtil.init0;
					throw new RuntimeException("合同年天数为null！");
				}
				form.setIncome(new BigDecimal(target.getLifed()).multiply(day_yield).setScale(4, BigDecimal.ROUND_HALF_UP));
				form.setIncomeRate(map.get(1));
			} else {
				form.setIncome(form.getExpIncome());
				form.setIncomeRate(form.getExpIncomeRate());
			}*/
			form.setIncome(form.getExpIncome());
			form.setIncomeRate(target.getExpAror());
			form.setCollectRate(target.getCollectIncomeRate());
			form.setOverdueRate(target.getOverdueRate());
			form.setSeq(1);
			
			return form;
			
//			formList.add(form);
		} else {
			// 设置周期递增基数
			int addNum = 0;
			// 获取付息周期 M:月；Q:季；H_Y:半年；Y:年
			if ("ACCRUALTYPE_01".equals(pay_mode)) {
				addNum = 1;
			} else if ("ACCRUALTYPE_02".equals(pay_mode)) {
				addNum = 3;
			} else if ("ACCRUALTYPE_03".equals(pay_mode)) {
				addNum = 6;
			} else if ("ACCRUALTYPE_04".equals(pay_mode)) {
				addNum = 12;
			}
			// 前一个付息日
			Date ldate = null;
			// 付息日
			Date sdate = null;
			// 获取付息周期的方式 NATURAL:自然；CONTRACT:合同
			if ("CONTRACT_YEAR".equals(target.getAccrualCycleType())) {
				// 首付日（收益分配基准日）算法：首付日自动为“收益起始日”+“周期”
				sdate = DateUtil.addMonth(new Date(target.getIncomeStartDate().getTime()), addNum);
			} else {
				sdate = target.getArorFirstDate();
			}
			// 起息天数
			int interest_days = 0;
			int seq = 1;
			if (DateUtil.compare_date(target.getIncomeEndDate(), sdate) > -1) {
				interest_days = DateUtil.getDaysBetween(new Date(target.getIncomeStartDate().getTime()), sdate);
				do {
					form = new TrustIncomeForm();
					form.setCapital(entity.getInvestVolume());
					form.setIncomeDate(sdate);
					if (DateUtil.compare_date(target.getIncomeEndDate(), sdate) == 0) {
						form.setIncomeDate(target.getIncomeEndDate());
						// 收益截止日 + 1
						interest_days = interest_days + 1;
						sdate = DateUtil.addMonth(sdate, addNum);
					} else {
						form.setIncomeDate(new java.sql.Date(sdate.getTime()));
						ldate = sdate;
						sdate = DateUtil.addMonth(sdate, addNum);
						if (DateUtil.compare_date(target.getIncomeEndDate(), sdate) < 1) {
							sdate = new Date(target.getIncomeEndDate().getTime());
						}
						interest_days = DateUtil.getDaysBetween(ldate, sdate);
					}
					form.setExpIncome(
							new BigDecimal(interest_days).multiply(day_yield)
							.setScale(4, BigDecimal.ROUND_HALF_UP));
					form.setExpIncomeRate(target.getExpAror());
					// 实际值
					/*if (map.containsKey(seq)) {
						if (target.getContractDays() != 0) {
							day_yield = entity.getInvestVolume()
									.multiply(map.get(1))
									.divide(new BigDecimal(target.getContractDays()), 4, BigDecimal.ROUND_HALF_UP);
						} else {
							day_yield = BigDecimalUtil.init0;
							throw new RuntimeException("合同年天数为null！");
						}
						form.setIncome(new BigDecimal(target.getLifed()).multiply(day_yield).setScale(4, BigDecimal.ROUND_HALF_UP));
						form.setIncomeRate(map.get(1));
					} else {
						form.setIncome(form.getExpIncome());
						form.setIncomeRate(form.getExpIncomeRate());
					}*/
					form.setSeq(seq);
					seq ++;
					formList.add(form);
				} while (DateUtil.compare_date(target.getIncomeEndDate(), sdate) > -1);
			}
		}
		
//		return formList.size() > no ? formList.get(no) : null;
		return null;
	}
	
	/**
	 * 投资标的-还款计划
	 * @param inv
	 */
	public void repayMentSchedule(Investment inv) {
		List<TrustEntity> list = trustService.getDataByTargetOid(inv.getOid());
		if (null != list && !list.isEmpty()) {
			for (TrustEntity entity : list) {
				this.repayMentSchedule(inv, entity);
			}
		}
	}
	
	/**
	 * 投资标的-还款计划
	 * @param inv
	 * @param trust
	 */
	public void repayMentSchedule(Investment inv, TrustEntity trust) {
		// 日利息 算法：利息=本金×收益率%÷365天(有时是360天)
		BigDecimal day_yield = BigDecimalUtil.init0;
		if (inv.getContractDays() != 0)
			day_yield = trust.getInvestVolume()
					.multiply(inv.getExpAror())
					.divide(new BigDecimal(inv.getContractDays()), 4, BigDecimal.ROUND_HALF_UP);
		
		// 逾期收益
		BigDecimal over_profit = BigDecimalUtil.init0;
//		TargetOverdue overdue = overdueService.findByTargetOid(inv.getOid());
		if (null != inv && inv.getOverdueDays() > 0) {
			over_profit = inv.getOverdueRate().multiply(trust.getInvestVolume())
					.divide(new BigDecimal(inv.getContractDays()), 4, BigDecimal.ROUND_HALF_UP)
					.multiply(new BigDecimal(inv.getOverdueDays()))
					.setScale(4, BigDecimal.ROUND_HALF_UP);
		}
		// 获取付息方式 OTP:一次性；CYCLE:周期
		String pay_mode = inv.getAccrualType();
		if ("ACCRUALTYPE_05".equals(pay_mode)) {
			RepaymentScheduleEntity entity = new RepaymentScheduleEntity();
			entity.setOid(StringUtil.uuid());
			entity.setHoldOid(trust.getOid());
			entity.setAssetPoolOid(trust.getAssetPoolOid());
			entity.setTargetOid(inv.getOid());
			entity.setTargetName(inv.getName());
			entity.setSeq(1);
			entity.setRepaymentDate(inv.getIncomeEndDate());
			entity.setRepaymentAmount(trust.getInvestVolume()
					.add(new BigDecimal(inv.getLife()).multiply(day_yield))
					.add(over_profit)
					.add(this.collectingProfit(inv, trust.getInvestVolume(), new Date(trust.getInvestDate().getTime())))
					.setScale(4, BigDecimal.ROUND_HALF_UP));
			entity.setType(RepaymentScheduleEntity.capital);
			entity.setStatus(RepaymentScheduleEntity.unPay);
			entity.setCreateTime(new Timestamp(System.currentTimeMillis()));
			repaymentDao.save(entity);
		} else {
			List<RepaymentScheduleEntity> list = Lists.newArrayList();
			RepaymentScheduleEntity entity = null;
			// 设置周期递增基数
			int addNum = 0;
			// 获取付息周期 M:月；Q:季；H_Y:半年；Y:年
			if ("ACCRUALTYPE_01".equals(pay_mode)) {
				addNum = 1;
			} else if ("ACCRUALTYPE_02".equals(pay_mode)) {
				addNum = 3;
			} else if ("ACCRUALTYPE_03".equals(pay_mode)) {
				addNum = 6;
			} else if ("ACCRUALTYPE_04".equals(pay_mode)) {
				addNum = 12;
			}
			// 前一个付息日
			Date ldate = null;
			// 付息日
			Date sdate = null;
			// 获取付息周期的方式 NATURAL:自然；CONTRACT:合同
			if ("CONTRACT_YEAR".equals(inv.getAccrualCycleType())) {
				// 首付日（收益分配基准日）算法：首付日自动为“收益起始日”+“周期” -1
				sdate = DateUtil.addMonth(new Date(inv.getIncomeStartDate().getTime()), addNum);
				sdate = DateUtil.addDay(sdate, -1);
			} else {
				sdate = inv.getArorFirstDate();
			}
			// 起息天数
			int interest_days = 0;
			// 还款期数
			int seq = 1;

			if (DateUtil.compare_date(inv.getIncomeEndDate(), sdate) > -1) {
				interest_days = DateUtil.getDaysBetween(new Date(inv.getIncomeStartDate().getTime()), sdate);
				do {
					entity = new RepaymentScheduleEntity();
					entity.setOid(StringUtil.uuid());
					entity.setHoldOid(trust.getOid());
					entity.setAssetPoolOid(trust.getAssetPoolOid());
					entity.setTargetOid(inv.getOid());
					entity.setTargetName(inv.getName());
					entity.setSeq(seq);
					if (DateUtil.compare_date(inv.getIncomeEndDate(), sdate) == 0) {
						entity.setRepaymentDate(inv.getIncomeEndDate());
						entity.setType(RepaymentScheduleEntity.capital);
						// 收益截止日 + 1
						interest_days = interest_days + 1;
						sdate = DateUtil.addMonth(sdate, addNum);
					} else {
						entity.setRepaymentDate(new java.sql.Date(sdate.getTime()));
						entity.setType(RepaymentScheduleEntity.interest);
						ldate = sdate;
						sdate = DateUtil.addMonth(sdate, addNum);
						if (DateUtil.compare_date(inv.getIncomeEndDate(), sdate) < 1) {
							sdate = new Date(inv.getIncomeEndDate().getTime());
						}
						interest_days = DateUtil.getDaysBetween(ldate, sdate);
					}
					entity.setRepaymentAmount(
							new BigDecimal(interest_days).multiply(day_yield)
							.setScale(4, BigDecimal.ROUND_HALF_UP));
					entity.setStatus(RepaymentScheduleEntity.unPay);
					entity.setCreateTime(new Timestamp(System.currentTimeMillis()));
					
					list.add(entity);
					seq ++;
				} while (DateUtil.compare_date(inv.getIncomeEndDate(), sdate) > -1);
				
				repaymentDao.save(list);
			}
		}
	}
	
	/**
	 * 募集期收益
	 * @param inv
	 * @param holdAmount
	 * 					持有额度
	 * @param investDay
	 * 					投资日
	 * @return
	 */
	public BigDecimal collectingProfit(Investment inv, BigDecimal holdAmount, Date investDay) {
		BigDecimal collectingProfit = BigDecimalUtil.init0;
		if (null != inv.getCollectIncomeRate()
				&& null != inv.getContractDays()
				&& null != inv.getCollectEndDate()) {
			int days = DateUtil.daysBetween(new Date(inv.getCollectEndDate().getTime()), investDay);
			// 当标的提前成立时，募集期截止日为成立日
			if (null != inv.getSetDate() && inv.getCollectEndDate().compareTo(inv.getSetDate()) > 0) {
				days = DateUtil.daysBetween(new Date(inv.getSetDate().getTime()), investDay);
			} else {
				days = DateUtil.daysBetween(new Date(inv.getCollectEndDate().getTime()), investDay);
			}
			if (days < 0) {
				days = 0;
			}
			collectingProfit = holdAmount.multiply(inv.getCollectIncomeRate())
					.multiply(new BigDecimal(days))
					.divide(new BigDecimal(inv.getContractDays()), 4, BigDecimal.ROUND_HALF_UP);
		} else {
			throw AMPException.getException("募集期数据不完整");
		}
//		System.out.println("募集期收益" + collectingProfit);
		
		return collectingProfit;
	}
	
	/**
	 * 获取 投资标的还款计划 列表
	 * @param pid
	 * @param pageable
	 * @return
	 */
	public PageResp<RepaymentScheduleEntity> getRepaymentScheduleList(String pid, Pageable pageable) {
		PageResp<RepaymentScheduleEntity> rep = new PageResp<RepaymentScheduleEntity>();
		pid = poolService.getPid(pid);
		Page<RepaymentScheduleEntity> list = repaymentDao.findByPid(pid, pageable);
		if (null != list && !list.getContent().isEmpty()) {
			rep.setTotal(list.getTotalElements());
			rep.setRows(list.getContent());
		}

		return rep;
	}
	
	/**
	 * 更新投资标的还款计划为已支付
	 * @param holdOid
	 * @param seq
	 */
	public void updateRepaymentSchedule(String holdOid, Integer seq) {
		repaymentDao.update(holdOid, seq);
	}
	
	/**
	 * 投资标的-坏账核销
	 * 资产池估值对应减去该笔投资，现金不变
	 * @param targetOid
	 */
	public void targetCancel(String targetOid) {
		List<TrustEntity> list = trustService.findByTargetOid(targetOid);
		if (null != list && !list.isEmpty()) {
			Map<String, BigDecimal> map = new HashMap<>();
			BigDecimal tmpVolume = BigDecimalUtil.init0;
			String key = null;
			for (TrustEntity entity : list) {
				if (TrustEntity.INVESTEND.equals(entity.getState())) {
					continue;
				}
				key = entity.getAssetPoolOid() + "-" + entity.getTarget().getOid();
				if (map.containsKey(key)) {
					tmpVolume = map.get(key);
					tmpVolume = tmpVolume.add(entity.getInvestVolume())
							.add(entity.getTotalProfit())
							.setScale(6, BigDecimal.ROUND_HALF_UP);
					map.put(key, tmpVolume);
				} else {
					map.put(key, entity.getInvestVolume().add(entity.getTotalProfit()).setScale(6, BigDecimal.ROUND_HALF_UP));
				}
				entity.setInvestVolume(BigDecimalUtil.init0);
				entity.setDailyProfit(BigDecimalUtil.init0);
				entity.setTotalProfit(BigDecimalUtil.init0);
				// 设置标的的持仓状态为1，已坏账核销，待确认
				entity.setState(TrustEntity.CANCEL);
				entity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
			}
			
			// 更新资产池的估值
			for (String id : map.keySet()) {
				poolService.updateInvestPlan(id.split("-")[0], map.get(key));
			}
			
			// 删除资产池的还款计划
			repaymentDao.delete(targetOid);
		}
	}
	
	/**
	 * 当标的进入兑付期时，销毁之前未确认的赎回（转让）订单
	 * 并且回归持仓额度，重置冻结，在途资金
	 * @param targetOid
	 */
	public void updateTargetByLifeState(String targetOid) {
		trustService.updateTargetByLifeState(targetOid);
	}
}
