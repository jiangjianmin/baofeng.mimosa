package com.guohuai.ams.guess;

import java.math.BigDecimal;
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
public class GuessDetailRep extends BaseRep{
	
	private String guessName;//活动名称
	private String guessTitle;//副标题
	private String imgPath;//图片路径
	private String content;//内容
	private String question;//题目
	private String remark;//备注
	private List<String> answer;//答案
	private List<BigDecimal> percent;//开奖信息
	private String productPackageName;//产品包简称
	public GuessDetailRep(GuessEntity guess, List<String> answerList, List<BigDecimal> percentList, String name) {
		this.guessName = guess.getGuessName();
		this.guessTitle = guess.getGuessTitle();
		this.imgPath = guess.getImgPath();
		this.content = guess.getContent();
		this.question = guess.getQuestion();
		this.remark = guess.getRemark();
		this.answer = answerList;
		this.percent = percentList;
		this.productPackageName = name;
	}

}
