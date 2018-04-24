package com.guohuai.ams.acct.doc.type;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.component.exception.AMPException;

@Service
public class DocTypeService {

	@Autowired
	private DocTypeDao docTypeDao;

	@Transactional
	public DocType get(String oid) {
		DocType t = this.docTypeDao.findOne(oid);
		if (null == t) {
			throw new AMPException(String.format("No data found for oid '%s'.", oid));
		}
		return t;
	}

	@Transactional
	public List<DocType> search() {
		List<DocType> list = this.docTypeDao.search();
		return list;
	}

}
