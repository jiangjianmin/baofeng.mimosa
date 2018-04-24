package com.guohuai.moonBox.to;

import java.math.BigDecimal;
import java.util.Date;

import com.guohuai.mmp.sys.SysConstant;

import lombok.NoArgsConstructor;

@lombok.Data
@NoArgsConstructor
public class protocalAddReq extends BaseReq{
 private String protocalName;
 private BigDecimal protocalAmount = SysConstant.BIGDECIMAL_defaultValue;
 private String protocalDate;
 private String nextInvestDate;
}
