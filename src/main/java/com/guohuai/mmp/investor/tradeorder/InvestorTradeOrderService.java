package com.guohuai.mmp.investor.tradeorder;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import com.guohuai.component.util.BeanUtil;
import com.guohuai.mmp.investor.tradeorder.cycleProduct.BookingOrderRep;
import com.guohuai.mmp.investor.tradeorder.cycleProduct.BookingOrderReq;
import com.guohuai.mmp.investor.tradeorder.p2p.*;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.chainsaw.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.guohuai.ams.product.OnSaleT0ProductRep;
import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductService;
import com.guohuai.ams.product.reward.ProductIncomeReward;
import com.guohuai.ams.product.reward.ProductIncomeRewardCacheService;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.StrRedisUtil;
import com.guohuai.component.util.StringUtil;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.component.web.view.PagesRep;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountEntity;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountService;
import com.guohuai.mmp.investor.orderlog.OrderLogService;
import com.guohuai.mmp.job.JobEnum;
import com.guohuai.mmp.job.lock.JobLockService;
import com.guohuai.mmp.job.log.JobLogEntity;
import com.guohuai.mmp.job.log.JobLogFactory;
import com.guohuai.mmp.job.log.JobLogService;
import com.guohuai.mmp.platform.investor.offset.InvestorOffsetEntity;
import com.guohuai.mmp.platform.payment.log.PayLogEntity;
import com.guohuai.mmp.platform.payment.log.PayLogService;
import com.guohuai.mmp.platform.publisher.offset.PublisherOffsetEntity;
import com.guohuai.mmp.publisher.hold.PublisherHoldEntity;
import com.guohuai.mmp.publisher.hold.TradeOrderHoldDaysDetail;
import com.guohuai.mmp.publisher.holdapart.closedetails.CloseDetailsService;
import com.guohuai.mmp.sys.SysConstant;
import com.guohuai.settlement.api.SettlementSdk;
import com.guohuai.settlement.api.response.OrderBankInfoResponse;

@Service
@Transactional
public class InvestorTradeOrderService {
	
	Logger logger = LoggerFactory.getLogger(InvestorTradeOrderService.class);
	private   final static String fomat = "yyyy-MM-dd HH:mm:ss";
	@Autowired
	private RedisTemplate<String, String> redis;
	@Autowired
	private InvestorTradeOrderDao investorTradeOrderDao;
	@Autowired
	private CloseDetailsService closeDetailsService;
	@Autowired
	private OrderLogService orderLogService;
	@Autowired
	private ProductIncomeRewardCacheService productIncomeRewardCacheService;
	@Autowired
	private PayLogService payLogService;
	@Autowired
	private JobLockService jobLockService;
	@Autowired
	private JobLogService jobLogService;
	@Autowired
	private OrderDateService orderDateService;
	@Autowired
	private InvestorBaseAccountService investorBaseAccountService;
	@Autowired
	private ProductService productService;
	@Autowired
	private InvestorTradeOrderRequireNewService investorTradeOrderRequireNewService;
	@Autowired
	private SettlementSdk settlementSdk;
	@Autowired
	private InvestorBfPlusRedeemDao investorBfPlusRedeemDao;
	@Autowired
	private InvestorOpenCycleService investorOpenCycleService;
	@Autowired
	private P2PCreditorService p2PCreditorService;
	
	/**
	 * 第三方支付购买成功回调
	 */
	public static final String PAYMENT_trade_finished  = "TRADE_FINISHED";
	public static final String PAYMENT_trade_failed = "TRADE_FAILED";
	
	/**
	 * 第三方支付购买成功回调
	 */
	public static final String PAYMENT_pay_finished  = "PAY_FINISHED";
	
	public static final String PAYMENT_apply_success = "APPLY_SUCCESS";
	
	
	/**
	 * 一次批量赎回量不能超过300
	 */
	public static final int MAX_REDEMPTION_TIMES = 300;
	
	public InvestorTradeOrderEntity saveEntity(InvestorTradeOrderEntity orderEntity) {
		return investorTradeOrderDao.save(orderEntity);
	}
	
	@Transactional(value = TxType.REQUIRES_NEW)
	public InvestorTradeOrderEntity saveEntityNewTrans(InvestorTradeOrderEntity orderEntity, String payStatus, String orderStatus) {
		orderEntity.setPayStatus(payStatus);
		orderEntity.setOrderStatus(orderStatus);
		return investorTradeOrderDao.save(orderEntity);
		
	}

	
	public InvestorTradeOrderEntity findOne(String oid) {
		InvestorTradeOrderEntity orderEntity = this.investorTradeOrderDao.findOne(oid);
		if (null == orderEntity) {
			//error.define[80008]=投资人-交易委托单：订单号不存在!(CODE:80008)
			throw new AMPException(80008);
		}
		return orderEntity;
	}

	
	public InvestorTradeOrderEntity findByOrderCode(String orderCode) {
		InvestorTradeOrderEntity tradeOrder = this.investorTradeOrderDao.findByOrderCode(orderCode);
		if (null == tradeOrder) {
			//error.define[80008]=投资人-交易委托单：订单号不存在!(CODE:80008)
			throw new AMPException(80008);
		}
		return tradeOrder;
	}
	
//	/**
//	 * 获取待结算订单
//	 */
//	public List<InvestorTradeOrderEntity> findToCloseOrders(InvestorOffsetEntity offset, String investorOid, String lastOid){
//		return this.investorTradeOrderDao.findToCloseOrders(offset.getOid(), investorOid, lastOid);
//	}
	
	/**
	 * 查询产品下所有某一状态的订单
	 */
	public List<InvestorTradeOrderEntity> findTradeOrderByProductAndOrderStatus(String productOid,String orderStatus){
		return this.investorTradeOrderDao.findTradeOrderByProductAndOrderStatus(productOid, orderStatus);
	}
	
//	/**
//	 * 获取待结算平台订单
//	 */
//	public List<InvestorTradeOrderEntity> findToClosePlatformOrders(InvestorOffsetEntity offset, String investorOid){
//		return this.investorTradeOrderDao.findToClosePlatformOrders(offset.getOid(), investorOid);
//	}
//	
	/**
	 * 获取待结算订单
	 */
	public List<InvestorTradeOrderEntity> findToCloseOrders(PublisherOffsetEntity offset, String lastOid){
		return this.investorTradeOrderDao.findToCloseOrders(offset.getOid(), lastOid);
	}
	
	/**
	 * 运营工作台订单查询
	 */
	public PagesRep<TradeOrderQueryRep> mng (TradeOrderQueryReq req, Pageable pageable){
		String investOid="";
		PagesRep<TradeOrderQueryRep> pagesRep=new PagesRep<TradeOrderQueryRep>();
		if(!StringUtil.isEmpty(req.getPhoneNum())){
			InvestorBaseAccountEntity baseEntity=this.investorBaseAccountService.findByPhoneNum(req.getPhoneNum());
			if(baseEntity==null){
				return pagesRep;
			}else{
				investOid=baseEntity.getOid();
			}
		}
		BigDecimal totalAmount=this.investorTradeOrderDao.findTotalAmount(req.getOrderType(), req.getOrderStatus(), 
				req.getIsAuto(), req.getOrderCode(), req.getCreateTimeBegin(), req.getCreateTimeEnd(), req.getMinOrderAmount(), req.getMaxOrderAmount(),req.getProductOid(),investOid);
		pagesRep=investorTradeOrderMng(buildSpecification(req),pageable);
		pagesRep.setTotalAmount(totalAmount);
		return pagesRep;
	}
	/**
	 * 订单查询 
	 */
	public PagesRep<TradeOrderQueryRep> investorTradeOrderMng(Specification<InvestorTradeOrderEntity> spec, Pageable pageable) {
		Page<InvestorTradeOrderEntity> cas = this.investorTradeOrderDao.findAll(spec, pageable);
		PagesRep<TradeOrderQueryRep> pagesRep = new PagesRep<TradeOrderQueryRep>();
		
		if (cas != null && cas.getContent() != null && cas.getTotalElements() > 0) {
			for (InvestorTradeOrderEntity tradeOrder : cas) {
				TradeOrderQueryRep queryRep = new TradeOrderQueryRep();
				queryRep.setTradeOrderOid(tradeOrder.getOid()); //OID
				queryRep.setProductOid(tradeOrder.getProduct().getOid());  //产品UUID
				queryRep.setProductName(tradeOrder.getProduct().getName()); //产品名称
				if (null != tradeOrder.getChannel()) {
					queryRep.setChannelOid(tradeOrder.getChannel().getOid());
					queryRep.setChannelName(tradeOrder.getChannel().getChannelName());
				}
				
				queryRep.setUserPhoneNum(StringUtil.kickstarOnPhoneNum(tradeOrder.getInvestorBaseAccount().getPhoneNum()));
				queryRep.setPhoneNum(StringUtil.kickstarOnPhoneNum(tradeOrder.getInvestorBaseAccount().getPhoneNum()));
				queryRep.setOrderCode(tradeOrder.getOrderCode()); //订单号
				queryRep.setOrderType(tradeOrder.getOrderType()); //订单类型
				queryRep.setOrderTypeDisp(TradeUtil.orderTypeEn2Ch(tradeOrder.getOrderType()));
				queryRep.setOrderAmount(tradeOrder.getOrderAmount()); //订单金额
				queryRep.setOrderVolume(tradeOrder.getOrderVolume()); //订单份额
				queryRep.setOrderStatus(tradeOrder.getOrderStatus());
				queryRep.setOrderStatusDisp(TradeUtil.orderStatusEn2Ch(tradeOrder.getOrderStatus())); //订单状态
				queryRep.setProfitOtherDisp(TradeUtil.profitOtherDeal(tradeOrder.getOrderTime(), tradeOrder.getOrderType()));// 奖励投资独用
				queryRep.setContractStatus(tradeOrder.getContractStatus());
				queryRep.setContractStatusDisp(TradeUtil.contractStatusEn2Ch(tradeOrder.getContractStatus()));
				queryRep.setCreateMan(tradeOrder.getCreateMan()); //订单创建人
				queryRep.setCreateManDisp(TradeUtil.createManEn2Ch(tradeOrder.getCreateMan())); //订单创建人disp
				queryRep.setOrderTime(tradeOrder.getOrderTime());
				queryRep.setCompleteTime(tradeOrder.getCompleteTime()); //订单完成时间
				queryRep.setPublisherClearStatus(tradeOrder.getPublisherClearStatus());
				queryRep.setPublisherClearStatusDisp(TradeUtil.publisherClearStatusEn2Ch(tradeOrder.getPublisherClearStatus()));
				queryRep.setPublisherConfirmStatus(tradeOrder.getPublisherConfirmStatus());
				queryRep.setPublisherConfirmStatusDisp(TradeUtil.publisherConfirmStatusEn2Ch(tradeOrder.getPublisherConfirmStatus()));
				queryRep.setPublisherCloseStatus(tradeOrder.getPublisherCloseStatus());
				queryRep.setPublisherCloseStatusDisp(TradeUtil.publisherCloseStatusEn2Ch(tradeOrder.getPublisherCloseStatus()));
				queryRep.setCouponType(tradeOrder.getCouponType());
				queryRep.setCouponAmount(tradeOrder.getCouponAmount()); //卡券面值
				queryRep.setPayAmount(tradeOrder.getPayAmount());
				queryRep.setHoldVolume(tradeOrder.getHoldVolume()); //持有份额
				queryRep.setRedeemStatus(tradeOrder.getRedeemStatus()); //可赎回状态
				queryRep.setRedeemStatusDisp(TradeUtil.redeemStatusEn2Ch(tradeOrder.getRedeemStatus()));
				queryRep.setAccrualStatus(tradeOrder.getAccrualStatus()); //可计息状态
				queryRep.setAccrualStatusDisp(TradeUtil.accrualStatusEn2Ch(tradeOrder.getAccrualStatus()));
				queryRep.setBeginAccuralDate(tradeOrder.getBeginAccuralDate()); //起息日
				queryRep.setBeginRedeemDate(tradeOrder.getBeginRedeemDate()); //起始赎回日
				queryRep.setTotalIncome(tradeOrder.getTotalIncome()); //累计收益
				queryRep.setTotalBaseIncome(tradeOrder.getTotalBaseIncome()); //累计基础收益
				queryRep.setTotalRewardIncome(tradeOrder.getTotalRewardIncome()); //累计奖励收益
				queryRep.setYesterdayBaseIncome(tradeOrder.getYesterdayBaseIncome());
				queryRep.setYesterdayRewardIncome(tradeOrder.getYesterdayRewardIncome());
				queryRep.setYesterdayIncome(tradeOrder.getYesterdayIncome());
				queryRep.setIncomeAmount(tradeOrder.getIncomeAmount()); // 定期收益
				queryRep.setExpectIncome(tradeOrder.getExpectIncome());
				queryRep.setExpectIncomeExt(tradeOrder.getExpectIncomeExt());
				queryRep.setValue(tradeOrder.getValue());
				queryRep.setHoldStatus(tradeOrder.getHoldStatus());
				queryRep.setHoldStatusDisp(TradeUtil.holdStatusEn2Ch(tradeOrder.getHoldStatus()));
				queryRep.setConfirmDate(tradeOrder.getConfirmDate());
				queryRep.setBaseIncomeRatio(tradeOrder.getProduct().getBasicRatio()); //基础收益率
				queryRep.setPayChannelName(tradeOrder.getPayChannel());
				queryRep.setIsAuto(tradeOrder.getIsAuto());
				if (null != tradeOrder.getBeginAccuralDate()) {
					long holdDays = DateUtil.daysBetween(DateUtil.getSqlDate(), tradeOrder.getBeginAccuralDate()) + 1;
					if (holdDays > 0) {
						queryRep.setHoldDays(holdDays);
						ProductIncomeReward w = this.productIncomeRewardCacheService.getRewardEntity(tradeOrder.getProduct().getOid(), (int)holdDays);
						if (null != w) {
							queryRep.setRewardIncomeRatio(w.getRatio()); //奖励收益率
							queryRep.setRewardIncomeLevel(w.getLevel()); //奖励阶梯
						}
					}
				}
				
				queryRep.setUpdateTime(tradeOrder.getUpdateTime());
				queryRep.setCreateTime(tradeOrder.getCreateTime()); //订单创建时间
				
				
				pagesRep.add(queryRep);
			}
		}
		pagesRep.setTotal(cas.getTotalElements());
		return pagesRep;
	}
	/**
	 * 
	 * @author yihonglei
	 * @Title: tradeOrderMng
	 * @Description: 交易明细查询
	 * @param req
	 * @param uid
	 * @return TradeDetailRep<Map<String,Object>>
	 * @date 2017年9月4日 下午9:10:46
	 * @since  1.0.0
	 */
	public TradeDetailRep<Map<String, Object>> tradeOrderMng(TradeDetailReq req, String uid) {
		// 获取快活宝产品ID
		OnSaleT0ProductRep prodcutRep = productService.getOnSaleProductOid();
		if(prodcutRep == null) {
			throw new AMPException("获取快活宝产品ID异常");
		}
		String t0ProductOid = prodcutRep.getProductOid();
		// 响应结果
		TradeDetailRep<Map<String, Object>> rep = new TradeDetailRep<Map<String, Object>>();
		// 交易明细列表对象(注意: 如无分页要求，可以将数据查询结果用代码处理类型)
		List<Object[]> objTradeDetailList = this.investorTradeOrderDao.queryTradeDetailList(
				uid, t0ProductOid, req.getTradeType(), 
				req.getOrderTimeStart(), req.getOrderTimeEnd(),
				(req.getPage() - 1) * req.getRow(), req.getRow());
		// 交易明细总条数
		int total = this.investorTradeOrderDao.queryTradeDetailCount(
				uid, t0ProductOid, req.getTradeType(), req.getOrderTimeStart(), req.getOrderTimeEnd());
		// 对象封装处理
		if (objTradeDetailList.size() > 0) {
			List<Map<String,Object>> tradeDetailList = new ArrayList<Map<String,Object>>();
			for (int i = 0;i < objTradeDetailList.size() ; i ++) {
				Object[] objData = objTradeDetailList.get(i);
				Map<String,Object> mapData = new HashMap<String,Object>();
				mapData.put("orderCode", objData[0]); // 订单Oid
				mapData.put("orderTime", DateUtil.parse(obj2String(objData[1]), DateUtil.defaultDatePattern)); // 交易时间
				mapData.put("amount", objData[2]); // 金额
				mapData.put("tradeType", objData[3]); // 交易类型
				mapData.put("tradeTypeDisp", TradeUtil.dealTradeTypeDisp(objData[3])); // 交易类型展现名称
				
				tradeDetailList.add(mapData);
			}
			
			rep.setRows(tradeDetailList);
			rep.setTotal(total);
			rep.setRow(req.getRow());
			rep.setPage(req.getPage());
			rep.reTotalPage();
		}
		
		return rep;
	}
	
	/**
	 * 
	 * @author yihonglei
	 * @Title: orderDetail
	 * @Description: app和pc根据订单号查询订单详情
	 * @param req
	 * @return Map<String,Object>
	 * @date 2017年9月4日 下午9:37:39
	 * @since  1.0.0
	 */
	public Map<String, Object> orderDetail(TradeDetailReq req) {
		Assert.notNull(req.getOrderCode());
		List<Object[]> objDetail = this.investorTradeOrderDao.queryOrderDetail(req.getOrderCode());
		Map<String, Object> messageMap = new HashMap<String, Object>();
		
		if (objDetail.size() > 0) {
			Object[] objData = objDetail.get(0); 
			messageMap = dealOrderDetail(objData);
		}
		
		return messageMap;
	}
	
	private Map<String, Object> dealOrderDetail(Object[] objData) {
		Map<String, Object> reMap = new HashMap<String, Object>();
		
		String orderCode = obj2String(objData[0]); // 订单号
		String createTime = obj2String(objData[1]); // 订单时间
		String orderType = obj2String(objData[2]); // 订单类型
		String orderStatus = obj2String(objData[3]); // 订单状态
		String amount = obj2String(objData[4]); // 订单金额
		String productType = obj2String(objData[5]); // 产品类型
		String productName = obj2String(objData[6]); // 产品名称
		String reward = obj2String(objData[7]); // 是否有奖励
		String completeTime = obj2String(objData[8]); // 提现订单完成时间
		String relateOid = obj2String(objData[9]); // 关联的定期赎回id
		reMap.put("orderCode", orderCode); // 订单号
		reMap.put("amount", amount); // 金额
		reMap.put("orderStatus", orderStatus); // 订单状态
		reMap.put("orderStatusDisp", TradeUtil.dealOrderStatusDisp(orderType, orderStatus)); // 订单状态展示名称
		reMap.put("createTime", createTime); // 订单创建时间
		reMap.put("productName", productName); // 产品名称
		// ##################投资类详情####################
		// 银行卡到快活宝；银行卡到定期；银行卡到暴风天天向上；
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_invest.equals(orderType)) { 
			reMap.put("payExplain", "银行卡-转到" + productName);// 付款说明
			if (InvestorTradeOrderEntity.TRADEORDER_orderStatus_payFailed.equals(orderStatus)
					|| InvestorTradeOrderEntity.TRADEORDER_orderStatus_paySuccess.equals(orderStatus)
					|| InvestorTradeOrderEntity.TRADEORDER_orderStatus_accepted.equals(orderStatus)
					|| InvestorTradeOrderEntity.TRADEORDER_orderStatus_confirmed.equals(orderStatus)) {
				reMap.put("payWay", getBankNameByOrderOid(orderCode)); // 付款方式(获取对应订单银行名称)
			} else {
				reMap.put("payWay", "--"); // 付款方式
			}
		}
		// 二级邀请月奖励到快活宝
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_profitInvest.equals(orderType)) {
			reMap.put("payExplain", DateUtil.formatStr(createTime) + "月邀请奖励" + productName); // 付款说明
			reMap.put("payWay", DateUtil.formatStr(createTime) + "月邀请奖励"); // 付款方式
		}
		// 快活宝到暴风天天向上
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_noPayInvest.equals(orderType)
				&& Product.TYPE_Producttype_02.equals(productType)
				&& !"".equals(reward)) {
			reMap.put("payExplain", "快活宝-转到" + productName); // 付款说明
			reMap.put("payWay", "快活宝"); // 付款方式
		}
		// 快活宝到定期
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_noPayInvest.equals(orderType)
				&& Product.TYPE_Producttype_01.equals(productType)) {
			reMap.put("payExplain", "快活宝-转到" + productName); // 付款说明
			reMap.put("payWay", "快活宝"); // 付款方式
		}
		// 快活宝到15天定期
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_noPayInvest.equals(orderType)
				&& Product.TYPE_Producttype_03.equals(productType)) {
			reMap.put("payExplain", "快活宝-转到" + productName); // 付款说明
			reMap.put("payWay", "快活宝"); // 付款方式
		}
		// ##################回款类详情####################
		// 定期到快活宝
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_noPayInvest.equals(orderType)
				&& Product.TYPE_Producttype_02.equals(productType)
				&& "".equals(reward)) {
			//根据relateOid先查询定期的赎回订单
			InvestorTradeOrderEntity cashOrderEntity = this.investorTradeOrderDao.findOne(relateOid);
			if (cashOrderEntity == null) {
				reMap.put("refundExplain", "智盈-转到" + productName); // 回款说明
				reMap.put("refundWay", "智盈"); // 回款方式
			} else if (Product.TYPE_Producttype_04.equals(cashOrderEntity.getProduct().getType().getOid())) {
				reMap.put("refundExplain", "智盈15D-转到" + productName); // 回款说明
				reMap.put("refundWay", "智盈15D"); // 回款方式
			} else if (Product.TYPE_Producttype_01.equals(cashOrderEntity.getProduct().getType().getOid()) && Product.IS_P2P_ASSET_PACKAGE_2.equals(cashOrderEntity.getProduct().getIsP2PAssetPackage())) {
				reMap.put("refundExplain", cashOrderEntity.getProduct().getName() + "-转到" + productName); // 回款说明
				reMap.put("refundWay", "散标"); // 回款方式
			} else {
				reMap.put("refundExplain", "智盈-转到" + productName); // 回款说明
				reMap.put("refundWay", "智盈"); // 回款方式
			}
		}
		// 提现驳回到快活宝或天天向上
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_writeOff.equals(orderType)) {
			reMap.put("refundExplain", "提现驳回-转到" + productName); // 回款说明
			reMap.put("refundWay", "提现驳回"); // 回款方式
		}
		// ##################提现类详情####################
		// 快活宝-转出到银行卡
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_normalRedeem.equals(orderType)) {
			reMap.put("redeemExplain", "快活宝-转出到银行卡"); // 提现说明
		}
		// 暴风天天向上-转出到银行卡
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_incrementRedeem.equals(orderType)) {
			reMap.put("redeemExplain", "暴风天天向上-转出到银行卡");
		}
		// 提现银行名称和提现完成时间
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_normalRedeem.equals(orderType)
				|| InvestorTradeOrderEntity.TRADEORDER_orderType_incrementRedeem.equals(orderType)) {
			reMap.put("completeTime", completeTime);// 提现订单为done，payFailed时才会产生完成时间，否则为空串
			if (InvestorTradeOrderEntity.TRADEORDER_orderStatus_confirmed.equals(orderStatus)
					|| InvestorTradeOrderEntity.TRADEORDER_orderStatus_payFailed.equals(orderStatus)
					|| InvestorTradeOrderEntity.TRADEORDER_orderStatus_done.equals(orderStatus)) {
				reMap.put("redeemBankName", getBankNameByOrderOid(orderCode));
			} else {
				reMap.put("redeemBankName", "提现金额");
			}
		}
		
		return  reMap;
	}
	
	private String obj2String(Object obj) {
		String str = "";
		if (null != obj) {
			str = obj.toString();
		}
		return str;
	}
	
	private String getBankNameByOrderOid(String orderOid) {
		// 需要根据订单号调用结算获取银行卡名称，做缓存
		String key = StrRedisUtil.ORDER_BANK_NAME_REDIS_KEY + orderOid;
		String bankName = StrRedisUtil.get(redis, key); // 先从缓存获取，如果取不到，再调用结算查询
		if (null == bankName || "".equals(bankName)) {
			OrderBankInfoResponse orderBankInfoResponse = settlementSdk.getOrderBankInfo(orderOid);
			if ("0".equals(orderBankInfoResponse.getReturnCode())) {
				// 获取银行卡名称
				bankName = StringUtils.isBlank(orderBankInfoResponse.getBankName()) ? "--" : orderBankInfoResponse.getBankName();
				// 存入缓存
				StrRedisUtil.set(redis, key, bankName);
			} else {
				bankName = "--";
			}
		}
		
		return bankName;
	}
	
	public Specification<InvestorTradeOrderEntity> buildSpecification(final TradeOrderQueryReq req) {
		Specification<InvestorTradeOrderEntity> spec = new Specification<InvestorTradeOrderEntity>() {
			@Override
			public Predicate toPredicate(Root<InvestorTradeOrderEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> bigList = new ArrayList<Predicate>();
				if (!StringUtil.isEmpty(req.getOrderType()))
					bigList.add(cb.equal(root.get("orderType").as(String.class), req.getOrderType()));
				if (!StringUtil.isEmpty(req.getOrderStatus()))
					bigList.add(cb.equal(root.get("orderStatus").as(String.class), req.getOrderStatus()));
				if (!StringUtil.isEmpty(req.getIsAuto()))
					bigList.add(cb.equal(root.get("isAuto").as(String.class), req.getIsAuto()));
				if (!StringUtil.isEmpty(req.getPhoneNum()))
					bigList.add(cb.equal(root.get("investorBaseAccount").get("phoneNum").as(String.class), req.getPhoneNum()));
				if (!StringUtil.isEmpty(req.getChannelOid()))
					bigList.add(
							cb.equal(root.get("channel").get("oid").as(String.class), req.getChannelOid()));
				if (!StringUtil.isEmpty(req.getChannelName()))
					bigList.add(cb.equal(root.get("channel").get("channelName").as(String.class), req.getChannelName()));
				if (!StringUtil.isEmpty(req.getOrderCode()))
					bigList.add(cb.equal(root.get("orderCode").as(String.class), req.getOrderCode()));
				
				if (!StringUtil.isEmpty(req.getCreateTimeBegin()) ) {
//					java.util.Date beginDate = DateUtil
//							.beginTimeInMillis(DateUtil.parseDate(req.getCreateTimeBegin(), fomat));
//					bigList.add(cb.greaterThanOrEqualTo(root.get("createTime").as(Timestamp.class),
//							new Timestamp(beginDate.getTime())));
					bigList.add(cb.greaterThanOrEqualTo(root.get("createTime").as(Timestamp.class),
							Timestamp.valueOf(req.getCreateTimeBegin())));
				}
				if (!StringUtil.isEmpty(req.getCreateTimeEnd()) ) {
//					java.util.Date endDate =DateUtil
//							.endTimeInMillis(DateUtil.parseDate(req.getCreateTimeEnd(), fomat));
//					bigList.add(cb.lessThanOrEqualTo(root.get("createTime").as(Timestamp.class),
//							new Timestamp(endDate.getTime())));
					bigList.add(cb.lessThanOrEqualTo(root.get("createTime").as(Timestamp.class),
							Timestamp.valueOf(req.getCreateTimeEnd())));
				}
				if (req.getMinOrderAmount()!=null&&!req.getMinOrderAmount().equals(""))
					bigList.add(cb.greaterThanOrEqualTo(root.get("orderAmount").as(BigDecimal.class),
							req.getMinOrderAmount()));
				if (req.getMaxOrderAmount()!=null&&!req.getMaxOrderAmount().equals(""))
					bigList.add(cb.lessThanOrEqualTo(root.get("orderAmount").as(BigDecimal.class),
							req.getMaxOrderAmount()));
				
				if (!StringUtil.isEmpty(req.getCreateMan()))
					bigList.add(cb.equal(root.get("createMan").as(String.class),   req.getCreateMan()  ));
				if (!StringUtil.isEmpty(req.getProductName()))
					bigList.add(cb.equal(root.get("product").get("name").as(String.class),   req.getProductName()  ));
				if (!StringUtil.isEmpty(req.getProductType()))
					bigList.add(cb.equal(root.get("product").get("type").get("oid").as(String.class), req.getProductType()));
				if (!StringUtil.isEmpty(req.getProductOid()))
					bigList.add(cb.equal(root.get("product").get("oid").as(String.class),   req.getProductOid()  ));
				if (!StringUtil.isEmpty(req.getInvestorOid()))
					bigList.add(cb.equal(root.get("investorBaseAccount").get("oid").as(String.class),   req.getInvestorOid()  ));
				if (!StringUtil.isEmpty(req.getPublisherClearStatus()))
					bigList.add(cb.equal(root.get("publisherClearStatus").as(String.class),   req.getPublisherClearStatus()  ));
				if (!StringUtil.isEmpty(req.getInvestorOffsetOid()))
					bigList.add(cb.equal(root.get("investorOffset").get("oid").as(String.class),   req.getInvestorOffsetOid()  ));
				if (!StringUtil.isEmpty(req.getPublisherOffsetOid()))
					bigList.add(cb.equal(root.get("publisherOffset").get("oid").as(String.class),   req.getPublisherOffsetOid()  ));
				query.where(cb.and(bigList.toArray(new Predicate[bigList.size()])));
//				query.orderBy(cb.desc(root.get("createTime")));
				// 条件查询
				return query.getRestriction();
			}
		};
		return spec;
	}

	/**
	 * 判断 订单是否已完成
	 */
	public BaseRep isDone(TradeOrderIsDoneReq isDone) {
		TradeOrderIsDoneRep rep = new TradeOrderIsDoneRep();

		InvestorTradeOrderEntity orderEntity = this.investorTradeOrderDao.findOne(isDone.getTradeOrderOid());
		if (null == orderEntity) {
			// error.define[80001]=投资人-银行委托单的订单号不存在!(CODE:80001)
			throw new AMPException(80001);
		}

		if (InvestorTradeOrderEntity.TRADEORDER_orderStatus_paySuccess.equals(orderEntity.getOrderStatus())
				|| InvestorTradeOrderEntity.TRADEORDER_orderStatus_accepted.equals(orderEntity.getOrderStatus())
				|| InvestorTradeOrderEntity.TRADEORDER_orderStatus_confirmed.equals(orderEntity.getOrderStatus())
				|| InvestorTradeOrderEntity.TRADEORDER_orderStatus_done.equals(orderEntity.getOrderStatus())) {
			if (Product.TYPE_Producttype_01.equals(orderEntity.getProduct().getType().getOid())) {
				rep.setBeginInterestDate(orderEntity.getProduct().getSetupDate());
				rep.setInterestArrivedDate(orderEntity.getProduct().getRepayDate());
			} else {
				setInterestDate(rep, orderEntity);
				rep.setPayDate(orderEntity.getPayDate());
			}
		} else if (InvestorTradeOrderEntity.TRADEORDER_orderStatus_payFailed.equals(orderEntity.getOrderStatus())) {
			PayLogEntity payLog = payLogService.findByOrderCodeAndHandleType(orderEntity.getOrderCode(), PayLogEntity.PAY_handleType_notify);
			rep.setErrorCode(-2);
			rep.setErrorMessage(payLog.getErrorMessage());
		} else {
			setInterestDate(rep, orderEntity);
			rep.setErrorCode(BaseRep.ERROR_CODE);
			rep.setErrorMessage("支付尚未完成");
			return rep;
		}

		return rep;
	}

	private void setInterestDate(TradeOrderIsDoneRep rep, InvestorTradeOrderEntity orderEntity) {
		if(InvestorTradeOrderEntity.TRADEORDER_orderType_noPayInvest.equals(orderEntity.getOrderType())) {
			rep.setBeginInterestDate(new Date(System.currentTimeMillis()));
		} else {
			rep.setBeginInterestDate(orderDateService.getBeginAccuralDate(orderEntity));
		}
		rep.setInterestArrivedDate(DateUtil.addSQLDays(rep.getBeginInterestDate(), 1));
	}
	
	/**
	 *  订单详情
	 */
	public TradeOrderDetailRep detail(String tradeOrderOid) {
		TradeOrderDetailRep rep = new TradeOrderDetailRep();
		InvestorTradeOrderEntity order = this.investorTradeOrderDao.findOne(tradeOrderOid);
		rep.setOrderCode(order.getOrderCode()); //订单号
		rep.setOrderType(order.getOrderType()); //交易类型
		rep.setOrderTypeDisp(TradeUtil.orderTypeEn2Ch(order.getOrderType())); //交易类型Disp
		rep.setOrderAmount(order.getOrderAmount()); //订单金额
		rep.setOrderVolume(order.getOrderVolume()); //订单份额
		rep.setOrderStatus(order.getOrderStatus()); //订单状态
		rep.setOrderStatusDisp(TradeUtil.orderStatusEn2Ch(order.getOrderStatus())); //订单状态Disp
		rep.setCreateMan(order.getCreateMan()); //订单创建人
		rep.setCreateManDisp(TradeUtil.createManEn2Ch(order.getCreateMan())); //订单创建人Disp
		rep.setCreateTime(order.getCreateTime()); //订单创建时间
		rep.setCompleteTime(order.getCompleteTime()); //订单完成时间
		return rep;
	}
	
	/**
	 * 更新投资人清算状态
	 */
	@Transactional(value = TxType.REQUIRES_NEW)
	public int updateInvestorClearStatus(InvestorOffsetEntity offset,String investorClearStatus){
		return this.investorTradeOrderDao.updateInvestorClearStatus(offset, investorClearStatus);
	}
	
	/**
	 * 更新投资人结算状态
	 */
	public int updateInvestorCloseStatus(InvestorOffsetEntity offset,String investorCloseStatus){
		return this.investorTradeOrderDao.updateInvestorCloseStatus(offset, investorCloseStatus);
	}
	
	public int updateInvestorCloseStatusDirectly(InvestorOffsetEntity offset,String investorCloseStatus){
		return this.investorTradeOrderDao.updateInvestorCloseStatusDirectly(offset, investorCloseStatus);
	}
	
	
	
	public int updatePlatformInvestorCloseStatus(InvestorOffsetEntity offset, String investorOid, String investorCloseStatus){
		return this.investorTradeOrderDao.updatePlatformInvestorCloseStatus(offset, investorOid, investorCloseStatus);
	}
	
	
	/**
	 * 查询用于份额确认 
	 */
	public List<InvestorTradeOrderEntity> findByOffsetOid(String offsetOid, String lastOid) {
		return this.investorTradeOrderDao.findByOffsetOidAndOid(offsetOid, lastOid);
	}
	
	/**
	 * 查询已经执行份额确认的订单 
	 */
	public List<InvestorTradeOrderEntity> findConfirmedByOffsetOid(String offsetOid, String lastOid) {
		return this.investorTradeOrderDao.findConfirmedByOffsetOidAndOid(offsetOid, lastOid);
	}
	
	/**
	 * 批量更新
	 */
	public void batchUpdate(List<InvestorTradeOrderEntity> orderList) {
		this.investorTradeOrderDao.save(orderList);

	}
	
	/**
	 * 查询订单用于生成PDF协议 HTML
	 */
	public List<InvestorTradeOrderEntity> findByProductOid4Contract(String productOid, String lastOid) {
		return this.investorTradeOrderDao.findByProductOid4Contract(productOid, lastOid);
	}
	
	/**
	 * 查询订单用于生成PDF协议  PDF
	 */
	public List<InvestorTradeOrderEntity> findByProductOid4PDF(String productOid, String lastOid) {
		return this.investorTradeOrderDao.findByProductOid4PDF(productOid, lastOid);
	}
	

	public int updatePublisherClearStatus(String offsetOid, String clearStatus) {
		int i = this.investorTradeOrderDao.updatePublisherClearStatus(offsetOid, clearStatus);
//		if (i < 1) {
//			// error.define[20021]=清算状态异常(CODE:20021)
//			throw new AMPException(20021);
//		}
		return i;
	}

	/**
	 * 发行人结算
	 */
	public int updateCloseStatus4Close(PublisherOffsetEntity offset, String closeStatus) {
		int i = investorTradeOrderDao.updateCloseStatus4Close(offset.getOid(), closeStatus);
		return i;
		
	}
	
	/**
	 * 结算--支付回调
	 */
	public int updateCloseStatus4CloseBack(PublisherOffsetEntity offset, String closeStatus) {
		int i = investorTradeOrderDao.updateCloseStatus4CloseBack(offset.getOid(), closeStatus);
		return i;
		
	}
	

	/**
	 * 份额确认
	 */
	public int update4Confirm(String oid) {
		int i = this.investorTradeOrderDao.update4Confirm(oid);
		if (i < 1) {
			// error.define[30068]=份额确认订单更新异常(CODE:30068)
			throw new AMPException(30068);
		}
		return i;

	}

	
//	/**
//	 * 查询投资状态、赎回状态的订单时间和订单金额
//	 * 
//	 * @param investorOid
//	 * @param productOid
//	 * @param orderType:
//	 *            invest投资订单，redeem赎回订单
//	 * @return
//	 */
//	public PagesRep<TradeOrderDetailsRep> queryHoldApartDetailsByHoldStatus(
//			Specification<InvestorTradeOrderEntity> spec, Pageable pageable) {
//
//		Page<InvestorTradeOrderEntity> page = this.investorTradeOrderDao.findAll(spec, pageable);
//	
//		PagesRep<TradeOrderDetailsRep> pagesRep = new PagesRep<TradeOrderDetailsRep>();
//		if (page != null && page.getContent() != null && page.getTotalElements() > 0) {
//			List<TradeOrderDetailsRep> rows = new ArrayList<TradeOrderDetailsRep>();
//			for (InvestorTradeOrderEntity p : page) {
//				TradeOrderDetailsRep queryRep = new TradeOrderDetailsRep(p.getOrderTime(), // 订单时间
//						p.getOrderAmount(), // 订单金额
//						getOrderStatusName(p.getOrderStatus())// 订单状态
//				);
//				rows.add(queryRep);
//			}
//		
//			pagesRep.setRows(rows);
//
//			pagesRep.setTotal(page.getTotalElements());
//		}
//
//		return pagesRep;
//	}
	
//	/**
//	 * 我的交易明细
//	 */
//	public PagesRep<MyTradeOrderDetailRep> queryMyTradeDetail(Specification<InvestorTradeOrderEntity> spec,
//			Pageable pageable) {
//
//		Page<InvestorTradeOrderEntity> page = this.investorTradeOrderDao.findAll(spec, pageable);
//		PagesRep<MyTradeOrderDetailRep> pagesRep = new PagesRep<MyTradeOrderDetailRep>();
//		if (page != null && page.getContent() != null && page.getTotalElements() > 0) {
//			List<MyTradeOrderDetailRep> rows = new ArrayList<MyTradeOrderDetailRep>();
//			for (InvestorTradeOrderEntity p : page) {
//				MyTradeOrderDetailRep queryRep = new MyTradeOrderDetailRep(getOrderTypeName(p.getOrderType()), // 订单类型
//						p.getOrderCode(), // 订单号
//						p.getOrderAmount(), // 订单金额
//						p.getOrderTime(), // 订单时间
//						getOrderStatusName(p.getOrderStatus())// 订单状态
//				);
//				rows.add(queryRep);
//			}
//			pagesRep.setRows(rows);
//
//			pagesRep.setTotal(page.getTotalElements());
//		}
//
//		return pagesRep;
//	}
	
	
	
	public void refundAll() {
		String lastOid = "0";
		while (true) {
			List<InvestorTradeOrderEntity> orderList = this.investorTradeOrderDao.findPage4Refund(lastOid);
			if (orderList.isEmpty()) {
				break;
			}
			for (InvestorTradeOrderEntity entity : orderList) {
//				if (!InvestorTradeOrderEntity.TRADEORDER_orderStatus_toRefund.equals(entity.getOrderStatus())) {
//					throw new AMPException("非待退款订单");
//				}
//				if (0 == this.refundPay(entity).getErrorCode()) {
//					entity.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_refunding);
//				}
				lastOid = entity.getOid();
			}
			this.investorTradeOrderDao.save(orderList);
			
		}
	}
	
	public void refundPart(List<String> orderOidList) {
		
		List<InvestorTradeOrderEntity> orderList = new ArrayList<InvestorTradeOrderEntity>();
		for (String tradeOrderOid : orderOidList) {
			InvestorTradeOrderEntity entity = this.findOne(tradeOrderOid);
			if (null == entity) {
				throw new AMPException("订单号不存在");
			}
//			if (!InvestorTradeOrderEntity.TRADEORDER_orderStatus_toRefund.equals(entity.getOrderStatus())) {
//				throw new AMPException("非待退款订单");
//			}
//			if (0 == this.refundPay(entity).getErrorCode()) {
//				entity.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_refunding);
//				orderList.add(entity);
//			}
		}
	
		this.investorTradeOrderDao.save(orderList);
	}
	
//	public BaseRep refundPay(InvestorTradeOrderEntity order) {
//		
//		BigDecimal platformBalance = this.platformBalanceService.getMiddleAccount4InvestCollect();
//		if (order.getOrderAmount().compareTo(platformBalance) > 0) {
//			this.logger.warn("balance({}) of platform middle account(投资专用) is not enough", platformBalance);
//			throw AMPException.getException(20014);
//		}
//		RefundRequest req = RefundRequestBuilder.n()
//				.refundAmount(order.getOrderAmount())
//				.origOuterTradeNo(order.getOrderCode())
//				.outTradeNo(this.seqGenerator.next(CodeConstants.Investor_batch_refund))
//				.summary("refund")
//				.build();
//		BaseRep baseRep = this.paymentServiceImpl.refund(req);
//		return baseRep;
//	}
	
//	public String refundPayCallBack(RefundStatusSync refundStatus) {
//		String status = PaymentLogEntity.PaymentLog_paymentStatus_success;
//		InvestorTradeOrderEntity order = this.findByOrderCode(refundStatus.getOrig_outer_trade_no());
//		if (PaymentLogEntity.PaymentLog_paymentStatus_success.equals(refundStatus.getRefund_status())) {
//			
//			order.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_refunded);
//			
//			investorInvestTradeOrderService.back(order);
//			
//			this.tulipNewService.tulipEventDeal(order);
//		}
//		
//		if (PaymentLogEntity.PaymentLog_refund_failed.equals(refundStatus.getRefund_status())) {
//			//order.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_refundFailed);
//			status = PaymentLogEntity.PaymentLog_paymentStatus_failure;
//		}
//		order.setCompleteTime(DateUtil.getSqlCurrentDate());
//		this.saveEntity(order);
//		return status;
//	}

	




	public InvestorTradeOrderEntity findByCoupons(String coupons) {
		
		return this.investorTradeOrderDao.findByCoupons(coupons);
	}


	/** 
	 * 份额确认（分仓）
	 */
	public int update4Confirm(InvestorTradeOrderEntity orderEntity, String redeemStatus, String accrualStatus) {
		return this.investorTradeOrderDao.update4Confirm(orderEntity.getOid(), redeemStatus, accrualStatus);
		
	}
	
	/**
	 * 根据订单平仓
	 */
	public FlatWareTotalRep flatWare(InvestorTradeOrderEntity redeemOrder) {
		FlatWareTotalRep totalRep = new FlatWareTotalRep();
		BigDecimal volume = redeemOrder.getOrderVolume();
		if (volume.compareTo(SysConstant.BIGDECIMAL_defaultValue) <= 0) {
			// error.define[20020]=赎回时分仓份额异常(CODE:20020)
			throw AMPException.getException(20020);
		}
		List<InvestorTradeOrderEntity> orderList = this.findApart(redeemOrder.getInvestorBaseAccount(), redeemOrder.getProduct());
		for (InvestorTradeOrderEntity entity : orderList) {
			if (volume.compareTo(SysConstant.BIGDECIMAL_defaultValue) <= 0) {
				break;
			}
			/** 定期统计本金、利息*/
			if (Product.TYPE_Producttype_01.equals(redeemOrder.getProduct().getType().getOid())) {
				totalRep.setTHoldVolume(entity.getHoldVolume());
				totalRep.setTIncomeAmount(entity.getTotalIncome());
			}
			
			if (entity.getHoldVolume().compareTo(volume) < 0) {
				BigDecimal holdVolume = entity.getHoldVolume();
				volume = volume.subtract(holdVolume);
				
				entity.setHoldVolume(SysConstant.BIGDECIMAL_defaultValue);
				entity.setValue(entity.getHoldVolume());
				entity.setHoldStatus(getFlatWareHoldStatus(redeemOrder));
				
				if (holdVolume.compareTo(BigDecimal.ZERO) > 0) {
					this.closeDetailsService.createCloseDetails(entity, holdVolume, redeemOrder);
					setFlatWareRep(holdVolume, entity, totalRep);
				}
				
				this.orderLogService.createRedeemCloseLog(entity, redeemOrder);
				continue;
			}

			if (entity.getHoldVolume().compareTo(volume) == 0) {
				entity.setHoldVolume(SysConstant.BIGDECIMAL_defaultValue);
				entity.setValue(entity.getHoldVolume());
				entity.setHoldStatus(getFlatWareHoldStatus(redeemOrder));
				
				this.closeDetailsService.createCloseDetails(entity, volume, redeemOrder);
				this.orderLogService.createRedeemCloseLog(entity, redeemOrder);
				setFlatWareRep(volume, entity, totalRep);
				volume = SysConstant.BIGDECIMAL_defaultValue;
				
				break;
			}

			if (entity.getHoldVolume().compareTo(volume) > 0) {
				entity.setHoldVolume(entity.getHoldVolume().subtract(volume));
				entity.setValue(entity.getHoldVolume());
				entity.setHoldStatus(getFlatWareHoldStatus(redeemOrder));
			
				this.closeDetailsService.createCloseDetails(entity, volume, redeemOrder);
				this.orderLogService.createRedeemCloseLog(entity, redeemOrder);
				
				setFlatWareRep(volume,  entity, totalRep);
				volume = SysConstant.BIGDECIMAL_defaultValue;
				break;
			}
		}
		if (volume.compareTo(SysConstant.BIGDECIMAL_defaultValue) != 0) {
			logger.info("flatWare orderCode:{}", redeemOrder.getOrderCode());
			// error.define[20020]=赎回时分仓份额异常(CODE:20020)
			throw AMPException.getException(20020);
		}
		this.batchUpdate(orderList);
		return totalRep;
	}
	
	private String getFlatWareHoldStatus(InvestorTradeOrderEntity orderEntity) {
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_cash.equals(orderEntity.getOrderType())) {
			return InvestorTradeOrderEntity.TRADEORDER_holdStatus_closed;
		}
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_cashFailed.equals(orderEntity.getOrderType())) {
			return InvestorTradeOrderEntity.TRADEORDER_holdStatus_refunded;
		}
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_normalRedeem.equals(orderEntity.getOrderType())
			|| InvestorTradeOrderEntity.TRADEORDER_orderType_incrementRedeem.equals(orderEntity.getOrderType())) {
			return InvestorTradeOrderEntity.TRADEORDER_holdStatus_partHolding;
		}
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_specialRedeem.equals(orderEntity.getOrderType())
				|| InvestorTradeOrderEntity.TRADEORDER_orderType_noPayRedeem.equals(orderEntity.getOrderType())) {
			return InvestorTradeOrderEntity.TRADEORDER_holdStatus_partHolding;
		}
		throw new AMPException("订单类型异常");
		
	}
	
	private void setFlatWareRep(BigDecimal holdVolume, InvestorTradeOrderEntity investOrder, FlatWareTotalRep totalRep) {

		FlatWareRep flatWareRep = new FlatWareRep();
		flatWareRep.setBeginAccuralDate(investOrder.getBeginAccuralDate());
		flatWareRep.setHoldVolume(holdVolume);
		flatWareRep.setAccrualStatus(investOrder.getAccrualStatus());
		
		flatWareRep.setCompleteTime(investOrder.getCompleteTime());
		totalRep.getFlatWareRepList().add(flatWareRep);
		if (InvestorTradeOrderEntity.TRADEORDER_accrualStatus_yes.equals(investOrder.getAccrualStatus())) {
			totalRep.setAccruableHoldVolume(totalRep.getAccruableHoldVolume().add(holdVolume));
		}
	}
	
	/**
	 * 查询（分仓）
	 */
	public List<InvestorTradeOrderEntity> findApart(InvestorBaseAccountEntity investorBaseAccount, Product product) {
		return this.investorTradeOrderDao.findApart(investorBaseAccount, product);
	}
		
	/**
	 * 按产品维度进行计息份额份额快照
	 */
	@Transactional(TxType.REQUIRES_NEW)
	public void snapshotVolume(Product product, Date incomeDate){
		this.investorTradeOrderDao.snapshotVolume(product.getOid(), incomeDate);
	}
	
	/**
	 * 体验金产品快照
	 */
	@Transactional(TxType.REQUIRES_NEW)
	public void snapshotTasteCouponVolume(Product product, Date incomeDate){
		this.investorTradeOrderDao.snapshotTasteCouponVolume(product.getOid(), incomeDate);
	}
	
	public List<InvestorTradeOrderEntity> findByBeforeBeginRedeemDateInclusive(Date date, String oid) {
		return this.investorTradeOrderDao.findByBeforerBeginRedeemDateInclusive(date, oid);
	}
	
	public List<InvestorTradeOrderEntity> findByBeforeBeginAccuralDateInclusive(Date date, String oid) {
		return this.investorTradeOrderDao.findByBeforeBeginAccuralDateInclusive(date, oid);
	}
	
	
	/**
	 * 解锁赎回订单
	 */
	public int unlockRedeem(String orderOid) {
		return this.investorTradeOrderDao.unlockRedeem(orderOid);
	}
	
	/**
	 * 解锁计息订单
	 */
	public int unlockAccrual(String orderOid) {
		return this.investorTradeOrderDao.unlockAccrual(orderOid);
		
	}
	
	public List<InvestorTradeOrderEntity> findInterestableApart(PublisherHoldEntity hold, Date incomeDate) {
		return this.investorTradeOrderDao.findInterestableApart(hold, incomeDate);
	}
	
	public boolean isConfirm(String productOid) {
		int cc = this.investorTradeOrderDao.getToConfirmCountByProduct(productOid);
		if (cc == 0) {
			return true;
		}
		return false;
	}
	
	@Transactional(TxType.REQUIRES_NEW)
	public void changeIncomeIntoHoldVolume(Product product) {
		
		if (null != product.getRecPeriodExpAnYield()
				&& product.getRecPeriodExpAnYield().compareTo(BigDecimal.ZERO) > 0) {
			this.investorTradeOrderDao.changeIncomeIntoHoldVolume(product);
			
		}
	}
	
	@Transactional(TxType.REQUIRES_NEW)
	public void unlockRedeem4Cash(Product product) {
		this.investorTradeOrderDao.unlockRedeem4Cash(product);
		
	}
	public int updateHoldApart4Interest(String apartOid, BigDecimal incomeVolume, BigDecimal incomeAmount, 
			BigDecimal netUnitAmount, Date incomeDate, BigDecimal baseAmount, BigDecimal rewardAmount) {
		
		int i = this.investorTradeOrderDao.updateHoldApart4Interest(apartOid, incomeVolume, incomeAmount,
				netUnitAmount, incomeDate, baseAmount, rewardAmount);
		if (i < 1) {
			throw new AMPException("计息失败");
		}
		return i;
	}
	
	public int updateHoldApart4InterestTn(String apartOid, BigDecimal incomeAmount, Date incomeDate) {
		
		int i = investorTradeOrderDao.updateHoldApart4InterestTn(apartOid, incomeAmount, incomeDate);
		if (i < 1) {
			throw new AMPException("计息失败");
		}
		return i;
	}
	
	/** 统计渠道截止昨日投资信息 */
	public List<Object[]> statInvestAmountByChannel(Timestamp startTime, Timestamp endTime) {

		// 统计各渠道昨日投资额
		return this.investorTradeOrderDao.statInvestAmountByChannel(startTime, endTime);
	}

	/** 统计各渠道截止到昨日累计投资额 */
	public List<Object[]> statInvestTotalAmountByChannel(Timestamp endTime) {

		return this.investorTradeOrderDao.statInvestTotalAmountByChannel(endTime);
	}



	public List<InvestorTradeOrderEntity> findByPublisherOffset(PublisherOffsetEntity offset) {
		return this.investorTradeOrderDao.findByPublisherOffset(offset);
	}


	public void flatExpGold() {
		
		if (this.jobLockService.getRunPrivilege(JobEnum.JOB_jobId_flatExpGold.getJobId())) {
			flatExpGoldLog();
		}
	}

	private void flatExpGoldLog() {
		JobLogEntity jobLog = JobLogFactory.getInstance(JobEnum.JOB_jobId_flatExpGold.getJobId());

		try {

			flatExpGoldDo();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			jobLog.setJobMessage(AMPException.getStacktrace(e));
			jobLog.setJobStatus(JobLogEntity.JOB_jobStatus_failed);

		}
		jobLog.setBatchEndTime(DateUtil.getSqlCurrentDate());
		this.jobLogService.saveEntity(jobLog);
		this.jobLockService.resetJob(JobEnum.JOB_jobId_flatExpGold.getJobId());
	}

	private void flatExpGoldDo() {
		String lastOid = "0";
		Date baseDate = DateUtil.getSqlDate();
		while (true) {
			List<InvestorTradeOrderEntity> orderList = this.investorTradeOrderDao.queryFlatExpGold(lastOid, baseDate);
			if (orderList.isEmpty()) {
				break;
			}
			
			for (InvestorTradeOrderEntity orderEntity : orderList) {
				try {
					logger.info("flatExpGoldDo start ,orderCode={}", orderEntity.getOrderCode());
					investorTradeOrderRequireNewService.processOneItem(orderEntity);
					logger.info("flatExpGoldDo success ,orderCode={}", orderEntity.getOrderCode());
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					logger.info("flatExpGoldDo failure ,orderCode={}", orderEntity.getOrderCode());
				}
				
				lastOid = orderEntity.getOid();
			}
			logger.info("flatExpGold 2000 Over {}", DateUtil.getSqlCurrentDate());
		}
		
	}


	public int updateOrderStatus4Abandon(String orderCode) {
		int i = this.investorTradeOrderDao.updateOrderStatus4Abandon(orderCode);
		if (i < 1) {
			throw new AMPException("废单订单状态异常");
		}
		return i;
		
	}

	public int updateOrderStatus4Refund(String orderCode) {
		int i = this.investorTradeOrderDao.updateOrderStatus4Refund(orderCode);
		if (i < 1) {
			throw new AMPException("废单订单状态异常");
		}
		return i;
		
	}
	
	public int unlockRedeemByHold(PublisherHoldEntity hold) {
		int i = this.investorTradeOrderDao.unlockRedeemByHold(hold);
		if (i < 1) {
			throw new AMPException("分仓解锁赎回异常");
		}
		return i;
	}
	
	public List<InvestorTradeOrderEntity> findByPublisherHold(PublisherHoldEntity hold) {
		return this.investorTradeOrderDao.findByPublisherHold(hold);
	}

	public InvestorTradeOrderEntity saveAndFlush(InvestorTradeOrderEntity orderEntity) {
		return investorTradeOrderDao.saveAndFlush(orderEntity);
	}

	public List<InvestorBaseAccountEntity> findAccountByWriteOffStatus() {
		return investorBaseAccountService.findByWriteOffStatus();
	}

	public List<Product> findAllProduct() {
		return productService.findAll();
	}
	
	public void updateBaseAccountStatus(String userOid) {
		investorBaseAccountService.updateBaseAccountStatus(userOid);
	}
	
	public List<InvestorTradeOrderEntity> findSpecialRedeemOrder(String userOid){
		return this.investorTradeOrderDao.findSpecialRedeemOrder(userOid);
	}
	public boolean fetchLock(String originalRedeemOrderCode) {
		return investorTradeOrderDao.updateByOrderCodeAndStatus(originalRedeemOrderCode)==1;
	}

	public MyOrderStatusRes isOrderAllDone(String investorOid){
		int qty = investorTradeOrderDao.findNotDoneQty(investorOid);
		logger.info("用户{}尚未完成的订单数为：{}单", investorOid, qty);
		MyOrderStatusRes res = new MyOrderStatusRes();
		res.setIsOrderAllDone(qty == 0 ? true : false);
		return res;
	}

	/**
	 * 订单查询 
	 */
	public PagesRep<InvestOrRedeemDetailRep> tradeOrderDetail(InvestOrRedeemDetailReq req) {
		PagesRep<InvestOrRedeemDetailRep> returnpages= new PagesRep<InvestOrRedeemDetailRep>();
		int total=this.investorTradeOrderDao.findInvestTotal(StringStr(req.getOrderType()),StringStr(req.getMinOrderAmount()),StringStr(req.getMaxOrderAmount()),StringStr(req.getPhoneNum()),StringStr(req.getRealName()),StringStr(req.getIdCardNo()),StringStr(req.getBeginTime()),StringStr(req.getEndTime()),StringStr(req.getGcbatch()));
		List<Object[]> orderList=this.investorTradeOrderDao.findInvestDetail(StringStr(req.getOrderType()),StringStr(req.getMinOrderAmount()),StringStr(req.getMaxOrderAmount()),StringStr(req.getPhoneNum()),StringStr(req.getRealName()),StringStr(req.getIdCardNo()),StringStr(req.getBeginTime()),StringStr(req.getEndTime()),StringStr(req.getGcbatch()),(req.getPage()-1)*req.getRows(),req.getRows());
		for(Object[] obj:orderList){
			InvestOrRedeemDetailRep rep= new InvestOrRedeemDetailRep();
			rep.setPhoneNum(StringStr(obj[0]));
			rep.setRealName(StringStr(obj[1]));	
			rep.setPublisherOid(StringStr(obj[2]));
			rep.setName(StringStr(obj[3]));
			rep.setChannelOid(StringStr(obj[4]));
			rep.setInvestorOffsetOid(StringStr(obj[5]));
			rep.setPublisherOffsetOid(StringStr(obj[6]));
			rep.setCheckOid(StringStr(obj[7]));
			rep.setHoldOid(StringStr(obj[8]));
			rep.setOrderCode(StringStr(obj[9]));
			rep.setOrderType(StringStr(obj[10]));
			rep.setOrderAmount(StringStr(obj[11]));
			rep.setOrderVolume(StringStr(obj[12]));
			rep.setPayAmount(StringStr(obj[13]));
			rep.setCouponAmount(StringStr(obj[14]));
			rep.setCoupons(StringStr(obj[15]));
			rep.setCouponType(StringStr(obj[16]));
			rep.setPayStatus(StringStr(obj[17]));
			rep.setAcceptStatus(StringStr(obj[18]));
			rep.setRefundStatus(StringStr(obj[19]));
			rep.setOrderStatus(StringStr(obj[20]));
			rep.setCheckStatus(StringStr(obj[21]));
			rep.setContractStatus(StringStr(obj[22]));
			rep.setCreateMan(StringStr(obj[23]));
			rep.setOrderTime(StringStr(obj[24]));
			rep.setCompleteTime(StringStr(obj[25]));
			rep.setPublisherClearStatus(StringStr(obj[26]));
			rep.setPublisherConfirmStatus(StringStr(obj[27]));
			rep.setPublisherCloseStatus(StringStr(obj[28]));
			rep.setInvestorClearStatus(StringStr(obj[29]));
			rep.setInvestorCloseStatus(StringStr(obj[30]));
			rep.setHoldVolume(StringStr(obj[31]));
			rep.setRedeemStatus(StringStr(obj[32]));
			rep.setAccrualStatus(StringStr(obj[33]));
			rep.setBeginAccuralDate(StringStr(obj[34]));
			rep.setCorpusAccrualEndDate(StringStr(obj[35]));
			rep.setBeginRedeemDate(StringStr(obj[36]));
			rep.setTotalIncome(StringStr(obj[37]));
			rep.setTotalBaseIncome(StringStr(obj[38]));
			rep.setTotalRewardIncome(StringStr(obj[39]));
			rep.setYesterdayBaseIncome(StringStr(obj[40]));
			rep.setYesterdayRewardIncome(StringStr(obj[41]));
			rep.setYesterdayIncome(StringStr(obj[42]));
			rep.setToConfirmIncome(StringStr(obj[43]));
			rep.setIncomeAmount(StringStr(obj[44]));
			rep.setExpectIncomeExt(StringStr(obj[45]));
			rep.setExpectIncome(StringStr(obj[46]));
			rep.setValue(StringStr(obj[47]));
			rep.setConfirmDate(StringStr(obj[48]));
			rep.setHoldStatus(StringStr(obj[49]));
			rep.setProvince(StringStr(obj[50]));
			rep.setCity(StringStr(obj[51]));
			rep.setUpdateTime(StringStr(obj[52]));
			rep.setCreateTime(StringStr(obj[53]));
			returnpages.add(rep);

		}
		returnpages.setTotal(total);
		return returnpages;
	}
	public String StringStr(Object obj){
		if(obj==null||obj.equals("null")){
			return "";
		}else{
			return String.valueOf(obj);
		}
	}
	// 指令管理审核导出数据
		public List<List<String>> data(InvestOrRedeemDetailReq req) {
			List<List<String>> data = new ArrayList<List<String>>();
			List<Object[]> orderList=this.investorTradeOrderDao.findInvestDetailDown(StringStr(req.getOrderType()),StringStr(req.getMinOrderAmount()),StringStr(req.getMaxOrderAmount()),StringStr(req.getPhoneNum()),StringStr(req.getRealName()),StringStr(req.getIdCardNo()),StringStr(req.getBeginTime()),StringStr(req.getEndTime()),StringStr(req.getGcbatch()));
			for(Object[] obj:orderList){
				List<String> line = new ArrayList<String>();
				for(int i=0;i<obj.length;i++){
					line.add(StringStr(obj[i]));
				}
				data.add(line);
			}
			return data;
		}
	public List<String> header() {
		List<String> header = new ArrayList<String>();
		header.add("所属投资人账号");
		header.add("所属投资人");
		header.add("所属发行人");
		header.add("所属产品");
		header.add("所属渠道");
		header.add("所属投资人轧差");
		header.add("所属发行人轧差");
		header.add("所属三方对账");
		header.add("所属持有人手册");
		header.add("订单号");
		header.add("交易类型");
		header.add("订单金额");
		header.add("订单份额");
		header.add("实付金额");
		header.add("卡券金额");
		header.add("使用卡券");
		header.add("卡券类型");
		header.add("支付状态");
		header.add("受理状态");
		header.add("退款状态");
		header.add("订单状态");
		header.add("三方对账状态");
		header.add("合同生成状态");
		header.add("订单创建人");
		header.add("订单时间");
		header.add("订单完成时间");
		header.add("发行人清算状态");
		header.add("发行人交收状态");
		header.add("发行人结算状态");
		header.add("投资人清算状态");
		header.add("投资人结算状态");
		header.add("持有份额");
		header.add("可赎回状态");
		header.add("可计息状态");
		header.add("起息日");
		header.add("本金计息截止日期");
		header.add("起始赎回日");
		header.add("累计收益");
		header.add("累计基础收益");
		header.add("累计奖励收益");
		header.add("昨日基础收益");
		header.add("昨日奖励收益");
		header.add("昨日收益");
		header.add("待结转收益");
		header.add("收益金额");
		header.add("预期收益Ext");
		header.add("预期收益");
		header.add("最新市值");
		header.add("收益确认日期");
		header.add("持有状态");
		header.add("省份");
		header.add("城市");
		header.add("更新日间");
		header.add("创建时间");
		return header;
	}
	
	public List<InvestorTradeOrderEntity> findConfirmedOrderByProductOid(String productOid) {
		return investorTradeOrderDao.getConfirmedOrderByProductOid(productOid);
	}
	
	public List<InvestorTradeOrderEntity> findOnwayList(String investorOid) {
		return this.investorTradeOrderDao.findOnwayList(investorOid);
	}
	
	public List<TradeOrderHoldDaysDetail> getCurrentDaysDetailByInvestor(String investorOid, String productOid) {
		return this.investorTradeOrderDao.getTradeOrderHoldDaysDetailByInvestor(investorOid, productOid, DateUtil.getSqlDate());
	}
	
	/**
	 * 是否首次投资定期产品（不包括新手标、精彩活动关联的产品）
	 */
	public boolean isFirstTn(String investorOid) {
		return investorTradeOrderDao.countTradeOrderTn(investorOid) == 0;
	}
	
	@Transactional(value = TxType.REQUIRES_NEW)
	public void refuseOrder(String orderCode) {
		InvestorTradeOrderEntity orderEntity = this.findByOrderCode(orderCode);
		orderEntity.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_refused);
	}

	public boolean hasInvestSuccessOrder(String investorOid) {
		return investorTradeOrderDao.countByInvestSuccess(investorOid)>0;
	}

	public List<InvestorTradeOrderEntity> getBfPlusRedeemList(java.util.Date payDate, java.util.Date createDate){
		return investorTradeOrderDao.getBfPlusRedeemList(payDate, createDate);
	}

	/**
	 * 获取预约单详情列表
	 * @param req
	 * @return
	 */
    public BookingOrderRep getBookingOrderList(BookingOrderReq req) {
		BookingOrderRep rep = new BookingOrderRep();
		int total = investorOpenCycleService.getBookingOrderListCount(req);
		List<Map<String, Object>> orders = investorOpenCycleService.getBookingOrderList(req);
		rep.setTotal(total);
		rep.setRows(orders);
		rep.setPage(req.getPage());
		rep.setSize(req.getSize());
		return rep;
    }

	public BaseRep changeOrderContinueStatus(String uid, String orderCode, int status) {
		BaseRep rep = new BaseRep();
		logger.info("【循环产品续投】设置续投参数uid:{},ordercode:{},status:{}.",uid, orderCode, status );
		InvestorTradeOrderEntity orderEntity = this.findByOrderCode(orderCode);
		Date durationPeriodEndDate = orderEntity.getProduct().getDurationPeriodEndDate();
		if (DateUtil.compare_current_(durationPeriodEndDate)) {
			rep.setErrorCode(-1);
			rep.setErrorMessage("非存续期内，不能设置续投属性");
		} else {
			int i = investorOpenCycleService.setContinueStatusByUidAndOrderCode(uid, orderCode, status);
			if (i < 1) {
				rep.setErrorCode(-1);
				rep.setErrorMessage("设置续投失败");
			}
		}

		return rep;
	}

    public int invalidateTradeOrder(String oid) {
        int result = investorTradeOrderDao.invalidateTradeOrder(oid);
        if (result < 1) {
            throw new AMPException("订单状态异常");
        }
        return result;
    }

	/**
	 * 根据订单oid获取债权匹配信息
	 * @param uid
	 * @param req
	 * @return
	 */
	public P2PCreditorDetailsRep findCreditorDetailByUidAndOrderOid(String uid, P2PCreditorDetailsReq req) {
		P2PCreditorDetailsRep rep = new P2PCreditorDetailsRep();

		int total = p2PCreditorService.countCreditorDetailByUidAndOrderOid(uid, req.getOrderOid());
		if (total < 1) {
			return rep;
		}
		List<P2PCreditorEntity> p2PCreditorEntities = p2PCreditorService.findCreditorDetailByUidAndOrderOid(uid, req.getOrderOid(), req.getPage(), req.getSize());
		for (P2PCreditorEntity entity : p2PCreditorEntities) {
			P2PCreditorDetail detail = new P2PCreditorDetail();
			BeanUtil.copy(detail, entity);
			rep.getRows().add(detail);
		}
		rep.setTotal(total);
		rep.setPage(req.getPage());
		rep.setSize(req.getSize());
		return rep;
	}
}
