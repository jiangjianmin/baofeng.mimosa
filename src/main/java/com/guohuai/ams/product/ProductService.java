package com.guohuai.ams.product;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import com.guohuai.ams.product.cycleProduct.CycleProductOperateContinueService;
import com.guohuai.mmp.publisher.holdapart.snapshot.SnapshotService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.guohuai.ams.dict.Dict;
import com.guohuai.ams.dict.DictService;
import com.guohuai.ams.duration.assetPool.AssetPoolDao;
import com.guohuai.ams.duration.assetPool.AssetPoolEntity;
import com.guohuai.ams.duration.assetPool.AssetPoolService;
import com.guohuai.ams.guess.GuessEntity;
import com.guohuai.ams.label.LabelEntity;
import com.guohuai.ams.label.LabelEnum;
import com.guohuai.ams.label.LabelService;
import com.guohuai.ams.product.coupon.ProductCouponService;
import com.guohuai.ams.product.order.channel.ProductChannelOrderService;
import com.guohuai.ams.product.productChannel.ProductChannel;
import com.guohuai.ams.product.productChannel.ProductChannelService;
import com.guohuai.ams.product.reward.ProductIncomeReward;
import com.guohuai.ams.product.reward.ProductIncomeRewardService;
import com.guohuai.ams.product.reward.ProductRewardResp;
import com.guohuai.ams.productLabel.ProductLabel;
import com.guohuai.ams.productLabel.ProductLabelService;
import com.guohuai.ams.productPackage.ProductPackage;
import com.guohuai.ams.productPackage.ProductPackageDao;
import com.guohuai.ams.supplement.order.MechanismOrder;
import com.guohuai.ams.supplement.order.RestOrderAmountRep;
import com.guohuai.ams.supplement.order.TnProduct;
import com.guohuai.ams.supplement.order.TnProductRep;
import com.guohuai.cache.service.CacheSPVHoldService;
import com.guohuai.calendar.TradeCalendarService;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.DecimalUtil;
import com.guohuai.component.util.StringUtil;
import com.guohuai.component.web.view.BaseResp;
import com.guohuai.component.web.view.PageResp;
import com.guohuai.file.File;
import com.guohuai.file.FileResp;
import com.guohuai.file.FileService;
import com.guohuai.file.SaveFileForm;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountEntity;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountService;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity;
import com.guohuai.mmp.platform.accment.AccmentService;
import com.guohuai.mmp.platform.baseaccount.statistics.PlatformStatisticsService;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountDao;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountEntity;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountEntityDao;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountService;
import com.guohuai.mmp.publisher.baseaccount.statistics.PublisherStatisticsService;
import com.guohuai.mmp.publisher.corporate.Corporate;
import com.guohuai.mmp.publisher.corporate.CorporateDao;
import com.guohuai.mmp.publisher.hold.PublisherHoldDao;
import com.guohuai.mmp.publisher.hold.PublisherHoldEntity;
import com.guohuai.mmp.publisher.hold.PublisherHoldService;
import com.guohuai.operate.api.AdminSdk;
import com.guohuai.operate.api.objs.admin.AdminObj;
import com.guohuai.tuip.api.TulipSdk;

@Service
@Transactional
public class ProductService {
	Logger logger = LoggerFactory.getLogger(ProductService.class);
	@Autowired
	private ProductDao productDao;
	@Autowired
	private ProductLogDao productLogDao;
	@Autowired
	private DictService dictService;
	@Autowired
	private FileService fileService;
	@Autowired
	private ProductChannelService productChannelService;
	@Autowired
	private AdminSdk adminSdk;
	@Autowired
	private AssetPoolDao assetPoolDao;
	@Autowired
	private PublisherBaseAccountEntityDao publisherDao;
	@Autowired
	private PublisherHoldDao publisherHoldDao;
	@Autowired
	private ProductIncomeRewardService productRewardService;
	@Autowired
	private AssetPoolService assetPoolService;
	@Autowired
	private ProductIncomeRewardService incomeRewardService;
	@Autowired
	private PublisherHoldService publisherHoldService;
	@Autowired
	private PublisherBaseAccountDao publisherBaseAccountDao;
	@Autowired
	TradeCalendarService tradeCalendarService;
	@Autowired
	private CorporateDao corporateDao;
	@Autowired
	private PublisherStatisticsService publisherStatisticsService;
	@Autowired
	private PlatformStatisticsService platformStatisticsService;
	@Autowired
	private ProductLabelService productLabelService;
	@Autowired
	private InvestorBaseAccountService investorBaseAccountService;
	@Autowired
	private AccmentService accmentService;
	@Autowired
	private CacheSPVHoldService cacheSPVHoldService;
	@Autowired
	private PublisherBaseAccountService publisherBaseAccountService;
	@Autowired
	private LabelService labelService;
	@Autowired
	private ProductPackageDao productPackageDao;
	@Autowired
	private ProductChannelOrderService productChannelOrderService;
	@Autowired
	private ProductCouponService productCouponService;
	@Autowired
	private ProductTypeDetailService productTypeDetailService;
	@Autowired
	private TulipSdk tulipSdk;
	@Autowired
	private SnapshotService snapshotService;
	@Autowired
	private CycleProductOperateContinueService cycleProductOperateContinueService;
	/**
	 * 获取prodictOid对应产品所有可以选择的资产池的名称列表
	 * 
	 * @param productOid
	 * @param isRegular 
	 * @return
	 */
	@Transactional
	public List<JSONObject> getOptionalAssetPoolNameList(String productOid, String type) {

		List<JSONObject> jsonObjList = assetPoolService.getAllNameList();

		Specification<Product> spec = new Specification<Product>() {
			@Override
			public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.equal(root.get("isDeleted").as(String.class), Product.NO);
			}
		};
		List<Product> products = this.productDao.findAll(Specifications.where(spec));
		
		if (productOid != null){
			Product pr = this.findByOid(productOid);
			type = pr.getType().getOid();
		}

		if (products != null && products.size() > 0) {
			Set<String> choosedAssetPoolOids = new HashSet<String>();

			String productAssetOid = null;// productOid该产品对应的资产池

			if (StringUtil.isEmpty(productOid)) {
				for (Product p : products) {
					if (p.getAssetPool() != null) {
						if (type == null || !type.equals(Product.TYPE_Producttype_01) || !p.getType().getOid().equals(Product.TYPE_Producttype_01)){
							choosedAssetPoolOids.add(p.getAssetPool().getOid());
						}
					}
				}
			} else {
				for (Product p : products) {
					if (p.getAssetPool() != null) {
						if (productOid.equals(p.getOid())) {
							productAssetOid = p.getAssetPool().getOid();
						} else {
							if (type == null || !type.equals(Product.TYPE_Producttype_01) || !p.getType().getOid().equals(Product.TYPE_Producttype_01)){
								choosedAssetPoolOids.add(p.getAssetPool().getOid());
							}
						}
					}
				}
			}

			List<JSONObject> validAssetPoolNames = new ArrayList<JSONObject>();
			for (JSONObject assetPoolName : jsonObjList) {
				if (productAssetOid != null && productAssetOid.equals(assetPoolName.get("oid"))) {
					validAssetPoolNames.add(assetPoolName);
				} else if (!choosedAssetPoolOids.contains(assetPoolName.get("oid"))) {
					validAssetPoolNames.add(assetPoolName);
				}
			}
			return validAssetPoolNames;

		} else {
			return jsonObjList;
		}

	}

	@Transactional
	public BaseResp savePeriodic(SavePeriodicProductForm form, String operator) {
		BaseResp response = new BaseResp();

		Product p = newProduct();
		p.setOid(StringUtil.uuid());
		p.setCode(form.getCode()); //产品编号
		p.setName(form.getName()); //产品简称
		p.setFullName(form.getFullName()); //产品全称
		p.setAdministrator(form.getAdministrator()); //产品管理人
		p.setState(Product.STATE_Create);
		p.setIsDeleted(Product.NO);
		p.setRepayInterestStatus(Product.PRODUCT_repayInterestStatus_toRepay);//付息状态
		p.setRepayLoanStatus(Product.PRODUCT_repayLoanStatus_toRepay);//还本状态

		// 产品类型
		Dict assetType = this.dictService.get(form.getTypeOid());
		p.setType(assetType);
		
		AssetPoolEntity assetPool = assetPoolDao.findOne(form.getAssetPoolOid());
		if (null == assetPool) {
			throw new AMPException("资产池不存在");
		}
		p.setAssetPool(assetPool);
		p.setPublisherBaseAccount(assetPool.getSpvEntity());
		p.setReveal(form.getReveal());
		if (Product.YES.equals(form.getReveal())) {
			p.setRevealComment(StringUtil.empty2Null(form.getRevealComment()));
		}
		p.setCurrency(form.getCurrency());
		p.setIncomeCalcBasis(form.getIncomeCalcBasis());
		p.setOperationRate(DecimalUtil.zoomIn(form.getOperationRate(), 100));
		
		// 年化收益
		p.setExpAror(DecimalUtil.zoomIn(form.getExpAror(), 100));
		if (null == form.getExpArorSec() || form.getExpArorSec().compareTo(BigDecimal.ZERO) == 0) {
			p.setExpArorSec(p.getExpAror());
		} else {
			p.setExpArorSec(DecimalUtil.zoomIn(form.getExpArorSec(), 100));
		}
		p.setRewardInterest(DecimalUtil.null2Zero(form.getRewardInterest()));
				
		// 募集开始时间类型;募集期天数:()个自然日;起息日天数:成立后()个自然日;存续期天数:()个自然日;还本付息日 存续期结束后第()个自然日
		if (Product.DATE_TYPE_ManualInput.equals(form.getRaiseStartDateType()) && null == form.getRaiseStartDate()) {
			// error.define[90009]=请填写募集开始时间
			throw AMPException.getException(90009);
		}
		p.setRaiseStartDateType(form.getRaiseStartDateType());
		
		p.setRaisePeriodDays(form.getRaisePeriod()); //募集期
		
		p.setInterestsFirstDays(form.getInterestsFirstDate()); //起息期(成立后)
		
		p.setDurationPeriodDays(form.getDurationPeriod()); // 存续期(成立后)
		
		p.setAccrualRepayDays(form.getAccrualDate()); // 还本付息日
		
		//募集期预期年化收益
		p.setRecPeriodExpAnYield(DecimalUtil.zoomIn(form.getRecPeriodExpAnYield(), 100));
		
		//认购确认日:认购订单提交后()个日内 
		p.setPurchaseConfirmDays(form.getSubscribeConfirmDays());
		
		//募集满额后是否自动触发成立
		p.setRaiseFullFoundType(form.getRaiseFullFoundType());
		
		//募集满额后第()个自然日后自动成立
		if (Product.RAISE_FULL_FOUND_TYPE_AUTO.equals(p.getRaiseFullFoundType())) {
			p.setAutoFoundDays(form.getAutoFoundDays());
		}
	
		//募集期满后最晚成立日
		p.setFoundDays(form.getFoundDays());
		
		// 募集开始日期,募集结束日期
		if (Product.DATE_TYPE_ManualInput.equals(form.getRaiseStartDateType())) {
			p.setRaiseStartDate(form.getRaiseStartDate());

			Date raiseEndDate = DateUtil.addSQLDays(p.getRaiseStartDate(), form.getRaisePeriod() - 1);
			p.setRaiseEndDate(raiseEndDate);// 募集结束时间

			Date setupDate = DateUtil.addSQLDays(raiseEndDate, form.getFoundDays());
			p.setSetupDate(setupDate);// 最晚产品成立时间

			Date durationPeriodEndDate = DateUtil.addSQLDays(setupDate, form.getDurationPeriod() - 1);
			p.setDurationPeriodEndDate(durationPeriodEndDate);// 存续期结束时间

			// 到期最晚还本付息日 指存续期结束后的还本付息最迟发生在存续期后的第X个自然日的23:59:59为止
			Date repayDate = DateUtil.addSQLDays(durationPeriodEndDate, form.getAccrualDate());
			// 到期最晚还本付息日 指存续期结束后的还本付息最迟发生在存续期后的第X个自然日的23:59:59为止
			p.setRepayDate(repayDate);// 到期还款时间

		} else {
			p.setRaiseStartDate(null);
		}
	
		p.setRaisedTotalNumber(form.getRaisedTotalNumber());
		p.setMaxSaleVolume(form.getRaisedTotalNumber());
		
		p.setInvestMin(form.getInvestMin());
		p.setInvestMax(form.getInvestMax());
		p.setInvestAdditional(form.getInvestAdditional());
		p.setInvestDateType(form.getInvestDateType());
		
		p.setNetUnitShare(form.getNetUnitShare());
		
		p.setDealStartTime(StringUtil.empty2Null(form.getDealStartTime()));
		p.setDealEndTime(StringUtil.empty2Null(form.getDealEndTime()));
		
		p.setIsOpenPurchase(Product.YES);
		p.setIsOpenRemeed(Product.NO);
		p.setInvestComment(StringUtil.empty2Null(form.getInvestComment()));

		p.setInstruction(StringUtil.empty2Null(form.getInstruction()));
		p.setRiskLevel(form.getRiskLevel());
		p.setInvestorLevel(form.getInvestorLevel());
		p.setStems(Product.STEMS_Userdefine);
		p.setAuditState(Product.AUDIT_STATE_Nocommit);
		// 其他字段 初始化默认值s
		p.setOperator(operator);
		p.setUpdateTime(DateUtil.getSqlCurrentDate());
		p.setCreateTime(DateUtil.getSqlCurrentDate());

		// 附件文件
		List<SaveFileForm> fileForms = null;
		String fkey = StringUtil.uuid();
		if (StringUtil.isEmpty(form.getFiles())) {
			p.setFileKeys(StringUtil.EMPTY);
		} else {
			p.setFileKeys(fkey);
			fileForms = JSON.parseArray(form.getFiles(), SaveFileForm.class);
		}

		// 投资协议书
		List<SaveFileForm> investFileForm = null;
		String investFileKey = StringUtil.uuid();
		if (StringUtil.isEmpty(form.getInvestFile())) {
			p.setInvestFileKey(StringUtil.EMPTY);
		} else {
			p.setInvestFileKey(investFileKey);
			investFileForm = JSON.parseArray(form.getInvestFile(), SaveFileForm.class);
		}

		// 信息服务协议
		List<SaveFileForm> serviceFileForm = null;

		String serviceFileKey = StringUtil.uuid();
		if (StringUtil.isEmpty(form.getServiceFile())) {
			p.setServiceFileKey(StringUtil.EMPTY);
		} else {
			p.setServiceFileKey(serviceFileKey);
			serviceFileForm = JSON.parseArray(form.getServiceFile(), SaveFileForm.class);
		}
		p.setIfP2P(form.getIfP2P());
		p.setIsP2PAssetPackage(form.getIsP2PAssetPackage());
		//产品详情相关
		p.setProductElement(form.getProductElement());
		p.setProductIntro(form.getProductIntro());
		p.setIsActivityProduct(form.getIsActivityProduct());
		if(form.getIsActivityProduct() == 1){
			//如果是活动产品，则设置折合年化收益率跟产品详情
			p.setActivityDetail(form.getActivityDetail());
			p.setExpectedArrorDisp(DecimalUtil.zoomIn(form.getExpectedArrorDisp(), 100));
		}
		p.setPurchaseApplyStatus(Product.APPLY_STATUS_None);
		p.setRedeemApplyStatus(Product.APPLY_STATUS_None);
		p.setBasicRatio(BigDecimal.ZERO);
		p.setFastRedeemStatus(Product.NO);
		p.setFastRedeemMax(BigDecimal.ZERO);
		p.setFastRedeemLeft(BigDecimal.ZERO);
		
		/**
		 * 账务账户ID
		 */
		String memeberId = accmentService.createSPVTnAccount(p);
		accmentService.createSPVTnLXAccount(p);
		p.setMemberId(memeberId);
		
		List<String> labelOids = new ArrayList<String>();
		if(!StringUtil.isEmpty(form.getBasicProductLabel())) {//基础标签
			labelOids.add(form.getBasicProductLabel());
			//将lableCode存储到product表
			String labelCode = labelService.findLabelCodeByOid(form.getBasicProductLabel());
			p.setProductLabel(labelCode);
		}
		if(form.getExpandProductLabels()!=null && form.getExpandProductLabels().length>0) {//扩展标签
			for(String ex : form.getExpandProductLabels()) {
				labelOids.add(ex);
			}
		}
		/**
		 * 卡券相关
		 */
		p.setUseRedPackages(form.getUseRedPackages());
		p.setUseraiseRateCoupons(form.getUseraiseRateCoupons());
		p = this.productDao.save(p);
		
		//新建产品卡券关联表
		productCouponService.saveEntity(p.getOid(),form.getRedPackages(),form.getRaiseRateCoupons());

		// 附件文件
		this.fileService.save(fileForms, fkey, File.CATE_User, operator);
		// 投资协议书
		this.fileService.save(investFileForm, investFileKey, File.CATE_User, operator);
		// 信息服务协议
		this.fileService.save(serviceFileForm, serviceFileKey, File.CATE_User, operator);
		
		this.productLabelService.saveAndFlush(p, labelOids);

		return response;
	}

	/**
	 *
	 *新建循环开放定期产品
	 * @author yujianlong
	 * @date 2018/3/19 15:32
	 * @param [form, operator]
	 * @return com.guohuai.component.web.view.BaseResp
	 */
	@Transactional
	public BaseResp savePeriodic03(SavePeriodicProduct03Form form, String operator) {
		//判断开放循环产品是否已存在
		if(this.productDao.getProduct03Count()>0){
			throw new AMPException("开放循环产品已存在");
		}
		BaseResp response = new BaseResp();

		Product p = newProduct();
		p.setOid(StringUtil.uuid());
		p.setCode(form.getCode()); //产品编号
		p.setName(form.getName()); //产品简称
		p.setFullName(form.getFullName()); //产品全称
		p.setAdministrator(form.getAdministrator()); //产品管理人
		p.setState(Product.STATE_Create);
		p.setIsDeleted(Product.NO);
		p.setRepayInterestStatus(Product.PRODUCT_repayInterestStatus_toRepay);//付息状态
		p.setRepayLoanStatus(Product.PRODUCT_repayLoanStatus_toRepay);//还本状态

		// 产品类型
		Dict assetType = this.dictService.get(form.getTypeOid());
		p.setType(assetType);

		//资产池
		AssetPoolEntity assetPool = assetPoolDao.findOne(form.getAssetPoolOid());
		if (null == assetPool) {
			throw new AMPException("资产池不存在");
		}
		p.setAssetPool(assetPool);

		/**
		 *循环开放相关
		 */
		p.setMovedupRedeemLockDays(form.getMovedupRedeemLockDays());
		p.setMovedupRedeemMinPay(form.getMovedupRedeemMinPay());
		p.setMovedupRedeemRate(form.getMovedupRedeemRate());

		//发行人
		p.setPublisherBaseAccount(assetPool.getSpvEntity());
		//额外增信
		p.setReveal(form.getReveal());
		if (Product.YES.equals(form.getReveal())) {
			p.setRevealComment(StringUtil.empty2Null(form.getRevealComment()));
		}
		//币种
		p.setCurrency(form.getCurrency());
		//收益计算基础
		p.setIncomeCalcBasis(form.getIncomeCalcBasis());
		//平台运营费率
		p.setOperationRate(BigDecimal.ZERO);
		p.setUseRedPackages(2);
		p.setUseraiseRateCoupons(2);

		// 预期年化收益率
		p.setExpAror(DecimalUtil.zoomIn(form.getExpAror(), 100));
		//预期年化收益率区间
		p.setExpArorSec(p.getExpAror());
		//平台奖励收益率
		p.setRewardInterest(BigDecimal.ZERO);


		p.setRaiseStartDateType("FIRSTRACKTIME");

		p.setRaisePeriodDays(99999); //募集期

		p.setInterestsFirstDays(1); //起息期(成立后)

		p.setDurationPeriodDays(15); // 存续期(成立后)

		p.setAccrualRepayDays(3); // 还本付息日

		//募集期预期年化收益
		p.setRecPeriodExpAnYield(BigDecimal.ZERO);

		//认购确认日:认购订单提交后()个日内
		p.setPurchaseConfirmDays(1);

		//募集满额后是否自动触发成立
		p.setRaiseFullFoundType(Product.RAISE_FULL_FOUND_TYPE_MANUAL);

		//募集满额后第()个自然日后自动成立
		if (Product.RAISE_FULL_FOUND_TYPE_AUTO.equals(p.getRaiseFullFoundType())) {
			p.setAutoFoundDays(null);
		}

		//募集期满后最晚成立日
		p.setFoundDays(0);


		p.setRaiseStartDate(null);

		p.setRaisedTotalNumber(BigDecimal.ZERO);
		p.setMaxHold(BigDecimal.valueOf(99999999999.00));
		p.setMaxSaleVolume(BigDecimal.ZERO);

		p.setInvestMin(form.getInvestMin());
		p.setInvestMax(null);
		p.setInvestAdditional(BigDecimal.valueOf(0.01));
		p.setInvestDateType("T");

		p.setNetUnitShare(BigDecimal.ONE);

		p.setDealStartTime(null);
		p.setDealEndTime(null);

		p.setIsOpenPurchase(Product.YES);
		p.setIsOpenRemeed(Product.NO);
		//投资标的
		p.setInvestComment(StringUtil.empty2Null(form.getInvestComment()));
		//产品说明
		p.setInstruction(StringUtil.empty2Null(form.getInstruction()));
		//风险等级
		p.setRiskLevel(form.getRiskLevel());
		//投资者类型
		p.setInvestorLevel(form.getInvestorLevel());
		p.setStems(Product.STEMS_Userdefine);
		//审核状态
		p.setAuditState(Product.AUDIT_STATE_Nocommit);
		// 其他字段 初始化默认值s
		p.setOperator(operator);
		p.setUpdateTime(DateUtil.getSqlCurrentDate());
		p.setCreateTime(DateUtil.getSqlCurrentDate());

		// 附件文件
		List<SaveFileForm> fileForms = null;
		String fkey = StringUtil.uuid();
		if (StringUtil.isEmpty(form.getFiles())) {
			p.setFileKeys(StringUtil.EMPTY);
		} else {
			p.setFileKeys(fkey);
			fileForms = JSON.parseArray(form.getFiles(), SaveFileForm.class);
		}

		// 投资协议书
		List<SaveFileForm> investFileForm = null;
		String investFileKey = StringUtil.uuid();
		if (StringUtil.isEmpty(form.getInvestFile())) {
			p.setInvestFileKey(StringUtil.EMPTY);
		} else {
			p.setInvestFileKey(investFileKey);
			investFileForm = JSON.parseArray(form.getInvestFile(), SaveFileForm.class);
		}

		// 信息服务协议
		List<SaveFileForm> serviceFileForm = null;

		String serviceFileKey = StringUtil.uuid();
		if (StringUtil.isEmpty(form.getServiceFile())) {
			p.setServiceFileKey(StringUtil.EMPTY);
		} else {
			p.setServiceFileKey(serviceFileKey);
			serviceFileForm = JSON.parseArray(form.getServiceFile(), SaveFileForm.class);
		}
		//产品详情相关
		//产品要素的ID
		p.setProductElement(form.getProductElement());
		//产品说明ID
		p.setProductIntro(form.getProductIntro());
		p.setIsActivityProduct(0);
		p.setPurchaseApplyStatus(Product.APPLY_STATUS_None);
		p.setRedeemApplyStatus(Product.APPLY_STATUS_None);
		p.setBasicRatio(p.getExpAror());
		p.setFastRedeemStatus(Product.NO);
		p.setFastRedeemMax(BigDecimal.ZERO);
		p.setFastRedeemLeft(BigDecimal.ZERO);

		/**
		 * 账务账户ID
		 */
		String memeberId = accmentService.createSPVTnAccount(p);
		accmentService.createSPVTnLXAccount(p);
		p.setMemberId(memeberId);

		List<String> labelOids = new ArrayList<String>();
		if(!StringUtil.isEmpty(form.getBasicProductLabel())) {//基础标签
			labelOids.add(form.getBasicProductLabel());
			//将lableCode存储到product表
			String labelCode = labelService.findLabelCodeByOid(form.getBasicProductLabel());
			p.setProductLabel(labelCode);
		}
		if(form.getExpandProductLabels()!=null && form.getExpandProductLabels().length>0) {//扩展标签
			for(String ex : form.getExpandProductLabels()) {
				labelOids.add(ex);
			}
		}
		p = this.productDao.save(p);


		// 附件文件
		this.fileService.save(fileForms, fkey, File.CATE_User, operator);
		// 投资协议书
		this.fileService.save(investFileForm, investFileKey, File.CATE_User, operator);
		// 信息服务协议
		this.fileService.save(serviceFileForm, serviceFileKey, File.CATE_User, operator);

		this.productLabelService.saveAndFlush(p, labelOids);

		return response;
	}


	/**
	 *
	 *日切创建04产品
	 * @author yujianlong
	 * @date 2018/3/22 14:31
	 * @param [virtualProduct, publisherHoldList]
	 * @return void
	 */
	@Transactional(TxType.REQUIRES_NEW)
	public void createProduct04(Product virtualProduct,List<PublisherHoldEntity> publisherHoldList){

	}

	/**
	 * 产品包首次审核通过后新建属于本产品包的第一个产品
	 */
	@Transactional
	public void createProductFromPackage(ProductPackage productPackage){
		logger.info("====create product from productPackage begin====product is:{}", productPackage.getOid());
		
		Product p = newProduct();
		p.setProductPackage(productPackage);
		p.setOid(StringUtil.uuid());
		//竞猜活动标签
		p.setGuess(productPackage.getGuess());
		//竞猜活动标签
		String toProductNum = productPackage.getToProductNum()+1+"";
		if (productPackage.getProductCount().equals(1)) {
			// 产品包如果只发一个产品 那么不加数量后缀
			p.setCode(productPackage.getCode());
			p.setName(productPackage.getName());
			p.setFullName(productPackage.getFullName());
		} else {
			p.setCode(productPackage.getCode() + changeNumber(toProductNum)); //产品编号  产品编号取“产品包编号＋000x”
			p.setName(productPackage.getName() + toProductNum + "期"); //产品简称  产品简称取“产品包简称＋x期”
			p.setFullName(productPackage.getFullName() + toProductNum + "期"); //产品全称  产品全称取“产品包全称＋x期”
		}
		p.setAdministrator(productPackage.getAdministrator()); //产品管理人
		p.setState(Product.STATE_Raising);
		p.setIsDeleted(Product.NO);
		p.setRepayInterestStatus(Product.PRODUCT_repayInterestStatus_toRepay);//付息状态
		p.setRepayLoanStatus(Product.PRODUCT_repayLoanStatus_toRepay);//还本状态
		// 产品类型
		Dict assetType = this.dictService.get(productPackage.getType().getOid());
		p.setType(assetType);
		p.setAssetPool(productPackage.getAssetPool());//资产池
		p.setPublisherBaseAccount(productPackage.getAssetPool().getSpvEntity());//发行人账户
		p.setReveal(productPackage.getReveal());//额外增信
		if (ProductPackage.YES.equals(productPackage.getReveal())) {
			p.setRevealComment(StringUtil.empty2Null(productPackage.getRevealComment()));//增信备注
		}
		p.setCurrency(productPackage.getCurrency());//币种
		p.setIncomeCalcBasis(productPackage.getIncomeCalcBasis());//收益计算基础
		p.setOperationRate(productPackage.getOperationRate());//平台运营费率
		
		// 年化收益
		p.setExpAror(productPackage.getExpAror());
		if (null == productPackage.getExpArorSec() || productPackage.getExpArorSec().compareTo(BigDecimal.ZERO) == 0) {
			p.setExpArorSec(p.getExpAror());
		} else {
			p.setExpArorSec(productPackage.getExpArorSec());
		}
		p.setRewardInterest(productPackage.getRewardInterest());
				
		p.setRaiseStartDateType(productPackage.getRaiseStartDateType());
		
		//产品的募集期  = 产品包募集期  - 产品包募集开始日期到本产品的上架时间的间隔天数 - 1
		Date now = DateUtil.parseToSqlDate(new SimpleDateFormat(DateUtil.datePattern).format(new java.util.Date()));
		int days = DateUtil.getDaysBetweenTwoDate(productPackage.getRaiseStartDate(), now);
		logger.info("===now===" + now + "===days===" + days);
		p.setRaisePeriodDays(productPackage.getRaisePeriodDays() - days); //募集期 
		
		p.setInterestsFirstDays(productPackage.getInterestsFirstDays()); //起息期(成立后)
		
		p.setDurationPeriodDays(productPackage.getDurationPeriodDays()); // 存续期(成立后)
		
		p.setAccrualRepayDays(productPackage.getAccrualRepayDays()); // 还本付息日
		
		//募集期预期年化收益
		p.setRecPeriodExpAnYield(productPackage.getRecPeriodExpAnYield());
		
		//认购确认日:认购订单提交后()个日内 
		p.setPurchaseConfirmDays(productPackage.getRedeemConfirmDays());
		
		//募集满额后是否自动触发成立
		p.setRaiseFullFoundType(productPackage.getRaiseFullFoundType());
		
		//募集满额后第()个自然日后自动成立
		if (Product.RAISE_FULL_FOUND_TYPE_AUTO.equals(p.getRaiseFullFoundType())) {
			p.setAutoFoundDays(productPackage.getAutoFoundDays());
		}
	
		//募集期满后最晚成立日
		p.setFoundDays(productPackage.getFoundDays());
		
		
		// 募集开始日期,募集结束日期
		p.setRaiseStartDate(DateUtil.getSqlDate());//募集开始日期

		p.setRaiseEndDate(productPackage.getRaiseEndDate());// 募集结束时间

		Date setupDate = DateUtil.addSQLDays(productPackage.getRaiseEndDate(), productPackage.getFoundDays());
		p.setSetupDate(setupDate);// 最晚产品成立时间

		Date durationPeriodEndDate = DateUtil.addSQLDays(setupDate, productPackage.getDurationPeriodDays() - 1);
		p.setDurationPeriodEndDate(durationPeriodEndDate);// 存续期结束时间

		// 到期最晚还本付息日 指存续期结束后的还本付息最迟发生在存续期后的第X个自然日的23:59:59为止
		Date repayDate = DateUtil.addSQLDays(durationPeriodEndDate, productPackage.getAccrualRepayDays());
		// 到期最晚还本付息日 指存续期结束后的还本付息最迟发生在存续期后的第X个自然日的23:59:59为止
		p.setRepayDate(repayDate);// 到期还款时间
		
		p.setRaisedTotalNumber(productPackage.getSingleProductVolume());
		p.setMaxSaleVolume(productPackage.getSingleProductVolume());
		
		p.setInvestMin(productPackage.getInvestMin());
		p.setInvestMax(productPackage.getInvestMax());
		p.setInvestAdditional(productPackage.getInvestAdditional());
		p.setInvestDateType(productPackage.getInvestDateType());
		
		p.setNetUnitShare(productPackage.getNetUnitShare());
		
		p.setDealStartTime(productPackage.getDealStartTime());
		p.setDealEndTime(productPackage.getDealEndTime());
		
		p.setIsOpenPurchase(Product.YES);
		p.setIsOpenRemeed(Product.NO);
		p.setInvestComment(productPackage.getInvestComment());

		p.setInstruction(productPackage.getInstruction());
		p.setRiskLevel(productPackage.getRiskLevel());
		p.setInvestorLevel(productPackage.getInvestorLevel());
		p.setStems(Product.STEMS_Userdefine);
		p.setAuditState(Product.AUDIT_STATE_Reviewed);
		// 其他字段 初始化默认值s
		p.setOperator(productPackage.getOperator());
		p.setUpdateTime(DateUtil.getSqlCurrentDate());
		p.setCreateTime(DateUtil.getSqlCurrentDate());
		//产品详情相关
		p.setProductElement(productPackage.getProductElement());
		p.setProductIntro(productPackage.getProductIntro());
		p.setIsActivityProduct(productPackage.getIsActivityProduct());
		p.setActivityDetail(productPackage.getActivityDetail());
		p.setExpectedArrorDisp(productPackage.getExpectedArrorDisp());

		/**
		 * P2P
		 */
		p.setIfP2P(productPackage.getIfP2P());
		p.setIsP2PAssetPackage(productPackage.getIsP2PAssetPackage());

		// 附件文件
		if (StringUtil.isEmpty(productPackage.getFileKeys())) {
			p.setFileKeys(StringUtil.EMPTY);
		} else {
			p.setFileKeys(productPackage.getFileKeys());
		}

		// 投资协议书
		if (StringUtil.isEmpty(productPackage.getInvestFileKey())) {
			p.setInvestFileKey(StringUtil.EMPTY);
		} else {
			p.setInvestFileKey(productPackage.getInvestFileKey());
		}

		// 信息服务协议
		if (StringUtil.isEmpty(productPackage.getServiceFileKey())) {
			p.setServiceFileKey(StringUtil.EMPTY);
		} else {
			p.setServiceFileKey(productPackage.getServiceFileKey());
		}

		p.setPurchaseApplyStatus(Product.APPLY_STATUS_None);
		p.setRedeemApplyStatus(Product.APPLY_STATUS_None);
		p.setBasicRatio(BigDecimal.ZERO);
		p.setFastRedeemStatus(Product.NO);
		p.setFastRedeemMax(BigDecimal.ZERO);
		p.setFastRedeemLeft(BigDecimal.ZERO);
		
		/**
		 * 账务账户ID
		 */
		String memeberId = accmentService.createSPVTnAccount(p);
		accmentService.createSPVTnLXAccount(p);
		p.setMemberId(memeberId);
		
		List<String> labelOids = new ArrayList<String>();
		if(!StringUtil.isEmpty(productPackage.getProductLabel())) {//基础标签
			labelOids.add(productPackage.getProductLabel());
			//将lableCode存储到product表
			String labelCode = labelService.findLabelCodeByOid(productPackage.getProductLabel());
			p.setProductLabel(labelCode);
		}
		
		// 扩展标签
		if(!StringUtil.isEmpty(productPackage.getProductExpandLabel())) {//扩展标签
			String[] expandProductLabels = productPackage.getProductExpandLabel().split(",");
			for(int i = 0;i < expandProductLabels.length; i++){
				labelOids.add(expandProductLabels[i]);
			}
		}
		
		// 产品渠道
		List<String> channelOids = new ArrayList<String>();
		if(!StringUtil.isEmpty(productPackage.getChannelOid())) {//产品标签
			String[] channelOidArr = productPackage.getChannelOid().split(",");
			for(int i = 0;i < channelOidArr.length; i++){
				channelOids.add(channelOidArr[i]);
			}
		}
		
		//this.createSPVHold(p);
		
		/**
		 * 卡券相关
		 */
		p.setUseRedPackages(productPackage.getUseRedPackages());
		p.setUseraiseRateCoupons(productPackage.getUseraiseRateCoupons());
		
		p = this.productDao.save(p);
		
		// 新建产品包中产品的时候，新增渠道订单
		productChannelOrderService.saveChannelOrder(p.getOid(), productPackage.getChannelOid(), productPackage.getOperator());

		AssetPoolEntity assetPool = this.assetPoolService.getByOid(productPackage.getAssetPool().getOid());
		if (assetPool == null) {
			throw AMPException.getException(30001);
		}
		
		PublisherBaseAccountEntity spv = this.publisherBaseAccountDao.findOne(assetPool.getSpvEntity().getOid());
		
		PublisherHoldEntity hold = this.publisherHoldService.getAssetPoolSpvHold4reviewProduct(assetPool, spv, productPackage.getType().getOid());
		if (hold != null) {
			hold.setTotalVolume(productPackage.getSingleProductVolume());// 每个定期产品的持有总份额等于配置产品包时的单个产品的份额
			if (hold.getProduct() == null) {
				hold.setProduct(p);
				this.publisherHoldDao.saveAndFlush(hold);
				cacheSPVHoldService.createSPVHoldCache(hold);
			}
		}
		
		this.productLabelService.saveAndFlush(p, labelOids);
		this.productChannelService.saveAndFlush(p, channelOids, productPackage.getOperator());
		
		// 更新产品包的toProductNum字段，记录产品上架的个数，更新产品剩余份额（maxSaleVolume）
		productPackageDao.increaseToProductNum(productPackage.getOid(),productPackage.getSingleProductVolume());
		
		// 定期产品进入募集期时，增加产品发行数量
		publisherStatisticsService.increaseReleasedProductAmount(p.getPublisherBaseAccount());
		platformStatisticsService.increaseReleasedProductAmount();
		
		// 定期产品进入募集期时，增加在售产品数量
		publisherStatisticsService.increaseOnSaleProductAmount(p.getPublisherBaseAccount());
		platformStatisticsService.increaseOnSaleProductAmount();
		
		
		logger.info("====create product from productPackage end====product is:{}", productPackage.getOid());
	}
	
	/**
	 * 新建SPV持仓
	 * @return
	 */
	public void createSPVHold(Product p){
		AssetPoolEntity assetPool = this.assetPoolService.getByOid(p.getAssetPool().getOid());
		if (assetPool == null) {
			throw AMPException.getException(30001);
		}

		PublisherBaseAccountEntity spv = this.publisherBaseAccountDao.findOne(assetPool.getSpvEntity().getOid());

		PublisherHoldEntity hold = this.publisherHoldService.getAssetPoolSpvHold4reviewProduct(assetPool, spv, p.getType().getOid());
		if (hold != null) {
			hold.setTotalVolume(p.getRaisedTotalNumber());// 每个定期产品的持有总份额等于配置产品包时的单个产品的份额
			if (hold.getProduct() == null) {
				hold.setProduct(p);
				this.publisherHoldDao.saveAndFlush(hold);
				cacheSPVHoldService.createSPVHoldCache(hold);
			}
		}
	}

	private Product newProduct() {
		Product p = new Product();
		p.setManageRate(new BigDecimal(0));
		p.setFixedManageRate(new BigDecimal(0));
		p.setBasicRatio(new BigDecimal(0));
		p.setOperationRate(new BigDecimal(0));
		p.setPayModeDay(0);
		p.setRaisePeriodDays(0);
		p.setLockPeriodDays(0);
		p.setInterestsFirstDays(0);
		p.setDurationPeriodDays(0);
		p.setExpAror(new BigDecimal(0));
		p.setExpArorSec(new BigDecimal(0));
		p.setRaisedTotalNumber(new BigDecimal(0));
		p.setNetUnitShare(new BigDecimal(0));
		p.setInvestMin(new BigDecimal(0));
		p.setInvestAdditional(new BigDecimal(0));
		p.setInvestMax(new BigDecimal(0));
		p.setMinRredeem(new BigDecimal(0));
		p.setNetMaxRredeemDay(new BigDecimal(0));
		p.setDailyNetMaxRredeem(new BigDecimal(0));
		p.setAccrualRepayDays(0);
		p.setPurchaseConfirmDays(0);
		p.setRedeemConfirmDays(0);
		p.setRedeemTimingTaskDays(0);
		p.setPurchaseNum(0);
		p.setCurrentVolume(new BigDecimal(0));
		p.setCollectedVolume(new BigDecimal(0));
		p.setLockCollectedVolume(new BigDecimal(0));
		p.setMaxSaleVolume(new BigDecimal(0));
		p.setIsAutoAssignIncome(Product.NO);
		return p;

	}

	@Transactional
	public BaseResp saveCurrent(SaveCurrentProductForm form, String operator) {
		BaseResp response = new BaseResp();

		Timestamp now = new Timestamp(System.currentTimeMillis());

		Product product = this.newProduct();
		product.setOid(StringUtil.uuid());
		product.setCode(form.getCode()); // 产品编号
		product.setName(form.getName()); // 产品简称
		product.setFullName(form.getFullName()); // 产品全称
		product.setAdministrator(form.getAdministrator()); // 产品管理人
		product.setState(Product.STATE_Create); // 产品状态
		product.setIsDeleted(Product.NO); // 是否已删除

		// 产品类型
		Dict type = this.dictService.get(form.getTypeOid());
		product.setType(type);

		// 收益结转周期
		product.setAccrualCycleOid(form.getAccrualCycleOid());

		// 所属资产池
		AssetPoolEntity assetPool = assetPoolDao.findOne(form.getAssetPoolOid());
		if (null == assetPool) {
			throw new AMPException("所属资产池不能为空");
		}
		product.setAssetPool(assetPool);
		product.setPublisherBaseAccount(publisherBaseAccountService.findOne(assetPool.getSpvEntity().getOid()));

		product.setReveal(form.getReveal()); //额外增信
		if (Product.YES.equals(form.getReveal())) {
			product.setRevealComment(StringUtil.empty2Null(form.getRevealComment()));
		}
		product.setCurrency(form.getCurrency()); //币种
		product.setIncomeCalcBasis(form.getIncomeCalcBasis()); //收益计算基础
		product.setOperationRate(DecimalUtil.zoomIn(form.getOperationRate(), 100));
		
		
		product.setSetupDateType(form.getSetupDateType()); // 产品成立时间类型
		product.setInterestsFirstDays(form.getInterestsDate()); // 起息日
		product.setLockPeriodDays(form.getLockPeriod());// 锁定期
		product.setPurchaseConfirmDays(form.getPurchaseConfirmDate()); //申购确认日
		product.setRedeemConfirmDays(form.getRedeemConfirmDate()); //赎回确认日
		// 产品成立时间（存续期开始时间）
		if (Product.DATE_TYPE_ManualInput.equals(form.getSetupDateType())) {
			product.setSetupDate(DateUtil.getSqlDate());
		}
		// 年化收益
		product.setExpAror(DecimalUtil.zoomIn(form.getExpAror(), 100));
		if (null == form.getExpArorSec() 
				|| form.getExpArorSec().compareTo(BigDecimal.ZERO) == 0) {
			product.setExpArorSec(product.getExpAror());
		} else {
			product.setExpArorSec(DecimalUtil.zoomIn(form.getExpArorSec(), 100));
		}
		/**
		 * 产品详情相关
		 */
		product.setProductElement(form.getProductElement());
		product.setProductIntro(form.getProductIntro());
		product.setIsActivityProduct(form.getIsActivityProduct());
		if(form.getIsActivityProduct() == 1){
			//如果是活动产品，则设置折合年化收益率跟产品详情
			product.setActivityDetail(form.getActivityDetail());
			product.setExpectedArrorDisp(DecimalUtil.zoomIn(form.getExpectedArrorDisp(), 100));
		}
		product.setInvestMin(form.getInvestMin()); // 单笔投资最低份额
		product.setInvestMax(form.getInvestMax()); // 单笔投资追加份额
		product.setInvestAdditional(form.getInvestAdditional()); // 单笔投资最高份额
		product.setInvestDateType(form.getInvestDateType()); //有效投资日类型
		product.setIsOpenPurchase(Product.YES); //申购开关
		
		
		product.setNetMaxRredeemDay(form.getNetMaxRredeemDay()); //// 单日净赎回上限
		product.setDailyNetMaxRredeem(form.getNetMaxRredeemDay());
		product.setMinRredeem(form.getMinRredeem()); // 单笔净赎回下限
		product.setMaxRredeem(form.getMaxRredeem()); // 单笔净赎回上限
		product.setAdditionalRredeem(form.getAdditionalRredeem()); ////单笔赎回递增份额
		product.setRredeemDateType(form.getRredeemDateType());  //有效赎回日类型
		product.setIsOpenRedeemConfirm(Product.YES);// 是否屏蔽赎回确认
		product.setSingleDailyMaxRedeem(form.getSingleDailyMaxRedeem());// 单人单日赎回上限
		product.setIsOpenRemeed(Product.YES);  //赎回开关
		product.setFastRedeemStatus(Product.NO); //快赎开关
		product.setFastRedeemMax(BigDecimal.ZERO); //快赎阈值
		product.setFastRedeemLeft(BigDecimal.ZERO); //快赎剩余
		product.setSingleDayRedeemCount(form.getSingleDayRedeemCount());
		
		product.setNetUnitShare(form.getNetUnitShare());
		product.setMaxHold(form.getMaxHold()); //单人持有份额上限
		product.setDealStartTime(StringUtil.empty2Null(form.getDealStartTime()));
		product.setDealEndTime(StringUtil.empty2Null(form.getDealEndTime()));
		product.setInvestComment(StringUtil.empty2Null(form.getInvestComment())); // 投资标的
		product.setInstruction(StringUtil.empty2Null(form.getInstruction())); // 产品说明
		product.setRiskLevel(form.getRiskLevel()); // 风险等级
		product.setInvestorLevel(form.getInvestorLevel()); // 投资者类型
		product.setStems(Product.STEMS_Userdefine);
		product.setAuditState(Product.AUDIT_STATE_Nocommit); // 未提交审核
		// 其他字段 初始化默认值s
		product.setOperator(operator); //操作人
		product.setUpdateTime(now); //更新时间
		product.setCreateTime(now); //创建时间
		// 附件文件
		List<SaveFileForm> fileForms = null;
		String fkey = StringUtil.uuid();
		if (StringUtil.isEmpty(form.getFiles())) {
			product.setFileKeys(StringUtil.EMPTY);
		} else {
			product.setFileKeys(fkey);
			fileForms = JSON.parseArray(form.getFiles(), SaveFileForm.class);
		}

		// 投资协议书
		List<SaveFileForm> investFileForm = null;
		String investFileKey = StringUtil.uuid();
		if (StringUtil.isEmpty(form.getInvestFile())) {
			product.setInvestFileKey(StringUtil.EMPTY);
		} else {
			product.setInvestFileKey(investFileKey);
			investFileForm = JSON.parseArray(form.getInvestFile(), SaveFileForm.class);
		}

		// 信息服务协议
		List<SaveFileForm> serviceFileForm = null;
		String serviceFileKey = StringUtil.uuid();
		if (StringUtil.isEmpty(form.getServiceFile())) {
			product.setServiceFileKey(StringUtil.EMPTY);
		} else {
			product.setServiceFileKey(serviceFileKey);
			serviceFileForm = JSON.parseArray(form.getServiceFile(), SaveFileForm.class);
		}

		product.setPurchaseApplyStatus(Product.APPLY_STATUS_None);
		product.setRedeemApplyStatus(Product.APPLY_STATUS_None);
		product.setMaxSaleVolume(BigDecimal.ZERO);
		product.setBasicRatio(BigDecimal.ZERO);
		
		/**
		 * 账务账户ID
		 */
		String memeberId = accmentService.createSPVTnAccount(product);
		accmentService.createSPVTnLXAccount(product);
		product.setMemberId(memeberId);
		/**
		 * 卡券相关
		 */
		product.setUseRedPackages(form.getUseRedPackages());
		product.setUseraiseRateCoupons(form.getUseraiseRateCoupons());
		product = this.productDao.save(product);
		/**
		 * 新建卡券产品关联表
		 */
		productCouponService.saveEntity(product.getOid(),form.getRedPackages(),form.getRaiseRateCoupons());

		// 附件文件
		this.fileService.save(fileForms, fkey, File.CATE_User, operator);
		// 投资协议书
		this.fileService.save(investFileForm, investFileKey, File.CATE_User, operator);
		// 信息服务协议
		this.fileService.save(serviceFileForm, serviceFileKey, File.CATE_User, operator);

		List<String> labelOids = new ArrayList<String>();
		if (!StringUtil.isEmpty(form.getBasicProductLabel())) {// 基础标签
			labelOids.add(form.getBasicProductLabel());
		}
		if (form.getExpandProductLabels() != null && form.getExpandProductLabels().length > 0) {// 扩展标签
			for (String ex : form.getExpandProductLabels()) {
				labelOids.add(ex);
			}
		}
		this.productLabelService.saveAndFlush(product, labelOids);

		return response;
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

	@Transactional
	public Product delete(String oid, String operator) {
		Product product = this.getProductByOid(oid);

		// 面向渠道的属性，同一产品在不同渠道可以为不同状态，同一产品，只要有一个渠道销售状态为“渠道X待上架”，该产品已录入详情字段就不可修改。
		List<ProductChannel> pcs = this.productChannelService.queryProductChannels(oid);
		if (pcs != null && pcs.size() > 0) {
			for (ProductChannel pc : pcs) {
				if (ProductChannel.MARKET_STATE_Shelfing.equals(pc.getMarketState()) || ProductChannel.MARKET_STATE_Onshelf.equals(pc.getMarketState())) {
					throw AMPException.getException(90007);
				}
			}
		}

		{
			product.setIsDeleted(Product.YES);
			// 其它：修改时间、操作人
			product.setOperator(operator);
			product.setUpdateTime(new Timestamp(System.currentTimeMillis()));
		}
		product = this.productDao.saveAndFlush(product);
		// 删除渠道和商品关联关系
		// productChannelDao.deleteByProductOid(oid);

		return product;
	}

	/**
	 * 进行更新产品信息的操作，同时记录并存储日志
	 */
	@Transactional
	public BaseResp updatePeriodic(SavePeriodicProductForm form, String operator) {

		BaseResp response = new BaseResp();

		// 根据form中的productOid，从数据库得到相应对象，之后进行为对象进行审核操作
		Product product = this.getProductByOid(form.getOid());
		// 当前时间
		Timestamp now = new Timestamp(System.currentTimeMillis());

		// 未提交审核的可修改
		if (!Product.AUDIT_STATE_Nocommit.equals(product.getAuditState())) {
			throw AMPException.getException(90008);
		}
		// 判断是否可以修改 名称类型不变
		AssetPoolEntity assetPool = assetPoolDao.findOne(form.getAssetPoolOid());
		if (null == assetPool) {
			throw new AMPException("资产池不存在");
		}

		product.setPublisherBaseAccount(publisherDao.findOne(assetPool.getSpvEntity().getOid()));

		product.setAssetPool(assetPool);

		product.setCode(form.getCode());
		product.setName(form.getName());
		product.setFullName(form.getFullName());
		product.setAdministrator(form.getAdministrator());
		product.setState(Product.STATE_Update);
		
		product.setReveal(form.getReveal());
		if (Product.YES.equals(form.getReveal())) {
			product.setRevealComment(StringUtil.empty2Null(form.getRevealComment()));
		}
		product.setCurrency(form.getCurrency());
		product.setIncomeCalcBasis(form.getIncomeCalcBasis());
		product.setOperationRate(DecimalUtil.zoomIn(form.getOperationRate(), 100));

		// 年化收益
		product.setExpAror(DecimalUtil.zoomIn(form.getExpAror(), 100));
		if (null == form.getExpArorSec() || form.getExpArorSec().compareTo(BigDecimal.ZERO) == 0) {
			product.setExpArorSec(product.getExpAror());
		} else {
			product.setExpArorSec(DecimalUtil.zoomIn(form.getExpArorSec(), 100));
		}
		product.setRewardInterest(DecimalUtil.null2Zero(form.getRewardInterest()));
		// 募集开始时间类型;募集期:()个自然日;起息日:募集满额后()个自然日;存续期:()个自然日
		if (Product.DATE_TYPE_ManualInput.equals(form.getRaiseStartDateType())
				&& null == form.getRaiseStartDate()) {
			// error.define[90009]=请填写募集开始时间
			throw AMPException.getException(90009);
		}
		product.setRaiseStartDateType(form.getRaiseStartDateType());

		product.setRaisePeriodDays(form.getRaisePeriod());

		product.setInterestsFirstDays(form.getInterestsFirstDate());

		product.setDurationPeriodDays(form.getDurationPeriod());

		product.setAccrualRepayDays(form.getAccrualDate());

		// 募集期预期年化收益
		product.setRecPeriodExpAnYield(DecimalUtil.zoomIn(form.getRecPeriodExpAnYield(), 100));

		// 认购确认日:认购订单提交后()个日内
		product.setPurchaseConfirmDays(form.getSubscribeConfirmDays());

		// 募集满额后是否自动触发成立
		product.setRaiseFullFoundType(form.getRaiseFullFoundType());

		// 募集满额后第()个自然日后自动成立</span>
		if (Product.RAISE_FULL_FOUND_TYPE_AUTO.equals(form.getRaiseFullFoundType())) {
			int autoFoundDays =  Objects.isNull(form.getAutoFoundDays()) ? 0 :Integer.valueOf(form.getAutoFoundDays());
			product.setAutoFoundDays(autoFoundDays);
		}

		// 募集期满后最晚成立日
		product.setFoundDays(Integer.valueOf(form.getFoundDays()));
		/**
		 * 产品详情相关
		 */
		product.setProductElement(form.getProductElement());
		product.setProductIntro(form.getProductIntro());
		product.setActivityDetail(form.getActivityDetail());
		product.setIsActivityProduct(form.getIsActivityProduct());
		product.setExpectedArrorDisp(DecimalUtil.zoomIn(form.getExpectedArrorDisp(), 100));
		// 募集开始日期,募集结束日期
		if (Product.DATE_TYPE_ManualInput.equals(form.getRaiseStartDateType())) {
			product.setRaiseStartDate(form.getRaiseStartDate());

			Date raiseEndDate = DateUtil.addSQLDays(product.getRaiseStartDate(), form.getRaisePeriod() - 1);
			product.setRaiseEndDate(raiseEndDate);// 募集结束时间

			Date setupDate = DateUtil.addSQLDays(raiseEndDate, form.getFoundDays());
			product.setSetupDate(setupDate);// 最晚产品成立时间

			Date durationPeriodEndDate = DateUtil.addSQLDays(setupDate, form.getDurationPeriod() - 1);
			product.setDurationPeriodEndDate(durationPeriodEndDate);// 存续期结束时间

			// 到期最晚还本付息日 指存续期结束后的还本付息最迟发生在存续期后的第X个自然日的23:59:59为止
			Date repayDate = DateUtil.addSQLDays(durationPeriodEndDate, form.getAccrualDate());
			// 到期最晚还本付息日 指存续期结束后的还本付息最迟发生在存续期后的第X个自然日的23:59:59为止
			product.setRepayDate(repayDate);// 到期还款时间
		} else {
			product.setRaiseStartDate(null);
		}

		product.setRaisedTotalNumber(form.getRaisedTotalNumber());
		product.setMaxSaleVolume(form.getRaisedTotalNumber());

		product.setInvestMin(form.getInvestMin());

		product.setInvestMax(form.getInvestMax());

		product.setInvestAdditional(form.getInvestAdditional());

		product.setInvestDateType(form.getInvestDateType());

		product.setNetUnitShare(form.getNetUnitShare());

		product.setInvestComment(StringUtil.empty2Null(form.getInvestComment()));
		product.setInstruction(StringUtil.empty2Null(form.getInstruction()));
		product.setRiskLevel(form.getRiskLevel());
		product.setInvestorLevel(form.getInvestorLevel());
		// 其它：修改时间、操作人
		product.setOperator(operator);
		product.setUpdateTime(now);

		product.setDealStartTime(StringUtil.empty2Null(form.getDealStartTime()));
		product.setDealEndTime(StringUtil.empty2Null(form.getDealEndTime()));

		// 附件文件
		List<SaveFileForm> fileForms = null;
		if (!StringUtil.isEmpty(form.getFiles())) {
			fileForms = JSON.parseArray(form.getFiles(), SaveFileForm.class);
		}
		String fkey = null;
		if (StringUtil.isEmpty(product.getFileKeys())) {
			fkey = StringUtil.uuid();
			if (fileForms != null && fileForms.size() > 0) {
				product.setFileKeys(fkey);
			}
		} else {
			fkey = product.getFileKeys();
			if (fileForms == null || fileForms.size() == 0) {
				product.setFileKeys(StringUtil.EMPTY);
			}
		}

		// 投资协议书
		List<SaveFileForm> investFileForm = null;
		if (!StringUtil.isEmpty(form.getInvestFile())) {
			investFileForm = JSON.parseArray(form.getInvestFile(), SaveFileForm.class);
		}
		String investFileKey = null;
		if (StringUtil.isEmpty(product.getInvestFileKey())) {
			investFileKey = StringUtil.uuid();
			if (investFileForm != null && investFileForm.size() > 0) {
				product.setInvestFileKey(investFileKey);
			}
		} else {
			investFileKey = product.getInvestFileKey();
			if (investFileForm == null || investFileForm.size() == 0) {
				product.setInvestFileKey(StringUtil.EMPTY);
			}
		}
		// 信息服务协议
		List<SaveFileForm> serviceFileForm = null;
		if (!StringUtil.isEmpty(form.getServiceFile())) {
			serviceFileForm = JSON.parseArray(form.getServiceFile(), SaveFileForm.class);
		}
		String serviceFileKey = null;
		if (StringUtil.isEmpty(product.getServiceFileKey())) {
			serviceFileKey = StringUtil.uuid();
			if (serviceFileForm != null && serviceFileForm.size() > 0) {
				product.setServiceFileKey(serviceFileKey);
			}
		} else {
			serviceFileKey = product.getServiceFileKey();
			if (serviceFileForm == null || serviceFileForm.size() == 0) {
				product.setServiceFileKey(StringUtil.EMPTY);
			}
		}
		/**
		 * 卡券相关
		 */
		product.setUseRedPackages(form.getUseRedPackages());
		product.setUseraiseRateCoupons(form.getUseraiseRateCoupons());

		// 是否P2P
		product.setIfP2P(form.getIfP2P());
		// 产品分类
		product.setIsP2PAssetPackage(form.getIsP2PAssetPackage());
		// 更新产品
		product = this.productDao.saveAndFlush(product);
		/**
		 * 新建卡券产品关联表
		 */
		productCouponService.updateEntity(product.getOid(),form.getRedPackages(),form.getRaiseRateCoupons());

		{
			// 附件文件
			this.fileService.save(fileForms, fkey, File.CATE_User, operator);
			// 投资协议书
			this.fileService.save(investFileForm, investFileKey, File.CATE_User, operator);
			// 信息服务协议
			this.fileService.save(serviceFileForm, serviceFileKey, File.CATE_User, operator);
		}

		List<String> labelOids = new ArrayList<String>();
		if (!StringUtil.isEmpty(form.getBasicProductLabel())) {// 基础标签
			labelOids.add(form.getBasicProductLabel());
		}
		if (form.getExpandProductLabels() != null && form.getExpandProductLabels().length > 0) {// 扩展标签
			for (String ex : form.getExpandProductLabels()) {
				labelOids.add(ex);
			}
		}
		this.productLabelService.saveAndFlush(product, labelOids);

		return response;
	}

	/**
	 *
	 *进行更新循环开放产品信息的操作，同时记录并存储日志
	 * @author yujianlong
	 * @date 2018/3/19 17:07
	 * @param [form, operator]
	 * @return com.guohuai.component.web.view.BaseResp
	 */
	@Transactional
	public BaseResp updatePeriodic03(SavePeriodicProduct03Form form, String operator) {

		BaseResp response = new BaseResp();

		// 根据form中的productOid，从数据库得到相应对象，之后进行为对象进行审核操作
		Product product = this.getProductByOid(form.getOid());
		// 当前时间
		Timestamp now = new Timestamp(System.currentTimeMillis());

		// 未提交审核的可修改
		if (!Product.AUDIT_STATE_Nocommit.equals(product.getAuditState())) {
			throw AMPException.getException(90008);
		}
		// 判断是否可以修改 名称类型不变
		AssetPoolEntity assetPool = assetPoolDao.findOne(form.getAssetPoolOid());
		if (null == assetPool) {
			throw new AMPException("资产池不存在");
		}

		product.setPublisherBaseAccount(publisherDao.findOne(assetPool.getSpvEntity().getOid()));

		product.setAssetPool(assetPool);

		product.setCode(form.getCode());
		product.setName(form.getName());
		product.setFullName(form.getFullName());
		product.setAdministrator(form.getAdministrator());
		product.setState(Product.STATE_Update);
		/**
		 *循环开放相关
		 */
		product.setMovedupRedeemLockDays(form.getMovedupRedeemLockDays());
		product.setMovedupRedeemMinPay(form.getMovedupRedeemMinPay());
		product.setMovedupRedeemRate(form.getMovedupRedeemRate());


		product.setReveal(form.getReveal());
		if (Product.YES.equals(form.getReveal())) {
			product.setRevealComment(StringUtil.empty2Null(form.getRevealComment()));
		}
		product.setCurrency(form.getCurrency());
		product.setIncomeCalcBasis(form.getIncomeCalcBasis());

		// 年化收益
		product.setExpAror(DecimalUtil.zoomIn(form.getExpAror(), 100));
		product.setExpArorSec(product.getExpAror());

		product.setRaiseStartDateType(form.getRaiseStartDateType());

		product.setDurationPeriodDays(form.getDurationPeriod());

		/**
		 * 产品详情相关
		 */
		product.setProductElement(form.getProductElement());
		product.setProductIntro(form.getProductIntro());

		product.setInvestMin(form.getInvestMin());


		product.setInvestComment(StringUtil.empty2Null(form.getInvestComment()));
		product.setInstruction(StringUtil.empty2Null(form.getInstruction()));
		product.setRiskLevel(form.getRiskLevel());
		product.setInvestorLevel(form.getInvestorLevel());
		// 其它：修改时间、操作人
		product.setOperator(operator);
		product.setUpdateTime(now);


		// 附件文件
		List<SaveFileForm> fileForms = null;
		if (!StringUtil.isEmpty(form.getFiles())) {
			fileForms = JSON.parseArray(form.getFiles(), SaveFileForm.class);
		}
		String fkey = null;
		if (StringUtil.isEmpty(product.getFileKeys())) {
			fkey = StringUtil.uuid();
			if (fileForms != null && fileForms.size() > 0) {
				product.setFileKeys(fkey);
			}
		} else {
			fkey = product.getFileKeys();
			if (fileForms == null || fileForms.size() == 0) {
				product.setFileKeys(StringUtil.EMPTY);
			}
		}

		// 投资协议书
		List<SaveFileForm> investFileForm = null;
		if (!StringUtil.isEmpty(form.getInvestFile())) {
			investFileForm = JSON.parseArray(form.getInvestFile(), SaveFileForm.class);
		}
		String investFileKey = null;
		if (StringUtil.isEmpty(product.getInvestFileKey())) {
			investFileKey = StringUtil.uuid();
			if (investFileForm != null && investFileForm.size() > 0) {
				product.setInvestFileKey(investFileKey);
			}
		} else {
			investFileKey = product.getInvestFileKey();
			if (investFileForm == null || investFileForm.size() == 0) {
				product.setInvestFileKey(StringUtil.EMPTY);
			}
		}
		// 信息服务协议
		List<SaveFileForm> serviceFileForm = null;
		if (!StringUtil.isEmpty(form.getServiceFile())) {
			serviceFileForm = JSON.parseArray(form.getServiceFile(), SaveFileForm.class);
		}
		String serviceFileKey = null;
		if (StringUtil.isEmpty(product.getServiceFileKey())) {
			serviceFileKey = StringUtil.uuid();
			if (serviceFileForm != null && serviceFileForm.size() > 0) {
				product.setServiceFileKey(serviceFileKey);
			}
		} else {
			serviceFileKey = product.getServiceFileKey();
			if (serviceFileForm == null || serviceFileForm.size() == 0) {
				product.setServiceFileKey(StringUtil.EMPTY);
			}
		}
		// 更新产品
		product = this.productDao.saveAndFlush(product);

		{
			// 附件文件
			this.fileService.save(fileForms, fkey, File.CATE_User, operator);
			// 投资协议书
			this.fileService.save(investFileForm, investFileKey, File.CATE_User, operator);
			// 信息服务协议
			this.fileService.save(serviceFileForm, serviceFileKey, File.CATE_User, operator);
		}

		List<String> labelOids = new ArrayList<String>();
		if (!StringUtil.isEmpty(form.getBasicProductLabel())) {// 基础标签
			labelOids.add(form.getBasicProductLabel());
		}
		if (form.getExpandProductLabels() != null && form.getExpandProductLabels().length > 0) {// 扩展标签
			for (String ex : form.getExpandProductLabels()) {
				labelOids.add(ex);
			}
		}
		this.productLabelService.saveAndFlush(product, labelOids);

		return response;
	}

	/**
	 * 进行更新产品信息的操作，同时记录并存储日志
	 * 
	 * @param form
	 * @param operator
	 * @return
	 */
	@Transactional
	public BaseResp updateCurrent(SaveCurrentProductForm form, String operator) throws ParseException, Exception {
		// 根据form中的productOid，从数据库得到相应对象，之后进行为对象进行审核操作
		BaseResp response = new BaseResp();

		Product product = this.getProductByOid(form.getOid());

		// 当前时间
		Timestamp now = new Timestamp(System.currentTimeMillis());
		// 未提交审核的可修改
		if (!Product.AUDIT_STATE_Nocommit.equals(product.getAuditState())) {
			throw AMPException.getException(90008);
		}
		// 判断是否可以修改 名称类型不变
		
		AssetPoolEntity assetPool = assetPoolDao.findOne(form.getAssetPoolOid());
		if (null == assetPool) {
			throw new AMPException("资产池不存在");
		}
		product.setAssetPool(assetPool);
		product.setPublisherBaseAccount(this.publisherBaseAccountService.findOne(assetPool.getSpvEntity().getOid()));
		
		product.setCode(form.getCode());
		product.setName(form.getName());
		product.setFullName(form.getFullName());
		product.setAdministrator(form.getAdministrator());
		product.setState(Product.STATE_Update);
		product.setReveal(form.getReveal());
		if (Product.YES.equals(form.getReveal())) {
			product.setRevealComment(StringUtil.empty2Null(form.getRevealComment()));
		}
		product.setCurrency(form.getCurrency());
		product.setIncomeCalcBasis(form.getIncomeCalcBasis());

		// 收益结转周期
		product.setAccrualCycleOid(form.getAccrualCycleOid());
		
		product.setOperationRate(DecimalUtil.zoomIn(form.getOperationRate(), 100));
		
		// 年化收益
		product.setExpAror(DecimalUtil.zoomIn(form.getExpAror(), 100));
		if (null == form.getExpArorSec() || form.getExpArorSec().compareTo(BigDecimal.ZERO) == 0) {
			product.setExpArorSec(product.getExpAror());
		} else {
			product.setExpArorSec(DecimalUtil.zoomIn(form.getExpArorSec(), 100));
		}
		product.setSetupDateType(form.getSetupDateType());

		product.setInterestsFirstDays(Integer.valueOf(form.getInterestsDate()));

		product.setLockPeriodDays(Integer.valueOf(form.getLockPeriod()));

		product.setPurchaseConfirmDays(Integer.valueOf(form.getPurchaseConfirmDate()));

		product.setRedeemConfirmDays(form.getRedeemConfirmDate());

		if (Product.DATE_TYPE_FirstRackTime.equals(form.getSetupDateType())) {
			form.setSetupDate(null);
		} else {
			// 产品成立时间（存续期开始时间）
			java.util.Date setupDate = DateUtil.parseDate(form.getSetupDate(), DateUtil.datePattern);
			product.setSetupDate(new Date(setupDate.getTime()));
		}

		product.setInvestMin(form.getInvestMin());
		product.setInvestMax(form.getInvestMax());
		product.setInvestAdditional(form.getInvestAdditional());
		product.setInvestDateType(form.getInvestDateType());
		product.setNetUnitShare(form.getNetUnitShare());

		product.setNetMaxRredeemDay(form.getNetMaxRredeemDay());
		product.setDailyNetMaxRredeem(form.getNetMaxRredeemDay());
		product.setMinRredeem(form.getMinRredeem());// 单笔赎回下限
		product.setMaxRredeem(form.getMaxRredeem());
		product.setAdditionalRredeem(form.getAdditionalRredeem());
		product.setRredeemDateType(form.getRredeemDateType());
		product.setSingleDailyMaxRedeem(form.getSingleDailyMaxRedeem());// 单人单日赎回上限
		product.setSingleDayRedeemCount(form.getSingleDayRedeemCount()); //单人单日赎回次数
		
		product.setDealStartTime(StringUtil.empty2Null(form.getDealStartTime()));
		product.setDealEndTime(StringUtil.empty2Null(form.getDealEndTime()));
		product.setMaxHold(form.getMaxHold());
		product.setInvestComment(StringUtil.empty2Null(form.getInvestComment()));
		product.setInstruction(StringUtil.empty2Null(form.getInstruction()));
		product.setRiskLevel(form.getRiskLevel());
		product.setInvestorLevel(form.getInvestorLevel());
		// 其它：修改时间、操作人
		product.setOperator(operator);
		product.setUpdateTime(now);

		/**
		 * 产品详情相关
		 */
		product.setProductElement(form.getProductElement());
		product.setProductIntro(form.getProductIntro());
		product.setActivityDetail(form.getActivityDetail());
		product.setIsActivityProduct(form.getIsActivityProduct());
		product.setExpectedArrorDisp(DecimalUtil.zoomIn(form.getExpectedArrorDisp(), 100));

		// 附件文件
		List<SaveFileForm> fileForms = null;
		if (!StringUtil.isEmpty(form.getFiles())) {
			fileForms = JSON.parseArray(form.getFiles(), SaveFileForm.class);
		}
		String fkey = null;
		if (StringUtil.isEmpty(product.getFileKeys())) {
			fkey = StringUtil.uuid();
			if (fileForms != null && fileForms.size() > 0) {
				product.setFileKeys(fkey);
			}
		} else {
			fkey = product.getFileKeys();
			if (fileForms == null || fileForms.size() == 0) {
				product.setFileKeys(StringUtil.EMPTY);
			}
		}
		// 投资协议书
		List<SaveFileForm> investFileForm = null;
		if (!StringUtil.isEmpty(form.getInvestFile())) {
			investFileForm = JSON.parseArray(form.getInvestFile(), SaveFileForm.class);
		}
		String investFileKey = null;
		if (StringUtil.isEmpty(product.getInvestFileKey())) {
			investFileKey = StringUtil.uuid();
			if (investFileForm != null && investFileForm.size() > 0) {
				product.setInvestFileKey(investFileKey);
			}
		} else {
			investFileKey = product.getInvestFileKey();
			if (investFileForm == null || investFileForm.size() == 0) {
				product.setInvestFileKey(StringUtil.EMPTY);
			}
		}

		// 信息服务协议
		List<SaveFileForm> serviceFileForm = null;
		if (!StringUtil.isEmpty(form.getServiceFile())) {
			serviceFileForm = JSON.parseArray(form.getServiceFile(), SaveFileForm.class);
		}
		String serviceFileKey = null;
		if (StringUtil.isEmpty(product.getServiceFileKey())) {
			serviceFileKey = StringUtil.uuid();
			if (serviceFileForm != null && serviceFileForm.size() > 0) {
				product.setServiceFileKey(serviceFileKey);
			}
		} else {
			serviceFileKey = product.getServiceFileKey();
			if (serviceFileForm == null || serviceFileForm.size() == 0) {
				product.setServiceFileKey(StringUtil.EMPTY);
			}
		}
		/**
		 * 卡券相关
		 */
		product.setUseRedPackages(form.getUseRedPackages());
		product.setUseraiseRateCoupons(form.getUseraiseRateCoupons());
		// 更新产品
		product = this.productDao.saveAndFlush(product);
		/**
		 * 新建卡券产品关联表
		 */
		productCouponService.updateEntity(product.getOid(),form.getRedPackages(),form.getRaiseRateCoupons());

		{
			// 附件文件
			this.fileService.save(fileForms, fkey, File.CATE_User, operator);
			// 投资协议书
			this.fileService.save(investFileForm, investFileKey, File.CATE_User, operator);
			// 信息服务协议
			this.fileService.save(serviceFileForm, serviceFileKey, File.CATE_User, operator);
		}

		List<String> labelOids = new ArrayList<String>();
		if (!StringUtil.isEmpty(form.getBasicProductLabel())) {// 基础标签
			labelOids.add(form.getBasicProductLabel());
		}
		if (form.getExpandProductLabels() != null && form.getExpandProductLabels().length > 0) {// 扩展标签
			for (String ex : form.getExpandProductLabels()) {
				labelOids.add(ex);
			}
		}
		this.productLabelService.saveAndFlush(product, labelOids);
		return response;
	}

	/**
	 * 产品详情
	 */
	@Transactional
	public ProductDetailResp read(String oid) {
		Product product = this.getProductByOid(oid);
		ProductDetailResp pr;
		String productEleTitle = null;
		String productIntroTitle = null;
		String productActivityTitle = null;
		String productEleOid = null;
		String productIntroOid = null;
		String productActivityOid = null;
		if(StringUtils.isNotEmpty(product.getProductElement())) {
			ProductTypeDetail productElementObj = productTypeDetailService.getOne(product.getProductElement());
			if(!ObjectUtils.isEmpty(productElementObj)) {
				productEleTitle = productElementObj.getTitle();
				productEleOid = productElementObj.getOid();
			}
		}
		if(StringUtils.isNotEmpty(product.getProductIntro())) {
			ProductTypeDetail productIntroObj = productTypeDetailService.getOne(product.getProductIntro());
			if(!ObjectUtils.isEmpty(productIntroObj)) {
				productIntroTitle = productIntroObj.getTitle();
				productIntroOid = productIntroObj.getOid();
			}
		}
		if(StringUtils.isNotEmpty(product.getActivityDetail())) {
			ProductTypeDetail activityDetailObj = productTypeDetailService.getOne(product.getActivityDetail());
			if(!ObjectUtils.isEmpty(activityDetailObj)) {
				productActivityTitle = activityDetailObj.getTitle();
				productActivityOid = activityDetailObj.getOid();
			}
		}
		pr = new ProductDetailResp(product,productEleOid,productIntroOid,productActivityOid,productEleTitle,productIntroTitle,productActivityTitle);

		if (product.getAssetPool() != null && product.getAssetPool().getSpvEntity() != null) {
			Corporate corporate = this.corporateDao.findOne(product.getAssetPool().getSpvEntity().getCorperateOid());
			if(corporate!=null) {
				pr.setSpvName(corporate.getName());
			}
		}
		Map<String, AdminObj> adminObjMap = new HashMap<String, AdminObj>();

		FileResp fr = null;
		AdminObj adminObj = null;
		if (!StringUtil.isEmpty(product.getFileKeys())) {
			List<File> files = this.fileService.list(product.getFileKeys(), File.STATE_Valid);
			if (files.size() > 0) {
				pr.setFiles(new ArrayList<FileResp>());

				for (File file : files) {
					fr = new FileResp(file);
					if (adminObjMap.get(file.getOperator()) == null) {
						try {
							adminObj = adminSdk.getAdmin(file.getOperator());
							adminObjMap.put(file.getOperator(), adminObj);
						} catch (Exception e) {
						}
					}
					if (adminObjMap.get(file.getOperator()) != null) {
						fr.setOperator(adminObjMap.get(file.getOperator()).getName());
					}
					pr.getFiles().add(fr);
				}
			}
		}

		if (!StringUtil.isEmpty(product.getInvestFileKey())) {
			List<File> files = this.fileService.list(product.getInvestFileKey(), File.STATE_Valid);
			if (files.size() > 0) {
				pr.setInvestFiles(new ArrayList<FileResp>());

				for (File file : files) {
					fr = new FileResp(file);
					if (adminObjMap.get(file.getOperator()) == null) {
						try {
							adminObj = adminSdk.getAdmin(file.getOperator());
							adminObjMap.put(file.getOperator(), adminObj);
						} catch (Exception e) {
						}
					}
					if (adminObjMap.get(file.getOperator()) != null) {
						fr.setOperator(adminObjMap.get(file.getOperator()).getName());
					}
					pr.getInvestFiles().add(fr);
				}
			}
		}

		if (!StringUtil.isEmpty(product.getServiceFileKey())) {
			List<File> files = this.fileService.list(product.getServiceFileKey(), File.STATE_Valid);
			if (files.size() > 0) {
				pr.setServiceFiles(new ArrayList<FileResp>());

				for (File file : files) {
					fr = new FileResp(file);
					if (adminObjMap.get(file.getOperator()) == null) {
						try {
							adminObj = adminSdk.getAdmin(file.getOperator());
							adminObjMap.put(file.getOperator(), adminObj);
						} catch (Exception e) {
						}
					}
					if (adminObjMap.get(file.getOperator()) != null) {
						fr.setOperator(adminObjMap.get(file.getOperator()).getName());
					}
					pr.getServiceFiles().add(fr);
				}
			}
		}

		List<ProductChannel> pcs = productChannelService.queryProductChannels(oid);
		if (pcs != null && pcs.size() > 0) {
			StringBuilder channelNames = new StringBuilder();
			List<String> channelOids = new ArrayList<String>();
			for (ProductChannel pc : pcs) {
				channelNames.append(pc.getChannel().getChannelName()).append(",");
				channelOids.add(pc.getChannel().getOid());
			}
			pr.setChannelNames(channelNames.substring(0, channelNames.length() - 1));
			pr.setChannelOids(channelOids);
		}

		List<ProductIncomeReward> pirs = productRewardService.productRewardList(oid);
		if (pirs != null && pirs.size() > 0) {
			List<ProductRewardResp> rewards = new ArrayList<ProductRewardResp>();
			for (ProductIncomeReward pir : pirs) {
				ProductRewardResp pirRep = new ProductRewardResp(pir);
				rewards.add(pirRep);
			}
			pr.setRewards(rewards);
		}
		
		List<ProductLabel> productLabels = productLabelService.findProductLabelsByProduct(product);
		if(productLabels!=null && productLabels.size()>0) {
			List<String> exOids = new ArrayList<String>();
			List<String> exNames = new ArrayList<String>();
			for(ProductLabel pl : productLabels) {
				if(LabelEntity.labelType_general.equals(pl.getLabel().getLabelType())) {
					pr.setBasicProductLabelOid(pl.getLabel().getOid());
					pr.setBasicProductLabelName(pl.getLabel().getLabelName());
				} else if(LabelEntity.labelType_extend.equals(pl.getLabel().getLabelType())) {
					exOids.add(pl.getLabel().getOid());
					exNames.add(pl.getLabel().getLabelName());
				}
			}
			if(exOids.size()>0) {
				pr.setExpandProductLabelOids(exOids.toArray(new String[exOids.size()]));
				pr.setExpandProductLabelNames(exNames.toArray(new String[exOids.size()]));
			}
			
		}
		//卡券相关
		pr.setUsered(product.getUseRedPackages()==1?"可以使用部分红包":(product.getUseRedPackages()==2?"不能使用红包":"可以使用所有的红包"));
		pr.setUseRaise(product.getUseraiseRateCoupons()==1?"可以使用部分加息券":(product.getUseraiseRateCoupons()==2?"不能使用加息券":"可以使用所有的加息券"));
		pr.setUseredId(product.getUseRedPackages());
		pr.setUseRaiseId(product.getUseraiseRateCoupons());
		if(product.getUseRedPackages()==1){
			List<Integer> redOids = productCouponService.getRedListByProductOid(product.getOid());
			pr.setRedPackageOids(redOids.toArray(new Integer[redOids.size()]));
			List<Object> redNames = tulipSdk.getCardNames(redOids);
			pr.setRedPackageNames(redNames.toArray(new String[redNames.size()]));
		}
		if(product.getUseraiseRateCoupons()==1){
			List<Integer> raiseOids = productCouponService.getRaiseListByProductOid(product.getOid());
			pr.setRaiseRateCouponOids(raiseOids.toArray(new Integer[raiseOids.size()]));
			List<Object> raiseNames = tulipSdk.getCardNames(raiseOids);
			pr.setRaiseRateCouponNames(raiseNames.toArray(new String[raiseNames.size()]));
		}
		
		return pr;
	}

	/**
	 * 查询
	 * 
	 * @param spec
	 * @param pageable
	 * @return {@link PagesRep<ProductResp>} ,如果返回的errCode属性等于0表示成功，否则表示失败，失败原因在errMessage里面体现
	 */
	public PageResp<ProductResp> list(Specification<Product> spec, Pageable pageable) {
		Page<Product> cas = this.productDao.findAll(spec, pageable);
		PageResp<ProductResp> pagesRep = new PageResp<ProductResp>();
		if (cas != null && cas.getContent() != null && cas.getTotalElements() > 0) {
			List<ProductResp> rows = new ArrayList<ProductResp>();

			Map<String, List<ProductIncomeReward>> incomeRewardNum = this.getProductRewards(cas.getContent());

			Map<String, Corporate> aoprateObjMap = new HashMap<String, Corporate>();
			Corporate corporate = null;
			for (Product p : cas) {
				ProductResp queryRep = new ProductResp(p);
				if (p.getAssetPool() != null && p.getAssetPool().getSpvEntity()!=null) {
					if (aoprateObjMap.get(p.getAssetPool().getSpvEntity().getCorperateOid()) == null) {
						try {
							corporate = this.corporateDao.findOne(p.getAssetPool().getSpvEntity().getCorperateOid());
							if(corporate!=null) {
								aoprateObjMap.put(p.getAssetPool().getSpvEntity().getCorperateOid(), corporate);
							}
						} catch (Exception e) {
						}
					}
					if (aoprateObjMap.get(p.getAssetPool().getSpvEntity().getCorperateOid()) != null) {
						queryRep.setSpvName(aoprateObjMap.get(p.getAssetPool().getSpvEntity().getCorperateOid()).getName());
					}
				}
				if (incomeRewardNum.get(p.getOid()) != null) {
					queryRep.setRewardNum(incomeRewardNum.get(p.getOid()).size());
				} else {
					queryRep.setRewardNum(0);
				}

				rows.add(queryRep);
			}
			pagesRep.setRows(rows);
		}
		pagesRep.setTotal(cas.getTotalElements());
		return pagesRep;
	}

	private Map<String, List<ProductIncomeReward>> getProductRewards(List<Product> ps) {
		List<String> productOids = new ArrayList<String>();
		for (Product p : ps) {
			productOids.add(p.getOid());
		}
		Map<String, List<ProductIncomeReward>> incomeRewardNum = new HashMap<String, List<ProductIncomeReward>>();

		List<ProductIncomeReward> list = incomeRewardService.productsRewardList(productOids);

		if (list != null && list.size() > 0) {
			for (ProductIncomeReward reward : list) {
				if (incomeRewardNum.get(reward.getProduct().getOid()) == null) {
					incomeRewardNum.put(reward.getProduct().getOid(), new ArrayList<ProductIncomeReward>());
				}
				incomeRewardNum.get(reward.getProduct().getOid()).add(reward);
			}
		}
		return incomeRewardNum;
	}

	/**
	 * 查询审核中
	 * 
	 * @param spec
	 * @param pageable
	 * @return
	 */
	public PageResp<ProductLogListResp> auditList(Specification<Product> spec, Pageable pageable) {
		Page<Product> cas = this.productDao.findAll(spec, pageable);
		PageResp<ProductLogListResp> pagesRep = new PageResp<ProductLogListResp>();
		if (cas != null && cas.getContent() != null && cas.getTotalElements() > 0) {
			List<ProductLogListResp> rows = new ArrayList<ProductLogListResp>();

			Map<String, AdminObj> adminObjMap = new HashMap<String, AdminObj>();
			Map<String, Corporate> aoprateObjMap = new HashMap<String, Corporate>();

			List<String> oids = new ArrayList<String>();
			for (Product p : cas) {
				oids.add(p.getOid());
			}
			Map<String, ProductLog> plMap = this.getProductLogs(oids, ProductLog.AUDIT_TYPE_Auditing, ProductLog.AUDIT_STATE_Commited);
			ProductLog pl = null;

			Corporate corporate = null;
			AdminObj adminObj = null;
			for (Product p : cas) {
				ProductLogListResp queryRep = new ProductLogListResp(p);
				if (p.getAssetPool() != null && p.getAssetPool().getSpvEntity()!=null) {
					if (aoprateObjMap.get(p.getAssetPool().getSpvEntity().getCorperateOid()) == null) {
						try {
							corporate = this.corporateDao.findOne(p.getAssetPool().getSpvEntity().getCorperateOid());
							if(corporate!=null) {
								aoprateObjMap.put(p.getAssetPool().getSpvEntity().getCorperateOid(), corporate);
							}
						} catch (Exception e) {
						}
					}
					if (aoprateObjMap.get(p.getAssetPool().getSpvEntity().getCorperateOid()) != null) {
						queryRep.setSpvName(aoprateObjMap.get(p.getAssetPool().getSpvEntity().getCorperateOid()).getName());
					}
				}
				// 申请人和申请时间
				pl = plMap.get(p.getOid());

				if (pl != null) {
					if (adminObjMap.get(pl.getAuditor()) == null) {
						try {
							adminObj = adminSdk.getAdmin(pl.getAuditor());
							adminObjMap.put(pl.getAuditor(), adminObj);
						} catch (Exception e) {
						}

					}
					if (adminObjMap.get(pl.getAuditor()) != null) {
						queryRep.setApplicant(adminObjMap.get(pl.getAuditor()).getName());
					}
					queryRep.setApplyTime(DateUtil.formatDatetime(pl.getAuditTime().getTime()));
				}
				rows.add(queryRep);
			}
			pagesRep.setRows(rows);
		}
		pagesRep.setTotal(cas.getTotalElements());

		return pagesRep;
	}

	/**
	 * 查询复核中
	 * 
	 * @param spec
	 * @param pageable
	 * @return
	 */
	public PageResp<ProductLogListResp> checkList(Specification<Product> spec, Pageable pageable) {
		PageResp<ProductLogListResp> pagesRep = new PageResp<ProductLogListResp>();

		Page<Product> cas = this.productDao.findAll(spec, pageable);

		if (cas != null && cas.getContent() != null && cas.getTotalElements() > 0) {
			List<ProductLogListResp> rows = new ArrayList<ProductLogListResp>();

			List<String> oids = new ArrayList<String>();
			for (Product p : cas) {
				oids.add(p.getOid());
			}
			Map<String, ProductLog> plMap1 = this.getProductLogs(oids, ProductLog.AUDIT_TYPE_Auditing, ProductLog.AUDIT_STATE_Commited);// 提交记录
			Map<String, ProductLog> plMap2 = this.getProductLogs(oids, ProductLog.AUDIT_TYPE_Auditing, ProductLog.AUDIT_STATE_Approval);// 审核记录

			Map<String, AdminObj> adminObjMap = new HashMap<String, AdminObj>();
			AdminObj adminObj = null;
			Map<String, Corporate> aoprateObjMap = new HashMap<String, Corporate>();
			Corporate corporate = null;
			ProductLog pl1 = null;
			ProductLog pl2 = null;
			for (Product p : cas) {
				ProductLogListResp queryRep = new ProductLogListResp(p);
				if (p.getAssetPool() != null && p.getAssetPool().getSpvEntity()!=null) {
					if (aoprateObjMap.get(p.getAssetPool().getSpvEntity().getCorperateOid()) == null) {
						try {
							corporate = this.corporateDao.findOne(p.getAssetPool().getSpvEntity().getCorperateOid());
							if(corporate!=null) {
								aoprateObjMap.put(p.getAssetPool().getSpvEntity().getCorperateOid(), corporate);
							}
						} catch (Exception e) {
						}
					}
					if (aoprateObjMap.get(p.getAssetPool().getSpvEntity().getCorperateOid()) != null) {
						queryRep.setSpvName(aoprateObjMap.get(p.getAssetPool().getSpvEntity().getCorperateOid()).getName());
					}
				}
				// 申请人和申请时间
				pl1 = plMap1.get(p.getOid());
				if (pl1 != null) {
					if (adminObjMap.get(pl1.getAuditor()) == null) {
						try {
							adminObj = adminSdk.getAdmin(pl1.getAuditor());
							adminObjMap.put(pl1.getAuditor(), adminObj);
						} catch (Exception e) {
						}

					}
					if (adminObjMap.get(pl1.getAuditor()) != null) {
						queryRep.setApplicant(adminObjMap.get(pl1.getAuditor()).getName());
					}
					queryRep.setApplyTime(DateUtil.formatDatetime(pl1.getAuditTime().getTime()));
				}

				pl2 = plMap2.get(p.getOid());
				if (pl2 != null) {
					// 审核人 审核时间
					if (adminObjMap.get(pl2.getAuditor()) == null) {
						try {
							adminObj = adminSdk.getAdmin(pl2.getAuditor());
							adminObjMap.put(pl2.getAuditor(), adminObj);
						} catch (Exception e) {
						}
					}
					if (adminObjMap.get(pl2.getAuditor()) != null) {
						queryRep.setAuditor(adminObjMap.get(pl2.getAuditor()).getName());
					}
					queryRep.setAuditTime(DateUtil.formatDatetime(pl2.getAuditTime().getTime()));
				}
				rows.add(queryRep);
			}
			pagesRep.setRows(rows);
		}
		pagesRep.setTotal(cas.getTotalElements());

		return pagesRep;
	}

	/**
	 * 查询productlogs
	 * 
	 * @param oids
	 * @param auditType
	 * @param auditState
	 * @return
	 */
	public Map<String, ProductLog> getProductLogs(final List<String> oids, final String auditType, final String auditState) {
		Direction sortDirection = Direction.DESC;
		Sort sort = new Sort(new Order(sortDirection, "auditTime"));

		Specification<ProductLog> spec = new Specification<ProductLog>() {
			@Override
			public Predicate toPredicate(Root<ProductLog> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.and(cb.equal(root.get("auditType").as(String.class), auditType), cb.equal(root.get("auditState").as(String.class), auditState));
			}
		};

		spec = Specifications.where(spec).and(new Specification<ProductLog>() {
			@Override
			public Predicate toPredicate(Root<ProductLog> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				Expression<String> exp = root.get("product").get("oid").as(String.class);
				In<String> in = cb.in(exp);
				for (String oid : oids) {
					in.value(oid);
				}
				return in;
			}
		});

		Map<String, ProductLog> map = new HashMap<String, ProductLog>();
		List<ProductLog> pls = this.productLogDao.findAll(spec, sort);
		if (null != pls && pls.size() > 0) {
			for (ProductLog pl : pls) {
				if (map.get(pl.getProduct().getOid()) == null) {
					map.put(pl.getProduct().getOid(), pl);
				}
			}
		}
		return map;
	}

	@Transactional
	public BaseResp aduitApply(List<String> oids, String operator) throws ParseException {
		BaseResp response = new BaseResp();

		List<Product> ps = productDao.findByOidIn(oids);
		if (ps == null || ps.size() == 0) {
			throw AMPException.getException(90000);
		}

		Timestamp now = new Timestamp(System.currentTimeMillis());
		for (Product product : ps) {
			if (product == null || Product.YES.equals(product.getIsDeleted())) {
				throw AMPException.getException(90000);
			}
			if (product.getAssetPool() == null) {
				throw AMPException.getException(90011);
			}
			// 必须有协议书
			if (!isExistFiles(product)) {
				throw AMPException.getException(90033);
			}
			if (!Product.AUDIT_STATE_Nocommit.equals(product.getAuditState())) {
				throw AMPException.getException(90015);
			}
			product.setState(Product.STATE_Auditing);
			product.setAuditState(Product.AUDIT_STATE_Auditing);
			product.setOperator(operator);
			product.setUpdateTime(now);

			ProductLog.ProductLogBuilder plb = ProductLog.builder().oid(StringUtil.uuid());
			{
				plb.product(product).auditType(ProductLog.AUDIT_TYPE_Auditing).auditState(ProductLog.AUDIT_STATE_Commited).auditor(operator).auditTime(now);
			}

			this.productDao.saveAndFlush(product);
			this.productLogDao.save(plb.build());

		}

		return response;
	}

	private boolean isExistFiles(Product product) {
		/*
		 * if (StringUtil.isEmpty(product.getFileKeys())) {// 附件
		 * return false;
		 * } else {// 附件
		 * List<File> files = this.fileService.list(product.getFileKeys(), File.STATE_Valid);
		 * if (files == null || files.size() == 0) {
		 * return false;
		 * }
		 * }
		 */
		if (StringUtil.isEmpty(product.getInvestFileKey())) {// 投资协议
			return false;
		} else {// 投资协议
			List<File> infiles = this.fileService.list(product.getInvestFileKey(), File.STATE_Valid);
			if (infiles == null || infiles.size() == 0) {
				return false;
			}
		}
		if (StringUtil.isEmpty(product.getServiceFileKey())) {// 服务协议
			return false;
		} else {// 服务协议
			List<File> sfiles = this.fileService.list(product.getServiceFileKey(), File.STATE_Valid);
			if (sfiles == null || sfiles.size() == 0) {
				return false;
			}
		}
		return true;
	}

	@Transactional
	public BaseResp aduitApprove(String oid, String operator) throws ParseException {
		BaseResp response = new BaseResp();

		Product product = this.getProductByOid(oid);

		if (!Product.AUDIT_STATE_Auditing.equals(product.getAuditState())) {
			throw AMPException.getException(90013);
		}

		Timestamp now = new Timestamp(System.currentTimeMillis());
		product.setState(Product.STATE_Auditpass);
		product.setAuditState(Product.AUDIT_STATE_Reviewing);
		product.setOperator(operator);
		product.setUpdateTime(now);

		ProductLog.ProductLogBuilder plb = ProductLog.builder().oid(StringUtil.uuid());
		{
			plb.product(product).auditType(ProductLog.AUDIT_TYPE_Auditing).auditState(ProductLog.AUDIT_STATE_Approval).auditor(operator).auditTime(now);
		}

		this.productDao.saveAndFlush(product);
		this.productLogDao.save(plb.build());

		return response;
	}

	@Transactional
	public BaseResp aduitReject(String oid, String auditComment, String operator) throws ParseException {
		BaseResp response = new BaseResp();

		Product product = this.getProductByOid(oid);
		if (!Product.AUDIT_STATE_Auditing.equals(product.getAuditState())) {
			throw AMPException.getException(90013);
		}
		Timestamp now = new Timestamp(System.currentTimeMillis());
		product.setState(Product.STATE_Auditfail);
		product.setAuditState(Product.AUDIT_STATE_Nocommit);
		product.setOperator(operator);
		product.setUpdateTime(now);

		ProductLog.ProductLogBuilder plb = ProductLog.builder().oid(StringUtil.uuid());
		{
			plb.product(product).auditType(ProductLog.AUDIT_TYPE_Auditing).auditState(ProductLog.AUDIT_STATE_Reject).auditor(operator).auditTime(now).auditComment(auditComment);
		}

		this.productDao.saveAndFlush(product);
		this.productLogDao.save(plb.build());

		return response;
	}

	@Transactional
	public BaseResp reviewApprove(String oid, String operator) throws ParseException {
		BaseResp response = new BaseResp();

		Product product = this.getProductByOid(oid);
		if (!Product.AUDIT_STATE_Reviewing.equals(product.getAuditState())) {
			throw AMPException.getException(90014);
		}
		Timestamp now = new Timestamp(System.currentTimeMillis());
		product.setState(Product.STATE_Reviewpass);
		product.setAuditState(Product.AUDIT_STATE_Reviewed);
		product.setOperator(operator);
		product.setUpdateTime(now);

		ProductLog.ProductLogBuilder plb = ProductLog.builder().oid(StringUtil.uuid());
		{
			plb.product(product).auditType(ProductLog.AUDIT_TYPE_Reviewing).auditState(ProductLog.AUDIT_STATE_Approval).auditor(operator).auditTime(now);
		}

		AssetPoolEntity assetPool = this.assetPoolService.getByOid(product.getAssetPool().getOid());
		if (assetPool == null) {
			throw AMPException.getException(30001);
		}

		PublisherBaseAccountEntity spv = this.publisherBaseAccountDao.findOne(assetPool.getSpvEntity().getOid());

		PublisherHoldEntity hold = this.publisherHoldService.getAssetPoolSpvHold4reviewProduct(assetPool, spv, product.getType().getOid());
		if (hold != null) {
			if(product.getType().getOid().equals(Product.TYPE_Producttype_02) || product.getType().getOid().equals(Product.TYPE_Producttype_03)) {
				product.setRaisedTotalNumber(hold.getTotalVolume());// 本金余额(持有总份额) totalHoldVolume decimal(16,4)
			}else {
				hold.setTotalVolume(product.getRaisedTotalNumber());// 定期持有总份额等于产品募集金额
			}
			if (hold.getProduct() == null) {
				hold.setProduct(product);
				this.publisherHoldDao.saveAndFlush(hold);
				cacheSPVHoldService.createSPVHoldCache(hold);
			}
		}

		this.productDao.saveAndFlush(product);
		this.productLogDao.save(plb.build());

		return response;
	}

	@Transactional
	public BaseResp reviewReject(String oid, String auditComment, String operator) throws ParseException {
		BaseResp response = new BaseResp();

		Product product = this.getProductByOid(oid);
		if (!Product.AUDIT_STATE_Reviewing.equals(product.getAuditState())) {
			throw AMPException.getException(90014);
		}
		Timestamp now = new Timestamp(System.currentTimeMillis());
		product.setState(Product.STATE_Reviewfail);
		product.setAuditState(Product.AUDIT_STATE_Auditing);
		product.setOperator(operator);
		product.setUpdateTime(now);

		ProductLog.ProductLogBuilder plb = ProductLog.builder().oid(StringUtil.uuid());
		{
			plb.product(product).auditType(ProductLog.AUDIT_TYPE_Reviewing).auditState(ProductLog.AUDIT_STATE_Reject).auditor(operator).auditTime(now).auditComment(auditComment);
		}

		this.productDao.saveAndFlush(product);
		this.productLogDao.save(plb.build());

		return response;
	}

	@Transactional
	public long validateSingle(final String attrName, final String value, final String oid) {

		Specification<Product> spec = new Specification<Product>() {
			@Override
			public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				if (StringUtil.isEmpty(oid)) {
					return cb.and(cb.equal(root.get("isDeleted").as(String.class), Product.NO), cb.equal(root.get(attrName).as(String.class), value));
				} else {
					return cb.and(cb.equal(root.get("isDeleted").as(String.class), Product.NO), cb.equal(root.get(attrName).as(String.class), value),
							cb.notEqual(root.get("oid").as(String.class), oid));
				}
			}
		};
		spec = Specifications.where(spec);

		return this.productDao.count(spec);
	}

	/**
	 * 查询某个资产下的未删除的产品
	 * 
	 * @param assetPoolOid
	 * @return
	 */
	public List<Product> getProductListByAssetPoolOid(final String assetPoolOid) {

		Specification<Product> spec = new Specification<Product>() {
			@Override
			public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.and(cb.equal(root.get("assetPool").get("oid").as(String.class), assetPoolOid), cb.equal(root.get("isDeleted").as(String.class), Product.NO));
			}
		};
		List<Product> products = productDao.findAll(spec);

		return products;
	}

	public List<Product> findByState(String state) {
		return this.productDao.findByState(state);
	}

	/**
	 * 投资校验产品
	 * 
	 * @param tradeOrder
	 */
	public void checkProduct4Invest(InvestorTradeOrderEntity tradeOrder) {
		Product product = tradeOrder.getProduct();

		if (Product.NO.equals(product.getIsOpenPurchase())) {
			// error.define[30020]=申购开关已关闭(CODE:30020)
			throw new AMPException(30020);
		}
		
		if (Product.TYPE_Producttype_01.equals(product.getType().getOid())) {
			if (!Product.STATE_Raising.equals(product.getState())) {
				// error.define[30017]=定期产品非募集期不能投资(CODE:30017)
				throw new AMPException(30017);
			}
		}
		
		if (Product.TYPE_Producttype_02.equals(product.getType().getOid())) {
			if (!Product.STATE_Durationing.equals(product.getState())) {
				// error.define[30055]=活期产品非存续期不能投资(CODE:30055)
				throw new AMPException(30055);
			}
		}

		if (Product.YES.equals(product.getIsDeleted())) {
			// error.define[30018]=产品已删除(CODE:30018)
			throw new AMPException(30018);
		}

		// 投资份额需要大于0
		if (tradeOrder.getOrderVolume().compareTo(BigDecimal.ZERO) <= 0) {
			// error.define[30040]=金额不能小于等于0(CODE:30040)
			throw new AMPException(30040);
		}

		if (null != product.getInvestMin() && product.getInvestMin().compareTo(BigDecimal.ZERO) != 0) {
			if (tradeOrder.getOrderVolume().compareTo(product.getInvestMin()) < 0) {
				// error.define[30008]=不能小于产品投资最低金额(CODE:30008)
				throw new AMPException(30008);
			}
		}
		if (null != product.getInvestMax() && product.getInvestMax().compareTo(BigDecimal.ZERO) != 0) {
			if (tradeOrder.getOrderVolume().compareTo(product.getInvestMax()) > 0) {
				// error.define[30009]=已超过产品投资最高金额(CODE:30009)
				throw new AMPException(30009);
			}
		}
		if (null != product.getInvestAdditional() && product.getInvestAdditional().compareTo(BigDecimal.ZERO) != 0) {
			if (null != product.getInvestMin() && product.getInvestMin().compareTo(BigDecimal.ZERO) != 0) {
				if (tradeOrder.getOrderVolume().subtract(product.getInvestMin()).remainder(product.getInvestAdditional()).compareTo(BigDecimal.ZERO) != 0) {
					// error.define[30010]=不满足产品投资追加金额(CODE:30010)
					throw new AMPException(30010);
				}
			} else {
				if (tradeOrder.getOrderVolume().remainder(product.getInvestAdditional()).compareTo(BigDecimal.ZERO) != 0) {
					// error.define[30010]=不满足产品投资追加金额(CODE:30010)
					throw new AMPException(30010);
				}
			}

		}
		
		// 新手专享
		//if (Product.PRODUCT_productLabel_freshman.equals(product.getProductLabel()) 
		//		&& InvestorBaseAccountEntity.BASEACCOUNT_isFreshMan_no.equals(tradeOrder.getInvestorBaseAccount().getIsFreshMan())) {
			// error.define[30047]=新手专享(CODE:30047)
		//	throw AMPException.getException(30047);
		//}
		
	}
	
	public void updateProduct4LockCollectedVolume(InvestorTradeOrderEntity tradeOrder) {
		this.updateProduct4LockCollectedVolume(tradeOrder, false);
	}
	/**
	 * 检验产品可售份额
	 * 
	 * @param tradeOrder
	 */
	public void updateProduct4LockCollectedVolume(InvestorTradeOrderEntity tradeOrder, boolean isRecovery) {
		BigDecimal orderVolume = tradeOrder.getOrderVolume();
		if (isRecovery) {
			orderVolume = orderVolume.negate();
		}
		int i = this.productDao.update4Invest(tradeOrder.getProduct().getOid(), orderVolume);
		if (i < 1) {
			// error.define[30011]=产品可投金额不足(CODE:30011)
			throw new AMPException(30011);
		}
	}

	/**
	 * 份额确认之后解除锁定份额
	 * 
	 * @param tradeOrder
	 */
	public int update4InvestConfirm(Product product, BigDecimal orderVolume) {
		int i = this.productDao.update4InvestConfirm(product.getOid(), orderVolume);
		if (i < 1) {
			// error.define[30012]=解除产品锁定份额异常(CODE:30012)
			throw new AMPException(30012);
		}
		return i;
	}

	public void checkProduct4Redeem(InvestorTradeOrderEntity tradeOrder) {
		Product product = tradeOrder.getProduct();
		
		if (Product.TYPE_Producttype_01.equals(product.getType().getOid())) {
			// error.define[30060]=非活期产品不能赎回(CODE:30060)
			throw new AMPException(30060);
		}
		
		// 投资份额需要大于0
		if (tradeOrder.getOrderVolume().compareTo(BigDecimal.ZERO) <= 0) {
			// error.define[30040]=份额不能小于等于0(CODE:30040)
			throw new AMPException(30040);
		}

		if (null != product.getMaxRredeem() && product.getMaxRredeem().compareTo(BigDecimal.ZERO) != 0) {
			if (tradeOrder.getOrderVolume().compareTo(product.getMaxRredeem()) > 0) {
				// error.define[30038]=不满足赎回最高份额条件(CODE:30038)
				throw new AMPException(30038);
			}
		}


		if (Product.NO.equals(product.getIsOpenRemeed())) {
			// error.define[30021]=赎回开关已关闭(CODE:30021)
			throw new AMPException(30021);
		}
		
		
		if (Product.NO.equals(product.getIsOpenRedeemConfirm()) 
				&& (InvestorTradeOrderEntity.TRADEORDER_orderType_normalRedeem.equals(tradeOrder.getOrderType())
				|| InvestorTradeOrderEntity.TRADEORDER_orderType_incrementRedeem.equals(tradeOrder.getOrderType()))) {
			// error.define[30033]=屏蔽赎回确认处于打开状态(CODE:30033)
			throw new AMPException(30033);
		}
		
	}

	public void update4Redeem(Product product, BigDecimal orderVolume) {

		if (null != product.getNetMaxRredeemDay() && product.getNetMaxRredeemDay().compareTo(BigDecimal.ZERO) != 0) { // 产品单日赎回上限为零或null，则表示无上限
			int i = this.productDao.update4Redeem(product.getOid(), orderVolume);
			if (i < 1) {
				// error.define[30014]=赎回超出产品单日净赎回上限(CODE:30014)
				throw new AMPException(30014);
			}
		}

	}

	public int update4RedeemConfirm(Product product, BigDecimal orderVolume) {
		int i = this.productDao.update4RedeemConfirm(product.getOid(), orderVolume);
		if (i < 1) {
			// error.define[30019]=赎回确认份额异常(CODE:30019)
			throw new AMPException(30019);
		}
		if (Product.STATE_Clearing.equals(product.getState()) 
				|| Product.STATE_Durationend.equals(product.getState())
				|| Product.STATE_RaiseFail.equals(product.getState())) {
			i = this.productDao.update4Liquidation(product.getOid());
			if (i > 0) {
				/**
				 * 定期产品发起还本付息之后/或定期产品募集失败,增加已结算产品数量
				 */
				publisherStatisticsService.increaseClosedProductAmount(product.getPublisherBaseAccount());
				this.platformStatisticsService.increaseClosedProductAmount();
			}
		}
		return i;
	}


	/**
	 * 更新投资次数
	 */
	public int updatePurchaseNum(String productOid) {
		return this.productDao.updatePurchaseNum(productOid);
	}

	/**
	 * 更新购买人数
	 */
	public int updatePurchasePeopleNumAndPurchaseNum(String productOid) {
		return this.productDao.updatePurchasePeopleNumAndPurchaseNum(productOid);
	}

	public List<Product> findAll() {
		return this.productDao.findAll();
	}
	
	public List<Product> findAll(Specification<Product> spec) {
		return this.productDao.findAll(spec);
	}

	public Product findByOid(String productOid) {
		Product product = this.productDao.findOne(productOid);
		if (null == product) {
			throw new AMPException("产品不存在");
		}
		return product;
	}
	
	public List<Product> needIncomeBeforeOffset(String spvOid) {
		return this.productDao.needIncomeBeforeOffset(spvOid);
	}
	
	/**
	 * 活期产品 存续期、清盘中 发放收益
	 */
	public List<Product> findProductT04Snapshot() {
		return this.productDao.findProductT04Snapshot();
	}
	
	/**
	 *定期产品 募集期、募集结束在募集失败和募集成立之前发放收益
	 */
	public List<Product> findProductTn4Snapshot() {
		return this.productDao.findProductTn4Snapshot();
	}
	
	public List<Product> findProductTn4Interest() {
		return this.productDao.findProductTn4Interest();
	}

    /**
     * 获取快定宝
     * @return
     */
	public Product getBfPlusProduct(){
		return this.productDao.getBfPlusProduct();
	}

	/**
	 * 活期产品新建轧差批次
	 */
	public List<Product> findProductT04NewOffset(PublisherBaseAccountEntity spv) {
		return this.productDao.findProductT04NewOffset(spv.getOid());
	}
	
	/**
	 * 定期产品新建轧差批次
	 */
	public List<Product> findProductTn4NewOffset(PublisherBaseAccountEntity spv) {
		return this.productDao.findProductTn4NewOffset(spv.getOid());
	}

	/**
	 * 快定宝新建轧差批次
	 */
	public List<Product> findBfPlusNewOffset(PublisherBaseAccountEntity spv) {
		return this.productDao.findBfPlusNewOffset(spv.getOid());
	}

	/**
	 * 活期废单：解锁产品锁定已募份额 
	 */
	public int update4T0InvestAbandon(InvestorTradeOrderEntity orderEntity) {
		int i = this.productDao.update4T0InvestAbandon(orderEntity.getProduct().getOid(), orderEntity.getOrderVolume());
		if (i < 1) {
			// error.define[30034]=废申购单时产品锁定份额异常(CODE:30034)
			throw new AMPException(30034);
		}
		return i;
	}
	
	/**
	 * 定期废单
	 */
	public int update4TnInvestAbandon(InvestorTradeOrderEntity orderEntity) {
		int i = this.productDao.update4TnInvestAbandon(orderEntity.getProduct().getOid(), orderEntity.getOrderVolume());
		if (i < 1) {
			// error.define[30034]=废申购单时产品锁定份额异常(CODE:30034)
			throw new AMPException(30034);
		}
		return i;
	}

	public void update4RedeemRefuse(Product product, BigDecimal orderVolume) {
		if (null != product.getDailyNetMaxRredeem() && product.getDailyNetMaxRredeem().compareTo(BigDecimal.ZERO) != 0) { // 产品单日赎回上限为0，表示无限制
			this.productDao.update4RedeemRefuse(product.getOid(), orderVolume);
		}
	}

	/**
	 * 查找生成协议的产品
	 * 
	 * @return
	 */
	public List<Product> findByProduct4Contract() {

		return this.productDao.findByProduct4Contract();
	}
	
	/**
	 * 获取需要还本的产品
	 * @return
	 */
	public List<Product> getRepayLoanProduct(){
		return this.productDao.getRepayLoanProduct(DateUtil.getSqlDate());
	}
	
	/**
	 * 获取需要付息的产品
	 * @return
	 */
	public List<Product> getRepayInterestProduct(){
		return this.productDao.getRepayInterestProduct(DateUtil.getSqlDate());
	}
	/**
	 * @author yuechao
	 * 获取订单类型
	 */
	public String getRedeemType(Product product, InvestorBaseAccountEntity baseAccount, BigDecimal orderAmount,
			BigDecimal orderVolume) {
		if (baseAccount.getOwner().equals(InvestorBaseAccountEntity.BASEACCOUNT_owner_platform)) {
			return InvestorTradeOrderEntity.TRADEORDER_orderType_normalRedeem;
		}
		if (Product.NO.equals(product.getFastRedeemStatus())) {
			return InvestorTradeOrderEntity.TRADEORDER_orderType_normalRedeem;
		} else {
			int i = updateFastRedeemLeft(product.getOid(), orderVolume);
			if (i < 1) {
				return InvestorTradeOrderEntity.TRADEORDER_orderType_normalRedeem;
			}
			int j = this.investorBaseAccountService.updateBalanceMinusMinus(orderAmount, investorBaseAccountService.getSuperInvestor());
			if (j < 1) {
				if (i > 1) {
					this.updateFastRedeemLeft(product.getOid(), orderVolume);
				}
				return InvestorTradeOrderEntity.TRADEORDER_orderType_normalRedeem;
			}

			return InvestorTradeOrderEntity.TRADEORDER_orderType_fastRedeem;

		}}
	
	public int updateFastRedeemLeft(String productOid, BigDecimal orderVolume) {
		int i = this.productDao.updateFastRedeemLeft(productOid, orderVolume);
		return i;
	}
	
	/**
	 * @author yuechao
	 * @return
	 */
	public int resetFastRedeemLeft() {
		int i = this.productDao.resetFastRedeemLeft();
		return i;
	}
	
	/**
	 * @author yuechao
	 */
	public Product saveEntity(Product product) {
		return this.productDao.save(product);
	}
	
	/**
	 * 发行人下的产品列表
	 * 
	 * @param spec
	 * @param pageable
	 * @return {@link PagesRep<ProductResp>} ,如果返回的errCode属性等于0表示成功，否则表示失败，失败原因在errMessage里面体现
	 */
	public PageResp<ProductResp> accproducts(final String corporateOid, Specification<Product> spec, int page, int number) {
		PageResp<ProductResp> pagesRep = new PageResp<ProductResp>();
		
		Specification<PublisherBaseAccountEntity> pbaspec = new Specification<PublisherBaseAccountEntity>() {
			@Override
			public Predicate toPredicate(Root<PublisherBaseAccountEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.equal(root.get("corperateOid").as(String.class), corporateOid);
			}
		};
		
		final List<PublisherBaseAccountEntity> pbas = this.publisherBaseAccountDao.findAll(pbaspec);
		if(pbas!=null && pbas.size()>0) {
			if(pbas.size()>1){
				spec = Specifications.where(spec).and(new Specification<Product>() {
					@Override
					public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
						Expression<String> exp = root.get("publisherBaseAccount").get("oid").as(String.class);
						In<String> in = cb.in(exp);
						for (PublisherBaseAccountEntity pba : pbas) {
							in.value(pba.getOid());
						}
						return in;
					}
				});
			} else {
				spec = Specifications.where(spec).and(new Specification<Product>() {
					@Override
					public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
						return cb.equal(root.get("publisherBaseAccount").get("oid").as(String.class), pbas.get(0).getOid());
					}
				});
			}
			Pageable pageable = new PageRequest(page - 1, number, new Sort(new Order(Direction.DESC, "createTime")));	
			
			Page<Product> cas = this.productDao.findAll(spec, pageable);
			
			if (cas != null && cas.getContent() != null && cas.getTotalElements() > 0) {
				List<ProductResp> rows = new ArrayList<ProductResp>();

				Map<String, List<ProductIncomeReward>> incomeRewardNum = this.getProductRewards(cas.getContent());

				Map<String, Corporate> aoprateObjMap = new HashMap<String, Corporate>();
				Corporate corporate = null;
				for (Product p : cas) {
					ProductResp queryRep = new ProductResp(p);
					if (p.getAssetPool() != null && p.getAssetPool().getSpvEntity()!=null) {
						if (aoprateObjMap.get(p.getAssetPool().getSpvEntity().getCorperateOid()) == null) {
							try {
								corporate = this.corporateDao.findOne(p.getAssetPool().getSpvEntity().getCorperateOid());
								if(corporate!=null) {
									aoprateObjMap.put(p.getAssetPool().getSpvEntity().getCorperateOid(), corporate);
								}
							} catch (Exception e) {
							}
						}
						if (aoprateObjMap.get(p.getAssetPool().getSpvEntity().getCorperateOid()) != null) {
							queryRep.setSpvName(aoprateObjMap.get(p.getAssetPool().getSpvEntity().getCorperateOid()).getName());
						}
					}
					if (incomeRewardNum.get(p.getOid()) != null) {
						queryRep.setRewardNum(incomeRewardNum.get(p.getOid()).size());
					} else {
						queryRep.setRewardNum(0);
					}

					rows.add(queryRep);
				}
				pagesRep.setRows(rows);
			}
			pagesRep.setTotal(cas.getTotalElements());
		} else {
			pagesRep.setRows(null);
			pagesRep.setTotal(0);
		}
		return pagesRep;
	}

	/** 根据发行人和产品状态查询产品 */
	public List<Product> getProductByPublisherOidAndState(final String publisherOid, final String state) {
		List<Product> list = this.productDao.findAll(new Specification<Product>() {
			@Override
			public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.and(cb.equal(root.get("publisherBaseAccount").get("oid").as(String.class), publisherOid),//发行人ID
						cb.equal(root.get("state").as(String.class), state)//产品状态
						);
			}
		});
		
		return list;
	}
	
	public Product findOne(String oid) {
		return  this.productDao.findOne(oid);
	}

	public int updateRepayStatus(Product product, String repayLoanStatus, String repayInterestStatus) {
		return this.productDao.updateRepayStatus(product.getOid(), repayLoanStatus, repayInterestStatus);
		
	}
	
	/**
	 * @author yuechao
	 *  还本付息逾期产品
	 */
	public List<Product> getOverdueProduct(Date curDate) {
		return this.productDao.getOverdueProduct(curDate);
	}

	public void batchUpdate(List<Product> pList) {
		this.productDao.save(pList);
		
	}
	
	/**
	 * 还本付息锁
	 */
	@Transactional(TxType.REQUIRES_NEW)
	public void repayLock(String productOid) {
		int i = this.productDao.repayLock(productOid);
		if (i < 1) {
			// error.define[30066]=已还本付息或还本付息中(CODE:30066)
			throw AMPException.getException(30066);
		}
	}
	
	/** 校验产品交易时间 */
	public void isInDealTime(String productOid) {
		Product product = this.findByOid(productOid);
		//交易时间
		if (!StringUtil.isEmpty(product.getDealStartTime())
				&& !StringUtil.isEmpty(product.getDealEndTime())) {
			if (!DateUtil.isIn(DateUtil.getSqlCurrentDate(), product.getDealStartTime(),
					product.getDealEndTime())) {
				// error.define[30048]=非交易时间不接收订单(CODE:30048)
				throw AMPException.getException(30048);
			}
		}
	}
	
	/**
	 * 获取当前在售的产品列表
	 */
	public List<Product> findOnSaleProducts(){
		return this.productDao.findOnSaleProducts();
	}
	
	/**
	 * 获取在售活期产品OID，不包括体验金和阶梯奖励产品
	 */
	public OnSaleT0ProductRep getOnSaleProductOid() {
		OnSaleT0ProductRep rep = new OnSaleT0ProductRep();
		
		List<Product> productList = this.productDao.getOnSaleNoAwardDemandProductOid();
		for (Product product : productList) {
			String label = productLabelService.findLabelByProduct(product);
			if (!labelService.isProductLabelHasAppointLabel(label, LabelEnum.tiyanjin.toString())) {
				rep.setProductOid(product.getOid());
				rep.setHaveFlag(true);
				return rep;
			}
		}
		return rep;
		
	}
	
	@Transactional(TxType.REQUIRES_NEW)
	public int lockProduct(String productOid) {
		int i = this.productDao.lockProduct(productOid);
		if (i < 1) {
			throw new AMPException("产品正在处理中,请稍侯");
		}
		return i;
		
	}
	
	@Transactional(TxType.REQUIRES_NEW)
	public int unLockProduct(String productOid) {
		int i = this.productDao.unLockProduct(productOid);
		if (i < 1) {
			logger.error("产品解锁异常");
		}
		return i;
	}
	
	// @Transactional(TxType.REQUIRES_NEW)
	public int repayInterestOk(String productOid) {
		int i = this.productDao.repayInterestOk(productOid);
		if (i < 1) {
			logger.error("{}产品正在派息状态更新失败", productOid);
		}
		return i;
	}
	
	@Transactional(TxType.REQUIRES_NEW)
	public int repayInterestLock(String productOid) {
		int i = this.productDao.repayInterestLock(productOid);
		if (i < 1) {
			throw new AMPException("产品正在派息中或已派息");
		}
		return i;
	}
	
	@Transactional(TxType.REQUIRES_NEW)
	public int repayLoanLock(String productOid) {
		int i = this.productDao.repayLoanLock(productOid);
		if (i < 1) {
			throw new AMPException("产品正在还本或尚未派息");
		}
		return i;
	}
	
	@Transactional(TxType.REQUIRES_NEW)
	public int repayLoanEnd(String productOid, String repayLoanStatus, String interestAuditStatus) {
		int i = this.productDao.repayLoanEnd(productOid, repayLoanStatus, interestAuditStatus);
		if (i < 1) {
			logger.error("{}产品正在派息状态更新失败", productOid);
		}
		return i;
	}
	
	/**扫尾单校验产品锁定份额
	 * @param mechanismOrders
	 */
	public void updateProduct4LockCollectedVolumeForSumplement(MechanismOrder mechanismOrder) {
		String oid = mechanismOrder.getProduct().getOid();
		int i = this.productDao.update4Invest(oid, mechanismOrder.getOrderAmount());
		if (i < 1) {
			// error.define[30011]=产品可投金额不足(CODE:30011)
			throw new AMPException(30011);
		}
	}

	/**解除锁定份额
	 * @param mechanismOrders
	 */
	public void update4InvestConfirmForSumplement(MechanismOrder mechanismOrder) {
		Product product = mechanismOrder.getProduct();
		this.update4InvestConfirm(product,mechanismOrder.getOrderAmount());
	}
	
//	/**扫尾单校验产品锁定份额
//	 * @param mechanismOrders
//	 */
//	public void updateProduct4LockCollectedVolumeForSumplement(List<MechanismOrder> mechanismOrders) {
//		BigDecimal totalOrderVolume = BigDecimal.ZERO;
//		String oid = "";
//		for(MechanismOrder mo : mechanismOrders){
//			totalOrderVolume = totalOrderVolume.add(mo.getOrderAmount());
//			oid = mo.getProduct().getOid();
//		}
//		int i = this.productDao.update4Invest(oid, totalOrderVolume);
//		if (i < 1) {
//			// error.define[30011]=产品可投金额不足(CODE:30011)
//			throw new AMPException(30011);
//		}
//	}

//	/**解除锁定份额
//	 * @param mechanismOrders
//	 */
//	public void update4InvestConfirmForSumplement(List<MechanismOrder> mechanismOrders) {
//		BigDecimal totalOrderVolume = BigDecimal.ZERO;
//		Product product = null;
//		for(MechanismOrder mo : mechanismOrders){
//			totalOrderVolume = totalOrderVolume.add(mo.getOrderAmount());
//			product = mo.getProduct();
//		}
//		this.update4InvestConfirm(product,totalOrderVolume);
//	}

	/**定期产品（非新手标）
	 * @return
	 */
	public TnProductRep findTnProductAndNotFreashman() {
		List<Object[]> objs = this.productDao.findTnProductAndNotFreashman();
		TnProductRep rep = new TnProductRep();
		for(Object[] obj: objs){
			TnProduct p = new TnProduct();
			p.setOid(String.valueOf(obj[0]));
			p.setName(String.valueOf(obj[1]));
			rep.getTns().add(p);
		}
		return rep;
	}

	/**查询产品可售份额
	 * @param productOid
	 * @return
	 */
	public RestOrderAmountRep findMaxScaleAmountByProduct(String productOid) {
		RestOrderAmountRep rep = new RestOrderAmountRep();
		Product p = this.productDao.findOne(productOid);
		if(p!=null){
			BigDecimal maxSaleVolume = p.getMaxSaleVolume();
			BigDecimal maxSaleAmount = DecimalUtil.zoomIn(maxSaleVolume, p.getNetUnitShare());
			rep.setRestOrderAmount(maxSaleAmount);
			return rep;
		}
		return null;
	}
	
	
	public static String changeNumber(String num){
		char[] ary1 = num.toCharArray();
		char[] ary2 = {'0','0','0','0'};
		System.arraycopy(ary1, 0, ary2, ary2.length-ary1.length, ary1.length);
		String result = new String(ary2);
		return result;
	}

	public static void main(String[] args) {
		AtomicInteger ai=new AtomicInteger(0);
		ai.incrementAndGet();
		System.out.println(changeNumber(ai.toString()));
	}

	public Product getByGuess(GuessEntity guess) {
		return this.productDao.findByGuess(guess).get(0);
	}
	
	public List<Product> getProductByProductPackage(ProductPackage productPackage) {
		return this.productDao.findByPackage(productPackage);
	}
	
	/**
	 * 批量派息审核修改产品派息审核状态
	 * @param productOid
	 * @param interestAuditStatus
	 */
	@Transactional(TxType.REQUIRES_NEW)
	public void batchUpdateInterestAuditStatus(List<String> oids, String interestAuditStatusFrom, String interestAuditStatusTo) {
		
		List<Product> ps = productDao.findByOidIn(oids);
		if (ps == null || ps.size() == 0) {
			throw AMPException.getException(90000);
		}
		for (Product product : ps) {
			if (product == null || Product.YES.equals(product.getIsDeleted())) {
				throw AMPException.getException(90000);
			}
			int i = this.productDao.updateInterestAuditStatus(product.getOid(), interestAuditStatusFrom, interestAuditStatusTo);
			if (i < 1) {
				// error.define[19001]=更新产品派息审核状态失败(CODE:19001)
				throw new AMPException(19001);
			}
		}
		
	}
	
	@Transactional(TxType.REQUIRES_NEW)
	public void updateInterestAuditStatus(String productOid, String interestAuditStatusFrom, String interestAuditStatusTo) {
		int i = this.productDao.updateInterestAuditStatus(productOid, interestAuditStatusFrom, interestAuditStatusTo);
		if (i < 1) {
			// error.define[19001]=更新产品派息审核状态失败(CODE:19001)
			throw new AMPException(19001);
		}
	}

	/**
	 * 循环开放产品派息
	 */
	@Transactional(TxType.REQUIRES_NEW)
	public void cycleProductInterest() {
		logger.info("【循环产品派息】派息开始");
		// 派息锁
		int i = this.productDao.cycleProductInterestLock();
		if (i < 1) {
			logger.info("【循环产品派息】没有产品需要派息，跳过后续操作");
			return;
		}
		// 实际派息
		i = this.productDao.cycleProductInterest();
		logger.info("【循环产品派息】派息完成，共处理{}条数据", i);

		// 循环开放产品发放收益到投资者合仓收益明细
		i = snapshotService.distributeCycleProductInterestToInvestorHoldIncome();
		logger.info("【循环产品派息】发放收益到投资者合仓收益明细完成，共处理{}条数据", i);

		// 循环开放产品发放收益到投资者收益明细
		i = snapshotService.distributeCycleProductInterestToInvestorIncome();
		logger.info("【循环产品派息】发放收益到投资者收益明细完成，共处理{}条数据", i);

		i = this.productDao.distributeCycleProductInterestToCurrentVolume();
		logger.info("【循环产品派息】更新产品当前份额完成，共处理{}条数据", i);

		i = this.snapshotService.distributeCycleProductInterestToStatisticsIncome();
		logger.info("【循环产品派息】更新统计表利息完成，共处理{}条数据", i);

//		i = this.publisherStatisticsDao.increaseTotalInterestAmount(publisherBaseAccount, successAllocateIncome);

		// 派息完成，修改产品派息状态
		this.productDao.cycleProductInterestDone();
		logger.info("【循环产品派息】派息完成");
	}

	/**
	 * 清除循环开放产品续投临时表已处理数据
	 */
	@Transactional(TxType.REQUIRES_NEW)
	public void deleteToRepayListNoUseData() {
		logger.info("【循环产品续投】清除临时表待处理数据开始。");
		int i = this.productDao.deleteToRepayListNoUseData();
		logger.info("【循环产品续投】清除临时表待处理数据成功，共{}条。", i);
	}

	/**
	 * 将待续投的循环开放产品按照规则添加到临时表
	 */
	@Transactional(TxType.REQUIRES_NEW)
	public void cycleProductAddToOperatingList() {
		logger.info("【循环产品续投】新增续投数据到临时表开始。");
		int i = this.productDao.cycleProductAddToOperatingList();
		logger.info("【循环产品续投】新增续投数据到临时表成功，共{}条。", i);
		// 初始化要处理的数据
		cycleProductOperateContinueService.initData();
	}

	public List<Product> getDurationEndCycleProductList() {
		return this.productDao.getDurationEndCycleProductList();
	}

	public List<Object[]> findProductsByChannelAndProductLabel(String channelOid, String label) {
		return this.productDao.findProductsByChannelAndProductLabel(channelOid, label);
	}

	public Product getType03ProductByAssetPoolOid(String assetPoolOid) {
		return productDao.getType03ProductByAssetPoolOid(assetPoolOid);
	}
}
