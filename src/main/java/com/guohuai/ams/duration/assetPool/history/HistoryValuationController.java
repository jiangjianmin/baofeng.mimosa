package com.guohuai.ams.duration.assetPool.history;

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

import com.guohuai.component.web.view.PageResp;

import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;

@RestController
@RequestMapping(value = "/mimosa/duration/assetPool/history", produces = "application/json;charset=utf-8")
public class HistoryValuationController {

	@Autowired
	private HistoryValuationService historyService;
	
	/**
	 * 获取当前资产池所有历史估值列表
	 * @return
	 */
	@RequestMapping(value = "/getAll", name = "历史估值 - 查询当前资产池所有历史估值列表", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<PageResp<HistoryValuationEntity>> getAll(
			@Spec(params = "pid", path = "assetPoolOid", spec = Equal.class)
			Specification<HistoryValuationEntity> spec,
			@RequestParam(required = false, defaultValue = "1") int page,
			@RequestParam(required = false, defaultValue = "10") int rows,
			@RequestParam(required = false, defaultValue = "createTime") String sortField,
			@RequestParam(required = false, defaultValue = "desc") String sort) {
		Direction sortDirection = Direction.DESC;
		if (!"desc".equals(sort)) {
			sortDirection = Direction.ASC;
		}
		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(sortDirection, sortField)));
		PageResp<HistoryValuationEntity> rep = historyService.findByPid(spec, pageable);
		return new ResponseEntity<PageResp<HistoryValuationEntity>>(rep, HttpStatus.OK);
	}
}
