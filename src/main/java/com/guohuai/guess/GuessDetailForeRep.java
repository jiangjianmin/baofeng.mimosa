package com.guohuai.guess;

import java.util.ArrayList;
import java.util.List;

import com.guohuai.component.web.view.BaseRep;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@lombok.Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuessDetailForeRep extends BaseRep{
	
	String name;
	String title;
	String content;
	String productStatus;//0：去下注（募集中（未售罄）） 1：来袭中（产品上架间隙或已下架）2：已售罄
	String productId;
	String guessStatus;//3：已上架 4：已结束（前台展示已售罄）
	List<ItemRep> itemList = new ArrayList<ItemRep>();

}
