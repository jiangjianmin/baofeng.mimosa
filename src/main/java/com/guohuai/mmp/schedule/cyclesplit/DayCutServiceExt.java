package com.guohuai.mmp.schedule.cyclesplit;

import com.guohuai.ams.dict.Dict;
import com.guohuai.ams.dict.DictService;
import com.guohuai.ams.duration.assetPool.AssetPoolDao;
import com.guohuai.ams.label.LabelService;
import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductDao;
import com.guohuai.ams.productLabel.ProductLabelService;
import com.guohuai.basic.common.SeqGenerator;
import com.guohuai.cache.service.CacheSPVHoldService;
import com.guohuai.calendar.TradeCalendarService;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.StringUtil;
import com.guohuai.mmp.investor.tradeorder.*;
import com.guohuai.mmp.platform.baseaccount.statistics.PlatformStatisticsService;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountDao;
import com.guohuai.mmp.publisher.baseaccount.statistics.PublisherStatisticsService;
import com.guohuai.mmp.publisher.hold.PublisherHoldDao;
import com.guohuai.mmp.publisher.hold.PublisherHoldEntity;
import com.guohuai.mmp.publisher.hold.PublisherHoldService;
import com.guohuai.mmp.publisher.investor.InterestFormula;
import com.guohuai.mmp.sys.CodeConstants;
import io.reactivex.subjects.PublishSubject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 *日切具体处理逻辑
 * @author yujianlong
 * @date 2018/4/2 09:39
 * @param
 * @return
 */
@Service
@Transactional
public class DayCutServiceExt {
	Logger logger = LoggerFactory.getLogger(DayCutServiceExt.class);
	//期号redis 的key
	public static final String ISSUENOKEY = "c:g:m:issueno";
	//04产品编号前缀 智盈15d
	public static final String PRODUCT04PREFIX = "ZY15D";
	@Autowired
	private CacheSPVHoldService cacheSPVHoldService;
	@Autowired
	private PublisherHoldService publisherHoldService;
	@Autowired
	private TradeCalendarService tradeCalendarService;
	@Autowired
	private InvestorOpenCycleDao investorOpenCycleDao;
	@Autowired
	private ProductDao productDao;
	@Autowired
	private AssetPoolDao assetPoolDao;
	@Autowired
	private PublisherBaseAccountDao publisherBaseAccountDao;
	@Autowired
	private InvestorTradeOrderDao investorTradeOrderDao;
	@Autowired
	private PublisherHoldDao publisherHoldDao;
	@Autowired
	private DayCutTradeOrderEMDao dayCutTradeOrderEMDao;
	@Autowired
	private DayCutPublisherHoldEMDao publisherHoldEMDao;
	@Autowired
	private DayCutOpenCycleRalationEMDao dayCutOpenCycleRalationEMDao;
	@Autowired
	private ProductLabelService productLabelService;
	@Autowired
	private PublisherStatisticsService publisherStatisticsService;
	@Autowired
	private PlatformStatisticsService platformStatisticsService;
	@Autowired
	private OrderDateService orderDateService;
	@Autowired
	private DictService dictService;
	@Autowired
	private LabelService labelService;
	@Autowired
	private SeqGenerator seqGenerator;
	@Autowired
	private RedisTemplate<String, String> redis;
	@Autowired
	private PublishSubject<DayCutPublishEvent> publishSubject;

	/**
	 * 200人日切
	 *
	 * @param [dayCutTransferEvent]
	 * @return void
	 * @author yujianlong
	 * @date 2018/4/1 19:28
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void doRealCut(DayCutTransferEvent dayCutTransferEvent) {
		try {
			doRealCutInner(dayCutTransferEvent);
		} catch (Exception e) {
			logger.error("{},{}",e.getMessage(),e.getCause());
			e.printStackTrace();
			logger.error("执行错误,page=" + dayCutTransferEvent.getPage());
			if (e instanceof AMPException) {
				throw e;
			} else {
				throw new AMPException(900002, "子事务执行错误,page=" + dayCutTransferEvent.getPage());
			}
		}
	}

	/**
	 * 200人日切具体
	 *
	 * @param [dayCutTransferEvent]
	 * @return void
	 * @author yujianlong
	 * @date 2018/4/1 19:29
	 */
	@Transactional
	public void doRealCutInner(DayCutTransferEvent dayCutTransferEvent) {
		int page = dayCutTransferEvent.getPage();
		BigDecimal availableAmount = dayCutTransferEvent.getAvailableAmount();
		//获取传进来的03产品实体
		Product product03 = dayCutTransferEvent.getProduct();
		ConcurrentLinkedDeque<BigDecimal> sumAmount = dayCutTransferEvent.getSumAmount();
		List<String> holdOidsIn = dayCutTransferEvent.getHoldOids();

		logger.info("正在执行第{}批", page);
		//获取持仓金额总数
		BigDecimal allAmount = sumAmount.stream().reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
		//根据holdoids获取所有持仓
		List<PublisherHoldEntity> holdList =
				publisherHoldDao.getHoldsIn(holdOidsIn,product03.getOid());
		if (null==holdList||holdList.isEmpty()){
			logger.error("holdoids={},本次获取持仓列表为空，可能已被处理，无需日切",holdOidsIn);
			return ;
		}
		//存放ordercode和新的产品名称
		Map<String, String> code_ProductNewName = new HashMap<>();
		List<String> holdOids = holdOidsIn;

		//获取当前持仓总额
		BigDecimal cuAmount = holdList.stream().map(PublisherHoldEntity::getRedeemableHoldVolume).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
		//持仓金额总数和资产池剩余份额对比
		if (allAmount.compareTo(availableAmount) > 0) {
			logger.error("资产池不足");
			throw new AMPException(900001, "资产池不足");
		}
		//对付懒加载
		holdList.stream().forEach(hold -> {
			hold.getPublisherBaseAccount();
			hold.getAssetPool();
			hold.getInvestorBaseAccount();
			hold.getConfirmDate();

		});
		LocalDate prevTrade = tradeCalendarService.getPrevTradeDate(new Date(System.currentTimeMillis())).toLocalDate();
		holdList.stream().filter(h->null==h.getConfirmDate()).forEach(h->h.setConfirmDate(Date.valueOf(prevTrade)));
		//按照持仓确认日期分组
		Map<LocalDate, List<PublisherHoldEntity>> holdsGroup =new HashMap<>();
		holdsGroup=holdList.stream().collect(Collectors.groupingBy(h -> h.getConfirmDate().toLocalDate()));
		//存放investoid和ExpectIncome
		Map<String, ExpectIncome> newtrade_investor_ExpectIncomeAll=new HashMap<>();
		//分组的数据分别建立产品和订单，以及转投单
		holdsGroup.entrySet().stream().forEach(holdEntry->{
			LocalDate ld=holdEntry.getKey();
			//根据确认时间获取订单时间
			Timestamp orderTime=Timestamp.valueOf(ld.atStartOfDay().withHour(12).withMinute(0).withSecond(0).minusDays(1));
			List<PublisherHoldEntity> holds = holdEntry.getValue();

			holds.stream().forEach(hold -> {
				hold.getPublisherBaseAccount();
				hold.getAssetPool();
				hold.getInvestorBaseAccount();
				hold.getConfirmDate();

			});
			BigDecimal holdTotalAmount=holds.stream().map(PublisherHoldEntity::getRedeemableHoldVolume).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
			//创建04产品
			Product productNew = createProduct(product03,ld,holdTotalAmount,holds.size());
			//生成新的持仓
			List<PublisherHoldEntity> newHolds = holds.stream().map(hold -> {
				return createPublisherHoldEntity(hold, productNew);
			}).collect(Collectors.toList());


			newHolds.stream().forEach(hold -> {
				hold.getPublisherBaseAccount();
				hold.getAssetPool();
				hold.getInvestorBaseAccount();
				hold.getConfirmDate();

			});
			//生成新的订单
			List<InvestorTradeOrderEntity> newtrades = newHolds.parallelStream().map(hold -> {
				return createInvestorTradeOrderEntity(hold, productNew,orderTime,InvestorTradeOrderEntity.TRADEORDER_orderType_changeInvest);
			}).collect(Collectors.toList());

//			newtrades.stream().forEach(t-> System.out.println(t.getInvestorBaseAccount()));
			//获取新的订单用户和订单号的map
			Map<String, String> newtrade_investor_code = newtrades.stream().collect(Collectors.toMap(e -> e.getInvestorBaseAccount().getOid(), e -> e.getOrderCode()));
			//添加预期收益
			Map<String, ExpectIncome> newtrade_investor_ExpectIncome =
					newtrades.stream().collect(Collectors.toMap(e -> e.getInvestorBaseAccount().getOid(),
							e -> {
								ExpectIncome expectIncome=new ExpectIncome(e.getExpectIncome(),e.getExpectIncomeExt());
						return expectIncome;
					}
					));
			newtrade_investor_ExpectIncomeAll.putAll(newtrade_investor_ExpectIncome);

			//外部增加转投单关系
			dayCutTransferEvent.getTmpMap().putAll(newtrade_investor_code);
			Map<String, String> cu_code_pName = newtrade_investor_code.values().stream().distinct().collect(Collectors.toMap(Function.identity(), t -> productNew.getName()));
			//添加新的订单code和产品名对应关系
			code_ProductNewName.putAll(cu_code_pName);


			//生成新的投资单到中间表
			List<InvestorOpenCycleRelationEntity> t_relations = newtrades.stream().map(newTrade -> {
				return createOpenCycleRelationEntity(newTrade, InvestorOpenCycleRelationEntity.ORDERTYPE_CHANGE);
			}).collect(Collectors.toList());

			//批量插入新的持仓
			publisherHoldEMDao.batchInsert(newHolds);
			//批量插入新的转投单

			dayCutTradeOrderEMDao.batchInsert(newtrades);
			//批量插入转投单关系
			dayCutOpenCycleRalationEMDao.batchInsert(t_relations);

			//释放内存
			newHolds.clear();
			newHolds=null;
			newtrades.clear();
			newtrades=null;
			t_relations.clear();
			t_relations=null;

		});
		//更新的老的预约持仓表
		List<PublisherHoldEntity> upOldHolds = holdList.stream().map(h -> {
			String investorOid=h.getInvestorBaseAccount().getOid();
			ExpectIncome expectIncome = newtrade_investor_ExpectIncomeAll.get(investorOid);

			BigDecimal redeemableHoldVolume=h.getRedeemableHoldVolume();
			h.setTotalVolume(h.getTotalVolume().subtract(redeemableHoldVolume));
			//原有持仓扣减新订单的预期收益
//			h.setExpectIncome(h.getExpectIncome().subtract(expectIncome.getExpectIncome()));
			h.setExpectIncomeExt(BigDecimal.ZERO);

			h.setHoldVolume(h.getHoldVolume().subtract(redeemableHoldVolume));
			h.setAccruableHoldVolume(h.getAccruableHoldVolume().subtract(redeemableHoldVolume));
			h.setValue(h.getValue().subtract(redeemableHoldVolume));
			h.setRedeemableHoldVolume(BigDecimal.ZERO);
			return h;
		}).collect(Collectors.toList());

		//更新预约单订单
		List<InvestorTradeOrderEntity> tradeIn = investorTradeOrderDao.kdbTrades4Update(holdOids);
		List<InvestorTradeOrderEntity> upOldTrades = tradeIn.stream().map(t -> {
			t.setHoldVolume(BigDecimal.ZERO);
			t.setValue(BigDecimal.ZERO);
			t.setHoldStatus(InvestorTradeOrderEntity.TRADEORDER_holdStatus_closed);
			return t;
		}).collect(Collectors.toList());

		List<String> ordercodes = tradeIn.stream().map(InvestorTradeOrderEntity::getOrderCode).collect(Collectors.toList());
		List<InvestorOpenCycleRelationEntity> investorOpenCycleRelationEntityIn = investorOpenCycleDao.getInvestorOpenCycleRelationEntityIn(ordercodes);
		//生成老订单和新订单的中间表关系 [只做批量更新]
		List<InvestorOpenCycleRelationEntity> s_t_relations = investorOpenCycleRelationEntityIn.stream().map(e -> {
			String newOrderCode=dayCutTransferEvent.getTmpMap().get(e.getInvestorOid());
			String newProductName=code_ProductNewName.get(newOrderCode);
			e.setOrderType(InvestorOpenCycleRelationEntity.ORDERTYPE_BOOKING);
			e.setInvestAmount(e.getSourceOrderAmount());
			e.setInvestOrderCode(newOrderCode);
			//增加转投的产品名称
			e.setInvestProductName(newProductName);
			e.setContinueStatus(InvestorOpenCycleRelationEntity.CONTINUESTATUSTYPE_YES);
			e.setAssignment(InvestorOpenCycleRelationEntity.ASSIGNMENTTYPE_CHANGEINVEST);
			return e;
		}).collect(Collectors.toList());
		publisherHoldEMDao.batchUpdate(upOldHolds);
		dayCutTradeOrderEMDao.batchUpdate(upOldTrades);
		//更新中间表关系
		dayCutOpenCycleRalationEMDao.batchUpdate(s_t_relations);

		//计数队列增加当前持仓总额
		sumAmount.add(cuAmount);
		//释放内存
		holdList.clear();
		holdList=null;
		holdsGroup.clear();
		holdsGroup=null;
	}

	/**
	 *用来存放预期收益的类
	 *
	 * @author yujianlong
	 * @date 2018/4/10 19:45
	 * @param
	 * @return
	 */
	private static class ExpectIncome{
		private BigDecimal expectIncome;
		private BigDecimal expectIncomeExt;

		public ExpectIncome() {
		}

		public ExpectIncome(BigDecimal expectIncome, BigDecimal expectIncomeExt) {
			this.expectIncome = expectIncome;
			this.expectIncomeExt = expectIncomeExt;
		}

		public BigDecimal getExpectIncome() {
			return expectIncome;
		}

		public void setExpectIncome(BigDecimal expectIncome) {
			this.expectIncome = expectIncome;
		}

		public BigDecimal getExpectIncomeExt() {
			return expectIncomeExt;
		}

		public void setExpectIncomeExt(BigDecimal expectIncomeExt) {
			this.expectIncomeExt = expectIncomeExt;
		}

		@Override
		public String toString() {
			return "ExpectIncome{" +
					"expectIncome=" + expectIncome +
					", expectIncomeExt=" + expectIncomeExt +
					'}';
		}
	}




	/**
	 *
	 *生成转投持仓
	 * @author yujianlong
	 * @date 2018/4/2 09:30
	 * @param [hold, productNew]
	 * @return com.guohuai.mmp.publisher.hold.PublisherHoldEntity
	 */
	@Transactional
	public PublisherHoldEntity createPublisherHoldEntity(PublisherHoldEntity hold, Product productNew) {

//		BigDecimal expectIncome = BigDecimal.ZERO;
//		expectIncome = InterestFormula.simple(hold.getRedeemableHoldVolume(),
//				productNew.getExpAror(), productNew.getIncomeCalcBasis(), productNew.getDurationPeriodDays());
//		BigDecimal expectIncomeExt = BigDecimal.ZERO;
//		expectIncomeExt=InterestFormula.simple(hold.getRedeemableHoldVolume(),
//				productNew.getExpArorSec(), productNew.getIncomeCalcBasis(),productNew.getDurationPeriodDays());

		PublisherHoldEntity newHold = new PublisherHoldEntity();
		newHold.setAssignedId(StringUtil.uuid());
		newHold.setProduct(productNew);
		newHold.setAssetPool(productNew.getAssetPool());
		newHold.setPublisherBaseAccount(hold.getPublisherBaseAccount());
		newHold.setInvestorBaseAccount(hold.getInvestorBaseAccount());
		newHold.setTotalVolume(hold.getRedeemableHoldVolume());
		newHold.setHoldVolume(hold.getRedeemableHoldVolume());
		newHold.setToConfirmInvestVolume(BigDecimal.ZERO);
		newHold.setToConfirmRedeemVolume(BigDecimal.ZERO);
		newHold.setRedeemableHoldVolume(hold.getRedeemableHoldVolume());
		newHold.setLockRedeemHoldVolume(BigDecimal.ZERO);
		newHold.setExpGoldVolume(BigDecimal.ZERO);
		newHold.setTotalInvestVolume(hold.getRedeemableHoldVolume());
		newHold.setAccruableHoldVolume(hold.getRedeemableHoldVolume());
		newHold.setValue(hold.getRedeemableHoldVolume());
		newHold.setHoldTotalIncome(BigDecimal.ZERO);
		newHold.setTotalBaseIncome(BigDecimal.ZERO);
		newHold.setTotalRewardIncome(BigDecimal.ZERO);
		newHold.setHoldYesterdayIncome(BigDecimal.ZERO);
		newHold.setYesterdayBaseIncome(BigDecimal.ZERO);
		newHold.setYesterdayRewardIncome(BigDecimal.ZERO);
		newHold.setIncomeAmount(BigDecimal.ZERO);
		newHold.setRedeemableIncome(BigDecimal.ZERO);
		newHold.setLockIncome(BigDecimal.ZERO);
		newHold.setConfirmDate(hold.getConfirmDate());

		newHold.setExpectIncome(hold.getExpectIncomeExt());
		newHold.setExpectIncomeExt(hold.getExpectIncomeExt());

		newHold.setAccountType(PublisherHoldEntity.PUBLISHER_accountType_INVESTOR);
		newHold.setMaxHoldVolume(hold.getRedeemableHoldVolume());
		newHold.setDayRedeemVolume(BigDecimal.ZERO);
		newHold.setDayInvestVolume(hold.getRedeemableHoldVolume());
		newHold.setDayRedeemCount(0);
		newHold.setProductAlias(hold.getProductAlias());
		newHold.setHoldStatus(PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_holding);
		newHold.setLatestOrderTime(hold.getLatestOrderTime());
		newHold.setUpdateTime(Timestamp.valueOf(LocalDateTime.now()));
		newHold.setCreateTime(Timestamp.valueOf(LocalDateTime.now()));
		return newHold;
	}


	/**
	 *
	 *生成转投订单
	 * @author yujianlong
	 * @date 2018/4/2 09:30
	 * @param [hold, productNew]
	 * @return com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity
	 */
	@Transactional
	public InvestorTradeOrderEntity createInvestorTradeOrderEntity(PublisherHoldEntity hold, Product productNew,Timestamp orderTime, String orderType) {
		InvestorTradeOrderEntity newtrade = new InvestorTradeOrderEntity();

		Timestamp timestampNow = Timestamp.valueOf(LocalDateTime.now());

		newtrade.setAssignedId(StringUtil.uuid());
		newtrade.setInvestorBaseAccount(hold.getInvestorBaseAccount());
		newtrade.setPublisherBaseAccount(hold.getPublisherBaseAccount());
		newtrade.setProduct(productNew);
		newtrade.setChannel(null);
		newtrade.setInvestorOffset(null);
		newtrade.setPublisherOffset(null);
		newtrade.setPlatformFinanceCheck(null);
		newtrade.setPublisherHold(hold);
		newtrade.setOrderCode(this.seqGenerator.next(CodeConstants.PAYMENT_invest));
		newtrade.setOrderType(orderType);
		newtrade.setOrderAmount(hold.getRedeemableHoldVolume());
		newtrade.setOrderVolume(hold.getRedeemableHoldVolume().divide(productNew.getNetUnitShare()));
		newtrade.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_confirmed);
		newtrade.setCheckStatus(InvestorTradeOrderEntity.TRADEORDER_checkStatus_yes);
		newtrade.setContractStatus(InvestorTradeOrderEntity.TRADEORDER_contractStatus_toHtml);
		newtrade.setCreateMan(InvestorTradeOrderEntity.TRADEORDER_createMan_platform);
		newtrade.setOrderTime(orderTime); // 订单时间
		newtrade.setCompleteTime(timestampNow);
		newtrade.setPublisherClearStatus(InvestorTradeOrderEntity.TRADEORDER_publisherClearStatus_cleared);
		newtrade.setPublisherConfirmStatus(InvestorTradeOrderEntity.TRADEORDER_publisherConfirmStatus_confirmed);
		newtrade.setPublisherCloseStatus(InvestorTradeOrderEntity.TRADEORDER_publisherCloseStatus_closed);
		newtrade.setInvestorClearStatus(InvestorTradeOrderEntity.TRADEORDER_investorClearStatus_cleared);
		newtrade.setInvestorCloseStatus(InvestorTradeOrderEntity.TRADEORDER_investorCloseStatus_closed);
		newtrade.setCoupons(null);
		newtrade.setCouponType(null);
		newtrade.setCouponAmount(null);
		newtrade.setPayAmount(hold.getRedeemableHoldVolume());
		newtrade.setHoldVolume(newtrade.getOrderVolume()); // 持有份额
		newtrade.setRedeemStatus(InvestorTradeOrderEntity.TRADEORDER_redeemStatus_yes);
		newtrade.setAccrualStatus(InvestorTradeOrderEntity.TRADEORDER_accrualStatus_no);
		newtrade.setCorpusAccrualEndDate(this.orderDateService.getCorpusAccrualEndDate(newtrade));
		newtrade.setBeginAccuralDate(productNew.getSetupDate());
		newtrade.setBeginRedeemDate(productNew.getSetupDate());
		newtrade.setTotalIncome(BigDecimal.ZERO);
		newtrade.setTotalBaseIncome(BigDecimal.ZERO);
		newtrade.setTotalRewardIncome(BigDecimal.ZERO);
		newtrade.setYesterdayBaseIncome(BigDecimal.ZERO);
		newtrade.setYesterdayRewardIncome(BigDecimal.ZERO);
		newtrade.setYesterdayIncome(BigDecimal.ZERO);
		newtrade.setToConfirmIncome(BigDecimal.ZERO);
		newtrade.setIncomeAmount(hold.getIncomeAmount());
		newtrade.setExpectIncomeExt(hold.getExpectIncomeExt());
		newtrade.setExpectIncome(hold.getExpectIncome());
		newtrade.setValue(newtrade.getOrderVolume());
		newtrade.setHoldStatus(InvestorTradeOrderEntity.TRADEORDER_holdStatus_holding);
		newtrade.setConfirmDate(hold.getConfirmDate());
		newtrade.setPayStatus(InvestorTradeOrderEntity.TRADEORDER_payStatus_paySuccess);
		newtrade.setAcceptStatus(null);
		newtrade.setRefundStatus(null);
		newtrade.setProvince(null);
		newtrade.setCity(null);
		newtrade.setCreateTime(timestampNow); // 创建时间
		newtrade.setUpdateTime(timestampNow);
		newtrade.setRelateOid(null);
		newtrade.setPayChannel(null);
		newtrade.setIsAuto("0");
		newtrade.setProtocalOid(null);
		newtrade.setPayDate(null);
		return newtrade;
	}

	/**
	 *
	 *新建转投关系记录
	 * @author yujianlong
	 * @date 2018/4/2 09:30
	 * @param [trade]
	 * @return com.guohuai.mmp.investor.tradeorder.InvestorOpenCycleRelationEntity
	 */
	@Transactional
	public InvestorOpenCycleRelationEntity createOpenCycleRelationEntity(InvestorTradeOrderEntity trade, String orderType) {
		InvestorOpenCycleRelationEntity cycleRelationEntity = new InvestorOpenCycleRelationEntity();

		cycleRelationEntity.setSourceOrderCode(trade.getOrderCode());
		cycleRelationEntity.setSourceOrderAmount(trade.getOrderAmount());
		cycleRelationEntity.setAssignment(InvestorOpenCycleRelationEntity.ASSIGNMENTTYPE_DEFAULT);
		cycleRelationEntity.setOrderType(orderType);
		cycleRelationEntity.setPayType(null);
		cycleRelationEntity.setOrderStatus(null);
		cycleRelationEntity.setInvestorOid(trade.getInvestorBaseAccount().getUserOid());
		cycleRelationEntity.setPhone(trade.getInvestorBaseAccount().getPhoneNum());
		cycleRelationEntity.setInvestOrderCode(null);
//		cycleRelationEntity.setInvestProductName(trade.getProduct().getName());
		cycleRelationEntity.setInvestAmount(BigDecimal.ZERO);
		cycleRelationEntity.setRedeemOrderCode(null);
		cycleRelationEntity.setRedeemAmount(null);
		cycleRelationEntity.setContinueStatus(InvestorOpenCycleRelationEntity.CONTINUESTATUSTYPE_YES);
		cycleRelationEntity.setCycleConfirmDate(trade.getConfirmDate());
		cycleRelationEntity.setCreateTime(Timestamp.valueOf(LocalDateTime.now()));
		return cycleRelationEntity;
	}

	/**
	 * 生成产品编号名称【日期+期号】
	 *
	 * @param []
	 * @return java.lang.String
	 * @author yujianlong
	 * @date 2018/4/2 00:04
	 */
	public String generProductCode(String dateStr) {
		StringBuffer sb = new StringBuffer();
		sb.append(dateStr);
		sb.append(getIssueNo(dateStr));
		return sb.toString();
	}

	/**
	 * 获取期号
	 *
	 * @param []
	 * @return java.lang.String
	 * @author yujianlong
	 * @date 2018/4/2 00:07
	 */
	public long getIssueNo(String dateStr) {
		BoundHashOperations<String, Object, Object> issueOper = redis.boundHashOps(ISSUENOKEY);
		long num = issueOper.increment(dateStr, 1);
		return num;
	}

	/**
	 *
	 *创建产品和标签等
	 * @author yujianlong
	 * @date 2018/4/4 17:17
	 * @param [baseProduct, ld, holdTotalAmount, purchasTimes]
	 * @return com.guohuai.ams.product.Product
	 */
	@Transactional
	public Product createProduct(Product baseProduct,LocalDate ld,BigDecimal holdTotalAmount,Integer purchasTimes) {
		String dateStr = ld.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		logger.info("===============循环子产品建立开始===============");
		Product p = newProduct();
		String productOid = StringUtil.uuid();
		// 产品类型
		Dict assetType = this.dictService.get(Product.TYPE_Producttype_04);
		Date setupDate = Date.valueOf(ld);
		Date durationPeriodEndDate = DateUtil.addSQLDays(setupDate, baseProduct.getDurationPeriodDays() - 1);
		Date repayDate = DateUtil.addSQLDays(durationPeriodEndDate, baseProduct.getAccrualRepayDays());
		//产品编码后缀日期+期号
		String codeSuffix=generProductCode(dateStr);
		p.setOid(productOid);
		p.setAssetPool(baseProduct.getAssetPool());//资产池
		p.setPublisherBaseAccount(baseProduct.getAssetPool().getSpvEntity());//发行人账户
		p.setMemberId(baseProduct.getMemberId());
		p.setCode(PRODUCT04PREFIX+codeSuffix); //产品编号  产品编号取“zy15D+日期+期号”
		p.setName(baseProduct.getName() + codeSuffix + "期"); //产品简称
		p.setFullName(baseProduct.getFullName()+codeSuffix + "期"); //产品全称
		p.setAdministrator(baseProduct.getAdministrator()); //产品管理人
		p.setType(assetType);
		p.setReveal(baseProduct.getReveal());//额外增信
		p.setRevealComment(baseProduct.getRevealComment());//增信备注
		p.setCurrency(baseProduct.getCurrency());//币种
		p.setIncomeCalcBasis(baseProduct.getIncomeCalcBasis());//收益计算基础
		p.setManageRate(BigDecimal.ZERO);
		p.setFixedManageRate(BigDecimal.ZERO);
		p.setBasicRatio(baseProduct.getExpAror());
		p.setOperationRate(baseProduct.getOperationRate());
		p.setAccrualCycleOid(null);
		p.setPayModeOid(null);
		p.setPayModeDay(0);
		p.setRaiseStartDateType("FIRSTRACKTIME");
		p.setRaiseStartDate(Date.valueOf(ld));
		p.setRaisePeriodDays(1);
		p.setLockPeriodDays(0);
		p.setFoundDays(0);
		p.setInterestsFirstDays(baseProduct.getInterestsFirstDays()); //起息期(成立后)
		p.setDurationPeriodDays(baseProduct.getDurationPeriodDays()); // 存续期(成立后)
		p.setExpAror(baseProduct.getExpAror());
		p.setExpArorSec(baseProduct.getExpArorSec());
		p.setRewardInterest(baseProduct.getRewardInterest());
		p.setRaisedTotalNumber(holdTotalAmount);
		p.setNetUnitShare(BigDecimal.ONE);
		p.setInvestMin(baseProduct.getInvestMin());
		p.setInvestAdditional(BigDecimal.valueOf(0.01));
		p.setInvestMax(null);
		p.setInvestDateType("T");
		p.setMinRredeem(BigDecimal.ZERO);
		p.setMaxRredeem(BigDecimal.ZERO);
		p.setAdditionalRredeem(BigDecimal.ONE);
		p.setRredeemDateType("T");
		p.setNetMaxRredeemDay(BigDecimal.ZERO);
		p.setMaxHold(holdTotalAmount);
		p.setDailyNetMaxRredeem(holdTotalAmount);
		p.setAccrualRepayDays(baseProduct.getAccrualRepayDays()); // 还本付息日
		p.setPurchaseConfirmDays(1);
		p.setPurchaseConfirmDaysType("T");
		p.setRedeemConfirmDays(1);
		p.setRedeemConfirmDaysType("T");
		p.setSetupDateType("T");
		p.setSetupDate(Date.valueOf(ld));//  最晚产品成立时间
		p.setRedeemTimingTaskDays(0);
		p.setInvestComment(baseProduct.getInvestComment());
		p.setInstruction(baseProduct.getInstruction());
		p.setRiskLevel(baseProduct.getRiskLevel());
		p.setInvestorLevel(baseProduct.getInvestorLevel());
		p.setFileKeys(baseProduct.getFileKeys());
		p.setInvestFileKey(baseProduct.getInvestFileKey());
		p.setServiceFileKey(baseProduct.getServiceFileKey());
		p.setState(Product.STATE_Durationing);
		p.setCreateTime(DateUtil.getSqlCurrentDate());
		p.setUpdateTime(DateUtil.getSqlCurrentDate());
		p.setOperator(baseProduct.getOperator());
		p.setRaiseEndDate(Date.valueOf(ld));
		p.setRaiseFailDate(null);
		p.setDurationPeriodEndDate(durationPeriodEndDate);// 存续期结束时间
		p.setEndDate(repayDate);
		p.setDurationRepaymentDate(null);
		p.setStems(Product.STEMS_Plateform);
		p.setIsDeleted(Product.NO);
		p.setAuditState(Product.AUDIT_STATE_Reviewed);
		p.setCurrentVolume(holdTotalAmount);
		p.setCollectedVolume(holdTotalAmount);
		p.setPurchaseNum(purchasTimes);
		p.setLockCollectedVolume(BigDecimal.ZERO);
		p.setRepayDate(repayDate);// 到期还款时间
		p.setRepayInterestStatus(Product.PRODUCT_repayInterestStatus_toRepay);//付息状态
		p.setRepayLoanStatus(Product.PRODUCT_repayLoanStatus_toRepay);//还本状态
		p.setNewestProfitConfirmDate(null);
		p.setMaxSaleVolume(holdTotalAmount);
		p.setIsOpenPurchase(Product.YES);
		p.setIsOpenRemeed(Product.NO);
		p.setPurchaseApplyStatus(Product.APPLY_STATUS_None);
		p.setRedeemApplyStatus(Product.APPLY_STATUS_None);
		p.setClearingTime(null);
		p.setClearingOperator(null);
		p.setClearedTime(null);
		p.setPurchasePeopleNum(purchasTimes);
		p.setSingleDailyMaxRedeem(null);
		p.setIsOpenRedeemConfirm(Product.YES);
		p.setDealStartTime(null);
		p.setDealEndTime(null);
		p.setFastRedeemStatus(Product.NO);
		p.setFastRedeemMax(BigDecimal.ZERO);
		p.setFastRedeemLeft(BigDecimal.ZERO);
		p.setRecPeriodExpAnYield(BigDecimal.ZERO);
		p.setRaiseFullFoundType(Product.RAISE_FULL_FOUND_TYPE_MANUAL);
		p.setAutoFoundDays(0);
		p.setOverdueStatus(null);
		p.setIsAutoAssignIncome("NO");
		p.setSingleDayRedeemCount(999);
		p.setProductLabel(baseProduct.getProductLabel());
		p.setProductPackage(null);
		p.setGuess(null);
		p.setUseRedPackages(baseProduct.getUseRedPackages());
		p.setInterestAuditStatus(Product.INTEREST_AUDIT_STATUS_toCommit);
		p.setUseraiseRateCoupons(baseProduct.getUseraiseRateCoupons());
		p.setProductElement(baseProduct.getProductElement());
		p.setProductIntro(baseProduct.getProductIntro());
		p.setActivityDetail(baseProduct.getActivityDetail());
		p.setIsActivityProduct(baseProduct.getIsActivityProduct());
		p.setExpectedArrorDisp(baseProduct.getExpectedArrorDisp());
		p.setMovedupRedeemLockDays(baseProduct.getMovedupRedeemLockDays());
		p.setMovedupRedeemMinPay(baseProduct.getMovedupRedeemMinPay());
		p.setMovedupRedeemRate(baseProduct.getMovedupRedeemRate());
		List<String> labelOids = labelService.findLabelCodeByProdcuctOid(baseProduct.getOid());
		p = this.productDao.saveAndFlush(p);
		this.productLabelService.saveAndFlush(p, labelOids);
		//增加spv持仓
		PublisherHoldEntity hold = this.publisherHoldService.getAssetPoolSpvHold4reviewProduct(baseProduct.getAssetPool(), baseProduct.getPublisherBaseAccount(), Product.TYPE_Producttype_04);
		if (hold != null) {
			hold.setTotalVolume(p.getCollectedVolume());
			if (hold.getProduct() == null) {
				hold.setProduct(p);
				this.publisherHoldDao.saveAndFlush(hold);
				cacheSPVHoldService.createSPVHoldCache(hold);
			}
		}

		// 定期产品进入募集期时，增加产品发行数量
		publisherStatisticsService.increaseReleasedProductAmount(p.getPublisherBaseAccount());
		platformStatisticsService.increaseReleasedProductAmount();
		// 定期产品进入募集期时，增加在售产品数量
		publisherStatisticsService.increaseOnSaleProductAmount(p.getPublisherBaseAccount());
		platformStatisticsService.increaseOnSaleProductAmount();
		logger.info("===============循环子产品{}建立结束,共{}人申购,创建时间{}==============",p.getName(),p.getPurchaseNum(),p.getCreateTime());
		return p;
	}
	/**
	 *
	 *初始化新产品
	 * @author yujianlong
	 * @date 2018/4/3 14:54
	 * @param []
	 * @return com.guohuai.ams.product.Product
	 */
	private Product newProduct() {
		Product p = new Product();
		p.setManageRate(new BigDecimal(0));
		p.setFixedManageRate(new BigDecimal(0));
		p.setBasicRatio(new BigDecimal(0));
		p.setOperationRate(new BigDecimal(0));
		p.setPayModeDay(0);
		p.setRaisePeriodDays(0);
		p.setLockPeriodDays(0);
		p.setInterestsFirstDays(0);
		p.setDurationPeriodDays(0);
		p.setExpAror(new BigDecimal(0));
		p.setExpArorSec(new BigDecimal(0));
		p.setRaisedTotalNumber(new BigDecimal(0));
		p.setNetUnitShare(new BigDecimal(0));
		p.setInvestMin(new BigDecimal(0));
		p.setInvestAdditional(new BigDecimal(0));
		p.setInvestMax(new BigDecimal(0));
		p.setMinRredeem(new BigDecimal(0));
		p.setNetMaxRredeemDay(new BigDecimal(0));
		p.setDailyNetMaxRredeem(new BigDecimal(0));
		p.setAccrualRepayDays(0);
		p.setPurchaseConfirmDays(0);
		p.setRedeemConfirmDays(0);
		p.setRedeemTimingTaskDays(0);
		p.setPurchaseNum(0);
		p.setCurrentVolume(new BigDecimal(0));
		p.setCollectedVolume(new BigDecimal(0));
		p.setLockCollectedVolume(new BigDecimal(0));
		p.setMaxSaleVolume(new BigDecimal(0));
		p.setIsAutoAssignIncome(Product.NO);
		return p;

	}
}
