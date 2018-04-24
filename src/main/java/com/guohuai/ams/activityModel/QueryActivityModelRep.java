package com.guohuai.ams.activityModel;

import java.util.List;
import com.alibaba.fastjson.JSONObject;
import com.guohuai.component.web.view.BaseRep;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @Desc: 活动模板response
 * @author huyong
 * @date 2017.12.12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class QueryActivityModelRep extends BaseRep{
	
	private String modelOid;  //活动模板oid
	private String title;	//活动模板标题
	private String bannerUrl;	//活动模板banner图地址
	private String backgroundUrl;	//活动模板背景图地址
	private String code;		//活动模板（activityA/activityB）
	private String platType;		//活动模板类型（不同平台展示）：h5/app
	private List<JSONObject> places;		//产品占位+普通占位
}
