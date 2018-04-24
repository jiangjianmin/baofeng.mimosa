package com.guohuai.ams.dict;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.guohuai.basic.component.ext.web.BaseController;

@RestController
@RequestMapping(value = "/mimosa/dict", produces = "application/json;charset=utf-8")
public class DictController extends BaseController {

	@Autowired
	private DictService dictService;

	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<Map<String, List<DictResp>>> list(@RequestParam String[] specs) {
		Map<String, List<DictResp>> r = this.dictService.structQuery(specs);

		return new ResponseEntity<Map<String, List<DictResp>>>(r, HttpStatus.OK);
	}

	@RequestMapping(value = "/list", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<Map<String, List<DictResp>>> list() {

		return new ResponseEntity<Map<String, List<DictResp>>>(this.dictService.structQuery(), HttpStatus.OK);
	}

	@RequestMapping(value = "/flush", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<Map<String, List<DictResp>>> flush() {
		this.dictService.flushCache();
		return new ResponseEntity<Map<String, List<DictResp>>>(this.dictService.structQuery(), HttpStatus.OK);
	}

}
