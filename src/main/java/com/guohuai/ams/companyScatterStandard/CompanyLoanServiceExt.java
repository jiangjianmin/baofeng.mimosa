package com.guohuai.ams.companyScatterStandard;

import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductDao;
import com.guohuai.ams.product.ProductDurationService;
import com.guohuai.ams.productPackage.ProductPackage;
import com.guohuai.ams.productPackage.ProductPackageDao;
import com.guohuai.basic.message.DealMessageEntity;
import com.guohuai.basic.message.DealMessageEnum;
import com.guohuai.component.message.MessageSendUtil;
import com.guohuai.component.util.DateUtil;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderDao;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity;
import com.guohuai.mmp.publisher.baseaccount.statistics.PublisherStatisticsService;
import com.guohuai.mmp.schedule.cyclesplit.DayCutTradeOrderEMDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 中间表统计service
 *
 * @author yujianlong
 */
@Service
@Transactional
@Slf4j
public class CompanyLoanServiceExt {
	@Autowired
	private ProductDurationService productDurationService;
	@Autowired
	ProductDao productDao;
	@Autowired
	ProductPackageDao productPackageDao;
	@Autowired
	InvestorTradeOrderDao investorTradeOrderDao;
	@Autowired
	ElectronicSignatureRelationEmDao electronicSignatureRelationEmDao;
	@Autowired
	DayCutTradeOrderEMDao dayCutTradeOrderEMDao;
	@Autowired
	private PublisherStatisticsService publisherStatisticsService;
//
	@Autowired
	private MessageSendUtil messageSendUtil;
	@Autowired
	private ElectronicSignatureRelationDao electronicSignatureRelationDao;



	/**
	 *
	 *募集满额
	 * @author yujianlong
	 * @date 2018/4/23 14:31
	 * @param [orderTime, product, handleScatterOper]
	 * @return void
	 */
	@Transactional(value= Transactional.TxType.REQUIRES_NEW)
	public void raiseFullStepOne(Date orderTime, Product product, BoundHashOperations<String, Object, Object> handleScatterOper) {
		long start = System.currentTimeMillis();
		String productOid=product.getOid();
		String productCode = product.getCode();
		Date repayDate = DateUtil.addSQLDays(product.getDurationPeriodEndDate(), product.getAccrualRepayDays());
		ProductPackage productPackage = product.getProductPackage();
		//更新产品
		product.setRaiseEndDate(orderTime);
		product.setSetupDate(orderTime);
		product.setRepayDate(repayDate);
		product.setState(Product.STATE_Durationing);
		product.setDurationPeriodEndDate(DateUtil.addSQLDays(product.getSetupDate(),
				product.getDurationPeriodDays() - 1));// 存续期结束时间
		//更新产品包
		productPackage.setRaiseEndDate(orderTime);
		productPackage.setSetupDate(orderTime);
		productPackage.setRepayDate(repayDate);
		productPackage.setDurationPeriodEndDate(DateUtil.addSQLDays(product.getSetupDate(),
				product.getDurationPeriodDays() - 1));
		productDao.save(product);
		//定期产品进入募集期时，增加产品发行数量
		publisherStatisticsService.increaseReleasedProductAmount(product.getPublisherBaseAccount());
		productPackageDao.save(productPackage);
		//查找订单
		List<InvestorTradeOrderEntity> tradeOrderEntities = investorTradeOrderDao.getConfirmedOrderByProductOid(product.getOid());
		List<ElectronicSignatureRelation> ElectronicSignatureRelations = new ArrayList<>();
		Timestamp electronicSignatureRelationCreateTime = Timestamp.valueOf(LocalDateTime.now());
		//修改订单，增加订单电子签章关联实体
		AtomicInteger ai=new AtomicInteger(0);
		tradeOrderEntities.stream().forEach(trade->{
			trade.setBeginAccuralDate(orderTime);
			trade.setBeginRedeemDate(orderTime);
			String orderCode = trade.getOrderCode();
			ElectronicSignatureRelation electronicSignatureRelation = new ElectronicSignatureRelation();
			electronicSignatureRelation.setOrderCode(orderCode);
			electronicSignatureRelation.setContractCode(productCode+changeNumber(ai.incrementAndGet()+""));
			electronicSignatureRelation.setProductOid(product.getOid());
			electronicSignatureRelation.setCreateTime(electronicSignatureRelationCreateTime);
			electronicSignatureRelation.setUpdateTime(electronicSignatureRelationCreateTime);
			ElectronicSignatureRelations.add(electronicSignatureRelation);
		});
		//批量更新订单
		dayCutTradeOrderEMDao.batchUpdate(tradeOrderEntities);
		//批量插入订单电子签章关联实体
		electronicSignatureRelationEmDao.batchInsert(ElectronicSignatureRelations);
		//定期募集成立时创建生成二级邀请奖励收益明细的序列化任务
		productDurationService.tnProfitDetailSerial(product);
		//设置该产品已处理
		handleScatterOper.increment(product.getOid(), 1);
		//发送计息提醒
		tradeOrderEntities.stream().forEach(trade->{
			DealMessageEntity messageEntity1 = new DealMessageEntity();
			messageEntity1.setPhone(trade.getInvestorBaseAccount().getPhoneNum());
			messageEntity1.setOrderAmount(trade.getOrderAmount().setScale(2, BigDecimal.ROUND_DOWN).toString());
			messageEntity1.setOrderTime(trade.getOrderTime());
			messageEntity1.setProductName(trade.getProduct().getName());
			messageEntity1.setSettlementDate(product.getRepayDate());
			messageSendUtil.sendTimedTopicMessage(messageSendUtil.getDealTopic(), DealMessageEnum.PRODUCT_BEGIN.name(), messageEntity1);
		});
		long end = System.currentTimeMillis();
		log.info("募集满额触发耗时{},{}",(end-start),product);
	}

	/**
	 *保存电子签章 REQUIRES_NEW
	 *
	 * @author yujianlong
	 * @date 2018/4/23 14:41
	 * @param [loanContract, tradeInfo]
	 * @return void
	 */
	@Transactional(value= Transactional.TxType.REQUIRES_NEW)
	public void saveElectronicSignatureRelation(LoanContract loanContract,Map<String, Object> tradeInfo) {
		//企业名
		String orgName=loanContract.getOrgName();
		String productOid=Objects.toString(tradeInfo.get("productOid"));
		String orderCode = Objects.toString(tradeInfo.get("orderCode"));
		String contractCode = Objects.toString(tradeInfo.get("contractCode"));
		//身份证id
		String idNum = Objects.toString(tradeInfo.get("idNum"));
		//真实姓名
		String realName = Objects.toString(tradeInfo.get("realName"));
		ElectronicSignatureRelation electronicSignatureRelation = new ElectronicSignatureRelation();
		electronicSignatureRelation.setOrderCode(orderCode);
		electronicSignatureRelation.setProductOid(productOid);
		electronicSignatureRelation.setContractCode(contractCode);
			//TODO 生成电子签章和存证
			//电子签章地址
//			electronicSignatureRelation.setElectronicSignatureUrl();
			//存证地址
//			electronicSignatureRelation.setEvidenceUrl();

		electronicSignatureRelationDao.saveAndFlush(electronicSignatureRelation);
	}

	/**
	 *
	 *生成合同编号001-200之间
	 * @author yujianlong
	 * @date 2018/4/23 16:21
	 * @param [num]
	 * @return java.lang.String
	 */
	public static String changeNumber(String num){
		char[] ary1 = num.toCharArray();
		char[] ary2 = {'0','0','0'};
		System.arraycopy(ary1, 0, ary2, ary2.length-ary1.length, ary1.length);
		String result = new String(ary2);
		return result;
	}

}
