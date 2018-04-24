package com.guohuai.ams.activityModel;

import lombok.Data;

@Data
public class ActivityModelReq {
	
	private String code;		//活动模板（activityA/activityB）
	private String platType;		//活动模板平台类型（不同平台展示）：h5/app
	private String modelOid;		// 产品占位oid
}
