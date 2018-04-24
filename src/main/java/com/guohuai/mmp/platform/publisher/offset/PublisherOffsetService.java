package com.guohuai.mmp.platform.publisher.offset;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.*;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import com.guohuai.ams.dict.Dict;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductService;
import com.guohuai.basic.message.DealMessageEntity;
import com.guohuai.basic.message.DealMessageEnum;
import com.guohuai.calendar.TradeCalendarService;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.message.MessageSendUtil;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.component.web.view.PagesRep;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderService;
import com.guohuai.mmp.job.lock.JobLockEntity;
import com.guohuai.mmp.job.lock.JobLockService;
import com.guohuai.mmp.job.log.JobLogEntity;
import com.guohuai.mmp.job.log.JobLogFactory;
import com.guohuai.mmp.job.log.JobLogService;
import com.guohuai.mmp.platform.StaticProperties;
import com.guohuai.mmp.platform.accment.AccmentService;
import com.guohuai.mmp.platform.accment.TpIntegratedRequest;
import com.guohuai.mmp.platform.accment.TransPublisherRequest;
import com.guohuai.mmp.platform.accountingnotify.AccountingNotifyService;
import com.guohuai.mmp.platform.finance.check.PlatformFinanceCheckEntity;
import com.guohuai.mmp.platform.finance.check.PlatformFinanceCheckNewService;
import com.guohuai.mmp.platform.finance.check.PlatformFinanceCheckService;
import com.guohuai.mmp.platform.publisher.offsetlog.PublisherOffsetLogEntity;
import com.guohuai.mmp.platform.publisher.order.PublisherOrderEntity;
import com.guohuai.mmp.platform.publisher.order.PublisherOrderService;
import com.guohuai.mmp.platform.publisher.product.offset.ProductOffsetEntity;
import com.guohuai.mmp.platform.publisher.product.offset.ProductOffsetService;
import com.guohuai.mmp.platform.publisher.product.offset.ProductOffsetServiceRequiresNew;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountEntity;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountService;
import com.guohuai.mmp.publisher.cashflow.PublisherCashFlowService;
import com.guohuai.mmp.publisher.corporate.Corporate;
import com.guohuai.mmp.publisher.corporate.CorporateService;
import com.guohuai.mmp.serialtask.PublisherClearTaskParams;
import com.guohuai.mmp.serialtask.PublisherCloseTaskParams;
import com.guohuai.mmp.serialtask.PublisherConfirmTaskParams;
import com.guohuai.mmp.serialtask.SerialTaskEntity;
import com.guohuai.mmp.serialtask.SerialTaskReq;
import com.guohuai.mmp.serialtask.SerialTaskRequireNewService;
import com.guohuai.mmp.serialtask.SerialTaskService;

@Service
@Transactional
public class PublisherOffsetService {

	Logger logger = LoggerFactory.getLogger(PublisherOffsetService.class);

	@Autowired
	private PublisherOffsetDao publisherOffsetDao;
	@Autowired
	private InvestorTradeOrderService investorTradeOrderService;
	@Autowired
	private PublisherBaseAccountService publisherBaseAccountService;
	@Autowired
	private AccountingNotifyService accountingNotifyService;
	@Autowired
	private ProductOffsetService productOffsetService;
	@Autowired
	private ProductService productService;
	@Autowired
	private JobLockService jobLockService;
	@Autowired
	private TradeCalendarService tradeCalendarService;
	@Autowired
	private PublisherOffsetServiceRequiresNew requiresNewService;
	@Autowired
	private ProductOffsetServiceRequiresNew productOffsetServiceRequiresNew;
	@Autowired
	private PublisherOrderService publisherOrderService;
	@Autowired
	private PublisherCashFlowService publisherCashFlowService;
	@Autowired
	private CorporateService corporateService;
	@Autowired
	private SerialTaskService serialTaskService;
	@Autowired
	private AccmentService accmentService;
	@Autowired
	private SerialTaskRequireNewService serialTaskRequireNewService;
	@Autowired
	private JobLogService jobLogService;
	@Autowired
	private PlatformFinanceCheckService platformFinanceCheckService;
	@Autowired
	private PlatformFinanceCheckNewService platformFinanceCheckNewService;
	@Autowired
	private MessageSendUtil messageSendUtil;
	
	public PublisherOffsetEntity updateEntity(PublisherOffsetEntity offset) {
		return this.publisherOffsetDao.save(offset);
	}
	
	public PublisherOffsetEntity getLatestOffset(InvestorTradeOrderEntity orderEntity, Date confirmDate) {
		return this.getLatestOffset(orderEntity, confirmDate, true);
	}
	/**
	 * 获取最新的轧差批次
	 */
	public PublisherOffsetEntity getLatestOffset(InvestorTradeOrderEntity tradeOrder, Date confirmDate, boolean isPositive) {
		BigDecimal orderAmount = tradeOrder.getOrderAmount();
		if (!isPositive) {
			orderAmount = orderAmount.negate();
		}
		PublisherOffsetEntity offset = this.getLatestOffset(tradeOrder.getPublisherBaseAccount(), confirmDate);
		if (tradeOrder.getOrderType().equals(InvestorTradeOrderEntity.TRADEORDER_orderType_invest)
                || (tradeOrder.getOrderType().equals(InvestorTradeOrderEntity.TRADEORDER_orderType_noPayInvest)// 快活宝购买快定宝
                && Product.TYPE_Producttype_03.equals(tradeOrder.getProduct().getType().getOid()))) {
			this.increaseInvest(offset, orderAmount);
		} else {
			this.increaseRedeem(offset, orderAmount);
		}
		return offset;
	}
	
	private PublisherOffsetEntity getLatestOffset(PublisherBaseAccountEntity publisherBaseAccount, Date confirmDate) {
		if (publisherBaseAccount == null) {
			// error.define[20015]=发行人不存在(CODE:20015)
			throw AMPException.getException(20015);
		}
		PublisherOffsetEntity offset = this.publisherOffsetDao.getLatestOffset(publisherBaseAccount, DateUtil.defaultFormat(confirmDate));
		if (offset == null) {
			try {
				offset = requiresNewService.createEntity(publisherBaseAccount, DateUtil.defaultFormat(confirmDate));
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
				throw new AMPException("新建轧差批次异常");
				//return this.getLatestOffset(spv, confirmDate);
			}
		}
		if (!PublisherOffsetEntity.OFFSET_clearStatus_toClear.equals(offset.getClearStatus())) {
			// error.define[30025]=轧差批次状态异常，非待清算(CODE:30025)
			throw new AMPException(30025);
		}
		return offset;
	}
	
	/**
	 * 清算 
	 */
	public BaseRep clear(String offsetOid) {

		PublisherOffsetEntity offset = this.findByOid(offsetOid);

		if (!PublisherOffsetEntity.OFFSET_clearStatus_toClear.equals(offset.getClearStatus())) {
			// error.define[20021]=清算状态异常(CODE:20021)
			throw AMPException.getException(20021);
		}

//		if (DateUtil.daysBetween(DateUtil.getSqlDate(), offset.getOffsetDate()) < 0) {
        Product plusProduct = productService.getBfPlusProduct();
        // 快定宝产品24点为界限
        if(plusProduct != null && offset.getPublisherBaseAccount().getOid().equals(plusProduct.getPublisherBaseAccount().getOid())) {
            if (DateUtil.daysBetween(new java.util.Date(), offset.getOffsetDate()) < 0) {
                // error.define[30062]=清算时间异常(CODE:30062)
                throw AMPException.getException(30062);
            }
        }else {// 其他产品15点为界限
            if (!DateUtil.isLessThanOrEqualToday(offset.getOffsetDate())) {
                // error.define[30062]=清算时间异常(CODE:30062)
                throw AMPException.getException(30062);
            }
        }

		if (0 != this.publisherOffsetDao.beforeOffsetDate(offset.getOffsetDate(),
				offset.getPublisherBaseAccount().getOid())) {
			// error.define[30063]=请优先处理之前的轧差批次(CODE:30063)
			throw AMPException.getException(30063);
		}

		PublisherClearTaskParams param = new PublisherClearTaskParams();
		param.setOffsetOid(offsetOid);

		SerialTaskReq<PublisherClearTaskParams> req = new SerialTaskReq<PublisherClearTaskParams>();
		req.setTaskCode(SerialTaskEntity.TASK_taskCode_publisherClear);
		req.setTaskParams(param);
		/** 清算去重 */
		this.serialTaskService.findByTaskCodeAndTaskParam(req);

		serialTaskService.createSerialTask(req);
		return new BaseRep();
	}
	
	public void clearDo(String offsetOid, String taskOid) {
		

		int i = publisherOffsetDao.updateClearStatus(offsetOid, PublisherOffsetEntity.OFFSET_clearStatus_cleared);
		if (i < 1) {
			// error.define[20021]=清算状态异常(CODE:20021)
			throw new AMPException(20021);
		}
		productOffsetService.updateClearStatus(offsetOid, PublisherOffsetEntity.OFFSET_clearStatus_cleared);
		investorTradeOrderService.updatePublisherClearStatus(offsetOid,
				InvestorTradeOrderEntity.TRADEORDER_publisherClearStatus_cleared);
		serialTaskRequireNewService.updateTime(taskOid);
		
	}
	
	/**
	 * 结算
	 */
	public BaseRep close(String offsetOid) {
		
		PublisherOffsetEntity offset = this.findByOid(offsetOid);
		if (!PublisherOffsetEntity.OFFSET_clearStatus_cleared.equals(offset.getClearStatus())) {
			// error.define[20022]=结算状态异常(CODE:20022)
			throw AMPException.getException(20022);
		}
		
		if (PublisherOffsetEntity.OFFSET_closeStatus_closed.equals(offset.getCloseStatus())) {
			// error.define[20022]=结算状态异常(CODE:20022)
			throw AMPException.getException(20022);
		}
		
		PublisherCloseTaskParams param = new PublisherCloseTaskParams();
		param.setOffsetOid(offsetOid);
		
		SerialTaskReq<PublisherCloseTaskParams> req = new SerialTaskReq<PublisherCloseTaskParams>();
		req.setTaskCode(SerialTaskEntity.TASK_taskCode_publisherClose);
		req.setTaskParams(param);
		/** 结算去重 */
		this.serialTaskService.findByTaskCodeAndTaskParam(req);
		serialTaskService.createSerialTask(req);
		return new BaseRep();
	}
	
	
	
	public BaseRep closeDo(String offsetOid, String taskOid) {
		PublisherOffsetEntity offset = this.findByOid(offsetOid);
		
		if (BigDecimal.ZERO.compareTo(offset.getNetPosition()) == 0) {
			closeDirectly(offset);
		} else {
			PublisherOrderEntity publisherOrder = this.publisherOrderService.createPublisherOrderPay(offset);
			
			String lastOid = "0";
			while (true) {
				List<InvestorTradeOrderEntity> orders = this.investorTradeOrderService.findToCloseOrders(offset, lastOid);
				if (orders.isEmpty()) {
					break;
				}
				TpIntegratedRequest tpIReq = new TpIntegratedRequest();
				List<TransPublisherRequest> tpList = new ArrayList<TransPublisherRequest>();
				tpIReq.setTpList(tpList);
				for (InvestorTradeOrderEntity order : orders) {
					TransPublisherRequest ireq = new TransPublisherRequest();
					ireq.setAccountNo(order.getProduct().getMemberId());
					ireq.setBalance(order.getOrderAmount());
					ireq.setOrderNo(order.getOrderCode());
					ireq.setRelationProductNo(order.getProduct().getOid());
					tpList.add(ireq);
				}
				accmentService.tradepublish(tpIReq);
				
				performClose(publisherOrder);
			}
		}
		serialTaskRequireNewService.updateTime(taskOid);
		return new BaseRep();
	}
	
	private void performClose(PublisherOrderEntity publisherOrder) {
		String orderStatus = PublisherOrderEntity.ORDER_orderStatus_paySuccess;
		String closeStatus4TradeOrder = InvestorTradeOrderEntity.TRADEORDER_publisherCloseStatus_closed;
		String closeStatus4Publisher = PublisherOffsetEntity.OFFSET_closeStatus_closed;
		String closeStatus4Product = ProductOffsetEntity.OFFSET_closeStatus_closed;
		
		
//		if (0 == baseRep.getErrorCode()) {
			this.publisherCashFlowService.createCashFlow(publisherOrder);
			this.publisherBaseAccountService.updateBalancePlusPlus(publisherOrder.getPublisher(), publisherOrder.getOrderAmount());
//		}  else {
//			
//			orderStatus = PublisherOrderEntity.ORDER_orderStatus_payFailed;
//			closeStatus4Publisher = PublisherOffsetEntity.OFFSET_closeStatus_closePayFailed;
//			closeStatus4Product = ProductOffsetEntity.OFFSET_closeStatus_closePayFailed;
//			closeStatus4TradeOrder = InvestorTradeOrderEntity.TRADEORDER_publisherCloseStatus_closePayFailed;
//		}
		
		publisherOrder.setOrderStatus(orderStatus);
		publisherOrder.setCompleteTime(DateUtil.getSqlCurrentDate());
		this.publisherOrderService.updateEntity(publisherOrder);
		
		updateCloseStatus(publisherOrder.getOffset(), closeStatus4TradeOrder, closeStatus4Publisher, closeStatus4Product);
	}
	
	private void closeDirectly(PublisherOffsetEntity offset) {
		String closeStatus4TradeOrder = InvestorTradeOrderEntity.TRADEORDER_publisherCloseStatus_closed;
		String closeStatus4Publisher = PublisherOffsetEntity.OFFSET_closeStatus_closed;
		String closeStatus4Product = ProductOffsetEntity.OFFSET_closeStatus_closed;
		updateCloseStatus(offset, closeStatus4TradeOrder, closeStatus4Publisher, closeStatus4Product);
	}

	private void updateCloseStatus(PublisherOffsetEntity offset, String closeStatus4TradeOrder, String closeStatus4Publisher,
			String closeStatus4Product) {
		int i = this.publisherOffsetDao.updateCloseStatus4Close(offset.getOid(), closeStatus4Publisher,
				PublisherOffsetEntity.OFFSET_closeMan_publisher);
		if (i < 1) {
			// error.define[20022]=结算状态异常(CODE:20022)
			throw AMPException.getException(20022);
		}
		this.productOffsetService.updateCloseStatus4Close(offset.getOid(), closeStatus4Product);
	
		this.investorTradeOrderService.updateCloseStatus4Close(offset, closeStatus4TradeOrder);
	}

	/**
	 * 生产所有发行人的轧差批次
	 */
	@Transactional(value = TxType.REQUIRES_NEW)
	public void createAllNew() {
		
		if (this.jobLockService.getRunPrivilege(JobLockEntity.JOB_jobId_createAllNew)) {
			this.createAllNewLog();
		}
		
	}
	

	
	public void createAllNewLog() {
		JobLogEntity jobLog =  JobLogFactory.getInstance(JobLockEntity.JOB_jobId_createAllNew);
		
		try {
			createAllNewDo();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			jobLog.setJobMessage(AMPException.getStacktrace(e));
			jobLog.setJobStatus(JobLogEntity.JOB_jobStatus_failed);
		}
		jobLog.setBatchEndTime(DateUtil.getSqlCurrentDate());
		this.jobLogService.saveEntity(jobLog);
		this.jobLockService.resetJob(JobLockEntity.JOB_jobId_createAllNew);
		
	}

	private void createAllNewDo() {
		Date curDate = StaticProperties.isIs24() ? DateUtil.getSqlDate() : DateUtil.getAfterDate();
		Date beforeDate = DateUtil.addSQLDays(curDate, -1);

		boolean isTradeDate = tradeCalendarService.isTrade(curDate);
		List<PublisherBaseAccountEntity> publishers = this.publisherBaseAccountService.findAll();
		
		for (PublisherBaseAccountEntity spv : publishers) {
			
			Map<String, OffsetCodePojo> productMap = new HashMap<String, OffsetCodePojo>();
			logger.info("spv.oid:{}, spv.corperateOid:{}", spv.getOid(), spv.getCorperateOid());
			List<Product> t0s = this.productService.findProductT04NewOffset(spv);
			List<Product> tns = this.productService.findProductTn4NewOffset(spv);
			List<Product> plus = this.productService.findBfPlusNewOffset(spv);
			List<Product> productList = new ArrayList<Product>();
			productList.addAll(t0s);
			productList.addAll(tns);
			productList.addAll(plus);
			for (Product product : productList) {
                logger.info("product:{}", product.getName());
				//申购确认日
				if (Product.Product_dateType_D.equals(product.getInvestDateType())) {
//					Date offsetDate = DateUtil.addSQLDays(curDate, product.getPurchaseConfirmDays());
//					String offsetCode = DateUtil.defaultFormat(offsetDate);
//					String startTime = DateUtil.getDaySysBeginTime(curDate);
//					String endTime  = DateUtil.getDaySysEndTime(offsetDate);
//					productMap.put(offsetCode, new OffsetCodePojo(startTime, endTime, product));

				} else {
					// 当是交易日的情况下，创建轧差批次，但前一日必须不是非交易日
					if (isTradeDate) {
						boolean beforeIsTrade = tradeCalendarService.isTrade(beforeDate);
                        logger.info("beforeIsTrade:{}", beforeIsTrade);
                        if (beforeIsTrade) {
							Date offsetDate = tradeCalendarService.nextTrade(curDate, product.getPurchaseConfirmDays());
							String offsetCode = DateUtil.defaultFormat(offsetDate);
							String startTime = DateUtil.getDaySysBeginTime(DateUtil.getSqlDate());
							String endTime  = DateUtil.getDaySysEndTime(tradeCalendarService.nextTrade(DateUtil.getSqlDate(),1));//DateUtil.getDaySysEndTime(DateUtil.addSQLDays(offsetDate, -1));
							productMap.put(offsetCode, new OffsetCodePojo(startTime, endTime, product));
						}
					} else {
						// 当非交易日情况下，判断前一日是否是交易日，如果是交易日，则创建轧差批次。
						// 订单接收日，为接下来一个交易日，下一个交易日为确认日
						boolean beforeIsTrade = tradeCalendarService.isTrade(beforeDate);
                        logger.info("beforeIsTrade:{}", beforeIsTrade);
						if (beforeIsTrade) {
							Date offsetDate = tradeCalendarService.nextTrade(curDate, product.getPurchaseConfirmDays() + 1);
							String offsetCode = DateUtil.defaultFormat(offsetDate);
							String startTime = DateUtil.getDaySysBeginTime(DateUtil.getSqlDate());
							String endTime  = DateUtil.getDaySysEndTime(tradeCalendarService.nextTrade(DateUtil.getSqlDate(),1));//DateUtil.getDaySysEndTime(DateUtil.addSQLDays(offsetDate, -1));
							productMap.put(offsetCode, new OffsetCodePojo(startTime, endTime, product));
						}
					}
				}

				int redeemConfirmDays = null == product.getRedeemConfirmDays() ? 1 : product.getRedeemConfirmDays();
				// 赎回确认日
				if (Product.Product_dateType_D.equals(product.getRredeemDateType())) {
//					Date offsetDate = DateUtil.addSQLDays(curDate, redeemConfirmDays);
//					String offsetCode = DateUtil.defaultFormat(offsetDate);
//					String startTime = DateUtil.getDaySysBeginTime(DateUtil.getSqlDate());
//					String endTime  = DateUtil.getDaySysEndTime(offsetDate);
//					productMap.put(offsetCode, new OffsetCodePojo(startTime, endTime, product));
				} else {
					// 当是交易日的情况下，创建轧差批次
					if (isTradeDate) {
						boolean beforeIsTrade = tradeCalendarService.isTrade(beforeDate);
						if (beforeIsTrade) {
//							Date offsetDate = tradeCalendarService.nextTrade(curDate, redeemConfirmDays);
//							String offsetCode = DateUtil.defaultFormat(offsetDate);
//							String startTime = DateUtil.getDaySysBeginTime(DateUtil.getSqlDate());
//							String endTime  = DateUtil.getDaySysEndTime(offsetDate);
//							productMap.put(offsetCode, new OffsetCodePojo(startTime, endTime, product));
						}
					} else {
						// 当非交易日情况下，判断前一日是否是交易日，如果是交易日，则创建轧差批次。
						// 订单接收日，为接下来一个交易日，下一个交易日为确认日
						boolean beforeIsTrade = tradeCalendarService.isTrade(beforeDate);
						if (beforeIsTrade) {
//							Date offsetDate = tradeCalendarService.nextTrade(curDate, redeemConfirmDays + 1);
//							String offsetCode = DateUtil.defaultFormat(offsetDate);
//							String startTime = DateUtil.getDaySysBeginTime(DateUtil.getSqlDate());
//							String endTime  = DateUtil.getDaySysEndTime(offsetDate);
//							productMap.put(offsetCode, new OffsetCodePojo(startTime, endTime, product));
						}
					}
				}
			}
			Set<String> set = productMap.keySet();
			Iterator<String> it = set.iterator();
			while (it.hasNext()) {
				String offsetCode = it.next();
				PublisherOffsetEntity offset = this.publisherOffsetDao.findByPublisherBaseAccountAndOffsetCode(spv, offsetCode);
				if (null == offset) {
					try {
						offset = this.requiresNewService.createEntity(spv, offsetCode);
                        logger.info("offset:{}:{}", offset.getPublisherBaseAccount().getOid(), offset.getOffsetCode());
					} catch (Exception e) {
						e.printStackTrace();
						logger.error(e.getMessage(), e);
						continue;
					}
				}

				ProductOffsetEntity pOffset = this.productOffsetService.findByProductAndOffsetCode(productMap.get(offsetCode).getProduct(), offsetCode);
				if (null == pOffset) {
					try {
						pOffset = this.productOffsetServiceRequiresNew.createEntity(productMap.get(offsetCode).getProduct(), offset);
					} catch (Exception e) {
						e.printStackTrace();
						logger.error(e.getMessage(), e);
					}
				}
			}
		}
		createCheckDate(curDate, beforeDate, isTradeDate);
	}

	// 按照T+1规则生成对账批次
	public void createCheckDate(Date curDate, Date beforeDate, boolean isTradeDate){
		boolean beforeIsTrade= tradeCalendarService.isTrade(beforeDate);
		Date offsetDate = null;
		if (beforeIsTrade) {
			if (isTradeDate) {
				offsetDate = tradeCalendarService.nextTrade(curDate, 1);
			} else {
				offsetDate = tradeCalendarService.nextTrade(curDate, 2);
			}
		}
		if (null != offsetDate) {
			String offsetCode = DateUtil.defaultFormat(offsetDate);
			String startTime = DateUtil.getDaySysBeginTime(DateUtil.getSqlDate());
			String endTime = DateUtil.getDaySysEndTime(tradeCalendarService.nextTrade(DateUtil.getSqlDate(), 1));
			
			Map<String, OffsetCodePojo> productMap = new HashMap<>();
			productMap.put(offsetCode, new OffsetCodePojo(startTime, endTime, null));
			
			PlatformFinanceCheckEntity checkEntity = this.platformFinanceCheckService.findByCheckCode(PlatformFinanceCheckEntity.PREFIX+offsetCode);
			if (null == checkEntity) {
				try {
					platformFinanceCheckNewService.createEntity(productMap,offsetCode);
				} catch (Exception e) {
					e.printStackTrace();
					logger.error(e.getMessage(), e);
				}
			}
		}
	}

	


	/**
	 * 增加待清算投资
	 * @param offset
	 * @param investAmount
	 */
	public void increaseInvest(PublisherOffsetEntity offset, BigDecimal investAmount){
		this.publisherOffsetDao.increaseInvest(offset.getOid(), investAmount);
	}
	
	/**
	 * 增加待清算赎回
	 * @param offset
	 * @param investAmount
	 */
	public void increaseRedeem(PublisherOffsetEntity offset,BigDecimal investAmount){
		this.publisherOffsetDao.increaseRedeem(offset.getOid(), investAmount);
	}

	/**
	 * @param spec
	 * @param pageable
	 * @return
	 */
	public PagesRep<PublisherOffsetQueryRep> mng(Specification<PublisherOffsetEntity> spec, Pageable pageable) {
		Page<PublisherOffsetEntity> cas = this.publisherOffsetDao.findAll(spec, pageable);
		PagesRep<PublisherOffsetQueryRep> pagesRep = new PagesRep<PublisherOffsetQueryRep>();
		if (cas != null && cas.getContent() != null && cas.getTotalElements() > 0) {
		    // 快定宝产品
            Product plusProduct = productService.getBfPlusProduct();
			for (PublisherOffsetEntity offset : cas) {
				PublisherOffsetQueryRep queryRep = new PublisherOffsetQueryRep();
				queryRep.setSpvOid(offset.getPublisherBaseAccount().getOid());
				Corporate cobj = corporateService.read(offset.getPublisherBaseAccount().getCorperateOid());
				
				queryRep.setSpvOid(cobj.getAccount());
				queryRep.setSpvName(cobj.getCompanyName());
				queryRep.setOffsetOid(offset.getOid()); // 轧差OID
				queryRep.setOffsetDate(offset.getOffsetDate()); // 轧差日期
				queryRep.setOffsetCode(offset.getOffsetCode()); // 轧差批次
				queryRep.setNetPosition(offset.getNetPosition()); // 净头寸
                // 快定宝产品24点为界限
                if(plusProduct != null && offset.getPublisherBaseAccount().getOid().equals(plusProduct.getPublisherBaseAccount().getOid())){
                    if (DateUtil.daysBetween(new java.util.Date(), offset.getOffsetDate()) >= 0) {
                        queryRep.setClearTimeArr(true);
                    } else {
                        queryRep.setClearTimeArr(false);
                    }
                }else {// 其他产品15点为界限
                    if (!DateUtil.isLessThanOrEqualToday(offset.getOffsetDate())) {
                        queryRep.setClearTimeArr(false);
                    } else {
                        queryRep.setClearTimeArr(true);
                    }
                }

//				if (DateUtil.daysBetween(DateUtil.getSqlDate(), offset.getOffsetDate()) < 0) {
//					queryRep.setClearTimeArr(false);
//				} else {
//					queryRep.setClearTimeArr(true);
//				}
				queryRep.setClearStatus(offset.getClearStatus()); // 轧差状态
				queryRep.setClearStatusDisp(clearStatusEn2Ch(offset.getClearStatus())); // 
				queryRep.setConfirmStatus(offset.getConfirmStatus());
				queryRep.setConfirmStatusDisp(confirmStatusEn2Ch(offset.getConfirmStatus()));
				queryRep.setCloseStatus(offset.getCloseStatus());
				queryRep.setCloseStatusDisp(closeStatusEn2Ch(offset.getCloseStatus()));
				queryRep.setBuyAmount(offset.getInvestAmount()); // 申购金额
				queryRep.setRedeemAmount(offset.getRedeemAmount()); // 赎回金额
				queryRep.setCloseMan(offset.getCloseMan()); // 结算人
				queryRep.setUpdateTime(offset.getUpdateTime());
				queryRep.setCreateTime(offset.getCreateTime());
				pagesRep.add(queryRep);
			}
		}
		pagesRep.setTotal(cas.getTotalElements());
		return pagesRep;
	}

	
	
	
	private String closeStatusEn2Ch(String closeStatus) {

		if (PublisherOffsetEntity.OFFSET_closeStatus_toClose.equals(closeStatus)) {
			return "待结算";
		} else if (PublisherOffsetEntity.OFFSET_closeStatus_closing.equals(closeStatus)) {
			return "结算中";
		} else if (PublisherOffsetEntity.OFFSET_closeStatus_closed.equals(closeStatus)) {
			return "已结算";
		} else if (PublisherOffsetEntity.OFFSET_closeStatus_closeSubmitFailed.equals(closeStatus)) {
			return "结算申请失败";
		} else if (PublisherOffsetEntity.OFFSET_closeStatus_closePayFailed.equals(closeStatus)) {
			return "结算支付失败";
		}
		return closeStatus;
	}
	
	/**
	 * public static final String OFFSET_confirmStatus_toConfirm = "cleared";
	public static final String OFFSET_confirmStatus_confirming = "confirming";
	public static final String OFFSET_confirmStatus_confirmed = "confirmed";
	public static final String OFFSET_confirmStatus_confirmFailed = "confirmFailed";
	 * @param confirmStatus
	 * @return
	 */
	private String confirmStatusEn2Ch(String confirmStatus) {
		if (PublisherOffsetEntity.OFFSET_confirmStatus_toConfirm.equals(confirmStatus)) {
			return "待确认";
		} else if (PublisherOffsetEntity.OFFSET_confirmStatus_confirming.equals(confirmStatus)) {
			return "确认中";
		} else if (PublisherOffsetEntity.OFFSET_confirmStatus_confirmed.equals(confirmStatus)) {
			return "已确认";
		} else if (PublisherOffsetEntity.OFFSET_confirmStatus_confirmFailed.equals(confirmStatus)) {
			return "确认失败";
		}
		return confirmStatus;
	}
	
	private String clearStatusEn2Ch(String clearStatus) {
		if (PublisherOffsetEntity.OFFSET_clearStatus_toClear.equals(clearStatus)) {
			return "待清算";
		} else if (PublisherOffsetEntity.OFFSET_clearStatus_clearing.equals(clearStatus)) {
			return "清算中";
		} else if (PublisherOffsetEntity.OFFSET_clearStatus_cleared.equals(clearStatus)) {
			return "已清算";
		}
		return clearStatus;
	}

	public PublisherOffsetDetailRep detail(String offsetOid) {
		PublisherOffsetEntity offset = this.findByOid(offsetOid);
		PublisherOffsetDetailRep rep = new PublisherOffsetDetailRep();
		rep.setSpvOid(offset.getPublisherBaseAccount().getOid());
		rep.setOffsetDate(offset.getOffsetDate()); // 轧差日期
		rep.setOffsetCode(offset.getOffsetCode()); // 轧差批次
		rep.setNetPosition(offset.getNetPosition()); // 净头寸
		rep.setClearStatus(offset.getClearStatus()); // 轧差状态
		rep.setClearStatusDisp(clearStatusEn2Ch(offset.getClearStatus())); // 
		rep.setConfirmStatus(offset.getConfirmStatus());
		rep.setConfirmStatusDisp(confirmStatusEn2Ch(offset.getConfirmStatus()));
		rep.setCloseStatus(offset.getCloseStatus());
		rep.setCloseStatusDisp(closeStatusEn2Ch(offset.getCloseStatus()));
		rep.setBuyAmount(offset.getInvestAmount()); // 申购金额
		rep.setRedeemAmount(offset.getRedeemAmount()); // 赎回金额
		rep.setCloseMan(offset.getCloseMan()); // 结算人
		rep.setUpdateTime(offset.getUpdateTime());
		rep.setCreateTime(offset.getCreateTime());
		return rep;
	}

	public PublisherOffsetEntity findByOid(String offsetOid) {
		PublisherOffsetEntity offset = this.publisherOffsetDao.findOne(offsetOid);
		if (null == offset) {
			// error.define[30022]=轧差不存在(CODE:30022)
			throw new AMPException(30022);
		}

		return offset;
	}
	
	/**
	 * 份额确认
	 */
	public BaseRep confirm(String offsetOid) {
		
		PublisherConfirmTaskParams params = new PublisherConfirmTaskParams();
		params.setOffsetOid(offsetOid);
		
		SerialTaskReq<PublisherConfirmTaskParams> req = new SerialTaskReq<PublisherConfirmTaskParams>();
		req.setTaskCode(SerialTaskEntity.TASK_taskCode_publisherConfirm);
		req.setTaskParams(params);
		this.serialTaskService.createSerialTask(req);
		return new BaseRep();
	}

	public BaseRep confirmDo(String offsetOid, String taskOid) {
		BaseRep rep = new BaseRep();

		PublisherOffsetEntity offsetEntity = this.findByOid(offsetOid);
		
		String spvConfirmStatus = PublisherOffsetEntity.OFFSET_confirmStatus_confirming;
		String proConfirmStatus = ProductOffsetEntity.OFFSET_confirmStatus_confirming;
		this.requiresNewService.updateSpvConfirmStatus4Lock(offsetEntity.getOid(), spvConfirmStatus,
				proConfirmStatus);
		
		String lastOid = "0";
		PublisherOffsetLogEntity offsetLog = new PublisherOffsetLogEntity();
		int arithmometer = 1;
		while (true) {
			List<InvestorTradeOrderEntity> orderList = investorTradeOrderService.findByOffsetOid(offsetEntity.getOid(),
					lastOid);
			if (orderList.isEmpty()) {
				break;
			}

			for (InvestorTradeOrderEntity orderEntity : orderList) {
				lastOid = orderEntity.getOid();

				VolumeConfirmRep iRep;
                if(Product.TYPE_Producttype_03.equals(orderEntity.getProduct().getType().getOid())){
                    iRep = requiresNewService.plusProcessOneItem(orderEntity);
                }else {
                    iRep = requiresNewService.processOneItem(orderEntity);
                }

				if (iRep.isSuccess()) {
					offsetLog.setSuccessPeopleNum(offsetLog.getSuccessPeopleNum() + 1);
					offsetLog.setInvestAmount(offsetLog.getInvestAmount().add(iRep.getInvestAmount()));
					offsetLog.setRedeemAmount(offsetLog.getRedeemAmount().add(iRep.getRedeemAmount()));
					String productType=Optional.ofNullable(orderEntity).map(InvestorTradeOrderEntity::getProduct)
							.map(Product::getType).map(Dict::getOid).orElse("");
					if (Objects.equals(Product.TYPE_Producttype_03,productType)){
						sendMessage(orderEntity, DealMessageEnum.CYCLE_INVEST_CONFIRMED.name());
					}else{
						sendMessage(orderEntity, DealMessageEnum.INVEST_CONFIRMED.name());
					}
				} else {
					offsetLog.setFailurePeopleNum(offsetLog.getFailurePeopleNum() + 1);
				}
				arithmometer++;
				if (arithmometer > 100) {
					arithmometer = 1;
					serialTaskRequireNewService.updateTime(taskOid);
				}
			}
		}
		String spvOffsetStatus = PublisherOffsetEntity.OFFSET_confirmStatus_confirmed;
		String proOffsetStatus = ProductOffsetEntity.OFFSET_confirmStatus_confirmed;

		if (offsetLog.getFailurePeopleNum() != 0) {
			spvOffsetStatus = PublisherOffsetEntity.OFFSET_confirmStatus_confirmFailed;
			proOffsetStatus = ProductOffsetEntity.OFFSET_confirmStatus_confirmFailed;
		}

		logger.info("====份额确认结果||" + offsetLog + "||====");

		requiresNewService.processStatus(offsetEntity, spvOffsetStatus, proOffsetStatus);
		return rep;
	}
	
	private void sendMessage(InvestorTradeOrderEntity orderEntity, String tag) {
		DealMessageEntity messageEntity = new DealMessageEntity();
		messageEntity.setPhone(orderEntity.getInvestorBaseAccount().getPhoneNum());
		messageEntity.setOrderAmount(orderEntity.getOrderAmount().setScale(2, BigDecimal.ROUND_DOWN).toString());
		messageEntity.setOrderTime(orderEntity.getOrderTime());
		messageEntity.setProductName(orderEntity.getProduct().getName());
		messageSendUtil.sendTimedTopicMessage(messageSendUtil.getDealTopic(), tag, messageEntity);
	}

	
	public BaseRep offsetMoney(OffsetMoneyReq moneyReq) {
		BaseRep rep = new BaseRep();
		System.out.println(moneyReq);
		List<Money> list = moneyReq.getOffsetMoneyList();
		this.accountingNotifyService.offsetMoney(list);
		return rep;
	}
	
	public List<PublisherOffsetEntity> getOverdueOffset(Date curDate) {
		
		return this.publisherOffsetDao.getOverdueOffset(curDate);
	}

	public void batchUpdate(List<PublisherOffsetEntity> pOffsetList) {
		this.publisherOffsetDao.save(pOffsetList);
	}

    /**
     * 生成快定宝轧差定时任务
     */
    @Transactional(value = TxType.REQUIRES_NEW)
    public void createBfPlusOffset() {
        if (this.jobLockService.getRunPrivilege(JobLockEntity.JOB_jobId_createBfPlusOffset)) {
            JobLogEntity jobLog =  JobLogFactory.getInstance(JobLockEntity.JOB_jobId_createBfPlusOffset);
            try {
                createBfPlusOffsetDo();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                jobLog.setJobMessage(AMPException.getStacktrace(e));
                jobLog.setJobStatus(JobLogEntity.JOB_jobStatus_failed);
            }
            jobLog.setBatchEndTime(DateUtil.getSqlCurrentDate());
            this.jobLogService.saveEntity(jobLog);
            this.jobLockService.resetJob(JobLockEntity.JOB_jobId_createBfPlusOffset);
        }
    }

    /**
     * 生成快定宝轧差
     */
    private void createBfPlusOffsetDo() {
        Date curDate = DateUtil.getSqlDate();

        boolean isTradeDate = tradeCalendarService.isTrade(curDate);
        // 正常逻辑应该按照发行人计算
        Product product = productService.getBfPlusProduct();

        String offsetCode = calcOffsetCode(curDate, isTradeDate, product);
        createOffset(product, offsetCode);

        createCheckDate(offsetCode);
    }

    /**
     * 计算快定宝轧差批次
     * @param curDate
     * @param isTradeDate
     * @param product
     * @return
     */
    private String calcOffsetCode(Date curDate, boolean isTradeDate, Product product) {
        //申购确认日
        if (Product.Product_dateType_T.equals(product.getInvestDateType())) {
            // 当是交易日的情况下，创建轧差批次，但前一日必须不是非交易日
            if (isTradeDate) {
                Date offsetDate = tradeCalendarService.nextTrade(curDate, product.getPurchaseConfirmDays());
                return DateUtil.defaultFormat(offsetDate);
            }
        }
        return null;
    }

    /**
     * 生成快定宝相关的发行人轧差和产品轧差
     * @param product
     * @param offsetCode
     */
    private void createOffset(Product product, String offsetCode) {
        PublisherBaseAccountEntity spv = product.getPublisherBaseAccount();
        PublisherOffsetEntity offset = this.publisherOffsetDao.findByPublisherBaseAccountAndOffsetCode(spv, offsetCode);
        if (null == offset) {
            try {
                offset = this.requiresNewService.createEntity(spv, offsetCode);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        ProductOffsetEntity pOffset = this.productOffsetService.findByProductAndOffsetCode(product, offsetCode);
        if (null == pOffset) {
            try {
                this.productOffsetServiceRequiresNew.createEntity(product, offset);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    // 按照T+1规则生成对账批次
    public void createCheckDate(String offsetCode) {
        PlatformFinanceCheckEntity checkEntity = this.platformFinanceCheckService.findByCheckCode(PlatformFinanceCheckEntity.PREFIX + offsetCode);
        if (null == checkEntity) {
            try {
                Map<String, OffsetCodePojo> productMap = new HashMap<>();
                String startTime = DateUtil.getDaySysBeginTime(DateUtil.getSqlDate());
                String endTime = DateUtil.getDaySysEndTime(tradeCalendarService.nextTrade(DateUtil.getSqlDate(), 1));
                productMap.put(offsetCode, new OffsetCodePojo(startTime, endTime, null));
                platformFinanceCheckNewService.createEntity(productMap, offsetCode);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
