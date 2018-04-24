package com.guohuai.mmp.investor.tradeorder.check;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.component.exception.AMPException;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderDao;
import com.guohuai.mmp.platform.finance.modifyorder.ModifyOrderNewService;

@Service
@Transactional
public class InvestorRefundTradeOrderRequireNewService {
	Logger logger = LoggerFactory.getLogger(InvestorRefundTradeOrderRequireNewService.class);


	@Autowired
	private InvestorTradeOrderDao investorTradeOrderDao;
	@Autowired
	private ModifyOrderNewService modifyOrderNewService;
	
	public void refundOrder(String orderCode) {
		int i = investorTradeOrderDao.refundOrder(orderCode);
		if (i < 1) {
			//error.define[30071]=退款失败(CODE:30071)
			throw new AMPException(30071);
		}
		this.modifyOrderNewService.updateDealStatusDealingByOrderCode(orderCode);
	}

	

}
