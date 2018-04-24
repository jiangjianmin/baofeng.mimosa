package com.guohuai.mmp.platform.publisher.product.offset;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.ams.product.Product;
import com.guohuai.mmp.platform.publisher.offset.PublisherOffsetEntity;

@Service
@Transactional
public class ProductOffsetServiceRequiresNew {

	Logger logger = LoggerFactory.getLogger(ProductOffsetServiceRequiresNew.class);

	@Autowired
	ProductOffsetDao productOffsetDao;
	
	
	public ProductOffsetEntity createEntity(Product product, PublisherOffsetEntity pOffset) {
		ProductOffsetEntity offset = new ProductOffsetEntity();
		offset.setPublisherBaseAccount(product.getPublisherBaseAccount());
		offset.setProduct(product);
		offset.setPublisherOffset(pOffset);
		offset.setOffsetCode(pOffset.getOffsetCode());
		offset.setOffsetDate(pOffset.getOffsetDate());
		offset.setClearStatus(ProductOffsetEntity.OFFSET_clearStatus_toClear);
		offset.setConfirmStatus(ProductOffsetEntity.OFFSET_confirmStatus_toConfirm);
		offset.setCloseStatus(ProductOffsetEntity.OFFSET_closeStatus_toClose);
		return this.productOffsetDao.save(offset);
	}
}
