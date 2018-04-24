package com.guohuai.mmp.investor.tradeorder;

import com.guohuai.ams.product.OnSaleT0ProductRep;
import com.guohuai.ams.product.ProductService;
import com.guohuai.cardvo.dao.MimosaDao;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class BfPlusRedeemScheduleService {
    private Logger logger = LoggerFactory.getLogger(BfPlusRedeemScheduleService.class);

    @Autowired
    private JobLockService jobLockService;
    @Autowired
    private JobLogService jobLogService;
    @Autowired
    private ProductService productService;
    @Autowired
    private BfPlusRedeemScheduleServiceExt bfPlusRedeemScheduleServiceExt;

    @Autowired
    private MimosaDao mimosaDao;

    public void bfPlusRedeem(){
        if (this.jobLockService.getRunPrivilege(JobLockEntity.JOB_jobId_bfPlusRedeem)) {
            JobLogEntity jobLog = JobLogFactory.getInstance(JobLockEntity.JOB_jobId_bfPlusRedeem);
            try {
                bfPlusRedeemInvestDo();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                jobLog.setJobMessage(AMPException.getStacktrace(e));
                jobLog.setJobStatus(JobLogEntity.JOB_jobStatus_failed);
            }
            jobLog.setBatchEndTime(DateUtil.getSqlCurrentDate());
            this.jobLogService.saveEntity(jobLog);
            this.jobLockService.resetJob(JobLockEntity.JOB_jobId_bfPlusRedeem);
        }
    }

    /**
     *  plus赎回定时任务调用
     */
    public void bfPlusRedeemInvestDo(){
        OnSaleT0ProductRep productRep = productService.getOnSaleProductOid();
        if(productRep == null) {
            throw new AMPException("暂无可售的活期产品");
        }

        while (true){
            //可赎回订单列表
            List<Map<String, Object>> redeemOrderList = mimosaDao.getBfPlusRedeemList(DateUtil.getCurrDate());
            if(null == redeemOrderList || redeemOrderList.size() <= 0){
                break;
            }
            for(Map<String, Object> redeemOrderEntity : redeemOrderList){
                String orderCode = redeemOrderEntity.get("orderCode").toString();
                try{
                    PlusRedeemInvestReq req = new PlusRedeemInvestReq();
                    req.setInvestorOid(redeemOrderEntity.get("investorOid").toString());

                    req.setOrderCode(orderCode);
                    req.setOrderOid(redeemOrderEntity.get("oid").toString());
                    BigDecimal orderAmount = new BigDecimal(redeemOrderEntity.get("orderAmount").toString());
                    req.setOrderVolume(orderAmount);
                    req.setProductOid(redeemOrderEntity.get("productOid").toString());
                    bfPlusRedeemInvest(productRep, req);
                }catch (Exception e){
                    logger.error("bfPlusRedeemInvestDo error! redeem order code {}", orderCode);
                    logger.error("bfPlusRedeemInvestDo error!", e);
                }
            }
        }
    }

    public void bfPlusRedeemInvest(OnSaleT0ProductRep productRep, PlusRedeemInvestReq req) {
        InvestorTradeOrderEntity investOrder = bfPlusRedeemScheduleServiceExt.createInvestorTradeOrderEntity(productRep, req);

        RedeemInvestParams params = new RedeemInvestParams();
        params.setInvestOrderCode(investOrder.getOrderCode());
        params.setRedeemOrderCode(req.getOrderCode());

        bfPlusRedeemScheduleServiceExt.redeemInvestDo(params);
    }

    class PlusRedeemInvestReq{
        private BigDecimal orderVolume;
        private String productOid;
        private String investorOid;
        private String orderOid;
        private String orderCode;

        public BigDecimal getOrderVolume() {
            return orderVolume;
        }

        public void setOrderVolume(BigDecimal orderVolume) {
            this.orderVolume = orderVolume;
        }

        public String getProductOid() {
            return productOid;
        }

        public void setProductOid(String productOid) {
            this.productOid = productOid;
        }

        public String getInvestorOid() {
            return investorOid;
        }

        public void setInvestorOid(String investorOid) {
            this.investorOid = investorOid;
        }

        public String getOrderOid() {
            return orderOid;
        }

        public void setOrderOid(String orderOid) {
            this.orderOid = orderOid;
        }

        public String getOrderCode() {
            return orderCode;
        }

        public void setOrderCode(String orderCode) {
            this.orderCode = orderCode;
        }
    }
}
