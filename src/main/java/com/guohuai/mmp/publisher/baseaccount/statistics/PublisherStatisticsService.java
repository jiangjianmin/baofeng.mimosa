package com.guohuai.mmp.publisher.baseaccount.statistics;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductDecimalFormat;
import com.guohuai.ams.product.ProductService;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.BigDecimalUtil;
import com.guohuai.component.util.DateUtil;
import com.guohuai.mmp.investor.bankorder.InvestorBankOrderEntity;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountEntity;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity;
import com.guohuai.mmp.platform.publisher.order.PublisherOrderEntity;
import com.guohuai.mmp.publisher.bankorder.PublisherBankOrderEntity;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountEntity;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountService;
import com.guohuai.mmp.publisher.baseaccount.loginacc.PublisherLoginAccService;
import com.guohuai.mmp.publisher.hold.PublisherHoldService;
import com.guohuai.mmp.publisher.product.statistics.PublisherProductStatisticsService;
import com.guohuai.mmp.sys.SysConstant;

@Service
@Transactional
public class PublisherStatisticsService {
	
	@Autowired
	PublisherStatisticsDao publisherStatisticsDao;
	@Autowired
	ProductService productService;
	@Autowired
	PublisherHoldService publisherHoldService;
	@Autowired
	PublisherProductStatisticsService publisherProductStatisticsService;
	@Autowired
	PublisherBaseAccountService publisherBaseAccountService;
	@Autowired
	PublisherLoginAccService publisherLoginAccService;
	
	private Logger logger = LoggerFactory.getLogger(PublisherStatisticsService.class);

	/**
	 * 充值
	 * @param {@link InvestorBankOrderEntity}
	 * @return int
	 */
	public int updateStatistics4Deposit(PublisherBankOrderEntity bankOrder) {
		
		return publisherStatisticsDao.updateStatistics4Deposit(bankOrder.getPublisherBaseAccount(), bankOrder.getOrderAmount());
	}
	
	/**
	 * 提现
	 * @param {@link InvestorBankOrderEntity}
	 * @return int
	 */
	public int updateStatistics4Withdraw(PublisherBankOrderEntity bankOrder) {
		return publisherStatisticsDao.updateStatistics4Withdraw(bankOrder.getPublisherBaseAccount(), bankOrder.getOrderAmount());
	}
	
	/**
	 * 还款
	 * @param order
	 * @return
	 */
	public int increaseTotalReturnAmount(InvestorTradeOrderEntity order) {
		return publisherStatisticsDao.increaseTotalReturnAmount(order.getPublisherBaseAccount(), order.getOrderAmount());
	}
	
	/**
	 * 借款
	 * @param order
	 * @return
	 */
	public int increaseTotalLoanAmount(InvestorTradeOrderEntity order) {
		return publisherStatisticsDao.increaseTotalLoanAmount(order.getPublisherBaseAccount(), order.getOrderAmount());
	}
	
	public PublisherStatisticsEntity findByPublisherBaseAccount(PublisherBaseAccountEntity baseAccount) {
		PublisherStatisticsEntity entity = publisherStatisticsDao.findByPublisherBaseAccount(baseAccount);
		if (null == entity) {
			throw new AMPException("发行人统计不存在");
		}
		return entity;
	}
	
	/**
	 * 重置相关今日统计数据
	 * @return
	 */
	public int resetToday() {
		int i = this.publisherStatisticsDao.resetToday();
		return i;
	}
	
	/**
	 * 定期产品进入募集期时，增加产品发行数量
	 * 活期产品进入存续期时
	 */
	public int increaseReleasedProductAmount(PublisherBaseAccountEntity baseAccount) {
		int i = this.publisherStatisticsDao.increaseReleasedProductAmount(baseAccount);
		return i;
	}
	
	/**
	 * 定期产品进入募集期时，增加在售产品数量
	 * 活期产品进入存续期时
	 */
	public int increaseOnSaleProductAmount(PublisherBaseAccountEntity baseAccount) {
		int i = this.publisherStatisticsDao.increaseOnSaleProductAmount(baseAccount);
		return i;
	}
	
	/**
	 * 定期产品存续期结束之后，增加待结算产品数量
	 * 活期产品发起清盘操作
	 */
	public int increaseToCloseProductAmount(PublisherBaseAccountEntity baseAccount) {
		int i = this.publisherStatisticsDao.increaseToCloseProductAmount(baseAccount);
		return i;
	}
	
	/**
	 * 定期产品发起还本付息之后/或定期产品募集失败,增加已结算产品数量
	 */
	public int increaseClosedProductAmount(PublisherBaseAccountEntity baseAccount) {
		int i = this.publisherStatisticsDao.increaseClosedProductAmount(baseAccount);
		return i;
	}

	public List<Object[]> queryByPublisherOid(String publisherOid) {
		return this.publisherStatisticsDao.queryByPublisherOid(publisherOid);
	}

	/** 发行人首页 */
	public PublisherHomeQueryRep publisherHome(String loginAcc) {
		
		logger.info("loginAcc:"+loginAcc);
		
		PublisherHomeQueryRep rep = new PublisherHomeQueryRep();

		//通过登录账户查询发行人oid
		PublisherBaseAccountEntity loginAccEntity = this.publisherLoginAccService.findByLoginAcc(loginAcc);
		if (loginAccEntity == null || StringUtils.isEmpty(loginAccEntity.getOid())) {
			return rep;
		}
		
		String publisherOid = loginAccEntity.getOid();
		logger.info("publisherOid:"+publisherOid);

		// -----------发行人统计信息-----------
		long start = System.currentTimeMillis();
		List<Object[]> list = queryByPublisherOid(publisherOid);
		logger.info("发行人统计信息耗时" + (System.currentTimeMillis() - start));
		if (list == null || list.size() == 0) {
			return rep;
		}
		
		Object[] statInfo = list.get(0);

		rep.setBalance(BigDecimalUtil.parseFromObject(statInfo[0]));// 账户余额
		rep.setTotalLoanAmount(BigDecimalUtil.parseFromObject(statInfo[1]));// 累计借款总额
		rep.setTotalReturnAmount(BigDecimalUtil.parseFromObject(statInfo[2]));// 累计还款总额
		if (!StringUtils.isEmpty(statInfo[3])) {
			rep.setInvestorCount(Integer.parseInt(statInfo[3].toString()));// 借款账号总数（总投资人数）
		}
		if (!StringUtils.isEmpty(statInfo[4])) {
			rep.setInvestorHoldCount(Integer.parseInt(statInfo[4].toString()));// 现借款账号总数（现持仓人数）
		}
		rep.setLoanAmountNow(rep.getTotalLoanAmount().subtract(rep.getTotalReturnAmount()));// 现借金额=累计借款总额-累计还款总额
		rep.setTotalDepositAmount(BigDecimalUtil.parseFromObject(statInfo[5]));// 累计充值总额
		rep.setTotalWithdrawAmount(BigDecimalUtil.parseFromObject(statInfo[6]));// 累计提现总额
		logger.info("处理耗时:" + (System.currentTimeMillis() - start));

		// -------------产品投资额TOP5
		long start2 = System.currentTimeMillis();
		// 昨日投资top5产品
		List<Object[]> top5EntityList = this.publisherProductStatisticsService
				.findTop5ByPublisherOidAndDate(publisherOid, DateUtil.getBeforeDate());
		logger.info("产品投资额TOP5耗时" + (System.currentTimeMillis() - start2));
		List<PublisherTop5ProRep> top5RepList = new ArrayList<PublisherTop5ProRep>();
		if (top5EntityList != null && top5EntityList.size() > 0) {
			for (Object[] arr : top5EntityList) {
				PublisherTop5ProRep top5Rep = new PublisherTop5ProRep();
				top5Rep.setProName(arr[0] == null ? "" : arr[0].toString());// 产品名称
				top5Rep.setInvestAmt(BigDecimalUtil.parseFromObject(arr[1]));// 投资金额
				top5RepList.add(top5Rep);
			}
		}
		rep.setTop5ProductList(top5RepList);
		logger.info("处理耗时:" + (System.currentTimeMillis() - start2));

		// -----------销售中产品募集进度-----------
		// 销售中产品信息
		long start3 = System.currentTimeMillis();
		List<Product> proList = this.productService.getProductByPublisherOidAndState(publisherOid,
				Product.STATE_Raising);
		logger.info("销售中产品募集进度耗时" + (System.currentTimeMillis() - start3));
		// 销售中产品募集进度
		List<PublisherRaiseRateRep> raiseList = new ArrayList<PublisherRaiseRateRep>();
		if (proList != null && proList.size() > 0) {
			for (Product product : proList) {
				PublisherRaiseRateRep raise = new PublisherRaiseRateRep();
				raise.setProName(product.getName());// 产品名称
				if (product.getRaisedTotalNumber() == null
						|| product.getRaisedTotalNumber().compareTo(SysConstant.BIGDECIMAL_defaultValue) == 0) {
					raise.setTotal(new BigDecimal("0.00"));// 总募集份额百分比
					raise.setRaised(new BigDecimal("0.00"));// 已募集份额百分比
					raise.setToRaised(new BigDecimal("0.00"));// 待募集百分比
				} else {
					raise.setTotal(new BigDecimal("100.00"));// 总募集份额百分比
					raise.setRaised(ProductDecimalFormat.multiply(product.getCollectedVolume()
							.divide(product.getRaisedTotalNumber(), 2, BigDecimal.ROUND_HALF_UP)));// 已募集份额百分比
					raise.setToRaised(new BigDecimal("100.00").subtract(raise.getRaised()));// 待募集百分比
				}

				raiseList.add(raise);
			}
			//按照已募集百分比升序显示（横向柱状图小的在下面，大的在上面）
			Collections.sort(raiseList,new Comparator<PublisherRaiseRateRep>() {
				@Override
				public int compare(PublisherRaiseRateRep o1, PublisherRaiseRateRep o2) {
					return o1.getRaised().compareTo(o2.getRaised());
				}
			});
		}
		rep.setRaiseRate(raiseList);
		logger.info("处理耗时:" + (System.currentTimeMillis() - start3));

		// -----------发行人下投资人质量分析-----------
		long start4 = System.currentTimeMillis();
		List<Object[]> invstorList = this.publisherHoldService.analyseInvestor(publisherOid);
		logger.info("发行人下投资人质量分析耗时" + (System.currentTimeMillis() - start4));
		List<PublisherInvestorAnalyseRep> analyseList = new ArrayList<PublisherInvestorAnalyseRep>();
		if (invstorList != null && invstorList.size() > 0) {
			// 取出各个范围的值
			for (Object[] objects : invstorList) {
				PublisherInvestorAnalyseRep an = new PublisherInvestorAnalyseRep();
				an.setScaleName(getLevelName(objects[0]));
				if (!StringUtils.isEmpty(objects[1])) {
					an.setScaleCount(Integer.parseInt(objects[1].toString()));
				}
				analyseList.add(an);
			}
		}
		rep.setInvestorAnalyse(analyseList);
		logger.info("处理耗时:" + (System.currentTimeMillis() - start4));

		logger.info("总耗时:" + (System.currentTimeMillis() - start));
		return rep;
	}

	private String getLevelName(Object level) {
		if (StringUtils.isEmpty(level)) {
			return "";
		}
		String levelStr = level.toString();
		switch (levelStr) {
		case "1":
			return "5万以下";
		case "2":
			return "5-10万";
		case "3":
			return "10-20万";
		case "4":
			return "20万以上";
		default:
			return "";
		}
	}
	
	/** 发行人统计--投资人数、持仓人数  */
	public void increaseInvestorAmount(InvestorBaseAccountEntity investorBaseAccount,
			PublisherBaseAccountEntity publisherBaseAccount) {
		int i = publisherHoldService.countByPublisherBaseAccountAndInvestorBaseAccount(investorBaseAccount, publisherBaseAccount);
		if (i == 1) {
			this.publisherStatisticsDao.increaseInvestorAmount(publisherBaseAccount);
		}
	}

	public PublisherStatisticsEntity saveEntity(PublisherStatisticsEntity st) {
		return this.publisherStatisticsDao.save(st);
		
	}

	public int increaseOverdueTimes(PublisherBaseAccountEntity publisherBaseAccount) {
		
		return this.publisherStatisticsDao.increaseOverdueTimes(publisherBaseAccount);
		
	}

	/**
	 * 累计付息总额
	 */
	public int increaseTotalInterestAmount(PublisherBaseAccountEntity publisherBaseAccount,
			BigDecimal successAllocateIncome) {
		return this.publisherStatisticsDao.increaseTotalInterestAmount(publisherBaseAccount, successAllocateIncome);
		
	}

	public List<Object[]> getPublisherStatisticsByBatch(String lastOid) {
		return publisherStatisticsDao.getPublisherStatisticsByBatch(lastOid);
	}
}
