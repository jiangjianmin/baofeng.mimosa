package com.guohuai.ams.activityModel.ttzActivity;

import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductPojo;
import com.guohuai.ams.product.ProductService;
import com.guohuai.cache.CacheKeyConstants;
import com.guohuai.cache.service.RedisExecuteLogExtService;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.DecimalUtil;
import com.guohuai.component.util.StrRedisUtil;
import com.guohuai.component.web.view.BaseRep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class TtzActivityService {

    @Autowired
    private ProductService productService;

    @Autowired
    private TtzActivityDao ttzActivityDao;

    @Autowired
    private TtzActivityRatioRangeCache ttzActivityRatioRangeCache;

    @Autowired
    private RedisTemplate<String, String> redis;

    @Autowired
    private RedisExecuteLogExtService redisExecuteLogExtService;

    @Value("${activity.ttz.tn.ratio:6.3}")
    private BigDecimal tnRatio;

    private static final String ACTIVITY_TTZ_KEY_DATA = "activity:ttz:data:totalInvest";
    private static final String ACTIVITY_TTZ_KEY_COUNT = "activity:ttz:data:shareCount";

    /**
     * 保存团团赚活动模板
     *
     * @param form
     * @return
     */
    public BaseRep save(TtzActivityForm form) {
        log.info("【团团赚活动】配置信息：{}", form);
        TtzActivityEntity entity = ttzActivityDao.getCurrentActivity();
        if (entity == null) {
            entity = new TtzActivityEntity();
        }
        BeanUtils.copyProperties(form, entity);
        ttzActivityDao.save(entity);
        return new BaseRep();
    }

    /**
     * 后台MimosaUI获取活动模板
     *
     * @return
     */
    public TtzActivityForm bootQuery() {
        TtzActivityEntity entity = ttzActivityDao.getCurrentActivity();
        TtzActivityForm form = new TtzActivityForm();
        if (entity != null) {
            BeanUtils.copyProperties(entity, form);
        }
        return form;
    }

    /**
     * 前台查询活动模板
     *
     * @return
     */
    public TtzActivityRep model() {
        TtzActivityEntity entity = ttzActivityDao.getCurrentActivity();
        TtzActivityRep rep = new TtzActivityRep();
        rep.setActivityEnd(true);
        if (entity != null) {
            Date now = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date endDate = null;
            try {
                endDate = sdf.parse(entity.getActivityTimeEnd());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (now.after(endDate)) {
                rep.setTitle(entity.getTitle());
                rep.setBannerImageUrl(entity.getBannerImageUrl());
                rep.setActivityEnd(true);
            } else {
                rep.setTitle(entity.getTitle());
                rep.setBannerImageUrl(entity.getBannerImageUrl());
                rep.setRuleImageUrl(entity.getRuleImageUrl());
                rep.setActivityEnd(false);
            }
        }
        return rep;
    }

    /**
     * 根据渠道号获取团团赚活动标签下的产品
     *
     * @param channelOid
     * @return
     */
    public TtzActivityProductListRep findProductsByGeneralTagAndChannel(String channelOid) {
        TtzActivityProductListRep rep = new TtzActivityProductListRep();
        TtzActivityEntity entity = ttzActivityDao.getCurrentActivity();
        if (entity != null) {
            List<Object[]> products = productService.findProductsByChannelAndProductLabel(channelOid, entity.getProductLabel());
            log.info("【团团赚活动】渠道：{}下产品列表：{}", channelOid, products);
            for (Object[] arr : products) {
                ProductPojo pojo = new ProductPojo();
                pojo.setProductOid((String) arr[0]);
                pojo.setName((String) arr[1]);
                pojo.setInvestMin((BigDecimal) arr[2]);
                pojo.setExpAror((BigDecimal) arr[3]);
                pojo.setExpArorSec((BigDecimal) arr[4]);
                pojo.setRewardInterest((BigDecimal) arr[5]);
                pojo.setDurationPeriodDays(((Integer) arr[6]));
                pojo.setRaisedTotalNumber((BigDecimal) arr[7]);
                pojo.setCollectedVolume((BigDecimal) arr[8]);
                pojo.setLockCollectedVolume((BigDecimal) arr[9]);
                pojo.setStateOrder((arr[10]).toString());
                pojo.setState((String) arr[11]);
                pojo.setType((String) arr[12]);
                pojo.setPurchaseNum(((Integer) arr[14]));
                pojo.setSetupDate((String) arr[15]);
                if (null != pojo.getExpAror() && null != pojo.getExpArorSec()) {
                    pojo.setShowType(ProductPojo.ProductPojo_showType_double);
                } else {
                    pojo.setShowType(ProductPojo.ProductPojo_showType_single);
                }
                if (pojo.getExpAror().compareTo(pojo.getExpArorSec()) == 0) {
                    pojo.setExpArrorDisp(DecimalUtil.zoomOut(pojo.getExpAror(), 100, 2) + "%");
                } else {
                    pojo.setExpArrorDisp(DecimalUtil.zoomOut(pojo.getExpAror(), 100, 2)
                            + "%~" + DecimalUtil.zoomOut(pojo.getExpArorSec(), 100, 2) + "%");
                }
                pojo.setSubType(ProductPojo.DEPOSIT_SUBTYPE);
                rep.getRows().add(pojo);
            }
        }
        return rep;
    }

    /**
     * 查询团团赚活动相关投资数据
     *
     * @param uid
     * @return
     */
    public TtzActivityInvestmentDataRep getInvestmentData(String uid) {
        TtzActivityInvestmentDataRep rep = new TtzActivityInvestmentDataRep();
        boolean exist = StrRedisUtil.exists(redis, ACTIVITY_TTZ_KEY_DATA);
        TtzActivityEntity entity = ttzActivityDao.getCurrentActivity();
        if (entity == null) {
            log.info("【团团赚活动】暂无团团赚活动！");
            return rep;
        }

        String startTime = entity.getActivityTimeBegin();
        String endTime = entity.getActivityTimeEnd();

        // 获取活动期间内在售的某一产品
        List<Object[]> productInfo = ttzActivityDao.getActivityProductInfoByLabelAndRaiseTimeRange(entity.getProductLabel(), startTime, endTime);
        if (productInfo == null || productInfo.size() < 1) {
            log.info("【团团赚活动】暂无团团赚活动相关产品！");
            if (uid != null) {
                rep.setMyInvestAmount("0.00");
                rep.setMyExpectedIncome("0.00");
                rep.setNormalTnIncome("0.00");
            }
            return rep;
        }

        BigDecimal incomeCalcBasics = new BigDecimal((String) productInfo.get(0)[0]);
        BigDecimal durationPeriodDays = new BigDecimal((Integer) productInfo.get(0)[1]);
        log.info("【团团赚活动】相关产品收益计算基础为：{}，存续期天数为：{}！", incomeCalcBasics, durationPeriodDays);

        BigDecimal total;
        BigDecimal ratio = BigDecimal.ZERO;

        // 活动产品对应总投资额
        if (exist) {
            total = new BigDecimal(StrRedisUtil.get(redis, ACTIVITY_TTZ_KEY_DATA)).setScale(2, BigDecimal.ROUND_DOWN);
        } else {
            total = ttzActivityDao.getTotalAmountByProductLabelAndRaiseTimeRange(entity.getProductLabel(), startTime, endTime).setScale(2, BigDecimal.ROUND_DOWN);
            StrRedisUtil.setEx(redis, ACTIVITY_TTZ_KEY_DATA, 60, total);
            log.info("【团团赚活动】{}-{}，总投资：{}，写入Redis", startTime, endTime, total);
        }
        // 总投资额对应利率
        List<TtzActivityRatio> ratioCache = ttzActivityRatioRangeCache.getRatioCache();
        for (TtzActivityRatio ttzActivityRatio : ratioCache) {
            if (total.compareTo(ttzActivityRatio.getAmount()) > 0) {
                ratio = ttzActivityRatio.getRatio();
            }
        }

        rep.setActivityTotalInvest(total.toString());
        rep.setCurrentRatio(ratio.toString());
        log.info("【团团赚活动】团团赚活动总投资：{}，当前利率{}！", total, ratio);

        if (uid != null) {
            // 我的投资总额
            BigDecimal myInvest = ttzActivityDao.getMyTotalInvestAmountByProductLabelAndRaiseTimeRange(uid, entity.getProductLabel(), startTime, endTime).setScale(2, BigDecimal.ROUND_DOWN);

            // 我的预期利息
            BigDecimal expIncome = ttzActivityDao.getIncomeByUidAndRatioAndProductLabelAndTimeRange(uid, DecimalUtil.zoomIn(ratio, 100), entity.getProductLabel(), startTime, endTime).setScale(2, BigDecimal.ROUND_DOWN);

            // 普通定期产品的逾期利息
            BigDecimal tnIncome = ttzActivityDao.getIncomeByUidAndRatioAndProductLabelAndTimeRange(uid, DecimalUtil.zoomIn(tnRatio, 100), entity.getProductLabel(), startTime, endTime).setScale(2, BigDecimal.ROUND_DOWN);


            rep.setMyInvestAmount(myInvest.toString());
            rep.setMyExpectedIncome(expIncome.toString());
            rep.setNormalTnIncome(tnIncome.toString());
            log.info("【团团赚活动】用户{}总投资{}，预期利息：{}，普通定期对应利息：{}！", uid, myInvest, expIncome, tnIncome);
        }

        return rep;
    }

    public BaseRep shareCount() {
        Long valOut = redisExecuteLogExtService.hincrByLong(ACTIVITY_TTZ_KEY_COUNT, DateUtil.format(new Date(), DateUtil.datePattern), 1, -1);
        log.info("【团团赚活动】用户分享次数：{}次", valOut);
        return new BaseRep();
    }
}
