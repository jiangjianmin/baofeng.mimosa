package com.guohuai.ams.guess;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.ams.guess.GuessEntity.GuessEntityBuilder;
import com.guohuai.component.persist.UUID;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**竞猜活动选项
 * @author qiuliang
 *
 */
@Entity
@Table(name = "T_GAM_GUESS_ITEM")
@lombok.Builder
@lombok.Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
public class GuessItemEntity extends UUID{
	
	private static final long serialVersionUID = 2244892179078598234L;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "guessOid", referencedColumnName = "oid")
	private GuessEntity guess;//对应竞猜活动
	private String content;//选项内容（答案）
	private BigDecimal percent;//加息百分比（支持正数，0，负数）
	private BigDecimal netPercent;//净值百分比
	@Temporal(TemporalType.TIMESTAMP)
	private Date createTime;
	private String createPerson;
	@Temporal(TemporalType.TIMESTAMP)
	private Date updateTime;
	private String updatePerson;
	
	
	

}
