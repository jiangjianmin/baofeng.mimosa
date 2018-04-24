package com.guohuai.ams.product;

import java.sql.Timestamp;

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
public class ProductTypeDetailQueryResp {
	public ProductTypeDetailQueryResp(ProductTypeDetail detail) {
		this.oid = detail.getOid();
		this.type = "00".equals(detail.getType())?"产品要素表":("01".equals(detail.getType())?"产品说明书":"活动产品详情介绍");
		this.title = detail.getTitle();
		this.detail = detail.getDetail();
		this.url = detail.getUrl();
		this.operator = detail.getOperator();
		this.createTime = detail.getCreateTime();
		this.updateTime = detail.getUpdateTime();
		
	}
	private String oid;
	private String type;
	private String title;
	private String detail;
	private String url;
	private String operator;
	private Timestamp createTime;
	private Timestamp updateTime;
}
