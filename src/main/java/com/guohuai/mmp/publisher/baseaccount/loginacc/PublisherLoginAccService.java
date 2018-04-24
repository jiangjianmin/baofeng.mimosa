package com.guohuai.mmp.publisher.baseaccount.loginacc;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.component.exception.AMPException;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountEntity;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountService;

@Service
@Transactional
public class PublisherLoginAccService {
	
	@Autowired
	private PublisherLoginAccDao publisherLoginAccDao;
	@Autowired
	private PublisherBaseAccountService publisherBaseAccountService;

	public PublisherLoginAccEntity save(PublisherLoginAccEntity loginAcc) {
		return this.publisherLoginAccDao.save(loginAcc);
		
	}

	public PublisherBaseAccountEntity findByLoginAcc(String uid) {
		PublisherLoginAccEntity entity = this.publisherLoginAccDao.findByLoginAcc(uid);
		if (null == entity) {
			entity = this.publisherLoginAccDao.findByLoginAcc("HOMEPAGE");//获取默认登录账号和发行账号关联关系（试用账号用到）
			if (entity == null) {
				//error.define[30056]=发行人账户不存在(CODE:30056)
				throw new AMPException(30056);
			}
		}
		return entity.getPublisherBaseAccount();
	}
	
	
	public boolean isExistsLoginAcc(String uid) {
		PublisherLoginAccEntity entity = this.publisherLoginAccDao.findByLoginAcc(uid);
		if (null != entity) {
			return true;
		}
		return false;
	}
	
	/**
	 *查询所有已分配 loginAcc 用予新建发行人时过滤
	 */
	public AllAccRep allAcc() {
		AllAccRep rep = new AllAccRep();
		List<String> allAcc = this.publisherLoginAccDao.findAllAcc();
		rep.setAllAcc(allAcc);
		return rep;
	}
	
	/**
	 *查询所有已分配 loginAcc,并去掉当前发行人的loginAcc   用予修改发行人时过滤
	 */
	public AllAccRep allAcc4Modify(String corperateOid) {
		AllAccRep rep = new AllAccRep();
		List<String> allAcc = this.publisherLoginAccDao.findAllAcc();
		List<String> selectedAcc = new ArrayList<String>();
		PublisherBaseAccountEntity baseAccount = this.publisherBaseAccountService.findByCorperateOid(corperateOid);
		List<PublisherLoginAccEntity> loginAccList = this.findByPublisherBaseAccount(baseAccount);
		for (PublisherLoginAccEntity loginAcc : loginAccList) {
			allAcc.remove(loginAcc.getLoginAcc());
			selectedAcc.add(loginAcc.getLoginAcc());
			
		}
		rep.setSelectedAcc(selectedAcc);
		rep.setAllAcc(allAcc);
		return rep;
	}

	private List<PublisherLoginAccEntity> findByPublisherBaseAccount(PublisherBaseAccountEntity baseAccount) {
		
		return this.publisherLoginAccDao.findByPublisherBaseAccount(baseAccount);
	}

	public BaseRep upLoginAcc(UpLoginAccReq upLoginAccReq) {
		PublisherBaseAccountEntity baseAccount = this.publisherBaseAccountService.findByCorperateOid(upLoginAccReq.getCorperateOid());
		List<PublisherLoginAccEntity> loginAccList = this.findByPublisherBaseAccount(baseAccount);
		this.publisherLoginAccDao.delete(loginAccList);
		this.publisherLoginAccDao.flush();
		String[] userOids = upLoginAccReq.getUserOids();
		if (null != userOids && 0 != userOids.length) {
			for (String userOid : userOids) {
				if (this.isExistsLoginAcc(userOid)) {
					// error.define[30059]=用户已有所属发行人(CODE:30059)
					throw new AMPException(30059);
				}
				PublisherLoginAccEntity entity = new PublisherLoginAccEntity();
				entity.setPublisherBaseAccount(baseAccount);
				entity.setLoginAcc(userOid);
				this.save(entity);
			}
		}
		
		return new BaseRep();
	}
	

	
	
	
	
}
