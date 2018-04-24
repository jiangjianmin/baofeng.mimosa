package com.guohuai.mmp.investor.tradeorder;

import com.guohuai.ams.product.OnSaleT0ProductRep;
import com.guohuai.mmp.serialtask.RedeemInvestParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class BfPlusRedeemScheduleServiceExt {
    @Autowired
    private InvestorInvestTradeOrderService investorInvestTradeOrderService;
    @Autowired
    private InvestorInvestTradeOrderExtService investorInvestTradeOrderExtService;

    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public InvestorTradeOrderEntity createInvestorTradeOrderEntity(OnSaleT0ProductRep productRep, BfPlusRedeemScheduleService.PlusRedeemInvestReq req) {
        RedeemInvestTradeOrderReq riReq = new RedeemInvestTradeOrderReq();
        riReq.setInvestProductOid(productRep.getProductOid());
        riReq.setOrderAmount(req.getOrderVolume());
        riReq.setRedeemProductOid(req.getProductOid());
        // 快活宝下单
        InvestorTradeOrderEntity investOrder = investorInvestTradeOrderService.createNoPayInvestTradeOrder(riReq,
                req.getInvestorOid());
        investOrder.setRelateOid(req.getOrderOid());
        investorInvestTradeOrderService.invest(investOrder);
        return investOrder;
    }

    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public void redeemInvestDo(RedeemInvestParams params){
        investorInvestTradeOrderExtService.redeemInvestDo(params);
    }
}
