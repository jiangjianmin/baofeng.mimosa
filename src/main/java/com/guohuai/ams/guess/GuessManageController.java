package com.guohuai.ams.guess;

import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.component.web.view.BaseResp;
import com.guohuai.component.web.view.PagesRep;
import com.guohuai.mmp.publisher.hold.MyTnClientRep;
import com.guohuai.mmp.publisher.hold.MyTnClientReq;

import lombok.extern.slf4j.Slf4j;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;

/**竞猜活动管理（后端端接口）
 * @author qiuliang
 *
 */
@RestController
@RequestMapping(value = "/mimosa/boot/guess", produces = "application/json;charset=UTF-8")
@Slf4j
public class GuessManageController  extends BaseController{
	
	@Autowired
	private GuessService guessService;
	
	
	
	@RequestMapping(name = "新建竞猜活动", value = "create", method = RequestMethod.POST)
	public ResponseEntity<BaseResp> save(@Valid @RequestBody GuessForm guessForm) {
		log.info("新建竞猜活动入参：{}",guessForm);
		String loginName =  super.getLoginUserName();
		BaseResp response = guessService.save(guessForm,loginName);
		return new ResponseEntity<BaseResp>(response, HttpStatus.OK);
	}
	
	@RequestMapping(name = "竞猜活动列表", value = "/query")
	public ResponseEntity<PagesRep<GuessQueryRep>> query(HttpServletRequest request,
		@And({
				@Spec(params = "guessName", path = "guessName", spec = Like.class),
				@Spec(params = "status", path = "status", spec = Equal.class)
				}) 
        Specification<GuessEntity> spec,
 		@RequestParam int page, 
		@RequestParam int rows) {
		Specification<GuessEntity> delFlagSpec = new Specification<GuessEntity>() {
			
			@Override
			public Predicate toPredicate(Root<GuessEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				
				return cb.equal(root.get("delFlag"), GuessEntity.GUESS_UNDEL);
			}
		};
		spec = Specifications.where(spec).and(delFlagSpec);
		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(Direction.DESC, "createTime")));		
		PagesRep<GuessQueryRep> rep = this.guessService.query(spec, pageable);
		return new ResponseEntity<PagesRep<GuessQueryRep>>(rep, HttpStatus.OK);
	}
	/**
	 * 
	 * @author yihonglei
	 * @Title: tnHoldList
	 * @Description: 我的定期--1.6.0 版本新增加方法
	 * 1. 旧版本方法中我的定期是按持有手册维度显示
	 * 2. 新版本方法中修改为按照定期订单单条展示
	 * @param req
	 * @return ResponseEntity<MyTnClientRep<Map<String,Object>>>
	 * @date 2017年9月7日 下午3:48:33
	 * @since  1.0.0
	 */
	@RequestMapping(value = "guessRecordList", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<GuessRecordResp<Map<String,Object>>> guessRecordList(@RequestParam(required = false) String guessName,@RequestParam(required = false) String productName,
			@RequestParam(required = false) String realName,@RequestParam(required = false) String orderStatus,
			@RequestParam(required = false) String phoneNum,@RequestParam(required = false) Integer page,@RequestParam(required = false) Integer row, HttpServletRequest request) {
		GuessRecordResp<Map<String,Object>> res = this.guessService.guessRecordList(guessName,productName,
				realName,orderStatus,
				phoneNum,page,row);
		return new ResponseEntity<GuessRecordResp<Map<String,Object>>>(res, HttpStatus.OK);
	}
	@RequestMapping(name = "修改竞猜活动", value = "/update")
	public ResponseEntity<BaseResp> update(@Valid @RequestBody GuessForm guessForm) {
		String loginName =  super.getLoginUserName();
		BaseResp rep = this.guessService.update(guessForm,loginName);
		return new ResponseEntity<BaseResp>(rep, HttpStatus.OK);
	}
	
	@RequestMapping(name = "修改时带出竞猜活动", value = "/see")
	public ResponseEntity<GuessSeeRep> see(@RequestParam String oid) {
		GuessSeeRep rep = this.guessService.see(oid);
		return new ResponseEntity<GuessSeeRep>(rep, HttpStatus.OK);
	}
	
	@RequestMapping(name = "查看竞猜活动", value = "/detail")
	public ResponseEntity<GuessDetailRep> detail(@RequestParam String oid) {
		GuessDetailRep rep = this.guessService.detail(oid);
		return new ResponseEntity<GuessDetailRep>(rep, HttpStatus.OK);
	}
	
	@RequestMapping(name = "删除竞猜活动", value = "/delete")
	public ResponseEntity<BaseResp> delete(@RequestParam String oid) {
		String loginName =  super.getLoginUserName();
		BaseResp rep = this.guessService.del(oid,loginName);
		return new ResponseEntity<BaseResp>(rep, HttpStatus.OK);
	}
	
	@RequestMapping(name = "删除竞猜活动选项", value = "/delItem")
	public ResponseEntity<BaseResp> delItem(@RequestParam String itemOid) {
		BaseResp rep = this.guessService.delItem(itemOid);
		return new ResponseEntity<BaseResp>(rep, HttpStatus.OK);
	}
	
	@RequestMapping(name = "开奖前查询竞猜活动选项", value = "/item")
	public ResponseEntity<GuessItemRep> item(@RequestParam String oid) {
		GuessItemRep rep = this.guessService.item(oid);
		return new ResponseEntity<GuessItemRep>(rep, HttpStatus.OK);
	}
	
	@RequestMapping(name = "开奖", value = "/lottery")
	public ResponseEntity<BaseRep> lottery(@Valid @RequestBody LotteryReq req) {
		BaseRep rep = this.guessService.lottery(req);
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	@RequestMapping(name = "查询已保存和已解锁状态的竞猜活动名称", value = "/guessNameList")
	public ResponseEntity<GuessNameRep> guessNameList() {
		GuessNameRep rep = this.guessService.guessNameList(GuessEntity.GUESS_STATUS_CREATED);
		return new ResponseEntity<GuessNameRep>(rep, HttpStatus.OK);
	}
	
	@RequestMapping(name = "查询所有的竞猜活动名称", value = "/guessNameAllList")
	public ResponseEntity<GuessNameRep> guessNameAllList() {
		GuessNameRep rep = this.guessService.guessNameAllList();
		return new ResponseEntity<GuessNameRep>(rep, HttpStatus.OK);
	}
	

}
