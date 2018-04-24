package com.guohuai.ams.product.cycleProduct;

import com.guohuai.ams.product.Product;
import com.guohuai.basic.message.DealMessageEntity;
import com.guohuai.basic.message.DealMessageEnum;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.message.MessageSendUtil;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.StringUtil;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountEntity;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountService;
import com.guohuai.mmp.investor.tradeorder.*;
import com.guohuai.mmp.publisher.hold.PublisherHoldDao;
import com.guohuai.mmp.publisher.hold.PublisherHoldEntity;
import com.guohuai.mmp.publisher.investor.InterestFormula;
import com.guohuai.mmp.schedule.cyclesplit.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class CycleProductContinueInvestExtService {

    @Autowired
    private CycleProductOperateContinueService cycleProductOperateContinueService;

    @Autowired
    private CycleProductOperateDao cycleProductOperateDao;

    @Autowired
    private DayCutServiceExt dayCutServiceExt;

    @Autowired
    private DayCutTradeOrderEMDao dayCutTradeOrderEMDao;

    @Autowired
    private DayCutPublisherHoldEMDao publisherHoldEMDao;

    @Autowired
    private DayCutOpenCycleRalationEMDao dayCutOpenCycleRalationEMDao;

    @Autowired
    private CycleProductOperateEMDao cycleProductOperateEMDao;

    @Autowired
    private InvestorBaseAccountService investorBaseAccountService;

    @Autowired
    private PublisherHoldDao publisherHoldDao;

    @Autowired
    private InvestorTradeOrderDao investorTradeOrderDao;

    @Autowired
    private InvestorOpenCycleDao investorOpenCycleDao;

    @Autowired
    private MessageSendUtil messageSendUtil;

    /**
     * 续投
     *
     * @param continueInvestTransferEvent
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void continueInvest(ContinueInvestTransferEvent continueInvestTransferEvent) {
        try {
            continueInvestDo(continueInvestTransferEvent);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("【循环产品续投】执行错误,page=" + continueInvestTransferEvent.getPage());
            if (e instanceof AMPException) {
                throw e;
            } else {
                throw new AMPException(900002, "【循环产品续投】子事务执行错误,page=" + continueInvestTransferEvent.getPage());
            }
        }
    }

    /**
     * 续投实际逻辑
     *
     * @param continueInvestTransferEvent
     */
    @Transactional
    public void continueInvestDo(ContinueInvestTransferEvent continueInvestTransferEvent) {
        // 当前处理页数
        int page = continueInvestTransferEvent.getPage();
        // 总资产
        BigDecimal availableAmount = continueInvestTransferEvent.getAvailableAmount();
        // 虚拟产品
        Product product = continueInvestTransferEvent.getProduct();
        ConcurrentLinkedDeque<BigDecimal> sumAmount = continueInvestTransferEvent.getSumAmount();

        // 需要处理的用户oid
        List<String> investorOids = continueInvestTransferEvent.getInvestorOids();

        List<String> investorOidList = new ArrayList<>(investorOids);

        if (investorOidList == null || investorOidList.size() < 1) {
            log.info("【循环产品续投】执行至第{}页，数据为空！", page);
            return;
        }

        log.info("【循环产品续投】执行至第{}页，共{}条数据", page, investorOidList.size());

        // 持仓金额总数
        BigDecimal allAmount = sumAmount.stream().reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        // TODO 资产池不足判断
        if (allAmount.compareTo(availableAmount) > 0) {
            log.error("【循环产品续投】资产池不足！");
            throw new AMPException(900001, "【循环产品续投】资产池不足");
        }


        List<CycleProductOperateContinueEntity> cycleProductOperateContinueEntities = cycleProductOperateContinueService.findAllByInvestorOids(investorOidList);
        List<CycleProductOperateEntity> cycleProductOperateEntities = cycleProductOperateDao.findByInvestorOidsAndStatus(investorOidList, 0);
        if (cycleProductOperateContinueEntities == null || cycleProductOperateEntities == null || cycleProductOperateContinueEntities.size() < 1 || cycleProductOperateEntities.size() < 1) {
            log.error("【循环产品续投】执行至第{}页，数据错误，续投中间表数据不能为空！", page);
            throw new AMPException("数据错误，续投中间表数据不能为空");
        }

        // 当前需要处理的持仓总额
        BigDecimal currentAmount = cycleProductOperateContinueEntities.stream().map(CycleProductOperateContinueEntity::getOrderAmount).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);

        Map<String, ProductNameAndInvestAmount> code_ProductNameAndInvestAmountAll = new HashMap<>();
        //需要发送消息的中间表关系集合
        List<InvestorOpenCycleRelationEntity> relation2Send = new ArrayList<>();
        // 按照处理日期分组
        Map<LocalDate, List<CycleProductOperateContinueEntity>> continueGroup = cycleProductOperateContinueEntities.stream().collect(Collectors.groupingBy(entity -> entity.getOperateDate().toLocalDate()));
        Timestamp orderTime = Timestamp.valueOf(LocalDateTime.now());
        continueGroup.entrySet().stream().forEach(entry -> {
            LocalDate localDate = entry.getKey();
            List<CycleProductOperateContinueEntity> entities = entry.getValue();
            // 新产品募集总额
            BigDecimal totalAmount = entities.stream().map(CycleProductOperateContinueEntity::getOrderAmount).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
            // 新建产品
            Product newProduct = dayCutServiceExt.createProduct(product, localDate, totalAmount, entities.size());
            // 新建持仓
            List<PublisherHoldEntity> newHolds = entities.stream().map(cycleProductOperateEntity -> {
                InvestorBaseAccountEntity investorBaseAccountEntity = investorBaseAccountService.findOne(cycleProductOperateEntity.getInvestorOid());
                BigDecimal expectIncome = InterestFormula.simple(cycleProductOperateEntity.getOrderAmount(), newProduct.getExpAror(), newProduct.getIncomeCalcBasis(), newProduct.getDurationPeriodDays());
                return createPublisherHoldEntity(investorBaseAccountEntity, newProduct, cycleProductOperateEntity.getOrderAmount(), expectIncome);
            }).collect(Collectors.toList());
            //新订单
            List<InvestorTradeOrderEntity> newTradeOrders = newHolds.stream().map(holdEntity -> {
                return dayCutServiceExt.createInvestorTradeOrderEntity(holdEntity, newProduct, orderTime, InvestorTradeOrderEntity.TRADEORDER_orderType_continueInvest);
            }).collect(Collectors.toList());

            //生成code和产品投资对应关系
            Map<String, ProductNameAndInvestAmount> code_ProductNameAndInvestAmount = newTradeOrders.stream().collect(Collectors.toMap(o -> o.getOrderCode(),
                    o -> {
                        ProductNameAndInvestAmount productNameAndInvestAmount = new ProductNameAndInvestAmount(o.getProduct().getName(), o.getOrderAmount());
                        return productNameAndInvestAmount;
                    }));

            code_ProductNameAndInvestAmountAll.putAll(code_ProductNameAndInvestAmount);
            Map<String, String> newOrderInvestorOidOrderCodeMap = newTradeOrders.stream().collect(Collectors.toMap(o -> o.getInvestorBaseAccount().getOid(), InvestorTradeOrderEntity::getOrderCode));

            //新中间关系
            continueInvestTransferEvent.getTmpMap().putAll(newOrderInvestorOidOrderCodeMap);

            List<InvestorOpenCycleRelationEntity> relationEntities = newTradeOrders.stream().map(investorTradeOrderEntity -> {
                return dayCutServiceExt.createOpenCycleRelationEntity(investorTradeOrderEntity, InvestorOpenCycleRelationEntity.ORDERTYPE_CONTINUE);
            }).collect(Collectors.toList());

            relation2Send.addAll(relationEntities);

            //批量插入新的持仓
            publisherHoldEMDao.batchInsert(newHolds);
            //批量插入新的转投单
            dayCutTradeOrderEMDao.batchInsert(newTradeOrders);
            //批量插入转投单关系
            dayCutOpenCycleRalationEMDao.batchInsert(relationEntities);

            //释放内存
            newHolds.clear();
            newHolds = null;
            newTradeOrders.clear();
            newTradeOrders = null;
        });

        List<String> holdOids = cycleProductOperateEntities.stream().map(cycleProductOperateEntity -> cycleProductOperateEntity.getHoldOid()).collect(Collectors.toList());
        List<String> orderCodes = cycleProductOperateEntities.stream().map(cycleProductOperateEntity -> cycleProductOperateEntity.getOrderCode()).collect(Collectors.toList());
        List<InvestorOpenCycleRelationEntity> investorOpenCycleRelationEntities = investorOpenCycleDao.findAll(orderCodes);
        cycleProductOperateEntities.forEach(cycleProductOperateEntity -> cycleProductOperateEntity.setStatus(1));
        investorOpenCycleRelationEntities.forEach(investorOpenCycleRelationEntity -> {
            String code = continueInvestTransferEvent.getTmpMap().get(investorOpenCycleRelationEntity.getInvestorOid());

            ProductNameAndInvestAmount productNameAndInvestAmount = code_ProductNameAndInvestAmountAll.get(code);
            //设置产品名和投资金额
            investorOpenCycleRelationEntity.setInvestAmount(productNameAndInvestAmount.getInvestAmount());
            investorOpenCycleRelationEntity.setInvestProductName(productNameAndInvestAmount.getProductName());
            investorOpenCycleRelationEntity.setInvestOrderCode(code);
            investorOpenCycleRelationEntity.setAssignment(InvestorOpenCycleRelationEntity.ASSIGNMENTTYPE_CONTINUEINVEST);
        });
        // 更新原持仓
        List<PublisherHoldEntity> holdEntities = publisherHoldDao.findAll(holdOids);
        holdEntities.stream().forEach(publisherHoldEntity -> {
            publisherHoldEntity.setTotalVolume(BigDecimal.ZERO);
            publisherHoldEntity.setHoldVolume(BigDecimal.ZERO);
            publisherHoldEntity.setAccruableHoldVolume(BigDecimal.ZERO);
            publisherHoldEntity.setRedeemableHoldVolume(BigDecimal.ZERO);
            publisherHoldEntity.setValue(BigDecimal.ZERO);
            publisherHoldEntity.setHoldStatus(PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_closed);
        });

        // 更新原订单
        List<InvestorTradeOrderEntity> tradeOrderEntities = investorTradeOrderDao.findByOrderCodes(orderCodes);
        tradeOrderEntities.stream().forEach(investorTradeOrderEntity -> {
            investorTradeOrderEntity.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_invalidate);
            investorTradeOrderEntity.setHoldVolume(BigDecimal.ZERO);
            investorTradeOrderEntity.setValue(BigDecimal.ZERO);
            investorTradeOrderEntity.setHoldStatus(PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_closed);
        });

        publisherHoldEMDao.batchUpdate(holdEntities);

        dayCutTradeOrderEMDao.batchUpdate(tradeOrderEntities);
        // 更新中间表数据
        cycleProductOperateEMDao.batchUpdate(cycleProductOperateEntities);
        // 更新关联关系表
        dayCutOpenCycleRalationEMDao.batchUpdate(investorOpenCycleRelationEntities);


        sumAmount.add(currentAmount);
        // 发送续投个推消息
        String tag = DealMessageEnum.CYCLE_CONTINUE_INVEST_DONE.name();
        relation2Send.forEach(investorOpenCycleRelationEntity -> {
            try {
                DealMessageEntity messageEntity = new DealMessageEntity();
                messageEntity.setPhone(investorOpenCycleRelationEntity.getPhone());
                messageEntity.setProductName(product.getName());
                messageEntity.setOrderTime(investorOpenCycleRelationEntity.getCreateTime());
                messageEntity.setOrderAmount(investorOpenCycleRelationEntity.getSourceOrderAmount().setScale(2, BigDecimal.ROUND_DOWN).toString());
                messageSendUtil.sendTimedTopicMessage(messageSendUtil.getDealTopic(), tag, messageEntity);
            } catch (Exception e) {
                log.error("【循环产品续投】用户{}续投个推消息发送异常！", investorOpenCycleRelationEntity.getPhone(), e);
            }
        });
        investorOidList = null;
        cycleProductOperateContinueEntities.clear();
        cycleProductOperateEntities.clear();
        relation2Send.clear();
        holdEntities.clear();
        tradeOrderEntities.clear();
        investorOpenCycleRelationEntities.clear();
        cycleProductOperateEntities.clear();

        log.info("【循环产品续投】第{}页执行完成！", page);

    }

    /**
     * 存放产品名和投资金额的class
     *
     * @param
     * @author yujianlong
     * @date 2018/4/10 20:07
     * @return
     */
    private static class ProductNameAndInvestAmount {
        private String productName;
        private BigDecimal investAmount;

        public ProductNameAndInvestAmount() {
        }

        public ProductNameAndInvestAmount(String productName, BigDecimal investAmount) {
            this.productName = productName;
            this.investAmount = investAmount;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public BigDecimal getInvestAmount() {
            return investAmount;
        }

        public void setInvestAmount(BigDecimal investAmount) {
            this.investAmount = investAmount;
        }

        @Override
        public String toString() {
            return "ProductNameAndInvestAmount{" +
                    "productName='" + productName + '\'' +
                    ", investAmount=" + investAmount +
                    '}';
        }
    }

    /**
     * 新建持仓
     *
     * @param investorBaseAccountEntity
     * @param product
     * @param holdVolume
     * @param expectIncome
     * @return
     */
    public PublisherHoldEntity createPublisherHoldEntity(InvestorBaseAccountEntity investorBaseAccountEntity, Product product, BigDecimal holdVolume, BigDecimal expectIncome) {
        BigDecimal expectIncomeExt = InterestFormula.simple(holdVolume,
                product.getExpArorSec(), product.getIncomeCalcBasis(), product.getDurationPeriodDays());

        PublisherHoldEntity publisherHoldEntity = new PublisherHoldEntity();
        publisherHoldEntity.setAssignedId(StringUtil.uuid());
        publisherHoldEntity.setProduct(product);
        publisherHoldEntity.setAssetPool(product.getAssetPool());
        publisherHoldEntity.setPublisherBaseAccount(product.getPublisherBaseAccount()); // 所属发行人
        publisherHoldEntity.setInvestorBaseAccount(investorBaseAccountEntity); // 所属投资人
        publisherHoldEntity.setTotalVolume(holdVolume); // 总份额
        publisherHoldEntity.setHoldVolume(holdVolume);
        publisherHoldEntity.setToConfirmInvestVolume(BigDecimal.ZERO);
        publisherHoldEntity.setToConfirmRedeemVolume(BigDecimal.ZERO);
        publisherHoldEntity.setRedeemableHoldVolume(holdVolume);// 可赎回份额
        publisherHoldEntity.setLockRedeemHoldVolume(BigDecimal.ZERO);// 赎回锁定份额
        publisherHoldEntity.setExpGoldVolume(BigDecimal.ZERO);
        publisherHoldEntity.setTotalInvestVolume(holdVolume);
        publisherHoldEntity.setAccruableHoldVolume(holdVolume);
        publisherHoldEntity.setValue(holdVolume);
        publisherHoldEntity.setHoldTotalIncome(BigDecimal.ZERO);
        publisherHoldEntity.setTotalBaseIncome(BigDecimal.ZERO);
        publisherHoldEntity.setTotalRewardIncome(BigDecimal.ZERO);
        publisherHoldEntity.setHoldYesterdayIncome(BigDecimal.ZERO);
        publisherHoldEntity.setYesterdayBaseIncome(BigDecimal.ZERO);
        publisherHoldEntity.setYesterdayRewardIncome(BigDecimal.ZERO);
        publisherHoldEntity.setIncomeAmount(BigDecimal.ZERO);
        publisherHoldEntity.setRedeemableIncome(BigDecimal.ZERO);
        publisherHoldEntity.setLockIncome(BigDecimal.ZERO);
        publisherHoldEntity.setConfirmDate(DateUtil.getSqlDate());
        publisherHoldEntity.setExpectIncome(expectIncome);
        publisherHoldEntity.setExpectIncomeExt(expectIncomeExt);
        publisherHoldEntity.setAccountType(PublisherHoldEntity.PUBLISHER_accountType_INVESTOR);
        publisherHoldEntity.setMaxHoldVolume(holdVolume);
        publisherHoldEntity.setDayInvestVolume(holdVolume);
        publisherHoldEntity.setDayRedeemVolume(BigDecimal.ZERO);
        publisherHoldEntity.setDayRedeemCount(0);
        publisherHoldEntity.setProductAlias(product.getName());
        publisherHoldEntity.setHoldStatus(PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_holding);

        return publisherHoldEntity;
    }

}
