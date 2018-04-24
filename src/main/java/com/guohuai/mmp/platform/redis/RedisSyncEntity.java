package com.guohuai.mmp.platform.redis;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "T_MONEY_REDIS_SYNC")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class RedisSyncEntity implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6586581236972774749L;

	
	/**

create table T_MONEY_REDIS_SYNC
(
   oid                  int not null auto_increment,
   syncOid              varchar(32),
   syncOidType          varchar(32) comment 'product:产品,investorHold:投资者持仓,
   spvHold:SPV持仓,investorStatistic:投资人统计,investor:投资人,channel:渠道，label:标签,assetpool:资产池,tradeorder:委托单',
   productOid           varchar(32),
   assetpoolOid         varchar(32),
   updateTime           timestamp not null default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
   createTime           timestamp default CURRENT_TIMESTAMP,
   primary key (oid)
);


	 */
	
	public static final String SYNC_syncOidType_product = "product"; //productOid
	public static final String SYNC_syncOidType_spvHold = "spvHold"; //productOid
	public static final String SYNC_syncOidType_investorHold = "investorHold"; // investorOid
	
	
	//useless
	public static final String SYNC_syncOidType_investorStatistic = "investorStatistic";
	public static final String SYNC_syncOidType_investor = "investor";
	public static final String SYNC_syncOidType_channel = "channel";
	public static final String SYNC_syncOidType_label = "label";
	public static final String SYNC_syncOidType_assetpool = "assetpool";
	public static final String SYNC_syncOidType_tradeorder = "tradeorder";
	
	@Id
	private int oid;
	
	private String syncOid;
	
	private String productOid;
	
	private String assetPoolOid;
	
	private String syncOidType;
	
	private Timestamp updateTime;
	
	private Timestamp creaetTime;
}
