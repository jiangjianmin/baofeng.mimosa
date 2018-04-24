package com.guohuai.ams.label;

import com.guohuai.component.web.view.BaseResp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class LabelResp extends BaseResp {

	public LabelResp(LabelEntity l) {
		this.oid = l.getOid();
		this.labelCode = l.getLabelCode();
		this.labelName = l.getLabelName();
		this.labelType = l.getLabelType();
		this.isOk = l.getIsOk();
		this.labelDesc = l.getLabelDesc();
	}

	private String oid;
	private String labelCode;//标签代码
	private String labelName;//标签名称
	private String labelType;//标签类型
	private String isOk;//是否可用
	private String labelDesc;//标签描述

}
