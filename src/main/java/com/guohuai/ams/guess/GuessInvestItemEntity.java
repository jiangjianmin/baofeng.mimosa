package com.guohuai.ams.guess;


import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.ams.guess.GuessItemEntity.GuessItemEntityBuilder;
import com.guohuai.component.persist.UUID;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountEntity;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**用户竞猜活动投资选项
 * @author qiuliang
 *
 */
@Entity
@Table(name = "T_MONEY_GUESS_INVEST_ITEM")
@lombok.Builder
@lombok.Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
public class GuessInvestItemEntity extends UUID{
	
	private static final long serialVersionUID = 9053775114718329608L;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "orderOid", referencedColumnName = "oid")
	private InvestorTradeOrderEntity order;//对应订单
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "investOid", referencedColumnName = "oid")
	private InvestorBaseAccountEntity investor;//对应投资人
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "itemOid", referencedColumnName = "oid")
	private GuessItemEntity item;//对应选项

}
