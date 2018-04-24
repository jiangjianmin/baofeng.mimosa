package com.guohuai.mmp.publisher.hold;

import com.alibaba.fastjson.JSON;
import com.guohuai.ams.companyScatterStandard.ElectronicSignatureRelation;
import com.guohuai.ams.companyScatterStandard.ElectronicSignatureRelationEmDao;
import com.guohuai.ams.dict.Dict;
import com.guohuai.ams.duration.assetPool.AssetPoolEntity;
import com.guohuai.ams.duration.assetPool.AssetPoolService;
import com.guohuai.ams.duration.fact.income.IncomeAllocate;
import com.guohuai.ams.duration.fact.income.IncomeAllocateService;
import com.guohuai.ams.label.LabelEnum;
import com.guohuai.ams.label.LabelService;
import com.guohuai.ams.product.*;
import com.guohuai.ams.product.reward.ProductIncomeReward;
import com.guohuai.ams.product.reward.ProductIncomeRewardCacheService;
import com.guohuai.ams.product.reward.ProductIncomeRewardSnapshot;
import com.guohuai.ams.product.reward.ProductIncomeRewardSnapshotService;
import com.guohuai.ams.productPackage.ProductPackage;
import com.guohuai.ams.productPackage.ProductPackageDao;
import com.guohuai.basic.cardvo.config.CardTypes;
import com.guohuai.cache.CacheConfig;
import com.guohuai.cache.entity.HoldCacheEntity;
import com.guohuai.cache.entity.ProductCacheEntity;
import com.guohuai.cache.service.CacheHoldService;
import com.guohuai.cache.service.CacheProductService;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.BigDecimalUtil;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.DecimalUtil;
import com.guohuai.component.util.StringUtil;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.component.web.view.Pages;
import com.guohuai.component.web.view.PagesRep;
import com.guohuai.component.web.view.RowsRep;
import com.guohuai.file.File;
import com.guohuai.file.FileResp;
import com.guohuai.file.FileService;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountEntity;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountService;
import com.guohuai.mmp.investor.tradeorder.*;
import com.guohuai.mmp.job.lock.JobLockEntity;
import com.guohuai.mmp.job.lock.JobLockService;
import com.guohuai.mmp.job.log.JobLogEntity;
import com.guohuai.mmp.job.log.JobLogFactory;
import com.guohuai.mmp.job.log.JobLogService;
import com.guohuai.mmp.platform.StaticProperties;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountDao;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountEntity;
import com.guohuai.mmp.publisher.investor.InterestFormula;
import com.guohuai.mmp.publisher.investor.levelincome.LevelIncomeRep;
import com.guohuai.mmp.publisher.investor.levelincome.LevelIncomeService;
import com.guohuai.mmp.publisher.product.client.ProductClientService;
import com.guohuai.mmp.publisher.product.client.ProductCurrentDetailResp;
import com.guohuai.mmp.serialtask.*;
import com.guohuai.mmp.sys.SysConstant;
import com.guohuai.operate.api.AdminSdk;
import com.guohuai.operate.api.objs.admin.AdminObj;
import com.guohuai.tuip.api.TulipSdk;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;


/**
 * 持有人手册
 */
@Service
@Transactional
public class PublisherHoldService {

	private static final Logger logger = LoggerFactory.getLogger(PublisherHoldService.class);

	@Autowired
	private PublisherHoldDao publisherHoldDao;
	@Autowired
	private ProductService productService;
	@Autowired
	private AssetPoolService assetPoolService;
	@Autowired
	private PublisherHoldServiceNew newService;
	@Autowired
	private InvestorBaseAccountService investorBaseAccountService;
	@Autowired
	private PublisherBaseAccountDao publisherBaseAccountDao;
	@Autowired
	private JobLockService jobLockService;
	@Autowired
	private LevelIncomeService levelIncomeService;
	@Autowired
	private IncomeAllocateService incomeAllocateService;
	@Autowired
	private InvestorTradeOrderService investorTradeOrderService;
	@Autowired
	private CacheHoldService cacheHoldService;
	@Autowired
	private CacheProductService cacheProductService;
	@Autowired
	private SerialTaskService serialTaskService;
	@Autowired
	private SerialTaskRequireNewService serialTaskRequireNewService;
	@Autowired
	private JobLogService jobLogService;
	@Autowired
	private PublisherHoldServiceNew publisherHoldServiceNew;
	@Autowired
	private LabelService labelService;
	@Autowired
	private ProductPackageDao productPackageDao;
	@Autowired
	private ProductDao productDao;
	@Autowired
	private ProductIncomeRewardCacheService incomeRewardCacheService;
	@Autowired
	private OrderDateService orderDateService;
	@Autowired
	private ProductIncomeRewardSnapshotService incomeRewardSnapshotService;
	@Autowired
	private CacheConfig cacheConfig;
	@Autowired
	private ProductClientService productClientService;
	@Autowired
	private FileService fileService;
	@Autowired
	private AdminSdk adminSdk;
	@Autowired
	private TulipSdk tulipSdk;
	@Autowired
	private PublisherHoldEMDao publisherHoldEMDao;
	private InvestorInvestTradeOrderService investorInvestTradeOrderService;
	@Autowired
	private InvestorOpenCycleDao investorOpenCycleDao;
	@Autowired
	ElectronicSignatureRelationEmDao electronicSignatureRelationEmDao;
	/**
	 * 投资
	 */
	public PublisherHoldEntity invest(InvestorTradeOrderEntity orderEntity) {
		PublisherHoldEntity hold = this.publisherHoldDao
				.findByInvestorBaseAccountAndProduct(orderEntity.getInvestorBaseAccount(), orderEntity.getProduct());

		Product product = orderEntity.getProduct();
		String productType = Optional.ofNullable(product).map(Product::getType).map(Dict::getOid).orElse("");

		//合仓
		if (null == hold) {
			hold = new PublisherHoldEntity();
			hold.setProduct(product); // 所属理财产品
			hold.setPublisherBaseAccount(orderEntity.getPublisherBaseAccount()); // 所属发行人
			hold.setInvestorBaseAccount(orderEntity.getInvestorBaseAccount()); // 所属投资人
			hold.setAssetPool(product.getAssetPool());
			hold.setTotalVolume(orderEntity.getOrderVolume()); // 总份额
			hold.setToConfirmInvestVolume(orderEntity.getOrderVolume()); // 待确认份额
			hold.setAccountType(PublisherHoldEntity.PUBLISHER_accountType_INVESTOR);
			hold.setRedeemableHoldVolume(BigDecimal.ZERO);// 可赎回份额
			hold.setLockRedeemHoldVolume(BigDecimal.ZERO);// 赎回锁定份额
			hold.setAccruableHoldVolume(BigDecimal.ZERO);
			hold.setValue(orderEntity.getOrderAmount()); // 最新市值
			hold.setHoldStatus(PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_toConfirm);
			if (Objects.equals(Product.TYPE_Producttype_03,productType)){
				hold.setExpectIncome(orderEntity.getExpectIncome());
			}else{
				hold.setExpectIncome(orderEntity.getExpectIncome());
				hold.setExpectIncomeExt(orderEntity.getExpectIncomeExt());
			}

			hold.setDayInvestVolume(orderEntity.getOrderVolume());
			hold.setTotalInvestVolume(orderEntity.getOrderVolume());
			hold.setLatestOrderTime(orderEntity.getOrderTime());
			hold.setMaxHoldVolume(orderEntity.getOrderVolume());

			this.saveEntity(hold);
			/** 统计产品投资人数和投资次数 */
			this.productService.updatePurchasePeopleNumAndPurchaseNum(product.getOid());
		} else {

			hold.setTotalVolume(hold.getTotalVolume().add(orderEntity.getOrderVolume())); // 总份额
			hold.setToConfirmInvestVolume(hold.getToConfirmInvestVolume().add(orderEntity.getOrderVolume())); // 待确认份额
			hold.setTotalInvestVolume(hold.getTotalInvestVolume().add(orderEntity.getOrderVolume()));
			hold.setDayInvestVolume(hold.getDayInvestVolume().add(orderEntity.getOrderVolume()));
			hold.setValue(hold.getValue().add(orderEntity.getOrderAmount()));
			//2018 4 10 yujianlong 处理03产品预期收益
			if (Objects.equals(Product.TYPE_Producttype_03,productType)){
				hold.setExpectIncome(hold.getExpectIncome().add(orderEntity.getExpectIncome()));

//				hold.setExpectIncomeExt(hold.getExpectIncomeExt().add(orderEntity.getExpectIncomeExt()));
			}else{
				hold.setExpectIncome(InterestFormula.simple(hold.getTotalInvestVolume(),
						orderEntity.getProduct().getExpAror(), orderEntity.getProduct().getIncomeCalcBasis(), orderEntity.getProduct().getDurationPeriodDays()));

				hold.setExpectIncomeExt(InterestFormula.simple(hold.getTotalInvestVolume(),
						orderEntity.getProduct().getExpArorSec(), orderEntity.getProduct().getIncomeCalcBasis(), orderEntity.getProduct().getDurationPeriodDays()));
			}
			hold.setLatestOrderTime(orderEntity.getOrderTime());
			hold.setMaxHoldVolume(hold.getMaxHoldVolume().add(orderEntity.getOrderVolume()));
			saveEntity(hold);

			/** 统计产品投资次数 */
			this.productService.updatePurchaseNum(product.getOid());
		}

		return hold;
	}

	public PublisherHoldEntity writerOffOrder(InvestorTradeOrderEntity orderEntity, String batchNo) {
		PublisherHoldEntity hold = this.publisherHoldDao
				.findByInvestorBaseAccountAndProduct(orderEntity.getInvestorBaseAccount(), orderEntity.getProduct());
		this.publisherHoldDao.writerOffOrder(hold.getOid(), orderEntity.getProduct().getNetUnitShare(),
				orderEntity.getOrderVolume());

		// 同步缓存数据
//		this.cacheHoldService.invest(orderEntity, batchNo);

		return hold;
	}
	/**
	 *
	 * @param tradeOrder
	 * @return
	 */


//	/**
//	 * 赎回锁定
//	 * 
//	 * @param tradeOrder
//	 */
//	
//	public void redeemLock(InvestorTradeOrderEntity tradeOrder) {
//
//		if (this.publisherHoldDao.redeemLock(tradeOrder.getOrderVolume(), tradeOrder.getInvestorBaseAccount(),
//				tradeOrder.getProduct()) <= 0) {
//			// error.define[20004]=赎回锁定份额异常(CODE:20004)
//			throw AMPException.getException(20004);
//		}
//	}

	public void redeemDayRules(InvestorTradeOrderEntity orderEntity) {

		// 产品单人单日赎回上限等于0时，表示无上限
		if (DecimalUtil.isGoRules(orderEntity.getProduct().getSingleDailyMaxRedeem())) {

			//-------------------------超级用户--------------2017.04.19-----
			if(investorBaseAccountService.isSuperMan(orderEntity)){
				int i = this.publisherHoldDao.redeem4DayRedeemVolumeSuperAccount(orderEntity.getOrderVolume(),
						orderEntity.getInvestorBaseAccount(), orderEntity.getProduct());
				if (i < 1) {
					// error.define[30032]=超过产品单人单日赎回上限(CODE:30032)
					throw AMPException.getException(30032);
				}
			}else{
				int i = this.publisherHoldDao.redeem4DayRedeemVolume(orderEntity.getOrderVolume(),
						orderEntity.getInvestorBaseAccount(), orderEntity.getProduct(),
						orderEntity.getProduct().getSingleDailyMaxRedeem());
				if (i < 1) {
					// error.define[30032]=超过产品单人单日赎回上限(CODE:30032)
					throw AMPException.getException(30032);
				}
			}
			//-------------------------超级用户--------------2017.04.19-----

		}
		if (DecimalUtil.isGoRules(orderEntity.getProduct().getSingleDayRedeemCount())) {

			//-------------------------超级用户--------------2017.04.19-----
			if(investorBaseAccountService.isSuperMan(orderEntity)){
				int i = this.publisherHoldDao.updateDayRedeemCountSuperAccount(orderEntity.getProduct().getOid(),orderEntity.getInvestorBaseAccount().getOid());
				if (i < 1) {
					throw new AMPException("超过单日赎回次数");
				}
			}else{
				int i = this.publisherHoldDao.updateDayRedeemCount(orderEntity.getProduct().getSingleDayRedeemCount(),orderEntity.getProduct().getOid(),orderEntity.getInvestorBaseAccount().getOid());
				if (i < 1) {
					throw new AMPException("超过单日赎回次数");
				}
			}
		}
	}

	/**
	 * 赎回
	 */
	public FlatWareTotalRep normalRedeem(InvestorTradeOrderEntity orderEntity) {
		String holdStatus = getFlatWareHoldStatus(orderEntity);
		// 合仓处理
		if (!(InvestorTradeOrderEntity.TRADEORDER_orderType_normalRedeem.equals(orderEntity.getOrderType())
				|| InvestorTradeOrderEntity.TRADEORDER_orderType_incrementRedeem.equals(orderEntity.getOrderType()))) {
			int i = this.publisherHoldDao.normalRedeem(orderEntity.getOrderVolume(),
					orderEntity.getProduct().getNetUnitShare(), orderEntity.getInvestorBaseAccount().getOid(),
					orderEntity.getProduct().getOid(), holdStatus);
			if (i < 1) {
				// error.define[20005]=赎回份额异常(CODE:20005)
				throw AMPException.getException(20005);
			}
		}
		// 分仓处理
		FlatWareTotalRep rep = this.investorTradeOrderService.flatWare(orderEntity);
		return rep;
	}

	/**
	 * 赎回
	 */
	public FlatWareTotalRep plusNormalRedeem(InvestorTradeOrderEntity orderEntity) {
		// 分仓处理
		FlatWareTotalRep rep = this.investorTradeOrderService.flatWare(orderEntity);
		return rep;
	}

	/**
	 * 提现失败回滚用户持有份额
	 * @param orderCode
	 */
	@Transactional(value = TxType.REQUIRES_NEW)
	public void normalRedeemFailed(String orderCode) {
		InvestorTradeOrderEntity orderEntity = investorTradeOrderService.findByOrderCode(orderCode);
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_normalRedeem.equals(orderEntity.getOrderType())
				|| InvestorTradeOrderEntity.TRADEORDER_orderType_incrementRedeem.equals(orderEntity.getOrderType())) {
			int i = this.publisherHoldDao.normalRedeemFailed(orderEntity.getOrderVolume(),
					orderEntity.getProduct().getNetUnitShare(), orderEntity.getInvestorBaseAccount().getOid(),
					orderEntity.getProduct().getOid());
		}
	}

	public String getFlatWareHoldStatus(InvestorTradeOrderEntity orderEntity) {
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_cash.equals(orderEntity.getOrderType())) {
			return PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_closed;
		}
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_cashFailed.equals(orderEntity.getOrderType())) {
			return PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_refunded;
		}
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_normalRedeem.equals(orderEntity.getOrderType())
			|| InvestorTradeOrderEntity.TRADEORDER_orderType_incrementRedeem.equals(orderEntity.getOrderType())) {
			return PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_holding;
		}
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_specialRedeem.equals(orderEntity.getOrderType()) || InvestorTradeOrderEntity.TRADEORDER_orderType_noPayRedeem.equals(orderEntity.getOrderType())) {
			return PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_holding;
		}
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_bfPlusRedeem.equals(orderEntity.getOrderType())){
            return PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_holding;
        }
		throw new AMPException("订单类型异常");

	}

	public void expGoldRedeem(InvestorTradeOrderEntity orderEntity) {
		// 合仓处理
		int i = this.publisherHoldDao.expGoldRedeem(orderEntity.getOrderVolume(),
				orderEntity.getProduct().getNetUnitShare(), orderEntity.getInvestorBaseAccount().getOid(),
				orderEntity.getProduct().getOid(), orderEntity.getTotalIncome());
		if (i < 1) {
			// error.define[20005]=赎回份额异常(CODE:20005)
			throw AMPException.getException(20005);
		}
	}


	/**
	 * 活期废单：扣除总份额和投资待确认份额
	 */
	public void abandon4T0Invest(InvestorTradeOrderEntity orderEntity) {

		int i = this.publisherHoldDao.abandon4T0Invest(orderEntity.getOrderVolume(), orderEntity.getInvestorBaseAccount(),
				orderEntity.getProduct());
		if (i < 1) {
			// error.define[20017]=废单份额异常(CODE:20017)
			throw AMPException.getException(20017);
		}
	}

	/**
	 * 定期废单
	 */
	public void abandon4TnInvest(InvestorTradeOrderEntity orderEntity) {

		int i = this.publisherHoldDao.abandon4TnInvest(orderEntity.getOrderVolume(), orderEntity.getInvestorBaseAccount(),
				orderEntity.getProduct());
		if (i < 1) {
			// error.define[20017]=废单份额异常(CODE:20017)
			throw AMPException.getException(20017);
		}
	}

	public int redeem4Refuse(InvestorTradeOrderEntity orderEntity) {
		// 暂时在份额确认之前不能再赎回，直接从锁定份额里面扣除就可以了
		int i = this.publisherHoldDao.redeem4Refuse(orderEntity.getOrderVolume(), orderEntity.getInvestorBaseAccount(),
				orderEntity.getProduct());
		if (i < 1) {
			// error.define[20017]=废单份额异常(CODE:20017)
			throw AMPException.getException(20017);
		}
		return i;
	}

	public void redeem4RefuseOfDayRedeemVolume(InvestorTradeOrderEntity tradeOrder) {
		// 单人单日产品赎回上限为0，表示无上限
		if (null != tradeOrder.getProduct().getSingleDailyMaxRedeem()
				&& tradeOrder.getProduct().getSingleDailyMaxRedeem().compareTo(BigDecimal.ZERO) != 0) {
			int i = this.publisherHoldDao.redeem4RefuseOfDayRedeemVolume(tradeOrder.getOrderVolume(),
					tradeOrder.getInvestorBaseAccount(), tradeOrder.getProduct());
			if (i < 1) {
				// error.define[30037]=废赎回单之单日最大赎回份额异常(CODE:30037)
				throw AMPException.getException(30037);
			}
		}
	}

	public void invest4AbandonOfDayInvestVolume(InvestorTradeOrderEntity orderEntity) {
		int i = this.publisherHoldDao.invest4AbandonOfDayInvestVolume(orderEntity.getPublisherHold().getOid(),
				orderEntity.getOrderVolume());
			if (i < 1) {
				// error.define[30037]=废赎回单之单日最大赎回份额异常(CODE:30037)
				throw AMPException.getException(30037);
			}
	}


	public void unlockRedeem() {

		if (this.jobLockService.getRunPrivilege(JobLockEntity.JOB_jobId_unlockRedeem)) {
			UnlockRedeemParams params = new UnlockRedeemParams();
			params.setRedeemBaseDate(StaticProperties.isIs24() ? DateUtil.getSqlDate() : DateUtil.getAfterDate());

			SerialTaskReq<UnlockRedeemParams> req = new SerialTaskReq<UnlockRedeemParams>();
			req.setTaskCode(SerialTaskEntity.TASK_taskCode_unlockRedeem);
			req.setTaskParams(params);
			serialTaskService.createSerialTask(req);
		}
	}

	/**
	 * 根据分仓更新合仓可赎回份额
	 */
	public void unlockRedeemDo(String taskOid, Date cur) {
		JobLogEntity jobLog =  JobLogFactory.getInstance(JobLockEntity.JOB_jobId_unlockRedeem);
		try {
			String lastOid = "0";

			int arithmometer = 1;
			while (true) {
				List<InvestorTradeOrderEntity> list = this.investorTradeOrderService
						.findByBeforeBeginRedeemDateInclusive(cur, lastOid);
				if (list.isEmpty()) {
					break;
				}
				for (InvestorTradeOrderEntity entity : list) {
					lastOid = entity.getOid();
					unlockRedeemItem(entity);
					arithmometer++;
					if (arithmometer > 100) {
						arithmometer = 1;
						serialTaskRequireNewService.updateTime(taskOid);
					}
				}

			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			jobLog.setJobMessage(AMPException.getStacktrace(e));
			jobLog.setJobStatus(JobLogEntity.JOB_jobStatus_failed);
		}

		jobLog.setBatchEndTime(DateUtil.getSqlCurrentDate());
		this.jobLogService.saveEntity(jobLog);
		this.jobLockService.resetJob(JobLockEntity.JOB_jobId_unlockRedeem);
	}

	/**
	 * 解锁计息
	 */
	public void unlockAccrual() {
		if (this.jobLockService.getRunPrivilege(JobLockEntity.JOB_jobId_unlockAccrual)) {
			UnlockAccrualParams params = new UnlockAccrualParams();
			params.setAccrualBaseDate(StaticProperties.isIs24() ? DateUtil.getSqlDate() : DateUtil.getAfterDate());

			SerialTaskReq<UnlockAccrualParams> req = new SerialTaskReq<UnlockAccrualParams>();
			req.setTaskCode(SerialTaskEntity.TASK_taskCode_unlockAccrual);
			req.setTaskParams(params);
			serialTaskService.createSerialTask(req);
		}
	}

	public void unlockAccrualDo(String taskOid, Date cur) {

		JobLogEntity jobLog =  JobLogFactory.getInstance(JobLockEntity.JOB_jobId_unlockAccrual);

		try {
			String lastOid = "0";

			int arithmometer = 1;
			while (true) {
				List<InvestorTradeOrderEntity> list = this.investorTradeOrderService
						.findByBeforeBeginAccuralDateInclusive(cur, lastOid);
				if (list.isEmpty()) {
					break;
				}
				for (InvestorTradeOrderEntity entity : list) {
					lastOid = entity.getOid();
					unlockAccrualItem(entity);
					arithmometer++;
					if (arithmometer > 100) {
						arithmometer = 1;
						serialTaskRequireNewService.updateTime(taskOid);
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			jobLog.setJobMessage(AMPException.getStacktrace(e));
			jobLog.setJobStatus(JobLogEntity.JOB_jobStatus_failed);
		}
		jobLog.setBatchEndTime(DateUtil.getSqlCurrentDate());
		this.jobLogService.saveEntity(jobLog);
		this.jobLockService.resetJob(JobLockEntity.JOB_jobId_unlockAccrual);

	}

	private void unlockAccrualItem(InvestorTradeOrderEntity entity) {
		this.newService.unlockAccrualItem(entity);

	}

	private void unlockRedeemItem(InvestorTradeOrderEntity entity) {
		this.newService.unlockRedeemItem(entity);
	}

	public PublisherHoldEntity saveEntity(PublisherHoldEntity hold) {
		return this.publisherHoldDao.save(hold);
	}

	public PagesRep<HoldQueryRep> holdMng(Specification<PublisherHoldEntity> spec, Pageable pageable) {
		Page<PublisherHoldEntity> cas = this.publisherHoldDao.findAll(spec, pageable);
		PagesRep<HoldQueryRep> pagesRep = new PagesRep<HoldQueryRep>();
		if (cas != null && cas.getContent() != null && cas.getTotalElements() > 0) {
			for (PublisherHoldEntity hold : cas) {
				HoldQueryRep queryRep = new HoldQueryRep();
				Product product = hold.getProduct();
				BigDecimal netUnitShare = product.getNetUnitShare();
				queryRep.setInvestorOid(hold.getInvestorBaseAccount().getOid());
				queryRep.setHoldOid(hold.getOid()); // 持仓OID
				queryRep.setProductOid(product.getOid()); // 产品OID
				queryRep.setProductCode(product.getCode()); // 产品编号
				queryRep.setProductName(product.getName()); // 产品名称
				queryRep.setExpAror(
						hold.getProduct().getExpAror().toString() + "-" + hold.getProduct().getExpArorSec().toString()); // 预期收益率

				queryRep.setTotalVolume(hold.getTotalVolume()); // 持仓总份额
				queryRep.setTotalAmount(hold.getTotalVolume().multiply(netUnitShare));
				queryRep.setToConfirmRedeemVolume(hold.getToConfirmRedeemVolume()); // 当前价值
				queryRep.setToConfirmRedeemAmount(hold.getToConfirmRedeemVolume().multiply(netUnitShare));
				queryRep.setToConfirmInvestVolume(hold.getToConfirmInvestVolume()); // 待确认
				queryRep.setToConfirmInvestAmount(hold.getToConfirmInvestVolume().multiply(netUnitShare));
				queryRep.setRedeemableHoldVolume(hold.getRedeemableHoldVolume()); // 可赎回份额
				queryRep.setRedeemableHoldAmount(hold.getRedeemableHoldVolume().multiply(netUnitShare));
				queryRep.setLockRedeemHoldVolume(hold.getLockRedeemHoldVolume()); // 赎回锁定份额
				queryRep.setLockRedeemHoldAmount(hold.getLockRedeemHoldVolume().multiply(netUnitShare));
				queryRep.setValue(hold.getValue()); // 最新市值
				queryRep.setHoldTotalIncome(hold.getHoldTotalIncome()); // 累计收益
				queryRep.setHoldYesterdayIncome(hold.getHoldYesterdayIncome()); // 昨日收益
				queryRep.setExpectIncome(hold.getExpectIncome() + "-" + hold.getExpectIncomeExt()); // 预期收益

				queryRep.setMaxHoldVolume(hold.getMaxHoldVolume()); // 单个产品最大持有份额
				queryRep.setDayInvestVolume(hold.getDayInvestVolume()); // 今日投资份额
				queryRep.setDayRedeemVolume(hold.getDayRedeemVolume()); // 今日赎回份额
				queryRep.setAccruableHoldVolume(hold.getAccruableHoldVolume()); // 计息份额

				queryRep.setConfirmDate(hold.getConfirmDate()); // 收益确认日期
				queryRep.setHoldStatus(hold.getHoldStatus()); // 持仓状态
				queryRep.setHoldStatusDisp(holdStatusEn2Ch(hold.getHoldStatus())); // 持仓状态disp

				pagesRep.add(queryRep);
			}
		}
		pagesRep.setTotal(cas.getTotalElements());
		return pagesRep;
	}

	public HoldDetailRep detail(String holdOid) {
		HoldDetailRep detailRep = new HoldDetailRep();
		PublisherHoldEntity hold = this.findByOid(holdOid);
		detailRep.setProductOid(hold.getProduct().getOid()); // 产品OID
		detailRep.setProductCode(hold.getProduct().getCode()); // 产品编号
		detailRep.setProductName(hold.getProduct().getName()); // 产品名称
		detailRep.setExpAror(hold.getProduct().getExpAror().toString() + hold.getProduct().getExpArorSec().toString()); // 预期收益率
		detailRep.setLockPeriod(hold.getProduct().getLockPeriodDays()); // 锁定期
		detailRep.setTotalHoldVolume(hold.getTotalVolume()); // 持仓总份额
		detailRep.setRedeemableHoldVolume(hold.getRedeemableHoldVolume()); // 可赎回份额
		detailRep.setLockRedeemHoldVolume(hold.getLockRedeemHoldVolume()); // 赎回锁定份额
		detailRep.setValue(hold.getValue()); // 最新市值
		detailRep.setHoldTotalIncome(hold.getHoldTotalIncome()); // 累计收益
		detailRep.setHoldYesterdayIncome(hold.getHoldYesterdayIncome()); // 昨日收益
		detailRep.setExpectIncome(hold.getExpectIncome()); // 预期收益
		detailRep.setLastConfirmDate(hold.getConfirmDate()); // 份额确认日期
		detailRep.setHoldStatus(hold.getHoldStatus()); // 持仓状态
		detailRep.setHoldStatusDisp(holdStatusEn2Ch(hold.getHoldStatus())); // 持仓状态disp
		return detailRep;
	}

	public PublisherHoldEntity findByOid(String holdOid) {
		PublisherHoldEntity hold = this.publisherHoldDao.findOne(holdOid);
		if (null == hold) {
			throw new AMPException("持仓不存在");
		}
		return hold;
	}


	private String holdStatusEn2Ch(String holdStatus) {
		if (PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_toConfirm.equals(holdStatus)) {
			return "待确认";
		}
		if (PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_holding.equals(holdStatus)) {
			return "持有中";
		}

		if (PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_expired.equals(holdStatus)) {
			return "已到期";
		}
		if (PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_closing.equals(holdStatus)) {
			return "结算中";
		}

		if (PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_closed.equals(holdStatus)) {
			return "已结算";
		}

		if (PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_refunding.equals(holdStatus)) {
			return "退款中";
		}

		if (PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_refunded.equals(holdStatus)) {
			return "已退款";
		}

		return holdStatus;
	}


	public List<PublisherHoldEntity> clearingHold(String productOid, String accountType, String lastOid) {
		return this.publisherHoldDao.clearingHold(productOid, accountType, lastOid);
	}

	/**
	 * 获取指定产品下面的所有持有人
	 */
	public List<PublisherHoldEntity> findByProduct(Product product, String lastOid) {
		return this.publisherHoldDao.findByProduct(product.getOid(), lastOid);
	}

	public List<PublisherHoldEntity> findByProductHoldStatus(Product product, String holdStatus, String lastOid,
			Date incomeDate) {
		return this.publisherHoldDao.findByProductAndHoldStatus(product.getOid(), holdStatus, lastOid,
				PublisherHoldEntity.PUBLISHER_accountType_INVESTOR, incomeDate);
	}

	public int updateHold4Interest(String holdOid, BigDecimal holdIncomeVolume, BigDecimal holdIncomeAmount,
			BigDecimal holdLockIncomeVolume, BigDecimal holdLockIncomeAmount, BigDecimal netUnitAmount,
			Date incomeDate, BigDecimal holdBaseAmount, BigDecimal holdRewardAmount) {

		int i = this.publisherHoldDao.updateHold4Interest(holdOid, holdIncomeVolume, holdIncomeAmount,
				holdLockIncomeVolume, holdLockIncomeAmount, netUnitAmount,
				incomeDate, holdBaseAmount, holdRewardAmount);
		if (i < 1) {
			throw new AMPException("计息失败");
		}
		return i;

	}

	public int updateHold4InterestTn(String holdOid, BigDecimal holdIncomeAmount, BigDecimal holdLockIncomeAmount, Date incomeDate) {

		int i = this.publisherHoldDao.updateHold4InterestTn(holdOid, holdIncomeAmount, holdLockIncomeAmount, incomeDate);
		if (i < 1) {
			throw new AMPException("计息失败");
		}
		return i;

	}

	@Transactional
	public PublisherHoldEntity getAssetPoolSpvHold(AssetPoolEntity assetPool, PublisherBaseAccountEntity spv) {
		// 如果该资产池下存在03产品 直接返回03产品的SPV持仓
		Product type03Product = productService.getType03ProductByAssetPoolOid(assetPool.getOid());
		if (type03Product != null) {
			PublisherHoldEntity type03PublisherHoldEntity = publisherHoldDao.findByAssetPoolOidAndProductOid(assetPool.getOid(), type03Product.getOid());
			return type03PublisherHoldEntity;
		}
		List<PublisherHoldEntity> list = this.publisherHoldDao.findByAssetPoolEntityAndSPV(assetPool, spv);

		if (null != list && list.size() > 0) {
			return list.get(0);
		} else {
			PublisherHoldEntity e = new PublisherHoldEntity();
			e.setAssetPool(assetPool);
			e.setPublisherBaseAccount(spv);

			List<Product> products = this.productService.getProductListByAssetPoolOid(assetPool.getOid());
			if (null != products && products.size() > 0) {
				e.setProduct(products.get(0));
			}
			e.setAccountType(PublisherHoldEntity.PUBLISHER_accountType_SPV);
			e.setHoldStatus(PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_holding);
			return this.publisherHoldDao.save(e);
		}
	}

	@Transactional
	public PublisherHoldEntity getAssetPoolSpvHold4SpvOrder(AssetPoolEntity assetPool, PublisherBaseAccountEntity spv, String productType) {
		// 如果存在03产品直接返回03产品的SPV持仓
		Product type03Product = productService.getType03ProductByAssetPoolOid(assetPool.getOid());
		if (type03Product != null) {
			PublisherHoldEntity type03PublisherHoldEntity = publisherHoldDao.findByAssetPoolOidAndProductOid(assetPool.getOid(), type03Product.getOid());
			return type03PublisherHoldEntity;
		}

		List<PublisherHoldEntity> list = this.publisherHoldDao.findByAssetPoolEntityAndSPV(assetPool, spv);

		if (null != list && list.size() > 0 ){
			if (productType != null && productType.equals(Product.TYPE_Producttype_02)){
				return list.get(0);
			}else{
				for (PublisherHoldEntity hold : list){
					if (hold.getProduct() == null){
						return hold;
					}
				}
			}
		}

		PublisherHoldEntity e = new PublisherHoldEntity();
		e.setAssetPool(assetPool);
		e.setPublisherBaseAccount(spv);
		e.setAccountType(PublisherHoldEntity.PUBLISHER_accountType_SPV);
		e.setHoldStatus(PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_holding);
		return this.publisherHoldDao.save(e);
	}

	@Transactional
	public PublisherHoldEntity getAssetPoolSpvHold4reviewProduct(AssetPoolEntity assetPool, PublisherBaseAccountEntity spv, String productType) {
		List<PublisherHoldEntity> list = this.publisherHoldDao.findByAssetPoolEntityAndSPV(assetPool, spv);

		if ((productType.equals(Product.TYPE_Producttype_02) || productType.equals(Product.TYPE_Producttype_03)) && null != list && list.size() > 0) {
			for (PublisherHoldEntity hold : list){
				if (hold.getProduct() == null){
					return hold;
				}
			}
		}

		PublisherHoldEntity e = new PublisherHoldEntity();
		e.setAssetPool(assetPool);
		e.setPublisherBaseAccount(spv);
		e.setAccountType(PublisherHoldEntity.PUBLISHER_accountType_SPV);
		e.setHoldStatus(PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_holding);
		return this.publisherHoldDao.save(e);
	}

	public int checkSpvHold4Invest(InvestorTradeOrderEntity tradeOrder) {
		return this.checkSpvHold4Invest(tradeOrder, false);
	}

	/**
	 * 检验SPV仓位
	 *
	 * @param tradeOrder
	 * @return
	 */
	public int checkSpvHold4Invest(InvestorTradeOrderEntity tradeOrder, boolean isRecovery) {
		BigDecimal orderVolume = tradeOrder.getOrderVolume();
		if (isRecovery) {
			orderVolume = orderVolume.negate();
		}
		int i = this.publisherHoldDao.checkSpvHold4Invest(tradeOrder.getProduct(),
				PublisherHoldEntity.PUBLISHER_accountType_SPV, orderVolume);
		if (i < 1) {
			// error.define[30011]=产品可投金额不足(CODE:30011)
			throw new AMPException(30011);
		}
		return i;

	}

	/**
	 * //更新SPV持仓
	 *
	 * @param product
	 * @param orderVolume
	 * @return
	 */
	public int update4InvestConfirm(Product product, BigDecimal orderVolume) {
		int i = this.publisherHoldDao.update4InvestConfirm(product, PublisherHoldEntity.PUBLISHER_accountType_SPV,
				orderVolume);
		if (i < 1) {
			// error.define[30024]=针对SPV持仓份额确认失败(CODE:30024)
			throw new AMPException(30024);
		}
		return i;
	}

	public int update4RedeemConfirm(Product product, BigDecimal orderVolume) {
		int i = this.publisherHoldDao.update4RedeemConfirm(product, PublisherHoldEntity.PUBLISHER_accountType_SPV,
				orderVolume);
		if (i < 1) {
			// error.define[30024]=针对SPV持仓份额确认失败(CODE:30024)
			throw new AMPException(30024);
		}

		return i;
	}

	public HoldDetailRep getHoldByAssetPoolOid(String assetPoolOid, String productOid) {
		HoldDetailRep detailRep = new HoldDetailRep();

		PublisherHoldEntity hold = null;
		AssetPoolEntity assetPool = this.assetPoolService.getByOid(assetPoolOid);
		if (assetPool != null && assetPool.getSpvEntity() != null) {
			PublisherBaseAccountEntity spv = this.publisherBaseAccountDao.findOne(assetPool.getSpvEntity().getOid());

			List<PublisherHoldEntity> list = this.publisherHoldDao.findByAssetPoolEntityAndSPV(assetPool, spv);

			if (null != list && list.size() > 0) {
				hold = list.get(0);
				if (hold.getProduct() != null) {
					detailRep.setProductOid(hold.getProduct().getOid()); // 产品OID
					detailRep.setProductCode(hold.getProduct().getCode()); // 产品编号
					detailRep.setProductName(hold.getProduct().getName()); // 产品名称
					detailRep.setExpAror(hold.getProduct().getExpAror().toString() + "~"
							+ hold.getProduct().getExpArorSec().toString()); // 预期收益率
					detailRep.setLockPeriod(hold.getProduct().getLockPeriodDays()); // 锁定期
				}

				detailRep.setTotalHoldVolume(hold.getTotalVolume()); // 持仓总份额
																		// 本金余额(持有总份额)
																		// totalHoldVolume
																		// decimal(16,4)
				detailRep.setRedeemableHoldVolume(hold.getRedeemableHoldVolume()); // 可赎回份额
				detailRep.setLockRedeemHoldVolume(hold.getLockRedeemHoldVolume()); // 赎回锁定份额
				detailRep.setValue(hold.getValue()); // 最新市值
				detailRep.setHoldTotalIncome(hold.getHoldTotalIncome()); // 累计收益
				detailRep.setHoldYesterdayIncome(hold.getHoldYesterdayIncome()); // 昨日收益
				detailRep.setExpectIncome(hold.getExpectIncome()); // 预期收益
				detailRep.setLastConfirmDate(hold.getConfirmDate()); // 份额确认日期
				detailRep.setHoldStatus(hold.getHoldStatus()); // 持仓状态
				detailRep.setHoldStatusDisp(holdStatusEn2Ch(hold.getHoldStatus())); // 持仓状态disp

			}
			detailRep.setPoolOverplusVolume(getPoolOverplusVolume(assetPool, productOid));	// 资产池剩余可募集规模
		}

		return detailRep;
	}

	// 资产池剩余可募集规模
	private BigDecimal getPoolOverplusVolume(AssetPoolEntity assetPool, String productOid) {

		// 查询资产池下的产品（不包含募集失败的）
		List<Product> productList = productDao.findProducts(assetPool.getOid());

		BigDecimal poolOverplusVolume = BigDecimal.ZERO;

		for (Product p : productList){
			if (p.getRaisedTotalNumber() != null && (productOid == null || !productOid.equals(p.getOid()))){
				if(p.getProductPackage() != null && !"RAISING".equals(p.getState())){
					poolOverplusVolume = poolOverplusVolume.add(p.getRaisedTotalNumber());
				}
				if(p.getProductPackage() == null){
					poolOverplusVolume = poolOverplusVolume.add(p.getRaisedTotalNumber());
				}
			}
		}

		// 查询募集期的产品包
		List<ProductPackage> productPackages = this.productPackageDao.findProductPackages(assetPool.getOid(),DateUtil.getSqlDate());
		if (productPackages != null && productPackages.size() > 0) {
			for (ProductPackage productPackage : productPackages) {
				poolOverplusVolume = poolOverplusVolume.add(productPackage.getRaisedTotalNumber());
			}
		}

		poolOverplusVolume = assetPool.getScale().subtract(poolOverplusVolume);

		if (poolOverplusVolume.compareTo(BigDecimal.ZERO) < 0){
			return BigDecimal.ZERO;
		}
		return poolOverplusVolume;
	}

	/**
	 * 检查是否超过单个产品最大持仓
	 */
	public void checkMaxHold4Invest(InvestorTradeOrderEntity orderEntity) {
		logger.info("============orderEntity.getProduct().getMaxHold())===========::" + orderEntity.getProduct().getMaxHold() + "ture or false" + DecimalUtil.isGoRules(orderEntity.getProduct().getMaxHold()));
		logger.info("============检查是否超过单个产品最大持仓===========" + JSON.toJSONString(orderEntity));
		if (DecimalUtil.isGoRules(orderEntity.getProduct().getMaxHold())) { // 等于0，表示无限制
			PublisherHoldEntity hold = this.publisherHoldDao.findByInvestorBaseAccountAndProduct(orderEntity.getInvestorBaseAccount(),
					orderEntity.getProduct());
			if (null == hold) {
				logger.info("=====hold is null======");
				if (orderEntity.getOrderVolume().compareTo(orderEntity.getProduct().getMaxHold()) > 0) {
					logger.info("=====hold is null======");
					// error.define[30031]=份额已超过所购产品最大持仓(CODE:30031)
					throw new AMPException(30031);
				}
			} else {
				logger.info("=====hold is not null======");
				int i = this.publisherHoldDao.checkMaxHold4Invest(orderEntity.getInvestorBaseAccount(),
						orderEntity.getProduct(), orderEntity.getProduct().getMaxHold(),
						orderEntity.getOrderVolume());
				if (i < 1) {
					// error.define[30031]=份额已超过所购产品最大持仓(CODE:30031)
					throw new AMPException(30031);
				}
			}
		}
	}

	public int updateHold4Confirm(PublisherHoldEntity publisherHold, BigDecimal redeemableHoldVolume,
			BigDecimal lockRedeemHoldVolume, BigDecimal accruableHoldVolume, BigDecimal orderVolume) {
		int i = this.publisherHoldDao.updateHold4Confirm(publisherHold.getOid(), redeemableHoldVolume,
				lockRedeemHoldVolume, accruableHoldVolume, orderVolume);
		if (i < 1) {
			// error.define[30074]=份额确认异常(CODE:30074)
			throw new AMPException(30074);
		}
		return i;

	}

	public int updateHold4ExpGoldConfirm(PublisherHoldEntity publisherHold, BigDecimal accruableHoldVolume, BigDecimal orderVolume) {
		int i = this.publisherHoldDao.updateHold4ExpGoldConfirm(publisherHold.getOid(), accruableHoldVolume, orderVolume);
		if (i < 1) {
			throw new AMPException("份额确认异常");
		}
		return i;

	}


	/**
	 * 活期废单：解锁SPV锁定份额
	 */
	public int updateSpvHold4T0InvestAbandon(InvestorTradeOrderEntity orderEntity) {
		int i = this.publisherHoldDao.updateSpvHold4T0InvestAbandon(orderEntity.getProduct(), orderEntity.getOrderVolume());
		if (i < 1) {
			// error.define[30035]=废申购单时SPV持仓锁定份额异常(CODE:30035)
			throw new AMPException(30035);
		}
		return i;
	}

	/**
	 * 定期废单
	 */
	public int updateSpvHold4TnInvestAbandon(InvestorTradeOrderEntity orderEntity) {
		int i = this.publisherHoldDao.updateSpvHold4TnInvestAbandon(orderEntity.getProduct(), orderEntity.getOrderVolume());
		if (i < 1) {
			// error.define[30035]=废申购单时SPV持仓锁定份额异常(CODE:30035)
			throw new AMPException(30035);
		}
		return i;

	}

	/**
	 * 废单:扣除投资人最大持仓份额
	 */
	public void updateMaxHold4InvestAbandon(InvestorBaseAccountEntity investorBaseAccount, Product product,
			BigDecimal orderVolume) {
		if (DecimalUtil.isGoRules(product.getMaxHold())) { // 等于0，表示无限制
			int i = this.publisherHoldDao.updateMaxHold4InvestAbandon(investorBaseAccount, product, orderVolume);
			if (i < 1) {
				// error.define[30036]=废申购单时最大持仓份额异常(CODE:30036)
				throw new AMPException(30036);
			}

		}

	}

	/**
	 * 付息
	 */
	public void repayInterest(InvestorTradeOrderEntity tradeOrder) {
		if (this.publisherHoldDao.repayInterest(tradeOrder.getOrderAmount(), tradeOrder.getInvestorBaseAccount(),
				tradeOrder.getProduct()) <= 0) {
			throw AMPException.getException(20009);
		}
	}

	/**
	 * 获取用户可赎回的快活宝
	 */
	public RowsRep<T0RedeemableRep> getT0RedeemableProduct(String investorOid) {
		RowsRep<T0RedeemableRep> rowsRep = new RowsRep<T0RedeemableRep>();
		List<HoldCacheEntity> holds = cacheHoldService.findByInvestorOid(investorOid);
		for (HoldCacheEntity cacheHold : holds) {
			ProductCacheEntity cacheProduct = cacheProductService.getProductCacheEntityById(cacheHold.getProductOid());
			if (!Product.TYPE_Producttype_02.equals(cacheProduct.getType())) {
				continue;
			}
			if(incomeRewardCacheService.hasRewardIncome(cacheHold.getProductOid())) {
				continue;
			}
			if (!labelService.isProductLabelHasAppointLabel(cacheProduct.getProductLabel(), LabelEnum.tiyanjin.toString())
					 && cacheHold.getRedeemableHoldVolume().compareTo(BigDecimal.ZERO) > 0) {
				T0RedeemableRep rep = new T0RedeemableRep();
				rep.setProductOid(cacheProduct.getProductOid());
				rep.setProductName(cacheProduct.getName());
				rep.setRedeemableHoldVolume(cacheHold.getRedeemableHoldVolume());
				rep.setDayRedeemCount(cacheHold.getDayRedeemCount());
				rep.setSingleDayRedeemCount(cacheProduct.getSingleDayRedeemCount());
				rowsRep.add(rep);
			}
		}
		return rowsRep;
	}

	/**
	 * 我的活期
	 */
	public MyHoldT0QueryRep queryMyT0HoldProList(String investorOid) {
		MyHoldT0QueryRep rep = new MyHoldT0QueryRep();
		Pages<HoldingT0Detail> holdingDetails = new Pages<HoldingT0Detail>();
		Pages<ToConfirmT0Detail> toConfirmDetails = new Pages<ToConfirmT0Detail>();

		List<HoldCacheEntity> holds = cacheHoldService.findByInvestorOid(investorOid);
		Date incomeDate = DateUtil.getBeforeDate();
		for (HoldCacheEntity cacheHold : holds) {

			ProductCacheEntity cacheProduct = cacheProductService.getProductCacheEntityById(cacheHold.getProductOid());
			if (!Product.TYPE_Producttype_02.equals(cacheProduct.getType())) {
				continue;
			}
			if(incomeRewardCacheService.hasRewardIncome(cacheHold.getProductOid())) {
				continue;
			}
			rep.setT0CapitalAmount(rep.getT0CapitalAmount().add(cacheHold.getTotalVolume()));
			if (null != cacheHold.getConfirmDate() && DateUtil.daysBetween(incomeDate, cacheHold.getConfirmDate()) == 0) {
				rep.setT0YesterdayIncome(rep.getT0YesterdayIncome().add(cacheHold.getHoldYesterdayIncome()));
			}
			rep.setTotalIncomeAmount(rep.getTotalIncomeAmount().add(cacheHold.getHoldTotalIncome()));

			if (!labelService.isProductLabelHasAppointLabel(cacheProduct.getProductLabel(), LabelEnum.tiyanjin.toString())) {
				rep.setProductOid(cacheProduct.getProductOid());
			}

			if (PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_toConfirm.equals(cacheHold.getHoldStatus())) {
				ToConfirmT0Detail toConfirmDetail = new ToConfirmT0Detail();
				toConfirmDetail.setProductOid(cacheHold.getProductOid());
				toConfirmDetail.setProductName(cacheProduct.getName());
				toConfirmDetail.setToConfirmInvestVolume(cacheHold.getToConfirmInvestVolume());
				toConfirmDetail.setSingleDailyMaxRedeem(cacheProduct.getSingleDailyMaxRedeem());// 产品--单人单日赎回上限
				toConfirmDetail.setDailyNetMaxRredeem(cacheProduct.getDailyNetMaxRredeem());// 产品--剩余赎回金额
				toConfirmDetail.setNetMaxRredeemDay(cacheProduct.getNetMaxRredeemDay());// 产品--单日净赎回上限
				toConfirmDetails.add(toConfirmDetail);
				continue;
			}

			if (cacheHold.getHoldVolume().compareTo(BigDecimal.ZERO) > 0) {
				HoldingT0Detail detail = new HoldingT0Detail();
				detail.setProductOid(cacheProduct.getProductOid());
				detail.setProductName(cacheProduct.getName());
				detail.setValue(cacheHold.getHoldVolume());
				if (null != cacheHold.getConfirmDate() && DateUtil.daysBetween(incomeDate, cacheHold.getConfirmDate()) == 0) {
					detail.setYesterdayIncome(cacheHold.getHoldYesterdayIncome());
				}
				detail.setMinRredeem(cacheProduct.getMinRredeem());// 单笔赎回最低金额
				detail.setMaxRredeem(cacheProduct.getMaxRredeem());// 单笔赎回最大金额
				detail.setHoldTotalIncome(cacheHold.getHoldTotalIncome());
				detail.setToConfirmRedeemVolume(cacheHold.getToConfirmRedeemVolume());
				detail.setSingleDailyMaxRedeem(cacheProduct.getSingleDailyMaxRedeem());// 产品--单人单日赎回上限
				detail.setDailyNetMaxRredeem(cacheProduct.getDailyNetMaxRredeem());// 产品--剩余赎回金额
				detail.setNetMaxRredeemDay(cacheProduct.getNetMaxRredeemDay());// 产品--单日净赎回上限

				detail.setSubType(ProductPojo.DEMAND_SUBTYPE);
				if (labelService.isProductLabelHasAppointLabel(cacheProduct.getProductLabel(), LabelEnum.tiyanjin.toString())) {
					detail.setSubType(ProductPojo.EXPERIENCE_FUND_SUBTYPE);
				}

				holdingDetails.add(detail);
			}

			if (cacheHold.getToConfirmInvestVolume().compareTo(BigDecimal.ZERO) > 0) {
				ToConfirmT0Detail toConfirmDetail = new ToConfirmT0Detail();
				toConfirmDetail.setProductName(cacheProduct.getName());
				toConfirmDetail.setToConfirmInvestVolume(cacheHold.getToConfirmInvestVolume());

				toConfirmDetail.setSubType(ProductPojo.DEMAND_SUBTYPE);
				if (labelService.isProductLabelHasAppointLabel(cacheProduct.getProductLabel(), LabelEnum.tiyanjin.toString())) {
					toConfirmDetail.setSubType(ProductPojo.EXPERIENCE_FUND_SUBTYPE);
				}
				toConfirmDetails.add(toConfirmDetail);
			}
		}
		holdingDetails.setTotal(holdingDetails.getRows().size());
		toConfirmDetails.setTotal(toConfirmDetails.getRows().size());
		rep.setToConfirmDetails(toConfirmDetails);
		rep.setHoldingDetails(holdingDetails);
		return rep;
	}

	public List<MyHoldQueryRep> queryMyT0HoldProListDetail(String investorOid) {
		List<MyHoldQueryRep> myHoldList = new ArrayList<MyHoldQueryRep>();
		List<HoldCacheEntity> holds = cacheHoldService.findByInvestorOidSort(investorOid);
		for (HoldCacheEntity cacheHold : holds) {
			//---------------------我的活期明细，过滤掉定期产品----------------2017.04.19---------------------
			// 过滤快定宝
			ProductCacheEntity cacheProduct = cacheProductService.getProductCacheEntityById(cacheHold.getProductOid());
			if (!Product.TYPE_Producttype_02.equals(cacheProduct.getType())) {
				continue;
			}
			//---------------------我的活期明细，过滤掉定期产品----------------2017.04.19---------------------
			if (cacheHold.getHoldVolume().compareTo(BigDecimal.ZERO) > 0) {// 持有中活期
//				ProductCacheEntity cacheProduct = cacheProductService.getProductCacheEntityById(cacheHold.getProductOid());
				MyHoldQueryRep rep = new MyHoldQueryRep();

				// 产品详情信息
				List<Object[]> holdList = this.publisherHoldDao.findProductDetail(investorOid, cacheHold.getProductOid());
				if (holdList == null || holdList.size() == 0) {
					// 您尚未购买当前产品！(CODE:80020)
					throw new AMPException(80020);
				}
				Object[] arr = holdList.get(0);

				//HoldCacheEntity cacheHold = this.cacheHoldService.getHoldCacheEntityByUidAndProductId(userOid, productOid);
				//ProductCacheEntity cacheProduct = this.cacheProductService.getProductCacheEntityById(productOid);
				Date incomeDate = DateUtil.getBeforeDate();
				BigDecimal expAror = BigDecimalUtil.parseFromObject(arr[5]);// 年化收益率
				BigDecimal expArorSec = arr[6] == null ? null : BigDecimalUtil.parseFromObject(arr[6]);// 年化收益率区间
				BigDecimal incomeCalcBasis = BigDecimalUtil.parseFromObject(arr[4]);// 收益计算基础
				String assetPoolOid = arr[7] == null ? "" : arr[7].toString();// 所属资产池
				if (null != cacheHold.getConfirmDate() && DateUtil.daysBetween(incomeDate, cacheHold.getConfirmDate()) == 0) {
					rep.setYesterdayIncome(cacheHold.getHoldYesterdayIncome());// 昨日收益
				}

				List<String> productOids = new ArrayList<String>();
				productOids.add(cacheProduct.getProductOid());
				Map<String,Map<String,BigDecimal>> productExpArorMap = productClientService.getProductsMinMaxRewards(productOids);

				rep.setTotalIncome(BigDecimalUtil.parseFromObject(arr[0]));// 累计收益

				rep.setProductOid(cacheProduct.getProductOid());// 产品ID
				rep.setProductName(cacheProduct.getName());// 产品名称
				rep.setMinRredeem(cacheProduct.getMinRredeem());// 单笔赎回最低金额
				rep.setMaxRredeem(cacheProduct.getMaxRredeem());// 单笔赎回最大金额
				rep.setAdditionalRredeem(cacheProduct.getAdditionalRredeem());// 单笔赎回递增金额
				rep.setSingleDailyMaxRedeem(cacheProduct.getSingleDailyMaxRedeem());// 产品--单人单日赎回上限
				rep.setDailyNetMaxRredeem(cacheProduct.getDailyNetMaxRredeem());// 产品--剩余赎回金额
				rep.setNetMaxRredeemDay(cacheProduct.getNetMaxRredeemDay());// 产品--单日净赎回上限
				rep.setSingleDayRedeemCount(Math.min(cacheProduct.getSingleDayRedeemCount(), cacheConfig.DAILY_REDEEM_COUNT.intValue()));
				rep.setDayRedeemCount(cacheHoldService.getDailyRedeemCount(cacheHold.getInvestorOid()));
				rep.setTotalValue(cacheHold.getHoldVolume());// 持有金额
				rep.setDayRedeemVolume(cacheHold.getDayRedeemVolume());// 投资者--今日赎回金额
				rep.setRedeemableHoldVolume(cacheHold.getRedeemableHoldVolume());// 可赎回金额
				rep.setSubType(ProductPojo.DEMAND_SUBTYPE);
				if(productExpArorMap.get(cacheProduct.getProductOid())!=null) {
					rep.setSubType(ProductPojo.INCREMENT_SUBTYPE);
					Product product = new Product();
					product.setRredeemDateType((String)arr[17]);
					product.setRedeemConfirmDays((Integer)arr[18]);
					rep.setPayDate(orderDateService.getRedeemDate(product, new Timestamp(System.currentTimeMillis())));
				} else if(LabelEnum.tiyanjin.toString().equals((String)arr[19])) {
					rep.setSubType(ProductPojo.EXPERIENCE_FUND_SUBTYPE);
					Product product = new Product();
					product.setRredeemDateType((String)arr[17]);
					product.setRedeemConfirmDays((Integer)arr[18]);
					rep.setPayDate(orderDateService.getRedeemDate(product, new Timestamp(System.currentTimeMillis())));
				} else {
					//快活宝返回大额赎回日期
					Product product = new Product();
					product.setRredeemDateType("T");
					product.setRedeemConfirmDays(1);
					rep.setPayDate(orderDateService.getRedeemDate(product, new Timestamp(System.currentTimeMillis())));
				}

				// 预期收益率
				BigDecimal expArorMid = expArorSec == null ? expAror : (expAror.add(expArorSec).divide(new BigDecimal("2.0")));
				// 万元收益=10000*预期年化收益率/收益计算基础
				if (incomeCalcBasis.compareTo(SysConstant.BIGDECIMAL_defaultValue) != 0) {
					rep.setMillionIncome(InterestFormula.compound(new BigDecimal("10000"), expArorMid, cacheProduct.getIncomeCalcBasis()));
	//				rep.setMillionIncome(new BigDecimal("10000").multiply(expArorMid).divide(incomeCalcBasis, 4,
	//						DecimalUtil.roundMode));
				}

				// 折线图最近日期
				Date lastedDate = DateUtil.addSQLDays(DateUtil.getSqlDate(), -1);

				// 实际收益分配的产品收益率
				Page<IncomeAllocate> pcas = this.incomeAllocateService.getProductIncomeAllocate(assetPoolOid, 30);
				if (pcas != null && pcas.getContent() != null && pcas.getTotalElements() > 0) {
					for (IncomeAllocate ia : pcas) {
						// 年化收益率走势 单位（%）
						rep.getExpArorList().add(0, new MyCurrProTendencyChartRep(DateUtil.format(ia.getBaseDate(), "MM-dd"),
								ProductDecimalFormat.multiply(ia.getRatio())));

						// 万份收益走势 单位（元）
						rep.getMillionIncomeList().add(0,
								new MyCurrProTendencyChartRep(DateUtil.format(ia.getBaseDate(), "MM-dd"), ia.getWincome()));

						lastedDate = ia.getBaseDate();
					}
				}

				int size = rep.getExpArorList().size();
				if (size < 30) {
					for (int i = 0; i < 30 - size; i++) {
						lastedDate = DateUtil.addSQLDays(lastedDate, -1);
						// 年化收益率走势 单位（%）,补足30条数据
						rep.getExpArorList().add(0, new MyCurrProTendencyChartRep(DateUtil.format(lastedDate, "MM-dd"),
								ProductDecimalFormat.multiply(expArorMid)));
						// 万份收益走势 单位（元）,补足30条数据
						rep.getMillionIncomeList().add(0,
								new MyCurrProTendencyChartRep(DateUtil.format(lastedDate, "MM-dd"), rep.getMillionIncome()));
					}
				}

				// 实际收益计算方式：
				// 有昨日实际收益时=奖励收益率+昨日实际奖励收益率
				// 无昨日实际收益率时=奖励收益率+奖励收益率+预期收益
				BigDecimal exp;
				if (pcas != null && pcas.getContent() != null && pcas.getTotalElements() > 0) {
					exp = ProductDecimalFormat.multiply(pcas.getContent().get(0).getRatio());
				} else {
					exp = ProductDecimalFormat.multiply(expArorMid);
				}

				// 我的产品分档明细
				List<LevelIncomeRep> levelList = levelIncomeService.queryLevelDetialsForInvestor(investorOid, cacheHold.getProductOid());
				if (levelList != null && levelList.size() > 0) {
					for (LevelIncomeRep entity : levelList) {
						// 实际收益率
						entity.setRatio(entity.getRatio().add(exp));
						// 昨日总收益率已经在sql中取出来了。这里不再加上昨日收益
					}
					// 产品分档明细
					rep.setLevelList(levelList);

					// 奖励收益率图表
					List<MyCurrProTendencyChartRep> rewardRadioList = new ArrayList<MyCurrProTendencyChartRep>();
					for (LevelIncomeRep entity : levelList) {
						MyCurrProTendencyChartRep levelRadio = new MyCurrProTendencyChartRep();
						levelRadio.setFloorday(entity.getStartDate());
						levelRadio.setVertical(entity.getRatio());// 实际收益率

						if (entity.getStartDate() == null && entity.getEndDate() != null) {
							levelRadio.setAxis("小于等于" + entity.getEndDate() + "天");
						} else if (entity.getStartDate() != null && entity.getEndDate() == null) {
							levelRadio.setAxis("大于等于" + entity.getStartDate() + "天");
						} else if (entity.getEndDate() != null && entity.getStartDate() != null) {
							if (entity.getEndDate().intValue() == entity.getStartDate().intValue()) {
								levelRadio.setAxis(entity.getStartDate() + "天");
							} else {
								levelRadio.setAxis(entity.getStartDate() + "天-" + entity.getEndDate() + "天");
							}
						}
						rewardRadioList.add(levelRadio);
					}
					rep.setRewardRadioList(rewardRadioList);
				}

				// 添加到集合
				myHoldList.add(rep);
			}
		}

		return myHoldList;
	}


	/**
	 * 查询活期产品详情（累计收益、昨日收益、总市值）
	 */
	public MyHoldQueryRep queryMyCurrProInfo(String investorOid, String productOid) {
		MyHoldQueryRep rep = new MyHoldQueryRep();
		// 产品详情信息
		List<Object[]> holdList = this.publisherHoldDao.findProductDetail(investorOid, productOid);
		if (holdList == null || holdList.size() == 0) {
			// 您尚未购买当前产品！(CODE:80020)
			throw new AMPException(80020);
		}
		Object[] arr = holdList.get(0);

		HoldCacheEntity cacheHold = this.cacheHoldService.getHoldCacheEntityByInvestorOidAndProductOid(investorOid, productOid);
		ProductCacheEntity cacheProduct = this.cacheProductService.getProductCacheEntityById(productOid);
		Date incomeDate = DateUtil.getBeforeDate();
		BigDecimal expAror = BigDecimalUtil.parseFromObject(arr[5]);// 年化收益率
		BigDecimal expArorSec = arr[6] == null ? null : BigDecimalUtil.parseFromObject(arr[6]);// 年化收益率区间
		BigDecimal incomeCalcBasis = BigDecimalUtil.parseFromObject(arr[4]);// 收益计算基础
		String assetPoolOid = arr[7] == null ? "" : arr[7].toString();// 所属资产池
		if (null != cacheHold.getConfirmDate() && DateUtil.daysBetween(incomeDate, cacheHold.getConfirmDate()) == 0) {
			rep.setYesterdayIncome(cacheHold.getHoldYesterdayIncome());// 昨日收益
		}

		rep.setTotalIncome(BigDecimalUtil.parseFromObject(arr[0]));// 累计收益

		rep.setMinRredeem(cacheProduct.getMinRredeem());// 单笔赎回最低金额
		rep.setMaxRredeem(cacheProduct.getMaxRredeem());// 单笔赎回最大金额
		rep.setAdditionalRredeem(cacheProduct.getAdditionalRredeem());// 单笔赎回递增金额
		rep.setSingleDailyMaxRedeem(cacheProduct.getSingleDailyMaxRedeem());// 产品--单人单日赎回上限
		rep.setDailyNetMaxRredeem(cacheProduct.getDailyNetMaxRredeem());// 产品--剩余赎回金额
		rep.setNetMaxRredeemDay(cacheProduct.getNetMaxRredeemDay());// 产品--单日净赎回上限
		rep.setSingleDayRedeemCount(cacheProduct.getSingleDayRedeemCount());
		rep.setDayRedeemCount(cacheHold.getDayRedeemCount());
		rep.setTotalValue(cacheHold.getHoldVolume());// 持有金额
		rep.setDayRedeemVolume(cacheHold.getDayRedeemVolume());// 投资者--今日赎回金额
		rep.setRedeemableHoldVolume(cacheHold.getRedeemableHoldVolume());// 可赎回金额

		if(incomeRewardCacheService.hasRewardIncome(productOid)) {
			Product product = new Product();
			product.setRredeemDateType((String)arr[17]);
			product.setRedeemConfirmDays((Integer)arr[18]);
			rep.setPayDate(orderDateService.getRedeemDate(product, new Timestamp(System.currentTimeMillis())));
		} else {
			//快活宝返回大额赎回日期
			Product product = new Product();
			product.setRredeemDateType("T");
			product.setRedeemConfirmDays(1);
			rep.setPayDate(orderDateService.getRedeemDate(product, new Timestamp(System.currentTimeMillis())));
		}
		// 预期收益率
		BigDecimal expArorMid = expArorSec == null ? expAror : (expAror.add(expArorSec).divide(new BigDecimal("2.0")));
		// 万元收益=10000*预期年化收益率/收益计算基础
		if (incomeCalcBasis.compareTo(SysConstant.BIGDECIMAL_defaultValue) != 0) {
			rep.setMillionIncome(InterestFormula.compound(new BigDecimal("10000"), expArorMid, cacheProduct.getIncomeCalcBasis()));
//			rep.setMillionIncome(new BigDecimal("10000").multiply(expArorMid).divide(incomeCalcBasis, 4,
//					DecimalUtil.roundMode));
		}

		// 折线图最近日期
		Date lastedDate = DateUtil.addSQLDays(DateUtil.getSqlDate(), -1);

		// 实际收益分配的产品收益率
		Page<IncomeAllocate> pcas = this.incomeAllocateService.getProductIncomeAllocate(assetPoolOid, 30);
		if (pcas != null && pcas.getContent() != null && pcas.getTotalElements() > 0) {
			for (IncomeAllocate ia : pcas) {
				// 年化收益率走势 单位（%）
				rep.getExpArorList().add(0, new MyCurrProTendencyChartRep(DateUtil.format(ia.getBaseDate(), "MM-dd"),
						ProductDecimalFormat.multiply(ia.getRatio())));

				// 万份收益走势 单位（元）
				rep.getMillionIncomeList().add(0,
						new MyCurrProTendencyChartRep(DateUtil.format(ia.getBaseDate(), "MM-dd"), ia.getWincome()));

				lastedDate = ia.getBaseDate();
			}
		}

		int size = rep.getExpArorList().size();
		if (size < 30) {
			for (int i = 0; i < 30 - size; i++) {
				lastedDate = DateUtil.addSQLDays(lastedDate, -1);
				// 年化收益率走势 单位（%）,补足30条数据
				rep.getExpArorList().add(0, new MyCurrProTendencyChartRep(DateUtil.format(lastedDate, "MM-dd"),
						ProductDecimalFormat.multiply(expArorMid)));
				// 万份收益走势 单位（元）,补足30条数据
				rep.getMillionIncomeList().add(0,
						new MyCurrProTendencyChartRep(DateUtil.format(lastedDate, "MM-dd"), rep.getMillionIncome()));
			}
		}

		// 实际收益计算方式：
		// 有昨日实际收益时=奖励收益率+昨日实际奖励收益率
		// 无昨日实际收益率时=奖励收益率+奖励收益率+预期收益
		BigDecimal exp;
		if (pcas != null && pcas.getContent() != null && pcas.getTotalElements() > 0) {
			exp = ProductDecimalFormat.multiply(pcas.getContent().get(0).getRatio());
		} else {
			exp = ProductDecimalFormat.multiply(expArorMid);
		}

		// 我的产品分档明细
		List<LevelIncomeRep> levelList = levelIncomeService.queryLevelDetialsForInvestor(investorOid, productOid);
		if (levelList != null && levelList.size() > 0) {
			for (LevelIncomeRep entity : levelList) {
				// 实际收益率
				entity.setRatio(entity.getRatio().add(exp));
				// 昨日总收益率已经在sql中取出来了。这里不再加上昨日收益
			}
			// 产品分档明细
			rep.setLevelList(levelList);

			// 奖励收益率图表
			List<MyCurrProTendencyChartRep> rewardRadioList = new ArrayList<MyCurrProTendencyChartRep>();
			for (LevelIncomeRep entity : levelList) {
				MyCurrProTendencyChartRep levelRadio = new MyCurrProTendencyChartRep();
				levelRadio.setFloorday(entity.getStartDate());
				levelRadio.setVertical(entity.getRatio());// 实际收益率

				if (entity.getStartDate() == null && entity.getEndDate() != null) {
					levelRadio.setAxis("小于等于" + entity.getEndDate() + "天");
				} else if (entity.getStartDate() != null && entity.getEndDate() == null) {
					levelRadio.setAxis("大于等于" + entity.getStartDate() + "天");
				} else if (entity.getEndDate() != null && entity.getStartDate() != null) {
					if (entity.getEndDate().intValue() == entity.getStartDate().intValue()) {
						levelRadio.setAxis(entity.getStartDate() + "天");
					} else {
						levelRadio.setAxis(entity.getStartDate() + "天-" + entity.getEndDate() + "天");
					}
				}
				rewardRadioList.add(levelRadio);
			}
			rep.setRewardRadioList(rewardRadioList);
		}

		return rep;
	}

	/**
	 * 查询我的持有中定期产品列表
	 *
	 * @param userOid
	 * @return
	 */
	public PagesRep<Object> myholdregular(String userOid) {
		PagesRep<Object> pagesRep = new PagesRep<Object>();
		// 查询我的活期持有中产品列表
		List<Object[]> holdList = this.publisherHoldDao.myHoldregular(userOid);

		if (holdList != null && holdList.size() > 0) {
			List<Object> rows = new ArrayList<Object>();
			for (Object[] arr : holdList) {
				MyHoldRegularProQueryRep rep = new MyHoldRegularProQueryRep();
				rep.setProOid(null == arr[0] ? "" : arr[0].toString());// 产品ID
				rep.setProName(null == arr[1] ? "" : arr[1].toString());// 产品名称
				rep.setExpAror(BigDecimalUtil.parseFromObject(arr[2]).multiply(new BigDecimal("100.00")));// 预期年化收益率
				rep.setInvestAmt(BigDecimalUtil.parseFromObject(arr[3]));// 投资金额
				rep.setIncomeTyp("到期还本付息");// 收益方式,暂时库里没有存，写死
				rep.setStatus(null == arr[5] ? "" : arr[5].toString());// 状态
				rep.setCloAmount(BigDecimalUtil.parseFromObject(arr[6]));// 结算中金额(结算中时显示)
				try {
					if (arr[4] != null) {
						rep.setDDate(DateUtil.parse(arr[4].toString()));// 到期日
					}
				} catch (Exception e) {
				}

				rows.add(rep);
			}
			pagesRep.setRows(rows);
			pagesRep.setTotal(rows.size());
		}
		return pagesRep;
	}

	/**
	 * 查询我的申请中定期产品列表
	 *
	 * @param userOid
	 * @return
	 */
	public PagesRep<Object> myapplyregular(String userOid) {
		PagesRep<Object> pagesRep = new PagesRep<Object>();
		List<Object[]> holdList = this.publisherHoldDao.myApplyregular(userOid);

		if (holdList != null && holdList.size() > 0) {
			List<Object> rows = new ArrayList<Object>();
			for (Object[] arr : holdList) {
				MyApplyRegularProQueryRep rep = new MyApplyRegularProQueryRep();
				rep.setProOid(null == arr[0] ? "" : arr[0].toString());// 产品ID
				rep.setProName(null == arr[1] ? "" : arr[1].toString());// 产品名称
				rep.setAcceptedAmt(BigDecimalUtil.parseFromObject(arr[3]));// 已受理金额
				rep.setToAcceptAmt(BigDecimalUtil.parseFromObject(arr[2]));// 待受理金额
				rep.setReAmount(BigDecimalUtil.parseFromObject(arr[4]));// 退款中金额(结算中时显示)
				rep.setApplyStatus(null == arr[5] ? "" : arr[5].toString());// 申请状态

				rows.add(rep);
			}
			pagesRep.setRows(rows);
			pagesRep.setTotal(rows.size());
		}
		return pagesRep;
	}

	/**
	 * 查询我的已结清定期产品列表
	 *
	 * @param userOid
	 * @return
	 */
	public PagesRep<Object> myclosedregular(String userOid) {
		PagesRep<Object> pagesRep = new PagesRep<Object>();
		List<Object[]> holdList = this.publisherHoldDao.myClosedregular(userOid);

		if (holdList != null && holdList.size() > 0) {
			List<Object> rows = new ArrayList<Object>();
			for (Object[] arr : holdList) {
				MyClosedRegularProQueryRep rep = new MyClosedRegularProQueryRep();
				rep.setProOid(null == arr[0] ? "" : arr[0].toString());// 产品ID
				rep.setProName(null == arr[1] ? "" : arr[1].toString());// 产品名称
				rep.setInvestAmt(BigDecimalUtil.parseFromObject(arr[2]));// 投资金额
				rep.setTotalIncome(BigDecimalUtil.parseFromObject(arr[7]));// 总收益
				rep.setStatus(arr[6] == null ? "" : arr[6].toString());// 状态
				try {
					if ("refunded".equals(arr[6])) {
						rep.setRefundDate(DateUtil.parse(arr[5].toString()));// 退款日
						rep.setToRefundDate("1个交易日");// 预计退款日
					} else if ("refunding".equals(arr[6])) {
						rep.setToRefundDate("1个交易日");// 预计退款日
					} else {
						rep.setBuyDate(DateUtil.parse(arr[3].toString()));// 购买日期
						rep.setRepayDate(DateUtil.parse(arr[4].toString()));// 还本付息日
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				rows.add(rep);
			}
			pagesRep.setRows(rows);
			pagesRep.setTotal(rows.size());
		}
		return pagesRep;
	}

	public int queryTotalInvestProductsByinvestorBaseAccount(String investorOid) {

		return this.publisherHoldDao.queryTotalInvestProductsByinvestorBaseAccount(investorOid);
	}

	/**
	 * 募集成立或失败之前，募集期收益划入本金
	 */
	@Transactional(TxType.REQUIRES_NEW)
	public void changeIncomeIntoHoldVolume(Product product) {

		if (null != product.getRecPeriodExpAnYield()
				&& product.getRecPeriodExpAnYield().compareTo(BigDecimal.ZERO) > 0) {
			this.publisherHoldDao.changeIncomeIntoHoldVolume(product);

		}
	}

	public int resetToday() {
		int i = this.publisherHoldDao.resetToday();
		return i;

	}

	public int resetToday(String holdOid) {
		int i = this.publisherHoldDao.resetToday(holdOid);
		return i;

	}

	/**
	 * 解锁定期本金
	 */
	@Transactional(TxType.REQUIRES_NEW)
	public int unlockCash(Product product) {
		int i = this.publisherHoldDao.unlockCash(product);
		return i;

	}
	/**
	 * 解锁定期本金和利息
	 */
	@Transactional(TxType.REQUIRES_NEW)
	public int unlockCashAndIncome(Product product) {
		int i = this.publisherHoldDao.unlockCashAndIncome(product);
		return i;

	}

	/** 发行人下投资人质量分析（某个投资金额范围内的投资人个数） */
	public List<Object[]> analyseInvestor(String publisherOid) {
		return this.publisherHoldDao.analyseInvestor(publisherOid);
	}

	public int countByInvestorBaseAccount(InvestorBaseAccountEntity investorBaseAccount) {
		return this.publisherHoldDao.countByInvestorBaseAccount(investorBaseAccount.getOid());
	}

	public int countByPublisherBaseAccountAndInvestorBaseAccount(InvestorBaseAccountEntity investorBaseAccount,
			PublisherBaseAccountEntity publisherBaseAccount) {
		return publisherHoldDao.countByPublisherBaseAccountAndInvestorBaseAccount(investorBaseAccount.getOid(),
				publisherBaseAccount.getOid());
	}

	/** 平台下-投资人质量分析（某个投资金额范围内的投资人个数） */
	public List<Object[]> analysePlatformInvestor() {
		return this.publisherHoldDao.analysePlatformInvestor();
	}

	public BaseRep getMaxHoldVol(String userOid, String productOid) {
		MaxHoldVolRep rep = new MaxHoldVolRep();

		BigDecimal maxHoldVol = this.publisherHoldDao
				.findMaxHoldVol(this.investorBaseAccountService.findByUid(userOid).getOid(), productOid);
		rep.setMaxHoldVol(maxHoldVol == null ? BigDecimal.ZERO : maxHoldVol);
		return rep;
	}

	/**
	 * 查看总持仓份额是否等于可赎回份额,并且是全部赎回
	 *
	 * @param product
	 * @param investorOid
	 * @return
	 */
	public void update4MinRedeem(InvestorTradeOrderEntity tradeOrder){
		Product product = tradeOrder.getProduct();

		int i = 1;
		if (product.getMinRredeem() != null) {//如果是全部赎回
			i = this.publisherHoldDao.update4MinRedeem(tradeOrder.getProduct(), tradeOrder.getInvestorBaseAccount(),
					tradeOrder.getOrderAmount());
		}

		if (i < 1) {
			if (null != product.getMinRredeem() && product.getMinRredeem().compareTo(BigDecimal.ZERO) != 0) {
				if (tradeOrder.getOrderVolume().compareTo(product.getMinRredeem()) < 0) {
					// error.define[30013]=不满足单笔赎回下限(CODE:30013)
					throw new AMPException(30013);
				}
			}

			if (null != product.getAdditionalRredeem() && product.getAdditionalRredeem().compareTo(BigDecimal.ZERO) != 0) {
				if (null != product.getMinRredeem() && product.getMinRredeem().compareTo(BigDecimal.ZERO) != 0) {
					if (tradeOrder.getOrderVolume().subtract(product.getMinRredeem()).remainder(product.getAdditionalRredeem()).compareTo(BigDecimal.ZERO) != 0) {
						// error.define[30039]=不满足赎回追加份额(CODE:30039)
						throw new AMPException(30039);
					}
				} else {
					if (tradeOrder.getOrderVolume().remainder(product.getAdditionalRredeem()).compareTo(BigDecimal.ZERO) != 0) {
						// error.define[30039]=不满足赎回追加份额(CODE:30039)
						throw new AMPException(30039);
					}
				}
			}
		}
	}

	public void batchUpdate(List<PublisherHoldEntity> holds) {
		this.publisherHoldDao.save(holds);
	}

	public PublisherHoldEntity findByProductAndInvestorBaseAccount(Product product,
			InvestorBaseAccountEntity investorBaseAccount) {
		return this.publisherHoldDao.findByInvestorBaseAccountAndProduct(investorBaseAccount, product);
	}

	public List<PublisherHoldEntity> findByInvestorOid(String investorOid) {
		return this.publisherHoldDao.findByInvestorOid(investorOid);
	}


	/**
	 * 批量获取持仓列表
	 * @param lastOid
	 * @return
	 */
	public List<PublisherHoldEntity> getHoldByBatch(String lastOid){
		return this.publisherHoldDao.getHoldByBatch(lastOid);
	}

	/**
	 * 获取SPV持仓列表
	 * @return
	 */
	public List<PublisherHoldEntity> getSPVHold() {
		return this.publisherHoldDao.getSPVHold();
	}
	/**
	 * 根据产品ID获取SPV持仓列表
	 * @param productOid
	 * @return
	 */
	public List<Object[]> getSPVHoldByProductOid(String productOid){
		return this.publisherHoldDao.getSPVHoldByProductOid(productOid);
	}

	/**
	 * 我的定期
	 */
	public BaseRep queryMyTnHoldProList(String investorOid) {
		MyHoldTnQueryRep rep = new MyHoldTnQueryRep();
		Pages<HoldingTnDetail> holdingTnDetails = new Pages<HoldingTnDetail>();
		Pages<ClosedTnDetail> closedTnDetails = new Pages<ClosedTnDetail>();

		rep.setHoldingTnDetails(holdingTnDetails);
		rep.setClosedTnDetails(closedTnDetails);

		List<HoldCacheEntity> holds = this.cacheHoldService.findByInvestorOid(investorOid);
		Date incomeDate = DateUtil.getBeforeDate();
		for (HoldCacheEntity hold : holds) {
			ProductCacheEntity cacheProduct = this.cacheProductService.getProductCacheEntityById(hold.getProductOid());
			if (Product.TYPE_Producttype_02.equals(cacheProduct.getType())) {
				continue;
			}
			rep.setTnCapitalAmount(rep.getTnCapitalAmount().add(hold.getTotalVolume()));
			rep.setTotalIncomeAmount(rep.getTotalIncomeAmount().add(hold.getHoldTotalIncome()));
			if (null != hold.getConfirmDate() && DateUtil.daysBetween(incomeDate, hold.getConfirmDate()) == 0) {
				rep.setTnYesterdayIncome(rep.getTnYesterdayIncome().add(hold.getHoldYesterdayIncome()));
			}

			/** 已完成 */
			if (PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_closed.equals(hold.getHoldStatus()) ||
					PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_refunded.equals(hold.getHoldStatus())) {
				ClosedTnDetail closedTnDetail = new ClosedTnDetail();
				closedTnDetail.setExpAror(DecimalUtil.zoomOut(cacheProduct.getExpAror(), 100) + "%"); // 预计年化收益
				closedTnDetail.setExpArorSec(DecimalUtil.zoomOut(cacheProduct.getExpArorSec(), 100) + "%");
				//-----新增关联竞猜活动-------2017.06.29-----
				closedTnDetail.setRelatedGuess(cacheProduct.getGuessOid()!=null?1:0);
				//-----新增关联竞猜活动-------2017.06.29-----
				closedTnDetail.setOrderAmount(hold.getTotalInvestVolume()); // 投资金额
				if (PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_closed.equals(hold.getHoldStatus())) {
					closedTnDetail.setIncome(hold.getHoldTotalIncome()); //实际收益
					closedTnDetail.setSetupDate(cacheProduct.getSetupDate()); // 起息日期
					closedTnDetail.setRepayDate(cacheProduct.getRepayDate()); // 结息日期
					closedTnDetail.setStatus(PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_closed);
					closedTnDetail.setStatusDisp("已结清");
				}
				if (PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_refunded.equals(hold.getHoldStatus())) {
					closedTnDetail.setIncome(hold.getHoldTotalIncome()); // 收益
					closedTnDetail.setRaiseFailDate(cacheProduct.getRaiseFailDate()); //退款日期
					//-------------为了兼容pc---------2017.04.20---------
					closedTnDetail.setSetupDate(cacheProduct.getRaiseFailDate()); // 退款日期
					closedTnDetail.setRepayDate(cacheProduct.getRaiseFailDate()); // 退款日期
					//-------------为了兼容pc---------2017.04.20---------
					closedTnDetail.setStatus(PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_refunded);
					closedTnDetail.setStatusDisp("已退款");
				}
				closedTnDetail.setLatestOrderTime(hold.getLatestOrderTime()); // 最近一次投资时间
				closedTnDetail.setProductOid(cacheProduct.getProductOid());
				closedTnDetail.setProductName(cacheProduct.getName());
				closedTnDetails.add(closedTnDetail);
				continue;
			}

			/** 持有中 */
			if (PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_holding.equals(hold.getHoldStatus()) ||
					PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_toConfirm.equals(hold.getHoldStatus())) {

				if (Product.STATE_Raising.equals(cacheProduct.getState()) || Product.STATE_Raiseend.equals(cacheProduct.getState())) {
					HoldingTnDetail holdingTnDetail = new HoldingTnDetail();
					holdingTnDetail.setExpAror(DecimalUtil.zoomOut(cacheProduct.getExpAror(), 100) + "%");  // 预期年化收益
					holdingTnDetail.setExpArorSec(DecimalUtil.zoomOut(cacheProduct.getExpArorSec(), 100) + "%");
					//-----新增关联竞猜活动-------2017.06.29-----
					holdingTnDetail.setRelatedGuess(cacheProduct.getGuessOid()!=null?1:0);
					//-----新增关联竞猜活动-------2017.06.29-----
					holdingTnDetail.setExpectIncome(hold.getExpectIncome()); //预计收益
					holdingTnDetail.setExpectIncomeExt(hold.getExpectIncomeExt());
					holdingTnDetail.setOrderAmount(hold.getTotalInvestVolume()); //投资金额
					holdingTnDetail.setLatestOrderTime(hold.getLatestOrderTime()); //最近一次投资时间
					holdingTnDetail.setProductOid(cacheProduct.getProductOid());
					holdingTnDetail.setProductName(cacheProduct.getName());
					holdingTnDetail.setSetupDate(cacheProduct.getSetupDate()); //起息日期
					holdingTnDetail.setDurationPeriodEndDate(cacheProduct.getDurationPeriodEndDate()); //结息日期
					holdingTnDetail.setRepayDate(cacheProduct.getRepayDate());

					if (Product.STATE_Raising.equals(cacheProduct.getState())) {
						holdingTnDetail.setStatus(Product.STATE_Raising);
						holdingTnDetail.setStatusDisp("募集中");
					} else {
						holdingTnDetail.setStatus(Product.STATE_Raiseend);
						holdingTnDetail.setStatusDisp("募集结束");
					}
					holdingTnDetails.add(holdingTnDetail);
					continue;
				}

				if (Product.STATE_Durationing.equals(cacheProduct.getState()) ||
						Product.STATE_Durationend.equals(cacheProduct.getState())) {
					HoldingTnDetail holdingTnDetail = new HoldingTnDetail();
					holdingTnDetail.setExpAror(DecimalUtil.zoomOut(cacheProduct.getExpAror(), 100) + "%"); // 预期年化收益
					holdingTnDetail.setExpArorSec(DecimalUtil.zoomOut(cacheProduct.getExpArorSec(), 100) + "%");
					//-----新增关联竞猜活动-------2017.06.29-----
					holdingTnDetail.setRelatedGuess(cacheProduct.getGuessOid()!=null?1:0);
					//-----新增关联竞猜活动-------2017.06.29-----
					holdingTnDetail.setExpectIncome(hold.getExpectIncome()); //预计收益
					holdingTnDetail.setExpectIncomeExt(hold.getExpectIncomeExt());
					holdingTnDetail.setOrderAmount(hold.getTotalVolume()); //投资金额
					holdingTnDetail.setLatestOrderTime(hold.getLatestOrderTime()); //最近一次投资时间

					holdingTnDetail.setProductOid(cacheProduct.getProductOid());
					holdingTnDetail.setProductName(cacheProduct.getName());
					holdingTnDetail.setSetupDate(cacheProduct.getSetupDate()); //起息日期
					holdingTnDetail.setDurationPeriodEndDate(cacheProduct.getDurationPeriodEndDate()); //结息日期
					holdingTnDetail.setRepayDate(cacheProduct.getRepayDate());
					if (Product.STATE_Durationing.equals(cacheProduct.getState())) {
						holdingTnDetail.setStatus(Product.STATE_Durationing);
						holdingTnDetail.setStatusDisp("持有中");
					} else {
						holdingTnDetail.setStatus(Product.STATE_Durationend);
						holdingTnDetail.setStatusDisp("到期处理中");
					}

					holdingTnDetails.add(holdingTnDetail);
				}
			}
		}

		holdingTnDetails.setTotal(holdingTnDetails.getRows().size());
		closedTnDetails.setTotal(closedTnDetails.getRows().size());

		return rep;
	}
	/**
	 *
	 * @author yihonglei
	 * @Title: tnHoldList
	 * @Description: 我的定期
	 * @param req
	 * @param uid
	 * @return MyTnClientRep<Map<String,Object>>
	 * @date 2017年9月8日 下午2:17:45
	 * @since  1.0.0
	 */
	public MyTnClientRep<Map<String, Object>> tnHoldList(MyTnClientReq req, String uid) {
		// 客户端请求响应结果
		MyTnClientRep<Map<String, Object>> rep = new MyTnClientRep<>();
		List<Map<String,Object>> tnHoldList = new ArrayList<Map<String,Object>>();
		// 我的定期列表查询
		List<Object[]> objTnHoldList = this.publisherHoldDao.getTnHoldList(
				uid, req.getHoldStatus(), req.getTnStartDate(), req.getTnEndDate(),
				(req.getPage() - 1) * req.getRow(), req.getRow());
		// 我的定期总条数查询
		int total = this.publisherHoldDao.getTnHoldCount(uid, req.getHoldStatus(), req.getTnStartDate(), req.getTnEndDate());
		objTnHoldList.stream().forEach(o->{
			System.out.println("---o.lenth:"+o.length+"第leng-1个"+o[o.length-1]+"第leng-2个"+o[o.length-2]+"第leng-3个"+o[o.length-3]);
			if((Integer)o[29] == 0) {
				//如果不是活动产品，则是普通定期产品
				o[29] = ProductPojo.DEPOSIT_SUBTYPE;
			}else {
				//如果是活动产品,则对应0元购
				o[29] = ProductPojo.ZERO_BUY_SUBTYPE;
			}
		});
		// 1. 汇总数据处理(定期总资产,定期累计收益,定期预期收益)
		rep = tnAmountSum(uid);
		if (objTnHoldList.size() > 0) {
			// 2. 定期列表数据处理
			tnHoldList = tnObj2MapList(objTnHoldList);
				//组装数据 增加字段 rateCouponIncome 加息券收益 
				tnHoldList.parallelStream().filter(m->Objects.equals(CardTypes.CARDVOLUME,m.get("couponType"))).forEach(m->{
					BigDecimal interestDays=new BigDecimal(Objects.toString(m.get("interestDays"),"0"));
					BigDecimal orderAmount=new BigDecimal(Objects.toString(m.get("orderAmount"),"0"));
					BigDecimal incomeCalcBasis=new BigDecimal(Objects.toString(m.get("incomeCalcBasis"),"0"));
					String addInterestStr=Objects.toString(m.get("addInterest"),"0").replaceAll("%", "");
					BigDecimal addInterest=new BigDecimal(addInterestStr);
					BigDecimal  ratio =addInterest.divide(BigDecimal.valueOf(100)).setScale(4,BigDecimal.ROUND_HALF_UP);
					BigDecimal interestIncome=orderAmount.multiply(ratio).multiply(interestDays)
							.divide(incomeCalcBasis, 2, BigDecimal.ROUND_DOWN);
//					String income=interestIncome.toString();
					m.remove("interestDays");
					m.remove("couponType");
					m.remove("incomeCalcBasis");
					m.put("rateCouponIncome", interestIncome);
				});
//			}
			
		}
		
		// 分页数据处理
		rep.setRows(tnHoldList);
		rep.setTotal(total);
		rep.setRow(req.getRow());
		rep.setPage(req.getPage());
		rep.reTotalPage();

		return rep;
	}
	/**
	 *
	 * @author yihonglei
	 * @Title: tnAmountSum
	 * @Description: 汇总数据处理(定期总资产,定期累计收益,定期预期收益)
	 * 使用原定期汇总逻辑，不能根据订单汇总，会有小数点保留位数不一致问题
	 * @param investorOid
	 * @return MyTnClientRep<Map<String,Object>>
	 * @date 2017年9月8日 下午2:33:38
	 * @since  1.0.0
	 */
	private MyTnClientRep<Map<String, Object>> tnAmountSum(String investorOid ) {
		MyTnClientRep<Map<String, Object>> rep = new MyTnClientRep<>();
		List<HoldCacheEntity> holds = this.cacheHoldService.findByInvestorOid(investorOid);
		for (HoldCacheEntity hold : holds) {
			ProductCacheEntity cacheProduct = this.cacheProductService.getProductCacheEntityById(hold.getProductOid());
			if (Product.TYPE_Producttype_01.equals(cacheProduct.getType())) {
				Product product = productService.getProductByOid(cacheProduct.getProductOid());
				if(!product.getIsP2PAssetPackage().equals(Product.IS_P2P_ASSET_PACKAGE_2)){
					rep.setTnCapitalAmount(rep.getTnCapitalAmount().add(hold.getTotalVolume())); // 定期总资产
					rep.setTotalIncomeAmount(rep.getTotalIncomeAmount().add(hold.getHoldTotalIncome())); // 定期累计收益

					if (PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_holding.equals(hold.getHoldStatus()) ||
							PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_toConfirm.equals(hold.getHoldStatus())) {
						rep.setTotalExpectIncomeAmount(rep.getTotalExpectIncomeAmount().add(hold.getExpectIncome()));// 定期预期收益(累计)
					}
				}
			}
		}
		return rep;
	}
	/**
	 *
	 * @author yihonglei
	 * @Title: tnObj2MapList
	 * @Description: 定期列表数据处理(List<Object[]>转换为List<Map<String,Object>>)
	 * @param objTnHoldList
	 * @return List<Map<String,Object>>
	 * @date 2017年9月8日 下午2:34:59
	 * @since  1.0.0
	 */
	private List<Map<String,Object>> tnObj2MapList(List<Object[]> objTnHoldList) {
		List<Map<String,Object>> tnHoldList = new ArrayList<Map<String,Object>>();
		try {
			for (int i = 0; i < objTnHoldList.size(); i ++ ) {
				Map<String,Object> mapData = new HashMap<String,Object>();
				Object[] objArr = objTnHoldList.get(i);
				mapData.put("orderOid", objArr[0]); // 订单Oid
				mapData.put("productOid", objArr[1]); // 产品Oid
				mapData.put("productName", objArr[2]); // 产品名称
				mapData.put("relatedGuess", objArr[3]); // 1：关联竞猜宝 0：不关联竞猜宝
				mapData.put("productStatus", objArr[4]); // 产品状态
				mapData.put("orderStatus", objArr[5]); // 订单状态
				mapData.put("holdStatus", objArr[6]); // 订单持有状态
				mapData.put("durationPeriodDays", objArr[7]); // 投资期限
				mapData.put("payAmount", objArr[8]); // 实付本金
				mapData.put("couponAmount", objArr[9]); // 红包抵扣金额
				mapData.put("addInterest", objArr[10]); // 加息
				mapData.put("orderAmount", objArr[11]); // 订单金额
				mapData.put("expAror", objArr[12]); // 预期年化收益
				mapData.put("expectIncome", objArr[13]); // 预期收益
				mapData.put("realRatio", objArr[14]); // 实际年化收益(年化收益率)
				mapData.put("investTime", objArr[15]); // 投资时间(yyyy-MM-dd HH:mm:ss)
				mapData.put("payDate", objArr[16]); // 购买日(yyyy-MM-dd)。
				mapData.put("setupDate", objArr[17]); // 募集开始时间(起息日)
				mapData.put("durationPeriodEndDate", objArr[18]); // 到期日
				mapData.put("repayDate", objArr[19]); // 预计到账日
				mapData.put("realCashDate", objArr[20]); // 实际到账日
				mapData.put("raiseFailDate", objArr[21]); // 募集失败时间
				mapData.put("instruction", objArr[22]); // 产品协议和要素地址
				mapData.put("realIncome", objArr[23]); // 实际收益
				mapData.put("expectedArrorDisp", objArr[24]);
				mapData.put("activityDetailUrl", objArr[25]);
				mapData.put("interestDays", objArr[26]);
				mapData.put("couponType", objArr[27]);
				mapData.put("incomeCalcBasis", objArr[28]);
				mapData.put("subType", objArr[29]);
				int isP2PAssetPackage = (int) objArr[30];
				mapData.put("isP2PAssetPackage", isP2PAssetPackage == 0 ? 0 : 1);
				// 产品协议
				Product product = productService.getProductByOid(objArr[1].toString());
				ProductCurrentDetailResp pr = new ProductCurrentDetailResp(product);
				Map<String, AdminObj> adminObjMap = new HashMap<String, AdminObj>();
				FileResp fr = null;
				AdminObj adminObj = null;
				if (!StringUtil.isEmpty(product.getServiceFileKey())) {
					List<File> files = this.fileService.list(product.getServiceFileKey(), File.STATE_Valid);
					if (files.size() > 0) {
						pr.setServiceFiles(new ArrayList<FileResp>());

						for (File file : files) {
							fr = new FileResp(file);
							if (adminObjMap.get(file.getOperator()) == null) {
								try {
									adminObj = adminSdk.getAdmin(file.getOperator());
									adminObjMap.put(file.getOperator(), adminObj);
								} catch (Exception e) {
								}
							}
							if (adminObjMap.get(file.getOperator()) != null) {
								fr.setOperator(adminObjMap.get(file.getOperator()).getName());
							}
							pr.getServiceFiles().add(fr);
						}
					}
				}
				mapData.put("serviceFiles", pr.getServiceFiles()); // 产品协议

				tnHoldList.add(mapData);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return tnHoldList;
	}


	/**
	 * 查询定期  我的持有中 产品详情
	 */
	public TnHoldingDetail queryTnHoldingDetail(String investorOid, String productOid) {
		TnHoldingDetail rep = new TnHoldingDetail();


		HoldCacheEntity holdCacheEntity = this.cacheHoldService.getHoldCacheEntityByInvestorOidAndProductOid(investorOid, productOid);
		if (PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_closed.equals(holdCacheEntity.getHoldStatus())) {
			rep.setInvestVolume(holdCacheEntity.getTotalInvestVolume());
			rep.setPayAmount(holdCacheEntity.getTotalInvestVolume().add(holdCacheEntity.getHoldTotalIncome()));
		} else {
			rep.setInvestVolume(holdCacheEntity.getTotalVolume());
			rep.setPayAmount(holdCacheEntity.getTotalVolume().add(holdCacheEntity.getExpectIncome()));
		}
		rep.setHoldStatus(holdCacheEntity.getHoldStatus());

		rep.setHoldTotalIncome(holdCacheEntity.getHoldTotalIncome());
		rep.setExpectIncome(holdCacheEntity.getExpectIncome());
		rep.setExpectIncomeExt(holdCacheEntity.getExpectIncomeExt());

		ProductCacheEntity productCacheEntity = this.cacheProductService.getProductCacheEntityById(productOid);
		rep.setExpAror(DecimalUtil.zoomOut(productCacheEntity.getExpAror(), 100)  + "%");// 预计收益率
		rep.setExpArorExt(DecimalUtil.zoomOut(productCacheEntity.getExpArorSec(), 100) + "%");

		rep.setLatestOrderTime(holdCacheEntity.getLatestOrderTime()); //最近一次投资时间
		rep.setRaiseStartDate(productCacheEntity.getRaiseStartDate());
		rep.setRaiseEndDate(productCacheEntity.getRaiseEndDate());
		rep.setSetupDate(productCacheEntity.getSetupDate()); // 起息日
		rep.setDurationPeriodEndDate(productCacheEntity.getDurationPeriodEndDate()); //到期日(即到期日)
		rep.setRepayDate(productCacheEntity.getRepayDate()); // 还本付息日(即到账日)

		return rep;
	}

	public PublisherHoldEntity findByInvestorBaseAccountAndProduct(InvestorBaseAccountEntity investorBaseAccount,
			Product product) {
		return this.publisherHoldDao.findByInvestorBaseAccountAndProduct(investorBaseAccount, product);
	}



	public List<PublisherHoldEntity> findByProductPaged(Product product, String accountType, String lastOid) {
		return this.publisherHoldDao.findByProductPaged(product.getOid(), accountType, lastOid);
	}

	public List<PublisherHoldEntity> findByProductPaged(String productOid, String accountType, String lastOid) {
		return this.publisherHoldDao.findByProductPaged(productOid, accountType, lastOid);
	}


	/**
	 * 获取含有体验金的用户
	 */
	public List<PublisherHoldEntity> getAllExpHolds(String lastOid) {

		return this.publisherHoldDao.getAllExpHolds(lastOid);
	}

	public List<PublisherHoldEntity> getResetTodayHold(String lastOid) {

		return this.publisherHoldDao.getResetTodayHold(lastOid);
	}

	public List<Object[]> getHoldByUserOid(String userOid) {
		return this.publisherHoldDao.getHoldByUserOid(userOid);
	}

	public void dealHold() {
		String lastOid = "0";
		while (true) {
			List<PublisherHoldEntity> list = publisherHoldDao.dealHold(lastOid);
			if (list.isEmpty()) {
				break;
			}
			for (PublisherHoldEntity hold : list) {
				publisherHoldServiceNew.dealHold(hold.getOid());
				lastOid = hold.getOid();
			}
			logger.info("2000 dealHold");
		}
	}

	public JjgHoldDetailRep queryMyJjgHoldProList(String investorOid) {
		JjgHoldDetailRep rep = new JjgHoldDetailRep();
		Date incomeDate = DateUtil.getBeforeDate();
		List<ProductIncomeRewardSnapshot> awardProductList = incomeRewardSnapshotService.findBySnapshotDate(incomeDate);
		List<String> productIdList = getProductList(awardProductList);
		String radio = "-1";
		JjgProfitRangeDetail detail = null;
		//修正订单和持仓差额数据
		BigDecimal totalAmount = BigDecimal.ZERO;
		List<HoldCacheEntity> holds = cacheHoldService.findByInvestorOid(investorOid);
		List<JjgProfitRangeDetail> incomeList = new ArrayList<>();
		if (!awardProductList.isEmpty()) {
			rep.setProductOid(productIdList.get(0));
			Product product = this.productDao.findOne(rep.getProductOid());
			List<TradeOrderHoldDaysDetail> snapShotList = investorTradeOrderService.getCurrentDaysDetailByInvestor(investorOid, rep.getProductOid());
			for (ProductIncomeRewardSnapshot rewardSnapshot : awardProductList) {
				if (!rewardSnapshot.getProductOid().equals(rep.getProductOid())) {
					continue;
				}
				String realRadio = getRealRadio(rewardSnapshot.getRatio(), product);
				if (!radio.equals(realRadio)) {
					detail = new JjgProfitRangeDetail();
					rep.getRows().add(detail);
					detail.setStartDays(rewardSnapshot.getStartDate());
					detail.setProfit(realRadio);
					radio = realRadio;
				}
				detail.setEndDays(rewardSnapshot.getEndDate());
			}
			for(JjgProfitRangeDetail rangeDetail : rep.getRows()) {
				for (Iterator<TradeOrderHoldDaysDetail> it = snapShotList.iterator(); it.hasNext();) {
					TradeOrderHoldDaysDetail snapshotEntity = (TradeOrderHoldDaysDetail) it.next();
					if(snapshotEntity.getHoldDays() >= rangeDetail.getStartDays() && snapshotEntity.getHoldDays() <= rangeDetail.getEndDays()) {
						if(snapshotEntity.getHoldVolume().compareTo(snapshotEntity.getTotalIncome()) >= 0) {
							//持仓金额包括部分本金和全部利息
							rangeDetail.setRemainIncome(rangeDetail.getRemainIncome().add(snapshotEntity.getTotalIncome().setScale(2, BigDecimal.ROUND_DOWN)));
						} else {
							//剩余本金等于0，持仓都是利息
							rangeDetail.setRemainIncome(rangeDetail.getRemainIncome().add(snapshotEntity.getHoldVolume().setScale(2, BigDecimal.ROUND_DOWN)));
						}
						rangeDetail.setTotalAmount(rangeDetail.getTotalAmount().add(snapshotEntity.getHoldVolume().setScale(2, BigDecimal.ROUND_DOWN)));
						it.remove();
					}
				}
				rangeDetail.setTotalAmount(new BigDecimal(format(rangeDetail.getTotalAmount())));
				totalAmount = totalAmount.add(rangeDetail.getTotalAmount());
				incomeList.add(rangeDetail);
			}
			Collections.sort(incomeList, new IncomeDetailComparator());
		} else {
			productIdList = incomeRewardCacheService.getAwardProductOid();
			if(!productIdList.isEmpty()) {
				rep.setProductOid(productIdList.get(0));
				ProductCacheEntity cacheProduct = this.cacheProductService.getProductCacheEntityById(rep.getProductOid());
				List<ProductIncomeReward> rewardList = incomeRewardCacheService.readCache(rep.getProductOid());
				for (ProductIncomeReward reward : rewardList) {
					String realRadio = getRealRadio(reward.getRatio(), cacheProduct);
					if (!radio.equals(realRadio)) {
						detail = new JjgProfitRangeDetail();
						rep.getRows().add(detail);
						detail.setStartDays(reward.getStartDate());
						detail.setProfit(realRadio);
						radio = realRadio;
					}
					detail.setEndDays(reward.getEndDate());
				}
			}
		}
		for (HoldCacheEntity cacheHold : holds) {
			if (productIdList.contains(cacheHold.getProductOid())) {
				rep.setJjgCapitalAmount(rep.getJjgCapitalAmount().add(cacheHold.getTotalVolume()));
				rep.setJjgYesterdayIncome(rep.getJjgYesterdayIncome().add(cacheHold.getHoldYesterdayIncome()));
				rep.setTotalIncomeAmount(rep.getTotalIncomeAmount().add(cacheHold.getHoldTotalIncome()));
				rep.setToConfirmAmount(rep.getToConfirmAmount().add(cacheHold.getToConfirmInvestVolume()));
			}
		}
		rep.setHoldingAmount(rep.getJjgCapitalAmount().subtract(rep.getToConfirmAmount()));
		if(rep.getHoldingAmount().compareTo(totalAmount) > 0) {
            logger.error("持仓量不可能大于订单总和，请核对用户{}", investorOid);
        } else {
        	BigDecimal diff = totalAmount.subtract(rep.getHoldingAmount());
        	if(diff.compareTo(BigDecimal.ZERO) > 0) {
        		logger.debug("进入用户节节高持仓修正数据流程");
        		for(JjgProfitRangeDetail income : incomeList) {
        			if(diff.compareTo(income.getRemainIncome()) > 0) {
        				income.setTotalAmount(income.getTotalAmount().subtract(income.getRemainIncome()));
        				diff = diff.subtract(income.getRemainIncome());
        			} else {
        				income.setTotalAmount(income.getTotalAmount().subtract(diff));
        				break;
        			}
        		}
        	}
        }
		return rep;
	}

	private String getRealRadio(BigDecimal value, ProductCacheEntity cacheProduct) {
		return format(cacheProduct.getExpAror().add(value).multiply(BigDecimal.valueOf(100))) + "%";
	}

	private String getRealRadio(BigDecimal value, Product product) {
        Page<IncomeAllocate> incomeAllocateList = incomeAllocateService.getProductIncomeAllocate(product.getAssetPool().getOid(), 1);
        IncomeAllocate incomeAllocate = incomeAllocateList.getContent().get(0);
        BigDecimal basicRatio = product.getExpAror();
        if(incomeAllocate != null) {
            basicRatio = incomeAllocate.getRatio();
        }
		return format(basicRatio.add(value).multiply(BigDecimal.valueOf(100))) + "%";
	}

	private String format(BigDecimal value) {
		NumberFormat format=NumberFormat.getNumberInstance() ;
		format.setMaximumFractionDigits(2);
		format.setGroupingUsed(false);
		format.setRoundingMode(RoundingMode.DOWN);
		return format.format(value);
	}

	public List<String> getProductList(List<ProductIncomeRewardSnapshot> awardProductList) {
		List<String> productList = new ArrayList<>();
		for(ProductIncomeRewardSnapshot snapshot : awardProductList) {
			if(!productList.contains(snapshot.getProductOid())) {
				productList.add(snapshot.getProductOid());
			}
		}
		return productList;
	}

	public List<PublisherHoldEntity> findByCycleProductOidAndContinueStatus(String productOid, int continueStatus, String lastOid) {
		return publisherHoldDao.findByProductOidAndContinueStatus(productOid, continueStatus, lastOid);
	}

	private static class IncomeDetailComparator implements Comparator<JjgProfitRangeDetail> {
		@Override
		public int compare(JjgProfitRangeDetail detail1, JjgProfitRangeDetail detail2) {
			return detail1.getRemainIncome().compareTo(detail2.getRemainIncome()) * -1;
		}
	}

	public Map<String, Object> getHoldByInvestorAndProduct(String investorOid,String productOid){
		logger.info("====getHoldByInvestorAndProduct.investorOid:{},productOid:{}====", investorOid, productOid);
		Object[] holdObj = publisherHoldDao.findHoldByInvestorAndProduct(investorOid, productOid);
		Object[] hold = (Object[]) holdObj[0];
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("oid", hold[0]);
		map.put("productOid", hold[1]);
		map.put("investorOid", hold[2]);
		map.put("totalVolume", hold[3]);
		map.put("holdVolume", hold[4]);
		map.put("toConfirmInvestVolume", hold[5]);
		map.put("toConfirmRedeemVolume", hold[6]);
		map.put("totalInvestVolume", hold[7]);
		map.put("lockRedeemHoldVolume", hold[8]);
		map.put("redeemableHoldVolume", hold[9]);
		map.put("accruableHoldVolume", hold[10]);
		map.put("value", hold[11]);
		map.put("expGoldVolume", hold[12]);
		map.put("holdTotalIncome", hold[13]);
		map.put("holdYesterdayIncome", hold[14]);
		map.put("confirmDate", hold[15]);
		map.put("expectIncomeExt", hold[16]);
		map.put("expectIncome", hold[17]);
		map.put("dayRedeemVolume", hold[18]);
		map.put("dayInvestVolume", hold[19]);
		map.put("dayRedeemCount", hold[20]);
		map.put("maxHoldVolume", hold[21]);
		map.put("holdStatus", hold[22]);
		map.put("latestOrderTime", hold[23]);
		map.put("latestReadTime", System.currentTimeMillis());
		logger.info("====getHoldByInvestorAndProduct.hold:{}====", JSON.toJSONString(hold));
		return map;
	}

	/**
	 * @Desc: 快定宝订单列表
	 * @author huyong
	 * @date 2018/3/21 下午4:15
	 */
	public MyTnClientRep<Map<String, Object>> kdbHoldList(MyKdbClientReq req, String userOid) {
		// 客户端请求响应结果
		MyTnClientRep<Map<String, Object>> rep = new MyTnClientRep<Map<String, Object>>();
		List<Map<String,Object>> kdbHoldList = new ArrayList<Map<String,Object>>();
		if(req.getHoldStatus() != 1 && req.getHoldStatus() != 2){
			logger.info("无效的查询状态码，查询状态码：{}",req.getHoldStatus());
			return rep;
		}
		if (req.getHoldStatus() == 2){
			Collection<Object> ordercodetree=publisherHoldEMDao.getRelationTree(userOid);
			if (null==ordercodetree||ordercodetree.isEmpty()){
				rep.setRows(kdbHoldList);
				rep.setTotal(0);
				rep.setRow(req.getRow());
				rep.setPage(req.getPage());
				rep.setTotalPage(0);
				return rep;
			}
			req.setOrdercodetree(ordercodetree);

		}
		// 快定宝总条数查询
		int total = publisherHoldEMDao.getKDBHoldCount(req,userOid).intValue();
		// 汇总数据处理(快定宝总资产,快定宝累计收益,快定宝预期收益)
		rep = kdbAmountSum(userOid);
		if (total > 0) {
			// 快定宝列表查询
			kdbHoldList = publisherHoldEMDao.getKDBHoldList(req,userOid);
		}
		// 分页数据处理
		rep.setRows(kdbHoldList);
		rep.setTotal(total);
		rep.setRow(req.getRow());
		rep.setPage(req.getPage());
		rep.reTotalPage();
		return rep;
	}

	/**
	 * @Desc: 汇总数据处理(快定宝总资产,快定宝累计收益,快定宝预期收益)，使用原定期汇总逻辑，不能根据订单汇总，会有小数点保留位数不一致问题
	 * @author huyong
	 * @date 2018/3/21 下午4:15
	 */
	private MyTnClientRep<Map<String, Object>> kdbAmountSum(String investorOid) {
		MyTnClientRep<Map<String, Object>> rep = new MyTnClientRep<>();
		List<HoldCacheEntity> holds = this.cacheHoldService.findByInvestorOid(investorOid);
		for (HoldCacheEntity hold : holds) {
			ProductCacheEntity cacheProduct = this.cacheProductService.getProductCacheEntityById(hold.getProductOid());
			if (Product.TYPE_Producttype_03.equals(cacheProduct.getType())) {
				rep.setTnCapitalAmount(rep.getTnCapitalAmount().add(hold.getTotalVolume())); // 快定宝虚拟总资产
				if (PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_holding.equals(hold.getHoldStatus()) ||
						PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_toConfirm.equals(hold.getHoldStatus())) {
					rep.setTotalExpectIncomeAmount(rep.getTotalExpectIncomeAmount().add(hold.getExpectIncome()));// 快定宝预期收益(累计)
					rep.setTotalExpectIncomeAmount(rep.getTotalExpectIncomeAmount().add(hold.getExpectIncomeExt()));// 快定宝预期收益(累计)
				}
			}else if (Product.TYPE_Producttype_04.equals(cacheProduct.getType())) {
				rep.setTotalIncomeAmount(rep.getTotalIncomeAmount().add(hold.getHoldTotalIncome())); // 快定宝累计收益
				rep.setTnCapitalAmount(rep.getTnCapitalAmount().add(hold.getTotalVolume())); // 快定宝实产品总资产
				if (PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_holding.equals(hold.getHoldStatus()) ||
						PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_toConfirm.equals(hold.getHoldStatus())) {
					rep.setTotalExpectIncomeAmount(rep.getTotalExpectIncomeAmount().add(hold.getExpectIncome()));// 快定宝预期收益(累计)
				}
			}
		}
		return rep;
	}

	/**
	 * @Desc: 快定宝订单详情
	 * @author huyong
	 * @date 2018/3/21 下午4:15
	 */
	public Map<String, Object> kdbHoldDetail(String orderCode,String paramOrderType, String userOid) {
		// 客户端请求响应结果
		Map<String, Object> rep = new HashMap<String, Object>();
		List<FileResp> serviceFiles = new ArrayList<>();
		// 01:持有中预约订单申请中、已受理订单、02:持有中预约订单合成新的产品订单、03:持有中到期续投、04:持有中提前部分转出，未转出订单、
		// 05:已完成提前部分转出后原订单、06:已完成提前部分转出后已转出订单、07:已完成全部提前转出、08:已完成到期续投已失效、
		// 09:已完成到期未续投还本付息订单、99:未知类型,前端可不加载
		String orderSubType = "99";
		String isOriginal = "N";
		if(Stream.of(InvestorTradeOrderEntity.TRADEORDER_orderType_bfPlusRedeem,
				InvestorTradeOrderEntity.TRADEORDER_orderType_cash).anyMatch(c -> Objects.equals(c,paramOrderType))){
			// 查询赎回单和还本付息单
			InvestorOpenCycleRelationEntity openCycleRelationEntity = investorOpenCycleDao.getOrderByRedeemOrderCode(orderCode);
			if (null == openCycleRelationEntity) {
				logger.info("订单号：{}未查询到订单关系",orderCode);
				rep.put("orderSubType", orderSubType);
				return rep;
			}
			int assignment = openCycleRelationEntity.getAssignment();
			String cycleOrderType = openCycleRelationEntity.getOrderType();
			if (assignment == 3) {
				orderSubType = "06";
			} else if (assignment == 4) {
				orderSubType = "07";
			} else if (assignment == 5) {
				orderSubType = "09";
			}
			if(Objects.equals(InvestorTradeOrderEntity.TRADEORDER_orderType_bfPlusRedeem,paramOrderType)) {
				rep =  publisherHoldEMDao.getRedeemOriginalOrder(orderCode,userOid);
//				Map<String, Object> redeemOrder = publisherHoldEMDao.getRedeemOrder(orderCode,userOid);
//				rep.put("transfDate",redeemOrder.get("transfDate"));
//				rep.put("poundage",redeemOrder.get("poundage"));
//				rep.put("transfAmount",redeemOrder.get("transfAmount"));
				serviceFiles = productServiceFiles(rep.get("productOid").toString());
			}else{
				rep = publisherHoldEMDao.getKDBHoldDetail(orderCode,userOid);
				serviceFiles = productServiceFiles(rep.get("productOid").toString());
			}
		} else if(Stream.of(InvestorTradeOrderEntity.TRADEORDER_orderType_invest,
				InvestorTradeOrderEntity.TRADEORDER_orderType_noPayInvest).anyMatch(c -> Objects.equals(c,paramOrderType))){
			// 查询预约单
			orderSubType = "01";
			rep = publisherHoldEMDao.getKDBHoldDetail(orderCode,userOid);
			serviceFiles = productServiceFiles(rep.get("productOid").toString());
		}else {
			// 查询转投和续投订单
			rep = publisherHoldEMDao.getKDBHoldDetail(orderCode,userOid);
			serviceFiles = productServiceFiles(rep.get("productOid").toString());
			if (Objects.equals(Product.TYPE_Producttype_04,rep.get("subType"))) {
				Object holdStatus = rep.get("holdStatus");
				Object orderStatus = rep.get("orderStatus");
				InvestorOpenCycleRelationEntity openCycleRelationEntity = null;
				if(Objects.equals(InvestorTradeOrderEntity.TRADEORDER_orderStatus_invalidate,orderStatus)) {
					openCycleRelationEntity = investorOpenCycleDao.findOne(orderCode);
				} else {
					openCycleRelationEntity = investorOpenCycleDao.getOrderByInvestOrderCode(orderCode);
				}
				if (null == openCycleRelationEntity) {
					logger.info("订单号：{}未查询到订单关系",orderCode);
					rep.put("orderSubType", orderSubType);
					return rep;
				}
				int assignment = openCycleRelationEntity.getAssignment();
				String cycleOrderType = openCycleRelationEntity.getOrderType();
				if (Objects.equals(InvestorTradeOrderEntity.TRADEORDER_holdStatus_holding,holdStatus)) {
					openCycleRelationEntity = investorOpenCycleDao.findOne(orderCode);
					Boolean flag = checkHoldDate(rep.get("durationPeriodEndDate").toString());
					int continueStatus = openCycleRelationEntity.getContinueStatus();
					String isRedeem = flag ? "Y" : "N";
					rep.put("isRedeem",isRedeem);
					rep.put("continueStatus",continueStatus == 1 ? "Y" : "N");
					if (assignment == 1 && Objects.equals(InvestorOpenCycleRelationEntity.ORDERTYPE_BOOKING,cycleOrderType)) {
						orderSubType = "02";
					} else if (assignment == 2 && Stream.of(InvestorOpenCycleRelationEntity.ORDERTYPE_CHANGE,
							InvestorOpenCycleRelationEntity.ORDERTYPE_CONTINUE).anyMatch(c -> Objects.equals(c,cycleOrderType))) {
						orderSubType = "03";
					} else if (assignment == 3) {
						orderSubType = "04";
					}
					if (Objects.equals(InvestorTradeOrderEntity.TRADEORDER_orderType_changeInvest,rep.get("orderType"))) {
						List<Map<String, Object>> originalOrder = publisherHoldEMDao.getKDBOriginalOrder(orderCode,userOid);
						rep.put("originalOrder",originalOrder);
					}
					rep.remove("invalidDate");
				} else if (Stream.of(InvestorTradeOrderEntity.TRADEORDER_holdStatus_refunded,
						InvestorTradeOrderEntity.TRADEORDER_holdStatus_closed).anyMatch(c -> Objects.equals(c,holdStatus))) {
					if (assignment == 3 && Objects.equals(InvestorTradeOrderEntity.TRADEORDER_orderStatus_invalidate,orderStatus)) {
						orderSubType = "05";
						List<String> listOrderCode = new ArrayList<>();
						listOrderCode.add(openCycleRelationEntity.getInvestOrderCode());
						listOrderCode.add(openCycleRelationEntity.getRedeemOrderCode());
						List<Map<String, Object>> orderList = publisherHoldEMDao.getOrderTransf(listOrderCode,userOid);
						if (null != orderList && orderList.size() > 0) {
							isOriginal = "Y";
						}
						for (Map<String, Object> map : orderList) {
							String transfOrderType = map.get("orderType").toString();
							map.put("investDate",rep.get("investDate"));
							if (Stream.of(InvestorTradeOrderEntity.TRADEORDER_orderType_changeInvest,
									InvestorTradeOrderEntity.TRADEORDER_orderType_continueInvest).anyMatch(c -> Objects.equals(c,transfOrderType))) {
								map.remove("productName");
								map.remove("transfDate");
								map.remove("setupDate");
								map.remove("instruction");
								map.remove("poundage");
								map.remove("transfAmount");
								rep.put("notTransfer",map);
							} else if (Objects.equals(InvestorTradeOrderEntity.TRADEORDER_orderType_bfPlusRedeem,transfOrderType)) {
								map.remove("invalidDate");
								map.put("serviceFiles", serviceFiles); // 产品协议
								rep.put("hasTransfer",map);
							}
						}
					} else if (assignment == 2 || assignment == 3) {
						orderSubType = "08";
					} else if(assignment == 4) {
						orderSubType = "07";
					} else if (assignment == 5) {
						orderSubType = "09";
					}
				}
			}
		}
		// 01:持有中预约订单申请中、已受理订单、02:持有中预约订单合成新的产品订单、03:持有中到期续投、04:持有中提前部分转出，未转出订单、
		// 05:已完成提前部分转出后原订单、06:已完成提前部分转出后已转出订单、07:已完成全部提前转出、08:已完成到期续投已失效、
		// 09:已完成到期未续投还本付息订单、99:未知类型,前端可不加载
		rep.put("orderSubType", orderSubType);
		rep.put("isOriginal",isOriginal);
		rep.put("serviceFiles", serviceFiles); // 产品协议
		return rep;
	}

	/**
	 * @Desc: 快定宝产品协议
	 * @author huyong
	 * @date 2018/3/21 下午4:15
	 */
	public List<FileResp> productServiceFiles(String productOid){
		// 产品协议
		List<FileResp> serviceFiles = new ArrayList<>();
		Product product = productService.getProductByOid(productOid);
		ProductCurrentDetailResp pr = new ProductCurrentDetailResp(product);
		Map<String, AdminObj> adminObjMap = new HashMap<String, AdminObj>();
		FileResp fr = null;
		AdminObj adminObj = null;
		if (!StringUtil.isEmpty(product.getServiceFileKey())) {
			List<File> files = this.fileService.list(product.getServiceFileKey(), File.STATE_Valid);
			if (files.size() > 0) {
				pr.setServiceFiles(new ArrayList<FileResp>());

				for (File file : files) {
					fr = new FileResp(file);
					if (adminObjMap.get(file.getOperator()) == null) {
						try {
							adminObj = adminSdk.getAdmin(file.getOperator());
							adminObjMap.put(file.getOperator(), adminObj);
						} catch (Exception e) {
						}
					}
					if (adminObjMap.get(file.getOperator()) != null) {
						fr.setOperator(adminObjMap.get(file.getOperator()).getName());
					}
					pr.getServiceFiles().add(fr);
				}
			}
		}
		serviceFiles = pr.getServiceFiles();
		return serviceFiles;
	}

	/**
	 * @Desc: 比较订单是否在存续期内
	 * @author huyong
	 * @date 2018/3/21 下午4:15
	 */
	public static Boolean checkHoldDate(String date) {
		if (StringUtils.isBlank(date)) {
			return false;
		}
		LocalDate currentDate = LocalDate.now();
		LocalDate endDate = LocalDate.parse(date);
		if (endDate.compareTo(currentDate) >= 0) {
			return true;
		}
		return false;
	}

	public BigDecimal getTotalHoldAmount(String investorOid) {
		BigDecimal totalAmount = BigDecimal.ZERO;
		List<HoldCacheEntity> holds = cacheHoldService.findByInvestorOid(investorOid);
		for (HoldCacheEntity cacheHold : holds) {
			ProductCacheEntity cacheProduct = cacheProductService.getProductCacheEntityById(cacheHold.getProductOid());
			if (labelService.isProductLabelHasAppointLabel(cacheProduct.getProductLabel(), LabelEnum.tiyanjin.toString())) {
				continue;
			}
			totalAmount = totalAmount.add(cacheHold.getTotalVolume());
		}
		return totalAmount;
	}
	/**
	 *
	 * @author jiangjianmin
	 * @Title: ScatterHoldList
	 * @Description: 我的企业散标
	 * @param req
	 * @param uid
	 * @return MyTnClientRep<Map<String,Object>>
	 * @date 2017年9月8日 下午2:17:45
	 * @since  1.0.0
	 */
	public MyTnClientRep<Map<String, Object>> scatterHoldList(MyTnClientReq req, String uid) {
		// 客户端请求响应结果
		MyTnClientRep<Map<String, Object>> rep = new MyTnClientRep<>();
		List<Map<String,Object>> tnHoldList = new ArrayList<Map<String,Object>>();
		// 我的定期列表查询
		List<Object[]> objTnHoldList = this.publisherHoldDao.getScatterHoldList(
				uid, req.getHoldStatus(),
				(req.getPage() - 1) * req.getRow(), req.getRow());
		// 我的定期总条数查询
		int total = this.publisherHoldDao.getScatterHoldCount(uid, req.getHoldStatus());
		objTnHoldList.stream().forEach(o->{
			System.out.println("---o.lenth:"+o.length+"第leng-1个"+o[o.length-1]+"第leng-2个"+o[o.length-2]+"第leng-3个"+o[o.length-3]);
			if((Integer)o[29] == 0) {
				//如果不是活动产品，则是普通定期产品
				o[29] = ProductPojo.DEPOSIT_SUBTYPE;
			}else {
				//如果是活动产品,则对应0元购
				o[29] = ProductPojo.ZERO_BUY_SUBTYPE;
			}
		});
		// 1. 汇总数据处理(定期总资产,定期累计收益,定期预期收益)
		rep = scatterAmountSum(uid);
		if (objTnHoldList.size() > 0) {
			// 2. 定期列表数据处理
			tnHoldList = scatterObj2MapList(objTnHoldList);
			//组装数据 增加字段 rateCouponIncome 加息券收益
			tnHoldList.parallelStream().filter(m->Objects.equals(CardTypes.CARDVOLUME,m.get("couponType"))).forEach(m->{
				BigDecimal interestDays=new BigDecimal(Objects.toString(m.get("interestDays"),"0"));
				BigDecimal orderAmount=new BigDecimal(Objects.toString(m.get("orderAmount"),"0"));
				BigDecimal incomeCalcBasis=new BigDecimal(Objects.toString(m.get("incomeCalcBasis"),"0"));
				String addInterestStr=Objects.toString(m.get("addInterest"),"0").replaceAll("%", "");
				BigDecimal addInterest=new BigDecimal(addInterestStr);
				BigDecimal  ratio =addInterest.divide(BigDecimal.valueOf(100)).setScale(4,BigDecimal.ROUND_HALF_UP);
				BigDecimal interestIncome=orderAmount.multiply(ratio).multiply(interestDays)
						.divide(incomeCalcBasis, 2, BigDecimal.ROUND_DOWN);
//					String income=interestIncome.toString();
				m.remove("interestDays");
				m.remove("couponType");
				m.remove("incomeCalcBasis");
				m.put("rateCouponIncome", interestIncome);
			});
//			}

		}

		// 分页数据处理
		rep.setRows(tnHoldList);
		rep.setTotal(total);
		rep.setRow(req.getRow());
		rep.setPage(req.getPage());
		rep.reTotalPage();

		return rep;
	}
	private List<Map<String,Object>> scatterObj2MapList(List<Object[]> objTnHoldList) {
		List<Map<String,Object>> tnHoldList = new ArrayList<Map<String,Object>>();
		try {
			for (int i = 0; i < objTnHoldList.size(); i ++ ) {
				Map<String,Object> mapData = new HashMap<String,Object>();
				Object[] objArr = objTnHoldList.get(i);
				mapData.put("orderOid", objArr[0]); // 订单Oid
				mapData.put("productOid", objArr[1]); // 产品Oid
				mapData.put("productName", objArr[2]); // 产品名称
				mapData.put("relatedGuess", objArr[3]); // 1：关联竞猜宝 0：不关联竞猜宝
				mapData.put("productStatus", objArr[4]); // 产品状态
				mapData.put("orderStatus", objArr[5]); // 订单状态
				mapData.put("holdStatus", objArr[6]); // 订单持有状态
				mapData.put("durationPeriodDays", new BigDecimal(String.valueOf(objArr[7])).subtract(new BigDecimal(30))); // 投资期限
				mapData.put("payAmount", objArr[8]); // 实付本金
				mapData.put("couponAmount", objArr[9]); // 红包抵扣金额
				mapData.put("addInterest", objArr[10]); // 加息
				mapData.put("orderAmount", objArr[11]); // 订单金额
				mapData.put("expAror", objArr[12]); // 预期年化收益
				mapData.put("expectIncome", objArr[13]); // 预期收益
				mapData.put("realRatio", objArr[14]); // 实际年化收益(年化收益率)
				mapData.put("investTime", objArr[15]); // 投资时间(yyyy-MM-dd HH:mm:ss)
				mapData.put("payDate", objArr[16]); // 购买日(yyyy-MM-dd)。
				if(!mapData.get("productStatus").equals(Product.STATE_Raising)&&
						!mapData.get("productStatus").equals(Product.STATE_Raiseend)&&
						!mapData.get("productStatus").equals(Product.STATE_RaiseFail)){
					mapData.put("setupDate", objArr[17]); // 募集开始时间(起息日)
					mapData.put("durationPeriodEndDate", objArr[18]); // 到期日
				}else{
					mapData.put("setupDate", "标的融满之日"); // 起息日：标的融满之日
					mapData.put("durationPeriodEndDate", "起息日+借款期限"); // 到期日:起息日+借款期限
				}
				mapData.put("repayDate", objArr[19]); // 预计到账日
				mapData.put("realCashDate", objArr[20]); // 实际到账日
				mapData.put("raiseFailDate", objArr[21]); // 募集失败时间
				mapData.put("instruction", objArr[22]); // 产品协议和要素地址
				mapData.put("realIncome", objArr[23]); // 实际收益
				mapData.put("expectedArrorDisp", objArr[24]);
				mapData.put("activityDetailUrl", objArr[25]);
				mapData.put("interestDays", objArr[26]);
				mapData.put("couponType", objArr[27]);
				mapData.put("incomeCalcBasis", objArr[28]);
				mapData.put("subType", objArr[29]);
				mapData.put("isP2PAssetPackage", objArr[30]);
				mapData.put("paymentMethod","到期一次性还本付息");
				mapData.put("arrangement ","回款至您的快活宝账户");
				ElectronicSignatureRelation electronicSignatureRelation = this.electronicSignatureRelationEmDao.findByOrderCode(mapData.get("orderOid").toString());
				if(electronicSignatureRelation!=null&&!StringUtil.isEmpty(electronicSignatureRelation.getElectronicSignatureUrl())){
					mapData.put("serviceFiles",electronicSignatureRelation.getElectronicSignatureUrl()); //借款协议
				}else{
					mapData.put("serviceFiles","");
				}

				tnHoldList.add(mapData);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return tnHoldList;
	}
	/**
	 *
	 * @author yihonglei
	 * @Title: tnAmountSum
	 * @Description: 汇总数据处理(定期总资产,定期累计收益,定期预期收益)
	 * 使用原定期汇总逻辑，不能根据订单汇总，会有小数点保留位数不一致问题
	 * @param investorOid
	 * @return MyTnClientRep<Map<String,Object>>
	 * @date 2017年9月8日 下午2:33:38
	 * @since  1.0.0
	 */
	private MyTnClientRep<Map<String, Object>> scatterAmountSum(String investorOid ) {
		MyTnClientRep<Map<String, Object>> rep = new MyTnClientRep<>();
		List<HoldCacheEntity> holds = this.cacheHoldService.findByInvestorOid(investorOid);
		for (HoldCacheEntity hold : holds) {
			ProductCacheEntity cacheProduct = this.cacheProductService.getProductCacheEntityById(hold.getProductOid());
			if (Product.TYPE_Producttype_01.equals(cacheProduct.getType())) {
				Product product = this.productDao.findOne(hold.getProductOid());
				if(product.getIsP2PAssetPackage().equals(Product.IS_P2P_ASSET_PACKAGE_2)){
					rep.setTnCapitalAmount(rep.getTnCapitalAmount().add(hold.getTotalVolume())); // 定期总资产
					rep.setTotalIncomeAmount(rep.getTotalIncomeAmount().add(hold.getHoldTotalIncome())); // 定期累计收益
					if (PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_holding.equals(hold.getHoldStatus()) ||
							PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_toConfirm.equals(hold.getHoldStatus())) {
						rep.setTotalExpectIncomeAmount(rep.getTotalExpectIncomeAmount().add(hold.getExpectIncome()));// 定期预期收益(累计)
					}
				}
			}
		}
		return rep;
	}
}
