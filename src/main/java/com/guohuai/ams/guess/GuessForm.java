package com.guohuai.ams.guess;

import java.util.List;

import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

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
public class GuessForm {
	
	private String oid;
	@NotEmpty(message="活动名称不能为空")
	@Length(max=20,message="只允许输入20个字以内的文本")
	private String guessName;//活动名称
	@NotEmpty(message="副标题名称不能为空")
	@Length(max=20,message="只允许输入20个字以内的文本")
	private String guessTitle;//副标题
	private String imgPath;//图片路径
	@NotEmpty(message="编辑内容不能为空")
	private String content;//内容
	@NotEmpty(message="题目不能为空")
	@Length(max=100,message="只允许输入100个字以内的文本")
	private String question;//题目
	@NotEmpty(message="备注不能为空")
	@Length(max=100,message="只允许输入100个字以内的文本")
	private String remark;//备注
	private String status;//状态（1：已保存 2：已锁定 3：已上架  4：已结束）
	@NotEmpty(message="答案选项不能为空")
	@Size(min=2,max=10,message="至少需要录入2个答案,最多只能添加10个答案")
	private List<String> itemContents;//选项内容列表
	private List<String> itemOids;//选项oid列表

}
