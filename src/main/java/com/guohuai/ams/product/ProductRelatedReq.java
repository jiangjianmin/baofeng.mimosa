package com.guohuai.ams.product;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Desc: 产品相关请求参数
 * @author huyong
 * @date 2017.11.03
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)

public class ProductRelatedReq implements Cloneable{

	String name;
	String type="desc";
	String status;
	String interestAuditStatus;
	String raiseTimeBegin;
	String raiseTimeEnd;
	String sort;
	String order;
	String isDeleted=Product.NO;
	String auditState=Product.AUDIT_STATE_Reviewed;
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	/**
	 * 默认页数
	 */
	private Integer page = 1;
	

	/**
	 * 默认行数
	 */
	private Integer rows = 10;
	
	


	@Override
	public String toString() {
		List<String> allParas=new ArrayList<>();
		StringBuilder sb=new StringBuilder();
		if (StringUtils.isNoneBlank(name)) {
			allParas.add("name="+name);
		}
		if (StringUtils.isNoneBlank(type)) {
			allParas.add("type="+type);
		}
		if (StringUtils.isNoneBlank(status)) {
			allParas.add("status="+status);
		}
		if (StringUtils.isNoneBlank(interestAuditStatus)) {
			allParas.add("interestAuditStatus="+interestAuditStatus);
		}
		if (StringUtils.isNoneBlank(raiseTimeBegin)) {
			allParas.add("raiseTimeBegin="+raiseTimeBegin);
		}
		if (StringUtils.isNoneBlank(raiseTimeEnd)) {
			allParas.add("raiseTimeEnd="+raiseTimeEnd);
		}
		if (StringUtils.isNoneBlank(isDeleted)) {
			allParas.add("isDeleted="+isDeleted);
		}
		if (StringUtils.isNoneBlank(auditState)) {
			allParas.add("auditState="+auditState);
		}
		return allParas.stream().collect(Collectors.joining(","));
	}
}
