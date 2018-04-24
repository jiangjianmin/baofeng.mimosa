package com.guohuai.mmp.schedule.cyclesplit;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.guohuai.ams.duration.assetPool.AssetPoolDao;
import com.guohuai.ams.duration.assetPool.AssetPoolEntity;
import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductDao;
import com.guohuai.basic.component.exception.GHException;
import com.guohuai.basic.message.DealMessageEnum;
import com.guohuai.bfsms.BfSMSTypeEnum;
import com.guohuai.bfsms.BfSMSUtils;
import com.guohuai.component.exception.AMPException;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import io.reactivex.subjects.PublishSubject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


/**
 *日切主要处理逻辑
 *
 * @author yujianlong
 * @date 2018/4/1 17:57
 * @param
 * @return
 */
@Service
@Transactional
public class DayCutService {

	Logger logger = LoggerFactory.getLogger(DayCutService.class);

	/*异常信号定义*/
	/*资产池不足异常*/
	public static final String ASSETPOOLUNAVAILABLE = "ASSETPOOLUNAVAILABLE";
	/*子事务执行异常*/
	public static final String SUBTRANSACTIONFAIL = "SUBTRANSACTIONFAIL";
	/*外部事务执行异常*/
	public static final String OUTTRANSACTIONFAIL = "OUTTRANSACTIONFAIL";
	/*异常重试次数*/
	public static final int RETRYTIMES = 6;
	/*等待时间间隔，默认30分钟*/
	public static final int WAITTIMEOFFSET = 30;
	/*时间单位*/
	public static final TimeUnit SLEEPTIMEUNIT = TimeUnit.MINUTES;

	/*日切200人，可测试时灵活修改*/
//	public static final int dayCutLimit = 200;
	//信号容器
	public static final ConcurrentLinkedDeque<String> signalQueue = new ConcurrentLinkedDeque<>();

	/*日切200人，可测试时灵活修改配置文件*/
	@Value("${daycut.limit:200}")
	private Integer dayCutLimit;

	@Value("${bfsms.managePhone:#{null}}")
	private String managePhone;

	@Autowired
	private BfSMSUtils bfSMSUtils;


	@Autowired
	private DayCutServiceExt dayCutServiceExt;

	@Autowired
	private ProductDao productDao;


	@Autowired
	private AssetPoolDao assetPoolDao;


	@Autowired
	private DayCutPublisherHoldEMDao publisherHoldEMDao;


	@Autowired
	private PublishSubject<DayCutPublishEvent> publishSubject;

	//订阅异常信号，按照次数递增等待时间
	@PostConstruct
	public void init() {
		//sig_time是状态码+时间戳数字
		publishSubject.subscribe(dayCutPublishEvent -> {
			if (null == dayCutPublishEvent) {//如果传递的信号是空值不处理
				return;
			}
			String sig_time = dayCutPublishEvent.getSignal();
			String[] data = sig_time.split("_");
			//获取信号
			String sig = data[0];
			//如果不满足信号规范要求不处理
			if (Stream.of(ASSETPOOLUNAVAILABLE, SUBTRANSACTIONFAIL, OUTTRANSACTIONFAIL)
					.noneMatch(s -> Objects.equals(s, sig))) {
				logger.error("传递的{}信号不符合规范", sig);
				return;
			}
			List<String> holdOids = dayCutPublishEvent.getHolds();
			logger.info("日切出现异常，原码:{},状态码:{},数据:{}", sig_time, sig, dayCutPublishEvent);
			//添加当前信号
			signalQueue.add(sig_time);
			//计算当前信号今天发生了几次
			long times = getTimesFromSignalQueue(sig);
			logger.info("{}信号执行第{}次", sig, times);
			//如果大于0次则每30分钟乘以次数等待后，再次执行日切
			if (times > 0) {
				switch (sig) {
					case ASSETPOOLUNAVAILABLE:
						//进行预警
						String[] phones = null;
						BigDecimal availableAmount =BigDecimal.ZERO;
						try{

							availableAmount=dayCutPublishEvent.getScale().subtract(assetPoolDao.getAssetPoolVolume()).setScale(2);
						}catch (Exception e){
							e.printStackTrace();
							logger.error("计算剩余可用资产池份额错误");
							throw new AMPException(900100,"计算剩余可用资产池份额错误");
						}
						String[] arrays = {DealMessageEnum.CYCLEPROCUCTNAME,availableAmount.toString()};
						try {
							phones = JSON.parseObject(managePhone, String[].class);
						} catch (Exception e) {
							logger.error("补充份额定时提醒手机号格式出错，请修改配置文件，例如：bfsms.managePhone=[“13245678999”,“13345678999”]，错误内容：" + e.getMessage());
							return;
						}

						if (phones != null && phones.length > 0) {
							//发预警信息
							Arrays.stream(phones).forEach(phone -> bfSMSUtils.sendByType(phone, BfSMSTypeEnum.smstypeEnum.sharesupplement.toString(), arrays));
						}
						SLEEPTIMEUNIT.sleep(times*WAITTIMEOFFSET);
						doDayCut(holdOids);
						return;
					case SUBTRANSACTIONFAIL:
						SLEEPTIMEUNIT.sleep((times-1)*WAITTIMEOFFSET);
						doDayCut(holdOids);
						return;

					default:
						logger.error("不满足信号规范");
						return;
				}
			}
		});
	}


	/**
	 * 计算信号当天发生次数
	 *
	 * @param [sig]
	 * @return long
	 * @author yujianlong
	 * @date 2018/4/1 17:58
	 */
	public long getTimesFromSignalQueue(String sig) {
		//获取当前00：00：00的时间戳数字
		Long now = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.of("+8")).toEpochMilli();
		//清除昨日异常信号
		signalQueue.stream().forEach(a -> {
			long thisTime = Long.parseLong(a.split("_")[1]);
			//循环比较信号时间戳和当天0点时间戳，如果小于则从信号队列移除
			if (thisTime < now) {
				signalQueue.remove(a);
			}
		});
		//只计算当日异常处理信号
		long times = signalQueue.stream()
				.filter(s -> s.indexOf(sig) != -1).count();
		return times;
	}



	/**
	 * 日切主方法
	 *
	 * @param []
	 * @return void
	 * @author yujianlong
	 * @date 2018/4/1 18:00
	 */
	@Transactional
	public void doDayCut(List<String> holdOids) {
		logger.info(StringUtils.center("开始转投日切处理",80,"="));
		//记录开始时间
		long start = System.currentTimeMillis();
		Long allCount = 0L;
		//获取产品
		List<Product> product03ReviewList = productDao.getProduct03ReviewList().stream().distinct().collect(Collectors.toList());
		if (null == product03ReviewList || product03ReviewList.isEmpty()) {
			logger.info("开放式循环产品不存在,无需日切操作");
			throw new AMPException(900010,"开放式循环产品不存在,无需日切操作");
		}
		if (product03ReviewList.size() > 1) {
			logger.info("开放式循环产品超过一种,无需日切操作");
			throw new AMPException(900011,"开放式循环产品超过一种,无需日切操作");
		}
		Product product = product03ReviewList.get(0);
		//对付懒加载
		{
			product.getPublisherBaseAccount();
			product.getType();
		}
		AssetPoolEntity assetPool = product.getAssetPool();
		if(null==assetPool){
			logger.error("资产池为空");
			throw new AMPException(900012,"资产池为空");
		}
		List<String> allHoldOids = new ArrayList<>();
		//判断传参
		if (Optional.ofNullable(holdOids).isPresent()) {
			allCount = Long.valueOf(holdOids.size());
			if (0 == allCount) {
				logger.info("循环产品持仓表数量为0，无需进行日切");
//				throw new AMPException(900013,"循环产品持仓表数量为0，无需进行日切");
				return;
			}
			allHoldOids = holdOids;
		} else {
			allCount = publisherHoldEMDao.getKDBHoldCount(product.getOid());
			if (0 == allCount) {
				logger.info("循环产品持仓表数量为0，无需进行日切");
//				throw new AMPException(900014,"循环产品持仓表数量为0，无需进行日切");
				return;
			}
			//查询符合条件的预约持仓oid列表
			allHoldOids = publisherHoldEMDao.getKDBHoldOids(product.getOid());
		}
		//获取当前系统核数
		final int DEFAULT_CORE_THREADS = Runtime.getRuntime().availableProcessors();
		//自定义线程池
		final ThreadPoolExecutor executor = new ThreadPoolExecutor(DEFAULT_CORE_THREADS, 50, 10 * 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1024), new ThreadPoolExecutor.CallerRunsPolicy());

		BigDecimal scale = Optional.ofNullable(assetPool.getScale()).orElse(BigDecimal.ZERO);
		//存放信号的Queue
		ConcurrentLinkedDeque<String> errSignalList = new ConcurrentLinkedDeque<>();
		//存放处理异常的oid
		ConcurrentLinkedDeque<String> errHoldlList = new ConcurrentLinkedDeque<>();
		//分页数
		int rows = dayCutLimit;
		// 获取资产池剩余份额
		BigDecimal availableAmount =
				scale.subtract(assetPoolDao.getAssetPoolVolume());
		if (availableAmount.compareTo(BigDecimal.ZERO)<=0){
			logger.error("资产池剩余可用份额不足");
			throw new AMPException(900015,"资产池剩余可用份额不足");
		}
		//用于统计处理订单的总额
		ConcurrentLinkedDeque<BigDecimal> sumAmount = new ConcurrentLinkedDeque<>();
		//200id一组
		List<List<String>> allHoldOidsPartition = Lists.partition(allHoldOids, dayCutLimit);
		//获取总数
		int totalPage = (int) ((allCount + rows - 1) / rows);
		/*disruptor相关*/
		ThreadFactory threadFactory = Executors.defaultThreadFactory();
		/*环形数组大小*/
		int bufferSize = 1024 * 16;
		/*初始化disruptor*/
		Disruptor<DayCutTransferEvent> disruptor =
				new Disruptor<DayCutTransferEvent>(DayCutTransferEvent::new, bufferSize, threadFactory, ProducerType.MULTI, new SleepingWaitStrategy());
		/*定义disruptor异常处理策略*/
		disruptor.handleExceptionsWith(new ExceptionHandler<DayCutTransferEvent>() {
			@Override
			public void handleEventException(Throwable throwable, long l, DayCutTransferEvent cycleTransferEvent) {
				throwable.printStackTrace();
				//增加到异常holds列表
				errHoldlList.addAll(cycleTransferEvent.getHoldOids());
				if (throwable instanceof GHException) {
					GHException me = (GHException) throwable;
					//报资产池不足异常
					if (me.getCode() == 900001) {
						errSignalList.add(ASSETPOOLUNAVAILABLE);
					}
					//require_new事务中断异常
					if (me.getCode() == 900002) {
						errSignalList.add(SUBTRANSACTIONFAIL);
					}
				}
				logger.error("异常数据节点:{}", cycleTransferEvent);
				logger.error("异常原因:{},{}", throwable.getMessage(), throwable.getCause());
			}
			@Override
			public void handleOnStartException(Throwable throwable) {
				logger.error("handleOnStartException:{}", throwable);
			}
			@Override
			public void handleOnShutdownException(Throwable throwable) {
				logger.error("handleOnShutdownException:{}", throwable);
			}
		});

		//如果分页很多则开更多处理器，而不是固定处理器，至少是1，最多开40个
		int handlerCount=totalPage>40?40:(totalPage+1)/2;
		/*定义消费者策略[new]*/
		disruptor.handleEventsWithWorkerPool(
				IntStream.rangeClosed(1, handlerCount).boxed().map(i -> {
					WorkHandler<DayCutTransferEvent> worker = (dayCutTransferEvent) -> {
						//消费者接受事件
						dayCutServiceExt.doRealCut(dayCutTransferEvent);
					};
					return worker;
				}).toArray(WorkHandler[]::new)
		);
		//开始监听
		disruptor.start();
		/*定义单参发射器*/
		RingBuffer<DayCutTransferEvent> ringBuffer = disruptor.getRingBuffer();
		EventTranslatorOneArg<DayCutTransferEvent, DayCutTransferEvent> TRANSLATOR_ONE = (dayCutTransferEvent1, sequence, dayCutTransferEvent2) -> {
			dayCutTransferEvent1.setAvailableAmount(dayCutTransferEvent2.getAvailableAmount());
			dayCutTransferEvent1.setPage(dayCutTransferEvent2.getPage());
			dayCutTransferEvent1.setProduct(product);
			dayCutTransferEvent1.setSumAmount(sumAmount);
			dayCutTransferEvent1.setHoldOids(dayCutTransferEvent2.getHoldOids());
		};
		//发送事件
		CompletableFuture[] completableFutures =
				IntStream.rangeClosed(1, totalPage).boxed().map(p -> CompletableFuture.runAsync(() -> {
					List<String> holdids = allHoldOidsPartition.get(p - 1);
					DayCutTransferEvent e1 = new DayCutTransferEvent(p, availableAmount, null, holdids, null);
					//发射事件，等待消费者接受事件
					ringBuffer.publishEvent(TRANSLATOR_ONE, e1);
				}, executor)).toArray(CompletableFuture[]::new);
		CompletableFuture.allOf(completableFutures).join();
		//关闭线程池
		executor.shutdown();
		//等待所有执行结束
		disruptor.shutdown();
		List<String> eList = errHoldlList.stream().collect(Collectors.toList());
		//获取异常信号错误次数
		//计算耗时
		long end = System.currentTimeMillis();
		String cycleprocuctname = DealMessageEnum.CYCLEPROCUCTNAME;

		logger.info(cycleprocuctname+"日切总耗时:{},处理数量:{},失败数量:{}", (end - start),allCount,eList.size());
		logger.info(StringUtils.center("转投日切处理完毕",80,"="));

		if (!errSignalList.isEmpty()) {
			String signal = errSignalList.getFirst();
			long timesFromSignalQueue = getTimesFromSignalQueue(signal);
			if (timesFromSignalQueue < RETRYTIMES) {
				Long stamp = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
				DayCutPublishEvent dayCutPublishEvent = new DayCutPublishEvent(signal + "_" + stamp, eList, null);
				if (Objects.equals(signal,ASSETPOOLUNAVAILABLE)){
					//如果是资产池不足，发送规模到事件，用于计算剩余可用份额
					dayCutPublishEvent.setScale(scale);
				}
				publishSubject.onNext(dayCutPublishEvent);
			}
			if (Objects.equals(SUBTRANSACTIONFAIL,signal)){
				logger.error("部分日切执行不成功，原因：内部处理异常,不成功holdoids={}",eList);
				throw new AMPException(900016,"内部事务执行异常,holdoids="+eList);
			}else if(Objects.equals(ASSETPOOLUNAVAILABLE,signal)){
				logger.error("部分日切执行不成功，原因：资产池剩余可用份额不足,不成功holdoids={}",eList);
				throw new AMPException(900017,"资产池剩余可用份额不足,holdoids="+eList);
			}
		}else{
			logger.info("日切插入处理完毕");
		}

	}

}
