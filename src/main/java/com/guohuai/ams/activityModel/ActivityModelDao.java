package com.guohuai.ams.activityModel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * @Desc: 活动模板dao
 * @author huyong
 * @date 2017.12.11
 */
public interface ActivityModelDao extends JpaRepository<ActivityModelEntity, String>, JpaSpecificationExecutor<ActivityModelEntity> {
	
	/**
	 * @Desc: 活动模板查询
	 * @author: huyong
	 * @date: 2017.12.12
	 */
	@Query(value="select t1 from ActivityModelEntity t1 where t1.platType = ?1 and t1.code = ?2")
	public ActivityModelEntity findByPlatTypeAndCode(String platType, String code);
}
