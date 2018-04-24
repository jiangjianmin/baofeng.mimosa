package com.guohuai.ams.productLabel;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;

import com.google.common.base.Joiner;
import com.guohuai.ams.label.LabelDao;
import com.guohuai.ams.label.LabelEntity;
import com.guohuai.ams.label.LabelRep;
import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductDao;
import com.guohuai.cache.service.CacheProductService;
import com.guohuai.component.util.StringUtil;

@Service
@Transactional
public class ProductLabelService {
	
	Logger logger = LoggerFactory.getLogger(ProductLabelService.class);
	
	@Autowired
	private LabelDao labelDao;
	@Autowired
	private ProductLabelDao productLabelDao;
	@Autowired
	private CacheProductService cacheProductService;
	@Autowired
	private ProductDao productDao;


	public List<ProductLabel> saveAndFlush(Product product, List<String> labelOids)  {
		
		List<ProductLabel> oldProductLabels = this.findProductLabelsByProduct(product);
		
		Map<String, ProductLabel> map = new HashMap<String, ProductLabel>();
		if (null != oldProductLabels && oldProductLabels.size() > 0) {
			for (ProductLabel pl : oldProductLabels) {
				map.put(pl.getLabel().getOid(), pl);
			}
		}

		List<ProductLabel> result = new ArrayList<ProductLabel>();

		Timestamp now = new Timestamp(System.currentTimeMillis());
		
		if (null != labelOids && labelOids.size() > 0) {
			for (String labelOid : labelOids) {
				if (!StringUtil.isEmpty(labelOid) && map.containsKey(labelOid)) {
					// 已经有的, 就不用动了
					ProductLabel pl = map.get(labelOids);
					result.add(pl);
					map.remove(labelOid);
				} else {
					// 存储新的
					LabelEntity label = labelDao.findOne(labelOid);
					ProductLabel pl = new ProductLabel();
					pl.setOid(StringUtil.uuid());
					pl.setLabel(label);
					pl.setProduct(product);
					pl.setCreateTime(now);
					pl.setUpdateTime(now);
					productLabelDao.save(pl);

					result.add(pl);
				}
			}
		}
		
		//删除新的中少的老的关联数据
		if (map.size() > 0) {
			for (String labelOid : map.keySet()) {
				ProductLabel pl = map.get(labelOid);
				productLabelDao.delete(pl);
			}
		}
		
//		cacheProductService.syncProductLabel(product);
		
		return result;
	}
	
	public List<ProductLabel> findProductLabelsByProduct(final Product product) {
		Specification<ProductLabel> spec = new Specification<ProductLabel>() {
			@Override
			public Predicate toPredicate(Root<ProductLabel> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.equal(root.get("product").get("oid").as(String.class), product.getOid());
			}
		};
		spec = Specifications.where(spec);
		
		return productLabelDao.findAll(spec);
	}
	
	public List<LabelRep> getLabelRepByProduct(String productOid) {
		
		List<LabelRep> repList = new ArrayList<LabelRep>();
		List<ProductLabel> list = this.findProductLabelsByProduct(productDao.findOne(productOid));
		for (ProductLabel label : list) {
			LabelRep rep = new LabelRep();
			rep.setLabelCode(label.getLabel().getLabelCode());
			rep.setLabelName(label.getLabel().getLabelName());
			rep.setLabelType(label.getLabel().getLabelType());
			repList.add(rep);
		}
		Collections.sort(repList, new LabelRepComparator());
		return repList;
	}
	
	public String findLabelByProduct(Product product) {
		List<ProductLabel> list = productLabelDao.findByProduct(product);
		List<String> labelCodeList = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		for  (ProductLabel label : list) {
			labelCodeList.add(label.getLabel().getLabelCode());
//			sb.append(label.getLabel().getLabelCode()).append(",");
		}
		sb.append(Joiner.on(",").join(labelCodeList));
		
		if (sb.length() != 0) {
			sb.substring(sb.length() - 1, sb.length());
		}
		return sb.toString();
	}
	
	class LabelRepComparator implements Comparator<LabelRep> {
		@Override
		public int compare(LabelRep o1, LabelRep o2) {
			return o1.getLabelType().compareTo(o2.getLabelType()) * -1;
		}
	}
}
