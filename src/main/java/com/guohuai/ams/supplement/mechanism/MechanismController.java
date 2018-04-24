package com.guohuai.ams.supplement.mechanism;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.guohuai.ams.project.Project;
import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.web.view.BaseResp;
import com.guohuai.component.web.view.PageResp;
import com.guohuai.operate.api.AdminSdk;

import lombok.extern.slf4j.Slf4j;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;

/**补单机构管理
 * @author qiuliang
 *
 */
@RestController
@RequestMapping(value = "/mimosa/boot/mechanism", produces = "application/json;charset=UTF-8")
@Slf4j
public class MechanismController extends BaseController{
	
	@Autowired
	private MechanismService mechanismService;
	
	
	@RequestMapping(name = "保存补单机构", value = "save", method = RequestMethod.POST)
	public ResponseEntity<BaseResp> save(@Valid MechanismForm mechanismForm) {

		String loginId = null;
		loginId = super.getLoginUser();
		Mechanism mechanism = new Mechanism();
		mechanism.setAccount(mechanismForm.getAccount());
		mechanism.setAccountBank(mechanismForm.getAccountBank());
		mechanism.setBankName(mechanismForm.getBankName());
		mechanism.setContactMan(mechanismForm.getContactMan());
		mechanism.setContactPhone(mechanismForm.getContactPhone());
		mechanism.setFullName(mechanismForm.getFullName());
		mechanism.setShotName(mechanismForm.getShotName());
		mechanism.setRemark(mechanismForm.getRemark());
		mechanism.setOperator(loginId);
		mechanism.setStatus(Mechanism.Enable);
		mechanism.setUpdateTime(new Date());
		mechanism.setCreateTime(new Date());
		BaseResp response = mechanismService.save(mechanism);
		return new ResponseEntity<BaseResp>(response, HttpStatus.OK);
	}
	
	@RequestMapping(name = "查询补单机构列表", value = "mechanismlist", method = { RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody ResponseEntity<PageResp<MechanismRep>> mechanismList(
			@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "50") int size, @RequestParam(defaultValue = "desc") String sort,
			@RequestParam(defaultValue = "createTime") String sortField) {
		if (page < 1) {
			page = 1;
		}
		if (size <= 0) {
			size = 50;
		}
		Direction sortDirection = Direction.DESC;
		if("asc".equals(sort)){
			sortDirection = Direction.ASC;
		}
		PageResp<MechanismRep> pageResp = mechanismService.list(page, size, sortDirection, sortField);
		return new ResponseEntity<PageResp<MechanismRep>>(pageResp, HttpStatus.OK);
	}
	
	@RequestMapping(name = "查询补单机构", value = "detail", method = { RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody ResponseEntity<MechanismRep> mechanism(@RequestParam String oid) {
		Mechanism mechanism = mechanismService.detail(oid);
		return new ResponseEntity<MechanismRep>(new MechanismRep(mechanism), HttpStatus.OK);
	}
	
	@RequestMapping(name = "查询补单机构", value = "all", method = { RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody ResponseEntity<MechanismListRep> mechanismAll() {
		MechanismListRep mechanism = mechanismService.findAll();
		return new ResponseEntity<MechanismListRep>(mechanism, HttpStatus.OK);
	}
	

}
