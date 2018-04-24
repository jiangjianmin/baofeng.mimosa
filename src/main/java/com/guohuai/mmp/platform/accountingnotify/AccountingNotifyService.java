package com.guohuai.mmp.platform.accountingnotify;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSONObject;
import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductService;
import com.guohuai.ams.product.reward.ProductIncomeReward;
import com.guohuai.component.config.AccountingDefineConfig;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.DecimalUtil;
import com.guohuai.component.web.view.PagesRep;
import com.guohuai.mmp.investor.tradeorder.InvestorInvestTradeOrderService;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderService;
import com.guohuai.mmp.platform.publisher.offset.Money;

@Service
@Transactional
public class AccountingNotifyService {
	Logger logger = LoggerFactory.getLogger(AccountingNotifyService.class);
	@Autowired
	AccountingNotifyDao notifyDao;
	
	
	
	@Autowired
	ProductService productService;
	
	@Autowired
	AccountingNotifyDao accountingNotifyDao;
	@Autowired
	private InvestorTradeOrderService investorTradeOrderService;
	@Autowired
	InvestorInvestTradeOrderService investorInvestTradeOrderService;
	
	public AccountingNotifyEntity save(AccountingNotifyEntity orderLog){
		return this.notifyDao.save(orderLog);
	}
	
	//@Transactional(value=TxType.REQUIRES_NEW)
	public AccountingNotifyEntity create(String notifyType,String notifyContent,LeAccounting le){
		AccountingNotifyEntity orderLog=new AccountingNotifyEntity();
		//orderLog.setNotifyId(this.seqGenerator.next(CodeConstants.OrderLog_notifyId));
		orderLog.setNotifyType(notifyType);
		orderLog.setNotifyStatus(AccountingNotifyEntity.ORDERLOG_notifyStatus_toConfirm);
		orderLog.setNotifyContent(notifyContent);
		logger.info("========================notifyContent=================="+notifyContent);
		return save(orderLog);
	}
	
	public AccountingNotifyEntity create(String notifyType,String notifyContent,
			String busDate,String productOid,String channelOid,BigDecimal costFee){
		AccountingNotifyEntity orderLog=new AccountingNotifyEntity();
		//orderLog.setNotifyId(this.seqGenerator.next(CodeConstants.OrderLog_notifyId));
		orderLog.setNotifyType(notifyType);
		orderLog.setNotifyStatus(AccountingNotifyEntity.ORDERLOG_notifyStatus_toConfirm);
		orderLog.setNotifyContent(notifyContent);
		orderLog.setBusDate(DateUtil.parseToSqlDate(busDate));
		orderLog.setProductOid(productOid);
		orderLog.setChannelOid(channelOid);
		orderLog.setCostFee(costFee);
		logger.info("========================notifyContent=================="+notifyContent);
		return save(orderLog);
	}
	
	/**
	 * 申购确认
	 * @param order
	 * @return
	 */
	public AccountingNotifyEntity createInvest(InvestorTradeOrderEntity order){
		LeAccountingInvest invest=new LeAccountingInvest();
		invest.setEvent_code(AccountingNotifyEntity.NOTIFY_EVENTCODE_QRLCCP);
		invest.setData_date(DateUtil.format(new java.util.Date()));
		invest.setProduct_code(order.getProduct().getOid());
		invest.setProduct_name(order.getProduct().getName());
		invest.setProduct_type(AccountingDefineConfig.define.get(order.getProduct().getType().getOid()));
		invest.setBook_value(DecimalUtil.setScaleDown(order.getOrderAmount()).abs().toString());
		invest.setLx_order_number(order.getOrderCode());
		invest.setCustomer_id(order.getInvestorBaseAccount().getUserOid());
		invest.setBatch_no(DateUtil.getCurrentDate());
//		invest.setBusiness_date(DateUtil.format(this.investorTradeOrderService.getConfirmDate(order.getProduct(), order.getOrderTime())));
		String businessCodeName = order.getProduct().getType().getOid().concat(AccountingNotifyEntity.BUSINESS_CODE_SUFFIX);
		invest.setBusiness_code(AccountingDefineConfig.define.get(businessCodeName));
		//invest.setSerial(this.seqGenerator.nextLong(CodeConstants.OrderLog_accoutingNotifyId));
		logger.info(JSONObject.toJSONString("=======申购确认==============="+invest));
//		return this.create(AccountingNotifyEntity.NOTIFY_notifyType_invest, JSONObject.toJSONString(invest),invest);
		return this.create(AccountingNotifyEntity.NOTIFY_notifyType_invest, JSONObject.toJSONString(invest),
				invest.getBusiness_date(),order.getProduct().getOid(),order.getChannel().getOid(),null);
	}
	
	/**
	 * 赎回确认
	 * @param order
	 * @return
	 */
	public AccountingNotifyEntity createRedeem(InvestorTradeOrderEntity order,BigDecimal deductedAmount){
		LeAccountingRedeem redeem=new LeAccountingRedeem();
		redeem.setEvent_code(AccountingNotifyEntity.NOTIFY_EVENTCODE_DFLCCP);
		redeem.setData_date(DateUtil.format(new java.util.Date()));
		redeem.setBatch_no(DateUtil.getCurrentDate());
		redeem.setProduct_code(order.getProduct().getOid());
		redeem.setProduct_type(AccountingDefineConfig.define.get(order.getProduct().getType().getOid()));
		redeem.setPaid_corpus(DecimalUtil.setScaleDown(deductedAmount).abs().toString());
		redeem.setLx_order_number(order.getOrderCode());
		redeem.setCustomer_id(order.getInvestorBaseAccount().getUserOid());
	    ///赎回针对投资单无赎回确认日期 
		//redeem.setBusiness_date(DateUtil.format(order.getRedeemConfirmDate()));
		redeem.setBusiness_date(DateUtil.format(new java.util.Date()));
		String businessCodeName = order.getProduct().getType().getOid().concat(AccountingNotifyEntity.BUSINESS_CODE_SUFFIX);
		redeem.setBusiness_code(AccountingDefineConfig.define.get(businessCodeName));
		//redeem.setSerial(this.seqGenerator.nextLong(CodeConstants.OrderLog_accoutingNotifyId));
		logger.info(JSONObject.toJSONString("=======赎回确认明细==============="+JSONObject.toJSONString(redeem)));
//		return this.create(AccountingNotifyEntity.NOTIFY_notifyType_redeem, JSONObject.toJSONString(redeem),redeem);
		return this.create(AccountingNotifyEntity.NOTIFY_notifyType_redeem, JSONObject.toJSONString(redeem),
				redeem.getBusiness_date(),order.getProduct().getOid(),order.getChannel().getOid(),null);
	}
	
	/**
	 * 投资者收益明细
	 * @param order
	 * @return
	 */
	public AccountingNotifyEntity createInvestorIncome(InvestorTradeOrderEntity holdApart, BigDecimal apartInerestAmount, Date incomeDate){
		LeAccountingInvestorIncome investorIncome=new LeAccountingInvestorIncome();
		investorIncome.setEvent_code(AccountingNotifyEntity.NOTIFY_EVENTCODE_JTKHLX);
		investorIncome.setData_date(DateUtil.format(new java.util.Date()));
		investorIncome.setBatch_no(DateUtil.getCurrentDate());
		investorIncome.setProduct_code(holdApart.getProduct().getOid());
		investorIncome.setProduct_type(AccountingDefineConfig.define.get(holdApart.getProduct().getType().getOid()));
		investorIncome.setProv_customer_rvn(DecimalUtil.setScaleDown(apartInerestAmount).abs().toString());
		investorIncome.setLx_order_number(holdApart.getOrderCode());
		investorIncome.setProv_start_date(DateUtil.format(incomeDate));
		investorIncome.setProv_end_date(DateUtil.format(incomeDate));
		investorIncome.setCustomer_id(holdApart.getInvestorBaseAccount().getUserOid());
		investorIncome.setBusiness_date(DateUtil.format(incomeDate));
		String businessCodeName = holdApart.getProduct().getType().getOid().concat(AccountingNotifyEntity.BUSINESS_CODE_SUFFIX);
		investorIncome.setBusiness_code(AccountingDefineConfig.define.get(businessCodeName));
		//investorIncome.setSerial(this.seqGenerator.nextLong(CodeConstants.OrderLog_accoutingNotifyId));
		logger.info(JSONObject.toJSONString("=======投资者收益明细==============="+JSONObject.toJSONString(investorIncome)));
//		return this.create(AccountingNotifyEntity.NOTIFY_notifyType_investorIncome, JSONObject.toJSONString(investorIncome),investorIncome);
		return this.create(AccountingNotifyEntity.NOTIFY_notifyType_investorIncome, JSONObject.toJSONString(investorIncome),
				investorIncome.getBusiness_date(),holdApart.getProduct().getOid(),holdApart.getChannel().getOid(),null);
	}
	
	/**
	 * 投资者收益明细
	 * @param holdApart
	 * @param bookAmount
	 * @param apartInerestAmount
	 * @param incomeAmount
	 * @param rewardAmount
	 * @param reward
	 * @param incomeDate
	 * @return
	 */
	public AccountingNotifyEntity createInvestorIncome(InvestorTradeOrderEntity holdApart,BigDecimal bookAmount, BigDecimal apartInerestAmount,
			BigDecimal incomeAmount, BigDecimal rewardAmount, ProductIncomeReward reward, Date incomeDate) {
			LeAccountingInvestorIncome investorIncome=new LeAccountingInvestorIncome();
			investorIncome.setEvent_code(AccountingNotifyEntity.NOTIFY_EVENTCODE_JTKHLX);
			investorIncome.setData_date(DateUtil.format(new java.util.Date()));
			investorIncome.setBatch_no(DateUtil.getCurrentDate());
			investorIncome.setProduct_code(holdApart.getProduct().getOid());
			investorIncome.setProduct_type(AccountingDefineConfig.define.get(holdApart.getProduct().getType().getOid()));
			investorIncome.setProv_customer_rvn(DecimalUtil.setScaleDown(apartInerestAmount).abs().toString());
			investorIncome.setLx_order_number(holdApart.getOrderCode());
			investorIncome.setProv_start_date(DateUtil.format(incomeDate));
			investorIncome.setProv_end_date(DateUtil.format(incomeDate));
			investorIncome.setCustomer_id(holdApart.getInvestorBaseAccount().getUserOid());
			investorIncome.setBusiness_date(DateUtil.format(incomeDate));
			String businessCodeName = holdApart.getProduct().getType().getOid().concat(AccountingNotifyEntity.BUSINESS_CODE_SUFFIX);
			investorIncome.setBusiness_code(AccountingDefineConfig.define.get(businessCodeName));
			investorIncome.setStart_date(holdApart.getBeginAccuralDate().toString());
			investorIncome.setBasic_profit_interest(incomeAmount.toString());
			investorIncome.setStep_profit_interest(rewardAmount.toString());
			investorIncome.setStep_profit_rate("0");
			if (reward != null && reward.getRatio() != null) {
				investorIncome.setStep_profit_rate(reward.getRatio().toString());
			}
			investorIncome.setBook_value(DecimalUtil.setScaleDown(bookAmount).toString());
			investorIncome.setBearing_base(AccountingDefineConfig.define.get(holdApart.getProduct().getIncomeCalcBasis()));
//			investorIncome.setSerial(this.seqGenerator.nextLong(CodeConstants.OrderLog_accoutingNotifyId));
			logger.info(JSONObject.toJSONString("=======投资者收益明细==============="+JSONObject.toJSONString(investorIncome)));
			return this.create(AccountingNotifyEntity.NOTIFY_notifyType_investorIncome, JSONObject.toJSONString(investorIncome),
					investorIncome.getBusiness_date(),holdApart.getProduct().getOid(),holdApart.getChannel().getOid(),null);
	}
	
	/**
	 * 确认费用 平台支付的费用
	 * @param productOid
	 * @param return_service_expense
	 * @return
	 */
	public AccountingNotifyEntity createPlatformFee(Product product,String channelOid,BigDecimal return_service_expense){
		LeAccountingPlatformFee platformFee=new LeAccountingPlatformFee();
		platformFee.setEvent_code(AccountingNotifyEntity.NOTIFY_EVENTCODE_QRFY);
		platformFee.setData_date(DateUtil.format(new java.util.Date()));
		platformFee.setBatch_no(DateUtil.getCurrentDate());
		platformFee.setProduct_code(product.getOid());
		platformFee.setProduct_type(AccountingDefineConfig.define.get(product.getType().getOid()));
		String businessCodeName = product.getType().getOid().concat(AccountingNotifyEntity.BUSINESS_CODE_SUFFIX);
		platformFee.setBusiness_code(AccountingDefineConfig.define.get(businessCodeName));
		platformFee.setBusiness_date(DateUtil.getBeforeDate().toString());
		platformFee.setReturn_service_expense(DecimalUtil.setScaleDown(return_service_expense).abs().toString());
		platformFee.setCustomer_id(AccountingDefineConfig.define.get(AccountingNotifyEntity.PLATFORM_CUSTOMER_ID));
		platformFee.setCustomer_account(AccountingDefineConfig.define.get(AccountingNotifyEntity.PLATFORM_CUSTOMER_ACCOUNT));
		//platformFee.setSerial(this.seqGenerator.nextLong(CodeConstants.OrderLog_accoutingNotifyId));
//		return this.create(AccountingNotifyEntity.NOTIFY_notifyType_platformFee, JSONObject.toJSONString(platformFee),platformFee);
		return this.create(AccountingNotifyEntity.NOTIFY_notifyType_platformFee, JSONObject.toJSONString(platformFee),
				platformFee.getBusiness_date(),product.getOid(),channelOid,return_service_expense.setScale(2, RoundingMode.FLOOR).abs());
	}

	/**
	 * 相关费用录入处理
	 * @param list
	 */
	public void offsetMoney(List<Money> list) {
		if(!CollectionUtils.isEmpty(list)){
			for (Money en : list) {
				String productOid = en.getProductOid();  //产品id
				Product product = productService.getProductByOid(productOid);
				String eventType = en.getType();    //事件类型
				if (StringUtils.isEmpty(eventType) || StringUtils.isEmpty(en.getBusDate())
						|| StringUtils.isEmpty(en.getAccountInfo()) || StringUtils.isEmpty(en.getAccountType())
						|| StringUtils.isEmpty(productOid)) {
					throw new AMPException("传参有误");
				}
				
				if (eventType.equals(AccountingNotifyEntity.NOTIFY_notifyType_offsetPay)) {
					if (en.getMoney() != null && en.getMoney().compareTo(new BigDecimal(0)) > 0) {
						createPay(product,en);
					}
					BigDecimal couFee = en.getCouFee();  //轧差支付交易交易手续费
					if (couFee != null && couFee.compareTo(new BigDecimal(0)) > 0) {//轧差支付手续费不为空入一条记录
						createPayCouFee(product,en,AccountingNotifyEntity.NOTIFY_notifyType_offsetPayFee);
					}
				} else if (eventType.equals(AccountingNotifyEntity.NOTIFY_notifyType_offsetCollect)) {
					if (en.getMoney() != null && en.getMoney().compareTo(new BigDecimal(0)) > 0) {
						createCollect(product,en);
					}
				} else if (eventType.equals(AccountingNotifyEntity.NOTIFY_notifyType_payPlatform)) {
					if (en.getMoney() != null && en.getMoney().compareTo(new BigDecimal(0)) > 0) {
						createPayPlatformFee(product,en);
					}
					BigDecimal couFee = en.getCouFee();  //支付费用交易手续费
					if (couFee != null && couFee.compareTo(new BigDecimal(0)) > 0) {//支付费用手续费不为空入一条记录
						createPayCouFee(product,en,AccountingNotifyEntity.NOTIFY_notifyType_payPlatformFee);
					}
				}
			}
		}
	}
	
	/**
	 * 支付费用
	 * @param product
	 * @param money
	 * @return
	 */
	public AccountingNotifyEntity createPayPlatformFee(Product product, Money money) {
		LeAccountingPayPlatformFee payPlatformFee = new LeAccountingPayPlatformFee();
		payPlatformFee.setEvent_code(AccountingNotifyEntity.NOTIFY_EVENTCODE_ZFFY);
		payPlatformFee.setBatch_no(DateUtil.getCurrentDate());
		payPlatformFee.setData_date(DateUtil.format(new java.util.Date()));
		payPlatformFee.setAccount_info(money.getAccountInfo());
		payPlatformFee.setAccount_type(money.getAccountType());
		payPlatformFee.setProduct_code(product.getOid());
		payPlatformFee.setIs_combine_pay(money.getCombinePay());
		payPlatformFee.setAccount_amount(money.getActualAmount().setScale(2, RoundingMode.FLOOR).abs().toString());
		String businessCodeName = product.getType().getOid().concat(AccountingNotifyEntity.BUSINESS_CODE_SUFFIX);
		payPlatformFee.setBusiness_date(money.getBusDate());
		payPlatformFee.setBusiness_code(AccountingDefineConfig.define.get(businessCodeName));
		payPlatformFee.setCustomer_id(money.getCustomerId());
		payPlatformFee.setCustomer_account(money.getCustomerInfo());
//		payPlatformFee.setCustomer_id(AccountingDefineConfig.define.get(AccountingNotifyEntity.PLATFORM_CUSTOMER_ID));
//		payPlatformFee.setCustomer_account(AccountingDefineConfig.define.get(AccountingNotifyEntity.PLATFORM_CUSTOMER_ACCOUNT));
		payPlatformFee.setProduct_type(AccountingDefineConfig.define.get(product.getType().getOid()));
		payPlatformFee.setPaid_service_expense(money.getMoney().setScale(2, RoundingMode.FLOOR).abs().toString());
//		payPlatformFee.setSerial(this.seqGenerator.nextLong(CodeConstants.OrderLog_accoutingNotifyId));
//		return this.create(AccountingNotifyEntity.NOTIFY_notifyType_payPlatform, JSONObject.toJSONString(payPlatformFee),payPlatformFee);
		return this.create(AccountingNotifyEntity.NOTIFY_notifyType_payPlatform, JSONObject.toJSONString(payPlatformFee),
				payPlatformFee.getBusiness_date(),product.getOid(),money.getChannelOid(),money.getActualAmount().setScale(2, RoundingMode.FLOOR).abs());
	}
	
	/**
	 * 坐扣手续费 (包括轧支付或者支付乐信)
	 * @param product
	 * @param money
	 * @return
	 */
	public AccountingNotifyEntity createPayCouFee(Product product, Money money, String notifyType) {
		LeAccountingPayCouFee couFee = new LeAccountingPayCouFee();
		
		if (AccountingNotifyEntity.NOTIFY_notifyType_offsetPay.equals(money.getType())){
			couFee.setEvent_code(AccountingNotifyEntity.NOTIFY_EVENTCODE_GZZF);
		} else { //支付费用的手续费
			couFee.setEvent_code(AccountingNotifyEntity.NOTIFY_EVENTCODE_ZFFY);
		}
		couFee.setPaid_pay_expense(money.getCouFee().setScale(2, RoundingMode.FLOOR).abs().toString());
		couFee.setData_date(DateUtil.format(new java.util.Date()));
		couFee.setBatch_no(DateUtil.getCurrentDate());
		couFee.setAccount_type(money.getAccountType());
		couFee.setAccount_info(money.getAccountInfo());
		couFee.setCustomer_id(money.getCouCustomerId());
		couFee.setCustomer_account(money.getCouCustomerInfo());
		couFee.setBusiness_date(money.getBusDate());
		String businessCodeName = product.getType().getOid().concat(AccountingNotifyEntity.BUSINESS_CODE_SUFFIX);
		couFee.setBusiness_code(AccountingDefineConfig.define.get(businessCodeName));
//		couFee.setSerial(this.seqGenerator.nextLong(CodeConstants.OrderLog_accoutingNotifyId));
//		return this.create(notifyType, JSONObject.toJSONString(couFee),couFee);
		return this.create(notifyType, JSONObject.toJSONString(couFee),
				couFee.getBusiness_date(),product.getOid(),money.getChannelOid(),money.getCouFee().setScale(2, RoundingMode.FLOOR).abs());
	}
	
	/**
	 * 轧差收款
	 * @param product
	 * @param money
	 * @return
	 */
	public AccountingNotifyEntity createCollect(Product product, Money money){
		LeAccountingCollect collect=new LeAccountingCollect();
		collect.setEvent_code(AccountingNotifyEntity.NOTIFY_EVENTCODE_GZSK);
		collect.setData_date(DateUtil.format(new java.util.Date()));
		collect.setBatch_no(DateUtil.getCurrentDate());
		collect.setAccount_info(money.getAccountInfo());
		collect.setAccount_type(money.getAccountType());
		collect.setBusiness_date(money.getBusDate());
		String businessCodeName = product.getType().getOid().concat(AccountingNotifyEntity.BUSINESS_CODE_SUFFIX);
		collect.setBusiness_code(AccountingDefineConfig.define.get(businessCodeName));
		collect.setProduct_code(product.getOid());
		collect.setProduct_type(AccountingDefineConfig.define.get(product.getType().getOid()));
		collect.setRisk_level(AccountingDefineConfig.define.get(product.getRiskLevel()));
		collect.setBearing_base(AccountingDefineConfig.define.get(product.getIncomeCalcBasis()));
		collect.setExpect_annual_rate(product.getExpAror().toString());
		collect.setIs_combine_pay(money.getCombinePay());
		collect.setAccount_amount(money.getActualAmount().toString());
		collect.setBalanced_account(money.getMoney().setScale(2, RoundingMode.FLOOR).abs().toString());
		collect.setCustomer_id(money.getCustomerId());
		collect.setCustomer_account(money.getCustomerInfo());
//		collect.setCustomer_id(AccountingDefineConfig.define.get(AccountingNotifyEntity.PLATFORM_CUSTOMER_ID));
//		collect.setCustomer_account(AccountingDefineConfig.define.get(AccountingNotifyEntity.PLATFORM_CUSTOMER_ACCOUNT));
//		collect.setSerial(this.seqGenerator.nextLong(CodeConstants.OrderLog_accoutingNotifyId));
		//return this.create(AccountingNotifyEntity.NOTIFY_notifyType_offsetCollect, JSONObject.toJSONString(collect),collect);
		return this.create(AccountingNotifyEntity.NOTIFY_notifyType_offsetCollect, JSONObject.toJSONString(collect),
				collect.getBusiness_date(),product.getOid(),"",money.getMoney().setScale(2, RoundingMode.FLOOR).abs());
	}
	
	/**
	 * 轧差支付
	 * @param product
	 * @param money
	 * @return
	 */
	public AccountingNotifyEntity createPay(Product product, Money money){
		LeAccountingPay pay=new LeAccountingPay();
		pay.setEvent_code(AccountingNotifyEntity.NOTIFY_EVENTCODE_GZZF);
		pay.setData_date(DateUtil.format(new java.util.Date()));
		pay.setBatch_no(DateUtil.getCurrentDate());
		pay.setAccount_info(money.getAccountInfo());
		pay.setAccount_type(money.getAccountType());
		pay.setExpect_annual_rate(product.getExpAror().toString());
		pay.setProduct_code(product.getOid());
		pay.setProduct_type(AccountingDefineConfig.define.get(product.getType().getOid()));
		pay.setIs_combine_pay(money.getCombinePay());
		pay.setAccount_amount(money.getActualAmount().toString());
		pay.setBusiness_date(money.getBusDate());
		String businessCodeName = product.getType().getOid().concat(AccountingNotifyEntity.BUSINESS_CODE_SUFFIX);
		pay.setBusiness_code(AccountingDefineConfig.define.get(businessCodeName));
		pay.setRisk_level(AccountingDefineConfig.define.get(product.getRiskLevel()));
		pay.setBearing_base(AccountingDefineConfig.define.get(product.getIncomeCalcBasis()));
		pay.setBalanced_account(money.getMoney().setScale(2, RoundingMode.FLOOR).abs().toString());
		pay.setCustomer_id(money.getCustomerId());
		pay.setCustomer_account(money.getCustomerInfo());
		pay.setIs_combine_pay(money.getCombinePay());
		pay.setAccount_amount(money.getActualAmount().toString());
//		pay.setCustomer_id(AccountingDefineConfig.define.get(AccountingNotifyEntity.PLATFORM_CUSTOMER_ID));
//		pay.setCustomer_account(AccountingDefineConfig.define.get(AccountingNotifyEntity.PLATFORM_CUSTOMER_ACCOUNT));
//		pay.setSerial(this.seqGenerator.nextLong(CodeConstants.OrderLog_accoutingNotifyId));
//		return this.create(AccountingNotifyEntity.NOTIFY_notifyType_offsetPay, JSONObject.toJSONString(pay),pay);
		return this.create(AccountingNotifyEntity.NOTIFY_notifyType_offsetPay, JSONObject.toJSONString(pay),
				pay.getBusiness_date(),product.getOid(),"",money.getMoney().setScale(2, RoundingMode.FLOOR).abs());
	}
	
	public List<Object[]>  totalActualOfPayment(String channelOid) {
		List<Object[]> repList = this.accountingNotifyDao.totalActualOfPayment(channelOid);
		return repList;
	}

	public PagesRep<AccountingNotifyRep> findOffsetFee(String productOid, String busDay,Pageable pageable) {
		Page<AccountingNotifyEntity> offPage = this.accountingNotifyDao.findOffsetFee(productOid,DateUtil.parseToSqlDate(busDay),pageable);
		PagesRep<AccountingNotifyRep> pagesRep = new PagesRep<AccountingNotifyRep>();
		if (offPage != null && offPage.getContent() != null && offPage.getTotalElements() > 0) {
			for (AccountingNotifyEntity tmp : offPage.getContent()) {
				AccountingNotifyRep rep = new AccountingNotifyRep();
				rep.setProductOid(tmp.getProductOid());
				rep.setBusDate(tmp.getBusDate());
				rep.setCostFee(tmp.getCostFee());
				rep.setNotifyType(tmp.getNotifyType());
				rep.setNotifyStatus(tmp.getNotifyStatus());
				pagesRep.add(rep);
			}
		}
		pagesRep.setTotal(offPage.getTotalElements());
		return pagesRep;
	}

	public PagesRep<AccountingNotifyRep>  getFeeListByOid(String productOid, Pageable pageable) {
		Page<AccountingNotifyEntity> feePage = this.accountingNotifyDao.getFeeListByOid(productOid,pageable);
		PagesRep<AccountingNotifyRep> pagesRep = new PagesRep<AccountingNotifyRep>();
		if (feePage != null && feePage.getContent() != null && feePage.getTotalElements() > 0) {
			for (AccountingNotifyEntity tmp : feePage.getContent()) {
				AccountingNotifyRep rep = new AccountingNotifyRep();
				rep.setProductOid(tmp.getProductOid());
				rep.setBusDate(tmp.getBusDate());
				rep.setCostFee(tmp.getCostFee());
				rep.setNotifyType(tmp.getNotifyType());
				rep.setNotifyStatus(tmp.getNotifyStatus());
				pagesRep.add(rep);
			}
		}
		pagesRep.setTotal(feePage.getTotalElements());
		return pagesRep;
	}
	
}