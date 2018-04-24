package com.guohuai.mmp.investor.tradeorder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface InvestorBfPlusRedeemDao extends JpaRepository<InvestorBfPlusRedeemEntity, String>, JpaSpecificationExecutor<InvestorBfPlusRedeemEntity> {

    @Query(value = "select * FROM T_MONEY_INVESTOR_PLUS_REDEEM where payDate = ?1 AND createDate > ?2 order by createDate DESC limit 2000", nativeQuery = true)
    List<InvestorBfPlusRedeemEntity> getBfPlusRedeemList(Date payDate, long createDate);

    @Query(value = "update T_MONEY_INVESTOR_PLUS_REDEEM r set status = ?1" +
            " where oid = ?2 AND status = 0", nativeQuery = true)
    @Modifying
    int updateBfPlusRedeem(int status, String oid);

    @Query(value = "SELECT * FROM T_MONEY_INVESTOR_PLUS_REDEEM where oid = ?1", nativeQuery = true)
    InvestorBfPlusRedeemEntity getByOid(String oid);
}
