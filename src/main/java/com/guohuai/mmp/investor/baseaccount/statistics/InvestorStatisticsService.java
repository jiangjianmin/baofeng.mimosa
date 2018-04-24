package com.guohuai.mmp.investor.baseaccount.statistics;


import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import com.guohuai.ams.product.ProductDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.ams.acct.account.AccountDao;

import com.guohuai.ams.label.LabelEnum;
import com.guohuai.ams.label.LabelService;
import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.reward.ProductIncomeRewardCacheService;
import com.guohuai.ams.productLabel.ProductLabelService;
import com.guohuai.cache.entity.HoldCacheEntity;
import com.guohuai.cache.entity.ProductCacheEntity;
import com.guohuai.cache.service.CacheHoldService;
import com.guohuai.cache.service.CacheProductService;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.mmp.investor.bankorder.InvestorBankOrderEntity;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountEntity;
import com.guohuai.mmp.investor.fund.YiLuService;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderService;
import com.guohuai.mmp.publisher.hold.PublisherHoldEntity;
import com.guohuai.mmp.publisher.hold.PublisherHoldService;


@Service
@Transactional
public class InvestorStatisticsService {
	
	@Autowired
	private InvestorStatisticsDao investorStatisticsDao;
	@Autowired
	private PublisherHoldService publisherHoldService;
	@Autowired
	private CacheHoldService cacheHoldService;
	@Autowired
	private CacheProductService cacheProductService;
	@Autowired
	private ProductIncomeRewardCacheService incomeRewardCacheService;
	@Autowired
	private InvestorTradeOrderService tradeOrderService;
	@Autowired
	private ProductLabelService productLabelService;
	@Autowired
	private LabelService labelService;
	@Autowired
	private AccountDao accountDao;
	@Autowired
	private YiLuService yiLuService;
	@Autowired
	private ProductDao productDao;
	/**
	 * 充值 
	 * 更新<<投资人-基本账户-统计>>.<<累计充值总额>><<累计充值次数>><<当日充值次数>> 
	 * @param {@link InvestorBankOrderEntity}
	 * @return int
	 */
	public int updateStatistics4Deposit(InvestorBankOrderEntity bankOrder) {
		return investorStatisticsDao.updateStatistics4Deposit(bankOrder.getInvestorBaseAccount(), bankOrder.getOrderAmount());
	}
	
	/**
	 * 投资人提现回调
	 * 更新<<投资人-基本账户-统计>>.<<累计提现总额>><<累计提现次数>><<当日提现次数>>
	 * @param {@link InvestorBankOrderEntity}
	 * @return int
	 */
	public int updateStatistics4Withdraw(InvestorBankOrderEntity bankOrder) {
		return investorStatisticsDao.updateStatistics4Withdraw(bankOrder.getInvestorBaseAccount(), bankOrder.getOrderAmount());
	}
	
	
	
	public InvestorStatisticsEntity findByInvestorBaseAccount(InvestorBaseAccountEntity baseAccount) {
		InvestorStatisticsEntity entity = investorStatisticsDao.findByInvestorBaseAccount(baseAccount);
		if (null == entity) {
			// error.define[30058]=投资人统计账户不存在(CODE:30058)
			throw new AMPException(30058);
		}
		return entity;
	}

	/**
	 * 新增
	 * @param entity
	 * @return
	 */
	public InvestorStatisticsEntity saveEntity(InvestorStatisticsEntity entity){
		entity.setCreateTime(DateUtil.getSqlCurrentDate());
		return this.updateEntity(entity);
	}
	
	private InvestorStatisticsEntity updateEntity(InvestorStatisticsEntity entity) {
		entity.setUpdateTime(DateUtil.getSqlCurrentDate());
		return this.investorStatisticsDao.save(entity);
	}

	/**
	 * 我的
	 */
	public MyHomeQueryRep myHomeInfo(String investorOid) {
		MyHomeQueryRep rep = new MyHomeQueryRep();

		List<HoldCacheEntity> holds = this.cacheHoldService.findByInvestorOid(investorOid);
		Date incomeDate = DateUtil.getBeforeDate();
		rep.setToAcceptedAmount(cacheHoldService.getToAcceptedAmount(investorOid));
		rep.setCapitalAmount(rep.getCapitalAmount().add(rep.getToAcceptedAmount()));
		for (HoldCacheEntity hold : holds) {
			ProductCacheEntity cacheProduct = this.cacheProductService.getProductCacheEntityById(hold.getProductOid());
			if (Product.TYPE_Producttype_01.equals(cacheProduct.getType())) {
				Product product=this.productDao.findOne(cacheProduct.getProductOid());
				if(product.getIsP2PAssetPackage().equals(Product.IS_P2P_ASSET_PACKAGE_2)){
					rep.setScatterCapitalAmount(rep.getScatterCapitalAmount().add(hold.getTotalVolume()));//企业散标
				}else{
					rep.setTnCapitalAmount(rep.getTnCapitalAmount().add(hold.getTotalVolume())); //定期总资产
				}
			} else if (Product.TYPE_Producttype_03.equals(cacheProduct.getType())){	//快定宝
				rep.setKdbCapitalAmount(rep.getKdbCapitalAmount().add(hold.getTotalVolume()));
			} else if (Product.TYPE_Producttype_04.equals(cacheProduct.getType())){	//快定宝
				rep.setKdbCapitalAmount(rep.getKdbCapitalAmount().add(hold.getTotalVolume()));
			}else if(incomeRewardCacheService.hasRewardIncome(hold.getProductOid())) {
				rep.setJjgCapitalAmount(rep.getJjgCapitalAmount().add(hold.getTotalVolume())); //活期总资产
			} else {
				rep.setT0CapitalAmount(rep.getT0CapitalAmount().add(hold.getTotalVolume())); //活期总资产
			}
			
			
			
//			if(map.get("fundWealth")!=null) {
//				runAmount = fundWealthAmount.subtract(new BigDecimal(map.get("fundWealth").toString()));
//			}
			rep.setCapitalAmount(rep.getCapitalAmount().add(hold.getTotalVolume())); //总资产
			
			//查询正在申请中的基金
//			Map<String,Object> maping = yiLuService.fundTrading(yiluoid);
//			List<HashMap<String,Object>> list = (List<HashMap<String,Object>>) maping.get("fundTradingList");
//			BigDecimal bd = new BigDecimal(0);
//			if(list!=null) {
//				for(HashMap<String,Object> m : list) {
//					bd = bd.add(new BigDecimal(m.get("applicationamount").toString()));
//				}
//			}
			rep.setTotalIncomeAmount(rep.getTotalIncomeAmount().add(hold.getHoldTotalIncome())); //累计收益
			if (null != hold.getConfirmDate() && DateUtil.daysBetween(incomeDate, hold.getConfirmDate()) == 0) {
				rep.setT0YesterdayIncome(rep.getT0YesterdayIncome().add(hold.getHoldYesterdayIncome())); //昨日收益
			}
		}
		//查询用户基金总额
		//1.根据investorOid查询用户在第三方uid
		List<String> list = accountDao.selectYiLuOid(investorOid);
		String yiluoid = null;
		Map<String,Object> map = new HashMap<String,Object>();
		if(null!=list&&list.size()>0) {
			yiluoid = list.get(0);
			map = yiLuService.myFund(yiluoid);
		}
		//String phone = accountDao.selectFourElement(yiluoid).get(0)[3].toString();
		//2.通过uid到一路查询用户基金
		BigDecimal fundWealthAmount = new BigDecimal(0);
		BigDecimal fundWealthAmountAll = new BigDecimal(0);
		if(map.get("fundWealth")!=null) {
			fundWealthAmount = new BigDecimal(map.get("fundWealth").toString());
		}
		if(map.get("fundWealthAll")!=null) {
			fundWealthAmountAll = new BigDecimal(map.get("fundWealthAll").toString());
		}
		rep.setFundCapitalAmountAll(fundWealthAmountAll);
		//总资产加入基金总额以及设置基金总额值
		if(fundWealthAmount!=null) {
			rep.setCapitalAmount(rep.getCapitalAmount().add(fundWealthAmount));
			rep.setFundCapitalAmount(fundWealthAmount);
		}
		return rep;
	}
	
	
	/**
	 * 我的资产
	 */
	public MyCaptialQueryRep myCaptial(String investorOid) {

		MyCaptialQueryRep rep = new MyCaptialQueryRep();

		List<HoldCacheEntity> holds = cacheHoldService.findByInvestorOid(investorOid);
		List<CapitalDetail> t0CapitalDetails = new ArrayList<CapitalDetail>();
		List<CapitalDetail> tnCapitalDetails = new ArrayList<CapitalDetail>();
		List<CapitalDetail> applyCapitalDetails = new ArrayList<CapitalDetail>();
		rep.setT0CapitalDetails(t0CapitalDetails);
		rep.setTnCapitalDetails(tnCapitalDetails);
		rep.setApplyCapitalDetails(applyCapitalDetails);
		
		rep.setToAcceptedAmount(cacheHoldService.getToAcceptedAmount(investorOid));
		rep.setCapitalAmount(rep.getCapitalAmount().add(rep.getToAcceptedAmount()));

		for (HoldCacheEntity hold : holds) {
			if (PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_closed.equals(hold.getHoldStatus())
					|| PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_refunded.equals(hold.getHoldStatus())) {
				continue;
			}
			
			ProductCacheEntity product = cacheProductService.getProductCacheEntityById(hold.getProductOid());
			rep.setCapitalAmount(rep.getCapitalAmount().add(hold.getTotalVolume())); // 总资产
			if (Product.TYPE_Producttype_02.equals(product.getType())) {
				CapitalDetail detail = new CapitalDetail();
				detail.setProductName(product.getName());
				detail.setAmount(hold.getHoldVolume().subtract(hold.getExpGoldVolume()));
				t0CapitalDetails.add(detail);
				rep.setT0CapitalAmount(rep.getT0CapitalAmount().add(hold.getHoldVolume().subtract(hold.getExpGoldVolume()))); // 活期总资产
				rep.setExperienceCouponAmount(rep.getExperienceCouponAmount().add(hold.getExpGoldVolume())); // 体验金总资产
			}
			if (Product.TYPE_Producttype_01.equals(product.getType())) {
				CapitalDetail detail = new CapitalDetail();
				detail.setProductName(product.getName());
				detail.setAmount(hold.getHoldVolume().subtract(hold.getExpGoldVolume()));
				tnCapitalDetails.add(detail);
				rep.setTnCapitalAmount(rep.getTnCapitalAmount().add(hold.getHoldVolume().subtract(hold.getExpGoldVolume()))); // 定期总资产
			}
			if (BigDecimal.ZERO.compareTo(hold.getToConfirmInvestVolume()) != 0) {
				CapitalDetail applyDetail = new CapitalDetail();
				applyDetail.setProductName(product.getName());
				applyDetail.setAmount(hold.getToConfirmInvestVolume());
				applyCapitalDetails.add(applyDetail);
				rep.setApplyAmt(rep.getApplyAmt().add(hold.getToConfirmInvestVolume())); // 申请中资产
			}
		}
		return rep;
	}
	
	/**
	 * 我的资产
	 */
	public MyCaptialNewRep myCaptialNew(String investorOid) {

		MyCaptialNewRep rep = new MyCaptialNewRep();
		//快活宝
		CapitalRow t0CapitalRow = new CapitalRow("快活宝(元)", "#ffab7a");
		//节节高
		CapitalRow jjgCapitalRow = new CapitalRow("暴风天天向上(元)", "#ffdac4");
		//智盈
		CapitalRow tnCapitalRow = new CapitalRow("智盈(元)", "#99cef2");
		//体验金
		CapitalRow expGoldCapitalRow = new CapitalRow("体验金(元)", "#9bdee0");
		//在途
		CapitalRow onwayCapitalRow = new CapitalRow("在途(元)", "#ffd7f2");

		//基金
		CapitalRow fundPlusCapitalRow = new CapitalRow("基金(元)","#a3adf6");

		//智盈15D
		CapitalRow bfPlusCapitalRow = new CapitalRow("智盈15D(元)","#9bdee0");

		//散标
		CapitalRow looseProductCapitalRow = new CapitalRow("散标(元)","#f98e8c");
		
		CapitalDetail investOnwayCapitalDetail = new CapitalDetail("含转入中");
		CapitalDetail redeemOnwayCapitalDetail = new CapitalDetail("含转出中");
		CapitalDetail applyT0CapitalDetail = new CapitalDetail("含申请中");
		CapitalDetail expGoldCapitalDetail = new CapitalDetail("含体验金收益");
		CapitalDetail applyJjgCapitalDetail = new CapitalDetail("含申请中");
		CapitalDetail raisingTnCapitalDetail = new CapitalDetail("含募集中");
		//快定宝 含申请中
		CapitalDetail applyBfPlusCapitalDetail = new CapitalDetail("含申请中");
		//基金 含申请中
		//CapitalDetail fundPlusCapitalDetail = new CapitalDetail("含确认中");
		fillCapitalRow(investorOid, rep, t0CapitalRow, jjgCapitalRow, tnCapitalRow, expGoldCapitalRow, onwayCapitalRow, bfPlusCapitalRow,fundPlusCapitalRow,looseProductCapitalRow,
				investOnwayCapitalDetail, redeemOnwayCapitalDetail, applyT0CapitalDetail, expGoldCapitalDetail,
				applyJjgCapitalDetail, raisingTnCapitalDetail, applyBfPlusCapitalDetail);

		sortCapitalRow(rep, t0CapitalRow, jjgCapitalRow, tnCapitalRow, expGoldCapitalRow, onwayCapitalRow, bfPlusCapitalRow,fundPlusCapitalRow,looseProductCapitalRow,
				investOnwayCapitalDetail, redeemOnwayCapitalDetail, applyT0CapitalDetail, expGoldCapitalDetail,
				applyJjgCapitalDetail, raisingTnCapitalDetail, applyBfPlusCapitalDetail);
		return rep;
	}

	private void sortCapitalRow(MyCaptialNewRep rep, CapitalRow t0CapitalRow, CapitalRow jjgCapitalRow,
								CapitalRow tnCapitalRow, CapitalRow expGoldCapitalRow, CapitalRow onwayCapitalRow,
								CapitalRow bfPlusCapitalRow, CapitalRow fundPlusCapitalRow,CapitalRow looseProductCapitalRow,CapitalDetail investOnwayCapitalDetail, CapitalDetail redeemOnwayCapitalDetail,
								CapitalDetail applyT0CapitalDetail, CapitalDetail expGoldCapitalDetail, CapitalDetail applyJjgCapitalDetail,
								CapitalDetail raisingTnCapitalDetail, CapitalDetail applyBfPlusCapitalDetail) {
		t0CapitalRow.getCapitalDetails().add(applyT0CapitalDetail);
		if(expGoldCapitalDetail.getAmount().compareTo(BigDecimal.ZERO) > 0) {
			t0CapitalRow.getCapitalDetails().add(expGoldCapitalDetail);
		}
		rep.getCapitalList().add(t0CapitalRow);
		jjgCapitalRow.getCapitalDetails().add(applyJjgCapitalDetail);
		rep.getCapitalList().add(jjgCapitalRow);
		//增加快定宝
		bfPlusCapitalRow.getCapitalDetails().add(applyBfPlusCapitalDetail);
		rep.getCapitalList().add(bfPlusCapitalRow);
		tnCapitalRow.getCapitalDetails().add(raisingTnCapitalDetail);
		rep.getCapitalList().add(tnCapitalRow);
//		//增加基金
//		fundPlusCapitalRow.getCapitalDetails().add(fundPlusCapitalDetail);
		rep.getCapitalList().add(fundPlusCapitalRow);
		if(expGoldCapitalRow.getCapitalAmount().compareTo(BigDecimal.ZERO) > 0) {
			rep.getCapitalList().add(expGoldCapitalRow);
		}
		//增加企业散标
		rep.getCapitalList().add(looseProductCapitalRow);
		onwayCapitalRow.getCapitalDetails().add(investOnwayCapitalDetail);
		onwayCapitalRow.getCapitalDetails().add(redeemOnwayCapitalDetail);
		rep.getCapitalList().add(onwayCapitalRow);
	}

	private void fillCapitalRow(String investorOid, MyCaptialNewRep rep, CapitalRow t0CapitalRow,
								CapitalRow jjgCapitalRow, CapitalRow tnCapitalRow, CapitalRow expGoldCapitalRow, CapitalRow onwayCapitalRow,
								CapitalRow bfPlusCapitalRow, CapitalRow fundPlusCapitalRow,CapitalRow looseProductCapitalRow,CapitalDetail investOnwayCapitalDetail, CapitalDetail redeemOnwayCapitalDetail,
								CapitalDetail applyT0CapitalDetail, CapitalDetail expGoldCapitalDetail, CapitalDetail applyJjgCapitalDetail,
								CapitalDetail raisingTnCapitalDetail, CapitalDetail applyBfPlusCapitalDetail) {
		List<InvestorTradeOrderEntity> onwayTradeOrderList = tradeOrderService.findOnwayList(investorOid);
		BigDecimal investAmount = BigDecimal.ZERO;
		BigDecimal redeemAmount = BigDecimal.ZERO;
		for(InvestorTradeOrderEntity tradeOrder : onwayTradeOrderList) {
			if(InvestorTradeOrderEntity.TRADEORDER_orderType_invest.equals(tradeOrder.getOrderType())) {
				investAmount = investAmount.add(tradeOrder.getOrderAmount());
			} else {
				redeemAmount = redeemAmount.add(tradeOrder.getOrderAmount());
			}
		}
		investOnwayCapitalDetail.setAmount(investAmount);
		redeemOnwayCapitalDetail.setAmount(redeemAmount);
		onwayCapitalRow.setCapitalAmount(investAmount.add(redeemAmount));
		rep.setTotalCapitalAmount(rep.getTotalCapitalAmount().add(onwayCapitalRow.getCapitalAmount()));
		
		List<HoldCacheEntity> holds = cacheHoldService.findByInvestorOid(investorOid);
		for (HoldCacheEntity hold : holds) {
			if (PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_closed.equals(hold.getHoldStatus())
					|| PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_refunded.equals(hold.getHoldStatus())) {
				continue;
			}
			ProductCacheEntity product = cacheProductService.getProductCacheEntityById(hold.getProductOid());
			
			if (Product.TYPE_Producttype_02.equals(product.getType())) {
				rep.setTotalCapitalAmount(rep.getTotalCapitalAmount().add(hold.getRedeemableHoldVolume()).add(hold.getToConfirmInvestVolume())); // 总资产
				Product productEntity = new Product();
				productEntity.setOid(hold.getProductOid());
				String label = productLabelService.findLabelByProduct(productEntity);
				if (labelService.isProductLabelHasAppointLabel(label, LabelEnum.tiyanjin.toString())) {
					expGoldCapitalDetail.setAmount(expGoldCapitalDetail.getAmount().add(hold.getHoldVolume().subtract(hold.getExpGoldVolume())));
					t0CapitalRow.setCapitalAmount(t0CapitalRow.getCapitalAmount().add(hold.getHoldVolume().subtract(hold.getExpGoldVolume())));
					expGoldCapitalRow.setCapitalAmount(expGoldCapitalRow.getCapitalAmount().add(hold.getExpGoldVolume()));
					rep.setTotalCapitalAmount(rep.getTotalCapitalAmount().add(hold.getHoldVolume().subtract(hold.getRedeemableHoldVolume())));
				} else if(incomeRewardCacheService.hasRewardIncome(hold.getProductOid())) {
					jjgCapitalRow.setCapitalAmount(jjgCapitalRow.getCapitalAmount().add(hold.getRedeemableHoldVolume().subtract(hold.getExpGoldVolume())).add(hold.getToConfirmInvestVolume()));
					applyJjgCapitalDetail.setAmount(applyJjgCapitalDetail.getAmount().add(hold.getToConfirmInvestVolume()));
				} else if (!labelService.isProductLabelHasAppointLabel(label, LabelEnum.tiyanjin.toString())) {
					t0CapitalRow.setCapitalAmount(t0CapitalRow.getCapitalAmount().add(hold.getRedeemableHoldVolume().subtract(hold.getExpGoldVolume())).add(hold.getToConfirmInvestVolume()));
					applyT0CapitalDetail.setAmount(applyT0CapitalDetail.getAmount().add(hold.getToConfirmInvestVolume()));
				}
			} else if (Product.TYPE_Producttype_01.equals(product.getType())) {
				Product productDetail=this.productDao.findOne(product.getProductOid());
				rep.setTotalCapitalAmount(rep.getTotalCapitalAmount().add(hold.getTotalVolume())); // 总资产
				if(productDetail.getIsP2PAssetPackage().equals(Product.IS_P2P_ASSET_PACKAGE_2)){//散标定期
					looseProductCapitalRow.setCapitalAmount(looseProductCapitalRow.getCapitalAmount().add(hold.getHoldVolume()));
				}else{
					tnCapitalRow.setCapitalAmount(tnCapitalRow.getCapitalAmount().add(hold.getHoldVolume().subtract(hold.getExpGoldVolume())));
					if(Product.STATE_Raising.equals(product.getState())) {
						raisingTnCapitalDetail.setAmount(raisingTnCapitalDetail.getAmount().add(hold.getHoldVolume().subtract(hold.getExpGoldVolume())));
					}
				}
			} else if (Product.TYPE_Producttype_03.equals(product.getType())){	//快定宝
				rep.setTotalCapitalAmount(rep.getTotalCapitalAmount().add(hold.getTotalVolume())); // 总资产
				bfPlusCapitalRow.setCapitalAmount(bfPlusCapitalRow.getCapitalAmount().add(hold.getRedeemableHoldVolume().subtract(hold.getExpGoldVolume())).add(hold.getToConfirmInvestVolume()));
				if(PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_toConfirm.equals(hold.getHoldStatus()) || PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_holding.equals(hold.getHoldStatus())) {
					applyBfPlusCapitalDetail.setAmount(applyBfPlusCapitalDetail.getAmount().add(hold.getToConfirmInvestVolume().subtract(hold.getExpGoldVolume())));
				}
			} else if (Product.TYPE_Producttype_04.equals(product.getType())){	//快定宝，已经日切了
				rep.setTotalCapitalAmount(rep.getTotalCapitalAmount().add(hold.getTotalVolume())); // 总资产
				bfPlusCapitalRow.setCapitalAmount(bfPlusCapitalRow.getCapitalAmount().add(hold.getRedeemableHoldVolume().subtract(hold.getExpGoldVolume())).add(hold.getToConfirmInvestVolume()));
			}
			
		}
		//增加基金
		//查询用户基金总额
		//1.根据investorOid查询用户在第三方uid
		List<String> list = accountDao.selectYiLuOid(investorOid);
		String yiluoid = null;
		Map<String,Object> map = new HashMap<String,Object>();
		if(null!=list&&list.size()>0) {
			yiluoid = list.get(0);
//			map = yiLuService.myFund(yiluoid);
		}
		BigDecimal fundWealthAmount = new BigDecimal(0);
		//BigDecimal runAmount = new BigDecimal(0);
		if(map.get("fundWealth")!=null) {
			fundWealthAmount = new BigDecimal(map.get("fundWealth").toString());
		}
//		if(map.get("fundWealth")!=null) {
//			runAmount = fundWealthAmount.subtract(new BigDecimal(map.get("fundWealth").toString()));
//		}
//		//查询正在申请中的基金
//		Map<String,Object> maping = yiLuService.fundTrading(yiluoid);
//		List<HashMap<String,Object>> list = (List<HashMap<String,Object>>) maping.get("fundTradingList");
//		BigDecimal bd = new BigDecimal(0);
//		if(list!=null) {
//			for(HashMap<String,Object> m : list) {
//				bd = bd.add(new BigDecimal(m.get("applicationamount").toString()));
//			}
//		}
		rep.setTotalCapitalAmount(rep.getTotalCapitalAmount().add(fundWealthAmount));
		//fundPlusCapitalDetail.setAmount(runAmount);
		fundPlusCapitalRow.setCapitalAmount(fundWealthAmount);
	}
	
	public boolean checkTodayWithdrawCount(InvestorBaseAccountEntity baseAccount, int wdFreeTimes) {
		int i = this.investorStatisticsDao.checkTodayWithdrawCount(baseAccount, wdFreeTimes);
		if (i < 1) {
			return false;
		}
		return true;
	}
	
	public int interestStatistics(InvestorBaseAccountEntity investorBaseAccount, BigDecimal holdIncomeAmount, BigDecimal holdLockIncomeAmount, Date incomeDate) {
		int i = this.investorStatisticsDao.interestStatistics(investorBaseAccount, holdIncomeAmount, holdLockIncomeAmount, incomeDate);
		return i;
		
	}
	
	public int interestStatisticsTn(InvestorBaseAccountEntity investorBaseAccount, BigDecimal holdIncomeAmount, BigDecimal holdLockIncomeAmount, Date incomeDate) {
		int i = this.investorStatisticsDao.interestStatisticsTn(investorBaseAccount, holdIncomeAmount, holdLockIncomeAmount, incomeDate);
		return i;
		
	}
	
	public int  resetToday() {
		int i = this.investorStatisticsDao.resetToday();
		return i;
	}
	
	public int  resetToday(String investorStatisticsOid) {
		int i = this.investorStatisticsDao.resetToday(investorStatisticsOid);
		return i;
	}
	
	/**
	 * 更新<<投资人-基本账户-统计>>.<<累计赎回总额>><<累计赎回次数>><<当日赎回次数>> <<活期资产总额>>或<
	 * <定期资产总额>>
	 */
	public int redeemStatistics(InvestorTradeOrderEntity order) {
		int i = this.investorStatisticsDao.updateStatistics4T0Redeem(order.getInvestorBaseAccount(), order.getOrderAmount());
		return i;
	}
	
	public int repayLoanStatistics(InvestorBaseAccountEntity baseAccount, BigDecimal orderAmount) {
		int i= this.investorStatisticsDao.updateStatistics4TnRepayLoan(baseAccount, orderAmount);
		return i;
	}
	
	public int repayInterestStatistics(InvestorBaseAccountEntity baseAccount, BigDecimal orderAmount) {
		int i = this.investorStatisticsDao.updateStatistics4TnRepayInterest(baseAccount, orderAmount);
		return i;
	}
	
	public int investStatistics(InvestorTradeOrderEntity tradeOrder, InvestorBaseAccountEntity baseAccount) {
		
		int totalInvestProducts = this.publisherHoldService.queryTotalInvestProductsByinvestorBaseAccount(baseAccount.getOid());
		int i;
		InvestorStatisticsEntity sta = this.findByInvestorBaseAccount(baseAccount);
		if (Product.TYPE_Producttype_02.equals(tradeOrder.getProduct().getType().getOid())) {
			if (null == sta.getFirstInvestTime()) {
				i = investorStatisticsDao.updateStatistics4T0InvestFtime(baseAccount, tradeOrder.getOrderAmount(), totalInvestProducts);
			} else {
				i = investorStatisticsDao.updateStatistics4T0Invest(baseAccount, tradeOrder.getOrderAmount(), totalInvestProducts);
			}
		} else {
			if (null == sta.getFirstInvestTime()) {
				i = investorStatisticsDao.updateStatistics4TnInvestFtime(baseAccount, tradeOrder.getOrderAmount(), totalInvestProducts);
			} else {
				i = investorStatisticsDao.updateStatistics4TnInvest(baseAccount, tradeOrder.getOrderAmount(), totalInvestProducts);
			}
		}
		
		return i; 
	}
	
	public List<InvestorStatisticsEntity> getResetTodayInvestorStatistics(String lastOid) {
		
		return this.investorStatisticsDao.getResetTodayInvestorStatistics(lastOid);
	}
	
}
