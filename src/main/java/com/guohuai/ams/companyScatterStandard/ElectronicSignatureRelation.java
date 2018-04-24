package com.guohuai.ams.companyScatterStandard;

import com.guohuai.component.persist.UUID;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 *
 *订单电子签章关联实体
 * @author yujianlong
 * @date 2018/4/21 12:20
 * @param
 * @return
 */
@Entity
@Table(name = "t_money_investor_tradeorder_electronicsignatrue_relation")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
public class ElectronicSignatureRelation  implements Serializable {


	private static final long serialVersionUID = -677505023622260342L;
	/**
	 * 订单号
	 */
	@Id
	private String orderCode;


	/**
	 * 产品oid
	 */
	private String productOid;

	/**
	 * 合同编号 产品code +001-200
	 */
	private String contractCode;


	/**
	 * 电子签章地址
	 */
	private String electronicSignatureUrl;
	/**
	 * 存证地址
	 */
	private String evidenceUrl;

	private Timestamp createTime;

	private Timestamp updateTime;

}
