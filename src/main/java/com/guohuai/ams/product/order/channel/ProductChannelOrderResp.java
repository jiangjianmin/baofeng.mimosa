package com.guohuai.ams.product.order.channel;

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
public class ProductChannelOrderResp extends BaseResp {
	
	public ProductChannelOrderResp(ProductChannelOrder pco) {
		this.oid = pco.getOid();
		if(pco.getProduct()!=null) {
			this.productOid = pco.getProduct().getOid();
			this.productName = pco.getProduct().getName();
		}
		if(pco.getChannel()!=null) {
			this.channelOid = pco.getChannel().getOid();
			this.channelName = pco.getChannel().getChannelName();
		}
		this.creator = pco.getCreator();
		this.createTime = DateUtil.formatDatetime(pco.getCreateTime().getTime());
		this.auditor = pco.getAuditor();
		if(pco.getAuditTime()!=null) {
			this.auditTime = DateUtil.formatDatetime(pco.getAuditTime().getTime());
		}
		this.status = pco.getStatus();
	}
	
	private String oid;
	private String productOid;
	private String productName;
	private String channelOid;
	private String channelName;
	private String creator;//申请人
	private String createTime;//申请时间
	private String auditor;//审批人
	private String auditTime;//审批时间	
	private String status;//状态

}
