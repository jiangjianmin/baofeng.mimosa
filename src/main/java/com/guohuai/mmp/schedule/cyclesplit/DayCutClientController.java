package com.guohuai.mmp.schedule.cyclesplit;

import com.guohuai.basic.component.ext.web.BaseController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/mimosa/client/tradeorder", produces = "application/json")
@Slf4j
public class DayCutClientController extends BaseController {
	
	@Autowired
	private DayCutService dayCutService;


	@RequestMapping(value = "doDayCut", method = {RequestMethod.POST,RequestMethod.GET})
	@ResponseBody
	public Object doDayCut() {
			dayCutService.doDayCut(null);
		return "success1";

	}


}
