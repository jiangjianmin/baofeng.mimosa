package com.guohuai.mmp.investor.tradeorder;

import com.guohuai.component.persist.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

/**
 * 开放循环产品关系表
 *
 * @param
 * @author yujianlong
 * @date 2018/3/23 15:56
 * @return
 */
@Entity
@Table(name = "T_MONEY_INVESTOR_OPENCYCLE_TRADEORDER_RELATION")
@lombok.Builder
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
public class InvestorOpenCycleRelationEntity implements Serializable {

    private static final long serialVersionUID = -3698224210276429131L;

    /*快定宝投资状态 0默认*/
    public static final Integer ASSIGNMENTTYPE_DEFAULT = 0;
    /*快定宝投资状态 ,1转投*/
    public static final Integer ASSIGNMENTTYPE_CHANGEINVEST = 1;
    /*快定宝投资状态 2续投*/
    public static final Integer ASSIGNMENTTYPE_CONTINUEINVEST = 2;
    /*快定宝投资状态 ,3部分赎回*/
    public static final Integer ASSIGNMENTTYPE_PARTREDEEM = 3;
    /*快定宝投资状态 ,4全部赎回*/
    public static final Integer ASSIGNMENTTYPE_ALLREDEEM = 4;
    /*快定宝投资状态 ,5还本付息*/
    public static final Integer ASSIGNMENTTYPE_CASH = 5;

    /*是否续投状态 1续投*/
    public static final Integer CONTINUESTATUSTYPE_YES = 1;
    /*是否续投状态 0不续投*/
    public static final Integer CONTINUESTATUSTYPE_NO = 0;

    /**
     * 循环产品相关订单类型 预约单
     */
    public static final String ORDERTYPE_BOOKING = "booking";
    /* 循环产品相关订单类型 转投单 */
    public static final String ORDERTYPE_CHANGE = "change";
    /* 循环产品相关订单类型 续投单 */
    public static final String ORDERTYPE_CONTINUE = "continue";

    /**
     *  订单支付方式 银行卡
     */
    public static final String PAYTYPE_BANK = "bank";
    /* 订单支付方式 快活宝 */
    public static final String PAYTYPE_T0 = "t0";

    /**
     *  循环产品相关订单状态 申请中
     */
    public static final String ORDERSTATUS_APPLYING = "applying";
    /* 循环产品相关订单状态 已受理 */
    public static final String ORDERSTATUS_ACCEPTED = "accepted";

    /**
     * 原订单orderCode
     */
    @Id
    private String sourceOrderCode;

    /**
     * 原订单金额
     */
    private BigDecimal sourceOrderAmount;

    /**
     * 快定宝投资状态,1转投,2续投,3部分赎回,4全部赎回,5还本付息
     */
    private Integer assignment;

    /**
     * 循环产品订单类型
     */
    private String orderType;

    /**
     * 支付方式
     */
    private String payType;

    /**
     * 循环产品订单状态
     */
    private String orderStatus;

    /**
     * 用户oid
     */
    private String investorOid;

    /**
     * 用户手机号
     */
    private String phone;

    /**
     * 对应转投或者续投的订单orderCode
     */
    private String investOrderCode;

    /**
     * 新订单的投资产品名称【转投或续投的产品名称】
     */
    private String investProductName;

    /**
     * 投资单订单金额【转投是更新】
     */
    private BigDecimal investAmount;

    /**
     * 赎回或还本付息订单orderCode
     */
    private String redeemOrderCode;

    /**
     * 赎回单订单金额
     */
    private BigDecimal redeemAmount;

    /**
     * 是否续投状态0否1是,默认1
     */
    private Integer continueStatus;

    /**
     * 循环产品确认日期
     */
    private Date cycleConfirmDate;

    private Timestamp createTime;

    private Timestamp updateTime;


}
