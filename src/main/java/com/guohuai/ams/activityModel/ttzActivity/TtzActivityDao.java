package com.guohuai.ams.activityModel.ttzActivity;

import com.guohuai.ams.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface TtzActivityDao extends JpaRepository<TtzActivityEntity, String> {

    @Query(value = "select * from T_GAM_ACTIVITY_TTZ limit 1", nativeQuery = true)
    public TtzActivityEntity getCurrentActivity();

    @Query(value = " SELECT ifnull(sum(t1.collectedVolume), 0) " +
            " FROM T_GAM_PRODUCT t1 " +
            " WHERE t1.type = 'PRODUCTTYPE_01' AND t1.productLabel = ?1 AND t1.raiseStartDate BETWEEN ?2 AND ?3 AND " +
            "      t1.isActivityProduct = 0 AND t1.guessOid IS NULL", nativeQuery = true)
    BigDecimal getTotalAmountByProductLabelAndRaiseTimeRange(String productLabel, String startTime, String endTime);

    @Query(value = " SELECT ifnull(sum(t1.holdVolume), 0) " +
            " FROM t_money_publisher_hold t1, t_gam_product t2 " +
            " WHERE t1.productOid = t2.oid AND t1.investorOid = ?1 AND t2.type = 'PRODUCTTYPE_01' AND t2.productLabel = ?2 AND " +
            "      t2.raiseStartDate BETWEEN ?3 AND ?4 AND t2.isActivityProduct = 0 AND t2.guessOid IS NULL", nativeQuery = true)
    BigDecimal getMyTotalInvestAmountByProductLabelAndRaiseTimeRange(String uid, String productLabel, String startTime, String endTime);

    @Query(value = " SELECT t1.incomeCalcBasis, t1.durationPeriodDays " +
            " FROM T_GAM_PRODUCT t1 " +
            " WHERE t1.type = 'PRODUCTTYPE_01' AND t1.productLabel = ?1 AND t1.raiseStartDate BETWEEN ?2 AND ?3 AND " +
            "      t1.isActivityProduct = 0 AND t1.guessOid IS NULL " +
            " LIMIT 1", nativeQuery = true)
    List<Object[]> getActivityProductInfoByLabelAndRaiseTimeRange(String productLabel, String startTime, String endTime);

    @Query(value = " SELECT ifnull(sum(truncate(t2.holdVolume * ?2 * t1.durationPeriodDays / t1.incomeCalcBasis, 2)), 0) " +
            " FROM T_GAM_PRODUCT t1, t_money_publisher_hold t2 " +
            " WHERE " +
            "  t1.oid = t2.productOid AND t1.type = 'PRODUCTTYPE_01' AND t1.productLabel = ?3 AND t1.raiseStartDate BETWEEN ?4 AND ?5 " +
            "  AND " +
            "  t1.isActivityProduct = 0 AND t1.guessOid IS NULL AND t2.investorOid = ?1", nativeQuery = true)
    BigDecimal getIncomeByUidAndRatioAndProductLabelAndTimeRange(String uid, BigDecimal ratio, String productLabel, String startTime, String endTime);

}
