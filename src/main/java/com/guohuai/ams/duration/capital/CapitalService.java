package com.guohuai.ams.duration.capital;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.guohuai.ams.duration.assetPool.AssetPoolEntity;
import com.guohuai.ams.duration.assetPool.AssetPoolService;
import com.guohuai.ams.duration.order.OrderService;
import com.guohuai.ams.duration.order.fund.FundAuditEntity;
import com.guohuai.component.util.StringUtil;
import com.guohuai.component.web.view.PageResp;

/**
 * 存续期--账户服务接口
 * 
 * @author star.zhu
 *         2016年5月16日
 */
@Service
public class CapitalService {

	private static final String APPLY = "apply";
	private static final String AUDIT = "audit";
	private static final String APPOINTMENT = "appointment";
	private static final String CONFIRM = "confirm";

	private static final Map<String, String> stateMap = new HashMap<>();
	static {
		stateMap.put("00", "申请待审核");
		stateMap.put("10", "审核未通过");
		stateMap.put("11", "审核通过待预约");
		stateMap.put("12", "审核通过待确认");
		stateMap.put("20", "预约未通过");
		stateMap.put("21", "预约通过待确认");
		stateMap.put("30", "确认未通过");
		stateMap.put("31", "确认通过");
	}

	@Autowired
	private CapitalDao capitalDao;

	@Autowired
	private AssetPoolService assetPoolService;
	// @Autowired
	// private WarehouseDocumentService documentService;

	/**
	 * 获取所有的出入金明细
	 * 
	 * @return
	 */
	@Transactional
	public List<CapitalForm> getAllList() {
		List<CapitalForm> formList = Lists.newArrayList();
		List<CapitalEntity> list = capitalDao.findAll();
		if (!list.isEmpty()) {
			CapitalForm form = null;
			String type = null;
			for (CapitalEntity entity : list) {
				form = new CapitalForm();
				form.setSubject(entity.getSubject());
				type = entity.getSubject().substring(0, 2);
				if (null != entity.getCashtoolOrderOid()) {
					form.setOrderOid(entity.getCashtoolOrderOid());
					form.setCapital(entity.getFreezeCash());
					form.setOperation("现金管理工具申赎");
				} else if (null != entity.getTargetOrderOid()) {
					form.setOrderOid(entity.getTargetOrderOid());
					form.setCapital(entity.getFreezeCash());
					form.setOperation("信托标的申购");
				} else if (null != entity.getTargetIncomeOid()) {
					form.setOrderOid(entity.getTargetIncomeOid());
					form.setCapital(entity.getTransitCash());
					form.setOperation("本息兑付");
				} else if (null != entity.getTargetTransOid()) {
					form.setOrderOid(entity.getTargetTransOid());
					form.setCapital(entity.getTransitCash());
					form.setOperation("转让");
				}
				if ("申购".equals(type)) {
					form.setStatus("未审核");
				} else if ("审核".equals(type)) {
					form.setStatus("资金处理中");
				} else if ("预约".equals(type)) {
					form.setStatus("资金处理中");
				} else if ("确认".equals(type)) {
					form.setStatus("完成");
				}

				formList.add(form);
			}
		}

		return formList;
	}

	/**
	 * 获取资产池的出入金明细
	 * 
	 * @return
	 */
	@Transactional
	public PageResp<CapitalForm> getCapitalListByPid(String pid, Pageable pageable) {
		List<CapitalForm> formList = Lists.newArrayList();
		Page<CapitalEntity> list = capitalDao.findByOid(pid, pageable);
		if (null != list.getContent() && !list.getContent().isEmpty()) {
			CapitalForm form = null;
			// String type = null;
			for (CapitalEntity entity : list.getContent()) {
				form = new CapitalForm();
				form.setSubject(entity.getSubject());
				form.setCreateTime(entity.getCreateTime());
				// type = entity.getSubject().substring(0, 2);
				if (null != entity.getCashtoolOrderOid()) {
					form.setOrderOid(entity.getCashtoolOrderOid());
					form.setCapital(entity.getFreezeCash());
					form.setOperation(entity.getOperation());
				} else if (null != entity.getTargetOrderOid()) {
					form.setOrderOid(entity.getTargetOrderOid());
					form.setCapital(entity.getFreezeCash());
					form.setOperation(entity.getOperation());
				} else if (null != entity.getTargetIncomeOid()) {
					form.setOrderOid(entity.getTargetIncomeOid());
					form.setCapital(entity.getTransitCash());
					form.setOperation(entity.getOperation());
				} else if (null != entity.getTargetTransOid()) {
					form.setOrderOid(entity.getTargetTransOid());
					form.setCapital(entity.getTransitCash());
					form.setOperation(entity.getOperation());
				}
				form.setStatus(stateMap.get(entity.getState()));

				formList.add(form);
			}
		}

		PageResp<CapitalForm> rep = new PageResp<>();
		rep.setTotal(list.getTotalElements());
		rep.setRows(formList);

		return rep;
	}

	/**
	 * 资金明细
	 * 
	 * @param pid
	 *            资产池id
	 * @param oid
	 *            标的id
	 * @param sn
	 *            订单id
	 * @param target
	 *            标的类型
	 * @param capital
	 *            操作金额
	 * @param account
	 *            已录金额
	 * @param operation
	 *            操作：purchase（申购）；redeem（赎回）；income（本息兑付）；transfer（资产转让）
	 * @param operationType
	 *            apply（申请）；audit（审批）；appointment（预约）；confirm（确认）
	 * @param uid
	 *            操作人
	 */
	@Transactional
	public void capitalFlow1(String pid, String oid, String sn, String target, BigDecimal capital, BigDecimal account, String operation, String operationType, String uid) {
		AssetPoolEntity poolEntity = assetPoolService.getByOid(pid);
		CapitalEntity entity = new CapitalEntity();
		entity.setOid(StringUtil.uuid());
		entity.setAssetPoolOid(pid);
		entity.setCashtoolOrderOid(oid);
		entity.setOperator(uid);
		if ("purchase".equals(operation)) {
			entity.setFreezeCash(capital);
			entity.setSubject("申购" + target);
			if (target.equals(OrderService.FUND))
				entity.setTargetOrderOid(sn);
			else
				entity.setCashtoolOrderOid(sn);
			// 可用现金
			poolEntity.setCashPosition(poolEntity.getCashPosition().subtract(capital));
			// 冻结资金
			poolEntity.setFreezeCash(poolEntity.getFreezeCash().add(capital));
		} else if ("redeem".equals(operation)) {
			entity.setFreezeCash(capital);
			entity.setSubject("赎回" + target);
			if (target.equals(OrderService.FUND))
				entity.setTargetOrderOid(sn);
			else
				entity.setCashtoolOrderOid(sn);
			// 在途资金
			poolEntity.setTransitCash(poolEntity.getTransitCash().add(capital));
		} else if ("income".equals(operation)) {
			entity.setTransitCash(capital);
			entity.setSubject("本息兑付");
			entity.setTargetIncomeOid(sn);
			// 在途资金
			poolEntity.setTransitCash(poolEntity.getTransitCash().add(capital));
		} else if ("transfer".equals(operation)) {
			entity.setTransitCash(capital);
			entity.setSubject("资产转让");
			entity.setTargetTransOid(sn);
			// 在途资金
			poolEntity.setTransitCash(poolEntity.getTransitCash().add(capital));
		} else if ("audit".equals(operation)) {
			entity.setUnfreezeCash(account);
			entity.setFreezeCash(capital);
			entity.setSubject("审批" + target);
		} else if ("appointment".equals(operation)) {
			entity.setUnfreezeCash(account);
			entity.setTransitCash(capital);
			entity.setSubject("预约" + target);
		} else if ("confirm".equals(operation)) {
			entity.setUntransitCash(account);
			entity.setInputCash(capital);
			entity.setSubject("确认" + target);
		}
		entity.setCreateTime(new Timestamp(System.currentTimeMillis()));

		capitalDao.save(entity);
	}

	/**
	 * 审批流
	 * 
	 * @param pid
	 *            资产池id
	 * @param oid
	 *            标的id
	 * @param sn
	 *            订单id
	 * @param target
	 *            标的类型
	 * @param capital
	 *            操作金额
	 * @param account
	 *            已录金额
	 * @param operation
	 *            操作：purchase（申购）；redeem（赎回）；income（本息兑付）；transfer（资产转让）
	 * @param operationType
	 *            apply（申请）；audit（审批）；appointment（预约）；confirm（确认）
	 * @param uid
	 *            操作人
	 * @param state
	 *            操作状态：0（通过）；-1（不通过）
	 */
	@Transactional
	public void capitalFlow(String pid, String oid, String sn, String target, BigDecimal capital, BigDecimal account, String operation, String operationType, String uid, String state) {
		AssetPoolEntity poolEntity = assetPoolService.getByOid(pid);
		CapitalEntity entity = new CapitalEntity();
		entity.setOid(StringUtil.uuid());
		entity.setAssetPoolOid(pid);
		entity.setOperator(uid);
		if (APPLY.equals(operationType)) {
			capitalWithApply(sn, target, capital, operation, poolEntity, entity);
		} else if (AUDIT.equals(operationType)) {
			capitalWithAudit(sn, target, capital, account, operation, poolEntity, entity, state);
		} else if (APPOINTMENT.equals(operationType)) {
			capitalWithAppointment(sn, target, capital, account, operation, poolEntity, entity, state);
		} else if (CONFIRM.equals(operationType)) {
			capitalWithConfirm(sn, target, capital, account, operation, poolEntity, entity, state);
		}
		entity.setCreateTime(new Timestamp(System.currentTimeMillis()));

		capitalDao.save(entity);
	}

	/**
	 * 资金明细--申请
	 * 
	 * @param sn
	 *            订单id
	 * @param target
	 *            标的类型
	 * @param capital
	 *            操作金额
	 * @param operation
	 *            操作：purchase（申购）；redeem（赎回）；income（本息兑付）；transfer（资产转让）
	 * @param poolEntity
	 *            资产池对象
	 * @param entity
	 *            现金变动明细
	 */
	@Transactional
	public void capitalWithApply(String sn, String target, BigDecimal capital, String operation, AssetPoolEntity poolEntity, CapitalEntity entity) {
		entity.setState(CapitalEntity.APPLY00);
		if ("purchase".equals(operation)) {
			entity.setFreezeCash(capital);
			entity.setSubject("申购" + target);
			if (target.equals(OrderService.FUND)) {
				entity.setCashtoolOrderOid(sn);
				entity.setOperation(CapitalEntity.PURCHASE);
				// 冻结资金（累计申购现金类管理工具）
				poolEntity.setFreezeCashForFund(poolEntity.getFreezeCashForFund().add(capital).setScale(4, BigDecimal.ROUND_HALF_UP));
			} else {
				entity.setTargetOrderOid(sn);
				entity.setOperation(CapitalEntity.APPLY);
			}
			// 可用现金
			poolEntity.setCashPosition(poolEntity.getCashPosition().subtract(capital).setScale(4, BigDecimal.ROUND_HALF_UP));
			// 冻结资金
			poolEntity.setFreezeCash(poolEntity.getFreezeCash().add(capital).setScale(4, BigDecimal.ROUND_HALF_UP));
		} else if ("trans".equals(operation)) {
			entity.setFreezeCash(capital);
			entity.setSubject("投资标的转入");
			entity.setTargetOrderOid(sn);
			entity.setOperation(CapitalEntity.TRANS);
			// 可用现金
			poolEntity.setCashPosition(poolEntity.getCashPosition().subtract(capital).setScale(4, BigDecimal.ROUND_HALF_UP));
			// 冻结资金
			poolEntity.setFreezeCash(poolEntity.getFreezeCash().add(capital).setScale(4, BigDecimal.ROUND_HALF_UP));
		} else if ("redeem".equals(operation)) {
			entity.setTransitCash(capital);
			entity.setFreezeCash(capital);
			entity.setSubject("赎回" + target);
			entity.setCashtoolOrderOid(sn);
			entity.setOperation(CapitalEntity.REDEEM);
			// 在途资金
			poolEntity.setTransitCash(poolEntity.getTransitCash().add(capital).setScale(4, BigDecimal.ROUND_HALF_UP));
		} else if ("income".equals(operation)) {
			entity.setTransitCash(capital);
			entity.setSubject("本息兑付");
			entity.setOperation(CapitalEntity.INCOME);
			entity.setTargetIncomeOid(sn);
			// 在途资金
			poolEntity.setTransitCash(poolEntity.getTransitCash().add(capital).setScale(4, BigDecimal.ROUND_HALF_UP));
		} else if ("back".equals(operation)) {
			entity.setTransitCash(capital);
			entity.setSubject("退款");
			entity.setOperation(CapitalEntity.BACK);
			entity.setTargetIncomeOid(sn);
			// 在途资金
			poolEntity.setTransitCash(poolEntity.getTransitCash().add(capital).setScale(4, BigDecimal.ROUND_HALF_UP));
		} else if ("transfer".equals(operation)) {
			entity.setTransitCash(capital);
			entity.setSubject("投资标的转让");
			entity.setOperation(CapitalEntity.TRANSFER);
			entity.setTargetTransOid(sn);
			// 在途资金
			poolEntity.setTransitCash(poolEntity.getTransitCash().add(capital).setScale(4, BigDecimal.ROUND_HALF_UP));
		} else if ("overdue_transfer".equals(operation)) {
			entity.setTransitCash(capital);
			entity.setSubject("投资标的逾期转让");
			entity.setOperation(CapitalEntity.OVERDUETRANSFER);
			entity.setTargetTransOid(sn);
			// 在途资金
			poolEntity.setTransitCash(poolEntity.getTransitCash().add(capital).setScale(4, BigDecimal.ROUND_HALF_UP));
		}
		entity.setCreateTime(new Timestamp(System.currentTimeMillis()));

		capitalDao.save(entity);
		assetPoolService.save(poolEntity);
	}

	/**
	 * 资金明细--审批
	 * 
	 * @param sn
	 *            订单id
	 * @param target
	 *            标的类型
	 * @param capital
	 *            操作金额
	 * @param account
	 *            已录金额
	 * @param operation
	 *            操作：purchase（申购）；redeem（赎回）；income（本息兑付）；transfer（资产转让）
	 * @param poolEntity
	 *            资产池对象
	 * @param entity
	 *            现金变动明细
	 * @param state
	 *            操作状态：0（通过）；-1（不通过）
	 */
	@Transactional
	public void capitalWithAudit(String sn, String target, BigDecimal capital, BigDecimal account, String operation, AssetPoolEntity poolEntity, CapitalEntity entity, String state) {
		if (FundAuditEntity.SUCCESSED.equals(state))
			entity.setState(CapitalEntity.AUDIT11);
		else
			entity.setState(CapitalEntity.AUDIT10);
		if ("purchase".equals(operation)) {
			if (target.equals(OrderService.FUND)) {
				entity.setCashtoolOrderOid(sn);
				entity.setOperation(CapitalEntity.PURCHASE);
			} else {
				entity.setTargetOrderOid(sn);
				entity.setOperation(CapitalEntity.APPLY);
			}
			entity.setUnfreezeCash(account);
			entity.setFreezeCash(capital);
			entity.setSubject("审批申请中" + target + "订单");
			if (FundAuditEntity.FAIL.equals(state)) {
				// 可用现金
				poolEntity.setCashPosition(poolEntity.getCashPosition().add(account).setScale(4, BigDecimal.ROUND_HALF_UP));
				// 冻结资金
				poolEntity.setFreezeCash(poolEntity.getFreezeCash().subtract(account).setScale(4, BigDecimal.ROUND_HALF_UP));
				if (target.equals(OrderService.FUND)) {
					// 冻结资金（累计申购现金类管理工具）
					poolEntity.setFreezeCashForFund(poolEntity.getFreezeCashForFund().subtract(account).setScale(4, BigDecimal.ROUND_HALF_UP));
				}
			} else {
				if (!capital.equals(account)) {
					// 可用现金
					poolEntity.setCashPosition(poolEntity.getCashPosition().add(account).subtract(capital).setScale(4, BigDecimal.ROUND_HALF_UP));
					// 冻结资金
					poolEntity.setFreezeCash(poolEntity.getFreezeCash().subtract(account).add(capital).setScale(4, BigDecimal.ROUND_HALF_UP));
					if (target.equals(OrderService.FUND)) {
						// 冻结资金（累计申购现金类管理工具）
						poolEntity.setFreezeCashForFund(poolEntity.getFreezeCashForFund().subtract(account).add(capital).setScale(4, BigDecimal.ROUND_HALF_UP));
					}
				}
			}
		} else if ("trans".equals(operation)) {
			entity.setUnfreezeCash(account);
			entity.setFreezeCash(capital);
			entity.setTargetOrderOid(sn);
			entity.setOperation(CapitalEntity.TRANS);
			entity.setSubject("审批申请中" + target + "订单");
			if (FundAuditEntity.FAIL.equals(state)) {
				// 可用现金
				poolEntity.setCashPosition(poolEntity.getCashPosition().add(account).setScale(4, BigDecimal.ROUND_HALF_UP));
				// 冻结资金
				poolEntity.setFreezeCash(poolEntity.getFreezeCash().subtract(account).setScale(4, BigDecimal.ROUND_HALF_UP));
			} else {
				if (!capital.equals(account)) {
					// 可用现金
					poolEntity.setCashPosition(poolEntity.getCashPosition().add(account).subtract(capital).setScale(4, BigDecimal.ROUND_HALF_UP));
					// 冻结资金
					poolEntity.setFreezeCash(poolEntity.getFreezeCash().subtract(account).add(capital).setScale(4, BigDecimal.ROUND_HALF_UP));
				}
			}
		} else if ("redeem".equals(operation)) {
			entity.setCashtoolOrderOid(sn);
			entity.setUnfreezeCash(account);
			entity.setFreezeCash(capital);
			entity.setSubject("审批赎回中" + target + "订单");
			entity.setOperation(CapitalEntity.REDEEM);
			if (FundAuditEntity.FAIL.equals(state)) {
				// 在途资金
				poolEntity.setTransitCash(poolEntity.getTransitCash().subtract(account).setScale(4, BigDecimal.ROUND_HALF_UP));
			} else {
				if (!capital.equals(account)) {
					// 在途资金
					poolEntity.setTransitCash(poolEntity.getTransitCash().subtract(account).add(capital).setScale(4, BigDecimal.ROUND_HALF_UP));
				}
			}
		} else if ("income".equals(operation)) {
			entity.setTargetIncomeOid(sn);
			entity.setUnfreezeCash(account);
			entity.setFreezeCash(capital);
			entity.setTransitCash(capital);
			entity.setSubject("审批本息兑付订单");
			entity.setOperation(CapitalEntity.INCOME);
			if (FundAuditEntity.FAIL.equals(state)) {
				// 在途资金
				poolEntity.setTransitCash(poolEntity.getTransitCash().subtract(account).setScale(4, BigDecimal.ROUND_HALF_UP));
			} else {
				if (!capital.equals(account)) {
					// 在途资金
					poolEntity.setTransitCash(poolEntity.getTransitCash().subtract(account).add(capital).setScale(4, BigDecimal.ROUND_HALF_UP));
				}
			}
		} else if ("back".equals(operation)) {
			entity.setTargetIncomeOid(sn);
			entity.setUnfreezeCash(account);
			entity.setFreezeCash(capital);
			entity.setTransitCash(capital);
			entity.setSubject("审批退款订单");
			entity.setOperation(CapitalEntity.BACK);
			if (FundAuditEntity.FAIL.equals(state)) {
				// 在途资金
				poolEntity.setTransitCash(poolEntity.getTransitCash().subtract(account).setScale(4, BigDecimal.ROUND_HALF_UP));
			} else {
				if (!capital.equals(account)) {
					// 在途资金
					poolEntity.setTransitCash(poolEntity.getTransitCash().subtract(account).add(capital).setScale(4, BigDecimal.ROUND_HALF_UP));
				}
			}
		} else if ("transfer".equals(operation)) {
			entity.setTargetTransOid(sn);
			entity.setUnfreezeCash(account);
			entity.setFreezeCash(capital);
			entity.setTransitCash(capital);
			entity.setSubject("审批资产转让订单");
			entity.setOperation(CapitalEntity.TRANSFER);
			if (FundAuditEntity.FAIL.equals(state)) {
				// 在途资金
				poolEntity.setTransitCash(poolEntity.getTransitCash().subtract(account).setScale(4, BigDecimal.ROUND_HALF_UP));
			} else {
				if (!capital.equals(account)) {
					// 在途资金
					poolEntity.setTransitCash(poolEntity.getTransitCash().subtract(account).add(capital).setScale(4, BigDecimal.ROUND_HALF_UP));
				}
			}
		} else if ("overdue_transfer".equals(operation)) {
			entity.setTargetTransOid(sn);
			entity.setUnfreezeCash(account);
			entity.setFreezeCash(capital);
			entity.setTransitCash(capital);
			entity.setSubject("审批资产逾期转让订单");
			entity.setOperation(CapitalEntity.OVERDUETRANSFER);
			if (FundAuditEntity.FAIL.equals(state)) {
				// 在途资金
				poolEntity.setTransitCash(poolEntity.getTransitCash().subtract(account).setScale(4, BigDecimal.ROUND_HALF_UP));
			} else {
				if (!capital.equals(account)) {
					// 在途资金
					poolEntity.setTransitCash(poolEntity.getTransitCash().subtract(account).add(capital).setScale(4, BigDecimal.ROUND_HALF_UP));
				}
			}
		}
		entity.setCreateTime(new Timestamp(System.currentTimeMillis()));

		capitalDao.save(entity);
		assetPoolService.save(poolEntity);
	}

	/**
	 * 资金明细--预约
	 * 
	 * @param sn
	 *            订单id
	 * @param target
	 *            标的类型
	 * @param capital
	 *            操作金额
	 * @param account
	 *            已录金额
	 * @param operation
	 *            操作：purchase（申购）；redeem（赎回）；income（本息兑付）；transfer（资产转让）
	 * @param poolEntity
	 *            资产池对象
	 * @param entity
	 *            现金变动明细
	 * @param state
	 *            操作状态：0（通过）；-1（不通过）
	 */
	@Transactional
	public void capitalWithAppointment(String sn, String target, BigDecimal capital, BigDecimal account, String operation, AssetPoolEntity poolEntity, CapitalEntity entity, String state) {
		if (FundAuditEntity.SUCCESSED.equals(state))
			entity.setState(CapitalEntity.APPOINTMENT21);
		else
			entity.setState(CapitalEntity.APPOINTMENT20);
		if ("purchase".equals(operation)) {
			if (target.equals(OrderService.FUND)) {
				entity.setCashtoolOrderOid(sn);
				entity.setOperation(CapitalEntity.PURCHASE);
			} else {
				entity.setTargetOrderOid(sn);
				entity.setOperation(CapitalEntity.APPLY);
			}
			entity.setUnfreezeCash(account);
			entity.setFreezeCash(capital);
			entity.setSubject("预约申请中" + target + "订单");
			if (FundAuditEntity.FAIL.equals(state)) {
				// 可用现金
				poolEntity.setCashPosition(poolEntity.getCashPosition().add(account).setScale(4, BigDecimal.ROUND_HALF_UP));
				// 冻结资金
				poolEntity.setFreezeCash(poolEntity.getFreezeCash().subtract(account).setScale(4, BigDecimal.ROUND_HALF_UP));
				if (target.equals(OrderService.FUND)) {
					// 冻结资金（累计申购现金类管理工具）
					poolEntity.setFreezeCashForFund(poolEntity.getFreezeCashForFund().subtract(account).setScale(4, BigDecimal.ROUND_HALF_UP));
				}
			} else {
				if (!capital.equals(account)) {
					// 可用现金
					poolEntity.setCashPosition(poolEntity.getCashPosition().add(account).subtract(capital).setScale(4, BigDecimal.ROUND_HALF_UP));
					// 冻结资金
					poolEntity.setFreezeCash(poolEntity.getFreezeCash().subtract(account).add(capital).setScale(4, BigDecimal.ROUND_HALF_UP));
					if (target.equals(OrderService.FUND)) {
						// 冻结资金（累计申购现金类管理工具）
						poolEntity.setFreezeCashForFund(poolEntity.getFreezeCashForFund().subtract(account).add(capital).setScale(4, BigDecimal.ROUND_HALF_UP));
					}
				}
			}
		} else if ("trans".equals(operation)) {
			entity.setTargetOrderOid(sn);
			entity.setUnfreezeCash(account);
			entity.setFreezeCash(capital);
			entity.setOperation(CapitalEntity.TRANS);
			entity.setSubject("预约申请中" + target + "订单");
			if (FundAuditEntity.FAIL.equals(state)) {
				// 可用现金
				poolEntity.setCashPosition(poolEntity.getCashPosition().add(account).setScale(4, BigDecimal.ROUND_HALF_UP));
				// 冻结资金
				poolEntity.setFreezeCash(poolEntity.getFreezeCash().subtract(account).setScale(4, BigDecimal.ROUND_HALF_UP));
			} else {
				if (!capital.equals(account)) {
					// 可用现金
					poolEntity.setCashPosition(poolEntity.getCashPosition().add(account).subtract(capital).setScale(4, BigDecimal.ROUND_HALF_UP));
					// 冻结资金
					poolEntity.setFreezeCash(poolEntity.getFreezeCash().subtract(account).add(capital).setScale(4, BigDecimal.ROUND_HALF_UP));
				}
			}
		} else if ("redeem".equals(operation)) {
			entity.setCashtoolOrderOid(sn);
			entity.setUnfreezeCash(account);
			entity.setFreezeCash(capital);
			entity.setSubject("预约赎回中" + target + "订单");
			entity.setOperation(CapitalEntity.REDEEM);
			if (FundAuditEntity.FAIL.equals(state)) {
				// 在途资金
				poolEntity.setTransitCash(poolEntity.getTransitCash().subtract(account).setScale(4, BigDecimal.ROUND_HALF_UP));
			} else {
				if (!capital.equals(account)) {
					// 在途资金
					poolEntity.setTransitCash(poolEntity.getTransitCash().subtract(account).add(capital).setScale(4, BigDecimal.ROUND_HALF_UP));
				}
			}
		} else if ("income".equals(operation)) {
			entity.setTargetIncomeOid(sn);
			entity.setUnfreezeCash(account);
			entity.setFreezeCash(capital);
			entity.setTransitCash(capital);
			entity.setSubject("预约本息兑付订单");
			entity.setOperation(CapitalEntity.INCOME);
			if (FundAuditEntity.FAIL.equals(state)) {
				// 在途资金
				poolEntity.setTransitCash(poolEntity.getTransitCash().subtract(account).setScale(4, BigDecimal.ROUND_HALF_UP));
			} else {
				if (!capital.equals(account)) {
					// 在途资金
					poolEntity.setTransitCash(poolEntity.getTransitCash().subtract(account).add(capital).setScale(4, BigDecimal.ROUND_HALF_UP));
				}
			}
		} else if ("back".equals(operation)) {
			entity.setTargetIncomeOid(sn);
			entity.setUnfreezeCash(account);
			entity.setFreezeCash(capital);
			entity.setTransitCash(capital);
			entity.setSubject("预约退款订单");
			entity.setOperation(CapitalEntity.BACK);
			if (FundAuditEntity.FAIL.equals(state)) {
				// 在途资金
				poolEntity.setTransitCash(poolEntity.getTransitCash().subtract(account).setScale(4, BigDecimal.ROUND_HALF_UP));
			} else {
				if (!capital.equals(account)) {
					// 在途资金
					poolEntity.setTransitCash(poolEntity.getTransitCash().subtract(account).add(capital).setScale(4, BigDecimal.ROUND_HALF_UP));
				}
			}
		} else if ("transfer".equals(operation)) {
			entity.setTargetTransOid(sn);
			entity.setUnfreezeCash(account);
			entity.setFreezeCash(capital);
			entity.setTransitCash(capital);
			entity.setSubject("预约资产转让订单");
			entity.setOperation(CapitalEntity.TRANSFER);
			if (FundAuditEntity.FAIL.equals(state)) {
				// 在途资金
				poolEntity.setTransitCash(poolEntity.getTransitCash().subtract(account).setScale(4, BigDecimal.ROUND_HALF_UP));
			} else {
				if (!capital.equals(account)) {
					// 在途资金
					poolEntity.setTransitCash(poolEntity.getTransitCash().subtract(account).add(capital).setScale(4, BigDecimal.ROUND_HALF_UP));
				}
			}
		}
		entity.setCreateTime(new Timestamp(System.currentTimeMillis()));

		capitalDao.save(entity);
		assetPoolService.save(poolEntity);
	}

	/**
	 * 资金明细--确认
	 * 
	 * @param sn
	 *            订单id
	 * @param target
	 *            标的类型
	 * @param capital
	 *            操作金额
	 * @param account
	 *            已录金额
	 * @param operation
	 *            操作：purchase（申购）；redeem（赎回）；income（本息兑付）；transfer（资产转让）
	 * @param poolEntity
	 *            资产池对象
	 * @param entity
	 *            现金变动明细
	 * @param state
	 *            操作状态：0（通过）；-1（不通过）
	 */
	@Transactional
	public void capitalWithConfirm(String sn, String target, BigDecimal capital, BigDecimal account, String operation, AssetPoolEntity poolEntity, CapitalEntity entity, String state) {
		if (FundAuditEntity.SUCCESSED.equals(state))
			entity.setState(CapitalEntity.CONFIRM31);
		else
			entity.setState(CapitalEntity.CONFIRM30);
		BigDecimal rate = BigDecimal.ZERO;
		if (null != capital)
			rate = capital.divide(poolEntity.getScale(), 4, BigDecimal.ROUND_HALF_UP);
		if ("purchase".equals(operation)) {
			if (target.equals(OrderService.FUND)) {
				entity.setCashtoolOrderOid(sn);
				entity.setOperation(CapitalEntity.PURCHASE);
			} else {
				entity.setTargetOrderOid(sn);
				entity.setOperation(CapitalEntity.APPLY);
			}
			entity.setUnfreezeCash(account);
			entity.setFreezeCash(capital);
			entity.setSubject("确认申请中" + target + "订单");
			if (FundAuditEntity.FAIL.equals(state)) {
				// 可用现金
				poolEntity.setCashPosition(poolEntity.getCashPosition().add(account).setScale(4, BigDecimal.ROUND_HALF_UP));
			} else {
				if (!capital.equals(account)) {
					// 可用现金
					poolEntity.setCashPosition(poolEntity.getCashPosition().add(account).subtract(capital).setScale(4, BigDecimal.ROUND_HALF_UP));
				}
				poolEntity.setCashFactRate(poolEntity.getCashFactRate().subtract(rate).setScale(4, BigDecimal.ROUND_HALF_UP));
				if (target.equals(OrderService.FUND)) {
					poolEntity.setCashtoolFactRate(poolEntity.getCashtoolFactRate().add(rate).setScale(4, BigDecimal.ROUND_HALF_UP));
					// documentService.cashtoolPurchase(poolEntity.getOid(), sn, capital);
				} else {
					poolEntity.setTargetFactRate(poolEntity.getTargetFactRate().add(rate).setScale(4, BigDecimal.ROUND_HALF_UP));
					// documentService.targetPurchase(poolEntity.getOid(), sn, capital);
				}
			}
			// 冻结资金
			poolEntity.setFreezeCash(poolEntity.getFreezeCash().subtract(account).setScale(4, BigDecimal.ROUND_HALF_UP));
			if (target.equals(OrderService.FUND)) {
				// 冻结资金（累计申购现金类管理工具）
				poolEntity.setFreezeCashForFund(poolEntity.getFreezeCashForFund().subtract(account).setScale(4, BigDecimal.ROUND_HALF_UP));
			}
		} else if ("trans".equals(operation)) {
			entity.setTargetOrderOid(sn);
			entity.setUnfreezeCash(account);
			entity.setFreezeCash(capital);
			entity.setOperation(CapitalEntity.TRANS);
			entity.setSubject("确认申请中" + target + "订单");
			if (FundAuditEntity.FAIL.equals(state)) {
				// 可用现金
				poolEntity.setCashPosition(poolEntity.getCashPosition().add(account).setScale(4, BigDecimal.ROUND_HALF_UP));
				// 冻结资金
				poolEntity.setFreezeCash(poolEntity.getFreezeCash().subtract(account).setScale(4, BigDecimal.ROUND_HALF_UP));
			} else {
				if (!capital.equals(account)) {
					// 可用现金
					poolEntity.setCashPosition(poolEntity.getCashPosition().add(account).subtract(capital).setScale(4, BigDecimal.ROUND_HALF_UP));
				}
				poolEntity.setCashFactRate(poolEntity.getCashFactRate().subtract(rate).setScale(4, BigDecimal.ROUND_HALF_UP));
				poolEntity.setTargetFactRate(poolEntity.getTargetFactRate().add(rate).setScale(4, BigDecimal.ROUND_HALF_UP));
				// documentService.targetPurchase(poolEntity.getOid(), sn, capital);
				// 冻结资金
				poolEntity.setFreezeCash(poolEntity.getFreezeCash().subtract(account).setScale(4, BigDecimal.ROUND_HALF_UP));
			}
		} else if ("redeem".equals(operation)) {
			entity.setCashtoolOrderOid(sn);
			entity.setUnfreezeCash(account);
			entity.setFreezeCash(capital);
			entity.setSubject("确认赎回中" + target + "订单");
			entity.setOperation(CapitalEntity.REDEEM);
			if (FundAuditEntity.SUCCESSED.equals(state)) {
				poolEntity.setCashFactRate(poolEntity.getCashFactRate().add(rate).setScale(4, BigDecimal.ROUND_HALF_UP));
				poolEntity.setCashtoolFactRate(poolEntity.getCashtoolFactRate().subtract(rate).setScale(4, BigDecimal.ROUND_HALF_UP));
				// 可用现金
				poolEntity.setCashPosition(poolEntity.getCashPosition().add(capital).setScale(4, BigDecimal.ROUND_HALF_UP));
				// documentService.cashtoolRedemption(poolEntity.getOid(), sn, capital);
			}
			// 在途资金
			poolEntity.setTransitCash(poolEntity.getTransitCash().subtract(account).setScale(4, BigDecimal.ROUND_HALF_UP));
		} else if ("income".equals(operation)) {
			entity.setTargetIncomeOid(sn);
			entity.setUnfreezeCash(account);
			entity.setFreezeCash(capital);
			entity.setTransitCash(capital);
			entity.setSubject("确认本息兑付订单");
			entity.setOperation(CapitalEntity.INCOME);
			if (FundAuditEntity.SUCCESSED.equals(state)) {
				poolEntity.setCashFactRate(poolEntity.getCashFactRate().add(rate).setScale(4, BigDecimal.ROUND_HALF_UP));
				poolEntity.setTargetFactRate(poolEntity.getTargetFactRate().subtract(rate).setScale(4, BigDecimal.ROUND_HALF_UP));
				// 可用现金
				poolEntity.setCashPosition(poolEntity.getCashPosition().add(capital).setScale(4, BigDecimal.ROUND_HALF_UP));
			}
			// 在途资金
			poolEntity.setTransitCash(poolEntity.getTransitCash().subtract(account).setScale(4, BigDecimal.ROUND_HALF_UP));
			poolEntity.setFactProfit(poolEntity.getFactProfit().add(capital).setScale(4, BigDecimal.ROUND_HALF_UP));
		} else if ("back".equals(operation)) {
			entity.setTargetIncomeOid(sn);
			entity.setUnfreezeCash(account);
			entity.setFreezeCash(capital);
			entity.setTransitCash(capital);
			entity.setSubject("确认退款订单");
			entity.setOperation(CapitalEntity.BACK);
			if (FundAuditEntity.SUCCESSED.equals(state)) {
				poolEntity.setCashFactRate(poolEntity.getCashFactRate().add(rate).setScale(4, BigDecimal.ROUND_HALF_UP));
				poolEntity.setTargetFactRate(poolEntity.getTargetFactRate().subtract(rate).setScale(4, BigDecimal.ROUND_HALF_UP));
				// 可用现金
				poolEntity.setCashPosition(poolEntity.getCashPosition().add(capital).setScale(4, BigDecimal.ROUND_HALF_UP));
			}
			// 在途资金
			poolEntity.setTransitCash(poolEntity.getTransitCash().subtract(account).setScale(4, BigDecimal.ROUND_HALF_UP));
			poolEntity.setFactProfit(poolEntity.getFactProfit().add(capital).setScale(4, BigDecimal.ROUND_HALF_UP));
		} else if ("transfer".equals(operation)) {
			entity.setTargetTransOid(sn);
			entity.setUnfreezeCash(account);
			entity.setFreezeCash(capital);
			entity.setTransitCash(capital);
			entity.setSubject("确认资产转让订单");
			entity.setOperation(CapitalEntity.TRANSFER);
			if (FundAuditEntity.SUCCESSED.equals(state)) {
				// poolEntity.setCashFactRate(poolEntity.getCashFactRate()
				// .add(rate).setScale(4, BigDecimal.ROUND_HALF_UP));
				// poolEntity.setTargetFactRate(poolEntity.getTargetFactRate()
				// .subtract(rate).setScale(4, BigDecimal.ROUND_HALF_UP));
				// 可用现金
				poolEntity.setCashPosition(poolEntity.getCashPosition().add(capital).setScale(4, BigDecimal.ROUND_HALF_UP));
			}
			// 在途资金
			poolEntity.setTransitCash(poolEntity.getTransitCash().subtract(account).setScale(4, BigDecimal.ROUND_HALF_UP));
		} else if ("overdue_transfer".equals(operation)) {
			entity.setTargetTransOid(sn);
			entity.setUnfreezeCash(account);
			entity.setFreezeCash(capital);
			entity.setTransitCash(capital);
			entity.setSubject("确认资产转让订单");
			entity.setOperation(CapitalEntity.OVERDUETRANSFER);
			if (FundAuditEntity.SUCCESSED.equals(state)) {
				// poolEntity.setCashFactRate(poolEntity.getCashFactRate()
				// .add(rate).setScale(4, BigDecimal.ROUND_HALF_UP));
				// poolEntity.setTargetFactRate(poolEntity.getTargetFactRate()
				// .subtract(rate).setScale(4, BigDecimal.ROUND_HALF_UP));
				// 可用现金
				poolEntity.setCashPosition(poolEntity.getCashPosition().add(capital).setScale(4, BigDecimal.ROUND_HALF_UP));
			}
			// 在途资金
			poolEntity.setTransitCash(poolEntity.getTransitCash().subtract(account).setScale(4, BigDecimal.ROUND_HALF_UP));
		}
		entity.setCreateTime(new Timestamp(System.currentTimeMillis()));

		capitalDao.save(entity);
		assetPoolService.save(poolEntity);
	}
}
