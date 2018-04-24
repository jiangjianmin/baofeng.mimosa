package com.guohuai.ams.product.cycleProduct;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

@Entity
@Table(name = "T_MONEY_CYCLE_PRODUCT_OPERATING_LIST")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@lombok.Builder
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
public class CycleProductOperateEntity implements Serializable{

    private static final long serialVersionUID = 6827911983529551545L;

    @Id
    private String orderCode;
    private String investorOid;
    private String holdOid;
    private Date operateDate;
    private BigDecimal orderAmount;
    private int status;

}
