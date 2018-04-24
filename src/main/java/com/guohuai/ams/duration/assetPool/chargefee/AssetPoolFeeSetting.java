package com.guohuai.ams.duration.assetPool.chargefee;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.ams.duration.assetPool.AssetPoolEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "T_GAM_ASSETPOOL_CHARGEFEE_SETTING")
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
public class AssetPoolFeeSetting implements Serializable {

	private static final long serialVersionUID = 3944120301107307186L;

	@Id
	private String oid;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assetPoolOid", referencedColumnName = "oid")
	private AssetPoolEntity assetPool;
	private BigDecimal startAmount;
	private BigDecimal endAmount;
	private BigDecimal feeRatio;

}
