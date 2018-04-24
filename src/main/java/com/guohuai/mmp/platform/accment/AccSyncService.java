package com.guohuai.mmp.platform.accment;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountEntity;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountService;
import com.guohuai.mmp.platform.baseaccount.PlatformBaseAccountEntity;
import com.guohuai.mmp.platform.baseaccount.PlatformBaseAccountService;
import com.guohuai.mmp.platform.reserved.account.ReservedAccountEntity;
import com.guohuai.mmp.platform.reserved.account.ReservedAccountService;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class AccSyncService {
	
	@Autowired
	private ReservedAccountService reservedAccountService;
	@Autowired
	private InvestorBaseAccountService investorBaseAccountService;
	@Autowired
	private PlatformBaseAccountService platformBaseAccountService;
	@Autowired
	private AccmentService accmentService;
	
	public void test() {
		init();
	}
	
//	@PostConstruct
	void init() {
		
		UserQueryIRep irep = this.accmentService.queryPlatformUser();
		if (irep.getErrorCode() == 0) {
			
			syncReservedAccount(irep.getUserOid());
			syncSuperAccount(irep.getUserOid());
			syncPlatformAccount(irep.getUserOid());
			syncOperationAccount(irep.getUserOid());
		}
		
		
	}
	
	/**
	 * 运营户
	 */
	private void syncOperationAccount(String platformUserOid) {
		log.info(" syncOperationAccount start");
		AccountQueryIRequest ireq = new AccountQueryIRequest();
		ireq.setUserOid(platformUserOid);
		ireq.setUserType(AccParam.UserType.PLATFORM.toString());
		ireq.setAccountType(AccParam.AccountType.OPERATION.toString());
		AccountQueryIRep irep = this.accmentService.accountQueryList(ireq);
		if (irep.getErrorCode() == 0) {
			String uid = irep.getUserOid();
			ReservedAccountEntity entity = this.reservedAccountService.getReservedAccount();
			if (null == entity) {
				log.info(" syncPlatformAccount already failed");
			} else {
				entity.setOperationId(uid);
				this.reservedAccountService.saveEntity(entity);
				log.info(" syncPlatformAccount already success");
			}
		}
		log.info(" syncOperationAccount end");
		
	}
	
	/**
	 * 平台户
	 */
	private void syncPlatformAccount(String platformUserOid) {
		log.info(" syncPlatformAccount start");
		AccountQueryIRequest ireq = new AccountQueryIRequest();
		ireq.setUserOid(platformUserOid);
		ireq.setUserType(AccParam.UserType.PLATFORM.toString());
		ireq.setAccountType(AccParam.AccountType.PLATFORM.toString());
		AccountQueryIRep irep = this.accmentService.accountQueryList(ireq);
		if (irep.getErrorCode() == 0) {
			String uid = irep.getUserOid();
			PlatformBaseAccountEntity entity = this.platformBaseAccountService.getPlatfromBaseAccount();
			if (null == entity) {
				log.info(" syncPlatformAccount already failed");
			} else {
				entity.setPlatformUid(uid);;
				this.platformBaseAccountService.saveEntity(entity);
				log.info(" syncPlatformAccount already success");
			}
		}
		log.info(" syncPlatformAccount end");
		
	}
	
	/**
	 * 超级户
	 */
	private void syncSuperAccount(String platformUserOid) {
		log.info(" syncSuperAccount start");
		AccountQueryIRequest ireq = new AccountQueryIRequest();
		ireq.setUserOid(platformUserOid);
		ireq.setUserType(AccParam.UserType.PLATFORM.toString());
		ireq.setAccountType(AccParam.AccountType.SUPERACCOUNT.toString());
		AccountQueryIRep irep = this.accmentService.accountQueryList(ireq);
		if (irep.getErrorCode() == 0) {
			String uid = irep.getUserOid();
			InvestorBaseAccountEntity entity = this.investorBaseAccountService.getSuperInvestor();
			if (null == entity) {
				log.info(" syncSuperAccount already failed");
			} else {
				entity.setMemberId(uid);
				this.investorBaseAccountService.saveEntity(entity);
				log.info(" syncSuperAccount already success");
			}
		}
		log.info(" syncSuperAccount end");
		
	}
	
	/**
	 * 备付金
	 */
	private void syncReservedAccount(String platformUserOid) {
		log.info(" syncReservedAccount start");
		AccountQueryIRequest ireq = new AccountQueryIRequest();
		ireq.setUserOid(platformUserOid);
		ireq.setUserType(AccParam.UserType.PLATFORM.toString());
		ireq.setAccountType(AccParam.AccountType.RESERVEDACCOUNT.toString());
		AccountQueryIRep irep = this.accmentService.accountQueryList(ireq);
		if (irep.getErrorCode() == 0) {
			String uid = irep.getUserOid();
			ReservedAccountEntity entity = this.reservedAccountService.getReservedAccount();
			if (null == entity) {
				log.info(" syncReservedAccount already failed");
			} else {
				entity.setReservedId(uid);
				this.reservedAccountService.saveEntity(entity);
				log.info(" syncReservedAccount already success");
			}
		}
		log.info(" syncReservedAccount end");
	}

	
}
