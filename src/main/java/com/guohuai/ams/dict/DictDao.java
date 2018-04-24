package com.guohuai.ams.dict;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DictDao extends JpaRepository<Dict, String>, JpaSpecificationExecutor<Dict> {

	public List<Dict> findByCateOrderByRank(String cate);

}
