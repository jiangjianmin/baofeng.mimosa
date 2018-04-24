package com.guohuai.mmp.platform.publisher.offset;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductService;
import com.guohuai.cache.service.CacheProductService;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.DecimalUtil;
import com.guohuai.mmp.investor.baseaccount.statistics.InvestorStatisticsService;
import com.guohuai.mmp.investor.tradeorder.FlatWareTotalRep;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderService;
import com.guohuai.mmp.platform.baseaccount.statistics.PlatformStatisticsService;
import com.guohuai.mmp.publisher.baseaccount.statistics.PublisherStatisticsService;
import com.guohuai.mmp.publisher.hold.PublisherHoldEntity;
import com.guohuai.mmp.publisher.hold.PublisherHoldService;
import com.guohuai.mmp.sys.SysConstant;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OffsetService {
	
	@Autowired
	private InvestorTradeOrderService investorTradeOrderService;
	@Autowired
	private PublisherHoldService publisherHoldService;
	@Autowired
	private ProductService productService;
	@Autowired
	private InvestorStatisticsService investorStatisticsService;
	@Autowired
	private PlatformStatisticsService platformStatisticsService;
	@Autowired
	private PublisherStatisticsService publisherStatisticsService;
	@Autowired
	private CacheProductService cacheProductService;
	
	
	@Transactional(value = TxType.REQUIRES_NEW)
	public void processItem(String orderOid, VolumeConfirmRep iRep) {
		InvestorTradeOrderEntity orderEntity = investorTradeOrderService.findOne(orderOid);
		processItem(orderEntity, iRep);
	}
	
	public void processItem(InvestorTradeOrderEntity orderEntity) {
		processItem(orderEntity, new VolumeConfirmRep());
	}
	
	public void processItem(InvestorTradeOrderEntity orderEntity, VolumeConfirmRep iRep) {

		if (InvestorTradeOrderEntity.TRADEORDER_orderType_normalRedeem.equals(orderEntity.getOrderType()) 
				|| InvestorTradeOrderEntity.TRADEORDER_orderType_clearRedeem.equals(orderEntity.getOrderType())
				|| InvestorTradeOrderEntity.TRADEORDER_orderType_noPayRedeem.equals(orderEntity.getOrderType())
				|| InvestorTradeOrderEntity.TRADEORDER_orderType_cash.equals(orderEntity.getOrderType())
				|| InvestorTradeOrderEntity.TRADEORDER_orderType_cashFailed.equals(orderEntity.getOrderType())
				|| InvestorTradeOrderEntity.TRADEORDER_orderType_incrementRedeem.equals(orderEntity.getOrderType())) {
			/** 更新合仓、分仓 */
			FlatWareTotalRep rep = this.publisherHoldService.normalRedeem(orderEntity);

			/** 投资人数据统计 */
			investorStatisticsService.redeemStatistics(orderEntity);
			/** 更新 SPV持仓、产品*/
			decreaseconfirm(orderEntity);
			
			/** 还本付息 */  /** 产品募集失败--退款处理 */
			if (InvestorTradeOrderEntity.TRADEORDER_orderType_cash.equals(orderEntity.getOrderType())
					|| InvestorTradeOrderEntity.TRADEORDER_orderType_cashFailed.equals(orderEntity.getOrderType())) {
				
				investorStatisticsService.repayLoanStatistics(orderEntity.getInvestorBaseAccount(), rep.getTHoldVolume());
				investorStatisticsService.repayInterestStatistics(orderEntity.getInvestorBaseAccount(), rep.getTIncomeAmount());
				/** 平台统计--还本  */
				this.platformStatisticsService.updateStatistics4RepayLoanConfirm(rep.getTHoldVolume());
			}
			iRep.setRedeemAmount(orderEntity.getOrderAmount());
		}

		/**
		 * 体验金投资、冲销单、投资单、二级邀请奖励收益投资
		 * noPayInvest 针对 定转活投资
		 */
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_invest.equals(orderEntity.getOrderType()) ||
				InvestorTradeOrderEntity.TRADEORDER_orderType_writeOff.equals(orderEntity.getOrderType()) ||
				InvestorTradeOrderEntity.TRADEORDER_orderType_expGoldInvest.equals(orderEntity.getOrderType()) ||
				InvestorTradeOrderEntity.TRADEORDER_orderType_noPayInvest.equals(orderEntity.getOrderType()) || 
				InvestorTradeOrderEntity.TRADEORDER_orderType_profitInvest.equals(orderEntity.getOrderType())
				) {
            investConfirm(iRep, orderEntity);
			orderEntity.setPublisherConfirmStatus(InvestorTradeOrderEntity.TRADEORDER_publisherConfirmStatus_confirmed);
			orderEntity.setHoldStatus(InvestorTradeOrderEntity.TRADEORDER_holdStatus_holding);
			orderEntity.setCompleteTime(DateUtil.getSqlCurrentDate());
		}
		
		orderEntity.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_confirmed);

		InvestorTradeOrderEntity confirmedOrderEntity = this.investorTradeOrderService.saveEntity(orderEntity);
		log.info("=====赎回序列化任务执行份额确认,订单状态过度到confirmed时save返回的订单对象====confirmedOrderEntity:{}", JSON.toJSONString(confirmedOrderEntity));
	}

	public void plusProcessItem(InvestorTradeOrderEntity orderEntity, VolumeConfirmRep iRep) {
        if (InvestorTradeOrderEntity.TRADEORDER_orderType_bfPlusRedeem.equals(orderEntity.getOrderType())) {
            /** 更新合仓、分仓 */
//            this.publisherHoldService.plusNormalRedeem(orderEntity);
            /** 投资人数据统计 */
            investorStatisticsService.redeemStatistics(orderEntity);
            /** 更新 SPV持仓、产品*/
            decreaseconfirm(orderEntity);
            iRep.setRedeemAmount(orderEntity.getOrderAmount());
        }
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_invest.equals(orderEntity.getOrderType()) ||
				InvestorTradeOrderEntity.TRADEORDER_orderType_noPayInvest.equals(orderEntity.getOrderType())) {
            plusInvestConfirm(iRep, orderEntity);
			orderEntity.setPublisherConfirmStatus(InvestorTradeOrderEntity.TRADEORDER_publisherConfirmStatus_confirmed);
			orderEntity.setHoldStatus(InvestorTradeOrderEntity.TRADEORDER_holdStatus_holding);
			orderEntity.setCompleteTime(DateUtil.getSqlCurrentDate());
		}
		orderEntity.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_confirmed);
		InvestorTradeOrderEntity confirmedOrderEntity = this.investorTradeOrderService.saveEntity(orderEntity);
		log.info("=====赎回序列化任务执行份额确认,订单状态过度到confirmed时save返回的订单对象====confirmedOrderEntity:{}", JSON.toJSONString(confirmedOrderEntity));
	}

	public void decreaseconfirm(InvestorTradeOrderEntity orderEntity) {
		/** 产品 */
		this.productService.update4RedeemConfirm(orderEntity.getProduct(), orderEntity.getOrderVolume());
		/** SPV */
		this.publisherHoldService.update4RedeemConfirm(orderEntity.getProduct(), orderEntity.getOrderVolume());
		/** 发行人统计--借款*/
		this.publisherStatisticsService.increaseTotalReturnAmount(orderEntity);
		/** 平台统计--累计交易额、累计还款额 */
		this.platformStatisticsService.updateStatistics4RedeemConfirm(orderEntity.getOrderAmount());
	}


	public void investConfirm(VolumeConfirmRep iRep, InvestorTradeOrderEntity orderEntity) {
		String productType = orderEntity.getProduct().getType().getOid();
		
		BigDecimal lockRedeemHoldVolume = SysConstant.BIGDECIMAL_defaultValue, redeemableHoldVolume = SysConstant.BIGDECIMAL_defaultValue;
		String redeemStatus = InvestorTradeOrderEntity.TRADEORDER_redeemStatus_no, accrualStatus = InvestorTradeOrderEntity.TRADEORDER_accrualStatus_no;
		BigDecimal accruableHoldVolume = SysConstant.BIGDECIMAL_defaultValue;
		if (Product.TYPE_Producttype_02.equals(productType)) { // 活期
			if (DateUtil.isLessThanOrEqualToday(orderEntity.getBeginRedeemDate())) {
				redeemableHoldVolume = orderEntity.getOrderVolume(); //可赎回份额增加
				redeemStatus = InvestorTradeOrderEntity.TRADEORDER_redeemStatus_yes;
			} else {
				lockRedeemHoldVolume = orderEntity.getOrderVolume(); //锁定赎回份额减少
				redeemStatus = InvestorTradeOrderEntity.TRADEORDER_redeemStatus_no;
			}
		} else {
			lockRedeemHoldVolume = orderEntity.getOrderVolume();
		}
		
		if (DateUtil.isLessThanOrEqualToday(orderEntity.getBeginAccuralDate())) {
			accruableHoldVolume = orderEntity.getOrderAmount();
			accrualStatus = InvestorTradeOrderEntity.TRADEORDER_accrualStatus_yes;
		}
		
		/** 正常申购单、冲销单、活转定--活期投资、定转活--活期投资、二级邀请奖励收益投资*/
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_invest.equals(orderEntity.getOrderType())
				|| InvestorTradeOrderEntity.TRADEORDER_orderType_writeOff.equals(orderEntity.getOrderType())
				|| InvestorTradeOrderEntity.TRADEORDER_orderType_noPayInvest.equals(orderEntity.getOrderType())
				|| InvestorTradeOrderEntity.TRADEORDER_orderType_profitInvest.equals(orderEntity.getOrderType())) {
			PublisherHoldEntity publisherHold = orderEntity.getPublisherHold();
			/** 更新订单状态 */
			if (Product.TYPE_Producttype_01.equals(orderEntity.getProduct().getType().getOid())) {
				publisherHold.setProductAlias(this.cacheProductService.getProductAlias(orderEntity.getProduct()));
			}
			publisherHold.setLockRedeemHoldVolume(publisherHold.getLockRedeemHoldVolume().add(lockRedeemHoldVolume));
			publisherHold.setRedeemableHoldVolume(publisherHold.getRedeemableHoldVolume().add(redeemableHoldVolume));
			publisherHold.setAccruableHoldVolume(publisherHold.getAccruableHoldVolume().add(accruableHoldVolume));
			publisherHold.setHoldVolume(publisherHold.getHoldVolume().add(orderEntity.getOrderVolume()));
			publisherHold.setToConfirmInvestVolume(publisherHold.getToConfirmInvestVolume().subtract(orderEntity.getOrderVolume()));
			// error.define[30074]=份额确认异常(CODE:30074)
			DecimalUtil.isValOutGreatThanOrEqualZero(publisherHold.getToConfirmInvestVolume(), 30074);
			if (publisherHold.getHoldStatus().equals(PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_toConfirm)) {
				publisherHold.setHoldStatus(PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_holding);
			}
			this.publisherHoldService.saveEntity(publisherHold);

		}
		/** 体验金投资 */
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_expGoldInvest.equals(orderEntity.getOrderType())) {
			PublisherHoldEntity hold = orderEntity.getPublisherHold();
				hold.setExpGoldVolume(hold.getExpGoldVolume().add(orderEntity.getOrderVolume()));
				hold.setAccruableHoldVolume(accruableHoldVolume);
				hold.setHoldVolume(hold.getHoldVolume().add(orderEntity.getOrderVolume()));
				hold.setToConfirmInvestVolume(hold.getToConfirmInvestVolume().subtract(orderEntity.getOrderAmount()));
				if (hold.getHoldStatus().equals(PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_toConfirm)) {
					hold.setHoldStatus(PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_holding);
				}
				this.publisherHoldService.saveEntity(hold);
		}
		
		orderEntity.setAccrualStatus(accrualStatus);
		orderEntity.setRedeemStatus(redeemStatus);
		
		/** 更新产品 */
		this.productService.update4InvestConfirm(orderEntity.getProduct(), orderEntity.getOrderVolume());
		/** 更新SPV持仓 */
		this.publisherHoldService.update4InvestConfirm(orderEntity.getProduct(), orderEntity.getOrderVolume());
		
		/** 投资人数据统计 */
		investorStatisticsService.investStatistics(orderEntity, orderEntity.getInvestorBaseAccount());
		/** 平台统计--投资单份额确认:累计交易额、累计借款额 */
		this.platformStatisticsService.updateStatistics4InvestConfirm(orderEntity.getOrderAmount());
		/** 平台统计--更新投资人数、持仓人数 */
		this.platformStatisticsService.increaseInvestorAmount(orderEntity.getInvestorBaseAccount());
		/** 发行人统计--投资人数、持仓人数 */
		this.publisherStatisticsService.increaseInvestorAmount(orderEntity.getInvestorBaseAccount(),
				orderEntity.getPublisherBaseAccount());
		/** 发行人统计--借款 */
		this.publisherStatisticsService.increaseTotalLoanAmount(orderEntity);
		
		iRep.setInvestAmount(orderEntity.getOrderAmount());

	}

    public void plusInvestConfirm(VolumeConfirmRep iRep, InvestorTradeOrderEntity orderEntity) {
        String productType = orderEntity.getProduct().getType().getOid();

        BigDecimal lockRedeemHoldVolume = SysConstant.BIGDECIMAL_defaultValue, redeemableHoldVolume = SysConstant.BIGDECIMAL_defaultValue;
        String redeemStatus = InvestorTradeOrderEntity.TRADEORDER_redeemStatus_no, accrualStatus = InvestorTradeOrderEntity.TRADEORDER_accrualStatus_no;
        BigDecimal accruableHoldVolume = SysConstant.BIGDECIMAL_defaultValue;
        if (Product.TYPE_Producttype_03.equals(productType)) {//快定宝
                redeemableHoldVolume = orderEntity.getOrderVolume(); //可赎回份额增加
                redeemStatus = InvestorTradeOrderEntity.TRADEORDER_redeemStatus_yes;
        } else {
            lockRedeemHoldVolume = orderEntity.getOrderVolume();
        }

        if (DateUtil.isLessThanOrEqualToday(orderEntity.getBeginAccuralDate())) {
            accruableHoldVolume = orderEntity.getOrderAmount();
            accrualStatus = InvestorTradeOrderEntity.TRADEORDER_accrualStatus_yes;
        }

        /** 正常申购单、冲销单、活转定--活期投资、定转活--活期投资、二级邀请奖励收益投资*/
        if (InvestorTradeOrderEntity.TRADEORDER_orderType_invest.equals(orderEntity.getOrderType())
                || InvestorTradeOrderEntity.TRADEORDER_orderType_noPayInvest.equals(orderEntity.getOrderType())) {
            PublisherHoldEntity publisherHold = orderEntity.getPublisherHold();
            /** 更新订单状态 */
            if (Product.TYPE_Producttype_03.equals(productType)) {
                publisherHold.setProductAlias(this.cacheProductService.getProductAlias(orderEntity.getProduct()));
            }

	        publisherHold.setExpectIncomeExt(publisherHold.getExpectIncomeExt().add(orderEntity.getExpectIncome()));
	        publisherHold.setExpectIncome(publisherHold.getExpectIncome().subtract(orderEntity.getExpectIncome()));

            publisherHold.setLockRedeemHoldVolume(publisherHold.getLockRedeemHoldVolume().add(lockRedeemHoldVolume));
            publisherHold.setRedeemableHoldVolume(publisherHold.getRedeemableHoldVolume().add(redeemableHoldVolume));
            publisherHold.setAccruableHoldVolume(publisherHold.getAccruableHoldVolume().add(accruableHoldVolume));
            publisherHold.setHoldVolume(publisherHold.getHoldVolume().add(orderEntity.getOrderVolume()));
            publisherHold.setToConfirmInvestVolume(publisherHold.getToConfirmInvestVolume().subtract(orderEntity.getOrderVolume()));
            // error.define[30074]=份额确认异常(CODE:30074)
            DecimalUtil.isValOutGreatThanOrEqualZero(publisherHold.getToConfirmInvestVolume(), 30074);
            if (publisherHold.getHoldStatus().equals(PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_toConfirm)) {
                publisherHold.setHoldStatus(PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_holding);
            }
	        //这里增加确认日期？
            // 答：是的
	        publisherHold.setConfirmDate(Date.valueOf(LocalDate.now()));
            this.publisherHoldService.saveEntity(publisherHold);

        }
        orderEntity.setAccrualStatus(accrualStatus);
        orderEntity.setRedeemStatus(redeemStatus);
	    orderEntity.setConfirmDate(Date.valueOf(LocalDate.now()));

        /** 更新产品 */
        this.productService.update4InvestConfirm(orderEntity.getProduct(), orderEntity.getOrderVolume());
        /** 更新SPV持仓 */
        this.publisherHoldService.update4InvestConfirm(orderEntity.getProduct(), orderEntity.getOrderVolume());

        /** 投资人数据统计 */
        investorStatisticsService.investStatistics(orderEntity, orderEntity.getInvestorBaseAccount());
        /** 发行人统计--投资人数、持仓人数 */
        this.publisherStatisticsService.increaseInvestorAmount(orderEntity.getInvestorBaseAccount(),
                orderEntity.getPublisherBaseAccount());

        iRep.setInvestAmount(orderEntity.getOrderAmount());
    }
}
