package com.guohuai.ams.duration.fact.feigin;

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

import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.web.view.PageResp;
import com.guohuai.component.web.view.Response;

import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;

@RestController
@RequestMapping(value = "/mimosa/duration/feigin", produces = "application/json;charset=utf-8")
public class FeiginController extends BaseController {

	@Autowired
	private FeiginService feiginService;
	
	/**
	 * 费金计提
	 * @param form
	 * @return
	 */
	@RequestMapping(value = "/create", name = "资产池 - 费金计提", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> create(FeiginForm form) {
		feiginService.create(form, super.getLoginUser());
		Response r = new Response();
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	
	/**
	 * 获取费金计提表单
	 * @param spec
	 * @return
	 */
	@RequestMapping(value = "/getByOid", name = "资产池 - 获取费金计提表单", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> getByOid(
			@And({
				@Spec(params = "oid", path = "oid", spec = Equal.class),
				@Spec(params = "state", path = "state", spec = Equal.class)
			})Specification<FeiginEntity> spec) {
		FeiginForm form = feiginService.getFormByOid(spec);
		Response r = new Response();
		r.with("result", form);
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	
	/**
	 * 费金提取
	 * @param oid
	 * @return
	 */
	@RequestMapping(value = "/updateByOid", name = "资产池 - 费金提取", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> updateByOid(String oid) {
		feiginService.updateByOid(oid, super.getLoginUser());
		Response r = new Response();
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	
	/**
	 * 逻辑删除
	 * @param oid
	 * @return
	 */
	@RequestMapping(value = "/deleteByOid", name = "资产池 - 逻辑删除费金计提记录", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> deleteByOid(String oid) {
		feiginService.deleteByOid(oid, super.getLoginUser());
		Response r = new Response();
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	
	/**
	 * 费金计提列表
	 * @param spec
	 * @param page
	 * @param rows
	 * @param sortField
	 * @param sort
	 * @return
	 */
	@RequestMapping(value = "/getAll", name = "资产池 - 获取所有费金计提列表", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<PageResp<FeiginForm>> getAll(
			@And({
				@Spec(params = "name", path = "assetPool.name", spec = Like.class),
				@Spec(params = "state", path = "state", spec = Equal.class)
			})Specification<FeiginEntity> spec,
			@RequestParam(required = false, defaultValue = "1") int page,
			@RequestParam(required = false, defaultValue = "10") int rows,
			@RequestParam(required = false, defaultValue = "createTime") String sortField,
			@RequestParam(required = false, defaultValue = "desc") String sort) {
		Direction sortDirection = Direction.DESC;
		if (!"desc".equals(sort)) {
			sortDirection = Direction.ASC;
		}
		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(sortDirection, sortField)));
		PageResp<FeiginForm> rep = feiginService.getAll(spec, pageable);
		return new ResponseEntity<PageResp<FeiginForm>>(rep, HttpStatus.OK);
	}
}
