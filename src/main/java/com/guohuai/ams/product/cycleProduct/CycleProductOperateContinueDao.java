package com.guohuai.ams.product.cycleProduct;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CycleProductOperateContinueDao extends JpaRepository<CycleProductOperateContinueEntity, String> {

    @Query(value = "SELECT * FROM T_MONEY_CYCLE_PRODUCT_CONTINUE_LIST WHERE investorOid > ?1 ORDER BY investorOid LIMIT 200", nativeQuery = true)
    public List<CycleProductOperateContinueEntity> findSortedListByLastOid(String lastOid);

    @Query(value = "SELECT investorOid FROM T_MONEY_CYCLE_PRODUCT_CONTINUE_LIST", nativeQuery = true)
    public List<String> findAllInvestorOids();

    @Query(value = "INSERT INTO t_money_cycle_product_continue_list (investorOid, operateDate, orderAmount) " +
            "  SELECT " +
            "    investorOid, " +
            "    operateDate, " +
            "    sum(orderAmount) " +
            "  FROM " +
            "    t_money_cycle_product_operating_list " +
            "  WHERE status = 0 " +
            "  GROUP BY investorOid ", nativeQuery = true)
    @Modifying
    int initData();

    @Query(value = "delete from t_money_cycle_product_continue_list", nativeQuery = true)
    @Modifying
    int truncateTable();

}
