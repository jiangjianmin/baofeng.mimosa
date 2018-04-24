package com.guohuai.ams.activityModel;

import java.io.Serializable;
import org.hibernate.validator.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsualPlace implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 893165658978667698L;
	
	@NotEmpty(message="普通占位图不能为空")
	private String usualPic;		//占位图地址
//	@NotEmpty(message="原生位链接地址不能为空")
	private String primaryUrl;		//原生位链接地址
	private int orderNum;		//展示顺序
}
