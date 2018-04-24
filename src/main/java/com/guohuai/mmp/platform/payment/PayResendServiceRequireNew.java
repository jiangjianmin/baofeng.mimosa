package com.guohuai.mmp.platform.payment;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.mmp.platform.payment.log.PayInterface;
import com.guohuai.mmp.platform.payment.log.PayLogEntity;

@Service
@Transactional
public class PayResendServiceRequireNew {

	
	@Autowired
	private PaymentServiceImpl paymentServiceImpl;
	
	@Transactional(value = TxType.REQUIRES_NEW)
	public BaseRep requireNew(PayLogEntity entity) throws Exception {
		BaseRep irep = (BaseRep)PaymentServiceImpl.class
				.getMethod(entity.getInterfaceName(),
						Class.forName(PayInterface.getIReq(entity.getInterfaceName())), boolean.class)
				.invoke(this.paymentServiceImpl,
						JSONObject.parseObject(entity.getContent(), Class.forName(PayInterface.getIReq(entity.getInterfaceName()))), false);
		return irep;
	}
}
