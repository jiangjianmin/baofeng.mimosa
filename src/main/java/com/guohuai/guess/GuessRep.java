package com.guohuai.guess;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@lombok.Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuessRep {
	
    String name,title,imgPath, status,guessId;//状态显示文字（0：已上架:1：已结束）
    
	

}
