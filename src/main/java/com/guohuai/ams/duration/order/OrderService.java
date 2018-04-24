package com.guohuai.ams.duration.order;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.guohuai.ams.cashtool.CashTool;
import com.guohuai.ams.duration.assetPool.AssetPoolEntity;
import com.guohuai.ams.duration.assetPool.AssetPoolService;
import com.guohuai.ams.duration.capital.CapitalEntity;
import com.guohuai.ams.duration.capital.CapitalService;
import com.guohuai.ams.duration.fact.calibration.TargetAdjustEntity;
import com.guohuai.ams.duration.fact.calibration.TargetAdjustService;
import com.guohuai.ams.duration.order.fund.FundAuditEntity;
import com.guohuai.ams.duration.order.fund.FundEntity;
import com.guohuai.ams.duration.order.fund.FundOrderEntity;
import com.guohuai.ams.duration.order.fund.FundService;
import com.guohuai.ams.duration.order.trust.TrustAuditEntity;
import com.guohuai.ams.duration.order.trust.TrustEntity;
import com.guohuai.ams.duration.order.trust.TrustIncomeEntity;
import com.guohuai.ams.duration.order.trust.TrustIncomeForm;
import com.guohuai.ams.duration.order.trust.TrustOrderEntity;
import com.guohuai.ams.duration.order.trust.TrustService;
import com.guohuai.ams.duration.order.trust.TrustTransEntity;
import com.guohuai.ams.duration.target.TargetService;
import com.guohuai.ams.investment.Investment;
import com.guohuai.ams.investment.pool.InvestmentPoolService;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.BigDecimalUtil;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.StringUtil;
import com.guohuai.component.web.view.PageResp;

/**
 * 存续期--订单服务接口
 * 
 * @author star.zhu
 *         2016年5月16日
 */
@Service
public class OrderService {

	/**
	 * 标的类型
	 */
	public static final String FUND = "现金类管理工具标的";
	public static final String TRUST = "信托（计划）标的";

	/**
	 * 操作类型
	 */
	private static final String PURCHASE = "purchase"; // 申购
	private static final String REDEEM = "redeem"; // 赎回
	private static final String INCOME = "income"; // 本息兑付
	private static final String BACK = "back"; // 退款
	private static final String TRANSFER = "transfer"; // 转让
	private static final String OVERDUETRANSFER = "overdue_transfer"; // 逾期转让
	private static final String TRANS = "trans"; // 转入

	private static final String APPLY = "apply"; // 申请
	private static final String AUDIT = "audit"; // 审核
	private static final String APPOINTMENT = "appointment"; // 预约
	private static final String CONFIRM = "confirm"; // 确认

	/**
	 * 订单状态
	 */
	private static final String APPLY00 = "00"; // 申请待审核
	private static final String AUDIT10 = "10"; // 审核未通过
	private static final String AUDIT11 = "11"; // 审核通过待预约
	private static final String AUDIT12 = "12"; // 审核通过待确认
	private static final String APPOINTMENT20 = "20"; // 预约未通过
	private static final String APPOINTMENT21 = "21"; // 预约通过待确认
	private static final String CONFIRM30 = "30"; // 确认未通过
	private static final String CONFIRM31 = "31"; // 确认通过
	public static final String INVALID = "-1"; // 订单已失效

	@Autowired
	private FundService fundService;
	@Autowired
	private TrustService trustService;
	@Autowired
	private CapitalService capitalService;
	@Autowired
	private TargetService targetService;
	@Autowired
	private AssetPoolService assetPoolService;
//	@Autowired
//	private CashtoolPoolService cashtoolPoolService;
	@Autowired
	private InvestmentPoolService investService;
	@Autowired
	private TargetAdjustService adjustService;
	// @Autowired
	// private WarehouseDocumentService documentService;

	/**
	 * 货币基金（现金类管理工具）申购
	 * 
	 * @param from
	 * @param uid
	 * @param type
	 *            申购方式：assetPool（资产池）；order（订单）
	 */
	@Transactional
	public void purchaseForFund(FundForm form, String uid, String type) {
		FundEntity entity = fundService.getFundByPidAndCashtoolOid(form.getAssetPoolOid(), form.getCashtoolOid());
		// if ("assetPool".equals(type)) {
		if (null == entity) {
			entity = new FundEntity();
			entity.setOid(StringUtil.uuid());
			CashTool cashTool = targetService.getCashToolByOid(form.getCashtoolOid());
			entity.setCashTool(cashTool);
			entity.setAssetPoolOid(form.getAssetPoolOid());
			entity.setState(FundEntity.INVESTEND);
			entity.setAmount(BigDecimal.ZERO);
			entity.setFrozenCapital(BigDecimal.ZERO);
			entity.setPurchaseVolume(BigDecimal.ZERO);
			entity.setRedeemVolume(BigDecimal.ZERO);
		} /*
			 * else {
			 * entity = fundService.getFundByOid(form.getOid());
			 * }
			 */
		BigDecimal applyCash = BigDecimalUtil.formatForMul10000(form.getApplyCash());
		entity.setPurchaseVolume(applyCash);
		entity.setFrozenCapital(applyCash);
		fundService.save(entity);

		FundOrderEntity order = new FundOrderEntity();
		order.setOid(StringUtil.uuid());
		order.setFundEntity(entity);
		order.setState(APPLY00);
		// order.setInvestDate(form.getInvestDate());
		order.setVolume(applyCash);
		order.setOptType(PURCHASE);
		order.setAsker(uid);
		order.setCreateTime(DateUtil.getSqlCurrentDate());

		fundService.save(order);

		// 资金变动记录
		capitalService.capitalFlow(form.getAssetPoolOid(), form.getCashtoolOid(), order.getOid(), FUND, applyCash, BigDecimal.ZERO, PURCHASE, APPLY, uid, null);
	}

	/**
	 * 货币基金（现金类管理工具）申购审核
	 * 
	 * @param form
	 * @param uid
	 */
	@Transactional
	public void auditForPurchase(FundForm form, String uid) {
		FundOrderEntity order = fundService.getFundOrderByOid(form.getOid());
		if (null == order) {
			throw new AMPException("无效的订单查询，请确认订单id！");
		} else if (!APPLY00.equals(order.getState())) {
			throw new AMPException("该订单已受理, 不可重复操作！");
		}
		BigDecimal auditVolume = BigDecimalUtil.formatForMul10000(form.getAuditVolume());
		FundEntity fundEntity = order.getFundEntity();
		BigDecimal temp = BigDecimalUtil.init0;
		if (FundAuditEntity.SUCCESSED.equals(form.getState())) {
			temp = auditVolume.subtract(order.getVolume()).setScale(4, BigDecimal.ROUND_HALF_UP);
			fundEntity.setPurchaseVolume(fundEntity.getPurchaseVolume().add(temp).setScale(4, BigDecimal.ROUND_HALF_UP));
			fundEntity.setFrozenCapital(fundEntity.getFrozenCapital().add(temp).setScale(4, BigDecimal.ROUND_HALF_UP));
			order.setState(AUDIT11);
		} else {
			auditVolume = BigDecimalUtil.init0;
			temp = order.getVolume();
			fundEntity.setPurchaseVolume(fundEntity.getPurchaseVolume().subtract(temp).setScale(4, BigDecimal.ROUND_HALF_UP));
			fundEntity.setFrozenCapital(fundEntity.getFrozenCapital().subtract(temp).setScale(4, BigDecimal.ROUND_HALF_UP));
			order.setState(AUDIT10);
		}
		fundEntity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
		// 录入申购订单的审核信息
		order.setAuditVolume(auditVolume);
		order.setAuditor(uid);
		order.setUpdateTime(DateUtil.getSqlCurrentDate());

		fundService.save(order);

		// 审核记录
		FundAuditEntity entity = new FundAuditEntity();
		entity.setOid(StringUtil.uuid());
		entity.setOrderOid(form.getOid());
		entity.setAuditType(FundAuditEntity.TYPE_AUDIT + "-Purchase");
		entity.setAuditState(form.getState());
		entity.setAuditor(uid);
		entity.setAuditTime(DateUtil.getSqlCurrentDate());

		fundService.save(entity);

		// 资金变动记录
		capitalService.capitalFlow(order.getFundEntity().getAssetPoolOid(), order.getFundEntity().getCashTool().getOid(), order.getOid(), FUND, auditVolume, order.getVolume(), PURCHASE, AUDIT, uid,
				form.getState());
	}

	/**
	 * 货币基金（现金类管理工具）申购资金预约
	 * 
	 * @param form
	 * @param uid
	 */
	@Transactional
	public void appointmentForPurchase(FundForm form, String uid) {
		FundOrderEntity order = fundService.getFundOrderByOid(form.getOid());
		if (null == order) {
			throw new AMPException("无效的订单查询，请确认订单id！");
		} else if (APPOINTMENT21.equals(order.getState()) || APPOINTMENT20.equals(order.getState()) || CONFIRM31.equals(order.getState()) || CONFIRM30.equals(order.getState())
				|| INVALID.equals(order.getState())) {
			throw new AMPException("该订单已受理, 不可重复操作！");
		}
		BigDecimal reserveVolume = BigDecimalUtil.formatForMul10000(form.getReserveVolume());
		FundEntity fundEntity = order.getFundEntity();
		BigDecimal temp = BigDecimalUtil.init0;
		BigDecimal tempSub = BigDecimalUtil.init0;
		if (AUDIT11.equals(order.getState())) {
			temp = order.getAuditVolume();
			tempSub = reserveVolume.subtract(temp).setScale(4, BigDecimal.ROUND_HALF_UP);
		} else if (APPLY00.equals(order.getState())) {
			temp = order.getVolume();
			tempSub = reserveVolume.subtract(temp).setScale(4, BigDecimal.ROUND_HALF_UP);
		}
		if (FundAuditEntity.SUCCESSED.equals(form.getState())) {
			// temp = reserveVolume.subtract(
			// order.getAuditVolume().compareTo(BigDecimal.ZERO) == 0 ? order.getVolume() : order.getAuditVolume())
			// .setScale(4, BigDecimal.ROUND_HALF_UP);
			fundEntity.setPurchaseVolume(fundEntity.getPurchaseVolume().add(tempSub).setScale(4, BigDecimal.ROUND_HALF_UP));
			fundEntity.setFrozenCapital(fundEntity.getFrozenCapital().add(tempSub).setScale(4, BigDecimal.ROUND_HALF_UP));
			order.setState(APPOINTMENT21);
		} else {
			reserveVolume = BigDecimalUtil.init0;
			// temp = order.getAuditVolume().compareTo(BigDecimal.ZERO) == 0 ? order.getVolume() : order.getAuditVolume();
			fundEntity.setPurchaseVolume(fundEntity.getPurchaseVolume().subtract(temp).setScale(4, BigDecimal.ROUND_HALF_UP));
			fundEntity.setFrozenCapital(fundEntity.getFrozenCapital().subtract(temp).setScale(4, BigDecimal.ROUND_HALF_UP));
			order.setState(APPOINTMENT20);
		}
		fundEntity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
		// 录入申购订单的资金预约信息
		order.setReserveVolume(reserveVolume);
		order.setReserver(uid);
		order.setUpdateTime(DateUtil.getSqlCurrentDate());

		fundService.save(order);

		FundAuditEntity entity = new FundAuditEntity();
		entity.setOid(StringUtil.uuid());
		entity.setOrderOid(form.getOid());
		entity.setAuditType(FundAuditEntity.TYPE_APPOINTMENT + "-Purchase");
		entity.setAuditState(form.getState());
		entity.setAuditor(uid);
		entity.setAuditTime(DateUtil.getSqlCurrentDate());

		fundService.save(entity);

		// 资金变动记录
		// BigDecimal volume = order.getAuditVolume().compareTo(BigDecimal.ZERO) == 0 ? order.getVolume() : order.getAuditVolume();
		capitalService.capitalFlow(order.getFundEntity().getAssetPoolOid(), order.getFundEntity().getCashTool().getOid(), order.getOid(), FUND, reserveVolume, temp, PURCHASE, APPOINTMENT, uid,
				form.getState());
	}

	/**
	 * 货币基金（现金类管理工具）申购订单确认
	 * 
	 * @param form
	 * @param uid
	 */
	@Transactional
	public void orderConfirmForPurchase(FundForm form, String uid) {
		FundOrderEntity order = fundService.getFundOrderByOid(form.getOid());
		if (null == order) {
			throw new AMPException("无效的订单查询，请确认订单id！");
		} else if (CONFIRM31.equals(order.getState()) || CONFIRM30.equals(order.getState()) || INVALID.equals(order.getState())) {
			throw new AMPException("该订单已受理, 不可重复操作！");
		}
		BigDecimal investVolume = BigDecimalUtil.formatForMul10000(form.getInvestVolume());
		FundEntity fundEntity = order.getFundEntity();
		BigDecimal temp = BigDecimalUtil.init0;
		if (APPOINTMENT21.equals(order.getState())) {
			temp = order.getReserveVolume();
		} else if (AUDIT11.equals(order.getState())) {
			temp = order.getAuditVolume();
		} else if (APPLY00.equals(order.getState())) {
			temp = order.getVolume();
		}
		if (FundAuditEntity.SUCCESSED.equals(form.getState())) {
			Timestamp time = new Timestamp(System.currentTimeMillis());
			order.setConfirmDate(new Date(System.currentTimeMillis()));
			order.setInvestTime(time);
			fundEntity.setState(FundEntity.INVESTING);
			// temp = order.getReserveVolume().compareTo(BigDecimal.ZERO) == 0 ?
			// order.getAuditVolume().compareTo(BigDecimal.ZERO) == 0 ? order.getVolume() : order.getAuditVolume()
			// : order.getReserveVolume();
			fundEntity.setAmount(fundEntity.getAmount().add(investVolume).setScale(4, BigDecimal.ROUND_HALF_UP));
			fundEntity.setCreateTime(time);
			fundEntity.setInterestDate(this.getInterestDate());
			if (this.dateFalg()) {
				fundEntity.setInterestAcount(fundEntity.getInterestAcount().add(investVolume).setScale(4, BigDecimal.ROUND_HALF_UP));
			} else {
				fundEntity.setPurchaseAcount(fundEntity.getPurchaseAcount().add(investVolume).setScale(4, BigDecimal.ROUND_HALF_UP));
			}
			order.setState(CONFIRM31);
		} else {
			investVolume = BigDecimalUtil.init0;
			// temp = order.getReserveVolume().compareTo(BigDecimal.ZERO) == 0 ?
			// order.getAuditVolume().compareTo(BigDecimal.ZERO) == 0 ? order.getVolume() : order.getAuditVolume()
			// : order.getReserveVolume();
			order.setState(CONFIRM30);
		}
		fundEntity.setPurchaseVolume(fundEntity.getPurchaseVolume().subtract(temp).setScale(4, BigDecimal.ROUND_HALF_UP));
		fundEntity.setFrozenCapital(fundEntity.getFrozenCapital().subtract(temp).setScale(4, BigDecimal.ROUND_HALF_UP));
		fundEntity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
		// 录入申购订单的确认信息
		order.setInvestVolume(investVolume);
		order.setConfirmer(uid);
		order.setUpdateTime(DateUtil.getSqlCurrentDate());

		fundService.save(order);

		FundAuditEntity entity = new FundAuditEntity();
		entity.setOid(StringUtil.uuid());
		entity.setOrderOid(form.getOid());
		entity.setAuditType(FundAuditEntity.TYPE_CONFIRM + "-Purchase");
		entity.setAuditState(form.getState());
		entity.setAuditor(uid);
		entity.setAuditTime(DateUtil.getSqlCurrentDate());

		fundService.save(entity);

		// 资金变动记录
		// BigDecimal volume = order.getReserveVolume().compareTo(BigDecimal.ZERO) == 0 ?
		// order.getAuditVolume().compareTo(BigDecimal.ZERO) == 0 ? order.getVolume() : order.getAuditVolume()
		// : order.getReserveVolume();
		capitalService.capitalFlow(order.getFundEntity().getAssetPoolOid(), order.getFundEntity().getCashTool().getOid(), order.getOid(), FUND, investVolume, temp, PURCHASE, CONFIRM, uid,
				form.getState());
	}

	/**
	 * 货币基金（现金类管理工具）赎回
	 * 
	 * @param from
	 * @param uid
	 */
	@Transactional
	public void redeem(FundForm form, String uid) {
		FundEntity entity = fundService.getFundByOid(form.getOid());
		if (null == entity) {
			throw new AMPException("无效的操作，请确认订单id！");
		}
		BigDecimal returnVolume = BigDecimalUtil.init0;
		if ("yes".equals(form.getAllFlag())) {
			returnVolume = entity.getAmount();
		} else {
			returnVolume = BigDecimalUtil.formatForMul10000(form.getReturnVolume());
		}
		entity.setAmount(entity.getAmount().subtract(returnVolume).setScale(4, BigDecimal.ROUND_HALF_UP));
		entity.setRedeemVolume(entity.getRedeemVolume().add(returnVolume).setScale(4, BigDecimal.ROUND_HALF_UP));
		entity.setOnWayCapital(entity.getOnWayCapital().add(returnVolume).setScale(4, BigDecimal.ROUND_HALF_UP));
		entity.setUpdateTime(new Timestamp(System.currentTimeMillis()));

		fundService.save(entity);

		FundOrderEntity order = new FundOrderEntity();
		order.setOid(StringUtil.uuid());
		order.setFundEntity(entity);
		// order.setRedeemDate(form.getRedeemDate());
		order.setReturnVolume(returnVolume);
		order.setOptType("redeem");
		order.setState(APPLY00);
		order.setAllFlag(form.getAllFlag());
		order.setAsker(uid);
		order.setCreateTime(DateUtil.getSqlCurrentDate());

		fundService.save(order);

		// 资金变动记录
		capitalService.capitalFlow(entity.getAssetPoolOid(), entity.getCashTool().getOid(), order.getOid(), FUND, returnVolume, BigDecimal.ZERO, REDEEM, APPLY, uid, null);
	}

	/**
	 * 货币基金（现金类管理工具）赎回审核
	 * 
	 * @param form
	 * @param uid
	 */
	@Transactional
	public void auditForRedeem(FundForm form, String uid) {
		FundOrderEntity order = fundService.getFundOrderByOid(form.getOid());
		if (null == order) {
			throw new AMPException("无效的订单查询，请确认订单id！");
		} else if (!APPLY00.equals(order.getState())) {
			throw new AMPException("该订单已受理, 不可重复操作！");
		}
		BigDecimal auditVolume = BigDecimalUtil.formatForMul10000(form.getAuditVolume());
		FundEntity fundEntity = order.getFundEntity();
		BigDecimal temp = BigDecimalUtil.init0;
		if (FundAuditEntity.SUCCESSED.equals(form.getState())) {
			temp = auditVolume.subtract(order.getReturnVolume()).setScale(4, BigDecimal.ROUND_HALF_UP);
			fundEntity.setAmount(fundEntity.getAmount().subtract(temp).setScale(4, BigDecimal.ROUND_HALF_UP));
			fundEntity.setRedeemVolume(fundEntity.getRedeemVolume().add(temp).setScale(4, BigDecimal.ROUND_HALF_UP));
			fundEntity.setOnWayCapital(fundEntity.getOnWayCapital().add(temp).setScale(4, BigDecimal.ROUND_HALF_UP));
			order.setState(AUDIT11);
		} else {
			auditVolume = BigDecimalUtil.init0;
			temp = order.getReturnVolume();
			fundEntity.setAmount(fundEntity.getAmount().add(temp).setScale(4, BigDecimal.ROUND_HALF_UP));
			fundEntity.setRedeemVolume(fundEntity.getRedeemVolume().subtract(temp).setScale(4, BigDecimal.ROUND_HALF_UP));
			fundEntity.setOnWayCapital(fundEntity.getOnWayCapital().subtract(temp).setScale(4, BigDecimal.ROUND_HALF_UP));
			order.setState(AUDIT10);
		}
		fundEntity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
		// 录入申购订单的审核信息
		order.setAuditVolume(auditVolume);
		order.setAuditor(uid);
		order.setUpdateTime(DateUtil.getSqlCurrentDate());

		fundService.save(order);

		// 审核记录
		FundAuditEntity entity = new FundAuditEntity();
		entity.setOid(StringUtil.uuid());
		entity.setOrderOid(form.getOid());
		entity.setAuditType(FundAuditEntity.TYPE_AUDIT + "-Redeem");
		entity.setAuditState(form.getState());
		entity.setAuditor(uid);
		entity.setAuditTime(DateUtil.getSqlCurrentDate());

		fundService.save(entity);

		// 资金变动记录
		capitalService.capitalFlow(order.getFundEntity().getAssetPoolOid(), order.getFundEntity().getCashTool().getOid(), order.getOid(), FUND, auditVolume, order.getReturnVolume(), REDEEM, AUDIT,
				uid, form.getState());
	}

	/**
	 * 货币基金（现金类管理工具）赎回资金预约
	 * 
	 * @param form
	 * @param uid
	 */
	@Transactional
	public void appointmentForRedeem(FundForm form, String uid) {
		FundOrderEntity order = fundService.getFundOrderByOid(form.getOid());
		if (null == order) {
			throw new AMPException("无效的订单查询，请确认订单id！");
		} else if (APPOINTMENT21.equals(order.getState()) || APPOINTMENT20.equals(order.getState()) || CONFIRM31.equals(order.getState()) || CONFIRM30.equals(order.getState())
				|| INVALID.equals(order.getState())) {
			throw new AMPException("该订单已受理, 不可重复操作！");
		}
		BigDecimal reserveVolume = BigDecimalUtil.formatForMul10000(form.getReserveVolume());
		FundEntity fundEntity = order.getFundEntity();
		BigDecimal temp = BigDecimalUtil.init0;
		BigDecimal tempSub = BigDecimalUtil.init0;
		if (AUDIT11.equals(order.getState())) {
			temp = order.getAuditVolume();
			tempSub = reserveVolume.subtract(temp).setScale(4, BigDecimal.ROUND_HALF_UP);
		} else if (APPLY00.equals(order.getState())) {
			temp = order.getReturnVolume();
			tempSub = reserveVolume.subtract(temp).setScale(4, BigDecimal.ROUND_HALF_UP);
		}
		if (FundAuditEntity.SUCCESSED.equals(form.getState())) {
			// temp = reserveVolume.subtract(
			// order.getAuditVolume().compareTo(BigDecimal.ZERO) == 0 ? order.getReturnVolume() : order.getAuditVolume())
			// .setScale(4, BigDecimal.ROUND_HALF_UP);
			fundEntity.setAmount(fundEntity.getAmount().subtract(tempSub).setScale(4, BigDecimal.ROUND_HALF_UP));
			fundEntity.setRedeemVolume(fundEntity.getRedeemVolume().add(tempSub).setScale(4, BigDecimal.ROUND_HALF_UP));
			fundEntity.setOnWayCapital(fundEntity.getOnWayCapital().add(tempSub).setScale(4, BigDecimal.ROUND_HALF_UP));
			order.setState(APPOINTMENT21);
		} else {
			reserveVolume = BigDecimalUtil.init0;
			// temp = order.getAuditVolume().compareTo(BigDecimal.ZERO) == 0 ? order.getReturnVolume() : order.getAuditVolume();
			fundEntity.setAmount(fundEntity.getAmount().add(temp).setScale(4, BigDecimal.ROUND_HALF_UP));
			fundEntity.setRedeemVolume(fundEntity.getRedeemVolume().subtract(temp).setScale(4, BigDecimal.ROUND_HALF_UP));
			fundEntity.setOnWayCapital(fundEntity.getOnWayCapital().subtract(temp).setScale(4, BigDecimal.ROUND_HALF_UP));
			order.setState(APPOINTMENT20);
		}
		fundEntity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
		// 录入申购订单的资金预约信息
		order.setReserveVolume(reserveVolume);
		order.setReserver(uid);
		order.setUpdateTime(DateUtil.getSqlCurrentDate());

		fundService.save(order);

		FundAuditEntity entity = new FundAuditEntity();
		entity.setOid(StringUtil.uuid());
		entity.setOrderOid(form.getOid());
		entity.setAuditType(FundAuditEntity.TYPE_APPOINTMENT + "-Redeem");
		entity.setAuditState(form.getState());
		entity.setAuditor(uid);
		entity.setAuditTime(DateUtil.getSqlCurrentDate());

		fundService.save(entity);

		// 资金变动记录
		// BigDecimal volume = order.getAuditVolume().compareTo(BigDecimal.ZERO) == 0 ? order.getReturnVolume() : order.getAuditVolume();
		capitalService.capitalFlow(order.getFundEntity().getAssetPoolOid(), order.getFundEntity().getCashTool().getOid(), order.getOid(), FUND, reserveVolume, temp, REDEEM, APPOINTMENT, uid,
				form.getState());
	}

	/**
	 * 货币基金（现金类管理工具）赎回订单确认
	 * 
	 * @param form
	 * @param uid
	 */
	@Transactional
	public void orderConfirmForRedeem(FundForm form, String uid) {
		FundOrderEntity order = fundService.getFundOrderByOid(form.getOid());
		if (null == order) {
			throw new AMPException("无效的订单查询，请确认订单id！");
		} else if (CONFIRM31.equals(order.getState()) || CONFIRM30.equals(order.getState()) || INVALID.equals(order.getState())) {
			throw new AMPException("该订单已受理, 不可重复操作！");
		}
		BigDecimal investVolume = BigDecimalUtil.formatForMul10000(form.getInvestVolume());
		FundEntity fundEntity = order.getFundEntity();
		BigDecimal temp = BigDecimalUtil.init0;
		BigDecimal tempSub = BigDecimalUtil.init0;
		if (APPOINTMENT21.equals(order.getState())) {
			temp = order.getReserveVolume();
		} else if (AUDIT11.equals(order.getState())) {
			temp = order.getAuditVolume();
		} else if (APPLY00.equals(order.getState())) {
			temp = order.getReturnVolume();
		}
		if (FundAuditEntity.SUCCESSED.equals(form.getState())) {
			order.setConfirmDate(new Date(System.currentTimeMillis()));
			// temp = order.getReserveVolume().compareTo(BigDecimal.ZERO) == 0 ?
			// order.getAuditVolume().compareTo(BigDecimal.ZERO) == 0 ? order.getReturnVolume() : order.getAuditVolume()
			// : order.getReserveVolume();
			tempSub = investVolume.subtract(temp).setScale(4, BigDecimal.ROUND_HALF_UP);
			fundEntity.setAmount(fundEntity.getAmount().subtract(tempSub).setScale(4, BigDecimal.ROUND_HALF_UP));
			fundEntity.setCreateTime(new Timestamp(System.currentTimeMillis()));
			if (this.dateFalg()) {
				fundEntity.setInterestAcount(fundEntity.getInterestAcount().subtract(investVolume).setScale(4, BigDecimal.ROUND_HALF_UP));
			} else {
				fundEntity.setRedeemAcount(fundEntity.getRedeemAcount().add(investVolume).setScale(4, BigDecimal.ROUND_HALF_UP));
			}
			order.setState(CONFIRM31);
		} else {
			investVolume = BigDecimalUtil.init0;
			// temp = order.getReserveVolume().compareTo(BigDecimal.ZERO) == 0 ?
			// order.getAuditVolume().compareTo(BigDecimal.ZERO) == 0 ? order.getReturnVolume() : order.getAuditVolume()
			// : order.getReserveVolume();
			fundEntity.setAmount(fundEntity.getAmount().add(temp).setScale(4, BigDecimal.ROUND_HALF_UP));
			order.setState(CONFIRM30);
		}
		fundEntity.setRedeemVolume(fundEntity.getRedeemVolume().subtract(temp).setScale(4, BigDecimal.ROUND_HALF_UP));
		fundEntity.setOnWayCapital(fundEntity.getOnWayCapital().subtract(temp).setScale(4, BigDecimal.ROUND_HALF_UP));
		fundEntity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
		// 录入申购订单的确认信息
		order.setInvestVolume(investVolume);
		order.setConfirmer(uid);
		order.setUpdateTime(DateUtil.getSqlCurrentDate());

		fundService.save(order);

		FundAuditEntity entity = new FundAuditEntity();
		entity.setOid(StringUtil.uuid());
		entity.setOrderOid(form.getOid());
		entity.setAuditType(FundAuditEntity.TYPE_CONFIRM + "-Redeem");
		entity.setAuditState(form.getState());
		entity.setAuditor(uid);
		entity.setAuditTime(DateUtil.getSqlCurrentDate());

		fundService.save(entity);

		// 资金变动记录
		// BigDecimal volume = order.getReserveVolume().compareTo(BigDecimal.ZERO) == 0 ?
		// order.getAuditVolume().compareTo(BigDecimal.ZERO) == 0 ? order.getReturnVolume() : order.getAuditVolume()
		// : order.getReserveVolume();
		capitalService.capitalFlow(order.getFundEntity().getAssetPoolOid(), order.getFundEntity().getCashTool().getOid(), order.getOid(), FUND, investVolume, temp, REDEEM, CONFIRM, uid,
				form.getState());
	}

	/**
	 * 信托（计划）申购
	 * 
	 * @param from
	 * @param uid
	 */
	@Transactional
	public void purchaseForTrust(TrustForm form, String uid) {
		TrustOrderEntity order = new TrustOrderEntity();
		BigDecimal applyCash = BigDecimalUtil.formatForMul10000(form.getApplyVolume());

		order.setOid(StringUtil.uuid());
		Investment target = targetService.getInvestmentByOid(form.getTargetOid());
		order.setTarget(target);
		order.setAssetPoolOid(form.getAssetPoolOid());
		// order.setInvestDate(form.getInvestDate());
		order.setApplyVolume(applyCash);
		order.setApplyCash(applyCash);
		order.setProfitType(form.getProfitType());
		order.setType(TrustOrderEntity.TYPE_PURCHASE);
		order.setState(APPLY00);
		order.setAsker(uid);
		order.setCreateTime(DateUtil.getSqlCurrentDate());

		trustService.save(order);

		investService.incApplyAmount(form.getTargetOid(), applyCash);
		investService.subRestTrustAmount(form.getTargetOid(), applyCash);

		// 资金变动记录
		capitalService.capitalFlow(form.getAssetPoolOid(), form.getTargetOid(), order.getOid(), TRUST, applyCash, BigDecimal.ZERO, PURCHASE, APPLY, uid, null);
	}

	/**
	 * 信托（计划）申购审核
	 * 
	 * @param form
	 * @param uid
	 */
	@Transactional
	public void auditForTrust(TrustForm form, String uid) {
		TrustOrderEntity order = trustService.getTrustOrderByOid(form.getOid());
		if (null == order) {
			throw new AMPException("无效的订单查询，请确认订单id！");
		} else if (!APPLY00.equals(order.getState())) {
			throw new AMPException("该订单已受理, 不可重复操作！");
		}
		BigDecimal auditCash = BigDecimalUtil.formatForMul10000(form.getAuditCash());
		BigDecimal auditVolume = BigDecimalUtil.formatForMul10000(form.getAuditVolume());
		if (TrustAuditEntity.SUCCESSED.equals(form.getState()))
			order.setState(AUDIT11);
		else {
			auditCash = BigDecimalUtil.init0;
			auditVolume = BigDecimalUtil.init0;
			order.setState(AUDIT10);
		}
		// 录入申购订单的审核信息
		order.setAuditVolume(auditVolume.equals(BigDecimalUtil.init0) ? auditCash : auditVolume);
		order.setAuditCash(auditCash);
		order.setAuditor(uid);
		order.setUpdateTime(DateUtil.getSqlCurrentDate());

		trustService.save(order);

		BigDecimal temp = order.getAuditVolume().subtract(order.getApplyVolume()).setScale(4, BigDecimal.ROUND_HALF_UP);
		if (temp.compareTo(BigDecimal.ZERO) != 0) {
			investService.incApplyAmount(order.getTarget().getOid(), temp);
			investService.subRestTrustAmount(order.getTarget().getOid(), temp);
		}

		TrustAuditEntity entity = new TrustAuditEntity();
		entity.setOid(StringUtil.uuid());
		entity.setOrderOid(form.getOid());
		entity.setAuditType(TrustAuditEntity.TYPE_AUDIT + "-Purchase");
		entity.setAuditState(form.getState());
		entity.setAuditor(uid);
		entity.setAuditTime(DateUtil.getSqlCurrentDate());

		trustService.save(entity);

		// 资金变动记录
		capitalService.capitalFlow(order.getAssetPoolOid(), order.getTarget().getOid(), order.getOid(), TRUST, auditCash, order.getApplyCash(), PURCHASE, AUDIT, uid, form.getState());
	}

	/**
	 * 信托（计划）资金预约
	 * 
	 * @param form
	 * @param uid
	 */
	@Transactional
	public void appointmentForTrust(TrustForm form, String uid) {
		TrustOrderEntity order = trustService.getTrustOrderByOid(form.getOid());
		if (null == order) {
			throw new AMPException("无效的订单查询，请确认订单id！");
		} else if (APPOINTMENT21.equals(order.getState()) || APPOINTMENT20.equals(order.getState()) || CONFIRM31.equals(order.getState()) || CONFIRM30.equals(order.getState())
				|| INVALID.equals(order.getState())) {
			throw new AMPException("该订单已受理, 不可重复操作！");
		}
		BigDecimal reserveCash = BigDecimalUtil.formatForMul10000(form.getReserveCash());
		BigDecimal reserveVolume = BigDecimalUtil.formatForMul10000(form.getReserveVolume());

		// 记录订单上一步操作状态
		String state = order.getState();

		if (TrustAuditEntity.SUCCESSED.equals(form.getState()))
			order.setState(APPOINTMENT21);
		else {
			reserveCash = BigDecimalUtil.init0;
			reserveVolume = BigDecimalUtil.init0;
			order.setState(APPOINTMENT20);
		}
		// 录入申购订单的资金预约信息
		order.setReserveVolume(reserveVolume.equals(BigDecimalUtil.init0) ? reserveCash : reserveVolume);
		order.setReserveCash(reserveCash);
		order.setReserver(uid);
		order.setUpdateTime(DateUtil.getSqlCurrentDate());

		trustService.save(order);

		// BigDecimal temp = order.getReserveVolume().subtract(
		// order.getAuditVolume().compareTo(BigDecimal.ZERO) == 0 ? order.getApplyVolume() : order.getAuditVolume())
		// .setScale(4, BigDecimal.ROUND_HALF_UP);
		BigDecimal temp = BigDecimalUtil.init0;
		BigDecimal tempSub = BigDecimalUtil.init0;
		if (AUDIT11.equals(state)) {
			temp = order.getAuditCash();
			tempSub = order.getReserveVolume().subtract(order.getAuditVolume()).setScale(4, BigDecimal.ROUND_HALF_UP);
		} else if (APPLY00.equals(state)) {
			temp = order.getApplyCash();
			tempSub = order.getReserveVolume().subtract(order.getApplyVolume()).setScale(4, BigDecimal.ROUND_HALF_UP);
		}
		if (temp.compareTo(BigDecimal.ZERO) != 0) {
			investService.incApplyAmount(order.getTarget().getOid(), tempSub);
			investService.subRestTrustAmount(order.getTarget().getOid(), tempSub);
		}

		TrustAuditEntity entity = new TrustAuditEntity();
		entity.setOid(StringUtil.uuid());
		entity.setOrderOid(form.getOid());
		entity.setAuditType(TrustAuditEntity.TYPE_APPOINTMENT + "-Purchase");
		entity.setAuditState(form.getState());
		entity.setAuditor(uid);
		entity.setAuditTime(DateUtil.getSqlCurrentDate());

		trustService.save(entity);

		// 资金变动记录
		// BigDecimal account = order.getAuditCash().compareTo(BigDecimal.ZERO) == 0 ? order.getApplyCash() : order.getAuditCash();
		capitalService.capitalFlow(order.getAssetPoolOid(), order.getTarget().getOid(), order.getOid(), TRUST, reserveCash, temp, PURCHASE, APPOINTMENT, uid, form.getState());
	}

	/**
	 * 信托（计划）订单确认
	 * 
	 * @param form
	 * @param uid
	 */
	@Transactional
	public void orderConfirmForTrust(TrustForm form, String uid) {
		TrustOrderEntity order = trustService.getTrustOrderByOid(form.getOid());
		if (null == order) {
			throw new AMPException("无效的订单查询，请确认订单id！");
		} else if (CONFIRM31.equals(order.getState()) || CONFIRM30.equals(order.getState()) || INVALID.equals(order.getState())) {
			throw new AMPException("该订单已受理, 不可重复操作！");
		}
		BigDecimal investCash = BigDecimalUtil.formatForMul10000(form.getInvestCash());
		BigDecimal investVolume = BigDecimalUtil.formatForMul10000(form.getInvestVolume());

		// BigDecimal temp = order.getReserveVolume().compareTo(BigDecimal.ZERO) == 0 ?
		// order.getAuditVolume().compareTo(BigDecimal.ZERO) == 0 ? order.getApplyVolume() : order.getAuditVolume()
		// : order.getReserveVolume();
		BigDecimal temp = BigDecimalUtil.init0;
		BigDecimal tempCash = BigDecimalUtil.init0;
		if (APPOINTMENT21.equals(order.getState())) {
			temp = order.getReserveVolume();
			tempCash = order.getReserveCash();
		} else if (AUDIT11.equals(order.getState())) {
			temp = order.getAuditVolume();
			tempCash = order.getAuditCash();
		} else if (APPLY00.equals(order.getState())) {
			temp = order.getApplyVolume();
			tempCash = order.getApplyCash();
		}
		if (TrustAuditEntity.SUCCESSED.equals(form.getState())) {
			order.setConfirmDate(new Date(System.currentTimeMillis()));
			Timestamp time = new Timestamp(System.currentTimeMillis());
			order.setInvestTime(time);
			order.setInvestDate(this.getInterestDate());
			// 录入申购订单的确认信息
			order.setInvestVolume(investVolume.equals(BigDecimalUtil.init0) ? investCash : investVolume);
			order.setInvestCash(investCash);
			order.setConfirmer(uid);
			order.setUpdateTime(DateUtil.getSqlCurrentDate());

			TrustEntity trustEntity = trustService.getFundByPidAndTargetOid(order.getAssetPoolOid(), order.getTarget().getOid(), order.getProfitType());
			if (null != trustEntity && trustEntity.getProfitType().equals(order.getProfitType())) {
				trustEntity.setInvestVolume(trustEntity.getInvestVolume().add(order.getInvestVolume()).setScale(4, BigDecimal.ROUND_HALF_UP));
				trustEntity.setUpdateTime(time);
			} else {
				trustEntity = new TrustEntity();
				trustEntity.setOid(StringUtil.uuid());
				trustEntity.setTarget(order.getTarget());
				trustEntity.setOrderOid(order.getOid());
				trustEntity.setAssetPoolOid(order.getAssetPoolOid());
				trustEntity.setPurchase(PURCHASE);
				trustEntity.setState(TrustEntity.INVESTING);
				trustEntity.setApplyVolume(order.getInvestVolume());
				trustEntity.setApplyCash(order.getInvestCash());
				trustEntity.setConfirmVolume(order.getInvestVolume());
				trustEntity.setInvestVolume(order.getInvestCash());
				// trustEntity.setInvestDate(order.getInvestDate());
				trustEntity.setProfitType(order.getProfitType());
				trustEntity.setCreateTime(time);
				trustEntity.setUpdateTime(time);
			}
			if (this.dateFalg()) {
				trustEntity.setInterestAcount(trustEntity.getInterestAcount().add(order.getInvestVolume()).setScale(4, BigDecimal.ROUND_HALF_UP));
			} else {
				trustEntity.setPurchaseAcount(trustEntity.getPurchaseAcount().add(order.getInvestVolume()).setScale(4, BigDecimal.ROUND_HALF_UP));
			}
			trustEntity.setInvestDate(this.getInterestDate());
			trustEntity.setInterestDate(this.getInterestDate());

			trustService.save(trustEntity);

			investService.incHoldAmount(trustEntity.getTarget().getOid(), order.getInvestVolume());
			if (!order.getInvestVolume().equals(temp)) {
				BigDecimal x = order.getInvestVolume().subtract(temp);
				investService.subRestTrustAmount(order.getTarget().getOid(), x);
			}

			if (Investment.INVESTMENT_LIFESTATUS_STAND_UP.equals(trustEntity.getTarget().getLifeState())) {
				targetService.repayMentSchedule(trustEntity.getTarget(), trustEntity);
			}
			order.setState(CONFIRM31);
		} else {
			investCash = BigDecimalUtil.init0;
			investVolume = BigDecimalUtil.init0;
			investService.incRestTrustAmount(order.getTarget().getOid(), temp);
			order.setState(CONFIRM30);
		}

		trustService.save(order);
		investService.incApplyAmount(order.getTarget().getOid(), temp.negate());

		TrustAuditEntity entity = new TrustAuditEntity();
		entity.setOid(StringUtil.uuid());
		entity.setOrderOid(form.getOid());
		entity.setAuditType(TrustAuditEntity.TYPE_CONFIRM + "-Purchase");
		entity.setAuditState(form.getState());
		entity.setAuditor(uid);
		entity.setAuditTime(DateUtil.getSqlCurrentDate());

		trustService.save(entity);

		// 资金变动记录
		// BigDecimal account = order.getReserveCash().compareTo(BigDecimal.ZERO) == 0 ?
		// order.getAuditCash().compareTo(BigDecimal.ZERO) == 0 ? order.getApplyCash() : order.getAuditCash()
		// : order.getReserveCash();
		capitalService.capitalFlow(order.getAssetPoolOid(), order.getTarget().getOid(), order.getOid(), TRUST, investCash, tempCash, PURCHASE, CONFIRM, uid, form.getState());
	}

	/**
	 * 信托（计划）转入申购
	 * 
	 * @param from
	 * @param uid
	 */
	@Transactional
	public void purchaseForTrans(TransForm form, String uid) {
		BigDecimal applyCash = BigDecimalUtil.formatForMul10000(form.getApplyCash());
		BigDecimal applyVolume = BigDecimalUtil.formatForMul10000(form.getApplyVolume());
		TrustOrderEntity order = new TrustOrderEntity();

		order.setOid(StringUtil.uuid());
		Investment target = targetService.getInvestmentByOid(form.getT_targetOid());
		order.setTarget(target);
		order.setAssetPoolOid(form.getAssetPoolOid());
		// order.setInvestDate(form.getInvestDate());
		order.setApplyVolume(applyVolume);
		order.setApplyCash(applyCash);
		order.setProfitType(form.getProfitType());
		order.setType(TrustOrderEntity.TYPE_TRANSFER);
		order.setState(APPLY00);
		order.setAsker(uid);
		order.setCreateTime(DateUtil.getSqlCurrentDate());

		trustService.save(order);

		investService.incApplyAmount(form.getT_targetOid(), applyVolume);
		investService.subRestTrustAmount(form.getT_targetOid(), applyVolume);

		// 资金变动记录
		capitalService.capitalFlow(form.getAssetPoolOid(), form.getT_targetOid(), order.getOid(), TRUST, applyCash, BigDecimal.ZERO, TRANS, APPLY, uid, null);
	}

	/**
	 * 信托（计划）转入审核
	 * 
	 * @param form
	 * @param uid
	 */
	@Transactional
	public void auditForTrans(TrustForm form, String uid) {
		TrustOrderEntity order = trustService.getTrustOrderByOid(form.getOid());
		if (null == order) {
			throw new AMPException("无效的订单查询，请确认订单id！");
		} else if (!APPLY00.equals(order.getState())) {
			throw new AMPException("该订单已受理, 不可重复操作！");
		}
		BigDecimal auditCash = BigDecimalUtil.formatForMul10000(form.getAuditCash());
		BigDecimal auditVolume = BigDecimalUtil.formatForMul10000(form.getAuditVolume());
		if (TrustAuditEntity.SUCCESSED.equals(form.getState()))
			order.setState(AUDIT11);
		else {
			auditCash = BigDecimalUtil.init0;
			auditVolume = BigDecimalUtil.init0;
			order.setState(AUDIT10);
		}
		// 录入申购订单的审核信息
		order.setAuditVolume(auditVolume.equals(BigDecimalUtil.init0) ? auditCash : auditVolume);
		order.setAuditCash(auditCash);
		order.setAuditor(uid);
		order.setUpdateTime(DateUtil.getSqlCurrentDate());

		trustService.save(order);

		BigDecimal temp = order.getAuditVolume().subtract(order.getApplyVolume()).setScale(4, BigDecimal.ROUND_HALF_UP);
		if (temp.compareTo(BigDecimal.ZERO) != 0) {
			investService.incApplyAmount(order.getTarget().getOid(), temp);
			investService.subRestTrustAmount(order.getTarget().getOid(), temp);
		}

		TrustAuditEntity entity = new TrustAuditEntity();
		entity.setOid(StringUtil.uuid());
		entity.setOrderOid(form.getOid());
		entity.setAuditType(TrustAuditEntity.TYPE_AUDIT + "-Trans");
		entity.setAuditState(form.getState());
		entity.setAuditor(uid);
		entity.setAuditTime(DateUtil.getSqlCurrentDate());

		trustService.save(entity);

		// 资金变动记录
		capitalService.capitalFlow(order.getAssetPoolOid(), order.getTarget().getOid(), order.getOid(), TRUST, auditCash, order.getApplyCash(), TRANS, AUDIT, uid, form.getState());
	}

	/**
	 * 信托（计划）转入预约
	 * 
	 * @param form
	 * @param uid
	 */
	@Transactional
	public void appointmentForTrans(TrustForm form, String uid) {
		TrustOrderEntity order = trustService.getTrustOrderByOid(form.getOid());
		if (null == order) {
			throw new AMPException("无效的订单查询，请确认订单id！");
		} else if (APPOINTMENT21.equals(order.getState()) || APPOINTMENT20.equals(order.getState()) || CONFIRM31.equals(order.getState()) || CONFIRM30.equals(order.getState())
				|| INVALID.equals(order.getState())) {
			throw new AMPException("该订单已受理, 不可重复操作！");
		}
		BigDecimal reserveCash = BigDecimalUtil.formatForMul10000(form.getReserveCash());
		BigDecimal reserveVolume = BigDecimalUtil.formatForMul10000(form.getReserveVolume());

		// 记录订单上一步操作状态
		String state = order.getState();

		if (TrustAuditEntity.SUCCESSED.equals(form.getState()))
			order.setState(APPOINTMENT21);
		else {
			reserveCash = BigDecimalUtil.init0;
			reserveVolume = BigDecimalUtil.init0;
			order.setState(APPOINTMENT20);
		}
		// 录入申购订单的资金预约信息
		order.setReserveVolume(reserveVolume.equals(BigDecimalUtil.init0) ? reserveCash : reserveVolume);
		order.setReserveCash(reserveCash);
		order.setReserver(uid);
		order.setUpdateTime(DateUtil.getSqlCurrentDate());

		trustService.save(order);

		// BigDecimal temp = order.getReserveVolume().subtract(
		// order.getAuditVolume().compareTo(BigDecimal.ZERO) == 0 ? order.getApplyVolume() : order.getAuditVolume())
		// .setScale(4, BigDecimal.ROUND_HALF_UP);
		BigDecimal temp = BigDecimalUtil.init0;
		BigDecimal tempSub = BigDecimalUtil.init0;
		if (AUDIT11.equals(state)) {
			temp = order.getAuditCash();
			tempSub = order.getReserveVolume().subtract(order.getAuditVolume()).setScale(4, BigDecimal.ROUND_HALF_UP);
		} else if (APPLY00.equals(state)) {
			temp = order.getApplyCash();
			tempSub = order.getReserveVolume().subtract(order.getApplyVolume()).setScale(4, BigDecimal.ROUND_HALF_UP);
		}
		if (temp.compareTo(BigDecimal.ZERO) != 0) {
			investService.incApplyAmount(order.getTarget().getOid(), tempSub);
			investService.subRestTrustAmount(order.getTarget().getOid(), tempSub);
		}

		TrustAuditEntity entity = new TrustAuditEntity();
		entity.setOid(StringUtil.uuid());
		entity.setOrderOid(form.getOid());
		entity.setAuditType(TrustAuditEntity.TYPE_APPOINTMENT + "-Trans");
		entity.setAuditState(form.getState());
		entity.setAuditor(uid);
		entity.setAuditTime(DateUtil.getSqlCurrentDate());

		trustService.save(entity);

		// 资金变动记录
		// BigDecimal account = order.getAuditCash().compareTo(BigDecimal.ZERO) == 0 ? order.getApplyCash() : order.getAuditCash();
		capitalService.capitalFlow(order.getAssetPoolOid(), order.getTarget().getOid(), order.getOid(), TRUST, reserveCash, temp, TRANS, APPOINTMENT, uid, form.getState());
	}

	/**
	 * 信托（计划）转入确认
	 * 
	 * @param form
	 * @param uid
	 */
	@Transactional
	public void orderConfirmForTrans(TrustForm form, String uid) {
		TrustOrderEntity order = trustService.getTrustOrderByOid(form.getOid());
		if (null == order) {
			throw new AMPException("无效的订单查询，请确认订单id！");
		} else if (CONFIRM31.equals(order.getState()) || CONFIRM30.equals(order.getState()) || INVALID.equals(order.getState())) {
			throw new AMPException("该订单已受理, 不可重复操作！");
		}
		BigDecimal investCash = BigDecimalUtil.formatForMul10000(form.getInvestCash());
		BigDecimal investVolume = BigDecimalUtil.formatForMul10000(form.getInvestVolume());

		// BigDecimal temp = order.getReserveVolume().compareTo(BigDecimal.ZERO) == 0 ?
		// order.getAuditVolume().compareTo(BigDecimal.ZERO) == 0 ? order.getApplyVolume() : order.getAuditVolume()
		// : order.getReserveVolume();
		BigDecimal temp = BigDecimalUtil.init0;
		BigDecimal tempCash = BigDecimalUtil.init0;
		if (APPOINTMENT21.equals(order.getState())) {
			temp = order.getReserveVolume();
			tempCash = order.getReserveCash();
		} else if (AUDIT11.equals(order.getState())) {
			temp = order.getAuditVolume();
			tempCash = order.getAuditCash();
		} else if (APPLY00.equals(order.getState())) {
			temp = order.getApplyVolume();
			tempCash = order.getApplyCash();
		}
		if (TrustAuditEntity.SUCCESSED.equals(form.getState())) {
			order.setConfirmDate(new Date(System.currentTimeMillis()));
			Timestamp time = new Timestamp(System.currentTimeMillis());
			order.setInvestTime(time);
			// 录入申购订单的确认信息
			order.setInvestVolume(investVolume.equals(BigDecimalUtil.init0) ? investCash : investVolume);
			order.setInvestCash(investCash);
			order.setConfirmer(uid);
			order.setUpdateTime(DateUtil.getSqlCurrentDate());

			TrustEntity trustEntity = trustService.getFundByPidAndTargetOid(order.getAssetPoolOid(), order.getTarget().getOid(), order.getProfitType());
			if (null != trustEntity && trustEntity.getProfitType().equals(order.getProfitType())) {
				trustEntity.setInvestVolume(trustEntity.getInvestVolume().add(order.getInvestVolume()).setScale(4, BigDecimal.ROUND_HALF_UP));
				trustEntity.setUpdateTime(time);
			} else {
				trustEntity = new TrustEntity();
				trustEntity.setOid(StringUtil.uuid());
				trustEntity.setTarget(order.getTarget());
				trustEntity.setOrderOid(order.getOid());
				trustEntity.setAssetPoolOid(order.getAssetPoolOid());
				trustEntity.setPurchase(PURCHASE);
				trustEntity.setState(TrustEntity.INVESTING);
				trustEntity.setApplyVolume(order.getInvestVolume());
				trustEntity.setApplyCash(order.getInvestCash());
				trustEntity.setConfirmVolume(order.getInvestVolume());
				trustEntity.setInvestVolume(order.getInvestCash());
				// trustEntity.setInvestDate(order.getInvestDate());
				trustEntity.setProfitType(order.getProfitType());
				trustEntity.setCreateTime(time);
				trustEntity.setUpdateTime(time);
			}
			if (this.dateFalg()) {
				trustEntity.setInterestAcount(trustEntity.getInterestAcount().add(order.getInvestVolume()).setScale(4, BigDecimal.ROUND_HALF_UP));
			} else {
				trustEntity.setPurchaseAcount(trustEntity.getPurchaseAcount().add(order.getInvestVolume()).setScale(4, BigDecimal.ROUND_HALF_UP));
			}
			trustEntity.setInvestDate(this.getInterestDate());
			trustEntity.setInterestDate(this.getInterestDate());

			trustService.save(trustEntity);

			investService.incHoldAmount(trustEntity.getTarget().getOid(), order.getInvestVolume());

			if (Investment.INVESTMENT_LIFESTATUS_STAND_UP.equals(trustEntity.getTarget().getLifeState())) {
				targetService.repayMentSchedule(trustEntity.getTarget(), trustEntity);
			}
			order.setState(CONFIRM31);
			if (temp.compareTo(investVolume) != 0) {
				investService.incRestTrustAmount(order.getTarget().getOid(), investVolume.subtract(temp).negate());
			}

		} else {
			investService.incRestTrustAmount(order.getTarget().getOid(), temp);
			investCash = BigDecimalUtil.init0;
			investVolume = BigDecimalUtil.init0;
			order.setState(CONFIRM30);
		}

		trustService.save(order);
		investService.incApplyAmount(order.getTarget().getOid(), temp.negate());

		TrustAuditEntity entity = new TrustAuditEntity();
		entity.setOid(StringUtil.uuid());
		entity.setOrderOid(form.getOid());
		entity.setAuditType(TrustAuditEntity.TYPE_CONFIRM + "-Trans");
		entity.setAuditState(form.getState());
		entity.setAuditor(uid);
		entity.setAuditTime(DateUtil.getSqlCurrentDate());

		trustService.save(entity);

		// 资金变动记录
		// BigDecimal account = order.getReserveCash().compareTo(BigDecimal.ZERO) == 0 ?
		// order.getAuditCash().compareTo(BigDecimal.ZERO) == 0 ? order.getApplyCash() : order.getAuditCash()
		// : order.getReserveCash();
		capitalService.capitalFlow(order.getAssetPoolOid(), order.getTarget().getOid(), order.getOid(), TRUST, investCash, tempCash, TRANS, CONFIRM, uid, form.getState());
	}

	/**
	 * 信托（计划）本息兑付订单
	 * 
	 * @param form
	 * @param uid
	 */
	@Transactional
	public void applyForIncome(TrustForm form, String uid) {
		TrustEntity trustEntity = trustService.getTrustByOid(form.getOid());
		if (null == trustEntity) {
			throw new AMPException("无效的操作，请确认订单id！");
		}
		BigDecimal income = BigDecimalUtil.formatForMul10000(form.getIncome());
		BigDecimal incomeRate = BigDecimalUtil.formatForDivide100(form.getIncomeRate());

		TrustIncomeEntity order = new TrustIncomeEntity();
		BigDecimal capital = BigDecimal.ZERO;

		order.setCapital(trustEntity.getInvestVolume());
		capital = order.getCapital();
		trustEntity.setTransOutVolume(trustEntity.getTransOutVolume().add(capital).setScale(4, BigDecimal.ROUND_HALF_UP));
		trustEntity.setTransOutFee(trustEntity.getTransOutFee().add(capital).setScale(4, BigDecimal.ROUND_HALF_UP));
		trustEntity.setInvestVolume(trustEntity.getInvestVolume().subtract(capital).setScale(4, BigDecimal.ROUND_HALF_UP));

		order.setOid(StringUtil.uuid());
		order.setTrustEntity(trustEntity);
		order.setSeq(form.getSeq());
		order.setState(APPLY00);
		order.setIncome(income);
		order.setIncomeRate(incomeRate);
		order.setIncomeDate(form.getIncomeDate());
		order.setAsker(uid);
		order.setCreateTime(DateUtil.getSqlCurrentDate());

		trustService.save(order);

		// 资金变动记录
		// capital = capital.add(income).setScale(4, BigDecimal.ROUND_HALF_UP);
		capital = income;
		capitalService.capitalFlow(trustEntity.getAssetPoolOid(), trustEntity.getTarget().getOid(), order.getOid(), TRUST, capital, BigDecimal.ZERO, INCOME, APPLY, uid, null);
	}

	/**
	 * 信托（计划）本息兑付审核
	 * 
	 * @param oid
	 *            标的oid
	 * @param uid
	 */
	@Transactional
	public void auditForIncome(TrustForm form, String uid) {
		TrustIncomeEntity order = trustService.getTrustIncomeOrderByOid(form.getOid());
		if (null == order) {
			throw new AMPException("无效的订单查询，请确认订单id！");
		} else if (!APPLY00.equals(order.getState())) {
			throw new AMPException("该订单已受理, 不可重复操作！");
		}
		BigDecimal auditIncome = BigDecimalUtil.formatForMul10000(form.getAuditCash());
		BigDecimal incomeRate = BigDecimalUtil.formatForDivide100(form.getAuditVolume());
		// BigDecimal auditAapital = BigDecimalUtil.formatForMul10000(form.getAuditCapital());
		TrustEntity trustEntity = order.getTrustEntity();
		BigDecimal capital = BigDecimal.ZERO;
		BigDecimal account = BigDecimal.ZERO;
		// 是否兑付本金
		// if (BigDecimal.ZERO.compareTo(order.getCapital()) < 0) {
		// capital = auditAapital;
		// account = order.getCapital();
		// }
		if (TrustAuditEntity.SUCCESSED.equals(form.getState()))
			order.setState(AUDIT12);
		else {
			auditIncome = BigDecimalUtil.init0;
			incomeRate = BigDecimalUtil.init0;
			trustEntity.setInvestVolume(order.getCapital());
			trustEntity.setTransOutVolume(trustEntity.getTransOutVolume().subtract(order.getCapital().setScale(4, BigDecimal.ROUND_HALF_UP)));
			trustEntity.setTransOutFee(trustEntity.getTransOutFee().subtract(order.getCapital().setScale(4, BigDecimal.ROUND_HALF_UP)));
			order.setState(AUDIT10);
		}
		// 录入申购订单的审核信息
		order.setAuditIncome(auditIncome);
		// order.setAuditCapital(auditAapital);
		order.setAuditIncomeRate(incomeRate);
		order.setAuditor(uid);
		order.setUpdateTime(DateUtil.getSqlCurrentDate());

		trustService.save(order);

		TrustAuditEntity entity = new TrustAuditEntity();
		entity.setOid(StringUtil.uuid());
		entity.setOrderOid(order.getTrustEntity().getOrderOid());
		entity.setAuditType(TrustAuditEntity.TYPE_AUDIT + "-Income");
		entity.setAuditState(form.getState());
		entity.setAuditor(uid);
		entity.setAuditTime(DateUtil.getSqlCurrentDate());

		trustService.save(entity);

		// 资金变动记录
		// capital = capital.add(auditAapital).setScale(4, BigDecimal.ROUND_HALF_UP);
		capital = auditIncome;
		// account = account.add(order.getIncome()).setScale(4, BigDecimal.ROUND_HALF_UP);
		account = order.getIncome();
		capitalService.capitalFlow(order.getTrustEntity().getAssetPoolOid(), order.getTrustEntity().getTarget().getOid(), order.getOid(), TRUST, capital, account, INCOME, AUDIT, uid, form.getState());
	}

	/**
	 * 信托（计划）本息兑付确认
	 * 
	 * @param oid
	 *            标的oid
	 * @param uid
	 */
	@Transactional
	public void orderConfirmForIncome(TrustForm form, String uid) {
		TrustIncomeEntity order = trustService.getTrustIncomeOrderByOid(form.getOid());
		if (null == order) {
			throw new AMPException("无效的订单查询，请确认订单id！");
		} else if (CONFIRM31.equals(order.getState()) || CONFIRM30.equals(order.getState()) || INVALID.equals(order.getState())) {
			throw new AMPException("该订单已受理, 不可重复操作！");
		}
		BigDecimal investIncome = BigDecimalUtil.formatForMul10000(form.getInvestCash());
		BigDecimal incomeRate = BigDecimalUtil.formatForDivide100(form.getInvestVolume());
		BigDecimal investCapital = BigDecimalUtil.formatForMul10000(form.getInvestCapital());
		TrustEntity trustEntity = order.getTrustEntity();
		BigDecimal capital = BigDecimal.ZERO;
		BigDecimal account = BigDecimal.ZERO;

		// 记录订单上一步操作状态
		String state = order.getState();

		if (TrustAuditEntity.SUCCESSED.equals(form.getState())) {
			investService.incHoldAmount(trustEntity.getTarget().getOid(), order.getCapital().negate());
			trustEntity.setInvestVolume(BigDecimal.ZERO);
			trustEntity.setTotalProfit(trustEntity.getTotalProfit().add(investIncome.subtract(order.getCapital())).setScale(4, BigDecimal.ROUND_HALF_UP));
			trustEntity.setState(TrustEntity.INVESTEND);
			trustEntity.setCreateTime(new Timestamp(System.currentTimeMillis()));
			trustEntity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
			order.setState(CONFIRM31);
		} else {
			investIncome = BigDecimalUtil.init0;
			investCapital = BigDecimalUtil.init0;
			incomeRate = BigDecimalUtil.init0;
			trustEntity.setInvestVolume(order.getCapital());
			order.setState(CONFIRM30);
		}
		trustEntity.setTransOutVolume(trustEntity.getTransOutVolume().subtract(order.getCapital().setScale(4, BigDecimal.ROUND_HALF_UP)));
		trustEntity.setTransOutFee(trustEntity.getTransOutFee().subtract(order.getCapital().setScale(4, BigDecimal.ROUND_HALF_UP)));
		// 录入申购订单的确认信息
		order.setInvestIncome(investIncome);
		order.setInvestCapital(investCapital);
		order.setInvestIncomeRate(incomeRate);
		order.setConfirmer(uid);
		order.setUpdateTime(DateUtil.getSqlCurrentDate());

		trustService.save(order);

		// 更新资产池-投资标的还款计划状态
		targetService.updateRepaymentSchedule(trustEntity.getOid(), order.getSeq());

		TrustAuditEntity entity = new TrustAuditEntity();
		entity.setOid(StringUtil.uuid());
		entity.setOrderOid(order.getTrustEntity().getOrderOid());
		entity.setAuditType(TrustAuditEntity.TYPE_CONFIRM + "-Income");
		entity.setAuditState(form.getState());
		entity.setAuditor(uid);
		entity.setAuditTime(DateUtil.getSqlCurrentDate());

		trustService.save(entity);

		// 资金变动记录
		// BigDecimal volume = order.getAuditIncome().compareTo(BigDecimal.ZERO) == 0 ? order.getIncome() : order.getAuditIncome();
		BigDecimal volume = BigDecimalUtil.init0;
		if (AUDIT12.equals(state)) {
			volume = order.getAuditIncome();
		} else if (APPLY00.equals(state)) {
			volume = order.getIncome();
		}
		// capital = capital.add(investIncome).setScale(4, BigDecimal.ROUND_HALF_UP);
		// account = account.add(volume).setScale(4, BigDecimal.ROUND_HALF_UP);
		capital = investIncome;
		account = volume;
		capitalService.capitalFlow(order.getTrustEntity().getAssetPoolOid(), order.getTrustEntity().getTarget().getOid(), order.getOid(), TRUST, capital, account, INCOME, CONFIRM, uid,
				form.getState());
	}

	/**
	 * 信托（计划）退款订单
	 * 
	 * @param form
	 * @param uid
	 */
	@Transactional
	public void applyForBack(TrustForm form, String uid) {
		TrustEntity trustEntity = trustService.getTrustByOid(form.getOid());
		if (null == trustEntity) {
			throw new AMPException("无效的操作，请确认订单id！");
		}
		BigDecimal income = BigDecimalUtil.formatForMul10000(form.getIncome());
		BigDecimal incomeRate = BigDecimalUtil.formatForDivide100(form.getIncomeRate());

		TrustIncomeEntity order = new TrustIncomeEntity();
		BigDecimal capital = BigDecimal.ZERO;
		order.setCapital(trustEntity.getInvestVolume());
		capital = order.getCapital();
		trustEntity.setTransOutVolume(trustEntity.getTransOutVolume().add(capital).setScale(4, BigDecimal.ROUND_HALF_UP));
		trustEntity.setTransOutFee(trustEntity.getTransOutFee().add(capital).setScale(4, BigDecimal.ROUND_HALF_UP));
		trustEntity.setInvestVolume(trustEntity.getInvestVolume().subtract(capital).setScale(4, BigDecimal.ROUND_HALF_UP));
		order.setOid(StringUtil.uuid());
		order.setTrustEntity(trustEntity);
		order.setSeq(form.getSeq());
		order.setState(APPLY00);
		order.setIncome(income);
		order.setIncomeRate(incomeRate);
		order.setIncomeDate(form.getIncomeDate());
		order.setAsker(uid);
		order.setCreateTime(DateUtil.getSqlCurrentDate());

		trustService.save(order);

		// 资金变动记录
		capital = income;
		capitalService.capitalFlow(trustEntity.getAssetPoolOid(), trustEntity.getTarget().getOid(), order.getOid(), TRUST, capital, BigDecimal.ZERO, BACK, APPLY, uid, null);
	}

	/**
	 * 信托（计划）退款审核
	 * 
	 * @param oid
	 *            标的oid
	 * @param uid
	 */
	@Transactional
	public void auditForBack(TrustForm form, String uid) {
		TrustIncomeEntity order = trustService.getTrustIncomeOrderByOid(form.getOid());
		if (null == order) {
			throw new AMPException("无效的订单查询，请确认订单id！");
		} else if (!APPLY00.equals(order.getState())) {
			throw new AMPException("该订单已受理, 不可重复操作！");
		}
		BigDecimal auditIncome = BigDecimalUtil.formatForMul10000(form.getAuditCash());
		BigDecimal incomeRate = BigDecimalUtil.formatForDivide100(form.getAuditVolume());
		TrustEntity trustEntity = order.getTrustEntity();
		BigDecimal capital = BigDecimal.ZERO;
		BigDecimal account = BigDecimal.ZERO;
		if (TrustAuditEntity.SUCCESSED.equals(form.getState()))
			order.setState(AUDIT12);
		else {
			auditIncome = BigDecimalUtil.init0;
			incomeRate = BigDecimalUtil.init0;
			trustEntity.setInvestVolume(order.getCapital());
			trustEntity.setTransOutVolume(trustEntity.getTransOutVolume().subtract(order.getCapital().setScale(4, BigDecimal.ROUND_HALF_UP)));
			trustEntity.setTransOutFee(trustEntity.getTransOutFee().subtract(order.getCapital().setScale(4, BigDecimal.ROUND_HALF_UP)));
			order.setState(AUDIT10);
		}
		trustEntity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
		// 录入申购订单的审核信息
		order.setAuditIncome(auditIncome);
		order.setAuditIncomeRate(incomeRate);
		order.setAuditor(uid);
		order.setUpdateTime(DateUtil.getSqlCurrentDate());

		trustService.save(order);

		TrustAuditEntity entity = new TrustAuditEntity();
		entity.setOid(StringUtil.uuid());
		entity.setOrderOid(order.getTrustEntity().getOrderOid());
		entity.setAuditType(TrustAuditEntity.TYPE_AUDIT + "-Back");
		entity.setAuditState(form.getState());
		entity.setAuditor(uid);
		entity.setAuditTime(DateUtil.getSqlCurrentDate());

		trustService.save(entity);

		// 资金变动记录
		capital = auditIncome;
		account = order.getIncome();
		capitalService.capitalFlow(order.getTrustEntity().getAssetPoolOid(), order.getTrustEntity().getTarget().getOid(), order.getOid(), TRUST, capital, account, BACK, AUDIT, uid, form.getState());
	}

	/**
	 * 信托（计划）退款确认
	 * 
	 * @param oid
	 *            标的oid
	 * @param uid
	 */
	@Transactional
	public void orderConfirmForBack(TrustForm form, String uid) {
		TrustIncomeEntity order = trustService.getTrustIncomeOrderByOid(form.getOid());
		if (null == order) {
			throw new AMPException("无效的订单查询，请确认订单id！");
		} else if (CONFIRM31.equals(order.getState()) || CONFIRM30.equals(order.getState()) || INVALID.equals(order.getState())) {
			throw new AMPException("该订单已受理, 不可重复操作！");
		}
		BigDecimal investIncome = BigDecimalUtil.formatForMul10000(form.getInvestCash());
		BigDecimal incomeRate = BigDecimalUtil.formatForDivide100(form.getInvestVolume());
		BigDecimal investCapital = BigDecimalUtil.formatForMul10000(form.getInvestCapital());
		TrustEntity trustEntity = order.getTrustEntity();
		BigDecimal capital = BigDecimal.ZERO;
		BigDecimal account = BigDecimal.ZERO;

		// 记录订单上一步操作状态
		String state = order.getState();

		if (TrustAuditEntity.SUCCESSED.equals(form.getState())) {
			investService.incHoldAmount(trustEntity.getTarget().getOid(), order.getCapital().negate());
			trustEntity.setInvestVolume(BigDecimal.ZERO);
			trustEntity.setTotalProfit(trustEntity.getTotalProfit().add(investIncome.subtract(order.getCapital())).setScale(4, BigDecimal.ROUND_HALF_UP));
			trustEntity.setState(TrustEntity.INVESTEND);
			trustEntity.setCreateTime(new Timestamp(System.currentTimeMillis()));
			trustEntity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
			order.setState(CONFIRM31);
		} else {
			trustEntity.setInvestVolume(order.getCapital());
			order.setState(CONFIRM30);
		}
		trustEntity.setTransOutVolume(BigDecimal.ZERO);
		trustEntity.setTransOutFee(BigDecimal.ZERO);
		// 录入申购订单的确认信息
		order.setInvestIncome(investIncome);
		order.setInvestCapital(investCapital);
		order.setInvestIncomeRate(incomeRate);
		order.setConfirmer(uid);
		order.setUpdateTime(DateUtil.getSqlCurrentDate());

		trustService.save(order);

		// 更新资产池-投资标的还款计划状态
		targetService.updateRepaymentSchedule(trustEntity.getOid(), order.getSeq());

		TrustAuditEntity entity = new TrustAuditEntity();
		entity.setOid(StringUtil.uuid());
		entity.setOrderOid(order.getTrustEntity().getOrderOid());
		entity.setAuditType(TrustAuditEntity.TYPE_CONFIRM + "-Back");
		entity.setAuditState(form.getState());
		entity.setAuditor(uid);
		entity.setAuditTime(DateUtil.getSqlCurrentDate());

		trustService.save(entity);

		// 资金变动记录
		// BigDecimal volume = order.getAuditIncome().compareTo(BigDecimal.ZERO) == 0 ? order.getIncome() : order.getAuditIncome();
		BigDecimal volume = BigDecimalUtil.init0;
		if (AUDIT12.equals(state)) {
			volume = order.getAuditIncome();
		} else if (APPLY00.equals(state)) {
			volume = order.getIncome();
		}
		capital = investIncome;
		account = volume;
		capitalService.capitalFlow(order.getTrustEntity().getAssetPoolOid(), order.getTrustEntity().getTarget().getOid(), order.getOid(), TRUST, capital, account, BACK, CONFIRM, uid, form.getState());
	}

	/**
	 * 信托（计划）转让
	 * 
	 * @param from
	 */
	@Transactional
	public void applyForTransfer(TrustForm form, String uid) {
		TrustEntity trustEntity = trustService.getTrustByOid(form.getOid());
		if (null == trustEntity) {
			throw new AMPException("无效的操作，请确认订单id！");
		}
		BigDecimal tranVolume = BigDecimalUtil.formatForMul10000(form.getTranVolume());
		BigDecimal tranCash = BigDecimalUtil.formatForMul10000(form.getTranCash());

		TrustTransEntity order = new TrustTransEntity();
		order.setOid(StringUtil.uuid());
		// order.setTranDate(form.getTranDate());
		order.setTranVolume(tranVolume);
		order.setTranCash(tranCash);
		trustEntity.setTransOutVolume(trustEntity.getTransOutVolume().add(tranVolume).setScale(4, BigDecimal.ROUND_HALF_UP));
		trustEntity.setTransOutFee(trustEntity.getTransOutFee().add(tranCash).setScale(4, BigDecimal.ROUND_HALF_UP));
		trustEntity.setInvestVolume(trustEntity.getInvestVolume().subtract(tranVolume).setScale(4, BigDecimal.ROUND_HALF_UP));
		trustEntity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
		trustService.save(trustEntity);

		order.setTrustEntity(trustEntity);
		order.setState(APPLY00);
		order.setCreater(uid);
		order.setAsker(uid);
		order.setCreateTime(DateUtil.getSqlCurrentDate());
		order.setOverdueFlag(false);
		order.setType(form.getTransType());
		order.setTransferee(form.getTransferee());

		trustService.save(order);

		// 资金变动记录
		capitalService.capitalFlow(trustEntity.getAssetPoolOid(), trustEntity.getTarget().getOid(), order.getOid(), TRUST, tranCash, BigDecimal.ZERO, TRANSFER, APPLY, uid, null);
	}

	/**
	 * 信托（计划）转让审核
	 * 
	 * @param oid
	 *            标的oid
	 * @param uid
	 */
	@Transactional
	public void auditForTransfer(TrustForm form, String uid) {
		TrustTransEntity order = trustService.getTrustTransOrderByOid(form.getOid());
		if (null == order) {
			throw new AMPException("无效的订单查询，请确认订单id！");
		} else if (!APPLY00.equals(order.getState())) {
			throw new AMPException("该订单已受理, 不可重复操作！");
		}
		BigDecimal auditVolume = BigDecimalUtil.formatForMul10000(form.getAuditVolume());
		BigDecimal auditCash = BigDecimalUtil.formatForMul10000(form.getAuditCash());
		TrustEntity trustEntity = order.getTrustEntity();
		if (TrustAuditEntity.SUCCESSED.equals(form.getState())) {
			trustEntity.setInvestVolume(trustEntity.getInvestVolume().add(order.getTranVolume()).subtract(auditVolume).setScale(4, BigDecimal.ROUND_HALF_UP));
			trustEntity.setTransOutVolume(trustEntity.getTransOutVolume().subtract(order.getTranVolume()).add(auditVolume).setScale(4, BigDecimal.ROUND_HALF_UP));
			trustEntity.setTransOutFee(trustEntity.getTransOutFee().subtract(order.getTranCash()).add(auditCash).setScale(4, BigDecimal.ROUND_HALF_UP));
			order.setState(AUDIT12);
		} else {
			trustEntity.setInvestVolume(trustEntity.getInvestVolume().add(order.getTranVolume()).setScale(4, BigDecimal.ROUND_HALF_UP));
			trustEntity.setTransOutVolume(trustEntity.getTransOutVolume().subtract(order.getTranVolume()).setScale(4, BigDecimal.ROUND_HALF_UP));
			trustEntity.setTransOutFee(trustEntity.getTransOutFee().subtract(order.getTranCash()).setScale(4, BigDecimal.ROUND_HALF_UP));
			order.setState(AUDIT10);
		}
		trustEntity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
		// 录入申购订单的审核信息
		order.setAuditVolume(auditVolume);
		order.setAuditCash(auditCash);
		order.setAuditor(uid);
		order.setUpdateTime(DateUtil.getSqlCurrentDate());

		trustService.save(order);

		TrustAuditEntity entity = new TrustAuditEntity();
		entity.setOid(StringUtil.uuid());
		entity.setOrderOid(order.getTrustEntity().getOrderOid());
		entity.setAuditType(TrustAuditEntity.TYPE_AUDIT + "-Transfer");
		entity.setAuditState(form.getState());
		entity.setAuditor(uid);
		entity.setAuditTime(DateUtil.getSqlCurrentDate());

		trustService.save(entity);

		// 资金变动记录
		capitalService.capitalFlow(order.getTrustEntity().getAssetPoolOid(), order.getTrustEntity().getTarget().getOid(), order.getOid(), TRUST, auditCash, order.getTranCash(), TRANSFER, AUDIT, uid,
				form.getState());
	}

	/**
	 * 信托（计划）转让确认
	 * 
	 * @param oid
	 *            标的oid
	 * @param uid
	 */
	@Transactional
	public void orderConfirmForTransfer(TrustForm form, String uid) {
		TrustTransEntity order = trustService.getTrustTransOrderByOid(form.getOid());
		if (null == order) {
			throw new AMPException("无效的订单查询，请确认订单id！");
		} else if (CONFIRM31.equals(order.getState()) || CONFIRM30.equals(order.getState()) || INVALID.equals(order.getState())) {
			throw new AMPException("该订单已受理, 不可重复操作！");
		}
		BigDecimal investVolume = BigDecimalUtil.formatForMul10000(form.getInvestVolume());
		BigDecimal investCash = BigDecimalUtil.formatForMul10000(form.getInvestCash());
		// 净值，用于操作溢价或折价转让时，修改资产池的估值数据
		// BigDecimal netVolume = investCash.subtract(investVolume).setScale(4, BigDecimal.ROUND_HALF_UP);

		TrustEntity trustEntity = order.getTrustEntity();
		// BigDecimal tempVolume = order.getAuditVolume().compareTo(BigDecimalUtil.init0) == 0 ?
		// order.getTranVolume() : order.getAuditVolume();
		// BigDecimal tempCash = order.getAuditCash().compareTo(BigDecimalUtil.init0) == 0 ?
		// order.getTranCash() : order.getAuditCash();
		BigDecimal tempVolume = BigDecimalUtil.init0;
		BigDecimal tempCash = BigDecimalUtil.init0;
		if (AUDIT12.equals(order.getState())) {
			tempVolume = order.getAuditVolume();
			tempCash = order.getAuditCash();
		} else if (APPLY00.equals(order.getState())) {
			tempVolume = order.getTranVolume();
			tempCash = order.getTranCash();
		}
		if (TrustAuditEntity.SUCCESSED.equals(form.getState())) {
			Timestamp time = new Timestamp(System.currentTimeMillis());
			order.setTranTime(time);
			trustEntity.setInvestVolume(trustEntity.getInvestVolume().add(tempVolume).subtract(investVolume).setScale(4, BigDecimal.ROUND_HALF_UP));
			trustEntity.setTransOutVolume(trustEntity.getTransOutVolume().subtract(tempVolume).setScale(4, BigDecimal.ROUND_HALF_UP));
			trustEntity.setTransOutFee(trustEntity.getTransOutFee().subtract(tempCash).setScale(4, BigDecimal.ROUND_HALF_UP));
			trustEntity.setUpdateTime(time);
			if (this.dateFalg()) {
				trustEntity.setInterestAcount(trustEntity.getInterestAcount().subtract(investVolume).setScale(4, BigDecimal.ROUND_HALF_UP));
			} else {
				trustEntity.setRedeemAcount(trustEntity.getPurchaseAcount().add(investVolume).setScale(4, BigDecimal.ROUND_HALF_UP));
			}
			trustEntity.setInvestDate(this.getInterestDate());

			investService.incHoldAmount(trustEntity.getTarget().getOid(), investVolume.negate());
			// 获取转让部分的标的理论市值
			BigDecimal targetValuation = trustService.calcTargetValuation(trustEntity, investVolume, new java.util.Date());
			BigDecimal nav = investCash.subtract(targetValuation).setScale(4, BigDecimal.ROUND_HALF_UP);
			// 重新计算资产池投资占比
			AssetPoolEntity poolEntity = assetPoolService.getByOid(trustEntity.getAssetPoolOid());
			assetPoolService.reCalcTargetRate(poolEntity, investVolume, nav, TrustEntity.TRUST);
			order.setState(CONFIRM31);
		} else {
			investVolume = BigDecimalUtil.init0;
			investCash = BigDecimalUtil.init0;
			trustEntity.setInvestVolume(trustEntity.getInvestVolume().add(tempVolume).setScale(4, BigDecimal.ROUND_HALF_UP));
			trustEntity.setTransOutVolume(trustEntity.getTransOutVolume().subtract(tempVolume).setScale(4, BigDecimal.ROUND_HALF_UP));
			trustEntity.setTransOutFee(trustEntity.getTransOutFee().subtract(tempCash).setScale(4, BigDecimal.ROUND_HALF_UP));
			trustEntity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
			order.setState(CONFIRM30);
		}

		// 录入申购订单的确认信息
		order.setInvestVolume(investVolume);
		order.setInvestCash(investCash);
		order.setConfirmer(uid);
		order.setUpdateTime(DateUtil.getSqlCurrentDate());

		trustService.save(order);

		TrustAuditEntity entity = new TrustAuditEntity();
		entity.setOid(StringUtil.uuid());
		entity.setOrderOid(order.getTrustEntity().getOrderOid());
		entity.setAuditType(TrustAuditEntity.TYPE_CONFIRM + "-Transfer");
		entity.setAuditState(form.getState());
		entity.setAuditor(uid);
		entity.setAuditTime(DateUtil.getSqlCurrentDate());

		trustService.save(entity);

		// 资金变动记录
		// BigDecimal account = order.getAuditCash().compareTo(BigDecimal.ZERO) == 0 ? order.getTranCash() : order.getAuditCash();
		capitalService.capitalFlow(order.getTrustEntity().getAssetPoolOid(), order.getTrustEntity().getTarget().getOid(), order.getOid(), TRUST, investCash, tempCash, TRANSFER, CONFIRM, uid,
				form.getState());
	}

	/**
	 * 信托（计划）逾期转让
	 * 
	 * @param from
	 */
	@Transactional
	public void applyForOverdueTransfer(TrustForm form, String uid) {
		TrustEntity trustEntity = trustService.getTrustByOid(form.getOid());
		if (null == trustEntity) {
			throw new AMPException("无效的操作，请确认订单id！");
		}
		BigDecimal tranVolume = BigDecimalUtil.formatForMul10000(form.getTranVolume());
		BigDecimal tranCash = BigDecimalUtil.formatForMul10000(form.getTranCash());

		TrustTransEntity order = new TrustTransEntity();
		order.setOid(StringUtil.uuid());
		order.setTranDate(form.getTranDate());
		order.setTranVolume(tranVolume);
		order.setTranCash(tranCash);
		trustEntity.setTransOutVolume(trustEntity.getTransOutVolume().add(tranVolume).setScale(4, BigDecimal.ROUND_HALF_UP));
		trustEntity.setTransOutFee(trustEntity.getTransOutFee().add(tranCash).setScale(4, BigDecimal.ROUND_HALF_UP));
		trustEntity.setInvestVolume(trustEntity.getInvestVolume().subtract(tranVolume).setScale(4, BigDecimal.ROUND_HALF_UP));
		trustEntity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
		trustService.save(trustEntity);

		order.setTrustEntity(trustEntity);
		order.setState(APPLY00);
		order.setCreater(uid);
		order.setAsker(uid);
		order.setCreateTime(DateUtil.getSqlCurrentDate());
		order.setOverdueFlag(true);
		order.setType(form.getTransType());
		order.setTransferee(form.getTransferee());

		trustService.save(order);

		// 资金变动记录
		capitalService.capitalFlow(trustEntity.getAssetPoolOid(), trustEntity.getTarget().getOid(), order.getOid(), TRUST, tranCash, BigDecimal.ZERO, OVERDUETRANSFER, APPLY, uid, null);
	}

	/**
	 * 信托（计划）逾期转让审核
	 * 
	 * @param oid
	 *            标的oid
	 * @param uid
	 */
	@Transactional
	public void auditForOverdueTransfer(TrustForm form, String uid) {
		TrustTransEntity order = trustService.getTrustTransOrderByOid(form.getOid());
		if (null == order) {
			throw new AMPException("无效的订单查询，请确认订单id！");
		} else if (!APPLY00.equals(order.getState())) {
			throw new AMPException("该订单已受理, 不可重复操作！");
		}
		BigDecimal auditVolume = BigDecimalUtil.formatForMul10000(form.getAuditVolume());
		BigDecimal auditCash = BigDecimalUtil.formatForMul10000(form.getAuditCash());
		TrustEntity trustEntity = order.getTrustEntity();
		if (TrustAuditEntity.SUCCESSED.equals(form.getState())) {
			// trustEntity.setInvestVolume(trustEntity.getInvestVolume()
			// .add(order.getTranVolume())
			// .subtract(auditVolume)
			// .setScale(4, BigDecimal.ROUND_HALF_UP));
			// trustEntity.setTransOutVolume(trustEntity.getTransOutVolume()
			// .subtract(order.getTranVolume())
			// .add(auditVolume)
			// .setScale(4, BigDecimal.ROUND_HALF_UP));
			trustEntity.setTransOutFee(trustEntity.getTransOutFee().subtract(order.getTranCash()).add(auditCash).setScale(4, BigDecimal.ROUND_HALF_UP));
			order.setState(AUDIT12);
		} else {
			auditVolume = BigDecimalUtil.init0;
			auditCash = BigDecimalUtil.init0;
			trustEntity.setInvestVolume(trustEntity.getInvestVolume().add(order.getTranVolume()).setScale(4, BigDecimal.ROUND_HALF_UP));
			trustEntity.setTransOutVolume(trustEntity.getTransOutVolume().subtract(order.getTranVolume()).setScale(4, BigDecimal.ROUND_HALF_UP));
			trustEntity.setTransOutFee(trustEntity.getTransOutFee().subtract(order.getTranCash()).setScale(4, BigDecimal.ROUND_HALF_UP));
			order.setState(AUDIT10);
		}
		trustEntity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
		// 录入申购订单的审核信息
		order.setAuditVolume(auditVolume);
		order.setAuditCash(auditCash);
		order.setAuditor(uid);
		order.setUpdateTime(DateUtil.getSqlCurrentDate());

		trustService.save(order);

		TrustAuditEntity entity = new TrustAuditEntity();
		entity.setOid(StringUtil.uuid());
		entity.setOrderOid(order.getTrustEntity().getOrderOid());
		entity.setAuditType(TrustAuditEntity.TYPE_AUDIT + "-OverdueTransfer");
		entity.setAuditState(form.getState());
		entity.setAuditor(uid);
		entity.setAuditTime(DateUtil.getSqlCurrentDate());

		trustService.save(entity);

		// 资金变动记录
		capitalService.capitalFlow(order.getTrustEntity().getAssetPoolOid(), order.getTrustEntity().getTarget().getOid(), order.getOid(), TRUST, auditCash, order.getTranCash(), OVERDUETRANSFER, AUDIT,
				uid, form.getState());
	}

	/**
	 * 信托（计划）逾期转让确认
	 * 
	 * @param oid
	 *            标的oid
	 * @param uid
	 */
	@Transactional
	public void orderConfirmForOverdueTransfer(TrustForm form, String uid) {
		TrustTransEntity order = trustService.getTrustTransOrderByOid(form.getOid());
		if (null == order) {
			throw new AMPException("无效的订单查询，请确认订单id！");
		} else if (CONFIRM31.equals(order.getState()) || CONFIRM30.equals(order.getState()) || INVALID.equals(order.getState())) {
			throw new AMPException("该订单已受理, 不可重复操作！");
		}
		BigDecimal investVolume = BigDecimalUtil.formatForMul10000(form.getInvestVolume());
		BigDecimal investCash = BigDecimalUtil.formatForMul10000(form.getInvestCash());
		// 净值，用于操作溢价或折价转让时，修改资产池的估值数据
		// BigDecimal netVolume = investCash.subtract(investVolume).setScale(4, BigDecimal.ROUND_HALF_UP);

		TrustEntity trustEntity = order.getTrustEntity();
		// BigDecimal tempVolume = order.getTranVolume();
		// BigDecimal tempCash = order.getAuditCash().compareTo(BigDecimalUtil.init0) == 0 ?
		// order.getTranCash() : order.getAuditCash();
		BigDecimal tempVolume = BigDecimalUtil.init0;
		BigDecimal tempCash = BigDecimalUtil.init0;
		if (AUDIT12.equals(order.getState())) {
			tempVolume = order.getAuditVolume();
			tempCash = order.getAuditCash();
		} else if (APPLY00.equals(order.getState())) {
			tempVolume = order.getTranVolume();
			tempCash = order.getTranCash();
		}
		if (TrustAuditEntity.SUCCESSED.equals(form.getState())) {
			trustEntity.setInvestVolume(BigDecimalUtil.init0);
			trustEntity.setTransOutVolume(trustEntity.getTransOutVolume().subtract(tempVolume).setScale(4, BigDecimal.ROUND_HALF_UP));
			trustEntity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
			trustEntity.setState(TrustEntity.INVESTEND);

			investService.incHoldAmount(trustEntity.getTarget().getOid(), investVolume.negate());
			// 获取转让部分的标的理论市值
			BigDecimal targetValuation = trustService.calcTargetValuation(trustEntity, tempVolume, new java.util.Date());
			BigDecimal nav = investCash.subtract(targetValuation).setScale(4, BigDecimal.ROUND_HALF_UP);
			// 重新计算资产池投资占比
			AssetPoolEntity poolEntity = assetPoolService.getByOid(trustEntity.getAssetPoolOid());
			assetPoolService.reCalcTargetRate(poolEntity, tempVolume, nav, TrustEntity.TRUST);
			order.setState(CONFIRM31);
		} else {
			investVolume = BigDecimalUtil.init0;
			investCash = BigDecimalUtil.init0;
			trustEntity.setInvestVolume(trustEntity.getInvestVolume().add(tempVolume).setScale(4, BigDecimal.ROUND_HALF_UP));
			trustEntity.setTransOutVolume(trustEntity.getTransOutVolume().subtract(tempVolume).setScale(4, BigDecimal.ROUND_HALF_UP));
			trustEntity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
			order.setState(CONFIRM30);
		}
		trustEntity.setTransOutFee(trustEntity.getTransOutFee().subtract(tempCash).setScale(4, BigDecimal.ROUND_HALF_UP));
		// 录入申购订单的确认信息
		order.setInvestVolume(investVolume);
		order.setInvestCash(investCash);
		order.setConfirmer(uid);
		order.setUpdateTime(DateUtil.getSqlCurrentDate());

		trustService.save(order);

		TrustAuditEntity entity = new TrustAuditEntity();
		entity.setOid(StringUtil.uuid());
		entity.setOrderOid(order.getTrustEntity().getOrderOid());
		entity.setAuditType(TrustAuditEntity.TYPE_CONFIRM + "-OverdueTransfer");
		entity.setAuditState(form.getState());
		entity.setAuditor(uid);
		entity.setAuditTime(DateUtil.getSqlCurrentDate());

		trustService.save(entity);

		// 资金变动记录
		// BigDecimal account = order.getAuditCash().compareTo(BigDecimal.ZERO) == 0 ? order.getTranCash() : order.getAuditCash();
		capitalService.capitalFlow(order.getTrustEntity().getAssetPoolOid(), order.getTrustEntity().getTarget().getOid(), order.getOid(), TRUST, investCash, tempCash, OVERDUETRANSFER, CONFIRM, uid,
				form.getState());
	}

	/**
	 * 根据订单oid获取 申购 的货币基金（现金管理工具）订单
	 * 
	 * @param oid
	 * @return
	 */
	@Transactional
	public TrustOrderEntity getTrustPurchaseOrderByOid(String oid) {
		TrustOrderEntity entity = trustService.getTrustOrderByOid(oid);

		return entity;
	}

	/**
	 * 根据订单oid获取 本息兑付 的货币基金（现金管理工具）订单
	 * 
	 * @param oid
	 * @return
	 */
	@Transactional
	public TrustIncomeEntity getTargetIncomeOrderByOid(String oid) {
		TrustIncomeEntity entity = trustService.getTrustIncomeOrderByOid(oid);

		return entity;
	}

	/**
	 * 根据订单oid获取 转让 的货币基金（现金管理工具）订单
	 * 
	 * @param oid
	 * @return
	 */
	@Transactional
	public TrustTransEntity getTargetTransOrderByOid(String oid) {
		TrustTransEntity entity = trustService.getTrustTransOrderByOid(oid);

		return entity;
	}

	/**
	 * 获取现金管理类工具的持仓信息
	 * 
	 * @param oid
	 * @return
	 */
	@Transactional
	public FundForm getFundByOid(String oid) {
		FundForm form = new FundForm();
		FundEntity entity = fundService.getFundByOid(oid);
		if (null != entity) {
			try {
				BeanUtils.copyProperties(form, entity);
				CashTool cashTool = entity.getCashTool();
//				CashToolRevenue revenue = cashtoolPoolService.findCashtoolRevenue(cashTool.getOid(), new Date(System.currentTimeMillis()));
//				if (null == revenue)
//					revenue = new CashToolRevenue();
				form.setCashtoolOid(cashTool.getOid());
				form.setCashtoolName(cashTool.getSecShortName());
				form.setCashtoolType(cashTool.getEtfLof());
				form.setNetRevenue(cashTool.getDailyProfit());
				form.setYearYield7(cashTool.getWeeklyYield());
//				form.setNetRevenue(revenue.getDailyProfit());
//				form.setYearYield7(revenue.getWeeklyYield());
				form.setCirculationShares(cashTool.getCirculationShares());
				form.setRiskLevel(cashTool.getRiskLevel());
				form.setDividendType(cashTool.getDividendType());
				form.setIncomeDate(entity.getInterestDate());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return form;
	}

	/**
	 * 获取现金管理类工具的订单信息
	 * 
	 * @param oid
	 * @return
	 */
	@Transactional
	public FundForm getFundOrderByOid(String oid) {
		FundForm form = new FundForm();
		FundOrderEntity entity = fundService.getFundOrderByOid(oid);
		if (null != entity) {
			try {
				BeanUtils.copyProperties(form, entity);
				CashTool cashTool = entity.getFundEntity().getCashTool();
//				CashToolRevenue revenue = cashtoolPoolService.findCashtoolRevenue(cashTool.getOid(), new Date(System.currentTimeMillis()));
//				if (null == revenue)
//					revenue = new CashToolRevenue();
				form.setCashtoolOid(cashTool.getOid());
				form.setCashtoolName(cashTool.getSecShortName());
				form.setCashtoolType(cashTool.getEtfLof());
				form.setNetRevenue(cashTool.getDailyProfit());
				form.setYearYield7(cashTool.getWeeklyYield());
//				form.setNetRevenue(revenue.getDailyProfit());
//				form.setYearYield7(revenue.getWeeklyYield());
				form.setCirculationShares(cashTool.getCirculationShares());
				form.setRiskLevel(cashTool.getRiskLevel());
				form.setDividendType(cashTool.getDividendType());
				form.setApplyCash(entity.getVolume());
				form.setIncomeDate(entity.getFundEntity().getInterestDate());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return form;
	}

	/**
	 * 根据资产池id获取 预约中 的货币基金（现金管理工具）列表
	 * 
	 * @param pid
	 *            资产池id
	 * @return
	 */
	@Transactional
	public PageResp<FundForm> getFundListForAppointmentByPid(String pid, Pageable pageable) {
		List<FundForm> list = Lists.newArrayList();

		pid = assetPoolService.getPid(pid);
		PageResp<FundOrderEntity> entityList = fundService.findByPidForAppointment(pid, pageable);
		if (null != entityList && !entityList.getRows().isEmpty()) {
			List<String> idList = Lists.newArrayList();
			for (FundOrderEntity entity : entityList.getRows()) {
				idList.add(entity.getFundEntity().getCashTool().getOid());
			}
			// 获取数据采集的对象
//			Map<String, CashToolRevenue> map = cashtoolPoolService.findCashtoolRevenue(idList, new Date(System.currentTimeMillis()));
			// 设置万分收益
			FundForm form = null;
			for (FundOrderEntity entity : entityList.getRows()) {
				form = new FundForm();
				try {
					BeanUtils.copyProperties(form, entity);
					CashTool cashTool = entity.getFundEntity().getCashTool();
//					CashToolRevenue revenue = new CashToolRevenue();
//					if (null != map && map.containsKey(entity.getFundEntity().getCashTool().getOid()))
//						revenue = map.get(entity.getFundEntity().getCashTool().getOid());
					form.setCashtoolOid(cashTool.getOid());
					form.setCashtoolName(cashTool.getSecShortName());
					form.setCashtoolType(cashTool.getEtfLof());
					form.setNetRevenue(cashTool.getDailyProfit());
					form.setYearYield7(cashTool.getWeeklyYield());
//					form.setNetRevenue(revenue.getDailyProfit());
//					form.setYearYield7(revenue.getWeeklyYield());
					form.setCirculationShares(cashTool.getCirculationShares());
					form.setRiskLevel(cashTool.getRiskLevel());
					form.setDividendType(cashTool.getDividendType());
					if ("purchase".equals(entity.getOptType())) {
						form.setApplyCash(entity.getVolume());
						form.setInvestDate(entity.getInvestDate());
					} else {
						form.setApplyCash(entity.getReturnVolume());
						form.setInvestDate(entity.getRedeemDate());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				list.add(form);
			}
		}
		PageResp<FundForm> resp = new PageResp<FundForm>();
		resp.setTotal(entityList.getTotal());
		resp.setRows(list);

		return resp;
	}

	/**
	 * 根据资产池id获取 成立中 货币基金（现金管理工具）列表
	 * 
	 * @param pid
	 *            资产池id
	 * @return
	 */
	@Transactional
	public PageResp<FundForm> getFundListByPid(String pid, Pageable pageable) {
		PageResp<FundForm> rep = new PageResp<FundForm>();
		List<FundForm> formList = Lists.newArrayList();

		pid = assetPoolService.getPid(pid);
		Page<FundEntity> list = fundService.findByPidForConfirm(pid, pageable);
		if (null != list && !list.getContent().isEmpty()) {
			List<String> idList = Lists.newArrayList();
			for (FundEntity entity : list.getContent()) {
				idList.add(entity.getCashTool().getOid());
			}
			// 昨天日期
//			java.util.Date date = DateUtil.lastDate(new java.util.Date(System.currentTimeMillis()));
			// 获取数据采集的对象
//			Map<String, CashToolRevenue> map = cashtoolPoolService.findCashtoolRevenue(idList, new Date(date.getTime()));
//			Map<String, CashToolRevenue> map = cashtoolPoolService.findCashtoolRevenue(idList, new Date(System.currentTimeMillis()));
			// 设置万分收益
			FundForm form = null;
			for (FundEntity entity : list.getContent()) {
				form = new FundForm();
				try {
					BeanUtils.copyProperties(form, entity);
					CashTool cashTool = entity.getCashTool();
//					CashToolRevenue revenue = new CashToolRevenue();
//					if (null != map && map.containsKey(entity.getCashTool().getOid()))
//						revenue = map.get(entity.getCashTool().getOid());
					form.setCashtoolOid(cashTool.getOid());
					form.setCashtoolName(cashTool.getSecShortName());
					form.setCashtoolType(cashTool.getEtfLof());
					form.setNetRevenue(cashTool.getDailyProfit());
					form.setYearYield7(cashTool.getWeeklyYield());
//					form.setNetRevenue(revenue.getDailyProfit());
//					form.setYearYield7(revenue.getWeeklyYield());
					form.setCirculationShares(cashTool.getCirculationShares());
					form.setRiskLevel(cashTool.getRiskLevel());
					form.setDividendType(cashTool.getDividendType());
					form.setAmount(entity.getAmount());
					form.setIncomeDate(entity.getInterestDate());
				} catch (Exception e) {
					e.printStackTrace();
				}

				formList.add(form);
			}
			rep.setTotal(list.getTotalElements());
			rep.setRows(formList);
		}

		return rep;
	}

	/**
	 * 获取信托计划的持仓信息
	 * 
	 * @param oid
	 * @param type
	 *            类型：income（本息兑付）；transfer（转让）
	 * @return
	 */
	@Transactional
	public TrustForm getTrustByOid(String oid, String type) {
		TrustForm form = new TrustForm();
		TrustEntity entity = trustService.getTrustByOid(oid);
		if (null != entity) {
			try {
				BeanUtils.copyProperties(form, entity);
				Investment target = entity.getTarget();
				form.setTargetOid(target.getOid());
				form.setTargetName(target.getName());
				form.setTargetType(target.getType());
				form.setIncomeRate(target.getExpIncome());
				form.setExpAror(target.getExpAror());
				form.setSetDate(target.getSetDate());
				form.setArorFirstDate(target.getArorFirstDate());
				form.setAccrualDate(target.getAccrualDate());
				form.setContractDays(target.getContractDays());
				form.setLife(target.getLifed());
				form.setFloorVolume(target.getFloorVolume());
				form.setCollectEndDate(target.getCollectEndDate());
				form.setCollectStartDate(target.getCollectStartDate());
				form.setCollectIncomeRate(target.getCollectIncomeRate());
				form.setExpSetDate(target.getExpSetDate());
				form.setHoldAmount(entity.getInvestVolume());
				form.setTranVolume(entity.getTransOutVolume());
				form.setSubjectRating(target.getSubjectRating());
				form.setRaiseScope(target.getRaiseScope());
				form.setAccrualType(target.getAccrualType());
				form.setIncome(target.getExpIncome());
				form.setState(target.getState());
				form.setOverdueRate(target.getOverdueRate());
				form.setIncomeDate(entity.getInterestDate());

				// 本息兑付
				// if ("income".equals(type) && (Investment.INVESTMENT_LIFESTATUS_CLOSE.equals(target.getLifeState())
				// || Investment.INVESTMENT_LIFESTATUS_PAY_BACK.equals(target.getLifeState()))) {
				if ("income".equals(type) && (Investment.INVESTMENT_LIFESTATUS_RETURN_BACK.equals(target.getLifeState()))) {
					// int seq = trustService.getSeqByIncome(entity.getOid());
					// List<TrustIncomeForm> list = targetService.getIncomeData(target, entity);
					// form.setIncomeFormList(list);
					int seq = 1;
					TrustIncomeForm income = targetService.getIncomeData(target, entity, seq);
					if (null == income) {
						// income = new TrustIncomeForm();
						// income.setSeq(++seq);
						throw AMPException.getException("本息兑付数据异常");
					}
					form.setIncomeForm(income);
				}

				// 退款
				if ("back".equals(type) && Investment.INVESTMENT_LIFESTATUS_STAND_FAIL.equals(target.getLifeState())) {
					TrustIncomeForm income = new TrustIncomeForm();
					income.setExpIncomeRate(target.getCollectIncomeRate());
					income.setExpIncome(entity.getInvestVolume().add(targetService.collectingProfit(target, entity.getInvestVolume(), new Date(entity.getInvestDate().getTime()))).setScale(4,
							BigDecimal.ROUND_HALF_UP));
					form.setIncomeForm(income);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return form;
	}

	/**
	 * 获取信托计划的订单信息
	 * 
	 * @param oid
	 * @return
	 */
	@Transactional
	public TrustForm getTrustOrderByOid(String oid, String type) {
		TrustForm form = new TrustForm();
		if (CapitalEntity.APPLY.equals(type) || CapitalEntity.TRANS.equals(type)) {
			TrustOrderEntity entity = trustService.getTrustOrderByOid(oid);
			if (null != entity) {
				try {
					BeanUtils.copyProperties(form, entity);
					Investment target = entity.getTarget();
					form.setTargetOid(target.getOid());
					form.setTargetName(target.getName());
					form.setTargetType(target.getType());
					form.setExpAror(target.getExpAror());
					form.setRaiseScope(target.getRaiseScope());
					form.setSetDate(target.getSetDate());
					form.setArorFirstDate(target.getArorFirstDate());
					form.setAccrualDate(target.getAccrualDate());
					form.setContractDays(target.getContractDays());
					form.setLife(target.getLifed());
					form.setFloorVolume(target.getFloorVolume());
					form.setAccrualType(target.getAccrualType());
					form.setSubjectRating(target.getSubjectRating());
					form.setCollectEndDate(target.getCollectEndDate());
					form.setCollectStartDate(target.getCollectStartDate());
					form.setCollectIncomeRate(target.getCollectIncomeRate());
					form.setProfitType("amortized_cost".equals(entity.getProfitType()) ? "摊余成本法" : "账面价值法");
					// form.setSellScope(target.getRaiseScope()
					// .subtract(target.getHoldAmount())
					// .subtract(target.getApplyAmount())
					// .setScale(4, BigDecimal.ROUND_HALF_UP));
					form.setTrustAmount(target.getTrustAmount());
					form.setIncomeDate(entity.getInvestDate());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else if (CapitalEntity.INCOME.equals(type) || CapitalEntity.BACK.equals(type)) {
			TrustIncomeEntity entity = trustService.getTrustIncomeOrderByOid(oid);
			if (null != entity) {
				try {
					TrustIncomeForm income = new TrustIncomeForm();
					income.setSeq(entity.getSeq());
					income.setCapital(entity.getCapital());
					income.setExpIncome(entity.getIncome());
					income.setExpIncomeRate(entity.getIncomeRate());
					income.setIncome(entity.getIncome());
					income.setIncomeRate(entity.getIncomeRate());
					income.setAuditCapital(entity.getAuditCapital());
					income.setAuditIncome(entity.getAuditIncome());
					income.setAuditIncomeRate(entity.getAuditIncomeRate());
					income.setInvestCapital(entity.getInvestCapital());
					income.setInvestIncome(entity.getInvestIncome());
					income.setInvestIncomeRate(entity.getInvestIncomeRate());
					income.setIncomeDate(entity.getIncomeDate());
					income.setCollectRate(entity.getTrustEntity().getTarget().getCollectIncomeRate());
					income.setOverdueRate(entity.getTrustEntity().getTarget().getOverdueRate());
					form.setIncomeForm(income);
					form.setOid(entity.getOid());
					form.setIncomeDate(entity.getTrustEntity().getInterestDate());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			TrustTransEntity entity = trustService.getTrustTransOrderByOid(oid);
			if (null != entity) {
				try {
					BeanUtils.copyProperties(form, entity);
					Investment target = entity.getTrustEntity().getTarget();
					form.setTargetOid(target.getOid());
					form.setTargetName(target.getName());
					form.setTargetType(target.getType());
					form.setExpAror(target.getExpAror());
					form.setRaiseScope(target.getRaiseScope());
					form.setSetDate(target.getSetDate());
					form.setArorFirstDate(target.getArorFirstDate());
					form.setAccrualDate(target.getAccrualDate());
					form.setContractDays(target.getContractDays());
					form.setLife(target.getLifed());
					form.setFloorVolume(target.getFloorVolume());
					form.setAccrualType(target.getAccrualType());
					form.setSubjectRating(target.getSubjectRating());
					form.setCollectEndDate(target.getCollectEndDate());
					form.setCollectStartDate(target.getCollectStartDate());
					form.setCollectIncomeRate(target.getCollectIncomeRate());
					form.setApplyVolume(entity.getTranVolume());
					form.setApplyCash(entity.getTranCash());
					form.setInvestDate(entity.getTrustEntity().getInvestDate());
					form.setSellScope(target.getRaiseScope().subtract(target.getHoldAmount()).subtract(target.getApplyAmount()).setScale(4, BigDecimal.ROUND_HALF_UP));
					form.setOverdueRate(target.getOverdueRate());
					form.setTransType(entity.getType());
					form.setTransferee(entity.getTransferee());
					form.setIncomeDate(entity.getTrustEntity().getInterestDate());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return form;
	}

	/**
	 * 根据资产池id获取 预约中 的信托（计划）列表
	 * 
	 * @param pid
	 *            资产池id
	 * @return
	 */
	@Transactional
	public List<TrustForm> getTrustListForAppointmentByPid(String pid) {
		List<TrustForm> formList = Lists.newArrayList();
		TrustForm form = null;
		try {
			pid = assetPoolService.getPid(pid);
			List<TrustOrderEntity> orderList = trustService.findPurchaseByPidForAppointment(pid);
			if (!orderList.isEmpty()) {
				for (TrustOrderEntity entity : orderList) {
					form = new TrustForm();
					BeanUtils.copyProperties(form, entity);
					Investment target = entity.getTarget();
					form.setTargetOid(target.getOid());
					form.setTargetName(target.getName());
					form.setTargetType(target.getType());
					form.setIncomeRate(target.getExpIncome());
					form.setExpAror(target.getExpAror());
					form.setSetDate(target.getSetDate());
					form.setArorFirstDate(target.getArorFirstDate());
					form.setAccrualDate(target.getAccrualDate());
					form.setContractDays(target.getContractDays());
					form.setLife(target.getLifed());
					form.setFloorVolume(target.getFloorVolume());
					form.setCollectEndDate(target.getCollectEndDate());
					form.setCollectStartDate(target.getCollectStartDate());
					form.setCollectIncomeRate(target.getCollectIncomeRate());
					form.setExpSetDate(target.getExpSetDate());
					form.setSubjectRating(target.getSubjectRating());
					form.setRaiseScope(target.getRaiseScope());
					form.setTrustAmount(target.getTrustAmount());
					form.setAccrualType(target.getAccrualType());
					if (Investment.INVESTMENT_LIFESTATUS_STAND_UP.equals(target.getLifeState()))
						form.setType(CapitalEntity.TRANS);
					else
						form.setType(CapitalEntity.APPLY);
					form.setUpdateTime(null == entity.getUpdateTime() ? entity.getCreateTime() : entity.getUpdateTime());

					formList.add(form);
				}
			}
			List<TrustIncomeEntity> incomeList = trustService.findIncomeByPidForAppointment(pid);
			if (!incomeList.isEmpty()) {
				for (TrustIncomeEntity entity : incomeList) {
					form = new TrustForm();
					BeanUtils.copyProperties(form, entity);
					Investment target = entity.getTrustEntity().getTarget();
					form.setTargetOid(target.getOid());
					form.setTargetName(target.getName());
					form.setTargetType(target.getType());
					form.setIncomeRate(target.getExpIncome());
					form.setExpAror(target.getExpAror());
					form.setSetDate(target.getSetDate());
					form.setArorFirstDate(target.getArorFirstDate());
					form.setAccrualDate(target.getAccrualDate());
					form.setContractDays(target.getContractDays());
					form.setLife(target.getLifed());
					form.setFloorVolume(target.getFloorVolume());
					form.setCollectEndDate(target.getCollectEndDate());
					form.setCollectStartDate(target.getCollectStartDate());
					form.setCollectIncomeRate(target.getCollectIncomeRate());
					form.setExpSetDate(target.getExpSetDate());
					form.setSubjectRating(target.getSubjectRating());
					form.setRaiseScope(target.getRaiseScope());
					form.setTrustAmount(target.getTrustAmount());
					form.setAccrualType(target.getAccrualType());
					form.setInvestDate(entity.getIncomeDate());
					form.setApplyVolume(entity.getCapital());
					form.setApplyCash(entity.getIncome());
					form.setProfitType(entity.getTrustEntity().getProfitType());
					if (!entity.getSeq().equals(0))
						form.setType(CapitalEntity.INCOME);
					else
						form.setType(CapitalEntity.BACK);
					form.setUpdateTime(null == entity.getUpdateTime() ? entity.getCreateTime() : entity.getUpdateTime());

					formList.add(form);
				}
			}
			List<TrustTransEntity> transList = trustService.findTransByPidForAppointment(pid);
			if (!transList.isEmpty()) {
				for (TrustTransEntity entity : transList) {
					form = new TrustForm();
					BeanUtils.copyProperties(form, entity);
					Investment target = entity.getTrustEntity().getTarget();
					form.setTargetOid(target.getOid());
					form.setTargetName(target.getName());
					form.setTargetType(target.getType());
					form.setIncomeRate(target.getExpIncome());
					form.setExpAror(target.getExpAror());
					form.setSetDate(target.getSetDate());
					form.setArorFirstDate(target.getArorFirstDate());
					form.setAccrualDate(target.getAccrualDate());
					form.setContractDays(target.getContractDays());
					form.setLife(target.getLifed());
					form.setFloorVolume(target.getFloorVolume());
					form.setCollectEndDate(target.getCollectEndDate());
					form.setCollectStartDate(target.getCollectStartDate());
					form.setCollectIncomeRate(target.getCollectIncomeRate());
					form.setExpSetDate(target.getExpSetDate());
					form.setSubjectRating(target.getSubjectRating());
					form.setRaiseScope(target.getRaiseScope());
					form.setTrustAmount(target.getTrustAmount());
					form.setAccrualType(target.getAccrualType());
					form.setInvestDate(entity.getTranDate());
					form.setApplyVolume(entity.getTranVolume());
					form.setApplyCash(entity.getTranCash());
					form.setProfitType(entity.getTrustEntity().getProfitType());
					if (entity.isOverdueFlag()) {
						form.setType(CapitalEntity.OVERDUETRANSFER);
					} else {
						form.setType(CapitalEntity.TRANSFER);
					}
					form.setUpdateTime(null == entity.getUpdateTime() ? entity.getCreateTime() : entity.getUpdateTime());

					formList.add(form);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		Collections.sort(formList, new Comparator<TrustForm>() {
			public int compare(TrustForm arg0, TrustForm arg1) {
				return arg1.getUpdateTime().compareTo(arg0.getUpdateTime());
			}
		});

		return formList;
	}

	/**
	 * 根据资产池id获取 成立中 的信托（计划）列表
	 * 
	 * @param pid
	 *            资产池id
	 * @return
	 */
	@Transactional
	public PageResp<TrustForm> getTrustListByPid(String pid, Pageable pageable) {
		PageResp<TrustForm> rep = new PageResp<TrustForm>();
		List<TrustForm> formList = Lists.newArrayList();
		pid = assetPoolService.getPid(pid);
		Page<TrustEntity> list = trustService.findByPidForConfirm(pid, pageable);
		if (null != list && !list.getContent().isEmpty()) {
			TrustForm form = null;
			for (TrustEntity entity : list.getContent()) {
				form = new TrustForm();
				try {
					BeanUtils.copyProperties(form, entity);
					Investment target = entity.getTarget();
					form.setTargetOid(target.getOid());
					form.setTargetName(target.getName());
					form.setTargetType(target.getType());
					form.setIncomeRate(target.getExpIncome());
					form.setExpAror(target.getExpAror());
					form.setSetDate(target.getSetDate());
					form.setArorFirstDate(target.getArorFirstDate());
					form.setAccrualDate(target.getAccrualDate());
					form.setContractDays(target.getContractDays());
					form.setLife(target.getLifed());
					form.setFloorVolume(target.getFloorVolume());
					form.setCollectEndDate(target.getCollectEndDate());
					form.setCollectStartDate(target.getCollectStartDate());
					form.setCollectIncomeRate(target.getCollectIncomeRate());
					form.setExpSetDate(target.getExpSetDate());
					form.setHoldAmount(entity.getInvestVolume());
					form.setTranVolume(entity.getTransOutVolume());
					form.setSubjectRating(target.getSubjectRating());
					form.setRaiseScope(target.getRaiseScope());
					form.setTrustAmount(target.getTrustAmount());
					form.setAccrualType(target.getAccrualType());
					form.setState(target.getLifeState());
					form.setLifeState(target.getState());
					form.setIncomeDate(entity.getInterestDate());
				} catch (Exception e) {
					e.printStackTrace();
				}

				formList.add(form);
			}
			rep.setTotal(list.getTotalElements());
			rep.setRows(formList);
		}

		return rep;
	}

	/**
	 * 逻辑删除订单
	 * 
	 * @param oid
	 * @param operation
	 */
	@Transactional
	public void updateOrder(String oid, String operation, String operator) {
		if ("现金管理工具".equals(operation)) {
			fundService.updateOrder(oid, operator);
		} else {
			trustService.updateOrder(oid, operation, operator);
		}
	}

	/**
	 * 逻辑作废订单--坏账核销
	 * 
	 * @param oid
	 */
	@Transactional
	public void cancelOrder(String oid) {
		trustService.cancelOrder(oid);
	}

	/**
	 * 纠偏现金管理工具的持有额度
	 * 
	 * @param form
	 */
	@Transactional
	public void updateFund(FundForm form, String operator) {
		FundEntity entity = fundService.getFundByOid(form.getOid());
		BigDecimal amount = BigDecimalUtil.formatForMul10000(form.getAmount());

		// 更新资产池估值
		AssetPoolEntity pool = assetPoolService.getByOid(entity.getAssetPoolOid());
		pool.setOperator(operator);
		BigDecimal value = amount.subtract(entity.getAmount()).setScale(4, BigDecimal.ROUND_HALF_UP);
		BigDecimal nscale = pool.getScale().add(value).setScale(4, BigDecimal.ROUND_HALF_UP);
		assetPoolService.calcAssetPoolRate(pool, nscale, value, BigDecimal.ZERO);

		// 录入校准记录
		TargetAdjustEntity adjust = new TargetAdjustEntity();
		adjust.setOid(StringUtil.uuid());
		adjust.setAssetPoolOid(entity.getAssetPoolOid());
		adjust.setCashtoolOid(entity.getCashTool().getOid());
		adjust.setBaseAmount(entity.getAmount());
		adjust.setNewAmount(amount);
		adjust.setChangeAmount(value);
		adjust.setOperaterDate(new Date(System.currentTimeMillis()));
		adjust.setCreateTime(new Timestamp(System.currentTimeMillis()));
		adjustService.save(adjust);

		entity.setAmount(amount);
		fundService.save(entity);
	}

	/**
	 * 纠偏投资标的的持有额度
	 * 
	 * @param form
	 */
	@Transactional
	public void updateTrust(TrustForm form, String operator) {
		TrustEntity entity = trustService.getTrustByOid(form.getOid());
		BigDecimal investVolume = BigDecimalUtil.formatForMul10000(form.getHoldAmount());

		// 更新资产池估值
		AssetPoolEntity pool = assetPoolService.getByOid(entity.getAssetPoolOid());
		pool.setOperator(operator);
		BigDecimal value = investVolume.subtract(entity.getInvestVolume()).setScale(4, BigDecimal.ROUND_HALF_UP);
		BigDecimal nscale = pool.getScale().add(value).setScale(4, BigDecimal.ROUND_HALF_UP);
		assetPoolService.calcAssetPoolRate(pool, nscale, BigDecimal.ZERO, value);

		// 录入校准记录
		TargetAdjustEntity adjust = new TargetAdjustEntity();
		adjust.setOid(StringUtil.uuid());
		adjust.setAssetPoolOid(entity.getAssetPoolOid());
		adjust.setTargetOid(entity.getTarget().getOid());
		adjust.setBaseAmount(entity.getInvestVolume());
		adjust.setNewAmount(investVolume);
		adjust.setChangeAmount(value);
		adjust.setOperaterDate(new Date(System.currentTimeMillis()));
		adjust.setCreateTime(new Timestamp(System.currentTimeMillis()));
		adjustService.save(adjust);

		entity.setInvestVolume(investVolume);
		trustService.save(entity);
		investService.incHoldAmount(entity.getTarget().getOid(), value);
	}

	/**
	 * 资产池当日的净申购额
	 * 
	 * @param pid
	 * @param date
	 * @return
	 */
	public BigDecimal navData(String pid, Date date) {
		BigDecimal navData = BigDecimalUtil.init0;

		navData = navData.add(fundService.navData(pid, date)).add(trustService.navData(pid, date)).setScale(4, BigDecimal.ROUND_HALF_UP);

		return navData;
	}

	/**
	 * 判断15点之前还是之后
	 * 当日15点以前的：true
	 * 当日15点以后的：false
	 * 
	 * @return
	 */
	private boolean dateFalg() {
		try {
			java.util.Date baseDate = new java.util.Date(System.currentTimeMillis());
			// 判断当前时间是否小于15点
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			String sdate = format.format(baseDate);
			String edate = sdate.split(" ")[0] + " 15:00:00";
			java.util.Date cdate = new java.util.Date(format.parse(edate).getTime());
			if (cdate.compareTo(baseDate) > 0) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	/**
	 * 获取起息日
	 * 当日15点以前的，算T日起息
	 * 当日15点以后的，算T+1日起息
	 * 
	 * @return
	 */
	private Date getInterestDate() {
		Date sqlDate = null;
		java.util.Date baseDate = new java.util.Date(System.currentTimeMillis());
		boolean flag = this.dateFalg();
		if (flag) {
			sqlDate = new Date(baseDate.getTime());
		} else {
			sqlDate = new Date(DateUtil.addDay(baseDate, 1).getTime());
		}

		return sqlDate;
	}
}
