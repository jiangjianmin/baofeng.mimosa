package com.guohuai.ams.system.config.risk.options;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.ams.system.config.risk.indicate.RiskIndicate;
import com.guohuai.ams.system.config.risk.indicate.RiskIndicateService;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.StringUtil;

@Service
public class RiskOptionsService {

	@Autowired
	private RiskOptionsDao riskOptionsDao;
	@Autowired
	private RiskIndicateService riskIndicateService;

	@Transactional
	public List<RiskOptionsResp> save(RiskOptionsForm form) {

		RiskIndicate indicate = this.riskIndicateService.get(form.getIndicateOid());
		this.riskOptionsDao.deleteByIndicate(indicate);

		int seq = 0;

		List<RiskOptionsResp> list = new ArrayList<RiskOptionsResp>();

		if (null != form.getOptions() && form.getOptions().size() > 0) {
			for (RiskOptionsForm.Option option : form.getOptions()) {
				RiskOptions.RiskOptionsBuilder builder = RiskOptions.builder().oid(StringUtil.uuid());
				builder.indicate(indicate).score(option.getScore()).dft("NO").seq(seq);
				builder.param0(option.getParam0()).param1(option.getParam1()).param2(option.getParam2()).param3(option.getParam3());
				RiskOptions ro = builder.build();
				ro = this.riskOptionsDao.save(ro);
				list.add(new RiskOptionsResp(ro));
				seq++;
			}
		}

		RiskOptions.RiskOptionsBuilder builder = RiskOptions.builder().oid(StringUtil.uuid());
		builder.indicate(indicate).score(form.getDftScore()).dft("YES").seq(seq);
		RiskOptions dro = builder.build();
		dro = this.riskOptionsDao.save(dro);
		list.add(new RiskOptionsResp(dro));
		return list;
	}

	@Transactional
	public void batchDelete(String indicateOid) {
		RiskIndicate indicate = this.riskIndicateService.get(indicateOid);
		this.riskOptionsDao.deleteByIndicate(indicate);
	}

	@Transactional
	public RiskOptionsForm preUpdate(String indicateOid) {
		RiskIndicate indicate = this.riskIndicateService.get(indicateOid);
		List<RiskOptions> list = this.riskOptionsDao.search(indicate);

		if (null == list || list.size() == 0) {
			throw new AMPException(String.format("No data found for indicate '%s'", indicateOid));
		}

		RiskOptionsForm form = new RiskOptionsForm();
		form.setOptions(new ArrayList<RiskOptionsForm.Option>());

		for (RiskOptions o : list) {
			if (o.getDft().equals("YES")) {
				form.setCateOid(o.getIndicate().getCate().getOid());
				form.setCateTitle(o.getIndicate().getCate().getTitle());
				form.setIndicateOid(o.getIndicate().getOid());
				form.setIndicateTitle(o.getIndicate().getTitle());
				form.setIndicateDataType(o.getIndicate().getDataType());
				form.setIndicateDataUnit(o.getIndicate().getDataUnit());
				form.setDftScore(o.getScore());
			}
			if (o.getDft().equals("NO")) {
				RiskOptionsForm.Option option = new RiskOptionsForm.Option();
				option.setScore(o.getScore());
				option.setParam0(o.getParam0());
				option.setParam1(o.getParam1());
				option.setParam2(o.getParam2());
				option.setParam3(o.getParam3());
				form.getOptions().add(option);
			}
		}

		return form;
	}

	@Transactional
	public List<RiskOptionsCollect> preCollect(String type) {

		List<RiskOptionsCollect> view = new ArrayList<RiskOptionsCollect>();

		List<RiskOptions> options = this.riskOptionsDao.search(type);

		if (null != options && options.size() > 0) {

			Map<String, Integer> dr = new HashMap<String, Integer>();

			for (RiskOptions o : options) {
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

	@Transactional
	public List<RiskOptionsView> showview(String type, String keyword) {

		List<RiskOptionsView> view = new ArrayList<RiskOptionsView>();

		List<RiskOptions> options = this.riskOptionsDao.search(type, String.format("%%%s%%", keyword));

		if (null != options && options.size() > 0) {
			LinkedHashMap<String, LinkedHashMap<String, List<RiskOptionsView>>> cmap = new LinkedHashMap<String, LinkedHashMap<String, List<RiskOptionsView>>>();
			for (RiskOptions o : options) {

				boolean showCate = false;
				if (!cmap.containsKey(o.getIndicate().getCate().getOid())) {
					cmap.put(o.getIndicate().getCate().getOid(), new LinkedHashMap<String, List<RiskOptionsView>>());
					showCate = true;
				}

				LinkedHashMap<String, List<RiskOptionsView>> imap = cmap.get(o.getIndicate().getCate().getOid());
				if (!imap.containsKey(o.getIndicate().getOid())) {
					List<RiskOptionsView> nv = new ArrayList<RiskOptionsView>();
					RiskOptionsView tv = new RiskOptionsView();
					tv.setCateOid(o.getIndicate().getCate().getOid());
					tv.setCateTitle(o.getIndicate().getCate().getTitle());
					tv.setShowCate(showCate);
					tv.setIndicateOid(o.getIndicate().getOid());
					tv.setIndicateTitle(o.getIndicate().getTitle());
					tv.setIndicateDataType(o.getIndicate().getDataType());
					tv.setIndicateDataUnit(o.getIndicate().getDataUnit());
					tv.setShowIndicate(true);
					tv.setShowOptions(false);
					nv.add(tv);
					imap.put(o.getIndicate().getOid(), nv);
				}

				List<RiskOptionsView> vlist = imap.get(o.getIndicate().getOid());
				RiskOptionsView v = new RiskOptionsView();
				v.setShowCate(false);
				v.setShowIndicate(false);
				v.setOptionsOid(o.getOid());
				v.setOptionsTitle(o.showTitle());
				v.setOptionsScore(o.getScore());
				v.setShowOptions(true);
				vlist.add(v);

			}

			for (String ck : cmap.keySet()) {
				for (String ik : cmap.get(ck).keySet()) {
					view.addAll(cmap.get(ck).get(ik));
				}
			}

		}

		return view;
	}

	@Transactional
	public List<RiskOptions> search(String type) {
		return this.riskOptionsDao.search(type);
	}

}
