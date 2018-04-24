package com.guohuai.mmp.job.log;
import java.sql.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.guohuai.component.web.view.PageResp;

@RestController
@RequestMapping(value = "/mimosa/job/log/", produces = "application/json")
public class JobLogController {
		
		@Autowired
		JobLogService jobLogService;
		
		@RequestMapping(value = "/list", method = { RequestMethod.GET, RequestMethod.POST })
		@ResponseBody
		public ResponseEntity<PageResp<JobLogEntityResp>> 
		getMoneyJobLogList(@RequestParam int page, @RequestParam int rows, @RequestParam String jobId
				, @RequestParam Date createTime, @RequestParam String jobStatus){
			PageResp<JobLogEntityResp> rep = jobLogService.getJobLogByJobId(page, rows, jobId,createTime,jobStatus);
			return new ResponseEntity<PageResp<JobLogEntityResp>>(rep, HttpStatus.OK);	 
		}
		
		
		

}
