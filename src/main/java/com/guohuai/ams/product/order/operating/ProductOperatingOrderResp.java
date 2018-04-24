package com.guohuai.ams.product.order.operating;

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
public class ProductOperatingOrderResp extends BaseResp {
	
	public ProductOperatingOrderResp(ProductOperatingOrder poo) {
		this.oid = poo.getOid();
		if(poo.getProduct()!=null) {
			this.productOid = poo.getProduct().getOid();
			this.productName = poo.getProduct().getName();
		}
		this.type = poo.getType();
		this.creator = poo.getCreator();
		this.createTime = DateUtil.formatDatetime(poo.getCreateTime().getTime());
		this.auditor = poo.getAuditor();
		if(poo.getAuditTime()!=null) {
			this.auditTime = DateUtil.formatDatetime(poo.getAuditTime().getTime());
		}
		this.status = poo.getStatus();
	}
	
	private String oid;
	private String productOid;
	private String productName;
	private String type;
	private String creator;
	private String createTime;
	private String auditor;
	private String auditTime;
	private String status;

}
