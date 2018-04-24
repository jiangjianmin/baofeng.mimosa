package com.guohuai.ams.order;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditSPVOrderForm implements Serializable {

	private static final long serialVersionUID = -3379348877561345347L;

	@NotBlank
	private String oid;//订单oid
	@NotBlank
	private String avaibleAmount;//可售余额
	@NotBlank
	private String payFee;//应付费金
	
	
}
