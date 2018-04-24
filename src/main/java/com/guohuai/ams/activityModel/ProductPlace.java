package com.guohuai.ams.activityModel;

import java.io.Serializable;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Range;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductPlace implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5990402994975111049L;
	
	@NotEmpty(message="产品占位图不能为空")
	private String productPic;		//占位图地址
	@NotEmpty(message="渠道不能为空")
	private String channelId;		//渠道id
	private String channelName;		//渠道名称
	@NotEmpty(message="第一位置产品不能为空")
	private String firstProductOid;		//第一位置产品id
	private String firstProductName;		//第一位置产品名称
	private int hasBaofengbao;	//普通暴风宝（0：未选择 1：已选择）
	private int hasFreshMan;		//新手标（0：未选择 1：已选择）
	private int hasZeroBuy;		//0元购（0：未选择 1：已选择）
	private int orderBy;		//产品排序（0：按照预计年化收益率或者折算年化收益率排序1：按照存续期降序2：按照存续期升序）
	@Range(min=1,max=20,message="展示产品的个数只能输入1-20")
	private int maxNum;		//展示产品的个数上限
	private int orderNum;		//展示顺序
}
