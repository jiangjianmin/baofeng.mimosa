package com.guohuai.guess;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.guohuai.ams.guess.GuessService;
import com.guohuai.basic.component.ext.web.BaseController;

import lombok.extern.slf4j.Slf4j;

/**竞猜活动管理（前端接口）
 * @author qiuliang
 *
 */
@RestController
@RequestMapping(value = "/mimosa/client/guess", produces = "application/json;charset=UTF-8")
@Slf4j
public class GuessController  extends BaseController{
	
	@Autowired
	private GuessService guessService;
	
	@RequestMapping(name = "竞猜活动列表", value = "/list")
	public ResponseEntity<GuessListRep> list()  {
		
		GuessListRep rep = this.guessService.list();
		return new ResponseEntity<GuessListRep>(rep, HttpStatus.OK);
	}
	
	@RequestMapping(name = "竞猜活动详情", value = "/detail")
	public ResponseEntity<GuessDetailForeRep> detail(@RequestParam String guessId,@RequestParam String cid,@RequestParam String ckey)  {
		
		GuessDetailForeRep rep = this.guessService.detailFore(guessId,cid,ckey);
		return new ResponseEntity<GuessDetailForeRep>(rep, HttpStatus.OK);
	}
	
	@RequestMapping(name = "我的竞猜", value = "/myGuess")
	public ResponseEntity<MyGuessRep> myGuess()  {
		String uid = super.getLoginUser();
		MyGuessRep rep = this.guessService.myGuess(uid);
		return new ResponseEntity<MyGuessRep>(rep, HttpStatus.OK);
	}
	

}
