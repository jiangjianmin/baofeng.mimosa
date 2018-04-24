//package com.guohuai.mmp.platform.payment;
//
//import java.math.BigDecimal;
//import java.text.ParseException;
//
//import javax.transaction.Transactional;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import com.ghg.pay.api.OperatorPayService;
//import com.ghg.pay.api.rep.QueryMiddleAccountResp;
//import com.ghg.pay.api.req.QueryAccountDetailsRequest;
//import com.ghg.pay.api.req.QueryAccountDetailsRequestBuilder;
//import com.ghg.pay.gateway.BalanceService;
//import com.ghg.pay.sina.model.query.QueryAccountDetailsResp;
//import com.ghg.pay.sina.model.query.QueryBalanceResp;
//import com.google.common.base.Splitter;
//import com.guohuai.component.util.DateUtil;
//import com.guohuai.mmp.investor.baseaccount.QueryAccountDetailsReq;
//import com.guohuai.mmp.sys.SysConstant;
//
//@Service
//@Transactional
//public class PlatformBalanceService {
//
//	Logger logger = LoggerFactory.getLogger(PlatformBalanceService.class);
//
//	@Autowired
//	private BalanceService balanceService;
//	@Autowired
//	private OperatorPayService operatorPayService;
//	
//	public static final String BALANCE_APPLY_SUCCESS = "APPLY_SUCCESS";
//	
//	/**
//	 * 查询投资者账户余额
//	 * @param String uid
//	 * @return BigDecimal
//	 */
//	public BigDecimal getBalance(String uid) {
//		QueryBalanceResp rep = this.queryBalance(uid);
//		return new BigDecimal(rep.getAvailable_balance() == null ? "0" : rep.getAvailable_balance());
//	}
//	/**
//	 * 查询存钱罐账户昨日收益
//	 * @param uid
//	 * @return BigDecimal
//	 */
//	public BigDecimal getBonus(String uid) {
//		QueryBalanceResp rep = this.queryBalance(uid);
//		if (!BALANCE_APPLY_SUCCESS.equals(rep.getResponse_code())) {
//			logger.info(uid + "==" + rep.getResponse_code() + "==" + rep.getResponse_message());
//		}
//		logger.info(rep.toString());
//		Splitter splitter = Splitter.on("^");
//		
//		return new BigDecimal(rep.getBonus() == null ? "0" : splitter.splitToList(rep.getBonus()).get(0));
//	}
//	
//	/**
//	 * 查询存钱罐余额收益
//	 * @param uid
//	 * @return {@link QueryBalanceResp resp}
//	 */
//	private QueryBalanceResp queryBalance(String uid) {
//		return this.balanceService.queryBalance(uid);
//	}
//	/**
//	 * 查询SPV账户余额
//	 * @param uid
//	 * @return BigDecimal
//	 */
//	public BigDecimal getCompanyBalance(String uid) {
//		QueryBalanceResp rep = this.queryCompanyBalance(uid);
//		return new BigDecimal(rep.getAvailable_balance() == null ? "0" : rep.getAvailable_balance());
//	}
//	/**
//	 * 查询SPV账户余额
//	 * @param uid
//	 * @return {@link QueryBalanceResp}
//	 */
//	private QueryBalanceResp queryCompanyBalance(String uid) {
//		return this.balanceService.queryCompanyBalance(uid);
//	}
//	/**
//	 * 查询平台对公基本户余额
//	 * @param uid
//	 * @return BigDecimal
//	 */
//	public BigDecimal getPlatFormBasicBalance(String uid) {
//		QueryBalanceResp rep = this.queryPlatFormBasicBalance(uid);
//		return new BigDecimal(rep.getAvailable_balance() == null ? "0" : rep.getAvailable_balance());
//	}
//	/**
//	 * 查询平台（对公基本户）余额
//	 * @param uid
//	 * @return {@link QueryBalanceResp}
//	 */
//	private QueryBalanceResp queryPlatFormBasicBalance(String uid) {
//		return this.balanceService.queryPlatFormBasicBalance(uid);
//	}
//	
//	/**
//	 * 中间户--投资专用(代收投资 == 代付借款)
//	 * @return BigDecimal
//	 */
//	public BigDecimal getMiddleAccount4InvestCollect() {
//		QueryMiddleAccountResp rep = this.balanceService.queryMiddleAccount4InvestCollect();
//		String accountList = rep.getAccount_list();
//		if (null == accountList || !PlatformBalanceService.BALANCE_APPLY_SUCCESS.equals(rep.getResponse_code())) {
//			return SysConstant.BIGDECIMAL_defaultValue;
//		} else {
//			return new BigDecimal(accountList.split("\\^")[2]);
//		}
//	}
//	
//	/**
//	 * 中间户--还款专用
//	 * @return BigDecimal
//	 */
//	public BigDecimal getMiddleAccount4RedeemCollect() {
//		QueryMiddleAccountResp rep = this.balanceService.queryMiddleAccount4RedeemCollect();
//		String accountList = rep.getAccount_list();
//		if (null == accountList || !PlatformBalanceService.BALANCE_APPLY_SUCCESS.equals(rep.getResponse_code())) {
//			return SysConstant.BIGDECIMAL_defaultValue;
//		} else {
//			return new BigDecimal(accountList.split("\\^")[2]);
//		}
//	}
//	
//	
//	/**
//	 * 备付金余额
//	 */
//	public BigDecimal getReserveBalance(String uid) {
//		QueryBalanceResp resp = this.balanceService.queryReserveBalance(uid);
//		return new BigDecimal(resp.getBalance());
//	}
//	
//	/**
//	 * sina用户收支明细
//	 * @throws ParseException 
//	 */
//	public QueryAccountDetailsResp queryAccountDetails(QueryAccountDetailsReq req)  {
//		
//		QueryAccountDetailsRequest request = QueryAccountDetailsRequestBuilder.n()
//				.pageNo(req.getPageNo())
//				.pageSize(req.getPageSize())
//				.extendsParam(req.getExtendsParam())
//				.startTime(DateUtil.parse(req.getStartTime(), DateUtil.fullDatePattern))
//				.endTime(DateUtil.parse(req.getEndTime(), DateUtil.fullDatePattern))
//				.identityId(req.getIdentityId())
//				.build();
//		
//		QueryAccountDetailsResp resp = operatorPayService.queryAccountDetails(request);
//		return resp;
//	}
//}
