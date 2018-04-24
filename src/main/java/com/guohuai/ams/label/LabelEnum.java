package com.guohuai.ams.label;

public enum LabelEnum {
	
	newbie("freshman"), zhangxinbao("2"), yueyueyin("3"), yuexiangyin("4"), yuejiayin("5"), yueyinyin("6"), baopin("7"), tiyanjin("experienceFund");
	
//	标签代码：1，标签名称：新手标
//	标签代码：2，标签名称：掌薪宝
//	标签代码：3，标签名称：悦月盈
//	标签代码：4，标签名称：悦享盈
//	标签代码：5，标签名称：悦嘉盈
//	标签代码：6，标签名称：悦盈盈                                                                                
//	标签代码：7，标签名称：爆品
//	标签代码：8，标签名称：体验金
	
	private String value;
	private LabelEnum(String value) {
		this.value = value;
	}
	@Override
	public String toString() {
		return this.value;
	}
}
