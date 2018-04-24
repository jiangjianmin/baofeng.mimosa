package com.guohuai.ams.system.config.project.warrantyLevel;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CCPWarrantyLevelService {

	@Autowired
	private CCPWarrantyLevelDao cCPWarrantyLevelDao;

	@Transactional
	public CCPWarrantyLevel save(CCPWarrantyLevelForm form) {
		CCPWarrantyLevel level = new CCPWarrantyLevel();
		BeanUtils.copyProperties(form, level);
		if (StringUtils.isBlank(level.getOid())) {
			level.setOid(StringUtil.uuid());
			log.debug("新增风险等级配置");
		} else {
			log.debug("修改风险等级配置");
		}
		CCPWarrantyLevel warrantyExpire = this.cCPWarrantyLevelDao.save(level);

		return warrantyExpire;

	}
	
	@Transactional
	public List<CCPWarrantyLevel> save(CCPWarrantyLevelOptionsForm form) {
		List<CCPWarrantyLevelOptionsForm.Option> list = form.getOptions();
		log.info("保存集合===>" + JSON.toJSONString(list));
		if (null != list) {
			List<CCPWarrantyLevel> entityList = new ArrayList<>(list.size());
			for (CCPWarrantyLevelOptionsForm.Option option : list) {
				CCPWarrantyLevel level = new CCPWarrantyLevel();
				BeanUtils.copyProperties(option, level);
				if (StringUtils.isBlank(level.getOid())) {
					level.setOid(null);
					log.debug("新增风险等级配置");
				} else {
					log.debug("修改风险等级配置");
				}
				CCPWarrantyLevel warrantyExpire = this.cCPWarrantyLevelDao.save(level);
				entityList.add(warrantyExpire);
			}
			return entityList;
		}
		return null;

	}

	@Transactional
	public void delete(String oid) {

		this.cCPWarrantyLevelDao.delete(oid);

	}

	public CCPWarrantyLevel get(String oid) {

		CCPWarrantyLevel warrantyLevel = this.cCPWarrantyLevelDao.findOne(oid);

		if (null == warrantyLevel) {
			throw new AMPException(String.format("No data found for oid '%s'", oid));
		}

		return warrantyLevel;

	}

	public List<CCPWarrantyLevel> search() {
		List<CCPWarrantyLevel> list = this.cCPWarrantyLevelDao.search();
		return list;
	}

}
