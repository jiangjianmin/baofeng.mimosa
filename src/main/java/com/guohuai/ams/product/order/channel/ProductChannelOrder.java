package com.guohuai.ams.product.order.channel;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.ams.channel.Channel;
import com.guohuai.ams.product.Product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "T_GAM_PRODUCT_CHANNEL_ORDER")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
public class ProductChannelOrder implements Serializable {

	private static final long serialVersionUID = -8728888636245221185L;

	// 待审核
	public static final String STATUS_SUBMIT = "SUBMIT";
	//审核通过
	public static final String STATUS_PASS = "PASS";
	//审核驳回
	public static final String STATUS_FAIL = "FAIL";
	//取消
	public static final String STATUS_CANCEL = "CANCEL";
	//删除
	public static final String STATUS_DELETE = "DELETE";
		
	@Id
	private String oid;
	@ManyToOne
	@JoinColumn(name = "productOid")
	private Product product;// 产品
	@ManyToOne
	@JoinColumn(name = "channelOid")
	private Channel channel; // 渠道
	private String creator;
	private Timestamp createTime;
	private String auditor;
	private Timestamp auditTime;
	private String status;

}
