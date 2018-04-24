package com.guohuai.mmp.publisher.product.agreement;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.transaction.Transactional;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductService;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.StringUtil;
import com.guohuai.file.FileService;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderService;
import com.guohuai.mmp.job.lock.JobLockEntity;
import com.guohuai.mmp.job.lock.JobLockService;
import com.guohuai.mmp.job.log.JobLogEntity;
import com.guohuai.mmp.job.log.JobLogFactory;
import com.guohuai.mmp.job.log.JobLogService;
import com.guohuai.mmp.publisher.corporate.Corporate;
import com.guohuai.mmp.publisher.corporate.CorporateService;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class ProductAgreementService {
	@Autowired
	private ProductAgreementDao productAgreementDao;
	@Autowired
	private FileService fileService;
	@Autowired
	private InvestorTradeOrderService investorTradeOrderService; 
	@Autowired
	private ProductService productService;
	@Autowired
	private JobLockService jobLockService;
	
	@Autowired
	private CorporateService corporateService;
	@Autowired
	private JobLogService jobLogService;
	
	@Value(value = "${agreement.path}")
	String agreementPath;
	@Value(value = "${agreement.shell.path}")
	String agreementShellPath;
	@Value(value = "${agreement.log.path}")
	String agreementLogPath;
	
	
	
	
	static ExecutorService pools = Executors.newFixedThreadPool(10);
	
	
	public String getContractModel(Product product) {
		FileOutputStream fos = null;
		InputStream is = null;
		int length;
		byte[] buffer = new byte[2048];


		try {
			List<com.guohuai.file.File> files = this.fileService.list(product.getInvestFileKey(), com.guohuai.file.File.STATE_Valid);
			if (null == files || files.isEmpty()) {
				//error.define[30002]=产品投资协议不存在(CODE:30002)
				throw new AMPException(30002);
			}
			if (files.size() != 1) {
				//error.define[30003]=产品投资协议异常(CODE:30003)
				throw new AMPException(30003);
			}
			com.guohuai.file.File agreement = files.get(0);
			if (null == agreement.getFurl() || "".equals(agreement.getFurl())) {
				//error.define[30004]=产品投资协议地址不存在(CODE:30004)
				throw new AMPException(30004);
			}
			URL url = new URL("http://localhost" + agreement.getFurl());
			is = url.openStream();
			StringBuilder model = new StringBuilder();
			
			while (-1 != (length = is.read(buffer, 0, buffer.length))) {
				model.append(new String(buffer, 0, length));
			}
			return model.toString();
		} catch (Exception e) {
			log.error("", e);
		} finally {
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(fos);
		}
		return null;
	}
	
	
	public String getServiceModel(Product product) {
		FileOutputStream fos = null;
		InputStream is = null;
		int length;
		byte[] buffer = new byte[2048];
		StringBuilder model = new StringBuilder();
		try {
			List<com.guohuai.file.File> files = this.fileService.list(product.getServiceFileKey(), com.guohuai.file.File.STATE_Valid);
			if (null == files || files.isEmpty()) {
				// error.define[30027]=产品服务协议不存在(CODE:30027)
				throw new AMPException(30027);
			}
			if (files.size() != 1) {
				// error.define[30028]=产品服务协议异常(CODE:30028)
				throw new AMPException(30028);
			}
			com.guohuai.file.File agreement = files.get(0);
			if (null == agreement.getFurl() || "".equals(agreement.getFurl())) {
				// error.define[30029]=产品服务协议地址不存在(CODE:30029)
				throw new AMPException(30029);
			}
			URL url = new URL("http://localhost" + agreement.getFurl());
			is = url.openStream();
			
			
			while (-1 != (length = is.read(buffer, 0, buffer.length))) {
				model.append(new String(buffer, 0, length));
			}
			
		} catch (Exception e) {
			log.error("", e);
		} finally {
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(fos);
		}
		return model.toString();
	}
	
	public void makeContract() {
		
		if (this.jobLockService.getRunPrivilege(JobLockEntity.JOB_jobId_createHtml)) {
			this.makeContractLog();
		}
	}

	public void makeContractLog()   {
		JobLogEntity jobLog = JobLogFactory.getInstance(JobLockEntity.JOB_jobId_createHtml);
		try {
			makeContractDo();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			jobLog.setJobMessage(AMPException.getStacktrace(e));
			jobLog.setJobStatus(JobLogEntity.JOB_jobStatus_failed);
		}
		jobLog.setBatchEndTime(DateUtil.getSqlCurrentDate());
		this.jobLogService.saveEntity(jobLog);
		this.jobLockService.resetJob(JobLockEntity.JOB_jobId_createHtml);
	}
	
	public void makeContractDo() {
		List<Product> productList = productService.findByProduct4Contract();
		if (productList.isEmpty()) {
			return;
		}
		for (Product product : productList) {
			try {
				processHTML4Product(product);
			} catch (Exception e) {
				log.error("productOid:{},协议生成异常", product.getOid(), e);
			}
		}

	}


	private void processHTML4Product(Product product) {
		log.info("productOid={}, productCode={}", product.getOid(), product.getCode());
		System.out.println(product.getCode() + "==================");
		BufferedWriter bw = null;
		try {
			String serviceModel = this.getServiceModel(product);
			if (StringUtils.isEmpty(serviceModel)) {
				throw new AMPException("服务协议不存在");
			}
			String agreementModel = this.getContractModel(product);
			if (StringUtils.isEmpty(agreementModel)) {
				throw new AMPException("投资协议不存在");
			}
			
			Corporate corporate = corporateService.read(product.getPublisherBaseAccount().getCorperateOid());
			String lastOid = "0";
			bw = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(this.agreementShellPath + product.getOid() + ".first.sh")));
			while (true) {

				List<InvestorTradeOrderEntity> orderList = this.investorTradeOrderService
						.findByProductOid4Contract(product.getOid(), lastOid);
				if (orderList.isEmpty()) {
					break;
				}
				List<ProductAgreementEntity> agreeList = new ArrayList<ProductAgreementEntity>();
				
				for (InvestorTradeOrderEntity order : orderList) {
					try {
						ProductAgreementEntity investAgreement = this.createInvestEntity(order);
						String agreeFullFile = generateHtmlAgreement(agreementModel, order, corporate);
						ProductAgreementEntity serviceAgreement = this.createServiceEntity(order);
						String serviceFullFile = generateServiceAgreement(serviceModel, order);
						
						String agreePdfFullPath = agreeFullFile.replace(".html", ".pdf");
						bw.append("/usr/bin/wkhtmltopdf ").append(agreeFullFile).append("  ")
								.append(agreePdfFullPath)
								.append(System.getProperty("line.separator"));
						agreeList.add(investAgreement);
						
						String servicePdfFullPath = serviceFullFile.replace(".html", ".pdf");
						bw.append("/usr/bin/wkhtmltopdf ").append(serviceFullFile).append("  ")
								.append(servicePdfFullPath)
								.append(System.getProperty("line.separator"));
						agreeList.add(serviceAgreement);
						order.setContractStatus(InvestorTradeOrderEntity.TRADEORDER_contractStatus_htmlOK);
					} catch (Exception e) {
						e.printStackTrace();
						log.error("{}生成HTML异常", order.getOrderCode(), e);
						order.setContractStatus(InvestorTradeOrderEntity.TRADEORDER_contractStatus_htmlFail);
					}

					lastOid = order.getOid();
				}
				this.saveBatch(agreeList);
				this.investorTradeOrderService.batchUpdate(orderList);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(bw);
			File file = new File(this.agreementShellPath + product.getOid() + ".first.sh");
			if (file.getTotalSpace() == 0) {
				file.delete();
			} else {
				File flagFile = new File(this.agreementShellPath + product.getOid() + ".first.success");
				try {
					flagFile.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
		
	}


	public ProductAgreementEntity findByInvestorTradeOrderAndAgreementType(InvestorTradeOrderEntity order, String agreementType) {
		return this.productAgreementDao.findByInvestorTradeOrderAndAgreementType(order, agreementType);
	}
	public List<ProductAgreementEntity> findByOrderOid(String orderOid) {
		return this.productAgreementDao.findByOrderOid(orderOid);
	}
	
	private String generateServiceAgreement(String agreementModel, InvestorTradeOrderEntity order) throws Exception {
		String abDir = abDir(order);
		FileUtils.forceMkdir(new File(abDir));
		String agreementFullPath = abServiceName(abDir, order);

		agreementModel = agreementModel.replace("#investorAccount", order.getInvestorBaseAccount().getPhoneNum()) // 乐视金融用户名：【#investorAccount】
				.replace("#tradeOrderOid", order.getOrderCode()) // 编号:
				.replace("#investorID", StringUtil.isEmpty(order.getInvestorBaseAccount().getIdNum()) ? "" : order.getInvestorBaseAccount().getIdNum()); // 身份证号码：【#investorID】
						
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(agreementFullPath)));
			bw.write(agreementModel);
			bw.flush();
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			IOUtils.closeQuietly(bw);
		}
		return agreementFullPath;
	}

	private String generateHtmlAgreement(String agreementModel, InvestorTradeOrderEntity order, 
			Corporate corporate) throws Exception {
		String abDir =  abDir(order);
		FileUtils.forceMkdir(new File(abDir));
		String abName = abInvestName(abDir, order);
		
		agreementModel = agreementModel.replace("#spvName", corporate.getName())  //SPV名称
						.replace("#spvAddr", null == corporate.getAddress() ? "空" : corporate.getAddress()) //SPV注册地址
						.replace("#busiLicenseNo", null == corporate.getLicenseNo() ? "空" : corporate.getLicenseNo()) //证件编号
						.replace("#tradeOrderOid", order.getOrderCode()) // 编号: [#tradeOrderOid]
						.replace("#investorName", order.getInvestorBaseAccount().getRealName()) //受让方
						.replace("#investorAccount", order.getInvestorBaseAccount().getPhoneNum()) //平台用户名
						.replace("#investorID", StringUtil.isEmpty(order.getInvestorBaseAccount().getIdNum()) ? "" : order.getInvestorBaseAccount().getIdNum()) //证件编号
						.replace("#orderDate", DateUtil.format(order.getOrderTime())) //转让日/投资起始日#orderDate
						.replace("#orderVolume", order.getOrderVolume().toString()); //单位份数#orderVolume
		
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(abName)));
			bw.write(agreementModel);
			bw.flush();
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			IOUtils.closeQuietly(bw);
		}
		return abName;
	}
	

	private String abInvestName(String abDir, InvestorTradeOrderEntity order) {
		return abDir + order.getOrderCode() + "_" + ProductAgreementEntity.Agreement_agreementType_investing + ".html";
	}
	
	private String abServiceName(String abDir, InvestorTradeOrderEntity order) {
		return abDir + order.getOrderCode() + "_" + ProductAgreementEntity.Agreement_agreementType_service + ".html";
	}
	
	private String abInvestPDFName(String abDir, InvestorTradeOrderEntity order) {
		return abDir + order.getOrderCode() + "_" + ProductAgreementEntity.Agreement_agreementType_investing + ".pdf";
	}
	
	private String abServicePDFName(String abDir, InvestorTradeOrderEntity order) {
		return abDir + order.getOrderCode() + "_" + ProductAgreementEntity.Agreement_agreementType_service + ".pdf";
	}


	private String abDir(InvestorTradeOrderEntity order) {
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(order.getOrderTime());
		StringBuilder sb = new StringBuilder();
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DATE);
		
		sb.append(this.agreementPath).append(order.getProduct().getOid()).append(File.separator);
		sb.append(year).append(File.separator)
		.append(month < 10 ? "0" + month : month).append(File.separator)
		.append(day < 10 ? "0" + day : day).append(File.separator);
		
		return sb.toString();
	}


	public ProductAgreementEntity createInvestEntity(InvestorTradeOrderEntity order) {
		ProductAgreementEntity entity = new ProductAgreementEntity();
		entity.setProduct(order.getProduct());
		entity.setInvestorTradeOrder(order);
		entity.setAgreementCode(order.getOrderCode());
		entity.setAgreementName("invest protocol");
		entity.setAgreementType(ProductAgreementEntity.Agreement_agreementType_investing);
		entity.setCreateTime(DateUtil.getSqlCurrentDate());
		entity.setUpdateTime(DateUtil.getSqlCurrentDate());
		return entity;
	}
	
	public ProductAgreementEntity createServiceEntity(InvestorTradeOrderEntity order) {
		ProductAgreementEntity entity = new ProductAgreementEntity();
		entity.setProduct(order.getProduct());
		entity.setInvestorTradeOrder(order);
		entity.setAgreementCode(order.getOrderCode());
		entity.setAgreementName("invest protocol");
		entity.setAgreementType(ProductAgreementEntity.Agreement_agreementType_service);
		entity.setCreateTime(DateUtil.getSqlCurrentDate());
		entity.setUpdateTime(DateUtil.getSqlCurrentDate());
		return entity;
	}
	
	public void saveBatch(List<ProductAgreementEntity> entitys) {
		this.productAgreementDao.save(entitys);
	}


	public ProductAgreementEntity updateEntity(ProductAgreementEntity entity) {
		return this.productAgreementDao.save(entity);
	}
	
	

	public void uploadPDFDo() {
		List<Product> productList = productService.findByProduct4Contract();
		if (productList.isEmpty()) {
			return;
		}
		for (Product product : productList) {
			try {
				processPDF4Product(product);
			} catch (Exception e) {
				log.error("productOid:{},协议生成异常", product.getOid(), e);
			}
		}
	}
	
	private void processPDF4Product(Product product) {
		String lastOid = "0";
		
		while (true) {

			List<InvestorTradeOrderEntity> orderList = this.investorTradeOrderService
					.findByProductOid4PDF(product.getOid(), lastOid);
			if (orderList.isEmpty()) {
				break;
			}
			List<ProductAgreementEntity> agreeList = new ArrayList<ProductAgreementEntity>();
			for (InvestorTradeOrderEntity order : orderList) {
				String service = this.abServicePDFName(this.abDir(order), order);
				String invest = this.abInvestPDFName(this.abDir(order), order);
				if (new File(service).exists() && new File(invest).exists()) {
					ProductAgreementEntity serviceEn = this.productAgreementDao.findByInvestorTradeOrderAndAgreementType(order, ProductAgreementEntity.Agreement_agreementType_service);
					ProductAgreementEntity investEn = this.productAgreementDao.findByInvestorTradeOrderAndAgreementType(order, ProductAgreementEntity.Agreement_agreementType_investing);
					serviceEn.setAgreementUrl(service);
					investEn.setAgreementUrl(invest);
					agreeList.add(serviceEn);
					agreeList.add(investEn);
					order.setContractStatus(InvestorTradeOrderEntity.TRADEORDER_contractStatus_pdfOK);
				}
				lastOid = order.getOid();
			}
			this.investorTradeOrderService.batchUpdate(orderList);
			productAgreementDao.save(agreeList);
		}
	}


	public void uploadPDF() {
		
		if (this.jobLockService.getRunPrivilege(JobLockEntity.JOB_jobId_uploadPDF)) {
			this.uploadPDFLog();
		}
	}

	public void uploadPDFLog() {
		JobLogEntity jobLog =  JobLogFactory.getInstance(JobLockEntity.JOB_jobId_uploadPDF);
		
		try {
			uploadPDFDo();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			jobLog.setJobMessage(AMPException.getStacktrace(e));
			jobLog.setJobStatus(JobLogEntity.JOB_jobStatus_failed);
		}
		jobLog.setBatchEndTime(DateUtil.getSqlCurrentDate());
		this.jobLogService.saveEntity(jobLog);
		this.jobLockService.resetJob(JobLockEntity.JOB_jobId_uploadPDF);
		
		
	}

}
