package com.guohuai.ams.product.order.salePosition;

import com.guohuai.ams.product.ProductDecimalFormat;
import com.guohuai.component.util.DateUtil;
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
public class ProductSaleApplyScheduleResp extends BaseResp {
	
	public ProductSaleApplyScheduleResp(ProductSaleScheduling schedule) {
		this.oid = schedule.getOid();
		this.baseDate = schedule.getBasicDate() != null ? DateUtil.formatDate(schedule.getBasicDate().getTime()) : "";//排期
		this.auditAmount = ProductDecimalFormat.format(schedule.getAuditAmount(), "0.##");//审批份额	
		this.applyAmount = ProductDecimalFormat.format(schedule.getApplyAmount(), "0.##");//申请份额
		this.approvalAmount = ProductDecimalFormat.format(schedule.getApprovalAmount(), "0.##");//生效份额
		this.syncTime = schedule.getSyncTime() != null ? DateUtil.formatDatetime(schedule.getSyncTime().getTime()) : null;//同步时间
		this.createTime = schedule.getCreateTime() != null ? DateUtil.formatDatetime(schedule.getCreateTime().getTime()) : "";
		this.updateTime = schedule.getUpdateTime() != null ? DateUtil.formatDatetime(schedule.getUpdateTime().getTime()) : "";
	}
	
	private String oid;
	private String baseDate;//排期
	private String auditAmount;//审批份额	
	private String applyAmount;//申请份额
	private String approvalAmount;//生效份额
	private String syncTime;//同步时间
	private String createTime;
	private String updateTime;

}
