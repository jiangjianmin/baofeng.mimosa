package com.guohuai.mmp.investor.bankorder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface InvestorBankOrderDao extends JpaRepository<InvestorBankOrderEntity, String>, JpaSpecificationExecutor<InvestorBankOrderEntity> {
	
	/**
	 * 根据<<订单号>>查询<<投资人-银行委托单>>
	 * @param orderCode
	 * @return
	 */
	public InvestorBankOrderEntity findByOrderCodeAndOrderStatusAndOrderType(String orgCode, String orderStatus, String orderType);

	public InvestorBankOrderEntity findByOrderCode(String orderCode);
}
