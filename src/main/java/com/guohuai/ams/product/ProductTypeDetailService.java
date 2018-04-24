package com.guohuai.ams.product;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.Clock;
import com.guohuai.component.web.view.PageResp;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class ProductTypeDetailService {
	@Autowired
	ProductTypeDetailDao daoProductTypeDetail;
	@Value("${productTypeDetail.headHtm}")
	private String headHtm;
	@Value("${productTypeDetail.backHtm}")
	private String backHtm;
	@Value("${productTypeDetailUrl}")
	private String productTypeDetailUrl;
	@Value("${productTypeDetailLocalPre}")
	private String productTypeDetailLocalPre;
	/**
	 * 查询产品列表
	 * @param spec
	 * @param pageable
	 * @return
	 */
	@Transactional
	public PageResp<ProductTypeDetailQueryResp> ProductTypeDetailQuery(Specification<ProductTypeDetail> spec, Pageable pageable) {
		Page<ProductTypeDetail> enchs = this.daoProductTypeDetail.findAll(spec, pageable);
		PageResp<ProductTypeDetailQueryResp> pageResp = new PageResp<ProductTypeDetailQueryResp>();
		List<ProductTypeDetailQueryResp> list = new ArrayList<ProductTypeDetailQueryResp>();
		
		if (enchs != null && enchs.getContent() != null && enchs.getTotalElements() > 0) {
			for (ProductTypeDetail ench : enchs) {
				ProductTypeDetailQueryResp r = new ProductTypeDetailQueryResp(ench);
				list.add(r);
			}
		}
		pageResp.setTotal(enchs.getTotalElements());
		pageResp.setRows(list);
		return pageResp;
	}
	
	/**
	 * 新增和修改产品详情
	 * 
	 * @param req
	 * @return
	 */
	public ProductTypeDetail addProductTypeDetail(ProductTypeDetailAddReq req) {
		Timestamp now = new Timestamp(Clock.DEFAULT.getCurrentTimeInMillis());
		ProductTypeDetail ProductTypeDetail = null;
		if (req.getId() != null && !"".equals(req.getId())) {
			ProductTypeDetail = this.getOne(req.getId());
		} else {
			ProductTypeDetail = new ProductTypeDetail();
			ProductTypeDetail.setCreateTime(now);
		}
		// 此处ProductTypeDetailId以后修改成想要的格式
		ProductTypeDetail.setType(req.getType());
		ProductTypeDetail.setTitle(req.getTitle());
		ProductTypeDetail.setDetail(req.getDetail());
		ProductTypeDetail.setUrl(getUrl(req.getDetail(),ProductTypeDetail.getOid()));
		ProductTypeDetail.setOperator(req.getOperator());
		ProductTypeDetail.setUpdateTime(now);
		ProductTypeDetail = this.daoProductTypeDetail.save(ProductTypeDetail);
		return ProductTypeDetail;
	}
	public String getUrl(String detail,String oid) {
		String content = headHtm+detail+backHtm;
		String str = "";
		String localUrl = "."+File.separator+oid+".html";
		try {
			PrintWriter pw = new PrintWriter(new FileWriter(localUrl));
			pw.print(content);
			pw.close();
		} catch (Exception e) {
			// TODO: handle exception
			log.error("保存文件错误：{}", e);
		}
		CloseableHttpClient client = HttpClients.createDefault();  
        
        HttpPost httppost = new HttpPost(productTypeDetailUrl);    
          
        MultipartEntityBuilder builder = MultipartEntityBuilder.create(); 
        File file = new File(localUrl);
        builder.addBinaryBody("uploadfile", file);  
          
        HttpEntity reqEntity = builder.build();  
          
        httppost.setEntity(reqEntity);  
        try {
        	CloseableHttpResponse resp = client.execute(httppost);  
            
            str = EntityUtils.toString(resp.getEntity());
            str = str.substring(1, str.length()-2);
            str = JSON.parseObject(str).get("url").toString();
            str = productTypeDetailLocalPre+str.substring(7);
            if(file.isFile() && file.exists()) {
            	file.delete();
            }
            resp.close();  
            client.close();  
		} catch (Exception e) {
			log.error("上传文件错误：{}", e);
		}
        
		return str;
	}
	/**
	 * 获取渠道详情
	 * 
	 * @param oid
	 * @return
	 */
	@Transactional
	public ProductTypeDetailInfoResp getProductTypeDetailInfo(String oid) {
		ProductTypeDetail productTypeDetail = this.getOne(oid);
		ProductTypeDetailInfoResp resp = new ProductTypeDetailInfoResp();
		resp.setOid(productTypeDetail.getOid());
		resp.setType(productTypeDetail.getType());
		resp.setTitle(productTypeDetail.getTitle());
		resp.setDetail(productTypeDetail.getDetail());
		resp.setOperator(productTypeDetail.getOperator());
		resp.setCreateTime(productTypeDetail.getCreateTime());
		resp.setUpdateTime(productTypeDetail.getUpdateTime());
		return resp;
	}
	/**
	 * 获取产品详情实体
	 * 
	 * @param oid
	 * @return
	 */
	public ProductTypeDetail getOne(String oid) {
		ProductTypeDetail en = this.daoProductTypeDetail.findOne(oid);
		return en;
	}
	public ProductTypeDetailOptionsResp getByType(String type){
		ProductTypeDetailOptionsResp res = new ProductTypeDetailOptionsResp();
		List<ProductTypeDetail> productTypeDetails=this.daoProductTypeDetail.findByType(type);
		productTypeDetails.stream().forEach(productTypeDetail->{
			ProductTypeDetailOption productTypeDetailOption = new ProductTypeDetailOption(productTypeDetail.getOid(),productTypeDetail.getType(),productTypeDetail.getTitle(),productTypeDetail.getUrl());
			res.getProductTypeDetailOptions().add(productTypeDetailOption);
		});
		return res;
	}
	public ProductTypeDetailOptionsResp getProductTypeDetailAll(){
		ProductTypeDetailOptionsResp res = new ProductTypeDetailOptionsResp();
		List<ProductTypeDetail> productTypeDetails=this.daoProductTypeDetail.findAllOrderByCreateTimeDesc();
		productTypeDetails.stream().forEach(productTypeDetail->{
			ProductTypeDetailOption productTypeDetailOption = new ProductTypeDetailOption(productTypeDetail.getOid(),productTypeDetail.getType(),productTypeDetail.getTitle(),productTypeDetail.getUrl());
			res.getProductTypeDetailOptions().add(productTypeDetailOption);
		});
		return res;
	}
}
