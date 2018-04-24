package com.guohuai.ams.activityModel;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.transaction.Transactional;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.guohuai.ams.product.ProductDecimalFormat;
import com.guohuai.basic.component.ext.web.BaseResp;
import com.guohuai.cardvo.dao.MimosaDao;
import com.guohuai.component.util.Clock;
import com.guohuai.component.util.GHUtil;
import com.guohuai.component.util.HashRedisUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @Desc: 活动模板
 * @author huyong
 * @date 2017.12.12
 */
@Service
@Slf4j
public class ActivityModelService {
	
	private static final String REDIS_KEY_ACTIVITY_MODEL = "com:mimosa:activity:model:";
	
	@Autowired
	private ActivityModelDao activityModelDao;
	
	@Autowired
	private UsualPlaceDao usualPlaceDao;
	
	@Autowired
	private ProductPlaceDao productPlaceDao;
	
//	@Autowired
//	private CacheProductService cacheProductService;
	
	@Autowired
	private MimosaDao mimosaDao;
	
	@Autowired
	private RedisTemplate<String, String> redis;
	
	/**
	 * @Desc: 活动模板保存
	 * @author huyong
	 * @date 2017.12.12
	 */
	@Transactional
	public BaseResp saveActivityModel(AddActivityModelReq req, String operator) {
		log.info("活动模板保存，模板参数：{}，操作人：{}", JSONObject.toJSONString(req),operator);
		BaseResp resp = new BaseResp();
		try {
			Timestamp now = new Timestamp(Clock.DEFAULT.getCurrentTimeInMillis());
			ActivityModelEntity activityModelEntity = activityModelDao.findByPlatTypeAndCode(req.getPlatType(), req.getCode());
			if(!ObjectUtils.isEmpty(activityModelEntity)) {
				log.info("活动模板删除，历史模板不进行记录。。。。begin");
				String modelOid = activityModelEntity.getOid();
				activityModelDao.delete(modelOid);
				usualPlaceDao.deleteByModelOid(modelOid);
				productPlaceDao.deleteByModelOid(modelOid);
				String key = REDIS_KEY_ACTIVITY_MODEL + req.getPlatType() + ":" + req.getCode();
				redis.delete(key);
				log.info("活动模板删除，历史模板不进行记录。。。。end，删除key：{}", key);
			}
			activityModelEntity = new ActivityModelEntity();
			activityModelEntity.setTitle(req.getTitle());
			activityModelEntity.setBannerUrl(req.getBannerUrl());
			activityModelEntity.setBackgroundUrl(req.getBackgroundUrl());
			activityModelEntity.setCode(req.getCode());
			activityModelEntity.setPlatType(req.getPlatType());
			activityModelEntity.setOperator(operator);
			activityModelEntity.setCreateOperator(operator);
			activityModelEntity.setCreateTime(now);
			activityModelEntity = activityModelDao.save(activityModelEntity);
			String modelOid = activityModelEntity.getOid();
			List<UsualPlaceEntity> usualPlaces = new ArrayList<>();
			req.getUsualPlaces().parallelStream().forEach(usualPlace->{
				UsualPlaceEntity usualPlaceEntity = new UsualPlaceEntity(); 
				usualPlaceEntity.setModelOid(modelOid);
				usualPlaceEntity.setUsualPic(usualPlace.getUsualPic());
				usualPlaceEntity.setPrimaryUrl(usualPlace.getPrimaryUrl());
				usualPlaceEntity.setOrderNum(usualPlace.getOrderNum());
				usualPlaceEntity.setCreateTime(now);
				usualPlaces.add(usualPlaceEntity);
			});
			usualPlaceDao.save(usualPlaces);
			List<ProductPlaceEntity> productPlaces = new ArrayList<>();
			for (ProductPlace productPlace : req.getProductPlaces()) {
				int hasBaofengbao = productPlace.getHasBaofengbao();
				int hasFreshMan = productPlace.getHasFreshMan();
				int hasZeroBuy = productPlace.getHasZeroBuy();
				if(hasBaofengbao == 0 && hasFreshMan == 0 && hasZeroBuy == 0 ) {
					resp.setErrorCode(-1);
					resp.setErrorMessage("产品类型不能为空");
					return resp;
				}
				ProductPlaceEntity productPlaceEntity = new ProductPlaceEntity();
				productPlaceEntity.setModelOid(modelOid);
				productPlaceEntity.setProductPic(productPlace.getProductPic());
				productPlaceEntity.setChannelId(productPlace.getChannelId());
				productPlaceEntity.setChannelName(productPlace.getChannelName());
				productPlaceEntity.setFirstProductOid(productPlace.getFirstProductOid());
				productPlaceEntity.setFirstProductName(productPlace.getFirstProductName());
				productPlaceEntity.setHasBaofengbao(hasBaofengbao);
				productPlaceEntity.setHasFreshMan(hasFreshMan);
				productPlaceEntity.setHasZeroBuy(hasZeroBuy);
				productPlaceEntity.setOrderBy(productPlace.getOrderBy());
				productPlaceEntity.setMaxNum(productPlace.getMaxNum());
				productPlaceEntity.setOrderNum(productPlace.getOrderNum());
				productPlaceEntity.setCreateTime(now);
				productPlaces.add(productPlaceEntity);
			}
			productPlaceDao.save(productPlaces);
		}catch (Exception e) {
			e.printStackTrace();
			log.error("活动模板保存失败，模板参数：{}，操作人：{}", JSONObject.toJSONString(req),operator);
			resp.setErrorCode(-1);
			resp.setErrorMessage("活动模板保存失败");
			return resp;
		}
		resp.setErrorCode(0);
		resp.setErrorMessage("保存成功");
		return resp;
	}
	
	/**
	 * @Desc: 活动模板查询
	 * @author huyong
	 * @date 2017.12.12
	 */
	public QueryActivityModelRep queryActivityModel(ActivityModelReq req) {
		log.info("活动模板查询，参数：{}", JSONObject.toJSONString(req));
		QueryActivityModelRep resp = new QueryActivityModelRep();
		String platType = req.getPlatType();
		String code = req.getCode();
		// 判断查询参数是否为空
		if(StringUtils.isBlank(platType) || StringUtils.isBlank(code)) {
			log.info("活动模板查询参数为空，不查询直接返回");
			return resp;
		}
		// 判断查询参数是否为支持的平台类型
		if(!platType.equalsIgnoreCase(ActivityModelEntity.PLAT_TYPE_APP) && !platType.equalsIgnoreCase(ActivityModelEntity.PLAT_TYPE_H5)) {
			log.info("活动模板查询平台类型不支持，不查询直接返回");
			return resp;
		}
		// 判断查询参数是否为支持的模板类型
		if(!code.equalsIgnoreCase(ActivityModelEntity.code_Activity_A) && !code.equalsIgnoreCase(ActivityModelEntity.code_Activity_B)) {
			log.info("活动模板查询模板类型为空，不查询直接返回");
			return resp;
		}
		// 根据查询参数生成key
		String key = REDIS_KEY_ACTIVITY_MODEL + req.getPlatType() + ":" + req.getCode();
		// 通过key获取数据
		Map<String, String> activityModelCache = HashRedisUtil.hgetall(redis, key);
		// 如果缓存存在则直接返回数据结果
		// 如果缓存不存在则查询数据库并生成缓存
		if(null != activityModelCache && !activityModelCache.isEmpty()) {
			resp.setErrorMessage("yes");
			JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(activityModelCache));
			resp.setModelOid(ofNullable(jsonObject.get("modelOid")));
			resp.setTitle(ofNullable(jsonObject.get("title")));
			resp.setBannerUrl(ofNullable(jsonObject.get("bannerUrl")));
			resp.setBackgroundUrl(ofNullable(jsonObject.get("backgroundUrl")));
			resp.setPlatType(ofNullable(jsonObject.get("platType")));
			resp.setCode(ofNullable(jsonObject.get("code")));
			List<JSONObject> jsonList = JSONArray.parseArray(ofNullable(jsonObject.get("places")), JSONObject.class);
			resp.setPlaces(jsonList);
			return resp;
		}
		// 通过参数查询模板
		ActivityModelEntity activityModel = activityModelDao.findByPlatTypeAndCode(req.getPlatType(), req.getCode());
		if(!ObjectUtils.isEmpty(activityModel)) {
			resp.setErrorMessage("yes");
			try {
				// 查询定制产品、普通占位
				List<UsualPlaceEntity> useualPlaceList = usualPlaceDao.findListByModelOid(activityModel.getOid());
				List<ProductPlaceEntity> productPlaceList = productPlaceDao.findListByModelOid(activityModel.getOid());
				// 添加占位类型并转换为流形式
				Stream<JSONObject> useualPlaces = useualPlaceList.parallelStream().map(useualPlace->{
					useualPlace.setType(ActivityModelEntity.USUAL_TYPE);
					return JSONObject.parseObject(JSONObject.toJSONString(useualPlace));
				});
				Stream<JSONObject> productPlaces = productPlaceList.parallelStream().map(productPlace->{
					productPlace.setType(ActivityModelEntity.PRODUCT_TYPE);
					return JSONObject.parseObject(JSONObject.toJSONString(productPlace));
				});
				// 设置排序规则
				Comparator<JSONObject> sort=(useual,product)->{
					return Integer.valueOf(useual.get("orderNum").toString()).compareTo(Integer.valueOf(product.get("orderNum").toString()));
				};
				// 占位排序同时转换为list
				List<JSONObject>  places = Stream.concat(useualPlaces,productPlaces).parallel().sorted(sort).collect(Collectors.toList());
				resp.setModelOid(activityModel.getOid());
				resp.setTitle(activityModel.getTitle());
				resp.setBannerUrl(activityModel.getBannerUrl());
				resp.setBackgroundUrl(activityModel.getBackgroundUrl());
				resp.setCode(activityModel.getCode());
				resp.setPlatType(activityModel.getPlatType());
		        resp.setPlaces(places);
			}catch(Exception e) {
				log.error("活动模板生成缓存失败，generate cache error：{}",JSONObject.toJSONString(resp));
				return resp;
			}
	        // 生成活动模板缓存
	        try {
	        		HashRedisUtil.hmset(redis, key, GHUtil.obj2Map(resp));
	        }catch(Exception e) {
	        		log.error("activityModel into cache failed：{}",JSONObject.toJSONString(resp));
	        		return resp;
	        }
		}
		return resp;
	}
	
	/**
	 * @Desc: 定制产品下产品查询
	 * @author huyong
	 * @date 2017.12.12
	 */
	public ActivityModelRep<Object> placeProducts(ActivityModelReq req) {
		log.info("定制产品下产品查询，参数：{}", JSONObject.toJSONString(req));
		List<ProductPlaceEntity> productPlaceList = productPlaceDao.findListByModelOid(req.getModelOid());
		ActivityModelRep<Object> resp = new ActivityModelRep<Object>();
		List<Object> list = new ArrayList<Object>();
		productPlaceList.forEach(productPlace->{
			int i = 1;  //排序序号
			List<Map<String, Object>> product = mimosaDao.findProductByOid(productPlace.getFirstProductOid(),productPlace.getChannelId());
			if(null != product && product.size() > 0) {
				Map<String, Object> productMap = product.get(0);
				BigDecimal expAror = new BigDecimal(Optional.ofNullable(productMap.get("expAror")).orElse("0").toString());//预期年化收益率起始
				BigDecimal expArorSec = new BigDecimal(Optional.ofNullable(productMap.get("expArorSec")).orElse("0").toString());//预期年化收益率截止
				BigDecimal expectedArrorDisp = new BigDecimal(Optional.ofNullable(productMap.get("expectedArrorDisp")).orElse("0").toString());//折算年化收益率
				String expArorStr = ProductDecimalFormat.format(ProductDecimalFormat.multiply(expAror))+"%";
				String expArorSecStr = ProductDecimalFormat.format(ProductDecimalFormat.multiply(expArorSec))+"%";
				String expectedArrorDisStr = ProductDecimalFormat.format(ProductDecimalFormat.multiply(expectedArrorDisp))+"%";
				productMap.remove("expAror");
				productMap.remove("expArorSec");
				if(expArorStr.equals(expArorSecStr)) {
					productMap.put("annualInterestSec", expArorStr);
				} else {
					productMap.put("annualInterestSec",expArorStr+"-"+expArorSecStr);
				}
				productMap.put("expectedArrorDisp", expectedArrorDisStr);
				productMap.put("placeOid", productPlace.getOid());
				productMap.put("orderNum", i);
				list.add(productMap);
			}
			List<Map<String, Object>> productList = mimosaDao.placeProducts(productPlace);
			for(int j = 0;j < productList.size();j++) {
				Map<String, Object> map = productList.get(j);
				BigDecimal expAror = new BigDecimal(Optional.ofNullable(map.get("expAror")).orElse("0").toString());//预期年化收益率起始
				BigDecimal expArorSec = new BigDecimal(Optional.ofNullable(map.get("expArorSec")).orElse("0").toString());//预期年化收益率截止
				BigDecimal expectedArrorDisp = new BigDecimal(Optional.ofNullable(map.get("expectedArrorDisp")).orElse("0").toString());//折算年化收益率
				String expArorStr = ProductDecimalFormat.format(ProductDecimalFormat.multiply(expAror))+"%";
				String expArorSecStr = ProductDecimalFormat.format(ProductDecimalFormat.multiply(expArorSec))+"%";
				String expectedArrorDisStr = ProductDecimalFormat.format(ProductDecimalFormat.multiply(expectedArrorDisp))+"%";
				map.remove("expAror");
				map.remove("expArorSec");
				if(expArorStr.equals(expArorSecStr)) {
					map.put("annualInterestSec", expArorStr);
				} else {
					map.put("annualInterestSec",expArorStr+"-"+expArorSecStr);
				}
				map.put("expectedArrorDisp", expectedArrorDisStr);
				map.put("placeOid", productPlace.getOid());
				map.put("orderNum", ++i);
			}
			list.addAll(productList);
		});
		resp.setTotal(list.size());
		resp.setRows(list);
		return resp;
	}
	
	/**
	 * @Desc: null字段转""
	 * @author huyong
	 * @date 2017.12.12
	 */
	public String ofNullable(Object obj) {
		return Optional.ofNullable(obj).orElse("").toString();
	}
}
