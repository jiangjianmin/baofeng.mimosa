package com.guohuai.ams.product.order.salePosition;

import java.io.Serializable;
import org.hibernate.validator.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveProductSalePositionForm implements Serializable {

	private static final long serialVersionUID = -8416826131481036566L;
	
	@NotBlank
	private String productOid;//产品oid
	@NotBlank
	private String basicDate;//申请销售份额排期
	@NotBlank
	private String newMaxSaleVolume;//申请销售份额

}
