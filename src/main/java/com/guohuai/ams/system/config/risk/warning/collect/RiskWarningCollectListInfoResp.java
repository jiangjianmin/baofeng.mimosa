package com.guohuai.ams.system.config.risk.warning.collect;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class RiskWarningCollectListInfoResp {

	public RiskWarningCollectListInfoResp(String oid, String sn, String name, String state, String level) {
		this.oid = oid;
		this.sn = sn;
		this.name = name;
		this.state = state;
		this.level = level;
	}

	private String oid; // 标的Oid
	private String sn; // 标的编号
	private String name; // 标的名称
	private String state; // 标的状态
	private String level; // 风险等级
}
