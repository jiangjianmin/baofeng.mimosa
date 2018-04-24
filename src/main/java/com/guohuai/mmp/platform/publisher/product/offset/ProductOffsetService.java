package com.guohuai.mmp.platform.publisher.product.offset;

import java.math.BigDecimal;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductService;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.web.view.RowsRep;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity;
import com.guohuai.mmp.platform.publisher.offset.PublisherOffsetEntity;
import com.guohuai.mmp.platform.publisher.offset.PublisherOffsetService;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountEntity;

@Service
@Transactional
public class ProductOffsetService {

	Logger logger = LoggerFactory.getLogger(ProductOffsetService.class);

	@Autowired
	ProductOffsetDao productOffsetDao;
	@Autowired
	ProductService productService;
	@Autowired
	PublisherOffsetService publisherOffsetService;
	@Autowired
	ProductOffsetServiceRequiresNew productOffsetServiceRequiresNew;
	

	public ProductOffsetEntity updateEntity(ProductOffsetEntity offset) {
		return this.productOffsetDao.save(offset);
	}
	

	/**
	 * 获取最新的轧差批次
	 * 
	 * @return
	 */
	public ProductOffsetEntity offset(PublisherBaseAccountEntity publisherBaseAccount, InvestorTradeOrderEntity tradeOrder, boolean isPositive) {
		BigDecimal orderAmount = tradeOrder.getOrderAmount();
		if (!isPositive) {
			orderAmount = orderAmount.negate();
		}
		ProductOffsetEntity offset = this.getLatestOffset(tradeOrder.getProduct(), tradeOrder.getPublisherOffset());
		if (tradeOrder.getOrderType().equals(InvestorTradeOrderEntity.TRADEORDER_orderType_invest)
				|| (tradeOrder.getOrderType().equals(InvestorTradeOrderEntity.TRADEORDER_orderType_noPayInvest)// 快活宝购买快定宝
				&& Product.TYPE_Producttype_03.equals(tradeOrder.getProduct().getType().getOid()))) {
			this.increaseInvest(offset, orderAmount);
		} else {
			this.increaseRedeem(offset, orderAmount);
		}
		return offset;
	}

	/**
	 * 增加待清算投资
	 * 
	 * @param offset
	 * @param investAmount
	 */
	public void increaseInvest(ProductOffsetEntity offset, BigDecimal investAmount){
		this.productOffsetDao.increaseInvest(offset.getOid(), investAmount);
	}

	/**
	 * 增加待清算赎回
	 * 
	 * @param offset
	 * @param investAmount
	 */
	public void increaseRedeem(ProductOffsetEntity offset, BigDecimal investAmount) {
		this.productOffsetDao.increaseRedeem(offset.getOid(), investAmount);
	}

	private ProductOffsetEntity getLatestOffset(Product product, PublisherOffsetEntity pOffset) {

		ProductOffsetEntity offset = this.productOffsetDao.getLatestOffset(product, pOffset.getOffsetCode());
		if (offset == null) {
			try {
				return this.productOffsetServiceRequiresNew.createEntity(product, pOffset);
			} catch (Exception e) {
				throw new AMPException("新建轧差异常");
				// return this.getLatestOffset(product, pOffset);
			}
			
		}
		return offset;
	}

	public int updateOffsetStatus2closed(String spvOid) {
		return this.productOffsetDao.updateOffsetStatus2closed(spvOid);
	}

	/**
	 * 根据产品查找<<非已结算>>状态产品轧差
	 * 
	 * @param productOid
	 * @return
	 */
	public OffsetConstantRep findByProductOid(String productOid) {
		List<ProductOffsetEntity> list = this.productOffsetDao.findByProductConstantly(productOid);
	
		OffsetConstantRep rep = new OffsetConstantRep();
		if (null != list && !list.isEmpty()) {
			for (ProductOffsetEntity entity : list) {
				rep.setNetPosition(rep.getNetPosition().add(entity.getNetPosition()));
			}
		}
		return rep;
	}

	public RowsRep<ProductOffsetMoneyRep> findByOffsetOid(String offsetOid) {
		List<ProductOffsetEntity> list = this.productOffsetDao.findByPublisherOffset(this.publisherOffsetService.findByOid(offsetOid));
		RowsRep<ProductOffsetMoneyRep> rows = new RowsRep<ProductOffsetMoneyRep>();
		for (ProductOffsetEntity entity : list) {
			ProductOffsetMoneyRep rep = ProductOffsetMoneyRep.builder()
					.productOid(entity.getProduct().getOid())
					.productCode(entity.getProduct().getCode())
					.productName(entity.getProduct().getName())
					.netPosition(entity.getNetPosition())
					.investAmount(entity.getInvestAmount())
					.redeemAmount(entity.getRedeemAmount())
					.build();
			rows.add(rep);
		}

		return rows;
	}



	public int updateConfirmStatus(String pOffsetOid, String confirmStatus) {
		return this.productOffsetDao.updateConfirmStatus(pOffsetOid, confirmStatus);
		
	}
	
	public int updateConfirmStatus4Lock(String pOffsetOid, String confirmStatus) {
		return this.productOffsetDao.updateConfirmStatus4Lock(pOffsetOid, confirmStatus);
		
	}

	public int updateClearStatus(String offsetOid, String clearStatus) {
		int i = this.productOffsetDao.updateClearStatus(offsetOid, clearStatus);
		if (i < 1) {
			// error.define[20021]=清算状态异常(CODE:20021)
			throw new AMPException(20021);
		}
		return i;
		
	}


	public int updateCloseStatus4Close(String offsetOid, String closeStatus) {
		int i = this.productOffsetDao.updateCloseStatus4Close(offsetOid, closeStatus);
		if (i < 1) {
			// error.define[20022]=结算状态异常(CODE:20022)
			throw AMPException.getException(20022);
		}
		return i;
	}
	
	public int updateCloseStatus4CloseBack(String offsetOid, String closeStatus) {
		int i = this.productOffsetDao.updateCloseStatus4CloseBack(offsetOid, closeStatus);
		if (i < 1) {
			// error.define[20022]=结算状态异常(CODE:20022)
			throw AMPException.getException(20022);
		}
		return i;
	}


	public List<ProductOffsetEntity> findByPublisherOffset(PublisherOffsetEntity offset) {
		return this.productOffsetDao.findByPublisherOffset(offset);
	}


	public ProductOffsetEntity findByProductAndOffsetCode(Product product, String offsetCode) {
		
		return this.productOffsetDao.findByProductAndOffsetCode(product, offsetCode);
	}


	

}
