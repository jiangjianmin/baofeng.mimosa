package com.guohuai.ams.system.config.risk.warning.options;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.ams.system.config.risk.cate.RiskCate;
import com.guohuai.ams.system.config.risk.indicate.RiskIndicate;
import com.guohuai.ams.system.config.risk.indicate.RiskIndicateService;
import com.guohuai.ams.system.config.risk.warning.RiskWarning;
import com.guohuai.ams.system.config.risk.warning.RiskWarningService;
import com.guohuai.component.exception.AMPException;

@Service
@Transactional
public class RiskWarningOptionsService {
	@Autowired
	private RiskWarningOptionsDao riskWarningOptionsDao;
	@Autowired
	private RiskIndicateService riskIndicateService;
	@Autowired
	private RiskWarningService riskWarningService;

	@Transactional
	public List<RiskWarningOptionsResp> save(RiskWarningOptionsForm form) {
		RiskIndicate indicate = this.riskIndicateService.get(form.getIndicateOid());
		RiskWarning warning = null;
		String warningOid = form.getWarningOid();
		if (StringUtils.isBlank(warningOid)) { // 添加风险预警
			warning = RiskWarning.builder().indicate(indicate).title(form.getWarningTitle()).build();
			warning = riskWarningService.save(warning);
		} else {
			warning = RiskWarning.builder().indicate(indicate).title(form.getWarningTitle()).build();
			warning.setOid(warningOid);
			warning = riskWarningService.save(warning); // 修改风险预警
			this.riskWarningOptionsDao.deleteByWarning(warningOid); // 删除预警下面所有配置项
		}

		int seq = 0;

		List<RiskWarningOptionsResp> list = new ArrayList<RiskWarningOptionsResp>();

		List<RiskWarningOptionsForm.Option> options = form.getOptions();
		int size = null == options ? 0 : options.size();
		if (size > 0) {
			for (RiskWarningOptionsForm.Option option : options) {
				RiskWarningOptions.RiskWarningOptionsBuilder builder = RiskWarningOptions.builder();

				builder.warning(warning).seq(seq).wlevel(option.getWlevel());
				builder.param0(option.getParam0()).param1(option.getParam1()).param2(option.getParam2()).param3(option.getParam3());
				RiskWarningOptions ro = builder.build();
				ro = this.riskWarningOptionsDao.save(ro);
				list.add(new RiskWarningOptionsResp(ro));
				seq++;
			}
		}

		return list;
	}

	@Transactional
	public void batchDelete(String warningOid) {
		this.riskWarningOptionsDao.deleteByWarning(warningOid);
		this.riskWarningService.delete(warningOid);
	}

	@Transactional
	public RiskWarningOptionsForm preUpdate(String warningOid) {
		RiskWarning warning = this.riskWarningService.get(warningOid);
		List<RiskWarningOptions> list = this.riskWarningOptionsDao.search(warning);

		if (null == list || list.size() == 0) {
			throw new AMPException(String.format("No data found for waring '%s'", warningOid));
		}

		RiskWarningOptionsForm form = new RiskWarningOptionsForm(list);
		
		return form;
	}

	/*
	@Transactional
	public List<RiskOptionsCollect> preCollect(String type) {

		List<RiskOptionsCollect> view = new ArrayList<RiskOptionsCollect>();

		List<RiskWarningOptions> options = this.riskWarningOptionsDao.search(type);

		if (null != options && options.size() > 0) {

			Map<String, Integer> dr = new HashMap<String, Integer>();

			for (RiskWarningOptions o : options) {
				if (!dr.containsKey(o.getIndicate().getCate().getOid())) {
					dr.put(o.getIndicate().getCate().getOid(), view.size());

					RiskOptionsCollect noc = new RiskOptionsCollect();
					noc.setCateOid(o.getIndicate().getCate().getOid());
					noc.setCateTitle(o.getIndicate().getCate().getTitle());
					view.add(noc);
				}

				RiskOptionsCollect collect = view.get(dr.get(o.getIndicate().getCate().getOid()));

				if (!dr.containsKey(o.getIndicate().getOid())) {
					dr.put(o.getIndicate().getOid(), collect.getIndicates().size());
					RiskOptionsCollect.Indicate i = new RiskOptionsCollect.Indicate();
					i.setIndicateOid(o.getIndicate().getOid());
					i.setIndicateTitle(o.getIndicate().getTitle());
					i.setIndicateType(o.getIndicate().getDataType());
					i.setIndicateUnit(o.getIndicate().getDataUnit());
					collect.getIndicates().add(i);
				}

				RiskOptionsCollect.Indicate indicate = collect.getIndicates().get(dr.get(o.getIndicate().getOid()));

				// if (indicate.getIndicateType().equals(RiskIndicate.DATA_TYPE_Text)) {
				RiskOptionsCollect.Indicate.Options option = new RiskOptionsCollect.Indicate.Options();
				option.setOid(o.getOid());
				option.setTitle(o.showTitle());
				option.setDft(o.getDft());
				indicate.getOptions().add(option);
				// }

			}

		}

		return view;
	}
*/
	
	@Transactional
	public List<RiskWarningOptionsView> showview(String type, String keyword) {

		List<RiskWarningOptionsView> view = new ArrayList<RiskWarningOptionsView>();

		List<RiskWarningOptions> options = this.riskWarningOptionsDao.search(type, String.format("%%%s%%", keyword));

		if (null != options && options.size() > 0) {
			Map<String, String> cmap = new HashMap<String, String>();
			Map<String, String> imap = new HashMap<String, String>();
			Map<String, String> wmap = new HashMap<String, String>();
//			Map<String, Map<String, Map<String, List<RiskWarningOptionsView>>>> cmap = new HashMap<String, Map<String, Map<String, List<RiskWarningOptionsView>>>>();
			for (RiskWarningOptions o : options) {
				RiskWarning warning = o.getWarning();
				String warningOid = warning.getOid();
				RiskIndicate indicate = warning.getIndicate();
				String indicateOid = indicate.getOid();
				RiskCate cate = indicate.getCate();
				String cateOid = cate.getOid();

				boolean showCate = false;
				if (!cmap.containsKey(cate.getOid())) {
//					cmap.put(cateOid, new HashMap<String, Map<String, List<RiskWarningOptionsView>>>());
					cmap.put(cateOid, "");
					showCate = true;
				}

				boolean showIndicate = false;
				if (!imap.containsKey(indicateOid)) {
//					imap.put(indicateOid, new HashMap<String, List<RiskWarningOptionsView>>());
					imap.put(indicateOid, "");
					showIndicate = true;
				}
				
				boolean showWarning = false;
//				Map<String, List<RiskWarningOptionsView>> wmap = imap.get(indicateOid);
				if (!wmap.containsKey(warningOid)) {
//					wmap.put(warningOid, new ArrayList<RiskWarningOptionsView>());
					wmap.put(warningOid, "");
					showWarning = true;
				}
				RiskWarningOptionsView v = new RiskWarningOptionsView();
				v.setCateOid(cateOid);
				v.setCateTitle(cate.getTitle());
				v.setShowCate(showCate);
				
				v.setIndicateOid(indicateOid);
				v.setIndicateTitle(indicate.getTitle());
				v.setIndicateDataType(indicate.getDataType());
				v.setIndicateDataUnit(indicate.getDataUnit());
				v.setShowIndicate(showIndicate);
				
				v.setWarningOid(warningOid);
				v.setWaringTitle(warning.getTitle());
				v.setShowWarning(showWarning);
				
				v.setOptionsOid(o.getOid());
				v.setOptionsTitle(o.showTitle());
				v.setOptionsWlevel(o.getWlevel());
				v.setShowOptions(true);
				view.add(v);
			}

		}

		return view;
	}

	@Transactional
	public List<RiskWarningOptions> search(String type) {
		return this.riskWarningOptionsDao.search(type);
	}

}
