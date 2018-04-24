package com.guohuai.ams.product.order.channel;

import java.io.Serializable;
import org.hibernate.validator.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveProductChannelForm implements Serializable {

	private static final long serialVersionUID = -5939079508868869840L;
	
	@NotBlank
	private String productOid;//产品oid
	@NotBlank
	private String channelOid;//申请渠道oid

}
