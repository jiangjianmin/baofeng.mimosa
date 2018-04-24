package com.guohuai.ams.duration.fact.feigin;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.guohuai.ams.duration.assetPool.AssetPoolEntity;
import com.guohuai.ams.duration.assetPool.AssetPoolService;
import com.guohuai.component.util.StringUtil;
import com.guohuai.component.web.view.PageResp;
import com.guohuai.operate.api.AdminSdk;

@Service
public class FeiginService {

	@Autowired
	private FeiginDao feiginDao;
	
	@Autowired
	private AssetPoolService poolService;
	
	@Autowired
	private AdminSdk adminSdk;
	
	@Transactional
	public void save(FeiginEntity entity) {
		feiginDao.save(entity);
	}
	
	@Transactional
	public FeiginEntity getByOid(String oid) {
		return feiginDao.findOne(oid);
	}
	
	@Transactional
	public void create(FeiginForm form, String operator) {
		FeiginEntity entity = new FeiginEntity();
		AssetPoolEntity pool = poolService.getByOid(form.getAssetPoolOid());
		pool.setCountintChargefee(pool.getCountintChargefee().add(form.getChargeFee())
				.setScale(4, BigDecimal.ROUND_HALF_UP));
		pool.setUpdateTime(new Timestamp(System.currentTimeMillis()));
		entity.setOid(StringUtil.uuid());
		entity.setAssetPool(pool);
		entity.setChargeFee(form.getChargeFee());
		entity.setDigest(form.getDigest());
		entity.setState("0");
//		entity.setCreator(operator);
		entity.setCreator(adminSdk.getAdmin(operator).getName());
		entity.setCreateTime(new Timestamp(System.currentTimeMillis()));
		feiginDao.save(entity);
	}
	
	@Transactional
	public void updateByOid(String oid, String operator) {
		FeiginEntity entity = feiginDao.findOne(oid);
		entity.setState("1");
		entity.setDrawer(adminSdk.getAdmin(operator).getName());
		entity.setDrawTime(new Timestamp(System.currentTimeMillis()));
		
		AssetPoolEntity pool = entity.getAssetPool();
		pool.setDrawedChargefee(pool.getDrawedChargefee().add(entity.getChargeFee())
				.setScale(4, BigDecimal.ROUND_HALF_UP));
		pool.setUpdateTime(new Timestamp(System.currentTimeMillis()));
		
		feiginDao.save(entity);
	}
	
	@Transactional
	public void deleteByOid(String oid, String operator) {
		FeiginEntity entity = feiginDao.findOne(oid);
		entity.setState("-1");
		entity.setDrawer(operator);
		entity.setDrawTime(new Timestamp(System.currentTimeMillis()));
		feiginDao.save(entity);
	}
	
	@Transactional
	public FeiginForm getFormByOid(Specification<FeiginEntity> spec) {
		FeiginEntity entity = feiginDao.findOne(spec);
		FeiginForm form = FeiginForm.builder()
				.oid(entity.getOid())
				.assetPoolName(entity.getAssetPool().getName())
				.chargeFee(entity.getChargeFee())
				.digest(entity.getDigest())
				.state(entity.getState())
				.creator(entity.getCreator())
				.createTime(entity.getCreateTime())
				.build();
		return form;
	}
	
	@Transactional
	public PageResp<FeiginForm> getAll(Specification<FeiginEntity> spec, Pageable pageable) {
		List<FeiginForm> formList = Lists.newArrayList();
		Page<FeiginEntity> list = feiginDao.findAll(spec, pageable);
		if (null != list && !list.getContent().isEmpty()) {
			for (FeiginEntity entity : list.getContent()) {
				FeiginForm form = FeiginForm.builder()
					.oid(entity.getOid())
					.assetPoolName(entity.getAssetPool().getName())
					.chargeFee(entity.getChargeFee())
					.digest(entity.getDigest())
					.state(entity.getState())
					.creator(entity.getCreator())
					.createTime(entity.getCreateTime())
					.drawer(entity.getDrawer())
					.drawTime(entity.getDrawTime())
					.build();
				formList.add(form);
			}
		}
		PageResp<FeiginForm> rep = new PageResp<FeiginForm>();
		rep.setRows(formList);
		rep.setTotal(list.getTotalElements());
		return rep;
	}
}
