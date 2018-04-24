package com.guohuai.mmp.publisher.bankorder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PublisherBankOrderDao extends JpaRepository<PublisherBankOrderEntity, String>, JpaSpecificationExecutor<PublisherBankOrderEntity> {
	
	/**
	 * 根据<<订单号>>查询<<投资人-银行委托单>>
	 * @param orderCode
	 * @return
	 */
	public PublisherBankOrderEntity findByOrderCodeAndOrderStatusAndOrderType(String orgCode, String bankOrderOrderStatusSubmitted, String bankOrderOrderTypeDeposit);
	
	
	public PublisherBankOrderEntity findByOidAndOrderStatusAndOrderType(String oid, String bankOrderOrderStatusSubmitted, String bankOrderOrderTypeDeposit);
}
