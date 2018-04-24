package com.guohuai.ams.duration.fact.income.schedule;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.guohuai.ams.duration.assetPool.AssetPoolEntity;
import com.guohuai.ams.duration.assetPool.AssetPoolService;
import com.guohuai.ams.duration.fact.income.schedule.IncomeScheduleApplyResp.IncomeScheduleApplyRespBuilder;
import com.guohuai.ams.product.ProductDecimalFormat;
import com.guohuai.basic.common.Clock;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.web.view.BaseResp;
import com.guohuai.component.web.view.PageResp;

@Service
@Transactional
public class IncomeScheduleApplyService {
	
	@Autowired
	private IncomeScheduleApplyDao incomeScheduleApplyDao;
	@Autowired
	private IncomeScheduleService incomeScheduleService;
	@Autowired
	private AssetPoolService assetPoolService;
	
	
	/**
	 * 根据OID查询
	 * @param oid
	 * @return
	 */
	public IncomeScheduleApply findByOid(String oid){
		IncomeScheduleApply entity = this.incomeScheduleApplyDao.findOne(oid);
		if(null == entity){
			// 资产池收益分配排期申请不存在！(CODE:60012)
			throw AMPException.getException(60012);
		}
		return entity;
	}
	
	/**
	 * 后台分页查询
	 * @return
	 */
	public PageResp<IncomeScheduleApplyResp> queryPage(Specification<IncomeScheduleApply> spec, Pageable pageable) {
		Page<IncomeScheduleApply> enchs = this.incomeScheduleApplyDao.findAll(spec, pageable);
		
		PageResp<IncomeScheduleApplyResp> pageResp = new PageResp<>();		
		
		List<IncomeScheduleApplyResp> list = new ArrayList<IncomeScheduleApplyResp>();
		for (IncomeScheduleApply en : enchs) {
			IncomeScheduleApplyResp rep = new IncomeScheduleApplyRespBuilder()
					.oid(en.getOid())
					.basicDate(en.getBasicDate())
					.annualizedRate(en.getAnnualizedRate())
					.creator(en.getCreator())
					.createTime(en.getCreateTime())
					.approver(en.getApprover())
					.approverTime(en.getApproverTime())
					.type(en.getType())
					.status(en.getStatus())
					.build();
			list.add(rep);
		}
		
		pageResp.setTotal(enchs.getTotalElements());
		pageResp.setRows(list);
		return pageResp;
	}
	

	/**
	 * 获取详细
	 * @param oid
	 * @return
	 */
	public IncomeScheduleApplyResp detail(String oid) {
		IncomeScheduleApply en = this.findByOid(oid);
		
		IncomeScheduleApplyResp rep = new IncomeScheduleApplyRespBuilder()
				.oid(en.getOid())
				.basicDate(en.getBasicDate())
				.annualizedRate(en.getAnnualizedRate())
				.creator(en.getCreator())
				.createTime(en.getCreateTime())
				.approver(en.getApprover())
				.approverTime(en.getApproverTime())
				.status(en.getStatus())
				.type(en.getType())
				.build();
		return rep;
	}

	
	/**
	 * 删除审核
	 * @param oid
	 * @param operator 
	 * @return 
	 */
	public BaseResp approveDelete(String oid, String operator) {
		IncomeScheduleApply en = this.findByOid(oid);
		if (en.getType().equals(IncomeScheduleApply.TYPE_new)){
			IncomeSchedule schedule = this.incomeScheduleService.findByOid(en.getSchedulingOid());
			this.incomeScheduleService.delete(schedule.getOid());
		}
		
		en.setStatus(IncomeScheduleApply.STATUS_delete);
		en.setApprover(operator);
		en.setApproverTime(new Timestamp(Clock.DEFAULT.getCurrentTimeInMillis()));
		this.incomeScheduleApplyDao.save(en);
		return new BaseResp();
	}

	/**
	 * 分配收益排期新建申请
	 * @param form
	 * @param operator
	 * @return
	 */
	public BaseResp add(IncomeScheduleApplyForm form, String operator) {
		AssetPoolEntity assetPool = assetPoolService.getByOid(form.getAssetpoolOid());
		
		Date basicDate = DateUtil.parseToSqlDate(form.getIncomeDistrDate());
		
		incomeScheduleService.checkExist(form.getAssetpoolOid(), basicDate);
		
		IncomeSchedule schedule = new IncomeSchedule();
		schedule.setAssetPool(assetPool);
		schedule.setBasicDate(basicDate);
//		schedule.setAnnualizedRate(ProductDecimalFormat.divide(new BigDecimal(form.getProductAnnualYield())));
		schedule.setAnnualizedRate(new BigDecimal(form.getProductAnnualYield()));
		schedule.setStatus(IncomeSchedule.STATUS_toApprove);
		schedule = incomeScheduleService.saveEntity(schedule);
		
		IncomeScheduleApply apply = new IncomeScheduleApply();
		apply.setAssetPool(assetPool);
		apply.setSchedulingOid(schedule.getOid());
		apply.setBasicDate(basicDate);
		apply.setAnnualizedRate(new BigDecimal(form.getProductAnnualYield()));
		apply.setCreator(operator);
		apply.setCreateTime(new Timestamp(Clock.DEFAULT.getCurrentTimeInMillis()));
		apply.setType(IncomeScheduleApply.TYPE_new);
		apply.setStatus(IncomeScheduleApply.STATUS_toApprove);
		this.incomeScheduleApplyDao.save(apply);
		
		return new BaseResp();
	}
	
	/**
	 * 分配收益排期修改申请
	 * @param form
	 * @param operator
	 * @return
	 */
	public BaseResp update(IncomeScheduleUpdateForm form, String operator) {
//		Date basicDate = DateUtil.parseToSqlDate(form.getIncomeDistrDate());
		IncomeSchedule schedule = this.incomeScheduleService.findByOid(form.getOid());
		AssetPoolEntity assetPool = schedule.getAssetPool();

		checkExistApply(assetPool.getOid(), schedule.getBasicDate());	 // 判断未审核申请是否存在
		
		IncomeScheduleApply apply = new IncomeScheduleApply();
		apply.setAssetPool(assetPool);
		apply.setSchedulingOid(schedule.getOid());
		apply.setBasicDate(schedule.getBasicDate());
		apply.setAnnualizedRate(new BigDecimal(form.getProductAnnualYield()));
		apply.setCreator(operator);
		apply.setCreateTime(new Timestamp(Clock.DEFAULT.getCurrentTimeInMillis()));
		apply.setType(IncomeScheduleApply.TYPE_update);
		apply.setStatus(IncomeScheduleApply.STATUS_toApprove);
		this.incomeScheduleApplyDao.save(apply);
		
		return new BaseResp();
	}
	

	/**
	 * 分配收益排期删除申请
	 * @param form
	 * @param operator
	 * @return
	 */
	public BaseResp delete(String oid, String operator) {
		
		IncomeSchedule schedule = this.incomeScheduleService.findByOid(oid);
		AssetPoolEntity assetPool = schedule.getAssetPool();
		
		checkExistApply(assetPool.getOid(), schedule.getBasicDate());	 // 判断未审核申请是否存在
		
		IncomeScheduleApply apply = new IncomeScheduleApply();
		apply.setAssetPool(assetPool);
		apply.setSchedulingOid(schedule.getOid());
		apply.setBasicDate(schedule.getBasicDate());
		apply.setAnnualizedRate(schedule.getAnnualizedRate());
		apply.setCreator(operator);
		apply.setCreateTime(new Timestamp(Clock.DEFAULT.getCurrentTimeInMillis()));
		apply.setType(IncomeScheduleApply.TYPE_delete);
		apply.setStatus(IncomeScheduleApply.STATUS_toApprove);
		this.incomeScheduleApplyDao.save(apply);
		
		return new BaseResp();
	}

	/**
	 * 判断未审核申请是否存在
	 * @param assetpoolOid
	 * @param basicDate 
	 * @param statusToapprove
	 * @return
	 */
	private void checkExistApply(String assetpoolOid, Date basicDate) {
		IncomeScheduleApply en = this.incomeScheduleApplyDao.findByAssetPoolOidAndBasicDateAndStatus(assetpoolOid, basicDate, IncomeScheduleApply.STATUS_toApprove);
		
		if (en != null){
			// 资产池该日期收益分配排期还有未审核申请，请审核后再次申请!(CODE:60015)
			throw AMPException.getException(60015);
		}
	}

	/**
	 * 分配收益排期申请审核
	 * @param oid
	 * @param operator
	 * @param approveResult
	 * @return
	 */
	public BaseResp approve(String oid, String operator, String approveResult) {
		IncomeScheduleApply apply = this.findByOid(oid);
		IncomeSchedule schedule = this.incomeScheduleService.findByOid(apply.getSchedulingOid());
		
		if (approveResult.equals(IncomeScheduleApply.STATUS_pass)){
			if (apply.getType().equals(IncomeScheduleApply.TYPE_new)){
				schedule.setStatus(IncomeSchedule.STATUS_pass);
				this.incomeScheduleService.updateEntity(schedule);
			}else if (apply.getType().equals(IncomeScheduleApply.TYPE_update)){
				schedule.setStatus(IncomeSchedule.STATUS_pass);
				schedule.setAnnualizedRate(apply.getAnnualizedRate());
				this.incomeScheduleService.updateEntity(schedule);
			}else if (apply.getType().equals(IncomeScheduleApply.TYPE_delete)){
				this.incomeScheduleService.delete(schedule.getOid());
			}
			
			apply.setStatus(IncomeScheduleApply.STATUS_pass);
			
		}else if (approveResult.equals(IncomeScheduleApply.STATUS_reject)){
			if (apply.getType().equals(IncomeScheduleApply.TYPE_new)){
				this.incomeScheduleService.delete(schedule.getOid());
			}
			
			apply.setStatus(IncomeScheduleApply.STATUS_reject);
		}
		
		apply.setApprover(operator);
		apply.setApproverTime(new Timestamp(Clock.DEFAULT.getCurrentTimeInMillis()));
		
		return new BaseResp();
	}

}
