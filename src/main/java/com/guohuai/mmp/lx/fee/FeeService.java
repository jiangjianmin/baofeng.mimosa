package com.guohuai.mmp.lx.fee;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class FeeService {
	Logger logger = LoggerFactory.getLogger(FeeService.class);
	@Autowired
	FeeDao feeDao;

}
