package com.guohuai.ams.activityModel;

import java.io.Serializable;
import java.sql.Timestamp;
import javax.persistence.Entity;
import javax.persistence.Table;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import com.guohuai.component.persist.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


/**
 * @Desc: 活动模板
 * @author huyong
 * @date 2017.12.11
 */
@Entity
@Table(name = "T_GAM_ACTIVITY_MODEL")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
public class ActivityModelEntity  extends UUID implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2921247095246014558L;
	
	public static final String PRODUCT_TYPE = "product";  //产品占位
	public static final String USUAL_TYPE = "usual";		//普通占位
	
	/**
	 * 活动模板平台类型（不同平台展示）：h5/app
	 */
	public static final String PLAT_TYPE_APP = "app";		//活动模板平台类型app
	public static final String PLAT_TYPE_H5 = "h5";		//活动模板平台类型h5
	
	/**
	 * 活动模板：activityA或者activityB
	 */
	public static final String code_Activity_A = "activityA";		//活动模板A
	public static final String code_Activity_B = "activityB";		//活动模板B
	
	private String title;	//活动标题
	private String bannerUrl;	//活动banner图地址
	private String backgroundUrl;	//活动背景图地址
	private String code;		//活动模板：activityA或者activityB
	private String platType;		//活动模板平台类型（不同平台展示）：h5/app
	private String createOperator;	//创建人
	private Timestamp createTime;	//创建时间
	private String operator;		//更新操作人
	private Timestamp updateTime;	//活动更新时间
}
