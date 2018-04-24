package com.guohuai.ams.duration.assetPool;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.guohuai.ams.duration.capital.CapitalForm;
import com.guohuai.ams.duration.capital.CapitalService;
import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.web.view.PageResp;
import com.guohuai.component.web.view.Response;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountService;


/**
 * 存续期--资产池操作入口
 * @author star.zhu
 * 2016年5月16日
 */
@RestController
@RequestMapping(value = "/mimosa/duration/assetPool", produces = "application/json;charset=utf-8")
public class AssetPoolController extends BaseController {
	
	@Autowired
	private AssetPoolService assetPoolService;
	@Autowired
	private CapitalService capitalService;
	@Autowired
	private PublisherBaseAccountService spvService;
	
	/**
	 * 获取spv列表
	 * @return
	 */
	@RequestMapping(value = "/getAllSPV", name = "资产池 - 获取spv列表", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> getAllSPV() {
		List<JSONObject> objList = spvService.getAllSPV();
		Response r = new Response();
		r.with("result", objList);
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}

	/**
	 * 新建资产池
	 * @param form
	 * @return
	 */
	@RequestMapping(value = "/createPool", name = "资产池 - 新建", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> createPool(AssetPoolForm form) {
		// old
		/*assetPoolService.createPool(form, super.getLoginUser());
		assetPoolService.createPool(form, super.getLoginUser());
		r.with("result", "SUCCESSED!");*/
		
		// ----------------20170526,yihonglei modify start-------------------
		Response r = new Response();
		// 判断基础资产编码是否重复
		int i = assetPoolService.queryBaseAssetCode(form.getBaseAssetCode());
		if (i<1) {
			assetPoolService.createPool(form, super.getLoginUser());
			r.with("result", "SUCCESSED!");
		} else {
			r.with("result", "基础资产池编码已存在");
		}
		
		// ----------------20170526,yihonglei modify end-------------------
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}

	/**
	 * 新建审核
	 * @param operation
	 * 			操作：yes（通过）；no（不通过）
	 * @param oid
	 * @return
	 */
	@RequestMapping(value = "/auditPool", name = "资产池 - 新建审核", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> auditPool(@RequestParam String operation, @RequestParam String oid) {
		assetPoolService.auditPool(operation, oid, super.getLoginUser());
		Response r = new Response();
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}

	/**
	 * 获取所有的资产池列表，支持模糊查询
	 * @return
	 */
	@RequestMapping(value = "/getAll", name = "资产池 - 获取所有资产池列表", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<PageResp<AssetPoolForm>> getAll(
			@RequestParam(required = false, defaultValue = "") final String name,
			@RequestParam(required = false, defaultValue = "all") final String state,
			@RequestParam(required = false, defaultValue = "1") int page,
			@RequestParam(required = false, defaultValue = "10") int rows,
			@RequestParam(required = false, defaultValue = "createTime") String sortField,
			@RequestParam(required = false, defaultValue = "desc") String sort) {
		Direction sortDirection = Direction.DESC;
		if (!"desc".equals(sort)) {
			sortDirection = Direction.ASC;
		}
		Specification<AssetPoolEntity> spec = new Specification<AssetPoolEntity>() {
			
			@Override
			public Predicate toPredicate(Root<AssetPoolEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> predicate = new ArrayList<>();
				if (!name.equals("")) {
					predicate.add(cb.like(root.get("name").as(String.class), "%" + name + "%"));
				}
				if (state.equals("all")) {
					predicate.add(cb.notEqual(root.get("state").as(String.class), AssetPoolEntity.status_invalid));
				} else {
					predicate.add(cb.equal(root.get("state").as(String.class), state));
				}
				Predicate[] pre = new Predicate[predicate.size()];
				
				return cb.and(predicate.toArray(pre));
			}
		};
		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(sortDirection, sortField)));
		PageResp<AssetPoolForm> rep = assetPoolService.getAllList(spec, pageable);
		return new ResponseEntity<PageResp<AssetPoolForm>>(rep, HttpStatus.OK);
	}

	/**
	 * 根据oid获取资产池的详细信息
	 * @param oid
	 * @return
	 */
	@RequestMapping(value = "/getPoolByOid", name = "资产池 - 根据oid查询资产池", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> getPoolByOid(@RequestParam String oid) {
		AssetPoolForm form = assetPoolService.getPoolByOid(oid);
		Response r = new Response();
		r.with("result", form);
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}

	/**
	 * 编辑资产池
	 * @param form
	 * @return
	 */
	@RequestMapping(value = "/editPool", name = "资产池 - 编辑", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> editPool(AssetPoolForm form) {
		assetPoolService.editPool(form, super.getLoginUser());
		Response r = new Response();
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}

	/**
	 * 编辑资产池账户信息
	 * @param form
	 * @return
	 */
	@RequestMapping(value = "/editPoolForCash", name = "资产池 - 编辑资产池账户信息", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> editPoolForCash(AssetPoolForm form) {
		assetPoolService.editPoolForCash(form, super.getLoginUser());
		Response r = new Response();
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	
	/**
	 * 逻辑删除资产池
	 * @param pid
	 * @return
	 */
	@RequestMapping(value = "/updateAssetPool", name = "资产池 - 逻辑删除", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> updateAssetPool(String pid) {
		assetPoolService.updateAssetPool(pid);
		Response r = new Response();
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}

	/**
	 * 获取所有资产池的名称列表，包含id
	 * @return
	 */
	@RequestMapping(value = "/getAllNameList", name = "资产池 - 获取所有资产池json列表，包含id和name", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> getAllNameList() {
		List<JSONObject> jsonList = assetPoolService.getAllNameList();
		Response r = new Response();
		r.with("rows", jsonList);
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}

	/**
	 * 计算资产池每日收益
	 * @return
	 */
	@RequestMapping(value = "/calcPoolProfit", name = "资产池 - 计算每日收益", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> calcPoolProfit() {
		assetPoolService.calcPoolProfit();
		Response r = new Response();
		r.with("result", "SUCCESS");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}

	/**
	 * 获取所有资产池的资金明细
	 * @return
	 */
	@RequestMapping(value = "/getAllCapitalList", name = "资产池 - 获取所有资产池的资金明细", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<PageResp<CapitalForm>> getAllCapitalList(String pid,
			@RequestParam(required = false, defaultValue = "1") int page,
			@RequestParam(required = false, defaultValue = "10") int rows,
			@RequestParam(required = false, defaultValue = "createTime") String sortField,
			@RequestParam(required = false, defaultValue = "desc") String sort) {
		Direction sortDirection = Direction.DESC;
		if (!"desc".equals(sort)) {
			sortDirection = Direction.ASC;
		}
		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(sortDirection, sortField)));
		pid = assetPoolService.getPid(pid);
		PageResp<CapitalForm> rep = capitalService.getCapitalListByPid(pid, pageable);
		return new ResponseEntity<PageResp<CapitalForm>>(rep, HttpStatus.OK);
	}
	
	/**
	 * 手动触发每日收益计算
	 * @param type
	 * @return
	 */
	@RequestMapping(value = "/userPoolProfit", name = "资产池 - 手动触发每日收益计算", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> calcCapital(@RequestParam String oid, @RequestParam String type) {
		assetPoolService.userPoolProfit(oid, super.getLoginUser(), type);
		Response r = new Response();
		r.with("result", "SUCCESS");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	
	/**
	 * 更新资产池的偏离损益
	 * @param form
	 */
	@RequestMapping(value = "/updateDeviationValue", name = "资产池 - 更新资产池的偏离损益", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> updateDeviationValue(AssetPoolForm form) {
		assetPoolService.updateDeviationValue(form, super.getLoginUser());
		Response r = new Response();
		r.with("result", "SUCCESS");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	
	/**
	 * 运营管理-统计管理-结算数据导出 <br />
	 * 根据param查询 资产池-产品 统计信息列表
	 * @param page
	 * @param rows
	 * @param assetPoolName
	 * @param productName
	 * @param productStatus
	 * @param raiseTimeBegin
	 * @param raiseTimeEnd
	 * @param repayTimeBegin
	 * @param repayTimeEnd
	 * @return
	 */
	@RequestMapping(value = "/getAssetPoolAndProduct", name = "资产池-产品列表", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<AssetPoolProductVoResp> getAssetPoolAndProduct(
			@RequestParam(required = false, defaultValue = "1") int page,
			@RequestParam(required = false, defaultValue = "10") int rows,
			@RequestParam(required = false, defaultValue = "") String assetPoolName,
			@RequestParam(required = false, defaultValue = "") String productName,
			@RequestParam(required = false, defaultValue = "") String productStatus,
			@RequestParam(required = false, defaultValue = "") String raiseTimeBegin,
			@RequestParam(required = false, defaultValue = "") String raiseTimeEnd,
			@RequestParam(required = false, defaultValue = "") String repayTimeBegin,
			@RequestParam(required = false, defaultValue = "") String repayTimeEnd){
		return assetPoolService.findAssetPoolAndProduct(page, rows, assetPoolName, productName, productStatus, raiseTimeBegin, raiseTimeEnd, repayTimeBegin, repayTimeEnd);
	}
	
	/**
	 * 运营管理-统计管理-结算数据导出 产品订单下载
	 * @param oids
	 */
	@RequestMapping(value = "/assetProductOrderDown", name = "资产池-产品订单明细下载", method = { RequestMethod.GET, RequestMethod.POST })
	public void down(@RequestParam String oids){
		assetPoolService.assetProductOrderDown(response, oids);
		
	}
}
