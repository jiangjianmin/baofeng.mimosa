package com.guohuai.ams.productLabel;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.guohuai.ams.product.Product;

public interface ProductLabelDao extends JpaRepository<ProductLabel, String>, JpaSpecificationExecutor<ProductLabel> {
	
	List<ProductLabel> findByProduct(Product product);

}
