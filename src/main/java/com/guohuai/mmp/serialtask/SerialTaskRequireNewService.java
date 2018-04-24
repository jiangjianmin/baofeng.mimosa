package com.guohuai.mmp.serialtask;

import java.sql.Timestamp;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.guohuai.ams.product.ProductDurationService;
import com.guohuai.component.exception.AMPException;
import com.guohuai.mmp.investor.tradeorder.InvestorInvestTradeOrderExtService;
import com.guohuai.mmp.investor.tradeorder.check.InvestorAbandonTradeOrderService;
import com.guohuai.mmp.platform.publisher.offset.PublisherOffsetService;
import com.guohuai.mmp.publisher.hold.PublisherHoldService;
import com.guohuai.mmp.publisher.investor.InterestRateMethodService;
import com.guohuai.mmp.publisher.investor.InterestDistributionService;
import com.guohuai.mmp.schedule.ResetTodayService;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class SerialTaskRequireNewService {

	@Autowired
	private SerialTaskDao serialTaskDao;
	@Autowired
	private PublisherOffsetService publisherOffsetService;
	@Autowired
	private PublisherHoldService publisherHoldService;
	@Autowired
	private InterestDistributionService interestDistributionService;
	@Autowired
	private InvestorInvestTradeOrderExtService investorInvestTradeOrderExtService;
	@Autowired
	private ResetTodayService resetTodayService;
	@Autowired
	private InvestorAbandonTradeOrderService investorAbandonTradeOrderService;
	@Autowired
	private ProductDurationService productDurationService;
	/**
	 * 创建新子任务
	 */
//	@Transactional(value = TxType.REQUIRES_NEW)
	public SerialTaskEntity createEntity(SerialTaskReq<?> req) {
		SerialTaskEntity en = new SerialTaskEntity();
		en.setTaskCode(req.getTaskCode());
		en.setTaskParams(JSONObject.toJSONString(req.getTaskParams()));
		en.setTaskStatus(SerialTaskEntity.TASK_taskStatus_toRun);
		if (en.getTaskCode().equals(SerialTaskEntity.TASK_taskCode_invest)) {
			en.setPriority(TaskPriority.INVEST.getValue());
		}
		if (en.getTaskCode().equals(SerialTaskEntity.TASK_taskCode_redeem)) {
			en.setPriority(TaskPriority.REDEEM.getValue());
		}
		if (en.getTaskCode().equals(SerialTaskEntity.TASK_taskCode_redeemInvest)) {
			en.setPriority(TaskPriority.REDEEMINVEST.getValue());
		}
		
		return this.serialTaskDao.save(en);
	}

	/**
	 * 开始任务
	 * @return
	 */
	@Transactional(value = TxType.REQUIRES_NEW)
	public boolean beginTask() {
		return serialTaskDao.beginTask()>0;
	}
	
	/**
	 * 重置主任务
	 */
	@Transactional(value = TxType.REQUIRES_NEW)
	public boolean endTask() {
		if (this.serialTaskDao.endTask()>0) {
			log.info("endTask 主任务重置成功......");
			return true;
		}
		return false;
	}

	/**
	 * 任务执行主体
	 */
	@Transactional(value = TxType.REQUIRES_NEW)
	public void taskDo(SerialTaskEntity en) {

		if (en.getTaskCode().equals(SerialTaskEntity.TASK_taskCode_publisherClear)) {
			PublisherClearTaskParams params = JSONObject.parseObject(en.getTaskParams(),
					PublisherClearTaskParams.class);
			publisherOffsetService.clearDo(params.getOffsetOid(), en.getOid());
		}

		if (en.getTaskCode().equals(SerialTaskEntity.TASK_taskCode_publisherConfirm)) {
			PublisherConfirmTaskParams params = JSONObject.parseObject(en.getTaskParams(),
					PublisherConfirmTaskParams.class);
			publisherOffsetService.confirmDo(params.getOffsetOid(), en.getOid());
		}

		if (en.getTaskCode().equals(SerialTaskEntity.TASK_taskCode_publisherClose)) {
			PublisherCloseTaskParams params = JSONObject.parseObject(en.getTaskParams(),
					PublisherCloseTaskParams.class);
			this.publisherOffsetService.closeDo(params.getOffsetOid(), en.getOid());
		}
		
		if (en.getTaskCode().equals(SerialTaskEntity.TASK_taskCode_tnProfitInit)) {
			TnProfitDetailParams params = JSONObject.parseObject(en.getTaskParams(),
					TnProfitDetailParams.class);
			this.productDurationService.tnProfitInitDo(params.getProductOid());
		}

		if (en.getTaskCode().equals(SerialTaskEntity.TASK_taskCode_unlockAccrual)) {
			UnlockAccrualParams params = JSONObject.parseObject(en.getTaskParams(), UnlockAccrualParams.class);
			publisherHoldService.unlockAccrualDo(en.getOid(), params.getAccrualBaseDate());
		}

		if (en.getTaskCode().equals(SerialTaskEntity.TASK_taskCode_unlockRedeem)) {
			UnlockRedeemParams params = JSONObject.parseObject(en.getTaskParams(), UnlockRedeemParams.class);
			publisherHoldService.unlockRedeemDo(en.getOid(), params.getRedeemBaseDate());
		}

		if (en.getTaskCode().equals(SerialTaskEntity.TASK_taskCode_interest)) {
			InterestParams params = JSONObject.parseObject(en.getTaskParams(), InterestParams.class);
//			interestRateMethodService.interestDo(params.getIncomeAllocateOid(), params.getProductOid(), en.getOid());
			this.interestDistributionService.distributeInterestByProduct(params.getIncomeAllocateOid(), params.getProductOid());
		}
		if (en.getTaskCode().equals(SerialTaskEntity.TASK_taskCode_invest)) {
			InvestNotifyParams params = JSONObject.parseObject(en.getTaskParams(), InvestNotifyParams.class);
			investorInvestTradeOrderExtService.investCallBackDo(params.getOrderCode(), params.getReturnCode(),
					en.getOid());
		}
		if (en.getTaskCode().equals(SerialTaskEntity.TASK_taskCode_redeemInvest)) {
			RedeemInvestParams params = JSONObject.parseObject(en.getTaskParams(), RedeemInvestParams.class);
			investorInvestTradeOrderExtService.redeemInvestDo(params);
		}
		if (en.getTaskCode().equals(SerialTaskEntity.TASK_taskCode_redeem)) {
			RedeemNotifyParams params = JSONObject.parseObject(en.getTaskParams(), RedeemNotifyParams.class);
			investorInvestTradeOrderExtService.redeemDo(params.getOrderCode(), en.getOid());
		}
		if (en.getTaskCode().equals(SerialTaskEntity.TASK_taskCode_specialRedeem)) {
			RedeemNotifyParams params = JSONObject.parseObject(en.getTaskParams(), RedeemNotifyParams.class);
			investorInvestTradeOrderExtService.redeemDo(params.getOrderCode(), en.getOid());
		}
		if (en.getTaskCode().equals(SerialTaskEntity.TASK_taskCode_resetToday)) {
			resetTodayService.resetTodayDb();
		}
		if (en.getTaskCode().equals(SerialTaskEntity.TASK_taskCode_abandon)) {
			AbandonParams params = JSONObject.parseObject(en.getTaskParams(), AbandonParams.class);
			investorAbandonTradeOrderService.abandonDo(params, en.getOid());
		}
		
		
	}

	/**
	 * 子任务结束
	 */
	@Transactional(value = TxType.REQUIRES_NEW)
	public int updateFinish(String taskOid, Timestamp executeEndTime, String taskStatus, String taskError) {
		int i = this.serialTaskDao.updateFinish(taskOid, executeEndTime, taskStatus, taskError);
		if (i < 1) {
			log.error("updateFinish>>结束任务状态更新异常：taskOid={}, executeEndTime={}, taskStatus={}", taskOid, executeEndTime,
					taskStatus);
			throw new AMPException("结束任务状态更新异常");
		} else {
			log.error("updateFinish>>任务结束：taskOid={}, executeEndTime={}, taskStatus={}", taskOid, executeEndTime,
					taskStatus);
		}
		return i;
	}

	/**
	 * 子任务开始
	 */
	@Transactional(value = TxType.REQUIRES_NEW)
	public int updateBegin(String taskOid, Timestamp executeStartTime, String taskStatus) {
		int i = this.serialTaskDao.updateBegin(taskOid, executeStartTime, taskStatus);
		if (i < 1) {
			log.error("updateBegin>>开始任务状态更新异常：taskOid={}, executeStartTime={}, taskStatus={}", taskOid,
					executeStartTime, taskStatus);
			throw new AMPException(" 开始任务状态更新异常");
		} else {
			log.error("updateBegin>>开始任务开始运行：taskOid={}, executeStartTime={}, taskStatus={}", taskOid, executeStartTime,
					taskStatus);
		}
		return i;
	}

	/**
	 * 更新正在执行的子任务时间
	 */
	@Transactional(value = TxType.REQUIRES_NEW)
	public int updateTime(String taskOid) {
		int i = this.serialTaskDao.updateTime(taskOid);
		if (i < 1) {
			// throw AMPException.getException("更新子任务时间异常");
			log.info("updateTime>>taskOid={}更新子任务时间异常......", taskOid);
		} else {
			log.info("updateTime>>taskOid={}更新子任务时间成功......", taskOid);
		}
		return i;
	}
}
