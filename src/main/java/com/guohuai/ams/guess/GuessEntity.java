package com.guohuai.ams.guess;


import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.component.persist.UUID;


import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**竞猜活动
 * @author qiuliang
 *
 */
@Entity
@Table(name = "T_GAM_GUESS")
@lombok.Builder
@lombok.Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
public class GuessEntity  extends UUID {
	
	/**
	 * 状态--已保存
	 */
	public static final Integer GUESS_STATUS_CREATED = 1;
	/**
	 * 状态--已锁定
	 */
	public static final Integer GUESS_STATUS_LOCKED = 2;
	/**
	 * 状态--已上架
	 */
	public static final Integer GUESS_STATUS_ONSHELF = 3;
	/**
	 * 状态--已结束
	 */
	public static final Integer GUESS_STATUS_END = 4;
	
	/**
	 * 删除标志--已删除
	 */
	public static final Integer GUESS_DEL = 1;
	
	/**
	 * 删除标志--未删除
	 */
	public static final Integer GUESS_UNDEL = 0;
	
	private static final long serialVersionUID = 5269230818141419033L;
	private String guessName;//活动名称
	private String guessTitle;//副标题
	private String imgPath;//图片路径
	private String content;//内容
	private String question;//题目
	private String remark;//备注
	private Integer status;//状态（1：已保存 2：已锁定 3：已上架  4：已结束）
	private Integer delFlag;//删除标识（0:未删除  1：已删除）
	@Temporal(TemporalType.TIMESTAMP)
	private Date createTime;//创建时间
	private String createPerson;//创建人
	@Temporal(TemporalType.TIMESTAMP)
	private Date updateTime;//更新时间
	private String updatePerson;//更新人
	
}
