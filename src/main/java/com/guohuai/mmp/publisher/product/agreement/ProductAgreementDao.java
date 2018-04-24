package com.guohuai.mmp.publisher.product.agreement;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity;

public interface ProductAgreementDao extends JpaRepository<ProductAgreementEntity, String>, JpaSpecificationExecutor<ProductAgreementEntity> {


	

	

	ProductAgreementEntity findByInvestorTradeOrderAndAgreementType(InvestorTradeOrderEntity order, String agreementType);
	

	
	
	@Query(value="from ProductAgreementEntity where orderOid=?1")
	List<ProductAgreementEntity> findByOrderOid(String orderOid);



	ProductAgreementEntity findByAgreementCodeAndAgreementType(String code, String type);

}
