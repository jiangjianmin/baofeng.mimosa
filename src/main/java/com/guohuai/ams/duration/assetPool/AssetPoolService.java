package com.guohuai.ams.duration.assetPool;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.guohuai.ams.acct.books.AccountBook;
import com.guohuai.ams.acct.books.AccountBookService;
import com.guohuai.ams.duration.assetPool.history.HistoryValuationService;
import com.guohuai.ams.duration.assetPool.scope.ScopeService;
import com.guohuai.ams.duration.capital.calc.AssetPoolCalc;
import com.guohuai.ams.duration.capital.calc.AssetPoolCalcService;
import com.guohuai.ams.duration.capital.calc.ScheduleLog;
import com.guohuai.ams.duration.capital.calc.ScheduleLogService;
import com.guohuai.ams.duration.capital.calc.error.ErrorCalc;
import com.guohuai.ams.duration.capital.calc.error.ErrorCalcService;
import com.guohuai.ams.duration.capital.calc.fund.FundCalc;
import com.guohuai.ams.duration.capital.calc.fund.FundCalcService;
import com.guohuai.ams.duration.capital.calc.trust.TrustCalc;
import com.guohuai.ams.duration.capital.calc.trust.TrustCalcService;
import com.guohuai.ams.duration.order.fund.FundEntity;
import com.guohuai.ams.duration.order.fund.FundService;
import com.guohuai.ams.duration.order.trust.TrustEntity;
import com.guohuai.ams.duration.order.trust.TrustService;
import com.guohuai.ams.duration.utils.ConstantUtil;
import com.guohuai.commons.tradingCalendar.TradingCalendarService;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.BigDecimalUtil;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.StringUtil;
import com.guohuai.component.web.view.PageResp;
import com.guohuai.file.CsvExport;
import com.guohuai.mmp.job.lock.JobLockEntity;
import com.guohuai.mmp.job.lock.JobLockService;
import com.guohuai.mmp.job.log.JobLogEntity;
import com.guohuai.mmp.job.log.JobLogFactory;
import com.guohuai.mmp.job.log.JobLogService;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountEntity;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountService;
import com.guohuai.mmp.publisher.corporate.Corporate;
import com.guohuai.mmp.publisher.corporate.CorporateDao;

/**
 * 存续期--资产池服务接口
 * 
 * @author star.zhu
 *         2016年5月16日
 */
@Service
public class AssetPoolService {

	@Autowired
	private AssetPoolDao assetPoolDao;

	@Autowired
	private ScopeService scopeService;
	@Autowired
	private FundService fundService;
	@Autowired
	private TrustService trustService;

	@Autowired
	private FundCalcService fundCalcService;
	@Autowired
	private TrustCalcService trustCalcService;
	@Autowired
	private AssetPoolCalcService poolCalcService;
	@Autowired
	private ErrorCalcService errorCalcService;
	@Autowired
	private ScheduleLogService logService;
	@Autowired
	private PublisherBaseAccountService spvService;
	@Autowired
	private HistoryValuationService historyService;
	@Autowired
	private CorporateDao corporateDao;
	@Autowired
	private AccountBookService accountService;
	@Autowired
	private JobLogService jobLogService;
	@Autowired
	private JobLockService jobLockService;

	Logger logger = LoggerFactory.getLogger(AssetPoolService.class);

	/**
	 * 新建资产池
	 * 
	 * @param form
	 * @param uid
	 */
	@Transactional
	public void createPool(AssetPoolForm form, String uid) {
		AssetPoolEntity entity = new AssetPoolEntity();
		try {
			entity.setOid(StringUtil.uuid());
			entity.setName(form.getName());
			entity.setScale(BigDecimalUtil.formatForMul10000(form.getScale()));
			entity.setMarketValue(entity.getScale());
			entity.setCashPosition(entity.getScale());
			entity.setCashRate(BigDecimalUtil.formatForDivide100(form.getCashRate()));
			entity.setCashtoolRate(BigDecimalUtil.formatForDivide100(form.getCashtoolRate()));
			entity.setTargetRate(BigDecimalUtil.formatForDivide100(form.getTargetRate()));
			entity.setCashFactRate(BigDecimalUtil.NUM1);
			entity.setCalcBasis(form.getCalcBasis());
			entity.setState(AssetPoolEntity.status_create);
//			SPV spv = spvService.findOne(form.getSpvOid());
//			PublisherBaseAccountEntity spv = spvService.findOne(form.getSpvOid());
			PublisherBaseAccountEntity spv = spvService.findByCorperateOid(form.getSpvOid());
			entity.setSpvEntity(spv);
			entity.setOrganization(form.getOrganization());
			entity.setPlanName(form.getPlanName());
			entity.setBank(form.getBank());
			entity.setAccount(form.getAccount());
			entity.setContact(form.getContact());
			entity.setTelephone(form.getTelephone());
			// ----------------20170526,yihonglei add start-------------------
			entity.setBaseAssetCode(form.getBaseAssetCode());
			// ----------------20170526,yihonglei add end-------------------
			entity.setCreater(uid);
			entity.setCreateTime(DateUtil.getSqlCurrentDate());
			entity.setPurchaseLimit(form.getPurchaseLimit());

    		entity.setTrusteeRate(BigDecimalUtil.formatForDivide100(form.getTrusteeRate()));
    		entity.setManageRate(BigDecimalUtil.formatForDivide100(form.getManageRate()));
			assetPoolDao.save(entity);

			for (String s : form.getScopes()) {
				scopeService.save(entity, s);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 新建审核
	 * 
	 * @param operation
	 *            yes：同意
	 *            no：不同意
	 * @param oid
	 * @param uid
	 */
	@Transactional
	public void auditPool(String operation, String oid, String uid) {
		AssetPoolEntity entity = assetPoolDao.findOne(oid);
		if ("yes".equals(operation)) {
			entity.setState(AssetPoolEntity.status_duration);
		} else {
			entity.setState(AssetPoolEntity.status_unpass);
		}
	}

	/**
	 * 获取所有资产池列表
	 * 
	 * @return
	 */
	@Transactional
	public PageResp<AssetPoolForm> getAllList(Specification<AssetPoolEntity> spec, Pageable pageable) {
		List<AssetPoolForm> formList = Lists.newArrayList();
		Page<AssetPoolEntity> entityList = assetPoolDao.findAll(spec, pageable);
		AssetPoolForm form = null;
		if (null != entityList.getContent() && entityList.getContent().size() > 0) {
			for (AssetPoolEntity entity : entityList.getContent()) {
				form = new AssetPoolForm();
				try {
					BeanUtils.copyProperties(form, entity);
				} catch (Exception e) {
					e.printStackTrace();
				}
				formList.add(form);
			}
		}

		PageResp<AssetPoolForm> rep = new PageResp<AssetPoolForm>();
		rep.setRows(formList);
		rep.setTotal(entityList.getTotalElements());

		return rep;
	}

	/**
	 * 获取所有资产池的名称列表，包含id
	 * 
	 * @return
	 */
	@Transactional
	public List<JSONObject> getAllNameList() {
		List<JSONObject> jsonObjList = Lists.newArrayList();
		List<Object> objList = assetPoolDao.findAllNameList();
		if (!objList.isEmpty()) {
			Object[] obs = null;
			JSONObject jsonObj = null;
			for (Object obj : objList) {
				obs = (Object[]) obj;
				jsonObj = new JSONObject();
				jsonObj.put("oid", obs[0]);
				jsonObj.put("name", obs[1]);

				jsonObjList.add(jsonObj);
			}
		}

		return jsonObjList;
	}

	/**
	 * 根据资产池id获取对应的资产池详情
	 * 
	 * @param pid
	 * @return
	 */
	@Transactional
	public AssetPoolEntity getByOid(String pid) {
		AssetPoolEntity entity = assetPoolDao.findOne(pid);

		return entity;
	}

	/**
	 * 保存
	 * 
	 * @param entity
	 */
	@Transactional
	public void save(AssetPoolEntity entity) {
		assetPoolDao.save(entity);
	}

	/**
	 * 根据资产池id获取对应的资产池详情
	 * 
	 * @param pid
	 * @return
	 */
	@Transactional
	public AssetPoolForm getPoolByOid(String pid) {
		AssetPoolForm form = new AssetPoolForm();
		AssetPoolEntity entity = new AssetPoolEntity();
		pid = this.getPid(pid);
		if (null == pid || "".equals(pid)) {
			return null;
		} else {
			entity = this.getByOid(pid);
		}
		String[] scopes = scopeService.getScopes(pid);
		try {
			BeanUtils.copyProperties(form, entity);
			form.setScopes(scopes);
//			form.setSpvOid(entity.getSpvEntity().getOid());
//			form.setSpvName(entity.getSpvEntity().getName());
			Corporate corporate = this.corporateDao.findOne(entity.getSpvEntity().getCorperateOid());
			if(corporate!=null) {
				form.setSpvName(corporate.getName());
        		form.setSpvOid(corporate.getOid());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 获取收益数据
		Map<String, AccountBook> accountMap = accountService.find(pid, "2201", "2301", "300101", "300102");
		if (null != accountMap) {
			if (accountMap.containsKey("2201")) {
				form.setUnDistributeProfit(accountMap.get("2201").getBalance());
			}
			if (accountMap.containsKey("2301")) {
				form.setPayFeigin(accountMap.get("2301").getBalance());
			}
			if (accountMap.containsKey("300101")) {
				form.setInvestorProfit(accountMap.get("300101").getBalance());
			}
			if (accountMap.containsKey("300102")) {
				form.setSpvProfit(accountMap.get("300102").getBalance());
			}
		}

		return form;
	}

	/**
	 * 当pid为空的时候，默认获取第一个资产池
	 * 
	 * @param pid
	 * @return
	 */
	@Transactional
	public String getPid(String pid) {
		if (null == pid || "".equals(pid)) {
			AssetPoolEntity entity = assetPoolDao.getLimitOne();
			if (null != entity) {
				pid = entity.getOid();
			}
		}

		return pid;
	}

	/**
	 * 编辑资产池
	 * 
	 * @param form
	 * @param uid
	 */
	@Transactional
	public void editPool(AssetPoolForm form, String uid) {
		AssetPoolEntity entity = assetPoolDao.findOne(form.getOid());
		entity.setName(form.getName());
		entity.setScale(BigDecimalUtil.formatForMul10000(form.getScale()));
		entity.setMarketValue(entity.getScale());
		entity.setCashPosition(entity.getScale());
		entity.setCashRate(BigDecimalUtil.formatForDivide100(form.getCashRate()));
		entity.setCashtoolRate(BigDecimalUtil.formatForDivide100(form.getCashtoolRate()));
		entity.setTargetRate(BigDecimalUtil.formatForDivide100(form.getTargetRate()));
		entity.setState(AssetPoolEntity.status_create);
		entity.setCalcBasis(form.getCalcBasis());
//		SPV spv = spvService.findOne(form.getSpvOid());
		PublisherBaseAccountEntity spv = spvService.findByCorperateOid(form.getSpvOid());
		entity.setSpvEntity(spv);
		entity.setOrganization(form.getOrganization());
		entity.setPlanName(form.getPlanName());
		entity.setBank(form.getBank());
		entity.setAccount(form.getAccount());
		entity.setContact(form.getContact());
		entity.setTelephone(form.getTelephone());
		entity.setBaseAssetCode(form.getBaseAssetCode());
		entity.setOperator(uid);
		entity.setUpdateTime(DateUtil.getSqlCurrentDate());
		entity.setPurchaseLimit(form.getPurchaseLimit());
		entity.setTrusteeRate(BigDecimalUtil.formatForDivide100(form.getTrusteeRate()));
        entity.setManageRate(BigDecimalUtil.formatForDivide100(form.getManageRate()));
		assetPoolDao.save(entity);

		for (String s : form.getScopes()) {
			scopeService.save(entity, s);
		}
	}

	/**
	 * 编辑资产池账户信息
	 * 
	 * @param form
	 * @param uid
	 */
	@Transactional
	public void editPoolForCash(AssetPoolForm form, String uid) {
		this.updatePoolForCash(form.getOid(), form.getCashPosition(), uid);
	}

	/**
	 * 编辑资产池账户信息
	 * 
	 * @param pid
	 * @param capital
	 *            申赎金额，赎回为负值
	 * @param uid
	 */
	@Transactional
	public void editPoolForCash(String pid, BigDecimal capital, String uid) {
		AssetPoolEntity entity = assetPoolDao.findOne(pid);
		BigDecimal cashPosition = BigDecimalUtil.formatForMul10000(capital);
		cashPosition = BigDecimalUtil.formatForDivide10000(cashPosition.add(entity.getCashPosition()));
		this.updatePoolForCash(pid, cashPosition, uid);
	}

	/**
	 * 编辑资产池账户信息
	 * 
	 * @param pid
	 * @param capital
	 *            申赎金额，赎回为负值
	 * @param uid
	 */
	@Transactional
	public void updatePoolForCash(String pid, BigDecimal capital, String uid) {
		AssetPoolEntity entity = assetPoolDao.findOne(pid);
		BigDecimal cashPosition = BigDecimalUtil.formatForMul10000(capital);
		// 原规模
		BigDecimal scale = entity.getScale();
		// 当前规模
		BigDecimal nscale = scale.subtract(entity.getCashPosition()).add(cashPosition).setScale(4, BigDecimal.ROUND_HALF_UP);
		BigDecimal cashRate = cashPosition.divide(nscale, 4, BigDecimal.ROUND_HALF_UP);
		BigDecimal cashtoolRate = entity.getCashtoolFactRate().multiply(scale).divide(nscale, 4, BigDecimal.ROUND_HALF_UP);
		BigDecimal targetRate = entity.getTargetFactRate().multiply(scale).divide(nscale, 4, BigDecimal.ROUND_HALF_UP);
		entity.setScale(nscale);
		entity.setCashPosition(cashPosition);
		entity.setCashFactRate(cashRate);
		entity.setCashtoolFactRate(cashtoolRate);
		entity.setTargetFactRate(targetRate);
		entity.setOperator(uid);
		entity.setUpdateTime(DateUtil.getSqlCurrentDate());

		assetPoolDao.save(entity);
	}

	/**
	 * 逻辑删除资产池
	 * 
	 * @param pid
	 */
	@Transactional
	public void updateAssetPool(String pid) {
		assetPoolDao.updateAssetPool(pid);
	}

	/**
	 * 获取所有资产池列表
	 * 
	 * @param name
	 * @return
	 */
	@Transactional
	public List<AssetPoolForm> getListByName(String name) {
		List<AssetPoolForm> formList = Lists.newArrayList();
		List<AssetPoolEntity> entityList = assetPoolDao.getListByName(name);
		if (!entityList.isEmpty()) {
			AssetPoolForm form = null;
			for (AssetPoolEntity entity : entityList) {
				form = new AssetPoolForm();
				try {
					BeanUtils.copyProperties(form, entity);
				} catch (Exception e) {
					e.printStackTrace();
				}
				formList.add(form);
			}
		}

		return formList;
	}

	/**
	 * 定时初始化资产池的每日收益计算和收益分配的状态
	 * 
	 * @return
	 */
	@Transactional
	public void updateStateSchedule() {
	
		if (this.jobLockService.getRunPrivilege(JobLockEntity.JOB_jobId_updateStateSchedule)) {
			this.updateState();
		}
	}

	/**
	 * 定时初始化资产池的每日收益计算和收益分配的状态
	 * 
	 * @return
	 */
	@Transactional
	public void updateState() {
		JobLogEntity jobLog = JobLogFactory.getInstance(JobLockEntity.JOB_jobId_updateStateSchedule);
		try {
			// 所有资产池列表
			List<AssetPoolEntity> poolList = assetPoolDao.getListByState();
			if (null != poolList && !poolList.isEmpty()) {
				for (AssetPoolEntity entity : poolList) {
					entity.setScheduleState(AssetPoolEntity.schedule_wjs);
					entity.setIncomeState(AssetPoolEntity.income_wfp);
					entity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
				}
				assetPoolDao.save(poolList);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			jobLog.setJobMessage(AMPException.getStacktrace(e));
			jobLog.setJobStatus(JobLogEntity.JOB_jobStatus_failed);
		}
		jobLog.setBatchEndTime(DateUtil.getSqlCurrentDate());
		this.jobLogService.saveEntity(jobLog);
		this.jobLockService.resetJob(JobLockEntity.JOB_jobId_updateStateSchedule);
	}

	/**
	 * 计算资产池当日的确认收益
	 */
	@Transactional
	public void calcPoolProfitSchedule() {
		
		if (this.jobLockService.getRunPrivilege(JobLockEntity.JOB_jobId_calcPoolProfitSchedule)) {
			this.calcPoolProfit();
		}
	}

	/**
	 * 计算资产池当日的确认收益
	 */
	@Transactional
	public void calcPoolProfit() {
		JobLogEntity jobLog = JobLogFactory.getInstance(JobLockEntity.JOB_jobId_calcPoolProfitSchedule);
		try {
			// 所有资产池列表
			List<AssetPoolEntity> poolList = assetPoolDao.getListByState();
			if (null != poolList && !poolList.isEmpty()) {
				// 日期
				Calendar c = Calendar.getInstance();
				c.add(Calendar.DATE, -1);
				Date sqlDate = new Date(c.getTimeInMillis());
				// 如果是非交易日，则不计算当日收益，并且记录非交易日的天数
				if (TradingCalendarService.TRADING_CALENDAR_MAP.containsKey(sqlDate)) {
					for (AssetPoolEntity entity : poolList) {
						entity.setNonTradingDays(entity.getNonTradingDays() + 1);
					}
				} else {
					// 统计一共有多少个标的数据
					int jobCount = 0;
					// 统计一共执行了多少个标的数据
					int successCount = 0;
					ScheduleLog log = new ScheduleLog();
					log.setOid(StringUtil.uuid());
					log.setBaseDate(sqlDate);
					log.setStartTime(new Timestamp(System.currentTimeMillis()));
	
					Integer[] counts = new Integer[2];
					for (AssetPoolEntity entity : poolList) {
						counts = this.calcPoolProfit(entity, sqlDate, AssetPoolCalc.EventType.SCHEDULE.toString());
						jobCount += counts[0];
						successCount += counts[1];
					}
	
					log.setJobCount(jobCount);
					log.setSuccessCount(successCount);
					log.setEndTime(new Timestamp(System.currentTimeMillis()));
					logService.save(log);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			jobLog.setJobMessage(AMPException.getStacktrace(e));
			jobLog.setJobStatus(JobLogEntity.JOB_jobStatus_failed);
		}
		jobLog.setBatchEndTime(DateUtil.getSqlCurrentDate());
		this.jobLogService.saveEntity(jobLog);
		this.jobLockService.resetJob(JobLockEntity.JOB_jobId_calcPoolProfitSchedule);
	}

	/**
	 * 计算资产池当日的确认收益--手动触发
	 * 
	 * @return
	 */
	@Transactional
	public void userPoolProfit(String oid, String operator, String type) {
		// 资产池
		AssetPoolEntity entity = assetPoolDao.findOne(oid);
		entity.setOperator(operator);
		// 日期
		Date sqlDate = new Date(System.currentTimeMillis());
		ScheduleLog log = new ScheduleLog();
		log.setOid(StringUtil.uuid());
		log.setBaseDate(sqlDate);
		log.setStartTime(new Timestamp(System.currentTimeMillis()));

		Integer[] counts = this.calcPoolProfit(entity, sqlDate, type);

		log.setJobCount(counts[0]);
		log.setSuccessCount(counts[1]);
		log.setEndTime(new Timestamp(System.currentTimeMillis()));
		logService.save(log);
	}

	/**
	 * 计算资产池当日的确认收益
	 * 
	 * @param assetPool
	 * @param fundCalcList
	 * @param trustCalcList
	 * @param poolCalcList
	 * @return
	 */
	@Transactional
	public Integer[] calcPoolProfit(AssetPoolEntity assetPool, Date baseDate, String type) {
		// 统计一共有多少个标的数据
		int jobCount = 0;
		// 统计一共执行了多少个标的数据
		int successCount = 0;
		// 总估值
		BigDecimal valuation = BigDecimal.ZERO;
		// 总收益
		BigDecimal totalProfit = BigDecimal.ZERO;
		// 现金类管理工具收益
		BigDecimal fundProfit = BigDecimal.ZERO;
		// 信托标的收益
		BigDecimal trustProfit = BigDecimal.ZERO;
		// 会计分录
		// List<Income> incomeList = Lists.newArrayList();
		// 记录有问题的标的数据oid
		List<ErrorCalc> errorList = Lists.newArrayList();
		// 存档错误数据
		ErrorCalc errorCalc = null;
		// 错误信息
		JSONObject jsonObj = null;
		// 参与计算的现金管理工具列表
		List<FundEntity> fcList = Lists.newArrayList();
		// 参与计算的募集期信托标的列表
		List<TrustEntity> mjq_tcList = Lists.newArrayList();
		// 参与计算的存续期信托标的列表
		List<TrustEntity> cxq_tcList = Lists.newArrayList();
		// 参与计算的逾期信托标的列表
		List<TrustEntity> yq_tcList = Lists.newArrayList();
		// 计算结果
		// 现金管理工具每日定时收益数据
		List<FundCalc> fundCalcList = Lists.newArrayList();
		// 信托标的每日定时收益数据
		List<TrustCalc> trustCalcList = Lists.newArrayList();

//		Map<String, CashToolRevenue> revenues = new HashMap<String, CashToolRevenue>();

		try {
			// 持仓的现金管理工具
			List<FundEntity> fundList = fundService.findFundListByPid(assetPool.getOid());
			logger.info("========================== fundList: " + fundList.size());
			/*if (null != fundList && !fundList.isEmpty()) {
				jobCount += fundList.size();
				for (FundEntity entity : fundList) {
					CashToolRevenue revenue = null;
					if (revenues.containsKey(entity.getCashTool().getOid())) {
						revenue = revenues.get(entity.getCashTool().getOid());
					} else {
						revenue = this.cashtoolRevenueService.findByCashToolAndDate(entity.getCashTool(), baseDate);
						revenues.put(entity.getCashTool().getOid(), revenue);
					}

					if (null == revenue || null == revenue.getDailyProfit()) {
						errorCalc = new ErrorCalc();
						jsonObj = new JSONObject();
						errorCalc.setOid(StringUtil.uuid());
						errorCalc.setCashTool(entity.getCashTool());
						errorCalc.setAssetPool(assetPool);
						jsonObj.put("DaylyProfit", "收益率为空");
						errorCalc.setMessage(jsonObj);
						errorCalc.setOperator(assetPool.getOperator());
						errorCalc.setCreateTime(new Timestamp(System.currentTimeMillis()));
						errorList.add(errorCalc);
					} else {
						fcList.add(entity);
					}
				}
			}*/
			if (null != fundList && !fundList.isEmpty()) {
				jobCount += fundList.size();
				for (FundEntity entity : fundList) {
					if (null == entity.getCashTool().getDailyProfit()) {
						errorCalc = new ErrorCalc();
						jsonObj = new JSONObject();
						errorCalc.setOid(StringUtil.uuid());
						errorCalc.setCashTool(entity.getCashTool());
						errorCalc.setAssetPool(assetPool);
						jsonObj.put("DaylyProfit", "收益率为空");
						errorCalc.setMessage(jsonObj);
						errorCalc.setOperator(assetPool.getOperator());
						errorCalc.setCreateTime(new Timestamp(System.currentTimeMillis()));
						errorList.add(errorCalc);
					} else {
						fcList.add(entity);
					}
				}
			}

			// 持仓的货基标的
			List<TrustEntity> trustList = trustService.findTargetListByPid(assetPool.getOid());
			logger.info("========================== trustList: " + trustList.size());
			if (null != trustList && !trustList.isEmpty()) {
				jobCount += trustList.size();
				for (TrustEntity entity : trustList) {
					// 判断是否成立
					if ("STAND_UP".equals(entity.getTarget().getLifeState())) {
						// 判断是否到起息日
						Date incomeStartDate = entity.getTarget().getIncomeStartDate();
						Date incomeEndDate = entity.getTarget().getIncomeEndDate();
						if (null == incomeStartDate || null == incomeEndDate) {
							continue;
						}

						// 判断是否到起息日
						if (!DateUtil.ge(baseDate, incomeStartDate)) {
							continue;
						}
						// 判断是否到止息日
						if (!DateUtil.lt(baseDate, incomeEndDate)) {
							continue;
						}

						// 判断收益方式（amortized_cost：摊余成本法；book_value：账面价值法）
						if ("amortized_cost".equals(entity.getProfitType())) {
							if (null == entity.getTarget().getExpAror() || null == entity.getTarget().getContractDays()) {
								errorCalc = new ErrorCalc();
								jsonObj = new JSONObject();
								errorCalc.setOid(StringUtil.uuid());
								errorCalc.setTarget(entity.getTarget());
								errorCalc.setAssetPool(assetPool);
								if (null == entity.getTarget().getExpAror())
									jsonObj.put("ExpAror", "收益率为空");
								if (null == entity.getTarget().getContractDays())
									jsonObj.put("ContractDays", "合同年天数为空");
								errorCalc.setMessage(jsonObj);
								errorCalc.setOperator(assetPool.getOperator());
								errorCalc.setCreateTime(new Timestamp(System.currentTimeMillis()));
								errorList.add(errorCalc);
							} else {
								cxq_tcList.add(entity);
							}
						}
						// 逾期
					} else if ("OVER_TIME".equals(entity.getTarget().getLifeState())) {
						// 判断收益方式（amortized_cost：摊余成本法；book_value：账面价值法）
						if ("amortized_cost".equals(entity.getProfitType())) {
							if (null == entity.getTarget().getOverdueRate() || null == entity.getTarget().getContractDays()) {
								errorCalc = new ErrorCalc();
								jsonObj = new JSONObject();
								errorCalc.setOid(StringUtil.uuid());
								errorCalc.setTarget(entity.getTarget());
								errorCalc.setAssetPool(assetPool);
								if (null == entity.getTarget().getExpAror())
									jsonObj.put("OverdueRate", "逾期收益率为空");
								if (null == entity.getTarget().getContractDays())
									jsonObj.put("ContractDays", "合同年天数为空");
								errorCalc.setMessage(jsonObj);
								errorCalc.setOperator(assetPool.getOperator());
								errorCalc.setCreateTime(new Timestamp(System.currentTimeMillis()));
								errorList.add(errorCalc);
							} else {
								yq_tcList.add(entity);
							}
						}
					} else {
						// 判断是否在募集期
						if (!DateUtil.ge(baseDate, entity.getTarget().getCollectStartDate())) {
							continue;
						}
						if (!DateUtil.lt(baseDate, entity.getTarget().getCollectEndDate())) {
							continue;
						}

						if (null == entity.getTarget().getCollectIncomeRate() || null == entity.getTarget().getContractDays()) {
							errorCalc = new ErrorCalc();
							jsonObj = new JSONObject();
							errorCalc.setOid(StringUtil.uuid());
							errorCalc.setTarget(entity.getTarget());
							errorCalc.setAssetPool(assetPool);
							if (null == entity.getTarget().getCollectIncomeRate())
								jsonObj.put("CollectIncomeRate", "募集期收益率为空");
							if (null == entity.getTarget().getContractDays())
								jsonObj.put("ContractDays", "合同年天数为空");
							errorCalc.setMessage(jsonObj);
							errorCalc.setOperator(assetPool.getOperator());
							errorCalc.setCreateTime(new Timestamp(System.currentTimeMillis()));
							errorList.add(errorCalc);
						} else {
							mjq_tcList.add(entity);
						}

					}
				}
			}
			if (type.equals(AssetPoolCalc.EventType.SCHEDULE.toString())) {
				logger.info("=================定时任务准备执行 errorList:" + errorList.size());
				if (errorList.size() > 0) {
					assetPool.setScheduleState(AssetPoolEntity.schedule_wjs);
					logger.error("=================定时任务未执行，待数据补录===============");
					// throw new RuntimeException("=================定时任务未执行，待数据补录===============");
				} else {
					AssetPoolCalc calc = new AssetPoolCalc();
					calc.setOid(StringUtil.uuid());
					calc.setAssetPool(assetPool);

					fundProfit = this.calcFundProfit(calc, baseDate, fundProfit, fcList, fundCalcList/*, revenues*/);
					trustProfit = this.calcTrustProfitForCollect(calc, baseDate, trustProfit, mjq_tcList, trustCalcList, assetPool.getNonTradingDays());
					trustProfit = this.calcTrustProfitForDuration(calc, baseDate, trustProfit, cxq_tcList, trustCalcList, assetPool.getNonTradingDays());
					trustProfit = this.calcTrustProfitForOverdue(calc, baseDate, trustProfit, yq_tcList, trustCalcList, assetPool.getNonTradingDays());
					totalProfit = fundProfit.add(trustProfit).setScale(4, BigDecimal.ROUND_HALF_UP);

					calc.setCapital(assetPool.getScale().add(totalProfit).setScale(4, BigDecimal.ROUND_HALF_UP));
					calc.setProfit(totalProfit);
					calc.setBaseDate(baseDate);
					calc.setEventType(AssetPoolCalc.EventType.SCHEDULE);
					calc.setCreateTime(new Timestamp(System.currentTimeMillis()));

					poolCalcService.saveOne(calc);
					if (fundCalcList.size() > 0)
						fundCalcService.save(fundCalcList);
					if (trustCalcList.size() > 0)
						trustCalcService.save(trustCalcList);

					successCount = fundCalcList.size() + trustCalcList.size();
					
					assetPool.setConfirmProfit(totalProfit);
					assetPool.setFactProfit(totalProfit);
					assetPool.setScheduleState(AssetPoolEntity.schedule_yjs);
					assetPool.setBaseDate(baseDate);

					// 总估值
					valuation = assetPool.getScale().add(totalProfit).setScale(4, BigDecimal.ROUND_HALF_UP);
					historyService.createHistory(assetPool.getOid(), valuation, assetPool.getCalcBasis());
					// 重计算投资占比
					this.calcAssetPoolRate(assetPool, valuation, fundProfit, trustProfit);
				}
			} else if (errorList.size() == 0 || type.equals(AssetPoolCalc.EventType.USER_CALC.toString())) {
				AssetPoolCalc calc = new AssetPoolCalc();
				calc.setOid(StringUtil.uuid());
				calc.setAssetPool(assetPool);

				fundProfit = this.calcFundProfit(calc, baseDate, fundProfit, fcList, fundCalcList/*, revenues*/);
				trustProfit = this.calcTrustProfitForCollect(calc, baseDate, trustProfit, mjq_tcList, trustCalcList, assetPool.getNonTradingDays());
				trustProfit = this.calcTrustProfitForDuration(calc, baseDate, trustProfit, cxq_tcList, trustCalcList, assetPool.getNonTradingDays());
				trustProfit = this.calcTrustProfitForOverdue(calc, baseDate, trustProfit, yq_tcList, trustCalcList, assetPool.getNonTradingDays());
				totalProfit = fundProfit.add(trustProfit).setScale(4, BigDecimal.ROUND_HALF_UP);

				calc.setCapital(assetPool.getScale().add(totalProfit).setScale(4, BigDecimal.ROUND_HALF_UP));
				calc.setProfit(totalProfit);
				calc.setBaseDate(baseDate);
				calc.setEventType(AssetPoolCalc.EventType.USER_CALC);
				calc.setCreateTime(new Timestamp(System.currentTimeMillis()));

				poolCalcService.saveOne(calc);
				if (fundCalcList.size() > 0)
					fundCalcService.save(fundCalcList);
				if (trustCalcList.size() > 0)
					trustCalcService.save(trustCalcList);

				successCount = fundCalcList.size() + trustCalcList.size();

				assetPool.setConfirmProfit(totalProfit);
				assetPool.setFactProfit(totalProfit);
				assetPool.setScheduleState(AssetPoolEntity.schedule_bfjs);
				assetPool.setBaseDate(baseDate);

				// 总估值
				valuation = assetPool.getScale().add(totalProfit).setScale(4, BigDecimal.ROUND_HALF_UP);
				historyService.createHistory(assetPool.getOid(), valuation, assetPool.getCalcBasis());
				// 重计算投资占比
				this.calcAssetPoolRate(assetPool, valuation, fundProfit, trustProfit);
			} else {
				AssetPoolCalc calc = new AssetPoolCalc();
				calc.setOid(StringUtil.uuid());
				calc.setAssetPool(assetPool);
				calc.setBaseDate(baseDate);
				calc.setEventType(AssetPoolCalc.EventType.USER_NONE);
				assetPool.setScheduleState(AssetPoolEntity.schedule_drbjs);
				poolCalcService.saveOne(calc);

				successCount = 1;

				logger.info("=================定时任务默认录入一条初始化数据===============");
				// throw new RuntimeException("=================定时任务默认录入一条初始化数据===============");
			}
			// assetPool.setUpdateTime(new Timestamp(System.currentTimeMillis()));
			// assetPoolDao.save(assetPool);

			if (errorList.size() > 0) {
				errorCalcService.save(errorList);
			}

			// if (incomeList.size() > 0)
			// incomeService.incomeConfirm(assetPool.getOid(), incomeList);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Integer[] counts = new Integer[2];
		counts[0] = jobCount;
		counts[1] = successCount;
		return counts;
	}

	/**
	 * 计算现金管理工具每日收益
	 * 
	 * @param assetPoolCalc
	 * @param baseDate
	 * @param totalProfit
	 * @param fundList
	 * @param fundCalcList
	 * @param incomeList
	 */
	@Transactional
	public BigDecimal calcFundProfit(AssetPoolCalc assetPoolCalc, Date baseDate, BigDecimal totalProfit, List<FundEntity> fundList, List<FundCalc> fundCalcList/*,
			Map<String, CashToolRevenue> revenues*/) {
		// 当日收益
		BigDecimal dayProfit = BigDecimal.ZERO;
		if (null != fundList && !fundList.isEmpty()) {
			for (FundEntity entity : fundList) {
//				dayProfit = entity.getInterestAcount().multiply(revenues.get(entity.getCashTool().getOid()).getDailyProfit()).divide(BigDecimalUtil.NUM10000, 4, BigDecimal.ROUND_HALF_UP);
				dayProfit = entity.getInterestAcount().multiply(entity.getCashTool().getDailyProfit()).divide(BigDecimalUtil.NUM10000, 4, BigDecimal.ROUND_HALF_UP);
				entity.setAmount(entity.getAmount().add(dayProfit).setScale(4, BigDecimal.ROUND_HALF_UP));
				// 起息份额=起息份额+当日收益+（T-1）日未计息份额-（T-1）日赎回计息份额
				entity.setInterestAcount(entity.getInterestAcount().add(dayProfit).add(entity.getPurchaseAcount()).subtract(entity.getRedeemAcount()).setScale(4, BigDecimal.ROUND_HALF_UP));
				entity.setDailyProfit(dayProfit);
				entity.setTotalProfit(entity.getTotalProfit().add(dayProfit).setScale(4, BigDecimal.ROUND_HALF_UP));
				entity.setPurchaseAcount(BigDecimal.ZERO);
				entity.setRedeemAcount(BigDecimal.ZERO);

				FundCalc calc = new FundCalc();
				calc.setOid(StringUtil.uuid());
				calc.setFundEntity(entity);
				calc.setAssetPoolCalc(assetPoolCalc);
				calc.setCapital(entity.getAmount());
				calc.setInterest(calc.getInterest().add(dayProfit).setScale(4, BigDecimal.ROUND_HALF_UP));
				calc.setYield(entity.getCashTool().getDailyProfit());
				calc.setIncome(dayProfit);
				calc.setBaseDate(baseDate);
				calc.setCreateTime(new Timestamp(System.currentTimeMillis()));
				fundCalcList.add(calc);

				totalProfit = totalProfit.add(dayProfit).setScale(4, BigDecimal.ROUND_HALF_UP);

				// Income income = new Income(calc.getOid(), dayProfit, IncomeType.CASHTOOL);
				// incomeList.add(income);
			}
			fundService.save(fundList);
		}

		return totalProfit;
	}

	/**
	 * 计算募集期的信托标的-每日收益
	 * 
	 * @param assetPoolCalc
	 * @param baseDate
	 * @param totalProfit
	 * @param trustList
	 * @param trustCalcList
	 * @param incomeList
	 * @param days	非交易日天数
	 */
	@Transactional
	public BigDecimal calcTrustProfitForCollect(AssetPoolCalc assetPoolCalc, Date baseDate, BigDecimal totalProfit, List<TrustEntity> trustList, List<TrustCalc> trustCalcList, int days) {
		// 当日收益
		BigDecimal dayProfit = BigDecimal.ZERO;
		if (null != trustList && !trustList.isEmpty()) {
			for (TrustEntity entity : trustList) {
				dayProfit = entity.getInterestAcount().multiply(entity.getTarget().getCollectIncomeRate()).divide(new BigDecimal(entity.getTarget().getContractDays()), 4, BigDecimal.ROUND_HALF_UP);
				if (days > 0) {
					// 除非交易日外，还要加上当天的收益
					dayProfit = dayProfit.multiply(new BigDecimal(days + 1)).setScale(4, BigDecimal.ROUND_HALF_UP);
				}
				entity.setDailyProfit(dayProfit);
				entity.setTotalProfit(entity.getTotalProfit().add(dayProfit).setScale(4, BigDecimal.ROUND_HALF_UP));
				// 起息份额=起息份额+（T-1）日未计息份额-（T-1）日赎回计息份额
				entity.setInterestAcount(entity.getInterestAcount().add(entity.getPurchaseAcount()).subtract(entity.getRedeemAcount()).setScale(4, BigDecimal.ROUND_HALF_UP));
				entity.setPurchaseAcount(BigDecimal.ZERO);
				entity.setRedeemAcount(BigDecimal.ZERO);

				TrustCalc calc = new TrustCalc();
				calc.setOid(StringUtil.uuid());
				calc.setTrustEntity(entity);
				calc.setAssetPool(assetPoolCalc);
				calc.setCapital(entity.getInvestVolume());
				calc.setYield(entity.getTarget().getCollectIncomeRate());
				calc.setProfit(dayProfit);
				calc.setBaseDate(baseDate);
				calc.setCreateTime(new Timestamp(System.currentTimeMillis()));
				calc.setType("COLLECT");
				trustCalcList.add(calc);

				totalProfit = totalProfit.add(dayProfit).setScale(4, BigDecimal.ROUND_HALF_UP);

				// Income income = new Income(calc.getOid(), dayProfit, IncomeType.TARGET);
				// incomeList.add(income);
			}
			trustService.save(trustList);
		}

		return totalProfit;
	}

	/**
	 * 计算存续期的信托标的-每日收益
	 * 
	 * @param assetPoolCalc
	 * @param baseDate
	 * @param totalProfit
	 * @param trustList
	 * @param trustCalcList
	 * @param incomeList
	 * @param days	非交易日天数
	 */
	@Transactional
	public BigDecimal calcTrustProfitForDuration(AssetPoolCalc assetPoolCalc, Date baseDate, BigDecimal totalProfit, List<TrustEntity> trustList, List<TrustCalc> trustCalcList, int days) {
		// 当日收益
		BigDecimal dayProfit = BigDecimal.ZERO;
		if (null != trustList && !trustList.isEmpty()) {
			for (TrustEntity entity : trustList) {
				dayProfit = entity.getInterestAcount().multiply(entity.getTarget().getExpAror()).divide(new BigDecimal(entity.getTarget().getContractDays()), 4, BigDecimal.ROUND_HALF_UP);
				if (days > 0) {
					dayProfit = dayProfit.multiply(new BigDecimal(days + 1)).setScale(4, BigDecimal.ROUND_HALF_UP);
				}
				entity.setDailyProfit(dayProfit);
				entity.setTotalProfit(entity.getTotalProfit().add(dayProfit).setScale(4, BigDecimal.ROUND_HALF_UP));
				// 起息份额=起息份额+（T-1）日未计息份额-（T-1）日赎回计息份额
				entity.setInterestAcount(entity.getInterestAcount().add(entity.getPurchaseAcount()).subtract(entity.getRedeemAcount()).setScale(4, BigDecimal.ROUND_HALF_UP));
				entity.setPurchaseAcount(BigDecimal.ZERO);
				entity.setRedeemAcount(BigDecimal.ZERO);

				TrustCalc calc = new TrustCalc();
				calc.setOid(StringUtil.uuid());
				calc.setTrustEntity(entity);
				calc.setAssetPool(assetPoolCalc);
				calc.setCapital(entity.getInvestVolume());
				calc.setYield(entity.getTarget().getExpAror());
				calc.setProfit(dayProfit);
				calc.setBaseDate(baseDate);
				calc.setCreateTime(new Timestamp(System.currentTimeMillis()));
				calc.setType("DURATION");
				trustCalcList.add(calc);

				totalProfit = totalProfit.add(dayProfit).setScale(4, BigDecimal.ROUND_HALF_UP);

				// Income income = new Income(calc.getOid(), dayProfit, IncomeType.TARGET);
				// incomeList.add(income);
			}
			trustService.save(trustList);
		}

		return totalProfit;
	}

	/**
	 * 计算逾期的信托标的-每日收益
	 * 
	 * @param assetPoolCalc
	 * @param baseDate
	 * @param totalProfit
	 * @param trustList
	 * @param trustCalcList
	 * @param incomeList
	 * @param days	非交易日天数
	 */
	@Transactional
	public BigDecimal calcTrustProfitForOverdue(AssetPoolCalc assetPoolCalc, Date baseDate, BigDecimal totalProfit, List<TrustEntity> trustList, List<TrustCalc> trustCalcList, int days) {
		// 当日收益
		BigDecimal dayProfit = BigDecimal.ZERO;
		if (null != trustList && !trustList.isEmpty()) {
			for (TrustEntity entity : trustList) {
				dayProfit = entity.getInvestVolume().multiply(entity.getTarget().getOverdueRate()).divide(new BigDecimal(entity.getTarget().getContractDays()), 4, BigDecimal.ROUND_HALF_UP);
				if (days > 0) {
					dayProfit = dayProfit.multiply(new BigDecimal(days + 1)).setScale(4, BigDecimal.ROUND_HALF_UP);
				}
				entity.setDailyProfit(dayProfit);
				entity.setTotalProfit(entity.getTotalProfit().add(dayProfit).setScale(4, BigDecimal.ROUND_HALF_UP));

				TrustCalc calc = new TrustCalc();
				calc.setOid(StringUtil.uuid());
				calc.setTrustEntity(entity);
				calc.setAssetPool(assetPoolCalc);
				calc.setCapital(entity.getInvestVolume());
				calc.setYield(entity.getTarget().getExpAror());
				calc.setProfit(dayProfit);
				calc.setBaseDate(baseDate);
				calc.setCreateTime(new Timestamp(System.currentTimeMillis()));
				calc.setType("OVERTIME");
				trustCalcList.add(calc);

				totalProfit = totalProfit.add(dayProfit).setScale(4, BigDecimal.ROUND_HALF_UP);

				// Income income = new Income(calc.getOid(), dayProfit, IncomeType.TARGET);
				// incomeList.add(income);
			}
			trustService.save(trustList);
		}

		return totalProfit;
	}

	/**
	 * 更新资产池的偏离损益
	 * 
	 * @param form
	 */
	@Transactional
	public void updateDeviationValue(AssetPoolForm form, String operator) {
		AssetPoolEntity entity = assetPoolDao.findOne(form.getOid());
		// BigDecimal marketValue = form.getShares().multiply(form.getNav())
		// .setScale(4, BigDecimal.ROUND_HALF_UP);
		// marketValue = BigDecimalUtil.formatForMul10000(marketValue);
		// entity.setMarketValue(marketValue);
		// entity.setDeviationValue(marketValue.subtract(entity.getScale())
		// .setScale(4, BigDecimal.ROUND_HALF_UP));
		BigDecimal marketValue = form.getDeviationValue();
		marketValue = BigDecimalUtil.formatForMul10000(marketValue);
		entity.setDeviationValue(marketValue);
		entity.setOperator(operator);
		assetPoolDao.save(entity);
		// BigDecimal nscale = entity.getScale().add(
		// form.getDeviationValue().subtract(entity.getDeviationValue())
		// .setScale(4, BigDecimal.ROUND_HALF_UP));
		//
		// this.calcAssetPoolRate(entity, nscale, BigDecimal.ZERO, BigDecimal.ZERO);
	}

	/**
	 * 重计算资产池的投资占比
	 * 
	 * @param entity
	 * @param nscale
	 *            当前资产池估值
	 * @param fvalue
	 *            现金管理工具纠偏的差额
	 * @param tvalue
	 *            投资标的纠偏的差额
	 */
	public void calcAssetPoolRate(AssetPoolEntity entity, BigDecimal nscale, BigDecimal fvalue, BigDecimal tvalue) {
		if (nscale.compareTo(BigDecimal.ZERO) != 0) {
			// 原规模
			BigDecimal scale = entity.getScale();
			BigDecimal cashtoolRate = (entity.getCashtoolFactRate().multiply(scale).add(fvalue)).divide(nscale, 4, BigDecimal.ROUND_HALF_UP);
			BigDecimal targetRate = (entity.getTargetFactRate().multiply(scale).add(tvalue)).divide(nscale, 4, BigDecimal.ROUND_HALF_UP);
			entity.setScale(nscale);
			entity.setCashFactRate(BigDecimalUtil.NUM1.subtract(cashtoolRate).subtract(targetRate).setScale(4, BigDecimal.ROUND_HALF_UP));
			entity.setCashtoolFactRate(cashtoolRate);
			entity.setTargetFactRate(targetRate);
			entity.setUpdateTime(DateUtil.getSqlCurrentDate());
		} else {
			entity.setScale(nscale);
			entity.setCashFactRate(BigDecimalUtil.NUM1);
			entity.setCashtoolFactRate(BigDecimal.ZERO);
			entity.setTargetFactRate(BigDecimal.ZERO);
			entity.setUpdateTime(DateUtil.getSqlCurrentDate());
		}
		entity = this.calcFee(entity);

		// 重新初始化非交易日天数
		entity.setNonTradingDays(0);

		assetPoolDao.save(entity);
	}

	/**
	 * SPV累计计提费金
	 * 
	 * @param pid
	 *            资产池id
	 * @param fee
	 *            费金
	 * @param type
	 *            类型（申购：purchase；赎回：redeem）
	 *            当类型为申购，且费金正数；或者，当类型为赎回，且费金为负数，则为 费金计提
	 *            否则为 费金提取
	 */
	public void dualChargeFee(String pid, BigDecimal fee, String type) {
		if (fee.compareTo(BigDecimal.ZERO) != 0) {
			AssetPoolEntity entity = assetPoolDao.findOne(pid);
			if ((ConstantUtil.PURCHASE.equals(type) && fee.compareTo(BigDecimal.ZERO) > 0) || (ConstantUtil.REDEEM.equals(type) && fee.compareTo(BigDecimal.ZERO) < 0)) {
				entity.setCountintChargefee(entity.getCountintChargefee().add(fee).setScale(4, BigDecimal.ROUND_HALF_UP));
			} else {
				entity.setDrawedChargefee(entity.getDrawedChargefee().add(fee).setScale(4, BigDecimal.ROUND_HALF_UP));
			}
			assetPoolDao.save(entity);
		}
	}

	public void createAssetPoolHistorData() {

	}

	/**
	 * 投资标的-坏账核销
	 * 资产池估值对应减去该笔投资，现金不变，同时改变投资占比
	 * 
	 * @param pid
	 * @param volume
	 */
	public void updateInvestPlan(String pid, BigDecimal volume) {
		AssetPoolEntity entity = assetPoolDao.findOne(pid);
		BigDecimal scale = entity.getScale();
		BigDecimal fundVolume = entity.getCashtoolFactRate().multiply(scale).setScale(4, BigDecimal.ROUND_HALF_UP);
		BigDecimal trustVolume = entity.getTargetFactRate().multiply(scale).setScale(4, BigDecimal.ROUND_HALF_UP);
		scale = scale.subtract(volume).setScale(4, BigDecimal.ROUND_HALF_UP);
		entity.setCashtoolFactRate(fundVolume.divide(scale, 4, BigDecimal.ROUND_HALF_UP));
		trustVolume = trustVolume.subtract(volume).setScale(4, BigDecimal.ROUND_HALF_UP);
		entity.setTargetFactRate(trustVolume.divide(scale, 4, BigDecimal.ROUND_HALF_UP));
		entity.setCashFactRate(new BigDecimal(1).subtract(entity.getCashtoolFactRate().setScale(5)).subtract(entity.getTargetFactRate()).setScale(4, BigDecimal.ROUND_HALF_UP));
		entity.setScale(scale);

		assetPoolDao.save(entity);
	}

	/**
	 * 重新计算资产池的投资占比
	 * 
	 * @param entity
	 *            资产池对象
	 * @param volume
	 *            发生的投资标的减值
	 * @param nav
	 *            发生的资产增值（默认为增值，减值则传入负值）
	 * @param type
	 *            标的类型（fund：现金类管理工具；target：信托标的）
	 */
	public void reCalcTargetRate(AssetPoolEntity entity, BigDecimal volume, BigDecimal nav, String type) {
		BigDecimal scale = entity.getScale();
		BigDecimal fundVolume = entity.getCashtoolFactRate().multiply(scale).setScale(4, BigDecimal.ROUND_HALF_UP);
		BigDecimal trustVolume = entity.getTargetFactRate().multiply(scale).setScale(4, BigDecimal.ROUND_HALF_UP);
		scale = scale.add(nav).setScale(4, BigDecimal.ROUND_HALF_UP);
		if (FundEntity.FUND.equals(type)) {
			fundVolume = fundVolume.subtract(volume).setScale(4, BigDecimal.ROUND_HALF_UP);
		} else {
			trustVolume = trustVolume.subtract(volume).setScale(4, BigDecimal.ROUND_HALF_UP);
		}
		entity.setCashtoolFactRate(fundVolume.divide(scale, 4, BigDecimal.ROUND_HALF_UP));
		entity.setTargetFactRate(trustVolume.divide(scale, 4, BigDecimal.ROUND_HALF_UP));
		entity.setCashFactRate(BigDecimalUtil.NUM1.subtract(entity.getCashtoolFactRate()).subtract(entity.getTargetFactRate()).setScale(4, BigDecimal.ROUND_HALF_UP));
		entity.setScale(scale);

		assetPoolDao.save(entity);
	}

	/**
	 * 申购标的-重新计算资产池的投资占比
	 * 
	 * @param entity
	 *            资产池对象
	 * @param volume
	 *            发生的投资标的持仓变化
	 * @param type
	 *            标的类型（fund：现金类管理工具；target：信托标的）
	 */
	public void reCalcInvestRateForPurchase(AssetPoolEntity entity, BigDecimal volume, String type) {
		BigDecimal scale = entity.getScale();
		BigDecimal fundVolume = entity.getCashtoolFactRate().multiply(scale).setScale(4, BigDecimal.ROUND_HALF_UP);
		BigDecimal trustVolume = entity.getTargetFactRate().multiply(scale).setScale(4, BigDecimal.ROUND_HALF_UP);
		if (FundEntity.FUND.equals(type)) {
			fundVolume = fundVolume.add(volume).setScale(4, BigDecimal.ROUND_HALF_UP);
		} else {
			trustVolume = trustVolume.add(volume).setScale(4, BigDecimal.ROUND_HALF_UP);
		}
		entity.setCashtoolFactRate(fundVolume.divide(scale, 4, BigDecimal.ROUND_HALF_UP));
		entity.setTargetFactRate(trustVolume.divide(scale, 4, BigDecimal.ROUND_HALF_UP));
		entity.setCashFactRate(BigDecimalUtil.NUM1.subtract(entity.getCashtoolFactRate()).subtract(entity.getTargetFactRate()).setScale(4, BigDecimal.ROUND_HALF_UP));
		entity.setScale(scale);

		assetPoolDao.save(entity);
	}

	/**
	 * 赎回标的-重新计算资产池的投资占比
	 * 
	 * @param entity
	 *            资产池对象
	 * @param volume
	 *            发生的投资标的持仓变化
	 * @param nav
	 *            发生的资产增值（默认为增值，减值则传入负值）
	 * @param type
	 *            标的类型（fund：现金类管理工具；target：信托标的）
	 */
	public void reCalcInvestRateForRedeem(AssetPoolEntity entity, BigDecimal volume, BigDecimal nav, String type) {
		BigDecimal scale = entity.getScale();
		BigDecimal fundVolume = entity.getCashtoolFactRate().multiply(scale).setScale(4, BigDecimal.ROUND_HALF_UP);
		BigDecimal trustVolume = entity.getTargetFactRate().multiply(scale).setScale(4, BigDecimal.ROUND_HALF_UP);
		scale = scale.add(nav).setScale(4, BigDecimal.ROUND_HALF_UP);
		if (FundEntity.FUND.equals(type)) {
			fundVolume = fundVolume.subtract(volume).setScale(4, BigDecimal.ROUND_HALF_UP);
		} else {
			trustVolume = trustVolume.subtract(volume).setScale(4, BigDecimal.ROUND_HALF_UP);
		}
		entity.setCashtoolFactRate(fundVolume.divide(scale, 4, BigDecimal.ROUND_HALF_UP));
		entity.setTargetFactRate(trustVolume.divide(scale, 4, BigDecimal.ROUND_HALF_UP));
		entity.setCashFactRate(BigDecimalUtil.NUM1.subtract(entity.getCashtoolFactRate()).subtract(entity.getTargetFactRate()).setScale(4, BigDecimal.ROUND_HALF_UP));
		entity.setScale(scale);

		assetPoolDao.save(entity);
	}
	
	/**
	 * 计算应付费金
	 * @param entity
	 * @return
	 */
	private AssetPoolEntity calcFee(AssetPoolEntity entity) {
		/**
		 * 应付托管费
		 * 算法：昨日资产净值 * 托管费率 / 费用计算基础
		 * 费用日累加
		 */
		BigDecimal trusteeFee = entity.getNetValue().multiply(entity.getTrusteeRate()).divide(new BigDecimal(entity.getCalcBasis()), 4, BigDecimal.ROUND_HALF_UP);
		entity.setTrusteeFee(entity.getTrusteeFee().add(trusteeFee).setScale(4, BigDecimal.ROUND_HALF_UP));
		
		/**
		 * 应付管理费
		 * 算法：昨日资产净值 * 托管费率 / 费用计算基础
		 * 费用日累加
		 */
		BigDecimal manageFee = entity.getNetValue().multiply(entity.getManageRate()).divide(new BigDecimal(entity.getCalcBasis()), 4, BigDecimal.ROUND_HALF_UP);
		entity.setManageFee(entity.getManageFee().add(manageFee).setScale(4, BigDecimal.ROUND_HALF_UP));
		
		/**
		 * 资产净值
		 * 算法：今日资产总值 - 应付费金（应付托管费 + 应付管理费）
		 */
		entity.setNetValue(entity.getScale().subtract(entity.getTrusteeFee()).subtract(entity.getManageFee()).setScale(4, BigDecimal.ROUND_HALF_UP));
		
		return entity;
	}
	/**
	 * 
	 * @author yihonglei
	 * @Title: queryBaseAssetCode
	 * @Description:判断基础资产池编码是否已经存在
	 * @param baseAssetCode
	 * @return int
	 * @date 2017年5月26日 上午11:33:14
	 * @since  1.0.0
	 */
	public int queryBaseAssetCode(String baseAssetCode) {
		
		return assetPoolDao.queryBaseAssetCode(baseAssetCode);
	}
	
	/**
	 * 结算数据导出界面查询 资产池-产品汇总信息
	 * @param page
	 * @param rows
	 * @param assetPoolName
	 * @param productName
	 * @param productStatus
	 * @param raiseTimeBegin
	 * @param raiseTimeEnd
	 * @param repayTimeBegin
	 * @param repayTimeEnd
	 * @return
	 */
	public ResponseEntity<AssetPoolProductVoResp> findAssetPoolAndProduct(int page,
			int rows,
			String assetPoolName,
			String productName,
			String productStatus,
			String raiseTimeBegin,
			String raiseTimeEnd,
			String repayTimeBegin,
			String repayTimeEnd){
		
		logger.info("查询参数：page:{},rows:{},assetPoolName:{},productName:{},productStatus:{},"
				+ "raiseTimeBegin:{},raiseTimeEnd:{},repayTimeBegin:{},repayTimeEnd:{}",
				page,rows,assetPoolName,productName,productStatus,raiseTimeBegin,raiseTimeEnd,repayTimeBegin,repayTimeEnd);
		assetPoolName = "%"+assetPoolName+"%";
		productName = "%"+productName+"%";
		
		AssetPoolProductVoResp resp = new AssetPoolProductVoResp();
		ResponseEntity<AssetPoolProductVoResp> res = new ResponseEntity<>(resp,HttpStatus.OK);
		// 明细
		Object[] details = assetPoolDao.findAssetPoolAndProduct(assetPoolName, productName, productStatus, raiseTimeBegin, raiseTimeEnd, repayTimeBegin, repayTimeEnd, (page-1)*rows, rows);
		logger.info("明细查询：{}",details);
		for(Object detail : details){
			Object[] product = (Object[])detail;
			AssetPoolProductVo vo = new AssetPoolProductVo();
			vo.setProductOid(StringStr(product[0]));
			vo.setProductCode(StringStr(product[1]));
			vo.setProductName(StringStr(product[2]));
			vo.setProductType(StringStr(product[3]));
			vo.setDurationPeriodDays(StringStr(product[4]));
			vo.setCollectedVolume(StringStr(product[5]));
			vo.setAssetPoolName(StringStr(product[6]));
			vo.setNoPayInvest(StringStr(product[7]));
			vo.setPayInvest(StringStr(product[8]));
			vo.setRaiseStartDate(StringStr(product[9]));
			vo.setRepayDate(StringStr(product[10]));
			vo.setRepayLoanStatus(StringStr(product[11]));
			vo.setRepayInterestStatus(StringStr(product[12]));
			vo.setProductCash(StringStr(product[13]));
			vo.setProductInvest(StringStr(product[14]));
			vo.setProductIncome(StringStr(product[15]));
			vo.setProductState(StringStr(product[16]));
			vo.setCreateTime(StringStr(product[17]));
			vo.setCreator(StringStr(product[18]));
			resp.getRows().add(vo);
		}
		
		// 汇总信息
		Object[] headers = assetPoolDao.findAssetPoolAndProduct(assetPoolName, productName, productStatus, raiseTimeBegin, raiseTimeEnd, repayTimeBegin, repayTimeEnd);
		Object[] obj = (Object[])headers[0];
		resp.setTotal(Long.parseLong(StringNum(obj[0])));
		resp.setPage(page);
		resp.setRow(rows);
		resp.setTotalPage((int)(resp.getTotal() / resp.getRow() + 1));
		resp.setTotalCollectedVolume(new BigDecimal(StringNum(obj[1])));
		resp.setTotalNoPayInvest(new BigDecimal(StringNum(obj[2])));
		resp.setTotalPayInvest(new BigDecimal(StringNum(obj[3])));
		resp.setTotalCash(new BigDecimal(StringNum(obj[4])));
		resp.setTotalInvest(new BigDecimal(StringNum(obj[5])));
		resp.setTotalIncome(new BigDecimal(StringNum(obj[6])));

		
		return res;
	}
	
	/**
	 * 将输入对象转换为字符串
	 * 空对象转为 “”
	 * @param obj
	 * @return
	 */
	public String StringStr(Object obj){
		if(obj==null||obj.equals("null")){
			return "";
		}else{
			return String.valueOf(obj);
		}
	}
	
	/**
	 * 将输入对象转换为字符串
	 * 空对象转为“0”
	 * @param obj
	 * @return
	 */
	private String StringNum(Object obj){
		if(obj == null || obj.equals("null")){
			return "0";
		}else{
			return String.valueOf(obj);
		}
	}
	
	public void assetProductOrderDown(HttpServletResponse response, String oids){
		try {
			String name = "产品订单明细导出.csv";
			String realPath = "../Temp/";
			String[] oidList = oids.split(",");
			List<String> oidCollection = new ArrayList<String>(); 
			for(String oid : oidList){
				oidCollection.add(oid);
			}
			List<List<String>> data = generateAssetProductOrderData(oidCollection); 
			List<String> header = generateAssetProductOrderHeader();
			
			File f = new File(realPath);
			if (!f.exists()) {
				f.mkdirs();
			}
			File file = new File(f, name);
			if(!file.exists()){
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			File filePathName = new File(realPath + name);
			CsvExport.exportCsv(filePathName, header, data);
			download(realPath + name, response);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private List<List<String>> generateAssetProductOrderData(List<String> oidCollection){
		List<Object[]> list = assetPoolDao.findAssetProductOrderList(oidCollection);
		List<List<String>> result = new ArrayList<>();
		for(Object[] objs : list){
			List<String> line = new ArrayList<>();
			for(Object obj : objs){
				line.add(StringStr(obj));
			}
			result.add(line);
		}
		return result;
	}
	
	private List<String> generateAssetProductOrderHeader(){
		List<String> header = new ArrayList<>();
		header.add("产品名称");
		header.add("订单号");
		header.add("手机号");
		header.add("订单金额");
		header.add("还款本金");
		header.add("还款利息");
		header.add("还款总金额");
		header.add("还款时间");
		header.add("产品编号");
		header.add("操作人");
		return header;
	}
	
	private void download(String path, HttpServletResponse response){
		try {
			File file = new File(path);
			String filename = file.getName();
			InputStream fis = new BufferedInputStream(new FileInputStream(path));
			byte[] buffer = new byte[fis.available()];
			fis.read(buffer);
			fis.close();
			response.reset();
			response.setHeader("Content-Disposition",
					"attachment;filename="+new String(filename.getBytes("UTF-8"), "ISO-8859-1"));
			response.addHeader("Content-Length", "" + file.length());
			OutputStream toClient = new BufferedOutputStream(response.getOutputStream());
			response.setContentType("application/vnd.ms-excel;charset=UTF-8");
			toClient.write(buffer);
			toClient.flush();
			toClient.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
//		String oids = "9216142f32a54e75a63bb2fe2312f045,40b4f7a2fcaf47dc951845bf600afc05,59c2efd35c744d38b080287224b248a3,9d62d934860349408e5e383863335fce,812192121c364d3e962bdc849433fddf,ed9aa92fac3543758bbc36e3efa751a7,cb1ebfe91c784df7aeb0f526f1d9dd35,d5a709fe037b49189eaf5c92d4eb6c78,f83782eaff044cecbf8399fd1b0dbb20,999d40431c44432a9f5316f3108ab88b";
		String oids = "9216142f32a54e75a63bb2fe2312f045";
		String[] liStrings = oids.split(",");
		String newStr = "(";
		for(String oid : liStrings){
			newStr = newStr + "'" + oid + "',";
		}
		newStr = newStr.substring(0, newStr.length() -1);
		newStr += ")";
		System.out.println(newStr);
	}
}
