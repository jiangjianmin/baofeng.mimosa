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
 * @Desc: 活动定制产品
 * @author huyong
 * @date 2017.12.11
 */
@Entity
@Table(name = "T_GAM_PRODUCT_PLACE")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
public class ProductPlaceEntity  extends UUID implements Serializable {
	
	public static final int EXPAROR_ARRORDISP_ASC = 0;  //按照存续期降序排序
	public static final int DURATIONPERIOD_DESC= 1;  //按照存续期降序排序
	public static final int DURATIONPERIOD_ASC = 2;  //按照存续期升序排序

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String modelOid;		//活动模板oid
	private String productPic;	//占位图地址
	private String channelId;	//渠道id
	private String channelName;		//渠道名称
	private String firstProductOid;		//第一个位置的产品
	private String firstProductName;		//第一位置产品名称
	private int hasBaofengbao;		//普通暴风宝（0：未选择 1：已选择）
	private int hasFreshMan;		//新手标（0：未选择 1：已选择）
	private int hasZeroBuy;		//0元购（0：未选择 1：已选择）
	private int orderBy;		//产品排序（0：按照预计年化收益率或者折算年化收益率排序 1：按照存续期降序 2：按照存续期升序）
	private int maxNum;		//展示产品的个数上限
	private int orderNum;		//展示顺序
	private Timestamp createTime;	//创建时间
	private Timestamp updateTime;	//更新时间
	@Transient
	private String type;  //占位类型
}
