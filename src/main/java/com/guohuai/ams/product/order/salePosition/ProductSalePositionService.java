package com.guohuai.ams.product.order.salePosition;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.guohuai.ams.duration.assetPool.AssetPoolEntity;
import com.guohuai.ams.duration.assetPool.AssetPoolService;
import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductDao;
import com.guohuai.ams.product.ProductService;
import com.guohuai.cache.service.CacheProductService;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.StringUtil;
import com.guohuai.component.web.view.BaseResp;
import com.guohuai.component.web.view.PageResp;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountDao;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountEntity;
import com.guohuai.mmp.publisher.corporate.Corporate;
import com.guohuai.mmp.publisher.corporate.CorporateDao;
import com.guohuai.mmp.publisher.hold.PublisherHoldEntity;
import com.guohuai.mmp.publisher.hold.PublisherHoldService;
import com.guohuai.operate.api.AdminSdk;
import com.guohuai.operate.api.objs.admin.AdminObj;

@Service
@Transactional
public class ProductSalePositionService {

	@Autowired
	private ProductSalePositionDao salePositionDao;
	@Autowired
	private ProductSaleScheduleDao productSaleScheduleDao;
	@Autowired
	private ProductDao productDao;
	@Autowired
	private ProductService productService;
	@Autowired
	private AdminSdk adminSdk;
	@Autowired
	private CorporateDao corporateDao;
	@Autowired
	private AssetPoolService assetPoolService;
	@Autowired
	private PublisherBaseAccountDao publisherBaseAccountDao;
	@Autowired
	private PublisherHoldService publisherHoldService;
	@Autowired
	private CacheProductService cacheProductService;

	/**
	 * 获取指定id的ProductSalePositionOrder对象
	 * 
	 * @param oid
	 *            ProductSalePositionOrder对象id
	 * @return {@link ProductSalePositionOrder}
	 */
	public ProductSalePositionOrder getProductSalePositionByOid(String oid) {
		ProductSalePositionOrder pspo = this.salePositionDao.findOne(oid);
		if (pspo == null || ProductSalePositionOrder.STATUS_DELETE.equals(pspo.getStatus())) {
			throw AMPException.getException(90003);
		}
		return pspo;
	}

	/**
	 * ProductSalePositionOrder列表
	 * 
	 * @param spec
	 * @param pageable
	 * @return
	 */
	public PageResp<ProductSalePositionResp> list(Specification<ProductSalePositionOrder> spec, Pageable pageable) {
		Page<ProductSalePositionOrder> pspos = this.salePositionDao.findAll(spec, pageable);

		PageResp<ProductSalePositionResp> pagesRep = new PageResp<ProductSalePositionResp>();

		if (pspos != null && pspos.getContent() != null && pspos.getTotalElements() > 0) {

			Map<String, AdminObj> adminObjMap = new HashMap<String, AdminObj>();
			AdminObj adminObj = null;

			List<ProductSalePositionResp> rows = new ArrayList<ProductSalePositionResp>();
			for (ProductSalePositionOrder pspo : pspos) {
				ProductSalePositionResp pspor = new ProductSalePositionResp(pspo);

				if (!StringUtil.isEmpty(pspor.getCreator())) {
					if (adminObjMap.get(pspor.getCreator()) == null) {
						try {
							adminObj = adminSdk.getAdmin(pspor.getCreator());
							adminObjMap.put(pspor.getCreator(), adminObj);
						} catch (Exception e) {
						}

					}
					if (adminObjMap.get(pspor.getCreator()) != null) {
						pspor.setCreator(adminObjMap.get(pspor.getCreator()).getName());
					}
				}

				if (!StringUtil.isEmpty(pspor.getAuditor())) {
					if (adminObjMap.get(pspor.getAuditor()) == null) {
						try {
							adminObj = adminSdk.getAdmin(pspor.getAuditor());
							adminObjMap.put(pspor.getAuditor(), adminObj);
						} catch (Exception e) {
						}
					}
					if (adminObjMap.get(pspor.getAuditor()) != null) {
						pspor.setAuditor(adminObjMap.get(pspor.getAuditor()).getName());
					}
				}
				rows.add(pspor);
			}
			pagesRep.setRows(rows);
		}
		pagesRep.setTotal(pspos.getTotalElements());
		return pagesRep;
	}

	public ProductSalePositionApplyResp findSalePositionApply(final String productOid) {
		ProductSalePositionApplyResp productSalePositionApplyResp = null;

		Product product = this.productService.getProductByOid(productOid);
		
		BigDecimal approvalAmount = productSaleScheduleDao.findApprovalAmountByProductOid(productOid);//申请过的已经生效份额
		if(approvalAmount==null) {
			approvalAmount = BigDecimal.ZERO;
		}
		BigDecimal applyAmount = BigDecimal.ZERO;//申请过的未开始生效份额
		
		final Date today = DateUtil.formatUtilToSql(DateUtil.getCurrDate());
		Specification<ProductSaleScheduling> spec = new Specification<ProductSaleScheduling>() {
			@Override
			public Predicate toPredicate(Root<ProductSaleScheduling> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				Predicate product = cb.equal(root.get("product").get("oid").as(String.class), productOid);
				Predicate basicDate = cb.greaterThanOrEqualTo(root.get("basicDate").as(Date.class), today);
				Predicate auditAmount = cb.gt(root.get("auditAmount").as(BigDecimal.class), BigDecimal.ZERO);
				Predicate applyAmount = cb.gt(root.get("applyAmount").as(BigDecimal.class), BigDecimal.ZERO);
				return cb.and(product, basicDate, cb.or(auditAmount, applyAmount));
			}
		};
		List<ProductSaleScheduling> schs = productSaleScheduleDao.findAll(spec, new Sort(new Order(Direction.ASC, "basicDate")));
		List<ProductSaleApplyScheduleResp> schedules = null;
		if (schs != null && schs.size() > 0) {
			schedules = new ArrayList<ProductSaleApplyScheduleResp>();
			ProductSaleApplyScheduleResp schedule = null;
			for (ProductSaleScheduling sch : schs) {
				if (sch.getBasicDate().getTime() == today.getTime()) {//今天的申请份额
					applyAmount = applyAmount.add(sch.getApplyAmount());
				} else {//今天之后的申请份额和审批份额
					applyAmount = applyAmount.add(sch.getApplyAmount()).add(sch.getAuditAmount());
				}
				schedule = new ProductSaleApplyScheduleResp(sch);
				schedules.add(schedule);
			}
		}
		
		AssetPoolEntity assetPool = this.assetPoolService.getByOid(product.getAssetPool().getOid());
		if (assetPool != null) {
			PublisherBaseAccountEntity spv = this.publisherBaseAccountDao.findOne(assetPool.getSpvEntity().getOid());

			PublisherHoldEntity hold = this.publisherHoldService.getAssetPoolSpvHold(assetPool, spv);
			if (hold != null) {
				BigDecimal holdTotalVolume = hold.getTotalVolume()!=null?hold.getTotalVolume():BigDecimal.ZERO;
				productSalePositionApplyResp = new ProductSalePositionApplyResp(product, applyAmount, approvalAmount, holdTotalVolume);
				productSalePositionApplyResp.setSchedules(schedules);
				
				if (product.getAssetPool() != null && product.getAssetPool().getSpvEntity() != null) {
					Corporate corporate = this.corporateDao.findOne(product.getAssetPool().getSpvEntity().getCorperateOid());
					if(corporate!=null) {
						productSalePositionApplyResp.setSpvName(corporate.getName());
					}	
				}

			}
			
		}

		return productSalePositionApplyResp;
	}

	public BaseResp save(SaveProductSalePositionForm form, String operator) {
		BaseResp response = new BaseResp();

		final String productOid = form.getProductOid();
		final Date basicDate = DateUtil.parseToSqlDate(form.getBasicDate());// 排期日期
		BigDecimal newMaxSaleVolume = new BigDecimal(form.getNewMaxSaleVolume());
		Timestamp now = new Timestamp(System.currentTimeMillis());

		if (DateUtil.getCurrDate().getTime() > basicDate.getTime()) {
			response.setErrorCode(-1);
			response.setErrorMessage("排期日期不能小于今天日期");
			return response;
		}

		Product product = this.productService.getProductByOid(form.getProductOid());

		Specification<ProductSaleScheduling> scheduleSpec = new Specification<ProductSaleScheduling>() {
			@Override
			public Predicate toPredicate(Root<ProductSaleScheduling> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.and(cb.equal(root.get("product").get("oid").as(String.class), productOid), cb.equal(root.get("basicDate").as(Date.class), basicDate));
			}
		};

		ProductSaleScheduling productSaleSchedule = productSaleScheduleDao.findOne(scheduleSpec);
		if (productSaleSchedule == null) {
			productSaleSchedule = new ProductSaleScheduling();
			productSaleSchedule.setOid(StringUtil.uuid());
			productSaleSchedule.setProduct(product);
			productSaleSchedule.setBasicDate(basicDate);
			productSaleSchedule.setAuditAmount(BigDecimal.ZERO);
			productSaleSchedule.setApplyAmount(newMaxSaleVolume);
			productSaleSchedule.setApprovalAmount(BigDecimal.ZERO);
			productSaleSchedule.setCreateTime(now);
			productSaleSchedule.setUpdateTime(now);
			productSaleScheduleDao.save(productSaleSchedule);
		} else {
			productSaleSchedule.setApplyAmount(productSaleSchedule.getApplyAmount().add(newMaxSaleVolume));
			productSaleSchedule.setUpdateTime(now);
		}

		ProductSalePositionOrder pspo = new ProductSalePositionOrder();
		pspo.setOid(StringUtil.uuid());
		pspo.setProduct(product);
		pspo.setProductSaleScheduling(productSaleSchedule);
		pspo.setVolume(newMaxSaleVolume);// 申请份额
		pspo.setBasicDate(basicDate);
		pspo.setCreateTime(now);
		pspo.setCreator(operator);
		pspo.setStatus(ProductSalePositionOrder.STATUS_SUBMIT);
		salePositionDao.save(pspo);

		return response;
	}

	public BaseResp auditFail(String oid, String operator) {
		BaseResp response = new BaseResp();

		ProductSalePositionOrder pspo = this.getProductSalePositionByOid(oid);
		if (!ProductSalePositionOrder.STATUS_SUBMIT.equals(pspo.getStatus())) {
			throw AMPException.getException(90013);
		}
		pspo.setAuditTime(new Timestamp(System.currentTimeMillis()));
		pspo.setAuditor(operator);
		pspo.setStatus(ProductSalePositionOrder.STATUS_FAIL);
		salePositionDao.saveAndFlush(pspo);

		if (pspo.getProductSaleScheduling() != null) {
			ProductSaleScheduling productSaleSchedule = this.productSaleScheduleDao.findOne(pspo.getProductSaleScheduling().getOid());
			productSaleSchedule.setApplyAmount(productSaleSchedule.getApplyAmount().subtract(pspo.getVolume()));// 申请份额
			productSaleScheduleDao.saveAndFlush(productSaleSchedule);
		}

		return response;
	}

	public BaseResp auditPass(String oid, String operator) throws Exception {
		BaseResp response = new BaseResp();

		ProductSalePositionOrder pspo = this.getProductSalePositionByOid(oid);
		if (pspo.getProductSaleScheduling() == null || !ProductSalePositionOrder.STATUS_SUBMIT.equals(pspo.getStatus())) {
			throw AMPException.getException(90013);
		}
		ProductSaleScheduling productSaleSchedule = this.productSaleScheduleDao.findOne(pspo.getProductSaleScheduling().getOid());

		Timestamp now = new Timestamp(System.currentTimeMillis());
		pspo.setAuditTime(now);
		pspo.setAuditor(operator);

		Date today = DateUtil.formatUtilToSql(DateUtil.getCurrDate());

		if (productSaleSchedule.getBasicDate().getTime() == today.getTime()) {// 排期是当日 发放可售余额申请
			this.sendApplyImmediately(oid);// 可售份额申请当日排期立即发送
		} else {
			BigDecimal newMaxSaleVolume = pspo.getVolume();
			productSaleSchedule.setApplyAmount(productSaleSchedule.getApplyAmount().subtract(newMaxSaleVolume));// 申请份额
			productSaleSchedule.setAuditAmount(productSaleSchedule.getAuditAmount().add(newMaxSaleVolume));// 审批份额
			productSaleSchedule.setUpdateTime(now);
			productSaleScheduleDao.saveAndFlush(productSaleSchedule);

			pspo.setStatus(ProductSalePositionOrder.STATUS_PASS);
			salePositionDao.saveAndFlush(pspo);
		}

		return response;
	}

	/**
	 * 可售份额申请当日排期立即发送
	 * 
	 * @param oid
	 * @param operator
	 * @throws Exception
	 */
	private void sendApplyImmediately(String oid) throws Exception {
		ProductSalePositionOrder pspo = this.getProductSalePositionByOid(oid);
		if (pspo.getProduct() == null || pspo.getProductSaleScheduling() == null) {
			throw AMPException.getException(90031);
		}
		
		ProductSaleScheduling productSaleSchedule = this.productSaleScheduleDao.findOne(pspo.getProductSaleScheduling().getOid());
		BigDecimal newMaxSaleVolume = pspo.getVolume();//申请份额
		
		Product product = this.productService.getProductByOid(pspo.getProduct().getOid());
		AssetPoolEntity assetPool = this.assetPoolService.getByOid(product.getAssetPool().getOid());
		if (assetPool != null) {
			PublisherBaseAccountEntity spv = this.publisherBaseAccountDao.findOne(assetPool.getSpvEntity().getOid());

			PublisherHoldEntity hold = this.publisherHoldService.getAssetPoolSpvHold(assetPool, spv);
			if (hold != null) {
				BigDecimal holdTotalVolume = hold.getTotalVolume()!=null?hold.getTotalVolume():BigDecimal.ZERO;
                //更新产品的maxSaleVolume
				int adjust = productDao.updateMaxSaleVolume(pspo.getProduct().getOid(), newMaxSaleVolume, holdTotalVolume);
				if(adjust>0) {
					Timestamp now = new Timestamp(System.currentTimeMillis());
					pspo.setStatus(ProductSalePositionOrder.STATUS_ACTIVE);
					salePositionDao.save(pspo);
					
					productSaleSchedule.setApplyAmount(productSaleSchedule.getApplyAmount().subtract(newMaxSaleVolume));// 申请份额
					productSaleSchedule.setAuditAmount(productSaleSchedule.getAuditAmount().add(newMaxSaleVolume));// 审批份额
					productSaleSchedule.setApprovalAmount(productSaleSchedule.getApprovalAmount().add(newMaxSaleVolume));// 生效份额
					productSaleSchedule.setSyncTime(now);// 同步时间
					productSaleSchedule.setUpdateTime(now);
					productSaleScheduleDao.save(productSaleSchedule);
					
					/**
					 * 同步信息到redis
					 * @author yuechao
					 */
				//	cacheProductService.syncProductMaxSaleVolume(pspo.getProduct().getOid(), newMaxSaleVolume);
				} else {
					throw AMPException.getException(90029);
				}
			}
		}
		
	}

	public BaseResp rollbackApply(String oid, String operator) {
		BaseResp response = new BaseResp();

		ProductSalePositionOrder pspo = this.getProductSalePositionByOid(oid);
		if (!ProductSalePositionOrder.STATUS_SUBMIT.equals(pspo.getStatus())) {
			throw AMPException.getException(90028);
		}
		pspo.setAuditTime(new Timestamp(System.currentTimeMillis()));
		pspo.setAuditor(operator);
		pspo.setStatus(ProductSalePositionOrder.STATUS_CANCEL);
		salePositionDao.saveAndFlush(pspo);

		if (pspo.getProductSaleScheduling() != null) {
			ProductSaleScheduling productSaleSchedule = this.productSaleScheduleDao.findOne(pspo.getProductSaleScheduling().getOid());
			productSaleSchedule.setApplyAmount(productSaleSchedule.getApplyAmount().subtract(pspo.getVolume()));// 申请份额
			productSaleScheduleDao.saveAndFlush(productSaleSchedule);
		}

		return response;

	}

	public BaseResp delete(String oid, String operator) {
		BaseResp response = new BaseResp();

		ProductSalePositionOrder pspo = this.getProductSalePositionByOid(oid);
		if (!ProductSalePositionOrder.STATUS_FAIL.equals(pspo.getStatus())) {
			throw AMPException.getException(90027);
		}
		pspo.setAuditTime(new Timestamp(System.currentTimeMillis()));
		pspo.setAuditor(operator);
		pspo.setStatus(ProductSalePositionOrder.STATUS_DELETE);
		salePositionDao.saveAndFlush(pspo);

		return response;
	}

}
