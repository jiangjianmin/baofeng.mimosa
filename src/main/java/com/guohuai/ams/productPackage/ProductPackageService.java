package com.guohuai.ams.productPackage;

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
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import com.guohuai.ams.companyScatterStandard.LoanContract;
import com.guohuai.ams.companyScatterStandard.LoanContractDao;
import com.guohuai.ams.productPackage.loan.CompanyLoanProductReq;
import com.guohuai.component.web.view.BaseRep;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
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
import com.google.common.base.Joiner;
import com.guohuai.ams.channel.Channel;
import com.guohuai.ams.channel.ChannelDao;
import com.guohuai.ams.dict.Dict;
import com.guohuai.ams.dict.DictService;
import com.guohuai.ams.duration.assetPool.AssetPoolDao;
import com.guohuai.ams.duration.assetPool.AssetPoolEntity;
import com.guohuai.ams.duration.assetPool.AssetPoolService;
import com.guohuai.ams.guess.GuessEntity;
import com.guohuai.ams.guess.GuessService;
import com.guohuai.ams.label.LabelDao;
import com.guohuai.ams.label.LabelEntity;
import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductDao;
import com.guohuai.ams.product.ProductLog;
import com.guohuai.ams.product.ProductLogDao;
import com.guohuai.ams.product.ProductService;
import com.guohuai.ams.product.ProductTypeDetail;
import com.guohuai.ams.product.ProductTypeDetailService;
import com.guohuai.ams.product.productChannel.ProductChannelService;
import com.guohuai.ams.product.reward.ProductIncomeRewardService;
import com.guohuai.ams.productPackage.coupon.ProductPackageCouponService;
import com.guohuai.basic.component.exception.GHException;
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
import com.guohuai.mmp.platform.accment.AccmentService;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountDao;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountEntityDao;
import com.guohuai.mmp.publisher.corporate.Corporate;
import com.guohuai.mmp.publisher.corporate.CorporateDao;
import com.guohuai.mmp.publisher.hold.PublisherHoldService;
import com.guohuai.operate.api.AdminSdk;
import com.guohuai.operate.api.objs.admin.AdminObj;
import com.guohuai.tuip.api.TulipSdk;

@Service
@Transactional
public class ProductPackageService {
	Logger logger = LoggerFactory.getLogger(ProductPackageService.class);
	@Autowired
	private ProductLogDao productLogDao;
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
	private ProductIncomeRewardService productRewardService;
	@Autowired
	private AssetPoolService assetPoolService;
	@Autowired
	TradeCalendarService tradeCalendarService;
	@Autowired
	private CorporateDao corporateDao;
	@Autowired
	private AccmentService accmentService;
	@Autowired
	private ProductPackageDao productPackageDao;
	@Autowired
	private ProductPackageLogDao productPackageLogDao;
	@Autowired
	private PublisherBaseAccountDao publisherBaseAccountDao;
	@Autowired
	private PublisherHoldService publisherHoldService;
	@Autowired
	private ProductService productService;
	@Autowired
	private DictService dictService;
	@Autowired
	private ProductDao productDao;
	@Autowired
	private ChannelDao channelDao;
	@Autowired
	private LabelDao labelDao;
	@Autowired
	private GuessService guessService;
	@Autowired
	private ProductPackageCouponService productPackageCouponService;
	@Autowired
	private ProductTypeDetailService productTypeDetailService;
	@Autowired
	private TulipSdk tulipSdk;
	@Autowired
	private LoanContractDao loanContractDao;

	@Value("${loan.product.assetPoolOid:}")
	private String loanProductAssetPoolOid;

	@Value("${loan.product.labelOid:}")
	private String loanProductLabelOid;

	@Value("${loan.product.channelOid:}")
	private String loanProductChannelOid;

	@Value("${loan.product.elementOid:}")
	private String loanProductElementOid;

	@Value("${loan.product.introOid:}")
	private String loanProductIntroOId;

	@Value("${loan.product.instruction:}")
	private String loanProductInstruction;

	@Value("${loan.product.investFile:}")
	private String loanProductInvestFile;

	@Value("${loan.product.serviceFile:}")
	private String loanProductServiceFile;

	@Value("${loan.product.files:}")
	private String loanProductFiles;

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
		// List<ProductPackage> products =
		// this.productPackageDao.findAll(Specifications.where(spec));
		List<Product> products = this.productDao.findAll(Specifications.where(spec));

		if (productOid != null) {
			ProductPackage pr = this.findByOid(productOid);
			type = pr.getType().getOid();
		}

		if (products != null && products.size() > 0) {
			Set<String> choosedAssetPoolOids = new HashSet<String>();

			String productAssetOid = null;// productOid该产品对应的资产池

			if (StringUtil.isEmpty(productOid)) {
				for (Product p : products) {
					if (p.getAssetPool() != null) {
						if (type == null || !type.equals(Product.TYPE_Producttype_01)
								|| !p.getType().getOid().equals(Product.TYPE_Producttype_01)) {
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
							if (type == null || !type.equals(Product.TYPE_Producttype_01)
									|| !p.getType().getOid().equals(Product.TYPE_Producttype_01)) {
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
	public BaseResp periodicPackage(SavePeriodicProductPackageForm form, String operator) {
		BaseResp response = new BaseResp();

		ProductPackage pp = newProductPackage();
		pp.setOid(StringUtil.uuid());
		// 竞猜活动添加
		pp.setRelateGuess(form.getRelateGuess());
		if (form.getRelateGuess() == 1) {
			GuessEntity guess = guessService.getByOid(form.getGuessOid());
			// 校验竞猜活动是否可用
			if (!GuessEntity.GUESS_STATUS_CREATED.equals(guess.getStatus())) {
				throw new GHException("该竞猜活动已被其他产品包关联");
			}
			pp.setGuess(guess);
		}
		// 竞猜活动添加
		pp.setCode(form.getCode()); // 产品包编号
		pp.setName(form.getName()); // 产品包简称
		pp.setFullName(form.getFullName()); // 产品包全称
		pp.setAdministrator(form.getAdministrator()); // 产品包管理人
		pp.setState(ProductPackage.STATE_Create);
		pp.setIsDeleted(ProductPackage.NO);
		pp.setRepayInterestStatus(ProductPackage.PRODUCT_repayInterestStatus_toRepay);// 付息状态
		pp.setRepayLoanStatus(ProductPackage.PRODUCT_repayLoanStatus_toRepay);// 还本状态

		// 产品包类型为定期
		Dict assetType = this.dictService.get(ProductPackage.TYPE_Producttype_01);
		pp.setType(assetType);
		// pp.setType(ProductPackage.TYPE_Producttype_01);

		AssetPoolEntity assetPool = assetPoolDao.findOne(form.getAssetPoolOid());
		if (null == assetPool) {
			throw new AMPException("资产池不存在");
		}
		pp.setAssetPool(assetPool);
		pp.setPublisherBaseAccount(assetPool.getSpvEntity());
		pp.setReveal(form.getReveal());
		if (ProductPackage.YES.equals(form.getReveal())) {
			pp.setRevealComment(StringUtil.empty2Null(form.getRevealComment()));
		}
		pp.setCurrency(form.getCurrency());
		pp.setIncomeCalcBasis(form.getIncomeCalcBasis());
		pp.setOperationRate(DecimalUtil.zoomIn(form.getOperationRate(), 100));

		// 年化收益
		pp.setExpAror(DecimalUtil.zoomIn(form.getExpAror(), 100));
		if (null == form.getExpArorSec() || form.getExpArorSec().compareTo(BigDecimal.ZERO) == 0) {
			pp.setExpArorSec(pp.getExpAror());
		} else {
			pp.setExpArorSec(DecimalUtil.zoomIn(form.getExpArorSec(), 100));
		}
		pp.setRewardInterest(DecimalUtil.null2Zero(form.getRewardInterest()));

		// 募集开始时间类型;募集期天数:()个自然日;起息日天数:成立后()个自然日;存续期天数:()个自然日;还本付息日
		// 存续期结束后第()个自然日
		if (ProductPackage.DATE_TYPE_ManualInput.equals(form.getRaiseStartDateType())
				&& null == form.getRaiseStartDate()) {
			// error.define[90009]=请填写募集开始时间
			throw AMPException.getException(90009);
		}
		pp.setRaiseStartDateType(form.getRaiseStartDateType());

		pp.setRaisePeriodDays(form.getRaisePeriod()); // 募集期

		pp.setInterestsFirstDays(form.getInterestsFirstDate()); // 起息期(成立后)

		pp.setDurationPeriodDays(form.getDurationPeriod()); // 存续期(成立后)

		pp.setAccrualRepayDays(form.getAccrualDate()); // 还本付息日

		// 募集期预期年化收益
		pp.setRecPeriodExpAnYield(DecimalUtil.zoomIn(form.getRecPeriodExpAnYield(), 100));

		// 认购确认日:认购订单提交后()个日内
		pp.setPurchaseConfirmDays(form.getSubscribeConfirmDays());

		// 募集满额后是否自动触发成立
		pp.setRaiseFullFoundType(form.getRaiseFullFoundType());

		// 募集满额后第()个自然日后自动成立
		if (ProductPackage.RAISE_FULL_FOUND_TYPE_AUTO.equals(pp.getRaiseFullFoundType())) {
			Integer foundDays = form.getAutoFoundDays() == null ? 0 : form.getAutoFoundDays();
			pp.setAutoFoundDays(foundDays);
		}

		// 募集期满后最晚成立日
		pp.setFoundDays(form.getFoundDays());

		// 募集开始日期,募集结束日期
		if (ProductPackage.DATE_TYPE_ManualInput.equals(form.getRaiseStartDateType())) {
			pp.setRaiseStartDate(form.getRaiseStartDate());

			Date raiseEndDate = DateUtil.addSQLDays(pp.getRaiseStartDate(), form.getRaisePeriod() - 1);
			pp.setRaiseEndDate(raiseEndDate);// 募集结束时间

			Date setupDate = DateUtil.addSQLDays(raiseEndDate, form.getFoundDays());
			pp.setSetupDate(setupDate);// 最晚产品成立时间

			Date durationPeriodEndDate = DateUtil.addSQLDays(setupDate, form.getDurationPeriod() - 1);
			pp.setDurationPeriodEndDate(durationPeriodEndDate);// 存续期结束时间

			// 到期最晚还本付息日 指存续期结束后的还本付息最迟发生在存续期后的第X个自然日的23:59:59为止
			Date repayDate = DateUtil.addSQLDays(durationPeriodEndDate, form.getAccrualDate());
			// 到期最晚还本付息日 指存续期结束后的还本付息最迟发生在存续期后的第X个自然日的23:59:59为止
			pp.setRepayDate(repayDate);// 到期还款时间

		} else {
			Date now = DateUtil
					.parseToSqlDate(new SimpleDateFormat(DateUtil.datetimePattern).format(new java.util.Date()));
			pp.setRaiseStartDate(now);

			Date raiseEndDate = DateUtil.addSQLDays(now, form.getRaisePeriod() - 1);
			pp.setRaiseEndDate(raiseEndDate);// 募集结束时间

			Date setupDate = DateUtil.addSQLDays(raiseEndDate, form.getFoundDays());
			pp.setSetupDate(setupDate);// 最晚产品成立时间

			Date durationPeriodEndDate = DateUtil.addSQLDays(setupDate, form.getDurationPeriod() - 1);
			pp.setDurationPeriodEndDate(durationPeriodEndDate);// 存续期结束时间

			// 到期最晚还本付息日 指存续期结束后的还本付息最迟发生在存续期后的第X个自然日的23:59:59为止
			Date repayDate = DateUtil.addSQLDays(durationPeriodEndDate, form.getAccrualDate());
			// 到期最晚还本付息日 指存续期结束后的还本付息最迟发生在存续期后的第X个自然日的23:59:59为止
			pp.setRepayDate(repayDate);// 到期还款时间
			// pp.setRaiseStartDate(null);
		}

		/** 产品包新加字段 20170518 **/
		pp.setSingleProductVolume(form.getSingleProductVolume());// 产品包包含的单个产品份额
		pp.setProductCount(form.getProductCount());// 产品包包含的产品数量
		String channelOid = "";
		List<String> channelOids = new ArrayList<String>();// 产品包发布渠道
		if (form.getChannelOids() != null && form.getChannelOids().length > 0) {
			for (String ex : form.getChannelOids()) {
				channelOids.add(ex);
			}
		}
		channelOid = Joiner.on(",").join(channelOids);
		pp.setChannelOid(channelOid);
		pp.setLimitTime(form.getLimitTime());// 募集期到期前几小时内不能发布新产品
		/** 产品包新加字段 20170518 **/

		pp.setRaisedTotalNumber(form.getSingleProductVolume().multiply(new BigDecimal(form.getProductCount())));
		pp.setMaxSaleVolume(form.getSingleProductVolume().multiply(new BigDecimal(form.getProductCount())));

		pp.setInvestMin(form.getInvestMin());
		pp.setInvestMax(form.getInvestMax());
		pp.setInvestAdditional(form.getInvestAdditional());
		pp.setInvestDateType(form.getInvestDateType());

		pp.setNetUnitShare(form.getNetUnitShare());

		pp.setDealStartTime(StringUtil.empty2Null(form.getDealStartTime()));
		pp.setDealEndTime(StringUtil.empty2Null(form.getDealEndTime()));

		pp.setIsOpenPurchase(ProductPackage.YES);
		pp.setIsOpenRemeed(ProductPackage.NO);
		pp.setInvestComment(StringUtil.empty2Null(form.getInvestComment()));

		pp.setInstruction(StringUtil.empty2Null(form.getInstruction()));
		pp.setRiskLevel(form.getRiskLevel());
		pp.setInvestorLevel(form.getInvestorLevel());
		pp.setStems(ProductPackage.STEMS_Userdefine);
		pp.setAuditState(ProductPackage.AUDIT_STATE_Nocommit);
		// 其他字段 初始化默认值s
		pp.setOperator(operator);
		pp.setUpdateTime(DateUtil.getSqlCurrentDate());
		pp.setCreateTime(DateUtil.getSqlCurrentDate());

		// 附件文件
		List<SaveFileForm> fileForms = null;
		String fkey = StringUtil.uuid();
		if (StringUtil.isEmpty(form.getFiles())) {
			pp.setFileKeys(StringUtil.EMPTY);
		} else {
			pp.setFileKeys(fkey);
			fileForms = JSON.parseArray(form.getFiles(), SaveFileForm.class);
		}

		// 投资协议书
		List<SaveFileForm> investFileForm = null;
		String investFileKey = StringUtil.uuid();
		if (StringUtil.isEmpty(form.getInvestFile())) {
			pp.setInvestFileKey(StringUtil.EMPTY);
		} else {
			pp.setInvestFileKey(investFileKey);
			investFileForm = JSON.parseArray(form.getInvestFile(), SaveFileForm.class);
		}

		// 信息服务协议
		List<SaveFileForm> serviceFileForm = null;

		String serviceFileKey = StringUtil.uuid();
		if (StringUtil.isEmpty(form.getServiceFile())) {
			pp.setServiceFileKey(StringUtil.EMPTY);
		} else {
			pp.setServiceFileKey(serviceFileKey);
			serviceFileForm = JSON.parseArray(form.getServiceFile(), SaveFileForm.class);
		}
		/**
		 * 产品详情相关
		 */
		pp.setProductElement(form.getProductElement());
		pp.setProductIntro(form.getProductIntro());
		pp.setActivityDetail(form.getActivityDetail());
		pp.setIsActivityProduct(form.getIsActivityProduct());
		pp.setExpectedArrorDisp(DecimalUtil.zoomIn(form.getExpectedArrorDisp(), 100));
		pp.setPurchaseApplyStatus(ProductPackage.APPLY_STATUS_None);
		pp.setRedeemApplyStatus(ProductPackage.APPLY_STATUS_None);
		pp.setBasicRatio(BigDecimal.ZERO);
		pp.setFastRedeemStatus(ProductPackage.NO);
		pp.setFastRedeemMax(BigDecimal.ZERO);
		pp.setFastRedeemLeft(BigDecimal.ZERO);

		/**
		 * P2P相关
		 */
		pp.setIfP2P(form.getIfP2P());
		pp.setIsP2PAssetPackage(form.getIsP2PAssetPackage());

		/**
		 * 账务账户ID
		 */
		String memeberId = accmentService.createSPVTnAccountProductPackage(pp);
		pp.setMemberId(memeberId);

		List<String> productExpandLabelOids = new ArrayList<String>();
		String productExpandLabel = "";
		if (!StringUtil.isEmpty(form.getBasicProductLabel())) {// 基础标签
			pp.setProductLabel(form.getBasicProductLabel());
		}
		if (form.getExpandProductLabels() != null && form.getExpandProductLabels().length > 0) {// 扩展标签
			for (String ex : form.getExpandProductLabels()) {
				productExpandLabelOids.add(ex);
			}
		}
		productExpandLabel = Joiner.on(",").join(productExpandLabelOids);
		pp.setProductExpandLabel(productExpandLabel);// 产品包扩展标签
		/**
		 * 卡券相关
		 */
		pp.setUseRedPackages(form.getUseRedPackages());
		pp.setUseraiseRateCoupons(form.getUseraiseRateCoupons());
		pp = this.productPackageDao.save(pp);

		/**
		 * 新建卡券产品关联表
		 */
		productPackageCouponService.saveEntity(pp.getOid(), form.getRedPackages(), form.getRaiseRateCoupons());
		// 附件文件
		this.fileService.save(fileForms, fkey, File.CATE_User, operator);
		// 投资协议书
		this.fileService.save(investFileForm, investFileKey, File.CATE_User, operator);
		// 信息服务协议
		this.fileService.save(serviceFileForm, serviceFileKey, File.CATE_User, operator);

		// this.productPackageLabelService.saveAndFlush(pp, labelOids);
		if (form.getRelateGuess() == 1) {
			// 修改竞猜活动状态为已锁定
			this.guessService.modiyStatusByOid(form.getGuessOid(), GuessEntity.GUESS_STATUS_CREATED,
					GuessEntity.GUESS_STATUS_LOCKED);
		}
		return response;
	}

	private ProductPackage newProductPackage() {
		ProductPackage pp = new ProductPackage();
		pp.setManageRate(new BigDecimal(0));
		pp.setFixedManageRate(new BigDecimal(0));
		pp.setBasicRatio(new BigDecimal(0));
		pp.setOperationRate(new BigDecimal(0));
		pp.setPayModeDay(0);
		pp.setRaisePeriodDays(0);
		pp.setLockPeriodDays(0);
		pp.setInterestsFirstDays(0);
		pp.setDurationPeriodDays(0);
		pp.setExpAror(new BigDecimal(0));
		pp.setExpArorSec(new BigDecimal(0));
		pp.setRaisedTotalNumber(new BigDecimal(0));
		pp.setNetUnitShare(new BigDecimal(0));
		pp.setInvestMin(new BigDecimal(0));
		pp.setInvestAdditional(new BigDecimal(0));
		pp.setInvestMax(new BigDecimal(0));
		pp.setMinRredeem(new BigDecimal(0));
		pp.setNetMaxRredeemDay(new BigDecimal(0));
		pp.setDailyNetMaxRredeem(new BigDecimal(0));
		pp.setAccrualRepayDays(0);
		pp.setPurchaseConfirmDays(0);
		pp.setRedeemConfirmDays(0);
		pp.setRedeemTimingTaskDays(0);
		pp.setPurchaseNum(0);
		pp.setCurrentVolume(new BigDecimal(0));
		pp.setCollectedVolume(new BigDecimal(0));
		pp.setLockCollectedVolume(new BigDecimal(0));
		pp.setMaxSaleVolume(new BigDecimal(0));
		pp.setIsAutoAssignIncome(ProductPackage.NO);
		pp.setProductCount(0);
		pp.setSingleProductVolume(new BigDecimal(0));
		pp.setToProductNum(0);
		return pp;
	}

	/**
	 * 获取指定id的产品对象
	 * 
	 * @param pid
	 *            产品对象id
	 * @return {@link Product}
	 */
	public ProductPackage getProductPackageByOid(String pid) {
		if (StringUtil.isEmpty(pid)) {
			return null;
		}
		ProductPackage product = this.productPackageDao.findOne(pid);
		if (product == null || ProductPackage.YES.equals(product.getIsDeleted())) {
			throw AMPException.getException(90000);
		}
		return product;

	}

	@Transactional
	public ProductPackage delete(String oid, String operator) {
		ProductPackage productPackage = this.getProductPackageByOid(oid);

		// 当产品包下面已经有上架的产品时，不能作废
		List<Product> products = productDao.productFromProductPackage(oid);
		if (products.size() > 0) {
			throw AMPException.getException(16001);
		}

		{
			productPackage.setIsDeleted(ProductPackage.YES);
			// 其它：修改时间、操作人
			productPackage.setOperator(operator);
			productPackage.setUpdateTime(new Timestamp(System.currentTimeMillis()));
		}
		productPackage = this.productPackageDao.saveAndFlush(productPackage);
		// 删除渠道和商品关联关系
		// productChannelDao.deleteByProductOid(oid);
		if (this.guessService.hasGuessInProductPackage(productPackage)) {
			// 修改竞猜活动为已解锁(已保存)
			this.guessService.modiyStatusByOid(productPackage.getGuess().getOid(), GuessEntity.GUESS_STATUS_LOCKED,
					GuessEntity.GUESS_STATUS_CREATED);
		}
		return productPackage;
	}

	/**
	 * 进行更新产品信息的操作，同时记录并存储日志
	 */
	@Transactional
	public BaseResp updateProductPackage(SavePeriodicProductPackageForm form, String operator) {

		BaseResp response = new BaseResp();

		// 根据form中的productOid，从数据库得到相应对象，之后进行为对象进行审核操作
		ProductPackage product = this.getProductPackageByOid(form.getOid());
		// 当前时间
		Timestamp now = new Timestamp(System.currentTimeMillis());

		// 未提交审核的可修改
		if (!ProductPackage.AUDIT_STATE_Nocommit.equals(product.getAuditState())) {
			throw AMPException.getException(90008);
		}
		// 判断是否可以修改 名称类型不变
		AssetPoolEntity assetPool = assetPoolDao.findOne(form.getAssetPoolOid());
		if (null == assetPool) {
			throw new AMPException("资产池不存在");
		}
		// 竞猜活动添加
		product.setRelateGuess(form.getRelateGuess());
		if (form.getRelateGuess() == 1) {
			GuessEntity guess = guessService.getByOid(form.getGuessOid());
			product.setGuess(guess);
			//
			// String newOid = guess.getOid();
			String newOid = form.getGuessOid();
			String oldOid = form.getGuessOldOid();
			if (StringUtils.isBlank(oldOid)) {
				this.guessService.modiyStatusByOid(newOid, GuessEntity.GUESS_STATUS_CREATED,
						GuessEntity.GUESS_STATUS_LOCKED);
			} else if (!newOid.equals(oldOid)) {
				// 将旧的oid解锁，新的锁定
				this.guessService.modiyStatusByOid(oldOid, GuessEntity.GUESS_STATUS_LOCKED,
						GuessEntity.GUESS_STATUS_CREATED);
				this.guessService.modiyStatusByOid(newOid, GuessEntity.GUESS_STATUS_CREATED,
						GuessEntity.GUESS_STATUS_LOCKED);
			}
			//
		} else {
			// 删除产品包和竞猜活动的关联，修改竞猜活动状态为已解锁（已保存）
			GuessEntity guess = product.getGuess();
			if (guess != null) {
				this.guessService.modiyStatusByOid(guess.getOid(), GuessEntity.GUESS_STATUS_LOCKED,
						GuessEntity.GUESS_STATUS_CREATED);
				product.setGuess(null);
			}
		}
		// 竞猜活动添加
		product.setPublisherBaseAccount(publisherDao.findOne(assetPool.getSpvEntity().getOid()));

		product.setAssetPool(assetPool);

		product.setCode(form.getCode());
		product.setName(form.getName());
		product.setFullName(form.getFullName());
		product.setAdministrator(form.getAdministrator());
		product.setState(ProductPackage.STATE_Update);

		product.setReveal(form.getReveal());
		if (ProductPackage.YES.equals(form.getReveal())) {
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
		if (ProductPackage.DATE_TYPE_ManualInput.equals(form.getRaiseStartDateType())
				&& null == form.getRaiseStartDate()) {
			// error.define[90009]=请填写募集开始时间
			throw AMPException.getException(90009);
		}

		/** 产品包新加字段 20170518 **/
		product.setSingleProductVolume(form.getSingleProductVolume());// 产品包包含的单个产品份额
		product.setProductCount(form.getProductCount());// 产品包包含的产品数量
		String channelOid = "";
		List<String> channelOids = new ArrayList<String>();// 产品包发布渠道
		if (form.getChannelOids() != null && form.getChannelOids().length > 0) {
			for (String ex : form.getChannelOids()) {
				channelOids.add(ex);
			}
		}
		channelOid = Joiner.on(",").join(channelOids);
		product.setChannelOid(channelOid);
		product.setLimitTime(form.getLimitTime());// 募集期到期前几小时内不能发布新产品
		/** 产品包新加字段 20170518 **/

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
		Integer foundDays = form.getAutoFoundDays() == null ? 0 : form.getAutoFoundDays();
		product.setAutoFoundDays(Integer.valueOf(foundDays));

		// 募集期满后最晚成立日
		product.setFoundDays(Integer.valueOf(form.getFoundDays()));

		// 募集开始日期,募集结束日期
		if (ProductPackage.DATE_TYPE_ManualInput.equals(form.getRaiseStartDateType())) {
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
			Date updateNow = DateUtil
					.parseToSqlDate(new SimpleDateFormat(DateUtil.datetimePattern).format(new java.util.Date()));
			product.setRaiseStartDate(updateNow);

			Date raiseEndDate = DateUtil.addSQLDays(updateNow, form.getRaisePeriod() - 1);
			product.setRaiseEndDate(raiseEndDate);// 募集结束时间

			Date setupDate = DateUtil.addSQLDays(raiseEndDate, form.getFoundDays());
			product.setSetupDate(setupDate);// 最晚产品成立时间

			Date durationPeriodEndDate = DateUtil.addSQLDays(setupDate, form.getDurationPeriod() - 1);
			product.setDurationPeriodEndDate(durationPeriodEndDate);// 存续期结束时间

			// 到期最晚还本付息日 指存续期结束后的还本付息最迟发生在存续期后的第X个自然日的23:59:59为止
			Date repayDate = DateUtil.addSQLDays(durationPeriodEndDate, form.getAccrualDate());
			// 到期最晚还本付息日 指存续期结束后的还本付息最迟发生在存续期后的第X个自然日的23:59:59为止
			product.setRepayDate(repayDate);// 到期还款时间
			// pp.setRaiseStartDate(null);
		}

		product.setRaisedTotalNumber(form.getSingleProductVolume().multiply(new BigDecimal(form.getProductCount())));

		product.setMaxSaleVolume(form.getSingleProductVolume().multiply(new BigDecimal(form.getProductCount())));

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
		/**
		 * 产品详情相关
		 */
		product.setProductElement(form.getProductElement());
		product.setProductIntro(form.getProductIntro());
		product.setActivityDetail(form.getActivityDetail());
		product.setIsActivityProduct(form.getIsActivityProduct());
		product.setExpectedArrorDisp(DecimalUtil.zoomIn(form.getExpectedArrorDisp(), 100));
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
		List<String> productExpandLabelOids = new ArrayList<String>();
		String productExpandLabel = "";
		if (!StringUtil.isEmpty(form.getBasicProductLabel())) {// 基础标签
			product.setProductLabel(form.getBasicProductLabel());
		}
		if (form.getExpandProductLabels() != null && form.getExpandProductLabels().length > 0) {// 扩展标签
			for (String ex : form.getExpandProductLabels()) {
				productExpandLabelOids.add(ex);
			}
		}
		productExpandLabel = Joiner.on(",").join(productExpandLabelOids);
		product.setProductExpandLabel(productExpandLabel);// 产品包扩展标签
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
		product = this.productPackageDao.saveAndFlush(product);
		/**
		 * 新建卡券产品关联表
		 */
		productPackageCouponService.updateEntity(product.getOid(), form.getRedPackages(), form.getRaiseRateCoupons());

		{
			// 附件文件
			this.fileService.save(fileForms, fkey, File.CATE_User, operator);
			// 投资协议书
			this.fileService.save(investFileForm, investFileKey, File.CATE_User, operator);
			// 信息服务协议
			this.fileService.save(serviceFileForm, serviceFileKey, File.CATE_User, operator);
		}

		return response;
	}

	/**
	 * 产品详情
	 */
	@Transactional
	public ProductPackageDetailResp read(String oid) {
		ProductPackage productPackage = this.getProductPackageByOid(oid);
		ProductPackageDetailResp pr;
		String productEleTitle = null;
		String productIntroTitle = null;
		String productActivityTitle = null;
		String productEleOid = null;
		String productIntroOid = null;
		String productActivityOid = null;
		if(StringUtils.isNotEmpty(productPackage.getProductElement())) {
			ProductTypeDetail productElementObj = productTypeDetailService.getOne(productPackage.getProductElement());
			if(!ObjectUtils.isEmpty(productElementObj)) {
				productEleTitle = productElementObj.getTitle();
				productEleOid = productElementObj.getOid();
			}
		}
		if(StringUtils.isNotEmpty(productPackage.getProductIntro())) {
			ProductTypeDetail productIntroObj = productTypeDetailService.getOne(productPackage.getProductIntro());
			if(!ObjectUtils.isEmpty(productIntroObj)) {
				productIntroTitle = productIntroObj.getTitle();
				productIntroOid = productIntroObj.getOid();
			}
		}
		if(StringUtils.isNotEmpty(productPackage.getActivityDetail())) {
			ProductTypeDetail activityDetailObj = productTypeDetailService.getOne(productPackage.getActivityDetail());
			if(!ObjectUtils.isEmpty(activityDetailObj)) {
				productActivityTitle = activityDetailObj.getTitle();
				productActivityOid = activityDetailObj.getOid();
			}
		}
		pr = new ProductPackageDetailResp(productPackage,productEleOid,productIntroOid,productActivityOid,productEleTitle,productIntroTitle,productActivityTitle);
		//关联的竞猜宝
		if(productPackage.getGuess()!=null){
			pr.setGuessName(productPackage.getGuess().getGuessName());
			pr.setGuessOid(productPackage.getGuess().getOid());
			pr.setRelateGuess("是");
		} else {
			pr.setRelateGuess("否");
		}
		// 关联的竞猜宝
		if (productPackage.getAssetPool() != null && productPackage.getAssetPool().getSpvEntity() != null) {
			Corporate corporate = this.corporateDao
					.findOne(productPackage.getAssetPool().getSpvEntity().getCorperateOid());
			if (corporate != null) {
				pr.setSpvName(corporate.getName());
			}
		}
		Map<String, AdminObj> adminObjMap = new HashMap<String, AdminObj>();

		FileResp fr = null;
		AdminObj adminObj = null;
		if (!StringUtil.isEmpty(productPackage.getFileKeys())) {
			List<File> files = this.fileService.list(productPackage.getFileKeys(), File.STATE_Valid);
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

		if (!StringUtil.isEmpty(productPackage.getInvestFileKey())) {
			List<File> files = this.fileService.list(productPackage.getInvestFileKey(), File.STATE_Valid);
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

		if (!StringUtil.isEmpty(productPackage.getServiceFileKey())) {
			List<File> files = this.fileService.list(productPackage.getServiceFileKey(), File.STATE_Valid);
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

		// List<ProductChannel> pcs =
		// productChannelService.queryProductChannels(oid);
		// if (pcs != null && pcs.size() > 0) {
		// StringBuilder channelNames = new StringBuilder();
		// List<String> channelOids = new ArrayList<String>();
		// for (ProductChannel pc : pcs) {
		// channelNames.append(pc.getChannel().getChannelName()).append(",");
		// channelOids.add(pc.getChannel().getOid());
		// }
		// pr.setChannelNames(channelNames.substring(0, channelNames.length() -
		// 1));
		// pr.setChannelOids(channelOids);
		// }

		// 产品包渠道
		String channelOid = productPackage.getChannelOid();
		String[] channelOidArr = channelOid.split(",");
		List<String> channelNames = new ArrayList<String>();
		pr.setChannelOids(channelOidArr);
		if (channelOidArr.length > 0) {
			for (int i = 0; i < channelOidArr.length; i++) {
				Channel channel = channelDao.findByOid(channelOidArr[i]);
				channelNames.add(channel.getChannelName());
			}
			pr.setChannelNames(channelNames.toArray(new String[channelNames.size()]));
		}

		// 产品包基础标签
		String productLabelOid = productPackage.getProductLabel();
		LabelEntity generalLabel = labelDao.findByOid(productLabelOid);
		pr.setBasicProductLabelOid(productLabelOid);
		pr.setBasicProductLabelName(generalLabel.getLabelName());

		// 产品扩展标签
		if (productPackage.getProductExpandLabel() != null && productPackage.getProductExpandLabel().length() > 0) {
			String productExtendLabel = productPackage.getProductExpandLabel();
			String[] productExtendLabelArr = productExtendLabel.split(",");
			List<String> exNames = new ArrayList<String>();
			pr.setExpandProductLabelOids(productExtendLabelArr);
			for (int i = 0; i < productExtendLabelArr.length; i++) {
				LabelEntity extendLabel = labelDao.findByOid(productExtendLabelArr[i]);
				exNames.add(extendLabel.getLabelName());
			}
			pr.setExpandProductLabelNames(exNames.toArray(new String[exNames.size()]));
		}
		// 卡券相关
		pr.setUsered(productPackage.getUseRedPackages() == 1 ? "可以使用部分红包"
				: (productPackage.getUseRedPackages() == 2 ? "不能使用红包" : "可以使用所有的红包"));
		pr.setUseRaise(productPackage.getUseraiseRateCoupons() == 1 ? "可以使用部分加息券"
				: (productPackage.getUseraiseRateCoupons() == 2 ? "不能使用加息券" : "可以使用所有的加息券"));
		pr.setUseredId(productPackage.getUseRedPackages());
		pr.setUseRaiseId(productPackage.getUseraiseRateCoupons());
		if (productPackage.getUseRedPackages() == 1) {
			List<Integer> redOids = productPackageCouponService.getRedListByProductPackageOid(productPackage.getOid());
			pr.setRedPackageOids(redOids.toArray(new Integer[redOids.size()]));
			List<Object> redNames = tulipSdk.getCardNames(redOids);
			pr.setRedPackageNames(redNames.toArray(new String[redNames.size()]));
		}
		if (productPackage.getUseraiseRateCoupons() == 1) {
			List<Integer> raiseOids = productPackageCouponService
					.getRaiseListByProductPackageOid(productPackage.getOid());
			pr.setRaiseRateCouponOids(raiseOids.toArray(new Integer[raiseOids.size()]));
			List<Object> raiseNames = tulipSdk.getCardNames(raiseOids);
			pr.setRaiseRateCouponNames(raiseNames.toArray(new String[raiseNames.size()]));
		}

		return pr;
	}

	/**
	 * 查询产品包
	 * 
	 * @param spec
	 * @param pageable
	 * @return {@link PagesRep<ProductPackageResp>}
	 *         ,如果返回的errCode属性等于0表示成功，否则表示失败，失败原因在errMessage里面体现
	 */
	public PageResp<ProductPackageResp> listPackage(Specification<ProductPackage> spec, Pageable pageable) {
		Page<ProductPackage> cas = this.productPackageDao.findAll(spec, pageable);
		PageResp<ProductPackageResp> pagesRep = new PageResp<ProductPackageResp>();
		if (cas != null && cas.getContent() != null && cas.getTotalElements() > 0) {
			List<ProductPackageResp> rows = new ArrayList<ProductPackageResp>();

			Map<String, Corporate> aoprateObjMap = new HashMap<String, Corporate>();
			Corporate corporate = null;
			for (ProductPackage p : cas) {
				ProductPackageResp queryRep = new ProductPackageResp(p);
				if (p.getAssetPool() != null && p.getAssetPool().getSpvEntity() != null) {
					if (aoprateObjMap.get(p.getAssetPool().getSpvEntity().getCorperateOid()) == null) {
						try {
							corporate = this.corporateDao.findOne(p.getAssetPool().getSpvEntity().getCorperateOid());
							if (corporate != null) {
								aoprateObjMap.put(p.getAssetPool().getSpvEntity().getCorperateOid(), corporate);
							}
						} catch (Exception e) {
						}
					}
					if (aoprateObjMap.get(p.getAssetPool().getSpvEntity().getCorperateOid()) != null) {
						queryRep.setSpvName(
								aoprateObjMap.get(p.getAssetPool().getSpvEntity().getCorperateOid()).getName());
					}
				}

				rows.add(queryRep);
			}
			pagesRep.setRows(rows);
		}
		pagesRep.setTotal(cas.getTotalElements());
		return pagesRep;
	}

	@Transactional
	public BaseResp aduitApply(List<String> oids, String operator) throws ParseException {
		BaseResp response = new BaseResp();

		List<ProductPackage> ps = productPackageDao.findByOidIn(oids);
		if (ps == null || ps.size() == 0) {
			throw AMPException.getException(90000);
		}

		Timestamp now = new Timestamp(System.currentTimeMillis());
		for (ProductPackage product : ps) {
			if (product == null || ProductPackage.YES.equals(product.getIsDeleted())) {
				throw AMPException.getException(90000);
			}
			if (product.getAssetPool() == null) {
				throw AMPException.getException(90011);
			}
			// 必须有协议书
			if (!isExistFiles(product)) {
				throw AMPException.getException(16002);
			}
			if (!ProductPackage.AUDIT_STATE_Nocommit.equals(product.getAuditState())) {
				throw AMPException.getException(90015);
			}
			product.setState(ProductPackage.STATE_Auditing);
			product.setAuditState(ProductPackage.AUDIT_STATE_Auditing);
			product.setOperator(operator);
			product.setUpdateTime(now);

			ProductPackageLog.ProductPackageLogBuilder plb = ProductPackageLog.builder().oid(StringUtil.uuid());
			{
				plb.product(product).auditType(ProductLog.AUDIT_TYPE_Auditing)
						.auditState(ProductLog.AUDIT_STATE_Commited).auditor(operator).auditTime(now);
			}

			this.productPackageDao.saveAndFlush(product);
			this.productPackageLogDao.save(plb.build());

		}

		return response;
	}

	/**
	 * 查询审核中
	 * 
	 * @param spec
	 * @param pageable
	 * @return
	 */
	public PageResp<ProductPackageLogListResp> auditList(Specification<ProductPackage> spec, Pageable pageable) {
		Page<ProductPackage> cas = this.productPackageDao.findAll(spec, pageable);
		PageResp<ProductPackageLogListResp> pagesRep = new PageResp<ProductPackageLogListResp>();
		if (cas != null && cas.getContent() != null && cas.getTotalElements() > 0) {
			List<ProductPackageLogListResp> rows = new ArrayList<ProductPackageLogListResp>();

			Map<String, AdminObj> adminObjMap = new HashMap<String, AdminObj>();
			Map<String, Corporate> aoprateObjMap = new HashMap<String, Corporate>();

			List<String> oids = new ArrayList<String>();
			for (ProductPackage p : cas) {
				oids.add(p.getOid());
			}
			Map<String, ProductPackageLog> plMap = this.getProductPackageLogs(oids,
					ProductPackageLog.AUDIT_TYPE_Auditing, ProductPackageLog.AUDIT_STATE_Commited);
			ProductPackageLog pl = null;

			Corporate corporate = null;
			AdminObj adminObj = null;
			for (ProductPackage p : cas) {
				ProductPackageLogListResp queryRep = new ProductPackageLogListResp(p);
				if (p.getAssetPool() != null && p.getAssetPool().getSpvEntity() != null) {
					if (aoprateObjMap.get(p.getAssetPool().getSpvEntity().getCorperateOid()) == null) {
						try {
							corporate = this.corporateDao.findOne(p.getAssetPool().getSpvEntity().getCorperateOid());
							if (corporate != null) {
								aoprateObjMap.put(p.getAssetPool().getSpvEntity().getCorperateOid(), corporate);
							}
						} catch (Exception e) {
						}
					}
					if (aoprateObjMap.get(p.getAssetPool().getSpvEntity().getCorperateOid()) != null) {
						queryRep.setSpvName(
								aoprateObjMap.get(p.getAssetPool().getSpvEntity().getCorperateOid()).getName());
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
	public PageResp<ProductPackageLogListResp> checkList(Specification<ProductPackage> spec, Pageable pageable) {
		PageResp<ProductPackageLogListResp> pagesRep = new PageResp<ProductPackageLogListResp>();

		Page<ProductPackage> cas = this.productPackageDao.findAll(spec, pageable);

		if (cas != null && cas.getContent() != null && cas.getTotalElements() > 0) {
			List<ProductPackageLogListResp> rows = new ArrayList<ProductPackageLogListResp>();

			List<String> oids = new ArrayList<String>();
			for (ProductPackage p : cas) {
				oids.add(p.getOid());
			}
			Map<String, ProductPackageLog> plMap1 = this.getProductPackageLogs(oids,
					ProductPackageLog.AUDIT_TYPE_Auditing, ProductPackageLog.AUDIT_STATE_Commited);// 提交记录
			Map<String, ProductPackageLog> plMap2 = this.getProductPackageLogs(oids,
					ProductPackageLog.AUDIT_TYPE_Auditing, ProductPackageLog.AUDIT_STATE_Approval);// 审核记录

			Map<String, AdminObj> adminObjMap = new HashMap<String, AdminObj>();
			AdminObj adminObj = null;
			Map<String, Corporate> aoprateObjMap = new HashMap<String, Corporate>();
			Corporate corporate = null;
			ProductPackageLog pl1 = null;
			ProductPackageLog pl2 = null;
			for (ProductPackage p : cas) {
				ProductPackageLogListResp queryRep = new ProductPackageLogListResp(p);
				if (p.getAssetPool() != null && p.getAssetPool().getSpvEntity() != null) {
					if (aoprateObjMap.get(p.getAssetPool().getSpvEntity().getCorperateOid()) == null) {
						try {
							corporate = this.corporateDao.findOne(p.getAssetPool().getSpvEntity().getCorperateOid());
							if (corporate != null) {
								aoprateObjMap.put(p.getAssetPool().getSpvEntity().getCorperateOid(), corporate);
							}
						} catch (Exception e) {
						}
					}
					if (aoprateObjMap.get(p.getAssetPool().getSpvEntity().getCorperateOid()) != null) {
						queryRep.setSpvName(
								aoprateObjMap.get(p.getAssetPool().getSpvEntity().getCorperateOid()).getName());
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
	public Map<String, ProductPackageLog> getProductPackageLogs(final List<String> oids, final String auditType,
			final String auditState) {
		Direction sortDirection = Direction.DESC;
		Sort sort = new Sort(new Order(sortDirection, "auditTime"));

		Specification<ProductPackageLog> spec = new Specification<ProductPackageLog>() {
			@Override
			public Predicate toPredicate(Root<ProductPackageLog> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.and(cb.equal(root.get("auditType").as(String.class), auditType),
						cb.equal(root.get("auditState").as(String.class), auditState));
			}
		};

		spec = Specifications.where(spec).and(new Specification<ProductPackageLog>() {
			@Override
			public Predicate toPredicate(Root<ProductPackageLog> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				Expression<String> exp = root.get("product").get("oid").as(String.class);
				In<String> in = cb.in(exp);
				for (String oid : oids) {
					in.value(oid);
				}
				return in;
			}
		});

		Map<String, ProductPackageLog> map = new HashMap<String, ProductPackageLog>();
		List<ProductPackageLog> pls = this.productPackageLogDao.findAll(spec, sort);
		if (null != pls && pls.size() > 0) {
			for (ProductPackageLog pl : pls) {
				if (map.get(pl.getProduct().getOid()) == null) {
					map.put(pl.getProduct().getOid(), pl);
				}
			}
		}
		return map;
	}

	private boolean isExistFiles(ProductPackage product) {

		if (StringUtil.isEmpty(product.getInvestFileKey())) {// 风险提示书
			return false;
		} else {// 风险提示书
			List<File> infiles = this.fileService.list(product.getInvestFileKey(), File.STATE_Valid);
			if (infiles == null || infiles.size() == 0) {
				return false;
			}
		}
		if (StringUtil.isEmpty(product.getServiceFileKey())) {// 认购协议
			return false;
		} else {// 认购协议
			List<File> sfiles = this.fileService.list(product.getServiceFileKey(), File.STATE_Valid);
			if (sfiles == null || sfiles.size() == 0) {
				return false;
			}
		}
		if (StringUtil.isEmpty(product.getFileKeys())) {// 授权书
			return false;
		} else {// 授权书
			List<File> sfiles = this.fileService.list(product.getFileKeys(), File.STATE_Valid);
			if (sfiles == null || sfiles.size() == 0) {
				return false;
			}
		}
		return true;
	}

	@Transactional
	public BaseResp aduitApprove(String oid, String operator) throws ParseException {
		BaseResp response = new BaseResp();

		ProductPackage product = this.getProductPackageByOid(oid);

		if (!ProductPackage.AUDIT_STATE_Auditing.equals(product.getAuditState())) {
			throw AMPException.getException(90013);
		}

		Timestamp now = new Timestamp(System.currentTimeMillis());
		product.setState(ProductPackage.STATE_Auditpass);
		product.setAuditState(ProductPackage.AUDIT_STATE_Reviewing);
		product.setOperator(operator);
		product.setUpdateTime(now);

		ProductPackageLog.ProductPackageLogBuilder plb = ProductPackageLog.builder().oid(StringUtil.uuid());
		{
			plb.product(product).auditType(ProductPackageLog.AUDIT_TYPE_Auditing)
					.auditState(ProductPackageLog.AUDIT_STATE_Approval).auditor(operator).auditTime(now);
		}

		this.productPackageDao.saveAndFlush(product);
		this.productPackageLogDao.save(plb.build());

		return response;
	}

	@Transactional
	public BaseResp aduitReject(String oid, String auditComment, String operator) throws ParseException {
		BaseResp response = new BaseResp();

		ProductPackage product = this.getProductPackageByOid(oid);
		if (!Product.AUDIT_STATE_Auditing.equals(product.getAuditState())) {
			throw AMPException.getException(90013);
		}
		Timestamp now = new Timestamp(System.currentTimeMillis());
		product.setState(Product.STATE_Auditfail);
		product.setAuditState(Product.AUDIT_STATE_Nocommit);
		product.setOperator(operator);
		product.setUpdateTime(now);
		// 竞猜宝修改
		// if(product.getGuess()!=null){
		// //解锁竞猜活动
		// this.guessService.modiyStatusByOid(product.getGuess().getOid(),GuessEntity.GUESS_STATUS_LOCKED,GuessEntity.GUESS_STATUS_CREATED);
		// }
		// 竞猜宝修改
		ProductPackageLog.ProductPackageLogBuilder plb = ProductPackageLog.builder().oid(StringUtil.uuid());
		{
			plb.product(product).auditType(ProductPackageLog.AUDIT_TYPE_Auditing)
					.auditState(ProductPackageLog.AUDIT_STATE_Reject).auditor(operator).auditTime(now)
					.auditComment(auditComment);
		}

		this.productPackageDao.saveAndFlush(product);
		this.productPackageLogDao.save(plb.build());

		return response;
	}

	@Transactional
	public BaseResp reviewApprove(String oid, String operator) throws ParseException {
		BaseResp response = new BaseResp();

		ProductPackage product = this.getProductPackageByOid(oid);
		if (!ProductPackage.AUDIT_STATE_Reviewing.equals(product.getAuditState())) {
			throw AMPException.getException(90014);
		}
		Timestamp now = new Timestamp(System.currentTimeMillis());
		product.setState(ProductPackage.STATE_Reviewpass);
		product.setAuditState(ProductPackage.AUDIT_STATE_Reviewed);
		product.setOperator(operator);
		product.setUpdateTime(now);

		// 募集开始日期,募集结束日期
		if (ProductPackage.DATE_TYPE_FirstRackTime.equals(product.getRaiseStartDateType())) {
			Date nowDate = DateUtil
					.parseToSqlDate(new SimpleDateFormat(DateUtil.datetimePattern).format(new java.util.Date()));
			product.setRaiseStartDate(nowDate);

			Date raiseEndDate = DateUtil.addSQLDays(nowDate, product.getRaisePeriodDays() - 1);
			product.setRaiseEndDate(raiseEndDate);// 募集结束时间

			Date setupDate = DateUtil.addSQLDays(raiseEndDate, product.getFoundDays());
			product.setSetupDate(setupDate);// 最晚产品成立时间

			Date durationPeriodEndDate = DateUtil.addSQLDays(setupDate, product.getDurationPeriodDays() - 1);
			product.setDurationPeriodEndDate(durationPeriodEndDate);// 存续期结束时间

			// 到期最晚还本付息日 指存续期结束后的还本付息最迟发生在存续期后的第X个自然日的23:59:59为止
			Date repayDate = DateUtil.addSQLDays(durationPeriodEndDate, product.getAccrualRepayDays());
			// 到期最晚还本付息日 指存续期结束后的还本付息最迟发生在存续期后的第X个自然日的23:59:59为止
			product.setRepayDate(repayDate);// 到期还款时间
			// pp.setRaiseStartDate(null);
		}

		ProductPackageLog.ProductPackageLogBuilder plb = ProductPackageLog.builder().oid(StringUtil.uuid());
		{
			plb.product(product).auditType(ProductPackageLog.AUDIT_TYPE_Reviewing)
					.auditState(ProductPackageLog.AUDIT_STATE_Approval).auditor(operator).auditTime(now);
		}

		this.productPackageDao.saveAndFlush(product);
		this.productPackageLogDao.save(plb.build());

		// 复核通过后新建本产品包的第一个产品（通过定时任务创建产品）
		// productService.createProductFromPackage(product);

		return response;
	}

	@Transactional
	public BaseResp reviewReject(String oid, String auditComment, String operator) throws ParseException {
		BaseResp response = new BaseResp();

		ProductPackage product = this.getProductPackageByOid(oid);
		if (!Product.AUDIT_STATE_Reviewing.equals(product.getAuditState())) {
			throw AMPException.getException(90014);
		}
		Timestamp now = new Timestamp(System.currentTimeMillis());
		product.setState(Product.STATE_Reviewfail);
		product.setAuditState(Product.AUDIT_STATE_Auditing);
		product.setOperator(operator);
		product.setUpdateTime(now);
		// 竞猜宝修改
		// if(product.getGuess()!=null){
		// //解锁竞猜活动
		// this.guessService.modiyStatusByOid(product.getGuess().getOid(),GuessEntity.GUESS_STATUS_LOCKED,GuessEntity.GUESS_STATUS_CREATED);
		// }
		// 竞猜宝修改
		ProductPackageLog.ProductPackageLogBuilder plb = ProductPackageLog.builder().oid(StringUtil.uuid());
		{
			plb.product(product).auditType(ProductPackageLog.AUDIT_TYPE_Reviewing)
					.auditState(ProductPackageLog.AUDIT_STATE_Reject).auditor(operator).auditTime(now)
					.auditComment(auditComment);
		}

		this.productPackageDao.saveAndFlush(product);
		this.productPackageLogDao.save(plb.build());

		return response;
	}

	@Transactional
	public long validateSingle(final String attrName, final String value, final String oid) {

		Specification<ProductPackage> spec = new Specification<ProductPackage>() {
			@Override
			public Predicate toPredicate(Root<ProductPackage> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				if (StringUtil.isEmpty(oid)) {
					return cb.and(cb.equal(root.get("isDeleted").as(String.class), ProductPackage.NO),
							cb.equal(root.get(attrName).as(String.class), value));
				} else {
					return cb.and(cb.equal(root.get("isDeleted").as(String.class), ProductPackage.NO),
							cb.equal(root.get(attrName).as(String.class), value),
							cb.notEqual(root.get("oid").as(String.class), oid));
				}
			}
		};
		spec = Specifications.where(spec);

		return this.productPackageDao.count(spec);
	}


	public ProductPackage findByOid(String productOid) {
		ProductPackage product = this.productPackageDao.findOne(productOid);
		if (null == product) {
			throw new AMPException("产品不存在");
		}
		return product;
	}

	/**
	 * 根据竞猜宝id查询产品包
	 * 
	 * @param oid
	 * @return
	 */
	public ProductPackage getByGuessOid(String guessOid) {

		return productPackageDao.findByGuessOid(guessOid);
	}

	@Transactional
	public BaseRep loanProductPackageSave(CompanyLoanProductReq req) {
		logger.info("【企业散标产品推送】入参信息：{}", req);
		BaseRep rep = new BaseRep();
		if (validateSingle("code", req.getProductCode(), null) > 0 || productService.validateSingle("code", req.getProductCode(), null) > 0) {
			rep.setErrorCode(0);
			rep.setErrorMessage("产品已推送成功");
			return rep;
		}
		SavePeriodicProductPackageForm form = initLoanProductForm(req);
		periodicPackage(form, "LOAN_AUTO");
		LoanContract loanContract = initLoanContract(req);
		loanContractDao.save(loanContract);
		logger.info("【企业散标产品推送】新建完成！~");
		return rep;
	}

	private LoanContract initLoanContract(CompanyLoanProductReq req) {
		LoanContract contract = new LoanContract();
		contract.setCode(req.getProductCode());
		contract.setName(req.getProductName());
		contract.setLoanPeriod(req.getLoanPeriod());
		contract.setLoanVolume(req.getLoanAmount());
		contract.setLoanRatio(req.getLoanRatio());
		contract.setLoanUsage(req.getLoanUsage());
		contract.setRefundMode(req.getRefundMode());
		contract.setOrgCode(req.getOrgCode());
		contract.setOrgName(req.getOrgName());
		contract.setOrgCorporationName(req.getOrgCorporationName());
		contract.setRegisteredCapital(req.getRegisteredCapital());
		contract.setSetupDate(req.getSetupDate());
		contract.setOrgAddress(req.getOrgAddress());
		return contract;
	}

	private SavePeriodicProductPackageForm initLoanProductForm(CompanyLoanProductReq req) {
		SavePeriodicProductPackageForm form = new SavePeriodicProductPackageForm();
		form.setCode(req.getProductCode());
		form.setName(req.getProductName());
		form.setFullName(req.getProductName());
		form.setAdministrator(req.getOrgName());
		form.setTypeOid(Product.TYPE_Producttype_01);
		form.setAssetPoolOid(loanProductAssetPoolOid);
		form.setExpAror(req.getLoanRatio());
		form.setExpArorSec(req.getLoanRatio());
		form.setIncomeCalcBasis("365");
		form.setOperationRate(BigDecimal.ZERO);
		form.setCurrency("CNY");
		form.setRaiseStartDateType(Product.DATE_TYPE_FirstRackTime);
		form.setRaiseStartDate(DateUtil.getSqlDate());
		form.setSingleProductVolume(req.getLoanAmount());
		form.setProductCount(1);
		form.setSubscribeConfirmDays(0);
		form.setRaisePeriod(20);
		form.setLimitTime("0.5");
		form.setRaisedTotalNumber(req.getLoanAmount());
		form.setFoundDays(0);
		form.setInterestsFirstDate(0);
		form.setDurationPeriod(req.getLoanPeriod() * 30);
		form.setAccrualDate(3);
		form.setRaiseFullFoundType(Product.RAISE_FULL_FOUND_TYPE_AUTO);
		form.setAutoFoundDays(0);
		form.setInvestMin(new BigDecimal(5000));
		form.setInvestAdditional(new BigDecimal(1000));
		form.setNetUnitShare(BigDecimal.ONE);
		form.setBasicProductLabel(loanProductLabelOid);
		form.setInvestDateType(Product.Product_dateType_T);
		String[] channelOid = JSON.parseObject(loanProductChannelOid, String[].class);
		form.setChannelOids(channelOid);
		form.setRelateGuess(0);
		form.setUseRedPackages(2);
		form.setUseraiseRateCoupons(2);
		form.setActivityDetail("");
		form.setProductElement(loanProductElementOid);
		form.setProductIntro(loanProductIntroOId);
		form.setIfP2P(1);
		form.setIsP2PAssetPackage(Product.IS_P2P_ASSET_PACKAGE_2);
		form.setIsActivityProduct(0);
		form.setReveal("YES");
		form.setInstruction(loanProductInstruction);
		form.setRiskLevel("R1");
		form.setInvestorLevel("conservative");
		form.setInvestFile(loanProductInvestFile);
		form.setServiceFile(loanProductServiceFile);
		form.setFiles(loanProductFiles);
		return form;
	}

}
