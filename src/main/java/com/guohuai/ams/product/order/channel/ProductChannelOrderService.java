package com.guohuai.ams.product.order.channel;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

import com.guohuai.ams.channel.Channel;
import com.guohuai.ams.channel.ChannelDao;
import com.guohuai.ams.channel.ChannelService;
import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductDao;
import com.guohuai.ams.product.ProductService;
import com.guohuai.ams.product.productChannel.ChooseChannelResp;
import com.guohuai.ams.product.productChannel.ProductChannel;
import com.guohuai.ams.product.productChannel.ProductChannelDao;
import com.guohuai.ams.product.productChannel.ProductChannelService;
import com.guohuai.cache.InitCacheService;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.StringUtil;
import com.guohuai.component.web.view.BaseResp;
import com.guohuai.component.web.view.PageResp;
import com.guohuai.mmp.platform.baseaccount.statistics.PlatformStatisticsService;
import com.guohuai.mmp.publisher.baseaccount.statistics.PublisherStatisticsService;
import com.guohuai.operate.api.AdminSdk;
import com.guohuai.operate.api.objs.admin.AdminObj;

@Service
@Transactional
public class ProductChannelOrderService {
	
	@Autowired
	private ProductChannelOrderDao productChannelOrderDao;
	@Autowired
	private ChannelDao channelDao;
	@Autowired
	private ProductDao productDao;
	@Autowired
	private ProductService productService;
	@Autowired
	private ChannelService channelService;
	@Autowired
	private ProductChannelDao productChannelDao;
	@Autowired
	private ProductChannelService productChannelService;
	@Autowired
	private AdminSdk adminSdk;
	@Autowired
	private PublisherStatisticsService publisherStatisticsService;
	@Autowired
	private PlatformStatisticsService platformStatisticsService;
	@Autowired
	private InitCacheService moneyCacheService;
	/**
	 * 获取指定id的ProductChannelOrder对象
	 * @param oid ProductChannelOrder对象id
	 * @return {@link ProductChannelOrder}
	 */
	public ProductChannelOrder getProductChannelByOid(String oid) {
		ProductChannelOrder pco = this.productChannelOrderDao.findOne(oid);
		if (pco == null) {
			throw AMPException.getException(90003);
		}
		return pco;
	}
	
	/**
	 * ProductChannelOrder列表
	 * @param spec
	 * @param pageable
	 * @return
	 */
	public PageResp<ProductChannelOrderResp> list(Specification<ProductChannelOrder> spec, Pageable pageable) {
		Page<ProductChannelOrder> pcos = this.productChannelOrderDao.findAll(spec, pageable);
		
		PageResp<ProductChannelOrderResp> pagesRep = new PageResp<ProductChannelOrderResp>();
		
		if (pcos != null && pcos.getContent() != null && pcos.getTotalElements() > 0) {
			
			Map<String, AdminObj> adminObjMap = new HashMap<String, AdminObj>();
			AdminObj adminObj = null;
			
			List<ProductChannelOrderResp> rows = new ArrayList<ProductChannelOrderResp>();
			for (ProductChannelOrder pco : pcos) {
				ProductChannelOrderResp pcor = new ProductChannelOrderResp(pco);
				
				if(!StringUtil.isEmpty(pcor.getCreator())) {
					if (adminObjMap.get(pcor.getCreator()) == null) {
						try {
							adminObj = adminSdk.getAdmin(pcor.getCreator());
							adminObjMap.put(pcor.getCreator(), adminObj);
						} catch (Exception e) {
						}

					}
					if (adminObjMap.get(pcor.getCreator()) != null) {
						pcor.setCreator(adminObjMap.get(pcor.getCreator()).getName());
					}
				}
				
				if(!StringUtil.isEmpty(pcor.getAuditor())) {
					if (adminObjMap.get(pcor.getAuditor()) == null) {
						try {
							adminObj = adminSdk.getAdmin(pcor.getAuditor());
							adminObjMap.put(pcor.getAuditor(), adminObj);
						} catch (Exception e) {
						}
					}
					if (adminObjMap.get(pcor.getAuditor()) != null) {
						pcor.setAuditor(adminObjMap.get(pcor.getAuditor()).getName());
					}
				}
				rows.add(pcor);
			}
			pagesRep.setRows(rows);
		}
		pagesRep.setTotal(pcos.getTotalElements());
		return pagesRep;
	}
	
	/**
	 * 已经申请过的ProductChannelOrder列表
	 * @param spec
	 * @param pageable
	 * @return
	 */
	public PageResp<ProductChannelOrderResp> productChannels(Specification<ProductChannelOrder> spec, Sort sort) {
		List<ProductChannelOrder> pcos = this.productChannelOrderDao.findAll(spec, sort);
		
		PageResp<ProductChannelOrderResp> pagesRep = new PageResp<ProductChannelOrderResp>();
		
		if (pcos != null && pcos.size() > 0) {
			
			Map<String, AdminObj> adminObjMap = new HashMap<String, AdminObj>();
			AdminObj adminObj = null;
			
			List<ProductChannelOrderResp> rows = new ArrayList<ProductChannelOrderResp>();
			for (ProductChannelOrder pco : pcos) {
				ProductChannelOrderResp pcor = new ProductChannelOrderResp(pco);
				
				if(!StringUtil.isEmpty(pcor.getCreator())) {
					if (adminObjMap.get(pcor.getCreator()) == null) {
						try {
							adminObj = adminSdk.getAdmin(pcor.getCreator());
							adminObjMap.put(pcor.getCreator(), adminObj);
						} catch (Exception e) {
						}

					}
					if (adminObjMap.get(pcor.getCreator()) != null) {
						pcor.setCreator(adminObjMap.get(pcor.getCreator()).getName());
					}
				}
				
				if(!StringUtil.isEmpty(pcor.getAuditor())) {
					if (adminObjMap.get(pcor.getAuditor()) == null) {
						try {
							adminObj = adminSdk.getAdmin(pcor.getAuditor());
							adminObjMap.put(pcor.getAuditor(), adminObj);
						} catch (Exception e) {
						}
					}
					if (adminObjMap.get(pcor.getAuditor()) != null) {
						pcor.setAuditor(adminObjMap.get(pcor.getAuditor()).getName());
					}
				}
				rows.add(pcor);
			}
			pagesRep.setRows(rows);
		}
		pagesRep.setTotal(pcos.size());
		return pagesRep;
	}
	
	@Transactional
	public PageResp<ChooseChannelResp> queryChannels(final String productOid) {
		Sort sort = new Sort(new Order(Direction.DESC, "createTime"));
		Specification<Channel> channelSpec = new Specification<Channel>() {
			@Override
			public Predicate toPredicate(Root<Channel> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.and(cb.equal(root.get("deleteStatus").as(String.class), Channel.CHANNEL_DELESTATUS_NO), 
						cb.equal(root.get("channelStatus").as(String.class), Channel.CHANNEL_STATUS_ON));
			}
		};
		
		Specification<ProductChannel> productChannelSpec = new Specification<ProductChannel>() {
			@Override
			public Predicate toPredicate(Root<ProductChannel> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.and(cb.equal(root.get("product").get("oid").as(String.class), productOid),
						cb.equal(root.get("status").as(String.class), ProductChannel.STATUS_BACKOUT));
			}
		};
		
		Specification<ProductChannelOrder> spec = new Specification<ProductChannelOrder>() {
			@Override
			public Predicate toPredicate(Root<ProductChannelOrder> root, CriteriaQuery<?> query,
					CriteriaBuilder cb) {
				return cb.and(cb.equal(root.get("product").get("oid").as(String.class), productOid),
						cb.notEqual(root.get("status").as(String.class), ProductChannelOrder.STATUS_FAIL),
						cb.notEqual(root.get("status").as(String.class), ProductChannelOrder.STATUS_CANCEL),
						cb.notEqual(root.get("status").as(String.class), ProductChannelOrder.STATUS_DELETE));
			}
		};
		
		PageResp<ChooseChannelResp> pagesRep = new PageResp<ChooseChannelResp>();
		
		List<Channel> channels = this.channelDao.findAll(channelSpec, sort);
		
		List<String> productChannelOids = new ArrayList<String>();//改产品发布过的所有渠道的回退过的渠道oid集合
		List<ProductChannel> productChannels = productChannelDao.findAll(productChannelSpec, sort);//回退过的
		if(productChannels!=null && productChannels.size()>0) {
			for(ProductChannel productChannel : productChannels) {
				if(productChannel.getChannel()!=null) {
					productChannelOids.add(productChannel.getChannel().getOid());
				}
			}
		}
		
		List<ProductChannelOrder> pcos = this.productChannelOrderDao.findAll(spec, sort);
		List<String> channelOids = new ArrayList<String>();//改产品已经有效发布过的渠道oid
		if(pcos!=null && pcos.size()>0) {
			for(ProductChannelOrder pco : pcos) {
				if(pco.getChannel()!=null) {
					if(!productChannelOids.contains(pco.getChannel().getOid())) {
						channelOids.add(pco.getChannel().getOid());
					}
				}
			}
		}
		
		List<ChooseChannelResp> rows = new ArrayList<ChooseChannelResp>();
		for (Channel channel : channels) {
			if(!channelOids.contains(channel.getOid())) {
				ChooseChannelResp rep = new ChooseChannelResp(channel);
				rows.add(rep);
			}
		}
		pagesRep.setTotal(rows.size());
		pagesRep.setRows(rows);
		return pagesRep;
	}
	
	public BaseResp save(final SaveProductChannelForm form, String operator) {
		BaseResp response = new BaseResp();
		
		ProductChannel pc = productChannelService.getProductChannel(form.getProductOid(), form.getChannelOid());
		if(pc!=null && ProductChannel.STATUS_VALID.equals(pc.getStatus())) {
			throw AMPException.getException("该渠道上已经发行过");
		}
		
		Specification<ProductChannelOrder> spec = new Specification<ProductChannelOrder>() {
			@Override
			public Predicate toPredicate(Root<ProductChannelOrder> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				Predicate p = cb.or(cb.equal(root.get("status").as(String.class), ProductChannelOrder.STATUS_SUBMIT),
						cb.equal(root.get("status").as(String.class), ProductChannelOrder.STATUS_PASS));
				return cb.and(cb.equal(root.get("product").get("oid").as(String.class),form.getProductOid()),
						cb.equal(root.get("channel").get("oid").as(String.class),form.getChannelOid())
						,p);
			}
		};
		
		Specification<ProductChannel> productChannelSpec = new Specification<ProductChannel>() {
			@Override
			public Predicate toPredicate(Root<ProductChannel> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.and(cb.equal(root.get("product").get("oid").as(String.class), form.getProductOid()),
						cb.equal(root.get("status").as(String.class), ProductChannel.STATUS_BACKOUT));
			}
		};
		
		List<String> productChannelOids = new ArrayList<String>();//改产品发布过的所有渠道的回退过的渠道oid集合
		List<ProductChannel> productChannels = productChannelDao.findAll(productChannelSpec);//回退过的
		if(productChannels!=null && productChannels.size()>0) {
			for(ProductChannel productChannel : productChannels) {
				if(productChannel.getChannel()!=null) {
					productChannelOids.add(productChannel.getChannel().getOid());
				}
			}
		}
		
		List<ProductChannelOrder> pcos = productChannelOrderDao.findAll(spec);
		List<String> channelOids = new ArrayList<String>();//改产品已经有效发布过的渠道oid
		if(pcos!=null && pcos.size()>0) {
			for(ProductChannelOrder pco : pcos) {
				if(pco.getChannel()!=null) {
					if(!productChannelOids.contains(pco.getChannel().getOid())) {
						channelOids.add(pco.getChannel().getOid());
					}
				}
			}
		}

		if (channelOids.size() == 0) {
			Product product = this.productService.getProductByOid(form.getProductOid());
			Channel channel = this.channelService.getOne(form.getChannelOid());
			
			ProductChannelOrder pco = new ProductChannelOrder();
			pco.setOid(StringUtil.uuid());
			pco.setProduct(product);
			pco.setChannel(channel);
			pco.setCreateTime(new Timestamp(System.currentTimeMillis()));
			pco.setCreator(operator);
			pco.setStatus(ProductChannelOrder.STATUS_SUBMIT);
			productChannelOrderDao.save(pco);
		}
		
		return response;
	}
	
	/**
	 * 新建产品包中产品的时候，新增渠道订单
	 * @param oid
	 * @param operator
	 * @return
	 */
	public void saveChannelOrder(String productOid,String channelOids,String operator){
		
		String[] channelOidArr = channelOids.split(",");
		for(int i = 0;i < channelOidArr.length; i++){
			Product product = this.productService.getProductByOid(productOid);
			Channel channel = this.channelService.getOne(channelOidArr[i]);
				
			ProductChannelOrder pco = new ProductChannelOrder();
			pco.setOid(StringUtil.uuid());
			pco.setProduct(product);
			pco.setChannel(channel);
			pco.setCreateTime(new Timestamp(System.currentTimeMillis()));
			pco.setCreator(operator);
			pco.setAuditor(operator);
			pco.setAuditTime(new Timestamp(System.currentTimeMillis()));
			pco.setStatus(ProductChannelOrder.STATUS_PASS);
			productChannelOrderDao.save(pco);
		}
	}
	
	
	public BaseResp auditFail(String oid, String operator) {
		BaseResp response = new BaseResp();
		
		ProductChannelOrder pspo = this.getProductChannelByOid(oid);
		if(!ProductChannelOrder.STATUS_SUBMIT.equals(pspo.getStatus())) {
			throw AMPException.getException(90013);
		}
		pspo.setAuditTime(new Timestamp(System.currentTimeMillis()));
		pspo.setAuditor(operator);
		pspo.setStatus(ProductChannelOrder.STATUS_FAIL);
		productChannelOrderDao.saveAndFlush(pspo);
		
		return response;
	}
	
	public BaseResp auditPass(String oid, String operator) throws ParseException,Exception {
		BaseResp response = new BaseResp();
		
		ProductChannelOrder pco = this.getProductChannelByOid(oid);
		if(pco.getProduct()==null || pco.getChannel()==null || !ProductChannelOrder.STATUS_SUBMIT.equals(pco.getStatus())) {
			throw AMPException.getException(90013);
		}
		
		Product product = productService.getProductByOid(pco.getProduct().getOid());
		
		Channel channel = channelDao.findOne(pco.getChannel().getOid());
		
		if (!Channel.CHANNEL_STATUS_ON.equals(channel.getChannelStatus()) 
				|| Channel.CHANNEL_DELESTATUS_YES.equals(channel.getDeleteStatus())) {
			// error.define[90013]=不能审核操作
			throw AMPException.getException(90013);
		}
		
		ProductChannel productChannel = productChannelService.getProductChannel(pco.getProduct().getOid(), pco.getChannel().getOid());
		if(productChannel!=null && ProductChannel.STATUS_VALID.equals(productChannel.getStatus())) {
			throw AMPException.getException("该渠道上已经发行过");
		}
		Timestamp now = new Timestamp(System.currentTimeMillis());
		
		product.setUpdateTime(now);
		productDao.saveAndFlush(product);
		
		pco.setAuditTime(now);
		pco.setAuditor(operator);
		pco.setStatus(ProductChannelOrder.STATUS_PASS);
		productChannelOrderDao.saveAndFlush(pco);
		
		if(productChannel==null) {
			productChannel = new ProductChannel();
			productChannel.setOid(StringUtil.uuid());
			productChannel.setProduct(product);
			productChannel.setChannel(channel);
			productChannel.setOperator(operator);
			productChannel.setCreateTime(now);
			productChannel.setUpdateTime(now);
			productChannel.setMarketState(ProductChannel.MARKET_STATE_Noshelf);
			productChannel.setOrder(pco);
			productChannel.setStatus(ProductChannel.STATUS_VALID);
			this.productChannelDao.save(productChannel);
		} else {
			productChannel.setUpdateTime(now);
			productChannel.setMarketState(ProductChannel.MARKET_STATE_Noshelf);
			productChannel.setOrder(pco);
			productChannel.setStatus(ProductChannel.STATUS_VALID);
			this.productChannelDao.saveAndFlush(productChannel);
		}
		return response;
	}
	
	public BaseResp rollbackApply(String oid, String operator) {
		BaseResp response = new BaseResp();
		
		ProductChannelOrder pspo = this.getProductChannelByOid(oid);
		if(!ProductChannelOrder.STATUS_SUBMIT.equals(pspo.getStatus())) {
			throw AMPException.getException(90028);
		}
		pspo.setAuditTime(new Timestamp(System.currentTimeMillis()));
		pspo.setAuditor(operator);
		pspo.setStatus(ProductChannelOrder.STATUS_CANCEL);
		productChannelOrderDao.saveAndFlush(pspo);
		
		return response;
		
	}
	
	public BaseResp delete(String oid, String operator) {
		BaseResp response = new BaseResp();
		
		ProductChannelOrder pspo = this.getProductChannelByOid(oid);
		if(!ProductChannelOrder.STATUS_FAIL.equals(pspo.getStatus())) {
			throw AMPException.getException(90027);
		}
		pspo.setAuditTime(new Timestamp(System.currentTimeMillis()));
		pspo.setAuditor(operator);
		pspo.setStatus(ProductChannelOrder.STATUS_DELETE);
		productChannelOrderDao.saveAndFlush(pspo);
		
		return response;
	}

}
