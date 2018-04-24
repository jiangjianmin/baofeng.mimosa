package com.guohuai.mmp.platform.publisher.offset;

import com.guohuai.ams.product.Product;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountEntity;




public class NewOffset {
	PublisherBaseAccountEntity spv;
	Product product;
	String offsetCode;
	
	
	
	
	public PublisherBaseAccountEntity getSpv() {
		return spv;
	}
	public void setSpv(PublisherBaseAccountEntity spv) {
		this.spv = spv;
	}




	public Product getProduct() {
		return product;
	}




	public void setProduct(Product product) {
		this.product = product;
	}




	public String getOffsetCode() {
		return offsetCode;
	}




	public void setOffsetCode(String offsetCode) {
		this.offsetCode = offsetCode;
	}

	
	
	@Override
	public int hashCode() {
		return this.offsetCode.hashCode() + this.product.getOid().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (null == obj) {
			return false;
		}
		if (!(obj instanceof NewOffset)) {
			return false;
		}
		NewOffset offset = (NewOffset)obj;
		if (this.offsetCode.equals(offset.getOffsetCode()) && this.product.equals(offset.getProduct())) {
			return true;
		}
		return false;
	}
}
