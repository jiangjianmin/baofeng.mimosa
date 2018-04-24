package com.guohuai.mmp.investor.tradeorder;

import com.guohuai.ams.product.OnSaleT0ProductRep;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.mmp.job.lock.JobLockEntity;
import com.guohuai.mmp.job.lock.JobLockService;
import com.guohuai.mmp.job.log.JobLogEntity;
import com.guohuai.mmp.job.log.JobLogFactory;
import com.guohuai.mmp.job.log.JobLogService;
import com.guohuai.mmp.serialtask.RedeemInvestParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

@Service
public class InvestorBfPlusRedeemService {
    private Logger logger = LoggerFactory.getLogger(InvestorBfPlusRedeemService.class);

    @Autowired
    private InvestorBfPlusRedeemDao investorBfPlusRedeemDao;

    public List<InvestorBfPlusRedeemEntity> getBfPlusRedeemList(Date payDate, long createDate){
        return investorBfPlusRedeemDao.getBfPlusRedeemList(payDate, createDate);
    }

    public void save(InvestorBfPlusRedeemEntity entity) {
        investorBfPlusRedeemDao.save(entity);
    }

    public int updateBfPlusRedeem(int status, String oid){
        int result = investorBfPlusRedeemDao.updateBfPlusRedeem(status, oid);
        if(result < 1){
            throw new AMPException(-1, "======updateBfPlusRedeem failure!======");
        }
        return result;
    }

    public InvestorBfPlusRedeemEntity getByOid(String oid){
        return investorBfPlusRedeemDao.getByOid(oid);
    }
}
