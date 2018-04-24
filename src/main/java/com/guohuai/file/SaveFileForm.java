package com.guohuai.file;

import java.io.Serializable;

import javax.validation.constraints.Min;

import org.hibernate.validator.constraints.NotBlank;

import com.guohuai.component.web.parameter.validation.groups.GeneralGroup;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveFileForm implements Serializable {

	private static final long serialVersionUID = 691015236113209974L;

	private String oid;
	@NotBlank(message = "文件名不可为空", groups = { GeneralGroup.class })
	private String name;
	@NotBlank(message = "文件链接不可为空", groups = { GeneralGroup.class })
	private String furl;
	@Min(value = 1, message = "文件尺寸参数错误", groups = { GeneralGroup.class })
	private long size;

}
