package com.guohuai.ams.label;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.StringUtil;
import com.guohuai.component.web.view.BaseResp;
import com.guohuai.component.web.view.PageResp;

@Service
@Transactional
public class LabelService {
	
	Logger logger = LoggerFactory.getLogger(LabelService.class);
	
	@Autowired
	private LabelDao labelDao;

	public List<JSONObject> getProductLabelNames(final String labelType) {
		
		Specification<LabelEntity> spec = new Specification<LabelEntity>() {
			@Override
			public Predicate toPredicate(Root<LabelEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.and(cb.equal(root.get("labelType").as(String.class), labelType), cb.equal(root.get("isOk").as(String.class), LabelEntity.isOk_yes));
			}
		};
		spec = Specifications.where(spec);

		List<LabelEntity> pls = this.labelDao.findAll(Specifications.where(spec));

		List<JSONObject> validProductLabelNames = new ArrayList<JSONObject>();
		if (pls != null && pls.size() > 0) {
			JSONObject validProductLabelName = null;
			for (LabelEntity pl : pls) {
				validProductLabelName = new JSONObject();
				validProductLabelName.put("oid", pl.getOid());
				validProductLabelName.put("name", pl.getLabelName());
				validProductLabelName.put("code", pl.getLabelCode());
				validProductLabelNames.add(validProductLabelName);
			}
		}
		
		return validProductLabelNames;

	}

	public BaseResp save(SaveLabelForm form) throws ParseException, Exception {
		BaseResp response = new BaseResp();

		Timestamp now = new Timestamp(System.currentTimeMillis());

		LabelEntity pl = new LabelEntity();
		pl.setOid(StringUtil.uuid());
		pl.setLabelCode(form.getLabelCode());
		pl.setLabelName(form.getLabelName());
		pl.setLabelType(form.getLabelType());
		pl.setIsOk(LabelEntity.isOk_yes);
		pl.setLabelDesc(form.getLabelDesc());
		
		pl.setUpdateTime(now);
		pl.setCreateTime(now);

		this.labelDao.save(pl);

		return response;
	}

	public BaseResp update(SaveLabelForm form) throws ParseException {

		BaseResp response = new BaseResp();

		LabelEntity pl = labelDao.findOne(form.getOid());
		// 当前时间
		Timestamp now = new Timestamp(System.currentTimeMillis());

		pl.setLabelCode(form.getLabelCode());
		pl.setLabelName(form.getLabelName());
		pl.setLabelType(form.getLabelType());
		pl.setLabelDesc(form.getLabelDesc());
		pl.setUpdateTime(now);

		this.labelDao.saveAndFlush(pl);

		return response;
	}

	public LabelResp read(String oid) {
		LabelEntity pl = labelDao.findOne(oid);
		LabelResp pr = new LabelResp(pl);
		return pr;
	}

	public PageResp<LabelResp> list(Specification<LabelEntity> spec, Pageable pageable) {
		Page<LabelEntity> cas = this.labelDao.findAll(spec, pageable);
		
		PageResp<LabelResp> pagesRep = new PageResp<LabelResp>();
		
		if (cas != null && cas.getContent() != null && cas.getTotalElements() > 0) {
			List<LabelResp> rows = new ArrayList<LabelResp>();
			for (LabelEntity pl : cas) {
				LabelResp queryRep = new LabelResp(pl);
				rows.add(queryRep);
			}
			pagesRep.setRows(rows);
		}
		pagesRep.setTotal(cas.getTotalElements());
		return pagesRep;
	}
	
	public BaseResp invalid(String oid) {
		BaseResp response = new BaseResp();
		
		LabelEntity pl = labelDao.findOne(oid);
		pl.setIsOk(LabelEntity.isOk_no);
		pl.setUpdateTime(new Timestamp(System.currentTimeMillis()));
		this.labelDao.saveAndFlush(pl);

		return response;
	}

	public BaseResp valid(String oid) {
		BaseResp response = new BaseResp();
		
		LabelEntity pl = labelDao.findOne(oid);
		pl.setIsOk(LabelEntity.isOk_yes);
		pl.setUpdateTime(new Timestamp(System.currentTimeMillis()));
		this.labelDao.saveAndFlush(pl);

		return response;
	}


	@Transactional
	public long validateSingle(final String attrName, final String value, final String oid) {

		Specification<LabelEntity> spec = new Specification<LabelEntity>() {
			@Override
			public Predicate toPredicate(Root<LabelEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				if (StringUtil.isEmpty(oid)) {
					return cb.equal(root.get(attrName).as(String.class), value);
				} else {
					return cb.and(cb.equal(root.get(attrName).as(String.class), value), cb.notEqual(root.get("oid").as(String.class), oid));
				}
			}
		};
		spec = Specifications.where(spec);

		return this.labelDao.count(spec);
	}
	/**
	 * 根据产品ID获取可用的基本标签
	 * @return
	 */
	public Integer findLabelByProductId(String productId){
		return this.labelDao.findLabelByProductId(productId);
	}
	
	/**
	 * 根据产品ID获取标签Code
	 * @param productId
	 * @return
	 */
	public List<String> findLabelCodeByProductId(String productId){
		List<String> returnList=new ArrayList<String>();
		List<Object[]> list=this.labelDao.findLabelCodeByProductId(productId);
		if(list.isEmpty()){
			throw new AMPException(productId+":产品没有产品标签!");
		}
		for(Object[] obj : list){
			returnList.add(obj[0].toString());
		}
		return returnList;
	}
	
	/**
	 * 产品标签是否含有指定标签判断
	 */
	public boolean isProductLabelHasAppointLabel(String productLabel, String appointLabel) {
		if (StringUtil.isEmpty(productLabel) || StringUtil.isEmpty(appointLabel)) {
			return false;
		}
		
		String[] arr = productLabel.split(",");
		for (String tmp : arr) {
			if (appointLabel.equals(tmp)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 根据产品ID获取标签Code
	 * @param productId
	 * @return
	 */
	public String findLabelCodeByOid(String oid){
		String labelCode = this.labelDao.findLabelCodeByOid(oid);
		return labelCode;
	}
	/***
	 *根据产品id和lableCode查找lableOid
	 *
	 * @author yujianlong
	 * @date 2018/4/4 18:35
	 * @param [productOid]
	 * @return java.util.List<java.lang.String>
	 */
	public List<String> findLabelCodeByProdcuctOid(String productOid){
		return this.labelDao.findLabelCodeByProdcuctOid(productOid);
	}


}
