package com.guohuai.mmp.investor.abandonlog;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity;

@Transactional
@Service
public class AbandonLogService {
	@Autowired
	AbandonLogDao abandonLogDao;
	
	public AbandonLogEntity create(InvestorTradeOrderEntity orderEntity, InvestorTradeOrderEntity refundOrderEntity){
		AbandonLogEntity abandonLog = new AbandonLogEntity();
		
		abandonLog.setOriginalOrderCode(orderEntity.getOrderCode());
		abandonLog.setRefundOrderCode(refundOrderEntity.getOrderCode());
		abandonLog.setRemark("abandon");
		
		return this.abandonLogDao.save(abandonLog);
	}
	
	public AbandonLogEntity create(String originalOrderCode, String reOrderCode){
		AbandonLogEntity abandonLog = new AbandonLogEntity();
		
		abandonLog.setOriginalOrderCode(originalOrderCode);
		abandonLog.setRefundOrderCode(reOrderCode);
		abandonLog.setRemark("resubmit");
		
		return this.abandonLogDao.save(abandonLog);
	}
	
	public String getOriginalOrderCodeByRefundOrderCode(String refundOrderCode) {
		return this.abandonLogDao.findByRefundOrderCode(refundOrderCode).getOriginalOrderCode();
	}
	
	public AbandonLogEntity create(InvestorTradeOrderEntity orderEntity){
		AbandonLogEntity abandonLog = new AbandonLogEntity();
		
		abandonLog.setOriginalOrderCode(orderEntity.getOrderCode());
		abandonLog.setRemark("abandon");
		
		return this.abandonLogDao.save(abandonLog);
	}
}
