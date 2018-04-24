package com.guohuai.ams.project;

import java.util.List;

import org.springframework.data.domain.Page;

import com.guohuai.component.web.view.PageResp;

//import com.guohuai.asset.hill.component.web.view.PageResp;

public class ProjectListResp extends PageResp<Project> {

	public ProjectListResp() {
		super();
	}

	public ProjectListResp(Page<Project> page) {
		this(page.getContent(), page.getTotalElements());
	}

	public ProjectListResp(List<Project> list) {
		this(list, list.size());
	}

	public ProjectListResp(List<Project> list, long total) {
		this();
		super.setTotal(total);
		for (Project prj : list) {
//			ProjectResp pr = new ProjectResp();
//			BeanUtils.copyProperties(approval, prj);
//			super.getRows().add(pr);
			super.getRows().add(prj);
		}
	}

}
