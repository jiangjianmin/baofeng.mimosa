package com.guohuai.ams.system.config.risk.warning.collect;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guohuai.ams.enums.RiskWarningCollectLevel;
import com.guohuai.ams.investment.Investment;
import com.guohuai.ams.investment.pool.InvestmentPoolService;
import com.guohuai.ams.system.config.risk.cate.RiskCate;
import com.guohuai.ams.system.config.risk.indicate.RiskIndicate;
import com.guohuai.ams.system.config.risk.warning.RiskWarning;
import com.guohuai.ams.system.config.risk.warning.RiskWarningDao;
import com.guohuai.ams.system.config.risk.warning.options.RiskWarningOptions;
import com.guohuai.ams.system.config.risk.warning.options.RiskWarningOptionsDao;
import com.guohuai.component.util.DateUtil;

@Service
@Transactional
public class RiskWarningCollectService {

	@Autowired
	InvestmentPoolService investmentPoolService;

	@Autowired
	RiskWarningDao riskWarningDao;

	@Autowired
	RiskWarningOptionsDao riskWarningOptionsDao;

	@Autowired
	RiskWarningCollectDao riskWarningCollectDao;

	/**
	 * 获得风险预警管理列表
	 * 
	 * @param spec
	 * @param pageable
	 * @return
	 */
	public RiskWarningCollectListResp list(Specification<Investment> spec, Pageable pageable) {
		List<RiskWarningCollectListInfoResp> res = new ArrayList<RiskWarningCollectListInfoResp>();
		Page<Investment> pageData = investmentPoolService.getInvestmentList(spec, pageable);
		if (null == pageData || 0 == pageData.getSize()) {
			return new RiskWarningCollectListResp(res);
		}
		for (Investment investment : pageData) {
			List<RiskWarningCollect> list = riskWarningCollectDao.findByRelative(investment.getOid());
			int levelCode = 0;
			for (RiskWarningCollect riskWarningCollect : list) {
				int tempCode = RiskWarningCollectLevel.getLeveCode(riskWarningCollect.getWlevel());
				if (tempCode > levelCode) {
					levelCode = tempCode;
				}
			}
			RiskWarningCollectListInfoResp temp = RiskWarningCollectListInfoResp.builder().oid(investment.getOid())
					.sn(investment.getSn()).name(investment.getName()).state(investment.getLifeState())
					.level(RiskWarningCollectLevel.getLevel(levelCode)).build();
			res.add(temp);
		}
		return new RiskWarningCollectListResp(res,pageData.getTotalElements());
	}

	public List<RiskWarningCollectDetResp> detail(String oid) {
		List<RiskWarningCollectDetResp> res = new ArrayList<RiskWarningCollectDetResp>();
		List<RiskWarningCollect> list = riskWarningCollectDao.findByRelative(oid);
		for (RiskWarningCollect riskWarningCollect : list) {
			RiskWarningCollectDetResp temp = null;
			temp = RiskWarningCollectDetResp.builder()
					.title(riskWarningCollect.getRiskWarning().getIndicate().getTitle())
					.riskLevel(riskWarningCollect.getWlevel()).riskData(riskWarningCollect.getCollectData())
					.riskUnit(riskWarningCollect.getRiskWarning().getIndicate().getDataUnit())
					.handleLevel(riskWarningCollect.getHandleLevel()).build();
			res.add(temp);
		}
		return res;
	}

	public RiskWaringCollectOptionResp collectOption() {
		RiskWaringCollectOptionResp res = new RiskWaringCollectOptionResp();
		Set<RiskWarning> riskList = new HashSet<RiskWarning>();
		Set<RiskIndicate> indicateList = new HashSet<RiskIndicate>();
		Set<RiskCate> cateList = new HashSet<RiskCate>();
		List<RiskWarningOptions> optinList = riskWarningOptionsDao.findAll();
		for (RiskWarningOptions riskWarningOptions : optinList) {
			if (RiskIndicate.STATE_Enable.equals(riskWarningOptions.getWarning().getIndicate().getState()))
				riskList.add(riskWarningOptions.getWarning());
		}
		for (RiskWarning riskWarning : riskList) {
			indicateList.add(riskWarning.getIndicate());
			cateList.add(riskWarning.getIndicate().getCate());
		}
		res.setRiskWarningList(riskList);
		res.setIndicateList(indicateList);
		res.setCateList(cateList);
		res.setOptinList(optinList);
		return res;
	}

	public void collect(RiskWarningCollectForm form) {
		RiskWarning riskWarning = riskWarningDao.findOne(form.getRiskWaring());
		if (null == riskWarning)
			throw new RuntimeException();
		String riskData = "";
		if (RiskIndicate.DATA_TYPE_NumRange.equals(riskWarning.getIndicate().getDataType())) {
			riskData = form.getRiskWaringDataInput();
		} else {
			RiskWarningOptions options = riskWarningOptionsDao.findOne(form.getRiskWaringDataSelect());
			riskData = options.getParam0();
		}
		RiskWarningCollect entity = riskWarningCollectDao.findByRelativeAndRiskWarning(form.getTargetOid(),
				riskWarning);
		if (entity == null) {
			entity = RiskWarningCollect.builder().riskWarning(riskWarning).relative(form.getTargetOid())
					.collectOption(form.getOptionOid()).collectData(riskData).createTime(DateUtil.getSqlCurrentDate())
					.wlevel(form.getWLevel()).build();
		} else {
			entity.setCollectOption(form.getOptionOid());
			entity.setCollectData(riskData);
			entity.setCreateTime(DateUtil.getSqlCurrentDate());
			entity.setWlevel(form.getWLevel());
			entity.setHandleLevel(null);
		}
		riskWarningCollectDao.save(entity);
	}
}
