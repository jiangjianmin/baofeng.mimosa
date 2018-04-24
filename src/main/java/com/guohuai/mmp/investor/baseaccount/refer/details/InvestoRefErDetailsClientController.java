package com.guohuai.mmp.investor.baseaccount.refer.details;

import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.basic.messageBody.annotations.SerializedField;
import com.guohuai.component.web.view.PagesRep;
import com.guohuai.plugin.PageVo;

/**
 * 我的推荐列表查询
 * 
 * @author wanglei
 */
@RestController
@RequestMapping(value = "/mimosa/client/investor/baseaccount/referdetail", produces = "application/json")
public class InvestoRefErDetailsClientController extends BaseController {
	
	
	@Autowired
	InvestoRefErDetailsService investoRefErDetailsService;

	@RequestMapping(value = "referlist", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<PagesRep<InvestoRefErDetailsRep>> referlist(@RequestParam int page, @RequestParam int rows) {

		final String uid = this.getLoginUser();
		page = page < 1 ? 1 : page;
		rows = rows < 1 ? 1 : rows;

		Specification<InvestoRefErDetailsEntity> spec = new Specification<InvestoRefErDetailsEntity>() {
			@Override
			public Predicate toPredicate(Root<InvestoRefErDetailsEntity> root, CriteriaQuery<?> query,
					CriteriaBuilder cb) {
				return cb.and(cb
						.equal(root.get("investorRefEree").get("investorBaseAccount").get("userOid").as(String.class), uid)); // 投资者ID
			}
		};
		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(Direction.DESC, "createTime")));
		PagesRep<InvestoRefErDetailsRep> pages = this.investoRefErDetailsService.referlist(spec,pageable);
		return new ResponseEntity<PagesRep<InvestoRefErDetailsRep>>(pages, HttpStatus.OK);
	}
	
	/**
	 * 推荐排名统计，前10名
	 * @return
	 */
	@RequestMapping(value = "recomtop10", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<PagesRep<InvestoRefErDetailsRankRep>> recommendRankTOP10() {
		
		PagesRep<InvestoRefErDetailsRankRep> pages = this.investoRefErDetailsService.recommendRankTOP10();
		
		return new ResponseEntity<PagesRep<InvestoRefErDetailsRankRep>>(pages, HttpStatus.OK);
	}
	
	/**
	 * 推荐人统计（注册，绑卡分开统计）--app
	 * @return
	 */
	@RequestMapping(value = "recominfo", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<InvestoRefErDetailsCountRep> recommendCountInfo() {
		String userOid = super.getLoginUser();
		InvestoRefErDetailsCountRep rep = this.investoRefErDetailsService.recommendCountInfo(userOid);
		
		return new ResponseEntity<InvestoRefErDetailsCountRep>(rep, HttpStatus.OK);
	}
	/**
	 * 
	 * @author yihonglei
	 * @Title: queryRecommendInfo
	 * @Description:查询推荐人 列表--pc
	 * @param req
	 * @return ResponseEntity<PageVo<Map<String,Object>>>
	 * @date 2017年5月31日 下午7:04:22
	 * @since  1.0.0
	 */
	@RequestMapping(value = "queryRecommendInfo", method = {RequestMethod.POST, RequestMethod.GET})
	@ResponseBody
	public ResponseEntity<InvestoRefErDetailsVoRep<Map<String,Object>>> queryRecommendInfo(@Valid @RequestBody InvestoRefErDetailsReq req) {
		String userOid = super.getLoginUser();
		
		InvestoRefErDetailsVoRep<Map<String,Object>> pageVo = this.investoRefErDetailsService.queryRecommendInfo(userOid,req);
		
		return new ResponseEntity<InvestoRefErDetailsVoRep<Map<String,Object>>>(pageVo, HttpStatus.OK);
	}
	
	/**
	 * 
	 * @author yihonglei
	 * @Title: queryFriendInvestRecord
	 * @Description: 查询好友投资列表
	 * @param req
	 * @return ResponseEntity<PageVo<Map<String,Object>>>
	 * @date 2017年6月16日 下午7:04:50
	 * @since  1.0.0
	 */
	@RequestMapping(value = "queryFriendInvestRecord", method = {RequestMethod.POST, RequestMethod.GET})
	@ResponseBody
	public ResponseEntity<PageVo<Map<String,Object>>> queryFriendInvestRecord(@Valid @RequestBody InvestoRefErDetailsReq req) {
		String userOid = super.getLoginUser();
		
		PageVo<Map<String,Object>> pageVo = this.investoRefErDetailsService.queryFriendInvestRecord(userOid,req);
		
		return new ResponseEntity<PageVo<Map<String,Object>>>(pageVo, HttpStatus.OK);
	}
	
	@RequestMapping(value = "myReferList",  method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<PagesRep<InvestoRefErDetailsRankRep>> myReferList(@RequestParam String investorOid,@RequestParam int page, @RequestParam int rows) {
//		final String uid = this.getLoginUser();
		page = page < 1 ? 1 : page;
		rows = rows < 1 ? 10 : rows;
		PagesRep<InvestoRefErDetailsRankRep> pages = this.investoRefErDetailsService.myReferList(investorOid,page,rows);
		return new ResponseEntity<PagesRep<InvestoRefErDetailsRankRep>>(pages, HttpStatus.OK);
	}
}
