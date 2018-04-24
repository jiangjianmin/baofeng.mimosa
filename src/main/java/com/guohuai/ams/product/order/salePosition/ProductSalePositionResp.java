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
public class ProductSalePositionResp extends BaseResp {
	
	public ProductSalePositionResp(ProductSalePositionOrder pspo) {
		this.oid = pspo.getOid();
		if(pspo.getProduct()!=null) {
			this.productOid = pspo.getProduct().getOid();
			this.productName = pspo.getProduct().getName();
		}
		if(pspo.getProductSaleScheduling()!=null) {
			this.baseDate = pspo.getProductSaleScheduling().getBasicDate() != null ? DateUtil.formatDate(pspo.getProductSaleScheduling().getBasicDate().getTime()) : "";//排期
		}
		this.volume = ProductDecimalFormat.format(pspo.getVolume(), "0.##");
		this.creator = pspo.getCreator();
		this.createTime = DateUtil.formatDatetime(pspo.getCreateTime().getTime());
		this.auditor = pspo.getAuditor();
		if(pspo.getAuditTime()!=null) {
			this.auditTime = DateUtil.formatDatetime(pspo.getAuditTime().getTime());
		}
		this.status = pspo.getStatus();
	}
	
	private String oid;
	private String productOid;
	private String productName;
	private String baseDate;
	private String volume;//申请份额
	private String creator;//申请人
	private String createTime;//申请时间
	private String auditor;//审批人
	private String auditTime;//审批时间	
	private String status;//状态

}
