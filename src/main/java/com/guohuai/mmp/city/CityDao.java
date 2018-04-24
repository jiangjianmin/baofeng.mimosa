package com.guohuai.mmp.city;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface CityDao extends JpaRepository<CityEntity, String>, JpaSpecificationExecutor<CityEntity> {

	@Query("FROM CityEntity WHERE cityParentCode = ?1 ORDER BY cityCode ASC")
	public List<CityEntity> findByCityParentCode(String parentCode);
	
	@Query("FROM CityEntity WHERE cityParentCode = '' OR cityParentCode is NULL ORDER BY cityCode ASC")
	public List<CityEntity> getProvinces();
	
}
