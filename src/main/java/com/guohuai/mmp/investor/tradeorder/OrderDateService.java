package com.guohuai.mmp.investor.tradeorder;

import java.sql.Date;
import java.sql.Timestamp;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.ams.product.Product;
import com.guohuai.calendar.TradeCalendarService;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.mmp.tulip.sdk.TulipSDKService;
import com.guohuai.tuip.api.objs.admin.MyCouponReq;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class OrderDateService {
	
	@Autowired
	private TulipSDKService tulipSDKService;
	@Autowired
	private TradeCalendarService tradeCalendarService;
	//节节高付款时间T+2
	public static final int INCREMENT_PAY_DAYS = 2;
	
	/**
	 * 本金计息截止日期
	 */
	public Date getCorpusAccrualEndDate(InvestorTradeOrderEntity orderEntity) {
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_expGoldInvest.equals(orderEntity.getOrderType())) {
			boolean isT = DateUtil.isT(orderEntity.getOrderTime());
			Date baseDate = DateUtil.getSqlDate(orderEntity.getOrderTime().getTime());

			MyCouponReq param = new MyCouponReq();
			param.setCouponId(orderEntity.getCoupons());
			Date corpusAccrualEndDate = null;
			if (!isT) {
				corpusAccrualEndDate = DateUtil.addSQLDays(baseDate, this.tulipSDKService.getCouponDetail(param).getValidPeriod() + 1);
			} else {
				corpusAccrualEndDate = DateUtil.addSQLDays(baseDate, this.tulipSDKService.getCouponDetail(param).getValidPeriod());
			}
			return corpusAccrualEndDate;
		}
		return null;
	}
	
	
	
	/**
	 * 获取体验金、冲销单开始起息日
	 */
	public Date getDirectlyBeginAccuralDate(InvestorTradeOrderEntity orderEntity) {
		boolean isT = DateUtil.isT(orderEntity.getOrderTime());
		Date baseDate = DateUtil.getSqlDate(orderEntity.getOrderTime().getTime());
		
		Date beginAccuralDate = baseDate;
		if (!isT) {
			beginAccuralDate = DateUtil.addSQLDays(baseDate, 1);
		}
		return beginAccuralDate;
	}
	
	/**
	 *  产品详情显示调用
	 */
	public Date getBeginAccuralDate(Product product) {
		return this.getNormalBeginAccuralDate(product, new Timestamp(System.currentTimeMillis()));
	}
	
	public Date getBeginAccuralDate(InvestorTradeOrderEntity orderEntity) {
		if (Product.TYPE_Producttype_01.equals(orderEntity.getProduct().getType().getOid())) {
			return this.getDirectlyBeginAccuralDate(orderEntity);
		}
		if (Product.TYPE_Producttype_03.equals(orderEntity.getProduct().getType().getOid())) {
			return this.get03ProductBeginAccuralDate(orderEntity.getProduct(), orderEntity.getOrderTime());
		}
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_invest.equals(orderEntity.getOrderType())) {
			return this.getNormalBeginAccuralDate(orderEntity.getProduct(), orderEntity.getOrderTime());
		}
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_expGoldInvest.equals(orderEntity.getOrderType())) {
			return this.getDirectlyBeginAccuralDate(orderEntity);
		}
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_reInvest.equals(orderEntity.getOrderType())) {
			return this.getDirectlyBeginAccuralDate(orderEntity);
		}
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_writeOff.equals(orderEntity.getOrderType())) {
			return this.getDirectlyBeginAccuralDate(orderEntity);
		}
		
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_profitInvest.equals(orderEntity.getOrderType())) {
			return this.getDirectlyBeginAccuralDate(orderEntity);
		}
		
		// 定转活处理
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_noPayInvest.equals(orderEntity.getOrderType())) {
			return this.getDirectlyBeginAccuralDate(orderEntity);
		}
		
		// 定转活处理
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_noPayInvest.equals(orderEntity.getOrderType())) {
			return this.getDirectlyBeginAccuralDate(orderEntity);
		}
		
		
		
		throw new AMPException("订单类型不存在，获取开始起息日异常");
	}

	/**
	 *
	 *计算虚拟产品起息日
	 * @author yujianlong
	 * @date 2018/3/28 11:16
	 * @param [product, orderTime]
	 * @return java.sql.Date
	 */
	public Date get03ProductBeginAccuralDate(Product product, Timestamp orderTime) {
		Date beginAccuralDate = null;
		int interestsFirstDays = product.getInterestsFirstDays();
		beginAccuralDate = tradeCalendarService
				.nextTrade(new Date(orderTime.getTime()), interestsFirstDays);
		return beginAccuralDate;

	}


	public Date getNormalBeginAccuralDate(Product product, Timestamp orderTime) {
		Date beginAccuralDate = null;
		if (Product.TYPE_Producttype_01.equals(product.getType().getOid())) {
			return getConfirmDate(product, orderTime);
		}
		
		boolean isT = DateUtil.isT(orderTime);
		int interestsFirstDays = product.getInterestsFirstDays();
		if (Product.Product_dateType_T.equals(product.getInvestDateType())) {
			boolean isTrade = this.tradeCalendarService.isTrade(new java.sql.Date(orderTime.getTime()));
			if (isTrade) {
				if (!isT) {
					interestsFirstDays = interestsFirstDays + 1;
				}
			} else {
				interestsFirstDays = interestsFirstDays + 1;
			}
			beginAccuralDate = tradeCalendarService
					.nextTrade(new Date(orderTime.getTime()), interestsFirstDays);
		} else {
			if (!isT) {
				interestsFirstDays = interestsFirstDays + 1;
			}
			beginAccuralDate = DateUtil.addSQLDays(new Date(orderTime.getTime()), interestsFirstDays);
		}
		return beginAccuralDate;
	}
	
	public Date getConfirmDate(InvestorTradeOrderEntity orderEntity) {
		return this.getConfirmDate(orderEntity.getProduct(), orderEntity.getOrderTime());
	}
	
	
	public Date getConfirmDate(Product product, Timestamp orderTime) {
		Date confirmDate;

		if(Product.TYPE_Producttype_03.equals(product.getType().getOid())){// 快定宝份额确认日，24点为界限，T+1
            confirmDate = tradeCalendarService.nextTrade(new java.sql.Date(orderTime.getTime()), product.getPurchaseConfirmDays());
		}else {// 快定宝份额确认日，15点为界限。T+1
			boolean isT = DateUtil.isT(orderTime);
			int purchaseConfirmDays = product.getPurchaseConfirmDays();
			if (Product.Product_dateType_T.equals(product.getInvestDateType())) {
				boolean isTrade = this.tradeCalendarService.isTrade(new java.sql.Date(orderTime.getTime()));
				if (isTrade) {
					if (!isT) {
						purchaseConfirmDays = purchaseConfirmDays + 1;
					}
				} else {
					purchaseConfirmDays = purchaseConfirmDays + 1;
				}
				confirmDate = tradeCalendarService.nextTrade(new java.sql.Date(orderTime.getTime()), purchaseConfirmDays);
			} else {
				if (!isT) {
					purchaseConfirmDays = purchaseConfirmDays + 1;
				}
				confirmDate = DateUtil.addSQLDays(new java.sql.Date(orderTime.getTime()), purchaseConfirmDays);
			}
		}
		return confirmDate;
	}
	
	/**
	 * 定期产品 开始赎回日永远为空 
	 */
	public Date getBeginRedeemDate(InvestorTradeOrderEntity orderEntity) {
		if (Product.TYPE_Producttype_01.equals(orderEntity.getProduct().getType().getOid())) {
			return null;
		}
		if (Product.TYPE_Producttype_03.equals(orderEntity.getProduct().getType().getOid())) {
			return this.get03ProductBeginRedeemDate(orderEntity);
		}
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_invest.equals(orderEntity.getOrderType())) {
			return this.getNormalBeginRedeemDate(orderEntity);
		}
		
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_expGoldInvest.equals(orderEntity.getOrderType())) {
			return this.getGoldBeginRedeemDate(orderEntity);
		}
		
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_writeOff.equals(orderEntity.getOrderType())) {
			return this.getDirectlyBeginRedeemDate(orderEntity);
		}
		
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_profitInvest.equals(orderEntity.getOrderType())) {
			return this.getDirectlyBeginRedeemDate(orderEntity);
		}
		
		// 定转活
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_noPayInvest.equals(orderEntity.getOrderType())) {
			return this.getDirectlyBeginRedeemDate(orderEntity);
		}
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_reInvest.equals(orderEntity.getOrderType())) {
			return this.getDirectlyBeginRedeemDate(orderEntity);
		}
		throw new AMPException("订单类型不存在，获取开始赎回日异常");
	}

	/**
	 *
	 *获取03产品赎回日
	 * @author yujianlong
	 * @date 2018/3/28 11:31
	 * @param [orderEntity]
	 * @return java.sql.Date
	 */
	public Date get03ProductBeginRedeemDate(InvestorTradeOrderEntity orderEntity) {
		Date beginRedeemDate = null;
		Product product=orderEntity.getProduct();
		Timestamp orderTime = orderEntity.getOrderTime();
		int purchaseConfirmDays = orderEntity.getProduct().getPurchaseConfirmDays();
		boolean isTrade = this.tradeCalendarService.isTrade(new java.sql.Date(orderTime.getTime()));
		if (!isTrade) {
			purchaseConfirmDays = purchaseConfirmDays + 1;
		}
		beginRedeemDate = tradeCalendarService.nextTrade(new java.sql.Date(orderTime.getTime()), purchaseConfirmDays);
		return beginRedeemDate;

	}
	/**
	 * 定期，开始赎回日直接为空
	 * 活期，在申购确认日的基础上进行累加
	 */
	private Date getNormalBeginRedeemDate(InvestorTradeOrderEntity orderEntity) {
		Date beginRedeemDate = null;
		int lockPeriodDays = orderEntity.getProduct().getLockPeriodDays();
		if (orderEntity.getProduct().getLockPeriodDays() == 0) {
			beginRedeemDate = getConfirmDate(orderEntity.getProduct(), orderEntity.getOrderTime());
		} else {
			if (Product.Product_dateType_T.equals(orderEntity.getProduct().getInvestDateType())) {
				beginRedeemDate = this.tradeCalendarService.nextTrade(getConfirmDate(orderEntity.getProduct(), orderEntity.getOrderTime()), lockPeriodDays);
			} else {
				beginRedeemDate = DateUtil.addSQLDays(getConfirmDate(orderEntity.getProduct(), orderEntity.getOrderTime()), lockPeriodDays);
			}
		}
		return beginRedeemDate;
	}
	
	/**
	 * 获取体验金开始赎回日
	 */
	public Date getGoldBeginRedeemDate(InvestorTradeOrderEntity orderEntity) {
		boolean isT = DateUtil.isT(orderEntity.getOrderTime());
		Date baseDate = DateUtil.getSqlDate(orderEntity.getOrderTime().getTime());

		MyCouponReq param = new MyCouponReq();
		param.setCouponId(orderEntity.getCoupons());
		Date beginRedeemDate = null;
		if (!isT) {
			beginRedeemDate = DateUtil.addSQLDays(baseDate, this.tulipSDKService.getCouponDetail(param).getValidPeriod() + 1);
		} else {
			beginRedeemDate = DateUtil.addSQLDays(baseDate, this.tulipSDKService.getCouponDetail(param).getValidPeriod());
		}
		return beginRedeemDate;
	}
	
	/**
	 * 获取开始赎回日
	 * 冲销单
	 */
	public Date getDirectlyBeginRedeemDate(InvestorTradeOrderEntity orderEntity) {
		boolean isT = DateUtil.isT(orderEntity.getOrderTime());
		Date baseDate = DateUtil.getSqlDate(orderEntity.getOrderTime().getTime());
		
		Date beginRedeemDate = baseDate;
		if (!isT) {
			beginRedeemDate = DateUtil.addSQLDays(baseDate, 1);
		}
		return beginRedeemDate;
	}
	
	public Date getRedeemDate(Product product, Timestamp orderTime) {		
		int payDays = product.getRedeemConfirmDays();
		log.debug("产品提现确认时长为{}，当前时间是{}", payDays, orderTime.toLocalDateTime());
		if (Product.Product_dateType_T.equals(product.getRredeemDateType())) {
			log.debug("产品提现规则为T日");
			boolean isTrade = this.tradeCalendarService.isTrade(new java.sql.Date(orderTime.getTime()));
			if (!isTrade) {
				payDays = payDays + 1;
				log.debug("当天非交易日");
			}
			log.debug("payDays:{}", payDays);
			return tradeCalendarService.nextTrade(new java.sql.Date(orderTime.getTime()), payDays);
		} else {
			return DateUtil.addSQLDays(new java.sql.Date(orderTime.getTime()), payDays);
		}
	}
}
