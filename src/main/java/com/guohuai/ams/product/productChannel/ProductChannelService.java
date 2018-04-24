package com.guohuai.ams.product.productChannel;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;

import com.guohuai.ams.channel.Channel;
import com.guohuai.ams.channel.ChannelDao;
import com.guohuai.ams.label.LabelEntity;
import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductDao;
import com.guohuai.ams.product.ProductService;
import com.guohuai.ams.productLabel.ProductLabel;
import com.guohuai.basic.cardvo.rep.PageRespTulip;
import com.guohuai.cardvo.util.CardVoUtil;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.StringUtil;
import com.guohuai.component.web.view.BaseResp;
import com.guohuai.component.web.view.PageResp;
import com.guohuai.mmp.platform.baseaccount.statistics.PlatformStatisticsService;
import com.guohuai.mmp.publisher.baseaccount.statistics.PublisherStatisticsService;
import com.guohuai.mmp.publisher.hold.PublisherHoldDao;
import com.guohuai.mmp.publisher.hold.PublisherHoldEntity;

@Service
@Transactional
public class ProductChannelService {

	@Autowired
	private ProductDao productDao;
	@Autowired
	private ProductChannelDao productChannelDao;
	@Autowired
	private ChannelDao channelDao;
	@Autowired
	private PublisherHoldDao publisherHoldDao;
	@Autowired
	private ProductService productService;
	@Autowired
	private PublisherStatisticsService publisherStatisticsService;
	@Autowired
	private PlatformStatisticsService platformStatisticsService;
	/**
	 * 绑定jpa EntityManager
	 */
	@PersistenceContext
	private EntityManager em;
	
	@Transactional
	public List<ProductChannel> queryProductChannels(final String productOid) {

		Specification<ProductChannel> spec = new Specification<ProductChannel>() {
			@Override
			public Predicate toPredicate(Root<ProductChannel> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.equal(root.get("product").get("oid").as(String.class), productOid);
			}
		};
		spec = Specifications.where(spec);

		List<ProductChannel> pcs = productChannelDao.findAll(spec);

		return pcs;
	}

	@Transactional
	public List<ProductChannel> queryProductChannels(final List<String> productOids) {

		Specification<ProductChannel> spec = new Specification<ProductChannel>() {
			@Override
			public Predicate toPredicate(Root<ProductChannel> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				Expression<String> exp = root.get("product").get("oid").as(String.class);
				return exp.in(productOids);
			}
		};
		spec = Specifications.where(spec);

		List<ProductChannel> pcs = productChannelDao.findAll(spec);

		return pcs;
	}

	@Transactional
	public PageResp<ProductChannelResp> list(Specification<ProductChannel> spec, Pageable pageable) {
		PageResp<ProductChannelResp> pagesRep = new PageResp<ProductChannelResp>();

		Page<ProductChannel> productChannels = productChannelDao.findAll(spec, pageable);

		List<ProductChannelResp> list = new ArrayList<ProductChannelResp>();
		for (ProductChannel pc : productChannels) {
			ProductChannelResp rep = new ProductChannelResp(pc);
			list.add(rep);
		}
		pagesRep.setTotal(productChannels.getTotalElements());
		pagesRep.setRows(list);
		return pagesRep;
	}

	@Transactional
	public boolean isPublish(String cid, String ckey, String productOid) {
		int c = this.productChannelDao.countForPublish(cid, ckey, productOid);
		if (c > 0) {
			return true;
		}
		throw new AMPException("该产品在该渠道尚未发行或发行已被退回");
	}

	@Transactional
	public BaseResp upshelf(String oid, String operator) {
		BaseResp response = new BaseResp();
		ProductChannel productChannel = this.productChannelDao.findOne(oid);
		if (productChannel == null) {
			// error.define[90017]=不能上下架操作
			throw AMPException.getException(90017);// 不能上架
		}
		Product product = this.productService.getProductByOid(productChannel.getProduct().getOid());
		Channel channel = this.channelDao.findOne(productChannel.getChannel().getOid());
		if (Channel.CHANNEL_STATUS_ON.equals(channel.getChannelStatus()) && Channel.CHANNEL_DELESTATUS_NO.equals(channel.getDeleteStatus())) {
			if (!ProductChannel.MARKET_STATE_Noshelf.equals(productChannel.getMarketState())
					&& !ProductChannel.MARKET_STATE_Offshelf.equals(productChannel.getMarketState())) {
				// error.define[90017]=不能上下架操作
				throw AMPException.getException(90017);
			}
			productChannel.setMarketState(ProductChannel.MARKET_STATE_Onshelf);
			productChannel.setRackTime(new Timestamp(System.currentTimeMillis()));
			this.productChannelDao.saveAndFlush(productChannel);
			
			if(Product.TYPE_Producttype_02.equals(product.getType().getOid())) { //活期产品
				if (Product.DATE_TYPE_FirstRackTime.equals(product.getSetupDateType())
						&& product.getSetupDate() == null) { //与渠道首次上架同时开始募集
					product.setSetupDate(DateUtil.getSqlDate());
					product.setState(Product.STATE_Durationing);
					product.setUpdateTime(DateUtil.getSqlCurrentDate());
					productDao.saveAndFlush(product);
					/**
					 * 定期产品进入募集期时，增加产品发行数量
					 * 活期产品进入存续期时
					 * @author yuechao
					 */
					publisherStatisticsService.increaseReleasedProductAmount(product.getPublisherBaseAccount());
					platformStatisticsService.increaseReleasedProductAmount();
					/**
					 * 定期产品进入募集期时，增加在售产品数量
					 * 活期产品进入存续期时
					 */
					publisherStatisticsService.increaseOnSaleProductAmount(product.getPublisherBaseAccount());
					platformStatisticsService.increaseOnSaleProductAmount();
				}
			} else {// 定期
				if (Product.DATE_TYPE_FirstRackTime.equals(product.getRaiseStartDateType())
						&& product.getRaiseStartDate() == null) {

					product.setRaiseStartDate(DateUtil.getSqlDate());
					product.setRaiseEndDate(
							DateUtil.addSQLDays(product.getRaiseStartDate(), product.getRaisePeriodDays() - 1));// 募集结束时间

					product.setSetupDate(DateUtil.addSQLDays(product.getRaiseEndDate(), product.getFoundDays()));// 最晚产品成立时间
					product.setDurationPeriodEndDate(
							DateUtil.addSQLDays(product.getSetupDate(), product.getDurationPeriodDays() - 1));// 存续期结束时间
					if (product.getAccrualRepayDays() != null && product.getAccrualRepayDays() > 0) {
						// 到期最晚还本付息日 指存续期结束后的还本付息最迟发生在存续期后的第X个自然日的23:59:59为止
						product.setRepayDate(
								DateUtil.addSQLDays(product.getDurationPeriodEndDate(), product.getAccrualRepayDays()));// 到期还款时间
					}

					product.setState(Product.STATE_Raising);
					product.setUpdateTime(DateUtil.getSqlCurrentDate());
					productDao.saveAndFlush(product);
					/**
					 * 定期产品进入募集期时，增加产品发行数量 活期产品进入存续期时
					 * @author yuechao
					 */
					publisherStatisticsService.increaseReleasedProductAmount(product.getPublisherBaseAccount());
					platformStatisticsService.increaseReleasedProductAmount();
					/**
					 * 定期产品进入募集期时，增加在售产品数量 活期产品进入存续期时
					 */
					publisherStatisticsService.increaseOnSaleProductAmount(product.getPublisherBaseAccount());
					platformStatisticsService.increaseOnSaleProductAmount();
				}
			}
		} else {
			// error.define[90017]=不能上下架操作
			throw AMPException.getException(90017);
		}
		
		List<PublisherHoldEntity> list = this.publisherHoldDao.findSpvHoldByProduct(product);
		if (list == null || list.size() == 0) {
			response.setErrorMessage("SPV仓位不足，请前往补仓位，以免影响该产品被购买!");
		} else {
			PublisherHoldEntity spvHold = list.get(0);
			if(spvHold.getTotalVolume().subtract(spvHold.getLockRedeemHoldVolume()).compareTo(BigDecimal.ZERO) > 0) {
				response.setErrorMessage("上架成功!");
			} else {
				response.setErrorMessage("SPV仓位不足，请前往补仓位，以免影响该产品被购买!");
			}
		}
		return response;
	}

	@Transactional
	public BaseResp donwshelf(String oid, String operator) {
		BaseResp response = new BaseResp();
		ProductChannel productChannel = this.productChannelDao.findOne(oid);
		if (productChannel == null) {
			// error.define[90017]=不能上下架操作
			throw AMPException.getException(90017);// 不能下架
		}
		if (!ProductChannel.MARKET_STATE_Onshelf.equals(productChannel.getMarketState())) {
			// error.define[90017]=不能上下架操作
			throw AMPException.getException(90017);
		}
		productChannel.setMarketState(ProductChannel.MARKET_STATE_Offshelf);
		productChannel.setDownTime(DateUtil.getSqlCurrentDate());
		productChannel.setOperator(operator);
		this.productChannelDao.saveAndFlush(productChannel);
		return response;
	}

	// 渠道详情中的产品列表查询
	@Transactional
	public ProductChannelViewPage channelQuery(Specification<ProductChannel> spec, Pageable pageable) {
		Page<ProductChannel> rs = this.productChannelDao.findAll(spec, pageable);

		ProductChannelViewPage r = new ProductChannelViewPage();
		r.setTotal(rs.getTotalElements());

		if (null != rs.getContent() && rs.getContent().size() > 0) {
			for (ProductChannel c : rs.getContent()) {
				r.getRows().add(new ProductChannelView(c));
			}
		}

		return r;
	}

	/**
	 * 
	 * @param productOid
	 *            产品id
	 * @param channelOid
	 *            渠道id
	 */
	public ProductChannel getProductChannel(final String productOid, final String channelOid) {
		Specification<ProductChannel> spec = new Specification<ProductChannel>() {
			@Override
			public Predicate toPredicate(Root<ProductChannel> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.and(cb.equal(root.get("product").get("oid").as(String.class), productOid),
						cb.equal(root.get("channel").get("oid").as(String.class), channelOid));
			}
		};
		return this.productChannelDao.findOne(spec);
	}

	/**
	 * 根据产品查<<产品--渠道>>
	 * 
	 * @param product
	 * @return
	 */
	public List<ProductChannel> getChannelByProduct(Product product) {
		List<ProductChannel> list = this.productChannelDao.findByProductOid(product.getOid());
		return list;
	}
	/**
	 * 查询可用渠道
	 * @param lastOid
	 * @return
	 */
	public List<Object[]> getChannelByBatch(){
		return this.productChannelDao.getChannelByBatch();
	}
	
	/**
	 * 根据Oid查询可用渠道
	 * @param oid
	 * @return
	 */
	public List<ProductChannel> getChannelByProductOid(String productOid){
		return this.productChannelDao.findByProductOid(productOid);
	}

	public List<ProductChannel> getChannelByChannelOid(String channelOid) {
		return this.productChannelDao.findByChannelOid(channelOid);
	}
	
	public List<ProductChannel> saveAndFlush(Product product, List<String> channelOids, String operator)  {
		
		List<ProductChannel> productChannelResult = new ArrayList<ProductChannel>();
		if (null != channelOids && channelOids.size() > 0) {
			for (String channelOid : channelOids) {
				if (!StringUtil.isEmpty(channelOid)) {
					Timestamp now = new Timestamp(System.currentTimeMillis());
					Channel channel = channelDao.findByOid(channelOid);
					ProductChannel productChannel = new ProductChannel();
					productChannel.setOid(StringUtil.uuid());
					productChannel.setProduct(product);
					productChannel.setChannel(channel);
					productChannel.setOperator(operator);
					productChannel.setRackTime(now);
					productChannel.setCreateTime(now);
					productChannel.setUpdateTime(now);
					productChannel.setMarketState(ProductChannel.MARKET_STATE_Onshelf);
					productChannel.setStatus(ProductChannel.STATUS_VALID);
					this.productChannelDao.save(productChannel);
					productChannelResult.add(productChannel);
				}
			}
		}
		return productChannelResult;
	}
	
	public PageRespTulip<Object> getActivityModelProducts(String channelId) {
		PageRespTulip<Object> pageResp = new PageRespTulip<>();
		StringBuilder sb = new StringBuilder();
 		sb.append("SELECT t1.oid, t1.name");
 		sb.append(" from t_gam_product t1, t_gam_product_channel t2, t_money_platform_channel t3 ");
 		sb.append(" where t3.oid = ?1 and t1.oid = t2.productOid and t2.channelOid = t3.oid and t2.marketState = 'ONSHELF' and ((t1.state = 'RAISING' and t1.type = 'PRODUCTTYPE_01') or (t1.state = 'DURATIONING' and t1.type = 'PRODUCTTYPE_02')) and t1.oid != '0e4ee8ead74241de90f82e06f987ef8d' ");
 		sb.append(" and t1.raisedTotalNumber - t1.collectedVolume - t1.lockCollectedVolume > 0 ");
 		Query emQuery  = em.createNativeQuery(sb.toString()).setParameter(1, channelId);
 		List<Map<String, Object>> list = CardVoUtil.query2Map(emQuery);
 		pageResp.setRows(list);
		return pageResp;
	}
}
