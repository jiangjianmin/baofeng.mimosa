package com.guohuai.mmp.publisher.product.statistics;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.guohuai.basic.component.ext.web.BaseController;

@RestController
@RequestMapping(value = "/mimosa/boot/publisherprostat", produces = "application/json")
public class PublisherProductStatisticsController extends BaseController {

	@Autowired
	PublisherProductStatisticsService publisherProductStatisticsService;

	/** 发行人首页统计 */
	@RequestMapping(value = "stat", method = RequestMethod.POST)
	@ResponseBody
	public int stat(@RequestParam Date date) {
		this.publisherProductStatisticsService.statPublishersProductInvestInfoByDateDo(date);
		return 1;
	}
}
