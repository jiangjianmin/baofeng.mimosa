package com.guohuai.mmp.platform.baseaccount.statistics.history;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.component.util.BeanUtil;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.StringUtil;
import com.guohuai.mmp.platform.baseaccount.statistics.PlatformStatisticsDao;
import com.guohuai.mmp.platform.baseaccount.statistics.PlatformStatisticsEntity;

@Service

public class PlatformStatisticsHistoryService {

	Logger logger = LoggerFactory.getLogger(PlatformStatisticsHistoryService.class);

	@Autowired
	private PlatformStatisticsDao platformStatisticsDao;
	@Autowired
	private PlatformStatisticsHistoryDao platformStatisticsHistoryDao;

	/** 分表(取出表中最新的记录增加日期字段，插入历史表中) */
	@Transactional
	public void splitTable(java.sql.Date splitdate) {
		List<PlatformStatisticsEntity> entityList = this.platformStatisticsDao.findAll();

		List<PlatformStatisticsHistoryEntity> newList = new ArrayList<PlatformStatisticsHistoryEntity>();
		for (PlatformStatisticsEntity entity : entityList) {
			PlatformStatisticsHistoryEntity dest = new PlatformStatisticsHistoryEntity();
			BeanUtil.copy(dest, entity);// 拷贝成员变量
			dest.setOid(StringUtil.uuid());
			dest.setConfirmDate(splitdate);
			dest.setCreateTime(DateUtil.getSqlCurrentDate());
			dest.setUpdateTime(DateUtil.getSqlCurrentDate());
			newList.add(dest);
		}
		// 删除旧数据
		this.platformStatisticsHistoryDao.deleteByConfirmDate(splitdate);
		// 保存新数据
		this.platformStatisticsHistoryDao.save(newList);
	}
}
