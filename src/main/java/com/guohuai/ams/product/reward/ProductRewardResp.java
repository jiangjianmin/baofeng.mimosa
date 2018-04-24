package com.guohuai.ams.product.reward;

import com.guohuai.ams.product.ProductDecimalFormat;
import com.guohuai.component.web.view.BaseResp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class ProductRewardResp extends BaseResp {

	public ProductRewardResp(ProductIncomeReward pir) {
		this.oid = pir.getOid();// 序号
		this.productOid = pir.getProduct().getOid();// 产品oid
		this.productName = pir.getProduct().getName();// 产品名称
		this.productFullName = pir.getProduct().getFullName();// 产品名称
		this.level = pir.getLevel();
		this.startDate = pir.getStartDate();// 起始天数
		this.endDate = pir.getEndDate();// 截止天数
		this.ratio = ProductDecimalFormat.format(ProductDecimalFormat.multiply(pir.getRatio()));// 奖励收益率
	}

	private String oid;// 序号
	private String productOid;// 产品oid
	private String productName;// 产品名称
	private String productFullName;// 产品名称
	private String level;//阶梯名称
	private Integer startDate;// 起始天数
	private Integer endDate;// 截止天数
	private String ratio;// 奖励收益率

}
