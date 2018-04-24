package com.guohuai.ams.duration.assetPool;

import java.util.List;

import org.springframework.data.domain.Page;

import com.guohuai.component.web.view.PageResp;

public class AssetPoolResp extends PageResp<AssetPoolEntity> {

	public AssetPoolResp() {
		super();
	}

	public AssetPoolResp(Page<AssetPoolEntity> approvals) {
		this(approvals.getContent(), approvals.getTotalElements());
	}

	public AssetPoolResp(List<AssetPoolEntity> approvals) {
		this(approvals, approvals.size());
	}

	public AssetPoolResp(List<AssetPoolEntity> Approvals, long total) {
		this();
		super.setTotal(total);
		super.setRows(Approvals);
	}

}
