package com.guohuai.ams.duration.fact.income;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;

import com.guohuai.ams.acct.books.AccountBook;
import com.guohuai.ams.acct.books.AccountBookService;
import com.guohuai.ams.acct.books.document.SPVDocumentService;
import com.guohuai.ams.duration.assetPool.AssetPoolEntity;
import com.guohuai.ams.duration.assetPool.AssetPoolService;
import com.guohuai.ams.duration.assetPool.chargefee.AssetPoolFeeSettingService;
import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductDao;
import com.guohuai.ams.product.ProductDecimalFormat;
import com.guohuai.ams.product.ProductService;
import com.guohuai.ams.product.reward.ProductIncomeRewardCacheService;
import com.guohuai.cache.service.CacheProductService;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.DecimalUtil;
import com.guohuai.component.util.StringUtil;
import com.guohuai.component.web.view.BaseResp;
import com.guohuai.component.web.view.PageResp;
import com.guohuai.mmp.publisher.investor.InterestRateMethodService;
import com.guohuai.mmp.publisher.investor.interest.result.InterestResultEntity;
import com.guohuai.mmp.publisher.product.rewardincomepractice.PracticeService;
import com.guohuai.mmp.publisher.product.rewardincomepractice.RewardIsNullRep;
import com.guohuai.operate.api.AdminSdk;
import com.guohuai.operate.api.objs.admin.AdminObj;

/**
 * 收益分配
 * 
 * @author wangyan
 *
 */
@Service
public class IncomeDistributionService {
	private static final Logger logger = LoggerFactory.getLogger(IncomeDistributionService.class);
	@Autowired
	private IncomeEventDao incomeEventDao;
	@Autowired
	private IncomeAllocateDao incomeAllocateDao;
	@Autowired
	private AccountBookService accountBookService;
	@Autowired
	private SPVDocumentService spvDocumentService;
	@Autowired
	private ProductDao productDao;
	@Autowired
	private ProductService productService;
	@Autowired
	private AssetPoolService poolService;
	@Autowired
	private AdminSdk adminSdk;
	@Autowired
	private PracticeService practiceService;
	@Autowired
	private InterestRateMethodService interestRateMethodService;
//	@Autowired
//	private SerFeeService serFeeService;
	@Autowired
	private AssetPoolFeeSettingService assetPoolFeeSettingService;
	@Autowired
	private CacheProductService cacheProductService;
	@Autowired
	private ProductIncomeRewardCacheService productIncomeRewardCacheService;
	
	public IncomeAllocateCalcResp getIncomeAdjustData(String assetPoolOid) {
		IncomeAllocateCalcResp resp = new IncomeAllocateCalcResp();

		BigDecimal undisIncome = new BigDecimal("0");// 未分配收益
		BigDecimal productTotalScale = new BigDecimal("0");// 产品总规模 王国处获取
		BigDecimal productRewardBenefit = new BigDecimal("0");// 奖励收益 王国处获取
		BigDecimal productDistributionIncome = new BigDecimal("0");// 分配收益
		BigDecimal productAnnualYield = new BigDecimal("0");// 年化收益率

		Product p = null;
		List<Product> ps = productService.getProductListByAssetPoolOid(assetPoolOid);
		if (ps != null && ps.size() > 0) {
			p = ps.get(0);
			
			if(Product.YES.equals(p.getIsAutoAssignIncome())) {
				resp.setErrorCode(-1);
				resp.setErrorMessage("自动派息，不能手动派息!");
				throw AMPException.getException("自动派息，不能手动派息!");
			}
			
			resp.setProductOid(p.getOid());
			resp.setIncomeCalcBasis(p.getIncomeCalcBasis());
		} else {
			resp.setIncomeCalcBasis("365");
		}

		IncomeEvent lastIncomeEvent = this.findLastValidIncomeEvent(assetPoolOid);// 查询该资产池最近一天的收益分配日 非 IncomeEvent.STATUS_Fail 和 非 IncomeEvent.STATUS_Delete

		if (lastIncomeEvent == null) {// 首次分配收益
			resp.setIncomeDate("");// 收益分配日
			resp.setLastIncomeDate("");// 上一收益分配日
		} else {// 非首次分配收益
			if (IncomeEvent.STATUS_Create.equals(lastIncomeEvent.getStatus())) {
				resp.setErrorCode(-1);
				resp.setErrorMessage("请先审核" + DateUtil.formatDate(lastIncomeEvent.getBaseDate().getTime()) + "的收益分配!");
				throw AMPException.getException(60004);
			} else if (IncomeEvent.STATUS_Allocating.equals(lastIncomeEvent.getStatus())) {
				resp.setErrorCode(-1);
				resp.setErrorMessage("请先等待" + DateUtil.formatDate(lastIncomeEvent.getBaseDate().getTime()) + "的收益分配完成!");
				throw AMPException.getException(60005);
			} else if (IncomeEvent.STATUS_AllocateFail.equals(lastIncomeEvent.getStatus())) {
				resp.setErrorCode(-1);
				resp.setErrorMessage("请先完成" + DateUtil.formatDate(lastIncomeEvent.getBaseDate().getTime()) + "的收益分配!");
				throw AMPException.getException(60006);
			} else {
				if (lastIncomeEvent.getBaseDate().getTime() == DateUtil.getBeforeDate().getTime()) {
					resp.setErrorCode(-1);
					resp.setErrorMessage("今日已经申请过昨日收益分配");
					throw AMPException.getException(60010);
				} else {
					// 上一收益分配日
					resp.setLastIncomeDate(DateUtil.format(lastIncomeEvent.getBaseDate()));
					Date incomeDate = DateUtil.addSQLDays(lastIncomeEvent.getBaseDate(), 1);

					Date today = DateUtil.formatUtilToSql(DateUtil.getCurrDate());
					if (incomeDate.getTime() >= today.getTime()) {
						resp.setErrorCode(-1);
						resp.setErrorMessage("今日只能申请昨日以及昨日之前的收益分配");
						throw AMPException.getException(60011);
					}
					resp.setIncomeDate(DateUtil.format(incomeDate));// 收益分配日
					// 计提费用
					BigDecimal feeValue = assetPoolFeeSettingService.feeCalac(assetPoolOid, incomeDate);
					if (feeValue != null) {
						resp.setFeeValue(ProductDecimalFormat.format(feeValue, "0.##"));
						if (feeValue.compareTo(new BigDecimal("0")) > 0) {
							resp.setFeeValueStr(resp.getFeeValue() + "元");
						}
					}
					if (p != null) {
						RewardIsNullRep practice;
						if(productIncomeRewardCacheService.hasRewardIncome(p.getOid())) {
							practice = this.practiceService.rewardIsNotNullRep(p, null);
						} else {
							practice = this.practiceService.rewardIsNullRep(p, null);
						}
						if (practice != null) {
							if (practice.getTotalHoldVolume() != null) {// 持有人总份额
								productTotalScale = practice.getTotalHoldVolume();
							}
							if (practice.getTotalRewardIncome() != null) {// 奖励收益
								productRewardBenefit = practice.getTotalRewardIncome();
							}
						}
					}
				}
			}
		}

		Map<String, AccountBook> accountBookMap = accountBookService.find(assetPoolOid, "1111", "1201", "2201");
		if (accountBookMap != null && accountBookMap.size() > 0) {
			// 资产池
			AccountBook investmentAssets = accountBookMap.get("1111");// 资产池 投资资产
			if (investmentAssets != null) {
				resp.setInvestmentAssets(ProductDecimalFormat.format(investmentAssets.getBalance(), "0.##"));
				if (investmentAssets.getBalance().compareTo(new BigDecimal("0")) > 0) {
					resp.setInvestmentAssetsStr(resp.getInvestmentAssets() + "元");// 投资资产
				}
			}
			AccountBook apUndisIncome = accountBookMap.get("2201");// 资产池 未分配收益
			if (apUndisIncome != null) {
				resp.setApUndisIncome(ProductDecimalFormat.format(apUndisIncome.getBalance(), "0.##"));
				if (apUndisIncome.getBalance().compareTo(new BigDecimal("0")) > 0) {
					resp.setApUndisIncomeStr(resp.getApUndisIncome() + "元");// 资产池未分配收益
				}
			}
			AccountBook apReceiveIncome = accountBookMap.get("1201");// 资产池 应收投资收益
			if (apReceiveIncome != null) {
				resp.setApReceiveIncome(ProductDecimalFormat.format(apReceiveIncome.getBalance(), "0.##"));
				if (apReceiveIncome.getBalance().compareTo(new BigDecimal("0")) > 0) {
					resp.setApReceiveIncomeStr(resp.getApReceiveIncome() + "元");// 应收投资收益
				}
				undisIncome = apReceiveIncome.getBalance().subtract(productRewardBenefit).subtract(productDistributionIncome);
			}
		}

		resp.setProductTotalScale(ProductDecimalFormat.format(productTotalScale, "0.##"));// 产品总规模 王国处获取
		if (productTotalScale.compareTo(new BigDecimal("0")) > 0) {
			resp.setProductTotalScaleStr(productTotalScale + "元");// 产品总规模
		}
		resp.setProductRewardBenefit(ProductDecimalFormat.format(productRewardBenefit, "0.##"));// 奖励收益 王国处获取
		if (productRewardBenefit.compareTo(new BigDecimal("0")) > 0) {
			resp.setProductRewardBenefitStr(productRewardBenefit + "元");// 奖励收益
		}
		// 年化收益率=分配收益/产品总规模*365
		if (productTotalScale.compareTo(new BigDecimal("0")) != 0) {
			productAnnualYield = productDistributionIncome.multiply(new BigDecimal(resp.getIncomeCalcBasis())).divide(productTotalScale, 4, RoundingMode.HALF_UP);// 分配收益/产品总规模*365
		}

		resp.setProductDistributionIncome(ProductDecimalFormat.format(productDistributionIncome, "0.##"));// 分配收益
		resp.setProductAnnualYield(ProductDecimalFormat.format(ProductDecimalFormat.multiply(productAnnualYield), "0.##"));// 年化收益率 单位%
		resp.setAssetpoolOid(assetPoolOid);
		resp.setUndisIncome(ProductDecimalFormat.format(undisIncome, "0.##"));// 未分配收益

		BigDecimal receiveIncome = new BigDecimal("0");// 应收投资收益
		if (undisIncome.compareTo(new BigDecimal("0")) < 0) {
			receiveIncome = undisIncome.negate();
		}
		resp.setUndisIncome(ProductDecimalFormat.format(receiveIncome, "0.##"));// 应收投资收益

		BigDecimal totalScale = productTotalScale.add(productRewardBenefit).add(productDistributionIncome);
		resp.setTotalScale(ProductDecimalFormat.format(totalScale, "0.##"));// 产品总规模
		resp.setAnnualYield(ProductDecimalFormat.format(productAnnualYield, "0.##"));// 产品年化收益率

		// 年化收益率=分配收益/产品总规模*365
		// 万份收益=年化收益率/365*10000
		if (productTotalScale.compareTo(new BigDecimal("0")) != 0) {
			resp.setMillionCopiesIncome(ProductDecimalFormat.format(productDistributionIncome.multiply(new BigDecimal("10000")).divide(productTotalScale, 4, RoundingMode.HALF_UP), "0.####"));// 万份收益
		} else {
			resp.setMillionCopiesIncome(ProductDecimalFormat.format(new BigDecimal("0"), "0.####"));// 万份收益
		}

		return resp;
	}

	/**
	 * 根据资产池和收益分配日获取 产品总规模和奖励收益
	 * 
	 * @param assetPoolOid
	 * @param incomeDate
	 * @return
	 */
	public IncomeAllocateCalcResp getTotalScaleRewardBenefit(String assetPoolOid, String incomeDate) {
		logger.debug("根据资产池和收益分配日获取 产品总规模和奖励收益开始,assetPoolOid:{} incomeDate:{}", assetPoolOid, incomeDate);
		IncomeAllocateCalcResp resp = new IncomeAllocateCalcResp();

		BigDecimal productTotalScale = new BigDecimal("0");// 产品总规模 王国处获取
		BigDecimal productRewardBenefit = new BigDecimal("0");// 奖励收益 王国处获取

		BigDecimal feeValue = new BigDecimal("0");// 计提费用

		List<Product> ps = productService.getProductListByAssetPoolOid(assetPoolOid);
		if (ps != null && ps.size() > 0) {
			Date incomeSqlDate = DateUtil.parseToSqlDate(incomeDate);

//			SerFeeQueryRep fee = serFeeService.findFeeByDate(ps.get(0).getOid(), incomeSqlDate);// 乐视服务费
//			if (fee != null) {
//				resp.setFees(new ArrayList<SerFeeQueryRep>());
//				resp.getFees().add(fee);
//			}

			// 计提费用
			BigDecimal assetPoolFeeValue = assetPoolFeeSettingService.feeCalac(assetPoolOid, incomeSqlDate);
			if (assetPoolFeeValue != null) {
				feeValue = assetPoolFeeValue;
			}
			resp.setFeeValue(ProductDecimalFormat.format(feeValue, "0.##"));
			if (feeValue.compareTo(new BigDecimal("0")) > 0) {
				resp.setFeeValueStr(resp.getFeeValue() + "元");
			}
			Product product = ps.get(0);
			logger.debug("试算产品为{}", product.getOid());
			RewardIsNullRep practice;
			if(productIncomeRewardCacheService.hasRewardIncome(product.getOid())) {
				logger.debug("进入有奖励收益分支");
				practice = this.practiceService.rewardIsNotNullRep(product, incomeSqlDate);
			} else {
				logger.debug("进入无奖励收益分支");
				practice = this.practiceService.rewardIsNullRep(product, incomeSqlDate);
			}
			if (practice != null) {
				if (practice.getTotalHoldVolume() != null) {// 持有人总份额
					productTotalScale = practice.getTotalHoldVolume();
				}
				if (practice.getTotalRewardIncome() != null) {// 奖励收益
					productRewardBenefit = practice.getTotalRewardIncome();
				}
			}

		}

		resp.setProductTotalScale(ProductDecimalFormat.format(productTotalScale, "0.##"));// 产品总规模 王国处获取
		if (productTotalScale.compareTo(new BigDecimal("0")) > 0) {
			resp.setProductTotalScaleStr(productTotalScale + "元");// 产品总规模
		}
		resp.setProductRewardBenefit(ProductDecimalFormat.format(productRewardBenefit, "0.##"));// 奖励收益 王国处获取
		if (productRewardBenefit.compareTo(new BigDecimal("0")) > 0) {
			resp.setProductRewardBenefitStr(productRewardBenefit + "元");// 奖励收益
		}
		logger.debug("根据资产池和收益分配日获取 产品总规模和奖励收益结束,result:{}", resp);
		return resp;
	}

	/**
	 * 查询该资产池最近一天的收益分配日 非 IncomeEvent.STATUS_Fail 和 非 IncomeEvent.STATUS_Delete
	 * 
	 * @param assetPoolOid
	 * @return
	 */
	private IncomeEvent findLastValidIncomeEvent(final String assetPoolOid) {
		Specification<IncomeEvent> spec = new Specification<IncomeEvent>() {
			@Override
			public Predicate toPredicate(Root<IncomeEvent> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.equal(root.get("assetPool").get("oid").as(String.class), assetPoolOid);
			}
		};

		Specification<IncomeEvent> statusSpec = new Specification<IncomeEvent>() {
			@Override
			public Predicate toPredicate(Root<IncomeEvent> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.and(cb.notEqual(root.get("status").as(String.class), IncomeEvent.STATUS_Fail), cb.notEqual(root.get("status").as(String.class), IncomeEvent.STATUS_Delete));
			}
		};
		spec = Specifications.where(spec).and(statusSpec);

		List<IncomeEvent> ims = incomeEventDao.findAll(spec, new Sort(new Order(Direction.DESC, "baseDate")));
		if (ims != null && ims.size() > 0) {
			return ims.get(0);
		}
		return null;

	}

	@Transactional
	public IncomeAllocateBaseResp saveIncomeAdjust(IncomeAllocateForm form, String operator) throws ParseException {
		IncomeAllocateBaseResp response = new IncomeAllocateBaseResp();
		
		AssetPoolEntity assetPool = poolService.getByOid(form.getAssetpoolOid());
		if (assetPool == null) {
			throw AMPException.getException(60000);
		}
		List<Product> ps = productService.getProductListByAssetPoolOid(form.getAssetpoolOid());
		Product product = null;
		if (ps != null && ps.size() > 0) {
			product = ps.get(0);
		}
		
		if(practiceService.isPractice(product, DateUtil.parseToSqlDate(form.getIncomeDistrDate()))) {

		IncomeEvent lastIncomeEvent = this.findLastValidIncomeEvent(form.getAssetpoolOid());// 查询该资产池最近一天的收益分配日 非 IncomeEvent.STATUS_Fail 和 非 IncomeEvent.STATUS_Delete
		if (lastIncomeEvent != null) {// 非首次分配收益
			if (IncomeEvent.STATUS_Create.equals(lastIncomeEvent.getStatus())) {
				response.setErrorCode(-1);
				response.setErrorMessage("请先审核" + DateUtil.formatDate(lastIncomeEvent.getBaseDate().getTime()) + "的收益分配!");
				throw AMPException.getException(60004);
			} else if (IncomeEvent.STATUS_Allocating.equals(lastIncomeEvent.getStatus())) {
				response.setErrorCode(-1);
				response.setErrorMessage("请先等待" + DateUtil.formatDate(lastIncomeEvent.getBaseDate().getTime()) + "的收益分配完成!");
				throw AMPException.getException(60005);
			} else if (IncomeEvent.STATUS_AllocateFail.equals(lastIncomeEvent.getStatus())) {
				response.setErrorCode(-1);
				response.setErrorMessage("请先完成" + DateUtil.formatDate(lastIncomeEvent.getBaseDate().getTime()) + "的收益分配!");
				throw AMPException.getException(60006);
			} else if (lastIncomeEvent.getBaseDate().getTime() == DateUtil.getBeforeDate().getTime()) {
				response.setErrorCode(-1);
				response.setErrorMessage("今日已经申请过昨日收益分配");
				throw AMPException.getException(60010);
			} else if (lastIncomeEvent.getBaseDate().getTime() >= DateUtil.formatUtilToSql(DateUtil.getCurrDate()).getTime()) {
				response.setErrorCode(-1);
				response.setErrorMessage("今日只能申请昨日以及昨日之前的收益分配");
				throw AMPException.getException(60011);
			}
		}

		Timestamp now = new Timestamp(System.currentTimeMillis());

		IncomeEvent incomeEvent = new IncomeEvent();
		incomeEvent.setOid(StringUtil.uuid());
		incomeEvent.setAssetPool(assetPool);
		incomeEvent.setBaseDate(DateUtil.parseToSqlDate(form.getIncomeDistrDate()));
		incomeEvent.setAllocateIncome(new BigDecimal(form.getProductRewardBenefit()).add(new BigDecimal(form.getProductDistributionIncome())));// 总分配收益
		incomeEvent.setCreator(operator);
		incomeEvent.setCreateTime(now);
		incomeEvent.setDays(1);
		incomeEvent.setStatus(IncomeEvent.STATUS_Create);
		incomeEventDao.save(incomeEvent);

		IncomeAllocate incomeAllocate = new IncomeAllocate();
		incomeAllocate.setOid(StringUtil.uuid());
		
		if(product!=null && Product.TYPE_Producttype_02.equals(product.getType().getOid())) {//活期产品
			incomeAllocate.setAllocateIncomeType(IncomeAllocate.ALLOCATE_INCOME_TYPE_durationIncome);
		} else if(product!=null && Product.TYPE_Producttype_01.equals(product.getType().getOid())) {
			incomeAllocate.setAllocateIncomeType(IncomeAllocate.ALLOCATE_INCOME_TYPE_raiseIncome);
		}
		
		incomeAllocate.setIncomeEvent(incomeEvent);
		incomeAllocate.setProduct(product);
		incomeAllocate.setBaseDate(incomeEvent.getBaseDate());
		incomeAllocate.setCapital(new BigDecimal(form.getProductTotalScale()));// 产品总规模
		incomeAllocate.setAllocateIncome(new BigDecimal(form.getProductDistributionIncome()));// 分配基础收益
		incomeAllocate.setRewardIncome(new BigDecimal(form.getProductRewardBenefit()));// 分配奖励收益
		incomeAllocate.setRatio(ProductDecimalFormat.divide(new BigDecimal(form.getProductAnnualYield())));// 收益率(年化) form.getProductAnnualYield()单位为%

		/**
		 * 万份收益=年化收益率/365*10000
		 */
		BigDecimal productAnnualYield = ProductDecimalFormat.divide(new BigDecimal(form.getProductAnnualYield())); // 产品范畴 年化收益率
//		BigDecimal incomeCalcBasis = new BigDecimal(product.getIncomeCalcBasis());// 计算基础
//		BigDecimal millionCopiesIncome = productAnnualYield.multiply(new BigDecimal("10000"))
//				.divide(incomeCalcBasis, 4, RoundingMode.HALF_UP);// 万份收益

		incomeAllocate.setWincome(getWinIncome(productAnnualYield, product));

		incomeAllocate.setDays(1);// 收益分配天数
		incomeAllocate.setSuccessAllocateIncome(new BigDecimal("0"));// 成功分配基础收益金额
		incomeAllocate.setSuccessAllocateRewardIncome(new BigDecimal("0"));// 成功分配奖励收益金额
		incomeAllocate.setLeftAllocateIncome(new BigDecimal(form.getProductRewardBenefit()).add(new BigDecimal(form.getProductDistributionIncome())));// 剩余总分配收益金额
		incomeAllocate.setLeftAllocateBaseIncome(new BigDecimal(form.getProductDistributionIncome()));//剩余分配基础金额
		incomeAllocate.setLeftAllocateRewardIncome(new BigDecimal(form.getProductRewardBenefit()));//剩余分配奖励金额
		incomeAllocate.setSuccessAllocateInvestors(0);// 成功分配投资者数
		incomeAllocate.setFailAllocateInvestors(0);// 失败分配投资者数
		incomeAllocate = incomeAllocateDao.save(incomeAllocate);
		response.setOid(incomeAllocate.getOid());
		} else {
			response.setErrorCode(-1);
			response.setErrorMessage("请先进行该资产池对应的产品收益试算，才能进行收益分配");
		}

		return response;
	}
	
	public BigDecimal getWinIncome(BigDecimal annualInterest, Product product) {
		return DecimalUtil.setScaleDown(new BigDecimal("10000")
				.multiply(new BigDecimal(Math.pow(1 + annualInterest.doubleValue(),
						1 / Double.parseDouble(product.getIncomeCalcBasis()))).subtract(BigDecimal.ONE).setScale(7,
								BigDecimal.ROUND_HALF_UP)));
	}

	public IncomeDistributionResp getIncomeAdjust(String oid) {
		IncomeAllocate incomeAllocate = incomeAllocateDao.findOne(oid);
		IncomeDistributionResp idr = new IncomeDistributionResp(incomeAllocate);

		Map<String, AdminObj> adminObjMap = new HashMap<String, AdminObj>();
		AdminObj adminObj = null;
		if (!StringUtil.isEmpty(idr.getCreator())) {
			if (adminObjMap.get(idr.getCreator()) == null) {
				try {
					adminObj = adminSdk.getAdmin(idr.getCreator());
					adminObjMap.put(idr.getCreator(), adminObj);
				} catch (Exception e) {
				}
			}
			if (adminObjMap.get(idr.getCreator()) != null) {
				idr.setCreator(adminObjMap.get(idr.getCreator()).getName());
			}
		}
		if (!StringUtil.isEmpty(idr.getAuditor())) {
			if (adminObjMap.get(idr.getAuditor()) == null) {
				try {
					adminObj = adminSdk.getAdmin(idr.getAuditor());
					adminObjMap.put(idr.getAuditor(), adminObj);
				} catch (Exception e) {
				}

			}
			if (adminObjMap.get(idr.getAuditor()) != null) {
				idr.setAuditor(adminObjMap.get(idr.getAuditor()).getName());
			}
		}

		return idr;
	}

	/**
	 * 资产池 收益分配列表
	 * 
	 * @param spec
	 * @param pageable
	 * @return
	 */
	public PageResp<IncomeDistributionResp> getIncomeAdjustList(Specification<IncomeAllocate> spec, Pageable pageable) {
		Page<IncomeAllocate> cas = this.incomeAllocateDao.findAll(spec, pageable);

		PageResp<IncomeDistributionResp> pagesRep = new PageResp<IncomeDistributionResp>();

		if (cas != null && cas.getContent() != null && cas.getTotalElements() > 0) {
			List<IncomeDistributionResp> rows = new ArrayList<IncomeDistributionResp>();

			Map<String, AdminObj> adminObjMap = new HashMap<String, AdminObj>();
			AdminObj adminObj = null;

			for (IncomeAllocate ia : cas) {
				IncomeDistributionResp idr = new IncomeDistributionResp(ia);

				if (!StringUtil.isEmpty(idr.getCreator())) {
					if (adminObjMap.get(idr.getCreator()) == null) {
						try {
							adminObj = adminSdk.getAdmin(idr.getCreator());
							adminObjMap.put(idr.getCreator(), adminObj);
						} catch (Exception e) {
						}
					}
					if (adminObjMap.get(idr.getCreator()) != null) {
						idr.setCreator(adminObjMap.get(idr.getCreator()).getName());
					}
				}
				if (!StringUtil.isEmpty(idr.getAuditor())) {
					if (adminObjMap.get(idr.getAuditor()) == null) {
						try {
							adminObj = adminSdk.getAdmin(idr.getAuditor());
							adminObjMap.put(idr.getAuditor(), adminObj);
						} catch (Exception e) {
						}

					}
					if (adminObjMap.get(idr.getAuditor()) != null) {
						idr.setAuditor(adminObjMap.get(idr.getAuditor()).getName());
					}
				}

				rows.add(idr);
			}
			pagesRep.setRows(rows);
		}
		pagesRep.setTotal(cas.getTotalElements());

		return pagesRep;
	}

	@Transactional
	public BaseResp auditPassIncomeAdjust(String oid, String operator) {
		BaseResp response = new BaseResp();

		IncomeAllocate incomeAllocate = incomeAllocateDao.findOne(oid);
		IncomeEvent ie = this.incomeEventDao.findOne(incomeAllocate.getIncomeEvent().getOid());

		if (ie != null && !IncomeEvent.STATUS_Create.equals(ie.getStatus())) {
			response.setErrorCode(-1);
			response.setErrorMessage("待审核状态才能审核!");
			throw AMPException.getException(60007);
		}

		Timestamp now = new Timestamp(System.currentTimeMillis());

		ie.setAuditor(operator);
		ie.setAuditTime(now);
		ie.setStatus(IncomeEvent.STATUS_Allocating);// 发放中
		incomeEventDao.saveAndFlush(ie);
		
		this.interestRateMethodService.interest(incomeAllocate.getOid(), incomeAllocate.getProduct().getOid());
		
		return response;
	}

	/**
	 * 发放收益 乐超收益分配完成后调用
	 * 
	 * @param allocateIncomeReturn
	 */
	@Transactional
	public void allocateIncome(InterestResultEntity allocateIncomeReturn) throws Exception {
		Product p = productDao.findOne(allocateIncomeReturn.getProduct().getOid());
		logger.info("Product is {}", p);
		IncomeAllocate im = incomeAllocateDao.findOne(allocateIncomeReturn.getIncomeAllocate().getOid());
		logger.info("IncomeAllocate is {}", im);
		IncomeEvent ie = incomeEventDao.findOne(im.getIncomeEvent().getOid());
		logger.info("IncomeEvent is {}", ie);
		/**
		 * 成功分配基础收益金额
		 */
		BigDecimal successAllocateBaseIncome = BigDecimal.ZERO;
		if (allocateIncomeReturn.getSuccessAllocateBaseIncome() != null) {
			successAllocateBaseIncome = allocateIncomeReturn.getSuccessAllocateBaseIncome();
			logger.info("successAllocateBaseIncome is {}", successAllocateBaseIncome);
		}

		/**
		 * 成功分配奖励收益金额
		 */
		BigDecimal successAllocateRewardIncome = BigDecimal.ZERO;
		if (allocateIncomeReturn.getSuccessAllocateRewardIncome() != null) {
			successAllocateRewardIncome = allocateIncomeReturn.getSuccessAllocateRewardIncome();
			logger.info("successAllocateRewardIncome is {}", successAllocateRewardIncome);
		}

		/**
		 * 成功分配投资者数
		 */
		Integer successAllocateInvestors = 0;
		if (allocateIncomeReturn.getSuccessAllocateInvestors() != null) {
			successAllocateInvestors = allocateIncomeReturn.getSuccessAllocateInvestors();
			logger.info("successAllocateInvestors is {}", successAllocateInvestors);
		}

		im.setSuccessAllocateIncome(im.getSuccessAllocateIncome().add(successAllocateBaseIncome));// 成功分配基础收益金额
		im.setSuccessAllocateRewardIncome(im.getSuccessAllocateRewardIncome().add(successAllocateRewardIncome));// 成功分配奖励收益金额
		im.setLeftAllocateIncome(allocateIncomeReturn.getLeftAllocateIncome());// 未分配总金额
		
		im.setLeftAllocateBaseIncome(allocateIncomeReturn.getLeftAllocateIncome());// 剩余分配基础金额
		im.setLeftAllocateRewardIncome(allocateIncomeReturn.getLeftAllocateIncome());//剩余分配奖励金额
		
		im.setSuccessAllocateInvestors(im.getSuccessAllocateInvestors() + successAllocateInvestors);// 成功分配投资者数
		im.setFailAllocateInvestors(allocateIncomeReturn.getFailAllocateInvestors());// 失败分配投资者数
		logger.info("begin incomeAllocateDao.saveAndFlush(im)");
		incomeAllocateDao.saveAndFlush(im);
		logger.info("incomeAllocateDao.saveAndFlush(im) end");
		ie.setStatus(allocateIncomeReturn.getStatus());// 分配状态
		logger.info("begin incomeEventDao.saveAndFlush(ie)");
		incomeEventDao.saveAndFlush(ie);
		logger.info("incomeEventDao.saveAndFlush(ie) end");
		/**
		 * 成功分配基础收益金额+成功分配奖励收益金额
		 */
		BigDecimal successAllIncome = successAllocateBaseIncome.add(successAllocateRewardIncome);
		logger.info("successAllIncome={}", successAllIncome);
		if (successAllIncome.compareTo(new BigDecimal("0")) != 0) {// 成功分配收益金额
			// 资产池收益分配成功发送更新产品currentVolume
			logger.info("successAllIncome={}, productOid={}", successAllIncome, p.getOid());
//			int i = this.productDao.incomeAllocateAdjustCurrentVolume(p.getOid(), successAllIncome, im.getRatio());
			/**
			 * 用holdIncome更新currentVolume,避免cash校验不过
			 */
			int i = this.productDao.incomeAllocateAdjustCurrentVolume(p.getOid(), allocateIncomeReturn.getSuccessAllocateIncome(), im.getRatio());
			/**
			 * 同步redis
			 * suzhicheng
			 */
//			this.cacheProductService.incomeAllocateAdjustCurrentVolume(p.getOid(), successAllIncome, im.getRatio());
			if (i < 1) {
				throw new AMPException("资产池收益分配成功发送更新产品currentVolume失败");
			}
			// 会计分录接口1
			logger.info("开始操作会计分录");
			spvDocumentService.incomeAllocate(ie.getAssetPool().getOid(), ie.getOid(), successAllIncome);
			logger.info("成功操作会计分录");
		}
		if (im.getAllocateIncomeType().equals(IncomeAllocate.ALLOCATE_INCOME_TYPE_durationIncome) && Product.TYPE_Producttype_01.equals(p.getType().getOid())) {
			this.productService.repayInterestOk(p.getOid());
			logger.error("{}产品正在派息状态更新成功", p.getOid());
		}

	}



	@Transactional
	public BaseResp auditFailIncomeAdjust(String oid, String operator) {
		BaseResp response = new BaseResp();

		IncomeAllocate incomeAllocate = incomeAllocateDao.findOne(oid);
		IncomeEvent ie = this.incomeEventDao.findOne(incomeAllocate.getIncomeEvent().getOid());

		if (ie != null && !IncomeEvent.STATUS_Create.equals(ie.getStatus())) {
			response.setErrorCode(-1);
			response.setErrorMessage("待审核状态才能审核!");
			throw AMPException.getException(60007);
		}

		Timestamp now = new Timestamp(System.currentTimeMillis());

		ie.setAuditor(operator);
		ie.setAuditTime(now);
		ie.setStatus(IncomeEvent.STATUS_Fail);
		incomeEventDao.saveAndFlush(ie);

		return response;
	}

	@Transactional
	public IncomeAllocate deleteIncomeAdjust(String oid, String operator) {
		IncomeAllocate incomeAllocate = incomeAllocateDao.findOne(oid);
		IncomeEvent ie = this.incomeEventDao.findOne(incomeAllocate.getIncomeEvent().getOid());

		if (ie != null && !IncomeEvent.STATUS_Create.equals(ie.getStatus()) && !IncomeEvent.STATUS_Fail.equals(ie.getStatus())) {
			throw AMPException.getException(60008);
		}

		Timestamp now = new Timestamp(System.currentTimeMillis());
		ie.setAuditor(operator);
		ie.setAuditTime(now);
		ie.setStatus(IncomeEvent.STATUS_Delete);
		incomeEventDao.saveAndFlush(ie);
		return incomeAllocate;
	}

	@Transactional
	public BaseResp allocateIncomeAgain(String oid, String operator) {
		BaseResp response = new BaseResp();

		IncomeAllocate incomeAllocate = incomeAllocateDao.findOne(oid);
		IncomeEvent ie = this.incomeEventDao.findOne(incomeAllocate.getIncomeEvent().getOid());

		if (ie != null && !IncomeEvent.STATUS_AllocateFail.equals(ie.getStatus())) {
			response.setErrorCode(-1);
			response.setErrorMessage("只有分配失败的收益分配才可以再次发送!");
			throw AMPException.getException(60009);
		}

		ie.setStatus(IncomeEvent.STATUS_Allocating);// 发放中
		incomeEventDao.saveAndFlush(ie);

		this.interestRateMethodService.interest(incomeAllocate.getOid(), incomeAllocate.getProduct().getOid());
		return response;
	}

	/**
	 * @author yuechao
	 *         获取最近的收益发放日期
	 * @param productOid
	 * @return
	 */
	public Date getLatestIncomeDate(String productOid) {
		Date incomeDate = this.incomeEventDao.getLatestIncomeDate(productOid);
		return incomeDate;
	}

	public boolean isIncomeAllocated(final String productOid, final Date baseDate) {
		Specification<IncomeAllocate> spec = new Specification<IncomeAllocate>() {
			@Override
			public Predicate toPredicate(Root<IncomeAllocate> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.and(cb.equal(root.get("product").get("oid").as(String.class), productOid), cb.equal(root.get("baseDate").as(Date.class), baseDate),
						cb.equal(root.get("incomeEvent").get("status").as(String.class), IncomeEvent.STATUS_Allocated));
			}
		};

		spec = Specifications.where(spec);

		List<IncomeAllocate> ims = incomeAllocateDao.findAll(spec);
		if (ims != null && ims.size() > 0) {
			return true;
		}
		return false;
	}

}
