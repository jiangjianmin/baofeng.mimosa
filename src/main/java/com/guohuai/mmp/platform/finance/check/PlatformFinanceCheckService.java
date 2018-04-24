package com.guohuai.mmp.platform.finance.check;


import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.guohuai.calendar.TradeCalendarService;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.component.web.view.PagesRep;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountDao;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountEntity;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderDao;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderRep;
import com.guohuai.mmp.job.lock.JobLockService;
import com.guohuai.mmp.job.log.JobLogService;
import com.guohuai.mmp.platform.StaticProperties;
import com.guohuai.mmp.platform.finance.data.PlatformFinanceCompareDataDao;
import com.guohuai.mmp.platform.finance.data.PlatformFinanceCompareDataEntity;
import com.guohuai.mmp.platform.finance.data.PlatformFinanceCompareDataNewService;
import com.guohuai.mmp.platform.finance.result.PlatformFinanceCompareDataResultEntity;
import com.guohuai.mmp.platform.finance.result.PlatformFinanceCompareDataResultNewService;
import com.guohuai.mmp.platform.finance.resultdetail.PlatformFinanceCompareDataResultDetailService;
import com.guohuai.mmp.serialtask.SerialTaskRequireNewService;
import com.guohuai.mmp.serialtask.SerialTaskService;

import lombok.extern.slf4j.Slf4j;


@Service
@Transactional
@Slf4j
public class PlatformFinanceCheckService {
	Logger logger = LoggerFactory.getLogger(PlatformFinanceCheckService.class);
	@Autowired
	private PlatformFinanceCheckNewService platformFinanceCheckNewService;
	@Autowired
	private PlatformFinanceCheckDao platformFinanceCheckDao;
	@Autowired
	private InvestorTradeOrderDao investorTradeOrderDao;
	@Autowired
	private PlatformFinanceCompareDataDao platformFinanceCompareDataDao;
	@Autowired
	private PlatformFinanceCompareDataNewService platformFinanceCompareDataNewService;
	@Autowired
	private PlatformFinanceCompareDataResultNewService platformFinanceCompareDataResultNewService;
	@Autowired
	private PlatformFinanceCompareDataResultDetailService platformFinanceCompareDataResultDetailService;
	@Autowired
	private JobLogService jobLogService;
	@Autowired
	private JobLockService jobLockService;
	@Autowired
	private SerialTaskService serialTaskService;
	@Autowired
	private SerialTaskRequireNewService serialTaskRequireNewService;
	@Autowired
	private TradeCalendarService tradeCalendarService;
	@Autowired
	private InvestorBaseAccountDao investorBaseAccountDao;
	
	public PagesRep<PlatformFinanceCheckRep> checkDataList(Specification<PlatformFinanceCheckEntity> spec,
			Pageable pageable) {
		Page<PlatformFinanceCheckEntity> cas = this.platformFinanceCheckDao.findAll(spec, pageable);
		PagesRep<PlatformFinanceCheckRep> pagesRep = new PagesRep<PlatformFinanceCheckRep>();
		for (PlatformFinanceCheckEntity entity : cas) {
			PlatformFinanceCheckRep rep = new PlatformFinanceCheckRep();
			rep.setOid(entity.getOid());
			rep.setCheckCode(entity.getCheckCode());
			rep.setCheckDate(entity.getCheckDate());
			rep.setCheckStatus(entity.getCheckStatus());
			rep.setTotalCount(entity.getTotalCount());
			rep.setWrongCount(entity.getWrongCount());
			rep.setConfirmStatus(entity.getConfirmStatus());
			rep.setCheckDataSyncStatus(entity.getCheckDataSyncStatus());
			rep.setOperator(entity.getOperator());
			rep.setCreateTime(entity.getCreateTime());
			rep.setUpdateTime(entity.getUpdateTime());
			pagesRep.add(rep);
		}
		pagesRep.setTotal(cas.getTotalElements());
		return pagesRep;
	}
	public BaseRep checkOrder(String checkOid, String checkDate,String operator){
		BaseRep rep = new BaseRep();
		try{
			PlatformFinanceCheckEntity pfcEntity=platformFinanceCheckNewService.findByCheckDate(DateUtil.parseToSqlDate(checkDate));
			Long count=platformFinanceCompareDataResultNewService.countByCheckOid(pfcEntity.getOid());
			if(count > 0){
				throw new AMPException("请先处理对账结果里处理状态为【处理中】的数据！");
			}
			//更新状态为对账中
			
			platformFinanceCheckNewService.checking(pfcEntity.getOid(),operator);
			new Thread(new Runnable() {
				@Override
				public void run() {
					checkOrderDo(checkOid,checkDate,operator);
				}
			}).start();
//			checkOrderDo(checkOid,checkDate,operator);
		}catch(AMPException e){
			e.printStackTrace();
			rep.setErrorCode(-1);
			rep.setErrorMessage(e.getMessage());
		}
		return rep;
	}
	
	public void isNeedCheck() {
		Date checkDate = StaticProperties.isIs24() ? DateUtil.getBeforeDate() : DateUtil.getSqlDate();
		boolean isTrade = tradeCalendarService.isTrade(checkDate);
		if (!isTrade) {
			throw new AMPException("非交易日不能对账");
		}
		// Date prevTradeDate = tradeCalendarService.getPrevTradeDate(checkDate);
	}
	
	public void checkOrderDo(String checkOid, String checkDate,String operator) {
		int errorCount=0;
		String orderCode="0";
		try{
			platformFinanceCompareDataResultNewService.deleteByCheckOid(checkOid);
			//对账处理
			List<PlatformFinanceCompareDataResultEntity> compareDataResultList=new ArrayList<PlatformFinanceCompareDataResultEntity>();
			PlatformFinanceCheckEntity chekEntity=platformFinanceCheckDao.findOne(checkOid);
			String beginTime=DateUtil.convertDate2String(DateUtil.fullDatePattern, chekEntity.getBeginTime());
			String endTime=DateUtil.convertDate2String(DateUtil.fullDatePattern, chekEntity.getEndTime());
			while (true) {
				List<InvestorTradeOrderRep> investorTradeOrderList = handlerArray2Obj(investorTradeOrderDao.findInvestorOrderByOrderTime(beginTime , endTime , orderCode));
				if (null == investorTradeOrderList || investorTradeOrderList.isEmpty()) {
					break;
				}
				//对账处理
				compareDataResultList=handlerCheckOrder(investorTradeOrderList,checkOid,checkDate);
				platformFinanceCompareDataResultNewService.save(compareDataResultList);
				platformFinanceCompareDataResultDetailService.save(compareDataResultList);
				for(PlatformFinanceCompareDataResultEntity entity : compareDataResultList){
					if(!entity.getCheckStatus().equals(PlatformFinanceCompareDataResultEntity.CHECKSTATUS_EQUALS)){
						errorCount++;
					}
				}
				InvestorTradeOrderRep e=investorTradeOrderList.get(investorTradeOrderList.size()-1);
				orderCode=e.getOrderCode();
			}
			Integer lessThenCount=handlerLessThenOrder(checkDate,checkOid);
			PlatformFinanceCheckEntity pfcEntity=platformFinanceCheckNewService.findByCheckDate(DateUtil.parseToSqlDate(checkDate));
			errorCount=errorCount+lessThenCount;
			pfcEntity.setWrongCount(errorCount);
			if(errorCount == 0){
				pfcEntity.setCheckStatus(PlatformFinanceCheckEntity.CHECKSTATUS_CHECKSUCCESS);
			}else{
				pfcEntity.setCheckStatus(PlatformFinanceCheckEntity.CHECKSTATUS_CHECKFAILED);
			}
			pfcEntity.setOperator(operator);
			pfcEntity.setUpdateTime(DateUtil.getSqlCurrentDate());
			platformFinanceCheckNewService.save(pfcEntity);
		}catch(Exception e){
			throw e;
		}
	}
	private List<InvestorTradeOrderRep> handlerArray2Obj(List<Object[]> findInvestorOrderByOrderTime) {
		List<InvestorTradeOrderRep> returnList=new ArrayList<InvestorTradeOrderRep>();
		InvestorTradeOrderRep rep=null;
		for(Object[] obj : findInvestorOrderByOrderTime){
			rep=new InvestorTradeOrderRep();
			rep.setMemberId(obj[4]+"");
			rep.setOrderAmount(new BigDecimal(obj[3]+""));
			rep.setOrderType(obj[2]+"");
			rep.setOrderStatus(obj[1]+"");
			rep.setOrderCode(obj[0]+"");
			returnList.add(rep);
		}
		return returnList;
	}
	/**
	 * 少帐处理--返回少帐记录数
	 * @param checkDate
	 * @return
	 */
	private Integer handlerLessThenOrder(String checkDate,String checkOid) {
		//对账处理
		List<PlatformFinanceCompareDataResultEntity> compareDataResultList=new ArrayList<PlatformFinanceCompareDataResultEntity>();
		List<PlatformFinanceCompareDataEntity> listCompareData=platformFinanceCompareDataDao.findByCheckDateAndCheckStatus(checkDate);
		PlatformFinanceCompareDataResultEntity compareDataResultEntity=null;
		for(PlatformFinanceCompareDataEntity cdEntity : listCompareData){
			compareDataResultEntity=new PlatformFinanceCompareDataResultEntity();
			compareDataResultEntity.setBuzzDate(DateUtil.parseToSqlDate(checkDate));
			compareDataResultEntity.setCheckInvestorOid(cdEntity.getInvestorOid());
			compareDataResultEntity.setCheckOid(checkOid);
			compareDataResultEntity.setCheckOrderAmount(cdEntity.getOrderAmount());
			compareDataResultEntity.setCheckOrderCode(cdEntity.getOrderCode());
			compareDataResultEntity.setCheckOrderStatus(cdEntity.getOrderStatus());
			compareDataResultEntity.setCheckOrderType(cdEntity.getOrderType());
			compareDataResultEntity.setCheckStatus(PlatformFinanceCompareDataResultEntity.CHECKSTATUS_LESSTHEN);
			compareDataResultEntity.setCreateTime(DateUtil.getSqlCurrentDate());
			compareDataResultEntity.setUpdateTime(DateUtil.getSqlCurrentDate());
			compareDataResultEntity.setDealStatus(PlatformFinanceCompareDataResultEntity.DEALSTATUS_TODEAL);
			compareDataResultList.add(compareDataResultEntity);
		}
		platformFinanceCompareDataResultNewService.save(compareDataResultList);
		platformFinanceCompareDataResultDetailService.save(compareDataResultList);
		return compareDataResultList.size();
	}
	/**
	 * 除少帐外的一致，多帐，异常等对账处理
	 * @param investorTradeOrderList
	 * @param checkOid
	 * @param checkDate
	 * @return
	 */
	private List<PlatformFinanceCompareDataResultEntity> handlerCheckOrder(
			List<InvestorTradeOrderRep> investorTradeOrderList, String checkOid, String checkDate) {

		List<PlatformFinanceCompareDataResultEntity> compareDataResultList = new ArrayList<PlatformFinanceCompareDataResultEntity>();

		List<PlatformFinanceCompareDataEntity> listCompareData = new ArrayList<PlatformFinanceCompareDataEntity>();
		for (InvestorTradeOrderRep itEntity : investorTradeOrderList) {
			PlatformFinanceCompareDataEntity compareDataEntity = platformFinanceCompareDataDao
					.findByOrderCode(itEntity.getOrderCode());

			PlatformFinanceCompareDataResultEntity compareDataResultEntity = initCompareDataResultEntity(checkOid,
					checkDate, itEntity);
			// 多帐
			if (null == compareDataEntity) {
				compareDataResultEntity.setCheckStatus(PlatformFinanceCompareDataResultEntity.CHECKSTATUS_MORETHEN);
			} else {
				boolean excpetionFlag = false;
				if (compareDataEntity.getInvestorOid().equals(itEntity.getMemberId())
						&& itEntity.getOrderAmount().compareTo(compareDataEntity.getOrderAmount()) == 0) {
					if (InvestorTradeOrderEntity.TRADEORDER_orderType_invest.equals(itEntity.getOrderType())) {
						if ("1".equals(compareDataEntity.getOrderStatus()) && 
								(itEntity.getOrderStatus().equals(InvestorTradeOrderEntity.TRADEORDER_orderStatus_accepted)
										||itEntity.getOrderStatus().equals(InvestorTradeOrderEntity.TRADEORDER_orderStatus_confirmed)
										||itEntity.getOrderStatus().equals(InvestorTradeOrderEntity.TRADEORDER_orderStatus_paySuccess))) {
							compareDataResultEntity
									.setCheckStatus(PlatformFinanceCompareDataResultEntity.CHECKSTATUS_EQUALS);
							compareDataResultEntity
									.setDealStatus(PlatformFinanceCompareDataResultEntity.DEALSTATUS_TODEAL);
							setProperties(compareDataResultEntity, compareDataEntity);
						} 
						else if ("2".equals(compareDataEntity.getOrderStatus()) && itEntity.getOrderStatus().equals(InvestorTradeOrderEntity.TRADEORDER_orderStatus_payFailed)) {
							compareDataResultEntity
									.setCheckStatus(PlatformFinanceCompareDataResultEntity.CHECKSTATUS_EQUALS);
							compareDataResultEntity
									.setDealStatus(PlatformFinanceCompareDataResultEntity.DEALSTATUS_TODEAL);
							setProperties(compareDataResultEntity, compareDataEntity);
						} 
						else {
							excpetionFlag = true;
						}
					}

					if (InvestorTradeOrderEntity.TRADEORDER_orderType_normalRedeem.equals(itEntity.getOrderType())
						|| InvestorTradeOrderEntity.TRADEORDER_orderType_incrementRedeem.equals(itEntity.getOrderType())) {
						if ("1".equals(compareDataEntity.getOrderStatus()) && itEntity.getOrderStatus()
								.equals(InvestorTradeOrderEntity.TRADEORDER_orderStatus_done)) {
							compareDataResultEntity
									.setCheckStatus(PlatformFinanceCompareDataResultEntity.CHECKSTATUS_EQUALS);
							compareDataResultEntity
									.setDealStatus(PlatformFinanceCompareDataResultEntity.DEALSTATUS_TODEAL);
							setProperties(compareDataResultEntity, compareDataEntity);
						} else {
							excpetionFlag = true;
						}
					} 
				} else {
					excpetionFlag = true;
				}
				if (excpetionFlag) {
					compareDataResultEntity
							.setCheckStatus(PlatformFinanceCompareDataResultEntity.CHECKSTATUS_EXCEPTION);
					setProperties(compareDataResultEntity, compareDataEntity);
				}
			}
			if(null != compareDataEntity){
				compareDataEntity.setCheckStatus(PlatformFinanceCompareDataEntity.CHECKSTATUS_YES);
				listCompareData.add(compareDataEntity);
			}
			compareDataResultList.add(compareDataResultEntity);
			
			

		}

		platformFinanceCompareDataNewService.save(listCompareData);
		return compareDataResultList;
	}

	private PlatformFinanceCompareDataResultEntity initCompareDataResultEntity(String checkOid, String checkDate, InvestorTradeOrderRep itEntity) {
		PlatformFinanceCompareDataResultEntity compareDataResultEntity = new PlatformFinanceCompareDataResultEntity();
		compareDataResultEntity.setBuzzDate(DateUtil.parseToSqlDate(checkDate));
		compareDataResultEntity.setCheckOid(checkOid);
		compareDataResultEntity.setInvestorOid(itEntity.getMemberId());
		compareDataResultEntity.setOrderAmount(itEntity.getOrderAmount());
		compareDataResultEntity.setOrderCode(itEntity.getOrderCode());
		compareDataResultEntity.setOrderStatus(itEntity.getOrderStatus());
		compareDataResultEntity.setOrderType(itEntity.getOrderType());
		compareDataResultEntity.setCreateTime(DateUtil.getSqlCurrentDate());
		compareDataResultEntity.setUpdateTime(DateUtil.getSqlCurrentDate());
		compareDataResultEntity.setDealStatus(PlatformFinanceCompareDataResultEntity.DEALSTATUS_TODEAL);
		return compareDataResultEntity;
	}
	private void setProperties(PlatformFinanceCompareDataResultEntity compareDataResultEntity,
			PlatformFinanceCompareDataEntity compareDataEntity) {
		compareDataResultEntity.setCheckInvestorOid(compareDataEntity.getInvestorOid());
		compareDataResultEntity.setCheckOrderAmount(compareDataEntity.getOrderAmount());
		compareDataResultEntity.setCheckOrderCode(compareDataEntity.getOrderCode());
		compareDataResultEntity.setCheckOrderStatus(compareDataEntity.getOrderStatus());
		compareDataResultEntity.setCheckOrderType(compareDataEntity.getOrderType());
		
	}

	public BaseRep checkDataConfirm(String oid, String operator) {
		BaseRep rep=new BaseRep();
		try {
			platformFinanceCheckNewService.checkDataConfirm(oid,operator);
		}catch(AMPException e){
			e.printStackTrace();
			rep.setErrorCode(-1);
			rep.setErrorMessage("对账数据确认失败!");
		}
		return rep;
	}
	
	public PlatformFinanceCheckEntity findByCheckCode(String checkCode) {
		return this.platformFinanceCheckDao.findByCheckCode(checkCode);
	}
	
}
