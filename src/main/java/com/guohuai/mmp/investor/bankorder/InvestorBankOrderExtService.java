package com.guohuai.mmp.investor.bankorder;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class InvestorBankOrderExtService {

	Logger logger = LoggerFactory.getLogger(InvestorBankOrderExtService.class);
	
	@Autowired
	private InvestorWithdrawBankOrderService investorWithdrawBankOrderService;
	@Autowired
	private InvestorDepositBankOrderService investorDepositBankOrderService;
	@Autowired
	private InvestorBankOrderService investorBankOrderService;
	
	public BankOrderRep withdraw(BankOrderReq bankOrderReq) {
		
		InvestorBankOrderEntity bankOrder = investorBankOrderService.createWithdrawBankOrder(bankOrderReq);
		BankOrderRep rep = investorWithdrawBankOrderService.withdraw(bankOrderReq, bankOrder.getOrderCode());
		
		return rep;
		
	}
	
	
	@Transactional
	public BankOrderRep deposit(BankOrderReq bankOrderReq) {
		InvestorBankOrderEntity bankOrder = investorBankOrderService.createDepostBankOrder(bankOrderReq);
		BankOrderRep rep = investorDepositBankOrderService.deposit(bankOrderReq, bankOrder.getOrderCode());
		return rep;
		
	}
	
}
