package com.guohuai.ams.activityModel.ttzActivity;

import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.web.view.BaseRep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping(value = "/mimosa/client/activity/ttzInfo", produces = "application/json")
public class TtzActivityClientController extends BaseController{

    @Autowired
    private TtzActivityService ttzActivityService;

    @Autowired
    protected HttpSession session;

    @RequestMapping(value = "/model", method = { RequestMethod.GET,RequestMethod.POST })
    public ResponseEntity<TtzActivityRep> model() {
        return new ResponseEntity<>(ttzActivityService.model(), HttpStatus.OK);
    }

    @RequestMapping(value = "/products", method = { RequestMethod.GET,RequestMethod.POST })
    public ResponseEntity<TtzActivityProductListRep> products(@RequestParam String channelOid) {
        return new ResponseEntity<>(ttzActivityService.findProductsByGeneralTagAndChannel(channelOid), HttpStatus.OK);
    }

    @RequestMapping(value = "/investmentData", method = { RequestMethod.GET,RequestMethod.POST })
    public ResponseEntity<TtzActivityInvestmentDataRep> data() {
        String uid = null;
        if (session.getAttribute(UID) != null) {
            uid = session.getAttribute(UID).toString();
        }
        return new ResponseEntity<>(ttzActivityService.getInvestmentData(uid), HttpStatus.OK);
    }

    @RequestMapping(value = "/shareCount", method = { RequestMethod.GET,RequestMethod.POST })
    public ResponseEntity<BaseRep> shareCount() {
        this.getLoginUser();
        return new ResponseEntity<>(ttzActivityService.shareCount(), HttpStatus.OK);
    }

}
