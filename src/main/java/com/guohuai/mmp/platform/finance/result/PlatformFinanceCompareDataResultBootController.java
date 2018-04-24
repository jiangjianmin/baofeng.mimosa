package com.guohuai.mmp.platform.finance.result;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.StringUtil;
import com.guohuai.component.web.view.PagesRep;
import com.guohuai.file.CsvExport;

import net.kaczmarzyk.spring.data.jpa.domain.DateAfterInclusive;
import net.kaczmarzyk.spring.data.jpa.domain.DateBeforeInclusive;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.GreaterThanOrEqual;
import net.kaczmarzyk.spring.data.jpa.domain.LessThanOrEqual;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;

@RestController
@RequestMapping(value = "/mimosa/platform/finance/result", produces = "application/json")
public class PlatformFinanceCompareDataResultBootController extends BaseController{
	@Autowired
	private PlatformFinanceCompareDataResultService platformFinanceCompareDataResultService;
	
	/**
	 * 查询
	 */
	@RequestMapping(value = "checkResultList", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<PagesRep<PlatformFinanceCompareDataResultRep>> checkResultList(HttpServletRequest request,
			@RequestParam int page, @RequestParam int rows,
			@RequestParam String orderCode,
			@And({@Spec(params = "orderAmount", path = "orderAmount", spec = GreaterThanOrEqual.class),
				@Spec(params = "orderStatus", path = "orderStatus", spec = Equal.class),
				@Spec(params = "orderType", path = "orderType", spec = Equal.class),
				@Spec(params = "checkStatus", path = "checkStatus", spec = Equal.class),
				@Spec(params = "checkDate", path = "buzzDate", spec = DateAfterInclusive.class, config = DateUtil.defaultDatePattern),
				@Spec(params = "checkDate", path = "buzzDate", spec = DateBeforeInclusive.class, config = DateUtil.defaultDatePattern),
				@Spec(params = "orderAmountMax", path = "orderAmount", spec = LessThanOrEqual.class),
				@Spec(params = "checkOrderStatus", path = "checkOrderStatus", spec = Equal.class)})
				Specification<PlatformFinanceCompareDataResultEntity> spec,
			@RequestParam(required = false, defaultValue = "createTime") String sort,
			@RequestParam(required = false, defaultValue = "desc") String order) {
		Direction sortDirection = Direction.DESC;
		if (!"desc".equals(order)) {
			sortDirection = Direction.ASC;
		}
		if (!StringUtil.isEmpty(orderCode)) {
			Specification<PlatformFinanceCompareDataResultEntity> stateSpec = new Specification<PlatformFinanceCompareDataResultEntity>() {
				@Override
				public Predicate toPredicate(Root<PlatformFinanceCompareDataResultEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
					Expression<String> exp = root.get("orderCode").as(String.class);
					Expression<String> exp2 = root.get("checkOrderCode").as(String.class);
					return cb.or(cb.like(exp, "%"+orderCode+"%"),cb.like(exp2, "%"+orderCode+"%"));
				}
			};
			spec = Specifications.where(spec).and(stateSpec);
		}
		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(sortDirection, sort)));
		PagesRep<PlatformFinanceCompareDataResultRep> rep = this.platformFinanceCompareDataResultService.checkResultList(spec, pageable);

		return new ResponseEntity<PagesRep<PlatformFinanceCompareDataResultRep>>(rep, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/checkResultListDown", method = {RequestMethod.GET, RequestMethod.POST})
	public void down(HttpServletRequest request,
			@RequestParam String orderCode,
			@RequestParam String orderAmount,
			@RequestParam String orderStatus,
			@RequestParam String orderType,
			@RequestParam String checkStatus,
			@RequestParam String checkDate,
			@RequestParam String orderAmountMax,
			@RequestParam String checkOrderStatus) {
		
		try{
			String name = "对账结果导出.csv";
			List<List<String>> data = new ArrayList<List<String>>();
			data = this.platformFinanceCompareDataResultService.data(orderCode,orderAmount,orderStatus,orderType,checkStatus,checkDate,orderAmountMax,checkOrderStatus);
			List<String> header = this.platformFinanceCompareDataResultService.header();
			String realPath = "../Temp/";
			File f = new File(realPath);
			if(!f.exists()){
				f.mkdirs();
			}
			File file = new File(f, name);
			if(!file.exists()){
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			File filePathName = new File(realPath + name);
			CsvExport.exportCsv(filePathName, header, data);
			download(realPath + name, response);
			CsvExport.deleteFiles(realPath);
		} catch (Exception e){
			e.printStackTrace();
		}
		
	}
	
	private void download(String path, HttpServletResponse response){
		try {
			// path是指欲下载的文件的路径。
			File file = new File(path);
			// 取得文件名。
			String filename = file.getName();
			// 以流的形式下载文件。
			InputStream fis = new BufferedInputStream(new FileInputStream(path));
			byte[] buffer = new byte[fis.available()];
			fis.read(buffer);
			fis.close();
			// 清空response
			response.reset();
			// 设置response的Header
			response.setHeader("Content-Disposition",
					"attachment;filename="+new String(filename.getBytes("UTF-8"), "ISO-8859-1"));
			response.addHeader("Content-Length", "" + file.length());
			OutputStream toClient = new BufferedOutputStream(response.getOutputStream());
			response.setContentType("application/vnd.ms-excel;charset=UTF-8");
			toClient.write(buffer);
			toClient.flush();
			toClient.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
	}
}
