package com.guohuai.ams.guess;

import com.guohuai.component.web.view.BaseRep;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class GuessRecordQueryReq  extends BaseRep{
	/**
	 * 默认页数
	 */
	private int page = 1;
	/**
	 * 默认行数
	 */
	private int row = 5;
	private String guessName = "";
	private String productName = "";
	private String realName = "";//用户姓名
	private String phoneNum = "";
	private String orderStarus = "";
}
