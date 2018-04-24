package com.guohuai.ams.duration.target;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.guohuai.ams.duration.assetPool.AssetPoolForm;
import com.guohuai.ams.duration.assetPool.AssetPoolService;
import com.guohuai.ams.duration.assetPool.scope.ScopeService;
import com.guohuai.ams.duration.order.FundForm;
import com.guohuai.ams.duration.order.OrderService;
import com.guohuai.ams.duration.order.TransForm;
import com.guohuai.ams.duration.order.TrustForm;
import com.guohuai.ams.duration.order.fund.FundService;
import com.guohuai.ams.duration.order.trust.TrustService;
import com.guohuai.component.web.view.PageResp;
import com.guohuai.component.web.view.Response;

/**
 * 存续期--产品综合视图入口
 * @author star.zhu
 * 2016年5月17日
 */
@RestController
@RequestMapping(value = "/mimosa/duration/target", produces = "application/json;charset=utf-8")
public class TargetController {
	
	@Autowired
	private TargetService targetService;
	@Autowired
	private AssetPoolService assetPoolService;
	@Autowired
	private OrderService orderService;
	@Autowired
	private ScopeService scopeService;
	@Autowired
	private FundService fundService;
	@Autowired
	private TrustService trustService;

	/**
	 * 资产池概要数据
	 * @param pid
	 * 			资产池id
	 * @return
	 */
	@RequestMapping(value = "/getAssetPool", name = "资产池 - 表单详情", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> getAssetPool(@RequestParam String pid) {
		AssetPoolForm form = assetPoolService.getPoolByOid(pid);
		Response r = new Response();
		r.with("result", form);
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}

	/**
	 * 可申购的标的列表
	 * @param pid
	 * 			资产池id
	 * @return
	 */
	@RequestMapping(value = "/getTargetList", name = "资产池 - 可申购的标的列表", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> getTargetList(@RequestParam String pid) {
		String[] scopes = scopeService.getScopes(pid);
		List<FundForm> fundList = targetService.getFundListByScopes(null);
		List<TrustForm> trustList = targetService.getTrustListByScopes(scopes);
		List<TransForm> transList = targetService.getTransListByScopes(scopes);
		Response r = new Response();
		r.with("fund", fundList);
		r.with("trust", trustList);
		r.with("trans", transList);
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}

	/**
	 * 预约中的 现金类管理工具 列表
	 * @param pid
	 * 			资产池id
	 * @return
	 */
	@RequestMapping(value = "/getFundListForAppointment", name = "资产池 - 预约中现金管理类工具列表", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<PageResp<FundForm>> getFundListForAppointment(@RequestParam String pid,
			@RequestParam(required = false, defaultValue = "1") int page,
			@RequestParam(required = false, defaultValue = "10") int rows,
			@RequestParam(required = false, defaultValue = "createTime") String sortField,
			@RequestParam(required = false, defaultValue = "desc") String sort) {
		Direction sortDirection = Direction.DESC;
		if (!"desc".equals(sort)) {
			sortDirection = Direction.ASC;
		}
		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(sortDirection, sortField)));
		
		PageResp<FundForm> rep = orderService.getFundListForAppointmentByPid(pid, pageable);
		return new ResponseEntity<PageResp<FundForm>>(rep, HttpStatus.OK);
	}

	/**
	 * 成立中的 现金类管理工具 列表
	 * @param pid
	 * 			资产池id
	 * @return
	 */
	@RequestMapping(value = "/getFundList", name = "资产池 - 成立中现金管理类工具列表", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<PageResp<FundForm>> getFundList(@RequestParam String pid,
			@RequestParam(required = false, defaultValue = "1") int page,
			@RequestParam(required = false, defaultValue = "10") int rows,
			@RequestParam(required = false, defaultValue = "createTime") String sortField,
			@RequestParam(required = false, defaultValue = "desc") String sort) {
		Direction sortDirection = Direction.DESC;
		if (!"desc".equals(sort)) {
			sortDirection = Direction.ASC;
		}
		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(sortDirection, sortField)));
		
		PageResp<FundForm> rep = orderService.getFundListByPid(pid, pageable);
		return new ResponseEntity<PageResp<FundForm>>(rep, HttpStatus.OK);
	}

	/**
	 * 预约中的 信托（计划） 列表
	 * @param pid
	 * 			资产池id
	 * @return
	 */
	@RequestMapping(value = "/getTrustListForAppointment", name = "资产池 - 预约中信托计划列表", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> getTrustListForAppointment(@RequestParam String pid,
			@RequestParam(required = false, defaultValue = "1") int page,
			@RequestParam(required = false, defaultValue = "10") int rows) {
		List<TrustForm> list = orderService.getTrustListForAppointmentByPid(pid);
		Response r = new Response();
		int sNo = (page -1) * rows;
		int eNo = page * rows;
		if (eNo > list.size()) {
			eNo = list.size();
		}
		
		if (list.size() == 0)
			r.with("rows", list);
		else
			r.with("rows", list.subList(sNo, eNo));
		r.with("total", list.size());
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}

	/**
	 * 成立中的 信托（计划） 列表
	 * @param pid
	 * 			资产池id
	 * @return
	 */
	@RequestMapping(value = "/getTrustList", name = "资产池 - 成立中信托计划列表", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<PageResp<TrustForm>> getTrustList(@RequestParam String pid,
			@RequestParam(required = false, defaultValue = "1") int page,
			@RequestParam(required = false, defaultValue = "10") int rows,
			@RequestParam(required = false, defaultValue = "createTime") String sortField,
			@RequestParam(required = false, defaultValue = "desc") String sort) {
		Direction sortDirection = Direction.DESC;
		if (!"desc".equals(sort)) {
			sortDirection = Direction.ASC;
		}
		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(sortDirection, sortField)));
		
		PageResp<TrustForm> rep = orderService.getTrustListByPid(pid, pageable);
		return new ResponseEntity<PageResp<TrustForm>>(rep, HttpStatus.OK);
	}
	
	/**
	 * 根据 oid 获取 货币基金（现金类管理工具） 详情
	 * @param oid
	 * @return
	 */
	@RequestMapping(value = "/getFundByOid", name = "资产池 - 根据 oid 获取 货币基金（现金类管理工具） 详情", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> getFundByOid(@RequestParam String oid) {
		FundForm form = targetService.getFundByOid(oid);
		Response r = new Response();
		r.with("result", form);
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	
	/**
	 * 根据 oid 获取 信托（计划） 详情
	 * @param oid
	 * @return
	 */
	@RequestMapping(value = "/getTrustByOid", name = "资产池 - 根据 oid 获取 信托（计划） 详情", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> getTrustByOid(@RequestParam String oid) {
		TrustForm form = targetService.getTrustByOid(oid);
		Response r = new Response();
		r.with("result", form);
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	
	/**
	 * 根据 oid 获取 现金管理工具 的申购列表
	 * @param oid
	 * @return
	 */
	@RequestMapping(value = "/getDataByCashtoolOid", name = "资产池 - 根据 oid 获取 现金管理工具 的申购列表", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<PageResp<JSONObject>> getDataByCashtoolOid(@RequestParam String oid,
			@RequestParam(required = false, defaultValue = "1") int page,
			@RequestParam(required = false, defaultValue = "10") int rows) {
		Pageable pageable = new PageRequest(page - 1, rows);
		PageResp<JSONObject> rep = fundService.getDataByCashtoolOid(oid, pageable);
		return new ResponseEntity<PageResp<JSONObject>>(rep, HttpStatus.OK);
	}
	
	/**
	 * 根据 oid 获取 信托（计划） 的申购列表
	 * @param oid
	 * @return
	 */
	@RequestMapping(value = "/getDataByTargetOid", name = "资产池 - 根据 oid 获取 信托（计划） 的申购列表", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<PageResp<JSONObject>> getDataByTargetOid(@RequestParam String oid,
			@RequestParam(required = false, defaultValue = "1") int page,
			@RequestParam(required = false, defaultValue = "10") int rows) {
		Pageable pageable = new PageRequest(page - 1, rows);
		PageResp<JSONObject> rep = trustService.getDataByTargetOid(oid, pageable);
		return new ResponseEntity<PageResp<JSONObject>>(rep, HttpStatus.OK);
	}

	/**
	 * 获取 投资标的还款计划 列表
	 * @param pid
	 * 			资产池id
	 * @return
	 */
	@RequestMapping(value = "/getRepaymentScheduleList", name = "资产池 - 获取 投资标的还款计划 列表", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<PageResp<RepaymentScheduleEntity>> getRepaymentScheduleList(@RequestParam String pid,
			@RequestParam(required = false, defaultValue = "1") int page,
			@RequestParam(required = false, defaultValue = "10") int rows) {
		
		Pageable pageable = new PageRequest(page - 1, rows);
		
		PageResp<RepaymentScheduleEntity> rep = targetService.getRepaymentScheduleList(pid, pageable);
		return new ResponseEntity<PageResp<RepaymentScheduleEntity>>(rep, HttpStatus.OK);
	}
}
