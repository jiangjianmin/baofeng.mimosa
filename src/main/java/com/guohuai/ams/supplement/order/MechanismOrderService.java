package com.guohuai.ams.supplement.order;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.guohuai.ams.product.ProductService;
import com.guohuai.cache.service.CacheProductService;
import com.guohuai.component.web.view.BaseResp;
import com.guohuai.component.web.view.PageResp;
import com.guohuai.operate.api.AdminSdk;
import com.guohuai.operate.api.objs.admin.AdminObj;
@Service
public class MechanismOrderService {
	
	@Autowired
	private MechanismOrderDao mechanismOrderDao;
	
	@Autowired
	private ProductService productService;
	
	@Autowired
	private CacheProductService cacheProductService;
	
	@Autowired
	private AdminSdk adminSdk;

//	/**创建机构订单
//	 * @param mechanismOrders
//	 * @return
//	 */
//	@Transactional
//	public BaseResp create(List<MechanismOrder> mechanismOrders) {
//		BaseResp rep = new BaseResp();
//		//1.锁定产品可售份额，乐观锁解决并发
//		productService.updateProduct4LockCollectedVolumeForSumplement(mechanismOrders);
//		//2.解除锁定份额，减少产品可售份额，增加募集份额（以前是在份额确认后做的）
//		productService.update4InvestConfirmForSumplement(mechanismOrders);
//		//3.保存扫尾单
//		mechanismOrderDao.save(mechanismOrders);
//		return rep;
//	}
	/**创建机构订单
	 * @param mechanismOrders
	 * @return
	 */
	@Transactional
	public BaseResp create(MechanismOrder mechanismOrder) {
		BaseResp rep = new BaseResp();
		//1.校验产品是否在募集期
		cacheProductService.checkProductIsRaisingOrRaisend(mechanismOrder);
		//2.锁定产品可售份额，乐观锁解决并发
		productService.updateProduct4LockCollectedVolumeForSumplement(mechanismOrder);
		//3.解除锁定份额，减少产品可售份额，增加募集份额（以前是在份额确认后做的）
		productService.update4InvestConfirmForSumplement(mechanismOrder);
		//4.保存扫尾单
		mechanismOrderDao.save(mechanismOrder);
		return rep;
	}

	/**查询机构订单
	 * @param spec 
	 * @param page
	 * @param size
	 * @param sortDirection
	 * @param sortField
	 * @return
	 */
	public PageResp<MechanismOrderRep> list(Specification<MechanismOrder> spec, int page, int size, Direction sortDirection, String sortField) {
		
		PageResp<MechanismOrderRep> pagesRep = new PageResp<MechanismOrderRep>();
		Pageable pageable = new PageRequest(page - 1, size, new Sort(new Order(sortDirection, sortField)));
		Page<MechanismOrder> orders = mechanismOrderDao.findAll(spec,pageable);
		if (orders != null && orders.getContent() != null && orders.getTotalElements() > 0) {
			List<MechanismOrderRep> rows = new ArrayList<MechanismOrderRep>();
			Map<String, AdminObj> adminObjMap = new HashMap<String, AdminObj>();
			AdminObj adminObj = null;
			for(MechanismOrder o : orders ){
				MechanismOrderRep rep = new MechanismOrderRep(o);
				if (adminObjMap.get(o.getOperator()) == null) {
					try {
						adminObj = adminSdk.getAdmin(o.getOperator());
						adminObjMap.put(o.getOperator(), adminObj);
					} catch (Exception e) {
					}
				}
				if (adminObjMap.get(o.getOperator()) != null) {
					rep.setOperator(adminObjMap.get(o.getOperator()).getName());
				}
				rows.add(rep);
			}
			pagesRep.setRows(rows);
		}
		pagesRep.setTotal(orders.getTotalElements());
		return pagesRep;
	}

}
