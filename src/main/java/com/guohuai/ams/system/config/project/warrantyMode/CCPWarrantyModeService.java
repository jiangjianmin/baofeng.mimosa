package com.guohuai.ams.system.config.project.warrantyMode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.StringUtil;

@Service
public class CCPWarrantyModeService {

	@Autowired
	private CCPWarrantyModeDao ccpWarrantyModeDao;

	@Transactional
	public CCPWarrantyMode create(CCPWarrantyModeForm form) {

		CCPWarrantyMode.CCPWarrantyModeBuilder builder = CCPWarrantyMode.builder().oid(StringUtil.uuid());
		builder.type(form.getType()).title(form.getTitle()).weight(form.getWeight100().divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_UP));

		CCPWarrantyMode warrantyMode = this.ccpWarrantyModeDao.save(builder.build());

		return warrantyMode;

	}

	@Transactional
	public CCPWarrantyMode update(CCPWarrantyModeForm form) {

		CCPWarrantyMode warrantyMode = this.get(form.getOid());
		warrantyMode.setType(form.getType());
		warrantyMode.setTitle(form.getTitle());
		warrantyMode.setWeight(form.getWeight100().divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_UP));

		warrantyMode = this.ccpWarrantyModeDao.save(warrantyMode);

		return warrantyMode;

	}

	@Transactional
	public void delete(String oid) {

		this.ccpWarrantyModeDao.delete(oid);

	}

	public CCPWarrantyMode get(String oid) {

		CCPWarrantyMode warrantyMode = this.ccpWarrantyModeDao.findOne(oid);

		if (null == warrantyMode) {
			throw new AMPException(String.format("No data found for oid '%s'", oid));
		}

		return warrantyMode;

	}

	public List<CCPWarrantyModeResp> search() {
		List<CCPWarrantyMode> list = this.ccpWarrantyModeDao.search();
		Map<String, List<CCPWarrantyMode>> map = new HashMap<String, List<CCPWarrantyMode>>();
		List<CCPWarrantyModeResp> result = new ArrayList<CCPWarrantyModeResp>();
		if (null != list && list.size() > 0) {

			for (CCPWarrantyMode mode : list) {
				if (!map.containsKey(mode.getType())) {
					map.put(mode.getType(), new ArrayList<CCPWarrantyMode>());
				}
				map.get(mode.getType()).add(mode);
			}

			if (map.containsKey("GUARANTEE")) {
				for (int i = 0; i < map.get("GUARANTEE").size(); i++) {
					result.add(new CCPWarrantyModeResp(map.get("GUARANTEE").get(i), i == 0));
				}
			}

			if (map.containsKey("MORTGAGE")) {
				for (int i = 0; i < map.get("MORTGAGE").size(); i++) {
					result.add(new CCPWarrantyModeResp(map.get("MORTGAGE").get(i), i == 0));
				}
			}

			if (map.containsKey("HYPOTHECATION")) {
				for (int i = 0; i < map.get("HYPOTHECATION").size(); i++) {
					result.add(new CCPWarrantyModeResp(map.get("HYPOTHECATION").get(i), i == 0));
				}
			}

		}

		return result;
	}

}
