package com.guohuai.ams.duration.capital.calc.error;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.alibaba.fastjson.JSONObject;
import com.guohuai.ams.cashtool.CashTool;
import com.guohuai.ams.duration.assetPool.AssetPoolEntity;
import com.guohuai.ams.investment.Investment;

import lombok.Data;

@Data
@Entity
@Table(name = "T_GAM_CALCULATE_ERROR_LOG")
@DynamicInsert
@DynamicUpdate
public class ErrorCalc implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	private String oid;
	@ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assetPoolOid", referencedColumnName = "oid")
	private AssetPoolEntity assetPool;
	@ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cashtoolOid", referencedColumnName = "oid")
	private CashTool cashTool;
	@ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "targetOid", referencedColumnName = "oid")
	private Investment target;
	// 消息
	private JSONObject message;
	// 创建日期
	private Timestamp createTime;
	// 操作人
	private String operator;
	
}
