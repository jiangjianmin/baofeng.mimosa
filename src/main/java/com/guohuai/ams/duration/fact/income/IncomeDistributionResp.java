package com.guohuai.ams.duration.fact.income;

import com.guohuai.ams.duration.assetPool.AssetPoolEntity;
import com.guohuai.ams.product.Product;
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
public class IncomeDistributionResp extends BaseResp {
	
	public IncomeDistributionResp(IncomeAllocate incomeAllocate) {
		this.oid = incomeAllocate.getOid();//收益分配表的oid
		AssetPoolEntity assetPool = incomeAllocate.getIncomeEvent().getAssetPool();
		if(assetPool!=null) {
			this.assetpoolOid = assetPool.getOid();
			this.assetPoolName = assetPool.getName();
		}
		Product product = incomeAllocate.getProduct();
		if(product!=null) {
			this.productOid = product.getOid();
			this.productName = product.getName();
		}
		
		this.baseDate = DateUtil.formatDate(incomeAllocate.getBaseDate().getTime()); // 基准日
		this.totalAllocateIncome = ProductDecimalFormat.format(incomeAllocate.getIncomeEvent().getAllocateIncome(),"0.##");//总分配收益
		this.capital = ProductDecimalFormat.format(incomeAllocate.getCapital(),"0.##");//产品总规模
		this.allocateIncome = ProductDecimalFormat.format(incomeAllocate.getAllocateIncome(),"0.##");//分配收益
		this.rewardIncome = ProductDecimalFormat.format(incomeAllocate.getRewardIncome(),"0.##");//奖励收益
		this.ratio = ProductDecimalFormat.format(ProductDecimalFormat.multiply(incomeAllocate.getRatio()),"0.##");//收益率
		this.successAllocateIncome = incomeAllocate.getSuccessAllocateIncome()!=null?ProductDecimalFormat.format(incomeAllocate.getSuccessAllocateIncome(),"0.##"):"";//成功分配收益	
		this.successAllocateRewardIncome = incomeAllocate.getSuccessAllocateRewardIncome()!=null?ProductDecimalFormat.format(incomeAllocate.getSuccessAllocateRewardIncome(),"0.##"):"";//成功分配奖励收益金额
		this.leftAllocateIncome = incomeAllocate.getLeftAllocateIncome()!=null?ProductDecimalFormat.format(incomeAllocate.getLeftAllocateIncome(),"0.##"):"";//剩余分配收益
		this.successAllocateInvestors = incomeAllocate.getSuccessAllocateInvestors()!=null?incomeAllocate.getSuccessAllocateInvestors().toString():"";//成功分配投资者数
		this.failAllocateInvestors = incomeAllocate.getFailAllocateInvestors()!=null?incomeAllocate.getFailAllocateInvestors().toString():"";//失败分配投资者数
		this.creator = incomeAllocate.getIncomeEvent().getCreator();// 申请人
		this.createTime = DateUtil.formatDatetime(incomeAllocate.getIncomeEvent().getCreateTime().getTime());  // 申请时间
		this.auditor = incomeAllocate.getIncomeEvent().getAuditor();  // 审批人
		this.auditTime = incomeAllocate.getIncomeEvent().getAuditTime()!=null?DateUtil.formatDatetime(incomeAllocate.getIncomeEvent().getAuditTime().getTime()):""; // 审批时间
		this.status = incomeAllocate.getIncomeEvent().getStatus();// 状态 (待审核: CREATE;通过: PASS;驳回: FAIL;已删除: DELETE)
		this.allocateIncomeType = incomeAllocate.getAllocateIncomeType();
	}
	
	private String oid;//收益分配表的oid
	private String assetpoolOid;
	private String assetPoolName;
	private String productOid;
	private String productName;
	private String baseDate; // 基准日
	private String totalAllocateIncome;//总分配收益
	private String capital;//产品总规模
	private String allocateIncome;//分配收益
	private String rewardIncome;//奖励收益
	private String ratio;//收益率
	private String creator;// 申请人
	private String createTime;  // 申请时间
	private String auditor;  // 审批人
	private String auditTime; // 审批时间
	private String status;// 状态 (待审核: CREATE;通过: PASS;驳回: FAIL;已删除: DELETE)
	private String successAllocateIncome;//成功分配收益	
	private String successAllocateRewardIncome;//成功分配奖励收益金额
	private String leftAllocateIncome;//剩余收益
	private String successAllocateInvestors;//成功分配投资者数
	private String failAllocateInvestors;//失败分配投资者数
	private String allocateIncomeType;
	

}
