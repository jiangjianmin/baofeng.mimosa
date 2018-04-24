package com.guohuai.mmp.investor.tradeorder.p2p;

import com.guohuai.component.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class P2PCreditorDetail {

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

    /**
     * 姓名加*处理
     * @param name
     */
    public void setName(String name) {
        if (StringUtil.isEmpty(name)) {
            return;
        }
        this.name = StringUtils.rightPad(name.substring(0, 1), 3, "*");
    }

    /**
     * 身份证加*处理
     * @param idNumb
     */
    public void setIdNumb(String idNumb) {
        if (StringUtil.isEmpty(idNumb) || idNumb.length() < 18) {
            return;
        }
        this.idNumb = StringUtils.rightPad(idNumb.substring(0, 1), 18, "*");
    }

    /**
     * 手机号加*处理
     * @param phone
     */
    public void setPhone(String phone) {
        if (StringUtil.isEmpty(phone) || phone.length() < 11) {
            return;
        }
        this.phone = StringUtils.rightPad(phone.substring(0, 2), 11, "*");
    }
}
