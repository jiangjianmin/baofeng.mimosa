package com.guohuai.ams.investment;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.guohuai.ams.investment.manage.InvestmentCheckDetResp;
import com.guohuai.ams.investment.manage.InvestmentCheckListConfirmForm;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.StringUtil;
import com.guohuai.file.File;
import com.guohuai.file.FileService;
import com.guohuai.file.SaveFileForm;
import com.guohuai.operate.api.AdminSdk;
import com.guohuai.operate.api.objs.admin.AdminObj;

@Service
@Transactional
public class InvestmentMeetingCheckService {

	@Autowired
	private InvestmentMeetingCheckDao investmentMeetingCheckDao;

	@Autowired
	private InvestmentService investmentService;

	@Autowired
	private FileService fileService;

	@Autowired
	private AdminSdk adminsSdk;

	/**
	 * 获得投资标的过会检查项列表
	 * 
	 * @param spec
	 * @param pageable
	 * @return
	 */
	public Page<InvestmentMeetingCheck> getMeetingCheckList(Specification<InvestmentMeetingCheck> spec,
			Pageable pageable) {
		return investmentMeetingCheckDao.findAll(spec, pageable);
	}

	/**
	 * 获得投资标的过会检查项详情
	 * 
	 * @param oid
	 * @return
	 */
	public InvestmentMeetingCheck getMeetingChecktDet(String oid) {
		return investmentMeetingCheckDao.findOne(oid);
	}

	/**
	 * 新建或更新投资标的过会检查项
	 * 
	 * @param entity
	 * @param operator
	 * @return
	 */
	public InvestmentMeetingCheck saveOrUpdateMeetingCheck(InvestmentMeetingCheck entity) {
		return this.investmentMeetingCheckDao.save(entity);
	}

	/**
	 * 根据标的ID获得检查项
	 * 
	 * @param investmentOid
	 * @return
	 */
	public List<InvestmentCheckDetResp> getMeetingCheckListByInvestmentOid(String investmentOid, String state) {
		List<InvestmentCheckDetResp> res = new ArrayList<InvestmentCheckDetResp>();
		Investment investment = investmentService.getInvestment(investmentOid);
		if (null == investment) {
			throw new RuntimeException();
		}
		List<InvestmentMeetingCheck> lists = null;
		if (null == state) {
			lists = investmentMeetingCheckDao.findByInvestment(investment);
		} else {
			lists = investmentMeetingCheckDao.findByInvestmentAndState(investment, state);
		}
		for (InvestmentMeetingCheck entity : lists) {
			InvestmentCheckDetResp det = new InvestmentCheckDetResp(entity);
			if (entity.getChecker() != null) {
				AdminObj admin = adminsSdk.getAdmin(entity.getChecker());
				det.setChecker(admin.getName());
			}
			if (!StringUtils.isEmpty(entity.getCheckFile())) {
				List<File> files = fileService.list(entity.getCheckFile(), 1);
				if (files != null && files.size() > 0)
					det.setFile(files.get(0).getFurl());
			}
			res.add(det);
		}
		return res;
	}

	/**
	 * 检查项确认
	 * 
	 * @param forms
	 * @param operator
	 */
	public void confirmCheckList(List<InvestmentCheckListConfirmForm> forms, String operator) {
		for (InvestmentCheckListConfirmForm form : forms) {
			if (null == form.getChecked() || !form.getChecked()) {
				continue;
			}
			InvestmentMeetingCheck check = this.getMeetingChecktDet(form.getId());
			if (null == check)
				continue;
			check.setState(InvestmentMeetingCheck.MEETINGCHEC_STATUS_check);
			check.setCheckDesc(form.getRemark());
			if (!StringUtils.isEmpty(form.getFile())) {
				String fkey = StringUtil.uuid();
				List<SaveFileForm> fileForms = new ArrayList<SaveFileForm>();
				SaveFileForm fileform = new SaveFileForm();
				fileform.setFurl(form.getFile());
				fileform.setName(form.getFileName());
				fileform.setSize(Long.valueOf(form.getFileSize()));
				fileForms.add(fileform);
				fileService.save(fileForms, fkey, File.CATE_User, operator);
				check.setCheckFile(fkey);
			}
			check.setCheckTime(DateUtil.getSqlCurrentDate());
			check.setChecker(operator);
			this.saveOrUpdateMeetingCheck(check);
		}
	}
}
