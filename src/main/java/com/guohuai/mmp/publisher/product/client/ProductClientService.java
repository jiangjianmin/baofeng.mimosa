package com.guohuai.mmp.publisher.product.client;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.guohuai.ams.companyScatterStandard.LoanContract;
import com.guohuai.ams.companyScatterStandard.LoanContractDao;
import com.guohuai.ams.companyScatterStandard.LoanContractResp;
import com.guohuai.ams.product.reward.*;
import com.guohuai.mmp.publisher.hold.PublisherHoldService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;

import com.guohuai.ams.duration.fact.income.IncomeAllocate;
import com.guohuai.ams.duration.fact.income.IncomeAllocateDao;
import com.guohuai.ams.duration.fact.income.IncomeEvent;
import com.guohuai.ams.label.LabelEnum;
import com.guohuai.ams.label.LabelRep;
import com.guohuai.ams.label.LabelResp;
import com.guohuai.ams.label.LabelService;
import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductDao;
import com.guohuai.ams.product.ProductDecimalFormat;
import com.guohuai.ams.product.ProductPojo;
import com.guohuai.ams.product.ProductService;
import com.guohuai.ams.product.ProductTypeDetail;
import com.guohuai.ams.product.ProductTypeDetailService;
import com.guohuai.ams.product.coupon.ProductCouponEnum;
import com.guohuai.ams.product.coupon.ProductCouponService;
import com.guohuai.ams.product.productChannel.ProductChannel;
import com.guohuai.ams.product.productChannel.ProductChannelDao;
import com.guohuai.ams.productLabel.ProductLabel;
import com.guohuai.ams.productLabel.ProductLabelDao;
import com.guohuai.ams.productLabel.ProductLabelService;
import com.guohuai.ams.productPackage.coupon.ProductPackageCouponService;
import com.guohuai.cache.entity.HoldCacheEntity;
import com.guohuai.cache.service.CacheHoldService;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.DecimalUtil;
import com.guohuai.component.util.StringUtil;
import com.guohuai.component.version.VersionUtils;
import com.guohuai.component.web.view.PagesRep;
import com.guohuai.component.web.view.RowsRep;
import com.guohuai.file.File;
import com.guohuai.file.FileResp;
import com.guohuai.file.FileService;
import com.guohuai.mmp.investor.tradeorder.OrderDateService;
import com.guohuai.mmp.publisher.corporate.Corporate;
import com.guohuai.mmp.publisher.corporate.CorporateDao;
import com.guohuai.mmp.publisher.investor.InterestFormula;
import com.guohuai.operate.api.AdminSdk;
import com.guohuai.operate.api.objs.admin.AdminObj;
import com.guohuai.plugin.PageVo;

import static com.guohuai.ams.companyScatterStandard.LoanContract_.loanPeriod;
import static com.guohuai.ams.companyScatterStandard.LoanContract_.loanRatio;
import static com.guohuai.ams.companyScatterStandard.LoanContract_.loanVolume;

@Service
public class ProductClientService {
	
	@Autowired
	private IncomeAllocateDao incomeAllocateDao;
	@Autowired
	private ProductService productService;
	@Autowired
	private FileService fileService;
	@Autowired
	private ProductChannelDao productChannelDao;
	@Autowired
	private AdminSdk adminSdk;
	@Autowired
	private ProductIncomeRewardService rewardService;
	@Autowired
	private ProductDao productDao;
	@Autowired
	private ProductLabelDao productLabelDao;
	@Autowired
	private ProductLabelService productLabelService;
	@Autowired
	private CorporateDao corporateDao;
	@Autowired
	private OrderDateService orderDateService;
	@Autowired
	private CacheHoldService cacheHoldService;
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private ProductIncomeRewardCacheService rewardCacheService;
	@Autowired
	private LabelService labelService;
	@Autowired
	private ProductCouponService productCouponService;
	@Autowired
	private ProductPackageCouponService productPackageCouponService;
	@Autowired
	private ProductTypeDetailService productTypeDetailService;
	@Autowired
	private ProductIncomeRewardSnapshotService incomeRewardSnapshotService;
	@Autowired
	private PublisherHoldService publisherHoldService;
	@Autowired
	private LoanContractDao loanContractDao;
	@Value("${p2p.amount.limit:50000}")
	private BigDecimal limitAmount;
	
	private static final int APP_PAGE_COUNT = 6;
	private static final int PC_PAGE_COUNT = 4;
	
	//起购金额 sort
	Comparator<ProductChannel> investMinSort(final String order) {
		return new Comparator<ProductChannel>(){
			 public int compare(ProductChannel o1, ProductChannel o2) {
				 //这里写比较方法
				 if(o1.getProduct()!=null && o2.getProduct()!=null && o1.getProduct().getInvestMin()!=null && o2.getProduct().getInvestMin()!=null) {
					 if (!"desc".equals(order)) {
						 int result = o1.getProduct().getInvestMin().compareTo(o2.getProduct().getInvestMin());
						 return result;
					 } else {
						 int result = o2.getProduct().getInvestMin().compareTo(o1.getProduct().getInvestMin());
						 return result;
					 }
				 }
			     return 0;//然后return一个int型的值
			 }
		};
	}
	
	//投资期限sort
	Comparator<ProductChannel> durationPeriodDaysSort(final String order) {
		return new Comparator<ProductChannel>() {
			 public int compare(ProductChannel o1, ProductChannel o2) {
				 //这里写比较方法
				 if(o1.getProduct()!=null && o2.getProduct()!=null && o1.getProduct().getDurationPeriodDays()!=null && o2.getProduct().getDurationPeriodDays()!=null) {
					 if (!"desc".equals(order)) {
						 int result = o1.getProduct().getDurationPeriodDays().compareTo(o2.getProduct().getDurationPeriodDays());
						 return result;
					 } else {
						 int result = o2.getProduct().getDurationPeriodDays().compareTo(o1.getProduct().getDurationPeriodDays());
						 return result;
					 }
				 }
			     return 0;//然后return一个int型的值
			 }
		};
	}
	
	//预期年化收益率 sort
	Comparator<ProductChannel> expArorSort(final String order,final Map<String,Map<String,BigDecimal>> productExpArorMap) {
		return new Comparator<ProductChannel>() {
			 public int compare(ProductChannel o1, ProductChannel o2) {
				 //这里写比较方法
				 if(o1.getProduct()!=null && o2.getProduct()!=null && o1.getProduct().getExpAror()!=null && o2.getProduct().getExpAror()!=null) {
					 BigDecimal o1expAror = o1.getProduct().getExpAror();
					 BigDecimal o2expAror = o2.getProduct().getExpAror();
					 if(productExpArorMap.get(o1.getProduct().getOid())!=null) {
						 Map<String,BigDecimal> minMaxReward = productExpArorMap.get(o1.getProduct().getOid());
						 o1expAror = o1expAror.add(minMaxReward.get("minReward"));
					 }
					 if(productExpArorMap.get(o2.getProduct().getOid())!=null) {
						 Map<String,BigDecimal> minMaxReward = productExpArorMap.get(o2.getProduct().getOid());
						 o2expAror = o2expAror.add(minMaxReward.get("minReward"));
					 }
					 if (!"desc".equals(order)) {
						 int result = o1expAror.compareTo(o2expAror);
						 return result;
					 } else {
						 int result = o2expAror.compareTo(o1expAror);
						 return result;
					 }
				 }
			     return 0;//然后return一个int型的值
			 }
		};
	}
	
	//活期产品maxSaleVolume-lockCollectedVolume sort
	Comparator<ProductChannel> maxSalelockCollectedVolumeSort(final String order) {
		return new Comparator<ProductChannel>() {
			public int compare(ProductChannel o1, ProductChannel o2) {
				// 这里写比较方法
				if (o1.getProduct() != null && o2.getProduct() != null && o1.getProduct().getMaxSaleVolume() != null
						&& o2.getProduct().getMaxSaleVolume() != null) {
					if (!"desc".equals(order)) {
						int result = o1.getProduct().getMaxSaleVolume()
								.subtract(o1.getProduct().getLockCollectedVolume()).compareTo(o2.getProduct()
										.getMaxSaleVolume().subtract(o2.getProduct().getLockCollectedVolume()));
						return result;
					} else {
						int result = o2.getProduct().getMaxSaleVolume()
								.subtract(o2.getProduct().getLockCollectedVolume()).compareTo(o1.getProduct()
										.getMaxSaleVolume().subtract(o1.getProduct().getLockCollectedVolume()));
						return result;
					}
				}
				return 0;// 然后return一个int型的值
			}
		};
	}
	
	//产品A的预期年化收益率是 1%-5% 奖励收益率最小是0.1% 最大是0.9% 那么展示的时候就展示 （1%+0.1%）-（5%+0.9%）即展示的是 1.1%-5.9%；
	//产品B预期年化收益率是4% 奖励收益率最小是3% 最大是9%  那么展示的时候就展示 （4%+3%）-（4%+9%）即展示的是 7%-13%；
	/**
	 * 获取产品的最小和最大奖励收益率
	 * @param productOids
	 * @return
	 */
	public Map<String,Map<String,BigDecimal>> getProductsMinMaxRewards(List<String> productOids) {
		Map<String,Map<String,BigDecimal>> productExpArorMap = new HashMap<String,Map<String,BigDecimal>>();
			
		if(productOids!=null && productOids.size()>0) {
			/** * 阶梯收益率 */
			Map<String,List<ProductIncomeReward>> productRewardMap = new HashMap<String,List<ProductIncomeReward>>();//<productOid,List<ProductIncomeReward>>
			List<ProductIncomeReward> rewards = rewardService.productsRewardList(productOids);
			if (rewards != null && rewards.size() > 0) {
				for (ProductIncomeReward reward : rewards) {
					if(productRewardMap.get(reward.getProduct().getOid())==null) {
						productRewardMap.put(reward.getProduct().getOid(), new ArrayList<ProductIncomeReward>());
					}
					productRewardMap.get(reward.getProduct().getOid()).add(reward);
				}
			}
			if(productRewardMap.size()>0) {
				for (String productOid : productOids) {
					List<ProductIncomeReward> prewards = productRewardMap.get(productOid);
					if(prewards!=null && prewards.size()>0) {//算上奖励收益
						BigDecimal minReward = prewards.get(0).getRatio();
						BigDecimal maxReward = prewards.get(0).getRatio();
						for(ProductIncomeReward preward : prewards) {
							if(preward.getRatio().compareTo(minReward)<0) {
								minReward = preward.getRatio();
							}
							if(preward.getRatio().compareTo(maxReward)>0) {
								maxReward = preward.getRatio();
							}
						}
						Map<String,BigDecimal> minMaxReward = new HashMap<String,BigDecimal>();
						minMaxReward.put("minReward", minReward);
						minMaxReward.put("maxReward", maxReward);
						productExpArorMap.put(productOid, minMaxReward);
					}
				}
			}
		}
		
		return productExpArorMap;
	}
	
	
	/**
	 * app查询可以申购的定期活期产品推荐列表
	 * @param spec
	 * @param pageable
	 * @return {@link PagesRep<ProductListResp>} ,如果返回的errCode属性等于0表示成功，否则表示失败，失败原因在errMessage里面体现 
	 */
	public PagesRep<ProductListResp> recommends(Specification<ProductChannel> spec, Pageable pageable) {
		Page<ProductChannel> cas = this.productChannelDao.findAll(spec, pageable);
		PagesRep<ProductListResp> pagesRep = new PagesRep<ProductListResp>();
		if (cas != null && cas.getContent() != null && cas.getTotalElements() > 0) {
			
			List<String> productOids = new ArrayList<String>();
			for (ProductChannel p : cas) {
				productOids.add(p.getProduct().getOid());
			}
			
			Map<String,List<LabelResp>> labelMap = this.findProductLabels(productOids);
			
			Map<String,Map<String,BigDecimal>> productExpArorMap = this.getProductsMinMaxRewards(productOids);
			List<ProductListResp> rows = new ArrayList<ProductListResp>();
			Page<IncomeAllocate> pcas = null;
			for (ProductChannel p : cas) {
				if(Product.TYPE_Producttype_02.equals(p.getProduct().getType().getOid())) {
					pcas = getProductIncomeAllocate(p.getProduct().getAssetPool().getOid(),1);
				} else {
					pcas = null;
				}
				ProductListResp queryRep = new ProductListResp(p,productExpArorMap,pcas);
				queryRep.setProductLabels(labelMap.get(p.getProduct().getOid()));
				rows.add(queryRep);
			}
			pagesRep.setRows(rows);
		}
		pagesRep.setTotal(cas.getTotalElements());
		return pagesRep;
	}
	
	/**
	 * app查询全部产品列表
	 * @param spec
	 * 
	 * @param 
	 * @return {@link PagesRep<ProductListResp>} ,如果返回的errCode属性等于0表示成功，否则表示失败，失败原因在errMessage里面体现 
	 */
	public PagesRep<ProductListResp> list(Specification<ProductChannel> spec, int page, int rows) {
		PagesRep<ProductListResp> pagesRep = new PagesRep<ProductListResp>();
		
		Direction sortDirection = Direction.DESC;
		Sort rackTimeSort = new Sort(new Order(sortDirection, "rackTime"));
		
		List<ProductChannel> productChannels = this.productChannelDao.findAll(spec,rackTimeSort);
		if(productChannels!=null && productChannels.size()>0) {
			List<ProductChannel> currnets = new ArrayList<ProductChannel>();//活期募集中
			List<ProductChannel> raisings = new ArrayList<ProductChannel>();//定期募集中
			List<ProductChannel> raiseends = new ArrayList<ProductChannel>();//定期募集結束
			
			for (ProductChannel pc : productChannels) {
				if(Product.TYPE_Producttype_01.equals(pc.getProduct().getType().getOid())) {
					if(Product.STATE_Raising.equals(pc.getProduct().getState())) {
						raisings.add(pc);
					} else if(Product.STATE_Raiseend.equals(pc.getProduct().getState())) {
						raiseends.add(pc);
					}
				} else {
					currnets.add(pc);
				}
			}
			
			List<ProductChannel> pcs = new ArrayList<ProductChannel>();
			for (ProductChannel pc : currnets) {
				pcs.add(pc);
			}
			for (ProductChannel pc : raisings) {
				pcs.add(pc);
			}
			for (ProductChannel pc : raiseends) {
				pcs.add(pc);
			}
			
			int i = 0;
			List<ProductChannel> pcrow = new ArrayList<ProductChannel>();//满足条件后的指定页数的列数的数据
			for (ProductChannel p : pcs) {
				if(i>=(page-1)*rows && i<page*rows) {
					pcrow.add(p);
				}
				i++;
			}
			if(pcrow.size()>0) {
				List<String> productOids = new ArrayList<String>();
				for (ProductChannel p : pcrow) {
					productOids.add(p.getProduct().getOid());
				}
				Map<String,List<LabelResp>> labelMap = this.findProductLabels(productOids);
				Map<String,Map<String,BigDecimal>> productExpArorMap = this.getProductsMinMaxRewards(productOids);
				
				List<ProductListResp> productRows = new ArrayList<ProductListResp>();
				for (ProductChannel p : pcrow) {
					ProductListResp queryRep = new ProductListResp(p, productExpArorMap, null);
					queryRep.setProductLabels(labelMap.get(p.getProduct().getOid()));
					productRows.add(queryRep);
				}
				pagesRep.setRows(productRows);
			}
			pagesRep.setTotal(pcs.size());
		}
		
		return pagesRep;
	}

	
	/**
	 * app定期查询可购买产品列表
	 * @param spec
	 * @param expArorStartStr
	 * @param expArorEndStr
	 * @param page
	 * @param rows
	 * @param sort
	 * @param order
	 * @return {@link PagesRep<ProductPeriodicResp>} ,如果返回的errCode属性等于0表示成功，否则表示失败，失败原因在errMessage里面体现 
	 */
	public PagesRep<ProductPeriodicResp> periodicList(Specification<ProductChannel> spec, String expArorStartStr, String expArorEndStr,
			int page, int rows, String sort, final String order) {
		PagesRep<ProductPeriodicResp> pagesRep = new PagesRep<ProductPeriodicResp>();
		
		Direction sortDirection = Direction.DESC;
		Sort rackTimeSort = new Sort(new Order(sortDirection, "rackTime"));
		
		if("rackTime".equals(sort)) {
			if (!"desc".equals(order)) {
				sortDirection = Direction.ASC;
			}
		}
		List<ProductChannel> productChannelList = this.productChannelDao.findAll(spec,rackTimeSort);
		if(productChannelList!=null && productChannelList.size()>0) {
			List<String> productOids = new ArrayList<String>();
			for (ProductChannel p : productChannelList) {
				productOids.add(p.getProduct().getOid());
			}
			
			List<ProductChannel> productChannels =  null;
			
			if(!StringUtil.isEmpty(expArorStartStr) || !StringUtil.isEmpty(expArorEndStr)) {
				productChannels =  new ArrayList<ProductChannel>();
				
				Map<String,Map<String,BigDecimal>> productExpArorMap = this.getProductsMinMaxRewards(productOids);
				
				if(!StringUtil.isEmpty(expArorStartStr) && !StringUtil.isEmpty(expArorEndStr)) {
					BigDecimal expArorStart = new BigDecimal(expArorStartStr);
					BigDecimal expArorEnd = new BigDecimal(expArorEndStr);
					for (ProductChannel pc : productChannelList) {
						BigDecimal expAror = pc.getProduct().getExpAror();
						BigDecimal expArorSec = pc.getProduct().getExpArorSec();
						if(productExpArorMap.get(pc.getProduct().getOid())!=null) {
							Map<String,BigDecimal> minMaxReward = productExpArorMap.get(pc.getProduct().getOid());
							expAror = expAror.add(minMaxReward.get("minReward"));
							expArorSec = expArorSec.add(minMaxReward.get("maxReward"));
						}
						if((expArorStart.compareTo(expAror)<=0 && expArorEnd.compareTo(expAror)>=0) 
								|| (expArorStart.compareTo(expArorSec)<=0 && expArorEnd.compareTo(expArorSec)>=0) ) {
							productChannels.add(pc);
						}
					}
				} else if(!StringUtil.isEmpty(expArorStartStr)) {
					BigDecimal expArorStart = new BigDecimal(expArorStartStr);
					for (ProductChannel pc : productChannelList) {
						BigDecimal expAror = pc.getProduct().getExpAror();
						BigDecimal expArorSec = pc.getProduct().getExpArorSec();
						if(productExpArorMap.get(pc.getProduct().getOid())!=null) {
							Map<String,BigDecimal> minMaxReward = productExpArorMap.get(pc.getProduct().getOid());
							expAror = expAror.add(minMaxReward.get("minReward"));
							expArorSec = expArorSec.add(minMaxReward.get("maxReward"));
						}
						if(expArorStart.compareTo(expAror)<=0 || expArorStart.compareTo(expArorSec)<=0) {
							productChannels.add(pc);
						}
					}
					
				} else if(!StringUtil.isEmpty(expArorEndStr)) {
					BigDecimal expArorEnd = new BigDecimal(expArorEndStr);
					for (ProductChannel pc : productChannelList) {
						BigDecimal expAror = pc.getProduct().getExpAror();
						BigDecimal expArorSec = pc.getProduct().getExpArorSec();
						if(productExpArorMap.get(pc.getProduct().getOid())!=null) {
							Map<String,BigDecimal> minMaxReward = productExpArorMap.get(pc.getProduct().getOid());
							expAror = expAror.add(minMaxReward.get("minReward"));
							expArorSec = expArorSec.add(minMaxReward.get("maxReward"));
						}
						if(expArorEnd.compareTo(expAror)>=0 || expArorEnd.compareTo(expArorSec)>=0){
							productChannels.add(pc);
						}
					}
				}
				
				List<ProductChannel> raisings = new ArrayList<ProductChannel>();//募集中
				List<ProductChannel> raiseends = new ArrayList<ProductChannel>();//募集結束
				
				//设置成基础收益率加上奖励收益率 便于按收益里排序的正确性
				for (ProductChannel pc : productChannels) {
					if(Product.STATE_Raising.equals(pc.getProduct().getState())) {
						raisings.add(pc);
					} else if(Product.STATE_Raiseend.equals(pc.getProduct().getState())) {
						raiseends.add(pc);
					}
				}
				
				if("expAror".equals(sort)) {//预期年化收益率 
					Collections.sort(raisings, expArorSort(order,productExpArorMap));
					Collections.sort(raiseends, expArorSort(order,productExpArorMap));
				} else if("investMin".equals(sort)) {//起投金额
					Collections.sort(raisings, investMinSort(order));
					Collections.sort(raiseends, investMinSort(order));
				} else if("durationPeriodDays".equals(sort)) {//投资期限
					Collections.sort(raisings, durationPeriodDaysSort(order));
					Collections.sort(raiseends, durationPeriodDaysSort(order));
				}
				
				List<ProductChannel> pcs = new ArrayList<ProductChannel>();
				for (ProductChannel pc : raisings) {
					pcs.add(pc);
				}
				for (ProductChannel pc : raiseends) {
					pcs.add(pc);
				}
				int i = 0;
				List<ProductPeriodicResp> productRows = new ArrayList<ProductPeriodicResp>();
				
				List<String> lproductOids = new ArrayList<String>();
				for (ProductChannel p : pcs) {
					lproductOids.add(p.getProduct().getOid());
				}
				Map<String,List<LabelResp>> labelMap = this.findProductLabels(lproductOids);
				
				for (ProductChannel p : pcs) {
					if(i>=(page-1)*rows && i<page*rows) {
						ProductPeriodicResp queryRep = new ProductPeriodicResp(p, productExpArorMap);
						queryRep.setProductLabels(labelMap.get(p.getProduct().getOid()));
						productRows.add(queryRep);
					}
					i++;
				}
				if(productRows.size()>0) {
					pagesRep.setRows(productRows);
				}
				pagesRep.setTotal(pcs.size());
			} else {
				productChannels = productChannelList;
				
				List<ProductChannel> raisings = new ArrayList<ProductChannel>();//募集中
				List<ProductChannel> raiseends = new ArrayList<ProductChannel>();//募集結束
				
				if("expAror".equals(sort)) {//预期年化收益率 
					
					Map<String,Map<String,BigDecimal>> productExpArorMap = this.getProductsMinMaxRewards(productOids);
					//设置成基础收益率加上奖励收益率 便于按收益里排序的正确性
					for (ProductChannel pc : productChannels) {
						if(Product.STATE_Raising.equals(pc.getProduct().getState())) {
							raisings.add(pc);
						} else if(Product.STATE_Raiseend.equals(pc.getProduct().getState())) {
							raiseends.add(pc);
						}
					}
					
					Collections.sort(raisings, expArorSort(order,productExpArorMap));
					Collections.sort(raiseends, expArorSort(order,productExpArorMap));
					
					List<ProductChannel> pcs = new ArrayList<ProductChannel>();
					for (ProductChannel pc : raisings) {
						pcs.add(pc);
					}
					for (ProductChannel pc : raiseends) {
						pcs.add(pc);
					}
					
					int i = 0;
					List<ProductPeriodicResp> productRows = new ArrayList<ProductPeriodicResp>();
					
					List<String> lproductOids = new ArrayList<String>();
					for (ProductChannel p : pcs) {
						lproductOids.add(p.getProduct().getOid());
					}
					Map<String,List<LabelResp>> labelMap = this.findProductLabels(lproductOids);
					
					for (ProductChannel p : pcs) {
						if(i>=(page-1)*rows && i<page*rows) {
							ProductPeriodicResp queryRep = new ProductPeriodicResp(p, productExpArorMap);
							queryRep.setProductLabels(labelMap.get(p.getProduct().getOid()));
							productRows.add(queryRep);
						}
						i++;
					}
					if(productRows.size()>0) {
						pagesRep.setRows(productRows);
					}
					pagesRep.setTotal(pcs.size());
				} else {
					for (ProductChannel pc : productChannels) {
						if(Product.STATE_Raising.equals(pc.getProduct().getState())) {
							raisings.add(pc);
						} else if(Product.STATE_Raiseend.equals(pc.getProduct().getState())) {
							raiseends.add(pc);
						}
					}
					if("investMin".equals(sort)) {//起投金额
						Collections.sort(raisings, investMinSort(order));
						Collections.sort(raiseends, investMinSort(order));
					} else if("durationPeriodDays".equals(sort)) {//投资期限
						Collections.sort(raisings, durationPeriodDaysSort(order));
						Collections.sort(raiseends, durationPeriodDaysSort(order));
					}
					
					List<ProductChannel> pcs = new ArrayList<ProductChannel>();
					for (ProductChannel pc : raisings) {
						pcs.add(pc);
					}
					for (ProductChannel pc : raiseends) {
						pcs.add(pc);
					}
					
					int i = 0;
					List<ProductChannel> pcrow = new ArrayList<ProductChannel>();//满足条件后的指定页数的列数的数据
					for (ProductChannel p : pcs) {
						if(i>=(page-1)*rows && i<page*rows) {
							pcrow.add(p);
						}
						i++;
					}
					
					if(pcrow.size()>0) {
						productOids = new ArrayList<String>();
						for (ProductChannel p : pcrow) {
							productOids.add(p.getProduct().getOid());
						}
						
						Map<String,List<LabelResp>> labelMap = this.findProductLabels(productOids);
						Map<String,Map<String,BigDecimal>> productExpArorMap = this.getProductsMinMaxRewards(productOids);
						
						List<ProductPeriodicResp> productRows = new ArrayList<ProductPeriodicResp>();
						for (ProductChannel p : pcrow) {
							ProductPeriodicResp queryRep = new ProductPeriodicResp(p, productExpArorMap);
							queryRep.setProductLabels(labelMap.get(p.getProduct().getOid()));
							productRows.add(queryRep);
						}
						pagesRep.setRows(productRows);
					}
					pagesRep.setTotal(pcs.size());
					
				}
			}
			
		}
		return pagesRep;
	}
	
	/**
	 * app活期查询可购买产品列表
	 * @param spec
	 * @param pageable
	 * @return {@link PagesRep<ProductCurrentResp>} ,如果返回的errCode属性等于0表示成功，否则表示失败，失败原因在errMessage里面体现 
	 */
	public PagesRep<ProductCurrentResp> currentList(Specification<ProductChannel> spec, int page, int rows, String sort, final String order) {
		PagesRep<ProductCurrentResp> pagesRep = new PagesRep<ProductCurrentResp>();
		Direction sortDirection = Direction.DESC;
		if (!"desc".equals(order)) {
			sortDirection = Direction.ASC;
		}
		Sort rackTimeSort = new Sort(new Order(sortDirection, sort));
		
		List<ProductChannel> productChannelList = this.productChannelDao.findAll(spec,rackTimeSort);
		if(productChannelList!=null && productChannelList.size()>0) {
//			Collections.sort(productChannelList, maxSalelockCollectedVolumeSort(order));
			int i = 0;
			List<ProductChannel> pcrow = new ArrayList<ProductChannel>();//满足条件后的指定页数的列数的数据
			for (ProductChannel p : productChannelList) {
				if(i>=(page-1)*rows && i<page*rows) {
					pcrow.add(p); 
				}
				i++;
			}
			if(pcrow.size()>0) {
				List<String> productOids = new ArrayList<String>();
				for (ProductChannel p : pcrow) {
					productOids.add(p.getProduct().getOid());
				}
				Map<String,List<LabelResp>> labelMap = this.findProductLabels(productOids);
				Map<String,Map<String,BigDecimal>> productExpArorMap = this.getProductsMinMaxRewards(productOids);
				List<ProductCurrentResp> productRows = new ArrayList<ProductCurrentResp>();
				for (ProductChannel cp : pcrow) {
					ProductCurrentResp queryRep = new ProductCurrentResp();
					Product product = cp.getProduct();
					queryRep.setOid(product.getOid());
					queryRep.setChannelOid(cp.getChannel().getOid());
					queryRep.setProductCode(product.getCode());
					queryRep.setProductName(product.getName());
					queryRep.setProductFullName(product.getFullName());//产品全称
					queryRep.setCurrentVolume(product.getCurrentVolume());//当前金额
					queryRep.setCollectedVolume(product.getCollectedVolume());//已集总金额
					queryRep.setLockCollectedVolume(product.getLockCollectedVolume());//锁定已募份额
					queryRep.setRaisedTotalNumber(product.getRaisedTotalNumber());//募集总份额
					queryRep.setMaxSaleVolume(product.getMaxSaleVolume());//最高可售份额
					queryRep.setPurchaseNum(product.getPurchaseNum());
					queryRep.setInvestMin(product.getInvestMin());//单笔投资最低金额
					queryRep.setLockPeriodDays(product.getLockPeriodDays());//锁定期
					queryRep.setNetUnitShare(product.getNetUnitShare());
					
					String incomeCalcBasis = product.getIncomeCalcBasis();//产品计算基础
					queryRep.setSubType(ProductPojo.DEMAND_SUBTYPE);
					if(productExpArorMap.get(product.getOid())!=null) {
						queryRep.setSubType(ProductPojo.INCREMENT_SUBTYPE);
						Map<String,BigDecimal> minMaxReward = productExpArorMap.get(product.getOid());
						BigDecimal minReward = minMaxReward.get("minReward");
						BigDecimal maxReward = minMaxReward.get("maxReward");
						
						String minRewardStr = ProductDecimalFormat.format(ProductDecimalFormat.multiply(product.getExpAror().add(minReward)))+"%";
						String maxRewardStr = ProductDecimalFormat.format(ProductDecimalFormat.multiply(product.getExpAror().add(maxReward)))+"%";
						if(minRewardStr.equals(maxRewardStr)) {
							queryRep.setRewardYieldRange(minRewardStr);
						} else {
							queryRep.setRewardYieldRange("0%-0%");
						}
						
						String rewardTenThsProfitFst = ProductDecimalFormat.format(minReward.multiply(new BigDecimal(10000)).divide(new BigDecimal(incomeCalcBasis), 4, RoundingMode.HALF_UP),"0.0000");
						String rewardTenThsProfitSec = ProductDecimalFormat.format(maxReward.multiply(new BigDecimal(10000)).divide(new BigDecimal(incomeCalcBasis), 4, RoundingMode.HALF_UP),"0.0000");
						if(rewardTenThsProfitFst.equals(rewardTenThsProfitSec)) {
							queryRep.setRewardTenThsProfit(rewardTenThsProfitFst);
						} else {
							queryRep.setRewardTenThsProfit(rewardTenThsProfitFst+"-"+rewardTenThsProfitSec);
						}
					}
					
					if((product.getExpAror()!=null && product.getExpAror().compareTo(new BigDecimal("0"))>0)
							|| (product.getExpArorSec()!=null && product.getExpArorSec().compareTo(new BigDecimal("0"))>0)) {
						BigDecimal expAror = product.getExpAror();//预期年化收益率
						BigDecimal expArorSec = product.getExpArorSec();//预期年化收益率
						
						String expArorStr = ProductDecimalFormat.format(ProductDecimalFormat.multiply(expAror))+"%";
						String expArorSecStr = ProductDecimalFormat.format(ProductDecimalFormat.multiply(expArorSec))+"%";
						if(expArorStr.equals(expArorSecStr)) {
							queryRep.setAnnualInterestSec(expArorStr);
						} else {
							queryRep.setAnnualInterestSec(expArorStr+"-"+expArorSecStr);
						}
						
						String tenThsPerDayProfitFst = ProductDecimalFormat.format(expAror.multiply(new BigDecimal(10000)).divide(new BigDecimal(incomeCalcBasis), 4, RoundingMode.HALF_UP),"0.0000");
						String tenThsPerDayProfitSec = ProductDecimalFormat.format(expArorSec.multiply(new BigDecimal(10000)).divide(new BigDecimal(incomeCalcBasis), 4, RoundingMode.HALF_UP),"0.0000");
						if(tenThsPerDayProfitFst.equals(tenThsPerDayProfitSec)) {
							queryRep.setTenThsPerDayProfit(tenThsPerDayProfitFst);
						} else {
							queryRep.setTenThsPerDayProfit(tenThsPerDayProfitFst+"-"+tenThsPerDayProfitSec);
						}
						
						if(expArorStr.equals(expArorSecStr)) {//固定预期收益率 
							if(productExpArorMap.get(product.getOid())!=null) {
								queryRep.setShowType(ProductCurrentResp.SHOW_TYPE_5);
							} else {
								queryRep.setShowType(ProductCurrentResp.SHOW_TYPE_2);
							}
						} else {
							if(productExpArorMap.get(product.getOid())!=null) {
								queryRep.setShowType(ProductCurrentResp.SHOW_TYPE_4);
							} else {
								queryRep.setShowType(ProductCurrentResp.SHOW_TYPE_1);
							}
						}
					} else {
						if(productExpArorMap.get(product.getOid())!=null) {
							Page<IncomeAllocate> pcas = getProductIncomeAllocate(product.getAssetPool().getOid(),1);
							if (pcas != null && pcas.getContent() != null && pcas.getTotalElements() > 0) {
								BigDecimal yesterdayYieldExp = pcas.getContent().get(0).getRatio();//昨日年化收益率
								queryRep.setYesterdayYield(ProductDecimalFormat.format(ProductDecimalFormat.multiply(yesterdayYieldExp))+"%");//昨日年化收益率
								queryRep.setTenThsPerDayProfit(ProductDecimalFormat.format(yesterdayYieldExp.multiply(new BigDecimal(10000)).divide(new BigDecimal(incomeCalcBasis), 4, RoundingMode.HALF_UP),"0.0000"));
								queryRep.setShowType(ProductCurrentResp.SHOW_TYPE_6);
							} else {
								queryRep.setShowType(ProductCurrentResp.SHOW_TYPE_7);
							}
						} else {
							Page<IncomeAllocate> pcas = getProductIncomeAllocate(product.getAssetPool().getOid(),7);
							if (pcas != null && pcas.getContent() != null && pcas.getTotalElements() > 0) {
								BigDecimal sevenDayYieldRatio = new BigDecimal("0");
								for(IncomeAllocate ia : pcas.getContent()) {
									sevenDayYieldRatio = sevenDayYieldRatio.add(ia.getRatio());
								}
								BigDecimal sevenDayYieldExp = sevenDayYieldRatio.multiply(new BigDecimal("100")).divide(new BigDecimal(""+new Long(pcas.getTotalElements()).intValue()), 4, RoundingMode.HALF_UP);//七日年化收益率单位（%）
								queryRep.setSevenDayYield(ProductDecimalFormat.format(sevenDayYieldExp,"0.00")+"%");//七日年化收益率:最新7条取平均值
								queryRep.setTenThsPerDayProfit(ProductDecimalFormat.format(sevenDayYieldExp.divide(new BigDecimal("100")).multiply(new BigDecimal("10000")).divide(new BigDecimal(incomeCalcBasis), 4, RoundingMode.HALF_UP),"0.0000"));
							}
							queryRep.setShowType(ProductCurrentResp.SHOW_TYPE_3);
						}
					}
					queryRep.setProductLabels(labelMap.get(product.getOid()));
					productRows.add(queryRep);
				}
				pagesRep.setRows(productRows);
			}
		}
		
		pagesRep.setTotal(productChannelList.size());
	
		return pagesRep;
	}
	
	/**
	 * 获取某个产品的最新收益分配
	 * @param assetPoolOid
	 * @return
	 */
	private Page<IncomeAllocate> getProductIncomeAllocate(final String assetPoolOid,int rows) {
		//收益分配
		Specification<IncomeAllocate> pspec = new Specification<IncomeAllocate>() {
			@Override
			public Predicate toPredicate(Root<IncomeAllocate> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.and(cb.equal(root.get("incomeEvent").get("status").as(String.class), IncomeEvent.STATUS_Allocated),
						cb.equal(root.get("incomeEvent").get("assetPool").get("oid").as(String.class), assetPoolOid));
				
			}
		};
		pspec = Specifications.where(pspec);
		Page<IncomeAllocate> pcas = this.incomeAllocateDao.findAll(pspec, new PageRequest(0, rows, new Sort(new Order(Direction.DESC, "baseDate"))));
		return pcas;
	}
	
	
	/**
	 * 活期产品详情
	 */
	public ProductCurrentDetailResp currentDetail(String oid) {

		Product product = productService.getProductByOid(oid);

		String incomeCalcBasis = product.getIncomeCalcBasis();// 产品计算基础
		BigDecimal expAror = product.getExpAror();// 预期年化收益率
		BigDecimal expArorSec = product.getExpArorSec();// 预期年化收益率

		String expArorStr = null;// 产品预期年化收益率开始值
		String expArorSecStr = null;// 产品预期年化收益率结束值
		if (product.getExpAror() != null && product.getExpAror().compareTo(new BigDecimal("0")) > 0) {
			expArorStr = ProductDecimalFormat.format(ProductDecimalFormat.multiply(expAror)) + "%";
		}
		if (product.getExpArorSec() != null && product.getExpArorSec().compareTo(new BigDecimal("0")) > 0) {
			expArorSecStr = ProductDecimalFormat.format(ProductDecimalFormat.multiply(expArorSec)) + "%";
		}

		List<ProductIncomeRewardSnapshot> prewards = incomeRewardSnapshotService.findBySnapshotDate(DateUtil.getBeforeDate());// 奖励收益率
		if(oid.equals(productService.getOnSaleProductOid().getProductOid()) && prewards != null) {
			prewards.clear();
		}
		Page<IncomeAllocate> pcas = getProductIncomeAllocate(product.getAssetPool().getOid(), 30);// 实际收益分配的产品收益率

		ProductCurrentDetailResp pr = new ProductCurrentDetailResp(product);
		Corporate corporate = this.corporateDao.findOne(product.getPublisherBaseAccount().getCorperateOid());
		pr.setIsShowRedPackets("NO");
		pr.setCompanyName(corporate.getName());
		// 根据当天时间计算起息日期
		java.sql.Date beginAccuralDate = this.orderDateService.getBeginAccuralDate(product);
		pr.setInterestsFirstDate(beginAccuralDate);

		/******************* 有奖励收益start *************/
		if (prewards != null && prewards.size() > 0) {// 有奖励收益
			BigDecimal minReward = prewards.get(0).getRatio();
			BigDecimal maxReward = prewards.get(0).getRatio();

			/******************* 昨日年化收益率柱状图start *************/
			BigDecimal yesterdayYieldExp = new BigDecimal("0");
			if (pcas != null && pcas.getContent() != null && pcas.getTotalElements() > 0) {
				yesterdayYieldExp = pcas.getContent().get(0).getRatio();// 昨日年化收益率
			} else if (!StringUtil.isEmpty(expArorStr) || !StringUtil.isEmpty(expArorSecStr)) {
				if (expArorStr.equals(expArorSecStr)) {
					yesterdayYieldExp = product.getExpAror();
				} else {
					if (!StringUtil.isEmpty(expArorStr) && !StringUtil.isEmpty(expArorSecStr)) {
//						yesterdayYieldExp = product.getExpAror().add(product.getExpArorSec())
//								.divide(new BigDecimal("2"));
						yesterdayYieldExp = expAror;
					} else if (!StringUtil.isEmpty(expArorStr)) {
						yesterdayYieldExp = product.getExpAror();
					} else {
						yesterdayYieldExp = product.getExpArorSec();
					}
				}
			}
			List<ProductDetailIncomeRewardProfit> rewardYields = new ArrayList<ProductDetailIncomeRewardProfit>();// 奖励收益率
																													// 单位（%）
			ProductDetailIncomeRewardProfit rewardYield = null;
			for (ProductIncomeRewardSnapshot preward : prewards) {
				if (preward.getRatio().compareTo(minReward) < 0) {
					minReward = preward.getRatio();
				}
				if (preward.getRatio().compareTo(maxReward) > 0) {
					maxReward = preward.getRatio();
				}

				rewardYield = new ProductDetailIncomeRewardProfit();
				rewardYield.setProfit(ProductDecimalFormat
						.format(ProductDecimalFormat.multiply(yesterdayYieldExp.add(preward.getRatio()))));
				if (preward.getEndDate() != null && preward.getEndDate().intValue() > 0) {
					rewardYield.setStandard(preward.getStartDate() + "天-" + preward.getEndDate() + "天");
					rewardYield.setEndDate(preward.getEndDate().toString());// 截止天数
				} else {
					rewardYield.setStandard("大于等于" + preward.getStartDate() + "天");
				}
				rewardYield.setLevel(preward.getLevel());// 阶梯名称
				rewardYield.setStartDate(preward.getStartDate().toString());// 起始天数
				rewardYields.add(rewardYield);
			}
			pr.setRewardYields(rewardYields);
			/******************* 昨日年化收益率柱状图end *************/

			/**************** 奖励年化收益率start ****************/
			String minRewardStr = ProductDecimalFormat.format(ProductDecimalFormat.multiply(minReward)) + "%";
			String maxRewardStr = ProductDecimalFormat.format(ProductDecimalFormat.multiply(maxReward)) + "%";
			if (minRewardStr.equals(maxRewardStr)) {
				pr.setRewardYieldRange(minRewardStr);
			} else {
				pr.setRewardYieldRange("0%-0%");
			}

			String rewardTenThsProfitFst = ProductDecimalFormat.format(minReward.multiply(new BigDecimal(10000))
					.divide(new BigDecimal(incomeCalcBasis), 4, RoundingMode.HALF_UP), "0.0000");
			String rewardTenThsProfitSec = ProductDecimalFormat.format(maxReward.multiply(new BigDecimal(10000))
					.divide(new BigDecimal(incomeCalcBasis), 4, RoundingMode.HALF_UP), "0.0000");
			if (rewardTenThsProfitFst.equals(rewardTenThsProfitSec)) {
				pr.setRewardTenThsProfit(rewardTenThsProfitFst);
			} else {
				pr.setRewardTenThsProfit(rewardTenThsProfitFst + "-" + rewardTenThsProfitSec);
			}
			/**************** 奖励年化收益率end ****************/
		}
		/******************* 有奖励收益end *************/

		/**************** 基准年化收益率走势图start ****************/
		CurrentProductDetailProfit[] annualYields = null;// 基准年化收益率走势 单位（%）
		CurrentProductDetailProfit[] perMillionIncomes = null;// 基准万份收益走势 单位（元）
		CurrentProductDetailProfit annualYield = null;
		CurrentProductDetailProfit perMillionIncome = null;

		if ((product.getExpAror() != null && product.getExpAror().compareTo(new BigDecimal("0")) > 0)
				|| (product.getExpArorSec() != null && product.getExpArorSec().compareTo(new BigDecimal("0")) > 0)) {
			// 确保有30条
			annualYields = new CurrentProductDetailProfit[30];// 基准年化收益率走势 单位（%）
			perMillionIncomes = new CurrentProductDetailProfit[30];// 基准万份收益走势
																	// 单位（元）

			// 用产品预期年化收率补齐前面的数据start
			for (int i = annualYields.length - 1; i >= 0; i--) {
				annualYield = new CurrentProductDetailProfit();
				annualYield.setProfit(
						ProductDecimalFormat.format(ProductDecimalFormat.multiply(product.getExpAror())));
				annualYield.setStandard(DateUtil.format(DateUtil.addDay(new Date(), i - annualYields.length)));
				annualYields[i] = annualYield;

				perMillionIncome = new CurrentProductDetailProfit();
				perMillionIncome.setProfit(getTenThousandIncome(product, incomeCalcBasis));
				perMillionIncome.setStandard(DateUtil.format(DateUtil.addDay(new Date(), i - annualYields.length)));
				perMillionIncomes[i] = perMillionIncome;
			}
			// 用产品预期年化收率补齐前面的数据end

		} else {
			if (pcas != null && pcas.getContent() != null && pcas.getTotalElements() > 0) {
				annualYields = new CurrentProductDetailProfit[new Long(pcas.getTotalElements()).intValue()];// 基准年化收益率走势
																											// 单位（%）
				perMillionIncomes = new CurrentProductDetailProfit[new Long(pcas.getTotalElements()).intValue()];// 基准万份收益走势
																													// 单位（元）
			}
		}

		List<BigDecimal> ratios = new ArrayList<BigDecimal>();
		// 用最新的实际收益分配 替换最后面的数据 start
		if (pcas != null && pcas.getContent() != null && pcas.getTotalElements() > 0) {
			int i = annualYields.length - 1;
			for (IncomeAllocate ia : pcas) {
				if (ratios.size() <= 7) {
					ratios.add(ia.getRatio());
				}

				annualYield = new CurrentProductDetailProfit();
				annualYield.setProfit(ProductDecimalFormat.format(ProductDecimalFormat.multiply(ia.getRatio())));
				annualYield.setStandard(DateUtil.format(ia.getBaseDate()));
				annualYields[i] = annualYield;

				perMillionIncome = new CurrentProductDetailProfit();
				perMillionIncome.setProfit(ProductDecimalFormat.format(ia.getWincome(), "0.0000"));
				perMillionIncome.setStandard(DateUtil.format(ia.getBaseDate()));
				perMillionIncomes[i] = perMillionIncome;

				i--;
			}
			pr.setAnnualYields(Arrays.asList(annualYields));
			pr.setPerMillionIncomes(Arrays.asList(perMillionIncomes));
		}
		// 用最新的实际收益分配 替换最后面的数据 end
		pr.setAnnualYields(Arrays.asList(annualYields));
		pr.setPerMillionIncomes(Arrays.asList(perMillionIncomes));
		/******************* 基准年化收益率走势图 end *************/

		if (!StringUtil.isEmpty(expArorStr) || !StringUtil.isEmpty(expArorSecStr)) {// 有预期年化收益率
			/**************** 预期年化收益率start ****************/

			if (expArorStr.equals(expArorSecStr)) {
				pr.setAnnualInterestSec(expArorStr);
				String tenThsPerDayProfitFst = ProductDecimalFormat.format(expAror.multiply(new BigDecimal(10000))
						.divide(new BigDecimal(incomeCalcBasis), 4, RoundingMode.HALF_UP), "0.0000");
				pr.setTenThsPerDayProfit(tenThsPerDayProfitFst);
			} else {
				if (!StringUtil.isEmpty(expArorStr) && !StringUtil.isEmpty(expArorSecStr)) {
					pr.setAnnualInterestSec(expArorStr + "-" + expArorSecStr);
					String tenThsPerDayProfitFst = ProductDecimalFormat.format(expAror.multiply(new BigDecimal(10000))
							.divide(new BigDecimal(incomeCalcBasis), 4, RoundingMode.HALF_UP), "0.0000");
					String tenThsPerDayProfitSec = ProductDecimalFormat
							.format(expArorSec.multiply(new BigDecimal(10000)).divide(new BigDecimal(incomeCalcBasis),
									4, RoundingMode.HALF_UP), "0.0000");
					pr.setTenThsPerDayProfit(tenThsPerDayProfitFst + "-" + tenThsPerDayProfitSec);
				} else if (!StringUtil.isEmpty(expArorStr)) {
					pr.setAnnualInterestSec(expArorStr);
					String tenThsPerDayProfitFst = ProductDecimalFormat.format(expAror.multiply(new BigDecimal(10000))
							.divide(new BigDecimal(incomeCalcBasis), 4, RoundingMode.HALF_UP), "0.0000");
					pr.setTenThsPerDayProfit(tenThsPerDayProfitFst);
				} else {
					pr.setAnnualInterestSec(expArorSecStr);
					String tenThsPerDayProfitSec = ProductDecimalFormat
							.format(expArorSec.multiply(new BigDecimal(10000)).divide(new BigDecimal(incomeCalcBasis),
									4, RoundingMode.HALF_UP), "0.0000");
					pr.setTenThsPerDayProfit(tenThsPerDayProfitSec);
				}
			}
			/**************** 预期年化收益率end ****************/

			if (expArorStr.equals(expArorSecStr)) {// 固定预期收益率
				if (prewards != null && prewards.size() > 0) {// 有奖励收益
					pr.setShowType(ProductCurrentResp.SHOW_TYPE_5);
				} else {
					pr.setShowType(ProductCurrentResp.SHOW_TYPE_2);
				}
			} else {
				if (prewards != null && prewards.size() > 0) {// 有奖励收益
					pr.setShowType(ProductCurrentResp.SHOW_TYPE_4);
				} else {
					pr.setShowType(ProductCurrentResp.SHOW_TYPE_1);
				}
			}
		} else {
			if (prewards != null && prewards.size() > 0) {// 有奖励收益
				if (pcas != null && pcas.getContent() != null && pcas.getTotalElements() > 0) {
					pr.setShowType(ProductCurrentResp.SHOW_TYPE_6);

					BigDecimal yesterdayYieldExp = pcas.getContent().get(0).getRatio();// 昨日年化收益率
					pr.setYesterdayYield(
							ProductDecimalFormat.format(ProductDecimalFormat.multiply(yesterdayYieldExp)) + "%");// 昨日年化收益率
					pr.setTenThsPerDayProfit(
							ProductDecimalFormat.format(yesterdayYieldExp.multiply(new BigDecimal(10000))
									.divide(new BigDecimal(incomeCalcBasis), 4, RoundingMode.HALF_UP), "0.0000"));
				} else {
					pr.setShowType(ProductCurrentResp.SHOW_TYPE_7);
				}
			} else {
				if (pcas != null && pcas.getContent() != null && pcas.getTotalElements() > 0) {
					pr.setShowType(ProductCurrentResp.SHOW_TYPE_3);

					BigDecimal sevenDayYieldRatio = new BigDecimal("0");

					for (BigDecimal ratio : ratios) {
						sevenDayYieldRatio = sevenDayYieldRatio.add(ratio);
					}
					BigDecimal sevenDayYieldExp = sevenDayYieldRatio.multiply(new BigDecimal("100"))
							.divide(new BigDecimal("" + ratios.size()), 4, RoundingMode.HALF_UP);// 七日年化收益率单位（%）
					pr.setSevenDayYield(ProductDecimalFormat.format(sevenDayYieldExp, "0.00") + "%");// 七日年化收益率:最新7条取平均值
					pr.setTenThsPerDayProfit(
							ProductDecimalFormat.format(
									sevenDayYieldExp.divide(new BigDecimal("100")).multiply(new BigDecimal("10000"))
											.divide(new BigDecimal(incomeCalcBasis), 4, RoundingMode.HALF_UP),
									"0.0000"));
				}
			}
		}

		Map<String, AdminObj> adminObjMap = new HashMap<String, AdminObj>();

		FileResp fr = null;
		AdminObj adminObj = null;
		if (!StringUtil.isEmpty(product.getFileKeys())) {
			List<File> files = this.fileService.list(product.getFileKeys(), File.STATE_Valid);
			if (files.size() > 0) {
				pr.setFiles(new ArrayList<FileResp>());

				for (File file : files) {
					fr = new FileResp(file);
					if (adminObjMap.get(file.getOperator()) == null) {
						try {
							adminObj = adminSdk.getAdmin(file.getOperator());
							adminObjMap.put(file.getOperator(), adminObj);
						} catch (Exception e) {
						}
					}
					if (adminObjMap.get(file.getOperator()) != null) {
						fr.setOperator(adminObjMap.get(file.getOperator()).getName());
					}
					pr.getFiles().add(fr);
				}
			}
		}

		if (!StringUtil.isEmpty(product.getInvestFileKey())) {
			List<File> files = this.fileService.list(product.getInvestFileKey(), File.STATE_Valid);
			if (files.size() > 0) {
				pr.setInvestFiles(new ArrayList<FileResp>());

				for (File file : files) {
					fr = new FileResp(file);
					if (adminObjMap.get(file.getOperator()) == null) {
						try {
							adminObj = adminSdk.getAdmin(file.getOperator());
							adminObjMap.put(file.getOperator(), adminObj);
						} catch (Exception e) {
						}
					}
					if (adminObjMap.get(file.getOperator()) != null) {
						fr.setOperator(adminObjMap.get(file.getOperator()).getName());
					}
					pr.getInvestFiles().add(fr);
				}
			}
		}

		if (!StringUtil.isEmpty(product.getServiceFileKey())) {
			List<File> files = this.fileService.list(product.getServiceFileKey(), File.STATE_Valid);
			if (files.size() > 0) {
				pr.setServiceFiles(new ArrayList<FileResp>());

				for (File file : files) {
					fr = new FileResp(file);
					if (adminObjMap.get(file.getOperator()) == null) {
						try {
							adminObj = adminSdk.getAdmin(file.getOperator());
							adminObjMap.put(file.getOperator(), adminObj);
						} catch (Exception e) {
						}
					}
					if (adminObjMap.get(file.getOperator()) != null) {
						fr.setOperator(adminObjMap.get(file.getOperator()).getName());
					}
					pr.getServiceFiles().add(fr);
				}
			}
		}

		List<ProductLabel> productLabels = productLabelService.findProductLabelsByProduct(product);
		if (productLabels != null && productLabels.size() > 0) {
			List<LabelResp> labelResps = new ArrayList<LabelResp>();
			for (ProductLabel pl : productLabels) {
				labelResps.add(new LabelResp(pl.getLabel()));
			}
			pr.setProductLabels(labelResps);
		}
		/**
		 * 关联卡券
		 */
		pr.setIsShowRedPacketsNew(product.getUseRedPackages());
		pr.setIsShowRaiseCoupons(product.getUseraiseRateCoupons());
		if(ProductCouponEnum.useRedCoupon.getCode()==product.getUseRedPackages()){
			pr.setRedPackages(productCouponService.getRedListByProductOid(product.getOid()));
		}
		if(ProductCouponEnum.useRaiseRateCoupon.getCode()==product.getUseraiseRateCoupons()){
			pr.setRaiseCoupons(productCouponService.getRaiseListByProductOid(product.getOid()));
		}
		return pr;

	}
	
	private String getTenThousandIncome(Product product, String incomeCalcBasis) {

		return InterestFormula.compound(new BigDecimal("10000"), product.getExpAror(), product.getIncomeCalcBasis()).toString();
	
	}
	
	/**
	 * 定期产品详情
	 * @param oid
	 * @return
	 */
	public ProductPeriodicDetailResp periodicDdetail(String oid, String uid) {
		Product product = productService.getProductByOid(oid);
		List<ProductIncomeReward> prewards = rewardService.productRewardList(oid);
		ProductPeriodicDetailResp pr;
		if(!StringUtils.isEmpty(product.getActivityDetail())) {
			ProductTypeDetail productTypeDetail = productTypeDetailService.getOne(product.getActivityDetail());
			pr = new ProductPeriodicDetailResp(product, prewards, productTypeDetail.getUrl());
		}else {
			pr = new ProductPeriodicDetailResp(product, prewards, null);
		}
		// 07 快定宝处理
		//增加快定宝 07类型
		if ( Product.TYPE_Producttype_03.equals(product.getType().getOid())){
			//重新计算时间
			/**
			 * a.预计起息日： 申购日的下一个工作日。
			 b.预计到期日：计算公式：起息日+存续期-1。
			 c.预计到账日：起息日+存续期。
			 */
			pr.setSetupDate(this.orderDateService.get03ProductBeginAccuralDate(product, new Timestamp(System.currentTimeMillis())));
			pr.setInterestsEndDate(new java.sql.Date(DateUtil.addDay(new java.util.Date(pr.getSetupDate().getTime()), pr.getDurationPeriod() -1).getTime()));
			pr.setRepayDate(new java.sql.Date(DateUtil.addDay(new java.util.Date(pr.getSetupDate().getTime()), pr.getDurationPeriod()).getTime()));
		}
		
		if (prewards != null && prewards.size() > 0) {//算上奖励收益
			BigDecimal minReward = prewards.get(0).getRatio();
			BigDecimal maxReward = prewards.get(0).getRatio();
			for (ProductIncomeReward preward : prewards) {
				if (preward.getRatio().compareTo(minReward) < 0) {
					minReward = preward.getRatio();
				}
				if (preward.getRatio().compareTo(maxReward) > 0) {
					maxReward = preward.getRatio();
				}
			}
		}
		
		
		if (Product.STATE_Raising.equals(product.getState())) {
			// 投资时间
			pr.setInvestTime(DateUtil.getSqlCurrentDate());
		} else {
			if (uid != null) {
				HoldCacheEntity hold = cacheHoldService.getHoldCacheEntityByInvestorOidAndProductOid(uid, oid);
				if (null == hold) {
					throw new AMPException("未投资用户不可查看详情");
				}
				pr.setInvestTime(hold.getLatestOrderTime());
			}
			
		}
		
		Map<String, AdminObj> adminObjMap = new HashMap<String, AdminObj>();

		FileResp fr = null;
		AdminObj adminObj = null;
		if (!StringUtil.isEmpty(product.getFileKeys())) {
			List<File> files = this.fileService.list(product.getFileKeys(), File.STATE_Valid);
			if (files.size() > 0) {
				pr.setFiles(new ArrayList<FileResp>());

				for (File file : files) {
					fr = new FileResp(file);
					if (adminObjMap.get(file.getOperator()) == null) {
						try {
							adminObj = adminSdk.getAdmin(file.getOperator());
							adminObjMap.put(file.getOperator(), adminObj);
						} catch (Exception e) {
						}
					}
					if (adminObjMap.get(file.getOperator()) != null) {
						fr.setOperator(adminObjMap.get(file.getOperator()).getName());
					}
					pr.getFiles().add(fr);
				}
			}
		}

		if (!StringUtil.isEmpty(product.getInvestFileKey())) {
			List<File> files = this.fileService.list(product.getInvestFileKey(), File.STATE_Valid);
			if (files.size() > 0) {
				pr.setInvestFiles(new ArrayList<FileResp>());

				for (File file : files) {
					fr = new FileResp(file);
					if (adminObjMap.get(file.getOperator()) == null) {
						try {
							adminObj = adminSdk.getAdmin(file.getOperator());
							adminObjMap.put(file.getOperator(), adminObj);
						} catch (Exception e) {
						}
					}
					if (adminObjMap.get(file.getOperator()) != null) {
						fr.setOperator(adminObjMap.get(file.getOperator()).getName());
					}
					pr.getInvestFiles().add(fr);
				}
			}
		}

		if (!StringUtil.isEmpty(product.getServiceFileKey())) {
			List<File> files = this.fileService.list(product.getServiceFileKey(), File.STATE_Valid);
			if (files.size() > 0) {
				pr.setServiceFiles(new ArrayList<FileResp>());

				for (File file : files) {
					fr = new FileResp(file);
					if (adminObjMap.get(file.getOperator()) == null) {
						try {
							adminObj = adminSdk.getAdmin(file.getOperator());
							adminObjMap.put(file.getOperator(), adminObj);
						} catch (Exception e) {
						}
					}
					if (adminObjMap.get(file.getOperator()) != null) {
						fr.setOperator(adminObjMap.get(file.getOperator()).getName());
					}
					pr.getServiceFiles().add(fr);
				}
			}
		}
		List<ProductLabel> productLabels = productLabelService.findProductLabelsByProduct(product);
		if (productLabels != null && productLabels.size() > 0) {
			List<LabelResp> labelResps = new ArrayList<LabelResp>();
			for (ProductLabel pl : productLabels) {
				labelResps.add(new LabelResp(pl.getLabel()));
			}
			pr.setProductLabels(labelResps);
		}
		/**
		 * 关联卡券
		 */
		if(product.getProductPackage()!=null){
			pr.setIsShowRedPacketsNew(product.getProductPackage().getUseRedPackages());
			pr.setIsShowRaiseCoupons(product.getProductPackage().getUseraiseRateCoupons());
		}else{
			pr.setIsShowRedPacketsNew(product.getUseRedPackages());
			pr.setIsShowRaiseCoupons(product.getUseraiseRateCoupons());
		}
		if(product.getProductPackage()==null&&ProductCouponEnum.useRedCoupon.getCode()==product.getUseRedPackages()){
			pr.setRedPackages(productCouponService.getRedListByProductOid(product.getOid()));
		}else if(product.getProductPackage()!=null&&ProductCouponEnum.useRedCoupon.getCode()==product.getProductPackage().getUseRedPackages()){
			pr.setRedPackages(productPackageCouponService.getRedListByProductPackageOid(product.getProductPackage().getOid()));
		}
		if(product.getProductPackage()==null&&ProductCouponEnum.useRaiseRateCoupon.getCode()==product.getUseraiseRateCoupons()){
			pr.setRaiseCoupons(productCouponService.getRaiseListByProductOid(product.getOid()));
		}else if(product.getProductPackage()!=null&&ProductCouponEnum.useRaiseRateCoupon.getCode()==product.getProductPackage().getUseraiseRateCoupons()){
			pr.setRaiseCoupons(productPackageCouponService.getRaiseListByProductPackageOid(product.getProductPackage().getOid()));
		}
		if(labelService.isProductLabelHasAppointLabel(product.getProductLabel(), LabelEnum.newbie.toString()) || product.getGuess() != null){
			pr.setIsShowRedPackets("NO");
		}else{
			pr.setIsShowRedPackets("YES");
		}
		return pr;
	}

	/**
	 * app查询可以申购的定期活期产品推荐列表
	 * @param spec
	 * @param sort
	 * @return {@link PagesRep<ProductListResp>} ,如果返回的errCode属性等于0表示成功，否则表示失败，失败原因在errMessage里面体现 
	 */
	public PagesRep<ProductListResp> labelProducts(Specification<ProductChannel> spec, Sort sort) {
		List<ProductChannel> pcs = this.productChannelDao.findAll(spec, sort);
		PagesRep<ProductListResp> pagesRep = new PagesRep<ProductListResp>();
		if (pcs != null && pcs.size() > 0) {
			List<String> productOids = new ArrayList<String>();
			for (ProductChannel p : pcs) {
				productOids.add(p.getProduct().getOid());
			}
			
			Map<String,Map<String,BigDecimal>> productExpArorMap = this.getProductsMinMaxRewards(productOids);
			List<ProductListResp> rows = new ArrayList<ProductListResp>();
			Page<IncomeAllocate> pcas = null;
			for (ProductChannel p : pcs) {
				if(Product.TYPE_Producttype_02.equals(p.getProduct().getType().getOid())) {
					pcas = getProductIncomeAllocate(p.getProduct().getAssetPool().getOid(),1);
				} else {
					pcas = null;
				}
				ProductListResp queryRep = new ProductListResp(p,productExpArorMap,pcas);
				rows.add(queryRep);
			}
			pagesRep.setRows(rows);
			pagesRep.setTotal(pcs.size());
		}
		return pagesRep;
	}
	
	private Map<String,List<LabelResp>> findProductLabels(final List<String> productOids) {
		//查询标签
		Specification<ProductLabel> spec = new Specification<ProductLabel>() {
			@Override
			public Predicate toPredicate(Root<ProductLabel> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				In<String> inProductOids = cb.in(root.get("product").get("oid").as(String.class));
				for(String productOid : productOids) {
					inProductOids.value(productOid);
				}
				return inProductOids;
			}
		};
		spec = Specifications.where(spec);
		List<ProductLabel> ls = productLabelDao.findAll(spec);
		Map<String,List<LabelResp>> labelMap = new HashMap<String,List<LabelResp>>();
		if(ls!=null && ls.size()>0) {
			for(ProductLabel l : ls) {
				if(labelMap.get(l.getProduct().getOid())==null) {
					labelMap.put(l.getProduct().getOid(),new ArrayList<LabelResp>());
				}
				labelMap.get(l.getProduct().getOid()).add(new LabelResp(l.getLabel()));
			}
		}
		return labelMap;
	}
	
	public PagesRep<ProductPojo> getTnProducts(int start, int end, BigDecimal expArorStart, BigDecimal expArorEnd,
			Integer durationPeriodDaysStart, Integer durationPeriodDaysEnd, String channelOid, String userOid, String sort, String order) {
		PagesRep<ProductPojo> pagesRep = new PagesRep<ProductPojo>();
		if (null == expArorStart) {
			expArorStart = BigDecimal.ZERO;
		}
		if (null == expArorEnd) {
			expArorEnd = new BigDecimal(10000);
		}
		if (null == durationPeriodDaysStart) {
			durationPeriodDaysStart = 0;
		}
		if (null == durationPeriodDaysEnd) {
			durationPeriodDaysEnd = 10000;
		}
		BigDecimal totalAmount = (userOid == null) ? BigDecimal.ZERO : publisherHoldService.getTotalHoldAmount(userOid);
		StringBuilder sb = new StringBuilder();
		StringBuilder count = new StringBuilder();
		StringBuilder where = new StringBuilder();
		StringBuilder sborder = new StringBuilder();
		count.append("SELECT count(*) FROM T_GAM_PRODUCT t1, T_GAM_PRODUCT_CHANNEL t2, T_MONEY_PLATFORM_CHANNEL t3 ");
//<<<<<<< HEAD  fixme dzq merge done
//		count.append(" WHERE t1.oid = t2.productOid AND t2.channelOid = t3.oid  AND (t1.type = 'PRODUCTTYPE_01' or t1.type = 'PRODUCTTYPE_03') AND t2.marketState = 'ONSHELF' AND t1.state in ('RAISING', 'RAISEEND', 'DURATIONING', 'DURATIONEND') AND t1.guessOid is null");
//=======
		count.append(" WHERE t1.oid = t2.productOid AND t2.channelOid = t3.oid  AND t2.marketState = 'ONSHELF' AND t1.guessOid is null ");
//>>>>>>> master
		// 1.7.0版本0元购产品单独列表 此处根据版本加以区分 20171029 by 王鹏
		if (!VersionUtils.checkVersionV170()) {
			count.append(" AND t1.isActivityProduct = 0");
		}
		
		sb.append("SELECT t1.oid, t1.name, t1.investMin, t1.expAror, t1.expArorSec, t1.rewardInterest,");
		sb.append(" t1.durationPeriodDays, t1.raisedTotalNumber, t1.collectedVolume, t1.lockCollectedVolume, ");
		sb.append(" CASE  WHEN t1.state = 'RAISING' THEN 1 WHEN t1.state = 'RAISEEND' THEN 2 ");
		sb.append(" WHEN t1.state = 'DURATIONING' THEN 3 WHEN t1.state = 'DURATIONEND' THEN 4 WHEN t1.state = 'CLEARED' THEN 5 ELSE 6 END AS stateOrder, ");
		sb.append(" t1.state, t1.type, ");
		sb.append(" CASE WHEN t1.raisedTotalNumber - t1.collectedVolume - t1.lockCollectedVolume = 0 THEN 1 ELSE 0 END AS investableVolume, DATE_FORMAT(t1.setupDate,'%Y-%m-%d') setupDate ");
		sb.append(" FROM T_GAM_PRODUCT t1, T_GAM_PRODUCT_CHANNEL t2, T_MONEY_PLATFORM_CHANNEL t3 ");
//<<<<<<< HEAD fixme dzq merge done
//		sb.append(" WHERE t1.oid = t2.productOid AND t2.channelOid = t3.oid AND (t1.type = 'PRODUCTTYPE_01' or t1.type = 'PRODUCTTYPE_03') AND t2.marketState = 'ONSHELF' AND t1.state in ('RAISING', 'RAISEEND', 'DURATIONING', 'DURATIONEND') ");
//
//=======
		sb.append(" WHERE t1.oid = t2.productOid AND t2.channelOid = t3.oid AND t2.marketState = 'ONSHELF' ");
		where.append(" AND t3.oid = ?5 ");
		where.append(" AND t1.guessOid is null ");
		if(totalAmount.compareTo(limitAmount) >= 0) {
			where.append(" AND (t1.type = 'PRODUCTTYPE_01' or t1.type = 'PRODUCTTYPE_03') AND t1.state in ('RAISING', 'RAISEEND', 'DURATIONING', 'DURATIONEND','CLEARED') ");
		} else {
			where.append(" AND (((t1.type = 'PRODUCTTYPE_01' or t1.type = 'PRODUCTTYPE_03') and t1.ifP2P=0 AND t1.state in ('RAISEEND','DURATIONING','DURATIONEND','CLEARED')) or ");
			where.append(" (t1.type = 'PRODUCTTYPE_01' and t1.ifP2P=1 and t1.state in ('RAISING','RAISEEND','DURATIONING','DURATIONEND','CLEARED'))) ");
		}
//>>>>>>> master
		where.append(" AND (t1.expAror + t1.rewardInterest) >= ?1");
		where.append(" AND (t1.expArorSec + t1.rewardInterest) <= ?2");
		where.append(" AND t1.durationPeriodDays >= ?3");
		where.append(" AND t1.durationPeriodDays <= ?4");
		where.append(" AND isP2PAssetPackage != 2 "); // 排除企业散标
		// 1.7.0版本0元购产品单独列表 此处根据版本加以区分 20171029 by 王鹏
		if (!VersionUtils.checkVersionV170()) {
			where.append(" AND t1.isActivityProduct = 0");
		}
		if (!StringUtil.isEmpty(sort) && !StringUtil.isEmpty(order)) {
			sborder.append(" order by t1.").append(sort).append(" ").append(order).append(",");
			sborder.append("  t1.type DESC, stateOrder, investableVolume, t2.rackTime desc ");
		} else {
			sborder.append(" order by t1.type DESC, stateOrder, investableVolume, t2.rackTime desc ");
		}
		count.append(where.toString());
		
		Integer total = ((BigInteger)em.createNativeQuery(count.toString()).setParameter(1, expArorStart)
				.setParameter(2, expArorEnd).setParameter(3, durationPeriodDaysStart)
				.setParameter(4, durationPeriodDaysEnd).setParameter(5, channelOid)
				.getSingleResult()).intValue();
		
		sb.append(where.toString()).append(sborder.toString());
		@SuppressWarnings("unchecked")
		List<Object[]> list = em.createNativeQuery(sb.toString()).setParameter(1, expArorStart)
				.setParameter(2, expArorEnd).setParameter(3, durationPeriodDaysStart)
				.setParameter(4, durationPeriodDaysEnd).setParameter(5, channelOid)
				.setFirstResult(start).setMaxResults(end).getResultList();
		for (Object[] arr : list) {
			ProductPojo pojo = new ProductPojo();
			pojo.setProductOid((String)arr[0]);
			pojo.setName((String)arr[1]);
			pojo.setInvestMin((BigDecimal)arr[2]);
			pojo.setExpAror((BigDecimal)arr[3]);
			pojo.setExpArorSec((BigDecimal)arr[4]);
			pojo.setRewardInterest((BigDecimal)arr[5]);
			pojo.setDurationPeriodDays(((Integer)arr[6]));
			pojo.setRaisedTotalNumber((BigDecimal)arr[7]);
			pojo.setCollectedVolume((BigDecimal)arr[8]);
			pojo.setLockCollectedVolume((BigDecimal)arr[9]);
			pojo.setStateOrder((arr[10]).toString());
			pojo.setState((String)arr[11]);
			pojo.setType((String)arr[12]);
			pojo.setSetupDate((String)arr[14]);
			setExpArrorDisp(pojo);
			List<LabelRep> labelList = this.productLabelService.getLabelRepByProduct(pojo.getProductOid());
			pojo.setLabelList(labelList);
			pojo.setSubType(ProductPojo.DEPOSIT_SUBTYPE);
			if (Product.TYPE_Producttype_03.equals(arr[12])){	//快定宝
				pojo.setSubType(ProductPojo.BF_PLUS_SUBTYPE);
			}
			pagesRep.add(pojo);
		}
		pagesRep.setTotal(total);
		return pagesRep;
	}
	
	//竞猜宝修改
	public RowsRep<ProductPojo> getPCHomeProducts(String channelOid, String userOid) {
		RowsRep<ProductPojo> rowsRep = new RowsRep<ProductPojo>();
		BigDecimal totalAmount = (userOid == null) ? BigDecimal.ZERO : publisherHoldService.getTotalHoldAmount(userOid);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT t1.oid, t1.name, t1.investMin, t1.expAror, t1.expArorSec, t1.rewardInterest,");
		sb.append(" t1.durationPeriodDays, t1.raisedTotalNumber, t1.collectedVolume, t1.lockCollectedVolume, ");
		sb.append(" CASE  WHEN t1.state = 'RAISING' THEN 1 WHEN t1.state = 'RAISEEND' THEN 2 ");
		sb.append(" WHEN t1.state = 'DURATIONING' THEN 3 WHEN t1.state = 'DURATIONEND' THEN 4 WHEN t1.state = 'CLEARED' THEN 5 ELSE 6 END AS stateOrder, ");
		sb.append(" t1.state, t1.type, ");
		sb.append(" CASE WHEN t1.raisedTotalNumber - t1.collectedVolume - t1.lockCollectedVolume = 0 THEN 1 ELSE 0 END AS investableVolume, ");
		sb.append(" t1.purchaseNum,DATE_FORMAT(t1.setupDate,'%Y-%m-%d') setupDate ");
		sb.append(" FROM T_GAM_PRODUCT t1, T_GAM_PRODUCT_CHANNEL t2, T_MONEY_PLATFORM_CHANNEL t3 ");
		sb.append(" WHERE t1.oid = t2.productOid AND t2.channelOid = t3.oid AND t2.marketState = 'ONSHELF' ");
		if(totalAmount.compareTo(limitAmount) >= 0) {
			sb.append(" AND t1.type = 'PRODUCTTYPE_01' AND  t1.state in ('RAISING') ");
		} else {
			sb.append(" AND ((t1.type = 'PRODUCTTYPE_01'  and t1.ifP2P=0 AND t1.state in ('RAISEEND','DURATIONING','DURATIONEND','CLEARED')) or ");
			sb.append(" (t1.type = 'PRODUCTTYPE_01' and t1.ifP2P=1 and t1.state in ('RAISING','RAISEEND','DURATIONING','DURATIONEND','CLEARED'))) ");
		}
		sb.append(" AND t3.oid = ?1 ");
		sb.append(" AND t1.guessOid is null");
		sb.append(" AND t1.productLabel != 'freshman'"); // 排除新手标
		sb.append(" AND t1.isP2PAssetPackage != 2 "); // 排除企业散标
		sb.append(" AND t1.maxSaleVolume > 0");//可售份额大于0
		// 1.7.0版本0元购产品单独列表，此处移除0元购
		sb.append(" AND t1.isActivityProduct = 0");
		sb.append(" order by stateOrder, investableVolume, t2.rackTime desc ");
		//获取快定宝
		getBfPlusProduct(rowsRep, channelOid);
		// 获取企业散标
		this.getScatterProduct(rowsRep, channelOid);
//		List<Product> t0ProductList = this.productDao.getT0ProductList(channelOid);
//		for(Product t0Product : t0ProductList) {
//			if(rewardCacheService.hasRewardIncome(t0Product.getOid())) {
//				addProductPojo(rowsRep, t0Product, ProductPojo.INCREMENT_SUBTYPE);
//			} else {
//				addProductPojo(rowsRep, t0Product, ProductPojo.DEMAND_SUBTYPE);
//			}
//		}
		Product newBie = this.productDao.getNewBieProduct(channelOid);
		if (null != newBie) {
			if(Product.IS_P2P.equals(newBie.getIfP2P()) || totalAmount.compareTo(limitAmount) >= 0) {
				addProductPojo(rowsRep, newBie, ProductPojo.NEW_BIE_SUBTYPE);
			}
		}

		int tnCount = 1;
		if (newBie == null) {
			tnCount = 0;
		}
		@SuppressWarnings("unchecked")
		List<Object[]> list = em.createNativeQuery(sb.toString()).setParameter(1, channelOid).setFirstResult(0)
				.setMaxResults(PC_PAGE_COUNT - tnCount).getResultList();
		
		for (Object[] arr : list) {
			ProductPojo pojo = new ProductPojo();
			pojo.setProductOid((String) arr[0]);
			if (null != newBie && pojo.getProductOid().equals(newBie.getOid())) {
				continue;
			}
			pojo.setName((String) arr[1]);
			pojo.setInvestMin((BigDecimal) arr[2]);
			pojo.setExpAror((BigDecimal) arr[3]);
			pojo.setExpArorSec((BigDecimal) arr[4]);
			pojo.setRewardInterest((BigDecimal) arr[5]);
			pojo.setDurationPeriodDays(((Integer) arr[6]));
			pojo.setRaisedTotalNumber((BigDecimal) arr[7]);
			pojo.setCollectedVolume((BigDecimal) arr[8]);
			pojo.setLockCollectedVolume((BigDecimal) arr[9]);
			pojo.setStateOrder((arr[10]).toString());
			pojo.setState((String) arr[11]);
			pojo.setType((String) arr[12]);
			pojo.setPurchaseNum(((Integer) arr[14]));
			pojo.setSetupDate((String) arr[15]);
			setExpArrorDisp(pojo);
			List<LabelRep> labelList = this.productLabelService.getLabelRepByProduct(pojo.getProductOid());
			pojo.setLabelList(labelList);
			pojo.setSubType(ProductPojo.DEPOSIT_SUBTYPE);
			rowsRep.add(pojo);
		}

		return rowsRep;
	}

	/**
	 * 获取快定宝商品
	 * @param rowsRep	返回结果
	 * @param channelOid	渠道号
	 */
	private void getBfPlusProduct(RowsRep<ProductPojo> rowsRep, String channelOid){
		for(Product t0Product : this.productDao.getBfPlusProductList(channelOid)) {
			addProductPojo(rowsRep, t0Product, ProductPojo.BF_PLUS_SUBTYPE);
		}
	}
	/**
	 * 获取产品利率最大的一个散标产品
	 * @author yihonglei
	 * @date 2018/4/21 14:10
	 * @param 
	 * @return 
	 * @throws 
	 * @since 1.0.0
	 */
	private void getScatterProduct(RowsRep<ProductPojo> rowsRep, String channelOid) {
		Product scatterProduct = this.productDao.getScatterProduct(channelOid);
		addProductPojo(rowsRep, scatterProduct, ProductPojo.SCATTER_SUBTYPE);
	}

	public RowsRep<ProductPojo> getAppHomeProducts(String channelOid, String userOid) {
		RowsRep<ProductPojo> rowsRep = new RowsRep<ProductPojo>();
		//获取新手标
		int count=1;
		BigDecimal totalAmount = (userOid == null) ? BigDecimal.ZERO : publisherHoldService.getTotalHoldAmount(userOid);
		if(totalAmount.compareTo(limitAmount) >= 0) {
			//获取快定宝
			getBfPlusProduct(rowsRep, channelOid);
		}
		Product newBie = this.productDao.getNewBieProduct4App(channelOid);
		if (null != newBie) {
			if(Product.IS_P2P.equals(newBie.getIfP2P()) || totalAmount.compareTo(limitAmount) >= 0) {
				addProductPojo(rowsRep, newBie, ProductPojo.NEW_BIE_SUBTYPE);
				count--;
			}
		}
		// 获取企业散标
		this.getScatterProduct(rowsRep, channelOid);

			StringBuilder sb = new StringBuilder();
			sb.append("SELECT t1.oid, t1.name, t1.investMin, t1.expAror, t1.expArorSec, t1.rewardInterest,");
			sb.append(" t1.durationPeriodDays, t1.raisedTotalNumber, t1.collectedVolume, t1.lockCollectedVolume, ");
			sb.append(" CASE  WHEN t1.state = 'RAISING' THEN 1 WHEN t1.state = 'RAISEEND' THEN 2 ");
			sb.append(
					" WHEN t1.state = 'DURATIONING' THEN 3 WHEN t1.state = 'DURATIONEND' THEN 4 WHEN t1.state = 'CLEARED' THEN 5 ELSE 6 END AS stateOrder, ");
			sb.append(" t1.state, t1.type, ");
			sb.append(
					" CASE WHEN t1.raisedTotalNumber - t1.collectedVolume - t1.lockCollectedVolume = 0 THEN 1 ELSE 0 END AS investableVolume, ");
			sb.append(" t1.purchaseNum, DATE_FORMAT(t1.setupDate,'%Y-%m-%d') setupDate ");
			sb.append(" FROM T_GAM_PRODUCT t1, T_GAM_PRODUCT_CHANNEL t2, T_MONEY_PLATFORM_CHANNEL t3 ");
			sb.append(
					" WHERE t1.oid = t2.productOid AND t2.channelOid = t3.oid ");
			sb.append(" AND t3.oid = ?1 AND t2.marketState = 'ONSHELF' ");
			sb.append(" AND t1.guessOid is null");
			sb.append(" AND t1.productLabel != 'freshman'"); // 排除新手标
			sb.append(" AND t1.isP2PAssetPackage != 2 "); // 排除企业散标
			// 1.7.0版本0元购产品单独列表 此处根据版本加以区分 20171029 by 王鹏
			if (!VersionUtils.checkVersionV170()) {
				sb.append(" AND t1.isActivityProduct = 0 ");
			}
			if(totalAmount.compareTo(limitAmount) >= 0) {
				sb.append(" AND t1.type ='PRODUCTTYPE_01' AND  t1.state in ('RAISING') ");
				sb.append(" AND t1.raisedTotalNumber - t1.collectedVolume - t1.lockCollectedVolume >=IFNULL(t1.investMin,0) ");
			} else {
				sb.append(" AND ((t1.type ='PRODUCTTYPE_01' and t1.ifP2P=0 AND t1.state in ('RAISEEND','DURATIONING','DURATIONEND','CLEARED')) or ");
				sb.append(" (t1.type = 'PRODUCTTYPE_01' and t1.ifP2P=1 and t1.state in ('RAISING','RAISEEND','DURATIONING','DURATIONEND','CLEARED'))) ");
			}
			sb.append(" order by stateOrder, (t1.raisedTotalNumber - t1.collectedVolume - t1.lockCollectedVolume) desc, t1.expAror desc,t1.expArorSec desc, t2.rackTime asc ");
			sb.append(" limit ").append(count == 1 ? 3 : 2).append(" ");
			@SuppressWarnings("unchecked")
			List<Object[]> list = em.createNativeQuery(sb.toString()).setParameter(1, channelOid).getResultList();
			for (Object[] arr : list) {
				ProductPojo pojo = new ProductPojo();
				pojo.setProductOid((String) arr[0]);

				if (null != newBie && pojo.getProductOid().equals(newBie.getOid())) {
					continue;
				}
				pojo.setName((String) arr[1]);
				pojo.setInvestMin((BigDecimal) arr[2]);
				pojo.setExpAror((BigDecimal) arr[3]);
				pojo.setExpArorSec((BigDecimal) arr[4]);
				pojo.setRewardInterest((BigDecimal) arr[5]);
				pojo.setDurationPeriodDays(((Integer) arr[6]));
				pojo.setRaisedTotalNumber((BigDecimal) arr[7]);
				pojo.setCollectedVolume((BigDecimal) arr[8]);
				pojo.setLockCollectedVolume((BigDecimal) arr[9]);
				pojo.setStateOrder((arr[10]).toString());
				pojo.setState((String) arr[11]);
				pojo.setType((String) arr[12]);
				pojo.setPurchaseNum(((Integer) arr[14]));
				pojo.setSetupDate((String) arr[15]);
				pojo.setSubType(ProductPojo.DEPOSIT_SUBTYPE);
				setExpArrorDisp(pojo);
				List<LabelRep> labelList = this.productLabelService.getLabelRepByProduct(pojo.getProductOid());
				pojo.setLabelList(labelList);
				rowsRep.add(pojo);
			}
		return rowsRep;
	}
	
	private void setExpArrorDisp(ProductPojo pojo) {
		setExpArrorDisp(pojo, null);
	}

	private void setExpArrorDisp(ProductPojo pojo, Product product) {
		if (null != pojo.getExpAror() && null != pojo.getExpArorSec()) {
			pojo.setShowType(ProductPojo.ProductPojo_showType_double);
		} else {
			pojo.setShowType(ProductPojo.ProductPojo_showType_single);
		}
		if (pojo.getExpAror().compareTo(pojo.getExpArorSec()) == 0) {
//			System.out.println(DecimalUtil.zoomOut(pojo.getExpAror(), 100));
//			System.out.println(DecimalUtil.setScaleDown(DecimalUtil.zoomOut(pojo.getExpAror(), 100)));
			pojo.setExpArrorDisp(DecimalUtil.zoomOut(pojo.getExpAror(), 100, 2) + "%");
		} else {
			pojo.setExpArrorDisp(DecimalUtil.zoomOut(pojo.getExpAror(), 100, 2) 
					+ "%~" + DecimalUtil.zoomOut(pojo.getExpArorSec(), 100, 2) + "%");
		}
		if (null != product && product.getType().getOid().equals(Product.TYPE_Producttype_02)) {
			pojo.setTenThousandIncome(InterestFormula.compound(new BigDecimal(10000), product.getExpAror(), product.getIncomeCalcBasis()));
		}
		
	}
	
	private void addProductPojo(RowsRep<ProductPojo> rowsRep, Product product, String subType) {
		if (null == product) {
			return;
		}
		ProductPojo pojo = new ProductPojo();
		pojo.setProductOid(product.getOid());
		pojo.setName(product.getName());
		pojo.setInvestMin(product.getInvestMin());
		pojo.setExpAror(product.getExpAror());
		pojo.setExpArorSec(product.getExpArorSec());
		pojo.setRewardInterest(product.getRewardInterest());
		pojo.setDurationPeriodDays(product.getDurationPeriodDays());
		pojo.setRaisedTotalNumber(product.getRaisedTotalNumber());
		pojo.setMaxSaleVolume(product.getMaxSaleVolume());
		pojo.setCollectedVolume(product.getCollectedVolume());
		pojo.setLockCollectedVolume(product.getLockCollectedVolume());
		pojo.setStateOrder("0");
		pojo.setState(product.getState());
		pojo.setType(product.getType().getOid());
		pojo.setPurchaseNum(product.getPurchaseNum());
		pojo.setSetupDate(DateUtil.format(product.getSetupDate()));
		setExpArrorDisp(pojo, product);
		List<LabelRep> labelList = this.productLabelService.getLabelRepByProduct(pojo.getProductOid());
		pojo.setLabelList(labelList);
		pojo.setSubType(subType);
		rowsRep.add(pojo);
	}
	
	/**
	 * 
	 * @author yihonglei
	 * @Title: queryProductInvestRecord
	 * @Description: 根据产品oid查询定期产品的投资记录
	 * @param oid
	 * @return ResponseEntity<PageVo<Map<String,Object>>>
	 * @date 2017年5月31日 下午2:06:53
	 * @since  1.0.0
	 */
	public PageVo<Map<String, Object>> queryProductInvestRecord(ProductInvestRecordReq req) {
		List<Object[]> records = new ArrayList<Object[]>();// 记录列表
		int total = 0;// 总条数
		// 根据产品id,查询产品投资记录
		records = this.productDao.queryProductInvestRecordAll(req.getProductOid(), (req.getPage() - 1) * req.getRow(), req.getRow());
		// 查询总条数
		total = this.productDao.queryProductInvestRecordCount(req.getProductOid());
		// 处理产品投资信息
		PageVo<Map<String,Object>> pageVo = dealProInvestRecord(req, total, records);
		
		return pageVo;
	}
	/** 产品投资信息列表数据处理  */
	private PageVo<Map<String, Object>> dealProInvestRecord(ProductInvestRecordReq req, int total, List<Object[]> records) {
		PageVo<Map<String,Object>> pageVo = new PageVo<Map<String,Object>>();
		List<Map<String,Object>> proInvestRecordList = new ArrayList<Map<String,Object>>();
		
		for (int i=0;i<records.size();i++) {
			Object[] objData = records.get(i);
			Map<String,Object> mapData = new HashMap<String,Object>();
			mapData.put("orderTime", objData[0].toString()); // 购买时间(订单时间)
			mapData.put("realName", objData[1].toString()); // 用户名
			mapData.put("phoneNum", objData[2].toString()); // 手机号
			mapData.put("orderAmount", new BigDecimal(objData[3].toString())); // 订单金额
			
			proInvestRecordList.add(mapData);
		}
		
		pageVo.setRows(proInvestRecordList);
		pageVo.setTotal(total);
		pageVo.setRow(req.getRow());
		pageVo.setPage(req.getPage());
		pageVo.reTotalPage();
		
		return pageVo;
	}
	
	/**
	 * PC端定期产品列表
	 * @param start
	 * @param end
	 * @param expArorStart
	 * @param expArorEnd
	 * @param durationPeriodDaysStart
	 * @param durationPeriodDaysEnd
	 * @param channelOid
	 * @param sort
	 * @param order
	 * @return
	 */
	public PagesRep<ProductPojo> getTnProductsPC(int start, int end, BigDecimal expArorStart, BigDecimal expArorEnd, 
			Integer durationPeriodDaysStart, Integer durationPeriodDaysEnd, String channelOid, String userOid, String sort, String order) {
		PagesRep<ProductPojo> pagesRep = new PagesRep<ProductPojo>();
		if (null == expArorStart) {
			expArorStart = BigDecimal.ZERO;
		}
		if (null == expArorEnd) {
			expArorEnd = new BigDecimal(10000);
		}
		if (null == durationPeriodDaysStart) {
			durationPeriodDaysStart = 0;
		}
		if (null == durationPeriodDaysEnd) {
			durationPeriodDaysEnd = 10000;
		}
		BigDecimal totalAmount = (userOid == null) ? BigDecimal.ZERO : publisherHoldService.getTotalHoldAmount(userOid);
		StringBuilder sb = new StringBuilder();
		StringBuilder count = new StringBuilder();
		StringBuilder where = new StringBuilder();
		StringBuilder sborder = new StringBuilder();
		count.append("SELECT count(*) FROM T_GAM_PRODUCT t1, T_GAM_PRODUCT_CHANNEL t2, T_MONEY_PLATFORM_CHANNEL t3 ");
//<<<<<<< HEAD	fixme dzq merge done
//		count.append(" WHERE t1.oid = t2.productOid AND t2.channelOid = t3.oid  AND (t1.type = 'PRODUCTTYPE_01' or t1.type = 'PRODUCTTYPE_03') AND t2.marketState = 'ONSHELF' AND t1.state in ('RAISING', 'RAISEEND', 'DURATIONING', 'DURATIONEND') AND t1.guessOid is null");
//=======
		count.append(" WHERE t1.oid = t2.productOid AND t2.channelOid = t3.oid AND t2.marketState = 'ONSHELF' AND t1.guessOid is null");
//>>>>>>> master
		// 1.7.0版本0元购产品单独列表 此处加以区分 20171029 by 王鹏
		count.append(" AND t1.isActivityProduct = 0");
		
		sb.append("SELECT t1.oid, t1.name, t1.investMin, t1.expAror, t1.expArorSec, t1.rewardInterest,");
		sb.append(" t1.durationPeriodDays, t1.raisedTotalNumber, t1.collectedVolume, t1.lockCollectedVolume, ");
		sb.append(" CASE  WHEN t1.state = 'RAISING' THEN 1 WHEN t1.state = 'RAISEEND' THEN 2 ");
		sb.append(" WHEN t1.state = 'DURATIONING' THEN 3 WHEN t1.state = 'DURATIONEND' THEN 4 WHEN t1.state = 'CLEARED' THEN 5 ELSE 6 END AS stateOrder, ");
		sb.append(" t1.state, t1.type, ");
		sb.append(" CASE WHEN t1.raisedTotalNumber - t1.collectedVolume - t1.lockCollectedVolume = 0 THEN 1 ELSE 0 END AS investableVolume, DATE_FORMAT(t1.setupDate,'%Y-%m-%d') setupDate ");
		sb.append(" FROM T_GAM_PRODUCT t1, T_GAM_PRODUCT_CHANNEL t2, T_MONEY_PLATFORM_CHANNEL t3 ");
//<<<<<<< HEAD
//		sb.append(" WHERE t1.oid = t2.productOid AND t2.channelOid = t3.oid AND (t1.type = 'PRODUCTTYPE_01' or t1.type = 'PRODUCTTYPE_03') AND t2.marketState = 'ONSHELF' AND t1.state in ('RAISING', 'RAISEEND', 'DURATIONING', 'DURATIONEND') ");
//
////=======fixme dzq merge done
		sb.append(" WHERE t1.oid = t2.productOid AND t2.channelOid = t3.oid AND t2.marketState = 'ONSHELF' ");
		where.append(" AND t3.oid = ?5 ");
		where.append(" AND t1.guessOid is null ");
		if(totalAmount.compareTo(limitAmount) >= 0) {
			where.append(" AND (t1.type = 'PRODUCTTYPE_01' or t1.type = 'PRODUCTTYPE_03') AND t1.state in ('RAISING', 'RAISEEND', 'DURATIONING', 'DURATIONEND','CLEARED') ");
		} else {
			where.append(" AND (((t1.type = 'PRODUCTTYPE_01' or t1.type = 'PRODUCTTYPE_03') and t1.ifP2P=0 AND t1.state in ('RAISEEND','DURATIONING','DURATIONEND','CLEARED')) or ");
			where.append(" (t1.type = 'PRODUCTTYPE_01' and t1.ifP2P=1 and t1.state in ('RAISING','RAISEEND','DURATIONING','DURATIONEND','CLEARED'))) ");
		}
//>>>>>>> master
		where.append(" AND (t1.expAror + t1.rewardInterest) >= ?1");
		where.append(" AND (t1.expArorSec + t1.rewardInterest) <= ?2");
		where.append(" AND t1.durationPeriodDays >= ?3");
		where.append(" AND t1.durationPeriodDays <= ?4");
		// 1.7.0版本0元购产品单独列表 此处加以区分 20171029 by 王鹏
		where.append(" AND t1.isActivityProduct = 0");
		//目前和前端确认过，sort参数没有传递，order 传递或者只会传递asc，所以这两个参数以及这里的校验实际上没有用了。
		if (!StringUtil.isEmpty(sort) && !StringUtil.isEmpty(order)) {
			sborder.append(" order by t1.").append(sort).append(" ").append(order).append(",");
			sborder.append("  t1.type DESC, stateOrder, investableVolume, t2.rackTime desc ");
		} else {
			sborder.append(" order by t1.type DESC, stateOrder, investableVolume, t2.rackTime desc ");
		}
		count.append(where.toString());
		
		Integer total = ((BigInteger)em.createNativeQuery(count.toString()).setParameter(1, expArorStart)
				.setParameter(2, expArorEnd).setParameter(3, durationPeriodDaysStart)
				.setParameter(4, durationPeriodDaysEnd).setParameter(5, channelOid)
				.getSingleResult()).intValue();
		
		sb.append(where.toString()).append(sborder.toString());
		@SuppressWarnings("unchecked")
		List<Object[]> list = em.createNativeQuery(sb.toString()).setParameter(1, expArorStart)
				.setParameter(2, expArorEnd).setParameter(3, durationPeriodDaysStart)
				.setParameter(4, durationPeriodDaysEnd).setParameter(5, channelOid)
				.setFirstResult(start).setMaxResults(end).getResultList();
		for (Object[] arr : list) {
			ProductPojo pojo = new ProductPojo();
			pojo.setProductOid((String)arr[0]);
			pojo.setName((String)arr[1]);
			pojo.setInvestMin((BigDecimal)arr[2]);
			pojo.setExpAror((BigDecimal)arr[3]);
			pojo.setExpArorSec((BigDecimal)arr[4]);
			pojo.setRewardInterest((BigDecimal)arr[5]);
			pojo.setDurationPeriodDays(((Integer)arr[6]));
			pojo.setRaisedTotalNumber((BigDecimal)arr[7]);
			pojo.setCollectedVolume((BigDecimal)arr[8]);
			pojo.setLockCollectedVolume((BigDecimal)arr[9]);
			pojo.setStateOrder((arr[10]).toString());
			pojo.setState((String)arr[11]);
			pojo.setType((String)arr[12]);
			pojo.setSetupDate((String)arr[14]);
			setExpArrorDisp(pojo);
			List<LabelRep> labelList = this.productLabelService.getLabelRepByProduct(pojo.getProductOid());
			pojo.setLabelList(labelList);
			pojo.setSubType(ProductPojo.DEPOSIT_SUBTYPE);
			if (Product.TYPE_Producttype_03.equals(arr[12])){	//快定宝
				pojo.setSubType(ProductPojo.BF_PLUS_SUBTYPE);
			}
			pagesRep.add(pojo);
		}
		pagesRep.setTotal(total);
		return pagesRep;
	}
	
	/**
	 * 获取0元购产品列表
	 * @param start
	 * @param end
	 * @param expArorStart
	 * @param expArorEnd
	 * @param durationPeriodDaysStart
	 * @param durationPeriodDaysEnd
	 * @param channelOid
	 * @param sort
	 * @param order
	 * @return
	 */
	public PagesRep<ProductPojo> getZeroBuyProducts(int start, int end, BigDecimal expArorStart, BigDecimal expArorEnd, 
			Integer durationPeriodDaysStart, Integer durationPeriodDaysEnd, String channelOid, String sort, String order) {
		PagesRep<ProductPojo> pagesRep = new PagesRep<ProductPojo>();
		if (null == expArorStart) {
			expArorStart = BigDecimal.ZERO;
		}
		if (null == expArorEnd) {
			expArorEnd = new BigDecimal(10000);
		}
		if (null == durationPeriodDaysStart) {
			durationPeriodDaysStart = 0;
		}
		if (null == durationPeriodDaysEnd) {
			durationPeriodDaysEnd = 10000;
		}
		StringBuilder sb = new StringBuilder();
		StringBuilder count = new StringBuilder();
		StringBuilder where = new StringBuilder();
		StringBuilder sborder = new StringBuilder();
		count.append("SELECT count(*) FROM T_GAM_PRODUCT t1, T_GAM_PRODUCT_CHANNEL t2, T_MONEY_PLATFORM_CHANNEL t3 ");
		count.append(" WHERE t1.oid = t2.productOid AND t2.channelOid = t3.oid  AND t1.type = 'PRODUCTTYPE_01' AND t2.marketState = 'ONSHELF' AND t1.state in ('RAISING', 'RAISEEND', 'DURATIONING', 'DURATIONEND') AND t1.guessOid is null");
		// TODO
		count.append(" AND t1.isActivityProduct = 1");
		
		sb.append("SELECT t1.oid, t1.name, t1.investMin, t1.expAror, t1.expArorSec, t1.rewardInterest,");
		sb.append(" t1.durationPeriodDays, t1.raisedTotalNumber, t1.collectedVolume, t1.lockCollectedVolume, ");
		sb.append(" CASE  WHEN t1.state = 'RAISING' THEN 1 WHEN t1.state = 'RAISEEND' THEN 2 ");
		sb.append(" WHEN t1.state = 'DURATIONING' THEN 3 WHEN t1.state = 'DURATIONEND' THEN 4 WHEN t1.state = 'CLEARED' THEN 5 ELSE 6 END AS stateOrder, ");
		sb.append(" t1.state, t1.type, ");
		sb.append(" CASE WHEN t1.raisedTotalNumber - t1.collectedVolume - t1.lockCollectedVolume = 0 THEN 1 ELSE 0 END AS investableVolume, DATE_FORMAT(t1.setupDate,'%Y-%m-%d') setupDate ");
		// TODO
		sb.append(" ,t1.expectedArrorDisp, t4.title");
		sb.append(" FROM T_GAM_PRODUCT t1, T_GAM_PRODUCT_CHANNEL t2, T_MONEY_PLATFORM_CHANNEL t3, T_GAM_PRODUCT_DETAIL t4 ");
		sb.append(" WHERE t1.oid = t2.productOid AND t1.activityDetail = t4.oid AND t2.channelOid = t3.oid AND t1.type = 'PRODUCTTYPE_01' AND t2.marketState = 'ONSHELF' AND t1.state in ('RAISING', 'RAISEEND', 'DURATIONING', 'DURATIONEND') ");
		
		where.append(" AND (t1.expAror + t1.rewardInterest) >= ?1");
		where.append(" AND (t1.expArorSec + t1.rewardInterest) <= ?2");
		where.append(" AND t1.durationPeriodDays >= ?3");
		where.append(" AND t1.durationPeriodDays <= ?4");
		where.append(" AND t3.oid = ?5 ");
		where.append(" AND t1.guessOid is null");
		// TODO
		where.append(" AND t1.isActivityProduct = 1");
		if (!StringUtil.isEmpty(sort) && !StringUtil.isEmpty(order)) {
			sborder.append(" order by t1.").append(sort).append(" ").append(order).append(",");
			sborder.append("  stateOrder, investableVolume, t2.rackTime desc ");
		} else {
			sborder.append(" order by stateOrder, investableVolume, t2.rackTime desc ");
		}
		count.append(where.toString());
		
		Integer total = ((BigInteger)em.createNativeQuery(count.toString()).setParameter(1, expArorStart)
				.setParameter(2, expArorEnd).setParameter(3, durationPeriodDaysStart)
				.setParameter(4, durationPeriodDaysEnd).setParameter(5, channelOid)
				.getSingleResult()).intValue();
		
		sb.append(where.toString()).append(sborder.toString());
		@SuppressWarnings("unchecked")
		List<Object[]> list = em.createNativeQuery(sb.toString()).setParameter(1, expArorStart)
				.setParameter(2, expArorEnd).setParameter(3, durationPeriodDaysStart)
				.setParameter(4, durationPeriodDaysEnd).setParameter(5, channelOid)
				.setFirstResult(start).setMaxResults(end).getResultList();
		for (Object[] arr : list) {
			ProductPojo pojo = new ProductPojo();
			pojo.setProductOid((String)arr[0]);
			pojo.setName((String)arr[1]);
			pojo.setInvestMin((BigDecimal)arr[2]);
			pojo.setExpAror((BigDecimal)arr[3]);
			pojo.setExpArorSec((BigDecimal)arr[4]);
			pojo.setRewardInterest((BigDecimal)arr[5]);
			pojo.setDurationPeriodDays(((Integer)arr[6]));
			pojo.setRaisedTotalNumber((BigDecimal)arr[7]);
			pojo.setCollectedVolume((BigDecimal)arr[8]);
			pojo.setLockCollectedVolume((BigDecimal)arr[9]);
			pojo.setStateOrder((arr[10]).toString());
			pojo.setState((String)arr[11]);
			pojo.setType((String)arr[12]);
			pojo.setSetupDate((String)arr[14]);
			pojo.setExpectedArrorDisp(ProductDecimalFormat.format(ProductDecimalFormat.multiply((BigDecimal)arr[15]))+"%");
			pojo.setActivityDetailTitle((String)arr[16]);
			setExpArrorDisp(pojo);
			List<LabelRep> labelList = this.productLabelService.getLabelRepByProduct(pojo.getProductOid());
			pojo.setLabelList(labelList);
			pojo.setSubType(ProductPojo.ZERO_BUY_SUBTYPE);
			pagesRep.add(pojo);
		}
		pagesRep.setTotal(total);
		return pagesRep;
	}
	
	/**
	 * 首页获取0元购产品列表
	 * @param start
	 * @param end
	 * @param expArorStart
	 * @param expArorEnd
	 * @param durationPeriodDaysStart
	 * @param durationPeriodDaysEnd
	 * @param channelOid
	 * @param sort
	 * @param order
	 * @param minNum 返回产品最低数量 不足则返回空
	 * @return
	 */
	public PagesRep<ProductPojo> getHomeZeroBuyProducts(int start, int end, BigDecimal expArorStart, BigDecimal expArorEnd, 
			Integer durationPeriodDaysStart, Integer durationPeriodDaysEnd, String channelOid, String sort, String order,
			Integer minNum) {
		PagesRep<ProductPojo> pagesRep = new PagesRep<ProductPojo>();
		if (null == expArorStart) {
			expArorStart = BigDecimal.ZERO;
		}
		if (null == expArorEnd) {
			expArorEnd = new BigDecimal(10000);
		}
		if (null == durationPeriodDaysStart) {
			durationPeriodDaysStart = 0;
		}
		if (null == durationPeriodDaysEnd) {
			durationPeriodDaysEnd = 10000;
		}
		StringBuilder sb = new StringBuilder();
		StringBuilder count = new StringBuilder();
		StringBuilder where = new StringBuilder();
		StringBuilder sborder = new StringBuilder();
		count.append("SELECT count(*) FROM T_GAM_PRODUCT t1, T_GAM_PRODUCT_CHANNEL t2, T_MONEY_PLATFORM_CHANNEL t3 , T_GAM_PRODUCT_DETAIL t4 ");
		count.append(" WHERE t1.oid = t2.productOid AND t2.channelOid = t3.oid  AND t4.oid = t1.activityDetail AND t1.type = 'PRODUCTTYPE_01' AND t2.marketState = 'ONSHELF' AND t1.state = 'RAISING' AND t1.guessOid is null AND t1.maxSaleVolume > 0");
		count.append(" AND t1.isActivityProduct = 1");
		
		sb.append("SELECT t1.oid, t1.name, t1.investMin, t1.expAror, t1.expArorSec, t1.rewardInterest,");
		sb.append(" t1.durationPeriodDays, t1.raisedTotalNumber, t1.collectedVolume, t1.lockCollectedVolume, ");
		sb.append(" CASE  WHEN t1.state = 'RAISING' THEN 1 WHEN t1.state = 'RAISEEND' THEN 2 ");
		sb.append(" WHEN t1.state = 'DURATIONING' THEN 3 WHEN t1.state = 'DURATIONEND' THEN 4 WHEN t1.state = 'CLEARED' THEN 5 ELSE 6 END AS stateOrder, ");
		sb.append(" t1.state, t1.type, ");
		sb.append(" CASE WHEN t1.raisedTotalNumber - t1.collectedVolume - t1.lockCollectedVolume = 0 THEN 1 ELSE 0 END AS investableVolume, DATE_FORMAT(t1.setupDate,'%Y-%m-%d') setupDate ");
		sb.append(" ,t1.expectedArrorDisp, t4.title");
		sb.append(" FROM T_GAM_PRODUCT t1, T_GAM_PRODUCT_CHANNEL t2, T_MONEY_PLATFORM_CHANNEL t3, T_GAM_PRODUCT_DETAIL t4 ");
		sb.append(" WHERE t1.oid = t2.productOid AND t2.channelOid = t3.oid AND t4.oid = t1.activityDetail AND t1.type = 'PRODUCTTYPE_01' AND t2.marketState = 'ONSHELF' AND t1.state = 'RAISING'");
		
		where.append(" AND (t1.expAror + t1.rewardInterest) >= ?1");
		where.append(" AND (t1.expArorSec + t1.rewardInterest) <= ?2");
		where.append(" AND t1.durationPeriodDays >= ?3");
		where.append(" AND t1.durationPeriodDays <= ?4");
		where.append(" AND t3.oid = ?5 ");
		where.append(" AND t1.guessOid is null");
		where.append(" AND t1.isActivityProduct = 1");
		where.append(" AND t1.maxSaleVolume > 0");
		if (!StringUtil.isEmpty(sort) && !StringUtil.isEmpty(order)) {
			sborder.append(" order by t1.").append(sort).append(" ").append(order).append(",");
			sborder.append("  stateOrder, investableVolume, t2.rackTime desc ");
		} else {
			sborder.append(" order by stateOrder, investableVolume, t2.rackTime desc ");
		}
		count.append(where.toString());
		
		Integer total = ((BigInteger)em.createNativeQuery(count.toString()).setParameter(1, expArorStart)
				.setParameter(2, expArorEnd).setParameter(3, durationPeriodDaysStart)
				.setParameter(4, durationPeriodDaysEnd).setParameter(5, channelOid)
				.getSingleResult()).intValue();
		// 如果列表数量少于最低数量 则返回空列表
		if(total < minNum){
			return pagesRep;
		}
		
		sb.append(where.toString()).append(sborder.toString());
		@SuppressWarnings("unchecked")
		List<Object[]> list = em.createNativeQuery(sb.toString()).setParameter(1, expArorStart)
				.setParameter(2, expArorEnd).setParameter(3, durationPeriodDaysStart)
				.setParameter(4, durationPeriodDaysEnd).setParameter(5, channelOid)
				.setFirstResult(start).setMaxResults(end).getResultList();
		for (Object[] arr : list) {
			ProductPojo pojo = new ProductPojo();
			pojo.setProductOid((String)arr[0]);
			pojo.setName((String)arr[1]);
			pojo.setInvestMin((BigDecimal)arr[2]);
			pojo.setExpAror((BigDecimal)arr[3]);
			pojo.setExpArorSec((BigDecimal)arr[4]);
			pojo.setRewardInterest((BigDecimal)arr[5]);
			pojo.setDurationPeriodDays(((Integer)arr[6]));
			pojo.setRaisedTotalNumber((BigDecimal)arr[7]);
			pojo.setCollectedVolume((BigDecimal)arr[8]);
			pojo.setLockCollectedVolume((BigDecimal)arr[9]);
			pojo.setStateOrder((arr[10]).toString());
			pojo.setState((String)arr[11]);
			pojo.setType((String)arr[12]);
			pojo.setSetupDate((String)arr[14]);
			pojo.setExpectedArrorDisp(ProductDecimalFormat.format(ProductDecimalFormat.multiply((BigDecimal)arr[15]))+"%"); 
			pojo.setActivityDetailTitle((String) arr[16]);
			setExpArrorDisp(pojo);
			List<LabelRep> labelList = this.productLabelService.getLabelRepByProduct(pojo.getProductOid());
			pojo.setLabelList(labelList);
			pojo.setSubType(ProductPojo.ZERO_BUY_SUBTYPE);
			pagesRep.add(pojo);
		}
		pagesRep.setTotal(total);
		return pagesRep;
	}
	
	public RowsRep<ProductPojo> getAppHomeZeroBuyProducts(String channelOid) {
		PagesRep<ProductPojo> pagesRep = getHomeZeroBuyProducts(0, 5, null, null, null, null, channelOid, null, null,3);
		RowsRep<ProductPojo> result = new RowsRep<ProductPojo>();
		result.setRows(pagesRep.getRows());
		result.setErrorCode(pagesRep.getErrorCode());
		result.setErrorMessage(pagesRep.getErrorMessage());
		return result;
	}
	
	public RowsRep<ProductPojo> getPcHomeZeroBuyProducts(String channelOid) {
		PagesRep<ProductPojo> pagesRep = getHomeZeroBuyProducts(0, 5, null, null, null, null, channelOid, null, null,4);
		RowsRep<ProductPojo> result = new RowsRep<ProductPojo>();
		result.setRows(pagesRep.getRows());
		result.setErrorCode(pagesRep.getErrorCode());
		result.setErrorMessage(pagesRep.getErrorMessage());
		return result;
	}

	public RowsRep<ProductPojo> getAppScatterList(int start, int end, String channelOid) {
		PagesRep<ProductPojo> pagesRep = getHomeScatterProducts(start, end, null, null, null, null, channelOid, null, null,3);
		RowsRep<ProductPojo> result = new RowsRep<ProductPojo>();
		result.setRows(pagesRep.getRows());
		result.setErrorCode(pagesRep.getErrorCode());
		result.setErrorMessage(pagesRep.getErrorMessage());
		return result;
	}

	public RowsRep<ProductPojo> getPcScatterList(int start, int end, String channelOid) {
		PagesRep<ProductPojo> pagesRep = getHomeScatterProducts(start, end, null, null, null, null, channelOid, null, null,4);
		RowsRep<ProductPojo> result = new RowsRep<ProductPojo>();
		result.setRows(pagesRep.getRows());
		result.setErrorCode(pagesRep.getErrorCode());
		result.setErrorMessage(pagesRep.getErrorMessage());
		return result;
	}

	public PagesRep<ProductPojo> getHomeScatterProducts(
			int start, int end, BigDecimal expArorStart, BigDecimal expArorEnd, Integer durationPeriodDaysStart,
			Integer durationPeriodDaysEnd, String channelOid, String sort, String order, Integer minNum) {

		PagesRep<ProductPojo> pagesRep = new PagesRep<ProductPojo>();
		if (null == expArorStart) {
			expArorStart = BigDecimal.ZERO;
		}
		if (null == expArorEnd) {
			expArorEnd = new BigDecimal(10000);
		}
		if (null == durationPeriodDaysStart) {
			durationPeriodDaysStart = 0;
		}
		if (null == durationPeriodDaysEnd) {
			durationPeriodDaysEnd = 10000;
		}

		StringBuilder sb = new StringBuilder();
		StringBuilder count = new StringBuilder();
		StringBuilder where = new StringBuilder();
		StringBuilder sborder = new StringBuilder();
		count.append(" SELECT count(*) FROM T_GAM_PRODUCT t1, T_GAM_PRODUCT_CHANNEL t2, T_MONEY_PLATFORM_CHANNEL t3 ");
		count.append(" WHERE t1.oid = t2.productOid AND t2.channelOid = t3.oid  AND t2.marketState = 'ONSHELF' AND t1.guessOid is null AND isP2PAssetPackage = 2 ");

		sb.append(" SELECT t1.oid, t1.name, t1.investMin, t1.expAror, t1.expArorSec, t1.rewardInterest, ");
		sb.append(" t1.durationPeriodDays, t1.raisedTotalNumber, t1.collectedVolume, t1.lockCollectedVolume, ");
		sb.append(" CASE  WHEN t1.state = 'RAISING' THEN 1 WHEN t1.state = 'RAISEEND' THEN 2 ");
		sb.append(" WHEN t1.state = 'DURATIONING' THEN 3 WHEN t1.state = 'DURATIONEND' THEN 4 WHEN t1.state = 'CLEARED' THEN 5 ELSE 6 END AS stateOrder, ");
		sb.append(" t1.state, t1.type, ");
		sb.append(" CASE WHEN t1.raisedTotalNumber - t1.collectedVolume - t1.lockCollectedVolume = 0 THEN 1 ELSE 0 END AS investableVolume, DATE_FORMAT(t1.setupDate,'%Y-%m-%d') setupDate ");
		sb.append(" FROM T_GAM_PRODUCT t1, T_GAM_PRODUCT_CHANNEL t2, T_MONEY_PLATFORM_CHANNEL t3 ");
		sb.append(" WHERE t1.oid = t2.productOid AND t2.channelOid = t3.oid AND t2.marketState = 'ONSHELF' AND t1.guessOid is null AND isP2PAssetPackage = 2 ");
		where.append(" AND t3.oid = ?5 ");
		where.append(" AND t1.type = 'PRODUCTTYPE_01' AND t1.state in ('RAISING', 'RAISEEND', 'DURATIONING', 'DURATIONEND','CLEARED') ");
		where.append(" AND (t1.expAror + t1.rewardInterest) >= ?1");
		where.append(" AND (t1.expArorSec + t1.rewardInterest) <= ?2");
		where.append(" AND t1.durationPeriodDays >= ?3");
		where.append(" AND t1.durationPeriodDays <= ?4");

		if (!StringUtil.isEmpty(sort) && !StringUtil.isEmpty(order)) {
			sborder.append(" order by t1.").append(sort).append(" ").append(order).append(",");
			sborder.append("  t1.type DESC, stateOrder, investableVolume, t2.rackTime desc ");
		} else {
			sborder.append(" order by t1.type DESC, stateOrder, investableVolume, t2.rackTime desc ");
		}
		count.append(where.toString());

		Integer total = ((BigInteger)em.createNativeQuery(count.toString()).setParameter(1, expArorStart)
				.setParameter(2, expArorEnd).setParameter(3, durationPeriodDaysStart)
				.setParameter(4, durationPeriodDaysEnd).setParameter(5, channelOid)
				.getSingleResult()).intValue();

		sb.append(where.toString()).append(sborder.toString());
		@SuppressWarnings("unchecked")
		List<Object[]> list = em.createNativeQuery(sb.toString()).setParameter(1, expArorStart)
				.setParameter(2, expArorEnd).setParameter(3, durationPeriodDaysStart)
				.setParameter(4, durationPeriodDaysEnd).setParameter(5, channelOid)
				.setFirstResult(start).setMaxResults(end).getResultList();
		for (Object[] arr : list) {
			ProductPojo pojo = new ProductPojo();
			pojo.setProductOid((String)arr[0]);
			pojo.setName((String)arr[1]);
			pojo.setInvestMin((BigDecimal)arr[2]);
			pojo.setExpAror((BigDecimal)arr[3]);
			pojo.setExpArorSec((BigDecimal)arr[4]);
			pojo.setRewardInterest((BigDecimal)arr[5]);
			pojo.setDurationPeriodDays(((Integer)arr[6])/30);
			pojo.setRaisedTotalNumber((BigDecimal)arr[7]);
			pojo.setCollectedVolume((BigDecimal)arr[8]);
			pojo.setLockCollectedVolume((BigDecimal)arr[9]);
			pojo.setStateOrder((arr[10]).toString());
			pojo.setState((String)arr[11]);
			pojo.setType((String)arr[12]);
			pojo.setSetupDate((String)arr[14]);
			setExpArrorDisp(pojo);
			List<LabelRep> labelList = this.productLabelService.getLabelRepByProduct(pojo.getProductOid());
			pojo.setLabelList(labelList);
			pojo.setSubType(ProductPojo.SCATTER_SUBTYPE);
			pagesRep.add(pojo);
		}
		pagesRep.setTotal(total);
		return pagesRep;
	}

	/**
	 * 企业散标产品详情
	 */
	public ProductPeriodicDetailResp scatterDetail(String oid, String uid) {
		Product product = productService.getProductByOid(oid);
		LoanContract loanContract = loanContractDao.getLoanContractByCode(product.getCode());
		List<ProductIncomeReward> prewards = rewardService.productRewardList(oid);
		ProductPeriodicDetailResp pr;

		if(!StringUtils.isEmpty(product.getActivityDetail())) {
			ProductTypeDetail productTypeDetail = productTypeDetailService.getOne(product.getActivityDetail());
			pr = new ProductPeriodicDetailResp(product, prewards, productTypeDetail.getUrl());
		}else {
			pr = new ProductPeriodicDetailResp(product, prewards, null);
		}

		if (null != loanContract) {
			pr.setLoanContract(this.loanContractDesc(loanContract));
		}
		/** 投资日，起息日，到期日中文描述: 以下三个条件需要显示中文描述 */
		if (Product.STATE_Raising.equals(product.getState())
				|| Product.STATE_Raiseend.equals(product.getState())
				|| Product.STATE_RaiseFail.equals(product.getState())) {
			pr.setInvestTimeDesc("现在");
			pr.setSetupDateDesc("标的的融满之日");
			pr.setDurationPeriodEndDateDesc("起息日+借款期限");
			pr.setChinese(true);
		}
		pr.setRepayWay("到期一次性还本付息");
		pr.setEndArragementDesc("回款至你的快活宝账户");
		pr.setSubType(ProductPojo.SCATTER_SUBTYPE);

		if (prewards != null && prewards.size() > 0) {//算上奖励收益
			BigDecimal minReward = prewards.get(0).getRatio();
			BigDecimal maxReward = prewards.get(0).getRatio();
			for (ProductIncomeReward preward : prewards) {
				if (preward.getRatio().compareTo(minReward) < 0) {
					minReward = preward.getRatio();
				}
				if (preward.getRatio().compareTo(maxReward) > 0) {
					maxReward = preward.getRatio();
				}
			}
		}

		if (Product.STATE_Raising.equals(product.getState())) {
			// 投资时间
			pr.setInvestTime(DateUtil.getSqlCurrentDate());
		} else {
			if (uid != null) {
				HoldCacheEntity hold = cacheHoldService.getHoldCacheEntityByInvestorOidAndProductOid(uid, oid);
				if (null == hold) {
					throw new AMPException("未投资用户不可查看详情");
				}
				pr.setInvestTime(hold.getLatestOrderTime());
			}

		}

		Map<String, AdminObj> adminObjMap = new HashMap<String, AdminObj>();

		FileResp fr = null;
		AdminObj adminObj = null;
		if (!StringUtil.isEmpty(product.getFileKeys())) {
			List<File> files = this.fileService.list(product.getFileKeys(), File.STATE_Valid);
			if (files.size() > 0) {
				pr.setFiles(new ArrayList<FileResp>());

				for (File file : files) {
					fr = new FileResp(file);
					if (adminObjMap.get(file.getOperator()) == null) {
						try {
							adminObj = adminSdk.getAdmin(file.getOperator());
							adminObjMap.put(file.getOperator(), adminObj);
						} catch (Exception e) {
						}
					}
					if (adminObjMap.get(file.getOperator()) != null) {
						fr.setOperator(adminObjMap.get(file.getOperator()).getName());
					}
					pr.getFiles().add(fr);
				}
			}
		}

		if (!StringUtil.isEmpty(product.getInvestFileKey())) {
			List<File> files = this.fileService.list(product.getInvestFileKey(), File.STATE_Valid);
			if (files.size() > 0) {
				pr.setInvestFiles(new ArrayList<FileResp>());

				for (File file : files) {
					fr = new FileResp(file);
					if (adminObjMap.get(file.getOperator()) == null) {
						try {
							adminObj = adminSdk.getAdmin(file.getOperator());
							adminObjMap.put(file.getOperator(), adminObj);
						} catch (Exception e) {
						}
					}
					if (adminObjMap.get(file.getOperator()) != null) {
						fr.setOperator(adminObjMap.get(file.getOperator()).getName());
					}
					pr.getInvestFiles().add(fr);
				}
			}
		}

		if (!StringUtil.isEmpty(product.getServiceFileKey())) {
			List<File> files = this.fileService.list(product.getServiceFileKey(), File.STATE_Valid);
			if (files.size() > 0) {
				pr.setServiceFiles(new ArrayList<FileResp>());

				for (File file : files) {
					fr = new FileResp(file);
					if (adminObjMap.get(file.getOperator()) == null) {
						try {
							adminObj = adminSdk.getAdmin(file.getOperator());
							adminObjMap.put(file.getOperator(), adminObj);
						} catch (Exception e) {
						}
					}
					if (adminObjMap.get(file.getOperator()) != null) {
						fr.setOperator(adminObjMap.get(file.getOperator()).getName());
					}
					pr.getServiceFiles().add(fr);
				}
			}
		}
		List<ProductLabel> productLabels = productLabelService.findProductLabelsByProduct(product);
		if (productLabels != null && productLabels.size() > 0) {
			List<LabelResp> labelResps = new ArrayList<LabelResp>();
			for (ProductLabel pl : productLabels) {
				labelResps.add(new LabelResp(pl.getLabel()));
			}
			pr.setProductLabels(labelResps);
		}
		/**
		 * 关联卡券
		 */
		if(product.getProductPackage()!=null){
			pr.setIsShowRedPacketsNew(product.getProductPackage().getUseRedPackages());
			pr.setIsShowRaiseCoupons(product.getProductPackage().getUseraiseRateCoupons());
		}else{
			pr.setIsShowRedPacketsNew(product.getUseRedPackages());
			pr.setIsShowRaiseCoupons(product.getUseraiseRateCoupons());
		}
		if(product.getProductPackage()==null&&ProductCouponEnum.useRedCoupon.getCode()==product.getUseRedPackages()){
			pr.setRedPackages(productCouponService.getRedListByProductOid(product.getOid()));
		}else if(product.getProductPackage()!=null&&ProductCouponEnum.useRedCoupon.getCode()==product.getProductPackage().getUseRedPackages()){
			pr.setRedPackages(productPackageCouponService.getRedListByProductPackageOid(product.getProductPackage().getOid()));
		}
		if(product.getProductPackage()==null&&ProductCouponEnum.useRaiseRateCoupon.getCode()==product.getUseraiseRateCoupons()){
			pr.setRaiseCoupons(productCouponService.getRaiseListByProductOid(product.getOid()));
		}else if(product.getProductPackage()!=null&&ProductCouponEnum.useRaiseRateCoupon.getCode()==product.getProductPackage().getUseraiseRateCoupons()){
			pr.setRaiseCoupons(productPackageCouponService.getRaiseListByProductPackageOid(product.getProductPackage().getOid()));
		}
		if(labelService.isProductLabelHasAppointLabel(product.getProductLabel(), LabelEnum.newbie.toString()) || product.getGuess() != null){
			pr.setIsShowRedPackets("NO");
		}else{
			pr.setIsShowRedPackets("YES");
		}

		return pr;
	}

	private LoanContractResp loanContractDesc(LoanContract loanContract) {
		LoanContractResp loanContractResp = new LoanContractResp();
		loanContractResp.setCode(loanContract.getCode());
		loanContractResp.setName(loanContract.getName());
		loanContractResp.setLoanPeriod(loanContract.getLoanPeriod() + "个月");
		loanContractResp.setLoanVolume(loanContract.getLoanVolume() + "万");
		loanContractResp.setLoanRatio(loanContract.getLoanRatio().multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_DOWN) + "%");
		loanContractResp.setLoanUsage(loanContract.getLoanUsage());
		loanContractResp.setRefundMode(loanContract.getRefundMode());
		loanContractResp.setOrgCode(loanContract.getOrgCode());
		loanContractResp.setOrgName(loanContract.getOrgName());
		loanContractResp.setOrgCorporationName(loanContract.getOrgCorporationName().substring(0,1) + "**");
		loanContractResp.setRegisteredCapital(loanContract.getRegisteredCapital());
		loanContractResp.setSetupDate(loanContract.getSetupDate());
		loanContractResp.setOrgAddress(loanContract.getOrgAddress().substring(0,1) + "**********");
		loanContractResp.setCreateTime(loanContract.getCreateTime());
		loanContractResp.setUpdateTime(loanContract.getUpdateTime());
		return loanContractResp;
	}

}
