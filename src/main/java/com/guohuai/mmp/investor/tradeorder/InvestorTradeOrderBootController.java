package com.guohuai.mmp.investor.tradeorder;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.guohuai.mmp.investor.tradeorder.cycleProduct.BookingOrderRep;
import com.guohuai.mmp.investor.tradeorder.cycleProduct.BookingOrderReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.guohuai.ams.product.Product;
import com.guohuai.basic.common.StringUtil;
import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.component.web.view.PagesRep;
import com.guohuai.file.CsvExport;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountEntity;
import com.guohuai.mmp.sys.SysConstant;

import lombok.extern.slf4j.Slf4j;
import net.kaczmarzyk.spring.data.jpa.domain.DateAfterInclusive;
import net.kaczmarzyk.spring.data.jpa.domain.DateBeforeInclusive;
import net.kaczmarzyk.spring.data.jpa.domain.GreaterThanOrEqual;
import net.kaczmarzyk.spring.data.jpa.domain.LessThanOrEqual;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.In;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
@Slf4j
@RestController
@RequestMapping(value = "/mimosa/boot/tradeorder", produces = "application/json")
public class InvestorTradeOrderBootController extends BaseController {
	
	@Autowired
	private InvestorTradeOrderService investorTradeOrderService;
	
	@Autowired
	private InvestorSpecialRedeemService investorSpecialRedeemService;
	
	@RequestMapping(value = "deta", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<TradeOrderDetailRep> detail(@RequestParam(required = true) String tradeOrderOid) {
		TradeOrderDetailRep detailRep = this.investorTradeOrderService.detail(tradeOrderOid);
		return new ResponseEntity<TradeOrderDetailRep>(detailRep, HttpStatus.OK);
	}
	

	
//	@RequestMapping(value = "mng", method = RequestMethod.POST)
//	@ResponseBody
//	public ResponseEntity<PagesRep<TradeOrderQueryRep>> mng(HttpServletRequest request,
//			@And({@Spec(params = "orderType", path = "orderType", spec = In.class),
//				  @Spec(params = "orderStatus", path = "orderStatus", spec = In.class),
//				  @Spec(params = "isAuto", path = "isAuto", spec = In.class),
//				  @Spec(params = "channelOid", path = "channel.oid", spec = Equal.class),
//				  @Spec(params = "channelName", path = "channel.channelName", spec = Equal.class),
//				  @Spec(params = "orderCode", path = "orderCode", spec = Equal.class),
//				  @Spec(params = "createTimeBegin", path = "createTime", spec = DateAfterInclusive.class, config = DateUtil.fullDatePattern),
//				  @Spec(params = "createTimeEnd", path = "createTime", spec = DateBeforeInclusive.class, config = DateUtil.fullDatePattern),
//				  @Spec(params = "minOrderAmount", path = "orderAmount", spec = GreaterThanOrEqual.class),
//				  @Spec(params = "maxOrderAmount", path = "orderAmount", spec = LessThanOrEqual.class),
//				  @Spec(params = "createMan", path = "createMan", spec = Equal.class),
//				  @Spec(params = "productName", path = "product.name", spec = Equal.class),
//				  @Spec(params = "productType", path = "product.type.oid", spec = Equal.class),
//				  @Spec(params = "productOid", path = "product.oid", spec = Equal.class),
//				  @Spec(params = "investorOid", path = "investorBaseAccount.oid", spec = Equal.class),
//				  @Spec(params = "publisherClearStatus", path = "publisherClearStatus", spec = In.class),
//				  @Spec(params = "investorOffsetOid", path = "investorOffset.oid", spec = Equal.class),
//				  @Spec(params = "publisherOffsetOid", path = "publisherOffset.oid", spec = Equal.class)}) Specification<InvestorTradeOrderEntity> spec,
//			@RequestParam int page, 
//			@RequestParam int rows,
//			@RequestParam(required = false, defaultValue = "createTime") String sort,
//			@RequestParam(required = false, defaultValue = "desc") String order) {
//		Direction sortDirection = Direction.DESC;
//		if (!"desc".equals(order)) {
//			sortDirection = Direction.ASC;
//		}
//		
//		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(sortDirection, sort)));
//		PagesRep<TradeOrderQueryRep> rep = this.investorTradeOrderService.investorTradeOrderMng(spec, pageable);
//		return new ResponseEntity<PagesRep<TradeOrderQueryRep>>(rep, HttpStatus.OK);
//	}
	
	@RequestMapping(value = "mng", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<PagesRep<TradeOrderQueryRep>> mng(TradeOrderQueryReq req) {
		log.info(JSONObject.toJSONString(req));
		Direction sortDirection = Direction.DESC;
		if (!StringUtil.isEmpty(req.getOrder())) {
			sortDirection = Direction.ASC;
		}
		if(StringUtil.isEmpty(req.getSort())){
			req.setSort("createTime");
		}
		Pageable pageable = new PageRequest(req.getPage() - 1, req.getRows(), new Sort(new Order(sortDirection, req.getSort())));
		PagesRep<TradeOrderQueryRep> rep = this.investorTradeOrderService.mng(req, pageable);
		return new ResponseEntity<PagesRep<TradeOrderQueryRep>>(rep, HttpStatus.OK);
	}
	
//	@RequestMapping(value = "superredeem", method = RequestMethod.POST)
//	@ResponseBody
//	public ResponseEntity<BaseRep> superRedeem(@RequestBody @Valid TradeOrderReq tradeOrderReq) {
//		this.getLoginUser();
//		
//		tradeOrderReq.setIp(RemoteUtil.getRemoteAddr(request));
//		tradeOrderReq.setUid(this.investorBaseAccountService.getSuperInvestor().getUserOid());
//		BaseRep rep = this.investorInvestTradeOrderExtService.redeem(tradeOrderReq);
//		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
//	}
	
//	@RequestMapping(value = "refuse", method = { RequestMethod.POST, RequestMethod.POST })
//	public @ResponseBody ResponseEntity<BaseRep> refuse(@RequestBody List<String> tradeOrderList) {
//		for (String tradeOrderOid : tradeOrderList) {
//			investorAbandonTradeOrderService.refuse(tradeOrderOid);
//		}
//		BaseRep rep = new BaseRep();
//		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
//	}
	
	
	
	@RequestMapping(value = "refundpart", method = { RequestMethod.POST, RequestMethod.POST })
	public @ResponseBody ResponseEntity<BaseRep> refundPart(@RequestBody List<String> tradeOrderList) {
		
			investorTradeOrderService.refundPart(tradeOrderList);
		
		BaseRep rep = new BaseRep();
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	@RequestMapping(value = "refundall", method = { RequestMethod.POST, RequestMethod.POST })
	public @ResponseBody ResponseEntity<BaseRep> refundAll() {
		
			investorTradeOrderService.refundAll();
		
		BaseRep rep = new BaseRep();
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	//---------------冲销----2017.04.01-------------
	/**
	 * 查询所有产品和冲销状态(writeOff)的用户
	 */
	@RequestMapping(value = "pandu")
	public @ResponseBody ResponseEntity<WriteOffRep> pandu() {
		WriteOffRep rep = new WriteOffRep();
		List<WriteOffUser> users = new ArrayList<WriteOffUser>();
		List<WriteOffProduct> products = new ArrayList<WriteOffProduct>();
		List<InvestorBaseAccountEntity> accounts = investorTradeOrderService.findAccountByWriteOffStatus();
		List<Product> products2 = investorTradeOrderService.findAllProduct();
		for(InvestorBaseAccountEntity a : accounts){
			WriteOffUser u = new WriteOffUser();
			u.setUserId(a.getUserOid());
			u.setUserName(a.getRealName());
			users.add(u);
		}
		for(Product p : products2){
			WriteOffProduct wop = new WriteOffProduct();
			wop.setProductId(p.getOid());
			wop.setProductName(p.getFullName());
			products.add(wop);
		}
		rep.getWriteOffData().getUsers().addAll(users);
		rep.getWriteOffData().getProducts().addAll(products);
		return new ResponseEntity<WriteOffRep>(rep, HttpStatus.OK);
	}
	
	/**
	 * 根据产品id和用户id查询冲销金额
	 */
	@RequestMapping(value = "writeOffAmount")
	public @ResponseBody ResponseEntity<WriteOffAmount> writeOffAmount(@RequestParam String userId) {
		WriteOffAmount rep = new WriteOffAmount();
		InvestorSpecialRedeemEntity specalRedeem = investorSpecialRedeemService.findByUserId(userId);
		rep.setWriteOffAmount(specalRedeem.getLeftSpecialRedeemAmount());
		return new ResponseEntity<WriteOffAmount>(rep, HttpStatus.OK);
	}
	//---------------冲销----2017.04.01-------------
	/**
	 * 查询申购赎回明细
	 */
	@RequestMapping(value = "tradeOrderDetail")
	public @ResponseBody ResponseEntity<PagesRep<InvestOrRedeemDetailRep>> tradeOrderDetail(InvestOrRedeemDetailReq req) {
		log.info("查询申购赎回明细,{}",JSONObject.toJSONString(req));
		PagesRep<InvestOrRedeemDetailRep> accounts = investorTradeOrderService.tradeOrderDetail(req);
		return new ResponseEntity<PagesRep<InvestOrRedeemDetailRep>>(accounts, HttpStatus.OK);
	}
	//-------------------后台申购明细与赎回明细查询导出-------------
	@RequestMapping(value = "/tradeOrderDetaildown", method = {RequestMethod.POST,RequestMethod.GET})
	public void down(InvestOrRedeemDetailReq req) {
		try {
			String name = "交易明细导出.csv";
			List<List<String>> data =new ArrayList<List<String>>();
			data=	investorTradeOrderService.data(req);
			List<String> header = investorTradeOrderService.header();
//			String realPath=getClass().getResource("/").getFile().toString()+"../Temp/"; 
			String realPath="../Temp/";
			File f = new File(realPath);
			if(!f.exists()){
			f.mkdirs();
			} 
			File file = new File(f,name);
			if(!file.exists()){
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} 
			File FilePathName=new File(realPath+name);
			CsvExport.exportCsv(FilePathName,header, data);
			download(realPath+name,response);
			CsvExport.deleteFiles(realPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void download(String path, HttpServletResponse response) {
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
		   OutputStream toClient = new BufferedOutputStream(
		     response.getOutputStream());
		   response.setContentType("application/vnd.ms-excel;charset=UTF-8");
		   toClient.write(buffer);
		   toClient.flush();
		   toClient.close();
		  } catch (IOException ex) {
		   ex.printStackTrace();
		  }
	}

	/**
	 * 获取预约单详情列表
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "/bookingOrder")
	@ResponseBody
	public ResponseEntity<BookingOrderRep> bookingOrderList(@RequestBody BookingOrderReq req) {
		BookingOrderRep rep = investorTradeOrderService.getBookingOrderList(req);
		return new ResponseEntity<>(rep, HttpStatus.OK);
	}
}
