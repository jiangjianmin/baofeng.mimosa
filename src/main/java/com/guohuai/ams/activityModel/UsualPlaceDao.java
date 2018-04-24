package com.guohuai.ams.activityModel;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * @Desc: 普通占位dao
 * @author huyong
 * @date 2017.12.11
 */
public interface UsualPlaceDao extends JpaRepository<UsualPlaceEntity, String>, JpaSpecificationExecutor<UsualPlaceEntity>{
	
	/**
	 * @Desc: 根据modelOid查询普通占位列表
	 * @author huyong
	 * @date 2017.12.11
	 */
	public List<UsualPlaceEntity> findListByModelOid(String modelOid);
	
	/**
	 * @Desc: 根据modelOid删除普通占位
	 * @author huyong
	 * @date 2017.12.11
	 */
	@Modifying
	@Query(value="delete t1 from T_GAM_USUAL_PLACE t1 where t1.modelOid = ?1 ", nativeQuery = true)
	public int deleteByModelOid(String modelOid);
}
