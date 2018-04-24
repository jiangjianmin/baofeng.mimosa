package com.guohuai.ams.activityModel;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * @Desc: 定制产品dao
 * @author huyong
 * @date 2017.12.11
 */
public interface ProductPlaceDao extends JpaRepository<ProductPlaceEntity, String>, JpaSpecificationExecutor<ProductPlaceEntity>{
	
	/**
	 * @Desc: 根据modelOid查询定制产品列表
	 * @author huyong
	 * @date 2017.12.11
	 */
	public List<ProductPlaceEntity> findListByModelOid(String modelOid);
	
	/**
	 * @Desc: 根据modelOid删除定制产品
	 * @author huyong
	 * @date 2017.12.11
	 */
	@Modifying
	@Query(value="delete t1 from T_GAM_PRODUCT_PLACE t1 where t1.modelOid = ?1 ", nativeQuery = true)
	public int deleteByModelOid(String modelOid);

}
