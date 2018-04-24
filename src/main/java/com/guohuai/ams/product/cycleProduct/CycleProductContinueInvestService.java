package com.guohuai.ams.product.cycleProduct;

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
import com.guohuai.mmp.schedule.cyclesplit.DayCutPublishEvent;
import com.guohuai.mmp.schedule.cyclesplit.DayCutServiceExt;
import com.guohuai.mmp.schedule.cyclesplit.DayCutTransferEvent;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import io.reactivex.subjects.PublishSubject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
@Slf4j
public class CycleProductContinueInvestService {

    /* 系统核数 */
    private static final int DEFAULT_CORE_THREADS = Runtime.getRuntime().availableProcessors();

    /* 信号容器 */
    private static final ConcurrentLinkedDeque<String> signalQueue = new ConcurrentLinkedDeque<>();

    /**
     * 异常信号定义
     */
    /* 资产池不足 */
    public static final String ASSETPOOL_UNAVAILABLE = "ASSETPOOLUNAVAILABLE";
    /* 子事物执行异常 */
    public static final String SUB_TRANSACTION_FAIL = "SUBTRANSACTIONFAIL";
    /* 外部事物执行异常 */
    public static final String OUT_TRANSACTION_FAIL = "OUTTRANSACTIONFAIL";

    /**
     * 异常重试次数
     */
    public static final int RETRY_TIMES = 6;

    /**
     * 等待时间间隔，默认30分钟
     */
    public static final int WAIT_TIME_OFFSET = 30;

    /**
     * 时间单位
     */
    public static final TimeUnit SLEEP_TIME_UNIT = TimeUnit.MINUTES;

    @Autowired
    private CycleProductOperateContinueService cycleProductOperateContinueService;

    @Autowired
    private ProductDao productDao;

    @Autowired
    private AssetPoolDao assetPoolDao;

    @Autowired
    private CycleProductContinueInvestExtService cycleProductContinueInvestExtService;

    @Autowired
    private BfSMSUtils bfSMSUtils;

    @Value("${daycut.limit:200}")
    private Integer dayCutLimit;

    @Value("${bfsms.managePhone:#{null}}")
    private String managerPhone;

    @Transactional
    public void continueInvest() {
        log.info("【循环产品续投】开始处理续投任务。");
        long startTime = System.currentTimeMillis();

        // 获取循环开放产品
        List<Product> product03List = productDao.getProduct03ReviewList().stream().distinct().collect(Collectors.toList());
        if (null == product03List || product03List.isEmpty() || product03List.size() > 1) {
            log.error("【循环产品续投】开放式循环产品配置不正确，无法进行后续操作！");
            throw new AMPException(900010, "开放式循环产品配置不正确，无法进行后续操作！");
        }
        // PRODUCTTYPE_03 循环开放产品
        Product cycleProduct = product03List.get(0);
        cycleProduct.getPublisherBaseAccount();
        cycleProduct.getType();

        // 循环产品资产池相关
        AssetPoolEntity assetPoolEntity = cycleProduct.getAssetPool();
        if (null == assetPoolEntity) {
            log.error("【循环产品续投】循环开放产品关联资产池不能为空！");
            throw new AMPException(900013, "循环开放产品关联资产池不能为空！");
        }
        BigDecimal scale = Optional.ofNullable(assetPoolEntity.getScale()).orElse(BigDecimal.ZERO);
        // 04产品募集总额
        BigDecimal availableAmount = scale.subtract(assetPoolDao.getAssetPoolVolume());
        if (availableAmount.compareTo(BigDecimal.ZERO) <= 0) {
            log.error("【循环产品续投】资产池可用份额不足！");
            throw new AMPException(900015, "资产池可用份额不足！");
        }

        // 需要处理的用户ID列表
        Long allCount = cycleProductOperateContinueService.countAllNeedContinueInvestData();
        ;
        if (0 == allCount) {
            log.info("【循环产品续投】需处理数据量为0，无需处理。");
            return;
        }
        List<String> investorOids = cycleProductOperateContinueService.getAllNeedContinueInvestorOids();
        ;

        // 线程池
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(DEFAULT_CORE_THREADS, 50, 10 * 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1024), new ThreadPoolExecutor.CallerRunsPolicy());


        // 存放信号
        ConcurrentLinkedDeque<String> errSignalList = new ConcurrentLinkedDeque<>();
        // 存放处理异常的investorOid
        ConcurrentLinkedDeque<String> errInvestorList = new ConcurrentLinkedDeque<>();
        // 用于统计处理订单的总额
        ConcurrentLinkedDeque<BigDecimal> sumAmount = new ConcurrentLinkedDeque<>();
        // 用于存放用户ID和续投单
        ConcurrentHashMap<String, String> investorAndOrderMap = new ConcurrentHashMap<>();

        // 按照单产品限制人数对需处理数据分组
        List<List<String>> investorOidsPartition = Lists.partition(investorOids, dayCutLimit);

        // 总页数
        int totalPage = (int) ((allCount + dayCutLimit - 1) / dayCutLimit);

        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        int bufferSize = 1024 * 16;

        Disruptor<ContinueInvestTransferEvent> disruptor = new Disruptor<>(ContinueInvestTransferEvent::new, bufferSize, threadFactory, ProducerType.MULTI, new SleepingWaitStrategy());

        // 异常处理
        disruptor.setDefaultExceptionHandler(new ExceptionHandler<ContinueInvestTransferEvent>() {
            @Override
            public void handleEventException(Throwable ex, long sequence, ContinueInvestTransferEvent event) {
                ex.printStackTrace();
                errInvestorList.addAll(event.getInvestorOids());
                event.getTmpMap().forEach((k, v) -> {
                    if (investorAndOrderMap.contains(k)) {
                        investorAndOrderMap.remove(k);
                    }
                });
                if (ex instanceof GHException) {
                    GHException e = (GHException) ex;
                    if (e.getCode() == 900001) {
                        errSignalList.add(ASSETPOOL_UNAVAILABLE);
                    }
                    if (e.getCode() == 900002) {
                        errSignalList.add(SUB_TRANSACTION_FAIL);
                    }
                }
                log.error("【循环产品续投】续投异常！异常数据：{}，\n异常原因：{}，\n{}", event, ex.getMessage(), ex.getCause());
            }

            @Override
            public void handleOnStartException(Throwable ex) {
                log.error("【循环产品续投】handleOnStartException:{}", ex);
            }

            @Override
            public void handleOnShutdownException(Throwable ex) {
                log.error("【循环产品续投】handleOnShutdownException:{}", ex);
            }
        });
        //如果分页很多则开更多处理器，而不是固定处理器，至少是1，最多开40个
        int handlerCount=totalPage>40?40:(totalPage+1)/2;
        // 单组处理逻辑
        disruptor.handleEventsWithWorkerPool(
                IntStream.rangeClosed(1, handlerCount).boxed().map(i -> {
                    WorkHandler<ContinueInvestTransferEvent> workHandler = (event -> {
                        // 单组处理逻辑
                        cycleProductContinueInvestExtService.continueInvest(event);
                    });
                    return workHandler;
                }).toArray(WorkHandler[]::new)
        );
        disruptor.start();

        RingBuffer<ContinueInvestTransferEvent> ringBuffer = disruptor.getRingBuffer();
        EventTranslatorOneArg<ContinueInvestTransferEvent, ContinueInvestTransferEvent> TRANSLATOR_ONE = ((event1, sequence, event2) -> {
            event1.setAvailableAmount(event2.getAvailableAmount());
            event1.setPage(event2.getPage());
            event1.setProduct(cycleProduct);
            event1.setSumAmount(sumAmount);
            event1.setInvestorOids(event2.getInvestorOids());
        });

        CompletableFuture[] completableFutures = IntStream.rangeClosed(1, totalPage).boxed().map(p -> CompletableFuture.runAsync(() -> {
            List<String> investors = investorOidsPartition.get(p - 1);
            ContinueInvestTransferEvent event = new ContinueInvestTransferEvent(p, availableAmount, null, investors, null);
            ringBuffer.publishEvent(TRANSLATOR_ONE, event);
        }, executor)).toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(completableFutures).join();

        executor.shutdown();
        disruptor.shutdown();

        // 部分数据处理失败
        if (!errSignalList.isEmpty()) {
            String signal = errSignalList.getFirst();
            exceptionHandling(signal);
            log.error("【循环产品续投】部分数据处理失败！");
            throw new AMPException("部分数据处理失败！");
        }

        long endTime = System.currentTimeMillis();
        log.info("【循环产品续投】续投处理结束，时长：{}s", (endTime - startTime) / 1000);

    }

    private void exceptionHandling(String signal) {
        String cycleprocuctname = DealMessageEnum.CYCLEPROCUCTNAME;
        switch (signal) {
            case ASSETPOOL_UNAVAILABLE:
                String[] phones;
                try {
                    phones = JSON.parseObject(managerPhone, String[].class);
                } catch (Exception e) {
                    log.error("【循环产品续投】提醒手机配置错误！");
                    return;
                }
                if (phones != null && phones.length > 0) {
                    String[] params = {cycleprocuctname, "0"};
                    Arrays.stream(phones).forEach(s -> bfSMSUtils.sendByType(s, BfSMSTypeEnum.smstypeEnum.sharesupplement.toString(), params));
                }
                throw new AMPException(cycleprocuctname+"份额不足，请补充！");
            default:
                break;
        }
    }


}
