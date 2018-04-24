package com.guohuai.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.cache.service.RedisExecuteLogExtService;
import com.guohuai.component.web.view.BaseRep;

/**
 * redis缓存Controller
 * @author suzhicheng
 *
 */
// 严禁使用初始化缓存。
//@RestController
//@RequestMapping(value="/mimosa/boot/cache",produces="application/json")
public class CacheController extends BaseController{
	
//	@Autowired
//	private InitCacheService initCacheService;
//	
//	/**
//	 * 初始化全部缓存数据
//	 * @return
//	 */
//	@RequestMapping(value = "initAllCache", method = RequestMethod.POST)
//	@ResponseBody
//	public ResponseEntity<BaseRep> initCache() {
//		BaseRep rep =new BaseRep(); 
//		this.initCacheService.initAllCacheData();
//		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
//	}
//	/**
//	 * 初始化全部产品缓存数据
//	 * @return
//	 */
//	@RequestMapping(value = "initProduct", method = RequestMethod.POST)
//	@ResponseBody
//	public ResponseEntity<BaseRep> initProduct() {
//		BaseRep rep =new BaseRep(); 
//		this.initCacheService.initAllProductCacheData();
//		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
//	}
//	/**
//	 * 根据产品ID重置产品缓存数据
//	 * @return
//	 */
//	@RequestMapping(value = "resetProduct")
//	@ResponseBody
//	public ResponseEntity<BaseRep> resetProduct(@RequestParam String oid) {
//		BaseRep rep =new BaseRep(); 
//		rep=this.initCacheService.resetProductCacheDataByOid(oid);
//		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
//	}
//
//	/**
//	 * 初始化SPV持有手册缓存数据
//	 * @return
//	 */
//	@RequestMapping(value = "initSPVHold", method = RequestMethod.POST)
//	@ResponseBody
//	public ResponseEntity<BaseRep> initSPVHold() {
//		BaseRep rep =new BaseRep(); 
//		this.initCacheService.initAllSVPHoldCacheData();
//		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
//	}
//	/**
//	 * 重置SPV持有手册缓存数据
//	 * @return
//	 */
//	@RequestMapping(value = "resetSPVHold")
//	@ResponseBody
//	public ResponseEntity<BaseRep> resetSPVHold(@RequestParam String productOid) {
//		BaseRep rep =new BaseRep(); 
//		rep = this.initCacheService.resetSVPHoldCacheDataByProductOid(productOid);
//		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
//	}
}
