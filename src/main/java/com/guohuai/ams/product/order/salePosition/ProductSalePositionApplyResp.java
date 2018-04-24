package com.guohuai.ams.product.order.salePosition;

import java.math.BigDecimal;
import java.util.List;

import com.guohuai.ams.product.Product;
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
public class ProductSalePositionApplyResp extends BaseResp {

	public ProductSalePositionApplyResp(Product p, BigDecimal applyAmount, BigDecimal approvalAmount, BigDecimal holdTotalVolume) {
		this.productOid = p.getOid();
		this.productCode = p.getCode();
		this.productName = p.getName();
		if (p.getAssetPool() != null) {
			this.assetPoolName = p.getAssetPool().getName();
		}
		this.collectedVolume = p.getCollectedVolume();
		this.netUnitShare = ProductDecimalFormat.format(p.getNetUnitShare(), "0.####");// 单位份额净值
		this.raisedTotalNumber = p.getRaisedTotalNumber() != null ? p.getRaisedTotalNumber() : BigDecimal.ZERO;// 产品募集总份额
		this.maxSaleVolume = p.getMaxSaleVolume() != null ? p.getMaxSaleVolume() : BigDecimal.ZERO;//产品可售份额
		this.currentVolume = p.getCurrentVolume() != null ? p.getCurrentVolume() : BigDecimal.ZERO;//产品 当前份额
		this.lockCollectedVolume = p.getLockCollectedVolume();// 产品锁定已募份额
		this.applyAmount = applyAmount; // 申请中份额（申请过的未开始生效份额）
		this.approvalAmount = approvalAmount; // 申请过的已经生效份额
		this.applyedAmount = applyAmount.add(approvalAmount); // 已申请份额（已经申请过的有效的申请份额总和=申请过的已经生效份额+申请过的未开始生效份额）
		this.holdTotalVolume = holdTotalVolume;
		this.availableMaxSaleVolume = holdTotalVolume.subtract(this.maxSaleVolume).subtract(this.applyAmount);// 剩余可申请份额
		this.leftSaleVolume = this.maxSaleVolume.subtract(this.lockCollectedVolume);
		this.netUnitShareStr = !"".equals(this.netUnitShare) ? this.netUnitShare + "元" : "";
		this.raisedTotalNumberStr = ProductDecimalFormat.format(this.raisedTotalNumber, "0.####") + "份";// 募集总份额
		this.maxSaleVolumeStr = ProductDecimalFormat.format(this.maxSaleVolume, "0.####") + "份";//产品可售份额
		this.currentVolumeStr = ProductDecimalFormat.format(this.currentVolume, "0.####") + "份";// 当前份额
		this.lockCollectedVolumeStr = ProductDecimalFormat.format(this.lockCollectedVolume, "0.####") + "份";// 锁定已募份额
		this.applyedAmountStr = ProductDecimalFormat.format(this.applyedAmount, "0.####") + "份";// 已申请份额
		this.applyAmountStr = ProductDecimalFormat.format(this.applyAmount, "0.####") + "份";// 申请过的未开始生效份额
		this.approvalAmountStr = ProductDecimalFormat.format(this.approvalAmount, "0.####") + "份";// 申请过的已经生效份额
		this.availableMaxSaleVolumeStr = ProductDecimalFormat.format(this.availableMaxSaleVolume, "0.####") + "份";
		this.holdTotalVolumeStr = ProductDecimalFormat.format(this.holdTotalVolume, "0.####") + "份";
		this.leftSaleVolumeStr = ProductDecimalFormat.format(this.leftSaleVolume, "0.####") + "份";
		this.collectedVolumeStr = ProductDecimalFormat.format(this.collectedVolume, "0.####") + "份";
	}

	private String productOid;// 产品oid
	private String productCode;// 产品编码
	private String productName;// 产品名称
	private String assetPoolName;// 资产池名称
	private String spvName;// SPV
	
	private String netUnitShare;// 单位份额净值
	private BigDecimal raisedTotalNumber;// 产品总份额
	private BigDecimal maxSaleVolume;// 产品可售份额
	private BigDecimal currentVolume;// 已售份额
	private BigDecimal lockCollectedVolume;// 锁定份额
	private BigDecimal applyedAmount;// 已申请份额 :产品已申请份额总和=申请过的已经生效份额+申请过的未开始生效份额
	private BigDecimal applyAmount;//申请过的未开始生效份额
	private BigDecimal approvalAmount;//申请过的已经生效份额
	private BigDecimal holdTotalVolume;//发行人持有份额
	private BigDecimal leftSaleVolume;//剩余可售份额
	private BigDecimal availableMaxSaleVolume;// 剩余可申请份额
	private BigDecimal collectedVolume;
	
	private String netUnitShareStr;// 单位份额净值
	private String raisedTotalNumberStr;// 产品总份额
	private String maxSaleVolumeStr;// 产品可售份额
	private String currentVolumeStr;// 已售份额
	private String lockCollectedVolumeStr;// 锁定份额
	private String applyedAmountStr;// 已申请份额
	
	private String applyAmountStr;//申请过的未开始生效份额
	private String approvalAmountStr;//申请过的已经生效份额
	private String holdTotalVolumeStr;//发行人持有份额
	private String leftSaleVolumeStr;//剩余可售份额
	private String availableMaxSaleVolumeStr;// 剩余可申请份额
	private String collectedVolumeStr;

	private List<ProductSaleApplyScheduleResp> schedules;

}
