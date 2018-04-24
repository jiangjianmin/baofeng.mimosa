package com.guohuai.ams.activityModel;

import java.io.Serializable;
import java.sql.Timestamp;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import com.guohuai.component.persist.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


/**
 * @Desc: 活动普通占位
 * @author huyong
 * @date 2017.12.11
 */
@Entity
@Table(name = "T_GAM_USUAL_PLACE")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
public class UsualPlaceEntity extends UUID implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String modelOid;		//活动模板oid
	private String usualPic;		//占位图地址
	private String primaryUrl;	//原生位链接地址
	private int orderNum;	//展示顺序
	private Timestamp createTime;	//创建时间
	private Timestamp updateTime;	//更新时间
	@Transient
	private String type;  //占位类型
}
