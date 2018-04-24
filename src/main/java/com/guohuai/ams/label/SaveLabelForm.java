package com.guohuai.ams.label;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveLabelForm implements Serializable {
	
	private static final long serialVersionUID = -7465353755243043192L;
	
	private String oid;
	private String labelCode;//标签代码
	private String labelName;//标签名称
	private String labelType;//标签类型
	private String labelDesc;//标签描述

}
