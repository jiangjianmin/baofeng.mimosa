package com.guohuai.mmp.investor.tradeorder.p2p;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class P2PCreditorService {

    @Autowired
    private P2PCreditorDao p2PCreditorDao;


    public List<P2PCreditorEntity> findCreditorDetailByUidAndOrderOid(String uid, String orderOid, int page, int size) {
        return p2PCreditorDao.findByUidAndOrderOid(uid, orderOid, (page - 1) * size, size);
    }

    public int countCreditorDetailByUidAndOrderOid(String uid, String orderOid) {
        return p2PCreditorDao.countByUidAndOrderOid(uid, orderOid);
    }
}
