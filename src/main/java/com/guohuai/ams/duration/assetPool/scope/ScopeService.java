package com.guohuai.ams.duration.assetPool.scope;

import java.math.BigDecimal;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.ams.dict.DictService;
import com.guohuai.ams.duration.assetPool.AssetPoolEntity;
import com.guohuai.component.util.StringUtil;

@Service
public class ScopeService {

	@Autowired
	private ScopeDao scopeDao;
	
	@Autowired
	private DictService dictService;
	
	/**
	 * 投资范围
	 * @param assetPool
	 * @param assetTypeOid
	 * @return
	 */
	@Transactional
	public ScopeEntity save(AssetPoolEntity assetPool, String assetTypeOid) {
		// 删除原先的范围数据
		scopeDao.deleteByPid(assetPool.getOid());
		
		ScopeEntity entity = new ScopeEntity();
		entity.setOid(StringUtil.uuid());
		entity.setAssetPool(assetPool);
		entity.setAssetType(dictService.get(assetTypeOid));
		entity.setRatio(BigDecimal.ZERO);
		
		return scopeDao.save(entity);
	}
	
	/**
	 * 获取投资范围
	 * @param pid
	 * @return
	 */
	@Transactional
	public String[] getScopes(String pid) {
		List<ScopeEntity> list = scopeDao.findByAssetPoolOid(pid);
		if (null != list && list.size() > 0) {
			String[] scopes = new String[list.size()];
			for (int i = 0; i < list.size(); i ++) {
				scopes[i] = list.get(i).getAssetType().getOid();
			}
			
			return scopes;
		}
		
		return null;
	}
}
