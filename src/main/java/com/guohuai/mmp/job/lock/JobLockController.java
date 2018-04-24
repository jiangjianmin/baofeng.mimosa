package com.guohuai.mmp.job.lock;

import java.sql.Date;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.guohuai.component.web.view.BaseRep;
import com.guohuai.component.web.view.PageResp;

@RestController
@RequestMapping(value = "/mimosa/job/", produces = "application/json")
public class JobLockController {
		
		@Autowired
		JobLockService jobLockService;
	
		/**
		 * 查询定时任务列表
		 * @param page
		 * @param rows
		 * @return
		 */
		@RequestMapping(value = "list", method = { RequestMethod.GET, RequestMethod.POST })
		@ResponseBody
		public ResponseEntity<PageResp<JobLockEntityResp>> getMoneyJobLockList(@RequestParam int page, @RequestParam int rows){
			PageResp<JobLockEntityResp> rep = jobLockService.findAll(page,rows);
			return new ResponseEntity<PageResp<JobLockEntityResp>>(rep, HttpStatus.OK);	 
		}
		
		/**
		 * 根据jobId执行定时任务
		 * @param inDate
		 * @param jobId
		 * @param token
		 * @param request
		 * @return
		 */
		@RequestMapping(value = "executetack", method = { RequestMethod.GET, RequestMethod.POST })
		public @ResponseBody ResponseEntity<BaseRep> executeTask(@RequestParam Date inDate,@RequestParam String jobId,@RequestParam String token,HttpServletRequest request) {
			BaseRep rep = new BaseRep();
			try {
				JobLockEntity jobLockEntity = jobLockService.findByJobId(jobId);
				if(JobLockEntity.JOB_jobStatus_toRun.equals(jobLockEntity.getJobStatus())){
					this.jobLockService.snapshot(inDate,jobId);
				}else if(JobLockEntity.JOB_jobStatus_processing.equals(jobLockEntity.getJobStatus())){
					//rep.setErrorCode(HttpStatus.FORBIDDEN.value());
					rep.setErrorMessage("任务执行中。。。");
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
			return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
		}
		
		
		
		/**
		 * 获取表单防止重复提交token
		 * @param httpSession
		 * @return
		 */
		/*@RequestMapping(value = "gettoken", method = { RequestMethod.GET, RequestMethod.POST })
		public ResponseEntity<TokenResp> getToken(HttpSession httpSession) {
			String token = TokenProccessor.getInstance().makeToken();
			httpSession.setAttribute("token", token);
			return new ResponseEntity<TokenResp>(new TokenResp(token), HttpStatus.OK);
		}*/
		
		/**
		 * 判断定时任务是否重复提交
		 * @param request
		 * @param clinetToken
		 * @return
		 */
		@SuppressWarnings("unused")
		private boolean isRepeatSubmit(HttpServletRequest request,String clinetToken) {
	         String serverToken = (String) request.getSession(false).getAttribute("token");
	         if (serverToken == null ) {
	             return false ;
	         }
	         if (clinetToken == null ) {
	             return false ;
	         }
	         if (!serverToken.equals(clinetToken)) {
	             return false ;
	         }
	         return true ;
	     }
		
		
		
	
		
		

}
