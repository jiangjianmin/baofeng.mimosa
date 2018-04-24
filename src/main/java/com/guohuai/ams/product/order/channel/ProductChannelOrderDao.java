package com.guohuai.ams.product.order.channel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductChannelOrderDao
		extends JpaRepository<ProductChannelOrder, String>, JpaSpecificationExecutor<ProductChannelOrder> {

}
