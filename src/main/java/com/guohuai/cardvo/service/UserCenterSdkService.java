package com.guohuai.cardvo.service;

import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.basic.cardvo.req.userInfoReq.MUAllReq;
import com.guohuai.usercenter.api.UserCenterSdk;

import lombok.extern.slf4j.Slf4j;

/**
 * 方便mimosasdk调用ucsdk的接口，暂时没用到
 * @author yujianlong
 *
 */
@Service
@Slf4j
@Transactional
@Deprecated
public class UserCenterSdkService {
	@Autowired
	UserCenterSdk userCenterSdk;
	
	
	
	
	
	
	
	
	public  Long  countNumUserOnly( MUAllReq mUAllReq){
		return userCenterSdk.countNumUserOnly(mUAllReq);
	}
	public Long  countNumJoinUserAndBank( MUAllReq mUAllReq){
		return userCenterSdk.countNumJoinUserAndBank(mUAllReq);
	}
	public   List<String>  query2IdListOnlyUser( MUAllReq mUAllReq){
		return userCenterSdk.query2IdListOnlyUser(mUAllReq);
	}
	public  List<String>  query2IdListJoinUserAndBank( MUAllReq mUAllReq){
		return userCenterSdk.query2IdListJoinUserAndBank(mUAllReq);
	}
	public   List<Map<String, Object>>  query2MapsOnlyUser( MUAllReq mUAllReq){
		return userCenterSdk.query2MapsOnlyUser(mUAllReq);
	}
//	public   List<Map<String, Object>>  query2MapsOnlyUserSpecial( MUAllReq mUAllReq){
//		return userCenterSdk.query2MapsOnlyUserSpecial(mUAllReq);
//	}
	public   List<Map<String, Object>>  query2MapsJoinUserAndBank( MUAllReq mUAllReq){
		return userCenterSdk.query2MapsJoinUserAndBank(mUAllReq);
	}
	
}
