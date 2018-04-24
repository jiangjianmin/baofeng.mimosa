
package com.guohuai.ams.project;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.web.view.BaseResp;
import com.guohuai.component.web.view.PageResp;

//import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;

/**
 * @ClassName: ProjectController
 * @Description: 立项管理
 * @author vania
 * @date 2016年3月25日 上午10:08:44
 *
 */
@RestController
@RequestMapping(value = "/mimosa/boot/project", produces = "application/json;charset=UTF-8")
@Slf4j
public class ProjectController extends BaseController {
	@Autowired
	private ProjectService projectService;

	@RequestMapping(name = "查询底层项目", value = "projectlist", method = { RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody ResponseEntity<PageResp<Project>> projectlist(HttpServletRequest request,
			@And({ @Spec(params = "targetOid", path = "investment.oid", spec = Equal.class), @Spec(params = "projectName", path = "projectName", spec = Like.class),
					@Spec(params = "projectManager", path = "projectManager", spec = Like.class),
					@Spec(path = "projectType", params = "projectType", spec = Equal.class) }) Specification<Project> spec,
			@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "50") int size, @RequestParam(defaultValue = "desc") String sortDirection,
			@RequestParam(defaultValue = "updateTime") String sortField) {
		if (page < 1) {
			page = 1;
		}
		if (size <= 0) {
			size = 50;
		}
		Page<Project> pagedata = projectService.list(spec, page - 1, size, sortDirection, sortField);
		PageResp<Project> pageResp = new PageResp<>(pagedata);
		return new ResponseEntity<PageResp<Project>>(pageResp, HttpStatus.OK);

		// ProjectListResp resp = new ProjectListResp(pagedata);
		//
		// return new ResponseEntity<ProjectListResp>(resp, HttpStatus.OK);
	}

	/**
	 * 创建底层项目
	 * 
	 * @Title: save
	 * @author vania
	 * @version 1.0
	 * @see:
	 * @param projectForm
	 * @return CommonResp 返回类型
	 */
	@RequestMapping(name = "保存底层项目", value = "save", method = RequestMethod.POST)
	public BaseResp save(@Valid ProjectForm projectForm) {

		String loginId = null;
		try {
			loginId = super.getLoginUser();
		} catch (Exception e) {
			log.error("获取操作员失败, 原因: " + e.getMessage());
		}
		projectForm.setCreator(loginId);
		projectForm.setOperator(loginId);
		this.projectService.save(projectForm);
		return new BaseResp();
	}

	/**
	 * 删除底层项目
	 * 
	 * @Title: deleteProject
	 * @author vania
	 * @version 1.0
	 * @see:
	 * @param approvalReq
	 * @return BaseResp 返回类型
	 */
	@RequestMapping(name = "删除底层项目", value = "deleteProject", method = { RequestMethod.POST, RequestMethod.GET })
	public BaseResp deleteProject(@RequestParam String targetOid, @RequestParam String oid) {
		log.info("删除投资标的id=" + targetOid + "的底层项目id=" + oid);
		projectService.deleteByTargetOidAndOid(targetOid, oid);
		return new BaseResp();
	}

	/**
	 * 根据标的id查询底层项目
	 * 
	 * @Title: getByTargetId
	 * @author vania
	 * @version 1.0
	 * @see:
	 * @param targetOid
	 * @return
	 * @return CommonResp 返回类型
	 */
//	@ApiOperation(value = "根据标的id查询底层项目")
	@RequestMapping(name = "根据标的id查询底层项目", value = "getByTargetId", method = { RequestMethod.POST, RequestMethod.GET })
	public PageResp<Project> getByTargetId(@RequestParam(required = true) String targetOid) {
		List<Project> list = this.projectService.findByTargetId(targetOid);
		PageResp<Project> pageResp = new PageResp<>(null == list ? 0 : list.size(), list);
		return pageResp;
		// return new ResponseEntity<PageResp<Project>>(pageResp, HttpStatus.OK);
		// return PageResp.builder().rows(list).total(null == list ? 0 : list.size()).build();
	}

	/**
	 * 根据项目id查询底层项目
	 * 
	 * @Title: getByOid
	 * @author vania
	 * @version 1.0
	 * @see:
	 * @param oid
	 * @return
	 * @return CommonResp 返回类型
	 */
//	@ApiOperation(value = "根据项目id查询底层项目")
	@RequestMapping(name = "根据项目id查询底层项目", value = "getByOid", method = { RequestMethod.POST, RequestMethod.GET })
	public ProjectResp getByOid(@RequestParam(required = true) String oid) {
		Project prj = this.projectService.findByOid(oid);
		ProjectResp resp = new ProjectResp(prj);
		return resp;
	}

}
