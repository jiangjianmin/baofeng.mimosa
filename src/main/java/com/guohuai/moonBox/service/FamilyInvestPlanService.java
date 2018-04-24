package com.guohuai.moonBox.service;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.runtime.directive.Break;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.guohuai.ams.product.OnSaleT0ProductRep;
import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductDao;
import com.guohuai.ams.product.ProductService;
import com.guohuai.basic.common.StringUtil;
import com.guohuai.basic.message.DealMessageEntity;
import com.guohuai.basic.message.DealMessageEnum;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.message.MessageSendUtil;
import com.guohuai.component.web.view.PagesRep;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountDao;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountEntity;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountService;
import com.guohuai.mmp.investor.tradeorder.InvestorInvestTradeOrderExtService;
import com.guohuai.mmp.investor.tradeorder.TradeOrderReq;
import com.guohuai.mmp.job.lock.JobLockEntity;
import com.guohuai.mmp.job.lock.JobLockService;
import com.guohuai.mmp.job.log.JobLogEntity;
import com.guohuai.mmp.job.log.JobLogFactory;
import com.guohuai.mmp.job.log.JobLogService;
import com.guohuai.moonBox.FamilyContans;
import com.guohuai.moonBox.FamilyEnum;
import com.guohuai.moonBox.dao.CheckInvestTradeDao;
import com.guohuai.moonBox.dao.FamilyInvestPlanDao;
import com.guohuai.moonBox.dao.ProtocalLogDao;
import com.guohuai.moonBox.dao.ProtocalSnapshotDao;
import com.guohuai.moonBox.entity.ProtocalEntity;
import com.guohuai.moonBox.entity.ProtocalLogEntity;
import com.guohuai.moonBox.entity.ProtocalSnapshotEntity;
import com.guohuai.moonBox.to.BaseReq;
import com.guohuai.moonBox.to.proticalChangeRes;
import com.guohuai.moonBox.to.proticalDeleteReq;
import com.guohuai.moonBox.to.proticalDeleteRes;
import com.guohuai.moonBox.to.proticalLogQueryRes;
import com.guohuai.moonBox.to.proticalQueryReq;
import com.guohuai.moonBox.to.proticalQueryRes;
import com.guohuai.moonBox.to.protocalAddReq;
import com.guohuai.moonBox.to.protocalAddRes;
import com.guohuai.moonBox.to.protocalInvestQueryReq;
import com.guohuai.moonBox.to.protocalInvestQueryRes;
import com.guohuai.moonBox.to.protocalUpdReq;
import com.guohuai.moonBox.to.protocalUpdRes;
import com.guohuai.moonBox.util.DateUtil;
import com.guohuai.moonBox.util.SetRedisUtil;
import com.guohuai.usercenter.api.UserCenterSdk;
import com.guohuai.usercenter.api.obj.UCBankQueryRes;

import lombok.extern.slf4j.Slf4j;


@Service
@Transactional
@Slf4j
public class FamilyInvestPlanService {
	private Logger logger = LoggerFactory.getLogger(FamilyInvestPlanService.class);
//	 @Autowired
//	 private DateUtil DateUtil;
	@Autowired
	private InvestorBaseAccountDao investorBaseAccountDao;
	@Autowired	
	 private ProtocalLogDao protocalLogDao;
	 @Autowired
	 private CheckInvestTradeDao checkInvestTradeDao;
	 @Autowired
	 private ProtocalSnapshotDao protocalSnapshotDao;
	 @Autowired
	 private FamilyInvestPlanDao familyInvestPlanDao;
	 @Autowired
	 private ProductDao productDao;
	 @Autowired
	 private JobLockService jobLockService;
	 @Autowired
  	 private JobLogService jobLogService;
	 @Autowired
	private InvestorBaseAccountService investorBaseAccountService;
	 @Autowired
	 private InvestorInvestTradeOrderExtService investorInvestTradeOrderExtService;
	 @Autowired
	 private UserCenterSdk userCenterSdk;
	 @Autowired
	 RedisTemplate<String, String> redis;
	 @Autowired
	 private MessageSendUtil messageSendUtil;
	 @Autowired
	 private ProductService productService;
	 
	 @Value("${family.investDate}")
	private String familyAutoInvestDate;
	
	 public protocalInvestQueryRes checkInvest(protocalInvestQueryReq req){
		logger.info("查询用户交易信息，{}",JSONObject.toJSONString(req));
		protocalInvestQueryRes res = new protocalInvestQueryRes();
		if(StringUtil.isEmpty(req.getInvestOid())){
			res.setReturnCode(FamilyEnum.return0001.getCode());
			res.setReturnMsg(FamilyEnum.return0001.getName());
			return res;
		}
//		if(StringUtil.isEmpty(req.getProductOid())){
//			res.setReturnCode(FamilyEnum.return0002.getCode());
//			res.setReturnMsg(FamilyEnum.return0002.getName());
//			return res;
//		}
//		long resultCount =checkInvestTradeDao.checkInvest(req.getInvestOid(),req.getProductOid(), FamilyEnum.isAuto0.getCode(), DateUtil.getMinMonthDate(),DateUtil.getMaxMonthDate());
//		//查询订单表里是否有扣款记录
//		if(resultCount<=0){
//			logger.info("订单表中没有扣款记录，查询redis记录");
			//订单表没有扣款记录
			 //查询redis，当月是否有扣款失败记录
			 List<ProtocalEntity> protocalList =familyInvestPlanDao.findByInvestOidAndUpdateTime(req.getInvestOid(),DateUtil.getMinMonthDate()+" 00:00:00");
			 logger.info("查询用户协议信息，{}",JSONObject.toJSONString(protocalList));
			 if(protocalList.size()>0){
				 for(ProtocalEntity protocal:protocalList){
					 String redisHashKey=SetRedisUtil.FAMILY_PLAN_KEY_PRE+protocal.getOid()+":"+protocal.getProductOid()+":"+protocal.getInvestOid()+":"+DateUtil.GetDateYear()+DateUtil.GetDateMonth();
					 logger.info("redis键："+redisHashKey);
					 Boolean exists=SetRedisUtil.exists(redis, redisHashKey);
					 logger.info("查询结果："+exists);
					 if(exists){//当月有用户信息，说明有过扣款记录
						String investCount= SetRedisUtil.hGet(redis,redisHashKey, SetRedisUtil.FAMILY_PLAN_COUNT_KEY_PRE);//查询当月扣款次数
						 if(Integer.valueOf(investCount)>0){
							//当月有扣款记录
							res.setReturnCode(FamilyContans.SUCCESS);
							res.setInvestCount(Integer.valueOf(investCount));
							logger.info("系统已执行过自动扣款："+investCount+"次");
							return res;
						 }
					 }else{
						 logger.info("redis不存在当月记录");
						 res.setReturnCode(FamilyContans.SUCCESS);
						 res.setInvestCount(0);
					 }
				 }
			 }else{
				logger.info("用户从未开通过协议，投资次数为0");
				 //当月有扣款记录
				res.setReturnCode(FamilyContans.SUCCESS);
				res.setInvestCount(0);
			 }
//		}else{
//			//当月有扣款记录
//			res.setReturnCode(FamilyContans.SUCCESS);
//			res.setInvestCount((int)resultCount);
//		}
		logger.info("查询交易返回，{}",JSONObject.toJSONString(res));
		return res;
	}
	public proticalQueryRes ProtocalQuery(proticalQueryReq req){
		proticalQueryRes res=new proticalQueryRes();
		logger.info("协议查询。传入参数为,{}",JSONObject.toJSONString(req));
		if(StringUtil.isEmpty(req.getInvestOid())){
			res.setReturnCode(FamilyEnum.return0001.getCode());
			res.setReturnMsg(FamilyEnum.return0001.getName());
			return res;
		}
		if(StringUtil.isEmpty(req.getProtocalStatus())){
			req.setProtocalStatus(FamilyEnum.protocalStatus0.getCode());
		}
		List<ProtocalEntity> protocalList=familyInvestPlanDao.findByInvestOid(req.getInvestOid(),req.getProtocalStatus());
		if(protocalList.size()<=0){
			res.setReturnCode(FamilyEnum.return1000.getCode());
			res.setReturnMsg(FamilyEnum.return1000.getName());
			return res;
		}else{
			for(ProtocalEntity protocal:protocalList){
				Product productName=productDao.findOne(protocal.getProductOid());
				if(productName==null){
					res.setReturnCode(FamilyEnum.return0003.getCode());
					res.setReturnMsg(FamilyEnum.return0003.getName());
					return res;
				}else{
					 //查询用户是否绑卡
					String bankNo=null;
					String BankName="";
					 UCBankQueryRes bankinfo= userCenterSdk.queryUserBankInfo(req.getInvestOid());
					 logger.info("用户绑卡信息为,{}",JSONObject.toJSONString(bankinfo));
					 if(bankinfo.isIsbind()&&!StringUtil.isEmpty(bankinfo.getCardNumb())){
						 bankNo=bankinfo.getCardNumb();
						 BankName=bankinfo.getBankName();
					 }
					//end
//					res.setInvestOid(req.getInvestOid());
					res.setProtocalOid(protocal.getOid());
					res.setProtocalName(protocal.getProtocalName());
					res.setProductOid(protocal.getProductOid());
					res.setProductName(productName.getFullName());
					res.setProtocalAmount(protocal.getProtocalAmount());
					res.setProtocalDate(protocal.getProtocalDate());
					res.setNextInvestDate(protocal.getNextInvestDate());
					res.setBankName(BankName);
					res.setBankCardNum(StringUtils.isBlank(bankNo)?"":com.guohuai.component.util.StringUtil.kickstarOnCardNum(bankNo)); // 
					if(bankNo == null) res.setFaultBankCardNum("");
					else res.setFaultBankCardNum(bankNo.substring(bankNo.length()-4,bankNo.length()));//后四位
					res.setReturnCode(FamilyEnum.return0000.getCode());
				}
			}
		}
		return res;
	}
	public proticalDeleteRes delProtocal(proticalDeleteReq req){
		proticalDeleteRes res=new proticalDeleteRes();
		logger.info("协议删除。传入参数为,{}",JSONObject.toJSONString(req));
		ProtocalEntity protocal=familyInvestPlanDao.findOne(req.getProtocalOid());
		if(protocal==null){
			logger.info("协议信息不存在无需删除");
			res.setReturnCode(FamilyEnum.return1000.getCode());
			res.setReturnMsg(FamilyEnum.return1000.getName());
			return res;
		}else{
			try{
				if(protocal.getProtocalStatus().equals(FamilyEnum.protocalStatus1.getCode())){
					logger.info("协议信息已删除无需再删除，返回码0000");
					res.setReturnCode(FamilyEnum.return0000.getCode());
					res.setReturnMsg(FamilyEnum.return0000.getName());
					return res;
				}
				if(DateUtil.Dateequals(protocal.getNextInvestDate(),0)){//判断当天是否为自动转入日期
					logger.info("当天为系统自动扣款日禁止删除理财计划");
					res.setReturnCode(FamilyEnum.return0005.getCode());
					res.setReturnMsg(FamilyEnum.return0005.getName());
					return res;
				}else if(DateUtil.Dateequals(protocal.getNextInvestDate(),1)){//判断明天是否等于自动转入日期
					if(!DateUtil.CheckTime(familyAutoInvestDate)){//判断是否是三点之前，三点之后禁止删除计划
						res.setReturnCode(FamilyEnum.return0006.getCode());
						res.setReturnMsg(FamilyEnum.return0006.getName()+","+familyAutoInvestDate+"点后禁止删除理财计划");
						return res;
					}
				}
				long returCount=protocalSnapshotDao.findSnapshotByInvestOid(req.getInvestOid(),req.getProtocalOid());//查询快照表中是否有待执行的任务
				if(returCount>0){
					logger.info("存在快照记录无法删除");
					res.setReturnCode(FamilyEnum.return0004.getCode());
					res.setReturnMsg(FamilyEnum.return0004.getName());
					return res;
				}
				String redisHashKey=SetRedisUtil.FAMILY_PLAN_KEY_PRE+protocal.getOid()+":"+protocal.getProductOid()+":"+protocal.getInvestOid()+":"+DateUtil.GetDateYear()+DateUtil.GetDateMonth();
				if(SetRedisUtil.exists(redis, redisHashKey)){
					String investStatus=SetRedisUtil.hGet(redis, redisHashKey, SetRedisUtil.FAMILY_PLAN_STATUS_KEY_PRE);
					 if(investStatus.equals(FamilyEnum.investStatus3.getCode())){//查询任务是否还未执行完成
					   	    logger.info("redis中存在处理中的数据，禁止删除");
							res.setReturnCode(FamilyEnum.return0004.getCode());
							res.setReturnMsg(FamilyEnum.return0004.getName());
							return res;
					 } 
				}
//				if(DateUtil.GetDateDay().equals(protocal.protocalDate)){
//					logger.info("当天为自动扣款日禁止删除");
//					res.setReturnCode(FamilyEnum.return0005.getCode());
//					res.setReturnMsg(FamilyEnum.return0005.getName());
//					return res;
//				}else if(DateUtil.GetDateDay().equals(String.valueOf(Integer.valueOf(protocal.protocalDate)-1))){
//					if(!DateUtil.CheckTime(familyAutoInvestDate)){
//						logger.info("明天为自动扣款日禁止删除");
//						res.setReturnCode(FamilyEnum.return0006.getCode());
//						res.setReturnMsg(FamilyEnum.return0006.getName());
//						return res;
//					}
//				}
				String LastPayDate="";
				if(protocal.getLastPayDate()!=null){
			        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");   
			        try {   
			            //方法一   
			        	LastPayDate = sdf.format(protocal.getLastPayDate());   
			            logger.info("最近一次扣款日期，{}",LastPayDate);
			        } catch (Exception e) {   
			            e.printStackTrace();   
			        }  
				}
				if(DateUtil.Dateequals(protocal.getNextInvestDate(),0)){//当天为自动扣款日禁止修改
					logger.info("当天为自动扣款日禁止删除");
					res.setReturnCode(FamilyEnum.return0005.getCode());
					res.setReturnMsg(FamilyEnum.return0005.getName());
					return res;
				}else if(DateUtil.Dateequals(protocal.getNextInvestDate(),1)){//明天为自动扣款日
					if(!DateUtil.CheckTime(familyAutoInvestDate)){
						logger.info("明天为自动扣款日禁止删除");
						res.setReturnCode(FamilyEnum.return0006.getCode());
						res.setReturnMsg(FamilyEnum.return0006.getName());
						return res;
					}
				}
				logger.info("可以修改该理财计划");
				protocal.setProtocalStatus(FamilyEnum.protocalStatus1.getCode());
				protocal.setUpdateTime(DateUtil.getSqlCurrentDate());
				this.familyInvestPlanDao.saveAndFlush(protocal);//将协议修改修改为作废
				 /**修改理财计划日志表**/
				ProtocalLogEntity  protocalLog=new ProtocalLogEntity();
				BeanUtils.copyProperties(protocal, protocalLog);
				protocalLog.setProtocalOid(protocal.getOid());
				protocalLog.setCreateTime(DateUtil.getSqlCurrentDate());
				protocalLog.setOperateStatus(FamilyEnum.operateStatus2.getCode());
				this.protocalLogDao.save(protocalLog);
				res.setReturnCode(FamilyEnum.return0000.getCode());
				res.setReturnMsg(FamilyEnum.return0000.getName());
			}catch(Exception e){
				logger.info("理财计划删除失败,详情为:{}",e.getMessage());
				e.printStackTrace();
				res.setReturnCode(FamilyEnum.return9999.getCode());
				res.setReturnMsg(FamilyEnum.return9999.getName());
			}
		}
		return res;
	}
	public protocalAddRes addProtocal(protocalAddReq req){
		logger.info("新增理财计划，{}",JSONObject.toJSONString(req));
		protocalAddRes res=new protocalAddRes();
		if(StringUtil.isEmpty(req.getInvestOid())){
			res.setReturnCode(FamilyEnum.return0001.getCode());
			res.setReturnMsg(FamilyEnum.return0001.getName());
			return res;
		}
//		if(StringUtil.isEmpty(req.getProtocalName())){
//			req.setProtocalName(FamilyEnum.protocalName.getName());
//		}
		if(req.getProtocalAmount().equals(new BigDecimal(0))){
			res.setReturnCode(FamilyEnum.return0007.getCode());
			res.setReturnMsg(FamilyEnum.return0007.getName());
			return res;
		}
		if(StringUtil.isEmpty(req.getProtocalDate())){
			res.setReturnCode(FamilyEnum.return0008.getCode());
			res.setReturnMsg(FamilyEnum.return0008.getName());
			return res;
		}
		if(req.getNextInvestDate()==null){
			res.setReturnCode(FamilyEnum.return0009.getCode());
			res.setReturnMsg(FamilyEnum.return0009.getName());
			return res;
		}
		//查询用户状态
		InvestorBaseAccountEntity baseAccount = investorBaseAccountService.findByUid(req.getInvestOid());
		if(baseAccount == null || "forbidden".equals(baseAccount.getStatus())) {
			logger.info("用户状态异常，禁止开通协议，详情为:{}",JSONObject.toJSONString(baseAccount));
			res.setReturnCode(FamilyEnum.return1001.getCode());
			res.setReturnMsg(FamilyEnum.return1001.getName());
			return res;
		}
		 //查询用户是否绑卡
		 UCBankQueryRes bankinfo= userCenterSdk.queryUserBankInfo(req.getInvestOid());
		 if(!bankinfo.isIsbind()||StringUtil.isEmpty(bankinfo.getIdNumb())){
			 logger.info("用户未绑卡，禁止开通协议，详情为:{}",JSONObject.toJSONString(baseAccount));
		  	 res.setReturnCode(FamilyEnum.return1002.getCode());
			res.setReturnMsg(FamilyEnum.return1002.getName());
			return res;
		 }
		List<ProtocalEntity> protocal=familyInvestPlanDao.findByInvestOid(req.getInvestOid(),FamilyEnum.protocalStatus0.getCode());
		logger.info("用户的理财计划，{}",JSONObject.toJSONString(protocal));
		if(protocal!=null&&protocal.size()>0){//协议不存在可以添加
			logger.info("协议已存在，无需再创建");
			res.setReturnCode(FamilyEnum.return0000.getCode());
			res.setReturnMsg(FamilyEnum.return0000.getName());
			return res;
		}
		//计算下次转入日期
//		String nextAutoInvestDate=nextAutoInvestDate(req,req.getProtocalDate());
//		if(nextAutoInvestDate==null){
//			logger.info("协议创建失败，请稍后重试");
//			res.setReturnCode(FamilyEnum.return9999.getCode());
//			res.setReturnMsg(FamilyEnum.return9999.getName());
//			return res;
//		}
//		logger.info("nextAutoInvestDate:"+nextAutoInvestDate);
		// 1. 获取可投资活期产品
		OnSaleT0ProductRep prodcutRep = productService.getOnSaleProductOid();
		if(prodcutRep == null) {
			logger.info("暂无可投资的活期产品");
			res.setReturnCode(FamilyEnum.return1004.getCode());
			res.setReturnMsg(FamilyEnum.return1004.getName());
			return res;
		}
		ProtocalEntity addproocal=new ProtocalEntity();
		addproocal.setInvestOid(req.getInvestOid());
		addproocal.setProductOid(prodcutRep.getProductOid());
		addproocal.setProtocalAmount(req.getProtocalAmount());
		addproocal.setProtocalDate(req.getProtocalDate());
		addproocal.setProtocalName(req.getProtocalName());
		addproocal.setNextInvestDate(req.getNextInvestDate());
		addproocal.setProtocalStatus(FamilyEnum.protocalStatus0.getCode());
		addproocal.setProtocalLabel(FamilyEnum.protocalName.getCode());
		addproocal.setCreateTime(DateUtil.getSqlCurrentDate()); //  
		addproocal.setUpdateTime(DateUtil.getSqlCurrentDate()); //  
		try{
			this.familyInvestPlanDao.saveAndFlush(addproocal);//新增协议信息
			/**新增理财计划日志表**/
			ProtocalLogEntity  protocalLog=new ProtocalLogEntity();
			BeanUtils.copyProperties(addproocal, protocalLog);
			protocalLog.setProtocalOid(addproocal.getOid());
			protocalLog.setOperateStatus(FamilyEnum.operateStatus0.getCode());
			protocalLog.setCreateTime(DateUtil.getSqlCurrentDate());
			this.protocalLogDao.save(protocalLog);
			res.setReturnCode(FamilyEnum.return0000.getCode());
			res.setReturnMsg("新增成功");
		}catch(Exception e){
			logger.info("理财计划创建失败,详情为:{}",e.getMessage());
			e.printStackTrace();
		}
		return res;
	}
	public PagesRep<proticalLogQueryRes> protocalLogQuery(String investorOid,int page,int size){
		PagesRep<proticalLogQueryRes> pages = new PagesRep<proticalLogQueryRes>();
		List<ProtocalLogEntity> protocalLog=this.protocalLogDao.findByInvestOid(investorOid,(page -1)*size,size);
		if(protocalLog.size()>0){
			for(ProtocalLogEntity entity:protocalLog){
				proticalLogQueryRes res=new proticalLogQueryRes();
				BeanUtils.copyProperties(entity, res);
				pages.add(res);
			}
		}
		pages.setTotal(protocalLog.size());
		return pages;
	}
	/**
	 * 修改协议信息
	 * @param req
	 * @return
	 */
	public protocalUpdRes updProtocal(protocalUpdReq req){
		protocalUpdRes res=new protocalUpdRes();
		if(StringUtil.isEmpty(req.getInvestOid())){
			res.setReturnCode(FamilyEnum.return0001.getCode());
			res.setReturnMsg(FamilyEnum.return0001.getName());
			return res;
		}
		if(StringUtil.isEmpty(req.getProductOid())){
			res.setReturnCode(FamilyEnum.return0002.getCode());
			res.setReturnMsg(FamilyEnum.return0002.getName());
			return res;
		}
//		if(StringUtil.isEmpty(req.getProtocalName())){
//			req.setProtocalName(FamilyEnum.protocalName.getName());
//		}
		if(req.getProtocalAmount().equals(new BigDecimal(0))){
			res.setReturnCode(FamilyEnum.return0007.getCode());
			res.setReturnMsg(FamilyEnum.return0007.getName());
			return res;
		}
		if(StringUtil.isEmpty(req.getProtocalOid())){
			res.setReturnCode(FamilyEnum.return0010.getCode());
			res.setReturnMsg(FamilyEnum.return0010.getName());
			return res;
		}
		if(StringUtil.isEmpty(req.getProtocalDate())){
			res.setReturnCode(FamilyEnum.return0008.getCode());
			res.setReturnMsg(FamilyEnum.return0008.getName());
			return res;
		}
		if(req.getNextInvestDate()==null){
			res.setReturnCode(FamilyEnum.return0009.getCode());
			res.setReturnMsg(FamilyEnum.return0009.getName());
			return res;
		}
		//查询用户状态
		InvestorBaseAccountEntity baseAccount = investorBaseAccountService.findByUid(req.getInvestOid());
		if(baseAccount == null || "forbidden".equals(baseAccount.getStatus())) {
			logger.info("用户状态异常，禁止修改协议，详情为:{}",JSONObject.toJSONString(baseAccount));
			res.setReturnCode(FamilyEnum.return1001.getCode());
			res.setReturnMsg(FamilyEnum.return1001.getName());
			return res;
		}
		 //查询用户是否绑卡
		 UCBankQueryRes bankinfo= userCenterSdk.queryUserBankInfo(req.getInvestOid());
		 if(!bankinfo.isIsbind()||StringUtil.isEmpty(bankinfo.getIdNumb())){
			logger.info("用户未绑卡，禁止修改协议，详情为:{}",JSONObject.toJSONString(baseAccount));
		  	res.setReturnCode(FamilyEnum.return1002.getCode());
			res.setReturnMsg(FamilyEnum.return1002.getName());
			return res;
		 }
		List<ProtocalEntity> protocal=familyInvestPlanDao.findByInvestOid(req.getInvestOid(),FamilyEnum.protocalStatus0.getCode());
		if(protocal==null||protocal.size()<=0){//协议不存在可以添加
			logger.info("协议不存在，无法修改");
			res.setReturnCode(FamilyEnum.return0000.getCode());
			res.setReturnMsg(FamilyEnum.return0000.getName());
			return res;
		}else{
			
			ProtocalEntity entite=familyInvestPlanDao.findOne(req.getProtocalOid());
			if(entite==null){
				logger.info("该协议不存在，无法修改");
				res.setReturnCode(FamilyEnum.return1000.getCode());
				res.setReturnMsg(FamilyEnum.return1000.getName());
				return res;
			}
			if(entite.getProtocalStatus().equals(FamilyEnum.protocalStatus1.getCode())){
				logger.info("该协议已作废，无法修改");
				res.setReturnCode(FamilyEnum.return1000.getCode());
				res.setReturnMsg(FamilyEnum.return1000.getName());
				return res;
			}
//			if(DateUtil.GetDateDay().equals(entite.getProtocalDate())){
//				logger.info("当天为自动扣款日禁止修改");
//				res.setReturnCode(FamilyEnum.return0005.getCode());
//				res.setReturnMsg(FamilyEnum.return0005.getName());
//				return res;
//			}else if(DateUtil.GetDateDay().equals(String.valueOf(Integer.valueOf(entite.getProtocalDate())-1))){
//				if(!DateUtil.CheckTime(familyAutoInvestDate)){
//					logger.info("明天为自动扣款日禁止修改");
//					res.setReturnCode(FamilyEnum.return0006.getCode());
//					res.setReturnMsg(FamilyEnum.return0006.getName());
//					return res;
//				}
//			}
			String LastPayDate="";
			if(entite.getLastPayDate()!=null){
		        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");   
		        try {   
		            //方法一   
		        	LastPayDate = sdf.format(entite.getLastPayDate());   
		            logger.info("最近一次扣款日期，{}",LastPayDate);
		        } catch (Exception e) {   
		            e.printStackTrace();   
		        }  
			}
			if(DateUtil.Dateequals(entite.getNextInvestDate(),0)){//当天为自动扣款日禁止修改
				logger.info("当天为自动扣款日禁止修改");
				res.setReturnCode(FamilyEnum.return0005.getCode());
				res.setReturnMsg(FamilyEnum.return0005.getName());
				return res;
			}else if(DateUtil.Dateequals(entite.getNextInvestDate(),1)){//明天为自动扣款日
				if(!DateUtil.CheckTime(familyAutoInvestDate)){
					logger.info("明天为自动扣款日禁止修改");
					res.setReturnCode(FamilyEnum.return0006.getCode());
					res.setReturnMsg(FamilyEnum.return0006.getName());
					return res;
				}
			}else if(!StringUtil.isEmpty(LastPayDate)){
				if(DateUtil.Dateequals(LastPayDate,0)){
					logger.info("当天为自动扣款日且已扣款完成");
					res.setReturnCode(FamilyEnum.return0005.getCode());
					res.setReturnMsg(FamilyEnum.return0005.getName());
					return res;
				}
			}
			OnSaleT0ProductRep prodcutRep = productService.getOnSaleProductOid();
			if(prodcutRep == null) {
				logger.info("暂无可投资的活期产品");
				res.setReturnCode(FamilyEnum.return1004.getCode());
				res.setReturnMsg(FamilyEnum.return1004.getName());
				return res;
			}
			BeanUtils.copyProperties(req, entite);
//			String nextAutoInvestDate=nextAutoInvestDate(req,req.getProtocalDate());
//			if(nextAutoInvestDate==null){
//				logger.info("协议修改失败，请稍后重试");
//				res.setReturnCode(FamilyEnum.return9999.getCode());
//				res.setReturnMsg(FamilyEnum.return9999.getName());
//				return res;
//			}
//			entite.setNextInvestDate(nextAutoInvestDate);
			entite.setUpdateTime(DateUtil.getSqlCurrentDate()); // 订单时间
			try{
				logger.info("待修改理财计划参数，{}",JSONObject.toJSONString(entite));
				 this.familyInvestPlanDao.saveAndFlush(entite);//修改协议信息
				 /**修改理财计划日志表**/
				ProtocalLogEntity  protocalLog=new ProtocalLogEntity();
				BeanUtils.copyProperties(entite, protocalLog);
				protocalLog.setOperateStatus(FamilyEnum.operateStatus1.getCode());
				protocalLog.setProtocalOid(entite.getOid());
				protocalLog.setCreateTime(DateUtil.getSqlCurrentDate());
				protocalLog.setProductOid(prodcutRep.getProductOid());
				this.protocalLogDao.save(protocalLog);
				res.setReturnCode(FamilyEnum.return0000.getCode());
				res.setReturnMsg(FamilyEnum.return0000.getName());
			}catch(Exception e){
				logger.info("理财计划修改失败,详情为:{}",e.getMessage());
				e.printStackTrace();
			}
		}
		return res;
	}
    public String nextAutoInvestDate(BaseReq req,String Date){
    	protocalInvestQueryReq queryReq=new protocalInvestQueryReq();
    	BeanUtils.copyProperties(req, queryReq);
    	protocalInvestQueryRes isInvest=this.checkInvest(queryReq);
    	if(isInvest.getReturnCode().equals(FamilyEnum.return0000.getCode())){
    		if(isInvest.getInvestCount()>0){
    			logger.info("当月有过扣款记录，开始计算下次转入日期");
    			if(DateUtil.Dateequals(DateUtil.addMonthDate(DateUtil.GetDateYear()+"-"+DateUtil.GetDateMonth()+"-"+Date,"month",1),1)){//当前是否是月末，设定日期为月初
					if(DateUtil.CheckTime(familyAutoInvestDate)){//判断是否是三点之前，三点之后禁止删除计划
						logger.info("三点前修改，当月有过自动扣款记录,下次转入日期为下个月当天");
						return DateUtil.addMonthDate(DateUtil.GetDateYear()+"-"+DateUtil.GetDateMonth()+"-"+Date,"month",1);
					}else{
						logger.info("三点后修改，当月有过自动扣款记录,下次转入日期为下下个月当天");
						return DateUtil.addMonthDate(DateUtil.GetDateYear()+"-"+DateUtil.GetDateMonth()+"-"+Date,"month",2);
					}
				}else{
					logger.info("当月不是月末，下次转入日期为下个月当天");
					return DateUtil.addMonthDate(DateUtil.GetDateYear()+"-"+DateUtil.GetDateMonth()+"-"+Date,"month",1);
				}
//    			return DateUtil.addMonthDate(DateUtil.GetDateYear()+"-"+DateUtil.GetDateMonth()+"-"+Date,"month",1);
    		}else{
    			if(DateUtil.Dateequals(DateUtil.GetDateYear()+"-"+DateUtil.GetDateMonth()+"-"+Date,0)){//判断当天是否为自动转入日期
    				logger.info("当天为系统自动扣款日,下次转入日期为下个月当天");
    				return DateUtil.addMonthDate(DateUtil.GetDateYear()+"-"+DateUtil.GetDateMonth()+"-"+Date,"month",1);
    			}else if(DateUtil.Dateequals(DateUtil.GetDateYear()+"-"+DateUtil.GetDateMonth()+"-"+Date,1)){//判断明天是否等于自动转入日期
    				if(DateUtil.CheckTime(familyAutoInvestDate)){//判断是否是三点之前，三点之后禁止删除计划
    					logger.info("三点之前修改，下次转入日期为当月当天");
    					return DateUtil.GetDateYear()+"-"+DateUtil.GetDateMonth()+"-"+Date;
    				}else{
    					logger.info("三点之后修改，下次转入日期为下月当天");
    					return DateUtil.addMonthDate(DateUtil.GetDateYear()+"-"+DateUtil.GetDateMonth()+"-"+Date,"month",1);
    				}
    			}else if(DateUtil.CheckDate(DateUtil.GetDateYear()+"-"+DateUtil.GetDateMonth()+"-"+Date)){//系统时间小于设定日期
    				return DateUtil.GetDateYear()+"-"+DateUtil.GetDateMonth()+"-"+Date;
    			}else{//系统时间大于设定日期
    				if(DateUtil.Dateequals(DateUtil.addMonthDate(DateUtil.GetDateYear()+"-"+DateUtil.GetDateMonth()+"-"+Date,"month",1),1)){//下一月的设定日期是否是
    					if(DateUtil.CheckTime(familyAutoInvestDate)){//判断是否是三点之前，三点之后禁止删除计划
    						return DateUtil.addMonthDate(DateUtil.GetDateYear()+"-"+DateUtil.GetDateMonth()+"-"+Date,"month",1);
    					}else{
    						return DateUtil.addMonthDate(DateUtil.GetDateYear()+"-"+DateUtil.GetDateMonth()+"-"+Date,"month",2);
    					}
    				}else{
    					return DateUtil.addMonthDate(DateUtil.GetDateYear()+"-"+DateUtil.GetDateMonth()+"-"+Date,"month",1);
    				}
    			}
    		}
    	}else{
    		logger.info("查询用户交易记录失败，请稍后重试，{}",JSONObject.toJSONString(isInvest));
    	}
    	return null;
    }
   public proticalChangeRes isAllowChange(String investOid){
	   proticalChangeRes res=new proticalChangeRes();
	   List<ProtocalEntity> protocal=familyInvestPlanDao.findByInvestOid(investOid,FamilyEnum.protocalStatus0.getCode());
		logger.info("用户的理财计划，{}",JSONObject.toJSONString(protocal));
		if(protocal!=null&&protocal.size()!=1){//协议不存在可以添加
			logger.info("协议不存在，无需修改或删除");
			res.setReturnCode(FamilyEnum.return1000.getCode());
			res.setReturnMsg(FamilyEnum.return1000.getName());
			return res;
		}
		ProtocalEntity entity=protocal.get(0);
		String LastPayDate="";
		if(entity.getLastPayDate()!=null){
	        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");   
	        try {   
	            //方法一   
	        	LastPayDate = sdf.format(entity.getLastPayDate());   
	            logger.info("最近一次扣款日期，{}",LastPayDate);
	        } catch (Exception e) {   
	            e.printStackTrace();   
	        }  
		}
		if(DateUtil.Dateequals(entity.getNextInvestDate(),0)){//当天为自动扣款日禁止修改
			logger.info("当天为自动扣款日且未扣款");
			res.setReturnCode(FamilyEnum.return0005.getCode());
			res.setReturnMsg(FamilyEnum.return0005.getName());
			return res;
		}else if(DateUtil.Dateequals(entity.getNextInvestDate(),1)){//明天为自动扣款日
			if(!DateUtil.CheckTime(familyAutoInvestDate)){
				logger.info("明天为自动扣款日");
				res.setReturnCode(FamilyEnum.return0006.getCode());
				res.setReturnMsg(FamilyEnum.return0006.getName());
				return res;
			}
		}else if(!StringUtil.isEmpty(LastPayDate)){
				if(DateUtil.Dateequals(LastPayDate,0)){
					logger.info("当天为自动扣款日且已扣款完成");
					res.setReturnCode(FamilyEnum.return0005.getCode());
					res.setReturnMsg(FamilyEnum.return0005.getName());
					return res;
				}
		}
		res.setReturnCode(FamilyEnum.return0000.getCode());
		res.setReturnMsg(FamilyEnum.return0000.getName());
	   return res;
   }
	 public static void main(String[] args) {
		 Timestamp ts = new Timestamp(System.currentTimeMillis());   
	        String tsStr = "";   
	        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");   
	        try {   
	            //方法一   
	            tsStr = sdf.format(ts);   
	            System.out.println(tsStr);   
	        } catch (Exception e) {   
	            e.printStackTrace();   
	        }  
	}
//	 public  boolean test(String date){
//		 if(DateUtil.Dateequals(date,0)){//当天为自动扣款日禁止修改
//				logger.info("当天为自动扣款日禁止删除");
//				return true;
//			}else if(DateUtil.Dateequals(date,1)){//明天为自动扣款日
//				if(!DateUtil.CheckTime("15:00:00")){
//					logger.info("明天为自动扣款日禁止删除");
//					return true;
//				}
//			}
//		 return false;
//	 }
	 public String testDate(String Date,String familyAutoInvestDate){
//		 if(DateUtil.Dateequals(DateUtil.GetDateYear()+"-"+DateUtil.GetDateMonth()+"-"+Date,0)){//判断当天是否为自动转入日期
//				logger.info("当天为系统自动扣款日,下次转入日期为下个月当天");
//				return DateUtil.addMonthDate(DateUtil.GetDateYear()+"-"+DateUtil.GetDateMonth()+"-"+Date,"month",1);
//			}else if(DateUtil.Dateequals(DateUtil.GetDateYear()+"-"+DateUtil.GetDateMonth()+"-"+Date,1)){//判断明天是否等于自动转入日期
//				if(DateUtil.CheckTime(familyAutoInvestDate)){//判断是否是三点之前，三点之后禁止删除计划
//					return DateUtil.GetDateYear()+"-"+DateUtil.GetDateMonth()+"-"+Date;
//				}else{
//					return DateUtil.addMonthDate(DateUtil.GetDateYear()+"-"+DateUtil.GetDateMonth()+"-"+Date,"month",1);
//				}
//			}else if(DateUtil.CheckDate(DateUtil.GetDateYear()+"-"+DateUtil.GetDateMonth()+"-"+Date)){//系统时间小于设定日期
//				return DateUtil.GetDateYear()+"-"+DateUtil.GetDateMonth()+"-"+Date;
//			}else{//系统时间大于设定日期
//				if(DateUtil.Dateequals(DateUtil.addMonthDate(DateUtil.GetDateYear()+"-"+DateUtil.GetDateMonth()+"-"+Date,"month",1),1)){//下一月的设定日期是否是
//					if(DateUtil.CheckTime(familyAutoInvestDate)){//判断是否是三点之前，三点之后禁止删除计划
//						return DateUtil.addMonthDate(DateUtil.GetDateYear()+"-"+DateUtil.GetDateMonth()+"-"+Date,"month",1);
//					}else{
//						return DateUtil.addMonthDate(DateUtil.GetDateYear()+"-"+DateUtil.GetDateMonth()+"-"+Date,"month",2);
//					}
//				}else{
//					return DateUtil.addMonthDate(DateUtil.GetDateYear()+"-"+DateUtil.GetDateMonth()+"-"+Date,"month",1);
//				}
//			}
		 if(DateUtil.Dateequals(DateUtil.addMonthDate(DateUtil.GetDateYear()+"-"+DateUtil.GetDateMonth()+"-"+Date,"month",1),1)){//当前是否是月末，设定日期为月初
				if(DateUtil.CheckTime(familyAutoInvestDate)){//判断是否是三点之前，三点之后禁止删除计划
					return DateUtil.addMonthDate(DateUtil.GetDateYear()+"-"+DateUtil.GetDateMonth()+"-"+Date,"month",1);
				}else{
					return DateUtil.addMonthDate(DateUtil.GetDateYear()+"-"+DateUtil.GetDateMonth()+"-"+Date,"month",2);
				}
			}else{
				return DateUtil.addMonthDate(DateUtil.GetDateYear()+"-"+DateUtil.GetDateMonth()+"-"+Date,"month",1);
			}
	 }
	 @Test
	 public void familySnapShotTest(){
		 FamilyInvestPlanService se=new FamilyInvestPlanService();
		 se.familySnapShot();
	 }
	

	 public void familySnapShot() {
			if (this.jobLockService.getRunPrivilege(JobLockEntity.JOB_jobId_familySnapShot)) {
				familySnapShotLog();
			}
		}
	 @Transactional(TxType.REQUIRES_NEW)
	 public void familySnapShotLog() {
		JobLogEntity jobLog = JobLogFactory.getInstance(JobLockEntity.JOB_jobId_familySnapShot);
		 Date curDate = DateUtil.getafterDate();
		 logger.info("开始将"+curDate+"待扣款的计划放到快照里");
		try {
			//删除旧数据
			this.protocalSnapshotDao.deleteShot();
			protocalSnapshotDao.snapshotPlan(curDate);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			jobLog.setJobMessage(AMPException.getStacktrace(e));
			jobLog.setJobStatus(JobLogEntity.JOB_jobStatus_failed);
		}
		jobLog.setBatchEndTime(DateUtil.getSqlCurrentDate());
		this.jobLogService.saveEntity(jobLog);
		this.jobLockService.resetJob(JobLockEntity.JOB_jobId_familySnapShot);
	}
	 /**
	  * 自动扣款
	  */
	 public void familyPlanAutoInvest() {
			if (this.jobLockService.getRunPrivilege(JobLockEntity.JOB_jobId_familyPlanAutoInvest)) {
				familyPlanAutoInvestDo();
			}
		}
	 @Transactional(TxType.REQUIRES_NEW)
	 public void familyPlanAutoInvestDo() {
		 logger.info("开始进行自动扣款");
			JobLogEntity jobLog = JobLogFactory.getInstance(JobLockEntity.JOB_jobId_familyPlanAutoInvest);
			 String lastOid = "0";
			 int group = 0;
			 try {
			  while(true){
				 List<ProtocalSnapshotEntity> protocalSnapShot=this.protocalSnapshotDao.findSnapShotAll(lastOid);
				if(protocalSnapShot.isEmpty()){
					break;
				}else{
					group++;
					for(ProtocalSnapshotEntity ProtocalSnapshot:protocalSnapShot){
						lastOid=ProtocalSnapshot.getOid()+"";
						logger.info("第" + group + "组的lastOid=" + lastOid);
						String redisHashKey=SetRedisUtil.FAMILY_PLAN_KEY_PRE+ProtocalSnapshot.getProtocalOid()+":"+ProtocalSnapshot.getProductOid()+":"+ProtocalSnapshot.getInvestOid()+":"+DateUtil.GetDateYear()+DateUtil.GetDateMonth();
						Boolean exists=SetRedisUtil.exists(redis, redisHashKey);
						Long countSet=SetRedisUtil.hIncrBy(redis, redisHashKey, SetRedisUtil.FAMILY_PLAN_COUNT_KEY_PRE, Long.valueOf(1));
						 if(exists){//当月有用户信息，说明有过扣款记录
							 String investStatus=SetRedisUtil.hGet(redis, redisHashKey, SetRedisUtil.FAMILY_PLAN_STATUS_KEY_PRE);
							 if(!investStatus.equals(FamilyEnum.investStatus2.getCode())){//查询任务是否还未执行完成
								 logger.info("有未完成的任务，请勿重复执行，{}",JSONObject.toJSONString(ProtocalSnapshot));
								 continue;
							 } 
						 }
//						 logger.info("用户当月首次进行扣款,{}",JSONObject.toJSONString(ProtocalSnapshot));
						 if(countSet<=3){
							 Boolean statusSet=SetRedisUtil.hSet(redis, redisHashKey, SetRedisUtil.FAMILY_PLAN_STATUS_KEY_PRE, FamilyEnum.investStatus3.getCode());
							 logger.info("扣款计划执行状态设置状态，"+statusSet);
							 //查询用户是否绑卡
							 UCBankQueryRes bankinfo= userCenterSdk.queryUserBankInfo(ProtocalSnapshot.getInvestOid());
							 if(!bankinfo.isIsbind()||StringUtil.isEmpty(bankinfo.getIdNumb())){
								 logger.info("用户未绑卡，请先绑卡");
								 this.payFail(ProtocalSnapshot.getInvestOid(),FamilyEnum.return1002.getName());
								  continue;
							 }else{
								 TradeOrderReq tradeOrderReq=new TradeOrderReq();
								 tradeOrderReq.setProductOid(ProtocalSnapshot.getProductOid());
								 tradeOrderReq.setProtocalOid(ProtocalSnapshot.getProtocalOid());
								 tradeOrderReq.setUid(ProtocalSnapshot.getInvestOid());
								 tradeOrderReq.setMoneyVolume(ProtocalSnapshot.getProtocalAmount());
									 try{
										 this.investorInvestTradeOrderExtService.autoInvest(tradeOrderReq);
									 }catch(Exception e){
										 e.printStackTrace();
										  continue;
									 }
							 }
						 }else{
							 logger.info("该用户已经执行过三次自动扣款任务，请勿重复执行，{}",JSONObject.toJSONString(ProtocalSnapshot));
							 this.payFail(ProtocalSnapshot.getInvestOid(),FamilyEnum.return1003.getName());
							 continue;
						 }
					}
					logger.info("-----------第"+group+"组订单发送成功，暂停3秒------");
					try {
						Thread.currentThread().sleep(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			 }
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				jobLog.setJobMessage(AMPException.getStacktrace(e));
				jobLog.setJobStatus(JobLogEntity.JOB_jobStatus_failed);
			}
			jobLog.setBatchEndTime(DateUtil.getSqlCurrentDate());
			this.jobLogService.saveEntity(jobLog);
			this.jobLockService.resetJob(JobLockEntity.JOB_jobId_familyPlanAutoInvest);
		}
	 public void paySuccess(String investorOid){
		List<ProtocalEntity> protocal = familyInvestPlanDao.findByInvestOid(investorOid, FamilyEnum.protocalStatus0.getCode());
		log.info("回调时查询用户协议信息：{}",JSONObject.toJSONString(protocal));
		if(protocal.size()<=0){
			return;
		}
		ProtocalEntity entity=protocal.get(0);
		String redisHashKey=SetRedisUtil.FAMILY_PLAN_KEY_PRE+entity.getOid()+":"+entity.getProductOid()+":"+entity.getInvestOid()+":"+DateUtil.GetDateYear()+DateUtil.GetDateMonth();
		logger.info("redisKey:"+redisHashKey);
		SetRedisUtil.hSet(redis, redisHashKey, SetRedisUtil.FAMILY_PLAN_STATUS_KEY_PRE, FamilyEnum.investStatus1.getCode());
		this.protocalSnapshotDao.deleteByinvestOid(entity.getInvestOid());//
//		String nextInvestDate=DateUtil.addMonthDate(String.valueOf(DateUtil.getSqlDate()), "month",1);//设置自动转入日
		String nextInvestDate=DateUtil.addMonthDate(String.valueOf(entity.getNextInvestDate()), "month",1);//设置自动转入日
		entity.setNextInvestDate(nextInvestDate);
		entity.setLastPayDate(DateUtil.getSqlCurrentDate());
		entity.setUpdateTime(DateUtil.getSqlCurrentDate());
		this.familyInvestPlanDao.save(entity);
		return;
	 }
	 public void payFail(String investorOid,String returnMsg){
		 if(StringUtil.isEmpty(returnMsg)){
			 returnMsg="交易失败";
		 }
		 List<ProtocalEntity> protocal = familyInvestPlanDao.findByInvestOid(investorOid, FamilyEnum.protocalStatus0.getCode());
			if(protocal.size()<=0){
				return;
			}
			log.info("回调时查询用户协议信息：{}",JSONObject.toJSONString(protocal));
		 ProtocalEntity entity=protocal.get(0);
		 String redisHashKey=SetRedisUtil.FAMILY_PLAN_KEY_PRE+entity.getOid()+":"+entity.getProductOid()+":"+entity.getInvestOid()+":"+DateUtil.GetDateYear()+DateUtil.GetDateMonth();
		 if(SetRedisUtil.exists(redis, redisHashKey)){
			 Boolean statusSet=SetRedisUtil.hSet(redis, redisHashKey, SetRedisUtil.FAMILY_PLAN_STATUS_KEY_PRE, FamilyEnum.investStatus2.getCode());
			 logger.info("设置状态修改，{}",statusSet);
			 String countSet=SetRedisUtil.hGet(redis, redisHashKey, SetRedisUtil.FAMILY_PLAN_COUNT_KEY_PRE);
			 /**修改最近一次扣款时间**/
			 entity.setLastPayDate(DateUtil.getSqlCurrentDate());
			 entity.setUpdateTime(DateUtil.getSqlCurrentDate());
			 if(Integer.valueOf(countSet)>=3){
				 String nextInvestDate=DateUtil.addMonthDate(String.valueOf(entity.getNextInvestDate()), "month",1);//设置自动转入日
				 entity.setNextInvestDate(nextInvestDate);
//				 entity.setUpdateTime(DateUtil.getSqlCurrentDate());
				 logger.info("删除用户快照信息，{}",entity.getInvestOid());
				 this.protocalSnapshotDao.deleteByinvestOid(entity.getInvestOid());//
			 }
			 this.familyInvestPlanDao.save(entity);
			 
			 Boolean RemarkSet=SetRedisUtil.hSet(redis, redisHashKey, SetRedisUtil.FAMILY_PLAN_REMARK_KEY_PRE,returnMsg);
			 logger.info("设置失败描述，{}",RemarkSet);
			 if(Integer.valueOf(countSet)==1){
				 /**如果是第一次失败**/
				 logger.info("交易失败发送站内信通知,{},{}",investorOid,returnMsg);
				 sendMessage(investorOid,returnMsg);
				 /****/
			 }
		 }
	 }
	 /**站内信通知**/
	 private void sendMessage(String investorOid,String returnMsg) {
		 List<ProtocalEntity> protocal = familyInvestPlanDao.findByInvestOid(investorOid, FamilyEnum.protocalStatus0.getCode());
			if(protocal.size()<=0){
				return;
			}
			/**查询用户手机号**/
			InvestorBaseAccountEntity account=  this.investorBaseAccountDao.findOne(investorOid);
			if(account.getPhoneNum()==null){
				return;
			}
			ProtocalEntity entity=protocal.get(0);
			String tag = "";
			DealMessageEntity messageEntity = new DealMessageEntity();
			messageEntity.setProtocalName(entity.getProtocalName());
			messageEntity.setPhone(account.getPhoneNum());
			messageEntity.setProductName(FamilyEnum.protocalName.getName());
			messageEntity.setMonth(DateUtil.GetDateMonth());
			messageEntity.setOrderAmount(String.valueOf(entity.getProtocalAmount()));
			UCBankQueryRes bankinfo= userCenterSdk.queryUserBankInfo(investorOid);
			 if(!bankinfo.isIsbind()||StringUtil.isEmpty(bankinfo.getIdNumb())){
				 messageEntity.setFaultBankCardNum("");
			 }else{
				 String bankNo=bankinfo.getCardNumb();
				 messageEntity.setFaultBankCardNum(bankNo.substring(bankNo.length()-4,bankNo.length()));//后四位
			 }
			 boolean needSend = false;
			 if(returnMsg.equals(FamilyEnum.return1002.getName())){
				 needSend=true;
				 tag = DealMessageEnum.AUTO_INVEST_FAIL_BANK.name();
			 }else{
				 needSend=true;
				 tag = DealMessageEnum.AUTO_INVEST_FAIL.name();
			 }
			if (needSend) {
				logger.info("待推送内容,{}",JSONObject.toJSONString(messageEntity));
				messageSendUtil.sendTopicMessage(messageSendUtil.getDealTopic(), tag, messageEntity);
			}
		}
}