package com.guohuai.mmp.investor.tradeorder;

import com.guohuai.component.persist.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 15天定期赎回列表
 */
@Entity
@Table(name = "T_MONEY_INVESTOR_PLUS_REDEEM")
public class InvestorBfPlusRedeemEntity {
    private static final long serialVersionUID = -3332295760819210048L;

    @Id
    private String oid;
    /**
     * 定时任务处理赎回订单时间
     */
    private Date payDate;
    /**
     * 时间戳
     */
    private Date createDate;
    /**
     * 本金金额'
     */
    private BigDecimal baseAmount;
    /**
     * 手续费
     */
    private BigDecimal fee;
    private int status;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getPayDate() {
        return payDate;
    }

    public void setPayDate(Date payDate) {
        this.payDate = payDate;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public BigDecimal getBaseAmount() {
        return baseAmount;
    }

    public void setBaseAmount(BigDecimal baseAmount) {
        this.baseAmount = baseAmount;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }
}
