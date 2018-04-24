package com.guohuai.mmp.investor.tradeorder;

import java.math.BigDecimal;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.ams.channel.ChannelService;
import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductService;
import com.guohuai.basic.common.SeqGenerator;
import com.guohuai.cache.service.CacheHoldService;
import com.guohuai.cache.service.CacheProductService;
import com.guohuai.cache.service.CacheSPVHoldService;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.DecimalUtil;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountService;
import com.guohuai.mmp.investor.cashflow.InvestorCashFlowService;
import com.guohuai.mmp.investor.orderlog.OrderLogEntity;
import com.guohuai.mmp.investor.orderlog.OrderLogService;
import com.guohuai.mmp.platform.accment.AccParam;
import com.guohuai.mmp.platform.accment.AccmentService;
import com.guohuai.mmp.platform.accment.TradeRequest;
import com.guohuai.mmp.platform.payment.PayParam;
import com.guohuai.mmp.platform.payment.PayRequest;
import com.guohuai.mmp.platform.payment.PaymentServiceImpl;
import com.guohuai.mmp.platform.publisher.offset.OffsetService;
import com.guohuai.mmp.platform.publisher.offset.PublisherOffsetService;
import com.guohuai.mmp.platform.publisher.product.offset.ProductOffsetService;
import com.guohuai.mmp.platform.tulip.TulipService;
import com.guohuai.mmp.publisher.hold.PublisherHoldEntity;
import com.guohuai.mmp.publisher.hold.PublisherHoldService;
import com.guohuai.mmp.serialtask.InvestNotifyParams;
import com.guohuai.mmp.serialtask.SerialTaskEntity;
import com.guohuai.mmp.serialtask.SerialTaskReq;
import com.guohuai.mmp.serialtask.SerialTaskService;
import com.guohuai.mmp.sys.CodeConstants;
import com.guohuai.mmp.tulip.sdk.TulipSDKService;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class InvestorSpecialRedeemService  {
	
	Logger logger = LoggerFactory.getLogger(InvestorSpecialRedeemService.class);
	
	@Autowired
	private InvestorSpecialRedeemDao investorSpecialRedeemDao;
	
    /**
     * 将授权单更新为已操作
     */
	public void updateLeftSpecialRedeemAmount(String userId,BigDecimal orderAmount){
		
		int i = this.investorSpecialRedeemDao.updateLeftSpecialRedeemAmount(userId, orderAmount);
		if (i < 1) {
			// error.define[130001]=更新特殊赎回剩余份额失败！(CODE:130001)
			throw new AMPException(130001);
		}
	}
	
	public InvestorSpecialRedeemEntity findByUserId(String userId) {
		InvestorSpecialRedeemEntity specialRedeem = this.investorSpecialRedeemDao.findByUserId(userId);
		if (null == specialRedeem) {
			//error.define[130002]=用户特殊赎回订单不存在！请检查并手动维护。(CODE:130002)
			throw new AMPException(130002);
		}
		return specialRedeem;
	}
}