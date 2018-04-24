package com.guohuai.ams.companyScatterStandard;

import com.alibaba.fastjson.JSONObject;
import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductDao;
import com.guohuai.ams.product.ProductDurationService;
import com.guohuai.ams.productPackage.ProductPackageDao;
import com.guohuai.basic.message.DealMessageEntity;
import com.guohuai.calendar.TradeCalendarService;
import com.guohuai.component.message.MessageSendUtil;
import com.guohuai.component.util.DateUtil;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderDao;
import com.guohuai.mmp.publisher.baseaccount.statistics.PublisherStatisticsService;
import com.guohuai.mmp.schedule.cyclesplit.DayCutTradeOrderEMDao;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import io.reactivex.subjects.PublishSubject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * 中间表统计service
 *
 * @author yujianlong
 */
@Service
@Transactional
@Slf4j
public class CompanyLoanService {
	@Autowired
	private ProductDurationService productDurationService;
	//	@Autowired
//	private ProfitRuleService profitRuleService;
//	@Autowired
//	private SerialTaskService serialTaskService;
	@Autowired
	private TradeCalendarService tradeCalendarService;
	@Autowired
	ProductDao productDao;
	@Autowired
	ProductPackageDao productPackageDao;
	@Autowired
	InvestorTradeOrderDao investorTradeOrderDao;
	@Autowired
	ElectronicSignatureRelationEmDao electronicSignatureRelationEmDao;
	@Autowired
	DayCutTradeOrderEMDao dayCutTradeOrderEMDao;
	@Autowired
	private RedisTemplate<String, String> redis;
	@Autowired
	private LoanContractDao loanContractDao;
	@Autowired
	private PublisherStatisticsService publisherStatisticsService;

	@Autowired
	private MessageSendUtil messageSendUtil;
	@Autowired
	CompanyLoanServiceExt companyLoanServiceExt;

	@Autowired
	@Qualifier(value="electronicSignatureSubject")
	private PublishSubject<String> publishSubject;
	//存储散标产品是否处理过满标
	public static final String HANDLESCATTER = "c:g:a:c:handleScatter";
	/*时间单位*/
	public static final TimeUnit SLEEPTIMEUNIT = TimeUnit.MINUTES;
	public static final ConcurrentLinkedDeque<String> electronicSignatureSignalQueue = new ConcurrentLinkedDeque<>();
	@PostConstruct
	public void init() {
		publishSubject.subscribe(productOid->{
			electronicSignatureSignalQueue.add(productOid);
			long times = electronicSignatureSignalQueue.stream().filter(s -> Objects.equals(s, productOid)).count();
			if (times>0){
				SLEEPTIMEUNIT.sleep(times*5);
				this.raiseFullStepTwoTryAgain(productOid);
			}
		});
	}

	/**
	 *手动触发募集满额
	 *
	 * @author yujianlong
	 * @date 2018/4/23 16:39
	 * @param []
	 * @return void
	 */
	@Transactional(value= Transactional.TxType.REQUIRES_NEW)
	public void handTrigRaiseFullAmountCallBack(){
		List<String> scatter2DurationingProductOids = productDao.getScatter2DurationingProductOids();
		List<DealMessageEntity> scatter2DurationingProductInfos = electronicSignatureRelationEmDao.getScatter2DurationingProductInfos(scatter2DurationingProductOids);
		scatter2DurationingProductInfos.forEach(messageEntity->{
			raiseFullAmountCallBack(messageEntity);
		});
	}


	/**
	 * 募集满额触发回调
	 *
	 * @param [messageEntity]
	 * @return java.util.Map<java.lang.String,java.lang.String>
	 * @author yujianlong
	 * @date 2018/4/21 15:04
	 */
	@Transactional
	public Map<String, String> raiseFullAmountCallBack(DealMessageEntity messageEntity) {
		log.info("开始处理募集满额触发条件");
		log.info("募集满额触发传参{}", JSONObject.toJSONString(messageEntity));
		Map<String, String> result = new HashMap<>();
		result.put("status", "200");
		result.put("desc", "募集满额触发成功");
		String productOid = messageEntity.getTriggerProductOid();

		Date orderTime = Optional.ofNullable(messageEntity.getOrderTime()).map(d -> Date.valueOf(DateUtil.format(d, "yyyy-MM-dd"))).orElse(Date.valueOf(LocalDate.now()));
		//判断是否
		Product product = productDao.findOne(productOid);
		if (null == product) {
			log.info("没有该产品");
			result.put("desc", "没有该产品");
			return result;
		}
		if (Objects.equals(product.getState(), Product.STATE_Durationing)) {
			log.info("已募集满额");
			result.put("desc", "已募集满额");
			return result;
		}
		BigDecimal collectedVolume = Optional.ofNullable(product).map(Product::getCollectedVolume).orElse(BigDecimal.ZERO);
		BigDecimal raisedTotalNumber = Optional.ofNullable(product).map(Product::getRaisedTotalNumber).orElse(BigDecimal.ZERO);
		if (raisedTotalNumber.compareTo(BigDecimal.ZERO) == 0) {
			log.info("产品信息有误");
			result.put("desc", "产品信息有误");
			return result;
		}
		if (raisedTotalNumber.compareTo(collectedVolume) > 0) {
			log.info("尚未募集满额");
			result.put("desc", "尚未募集满额");
			return result;
		}

		String productCode = product.getCode();

		if (raisedTotalNumber.compareTo(collectedVolume) == 0) {
			DistributeLock lock = new DistributeLock(redis, "HANDLESCATTER_" + productOid, 600000, 10000, 20, 40);
			try {
				if (lock.lock()) {
					BoundHashOperations<String, Object, Object> handleScatterOper = redis.boundHashOps(HANDLESCATTER);
					Object isHandled = handleScatterOper.get(productOid);
					if (null != isHandled) {
						log.info("募集满额已处理");
						result.put("desc", "募集满额已处理");
						return result;
					}
					LoanContract loanContract = loanContractDao.findOne(productCode);
					//募集满额
					companyLoanServiceExt.raiseFullStepOne(orderTime, product, handleScatterOper);
					//电子签章
					this.raiseFullStepTwo(loanContract, productOid);

				} else {
					result.put("desc", "程序正被其他线程处理...");
					return result;
				}
			} catch (Exception e) {
				e.printStackTrace();
				log.error("{}{}", e.getCause(), e.getMessage());
				log.error("募集满额触发失败,请重试,productOid={}", productOid);
				result.put("status", "400");
				result.put("desc", "募集满额触发失败,请重试");
			} finally {
				lock.unLock();
			}
		}
		log.info("结束处理募集满额触发条件");
		return result;
	}

	/**
	 *
	 *电子签章重试逻辑
	 * @author yujianlong
	 * @date 2018/4/23 15:38
	 * @param [productOid]
	 * @return void
	 */
	public void raiseFullStepTwoTryAgain(String productOid){
		Product product = productDao.findOne(productOid);
		String productCode = product.getCode();
		LoanContract loanContract = loanContractDao.findOne(productCode);
		raiseFullStepTwo(loanContract,productOid);
	}

	/**
	 * 电子签章
	 *
	 * @param [loanContract, productOid]
	 * @return void
	 * @author yujianlong
	 * @date 2018/4/23 14:31
	 */
	public void raiseFullStepTwo(LoanContract loanContract, String productOid) {
		long start = System.currentTimeMillis();
		ConcurrentLinkedDeque<Map<String, Object>> errList = new ConcurrentLinkedDeque<>();
		//查询需要打电子签章的订单信息
		List<Map<String, Object>> tradeInfos = electronicSignatureRelationEmDao.getTradeInfos(productOid);
		int totalCount = tradeInfos.size();
		if (0 == totalCount) {
			log.info("需处理数据为0，无需生成电子签章");
			return;
		}
		ThreadFactory threadFactory = Executors.defaultThreadFactory();
		int bufferSize = 1024 * 16;
		Disruptor<ElectronicSignatureEvent> disruptor =
				new Disruptor<>(ElectronicSignatureEvent::new, bufferSize, threadFactory, ProducerType.MULTI, new SleepingWaitStrategy());

		/*定义disruptor异常处理策略*/
		disruptor.handleExceptionsWith(new ExceptionHandler<ElectronicSignatureEvent>() {
			@Override
			public void handleEventException(Throwable throwable, long l, ElectronicSignatureEvent electronicSignatureEvent) {
				throwable.printStackTrace();
				errList.add(electronicSignatureEvent.getTradeInfo());
				log.error("异常数据节点:{}", electronicSignatureEvent);
				log.error("异常原因:{},{}", throwable.getMessage(), throwable.getCause());
			}

			@Override
			public void handleOnStartException(Throwable throwable) {
				log.error("handleOnStartException:{}", throwable);
			}

			@Override
			public void handleOnShutdownException(Throwable throwable) {
				log.error("handleOnShutdownException:{}", throwable);
			}
		});

		//企业名
		String orgName = loanContract.getOrgName();
		int handlerCount = totalCount > 40 ? 40 : (totalCount + 1) / 2;
		/*定义消费者策略[new]*/
		disruptor.handleEventsWithWorkerPool(
				IntStream.rangeClosed(1, handlerCount).boxed().map(i -> {
					WorkHandler<ElectronicSignatureEvent> worker = (electronicSignatureEvent) -> {
						//消费者接受事件
						companyLoanServiceExt.saveElectronicSignatureRelation(electronicSignatureEvent.getLoanContract(), electronicSignatureEvent.getTradeInfo());
					};
					return worker;
				}).toArray(WorkHandler[]::new)
		);
		//开始监听
		disruptor.start();
		/*定义单参发射器*/
		RingBuffer<ElectronicSignatureEvent> ringBuffer = disruptor.getRingBuffer();
		EventTranslatorOneArg<ElectronicSignatureEvent, ElectronicSignatureEvent> TRANSLATOR_ONE = (electronicSignatureEvent1, sequence, electronicSignatureEvent2) -> {
			electronicSignatureEvent1.setLoanContract(electronicSignatureEvent2.getLoanContract());
			electronicSignatureEvent1.setTradeInfo(electronicSignatureEvent2.getTradeInfo());
		};
		tradeInfos.parallelStream().forEach(m -> {
			ElectronicSignatureEvent electronicSignatureEvent = new ElectronicSignatureEvent();
			electronicSignatureEvent.setTradeInfo(m);
			electronicSignatureEvent.setLoanContract(loanContract);
			ringBuffer.publishEvent(TRANSLATOR_ONE, electronicSignatureEvent);
		});
		//等待所有执行结束
		disruptor.shutdown();
		//计算耗时
		long end = System.currentTimeMillis();
		if (errList.size() > 0) {
			log.error("失败数据:{}", errList);
			//发送重试消息
			publishSubject.onNext(productOid);
		}
		log.info("企业合同:{},产品{}电子签章总耗时:{},处理数量:{},失败数量:{}", loanContract, productOid, (end - start), handlerCount, errList.size());
	}

	/**
	 * 电子签章传输事件
	 *
	 * @param
	 * @author yujianlong
	 * @date 2018/4/23 14:51
	 * @return
	 */
	public static class ElectronicSignatureEvent {

		private LoanContract loanContract;
		private Map<String, Object> tradeInfo;

		public ElectronicSignatureEvent() {
		}

		public ElectronicSignatureEvent(LoanContract loanContract, Map<String, Object> tradeInfo) {
			this.loanContract = loanContract;
			this.tradeInfo = tradeInfo;
		}

		public LoanContract getLoanContract() {
			return loanContract;
		}

		public void setLoanContract(LoanContract loanContract) {
			this.loanContract = loanContract;
		}

		public Map<String, Object> getTradeInfo() {
			return tradeInfo;
		}

		public void setTradeInfo(Map<String, Object> tradeInfo) {
			this.tradeInfo = tradeInfo;
		}

		@Override
		public String toString() {
			return "ElectronicSignatureEvent{" +
					"loanContract=" + loanContract +
					", tradeInfo=" + tradeInfo +
					'}';
		}
	}
}
