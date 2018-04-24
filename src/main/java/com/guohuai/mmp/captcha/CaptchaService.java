package com.guohuai.mmp.captcha;

import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import com.google.code.kaptcha.Producer;
import com.guohuai.basic.component.exception.GHException;
import com.guohuai.component.util.StrRedisUtil;
import com.guohuai.component.web.view.BaseRep;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class CaptchaService {
	
	/** 图形验证码有效时间 */
	public static final long IMG_VERICODE_TIME = 120;
	
	
	private Producer captchaProducer = null;  
	@Autowired
	private RedisTemplate<String, String> redis;  
	
    @Autowired  
    public void setCaptchaProducer(Producer captchaProducer) {  
        this.captchaProducer = captchaProducer;  
    }  
	
	public ModelAndView getImgVc(HttpServletRequest request,
            HttpServletResponse response,
            String sessionId) throws Exception {
		response.setDateHeader("Expires", 0);
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");
        response.setContentType("image/jpeg");
        String capText = captchaProducer.createText();
        log.info("uid：{}生成的图验证码：{}", sessionId, capText);
        try {
            redis.opsForValue().set(StrRedisUtil.IMG_VERICODE_REDIS_KEY + sessionId, capText, IMG_VERICODE_TIME, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
 
        BufferedImage bi = captchaProducer.createImage(capText);
        ServletOutputStream out = response.getOutputStream();
        ImageIO.write(bi, "jpg", out);
        try {
            out.flush();
        } finally {
            out.close();
        }
        return null;
	}
	
	/**
	 * 校验图形验证码
	 * @param req
	 * @return
	 */
	public BaseRep checkImgVc(CaptchaValidReq req) {
		BaseRep rep = new BaseRep();
		String redisimgvc =  redis.opsForValue().get(StrRedisUtil.IMG_VERICODE_REDIS_KEY + req.getSessionId());
		if (!req.getImgvc().equalsIgnoreCase(redisimgvc)) {
			// error.define[120004]=图形验证码错误，请重新输入
			throw GHException.getException(120004);
		}
		return rep;
	}
	
	/**
	 * 校验图形验证码
	 * @param sessionId
	 * @param imgVc
	 * @return
	 */
	public BaseRep checkImgVc(String sessionId, String imgVc) {
		BaseRep rep = new BaseRep();
		String redisimgvc =  redis.opsForValue().get(StrRedisUtil.IMG_VERICODE_REDIS_KEY + sessionId);
		if (!imgVc.equalsIgnoreCase(redisimgvc)) {
			// error.define[120004]=图形验证码错误，请重新输入
			throw GHException.getException(120004);
		}
		return rep;
	}

}
