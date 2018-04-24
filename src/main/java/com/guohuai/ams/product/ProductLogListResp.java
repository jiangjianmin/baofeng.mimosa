package com.guohuai.ams.product;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

import com.guohuai.ams.dict.Dict;
import com.guohuai.ams.duration.assetPool.AssetPoolEntity;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.web.view.BaseResp;
import com.guohuai.file.FileResp;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ProductLogListResp extends BaseResp {

	public  ProductLogListResp(Product p) {
		this.oid = p.getOid();
		this.code = p.getCode();
		this.name = p.getName();
		this.fullName = p.getFullName();
		this.administrator = p.getAdministrator();// 管理人
		Optional<Dict> p_type=Optional.ofNullable(p.getType());
		this.typeOid =p_type.map(Dict::getOid).orElse("");
		this.typeName =p_type.map(Dict::getName).orElse("");
		Optional<AssetPoolEntity> p_AssetPool=Optional.ofNullable(p.getAssetPool());
		Optional<PublisherBaseAccountEntity> p_SpvEntity=Optional.empty();
		try {
			 p_SpvEntity=p_AssetPool.map(AssetPoolEntity::getSpvEntity);
		} catch (Exception e) {
			System.out.println("poid="+p.getOid());
			e.printStackTrace();
			p_SpvEntity=Optional.empty();
		}
		this.assetPoolOid = p_AssetPool.map(AssetPoolEntity::getOid).orElse("");
		this.assetPoolName = p_AssetPool.map(AssetPoolEntity::getName).orElse("");
		this.spvOid =p_SpvEntity.map(PublisherBaseAccountEntity::getOid).orElse("");
		this.corporateId=p_SpvEntity.map(PublisherBaseAccountEntity::getCorperateOid).orElse("");
//		if (p.getAssetPool().getSpvEntity() != null) {
//			this.spvOid = p.getAssetPool().getSpvEntity().getOid();
//		}
//		if (null != p.getType()) {
//			this.typeOid = p.getType().getOid();// 产品类型
//			this.typeName = p.getType().getName();
//		}
//		if (p.getAssetPool() != null) {
//			this.assetPoolOid = p.getAssetPool().getOid();
//			this.assetPoolName = p.getAssetPool().getName();
//			if (p.getAssetPool().getSpvEntity() != null) {
//				this.spvOid = p.getAssetPool().getSpvEntity().getOid();
//			}
//		}
		this.raiseStartDate = p.getRaiseStartDate() != null ? DateUtil.formatDate(p.getRaiseStartDate().getTime()) : "";// 募集开始时间
		this.raisePeriod = p.getRaisePeriodDays();// 募集期:()个自然日
		this.interestsFirstDate = p.getInterestsFirstDays();// 起息日:募集满额后()个自然日
		this.durationPeriod = p.getDurationPeriodDays();// 存续期:()个自然日
		this.expAror = ProductDecimalFormat.format(ProductDecimalFormat.multiply(p.getExpAror()), "0.##");// 预期年化收益率
		this.expArorSec = ProductDecimalFormat.format(ProductDecimalFormat.multiply(p.getExpArorSec()), "0.##");// 预期年化收益率区间
		this.raisedTotalNumber = p.getRaisedTotalNumber();// 募集总份额
		this.netUnitShare = ProductDecimalFormat.format(p.getNetUnitShare(), "0.####");// 单位份额净值
		this.investMin = p.getInvestMin();// 单笔投资最低份额
		this.investMax = p.getInvestMax();// 单笔投资最高份额
		this.investAdditional = p.getInvestAdditional();// 单笔投资追加份额
		this.netMaxRredeemDay = p.getNetMaxRredeemDay();// 单日净赎回上限
		this.minRredeem = p.getMinRredeem();
		this.maxRredeem = p.getMaxRredeem();
		this.additionalRredeem = p.getAdditionalRredeem();
		this.maxHold = p.getMaxHold();
		this.accrualCycleOid = p.getAccrualCycleOid();
		this.purchaseConfirmDate = p.getPurchaseConfirmDays();// 申购确认日:()个
		this.purchaseConfirmDateType = p.getPurchaseConfirmDaysType();// 申购确认日类型:自然日或交易日
		this.redeemConfirmDate = p.getRedeemConfirmDays();// 赎回确认日:()个
		this.redeemConfirmDateType = p.getRedeemConfirmDaysType();// 赎回确认日类型:自然日或交易日
		this.accrualDate = p.getAccrualRepayDays();// 还本付息日 存续期结束后第()个自然日
		this.investComment = p.getInvestComment();// 投资标的
		this.instruction = p.getInstruction();// 产品说明
		this.riskLevel = p.getRiskLevel();// 风险等级
		this.fileKeys = p.getFileKeys();// 附加文件
		this.status = p.getState();// 产品状态
		this.recPeriodExpAnYield=Optional.ofNullable(p.getRecPeriodExpAnYield()).orElse(BigDecimal.ZERO);
		this.incomeCalcBasis=Optional.ofNullable(p.getIncomeCalcBasis()).orElse("0");
		this.createTime = DateUtil.formatDate(p.getCreateTime().getTime());// 创建时间
		this.updateTime = DateUtil.formatDate(p.getUpdateTime().getTime());// 更新时间
		this.operator = p.getOperator();// 操作员
		this.auditState = p.getAuditState();// 审核状态
		this.setupDate = p.getSetupDate() != null ? DateUtil.formatDate(p.getSetupDate().getTime()) : "";// 产品成立时间（存续期开始时间）
		this.isOpenPurchase = p.getIsOpenPurchase();// 开放申购期
		this.isOpenRemeed = p.getIsOpenRemeed();// 开放赎回期

		this.purchaseApplyStatus = p.getPurchaseApplyStatus();
		this.redeemApplyStatus = p.getRedeemApplyStatus();
		this.maxSaleVolume = p.getMaxSaleVolume();
		if (null != p.getAssetPool()) {
			AssetPoolEntity ap = p.getAssetPool();
			BigDecimal hqla = BigDecimal.ZERO;
			if (null != ap.getCashPosition()) {
				hqla = hqla.add(ap.getCashPosition());
			}
			if (null != ap.getScale() && null != ap.getCashtoolFactRate()) {
				hqla = hqla.add(ap.getScale().multiply(ap.getCashtoolFactRate()).setScale(2, RoundingMode.HALF_UP));
			}
			hqla = hqla.setScale(2, RoundingMode.HALF_UP);
			this.hqla = hqla;
		}
		
		/** 批量派息审核新增  **/
		this.repayDate = p.getRepayDate() != null ? DateUtil.formatDate(p.getRepayDate().getTime()) : "";  // 到期日
		this.collectedVolume = p.getCollectedVolume();  // 用户申购总额
		this.interestAuditStatus = p.getInterestAuditStatus();  // 派息审核状态
		/** 批量派息审核新增  **/

	}

	private String oid;
	private String corporateId;
	private String code;
	private String name;// 产品名称
	private String fullName;// 产品全称
	private String administrator;// 管理人
	private String typeOid;// 产品类型
	private String typeName;// 产品类型
	private String assetPoolOid;// 资产池Oid
	private String assetPoolName;// 资产池名称
	private String spvOid;// SPV Oid
	private String spvName;// SPV名称
	private String raiseStartDate;// 募集开始时间
	private Integer raisePeriod;// 募集期:()个自然日
	private Integer interestsFirstDate;// 起息日:募集满额后()个自然日
	private Integer durationPeriod;// 存续期:()个自然日
	private String expAror;// 预期年化收益率
	private String expArorSec;// 预期年化收益率区间
	private BigDecimal raisedTotalNumber;// 募集总份额
	private String netUnitShare;// 单位份额净值
	private BigDecimal investMin;// 单笔投资最低份额
	private BigDecimal recPeriodExpAnYield;
	private String incomeCalcBasis;
	private BigDecimal investMax;// 单笔投资最高份额
	private BigDecimal investAdditional;// 单笔投资追加份额
	private BigDecimal netMaxRredeemDay;// 单日净赎回上限
	private BigDecimal minRredeem;// 单笔净赎回下限
	private BigDecimal maxRredeem;
	private BigDecimal additionalRredeem;
	private BigDecimal maxHold;
	private String accrualCycleOid;// 收益结转周期
	private String accrualCycleName;// 收益结转周期
	private Integer purchaseConfirmDate;// 申购确认日:()个
	private String purchaseConfirmDateType;// 申购确认日类型:自然日或交易日
	private Integer redeemConfirmDate;// 赎回确认日:()个
	private String redeemConfirmDateType;// 赎回确认日类型:自然日或交易日
	private Integer accrualDate;// 还本付息日 存续期结束后第()个自然日
	private String investComment;// 投资标的
	private String instruction;// 产品说明
	private String riskLevel;// 风险等级
	private String fileKeys;// 附加文件
	private String status;// 产品状态
	private String createTime;// 创建时间
	private String updateTime;// 更新时间
	private String operator;// 操作员
	private String auditState;// 审核状态
	private String auditComment;// 审核备注
	private String setupDate;// 产品成立时间（存续期开始时间）
	private String isOpenPurchase;// 开放申购期
	private String isOpenRemeed;// 开放赎回期
	private Integer channelNum;// 取到数
	private List<FileResp> files;
	private String applicant;// 申请人
	private String applyTime;// 申请时间
	private String auditor;// 审核人
	private String auditTime;// 审核时间
	private String reviewer;// 复核人
	private String reviewTime;// 复核时间
	private String accesser; // 准入人
	private String accessTime; // 准入时间
	private String purchaseApplyStatus;
	private String redeemApplyStatus;
	private BigDecimal maxSaleVolume;
	private BigDecimal hqla;

	private BigDecimal balanceCostSum;
	private BigDecimal businessCostSum;
	
	/** 批量派息审核新增  **/
	private String repayDate;  // 到期日
	private String ratio;  // 实际年化收益率
	private BigDecimal totalRepay;  // 本金利息总额
	private BigDecimal collectedVolume;  // 用户申购总额
	private BigDecimal allocateIncomeAmount;  // 分配收益总额
	private String interestAuditStatus;  // 派息审核状态
	/** 批量派息审核新增  **/
}
