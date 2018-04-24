package com.guohuai.mmp.investor.tradeorder;

import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.guohuai.ams.product.Product;
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
public class AllocateIncomeAuditResp extends BaseResp {
	
	public AllocateIncomeAuditResp(AllocateInterestAudit pco) {
		this.oid = pco.getOid();
		if(pco.getProduct()!=null) {
			this.productOid = pco.getProduct().getOid();
			this.productName = pco.getProduct().getName();
		}
		this.totalVolume = pco.getTotalVolume();
		this.totalRepay = pco.getTotalRepay();
		this.buyAmount = pco.getBuyAmount();
		this.allocateIncomeAmount = pco.getAllocateIncomeAmount();
		this.ratio = pco.getRatio();
		this.applicant = pco.getApplicant();
		this.applyTime = DateUtil.formatDatetime(pco.getApplyTime().getTime());
		this.auditStatus = pco.getAuditStatus();
		if (pco.getAuditor()!=null) {
			this.auditor = pco.getAuditor();
			this.auditTime = DateUtil.formatDatetime(pco.getAuditTime().getTime());
			this.auditComment = pco.getAuditComment();
		}
		this.updateTime = DateUtil.formatDatetime(pco.getUpdateTime().getTime());
	}
	
	private String oid;
	private String productOid;
	private String productName;
	private BigDecimal totalVolume; // 产品总规模
	private BigDecimal totalRepay; // 本金利息总额（元）
	private BigDecimal buyAmount; // 用户申购总额（元）
	private BigDecimal allocateIncomeAmount; // 分配收益总额（元）
	private BigDecimal ratio; // 年化收益率
	private String applicant; // 申请人
	private String applyTime; // 申请时间
	private String auditStatus; // 待审核:toAudit 已通过:auditPass 已驳回:auditReject 已撤回:withdrawed
	private String auditor; // 审核人
	private String auditTime; // 审核时间
	private String auditComment; // 审核理由
	private String updateTime; // 更新时间

	
	
}
