package com.guohuai.ams.duration.fact.calibration;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.web.view.Response;

@RestController
@RequestMapping(value = "/mimosa/duration/market", produces = "application/json;charset=utf-8")
public class MarketAdjustController extends BaseController {

	@Autowired
	private MarketAdjustService adjustService;

	/**
	 * 市值校准录入 详情表单
	 * 
	 * @param pid
	 * @return
	 */
	@RequestMapping(value = "/getMarketAdjustData", name = "资产池 - 市值校准录入详情表单", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> getMarketAdjustData(@RequestParam String pid) {
		MarketAdjustResp form = adjustService.getMarketAdjust(pid);
		Response r = new Response();
		r.with("result", form);
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}

	/**
	 * 市值校准录入
	 * 
	 * @param form
	 * @return
	 */
	@RequestMapping(value = "/saveMarketAdjust", name = "资产池 - 市值校准录入", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> saveMarketAdjust(MarketAdjustForm form) {
		form.setStatus(MarketAdjustEntity.CREATE);
		adjustService.saveMarketAdjust(form, super.getLoginUser());
		Response r = new Response();
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}

	/**
	 * 市值校准记录详情
	 * 
	 * @param form
	 * @return
	 */
	@RequestMapping(value = "/getMarketAdjust", name = "资产池 - 市值校准记录详情", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> getMarketAdjust(@RequestParam String oid) {
		MarketAdjustEntity market = adjustService.findOne(oid);
		Response r = new Response();
		r.with("result", market);
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}

	/**
	 * 市值校准录入审核
	 * 
	 * @param form
	 * @return
	 */
	@RequestMapping(value = "/auditMarketAdjust", name = "资产池 - 市值校准录入审核", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> auditMarketAdjust(@RequestParam String oid, @RequestParam String type) {
		adjustService.auditMarketAdjust(oid, type, super.getLoginUser());
		Response r = new Response();
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}

	/**
	 * 查询当天的订单状态
	 * -1：未审核；0：未录入；1：已通过
	 * 
	 * @param pid
	 * @return
	 */
	@RequestMapping(value = "/getMarketAdjustStuts", name = "资产池 - 查询当天市值校准的状态", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> getMarketAdjustStatus(@RequestParam String pid) {
		int stutas = adjustService.getMarketAdjustStatus(pid);
		Response r = new Response();
		r.with("result", stutas);
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}

	/**
	 * 市值校准录入删除
	 * 
	 * @param form
	 * @return
	 */
	@RequestMapping(value = "/deleteMarketAdjust", name = "资产池 - 市值校准记录删除", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> deleteMarketAdjust(@RequestParam String oid) {
		MarketAdjustEntity market = adjustService.findOne(oid);
		market.setStatus(MarketAdjustEntity.DELETE);
		market.setAuditor(super.getLoginUser());
		market.setAuditTime(new Timestamp(System.currentTimeMillis()));
		adjustService.save(market);
		Response r = new Response();
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}

	/**
	 * 市值校准记录 列表
	 * 
	 * @param pid
	 *            资产池id
	 * @return
	 */
	@RequestMapping(value = "/getMarketAdjustList", name = "资产池 - 市值校准记录列表", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> getMarketAdjustList(@RequestParam String pid, @RequestParam(required = false, defaultValue = "1") int page,
			@RequestParam(required = false, defaultValue = "10") int rows, @RequestParam(required = false, defaultValue = "baseDate") String sortField,
			@RequestParam(required = false, defaultValue = "desc") String sort) {
		Direction sortDirection = Direction.DESC;
		if (!"desc".equals(sort)) {
			sortDirection = Direction.ASC;
		}
		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(sortDirection, sortField)));

		Date baseDate = this.adjustService.getBaseDate();
		Page<MarketAdjustEntity> list = adjustService.getMarketAdjustList(pid, pageable);
		List<MarketAdjustResp> resps = new ArrayList<MarketAdjustResp>();
		if (null != list.getContent() && list.getContent().size() > 0) {
			for (MarketAdjustEntity e : list.getContent()) {
				resps.add(new MarketAdjustResp(e, baseDate));
			}
		}
		Response r = new Response();
		r.with("rows", resps);
		r.with("total", list.getTotalElements());
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}

	/**
	 * 市值校准 - 收益率
	 * 
	 * @param form
	 * @return
	 */
	@RequestMapping(value = "/getYield", name = "资产池 - 市值校准 - 收益率", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> getYield(@RequestParam String pid) {
		List<JSONObject> obj = adjustService.getListForYield(pid);
		Response r = new Response();
		r.with("result", obj);
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
}
