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
public class InvestorSpecialRedeemAuthService  {
	
	Logger logger = LoggerFactory.getLogger(InvestorSpecialRedeemAuthService.class);
	
	@Autowired
	private InvestorSpecialRedeemAuthDao investorSpecialRedeemAuthDao;
	
    /**
     * 将授权单更新为已操作
     */
	public void updateOperateStatus(String userId,String operateStatus){
		
		int i = this.investorSpecialRedeemAuthDao.updateOperateStatus(userId, operateStatus);
		if (i < 1) {
			// error.define[130000]=授权单更新状态失败(CODE:130000)
			throw new AMPException(130000);
		}
	}
	
	public InvestorSpecialRedeemAuthEntity findByUserIdOperateStatus(String userId,String operateStatus) {
		InvestorSpecialRedeemAuthEntity specialRedeemAuth = this.investorSpecialRedeemAuthDao.findByUserIdAndOperateStatus(userId,operateStatus);
		if (null == specialRedeemAuth) {
			//error.define[130003]=无此用户待操作的授权单！(CODE:130003)
			throw new AMPException(130003);
		}
		return specialRedeemAuth;
	}

}