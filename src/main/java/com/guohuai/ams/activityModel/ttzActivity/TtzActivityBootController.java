package com.guohuai.ams.activityModel.ttzActivity;

import com.guohuai.component.web.view.BaseRep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/mimosa/boot/activity/ttzInfo", produces = "application/json")
public class TtzActivityBootController {

    @Autowired
    private TtzActivityService ttzActivityService;

    @RequestMapping(value = "/save", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<BaseRep> save(@Valid @RequestBody TtzActivityForm form) {
        BaseRep rep = ttzActivityService.save(form);
        return new ResponseEntity<>(rep, HttpStatus.OK);
    }

    @RequestMapping(value = "/query", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<TtzActivityForm> query() {
        return new ResponseEntity<>(ttzActivityService.bootQuery(), HttpStatus.OK);
    }

}
