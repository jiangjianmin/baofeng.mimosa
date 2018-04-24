package com.guohuai.mmp.investor.referprofit;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.guohuai.ams.product.Product;

import lombok.extern.slf4j.Slf4j;
/**
 * 
 * @ClassName: ProfitDetailService
 * @Description: 二级邀请--奖励收益明细业务
 * @author yihonglei
 * @date 2017年6月13日 下午4:28:37
 * @version 1.0.0
 */
@Service
@Slf4j
public class ProfitDetailService {
	
	@Autowired
	private ProfitDetailDao profitDetailDao;
	/** 二级邀请关系有效开始时间 */
	@Value("${secondLevelInvestStartDate}")
	private String secondLevelInvestStartDate;
	/**
	 * 
	 * @author yihonglei
	 * @Title: initProductProfitDetail
	 * @Description: 生成定期奖励收益明细
	 * @param productOid
	 * @return int
	 * @date 2017年6月13日 下午4:12:14
	 * @since  1.0.0
	 */
	@Transactional(value=Transactional.TxType.REQUIRES_NEW)
	public int initTnProductProfitDetail(String productOid) {
		log.info("<------生成定期二级邀请奖励收益明细开始,productOid:{}------>", productOid);
		int result = this.profitDetailDao.initTnProductProfitDetail(productOid, secondLevelInvestStartDate);
		log.info("<------生成定期二级邀请奖励收益明细结束,结果为:{}------>", result>0);
		return result;
	}
	/**
	 * 
	 * @author yihonglei
	 * @Title: initT0ProductProfitDetail
	 * @Description: 生成上月活期奖励收益明细
	 * @param product
	 * @return int
	 * @date 2017年6月13日 下午9:57:59
	 * @since  1.0.0
	 */
	@Transactional(value=Transactional.TxType.REQUIRES_NEW)
	public int initT0ProductProfitDetail(Product product, String lastYearMonth) {
		log.info("<------生成上月活期奖励收益明细开始,productOid:{}------>", product.getOid());
		int result = this.profitDetailDao.initT0ProductProfitDetail(product.getOid(), lastYearMonth, secondLevelInvestStartDate);
		log.info("<------生成上月活期奖励收益明细结束,结果为:{}------>", result > 0);
		return result;
	}
	/** 判断上月活期奖励收益明细是否已生成 */
	public boolean  checkT0ProductProfit(String lastYearMonth, String productOid) {
		int result = this.profitDetailDao.queryT0ProductProfitCount(lastYearMonth, productOid);
		log.info("{},活期奖励收益明细是否已生成:{}", lastYearMonth, result>0);
		return result>0;
	}

}
