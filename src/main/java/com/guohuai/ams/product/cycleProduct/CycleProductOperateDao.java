package com.guohuai.ams.product.cycleProduct;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CycleProductOperateDao  extends JpaRepository<CycleProductOperateEntity, String> {

    @Query(value = "SELECT * FROM T_MONEY_CYCLE_PRODUCT_OPERATING_LIST WHERE investorOid IN (?1) AND status = ?2", nativeQuery = true)
    List<CycleProductOperateEntity> findByInvestorOidsAndStatus(List<String> investorOids, int status);
}
