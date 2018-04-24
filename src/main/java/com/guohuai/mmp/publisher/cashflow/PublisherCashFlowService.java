package com.guohuai.mmp.publisher.cashflow;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.component.util.DateUtil;
import com.guohuai.mmp.platform.publisher.order.PublisherOrderEntity;
import com.guohuai.mmp.publisher.bankorder.PublisherBankOrderEntity;

@Service
@Transactional
public class PublisherCashFlowService {
	
	
	@Autowired
	PublisherCashFlowDao publisherCashFlowDao;
	
	
	
	public PublisherCashFlowEntity saveEntity(PublisherCashFlowEntity cashFlow) {
		cashFlow.setCreateTime(DateUtil.getSqlCurrentDate());
		return this.updateEntity(cashFlow);
	}



	public PublisherCashFlowEntity updateEntity(PublisherCashFlowEntity cashFlow) {
		cashFlow.setUpdateTime(DateUtil.getSqlCurrentDate());
		return publisherCashFlowDao.save(cashFlow);
	}



	public PublisherCashFlowEntity createCashFlow(PublisherBankOrderEntity bankOrder) {
		PublisherCashFlowEntity cashFlow = new PublisherCashFlowEntity();
		cashFlow.setPublisherBaseAccount(bankOrder.getPublisherBaseAccount());
		cashFlow.setPublisherBankOrder(bankOrder);
		cashFlow.setTradeAmount(bankOrder.getOrderAmount());
		if (PublisherBankOrderEntity.BANK_ORDER_ORDER_TYPE_deposit.equals(bankOrder.getOrderType())) {
			cashFlow.setTradeType(PublisherCashFlowEntity.FLOW_tradeType_deposit);
		}
		if (PublisherBankOrderEntity.BANK_ORDER_ORDER_TYPE_withdraw.equals(bankOrder.getOrderType())) {
			cashFlow.setTradeType(PublisherCashFlowEntity.FLOW_tradeType_withdraw);
		}
		if (PublisherBankOrderEntity.BANK_ORDER_FEE_PAYER_user.equals(bankOrder.getFeePayer())) {
			this.createCashFlowWithTradeType(bankOrder, PublisherCashFlowEntity.FLOW_tradeType_fee);
		}
		return this.saveEntity(cashFlow);
	}



	private PublisherCashFlowEntity createCashFlowWithTradeType(PublisherBankOrderEntity bankOrder, String tradeType) {
		PublisherCashFlowEntity cashFlow = new PublisherCashFlowEntity();
		cashFlow.setPublisherBaseAccount(bankOrder.getPublisherBaseAccount());
		cashFlow.setPublisherBankOrder(bankOrder);
		cashFlow.setTradeAmount(bankOrder.getOrderAmount());
		cashFlow.setTradeType(tradeType);
		return this.saveEntity(cashFlow);
		
	}



	public PublisherCashFlowEntity createCashFlow(PublisherOrderEntity publisherOrder) {
		PublisherCashFlowEntity cashFlow = new PublisherCashFlowEntity();
		cashFlow.setPublisherBaseAccount(publisherOrder.getPublisher());
		cashFlow.setPublisherOrder(publisherOrder);
		cashFlow.setTradeAmount(publisherOrder.getOrderAmount());
		if (PublisherOrderEntity.ORDER_orderType_return.equals(publisherOrder.getOrderType())) {
			cashFlow.setTradeType(PublisherCashFlowEntity.FLOW_tradeType_return);
		}
		if (PublisherOrderEntity.ORDER_orderType_borrow.equals(publisherOrder.getOrderType())) {
			cashFlow.setTradeType(PublisherCashFlowEntity.FLOW_tradeType_borrow);
		}
		return this.saveEntity(cashFlow);
		
	}

}
