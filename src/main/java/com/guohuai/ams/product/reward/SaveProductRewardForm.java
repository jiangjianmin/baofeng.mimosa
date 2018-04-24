package com.guohuai.ams.product.reward;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveProductRewardForm implements Serializable {

	private static final long serialVersionUID = -5996888164064413780L;

	@NotBlank
	private String productOid;
	@NotBlank
	private String reward;

}