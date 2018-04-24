package com.guohuai.mmp.investor.baseaccount.refer.details;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.StringUtil;
import com.guohuai.component.web.view.PagesRep;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountEntity;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountService;
import com.guohuai.mmp.investor.baseaccount.referee.InvestorRefEreeEntity;
import com.guohuai.mmp.investor.baseaccount.referee.InvestorRefEreeService;
import com.guohuai.plugin.PageVo;
import com.guohuai.usercenter.api.UserCenterSdk;
import com.guohuai.usercenter.api.obj.RecBankInfoRep;

import lombok.extern.slf4j.Slf4j;

/**
 * 推荐人查询
 * @author wanglei
 *
 */
@Slf4j
@Service
@Transactional
public class InvestoRefErDetailsService {

	@Autowired
	InvestoRefErDetailsDao investoRefErDetailsDao;
	@Autowired
	InvestorBaseAccountService investorBaseAccountService;
	@Autowired
	InvestorRefEreeService investorRefEreeService;
	@Autowired
	UserCenterSdk ucSdk;

	/**我推荐的列表*/
	public PagesRep<InvestoRefErDetailsRep> referlist(Specification<InvestoRefErDetailsEntity> spec,
			Pageable pageable) {

		PagesRep<InvestoRefErDetailsRep> rep = new PagesRep<InvestoRefErDetailsRep>();

		Page<InvestoRefErDetailsEntity> page = this.investoRefErDetailsDao.findAll(spec, pageable);
		if (page != null && page.getSize() > 0 && page.getTotalElements() > 0) {
			
			List<InvestoRefErDetailsRep> rows = new ArrayList<InvestoRefErDetailsRep>();
			
			for (InvestoRefErDetailsEntity entity : page) {
				InvestoRefErDetailsRep row = new InvestoRefErDetailsRep(entity);
				rows.add(row);
			}
			rep.setRows(rows);
			rep.setTotal(page.getTotalElements());
		}

		return rep;
	}
	/**
	 * 查询根据id 邀请列表
	 * @return
	 */
	public PagesRep<InvestoRefErDetailsRankRep> myReferList( String investorOid,int  page,int size) {
			log.info("邀请人列表查询,{},{},{},",investorOid,page,size);
			PagesRep<InvestoRefErDetailsRankRep> pages = new PagesRep<InvestoRefErDetailsRankRep>();
			Object countSum=this.investoRefErDetailsDao.myReferListSumCount(investorOid);
			Object[] countSums=(Object[])countSum;
			System.out.println(countSums.length);
			if(countSums.length>0){
				if( Integer.valueOf(String.valueOf( countSums[0])) > 0){
					List<Object[]> tops = this.investoRefErDetailsDao.myReferList(investorOid,(page -1)*size,size);
					for (Object[] obj : tops) {
						if (null != obj&&null != obj[1]) {
							InvestoRefErDetailsRankRep rep = new InvestoRefErDetailsRankRep();
							//　手机号
							rep.setPhoneNum(null == obj[1] ? StringUtil.EMPTY : obj[1].toString());
							//　实名认证
							rep.setRealName(null == obj[0] ? StringUtil.EMPTY : obj[0].toString());
							// 推荐人数
							pages.add(rep);
						}
					}	
				}
			}
			pages.setTotal( Integer.valueOf(String.valueOf( countSums[0])));
			pages.setRealTotal( Integer.valueOf(String.valueOf( countSums[1])));
			return pages;
	}
 
	/**
	 * 根据被邀请人获取邀请记录
	 * @param account
	 * @return
	 */
	public InvestoRefErDetailsEntity findByAccount(InvestorBaseAccountEntity account) {
		return this.investoRefErDetailsDao.findByInvestorBaseAccount(account);
	}

	/**
	 * 推荐排名统计，前10名
	 * @return
	 */
	public PagesRep<InvestoRefErDetailsRankRep> recommendRankTOP10() {
		PagesRep<InvestoRefErDetailsRankRep> pages = new PagesRep<InvestoRefErDetailsRankRep>();

		List<Object[]> tops = this.investoRefErDetailsDao.recommendRankTOP10();
		
		for (Object[] obj : tops) {
			if (null != obj) {
				InvestoRefErDetailsRankRep rep = new InvestoRefErDetailsRankRep();
				//　手机号
				rep.setPhoneNum(StringUtil.kickstarOnPhoneNum(null == obj[0] ? StringUtil.EMPTY : obj[0].toString()));
				//　实名认证
				rep.setRealName(StringUtil.kickstarOnRealname(null == obj[1] ? StringUtil.EMPTY : obj[1].toString()));
				// 推荐人数
				rep.setRecommendCount(obj[2].toString());
				pages.add(rep);
			}
		}
		pages.setTotal(tops.size());
		
		return pages;
	}
	
	/**
	 * 推荐人统计（注册，绑卡分开统计）
	 * @return
	 */
	public InvestoRefErDetailsCountRep recommendCountInfo(String userOid) {
		InvestoRefErDetailsCountRep rep = new InvestoRefErDetailsCountRep();

		InvestorBaseAccountEntity account = this.investorBaseAccountService.findByUid(userOid);
		
		InvestorRefEreeEntity ref = this.investorRefEreeService.getInvestorRefEreeByAccount(account);
		
		if (null != ref) {
			List<Object[]> recPeople = this.investoRefErDetailsDao.getRecPeopleInfo(ref.getOid());
			//--------被邀请人绑卡时间bug----2017.07.24---
			List<Object[]> recBindBank1 = this.investoRefErDetailsDao.getRecBindBankPeopleInfo(ref.getOid());
			RecBankInfoRep recBindBank = this.ucSdk.getRecBindBankPeopleInfo(userOid);
			log.info("uc查出的被邀请人绑卡信息:{}",JSON.toJSONString(recBindBank));
			//--------被邀请人绑卡时间bug----2017.07.24---
			List<InvestoRefErDetailsRep> peopleList = new ArrayList<InvestoRefErDetailsRep>();
			
			for (Object[] obj : recPeople) {
				if (null != obj) {
					InvestoRefErDetailsRep peopleRep = new InvestoRefErDetailsRep();
					//　手机号
					peopleRep.setPhone(StringUtil.kickstarOnPhoneNum(null == obj[0] ? StringUtil.EMPTY : obj[0].toString()));
					//　时间
					peopleRep.setDate(DateUtil.parse(obj[1].toString(), DateUtil.datetimePattern));
					peopleList.add(peopleRep);
				}
			}
			
			List<InvestoRefErDetailsRep> bindBankList = new ArrayList<InvestoRefErDetailsRep>();
			List<String[]> recBindBankObjs = recBindBank.getObjs();
//			for (Object[] obj : recBindBankObjs) {
			for(int i=0;i<recBindBankObjs.size()&&i<recBindBank1.size();i++){
				String[] obj = recBindBankObjs.get(i);
				Object[] obj1 = recBindBank1.get(i);
				if (null != obj) {
					InvestoRefErDetailsRep bindBankRep = new InvestoRefErDetailsRep();
					//　手机号
					bindBankRep.setPhone(StringUtil.kickstarOnPhoneNum(null == obj1[0] ? StringUtil.EMPTY : obj1[0].toString()));
					//　时间
					bindBankRep.setDate(DateUtil.parse(obj[1].toString(), DateUtil.datetimePattern));
					bindBankList.add(bindBankRep);
				}
			}
//			}
			rep.setRecommendInfo(peopleList);
			rep.setBindBankInfo(bindBankList);
			rep.setRegistNum(String.valueOf(recPeople.size()));
			rep.setBindBankNum(String.valueOf(recBindBank1.size()));
			rep.setFriendInvestCount(this.investoRefErDetailsDao.queryFriendInvestRecordCount(userOid));
		}
		
		return rep;
	}
	/**
	 * 
	 * @author yihonglei
	 * @Title: queryRecommendInfo
	 * @Description:查询推荐列表
	 * @param req
	 * @return ResponseEntity<PageVo<Map<String,Object>>>
	 * @date 2017年5月31日 下午7:04:22
	 * @since  1.0.0
	 */
	public InvestoRefErDetailsVoRep<Map<String, Object>> queryRecommendInfo(String userOid, InvestoRefErDetailsReq req) {
		// 分页对象
		InvestoRefErDetailsVoRep<Map<String, Object>> pageVo = new InvestoRefErDetailsVoRep<Map<String,Object>>();
		// 推荐列表
		List<Object[]> objRecList = new ArrayList<Object[]>();
		List<Object[]> countStat = new ArrayList<Object[]>();
		// 总条数
		int total = 0;
		// 相关查询
		objRecList = this.investoRefErDetailsDao.queryRecommendInfo(userOid, (req.getPage() - 1) * req.getRow(), req.getRow());
		total = this.investoRefErDetailsDao.queryRecommendInfoCount(userOid);
		countStat = this.investoRefErDetailsDao.queryRecommendStat(userOid);
		// 处理数据列表
		List<Map<String,Object>> recList = new ArrayList<Map<String,Object>>();
		for (int i=0;i<objRecList.size();i++) {
			Object[] objData = objRecList.get(i);
			Map<String,Object> mapData = new HashMap<String,Object>();
			mapData.put("phoneNum", StringUtil.kickstarOnPhoneNum(null == objData[0] ? StringUtil.EMPTY : objData[0].toString()));// 手机号
			mapData.put("createTime", objData[1].toString());// 注册时间
			String realName = objData[2].toString();
			mapData.put("realName", StringUtils.isBlank(realName)?"":StringUtil.kickstarOnRealname(realName));// 姓名
			mapData.put("isBankStr", objData[3].toString());// 是否绑卡
			mapData.put("isInvestor", objData[4].toString());// 是否投资
			
			recList.add(mapData);
		}
		
		if (countStat.size() > 0) {
			// 注册，绑卡数汇总
			Object[] objCount = countStat.get(0);
			pageVo.setRegistNum(objCount[0].toString());// 注册人数
			pageVo.setBindBankNum(objCount[1].toString());// 绑卡人数
			pageVo.setFriendInvestCount(this.investoRefErDetailsDao.queryFriendInvestRecordCount(userOid));// 好友投资笔数
		}
		
		// 分页处理
		pageVo.setRows(recList);
		pageVo.setTotal(total);
		pageVo.setRow(req.getRow());
		pageVo.setPage(req.getPage());
		pageVo.reTotalPage();
		
		return pageVo;
	}
	/**
	 * 
	 * @author yihonglei
	 * @Title: queryFriendInvestRecord
	 * @Description: 查询好友投资列表
	 * @param userOid
	 * @param req
	 * @return PageVo<Map<String,Object>>
	 * @date 2017年6月16日 下午7:10:18
	 * @since  1.0.0
	 */
	public PageVo<Map<String, Object>> queryFriendInvestRecord(String userOid, InvestoRefErDetailsReq req) {
		// 分页对象
		PageVo<Map<String, Object>> pageVo = new PageVo<Map<String,Object>>();
		// 查询好友投资列表
		List<Object[]> objRefRecordList = new ArrayList<Object[]>();
		// 总条数
		int total = 0;
		// 相关查询
		objRefRecordList = this.investoRefErDetailsDao.queryFriendInvestRecord(userOid, (req.getPage() - 1) * req.getRow(), req.getRow());
		total = this.investoRefErDetailsDao.queryFriendInvestRecordCount(userOid);
		// 处理数据列表
		List<Map<String,Object>> recList = new ArrayList<Map<String,Object>>();
		for (int i=0;i<objRefRecordList.size();i++) {
			Object[] objData = objRefRecordList.get(i);
			Map<String,Object> mapData = new HashMap<String,Object>();
			mapData.put("phoneNum", objData[0].toString());// 好友用户名
			mapData.put("orderTime", objData[1].toString());// 交易时间
			
			recList.add(mapData);
		}
		// 分页处理
		pageVo.setRows(recList);
		pageVo.setTotal(total);
		pageVo.setRow(req.getRow());
		pageVo.setPage(req.getPage());
		pageVo.reTotalPage();
		
		return pageVo;
	}
	
	/**
	 * 新增
	 * @param entity
	 * @return
	 */
	public InvestoRefErDetailsEntity saveEntity(InvestoRefErDetailsEntity entity){
		entity.setCreateTime(DateUtil.getSqlCurrentDate());
		return this.updateEntity(entity);
	}
	
	/**
	 * 修改
	 * @param entity
	 * @return
	 */
	private InvestoRefErDetailsEntity updateEntity(InvestoRefErDetailsEntity entity) {
		entity.setUpdateTime(DateUtil.getSqlCurrentDate());
		return this.investoRefErDetailsDao.save(entity);
	}
	
	/**
	 * 根据被邀请人OID获取邀请记录
	 * @param investorOid
	 * @return
	 */
	public InvestoRefErDetailsEntity getRefErDetailsByInvestorOid(String investorOid) {
		return this.investoRefErDetailsDao.getRefErDetailsByInvestorOid(investorOid);
	}
}
