package com.guohuai.ams.product.reward;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductDecimalFormat;
import com.guohuai.ams.product.ProductService;
import com.guohuai.component.util.DecimalUtil;
import com.guohuai.component.util.StringUtil;
import com.guohuai.component.web.view.BaseResp;
import com.guohuai.component.web.view.PageResp;

@Service
@Transactional
public class ProductIncomeRewardService {

	@Autowired
	private ProductIncomeRewardDao productIncomeRewardDao;
	@Autowired
	private ProductService productService;

	public static void main(String[] args) {
		BigDecimal x = new BigDecimal("0.0005");
		BigDecimal y = x.divide(new BigDecimal(360), 12, RoundingMode.HALF_UP);
		System.out.println(y);
	}

	public BaseResp save(SaveProductRewardForm form, String operator) throws ParseException {

		BaseResp response = new BaseResp();

		Product product = productService.getProductByOid(form.getProductOid());

		List<ProductIncomeReward> productRewards = JSON.parseArray(form.getReward(), ProductIncomeReward.class);

		List<ProductIncomeReward> list = this.productRewardList(form.getProductOid());

		List<ProductIncomeReward> saveProductRewards = new ArrayList<ProductIncomeReward>();
		List<String> existOids = new ArrayList<String>();
		for (ProductIncomeReward productReward : productRewards) {
			if (StringUtil.isEmpty(productReward.getOid())) {
				productReward.setOid(StringUtil.uuid());
				productReward.setProduct(product);
				productReward.setRatio(ProductDecimalFormat.divide(productReward.getRatio()));
				productReward.setDratio(productReward.getRatio().divide(new BigDecimal(product.getIncomeCalcBasis()), 12, RoundingMode.DOWN));
				if(productReward.getEndDate() == null) {
					productReward.setEndDate(999999999);
				}
				saveProductRewards.add(productReward);
			} else {
				existOids.add(productReward.getOid());
			}
		}

		List<ProductIncomeReward> deletePrs = new ArrayList<ProductIncomeReward>();
		if (list != null && list.size() > 0) {
			for (ProductIncomeReward pr : list) {
				if (!existOids.contains(pr.getOid())) {
					deletePrs.add(pr);
				}
			}
		}

		if (saveProductRewards.size() > 0) {
			productIncomeRewardDao.save(saveProductRewards);
		}
		if (deletePrs.size() > 0) {
			productIncomeRewardDao.delete(deletePrs);
		}
		return response;
	}

	public PageResp<ProductRewardResp> list(String productOid) {

		List<ProductIncomeReward> list = this.productRewardList(productOid);

		PageResp<ProductRewardResp> pagesRep = new PageResp<ProductRewardResp>();
		if (list != null && list.size() > 0) {
			List<ProductRewardResp> rows = new ArrayList<ProductRewardResp>();

			for (ProductIncomeReward pir : list) {
				ProductRewardResp queryRep = new ProductRewardResp(pir);

				rows.add(queryRep);
			}
			pagesRep.setRows(rows);
		}
		pagesRep.setTotal(list.size());
		return pagesRep;
	}

	public List<ProductIncomeReward> productRewardList(final String productOid) {
		Specification<ProductIncomeReward> spec = new Specification<ProductIncomeReward>() {
			@Override
			public Predicate toPredicate(Root<ProductIncomeReward> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.equal(root.get("product").get("oid").as(String.class), productOid);
			}
		};
		spec = Specifications.where(spec);

		Sort sort = new Sort(new Order(Direction.ASC, "startDate"));

		List<ProductIncomeReward> list = this.productIncomeRewardDao.findAll(spec, sort);

		return list;
	}

	public List<ProductIncomeReward> productsRewardList(final List<String> productOids) {
		Specification<ProductIncomeReward> spec = new Specification<ProductIncomeReward>() {
			@Override
			public Predicate toPredicate(Root<ProductIncomeReward> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				Expression<String> exp = root.get("product").get("oid").as(String.class);
				return exp.in(productOids);

			}
		};
		spec = Specifications.where(spec);

		Sort sort = new Sort(new Order(Direction.ASC, "startDate"));

		List<ProductIncomeReward> list = this.productIncomeRewardDao.findAll(spec, sort);

		return list;
	}

	public Map<String, List<ProductIncomeReward>> productRewardList(List<String> productOid) {

		List<ProductIncomeReward> list = this.productIncomeRewardDao.findByProductOid(productOid);

		Map<String, List<ProductIncomeReward>> map = new HashMap<>();
		if (null != list)
			for (ProductIncomeReward pi : list) {
				String poid = pi.getProduct().getOid();
				List<ProductIncomeReward> l = map.get(poid);
				if (null == l) {
					l = new ArrayList<ProductIncomeReward>();
					map.put(poid, l);
				}
				boolean has = false;
				for (ProductIncomeReward p : l) {
					if (p.getProduct().getOid().equals(poid)) {
						has = true;
					}
				}
				if (!has)
					l.add(pi);
			}

		return map;
	}

	/**
	 * @author yuechao
	 * @param rewardRules
	 * @param apart
	 * @return BigDecimal
	 */
	public BigDecimal getRewardAmount(ProductIncomeReward reward, BigDecimal volume) {
		return DecimalUtil.setScaleDown(reward.getDratio().multiply(volume));

	}
	
	/** 查询某产品的奖励设置,按照奖励等级升序 */
	public List<ProductIncomeReward> findAllByLevelAsc(String productOid) {
		final String proOid = productOid;
		Specification<ProductIncomeReward> spec = new Specification<ProductIncomeReward>() {
			@Override
			public Predicate toPredicate(Root<ProductIncomeReward> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

				return cb.equal(root.get("product").get("oid").as(String.class), proOid);
			}

		};

		Sort sort = new Sort(new Order(Direction.ASC, "level"));

		return this.productIncomeRewardDao.findAll(spec,sort);

	}
}
