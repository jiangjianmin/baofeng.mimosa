package com.guohuai.ams.dict;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.StringUtil;

@Service
public class DictService {

	@Autowired
	private DictDao dictDao;

	private Map<String, List<DictResp>> structCache = new HashMap<String, List<DictResp>>();

	private Map<String, Dict> dataCache = new HashMap<String, Dict>();

	@PostConstruct
	public void flushCache() {
		this.structCache.clear();
		this.dataCache.clear();

		List<Dict> dicts = this.dictDao.findAll(new Sort(new Order(Direction.ASC, "cate"), new Order(Direction.ASC, "rank")));
		Map<String, DictResp> maps = new LinkedHashMap<String, DictResp>();

		for (Dict dict : dicts) {
			maps.put(dict.getOid(), new DictResp(dict));
			this.dataCache.put(dict.getOid(), dict);
		}

		for (Dict dict : dicts) {
			if (StringUtil.isEmpty(dict.getPoid())) {
				if (!this.structCache.containsKey(dict.getCate())) {
					this.structCache.put(dict.getCate(), new ArrayList<DictResp>());
				}
				this.structCache.get(dict.getCate()).add(maps.get(dict.getOid()));
			} else {
				maps.get(dict.getPoid()).getChildren().add(maps.get(dict.getOid()));
			}
		}

	}

	public Dict get(String oid) {
		if (this.dataCache.containsKey(oid)) {
			return this.dataCache.get(oid);
		}
		throw new AMPException("No dict config about oid " + oid);
	}

	public List<Dict> get(String... oids) {
		List<Dict> list = new ArrayList<Dict>();
		if (null != oids && oids.length > 0) {
			for (String oid : oids) {
				if (this.dataCache.containsKey(oid)) {
					list.add(this.dataCache.get(oid));
				}
			}
		}
		return list;
	}

	public Map<String, List<DictResp>> structQuery(String... specs) {
		Map<String, List<DictResp>> result = new HashMap<String, List<DictResp>>();
		if (null != specs && specs.length > 0) {
			for (String c : specs) {
				if (this.structCache.containsKey(c)) {
					result.put(c, this.structCache.get(c));
				}
			}
		}
		return result;
	}

	public Map<String, List<DictResp>> structQuery() {
		return this.structCache;
	}

}
