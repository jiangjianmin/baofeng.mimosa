package com.guohuai.mmp.investor.referprofit;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.guohuai.component.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

@Transactional
@Service
@Slf4j
public class ProfitRuleService {
	
	@Autowired
	private ProfitRuleDao profitRuleDao;
	
	/**
	 * 
	 * @author yihonglei
	 * @Title: saveProfitRule
	 * @Description: 奖励发放规则保存
	 * @param form
	 * @return int
	 * @date 2017年6月16日 下午2:57:32
	 * @since  1.0.0
	 */
	public int saveProfitRule(ProfitRuleForm form) {
		log.info("<<-------奖励发放规则保存开始!开始时间:{}----------->>",new Date());
		// 保存结果
		int result = 1;
		try {
			if (checkProfitRuleForm(form)) {
				// 先清空表
				profitRuleDao.deleteAll();
				// 保存
				ProfitRuleEntity profitRuleEntity = new ProfitRuleEntity();
				profitRuleEntity.setOid(StringUtil.uuid());
				profitRuleEntity.setFirstFactor(form.getFirstFactor());
				profitRuleEntity.setSecondFactor(form.getSecondFactor());
				profitRuleEntity.setInvestorFactor(form.getInvestorFactor());
				profitRuleEntity.setDemandFactor(form.getDemandFactor());
				profitRuleEntity.setDepositFactor(form.getDepositFactor());
				profitRuleEntity.setUpdateTime(new Date());
				profitRuleEntity.setCreateTime(new Date());
				
				log.info("奖励发放规则保存对象:{}",JSONObject.toJSONString(profitRuleEntity));
				profitRuleDao.save(profitRuleEntity);
			} else {
				result = 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info("<<-------奖励发放规则保存结束!结束时间:{},结果为:{}----------->>", new Date(), result>0);
		
		return result;
	}
	
	// 参数校验
	private boolean checkProfitRuleForm(ProfitRuleForm form) {
		boolean flag = true;
		try {
			// 一级邀请人奖励比例
			if (null == form.getFirstFactor() || "".equals(form.getFirstFactor()) 
					|| new BigDecimal(form.getFirstFactor().toString()).compareTo(BigDecimal.ZERO) < 0
					|| new BigDecimal(form.getFirstFactor().toString()).compareTo(new BigDecimal(80)) > 0 ) {
				flag = false;
			}
			// 二级邀请人奖励比例
			if (null == form.getSecondFactor() || "".equals(form.getSecondFactor())
					|| new BigDecimal(form.getSecondFactor().toString()).compareTo(BigDecimal.ZERO) < 0
					|| new BigDecimal(form.getSecondFactor().toString()).compareTo(new BigDecimal(80)) > 0 ) {
				flag = false;
			}
			// 投资人奖励比例
			if (null == form.getInvestorFactor() || "".equals(form.getInvestorFactor())
					|| new BigDecimal(form.getInvestorFactor().toString()).compareTo(BigDecimal.ZERO) < 0
					|| new BigDecimal(form.getInvestorFactor().toString()).compareTo(new BigDecimal(80)) > 0 ) {
				flag = false;
			}
			// 活期奖励比例
			if (null == form.getDemandFactor() || "".equals(form.getDemandFactor())
					|| new BigDecimal(form.getDemandFactor().toString()).compareTo(BigDecimal.ZERO) < 0
					|| new BigDecimal(form.getDemandFactor().toString()).compareTo(new BigDecimal(80)) > 0 ) {
				flag = false;
			}
			// 定期奖励比例
			if (null == form.getDepositFactor() || "".equals(form.getDepositFactor())
					|| new BigDecimal(form.getDepositFactor().toString()).compareTo(BigDecimal.ZERO) < 0
					|| new BigDecimal(form.getDepositFactor().toString()).compareTo(new BigDecimal(80)) > 0 ) {
				flag = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			flag = true;
		}
		
		return flag;
	}
	/**
	 * 
	 * @author yihonglei
	 * @Title: checkProfitRule
	 * @Description: 校验奖励收益规则各项参数如果均为0，二级邀请活动下架，否则，不下架
	 * @return boolean
	 * @date 2017年6月20日 下午6:26:11
	 * @since  1.0.0
	 */
	public boolean checkProfitRule() {
		boolean flag = true;// 二级邀请活动是否下架标识，true 上架，false 下架
		List<ProfitRuleEntity> profitRuleList = this.profitRuleDao.findAll();
		
		if (profitRuleList.size() > 0) {
			ProfitRuleEntity profitRuleEntity = profitRuleList.get(0);
			BigDecimal investorFactor = profitRuleEntity.getInvestorFactor();
			BigDecimal firstFactor = profitRuleEntity.getFirstFactor();
			BigDecimal secondFactor = profitRuleEntity.getSecondFactor();
			BigDecimal demandFactor = profitRuleEntity.getDemandFactor();
			BigDecimal depositFactor = profitRuleEntity.getDepositFactor();
			
			if (investorFactor.compareTo(BigDecimal.ZERO) < 1
					&& firstFactor.compareTo(BigDecimal.ZERO) < 1
					&& secondFactor.compareTo(BigDecimal.ZERO) < 1
					&& demandFactor.compareTo(BigDecimal.ZERO) < 1
					&& depositFactor.compareTo(BigDecimal.ZERO) < 1) {// 都等于0时，表示二级邀请活动下架
				flag = false;
			}
		} else {
			flag = false;
		}
		
		return flag;
	}
	/**
	 * 
	 * @author yihonglei
	 * @Title: queryProfitRule
	 * @Description: 查询奖励收益规则
	 * @return ProfitRuleEntity
	 * @date 2017年6月26日 下午9:47:26
	 * @since  1.0.0
	 */
	public ProfitRuleEntity queryProfitRule() {
		List<ProfitRuleEntity> profitRuleList = this.profitRuleDao.findAll();
		if (profitRuleList.size() > 0) {
			ProfitRuleEntity profitRuleEntity = profitRuleList.get(0);
			return profitRuleEntity;
		}
		
		return null;
	}
}
