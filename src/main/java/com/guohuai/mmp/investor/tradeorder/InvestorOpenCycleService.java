package com.guohuai.mmp.investor.tradeorder;

import com.guohuai.mmp.investor.tradeorder.cycleProduct.BookingOrderEMDao;
import com.guohuai.mmp.investor.tradeorder.cycleProduct.BookingOrderReq;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;

/**
 *
 *开放循环产品关系service
 * @author yujianlong
 * @date 2018/3/23 16:40
 * @param
 * @return
 */
@Service
@Transactional
@Slf4j
public class InvestorOpenCycleService {
	
	Logger logger = LoggerFactory.getLogger(InvestorOpenCycleService.class);
	
	@Autowired
	private InvestorOpenCycleDao investorOpenCycleDao;

	@Autowired
	private BookingOrderEMDao bookingOrderEMDao;

	/**
	 *
	 *开启新事务插入数据
	 * @author yujianlong
	 * @date 2018/3/23 17:25
	 * @param [investorOpenCycleRelationEntity]
	 * @return com.guohuai.mmp.investor.tradeorder.InvestorOpenCycleRelationEntity
	 */
	@Transactional(value = Transactional.TxType.REQUIRES_NEW)
	public InvestorOpenCycleRelationEntity saveAndFlushRequireNew(InvestorOpenCycleRelationEntity investorOpenCycleRelationEntity) {
		return investorOpenCycleDao.saveAndFlush(investorOpenCycleRelationEntity);
	}

	@Transactional
	public InvestorOpenCycleRelationEntity saveAndFlush(InvestorOpenCycleRelationEntity investorOpenCycleRelationEntity) {
		return investorOpenCycleDao.saveAndFlush(investorOpenCycleRelationEntity);
	}

	public InvestorOpenCycleRelationEntity findBySourceOrderCode(String sourceOrderCode) {
		return investorOpenCycleDao.findOne(sourceOrderCode);
	}

	/**
	 * 获取预约单列表
	 * @param req
	 * @return
	 */
    public List<Map<String, Object>> getBookingOrderList(BookingOrderReq req) {
		return bookingOrderEMDao.getBookingOrderList(req);
    }

	/**
	 * 获取预约单列表总数
	 * @param req
	 * @return
	 */
	public int getBookingOrderListCount(BookingOrderReq req) {
		return bookingOrderEMDao.getBookingOrderListCount(req);
	}

	/**
	 * 修改续投状态
	 * @param uid
	 * @param orderCode
	 * @param status
	 * @return
	 */
    public int setContinueStatusByUidAndOrderCode(String uid, String orderCode, int status) {
		return investorOpenCycleDao.setContinueStatusByUidAndOrderCode(uid, orderCode, status);
    }
}