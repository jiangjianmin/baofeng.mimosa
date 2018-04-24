package com.guohuai.ams.guess;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GuessItemService {
	
	@Autowired
	private GuessItemDao guessItemDao;

	public GuessItemEntity getByOid(String oid) {
		
		return guessItemDao.findOne(oid);
	}
	
}
