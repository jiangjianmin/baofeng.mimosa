package com.guohuai.mmp.platform.finance.data;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.mmp.platform.finance.check.PlatformFinanceCheckEntity;
import com.guohuai.mmp.platform.finance.check.PlatformFinanceCheckNewService;
import com.guohuai.mmp.serialtask.SerialTaskEntity;
import com.guohuai.mmp.serialtask.SerialTaskReq;
import com.guohuai.mmp.serialtask.SerialTaskRequireNewService;
import com.guohuai.mmp.serialtask.SerialTaskService;
import com.guohuai.mmp.serialtask.SynCompareDataParams;
import com.guohuai.settlement.api.SettlementSdk;
import com.guohuai.settlement.api.request.OrderAccountRequest;
import com.guohuai.settlement.api.response.OrderAccountResponse;

@Service
@Transactional
public class PlatformFinanceCompareDataService {
	@Autowired
	private PlatformFinanceCompareDataNewService platformFinanceCompareDataNewService;
	@Autowired
	private SettlementSdk settlementSdk;
	@Autowired
	private PlatformFinanceCheckNewService platformFinanceCheckNewService;
	@Autowired
	private SerialTaskService serialTaskService;
	@Autowired
	private SerialTaskRequireNewService serialTaskRequireNewService;
	/**
	 * 同步对账数据
	 */
	public BaseRep synCompareData(String date){
		BaseRep rep = new BaseRep();
		Date checkDate = DateUtil.parseToSqlDate(date);
		PlatformFinanceCheckEntity pfcEntity = platformFinanceCheckNewService
				.findByCheckDate(checkDate);
		if (null == checkDate) {
			throw new AMPException("尚未生成对账批次");
		}
		platformFinanceCheckNewService.syncing(pfcEntity.getOid());
		new Thread(new Runnable() {
			@Override
			public void run() {
				synCompareDataDo(date);
			}
		}).start();
//		synCompareDataDo(date);
		return rep;
	}

	public void synCompareDataDo(String date) {

		PlatformFinanceCheckEntity pfcEntity = platformFinanceCheckNewService
				.findByCheckDate(DateUtil.parseToSqlDate(date));
		int totalCount = 0;
		platformFinanceCompareDataNewService.deleteByBuzzDate(date);
		OrderAccountRequest req = new OrderAccountRequest();
		req.setBeginTime(DateUtil.convertDate2String(DateUtil.fullDatePattern,pfcEntity.getBeginTime()));
		req.setEndTime(DateUtil.convertDate2String(DateUtil.fullDatePattern, pfcEntity.getEndTime()));
		req.setCountNum(0L);

		while (true) {
			List<PlatformFinanceCompareDataEntity> fcdList = new ArrayList<PlatformFinanceCompareDataEntity>();
			List<OrderAccountResponse> resList = null;
			try {
				resList = settlementSdk.getChackCompareData(req);
			} catch (Exception e) {
				platformFinanceCheckNewService.syncFailed(pfcEntity.getOid());
				throw e;
			}

			if (null == resList || resList.isEmpty()) {
				break;
			}
			PlatformFinanceCompareDataEntity fcd = null;
			Timestamp ordertime = new Timestamp(System.currentTimeMillis());
			for (OrderAccountResponse entity : resList) {
				fcd = new PlatformFinanceCompareDataEntity();
				fcd.setBuzzDate(DateUtil.parseToSqlDate(date));
				fcd.setInvestorOid(entity.getInvestorOid());
				fcd.setOrderAmount(entity.getOrderAmount());
				fcd.setOrderCode(entity.getOrderCode());
				fcd.setOrderStatus(entity.getOrderStatus());
				fcd.setOrderType(entity.getOrderType());
				fcd.setCreateTime(ordertime);
				fcd.setUpdateTime(ordertime);
				fcd.setCheckStatus(PlatformFinanceCompareDataEntity.CHECKSTATUS_NO);
				if(!fcd.getOrderStatus().equals("5")){
					fcdList.add(fcd);
				}
				req.setCountNum(entity.getCountNum());
			}
			totalCount += fcdList.size();
			this.platformFinanceCompareDataNewService.save(fcdList);
		}
		platformFinanceCheckNewService.syncOK(pfcEntity.getOid(), totalCount);

	}
}