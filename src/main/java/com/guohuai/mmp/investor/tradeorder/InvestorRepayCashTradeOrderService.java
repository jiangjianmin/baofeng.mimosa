package com.guohuai.mmp.investor.tradeorder;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import com.guohuai.calendar.TradeCalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.guohuai.ams.duration.fact.income.IncomeAllocate;
import com.guohuai.ams.guess.GuessEntity;
import com.guohuai.ams.guess.GuessItemEntity;
import com.guohuai.ams.guess.GuessService;
import com.guohuai.ams.product.OnSaleT0ProductRep;
import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductDao;
import com.guohuai.ams.product.ProductLog;
import com.guohuai.ams.product.ProductRaisingIncomeDao;
import com.guohuai.ams.product.ProductService;
import com.guohuai.ams.product.reward.ProductIncomeRewardCacheService;
import com.guohuai.basic.component.exception.GHException;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DecimalUtil;
import com.guohuai.component.util.StringUtil;
import com.guohuai.component.web.view.BaseResp;
import com.guohuai.component.web.view.PageResp;
import com.guohuai.mmp.publisher.hold.PublisherHoldEntity;
import com.guohuai.mmp.publisher.hold.PublisherHoldService;
import com.guohuai.mmp.publisher.investor.InterestRateMethodService;
import com.guohuai.mmp.publisher.investor.InterestReq;
import com.guohuai.mmp.publisher.investor.InterestRequireNew;
import com.guohuai.mmp.publisher.product.rewardincomepractice.PracticeService;
import com.guohuai.mmp.publisher.product.rewardincomepractice.RewardIsNullRep;
import com.guohuai.operate.api.AdminSdk;
import com.guohuai.operate.api.objs.admin.AdminObj;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class InvestorRepayCashTradeOrderService {

	@Autowired
	private ProductService productService;
	@Autowired
	private PublisherHoldService publisherHoldService;
	@Autowired
	private InterestRequireNew interestRequireNew;
	@Autowired
	private InvestorRepayCashTradeOrderRequireNewService investorRepayCashTradeOrderRequireNewService;
	@Autowired
	private InterestRateMethodService interestRateMethodService;
	@Autowired
	private PracticeService practiceService;
	@Autowired
	private GuessService guessService;
	@Autowired
	private ProductDao productDao;
	@Autowired
	private AllocateInterestAuditDao allocateInterestAuditDao;
	@Autowired
	private AdminSdk adminSdk;
	@Autowired
	private ProductRaisingIncomeDao productRaisingIncomeDao;
	@Autowired
	private ProductIncomeRewardCacheService productIncomeRewardCacheService;
	@Autowired
	private TradeCalendarService tradeCalendarService;

	/**
	 * isEstablish true 产品募集成立，进入存续期
	 * false 产品募集失败，还本付募集期利息
	 * @param productOid
	 * @param isEstablish
	 */
	public boolean isEstablish(String productOid, boolean isEstablish) {
		Product product = this.productService.findByOid(productOid);
		boolean okFlag = true;


		if (isEstablish) {
			this.productService.lockProduct(productOid);
			updateExpectedRevenue(product);
			this.productService.unLockProduct(productOid);
		} else {
			/** 还本付息锁 */
			this.productService.repayLock(productOid);
			OnSaleT0ProductRep prodcutRep = productService.getOnSaleProductOid();
			if(prodcutRep == null) {
				throw new AMPException("暂无可售的活期产品");
			}
			String lastOid = "0";
			while (true) {
				List<PublisherHoldEntity> holds = this.publisherHoldService.findByProduct(
						product, lastOid);
				if (holds.isEmpty()) {
					break;
				}
				for (PublisherHoldEntity hold : holds) {
					try {
						this.investorRepayCashTradeOrderRequireNewService.processItem(hold.getOid(), prodcutRep.getProductOid());
					} catch (Exception e) {
						log.error(e.getMessage(), e);
						okFlag = false;
					}
					lastOid = hold.getOid();
				}
			}
			if (okFlag) {
				this.productService.updateRepayStatus(product, Product.PRODUCT_repayLoanStatus_repayed, Product.PRODUCT_repayInterestStatus_repayed);
			} else {
				this.productService.updateRepayStatus(product, Product.PRODUCT_repayLoanStatus_repayFailed, Product.PRODUCT_repayInterestStatus_repayFailed);
			}
		}
		return okFlag;
	}

	private void updateExpectedRevenue(Product product) {
		String lastOid = "0";
		while (true) {
			List<PublisherHoldEntity> holds = this.publisherHoldService.findByProduct(
					product, lastOid);
			if (holds.isEmpty()) {
				break;
			}
			for (PublisherHoldEntity hold : holds) {
				try {
					this.investorRepayCashTradeOrderRequireNewService.updateExpectedRevenue(hold.getOid());
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
				lastOid = hold.getOid();
			}
		}

	}

	/**
	 * 派息申请
	 * @param oids
	 * @param operator
	 * @return
	 * @throws ParseException
	 */
	@Transactional
	public BaseResp allocateIncomeApply(String productOid, BigDecimal incomeAmount, BigDecimal fpRate, String operator) throws ParseException {
		BaseResp response = new BaseResp();
		Product product = productDao.findOne(productOid);
		if (Arrays.asList("APPLYING", "INTERESTING", "REPAYING", "TOAUDIT", "AUDITPASS", "done").contains(product.getInterestAuditStatus())) {
			throw new AMPException("当前状态不能提交派息审核");
		}
		this.productService.updateInterestAuditStatus(productOid, product.getInterestAuditStatus(), Product.INTEREST_AUDIT_STATUS_applying);
		this.doAllocateIncomeApply(productOid, incomeAmount, fpRate, operator);
		return response;
	}

	/**
	 * 批量提交派息审核
	 * @param oids
	 * @param operator
	 * @return
	 * @throws ParseException
	 */
	@Transactional
	public BaseResp batchAllocateIncomeApply(List<String> oids, String interestAuditStatus, String operator) throws ParseException {
		if (Arrays.asList("APPLYING", "INTERESTING", "REPAYING", "TOAUDIT", "AUDITPASS", "done").contains(interestAuditStatus)) {
			throw new AMPException("当前状态不能提交派息审核");
		}
		BaseResp response = new BaseResp();
		List<Product> ps = productDao.findByOidIn(oids);
		this.productService.batchUpdateInterestAuditStatus(oids, interestAuditStatus, Product.INTEREST_AUDIT_STATUS_applying);
		for (Product p : ps) {
			BigDecimal incomeAmount = new BigDecimal(0);
			incomeAmount = p.getExpAror().multiply(p.getCollectedVolume()).divide(new BigDecimal(p.getIncomeCalcBasis()), 2, BigDecimal.ROUND_HALF_DOWN);
			this.doAllocateIncomeApply(p.getOid(), incomeAmount, p.getExpAror().multiply(new BigDecimal(100)), operator);
		}
		return response;
	}

	public void doAllocateIncomeApply(String productOid, BigDecimal incomeAmount, BigDecimal fpRate, String operator) {
		Product product = productDao.findOne(productOid);
		if (!Product.STATE_Durationend.equals(product.getState())) {
			// error.define[30054]=募集期尚未结束(CODE:30054)
			throw AMPException.getException(30054);
		}
		List<AllocateInterestAudit> allocateInterestAuditList = allocateInterestAuditDao.findInterestToAudit(productOid);
		if (!allocateInterestAuditList.isEmpty()) {
			// error.define[16006]=不允许重复提交(CODE:16006)
			throw AMPException.getException(16006);
		}

		AllocateInterestAudit allocateInterestAudit = new AllocateInterestAudit();
		Timestamp now = new Timestamp(System.currentTimeMillis());
		allocateInterestAudit.setOid(StringUtil.uuid());
		allocateInterestAudit.setProduct(product);
		allocateInterestAudit.setTotalVolume(product.getRaisedTotalNumber()); // 产品总规模
		allocateInterestAudit.setBuyAmount(product.getCollectedVolume());  // 用户申购总额（元）
		BigDecimal allocateIncomeAmount = BigDecimal.ZERO;   // 总收益（募集期+存续期）
		BigDecimal productRaiseIncome = BigDecimal.ZERO;     // 募集期收益
		BigDecimal productDurationIncome = BigDecimal.ZERO;  // 存续期收益
		productDurationIncome = incomeAmount.multiply(new BigDecimal(product.getDurationPeriodDays())); // 存续期收益
		if (product.getRecPeriodExpAnYield().compareTo(BigDecimal.ZERO) == 0) {  // 募集期无利息
			allocateIncomeAmount = allocateIncomeAmount.add(productDurationIncome);
			allocateInterestAudit.setAllocateIncomeAmount(allocateIncomeAmount); // 分配收益总额（元）
		} else { // 募集期有利息
			// 募集期收益（从定期募集期收益明细中查询T_MONEY_PRODUCT_RAISING_INCOME）
			productRaiseIncome = productRaisingIncomeDao.queryProductRaisingIncome(productOid);
			productRaiseIncome = productRaiseIncome == null ? BigDecimal.ZERO:productRaiseIncome;
			allocateIncomeAmount = allocateIncomeAmount.add(productRaiseIncome).add(productDurationIncome);
			allocateInterestAudit.setAllocateIncomeAmount(allocateIncomeAmount); // 分配收益总额（元）
//			if (productRaiseIncome == null) {
//				// error.define[16007]=募集期利息没有生成(CODE:16007)
//				throw AMPException.getException(16007);
//			}else {
//				allocateIncomeAmount = allocateIncomeAmount.add(productRaiseIncome).add(productDurationIncome);
//				allocateInterestAudit.setAllocateIncomeAmount(allocateIncomeAmount); // 分配收益总额（元）
//			}
		}
		allocateInterestAudit.setTotalRepay(product.getCollectedVolume().add(allocateIncomeAmount)); // 本金利息总额（元）
		allocateInterestAudit.setRatio(fpRate);
		allocateInterestAudit.setApplicant(operator);
		allocateInterestAudit.setApplyTime(now);
		allocateInterestAudit.setAuditStatus(allocateInterestAudit.AUDIT_STATE_ToAudit);
		allocateInterestAudit.setUpdateTime(now);
		this.allocateInterestAuditDao.save(allocateInterestAudit);
		this.productService.updateInterestAuditStatus(productOid, Product.INTEREST_AUDIT_STATUS_applying, Product.INTEREST_AUDIT_STATUS_toAudit);
	}

	/**
	 * 派发收益主动撤回
	 * @param oid   审核记录oid
	 * @param auditComment
	 * @param operator
	 * @return
	 * @throws ParseException
	 */
	@Transactional
	public BaseResp allocateIncomeReject(String oid, String auditComment, String operator) throws ParseException {
		BaseResp response = new BaseResp();

		AllocateInterestAudit allocateInterestAudit = allocateInterestAuditDao.findOne(oid);
		Product product = allocateInterestAudit.getProduct();
		this.productService.updateInterestAuditStatus(product.getOid(), Product.INTEREST_AUDIT_STATUS_toAudit, Product.INTEREST_AUDIT_STATUS_auditReject);
		Timestamp now = new Timestamp(System.currentTimeMillis());
		int i = this.allocateInterestAuditDao.updateAllocateInterestAudit(oid, operator, AllocateInterestAudit.AUDIT_STATE_AuditReject, now, auditComment);
		if (i < 1) {
			// 派息申请驳回出错
			throw AMPException.getException(16003);
		}
		return response;
	}

	/**
	 * 派发收益驳回
	 * @param oid   审核记录oid
	 * @param auditComment
	 * @param operator
	 * @return
	 * @throws ParseException
	 */
	@Transactional
	public BaseResp allocateIncomeWithdraw(String oid, String operator) throws ParseException {
		BaseResp response = new BaseResp();

		AllocateInterestAudit allocateInterestAudit = allocateInterestAuditDao.findOne(oid);
		Product product = allocateInterestAudit.getProduct();
		this.productService.updateInterestAuditStatus(product.getOid(), Product.INTEREST_AUDIT_STATUS_toAudit, Product.INTEREST_AUDIT_STATUS_withDrawed);
		Timestamp now = new Timestamp(System.currentTimeMillis());
		int i = this.allocateInterestAuditDao.updateAllocateInterestAudit(oid, operator, AllocateInterestAudit.AUDIT_STATE_Withdrawed, now, null);
		if (i < 1) {
			// 派息申请主动撤回出错
			throw AMPException.getException(16004);
		}
		return response;
	}

	/**
	 * 产品派发收益明细
	 */
	@Transactional
	public AllocateIncomeAuditResp interestDetail(String oid){
		AllocateInterestAudit allocateInterestAuditLog = allocateInterestAuditDao.findOne(oid);
		AllocateIncomeAuditResp pr = new AllocateIncomeAuditResp(allocateInterestAuditLog);
		return pr;
	}

	/**
	 * 派发收益审核列表查询
	 * @param spec
	 * @param pageable
	 * @return
	 */
	public PageResp<AllocateIncomeAuditResp> allocateIncomeList(Specification<AllocateInterestAudit> spec, Pageable pageable) {
		Page<AllocateInterestAudit> cas = this.allocateInterestAuditDao.findAll(spec, pageable);
		PageResp<AllocateIncomeAuditResp> pagesRep = new PageResp<AllocateIncomeAuditResp>();
		if (cas != null && cas.getContent() != null && cas.getTotalElements() > 0) {

			Map<String, AdminObj> adminObjMap = new HashMap<String, AdminObj>();
			AdminObj adminObj = null;

			List<AllocateIncomeAuditResp> rows = new ArrayList<AllocateIncomeAuditResp>();

			for (AllocateInterestAudit p : cas) {
				AllocateIncomeAuditResp queryRep = new AllocateIncomeAuditResp(p);

				if(!StringUtil.isEmpty(queryRep.getApplicant())) {
					if (adminObjMap.get(queryRep.getApplicant()) == null) {
						try {
							adminObj = adminSdk.getAdmin(queryRep.getApplicant());
							adminObjMap.put(queryRep.getApplicant(), adminObj);
						} catch (Exception e) {
						}

					}
					if (adminObjMap.get(queryRep.getApplicant()) != null) {
						queryRep.setApplicant(adminObjMap.get(queryRep.getApplicant()).getName());
					}
				}

				if(!StringUtil.isEmpty(queryRep.getAuditor())) {
					if (adminObjMap.get(queryRep.getAuditor()) == null) {
						try {
							adminObj = adminSdk.getAdmin(queryRep.getAuditor());
							adminObjMap.put(queryRep.getAuditor(), adminObj);
						} catch (Exception e) {
						}

					}
					if (adminObjMap.get(queryRep.getAuditor()) != null) {
						queryRep.setAuditor(adminObjMap.get(queryRep.getAuditor()).getName());
					}
				}


				rows.add(queryRep);
			}
			pagesRep.setRows(rows);
		}
		pagesRep.setTotal(cas.getTotalElements());
		return pagesRep;
	}

	/**
	 * 付息
	 * @param oid : 派发收益审核记录的oid
	 */
	public void allocateIncome(String oid,String auditComment, String productOid, BigDecimal incomeAmount, BigDecimal fpRate,String operator) {
		log.info("【派息审核】产品：{}，利率：{}，操作人：{}，incomeAmount：{}，审核意见：{}", productOid, fpRate, operator, incomeAmount, auditComment);
//		if(incomeAmount==null||incomeAmount.compareTo(BigDecimal.ZERO)==0){
//			throw new GHException("所派利息为0请检查");
//		}
//		Product product = this.productService.findByOid(productOid);
//		if (!Product.STATE_Durationend.equals(product.getState())) {
//			// error.define[30054]=募集期尚未结束(CODE:30054)
//			throw AMPException.getException(30054);
//		}
//		//竞猜宝相关校验
//		if(product.getGuess()!=null){
//			//1.产品对应的竞猜活动开奖后才可派息
//			//2.派息利率出现负数或0时不能派息
//			GuessEntity guess = product.getGuess();
//			List<GuessItemEntity> guessItems = this.guessService.getGuessItemByGuessOid(guess.getOid());
//			for(GuessItemEntity item:guessItems){
//				BigDecimal percent = item.getPercent();
//				if(percent==null){
//					throw new GHException("请在派息前先设置开奖结果");
//				}
//				BigDecimal netPercent = item.getNetPercent();
//				if(netPercent!=null&&netPercent.compareTo(BigDecimal.ZERO)<=0){
//					throw new GHException("派息利率计算出有负数存在，请重新设置开奖答案");
//				}
//			}
//			
//		}
//		
//		
//		/** 派息锁 */
//		this.productService.repayInterestLock(productOid);
//		
//		RewardIsNullRep rewardIsNullRep = null;
//		if(productIncomeRewardCacheService.hasRewardIncome(product.getOid())) {
//			rewardIsNullRep = this.practiceService.rewardIsNotNullRep(product, null);
//		} else {
//			rewardIsNullRep = this.practiceService.rewardIsNullRep(product, null);
//		}
//		BigDecimal ratio = DecimalUtil.zoomIn(fpRate, 100);
//		incomeAmount = incomeAmount.multiply(new BigDecimal(product.getDurationPeriodDays()));
//		InterestReq ireq = new InterestReq();
//		ireq.setProduct(product);
//		ireq.setTotalInterestedVolume(rewardIsNullRep.getTotalHoldVolume());
//		ireq.setIncomeAmount(incomeAmount);
//		ireq.setRatio(ratio);
//		ireq.setIncomeDate(rewardIsNullRep.getTDate());
//		ireq.setIncomeType(IncomeAllocate.ALLOCATE_INCOME_TYPE_durationIncome);
//		
//		IncomeAllocate incomeAllocate = this.interestRequireNew.newAllocate(ireq);
//		
//		// 更新派发收益记录
//		AllocateInterestAudit allocateInterestAudit = new AllocateInterestAudit();
//		Timestamp now = new Timestamp(System.currentTimeMillis());
//		int i = this.allocateInterestAuditDao.updateAllocateInterestAudit(oid, operator, allocateInterestAudit.AUDIT_STATE_AuditPass, now, auditComment);
//		if (i < 1) {
//			// 派息审核通过出错
//			throw AMPException.getException(16005);
//		}
        this.productService.updateInterestAuditStatus(productOid, Product.INTEREST_AUDIT_STATUS_toAudit, Product.INTEREST_AUDIT_STATUS_interesting);
        AllocateInterestAudit allocateInterestAudit = allocateInterestAuditDao.findOne(oid);
        BigDecimal basicIncomeAmount = new BigDecimal(0);
        Product p = allocateInterestAudit.getProduct();
        incomeAmount = DecimalUtil.zoomIn(allocateInterestAudit.getRatio(), 100).multiply(p.getCollectedVolume()).divide(new BigDecimal(p.getIncomeCalcBasis()), 2, BigDecimal.ROUND_HALF_DOWN);
        this.investorRepayCashTradeOrderRequireNewService.allocateIncome(oid, auditComment, productOid, incomeAmount, allocateInterestAudit.getRatio(), operator);
    }

    /**
     * 批量派息
     * @param oids
     * @param operator
     * @return
     * @throws ParseException
     */
    public BaseResp batchAllocateIncome(List<String> oids, String operator) throws ParseException {
        BaseResp response = new BaseResp();
//		this.productService.batchUpdateInterestAuditStatus(oids, Product.INTEREST_AUDIT_STATUS_toAudit, Product.INTEREST_AUDIT_STATUS_interesting);
        List<Product> ps = productDao.findByOidIn(oids);
        for (Product p : ps) {
            this.productService.updateInterestAuditStatus(p.getOid(), Product.INTEREST_AUDIT_STATUS_toAudit, Product.INTEREST_AUDIT_STATUS_interesting);
            // 根据产品查询产品的派息审核申请记录
            AllocateInterestAudit allocateInterestAudit = allocateInterestAuditDao.findProductInterestToAudit(p.getOid());
            BigDecimal incomeAmount = new BigDecimal(0);
            incomeAmount = DecimalUtil.zoomIn(allocateInterestAudit.getRatio(), 100).multiply(p.getCollectedVolume()).divide(new BigDecimal(p.getIncomeCalcBasis()), 2, BigDecimal.ROUND_HALF_DOWN);
            log.info("【批量派息审核】产品：{}，利率：{}，操作人：{}，incomeAmount：{}", p.getOid(), allocateInterestAudit.getRatio(), operator, incomeAmount);
            this.investorRepayCashTradeOrderRequireNewService.allocateIncome(allocateInterestAudit.getOid(), AllocateInterestAudit.AUDIT_TYPE_batch, p.getOid(), incomeAmount, allocateInterestAudit.getRatio(), operator);
		}
		return response;
	}

	@Transactional
	public void repayCash(String productOid) {
		this.productService.updateInterestAuditStatus(productOid, Product.INTEREST_AUDIT_STATUS_auditPass, Product.INTEREST_AUDIT_STATUS_repaying);
		this.doRepayCash(productOid);
	}

	/**
	 * 批量还本付息
	 * @param oids
	 * @param operator
	 */
	@Transactional
	public void batchRepayCash(List<String> oids) {
		this.productService.batchUpdateInterestAuditStatus(oids, Product.INTEREST_AUDIT_STATUS_auditPass, Product.INTEREST_AUDIT_STATUS_repaying);
		List<Product> ps = productDao.findByOidIn(oids);
		for (Product p : ps) {
			this.doRepayCash(p.getOid());
		}
	}

	public void doRepayCash(String productOid) {
		/** 还本付息锁 */
		this.productService.repayLoanLock(productOid);

		Product product = productService.findByOid(productOid);
		if (Product.TYPE_Producttype_01.equals(product.getType().getOid())
				&& Product.STATE_Durationend.equals(product.getState())) {
			String lastOid = "0";
			boolean okFlag = true;
			OnSaleT0ProductRep prodcutRep = productService.getOnSaleProductOid();
			if(prodcutRep == null) {
				throw new AMPException("暂无可售的活期产品");
			}
			String investDemandProductOid = prodcutRep.getProductOid();
			while (true) {
				List<PublisherHoldEntity> holds = this.publisherHoldService.findByProduct(product, lastOid);
				if (holds.isEmpty()) {
					break;
				}
				for (PublisherHoldEntity hold : holds) {
					try {
						investorRepayCashTradeOrderRequireNewService.processCashItem(hold.getOid(), investDemandProductOid);
					} catch (Exception e) {
						log.error(e.getMessage(), e);
						okFlag = false;
					}
					lastOid = hold.getOid();
				}
			}

			if (okFlag) {
				this.productService.repayLoanEnd(product.getOid(), Product.PRODUCT_repayLoanStatus_repayed, Product.INTEREST_AUDIT_STATUS_done);
			} else {
				this.productService.repayLoanEnd(product.getOid(), Product.PRODUCT_repayLoanStatus_repayFailed, Product.INTEREST_AUDIT_STATUS_done);
			}
		} else {
			throw new AMPException("产品非存续期结束 ");
		}
	}

	/**
	 * 获取指定id的产品对象
	 *
	 * @param pid
	 *            产品对象id
	 * @return {@link Product}
	 */
	public Product getProductByOid(String pid) {
		if (StringUtil.isEmpty(pid)) {
			return null;
		}
		Product product = this.productDao.findOne(pid);
		if (product == null || Product.YES.equals(product.getIsDeleted())) {
			throw AMPException.getException(90000);
		}
		return product;

	}

	public void cycleProductRepay() {
		Date date = Date.valueOf(LocalDate.now());
		log.info("【循环产品还本付息】当前日期：{}，开始还本付息", date);
		if (tradeCalendarService.isTrade(date)) {
			List<Product> products = productService.getDurationEndCycleProductList();
			if (products == null || products.size() < 1) {
				log.info("【循环产品还本付息】当前日期{}无需还本付息产品！", date);
				return;
			}
			log.info("【循环产品还本付息】需还本付息产品共{}个，具体产品ID如下：{}", products.size(), products.stream().map(Product::getOid).collect(Collectors.toList()));

			OnSaleT0ProductRep productRep = productService.getOnSaleProductOid();
			if (productRep == null) {
				throw new AMPException("暂无可售的活期产品");
			}
			String investDemandProductOid = productRep.getProductOid();
			for (Product repayProduct : products) {
				// 还本付息锁
				this.productService.updateInterestAuditStatus(repayProduct.getOid(), Product.INTEREST_AUDIT_STATUS_auditPass, Product.INTEREST_AUDIT_STATUS_repaying);
				this.productService.repayLoanLock(repayProduct.getOid());
				boolean okFlag = true;
				String lastOid = "0";
				while (true) {
					List<PublisherHoldEntity> holds = this.publisherHoldService.findByCycleProductOidAndContinueStatus(repayProduct.getOid(), 0, lastOid);
					if (holds == null || holds.isEmpty()) {
						break;
					}
					for (PublisherHoldEntity hold : holds) {
						try {
							investorRepayCashTradeOrderRequireNewService.processCycleProductCashItem(hold.getOid(), investDemandProductOid);
						} catch (Exception e) {
							log.error("【循环产品还本付息】持仓{}还本付息失败，具体原因如下{},{}", hold.getOid(), e.getMessage(), e);
							okFlag = false;
						}
						lastOid = hold.getOid();
					}
				}
				if (okFlag) {
					this.productService.repayLoanEnd(repayProduct.getOid(), Product.PRODUCT_repayLoanStatus_repayed, Product.INTEREST_AUDIT_STATUS_done);
				} else {
					this.productService.repayLoanEnd(repayProduct.getOid(), Product.PRODUCT_repayLoanStatus_repayFailed, Product.INTEREST_AUDIT_STATUS_done);
				}
			}
		} else {
			log.info("【循环产品还本付息】当前日期{}不是交易日，跳过还本付息。", date);
		}
	}
}
