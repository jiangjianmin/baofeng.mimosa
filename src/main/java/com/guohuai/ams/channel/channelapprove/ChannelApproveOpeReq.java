package com.guohuai.ams.channel.channelapprove;

import javax.validation.constraints.NotNull;

import com.guohuai.component.web.parameter.validation.Enumerations;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class ChannelApproveOpeReq {

	@NotNull(message = "审批唯一键不能为空！")
	private String apprOid;
	
	@NotNull(message = "审批结果不能为空！")
	@Enumerations(values = {"pass","refused"}, message = "审批结果参数有误！")
	private String apprResult;
	
	@NotNull(message = "审核意见不能为空！")
	private String remark;
}
