package com.guohuai.ams.label;

import java.text.ParseException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
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
import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.web.view.BaseResp;
import com.guohuai.component.web.view.PageResp;
import com.guohuai.component.web.view.Response;

import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;

/**
 * 标签管理操作相关接口
 * 
 * @author wangyan
 *
 */
@RestController
@RequestMapping(value = "/mimosa/productLabel", produces = "application/json")
public class LabelController extends BaseController {

	@Autowired
	private LabelService labelService;

	@RequestMapping(value = "/getProductLabelNames", name = "新加编辑产品可以选择的标签名称列表", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> getAllNameList(@RequestParam(required = true) String labelType) {
		List<JSONObject> jsonList = labelService.getProductLabelNames(labelType);
		Response r = new Response();
		r.with("rows", jsonList);
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}

	/**
	 * 新加标签
	 * 
	 * @param form
	 * @return
	 */
	@RequestMapping(value = "/save", name = "新加标签", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseResp> save(@Valid SaveLabelForm form) throws ParseException, Exception {
		BaseResp repponse = this.labelService.save(form);
		return new ResponseEntity<BaseResp>(repponse, HttpStatus.OK);
	}

	/**
	 * 更新标签
	 * 
	 * @param form
	 * @return
	 */
	@RequestMapping(value = "/update", name = "编辑标签", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseResp> update(@Valid SaveLabelForm form) throws ParseException {
		BaseResp repponse = this.labelService.update(form);
		return new ResponseEntity<BaseResp>(repponse, HttpStatus.OK);
	}

	/**
	 * 标签明细
	 * 
	 * @param oid
	 * @return {@link ResponseEntity<ProductLabelResp>}
	 *         ,如果返回的errCode属性等于0表示成功，否则表示失败，失败原因在errMessage里面体现
	 */
	@RequestMapping(value = "/detail", name = "产品明细", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<LabelResp> detail(@RequestParam(required = true) String oid) {
		LabelResp plr = this.labelService.read(oid);
		return new ResponseEntity<LabelResp>(plr, HttpStatus.OK);
	}

	@RequestMapping(value = "/invalid", name = "停用标签", method = { RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<BaseResp> invalid(@RequestParam(required = true) String oid) {
		BaseResp repponse = this.labelService.invalid(oid);
		return new ResponseEntity<BaseResp>(repponse, HttpStatus.OK);
	}

	@RequestMapping(value = "/valid", name = "启用标签", method = { RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<BaseResp> valid(@RequestParam(required = true) String oid) {
		BaseResp repponse = this.labelService.valid(oid);
		return new ResponseEntity<BaseResp>(repponse, HttpStatus.OK);
	}

	/**
	 * 标签列表查询
	 * 
	 * @param request
	 * @param page
	 *            第几页
	 * @param rows
	 *            每页显示多少记录数
	 * @return {@link ResponseEntity<PagesRep<ProductQueryRep>>}
	 *         ,如果返回的errCode属性等于0表示成功，否则表示失败，失败原因在errMessage里面体现
	 */
	@RequestMapping(value = "/productLabelList", name = "产品申请列表查询", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<PageResp<LabelResp>> list(HttpServletRequest request,
			@Spec(params = "labelType", path = "labelType", spec = Equal.class) Specification<LabelEntity> spec,
			@RequestParam int page, @RequestParam int rows,
			@RequestParam(required = false, defaultValue = "updateTime") String sort,
			@RequestParam(required = false, defaultValue = "desc") String order) {

		if (page < 1) {
			page = 1;
		}
		if (rows < 1) {
			rows = 1;
		}

		Direction sortDirection = Direction.DESC;
		if (!"desc".equals(order)) {
			sortDirection = Direction.ASC;
		}

		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(sortDirection, sort)));
		PageResp<LabelResp> rep = this.labelService.list(spec, pageable);
		return new ResponseEntity<PageResp<LabelResp>>(rep, HttpStatus.OK);
	}

	/**
	 * 验证标签代码是否唯一
	 * 
	 * @param name
	 * @param oid
	 * @return
	 */
	@RequestMapping(value = "/validateCode", name = "验证标签代码是否唯一", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<BaseResp> validateCode(@RequestParam String labelCode,
			@RequestParam(required = false) String id) {
		BaseResp pr = new BaseResp();
		long single = this.labelService.validateSingle("labelCode", labelCode, id);
		return new ResponseEntity<BaseResp>(pr, single > 0 ? HttpStatus.CONFLICT : HttpStatus.OK);
	}

}
