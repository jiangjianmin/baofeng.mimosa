package com.guohuai.ams.product.order.operating;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductOperatingDao
		extends JpaRepository<ProductOperatingOrder, String>, JpaSpecificationExecutor<ProductOperatingOrder> {

}
