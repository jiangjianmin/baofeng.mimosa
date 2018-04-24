package com.guohuai.cardvo.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.guohuai.ams.label.LabelEntity;
import com.guohuai.basic.cardvo.ActivityTriggerEnum;
import com.guohuai.basic.cardvo.rep.CouponRep;
import com.guohuai.basic.cardvo.req.cardreq.ValidCardReq;
import com.guohuai.basic.cardvo.req.userInfoReq.CardbalanceReq;
import com.guohuai.basic.cardvo.req.userInfoReq.MUAllReq;
import com.guohuai.basic.cardvo.req.userInfoReq.TimePageReq;
import com.guohuai.basic.cardvo.req.userInfoReq.UserInvedtorOidsReq;
import com.guohuai.basic.cardvo.req.userInfoReq.UserLockReq;
import com.guohuai.cardvo.dao.MimosaDao;
import com.guohuai.cardvo.dao.MimosaRepository;
import com.guohuai.cardvo.req.ProductRep;
import com.guohuai.cardvo.req.ProductReq;
import com.guohuai.cardvo.util.CardVoUtil;
import com.guohuai.component.web.view.PageResp;
import com.guohuai.tuip.api.TulipSdk;

@Service
@Transactional
public class MimosaService {
	@Autowired
	private MimosaDao mimosaDao;
	@Autowired
	private MimosaRepository mimosaRepository;
	@Autowired
	TulipSdk tulipSdk;
//	1.已保存 2.已删除 3.已确认 4.已发放已生效 5.已发放已无效 6 已发放已删除
//	private static final int defaultCardStaus=4;
	
	
	/**
	 * 获取用户触发条件
	 * @param productOid
	 * @param isAuto
	 * @param orderType
	 * @param orderStatus
	 * @return
	 */
	public String getTriggerCode(String productOid,String isAuto,String orderType,String orderStatus){
		if (StringUtils.isBlank(productOid)||
				StringUtils.isBlank(isAuto)||
				StringUtils.isBlank(orderType)||
				StringUtils.isBlank(orderStatus)
				) {
			return "";
		}
		if (!Objects.equals("1", isAuto)&&!Objects.equals("0", isAuto)) {
			return "";
		}
		if (!Objects.equals("invest", orderType)) {
			return "";
		}
		if (Stream.of("paySuccess","accepted","confirmed").noneMatch(st->Objects.equals(st, orderStatus))) {
			return "";
		}
		
		
		Map<String,Object> product= mimosaDao.getProductInfoOnTrigger(productOid);
		if (Objects.equals("BFJRKHB001", product.get("code"))) {//购买快活宝
			return  isAuto.equals("1")?ActivityTriggerEnum.TKHB.getCode()://银行卡购买快活宝
				ActivityTriggerEnum.TYGKH.getCode();//月光宝盒购买快活宝
					 
		}
		if (product.get("name").toString().indexOf("天天向上")!=-1&&Objects.equals("1", isAuto)) {
			return  ActivityTriggerEnum.TTXS.getCode();
			
		}
		if (null!=product.get("guessOid")&&Objects.equals("1", isAuto)) {
			return  ActivityTriggerEnum.TJCB.getCode();
					
		}
		//要排除0元购 竞猜宝 新手标| 是定期的
		if (null==product.get("guessOid")&&Objects.equals("1", isAuto)&&!product.get("labelCode").equals("freshman")
				&&product.get("type").equals("PRODUCTTYPE_01")
				&&!Objects.equals(Objects.toString(product.get("isActivityProduct")), "1")
				) {
			return  ActivityTriggerEnum.TBFB.getCode();
			
		}
	
		
		return "";
	}
	
	/**
	 * 获取数量publisherHoldSql
	 * @param mUAllReq
	 * @return
	 */
	public Long counPublisherHold(MUAllReq mUAllReq) {
		return mimosaDao.counPublisherHold(mUAllReq);
	}

	/**
	 * publisherHoldSql获取map
	 * @param mUAllReq
	 * @return
	 */
	public List<Map<String, Object>> query2MapsPublisherHold(MUAllReq mUAllReq) {
		return mimosaDao.query2MapsPublisherHold(mUAllReq);

	}

	/**
	 * publisherHoldSql获取ids
	 * @param mUAllReq
	 * @return
	 */
	public List<String> query2IdListPublisherHold(MUAllReq mUAllReq) {
		return mimosaDao.query2IdListPublisherHold(mUAllReq);
	}

	/**
	 * 获取用户使用红包的订单id
	 * @param userInvedtorOidsReq
	 * @return
	 */
	public List<Map<String,Object>> getInvestOidsByUserOids(UserInvedtorOidsReq userInvedtorOidsReq){
		return mimosaDao.getInvestOidsByUserOids(userInvedtorOidsReq);
	}
	
	
	
	/**
	 * 取消回滚
	 * @param validCardReq
	 * @return
	 */
	public CouponRep unCheckMyCoupon(ValidCardReq validCardReq){
		return tulipSdk.unCheckMyCoupon(validCardReq);
	}
	
	/**
	 * 使用卡券
	 * @param validCardReq
	 * @return
	 */
	public CouponRep useMyCoupon(ValidCardReq validCardReq){
		return tulipSdk.useMyCoupon(validCardReq);
	}
	
	/**
	 * 验券
	 * @param validCardReq
	 * @return
	 */
	public CouponRep validMyCoupon(ValidCardReq validCardReq){
		return tulipSdk.validMyCoupon(validCardReq);
	}
	
	
	
	/**
	 * 竞猜宝和新手标
	 * @return
	 */
	public List<String> getInvalidProductIds(){
		return mimosaDao.getInvalidProductIds();
	}
	/**排除
	 * 判断是否定期产品 新手标和竞猜宝
	 * @param productOid
	 * @return
	 */
	public Object isDepositProduct( ValidCardReq validCardReq){
		
		return mimosaDao.isDepositProduct(validCardReq.getProductOid());
	}
	/**
	 * 修改用户锁定解锁 Stastics
	 * @param userLockReq
	 * @return
	 */
	public int changeStasticsUserLockStatus(UserLockReq userLockReq){
		return mimosaRepository.changeStasticsUserLockStatus(userLockReq.getLockStatus(), userLockReq.getUserOid());
		
	}
	
	/**
	 * 判断用户是否绑卡过
	 * @param userOid
	 * @return
	 */
	public Object findIdNumByUserOid(String userOid){
		return mimosaRepository.findIdNumByUserOid(userOid);
		
	}
	
	public Map<String,String> getUserInfoByUserId(String userOid){
		return mimosaDao.getUserInfoByUserId(userOid);
		
	}
	
	
	/**
	 * 修改用户锁定解锁 Tradeorder
	 * @param userLockReq
	 * @return
	 */
	public int changeTradeorderUserLockStatus(UserLockReq userLockReq){
		return	mimosaRepository.changeTradeorderUserLockStatus(userLockReq.getLockStatus(), userLockReq.getUserOid());
		
	}
	/**
	 * 修改用户锁定解锁 baseaccount
	 * @param userLockReq
	 * @return
	 */
	public int changeBaseAccountUserLockStatus(UserLockReq userLockReq){
		return	mimosaRepository.changeBaseAccountUserLockStatus(userLockReq.getLockStatus(), userLockReq.getUserOid());
		
	}
	
	/**
	 * 插入末次交易订单
	 * @param mUAllReq
	 * @return
	 */
	public int insertLastTradeOrder(MUAllReq mUAllReq){
		return mimosaRepository.insertLastTradeOrder(mUAllReq.getOrderTypeList(),mUAllReq.getOrderStatusList());
	}
	
	/**
	 *  红包对账数量
	 * @param cardbalanceReq
	 * @return
	 */
	public Long countCard_RedpackUsesInfo(CardbalanceReq cardbalanceReq) {
		return mimosaDao.countCard_RedpackUsesInfo(cardbalanceReq);
	}
	/**
	 *  红包对账信息
	 * @param cardbalanceReq
	 * @return
	 */
	public List<Map<String, Object>> getCard_RedpackUsesInfo(CardbalanceReq cardbalanceReq) {
		return mimosaDao.getCard_RedpackUsesInfo(cardbalanceReq);
	}
	/**
	 * 获取全部产品信息，用于缓存
	 * @return
	 */
	public List<Map<String,Object>> getProductInfos(){
		return mimosaDao.getProductInfos();
	}
	/**
	 * 获取全部产品信息，用于缓存
	 * @return
	 */
	public List<Map<String, Object>> getProductInfosByOid(Collection<Object> oids) {
		return mimosaDao.getProductInfosByOid(oids);
	}
	/**
	 * 根据产品名称获取产品id
	 * @param mUAllReq
	 * @return
	 */
	public List<String> queryProductOidByName(MUAllReq mUAllReq){
		return mimosaDao.queryProductOidByName(mUAllReq);
	}
	/**
	 * 根据产品类型获取产品id
	 * @param mUAllReq
	 * @return
	 */
	public List<String> queryProductOidsByType(MUAllReq mUAllReq){
		return mimosaDao.queryProductOidsByType(mUAllReq);
	}
	/**
	 *  获取mimosaStastics的最大更新时间，作为增量缓存数据依据
	 * @return
	 */
	public Object getMaxMimosaStatsticsUpdateTime() {
		return mimosaDao.getMaxMimosaStatsticsUpdateTime();
	}
	/**
	 * 查询mimosaStastics的全部数量
	 * @param mUAllReq
	 * @return
	 */
	public Long countNumMimosaOnly(MUAllReq mUAllReq) {
		return mimosaDao.countNumMimosaOnly(mUAllReq);
	}
	/**
	 * 获取只有tradeOrder条件的用户id数量
	 * @param mUAllReq
	 * @return
	 */
	public Long countNumProductOnly(MUAllReq mUAllReq) {
		return mimosaDao.countNumProductOnly(mUAllReq);
	}
	/**
	 * 查询大于某个日期的mimosaStastics数量
	 * @param timePageReq
	 * @return
	 */
	public Long countNumMimosaOnlyBiggerThan(TimePageReq timePageReq) {
		return mimosaDao.countNumMimosaOnlyBiggerThan(timePageReq);
	}
	/**
	 * 获取查询条件下，同时tradeOrder和mimosa信息的全部数量
	 * @param mUAllReq
	 * @return
	 */
	public Long countNumJoinMimosaAndTradeOrder(MUAllReq mUAllReq) {
		return mimosaDao.countNumJoinMimosaAndTradeOrder(mUAllReq);
	}
	/**
	 * 获取mimosaStastics的全部相关结果集
	 * @param mUAllReq
	 * @return
	 */
	public List<Map<String, Object>> query2MapsOnlyMimosa(MUAllReq mUAllReq) {
		return mimosaDao.query2MapsOnlyMimosa(mUAllReq);
	}
	/**
	 * 获取大于某个时间的mimosaStastics的结果集
	 * @param timePageReq
	 * @return
	 */
	public List<Map<String, Object>> query2MapsOnlyMimosaBiggerThan(TimePageReq timePageReq) {
		return mimosaDao.query2MapsOnlyMimosaBiggerThan(timePageReq);
	}
//	public List<Map<String, Object>> query2MapsJoinMimosaAndTradeOrder(MUAllReq mUAllReq) {
//		return mimosaDao.query2MapsJoinMimosaAndTradeOrder(mUAllReq);
//	}
	/**
	 *  获取mimosaStastics的查询条件下的用户ids
	 * @param mUAllReq
	 * @return
	 */
	public List<String> query2IdListOnlyMimosa(MUAllReq mUAllReq) {
		return mimosaDao.query2IdListOnlyMimosa(mUAllReq);
	}
	/**
	 * 查询只有tradeOrder信息的用户ids
	 * @param mUAllReq
	 * @return
	 */
	public List<String> query2IdListOnlyProduct(MUAllReq mUAllReq) {
		return mimosaDao.query2IdListOnlyProduct(mUAllReq);
	}
	/**
	 * 获取mimosa组合的查询条件下的用户ids
	 * @param mUAllReq
	 * @return
	 */
	public List<String> query2IdListJoinMimosaAndTradeOrder(MUAllReq mUAllReq) {
		return mimosaDao.query2IdListJoinMimosaAndTradeOrder(mUAllReq);
	}
	
	/**
	 * @desc   分页查询产品列表和相关信息
	 * @author huyong 
	 * @data   2017.5.11
	 */
	public PageResp<ProductRep> getProductList(ProductReq req) {
		PageResp<ProductRep> pageResp = new PageResp<ProductRep>();
		//根据条件查询符合条件的产品数量
		long total = mimosaDao.getProductCount(req);
		//如果未查询到符合条件的产品直接返回
		if(total <= 0){
			return pageResp;
		}
		//查询符合条件的产品
		List<Map<String, Object>> list = mimosaDao.getProductByType(req);
		if (list != null && list.size() > 0) {
			List<ProductRep> rows = new ArrayList<ProductRep>();
			List<String> productOidList = new ArrayList<String>();
			for(Map<String, Object> map : list) {
				productOidList.add(CardVoUtil.nullToStr(map.get("oid")));
			}
			//查询产品标签
			List<Map<String, Object>> productLabels = mimosaDao.getProducLabletByProductOid(productOidList);
			productOidList.clear();
			for (Map<String, Object> map : list) {
				ProductRep rep = new ProductRep();
				rep.setOid(CardVoUtil.nullToStr(map.get("oid")));
				rep.setName(CardVoUtil.nullToStr(map.get("fullName")));
				rep.setExpAror(CardVoUtil.nullToStr(map.get("expAror")));
				rep.setCreateTime(CardVoUtil.nullToStr(map.get("createTime")));
				rep.setRaisedTotalNumber(CardVoUtil.nullToStr(map.get("raisedTotalNumber")));
				if(productLabels!=null && productLabels.size()>0) {
					String expandLabelName = "";
					for(Map<String, Object>  pl : productLabels) {
						String productOid=CardVoUtil.nullToStr(pl.get("productOid"));
						String labelType=CardVoUtil.nullToStr(pl.get("labelType"));
						if(rep.getOid().equals(productOid)){
							if(LabelEntity.labelType_extend.equals(labelType)) {
								expandLabelName +=CardVoUtil.nullToStr(pl.get("labelName"))+"，";
							} else if(LabelEntity.labelType_general.equals(labelType)) {
								rep.setBasicProductLabelName(CardVoUtil.nullToStr(pl.get("labelName")));
							}
						}
					}
					if(expandLabelName.length()>0) {
						expandLabelName = expandLabelName.substring(0, expandLabelName.length()-1);
						rep.setExpandProductLabelName(expandLabelName);
					}
				}
				rows.add(rep);
				rep = null;
			}
			productLabels.clear();
			list.clear();
			pageResp.setRows(rows);
			pageResp.setTotal(total);
		}
		return pageResp;
	}
	
	/**
	 * @desc   根据产品类型和oid查询产品
	 * @author huyong 
	 * @data   2017.5.11
	 */
	public Long getProductCountByOid(ProductReq req) {
		return mimosaDao.getProductCount(req);
	}
	
	/**
	 * @desc   根据产品oid查询产品编码
	 * @author huangzijian
	 * @data   2017.5.11
	 */
	public String getProductCodeByOid(String productOid) {
		return mimosaDao.getProductCodeByOid(productOid);
	}
}
