package com.guohuai.mmp.platform;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.guohuai.component.util.FileUtil;
@Component
public class FileDirectory {

//	@Value("${finance.waterflow.csv.url}")
//	private String csvUrl;
//	
//	@Value("${contract.html.contract}")
//	private String htmlContract;
//	
//	@Value("${contract.pdf.contract}")
//	private String pdfContract;
//	
//	@Value("${contract.html.model}")
//	private String htmlModel;
//	
//	@Value("${contract.logs}")
//	private String contractLogsDir;
//	
//	@Value("${contract.path}")
//	private String contractPath;
	/** 协议存放目录 */
	@Value("${agreement.path}")
	String agreementPath;
	
	/** shell存放目录 */
	@Value("${agreement.shell.path}")
	String agreementShellPath;
	
	/** 日志目录 */
	@Value("${agreement.log.path}")
	String agreementLogPath;
	
	/**
	 * 创建文件目录
	 */
	@PostConstruct
	public void createFileDirectory(){
		FileUtil.mkdirs(this.agreementPath);
		FileUtil.mkdirs(this.agreementShellPath);
		FileUtil.mkdirs(this.agreementLogPath);
//		//流水单CSV目录
//		FileUtil.mkdirs(this.csvUrl);
//		//html合同目录
//		FileUtil.mkdirs(this.htmlContract);
//		//pdf合同目录
//		FileUtil.mkdirs(this.pdfContract);
//		//html模板目录
//		FileUtil.mkdirs(this.htmlModel);
//		//生产合同模版日志
//		FileUtil.mkdirs(this.contractLogsDir);
//		
//		FileUtil.mkdirs(this.contractPath);
	}
	
}
