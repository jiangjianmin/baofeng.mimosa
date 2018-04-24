package com.guohuai.ams.order;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.guohuai.ams.acct.books.document.SPVDocumentService;
import com.guohuai.ams.duration.assetPool.AssetPoolEntity;
import com.guohuai.ams.duration.assetPool.AssetPoolService;
import com.guohuai.ams.duration.utils.ConstantUtil;
import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductDao;
import com.guohuai.ams.product.ProductDecimalFormat;
import com.guohuai.ams.product.ProductDetailResp;
import com.guohuai.ams.product.ProductService;
import com.guohuai.basic.common.SeqGenerator;
import com.guohuai.cache.service.CacheSPVHoldService;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.StringUtil;
import com.guohuai.component.web.view.PageResp;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountDao;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountEntity;
import com.guohuai.mmp.publisher.corporate.Corporate;
import com.guohuai.mmp.publisher.corporate.CorporateDao;
import com.guohuai.mmp.publisher.hold.PublisherHoldDao;
import com.guohuai.mmp.publisher.hold.PublisherHoldEntity;
import com.guohuai.mmp.publisher.hold.PublisherHoldService;
import com.guohuai.mmp.sys.CodeConstants;
import com.guohuai.operate.api.AdminSdk;
import com.guohuai.operate.api.objs.admin.AdminObj;

@Service
@Transactional
public class SPVOrderService {

	@Autowired
	private SPVOrderDao spvOrderDao;
	@Autowired
	private PublisherHoldDao holdDao;
	@Autowired
	private ProductDao productDao;
	@Autowired
	private PublisherHoldService publisherHoldService;
	@Autowired
	private ProductService productService;
	@Autowired
	private PublisherBaseAccountDao publisherBaseAccountDao;
	@Autowired
	private AssetPoolService assetPoolService;
	@Autowired
	private AdminSdk adminSdk;
	@Autowired
	private SPVDocumentService spvDocumentService;
	@Autowired
	private CorporateDao corporateDao;
	@Autowired
	private SeqGenerator seqGenerator;
	@Autowired
	private CacheSPVHoldService cacheSPVHoldService;

	/**
	 * 获取指定oid的订单对象
	 * 
	 * @param oid
	 *            订单对象id
	 * @return {@link InvestorOrder}
	 */
	public SPVOrder getSpvOrderById(String oid) {
		SPVOrder investorOrder = this.spvOrderDao.findOne(oid);
		if (investorOrder == null || SPVOrder.STATUS_Disable.equals(investorOrder.getOrderStatus())) {
			throw AMPException.getException(100000);
		}
		return investorOrder;

	}

	@Transactional
	public SPVOrder saveSpvOrder(SaveSPVOrderForm form, String operator) {
		Timestamp now = new Timestamp(System.currentTimeMillis());

		AssetPoolEntity assetPool = this.assetPoolService.getByOid(form.getAssetPoolOid());
		if (assetPool == null) {
			throw AMPException.getException(30001);
		}
		
		PublisherBaseAccountEntity spv = this.publisherBaseAccountDao.findOne(assetPool.getSpvEntity().getOid());

		String orderCode = this.seqGenerator.next(CodeConstants.SPV_order);
		
		String productType = null;
		List<Product> products = productService.getProductListByAssetPoolOid(assetPool.getOid());
		if (products != null && products.size() >0){
			productType = products.get(0).getType().getOid();
		}
		
		PublisherHoldEntity hold = this.publisherHoldService.getAssetPoolSpvHold4SpvOrder(assetPool, spv, productType);
		SPVOrder spvOrder = new SPVOrder();
		spvOrder.setOid(StringUtil.uuid());
		spvOrder.setSpv(spv);
		spvOrder.setAssetPool(assetPool);
		spvOrder.setHold(hold);
		spvOrder.setOrderCode(orderCode);
		spvOrder.setOrderType(form.getOrderType());
		spvOrder.setOrderCate(form.getOrderCate());
		spvOrder.setOrderAmount(ProductDecimalFormat.multiply(new BigDecimal(form.getOrderAmount()),10000));
		spvOrder.setOrderVolume(ProductDecimalFormat.multiply(new BigDecimal(form.getOrderAmount()),10000));
		spvOrder.setOrderDate(DateUtil.parseToSqlDate(form.getOrderDate()));
		spvOrder.setOrderStatus(SPVOrder.STATUS_Submit);
		spvOrder.setEntryStatus(SPVOrder.ENTRY_STATUS_No);
		spvOrder.setPayFee(BigDecimal.ZERO);
		spvOrder.setCreater(operator);
		spvOrder.setCreateTime(now);
		spvOrder.setUpdateTime(now);
		
		spvOrder = this.spvOrderDao.save(spvOrder);

		return spvOrder;
	}

	@Transactional
	public SPVOrder delete(String oid, String operator) {
		Timestamp now = new Timestamp(System.currentTimeMillis());

		SPVOrder investorOrder = this.getSpvOrderById(oid);
		investorOrder.setOrderStatus(SPVOrder.STATUS_Disable);
		investorOrder.setUpdateTime(now);
		this.spvOrderDao.saveAndFlush(investorOrder);
		return investorOrder;
	}

	public SPVOrder confirm(AuditSPVOrderForm form, String operator) throws Exception {
		
		Timestamp now = new Timestamp(System.currentTimeMillis());

		SPVOrder spvOrder = this.getSpvOrderById(form.getOid());

		BigDecimal avaibleAmount = ProductDecimalFormat.multiply(new BigDecimal(form.getAvaibleAmount()), 10000);
		BigDecimal payFee = ProductDecimalFormat.multiply(new BigDecimal(form.getPayFee()), 10000);

		if (spvOrder.getOrderAmount().compareTo(avaibleAmount.add(payFee)) != 0) {
			throw AMPException.getException(100004);
		}
		
		PublisherHoldEntity holder = null;
		if (spvOrder.getHold() != null) {
			holder =this.publisherHoldService.findByOid(spvOrder.getHold().getOid());
		}
		
		AssetPoolEntity assetPool = null;
		if(spvOrder.getAssetPool()!=null) {
			assetPool = this.assetPoolService.getByOid(spvOrder.getAssetPool().getOid());
		}
		String holdOid = holder.getOid();
		
		if (SPVOrder.ORDER_TYPE_Redeem.equals(spvOrder.getOrderType())) {
			int adjust = -1;
			//调原始sql
			if(holder!=null) {
				adjust = holdDao.spvOrderRedeemConfirm(holdOid, avaibleAmount);
			}
			
			if(adjust<=0) {
				throw AMPException.getException(100003);
			}
			
			spvOrder.setOrderStatus(SPVOrder.STATUS_Confirm);
			spvOrder.setAuditor(operator);
			spvOrder.setCompleteTime(now);
			spvOrder.setUpdateTime(now);
			spvOrder.setPayFee(spvOrder.getPayFee().add(payFee));
			spvOrderDao.saveAndFlush(spvOrder);
			
			spvDocumentService.redemption(assetPool.getOid(), spvOrder.getOid(), spvOrder.getOrderAmount(), avaibleAmount, payFee);
			
			//调原始sql
			if(assetPool!=null) {
				// SPV累计计提费金
				assetPoolService.dualChargeFee(assetPool.getOid(), payFee, ConstantUtil.REDEEM);
				//编辑资产池账户信息
				assetPoolService.editPoolForCash(assetPool.getOid(), new BigDecimal(0).subtract(new BigDecimal(form.getAvaibleAmount())), operator);
				List<Product> products = productService.getProductListByAssetPoolOid(assetPool.getOid());
				if (products != null && products.size() > 0) {
					Product p = products.get(0);
					if(p.getType().getOid().equals(Product.TYPE_Producttype_02)) {
						int adjustp = -1;
						adjustp = this.productDao.adjustRaisedTotalNumber(p.getOid(), new BigDecimal(0).subtract(avaibleAmount));
						if(adjustp<=0) {
							throw AMPException.getException(100003);
						}
					}
				}
			}
			/**
			 * 同步数据 redis
			 * @author yuechao
			 */
		//	cacheSPVHoldService.syncSpvHoldTotalVolume(holder.getProduct(), avaibleAmount.negate());
		} else if (SPVOrder.ORDER_TYPE_Invest.equals(spvOrder.getOrderType())) {

			//调原始sql
			int adjust = -1;
			adjust = holdDao.spvOrderInvestConfirm(holdOid, avaibleAmount);
			if(adjust<=0) {
				throw AMPException.getException(100003);
			}
			
			spvOrder.setOrderStatus(SPVOrder.STATUS_Confirm);
			spvOrder.setAuditor(operator);
			spvOrder.setCompleteTime(now);
			spvOrder.setUpdateTime(now);
			spvOrder.setPayFee(spvOrder.getPayFee().add(payFee));
			spvOrderDao.saveAndFlush(spvOrder);

			spvDocumentService.purchase(assetPool.getOid(), spvOrder.getOid(), spvOrder.getOrderAmount(), avaibleAmount, payFee);

			//调原始sql
			if(assetPool!=null) {
				// SPV累计计提费金
				assetPoolService.dualChargeFee(assetPool.getOid(), payFee, ConstantUtil.PURCHASE);
				//编辑资产池账户信息
				assetPoolService.editPoolForCash(assetPool.getOid(), new BigDecimal(form.getAvaibleAmount()), operator);

				// 如果存在03产品 直接处理该产品
				Product type03Product = productService.getType03ProductByAssetPoolOid(assetPool.getOid());
				if (type03Product != null) {
					int adjustp = -1;
					adjustp = this.productDao.adjustRaisedTotalNumber(type03Product.getOid(), avaibleAmount);
					if (adjustp <= 0) {
						throw AMPException.getException(100003);
					}
				} else {
					List<Product> products = productService.getProductListByAssetPoolOid(assetPool.getOid());
					if (products != null && products.size() > 0) {
						Product p = products.get(0);
						if(p.getType().getOid().equals(Product.TYPE_Producttype_02)) {
							int adjustp = -1;
							adjustp = this.productDao.adjustRaisedTotalNumber(products.get(0).getOid(), avaibleAmount);
							if(adjustp<=0) {
								throw AMPException.getException(100003);
							}
						}
					}
				}
			}
		}

		return spvOrder;
	}


	public ProductDetailResp getProduct(String assetPoolOid) {

		List<Product> products = productService.getProductListByAssetPoolOid(assetPoolOid);
		ProductDetailResp pr = null;
		if (products != null && products.size() > 0) {
			Product p = products.get(0);
			pr = new ProductDetailResp(p,null,null,null,null,null,null);
			if (p.getAssetPool().getSpvEntity() != null) {
				Corporate corporate = this.corporateDao.findOne(p.getAssetPool().getSpvEntity().getCorperateOid());
				if(corporate!=null) {
					pr.setSpvName(corporate.getName());
				}
			}
		} else {
			pr = new ProductDetailResp();
		}
		return pr;
	}

	public PageResp<SPVOrderResp> list(Specification<SPVOrder> spec, Pageable pageable) {
		PageResp<SPVOrderResp> pagesRep = new PageResp<SPVOrderResp>();

		Page<SPVOrder> cas = this.spvOrderDao.findAll(spec, pageable);

		if (cas != null && cas.getContent() != null && cas.getTotalElements() > 0) {
			List<SPVOrderResp> rows = new ArrayList<SPVOrderResp>();

			Map<String, AdminObj> adminObjMap = new HashMap<String, AdminObj>();
			
			Map<String, Corporate> aoprateObjMap = new HashMap<String, Corporate>();

			AdminObj adminObj = null;
			Corporate corporate = null;
			for (SPVOrder order : cas) {
				SPVOrderResp spvOrderRep = new SPVOrderResp(order);
				//spvName获取
				if (order.getSpv() != null) {
					if (aoprateObjMap.get(order.getSpv().getCorperateOid()) == null) {
						try {
							corporate = this.corporateDao.findOne(order.getSpv().getCorperateOid());
							if(corporate!=null) {
								aoprateObjMap.put(order.getSpv().getCorperateOid(), corporate);
							}
						} catch (Exception e) {
						}
					}
					if (aoprateObjMap.get(order.getSpv().getCorperateOid()) != null) {
						spvOrderRep.setSpvName(aoprateObjMap.get(order.getSpv().getCorperateOid()).getName());
					}
				}

				if (adminObjMap.get(order.getCreater()) == null) {
					try {
						adminObj = adminSdk.getAdmin(order.getCreater());
						adminObjMap.put(order.getCreater(), adminObj);
					} catch (Exception e) {
					}
				}
				if (adminObjMap.get(order.getCreater()) != null) {
					spvOrderRep.setCreater(adminObjMap.get(order.getCreater()).getName());
				}

				if (adminObjMap.get(order.getAuditor()) == null) {
					try {
						adminObj = adminSdk.getAdmin(order.getAuditor());
						adminObjMap.put(order.getAuditor(), adminObj);
					} catch (Exception e) {
					}
				}
				if (adminObjMap.get(order.getAuditor()) != null) {
					spvOrderRep.setAuditor(adminObjMap.get(order.getAuditor()).getName());
				}
				rows.add(spvOrderRep);
			}
			pagesRep.setRows(rows);
		}
		pagesRep.setTotal(cas.getTotalElements());

		return pagesRep;
	}

	public SPVOrderDetailResp detail(String oid) {

		SPVOrder order = this.spvOrderDao.findOne(oid);

		SPVOrderDetailResp spvOrderRep = new SPVOrderDetailResp(order);
		if (order.getSpv() != null) {
			Corporate corporate = this.corporateDao.findOne(order.getSpv().getCorperateOid());
			if(corporate!=null) {
				spvOrderRep.setSpvName(corporate.getName());
			}
		}

		Map<String, AdminObj> adminObjMap = new HashMap<String, AdminObj>();

		AdminObj adminObj = null;

		if (adminObjMap.get(order.getCreater()) == null) {
			try {
				adminObj = adminSdk.getAdmin(order.getCreater());
				adminObjMap.put(order.getCreater(), adminObj);
			} catch (Exception e) {
			}
		}
		if (adminObjMap.get(order.getCreater()) != null) {
			spvOrderRep.setCreater(adminObjMap.get(order.getCreater()).getName());
		}

		if (adminObjMap.get(order.getAuditor()) == null) {
			try {
				adminObj = adminSdk.getAdmin(order.getAuditor());
				adminObjMap.put(order.getAuditor(), adminObj);
			} catch (Exception e) {
			}
		}
		if (adminObjMap.get(order.getAuditor()) != null) {
			spvOrderRep.setAuditor(adminObjMap.get(order.getAuditor()).getName());
		}
		return spvOrderRep;
	}

	/**
	 * 查询满足条件的订单列表，计算资产池的实际市值
	 * 
	 * @param pid
	 *            资产池id
	 * @param baseDate
	 *            基准日
	 * @author star
	 * @return
	 */
	/*
	public List<Object[]> getListForMarketAdjust(String pid, Date baseDate) {
		List<SPVOrder> list = this.spvOrderDao.getListForMarketAdjust(pid, baseDate);
		if (null != list && !list.isEmpty()) {
			List<Object[]> objList = Lists.newArrayList();
			Object[] obj = null;
			for (SPVOrder order : list) {
				obj = new Object[3];
				obj[0] = order.getOid();
				obj[1] = order.getOrderType();
				obj[2] = order.getOrderAmount();
				objList.add(obj);
			}
			return objList;
		}

		return null;
	}
	*/
	
	public List<SPVOrder> getListForMarketAdjust(String pid, Date baseDate) {
		List<SPVOrder> list = this.spvOrderDao.getListForMarketAdjust(pid, baseDate);
		return list;
	}
	
	/**
	 * 更新订单状态为已入账
	 * 
	 * @param oid
	 * @author star
	 */
	public void updateEntryStatus(String oid) {
		this.spvOrderDao.updateEntryStatus(oid);
	}

}
