package com.guohuai.ams.order;

import java.io.Serializable;
import org.hibernate.validator.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveSPVOrderForm implements Serializable {

	private static final long serialVersionUID = 786085874709658377L;
	
	@NotBlank
	private String assetPoolOid;//资产池
	@NotBlank
	private String orderType;//交易类型
	@NotBlank
	private String orderCate;//订单类型
	@NotBlank
	private String orderAmount;//订单金额
	@NotBlank
	private String orderDate;//申购日期

}
