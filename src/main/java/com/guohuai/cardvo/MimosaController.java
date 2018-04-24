package com.guohuai.cardvo;

//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileWriter;
//import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.guohuai.basic.cardvo.req.cardreq.ValidCardReq;
import com.guohuai.basic.cardvo.req.trigger.InvestTriggerReq;
import com.guohuai.basic.cardvo.req.userInfoReq.CardbalanceReq;
import com.guohuai.basic.cardvo.req.userInfoReq.MUAllReq;
import com.guohuai.basic.cardvo.req.userInfoReq.TimePageReq;
import com.guohuai.basic.cardvo.req.userInfoReq.UserInvedtorOidsReq;
import com.guohuai.basic.cardvo.req.userInfoReq.UserLockReq;
import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.basic.message.DealMessageEntity;
import com.guohuai.cardvo.service.MimosaService;
import com.guohuai.cardvo.service.TradeOrderStatisticsService;
import com.guohuai.tuip.api.TulipSdk;
import com.guohuai.usercenter.api.UserCenterSdk;

import lombok.extern.slf4j.Slf4j;


/**
 * 用户信息
 * @author yujianlong
 *
 */
@Slf4j
@RestController
@RequestMapping(value = "/mimosa/cardvo/mimosaUC/",produces = "application/json")
public class MimosaController extends BaseController {
//	1.已保存 2.已删除 3.已确认 4.已发放已生效 5.已发放已无效 6 已发放已删除
//	private static final int defaultCardStaus=4;
//	 private Log log = LogFactory.getLog(this.getClass());  
	@Autowired
	private MimosaService mimosaService;
	@Autowired
	private TradeOrderStatisticsService TradeOrderStatisticsService;
//	@Autowired
//	private UserCenterSdkService userCenterSdkService;
//	
	@Autowired
	UserCenterSdk userCenterSdk;
	@Autowired
	TulipSdk tulipSdk;
	
	
	/**
	 * 获取用户触发条件
	 * @param investTriggerReq
	 * @return
	 */
	@RequestMapping(value = "/getTriggerCode", method = RequestMethod.POST)
	public Object getTriggerCode(@RequestBody InvestTriggerReq investTriggerReq){
		
		return mimosaService.getTriggerCode(investTriggerReq.getProductOid(), investTriggerReq.getIsAuto(), investTriggerReq.getOrderType(), investTriggerReq.getOrderStatus());
	}
	/**
	 * 中间表处理
	 * @param investTriggerReq
	 * @return
	 */
	@RequestMapping(value = "/tradeOrderStatisticsHandle", method = RequestMethod.POST)
	public Map<String,String> tradeOrderStatisticsHandle(@RequestBody DealMessageEntity dealMessageEntity){
		
		return TradeOrderStatisticsService.Handle(dealMessageEntity);
	}
	

	
	/**
	 * 获取数量publisherHoldSql
	 * @param mUAllReq
	 * @return
	 */
	@RequestMapping(value = "/counPublisherHold", method = RequestMethod.POST)
	public @ResponseBody Long counPublisherHold(@RequestBody @Valid MUAllReq mUAllReq) {
		return mimosaService.counPublisherHold(mUAllReq);
	}

	/**
	 * publisherHoldSql获取map
	 * @param mUAllReq
	 * @return
	 */
	@RequestMapping(value = "/query2MapsPublisherHold", method = RequestMethod.POST)
	public @ResponseBody List<Map<String, Object>> query2MapsPublisherHold(@RequestBody @Valid MUAllReq mUAllReq) {
		return mimosaService.query2MapsPublisherHold(mUAllReq);

	}
	
	/**
	 * 判断用户是否绑卡过
	 * @param userOid
	 * @return
	 */
	@RequestMapping(value = "/findIdNumByUserOid", method = RequestMethod.POST)
	public @ResponseBody Object findIdNumByUserOid(String userOid) {
		return mimosaService.findIdNumByUserOid(userOid);
		
	}
	/**
	 * 获取用户手机号和用户名
	 * @param userOid
	 * @return
	 */
	@RequestMapping(value = "/getUserInfoByUserId", method = RequestMethod.POST)
	public @ResponseBody Map<String,String> getUserInfoByUserId(String userOid) {
		return mimosaService.getUserInfoByUserId(userOid);
		
	}

	/**
	 * publisherHoldSql获取ids
	 * @param mUAllReq
	 * @return
	 */
	@RequestMapping(value = "/query2IdListPublisherHold", method = RequestMethod.POST)
	public @ResponseBody List<String> query2IdListPublisherHold(@RequestBody @Valid MUAllReq mUAllReq) {
		return mimosaService.query2IdListPublisherHold(mUAllReq);
	}
	
	
	
	/**
	 * 获取用户使用红包的订单id
	 * @param userInvedtorOidsReq
	 * @return
	 */
	@RequestMapping(value = "/getInvestOidsByUserOids", method = RequestMethod.POST)
	public @ResponseBody List<Map<String, Object>> getInvestOidsByUserOids(@RequestBody @Valid UserInvedtorOidsReq userInvedtorOidsReq){
		return mimosaService.getInvestOidsByUserOids(userInvedtorOidsReq);
	}
	
	
	
	
	
	
	/**
	 *  竞猜宝和新手标
	 * @return
	 */
	@RequestMapping(value = "/getInvalidProductIds", method = RequestMethod.POST)
	public @ResponseBody List<String> getInvalidProductIds(){
		return mimosaService.getInvalidProductIds();
	}
	/**排除
	 * 判断是否定期产品 新手标和竞猜宝
	 * @param productOid
	 * @return
	 */
	@RequestMapping(value = "/isDepositProduct", method = RequestMethod.POST)
	public @ResponseBody Object isDepositProduct(@RequestBody @Valid ValidCardReq validCardReq){
		return mimosaService.isDepositProduct(validCardReq);
	}
	
	/**
	 * 改变用户锁定状态
	 * 用户
	 * @param userLockReq
	 * @return
	 */
	@RequestMapping(value = "/changeStasticsUserLockStatus", method = RequestMethod.POST)
	public @ResponseBody int changeStasticsUserLockStatus(@RequestBody @Valid UserLockReq userLockReq) {
		return mimosaService.changeStasticsUserLockStatus(userLockReq);
	}
	
	/**
	 * 改变用户锁定状态
	 * @param userLockReq
	 * @return
	 */
	@RequestMapping(value = "/changeTradeorderUserLockStatus", method = RequestMethod.POST)
	public @ResponseBody int changeTradeorderUserLockStatus(@RequestBody @Valid UserLockReq userLockReq) {
		return mimosaService.changeTradeorderUserLockStatus(userLockReq);
	}
	
	/**
	 * 改变用户锁定状态
	 * @param userLockReq baseaccount
	 * @return
	 */
	@RequestMapping(value = "/changeBaseAccountUserLockStatus", method = RequestMethod.POST)
	public @ResponseBody int changeBaseAccountUserLockStatus(@RequestBody @Valid UserLockReq userLockReq) {
		return mimosaService.changeBaseAccountUserLockStatus(userLockReq);
	}
	
	/**
	 * 插入末次交易订单
	 * @param mUAllReq
	 * @return
	 */
	@RequestMapping(value = "/insertLastTradeOrder", method = RequestMethod.POST)
	public @ResponseBody int insertLastTradeOrder(@RequestBody @Valid MUAllReq mUAllReq){
		return mimosaService.insertLastTradeOrder(mUAllReq);
	}
	
	/**
	 * 获取红包对账
	 * @param cardbalanceReq
	 * @return
	 */
	@RequestMapping(value = "/countCard_RedpackUsesInfo", method = RequestMethod.POST)
	public @ResponseBody Long countCard_RedpackUsesInfo(@RequestBody @Valid CardbalanceReq cardbalanceReq) {
		return mimosaService.countCard_RedpackUsesInfo(cardbalanceReq);
	}
	/**
	 * 获取红包对账
	 * @param cardbalanceReq
	 * @return
	 */
	@RequestMapping(value = "/getCard_RedpackUsesInfo", method = RequestMethod.POST)
	public @ResponseBody List<Map<String, Object>> getCard_RedpackUsesInfo(@RequestBody @Valid CardbalanceReq cardbalanceReq) {
		return mimosaService.getCard_RedpackUsesInfo(cardbalanceReq);
	}
	
	
	@RequestMapping(value = "/getProductInfos", method = RequestMethod.POST)
	public @ResponseBody  List<Map<String,Object>> getProductInfos(){
		return mimosaService.getProductInfos();
		
	}
	
	@RequestMapping(value = "/getProductInfosByOid", method = RequestMethod.POST)
	public @ResponseBody  List<Map<String,Object>> getProductInfosByOid(@RequestBody @Valid Collection<Object> oids){
		return mimosaService.getProductInfosByOid(oids);
		
	}
	
	@RequestMapping(value = "/queryProductOidByName", method = RequestMethod.POST)
	public @ResponseBody  List<String> queryProductOidByName( @RequestBody @Valid MUAllReq mUAllReq){
		return mimosaService.queryProductOidByName(mUAllReq);
				
	}
	
	@RequestMapping(value = "/queryProductOidsByType", method = RequestMethod.POST)
	public @ResponseBody  List<String> queryProductOidsByType(@RequestBody @Valid MUAllReq mUAllReq){
		return mimosaService.queryProductOidsByType(mUAllReq);
		
	}
	
	@RequestMapping(value = "/getMaxMimosaStatsticsUpdateTime", method = RequestMethod.POST)
	public @ResponseBody Object getMaxMimosaStatsticsUpdateTime() {
		return mimosaService.getMaxMimosaStatsticsUpdateTime();
	}
	
	@RequestMapping(value = "/countNumMimosaOnly", method = RequestMethod.POST)
	public @ResponseBody  Long countNumMimosaOnly(@RequestBody @Valid MUAllReq mUAllReq){
		return mimosaService.countNumMimosaOnly(mUAllReq);
		
	}
	@RequestMapping(value = "/countNumProductOnly", method = RequestMethod.POST)
	public @ResponseBody  Long countNumProductOnly(@RequestBody @Valid MUAllReq mUAllReq){
		return mimosaService.countNumProductOnly(mUAllReq);
		
	}
	@RequestMapping(value = "/countNumMimosaOnlyBiggerThan", method = RequestMethod.POST)
	public @ResponseBody  Long countNumMimosaOnlyBiggerThan(@RequestBody @Valid TimePageReq timePageReq){
		return mimosaService.countNumMimosaOnlyBiggerThan(timePageReq);
		
	}
	@RequestMapping(value = "/countNumJoinMimosaAndTradeOrder", method = RequestMethod.POST)
	public @ResponseBody  Long countNumJoinMimosaAndTradeOrder(@RequestBody @Valid MUAllReq mUAllReq){
		return mimosaService.countNumJoinMimosaAndTradeOrder(mUAllReq);
		
	}
	@RequestMapping(value = "/query2MapsOnlyMimosa", method = RequestMethod.POST)
	public @ResponseBody  List<Map<String, Object>> query2MapsOnlyMimosa(@RequestBody @Valid MUAllReq mUAllReq){
		return mimosaService.query2MapsOnlyMimosa(mUAllReq);
	}
	@RequestMapping(value = "/query2MapsOnlyMimosaBiggerThan", method = RequestMethod.POST)
	public @ResponseBody  List<Map<String, Object>> query2MapsOnlyMimosaBiggerThan(@RequestBody @Valid TimePageReq timePageReq){
		return mimosaService.query2MapsOnlyMimosaBiggerThan(timePageReq);
		
	}
//	@RequestMapping(value = "/query2MapsJoinMimosaAndTradeOrder", method = RequestMethod.POST)
//	public @ResponseBody  List<Map<String, Object>> query2MapsJoinMimosaAndTradeOrder(@RequestBody @Valid MUAllReq mUAllReq){
//		return mimosaService.query2MapsJoinMimosaAndTradeOrder(mUAllReq);
//		
//	}
	@RequestMapping(value = "/query2IdListOnlyMimosa", method = RequestMethod.POST)
	public @ResponseBody  List<String>  query2IdListOnlyMimosa(@RequestBody @Valid MUAllReq mUAllReq){
		return mimosaService.query2IdListOnlyMimosa(mUAllReq);
		
	}
	@RequestMapping(value = "/query2IdListOnlyProduct", method = RequestMethod.POST)
	public @ResponseBody  List<String>  query2IdListOnlyProduct(@RequestBody @Valid MUAllReq mUAllReq){
		return mimosaService.query2IdListOnlyProduct(mUAllReq);
		
	}
	@RequestMapping(value = "/query2IdListJoinMimosaAndTradeOrder", method = RequestMethod.POST)
	public @ResponseBody  List<String>  query2IdListJoinMimosaAndTradeOrder(@RequestBody @Valid MUAllReq mUAllReq){
		return mimosaService.query2IdListJoinMimosaAndTradeOrder(mUAllReq);
		
	}
	
	
	
	
	
	



}
