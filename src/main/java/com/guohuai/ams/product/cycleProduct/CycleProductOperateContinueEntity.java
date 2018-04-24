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
@Table(name = "T_MONEY_CYCLE_PRODUCT_CONTINUE_LIST")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Builder
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
public class CycleProductOperateContinueEntity implements Serializable{

    private static final long serialVersionUID = 6827911983529551545L;

    @Id
    private String investorOid;
    private Date operateDate;
    private BigDecimal orderAmount;
}
