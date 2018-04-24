package com.guohuai.ams.product;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.guohuai.ams.acct.books.AccountBook;
import com.guohuai.ams.acct.books.AccountBookService;
import com.guohuai.ams.duration.assetPool.AssetPoolEntity;
import com.guohuai.ams.label.LabelEntity;
import com.guohuai.ams.label.LabelEnum;
import com.guohuai.ams.label.LabelService;
import com.guohuai.ams.productLabel.ProductLabel;
import com.guohuai.ams.productLabel.ProductLabelService;
import com.guohuai.basic.message.DealMessageEntity;
import com.guohuai.basic.message.DealMessageEnum;
import com.guohuai.cache.entity.ProductCacheEntity;
import com.guohuai.cache.service.CacheProductService;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.message.MessageSendUtil;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.StringUtil;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.component.web.view.BaseResp;
import com.guohuai.component.web.view.PageInterestResp;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountDao;
import com.guohuai.mmp.investor.referprofit.ProfitDetailService;
import com.guohuai.mmp.investor.referprofit.ProfitProvideDetailService;
import com.guohuai.mmp.investor.referprofit.ProfitRuleService;
import com.guohuai.mmp.investor.tradeorder.InvestorRepayCashTradeOrderService;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderService;
import com.guohuai.mmp.platform.baseaccount.statistics.PlatformStatisticsService;
import com.guohuai.mmp.platform.publisher.product.offset.OffsetConstantRep;
import com.guohuai.mmp.platform.publisher.product.offset.ProductOffsetService;
import com.guohuai.mmp.publisher.baseaccount.statistics.PublisherStatisticsService;
import com.guohuai.mmp.publisher.corporate.Corporate;
import com.guohuai.mmp.publisher.corporate.CorporateDao;
import com.guohuai.mmp.serialtask.SerialTaskEntity;
import com.guohuai.mmp.serialtask.SerialTaskReq;
import com.guohuai.mmp.serialtask.SerialTaskService;
import com.guohuai.mmp.serialtask.TnProfitDetailParams;

import lombok.extern.slf4j.Slf4j;

@Transactional
@Service
@Slf4j
public class ProductDurationService {

	@Autowired
	private ProductDao productDao;
	@Autowired
	private ProductService productService;
	@Autowired
	private AccountBookService accountBookService;
	@Autowired
	private ProductOffsetService productOffsetService;
	@Autowired
	private InvestorRepayCashTradeOrderService investorCashTradeOrderService;
	@Autowired
	private CorporateDao corporateDao;
	@Autowired
	private PublisherStatisticsService publisherStatisticsService;
	@Autowired
	private PlatformStatisticsService platformStatisticsService;
	@Autowired
	private InvestorTradeOrderService investorTradeOrderService;
	@Autowired
	private ProductLabelService productLabelService;
	@Autowired
	private MessageSendUtil messageSendUtil;
	@Autowired
	private CacheProductService cacheProductService;
	@Autowired
	private LabelService labelService;
	@Autowired
	private InvestorBaseAccountDao investorBaseAccountDao;
	@Autowired
	private ProfitDetailService profitDetailService;
	@Autowired
	private ProfitProvideDetailService profitProvideDetailService;
	@Autowired
	private SerialTaskService serialTaskService;
	@Autowired
	private ProfitRuleService profitRuleService;
	@Autowired
	private ProductRelatedEMDao productRelatedEMDao;

	@Transactional
	public PageInterestResp<ProductLogListResp> durationList(Specification<Product> spec, Pageable pageable,ProductRelatedReq productRelatedReq) {
		long start=System.currentTimeMillis();
		BigDecimal raisedTotalVolume= BigDecimal.ZERO; // 募集总份额
		BigDecimal hqlaTotalVolume=BigDecimal.ZERO;;  // 流动性资产
		BigDecimal collectedTotalVolume= BigDecimal.ZERO; // 用户申购总额
		BigDecimal interestTotalAmount= BigDecimal.ZERO; // 分配收益总额
		BigDecimal repayTotalAmount= BigDecimal.ZERO; // 本金利息总额
		BigDecimal balanceCostTotalAmount= BigDecimal.ZERO;//结算成本总计
		BigDecimal businessCostTotalAmount= BigDecimal.ZERO;//运营成本总计
		Page<Product> cas = this.productDao.findAll(spec, pageable);
		PageInterestResp<ProductLogListResp> pagesRep = new PageInterestResp<ProductLogListResp>();
		List<ProductLogListResp> allList=Optional.ofNullable(cas.getContent()).orElse(new ArrayList<>()).stream().map(p->{
			return new ProductLogListResp(p);
		}).collect(Collectors.toList());
		pagesRep.setTotal(cas.getTotalElements());
		List<ProductRelatedInfoReq> productRelatedInfos=productRelatedEMDao.getProductRelatedInfo(productRelatedReq);
		ConcurrentLinkedDeque<BigDecimal> raisedTotalVolumeList=new ConcurrentLinkedDeque<>();
		ConcurrentLinkedDeque<BigDecimal> hqlaTotalVolumeList=new ConcurrentLinkedDeque<>();
		ConcurrentLinkedDeque<BigDecimal> collectedTotalVolumeList=new ConcurrentLinkedDeque<>();
		ConcurrentLinkedDeque<BigDecimal> interestTotalAmountList=new ConcurrentLinkedDeque<>();
		ConcurrentLinkedDeque<BigDecimal> balanceCostTotalAmountList=new ConcurrentLinkedDeque<>();
		ConcurrentLinkedDeque<BigDecimal> businessCostTotalAmountList=new ConcurrentLinkedDeque<>();
		
		CompletableFuture<Map<String, BigDecimal>> fupoid_SumIncomeAmount=CompletableFuture.supplyAsync(()->productRelatedEMDao.findSumIncomeAmount(productRelatedReq));
		CompletableFuture<Map<String, BigDecimal>> fupoid_SumTotalBaseIncome=CompletableFuture.supplyAsync(()->productRelatedEMDao.findSumTotalBaseIncome(productRelatedReq));
		CompletableFuture<Map<String, BigDecimal>> fupoid_SumTotalRewardIncome=CompletableFuture.supplyAsync(()->productRelatedEMDao.findSumTotalRewardIncome(productRelatedReq));
		CompletableFuture<Map<String, BigDecimal>> fupoid_allocateInterestAuditRatio=CompletableFuture.supplyAsync(()->productRelatedEMDao.getallocateInterestAuditRatio(productRelatedReq));
		Map<String, BigDecimal> poid_SumIncomeAmountALl=fupoid_SumIncomeAmount.join();
		Map<String, BigDecimal> poid_SumTotalBaseIncomeAll=fupoid_SumTotalBaseIncome.join();
		Map<String, BigDecimal> poid_SumTotalRewardIncomeAll=fupoid_SumTotalRewardIncome.join();
		Map<String, BigDecimal> poid_allocateInterestAuditRatioAll=fupoid_allocateInterestAuditRatio.join();
			
		productRelatedInfos.parallelStream().distinct().filter(p->Objects.equals(Product.TYPE_Producttype_01, p.getType())).forEach(p->{
				try {
					TimeUnit.MILLISECONDS.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				 raisedTotalVolumeList.add(p.getRaisedTotalNumber());
				 BigDecimal hqlaTotalVolumeTmp= BigDecimal.ZERO;
				if (null != p.getCashPosition()) {
					hqlaTotalVolumeTmp=	hqlaTotalVolumeTmp.add(p.getCashPosition());
				}
				if (null != p.getScale() && null != p.getCashtoolFactRate()) {
					hqlaTotalVolumeTmp=hqlaTotalVolumeTmp.add(p.getScale().multiply(p.getCashtoolFactRate()).setScale(2, RoundingMode.HALF_UP));
				}
				hqlaTotalVolumeTmp = hqlaTotalVolumeTmp.setScale(2, RoundingMode.HALF_UP);
				hqlaTotalVolumeList.add(hqlaTotalVolumeTmp);
				collectedTotalVolumeList.add(p.getCollectedVolume());
				if (Product.STATE_Durationend.equals(p.getState()) || Product.STATE_Clearing.equals(p.getState()) || Product.STATE_Cleared.equals(p.getState())) {
					BigDecimal allocateIncomeAmount = BigDecimal.ZERO;   // 总收益（募集期+存续期）
					BigDecimal productRaiseIncome = BigDecimal.ZERO;     // 募集期收益 
					BigDecimal productDurationIncome = BigDecimal.ZERO;  // 存续期收益 
					BigDecimal incomeAmount = BigDecimal.ZERO;
					BigDecimal sumIncomeAmount = poid_SumIncomeAmountALl.get(p.getOid());
					if(ObjectUtils.isEmpty(sumIncomeAmount)) {
						sumIncomeAmount = new BigDecimal(0);
					}
					BigDecimal sumTotalBaseIncome = poid_SumTotalBaseIncomeAll.get(p.getOid());
					if(ObjectUtils.isEmpty(sumTotalBaseIncome)) {
						sumTotalBaseIncome = new BigDecimal(0);
					}
					BigDecimal balanceCostSum = sumIncomeAmount.add(sumTotalBaseIncome);
					balanceCostTotalAmountList.add(balanceCostSum);
					/**
					 * totalRewardIncome跟couponAmount
					 */
					BigDecimal businessCostSum =poid_SumTotalRewardIncomeAll.get(p.getOid());
					if(ObjectUtils.isEmpty(businessCostSum)) {
						businessCostSum = new BigDecimal(0);
					}
					businessCostSum.setScale(2, RoundingMode.HALF_UP);
					 businessCostTotalAmountList.add(businessCostSum);
					// 实际年化收益率
					 BigDecimal ratio=poid_allocateInterestAuditRatioAll.get(p.getOid());
					if (null==ratio) {
						incomeAmount = p.getExpAror().multiply(p.getCollectedVolume()).divide(new BigDecimal(100));
					} else {
						incomeAmount = ratio.multiply(p.getCollectedVolume()).divide(new BigDecimal(100));
					}
					
					productDurationIncome = incomeAmount.multiply(new BigDecimal(p.getDurationPeriodDays()))
							.divide(new BigDecimal(p.getIncomeCalcBasis()), 2, BigDecimal.ROUND_DOWN); // 存续期收益
					if (p.getRecPeriodExpAnYield().compareTo(BigDecimal.ZERO) == 0) {  // 募集期无利息
						allocateIncomeAmount = allocateIncomeAmount.add(productDurationIncome);
					} else { // 募集期有利息
						// 募集期收益（从定期募集期收益明细中查询T_MONEY_PRODUCT_RAISING_INCOME）
						productRaiseIncome =  poid_SumIncomeAmountALl.get(p.getOid());
						if (productRaiseIncome == null) {
							// error.define[16007]=募集期利息没有生成(CODE:16007)
							throw AMPException.getException(16007);
						}else {
							allocateIncomeAmount = allocateIncomeAmount.add(productRaiseIncome).add(productDurationIncome);
						}
					}
					interestTotalAmountList.add(allocateIncomeAmount);
				}
			});
		raisedTotalVolume= raisedTotalVolumeList.stream().reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
		hqlaTotalVolume=hqlaTotalVolumeList.parallelStream().reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
		hqlaTotalVolume.setScale(2, RoundingMode.HALF_UP);
		collectedTotalVolume=collectedTotalVolumeList.parallelStream().reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
		interestTotalAmount=interestTotalAmountList.parallelStream().reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
		repayTotalAmount=collectedTotalVolume.add(interestTotalAmount);
		balanceCostTotalAmount= balanceCostTotalAmountList.parallelStream().reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
		businessCostTotalAmount= businessCostTotalAmountList.parallelStream().reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
		//拼cas
		List<String> dict= Arrays.asList("DURATIONEND","CLEARING","CLEARED");
		CompletableFuture<List<String>> fustatePoids=CompletableFuture.supplyAsync(()->{
			return allList.parallelStream().filter(p->dict.contains(p.getStatus()))
					.map(ProductLogListResp::getOid).collect(Collectors.toList());
		});
		CompletableFuture<List<String>> fulimitPoids=CompletableFuture.supplyAsync(()->{
			return allList.parallelStream().filter(p->{
				return Objects.equals("PRODUCTTYPE_01", p.getTypeOid())&&dict.contains(p.getStatus());
				}).map(ProductLogListResp::getOid).collect(Collectors.toList());
		});
		CompletableFuture<Map<String, String>> fupoid_corporateId=CompletableFuture.supplyAsync(()->{
			return allList.parallelStream().filter(p->{
				return StringUtils.isNoneBlank(p.getCorporateId());
			}).collect(Collectors.toMap(ProductLogListResp::getOid, ProductLogListResp::getCorporateId));
		});
		List<String> statePoids=fustatePoids.join();
		List<String> limitPoids=fulimitPoids.join();
		Map<String, String> poid_corporateId=fupoid_corporateId.join();
		Collection<String> corporateIds=poid_corporateId.values();
		Map<String, String> poid_spvNames=productRelatedEMDao.getCorporateNamesByOids(corporateIds,poid_corporateId);
		Map<String, BigDecimal> poid_SumIncomeAmount=productRelatedEMDao.getSumIncomeAmountByPoids(statePoids);
		Map<String, BigDecimal> poid_SumBaseIncome=productRelatedEMDao.getSumBaseIncomeByPoids(statePoids);
		Map<String, BigDecimal> poid_RewardIncome=productRelatedEMDao.getSumRewardIncomeByPoids(statePoids);
		Map<String, BigDecimal> poid_allocateInterestAuditRatio=productRelatedEMDao.getallocateInterestAuditRatioByPoids(limitPoids);
		Map<String, BigDecimal> poid_SumProductRaiseIncome=productRelatedEMDao.getSumIncomeAmountByPoids(limitPoids);
		
		ConcurrentLinkedDeque<ProductLogListResp> productLogListRespRows=new ConcurrentLinkedDeque<>();
		allList.parallelStream().forEach(queryRep->{
			String poid=queryRep.getOid();
			try {
				TimeUnit.MILLISECONDS.sleep(2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			queryRep.setSpvName(poid_spvNames.get(poid));
			if (Product.STATE_Durationend.equals(queryRep.getStatus()) || Product.STATE_Clearing.equals(queryRep.getStatus()) || Product.STATE_Cleared.equals(queryRep.getStatus())) {
				BigDecimal sumIncomeAmount = poid_SumIncomeAmount.get(poid);
				if(ObjectUtils.isEmpty(sumIncomeAmount)) {
					sumIncomeAmount = new BigDecimal(0);
				}
				BigDecimal sumTotalBaseIncome = poid_SumBaseIncome.get(poid);
				if(ObjectUtils.isEmpty(sumTotalBaseIncome)) {
					sumTotalBaseIncome = new BigDecimal(0);
				}
				BigDecimal balanceCostSum = sumIncomeAmount.add(sumTotalBaseIncome);
				//根据productOid查询募集期活期收益、存续期定期收益并相加计算结算成本总额
				BigDecimal businessCostSum = poid_RewardIncome.get(poid);
				if(ObjectUtils.isEmpty(businessCostSum)) {
					businessCostSum = new BigDecimal(0);
				}
				queryRep.setBalanceCostSum(balanceCostSum);
				queryRep.setBusinessCostSum(businessCostSum);
				if (Product.TYPE_Producttype_01.equals(queryRep.getTypeOid())) {
					BigDecimal allocateIncomeAmount = BigDecimal.ZERO;   // 总收益（募集期+存续期）
					BigDecimal productRaiseIncome = BigDecimal.ZERO;     // 募集期收益 
					BigDecimal productDurationIncome = BigDecimal.ZERO;  // 存续期收益 
					BigDecimal incomeAmount = BigDecimal.ZERO;
					
					BigDecimal ratio=poid_allocateInterestAuditRatio.get(poid);
					// 实际年化收益率
					if (null==ratio) {
//						queryRep.setRatio(ProductDecimalFormat.format(ProductDecimalFormat.multiply(new BigDecimal(queryRep.getExpAror())), "0.##"));
						queryRep.setRatio(queryRep.getExpAror());
						incomeAmount = new BigDecimal(queryRep.getExpAror()).multiply(queryRep.getCollectedVolume()).divide(new BigDecimal(100));
					} else {
						queryRep.setRatio(ratio.toString());
						incomeAmount = ratio.multiply(queryRep.getCollectedVolume()).divide(new BigDecimal(100));
					}
					productDurationIncome = incomeAmount.multiply(new BigDecimal(queryRep.getDurationPeriod()))
							.divide(new BigDecimal(queryRep.getIncomeCalcBasis()), 2, BigDecimal.ROUND_DOWN); // 存续期收益
					if (queryRep.getRecPeriodExpAnYield().compareTo(BigDecimal.ZERO) == 0) {  // 募集期无利息
						allocateIncomeAmount = allocateIncomeAmount.add(productDurationIncome);
						queryRep.setAllocateIncomeAmount(allocateIncomeAmount); // 分配收益总额（元）
					} else { // 募集期有利息
						// 募集期收益（从定期募集期收益明细中查询T_MONEY_PRODUCT_RAISING_INCOME）
						productRaiseIncome = poid_SumProductRaiseIncome.get(poid);
						if (null==productRaiseIncome) {
							// error.define[16007]=募集期利息没有生成(CODE:16007)
//							throw AMPException.getException(16007);
							queryRep.setAllocateIncomeAmount(null);
						}else {
							allocateIncomeAmount = allocateIncomeAmount.add(productRaiseIncome).add(productDurationIncome);
							queryRep.setAllocateIncomeAmount(allocateIncomeAmount); // 分配收益总额（元）
						}
					}
					queryRep.setTotalRepay(queryRep.getCollectedVolume().add(allocateIncomeAmount)); // 本金利息总额（元）
				}
			}
			
			productLogListRespRows.add(queryRep);
		});
		
		pagesRep.setRows(productLogListRespRows.parallelStream().sorted(Comparator.comparing(ProductLogListResp::getUpdateTime).reversed()).collect(Collectors.toList()));
		pagesRep.setRaisedTotalVolume(raisedTotalVolume);
		pagesRep.setHqlaTotalVolume(hqlaTotalVolume);
		pagesRep.setCollectedTotalVolume(collectedTotalVolume);
		pagesRep.setInterestTotalAmount(interestTotalAmount);
		pagesRep.setRepayTotalAmount(repayTotalAmount);
		pagesRep.setBalanceCostTotalAmount(balanceCostTotalAmount);
		pagesRep.setBusinessCostTotalAmount(businessCostTotalAmount);
		long start1=System.currentTimeMillis();
		log.info("耗时={}",(start1-start));
		return pagesRep;
	}
	

	/**
	 * 获取存续期产品的名称列表，包含id
	 * 
	 * @return
	 */
	@Transactional
	public List<JSONObject> productNameList(Specification<Product> spec) {
		List<JSONObject> jsonObjList = Lists.newArrayList();
		List<Product> ps = productDao.findAll(spec, new Sort(new Order(Direction.DESC, "updateTime")));
		if (ps != null && ps.size() > 0) {
			JSONObject jsonObj = null;
			for (Product p : ps) {
				jsonObj = new JSONObject();
				jsonObj.put("oid", p.getOid());
				jsonObj.put("name", p.getName());
				jsonObjList.add(jsonObj);
			}
		}

		return jsonObjList;
	}

	public ProductDetailResp getProductByOid(String oid) {
		ProductDetailResp pr = null;
		if (StringUtil.isEmpty(oid)) {
			Specification<Product> spec = new Specification<Product>() {
				@Override
				public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
					return cb.and(cb.equal(root.get("isDeleted").as(String.class), Product.NO), cb.equal(root.get("auditState").as(String.class), Product.AUDIT_STATE_Reviewed));
				}
			};
			spec = Specifications.where(spec);
			List<Product> ps = productDao.findAll(spec, new Sort(new Order(Direction.DESC, "updateTime")));
			if (ps != null && ps.size() > 0) {
				Product p = ps.get(0);
				pr = new ProductDetailResp(p,null,null,null,null,null,null);
				if (p.getAssetPool().getSpvEntity() != null) {
					Corporate corporate = this.corporateDao.findOne(p.getAssetPool().getSpvEntity().getCorperateOid());
					if(corporate!=null) {
						pr.setSpvName(corporate.getName());
					}
				}
			}
		} else {
			pr = this.productService.read(oid);
		}

		return pr;
	}

//	/**
//	 * 投资赎回确认接口
//	 * 
//	 * @param productOid
//	 *            产品oid
//	 * @param investAmount
//	 *            投资金额
//	 * @param remeedAmount
//	 *            赎回金额
//	 * @throws Exception
//	 */
//	public void comfirmInvest(String productOid, BigDecimal investAmount, BigDecimal remeedAmount) throws Exception {
//		if (!StringUtil.isEmpty(productOid)) {
//			Product p = productService.getProductByOid(productOid);
//
//			if (investAmount == null) {
//				investAmount = BigDecimal.ZERO;
//			}
//			if (remeedAmount == null) {
//				remeedAmount = BigDecimal.ZERO;
//			}
//			if (p.getAssetPool() == null || p.getAssetPool().getSpvEntity() == null) {
//				throw AMPException.getException("没有找到产品对应的资产池和SPV");
//			}
//			if (p.getAssetPool() != null && p.getAssetPool().getSpvEntity() != null) {
//
//				AssetPoolEntity assetPool = this.assetPoolService.getByOid(p.getAssetPool().getOid());
//
//				PublisherBaseAccountEntity spv = this.publisherBaseAccountDao.findOne(assetPool.getSpvEntity().getOid());
//
//				PublisherHoldEntity hold = this.publisherHoldService.getAssetPoolSpvHold(assetPool, spv);
//				if (hold == null) {
//					throw AMPException.getException("没有找到产品对应的持有人名册");
//				}
//
//				// 其他事件: 判断产品状态是否是清盘中, 如果是清盘中, 调用 清盘完成接口
//				if (Product.STATE_Clearing.equals(p.getState())) {
//					this.productCleared(productOid);
//				}
//
//
//			}
//
//		}
//
//	}

	/**
	 * 产品清盘接口:
	 * 清盘中的产品, 不可开启申购/开启赎回, 并在该事件中自动关闭申购/赎回, 在产品表中记录清盘操作人, 清盘操作时间
	 * 产品状态调整为清盘中: clearing
	 * 其他事件: 调用dubbo接口 LEOrderService.productClear()
	 * 
	 * @param productOid
	 * @param operator
	 * @param remark
	 *            备注
	 * @return
	 * @throws Exception
	 */
	public BaseResp productClearing(String productOid, String operator, String remark) throws Exception {
		BaseResp resp = new BaseResp();
		if (!StringUtil.isEmpty(productOid)) {
			Product p = productService.getProductByOid(productOid);
			p.setState(Product.STATE_Clearing);
			p.setIsOpenPurchase(Product.NO);
			p.setIsOpenRemeed(Product.NO);
			p.setUpdateTime(DateUtil.getSqlCurrentDate());
			p.setClearingTime(DateUtil.getSqlCurrentDate());
			p.setClearingOperator(operator);
			this.productDao.saveAndFlush(p);
//			/**
//			 * 同步redis
//			 * suzhicheng
//			 */
//			this.cacheProductService.productClearing(p);
			
			/**
			 * 定期产品存续期结束之后，增加待结算产品数量
			 * 活期产品发起清盘操作
			 * @author yuechao
			 */
			this.publisherStatisticsService.increaseToCloseProductAmount(p.getPublisherBaseAccount());
			this.platformStatisticsService.increaseToCloseProductAmount();
		}
		return resp;
	}

//	/**
//	 * 清盘完成接口
//	 * 判断产品 currentVolume 是否为0, 如果为0, 则认为产品清盘完成, 修改产品装填为 cleared
//	 * 其他事件: 调用dubbo接口 updateProductAmount() 不调了
//	 * 
//	 * @param productOid
//	 */
//	public void productCleared(String productOid) throws Exception {
//		if (!StringUtil.isEmpty(productOid)) {
//			Product p = productService.getProductByOid(productOid);
//			if (p.getCurrentVolume().compareTo(BigDecimal.ZERO) == 0) {// 判断产品 currentVolume 是否为0, 如果为0, 则认为产品清盘完成, 修改产品装填为 cleared
//				p.setState(Product.STATE_Cleared);
//				p.setUpdateTime(DateUtil.getSqlCurrentDate());
//				p.setClearedTime(DateUtil.getSqlCurrentDate());
////				this.productDao.saveAndFlush(p);
////				/**
////				 * 同步redis
////				 * suzhicheng
////				 */
//				this.cacheProductService.productCleared(p);
//			}
//		}
//	}

	public ProductDurationResp getProductDuration(String oid) {
		ProductDurationResp pdr = new ProductDurationResp();

		Product p = null;
		if (StringUtil.isEmpty(oid)) {
			Specification<Product> spec = new Specification<Product>() {
				@Override
				public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
					return cb.and(cb.equal(root.get("isDeleted").as(String.class), Product.NO), cb.equal(root.get("auditState").as(String.class), Product.AUDIT_STATE_Reviewed));
				}
			};
			spec = Specifications.where(spec);
			List<Product> ps = productDao.findAll(spec, new Sort(new Order(Direction.DESC, "updateTime")));
			if (ps != null && ps.size() > 0) {
				p = ps.get(0);
			}
		} else {
			p = this.productService.getProductByOid(oid);
		}

		if (p != null) {
			pdr.setOid(p.getOid());
			pdr.setCurrentVolume(p.getCurrentVolume()); // 持有人总份额
			// realNetting 产品实时轧差结果(通过王国接口取)
			OffsetConstantRep offsets = this.productOffsetService.findByProductOid(p.getOid());
			
			pdr.setRealNetting(offsets.getNetPosition());

			if (p.getAssetPool() != null) {
				// SPV预付费金 1401 SPV应收费金 2301
				Map<String, AccountBook> accountBookMap = accountBookService.find(p.getAssetPool().getOid(), "1401", "2301");
				if (accountBookMap != null && accountBookMap.size() > 0) {
					// 资产池
					AccountBook prepaidFee = accountBookMap.get("1401");// SPV预付费金 1401
					if (prepaidFee != null) {
						pdr.setPrepaidFee(prepaidFee.getBalance());
					}
					AccountBook payFee = accountBookMap.get("2301");// SPV应收费金 2301
					if (payFee != null) {
						pdr.setPayFee(payFee.getBalance());
					}
				}
				pdr.setShares(p.getAssetPool().getShares());// SPV基子份额
				pdr.setMarketValue(p.getAssetPool().getMarketValue());// SPV基子市值
				pdr.setDrawedChargefee(p.getAssetPool().getDrawedChargefee());// SPV累计已提取费金
				pdr.setCountintChargefee(p.getAssetPool().getCountintChargefee());// SPV累计已计提费金
			}
			if (null != p.getAssetPool()) {
				AssetPoolEntity ap = p.getAssetPool();
				BigDecimal hqla = BigDecimal.ZERO;
				if (null != ap.getCashPosition()) {
					hqla = hqla.add(ap.getCashPosition());
				}
				if (null != ap.getScale() && null != ap.getCashtoolFactRate()) {
					hqla = hqla.add(ap.getScale().multiply(ap.getCashtoolFactRate()).setScale(2, RoundingMode.HALF_UP));
				}
				hqla = hqla.setScale(2, RoundingMode.HALF_UP);
				pdr.setHqla(hqla);
			}
		}

		return pdr;
	}

	public BaseResp currentTradingRuleSet(CurrentTradingRuleSetForm form, String operator) throws ParseException, Exception {
		BaseResp response = new BaseResp();
		Product product = this.productService.getProductByOid(form.getOid());
		Timestamp now = new Timestamp(System.currentTimeMillis());

		if(Product.TYPE_Producttype_02.equals(product.getType().getOid())) {
			if (!StringUtil.isEmpty(form.getPurchaseConfirmDate())) {
				product.setPurchaseConfirmDays(Integer.valueOf(form.getPurchaseConfirmDate()));
			} else {
				product.setPurchaseConfirmDays(null);
			}
			product.setPurchaseConfirmDaysType(form.getPurchaseConfirmDateType());

			if (!StringUtil.isEmpty(form.getInterestsDate())) {
				product.setInterestsFirstDays(Integer.valueOf(form.getInterestsDate()));
			} else {
				product.setInterestsFirstDays(null);
			}

			if (!StringUtil.isEmpty(form.getRedeemConfirmDate())) {
				product.setRedeemConfirmDays(Integer.valueOf(form.getRedeemConfirmDate()));
			} else {
				product.setRedeemConfirmDays(null);
			}
			product.setRedeemConfirmDaysType(form.getRedeemConfirmDateType());

			if (!StringUtil.isEmpty(form.getNetUnitShare())) {
				product.setNetUnitShare(new BigDecimal(form.getNetUnitShare()));
			} else {
				product.setNetUnitShare(null);
			}

			if (!StringUtil.isEmpty(form.getNetMaxRredeemDay())) {
				product.setNetMaxRredeemDay(new BigDecimal(form.getNetMaxRredeemDay()));
			} else {
				product.setNetMaxRredeemDay(null);
			}

			if (!StringUtil.isEmpty(form.getMaxHold())) {
				product.setMaxHold(new BigDecimal(form.getMaxHold()));
			} else {
				product.setMaxHold(null);
			}
			if (!StringUtil.isEmpty(form.getSingleDailyMaxRedeem())) {
				product.setSingleDailyMaxRedeem(new BigDecimal(form.getSingleDailyMaxRedeem()));
			} else {
				product.setSingleDailyMaxRedeem(null);
			}
			if (!StringUtil.isEmpty(form.getMinRredeem())) {
				product.setMinRredeem(new BigDecimal(form.getMinRredeem()));
			} else {
				product.setMinRredeem(null);
			}

			if (!StringUtil.isEmpty(form.getAdditionalRredeem())) {
				product.setAdditionalRredeem(new BigDecimal(form.getAdditionalRredeem()));
			} else {
				product.setAdditionalRredeem(null);
			}

			if (!StringUtil.isEmpty(form.getMaxRredeem())) {
				product.setMaxRredeem(new BigDecimal(form.getMaxRredeem()));
			} else {
				product.setMaxRredeem(null);
			}
			product.setRredeemDateType(form.getRredeemDateType());
		}
		
		if (!StringUtil.isEmpty(form.getInvestMin())) {
			product.setInvestMin(new BigDecimal(form.getInvestMin()));
		} else {
			product.setInvestMin(null);
		}
		if (!StringUtil.isEmpty(form.getInvestAdditional())) {
			product.setInvestAdditional(new BigDecimal(form.getInvestAdditional()));
		} else {
			product.setInvestAdditional(null);
		}

		if (!StringUtil.isEmpty(form.getInvestMax())) {
			product.setInvestMax(new BigDecimal(form.getInvestMax()));
		} else {
			product.setInvestMax(null);
		}
		if (!StringUtil.isEmpty(form.getDealStartTime())) {
			product.setDealStartTime(form.getDealStartTime());
		}else {
			product.setDealStartTime(null);
		}
		if (!StringUtil.isEmpty(form.getDealEndTime())) {
			product.setDealEndTime(form.getDealEndTime());
		}else {
			product.setDealEndTime(null);
		}
		product.setInvestDateType(form.getInvestDateType());
		product.setSingleDayRedeemCount(form.getSingleDayRedeemCount()); // 单人单日赎回次数
		
		product.setOperator(operator);
		product.setUpdateTime(now);

		product = this.productDao.saveAndFlush(product);
//		/**
//		 * 同步信息到redis
//		 * @author yuechao
//		 */
//		this.cacheProductService.syncProductTradingRule(product);
		return response;
	}

	/*
	 * 单人单日赎回限额设置
	 */
	public BaseResp updateSingleDailyMaxRedeem(final String oid, BigDecimal singleDailyMaxRedeem, String operator) throws ParseException, Exception {
		BaseResp response = new BaseResp();

		Product p = productService.getProductByOid(oid);

		p.setSingleDailyMaxRedeem(singleDailyMaxRedeem);
		p.setUpdateTime(new Timestamp(System.currentTimeMillis()));
		productDao.saveAndFlush(p);
//		
//		/**
//		 * 同步信息到redis
//		 * @author yuechao
//		 */
//		this.cacheProductService.syncProductSingleDailyMaxRedeem(p);
		return response;
	}

	/*
	 * 激活赎回确认
	 */
	public BaseResp openRedeemConfirm(final String oid, String operator) throws ParseException {
		BaseResp response = new BaseResp();
		Product p = productService.getProductByOid(oid);
		if (Product.NO.equals(p.getIsOpenRedeemConfirm())) {
			p.setIsOpenRedeemConfirm(Product.YES);
			p.setUpdateTime(new Timestamp(System.currentTimeMillis()));
			productDao.saveAndFlush(p);
//			
//			/**
//			 * 同步信息到redis
//			 * @author yuechao
//			 */
//			cacheProductService.syncProductIsOpenRedeemConfirm(p);
			
			//investorTradeOrderService.isOpenRedeemConfirm(p);
		}
		return response;
	}

	/*
	 * 屏蔽赎回确认
	 */
	public BaseResp closeRedeemConfirm(final String oid, String operator) throws ParseException {
		BaseResp response = new BaseResp();
		Product p = productService.getProductByOid(oid);
		if (Product.YES.equals(p.getIsOpenRedeemConfirm())) {
			p.setIsOpenRedeemConfirm(Product.NO);
			p.setUpdateTime(new Timestamp(System.currentTimeMillis()));
			productDao.saveAndFlush(p);
			
//			/**
//			 * 同步信息到redis
//			 * @author yuechao
//			 */
//			cacheProductService.syncProductIsOpenRedeemConfirm(p);
//			
			//investorTradeOrderService.isOpenRedeemConfirm(p);
		}
		return response;
	}
	
	/*
	 * 快速赎回设置
	 */
	public BaseResp updateFastRedeem(final String oid, String fastRedeemStatus, BigDecimal fastRedeemMax, String operator) throws ParseException, Exception {
		BaseResp response = new BaseResp();

		Product p = productService.getProductByOid(oid);
		
		if(Product.YES.equals(p.getFastRedeemStatus())) {
			if(Product.YES.equals(fastRedeemStatus)) {
				productDao.updateFastRedeemMax(oid, fastRedeemMax, fastRedeemStatus, operator);
			} else {
				p.setFastRedeemStatus(fastRedeemStatus);
				p.setOperator(operator);
				p.setUpdateTime(new Timestamp(System.currentTimeMillis()));
				productDao.saveAndFlush(p);
			}
		} else if(Product.YES.equals(fastRedeemStatus)) {
			p.setFastRedeemStatus(fastRedeemStatus);
			p.setFastRedeemMax(fastRedeemMax);
			p.setFastRedeemLeft(fastRedeemMax);
			p.setOperator(operator);
			p.setUpdateTime(new Timestamp(System.currentTimeMillis()));
			productDao.saveAndFlush(p);
		}
		return response;
	}
	
	/*
	 * 扩展标签设置
	 */
	public BaseResp updateProductExtendLabel(final String oid, String[] expandProductLabels, String operator) throws ParseException, Exception {
		BaseResp response = new BaseResp();
		
		Product product = productService.getProductByOid(oid);
		
		List<String> labelOids = new ArrayList<String>();

		List<ProductLabel> productLabels = productLabelService.findProductLabelsByProduct(product);
		if(productLabels!=null && productLabels.size()>0) {
			for(ProductLabel pl : productLabels) {
				if(LabelEntity.labelType_general.equals(pl.getLabel().getLabelType())) {//基础标签
					labelOids.add(pl.getLabel().getOid());
				}
			}
		}
		if(expandProductLabels!=null && expandProductLabels.length>0) {//扩展标签
			for(String ex : expandProductLabels) {
				labelOids.add(ex);
			}
		}
		this.productLabelService.saveAndFlush(product, labelOids);

		return response;
	}
	
	
	/**
	 * 募集失败
	 */
	public BaseRep productRaiseFail(String productOid, String operator) {

		Product product = productService.getProductByOid(productOid);
		if (!Product.STATE_Raiseend.equals(product.getState())) {
			// error.define[30076]=非募集期结束,不可募集失败(CODE:30076)
			throw new AMPException(30076);
		}
		boolean isConfirm = this.investorTradeOrderService.isConfirm(productOid);
		if (!isConfirm) {
			// error.define[30075]=请先确认份额(CODE:30075)
			throw new AMPException(30075);
		}

		boolean isOk = investorCashTradeOrderService.isEstablish(productOid, false);
		if (isOk) {
			//如果产品是新手标，则将购买过本产品的所有的用户的新手状态变为新手 freshman
			ProductCacheEntity cache = this.cacheProductService.getProductCacheEntityById(productOid);
			if (Product.TYPE_Producttype_01.equals(cache.getType()) && labelService.isProductLabelHasAppointLabel(cache.getProductLabel(), LabelEnum.newbie.toString())) {
				int i = this.investorBaseAccountDao.updateFreshmanBatch(productOid);
				if (i < 1) {
					// error.define[15003]=回退用户账号为新手失败(CODE:15003)
					throw new AMPException(15003);
				}
			}
			
			product.setUpdateTime(DateUtil.getSqlCurrentDate());
			product.setOperator(operator);
			product.setState(Product.STATE_RaiseFail);
			product.setRaiseFailDate(DateUtil.getSqlDate());

			productDao.save(product);

//			this.cacheProductService.updateProductState(product);
		}
		
		return new BaseRep();
	}
	
	/*
	 * 募集成功
	 */
	public BaseResp productRaiseSuccess(final String oid, String operator) throws ParseException, Exception {
		BaseResp response = new BaseResp();

		Product p = productService.getProductByOid(oid);
		if(Product.STATE_Raiseend.equals(p.getState())) {
			if (p.getCollectedVolume().compareTo(BigDecimal.ZERO) > 0) {
				investorCashTradeOrderService.isEstablish(oid, true);

				Timestamp now = new Timestamp(System.currentTimeMillis());
				p.setUpdateTime(now);
				p.setOperator(operator);
				p.setState(Product.STATE_Durationing);

				java.util.Date setupDate = new java.util.Date(now.getTime());
				p.setSetupDate(new Date(setupDate.getTime()));// 产品成立时间（存续期开始时间）

				java.util.Date durationPeriodEndDate = DateUtil.addDay(setupDate, p.getDurationPeriodDays() - 1);
				p.setDurationPeriodEndDate(new Date(durationPeriodEndDate.getTime()));// 存续期结束时间
				// 到期最晚还本付息日 指存续期结束后的还本付息最迟发生在存续期后的第X个自然日的23:59:59为止
				java.util.Date repayDate = DateUtil.addDay(durationPeriodEndDate, p.getAccrualRepayDays());
				// 到期最晚还本付息日 指存续期结束后的还本付息最迟发生在存续期后的第X个自然日的23:59:59为止
				p.setRepayDate(new Date(repayDate.getTime()));// 到期还款时间

				productDao.saveAndFlush(p);
				/**
				 * 同步redis
				 * suzhicheng
				 */
//				this.cacheProductService.updateProductState(p);
				sendMessage(p, DealMessageEnum.PRODUCT_BEGIN.name());
				// 定期募集成立时创建生成二级邀请奖励收益明细的序列化任务
				this.tnProfitDetailSerial(p);
			} else {
				response.setErrorCode(-1);
				response.setErrorMessage("产品募集总份额为0，不能成立");
			}
		} else {
			response.setErrorCode(-1);
			response.setErrorMessage("产品非募集结束状态，不可操作募集成功");
		}
		return response;
	}
	/**
	 * 
	 * @author yihonglei
	 * @Title: distributeProductRaiseSuccessRewardIncome
	 * @Description:定期产品募集成立时，序列化任务生成奖励收益明细
	 * @param product
	 * @return void
	 * @date 2017年6月13日 下午4:22:01
	 * @since  1.0.0
	 */
	public void tnProfitInitDo(String productOid) {
		if (!"".equals(productOid)) {
			log.info("<---定期产品募集成立时，二级邀请生成奖励收益明细开始!产品id:{}--->", productOid);
			// 生成定期奖励收益明细
			int result = this.profitDetailService.initTnProductProfitDetail(productOid);
			if (result > 0) {
				// 根据奖励收益明细和奖励规则生成奖励发放明细
				result = this.profitProvideDetailService.initProductProfitProvideDetail(productOid);
			}
			log.info("<---定期产品募集成立时，二级邀请生成奖励收益明细结束!结果为:{}", result>0);
		} else {
			log.info("二级邀请奖励收益生成明细无法生成，产品不存在，产品ID:{}",productOid);
		}
	}
	
	private void sendMessage(Product p, String tag) {
		List<InvestorTradeOrderEntity> tradeOrderList = investorTradeOrderService
				.findConfirmedOrderByProductOid(p.getOid());
		for (InvestorTradeOrderEntity orderEntity : tradeOrderList) {
			DealMessageEntity messageEntity = new DealMessageEntity();
			messageEntity.setPhone(orderEntity.getInvestorBaseAccount().getPhoneNum());
			messageEntity.setOrderAmount(orderEntity.getOrderAmount().setScale(2, BigDecimal.ROUND_DOWN).toString());
			messageEntity.setOrderTime(orderEntity.getOrderTime());
			messageEntity.setProductName(orderEntity.getProduct().getName());
			messageEntity.setSettlementDate(p.getRepayDate());
			messageSendUtil.sendTimedTopicMessage(messageSendUtil.getDealTopic(), tag, messageEntity);
		}
	}
	
	/*
	 * 是否自动派息设置
	 */
	public BaseResp isAutoAssignIncomeSet(final String oid, String isAutoAssignIncome, String operator) throws ParseException, Exception {
		BaseResp response = new BaseResp();
		Product p = productService.getProductByOid(oid);
		if(!isAutoAssignIncome.equals(p.getIsAutoAssignIncome())) {
			p.setUpdateTime(new Timestamp(System.currentTimeMillis()));
			p.setOperator(operator);
			p.setIsAutoAssignIncome(isAutoAssignIncome);
			productDao.saveAndFlush(p);
		}
		return response;
	}
	
	/**
	 * 是否满标判断
	 */
	public BaseResp productIsFull(String oid){
		BaseResp response = new BaseResp();
		Product product = productService.getProductByOid(oid);
		//判断是否满标
		if(product.getCurrentVolume().compareTo(product.getRaisedTotalNumber()) < 0){
			//未满标
			response.setErrorCode(1);
			response.setErrorMessage("未满标");
		}else{
			//满标
			response.setErrorCode(0);
			response.setErrorMessage("满标");
		}
		return response;
	}
	
	/**
	 * 定期募集成立时创建生成二级邀请奖励收益明细的序列化任务
	 */
	public void tnProfitDetailSerial(Product product) {
		if (this.profitRuleService.checkProfitRule()) {
			TnProfitDetailParams params = new TnProfitDetailParams();
			params.setProductOid(product.getOid());
			SerialTaskReq<TnProfitDetailParams> sreq = new SerialTaskReq<TnProfitDetailParams>();
			sreq.setTaskParams(params);
			sreq.setTaskCode(SerialTaskEntity.TASK_taskCode_tnProfitInit);
			serialTaskService.createSerialTask(sreq);
		} else {
			log.info("二级邀请活动已经下架");
		}
	}
	
}
