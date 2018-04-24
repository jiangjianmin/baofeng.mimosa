package com.guohuai.ams.guess;



import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@lombok.Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuessQueryRep {
	
	
	
	public GuessQueryRep(GuessEntity guess) {
		this.oid = guess.getOid();
		this.guessName = guess.getGuessName();
		this.status = guess.getStatus();
		this.createTime = new Timestamp(guess.getCreateTime().getTime());
		this.createPerson = guess.getCreatePerson();
	}
	
	private String oid;

	private String guessName;//标题
	
	private Integer status;
	
	private Timestamp createTime;//创建时间
	
	private String createPerson;//创建人
	
	private Integer isAllRaiseFail;//产品包下的产品是否全部流标（1：是，0：否）
	
	private Integer hasInterest;//产品包下的产品是否有派息的（1：是，0：否）
	
	private String guessTitle;//副标题
	
    private String imgPath;//图片路径
    
    private String content;//内容

}
