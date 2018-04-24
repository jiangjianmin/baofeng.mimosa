package com.guohuai.ams.system.config.risk.indicate.collect;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.ams.system.config.risk.indicate.RiskIndicate;
import com.guohuai.ams.system.config.risk.options.RiskOptions;
import com.guohuai.ams.system.config.risk.options.RiskOptionsService;
import com.guohuai.component.util.StringUtil;

@Service
public class RiskIndicateCollectService {

	@Autowired
	private RiskIndicateCollectDao riskIndicateCollectDao;
	@Autowired
	private RiskOptionsService riskOptionsService;

	@Transactional
	public List<RiskIndicateCollectResp> save(RiskIndicateCollectForm form) {

		List<RiskIndicateCollectResp> result = new ArrayList<RiskIndicateCollectResp>();

		if (null == form.getDatas() || form.getDatas().size() == 0) {
			return result;
		}

		List<RiskOptions> options = this.riskOptionsService.search(form.getType());
		if (null == options || options.size() == 0) {
			return result;
		}

		this.riskIndicateCollectDao.deleteByRelative(form.getRelative());

		Map<String, List<RiskOptions>> omap = new HashMap<String, List<RiskOptions>>();
		Map<String, RiskIndicate> imap = new HashMap<String, RiskIndicate>();
		for (RiskOptions option : options) {
			if (!omap.containsKey(option.getIndicate().getOid())) {
				omap.put(option.getIndicate().getOid(), new ArrayList<RiskOptions>());

			}
			omap.get(option.getIndicate().getOid()).add(option);
			if (!imap.containsKey(option.getIndicate().getOid())) {
				imap.put(option.getIndicate().getOid(), option.getIndicate());
			}
		}

		for (RiskIndicateCollectForm.OptionData data : form.getDatas()) {
			if (omap.containsKey(data.getIndicateOid()) && imap.containsKey(data.getIndicateOid())) {
				RiskIndicate indicate = imap.get(data.getIndicateOid());
				RiskIndicateCollect collect = new RiskIndicateCollect();
				collect.setOid(StringUtil.uuid());
				collect.setIndicate(indicate);
				collect.setRelative(form.getRelative());

				boolean matched = false;
				RiskOptions dft = null;
				for (RiskOptions option : omap.get(data.getIndicateOid())) {
					if (option.getDft().equals("YES")) {
						dft = option;
						break;
					}
				}

				if (indicate.getDataType().equals(RiskIndicate.DATA_TYPE_Number)) {

					for (RiskOptions option : omap.get(data.getIndicateOid())) {
						if (option.getOid().equals(data.getOptions())) {
							collect.setCollectOption(option.getOid());
							collect.setCollectData(option.showTitle());
							collect.setCollectScore(option.getScore());
							matched = true;
							break;
						}
					}

				} else if (indicate.getDataType().equals(RiskIndicate.DATA_TYPE_NumRange)) {

					// 没有录入数据的, 后面取默认值
					if (!StringUtil.isEmpty(data.getCollectData())) {

						BigDecimal datad = new BigDecimal(data.getCollectData());

						for (RiskOptions option : omap.get(data.getIndicateOid())) {
							if (option.getDft().equals("YES")) {
								continue;
							}

							boolean accept = true;

							if (!StringUtil.isEmpty(option.getParam1())) {
								BigDecimal param1d = new BigDecimal(option.getParam1());
								int cr = datad.compareTo(param1d);
								if (option.getParam0().equals("[")) {
									accept = accept && (cr >= 0);
								} else if (option.getParam0().equals("(")) {
									accept = accept && (cr > 0);
								} else {
									accept = accept && false;
								}
							}

							if (!StringUtil.isEmpty(option.getParam2())) {
								BigDecimal param2d = new BigDecimal(option.getParam2());
								int cr = datad.compareTo(param2d);
								if (option.getParam3().equals("]")) {
									accept = accept && (cr <= 0);
								} else if (option.getParam0().equals(")")) {
									accept = accept && (cr < 0);
								} else {
									accept = accept && false;
								}
							}

							if (accept) {
								collect.setCollectOption(option.getOid());
								collect.setCollectData(data.getCollectData());
								collect.setCollectScore(option.getScore());
								matched = true;
								break;
							}

						}
					}

				} else if (indicate.getDataType().equals(RiskIndicate.DATA_TYPE_Text)) {

					for (RiskOptions option : omap.get(data.getIndicateOid())) {
						if (option.getOid().equals(data.getOptions())) {
							collect.setCollectOption(option.getOid());
							collect.setCollectData(option.showTitle());
							collect.setCollectScore(option.getScore());
							matched = true;
							break;
						}
					}

				} else {
					continue;
				}

				if (!matched && null != dft) {
					collect.setCollectOption(dft.getOid());
					collect.setCollectData(dft.showTitle());
					collect.setCollectScore(dft.getScore());
					matched = true;
				}

				if (matched) {

					collect = this.riskIndicateCollectDao.save(collect);
					result.add(new RiskIndicateCollectResp(collect));

				}
			}
		}

		return result;
	}

	@Transactional
	public List<RiskIndicateCollectResp> preUpdate(String relative) {

		List<RiskIndicateCollect> list = this.riskIndicateCollectDao.findByRelative(relative);
		List<RiskIndicateCollectResp> result = new ArrayList<RiskIndicateCollectResp>();

		if (null != list && list.size() > 0) {
			for (RiskIndicateCollect collect : list) {
				result.add(new RiskIndicateCollectResp(collect));
			}
		}

		return result;
	}

}
