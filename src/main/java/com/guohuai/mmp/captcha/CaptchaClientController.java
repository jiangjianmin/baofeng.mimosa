package com.guohuai.mmp.captcha;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.web.view.BaseRep;

@RestController
@RequestMapping(value = "/mimosa/client/captcha", produces = "application/json;charset=UTF-8")
public class CaptchaClientController extends BaseController {

	@Autowired
	private CaptchaService captchaService;
	
	/**
	 * 获取图形验证码
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "getimgvc")
    public ModelAndView getKaptchaImage(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
		String sessionId = super.session.getId();
        return captchaService.getImgVc(request, response, sessionId);
    }
	
	/**
	 * 校验图形验证码
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "checkimgvc")
	@ResponseBody
    public ResponseEntity<BaseRep> getKaptchaImage(@Valid @RequestBody CaptchaValidReq req) {
		req.setSessionId(super.session.getId());
		BaseRep rep = captchaService.checkImgVc(req);
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
    }
	
}
