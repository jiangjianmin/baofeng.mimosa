package com.guohuai.ams.product.order.salePosition;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductSalePositionDao
		extends JpaRepository<ProductSalePositionOrder, String>, JpaSpecificationExecutor<ProductSalePositionOrder> {

}
