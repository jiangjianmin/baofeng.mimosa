package com.guohuai.ams.product;

import com.alibaba.fastjson.JSON;
import com.guohuai.ams.duration.assetPool.AssetPoolEntity;
import com.guohuai.ams.duration.assetPool.AssetPoolService;
import com.guohuai.ams.guess.GuessEntity;
import com.guohuai.ams.guess.GuessService;
import com.guohuai.ams.label.LabelEnum;
import com.guohuai.ams.label.LabelService;
import com.guohuai.ams.product.cycleProduct.CycleProductContinueInvestService;
import com.guohuai.ams.product.cycleProduct.CycleProductOperateEntity;
import com.guohuai.ams.product.cycleProduct.CycleProductOperateService;
import com.guohuai.ams.product.order.salePosition.ProductSalePositionDao;
import com.guohuai.ams.product.order.salePosition.ProductSalePositionOrder;
import com.guohuai.ams.product.order.salePosition.ProductSaleScheduleDao;
import com.guohuai.ams.product.order.salePosition.ProductSaleScheduling;
import com.guohuai.ams.productLabel.ProductLabelService;
import com.guohuai.ams.productPackage.ProductPackage;
import com.guohuai.ams.productPackage.ProductPackageDao;
import com.guohuai.basic.message.DealMessageEntity;
import com.guohuai.basic.message.DealMessageEnum;
import com.guohuai.bfsms.BfSMSTypeEnum;
import com.guohuai.bfsms.BfSMSUtils;
import com.guohuai.calendar.TradeCalendarService;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.message.MessageSendUtil;
import com.guohuai.component.util.DateUtil;
import com.guohuai.mmp.investor.referprofit.ProfitDetailService;
import com.guohuai.mmp.investor.referprofit.ProfitProvideDetailService;
import com.guohuai.mmp.investor.referprofit.ProfitSpecialDateUtil;
import com.guohuai.mmp.investor.tradeorder.InvestorRepayCashTradeOrderRequireNewService;
import com.guohuai.mmp.investor.tradeorder.InvestorRepayCashTradeOrderService;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderService;
import com.guohuai.mmp.job.lock.JobLockEntity;
import com.guohuai.mmp.job.lock.JobLockService;
import com.guohuai.mmp.job.log.JobLogEntity;
import com.guohuai.mmp.job.log.JobLogFactory;
import com.guohuai.mmp.job.log.JobLogService;
import com.guohuai.mmp.platform.baseaccount.statistics.PlatformStatisticsService;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountDao;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountEntity;
import com.guohuai.mmp.publisher.baseaccount.statistics.PublisherStatisticsService;
import com.guohuai.mmp.publisher.hold.PublisherHoldEntity;
import com.guohuai.mmp.publisher.hold.PublisherHoldService;
import com.guohuai.mmp.publisher.product.rewardincomepractice.PracticeService;
import com.guohuai.mmp.schedule.cyclesplit.DayCutService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductSchedService {

    private Logger logger = LoggerFactory.getLogger(ProductSchedService.class);

    @Autowired
    private ProductDao productDao;
    @Autowired
    private DayCutService dayCutService;
    @Autowired
    private ProductSalePositionDao salePositionDao;
    @Autowired
    private ProductSaleScheduleDao productSaleScheduleDao;
    @Autowired
    private ProductService productService;
    @Autowired
    private ProfitDetailService profitDetailService;
    @Autowired
    private ProfitProvideDetailService profitProvideDetailService;
    @Autowired
    private ProductLabelService productLabelService;
    @Autowired
    private LabelService labelService;
    @Autowired
    private ProductDurationService productDurationService;
    @Autowired
    private JobLockService jobLockService;
    @Autowired
    private PracticeService practiceService;
    @Autowired
    private PublisherStatisticsService publisherStatisticsService;
    @Autowired
    private InvestorTradeOrderService investorTradeOrderService;
    @Autowired
    private PlatformStatisticsService platformStatisticsService;
    @Autowired
    private AssetPoolService assetPoolService;
    @Autowired
    private PublisherBaseAccountDao publisherBaseAccountDao;
    @Autowired
    private PublisherHoldService publisherHoldService;
    @Autowired
    private JobLogService jobLogService;
    @Autowired
    private ProductPackageDao productPackageDao;

    @Autowired
    private CycleProductContinueInvestService cycleProductContinueInvestService;

    @Autowired
    private InvestorRepayCashTradeOrderService investorRepayCashTradeOrderService;

    @Autowired
    private BfSMSUtils bfSMSUtils;
    @Autowired
    private MessageSendUtil messageSendUtil;
    @Value("${bfsms.managePhone:#{null}}")
    private String managePhone;
    @Value("${bfsms.supplement.limit:#{null}}")
    private Integer supplementLimit;
    @Value("${bfsms.volumeInsufficiente.limit:1000000}")
    private BigDecimal volumeInsufficiente;
    @Autowired
    private GuessService guessService;

    /**
     * 活期: 当<<成立开始日期>>到,募集未开始变为募集中;
     * 定期: 当<<募集开始日期>>到,募集未开始变为募集中;
     * 定期: 当<<募集結束日期>>到或募集满额,募集中变为募集結束;
     * 定期: 当<<存续期开始日期>>到,募集結束变为存续期;
     * 定期: 当<<存续期结束日期>>到,存续期变为存续期結束;
     */
    @Transactional(value = TxType.REQUIRES_NEW)
    public void notstartraiseToRaisingOrRaised() {

        if (this.jobLockService.getRunPrivilege(JobLockEntity.JOB_jobId_scheduleProductState)) {
            this.notstartraiseToRaising();
        }

    }

    private void notstartraiseToRaising() {
        JobLogEntity jobLog = JobLogFactory.getInstance(JobLockEntity.JOB_jobId_scheduleProductState);
        try {
            List<Product> products = new ArrayList<Product>();
            Date now = DateUtil.parseToSqlDate(new SimpleDateFormat(DateUtil.datePattern).format(new java.util.Date()));

            t0ProductStateChange(products, now);

            tnProductStateChange(products, now);

            if (products.size() > 0) {
                this.productDao.save(products);

            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            jobLog.setJobMessage(AMPException.getStacktrace(e));
            jobLog.setJobStatus(JobLogEntity.JOB_jobStatus_failed);
        }
        jobLog.setBatchEndTime(DateUtil.getSqlCurrentDate());
        this.jobLogService.saveEntity(jobLog);
        this.jobLockService.resetJob(JobLockEntity.JOB_jobId_scheduleProductState);

    }

    //定期: 当<<募集开始日期>>到或<<成立开始日期>>到,募集未开始变为募集中;
    //定期: 当<<募集結束日期>>到或募集满额,募集募集中变为募集結束;
    //定期: 当<<存续期结束日期>>到,存续期变为存续期結束;
    private void tnProductStateChange(List<Product> products, Date now) {
        Specification<Product> periodicSpec = new Specification<Product>() {
            @Override
            public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                In<String> status = cb.in(root.get("state").as(String.class));
                status.value(Product.STATE_Reviewpass).value(Product.STATE_Raising).value(Product.STATE_Durationing);
                return cb.and(cb.equal(root.get("type").get("oid").as(String.class), Product.TYPE_Producttype_01),
                        cb.equal(root.get("isDeleted").as(String.class), Product.NO), status);
            }
        };

        periodicSpec = Specifications.where(periodicSpec);
        List<Product> periodics = this.productDao.findAll(periodicSpec);
        if (periodics != null && periodics.size() > 0) {
            for (Product product : periodics) {
                if (Product.STATE_Reviewpass.equals(product.getState())) {
                    if (product.getRaiseStartDate() != null && DateUtil.daysBetween(DateUtil.getSqlDate(), product.getRaiseStartDate()) >= 0) {
                        product.setState(Product.STATE_Raising);
                        product.setUpdateTime(DateUtil.getSqlCurrentDate());
                        products.add(product);
                        /**
                         * 定期产品进入募集期时，增加产品发行数量 活期产品进入存续期时
                         * @author yuechao
                         */
                        publisherStatisticsService.increaseReleasedProductAmount(product.getPublisherBaseAccount());
                        platformStatisticsService.increaseReleasedProductAmount();
                        /**
                         * 定期产品进入募集期时，增加在售产品数量 活期产品进入存续期时
                         */
                        publisherStatisticsService.increaseOnSaleProductAmount(product.getPublisherBaseAccount());
                        platformStatisticsService.increaseOnSaleProductAmount();
                    }
                } else if (Product.STATE_Raising.equals(product.getState())) {
                    //如果产品包募集期到，将状态改为募集结束----
                    ProductPackage pp = product.getProductPackage();
                    if (pp != null && product.getRaiseEndDate() != null
                            && DateUtil.daysBetween(DateUtil.getSqlDate(), pp.getRaiseEndDate()) > 0) {//判断产品包是否到期
                        pp.setState(ProductPackage.STATE_Raiseend);
                        //改关联的竞猜活动的状态为“已结束”
                        guessService.endGuessIfNotEnd(pp);
                    }
                    //如果产品包募集期到，将状态改为募集结束----
                    BigDecimal investMin = null == product.getInvestMin() ? BigDecimal.ZERO : product.getInvestMin();

                    if (Product.RAISE_FULL_FOUND_TYPE_AUTO.equals(product.getRaiseFullFoundType())
                            && product.getCurrentVolume().compareTo(product.getRaisedTotalNumber()) >= 0
                            && product.getRaiseEndDate() != null
                            && DateUtil.daysBetween(DateUtil.getSqlDate(), product.getRaiseEndDate()) > 0) { // 募集满额后是否自动触发成立

                        product.setState(Product.STATE_Durationing);

                        product.setSetupDate(DateUtil.getSqlDate());// 产品成立时间（存续期开始时间）

                        product.setDurationPeriodEndDate(DateUtil.addSQLDays(product.getSetupDate(),
                                product.getDurationPeriodDays() - 1));// 存续期结束时间

                        // 到期最晚还本付息日 指存续期结束后的还本付息最迟发生在存续期后的第X个自然日的23:59:59为止
                        product.setRepayDate(DateUtil.addSQLDays(product.getDurationPeriodEndDate(), product.getAccrualRepayDays()));// 到期还款时间

                        products.add(product);

                        publisherStatisticsService.increaseReleasedProductAmount(product.getPublisherBaseAccount());

                        sendMessage(product, DealMessageEnum.PRODUCT_BEGIN.name());
                        // 定期募集成立时创建生成二级邀请奖励收益明细的序列化任务--yihonglei add 2017-06-13
                        productDurationService.tnProfitDetailSerial(product);
                    } else {
                        if (product.getRaiseEndDate() != null
                                && DateUtil.daysBetween(DateUtil.getSqlDate(), product.getRaiseEndDate()) > 0) { // 定期:
                            // 募集募集中变为募集結束;
                            product.setState(Product.STATE_Raiseend);
                            product.setUpdateTime(DateUtil.getSqlCurrentDate());
                            products.add(product);
                        }
                    }

                } else if (Product.STATE_Durationing.equals(product.getState())) { // 定期:
                    // 当<<存续期结束日期>>到,存续期变为存续期結束;
                    if (product.getDurationPeriodEndDate() != null
                            && DateUtil.daysBetween(DateUtil.getSqlDate(), product.getDurationPeriodEndDate()) > 0) {
                        product.setState(Product.STATE_Durationend);
                        /** 批量派息审核新增 **/
                        product.setInterestAuditStatus(Product.INTEREST_AUDIT_STATUS_toCommit);
                        /** 批量派息审核新增 **/
                        product.setUpdateTime(DateUtil.getSqlCurrentDate());
                        products.add(product);

                        /**
                         * @author yuechao
                         */
						/*investorTradeOrderService.snapshotVolume(product, DateUtil.getSqlDate());
						practiceService.processOneItem(product.getOid(), DateUtil.getSqlDate());*/

                        // modify snapShotDate
                        investorTradeOrderService.snapshotVolume(product, product.getDurationPeriodEndDate());
                        practiceService.processOneItem(product.getOid(), product.getDurationPeriodEndDate());

                        /**
                         * 定期产品存续期结束之后，增加待结算产品数量
                         */
                        publisherStatisticsService.increaseToCloseProductAmount(product.getPublisherBaseAccount());
                        this.platformStatisticsService.increaseToCloseProductAmount();
                    }
                }
            }
        }
    }

    private void sendMessage(Product p, String tag) {
        List<InvestorTradeOrderEntity> tradeOrderList = investorTradeOrderService
                .findConfirmedOrderByProductOid(p.getOid());
        for (InvestorTradeOrderEntity orderEntity : tradeOrderList) {
            DealMessageEntity messageEntity = new DealMessageEntity();
            messageEntity.setPhone(orderEntity.getInvestorBaseAccount().getPhoneNum());
            messageEntity.setOrderAmount(orderEntity.getOrderAmount().setScale(2, BigDecimal.ROUND_DOWN).toString());
            messageEntity.setOrderTime(orderEntity.getOrderTime());
            messageEntity.setProductName(orderEntity.getProduct().getName());
            messageEntity.setSettlementDate(p.getRepayDate());
            messageSendUtil.sendTimedTopicMessage(messageSendUtil.getDealTopic(), tag, messageEntity);
        }
    }

    //活期产品 当<<成立开始日期>>到,存续期未开始变为存续期;
    private void t0ProductStateChange(List<Product> products, Date now) {
        Specification<Product> nostartDurationsSpec = new Specification<Product>() {
            @Override
            public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.and(cb.equal(root.get("type").get("oid").as(String.class), Product.TYPE_Producttype_02), // 活期
                        cb.equal(root.get("state").as(String.class), Product.STATE_Reviewpass), // 存续期未开始
                        cb.equal(root.get("isDeleted").as(String.class), Product.NO)// 产品未删除
                );
            }
        };
        nostartDurationsSpec = Specifications.where(nostartDurationsSpec);
        List<Product> nostartDurations = this.productDao.findAll(nostartDurationsSpec);
        if (nostartDurations != null && nostartDurations.size() > 0) {
            for (Product product : nostartDurations) {
                if (product.getSetupDate() != null && product.getSetupDate().getTime() <= now.getTime()) {
                    product.setState(Product.STATE_Durationing);
                    product.setUpdateTime(DateUtil.getSqlCurrentDate());
                    products.add(product);
                    /**
                     * 定期产品进入募集期时，增加产品发行数量 活期产品进入存续期时
                     *
                     * @author yuechao
                     */
                    publisherStatisticsService.increaseReleasedProductAmount(product.getPublisherBaseAccount());
                    platformStatisticsService.increaseReleasedProductAmount();
                    /**
                     * 定期产品进入募集期时，增加在售产品数量 活期产品进入存续期时
                     */
                    publisherStatisticsService.increaseOnSaleProductAmount(product.getPublisherBaseAccount());
                    platformStatisticsService.increaseOnSaleProductAmount();
                }
            }
        }
    }

    /**
     * 可售份额排期发放
     */
    @Transactional(value = TxType.REQUIRES_NEW)
    public void scheduleSendProductMaxSaleVolume() {

        if (this.jobLockService.getRunPrivilege(JobLockEntity.JOB_jobId_scheduleSendProductMaxSaleVolume)) {
            this.sendProductMaxSaleVolume();
        }

    }

    /**
     * 可售份额排期发放
     */
    private void sendProductMaxSaleVolume() {
        JobLogEntity jobLog = JobLogFactory.getInstance(JobLockEntity.JOB_jobId_scheduleSendProductMaxSaleVolume);
        try {
            final Date today = DateUtil.formatUtilToSql(DateUtil.getCurrDate());

            Specification<ProductSalePositionOrder> spec = new Specification<ProductSalePositionOrder>() {
                @Override
                public Predicate toPredicate(Root<ProductSalePositionOrder> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                    return cb.and(cb.equal(root.get("productSaleScheduling").get("basicDate").as(Date.class), today),
                            cb.equal(root.get("status").as(String.class), ProductSalePositionOrder.STATUS_PASS));
                }
            };
            spec = Specifications.where(spec);

            List<ProductSalePositionOrder> maxSaleVolumes = this.salePositionDao.findAll(spec, new Sort(new Order(Direction.ASC, "createTime")));
            if (maxSaleVolumes != null && maxSaleVolumes.size() > 0) {
                for (ProductSalePositionOrder sp : maxSaleVolumes) {
                    try {
                        this.sendApply(sp);
                    } catch (Exception e) {
                        sp.setStatus(ProductSalePositionOrder.STATUS_DEACTIVE);
                        sp.setErrorMessage("发生异常");
                        if (e.getClass().equals(AMPException.class)) {
                            int errorCode = ((AMPException) e).getCode();
                            if (errorCode == 90031) {
                                sp.setErrorMessage("数据不正常");
                            } else if (errorCode == 90029) {
                                sp.setErrorMessage("可申请份额不足");

                            } else if (errorCode == 90032) {
                                sp.setErrorMessage("更新乐视接口最高可售份额失败");
                            }
                        }
                        salePositionDao.saveAndFlush(sp);
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
        this.jobLockService.resetJob(JobLockEntity.JOB_jobId_scheduleSendProductMaxSaleVolume);

    }

    /**
     * 可售份额申请发送
     *
     * @param oid
     * @param operator
     * @throws Exception
     */
    private void sendApply(ProductSalePositionOrder pspo) throws Exception {
        if (pspo.getProduct() == null || pspo.getProductSaleScheduling() == null || !ProductSalePositionOrder.STATUS_PASS.equals(pspo.getStatus())) {
            throw AMPException.getException(90031);
        }
        ProductSaleScheduling productSaleSchedule = this.productSaleScheduleDao.findOne(pspo.getProductSaleScheduling().getOid());
        BigDecimal newMaxSaleVolume = pspo.getVolume();//申请份额

        Product product = this.productService.getProductByOid(pspo.getProduct().getOid());
        AssetPoolEntity assetPool = this.assetPoolService.getByOid(product.getAssetPool().getOid());
        if (assetPool != null) {
            PublisherBaseAccountEntity spv = this.publisherBaseAccountDao.findOne(assetPool.getSpvEntity().getOid());

            PublisherHoldEntity hold = this.publisherHoldService.getAssetPoolSpvHold(assetPool, spv);
            if (hold != null) {
                BigDecimal holdTotalVolume = hold.getTotalVolume() != null ? hold.getTotalVolume() : BigDecimal.ZERO;
                //更新产品的maxSaleVolume
                int adjust = productDao.updateMaxSaleVolume(pspo.getProduct().getOid(), newMaxSaleVolume, holdTotalVolume);
                if (adjust > 0) {
                    Timestamp now = new Timestamp(System.currentTimeMillis());
                    pspo.setStatus(ProductSalePositionOrder.STATUS_ACTIVE);
                    salePositionDao.saveAndFlush(pspo);

                    productSaleSchedule.setApprovalAmount(productSaleSchedule.getApprovalAmount().add(newMaxSaleVolume));// 生效份额
                    productSaleSchedule.setSyncTime(now);// 同步时间
                    productSaleSchedule.setUpdateTime(now);
                    productSaleScheduleDao.save(productSaleSchedule);

                    /**
                     * 同步信息到redis
                     * @author yuechao
                     */
//					cacheProductService.syncProductMaxSaleVolume(pspo.getProduct().getOid(), newMaxSaleVolume);
                }
            }

        }

    }

    /**
     * 剩余赎回金额每日还原 :dailyNetMaxRredeem 重置为 netMaxRredeemDay
     */
    @Transactional(value = TxType.REQUIRES_NEW)
    public void scheduleProductDailyMaxRredeem() {
        if (this.jobLockService.getRunPrivilege(JobLockEntity.JOB_jobId_scheduleProductDailyMaxRredeem)) {
            this.productDailyMaxRredeem();
        }
    }

    private void productDailyMaxRredeem() {
        JobLogEntity jobLog = JobLogFactory.getInstance(JobLockEntity.JOB_jobId_scheduleProductDailyMaxRredeem);
        try {
            Specification<Product> spec = new Specification<Product>() {
                @Override
                public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                    return cb.and(cb.equal(root.get("isDeleted").as(String.class), Product.NO), cb.equal(root.get("auditState").as(String.class), Product.AUDIT_STATE_Reviewed),
                            cb.equal(root.get("type").get("oid").as(String.class), Product.TYPE_Producttype_02));
                }
            };
            spec = Specifications.where(spec);

            List<Product> ps = this.productDao.findAll(spec, new Sort(new Order(Direction.DESC, "updateTime")));

            if (ps != null && ps.size() > 0) {
                for (Product p : ps) {
                    logger.info("<<-----" + p.getOid() + "剩余赎回金额每日还原 start----->>");
                    try {
                        p.setDailyNetMaxRredeem(p.getNetMaxRredeemDay());
                        this.productDao.save(p);
//						/**
//						 * 同步信息到redis
//						 * @author yuechao
//						 */
//						cacheProductService.syncProductDailyNetMaxRredeem(p);
                    } catch (Exception e) {
                        this.logger.error(p.getOid() + "dailyNetMaxRredeem 每日重置为 netMaxRredeemDay失败", e);
                        throw e;
                    }
                    logger.info("<<-----" + p.getOid() + "剩余赎回金额每日还原 end----->>");
                }
            }


        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            jobLog.setJobMessage(AMPException.getStacktrace(e));
            jobLog.setJobStatus(JobLogEntity.JOB_jobStatus_failed);
        }

        jobLog.setBatchEndTime(DateUtil.getSqlCurrentDate());
        this.jobLogService.saveEntity(jobLog);
        this.jobLockService.resetJob(JobLockEntity.JOB_jobId_scheduleProductDailyMaxRredeem);

    }

    /**
     * 产品份额补充定时提醒
     */
    @Transactional(value = TxType.REQUIRES_NEW)
    public void scheduleShareSupplement() {

        if (this.jobLockService.getRunPrivilege(JobLockEntity.JOB_jobId_scheduleShareSupplement)) {
            JobLogEntity jobLog = JobLogFactory.getInstance(JobLockEntity.JOB_jobId_scheduleShareSupplement);
            try {
                shareSupplementDo();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                jobLog.setJobMessage(AMPException.getStacktrace(e));
                jobLog.setJobStatus(JobLogEntity.JOB_jobStatus_failed);
            }
            jobLog.setBatchEndTime(DateUtil.getSqlCurrentDate());
            this.jobLogService.saveEntity(jobLog);
            this.jobLockService.resetJob(JobLockEntity.JOB_jobId_scheduleShareSupplement);
        }

    }

    /**
     * 产品份额补充定时提醒
     *
     * @throws Exception
     */
    private void shareSupplementDo() {
        if (supplementLimit == null || supplementLimit == 0) {
            logger.info("补充份额定时提醒未配置限额或限额为0，将不提醒！");
            return;
        }
        if (managePhone == null || managePhone.isEmpty()) {
            logger.info("补充份额定时提醒未配置手机号，将不提醒！");
            return;
        }

        Specification<Product> spec = new Specification<Product>() {
            @Override
            public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Predicate a = cb.equal(root.get("isDeleted").as(String.class), Product.NO);
                Predicate b = cb.equal(root.get("state").as(String.class), Product.STATE_Durationing);
                Predicate c = cb.equal(root.get("type").get("oid").as(String.class), Product.TYPE_Producttype_02);
                query.where(cb.and(a, b, c));
                query.orderBy(cb.asc(root.get("updateTime")));
                return query.getRestriction();
            }
        };

        List<Product> ps = this.productDao.findAll(spec);

        if (ps != null && !ps.isEmpty()) {
            for (Product en : ps) {
                BigDecimal cha = en.getMaxSaleVolume().subtract(en.getLockCollectedVolume());
                if (cha.compareTo(new BigDecimal(supplementLimit)) < 1) {
                    String[] arrays = {en.getFullName(), cha.setScale(2).toString()};

                    String[] phones = null;
                    try {
                        phones = JSON.parseObject(managePhone, String[].class);
                    } catch (Exception e) {
                        logger.error("补充份额定时提醒手机号格式出错，请修改配置文件，例如：bfsms.managePhone=[“13245678999”,“13345678999”]，错误内容：" + e.getMessage());
                        return;
                    }

                    if (phones != null && phones.length > 0) {
                        for (String phone : phones) {
                            bfSMSUtils.sendByType(phone, BfSMSTypeEnum.smstypeEnum.sharesupplement.toString(), arrays);
                        }
                    }
                }
            }
        }

    }

    /**
     * 每月一号对上月活期奖励收益明细汇总
     */
    public void t0Profit() {

        if (this.jobLockService.getRunPrivilege(JobLockEntity.JOB_jobId_profitT0)) {
            this.t0ProfitLog();
        }

    }

    public void t0ProfitLog() {
        JobLogEntity jobLog = JobLogFactory.getInstance(JobLockEntity.JOB_jobId_profitT0);
        try {
            this.t0ProfitDo();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            jobLog.setJobMessage(AMPException.getStacktrace(e));
            jobLog.setJobStatus(JobLogEntity.JOB_jobStatus_failed);
        }
        jobLog.setBatchEndTime(DateUtil.getSqlCurrentDate());
        this.jobLogService.saveEntity(jobLog);
        this.jobLockService.resetJob(JobLockEntity.JOB_jobId_profitT0);
    }

    public void t0ProfitDo() {
        String lastYearMonth = ProfitSpecialDateUtil.getLastYearMonth();// 获取上月月日
        // 查询满足奖励发放的活期产品
        List<Product> productList = this.productDao.getT0ProductProfit();

        if (productList.size() > 0) {
            for (Product product : productList) {
                String label = productLabelService.findLabelByProduct(product);
                if (!labelService.isProductLabelHasAppointLabel(label, LabelEnum.tiyanjin.toString())) {// 排除体验金产品
                    if (!profitDetailService.checkT0ProductProfit(lastYearMonth, product.getOid())) {// 判断活期上月收益明细是否已生成
                        // 生成活期奖励收益明细
                        int result = this.profitDetailService.initT0ProductProfitDetail(product, lastYearMonth);
                        if (result > 0) {
                            // 根据奖励收益明细和奖励规则生成奖励发放明细
                            result = this.profitProvideDetailService.initProductProfitProvideDetail(product.getOid());
                        }
                    } else {
                        logger.info("{}活期奖励收益明细已生成，不能再生成！", lastYearMonth);
                    }
                }
            }
        } else {
            logger.info("无可汇总奖励收益明细活期产品!");
        }
    }


    /**
     * 开放式循环产品200人日切
     *
     * @param []
     * @return void
     * @author yujianlong
     * @date 2018/3/21 15:24
     */
    public void createProduct03() {
        if (this.jobLockService.getRunPrivilege(JobLockEntity.JOB_jobId_createProduct03)) {
            createProduct03Log();
        }
    }

    public void createProduct03Log() {
        JobLogEntity jobLog = JobLogFactory.getInstance(JobLockEntity.JOB_jobId_createProduct03);
        try {
            createProduct03Do();

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            jobLog.setJobMessage(AMPException.getStacktrace(e));
            jobLog.setJobStatus(JobLogEntity.JOB_jobStatus_failed);
        }
        jobLog.setBatchEndTime(DateUtil.getSqlCurrentDate());
        this.jobLogService.saveEntity(jobLog);
        this.jobLockService.resetJob(JobLockEntity.JOB_jobId_createProduct03);
    }

    /**
     * 循环产品日切主逻辑
     *
     * @param []
     * @return void
     * @author yujianlong
     * @date 2018/3/21 17:18
     */
    public void createProduct03Do() {
        //查询虚产品，isdeleted是no的，不存在则直接return
        logger.info("=====createProduct03DoDayCut begin=====");
        dayCutService.doDayCut(null);
        logger.info("=====createProduct03DoDayCut end=====");
    }


    /**
     * 根据产品包上架产品
     */
    public void createProductFromPackage() {
        if (this.jobLockService.getRunPrivilege(JobLockEntity.JOB_jobId_createProductFromPackage)) {
            createProductFromPackageLog();
        }
    }

    public void createProductFromPackageLog() {
        JobLogEntity jobLog = JobLogFactory.getInstance(JobLockEntity.JOB_jobId_createProductFromPackage);
        try {
            createProductFromPackageDo();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            jobLog.setJobMessage(AMPException.getStacktrace(e));
            jobLog.setJobStatus(JobLogEntity.JOB_jobStatus_failed);
        }
        jobLog.setBatchEndTime(DateUtil.getSqlCurrentDate());
        this.jobLogService.saveEntity(jobLog);
        this.jobLockService.resetJob(JobLockEntity.JOB_jobId_createProductFromPackage);
    }

    /**
     * 根据产品包上架产品
     */
    public void createProductFromPackageDo() {
        logger.info("=====createProductFromPackageDo begin=====");
        // 查询在售的所有产品包（复核通过且当前时间在募集时间之内）
        List<ProductPackage> productPackages = this.productPackageDao.findOnSaleProductPackages(DateUtil.getSqlDate());
        logger.info("DateUtil.getSqlDate()" + DateUtil.getSqlDate());
        if (productPackages != null && productPackages.size() > 0) {
            for (ProductPackage productPackage : productPackages) {
                GuessEntity guess = guessService.getGuessById(productPackage);
                if (guess != null) {
                    //如果关联的竞猜活动已结束，直接跳到下一次循环
                    if (GuessEntity.GUESS_STATUS_END.equals(guess.getStatus())) {
                        logger.debug("<-----产品包:{}对应的竞猜活动已结束，进行下次循环---->", productPackage.getOid());
                        continue;
                    }
                }
                int toProductNum = productPackage.getToProductNum();//上架产品数量
                int productCount = productPackage.getProductCount();//产品包包含的产品数量
                // 校验剩余时间是否满足上架产品
                java.util.Date now = new java.util.Date();

                String limitTimes = productPackage.getLimitTime();
                Double limitHour = Double.parseDouble(limitTimes);
                long nowTime = now.getTime();
                long endTime = DateUtil.addDay(productPackage.getRaiseEndDate(), 1).getTime();
                long leftTime = endTime - nowTime;
                long limitTime = (long) (limitHour * 3600 * 1000);
                logger.info("===productPackage:{}===", productPackage.getOid());
                logger.info("===now:{},RaiseEndDate:{}===", now, DateUtil.addDay(productPackage.getRaiseEndDate(), 1));
                logger.info("===limitTime:{},leftTime:{}===", limitTime, leftTime);
                logger.info("====checkProductFound:{}====", checkProductFound(productPackage));

                if (checkProductFound(productPackage)) {
                    if (leftTime >= limitTime) { //剩余时间足够，可以上架此产品包的产品
                        // 查询产品包下面已经上架的产品的数量
                        if (toProductNum < productCount) {//当上架数量小于产品总数时
                            //上架本产品包的产品
                            productService.createProductFromPackage(productPackage);
                            guessService.onshelfGuess(productPackage);//上架竞猜活动
                        } else {
                            logger.info("=====productPackage{}所有产皮包上架完毕，无可用产品{}=====", productPackage.getOid(), productPackage.getToProductNum());
                            guessService.endGuess(productPackage);//结束竞猜活动
                        }
                    } else {
                        logger.info("=====productPackage{}剩余募集时间不满足上架产品需求{}=====", productPackage.getOid(), productPackage.getLimitTime());
                        guessService.endGuess(productPackage);//结束竞猜活动
                    }
                } else {
                    logger.info("=====productPackage{}下存在募集中的产品,不能新建新的=====", productPackage.getOid());
                }
            }
        }
        logger.info("=====createProductFromPackageDo end=====");
    }

    /**
     * 判断定期产品的产品剩余份额与最小可投金额
     */
    public boolean checkProductFound(ProductPackage productPackage){
        boolean canFoundProduct = false;
        List<Product> raisingProductFromProductPackages = productDao.raisingProductFromProductPackage(productPackage.getOid());
        if(raisingProductFromProductPackages.size() == 0){
            canFoundProduct = true;
        }else{
            //bug#3218 产品份额   -  （已售份额+锁定的订单份额））小于起投金额时，下一个产品即上架。
            long lessThanInvestMinCount = raisingProductFromProductPackages.stream().filter(product -> {
                BigDecimal raisedTotalNumber = Optional.ofNullable(product.getRaisedTotalNumber()).orElse(BigDecimal.ZERO);
                BigDecimal lockCollectedVolume = Optional.ofNullable(product.getLockCollectedVolume()).orElse(BigDecimal.ZERO);
                BigDecimal investMin = Optional.ofNullable(product.getInvestMin()).orElse(BigDecimal.ZERO);
                BigDecimal currentVolume = Optional.ofNullable(product.getCurrentVolume()).orElse(BigDecimal.ZERO);
                BigDecimal currentVolumeAddlockCollectedVolume = currentVolume.add(lockCollectedVolume);
                BigDecimal jugement = raisedTotalNumber.subtract(currentVolumeAddlockCollectedVolume).subtract(investMin);
                return jugement.compareTo(BigDecimal.ZERO) < 0;
            }).count();
            if (lessThanInvestMinCount==raisingProductFromProductPackages.size()){
                canFoundProduct= true;
            }
        }
        return canFoundProduct;
    }

    /**
     * <p>Title: </p>
     * <p>Description: 快活宝，天天向上份额不足提醒</p>
     * <p>Company: </p>
     *
     * @author 邱亮
     * @date 2017年11月2日 下午5:25:12
     * @since 1.0.0
     */
    public void t0ProductVolumeInsufficientNotice() {
        if (this.jobLockService.getRunPrivilege(JobLockEntity.JOB_jobId_t0ProductVolumeInsufficientNotice)) {
            JobLogEntity jobLog = JobLogFactory.getInstance(JobLockEntity.JOB_jobId_t0ProductVolumeInsufficientNotice);
            try {
                t0ProductVolumeInsufficientNoticeDo();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                jobLog.setJobMessage(AMPException.getStacktrace(e));
                jobLog.setJobStatus(JobLogEntity.JOB_jobStatus_failed);
            }
            jobLog.setBatchEndTime(DateUtil.getSqlCurrentDate());
            this.jobLogService.saveEntity(jobLog);
            this.jobLockService.resetJob(JobLockEntity.JOB_jobId_t0ProductVolumeInsufficientNotice);
        }

    }

    private void t0ProductVolumeInsufficientNoticeDo() {
        if (managePhone == null || managePhone.isEmpty()) {
            logger.info("发放收益提醒未配置手机号，将不提醒！");
            return;
        }
        String[] phones = null;
        try {
            phones = JSON.parseObject(managePhone, String[].class);
        } catch (Exception e) {
            logger.error("发放收益提醒手机号格式出错，请修改配置文件，例如：bfsms.incomeAllocate.managePhone=[“13245678999”,“13345678999”]，错误内容：" + e.getMessage());
            return;
        }
        if (phones == null || phones.length == 0) {
            logger.info("发放收益提醒未配置手机号，将不提醒！");
            return;
        }
        Specification<Product> spec = new Specification<Product>() {
            @Override
            public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Predicate a = cb.equal(root.get("type").get("oid").as(String.class), Product.TYPE_Producttype_02);
                Predicate b = cb.equal(root.get("state").as(String.class), Product.STATE_Durationing);
                return cb.and(a, b);
            }
        };

        List<Product> productList = productService.findAll(spec);
        for (Product p : productList) {
            BigDecimal maxSaleVolume = p.getMaxSaleVolume();
            BigDecimal lockedCollectedVolume = p.getLockCollectedVolume();
            if (maxSaleVolume.subtract(lockedCollectedVolume).compareTo(volumeInsufficiente) <= 0) {
                String[] pNames = new String[]{p.getFullName()};
                //发消息提醒
                for (String phone : phones) {
                    logger.info("========份额不足报警，phone:{},pNames:{}=====", phone, pNames);
                    bfSMSUtils.sendByType(phone, BfSMSTypeEnum.smstypeEnum.volumeInsufficienteNotice.toString(), pNames);
                }
            }
        }
    }

    /**
     * 循环产品到期处理
     * <div>
     * 1. 派息
     * 2. 判断是否续投
     * 2.1 还本付息
     * 2.2 续投
     * </div>
     */
    @Transactional
    public void cycleProductDurationEnd() {
        if (this.jobLockService.getRunPrivilege(JobLockEntity.JOB_jobId_cycleProductDurationEnd)) {
            this.cycleProductDurationEndLog();
        }
    }

    private void cycleProductDurationEndLog() {
        JobLogEntity jobLog = JobLogFactory.getInstance(JobLockEntity.JOB_jobId_cycleProductDurationEnd);
        try {
            cycleProductDurationEndDo();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            jobLog.setJobMessage(AMPException.getStacktrace(e));
            jobLog.setJobStatus(JobLogEntity.JOB_jobStatus_failed);
        }
        jobLog.setBatchEndTime(DateUtil.getSqlCurrentDate());
        this.jobLogService.saveEntity(jobLog);
        this.jobLockService.resetJob(JobLockEntity.JOB_jobId_cycleProductDurationEnd);
    }

    private void cycleProductDurationEndDo() {
        // 1. 派息
        productService.cycleProductInterest();
        // 2. 删除临时表已处理数据
        productService.deleteToRepayListNoUseData();
        productService.cycleProductAddToOperatingList();

        // 还本付息
        investorRepayCashTradeOrderService.cycleProductRepay();

        // 续投处理
        cycleProductContinueInvestService.continueInvest();
    }
}
