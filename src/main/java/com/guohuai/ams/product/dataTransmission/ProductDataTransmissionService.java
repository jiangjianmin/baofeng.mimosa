package com.guohuai.ams.product.dataTransmission;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSSException;
import com.guohuai.basic.common.ApiOSSClient;
import com.guohuai.basic.common.ApiOSSClient.ContentType;
import com.guohuai.basic.common.SFTPUtil;
import com.guohuai.operate.api.AdminSdk;
import com.guohuai.operate.api.objs.admin.AdminObj;
import com.guohuai.plugin.PageVo;
import com.guohuai.util.BfFileUtil;
import com.jcraft.jsch.SftpException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@PropertySource(value = {"classpath:application-dev.properties"}, encoding="utf-8")
@Transactional
public class ProductDataTransmissionService {
	
	/** FTP文件路径 */
	@Value("${nj.sftp.dir}")
	public String sftpDir;
	/** 本息支付方式  */
	@Value("${product.payway}")
	public String payway;
	/** 到期日是否计息(0否，1是) */
	@Value("${product.endDay}")
	public String endDay;
	/** 平台代码 */
	@Value("${product.platformCode}")
	public String platformCode;
	/** 产品销售表命名版本号 */
	@Value("${product.proSaleVersion}")
	public String proSaleVersion;
	/** 客户销售表命名版本号 */
	@Value("${product.peoSaleVersion}")
	public String peoSaleVersion;
	/** 产品销售表头*/
	@Value("${product.proInfoHeard}")
	public String proInfoHeard;
	/** 客户销售表头*/
	@Value("${product.invInfoHeard}")
	public String invInfoHeard;
	/** 产品发行人*/
	@Value("${product.nj_publisher}")
	public String publisher;
	
	
	@Autowired
	private ProductDataTransmissionDao productDataTransmissionDao;
	@Autowired
	private AdminSdk adminSdk;
	@Autowired
	private BfFileUtil bfFileUtil;
	
	/**
	 * 
	 * @author yihonglei
	 * @Title: queryProductInfo
	 * @Description:查询募集期结束，有投资成功记录并且募集结束日期小于当前系统时间的定期产品列表
	 * @param req
	 * @return PageVo<Map<String,Object>>
	 * @throws UnsupportedEncodingException 
	 * @date 2017年5月23日 下午9:59:44
	 * @since  1.0.0
	 */
	public PageVo<Map<String, Object>> queryProductInfo(ProductDataTransmissionReq req) throws UnsupportedEncodingException {
		log.info("产品列表信息查询开始！");
		PageVo<Map<String,Object>> pageVo = new PageVo<Map<String,Object>>();
		// 产品信息列表
		List<Object[]> productInfo = new ArrayList<Object[]>();
		// 总条数
		int total = 0;
		// 查询产品信息
		productInfo = productDataTransmissionDao.getProductInfo(
				req.getStartTime(),req.getEndTime(),req.getFullName(),new String(publisher.getBytes("UTF-8"),"UTF-8"),
				(req.getPage() - 1) * req.getRow(), req.getRow());
		
		total = productDataTransmissionDao.getProductInfoCount(req.getStartTime(),req.getEndTime(),req.getFullName(),new String(publisher.getBytes("UTF-8"),"UTF-8"));
		// 封装产品信息列表
		List<Map<String,Object>> productInfoList = new ArrayList<Map<String,Object>>();
		for (int i=0;i<productInfo.size();i++) {
			Map<String,Object> mapMessage = new HashMap<String,Object>();
			Object[] objData = productInfo.get(i);
			mapMessage.put("productOid", objData[0].toString());
			mapMessage.put("code", objData[1].toString());// 产品代码
			mapMessage.put("fullName", objData[2].toString());// 产品名称
			mapMessage.put("raisedTotalNumber", objData[3].toString());// 产品发行金额(元)
			mapMessage.put("collectedVolume", objData[4].toString());// 实际募集金额(元)
			mapMessage.put("expAror", objData[5].toString());// 产品利率
			mapMessage.put("durationPeriodDays", objData[6].toString());// 期限(天)
			if ("1".equals(payway)) {// 本息支付方式
				mapMessage.put("payPrincipalWay", "到期一次性还本付息");
			} else {
				mapMessage.put("payPrincipalWay", "其他");
			}
			mapMessage.put("raiseStartDate", objData[7].toString());// 募集开始日期
			mapMessage.put("raiseEndDate", objData[8].toString());// 募集结束日期
			mapMessage.put("durationPeriodEndDate", objData[9].toString());// 到期日期
			mapMessage.put("interestsFirstDays", objData[10].toString());// 起息日期
			if ("1".equals(endDay)) {// 到期日是否计息(0否，1是)
				mapMessage.put("durationend", "是");
			} else {
				mapMessage.put("durationend", "否");
			}
			
			productInfoList.add(mapMessage);
		}
		
		// 分页
		pageVo.setRows(productInfoList);
		pageVo.setTotal(total);
		pageVo.setRow(req.getRow());
		pageVo.setPage(req.getPage());
		pageVo.reTotalPage();
		
		return pageVo;
	}
	
	/**
	 * 
	 * @author yihonglei
	 * @Title: queryProductInvestorInfo
	 * @Description:根据产品Id查询某个产品对应投资人投资信息列表
	 * @param req
	 * @return PageVo<Map<String,Object>>
	 * @date 2017年5月23日 下午10:00:04
	 * @since  1.0.0
	 */
	public PageVo<Map<String, Object>> queryProductInvestorInfo(ProductDataTransmissionReq req) {
		log.info("根据产品Id查询某个产品对应投资人投资信息列表开始！");
		PageVo<Map<String,Object>> pageVo = new PageVo<Map<String,Object>>();
		// 产品投资详细列表
		List<Object[]> productInvestorInfo = new ArrayList<Object[]>();
		// 总条数
		int total = 0;
		// 查询产品信息
		productInvestorInfo = productDataTransmissionDao.getProductInvestorInfo(
				req.getProductOid(),(req.getPage() - 1) * req.getRow(), req.getRow());
		
		total = productDataTransmissionDao.getProductInvestorInfoCount(req.getProductOid());
		
		// 封装产品投资详细列表
		List<Map<String, Object>> productInvestorInfoList = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < productInvestorInfo.size(); i++) {
			Map<String, Object> mapMessage = new HashMap<String, Object>();
			Object[] objData = productInvestorInfo.get(i);
			mapMessage.put("orderCode", objData[0].toString());// 交易代码
			mapMessage.put("orderTime", objData[1].toString());// 交易时间
			mapMessage.put("code", objData[2].toString());// 产品代码
			mapMessage.put("orderAmount", objData[3].toString());// 认购金额(元)
			mapMessage.put("realName", objData[4].toString());// 客户姓名
			mapMessage.put("idNum", objData[5].toString());// 证件号码
			mapMessage.put("phoneNum", objData[6].toString());// 手机
			mapMessage.put("expAror", objData[7].toString());// 产品利率
			
			productInvestorInfoList.add(mapMessage);
		}

		// 分页
		pageVo.setRows(productInvestorInfoList);
		pageVo.setTotal(total);
		pageVo.setRow(req.getRow());
		pageVo.setPage(req.getPage());
		pageVo.reTotalPage();

		return pageVo;
	}
	/**
	 * 
	 * @author yihonglei
	 * @Title: queryProductDataTransRecord
	 * @Description:查询某个产品对应数据传输记录列表
	 * @param req
	 * @return PageVo<Map<String,Object>>
	 * @date 2017年5月24日 上午12:14:40
	 * @since  1.0.0
	 */
	public PageVo<Map<String, Object>> queryProductDataTransRecord(ProductDataTransmissionReq req) {
		log.info("查询某个产品对应数据传输记录列表开始！");
		PageVo<Map<String,Object>> pageVo = new PageVo<Map<String,Object>>();
		// 产品信息传输记录列表
		List<Object[]> productITransRecord = new ArrayList<Object[]>();
		// 总条数
		int total = 0;
		// 查询产品信息传输记录
		productITransRecord = productDataTransmissionDao.getProductDataTransRecord(req.getProductOid());
		
		total = productDataTransmissionDao.getProductDataTransRecordCount(req.getProductOid());
		
		// 封装产品信息传输记录列表
		List<Map<String, Object>> productITransRecordList = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < productITransRecord.size(); i++) {
			Map<String, Object> mapMessage = new HashMap<String, Object>();
			Object[] objData = productITransRecord.get(i);
			mapMessage.put("createTime", objData[0].toString());// 传输时间
			AdminObj adminObj = adminSdk.getAdmin(objData[1].toString());// 根据操作者id,查询操作者名字
			if (null != adminObj) {
				mapMessage.put("operateName", adminObj.getName());// 操作者
			} else {
				mapMessage.put("operateName", "-");// 操作者
			}
			
			productITransRecordList.add(mapMessage);
		}

		// 分页
		pageVo.setRows(productITransRecordList);
		pageVo.setTotal(total);
		pageVo.setRow(req.getRow());
		pageVo.setPage(req.getPage());
		pageVo.reTotalPage();

		return pageVo;
	}
	/**
	 * 
	 * @author yihonglei
	 * @Title: productDataTransBySftp
	 * @Description:通过sftp方式，进行数据传输(包含产品基本信息，产品投资人信息，分成两个文件传输)
	 * @param req
	 * @return PageVo<Map<String,Object>>
	 * @throws IOException 
	 * @throws ClientException 
	 * @throws OSSException 
	 * @throws SftpException 
	 * @date 2017年5月24日 上午12:15:10
	 * @since  1.0.0
	 */
	public Map<String, Object> productDataTransBySftp(String operator) {
		log.info("<-----------数据传输开始！------------->");
		Map<String,Object> messageMap = new HashMap<String,Object>();
		boolean uploadResult = false;
		
		try {
			String lastUploadDate = productDataTransmissionDao.getProductDataTransRecordLastTime();// 获取上次上传的时间
			// 循环满足数据传输的基础资产编号
			String[] baseAssetCodes = productDataTransmissionDao.getBaseAssetCodes(lastUploadDate,new String(publisher.getBytes("UTF-8"),"UTF-8"));
			if (baseAssetCodes.length > 0) {
				for (int i=0;i<baseAssetCodes.length;i++) {
					String baseAssetCode = baseAssetCodes[i];
					// 循环所有资产编号下满足上传条件的所有募集结束日期
					String[] raiseEndDates = productDataTransmissionDao.getRaiseEndDates(lastUploadDate, baseAssetCode);
					for (int j=0;j<raiseEndDates.length;j++) {
						String raiseEndDate = raiseEndDates[j];
						uploadResult = uploadProductInfo(raiseEndDate,baseAssetCode,operator);
					}
				}
				if (uploadResult) {
					messageMap.put("uploadResult", "数据上传成功");
				} else {
					messageMap.put("uploadResult", "数据上传失败");
				}
				
			} else {
				messageMap.put("uploadResult", "暂无可上传数据");
			}
		} catch (Exception e) {
			e.printStackTrace();
			messageMap.put("uploadResult", "数据传输失败,请联系管理员!");
		}
		
		log.info("<-----------根据产品Id进行数据传输结束！------------->");
		return messageMap;
	}
	/**
	 * 
	 * @author yihonglei
	 * @Title: uploadProductInfo
	 * @Description: 上传产品信息
	 * @param raiseEndDate
	 * @param baseAssetCode
	 * @param operator
	 * @throws OSSException
	 * @throws ClientException
	 * @throws IOException
	 * @throws SftpException
	 * @return boolean
	 * @throws UnsupportedEncodingException 
	 * @date 2017年5月25日 下午2:43:40
	 * @since  1.0.0
	 */
	private boolean uploadProductInfo(String raiseEndDate,String baseAssetCode,String operator) throws UnsupportedEncodingException {
		log.info("<----------上传产品信息开始，操作者:{},募集结束日期:{},基础资产编号:{}----------->",operator,raiseEndDate,baseAssetCode);
		// 上传结果
		boolean uploadResult = false;
		// 产品信息列表
		List<Object[]> productUploadInfo = new ArrayList<Object[]>();
		// 查询产品信息
		productUploadInfo = productDataTransmissionDao.getProductUploadInfo(raiseEndDate,baseAssetCode);
		if (productUploadInfo.size() > 0) {
			// 文件名（募集结束时间_平台代码_基础资产编号_产品销售表_v2.0.csv eg: 20170520_bfjr_LCJH-BFJRBFBXSZX14XL06_产品销售表_v2.0.csv）
			String uploadFileName = productUploadInfo.get(0)[8].toString()+platformCode+baseAssetCode+new String(proSaleVersion.getBytes("UTF-8"),"UTF-8");
			// 生成文件流
			InputStream fileIs = getProductInfoIs(productUploadInfo);
			// 上传交易所(产品信息)
			uploadResult = uploadProductData(uploadFileName,fileIs);
			log.info("<----------上传产品信息结束，上传结果:{}----------->",uploadResult);
			if (uploadResult) {
				// 上传产品对应投资人信息
				uploadResult = uploadProductInvestorInfo(productUploadInfo, baseAssetCode);
			}
			// 存入产品传输记录
			if (uploadResult) {
				log.info("<----------存入产品传输记录开始！----------->");
				for (int i=0;i<productUploadInfo.size();i++) {
					String productOid = productUploadInfo.get(i)[0].toString();
					int saveR = productDataTransmissionDao.saveProductDataTransRecord(productOid, operator);
					log.info("<----------productOid:{},存入传输记录结果:{}----------->",productOid,saveR>0);
				}
				log.info("<----------存入产品传输记录结束！----------->");
			}
		}
		
		return uploadResult;
	}
	/**
	 * 
	 * @author yihonglei
	 * @Title: uploadProductInvestorInfo
	 * @Description: 上传产品对应投资人信息
	 * @param productUploadInfo
	 * @param baseAssetCode
	 * @return boolean
	 * @throws UnsupportedEncodingException 
	 * @date 2017年5月25日 下午3:01:59
	 * @since  1.0.0
	 */
	private boolean uploadProductInvestorInfo(List<Object[]> productUploadInfo,String baseAssetCode) throws UnsupportedEncodingException {
		boolean uploadResult = false;
		// 所有产品对应投资人信息列表
		List<Object[]> productUploadInvestorInfoAll = new ArrayList<Object[]>();
		for (int i=0;i<productUploadInfo.size();i++) {
			// 根据产品Id获取对应投资人信息
			List<Object[]> productUploadInvestorInfo = new ArrayList<Object[]>();
			productUploadInvestorInfo = productDataTransmissionDao.getProductUploadInvestorInfo(productUploadInfo.get(i)[0].toString());
			productUploadInvestorInfoAll.addAll(productUploadInvestorInfo);
		}
		
		if (productUploadInvestorInfoAll.size() > 0) {
			// 文件名（募集结束时间_平台代码_基础资产编号_客户销售表_v2.0.csv eg: 20170520_bfjr_LCJH-BFJRBFBXSZX14XL06_客户销售表_v2.0.csv）
			String uploadFileName = productUploadInvestorInfoAll.get(0)[8].toString()+platformCode+baseAssetCode+new String(peoSaleVersion.getBytes("UTF-8"),"UTF-8");
			// 生成文件流
			InputStream fileIs = getProductInvestorInfoIs(productUploadInvestorInfoAll);
			// 上传交易所(产品投资信息)
			uploadResult = uploadProductData(uploadFileName,fileIs);
		}
		
		return uploadResult;
	}
	/**
	 * 
	 * @author yihonglei
	 * @Title: getProductInfoIs
	 * @Description: 上传产品信息文件流生成
	 * @param productUploadInfo
	 * @return InputStream
	 * @throws UnsupportedEncodingException 
	 * @date 2017年5月25日 下午3:02:16
	 * @since  1.0.0
	 */
	private InputStream getProductInfoIs(List<Object[]> productUploadInfo) throws UnsupportedEncodingException {
		StringBuffer buf = new StringBuffer();
		try {
			// 表格头
			String heard = new String(proInfoHeard.getBytes("UTF-8"),"UTF-8");
			buf.append(heard);
			buf.append(System.getProperty("line.separator"));// 换行符
			for (int i=0;i<productUploadInfo.size();i++) {
				Object[] objData = productUploadInfo.get(i);
				// 表格内容
				String content = objData[1].toString()+"|"+ // 产品代码
						  objData[2].toString()+"|"+ // 产品名称
						  objData[3].toString()+"|"+ // 产品发行金额(元)
						  objData[4].toString()+"|"+ // 实际募集金额(元)
						  objData[5].toString()+"|"+ // 产品利率
						  objData[6].toString()+"|"+ // 期限(天)
						  payway+"|"+ // 本息支付方式
						  objData[7].toString()+"|"+ // 募集开始日期
						  objData[8].toString()+"|"+ // 募集结束日期
						  objData[9].toString()+"|"+ // 到期日期
						  objData[10].toString()+"|"+ // 起息日期
						  endDay; // 到期日是否计息(0否，1是)
				// 字符串拼接
				buf.append(content);
				buf.append(System.getProperty("line.separator"));// 换行符
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		InputStream is = new ByteArrayInputStream(buf.toString().getBytes("UTF-8"));
	
		return is;
	}
	/**
	 * 
	 * @author yihonglei
	 * @Title: getProductInvestorInfoIs
	 * @Description: 上传产品投资人信息文件流生成
	 * @param productUploadInvestorInfo
	 * @return InputStream
	 * @throws UnsupportedEncodingException 
	 * @date 2017年5月25日 下午3:02:32
	 * @since  1.0.0
	 */
	private InputStream getProductInvestorInfoIs(List<Object[]> productUploadInvestorInfo) throws UnsupportedEncodingException {
		StringBuffer buf = new StringBuffer();
		try {
			// 表格头
			String heard = new String(invInfoHeard.getBytes("UTF-8"),"UTF-8");
			buf.append(heard);
			buf.append(System.getProperty("line.separator"));// 换行符
			// 表格内容
			for (int i = 0; i < productUploadInvestorInfo.size(); i++) {
				Object[] objData = productUploadInvestorInfo.get(i);
				// 字符串拼接
				String content = objData[0].toString()+"|"+ // 交易代码
						  objData[1].toString()+"|"+ // 交易时间
						  objData[2].toString()+"|"+ // 产品代码
						  objData[3].toString()+"|"+ // 认购金额
						  objData[4].toString()+"|"+ // 客户姓名
						  objData[5].toString()+"|"+ // 证件号码
						  objData[6].toString()+"|"+ // 手机
						  objData[7].toString(); // 利率
				
				buf.append(content);
				buf.append(System.getProperty("line.separator"));// 换行符
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		InputStream is = new ByteArrayInputStream(buf.toString().getBytes("UTF-8"));
		
		return is;
	}
	/**
	 * 
	 * @author yihonglei
	 * @Title: uploadProductData
	 * @Description: 上传--oss--sftp
	 * @param uploadFileName
	 * @param fileIs
	 * @return boolean
	 * @date 2017年5月25日 下午2:59:41
	 * @since  1.0.0
	 */
	private boolean uploadProductData(String uploadFileName,InputStream fileIs) {
		log.info("<------------上传文件名:{}------------>",uploadFileName);
		ApiOSSClient apiOSSClient = bfFileUtil.getApiOSSClient();// 获取oss
		//SFTPUtil sftp = bfFileUtil.getSFTPUtilPas();// 获取sftp
		SFTPUtil sftp = bfFileUtil.getSFTPUtilKey();// 获取sftp
		// 上传结果
		boolean uploadResult = true;
		try {
			// 上传到阿里云
			log.info("<-------------上传到oss开始！--------------->");
			apiOSSClient.uploadFile(uploadFileName, fileIs, ContentType.CSV);
			log.info("<-------------上传到oss结束！--------------->");
			
			// 从阿里云下载
			log.info("<-------------从oss下载开始！--------------->");
			InputStream isDown = apiOSSClient.downloadFile(uploadFileName);
			log.info("<-------------从oss下载结束！--------------->");
			
			// 从阿里云到sftp服务器
			sftp.login();
			log.info("<-------------通过sftp上传到南京交易所开始！--------------->");
			String fileDirName = new SimpleDateFormat("yyyyMMdd").format(new Date());
			sftp.upload(sftpDir+fileDirName, uploadFileName, isDown);
			log.info("<-------------通过sftp上传到南京交易所结束！--------------->");
		} catch (Exception e) {
			e.printStackTrace();
			uploadResult = false;
		} finally {
			sftp.logout();
		}
		
		return uploadResult;
	}

}
