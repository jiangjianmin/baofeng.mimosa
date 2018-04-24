package com.guohuai.mmp.platform.tulip;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.web.view.PagesRep;
import com.guohuai.mmp.tulip.rep.MyAllCouponRep;
import com.guohuai.mmp.tulip.rep.MyCouponOfProRep;
import com.guohuai.mmp.tulip.rep.OneCouponInfoRep;
import com.guohuai.tuip.api.objs.admin.EventRep;
@RestController
@RequestMapping(value = "/mimosa/client/tulip", produces = "application/json")
public class TulipClientController extends BaseController {
    @Autowired
    private TulipService tulipNewService;
    /**
     * 我的所有卡券
     * 
     * @param status:(可送空)卡券状态(卡券状态notUsed未使用;used已使用;expired过期)
     * @param type:(可送空)卡券类型(redPackets红包;coupon优惠券;3折扣券;体验金tasteCoupon;加息券rateCoupon)
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping(value = "myallcoupon", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<PagesRep<MyAllCouponRep>> myAllCoupon(@RequestParam(required = false) String status,
            @RequestParam(required = false) String type, @RequestParam int page, @RequestParam int rows) {
        String userOid = this.getLoginUser();
        return new ResponseEntity<PagesRep<MyAllCouponRep>>(
                this.tulipNewService.getMyAllCouponList(userOid, status, type, page, rows), HttpStatus.OK);
    }
    /**
     * 我的可购买某产品的卡券列表
     * 
     * @param proOid：产品oid
     * @return
     */
    @RequestMapping(value = "mycouponofpro", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<PagesRep<MyCouponOfProRep>> myCouponOfPro(@RequestParam String proOid) {
        String userOid = this.getLoginUser();
        return new ResponseEntity<PagesRep<MyCouponOfProRep>>(this.tulipNewService.getMyCouponListOfPro(userOid, proOid),
                HttpStatus.OK);
    }
    /**
     * 卡券金额
     * 
     * @param couponId:卡券编码
     * @return
     */
    @RequestMapping(value = "coupondetail", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<OneCouponInfoRep> getCouponAmount(@RequestParam(required = false) String userOid,
            @RequestParam String couponId) {
        if (StringUtils.isEmpty(userOid)) {
            userOid = this.getLoginUser();
        }
        return new ResponseEntity<OneCouponInfoRep>(this.tulipNewService.getCouponDetail(couponId), HttpStatus.OK);
    }
    
    /**
     * 获取推荐人活动
     * @return
     */
    @RequestMapping(value = "getFriendEventInfo", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<EventRep> getFriendEventInfo() {
        // this.getLoginUser();// 根据需求，将登录注掉
        return new ResponseEntity<EventRep>(this.tulipNewService.getFriendEventInfo(), HttpStatus.OK);
    }
    /**
     * 获取注册活动
     * @return
     */
    @RequestMapping(value = "getRegisterEventInfo", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<EventRep> getRegisterEventInfo() {
        return new ResponseEntity<EventRep>(this.tulipNewService.getRegisterEventInfo(), HttpStatus.OK);
    }
}