package com.guohuai.mmp.publisher.baseaccount.statistics.history;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.component.util.BeanUtil;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.StringUtil;
import com.guohuai.mmp.publisher.baseaccount.statistics.PublisherStatisticsDao;
import com.guohuai.mmp.publisher.baseaccount.statistics.PublisherStatisticsEntity;

@Service

public class PublisherStatisticsHistoryService {
	
	@Autowired
	PublisherStatisticsHistoryDao publisherStatisticsHistoryDao;
	@Autowired
	PublisherStatisticsDao publisherStatisticsDao;
	
	/** 分表(取出表中最新的记录增加日期字段，插入历史表中) */
	@Transactional
	public void splitTable(java.sql.Date splitdate) {
		
		List<PublisherStatisticsEntity> entityList = this.publisherStatisticsDao.findAll();
		
		
		List<PublisherStatisticsHistoryEntity> newList = new ArrayList<PublisherStatisticsHistoryEntity>();
		for (PublisherStatisticsEntity entity : entityList) {
			PublisherStatisticsHistoryEntity dest = new PublisherStatisticsHistoryEntity();
			BeanUtil.copy(dest, entity);
			dest.setOid(StringUtil.uuid());
			dest.setConfirmDate(splitdate);
			dest.setCreateTime(DateUtil.getSqlCurrentDate());
			dest.setUpdateTime(DateUtil.getSqlCurrentDate());
			newList.add(dest);
		}
		// 删除旧数据
		this.publisherStatisticsHistoryDao.deleteByConfirmDate(splitdate);
		// 保存新数据
		this.publisherStatisticsHistoryDao.save(newList);
	}
}
