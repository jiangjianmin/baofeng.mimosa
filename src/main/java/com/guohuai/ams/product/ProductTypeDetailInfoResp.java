package com.guohuai.ams.product;

import java.sql.Timestamp;

import com.guohuai.component.web.view.BaseResp;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class ProductTypeDetailInfoResp extends BaseResp{
	private String oid;
	private String type;
	private String title;
	private String detail;
	private String url;
	private String operator;
	private Timestamp createTime;
	private Timestamp updateTime;
}
