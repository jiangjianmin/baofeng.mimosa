package com.guohuai.mmp.investor.tradeorder.p2p;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface P2PCreditorDao extends JpaRepository<P2PCreditorEntity, String> {

    @Query(value = " SELECT * FROM T_P2P_ORDER_CREDITOR WHERE userOid = ?1 AND orderOid = ?2 LIMIT ?3, ?4", nativeQuery = true)
    List<P2PCreditorEntity> findByUidAndOrderOid(String uid, String orderOid, int offset, int size);

    @Query(value = " SELECT count(1) FROM T_P2P_ORDER_CREDITOR WHERE userOid = ?1 AND orderOid = ?2 ", nativeQuery = true)
    int countByUidAndOrderOid(String uid, String orderOid);

}
