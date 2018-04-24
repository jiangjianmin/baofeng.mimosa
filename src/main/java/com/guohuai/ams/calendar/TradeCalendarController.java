package com.guohuai.ams.calendar;

import java.sql.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.basic.component.ext.web.BaseResp;
import com.guohuai.basic.component.ext.web.Response;

import net.kaczmarzyk.spring.data.jpa.domain.DateAfterInclusive;
import net.kaczmarzyk.spring.data.jpa.domain.DateBeforeInclusive;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;

@RestController
@RequestMapping(value = "/mimosa/calendar", produces = "application/json")
public class TradeCalendarController extends BaseController {
	@Autowired
	private TradeCalendarTService tradeCalendarService;

	@RequestMapping(value = "/reload", method = { RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody ResponseEntity<BaseResp> reload() {
		this.tradeCalendarService.initCache();
		return new ResponseEntity<BaseResp>(new BaseResp(), HttpStatus.OK);
	}

	@RequestMapping(value = "/query", method = { RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody ResponseEntity<TradeCalendarListResp> query() {
		List<TradeCalendar> tds = this.tradeCalendarService.findAll();
		TradeCalendarListResp resp = new TradeCalendarListResp();
		resp.setTotal(tds.size());
		resp.setRows(tds);
		return new ResponseEntity<TradeCalendarListResp>(resp, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/queryPages", method = { RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody ResponseEntity<TradeCalendarListResp> queryPages(HttpServletRequest request,
     		@And({
     			@Spec(params = "exchangeCD", path = "exchangeCD", spec = Like.class),
     			@Spec(params = "isOpen", path = "isOpen", spec = Equal.class),
     			@Spec(params = "isWork", path = "isWork", spec = Equal.class),
     			@Spec(params = "isWeekEnd", path = "isWeekEnd", spec = Equal.class),
     			@Spec(params = "isMonthEnd", path = "isMonthEnd", spec = Equal.class),
     			@Spec(params = "isQuarterEnd", path = "isQuarterEnd", spec = Equal.class),
     			@Spec(params = "isYearEnd", path = "isYearEnd", spec = Equal.class),
     			@Spec(params = "calendarDateBegin", path = "calendarDate", spec = DateAfterInclusive.class, config = "yyyy-MM-dd"),
				@Spec(params = "calendarDateEnd", path = "calendarDate", spec = DateBeforeInclusive.class, config = "yyyy-MM-dd") 
			}) 
     		Specification<TradeCalendar> spec,
			@RequestParam int page, 
			@RequestParam int rows) {
		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(Direction.ASC, "calendarDate")));	
		
		TradeCalendarListResp resp = this.tradeCalendarService.queryPages(spec, pageable);
		
		return new ResponseEntity<TradeCalendarListResp>(resp, HttpStatus.OK);
	}

	@RequestMapping(value = "/istd", method = { RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody ResponseEntity<Response> istd(@RequestParam long ts) {
		boolean istd = this.tradeCalendarService.isTrade(new Date(ts));
		Response response = new Response().with("ts", ts).with("td", istd);
		return new ResponseEntity<Response>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/nexttd", method = { RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody ResponseEntity<Response> nexttd(@RequestParam long ts, @RequestParam(defaultValue = "0") int skip) {
		long nexttd = this.tradeCalendarService.nextTrade(new Date(ts), skip).getTime();
		Response response = new Response().with("ts", new Date(ts)).with("nexttd", new Date(nexttd));
		return new ResponseEntity<Response>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/lasttd", method = { RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody ResponseEntity<Response> lasttd(@RequestParam long ts, @RequestParam(defaultValue = "0") int skip) {
		long lasttd = this.tradeCalendarService.lastTrade(new Date(ts), skip).getTime();
		Response response = new Response().with("ts", new Date(ts)).with("lasttd", new Date(lasttd));
		return new ResponseEntity<Response>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/iswd", method = { RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody ResponseEntity<Response> iswd(@RequestParam long ts) {
		boolean iswd = this.tradeCalendarService.isWork(new Date(ts));
		Response response = new Response().with("ts", ts).with("wd", iswd);
		return new ResponseEntity<Response>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/nextwd", method = { RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody ResponseEntity<Response> nextwd(@RequestParam long ts, @RequestParam(defaultValue = "0") int skip) {
		long nextwd = this.tradeCalendarService.nextWork(new Date(ts), skip).getTime();
		Response response = new Response().with("ts", new Date(ts)).with("nextwd", new Date(nextwd));
		return new ResponseEntity<Response>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/lastwd", method = { RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody ResponseEntity<Response> lastwd(@RequestParam long ts, @RequestParam(defaultValue = "0") int skip) {
		long lastwd = this.tradeCalendarService.lastTrade(new Date(ts), skip).getTime();
		Response response = new Response().with("ts", new Date(ts)).with("lastwd", new Date(lastwd));
		return new ResponseEntity<Response>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/workds", method = { RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody ResponseEntity<Response> wds(@RequestParam long begin, @RequestParam long end) {
		List<Date> dates = this.tradeCalendarService.workDates(new Date(begin), new Date(end));
		Response response = new Response().with("begin", begin).with("end", end).with("dates", dates);
		return new ResponseEntity<Response>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/delete", method = { RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody ResponseEntity<Response> delete(@RequestParam String oid) {
		this.getLoginUser();
		this.tradeCalendarService.delete(oid);
		return new ResponseEntity<Response>(new Response(), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/add",  method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseResp> add(HttpServletRequest request, @Valid TradeCalendarAddReq req){
		this.getLoginUser();
		
		this.tradeCalendarService.add(req);
		
		return new ResponseEntity<BaseResp>(new BaseResp(), HttpStatus.OK);
	}
}
