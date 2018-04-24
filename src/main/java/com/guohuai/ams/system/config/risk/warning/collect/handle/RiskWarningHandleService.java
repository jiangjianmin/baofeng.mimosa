package com.guohuai.ams.system.config.risk.warning.collect.handle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guohuai.ams.enums.RiskWarningCollectLevel;
import com.guohuai.ams.investment.Investment;
import com.guohuai.ams.investment.InvestmentService;
import com.guohuai.ams.system.config.risk.warning.collect.RiskWarningCollect;
import com.guohuai.ams.system.config.risk.warning.collect.RiskWarningCollectDao;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.StringUtil;
import com.guohuai.file.File;
import com.guohuai.file.FileService;
import com.guohuai.file.SaveFileForm;

@Service
@Transactional
public class RiskWarningHandleService {

	@Autowired
	RiskWarningCollectDao riskWarningCollectDao;

	@Autowired
	RiskWarningHandleDao riskWarningHandleDao;

	@Autowired
	InvestmentService investmentService;

	@Autowired
	FileService fileService;
	
	public Map<String,String> targetList(){
		List<RiskWarningCollect> allLists = riskWarningCollectDao.findAll();
		Map<String, String> investments = new HashMap<String, String>();
		for (RiskWarningCollect riskWarningCollect : allLists) {
			Investment investment = investmentService.getInvestment(riskWarningCollect.getRelative());
			if (null != investment){
				investments.put(investment.getOid(), investment.getName());
			}
		}
		return investments;
	}

	public RiskWarningHandleListResp list(Specification<RiskWarningCollect> spec, Pageable pageable) {
		List<RiskWarningHandleDetResp> dets = new ArrayList<RiskWarningHandleDetResp>();
		Page<RiskWarningCollect> list = riskWarningCollectDao.findAll(spec, pageable);
		for (RiskWarningCollect entity : list) {
			RiskWarningHandleDetResp temp = new RiskWarningHandleDetResp();
			temp.setOid(entity.getOid());
			temp.setRiskType(entity.getRiskWarning().getIndicate().getCate().getTitle());
			temp.setRiskName(entity.getRiskWarning().getIndicate().getTitle());
			temp.setRiskDet(entity.getRiskWarning().getTitle());
			temp.setWLevel(entity.getWlevel());
			temp.setHandleLevel(entity.getHandleLevel());
			temp.setRiskData(entity.getCollectData());
			temp.setRiskUnit(entity.getRiskWarning().getIndicate().getDataUnit());
			temp.setRelative(entity.getRelative());
			Investment investment = investmentService.getInvestment(entity.getRelative());
			if (null != investment){
				temp.setRelativeName(investment.getName());
			}
			dets.add(temp);
		}
		RiskWarningHandleListResp res = new RiskWarningHandleListResp(dets,list.getTotalElements());
		return res ;
	}

	public RiskWarningHandleHisListResp hisList(Specification<RiskWarningHandle> spec, Pageable pageable) {
		List<RiskWarningHandleHisDetResp> res = new ArrayList<RiskWarningHandleHisDetResp>();
		Page<RiskWarningHandle> pageData = riskWarningHandleDao.findAll(spec, pageable);
		for (RiskWarningHandle entity : pageData) {
			RiskWarningHandleHisDetResp temp = new RiskWarningHandleHisDetResp();
			temp.setOid(entity.getOid());
			temp.setRelative(entity.getRiskWarningCollect().getRelative());
			temp.setRiskType(entity.getRiskWarningCollect().getRiskWarning().getIndicate().getCate().getTitle());
			temp.setRiskName(entity.getRiskWarningCollect().getRiskWarning().getIndicate().getTitle());
			temp.setRiskDet(entity.getRiskWarningCollect().getRiskWarning().getTitle());
			temp.setHandle(entity.getHandle());
			temp.setCreateTime(entity.getCreateTime());
//			temp.setReport(entity.getReport());
			temp.setSummary(entity.getSummary());
//			temp.setMeeting(entity.getMeeting());
			if (!StringUtils.isEmpty(entity.getReport())) {
				List<File> files = fileService.list(entity.getReport(), 1);
				if (files != null && files.size() > 0){
					temp.setReport(files.get(0).getFurl());
					temp.setReportName(files.get(0).getName());
				}
			}
			if (!StringUtils.isEmpty(entity.getMeeting())) {
				List<File> files = fileService.list(entity.getMeeting(), 1);
				if (files != null && files.size() > 0){
					temp.setMeeting(files.get(0).getFurl());
					temp.setMeetingName(files.get(0).getName());
				}
			}
			Investment investment = investmentService.getInvestment(entity.getRiskWarningCollect().getRelative());
			if (null != investment)
				temp.setRelativeName(investment.getName());
			res.add(temp);
		}
		return new RiskWarningHandleHisListResp(res,pageData.getTotalElements());
	}

	public void handle(RiskWarningHandleForm form, String operator) {
		RiskWarningCollect collect = riskWarningCollectDao.findOne(form.getOid());
		if (collect == null)
			throw new RuntimeException();
		if (RiskWarningHandle.HANDLE_CLEARLEVEL.equals(form.getHandle())) {
			// 预警解除
			collect.setHandleLevel(collect.getWlevel());
			collect.setWlevel(RiskWarningCollect.COLLECT_LEVEL_NONE);
		} else if (RiskWarningHandle.HANDLE_DOWNLEVEL.equals(form.getHandle())) {
			// 预警等级降级
			int riskCode = RiskWarningCollectLevel.getLeveCode(collect.getWlevel());
			if (riskCode == 0) {
				return;
			}
			collect.setHandleLevel(collect.getWlevel());
			collect.setWlevel(RiskWarningCollectLevel.getLevel(riskCode - 1));
		} else if (RiskWarningHandle.HANDLE_KEEPLEVEL.equals(form.getHandle())) {
			// 保留等级
			collect.setHandleLevel(collect.getWlevel());
		}
		String reportFkey = null;
		if (!StringUtils.isEmpty(form.getReport())) {
			reportFkey = StringUtil.uuid();
			List<SaveFileForm> fileForms = new ArrayList<SaveFileForm>();
			SaveFileForm fileform = new SaveFileForm();
			fileform.setFurl(form.getReport());
			fileform.setName(form.getReportName());
			fileform.setSize(Long.valueOf(form.getReportSize()));
			fileForms.add(fileform);
			fileService.save(fileForms, reportFkey, File.CATE_User, operator);
		}
		String meetingFkey = null;
		if (!StringUtils.isEmpty(form.getMeeting())) {
			meetingFkey = StringUtil.uuid();
			List<SaveFileForm> fileForms = new ArrayList<SaveFileForm>();
			SaveFileForm fileform = new SaveFileForm();
			fileform.setFurl(form.getMeeting());
			fileform.setName(form.getMeetingName());
			fileform.setSize(Long.valueOf(form.getMeetingSize()));
			fileForms.add(fileform);
			fileService.save(fileForms, meetingFkey, File.CATE_User, operator);
		}
		RiskWarningHandle handle = RiskWarningHandle.builder().riskWarningCollect(collect).handle(form.getHandle())
				.summary(form.getSummary()).createTime(DateUtil.getSqlCurrentDate()).report(reportFkey)
				.meeting(meetingFkey).build();
		riskWarningHandleDao.save(handle);
		riskWarningCollectDao.save(collect);
	}
}
