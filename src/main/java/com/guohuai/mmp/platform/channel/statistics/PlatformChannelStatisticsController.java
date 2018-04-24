package com.guohuai.mmp.platform.channel.statistics;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.guohuai.basic.component.ext.web.BaseController;

@RestController
@RequestMapping(value = "/mimosa/boot/platform/channel/statistics", produces = "application/json")
public class PlatformChannelStatisticsController extends BaseController {

	@Autowired
	PlatformChannelStatisticsService platformChannelStatisticsService;

	@RequestMapping(value = "stat", method = RequestMethod.POST)
	@ResponseBody
	public int deposit(@RequestParam Date date) {
		this.platformChannelStatisticsService.statChannelYesterdayInvestInfoDo(date);
		return 1;
	}
}
