package com.guohuai.ams.activityModel;

import java.io.Serializable;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Desc: 活动模板新增请求
 * @author huyong
 * @date 2017.12.12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddActivityModelReq implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2543029016052713293L;
	private String modelOid;	//活动oid
//	@NotEmpty(message="活动标题不能为空")
	@Length(max=10,message="活动标题只允许输入10个字以内的文本")
	private String title;	//活动标题
//	@NotEmpty(message="活动banner图不能为空")
	private String bannerUrl;	//活动banner图地址
//	@NotEmpty(message="活动背景图不能为空")
	private String backgroundUrl;	//活动背景图地址
	@NotEmpty(message="活动模板不能为空")
	private String code;		//活动模板（activityA/activityB）
	@NotEmpty(message="活动模板类型不能为空")
	private String platType;		//活动模板类型（不同平台展示）：h5/app
//	@NotEmpty(message="活动普通站位不能为空")
	@Size(max=10,message="活动普通站位最多只能添加10个")
	@Valid
	private List<UsualPlace> usualPlaces;	//活动普通站位项
//	@NotEmpty(message="活动定制产品不能为空")
	@Size(max=2,message="活动定制产品最多只能添加2个")
	@Valid
	private List<ProductPlace> productPlaces;	//活动定制产品项
}
