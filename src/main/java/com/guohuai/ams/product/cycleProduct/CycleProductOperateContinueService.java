package com.guohuai.ams.product.cycleProduct;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CycleProductOperateContinueService {

    @Autowired
    private CycleProductOperateContinueDao cycleProductOperateContinueDao;

    public List<CycleProductOperateContinueEntity> findSortedListByLastOid(String lastOid) {
        return cycleProductOperateContinueDao.findSortedListByLastOid(lastOid);
    }

    /**
     * 获取需续投处理总数
     *
     * @return
     */
    public long countAllNeedContinueInvestData() {
        return cycleProductOperateContinueDao.count();
    }

    public List<String> getAllNeedContinueInvestorOids() {
        return cycleProductOperateContinueDao.findAllInvestorOids();
    }

    public List<CycleProductOperateContinueEntity> findAllByInvestorOids(List<String> investorOids) {
        return cycleProductOperateContinueDao.findAll(investorOids);
    }

    /**
     * 初始化数据
     */
    public void initData() {
        int i = cycleProductOperateContinueDao.truncateTable();
        log.info("【循环产品续投】清空续投数据成功，共{}条", i);

        i = cycleProductOperateContinueDao.initData();
        log.info("【循环产品续投】初始化待续投数据成功，共{}条", i);
    }
}
