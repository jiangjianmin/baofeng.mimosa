package com.guohuai.ams.activityModel;

import java.util.ArrayList;
import java.util.Collection;
import com.guohuai.basic.cardvo.CardVoStatus;
import com.guohuai.basic.component.ext.web.BaseResp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


/**
 * @Desc: 产品占位下产品response
 * @author: huyong
 * @date: 2017.12.12
 */
@lombok.Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ActivityModelRep<T> extends BaseResp{
	//返回总数
	protected int total;
	//返回数据
	protected Collection<? extends T> rows = new ArrayList<>();
}
