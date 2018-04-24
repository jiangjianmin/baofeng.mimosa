package com.guohuai.ams.supplement.mechanism;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import com.guohuai.ams.order.SPVOrderResp;
import com.guohuai.component.web.view.BaseResp;
import com.guohuai.component.web.view.PageResp;
import com.guohuai.operate.api.AdminSdk;
import com.guohuai.operate.api.objs.admin.AdminObj;
@Service
public class MechanismService {
	
	@Autowired
	private MechanismDao mechanismDao;
	@Autowired
	private AdminSdk adminSdk;

	public PageResp<MechanismRep> list(int page, int size, Direction sortDirection, String sortField) {
		PageResp<MechanismRep> pagesRep = new PageResp<MechanismRep>();
		Pageable pageable = new PageRequest(page - 1, size, new Sort(new Order(sortDirection, sortField)));
		Page<Mechanism> mecs = mechanismDao.findAll(pageable);
		if (mecs != null && mecs.getContent() != null && mecs.getTotalElements() > 0) {
			List<MechanismRep> rows = new ArrayList<MechanismRep>();
			Map<String, AdminObj> adminObjMap = new HashMap<String, AdminObj>();
			AdminObj adminObj = null;
			for(Mechanism mec : mecs ){
				MechanismRep rep = new MechanismRep(mec);
				if (adminObjMap.get(mec.getOperator()) == null) {
					try {
						adminObj = adminSdk.getAdmin(mec.getOperator());
						adminObjMap.put(mec.getOperator(), adminObj);
					} catch (Exception e) {
					}
				}
				if (adminObjMap.get(mec.getOperator()) != null) {
					rep.setOperator(adminObjMap.get(mec.getOperator()).getName());
				}
				rows.add(rep);
			}
			pagesRep.setRows(rows);
		}
		pagesRep.setTotal(mecs.getTotalElements());
		return pagesRep;
	}

	public BaseResp save(Mechanism mechanism) {
		BaseResp response = new BaseResp();
		mechanismDao.save(mechanism);
		return response;
	}

	public Mechanism detail(String oid) {
		return mechanismDao.findOne(oid);
	}

	public MechanismListRep findAll() {
		List<Mechanism> mecs = mechanismDao.findAll();
		MechanismListRep reps = new MechanismListRep();
		for(Mechanism mec : mecs){
			MechanismRep rep = new MechanismRep(mec);
			reps.getMecs().add(rep);
		}
		return reps;
	}



}
