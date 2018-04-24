package com.guohuai.mmp.investor.tradeorder.p2p;


import com.guohuai.component.persist.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "T_P2P_ORDER_CREDITOR")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class P2PCreditorEntity extends UUID{

    private static final long serialVersionUID = 28011400370963713L;

    private String orderOid;

    private String contractId;

    private String name;

    private String idNumb;

    private String phone;

    private String gender;

    private int age;

    private BigDecimal loanAmount;

    private String loanUsage;

    private String loanDuration;

    private String refundMode;

}
