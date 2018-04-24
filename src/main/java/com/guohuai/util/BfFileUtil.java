package com.guohuai.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.guohuai.basic.common.ApiOSSClient;
import com.guohuai.basic.common.SFTPUtil;
import com.jcraft.jsch.Session;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class BfFileUtil {
	private static ApiOSSClient apiOSSClient;// oss
	private static SFTPUtil sftp;// sftp
	// oss params
	@Value("${OSS.ACCESS.ID}")
	private String ACCESS_ID;
	@Value("${OSS.ACCESS.KEY}")
	private String ACCESS_KEY;
	@Value("${OSS.HOST}")
	private String HOST;
	@Value("${OSS.BUCKET.NAME}")
	private String BUCKET_NAME;
	@Value("${OSS.ROOT.DIR}")
	private String ROOT_DIR;
	// sftp params
	/** FTP 登录用户名*/  
	@Value("${nj.sftp.username}")
	private String username;
	/** FTP 登录密码*/  
	@Value("${nj.sftp.password}")
	private String password;
	/** 私钥 */  
	@Value("${nj.sftp.privateKey}")
	private String privateKey;
	/** FTP 服务器地址IP地址*/  
	@Value("${nj.sftp.host}")
	private String host;
	/** FTP 端口*/
	@Value("${nj.sftp.port}")
	private int port;
	
	/**
	 * 
	 * @author yihonglei
	 * @Title: getApiOSSClient
	 * @Description:获取oss操作对象
	 * @return ApiOSSClient
	 * @date 2017年5月23日 下午3:55:36
	 * @since  1.0.0
	 */
	public ApiOSSClient getApiOSSClient() {
		String fileDirName = new SimpleDateFormat("yyyyMMdd").format(new Date())+"/";
		apiOSSClient = new ApiOSSClient(this.ACCESS_ID,this.ACCESS_KEY,HOST,this.BUCKET_NAME,this.ROOT_DIR+fileDirName);
		return apiOSSClient;
	}
	
	/**
	 * 
	 * @author yihonglei
	 * @Title: getSFTPUtilPas
	 * @Description: 密码方式登陆
	 * @return SFTPUtil
	 * @date 2017年5月23日 下午3:55:03
	 * @since  1.0.0
	 */
	public SFTPUtil getSFTPUtilPas() {
		sftp = new SFTPUtil(this.username,this.password,this.host,this.port);
		return sftp;
	}
	/**
	 * 
	 * @author yihonglei
	 * @Title: getSFTPUtilKey
	 * @Description:密钥方式登陆
	 * @return SFTPUtil
	 * @date 2017年5月23日 下午3:55:19
	 * @since  1.0.0
	 */
	public SFTPUtil getSFTPUtilKey() {
		sftp = new SFTPUtil(this.username,this.host,this.port,this.privateKey);
		return sftp;
	}
}
