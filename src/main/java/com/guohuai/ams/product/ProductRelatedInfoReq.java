package com.guohuai.ams.product;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Desc: 产品相关请求参数
 * @author huyong
 * @date 2017.11.03
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ProductRelatedInfoReq {

	String oid;
	String type;
	BigDecimal raisedTotalNumber=BigDecimal.ZERO;
	BigDecimal cashPosition=BigDecimal.ZERO;
	BigDecimal scale=BigDecimal.ZERO;
	BigDecimal cashtoolFactRate=BigDecimal.ZERO;
	BigDecimal recPeriodExpAnYield=BigDecimal.ZERO;
	String state;
	BigDecimal expAror=BigDecimal.ZERO;
	BigDecimal collectedVolume=BigDecimal.ZERO;
	String incomeCalcBasis="0";
	Integer durationPeriodDays=0;
}
