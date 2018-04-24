package com.guohuai.ams.product;

import java.math.BigDecimal;
import com.guohuai.component.web.view.BaseResp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class ProductDurationResp extends BaseResp {
	
	public ProductDurationResp() {
		this.oid = "";
		this.currentVolume = BigDecimal.ZERO;//持有人总份额
		this.realNetting = BigDecimal.ZERO;//产品实时轧差结果
		this.shares = BigDecimal.ZERO;//SPV基子份额	
		this.marketValue = BigDecimal.ZERO;//SPV基子市值	
		this.payFee = BigDecimal.ZERO;//SPV应收费金
		this.prepaidFee = BigDecimal.ZERO;//SPV预付费金
		this.countintChargefee = BigDecimal.ZERO;//SPV累计已计提费金
		this.drawedChargefee = BigDecimal.ZERO;//SPV累计已提取费金
	}
	
	private String oid;
	private BigDecimal currentVolume;//持有人总份额
	private BigDecimal realNetting;//产品实时轧差结果
	private BigDecimal shares;//SPV基子份额	
	private BigDecimal marketValue;//SPV基子市值	
	private BigDecimal payFee;//SPV应收费金
	private BigDecimal prepaidFee;//SPV预付费金
	private BigDecimal countintChargefee;//SPV累计已计提费金
	private BigDecimal drawedChargefee;//SPV累计已提取费金
	private BigDecimal hqla; // 流动性资产

}
