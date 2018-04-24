package com.guohuai.ams.duration.assetPool.chargefee;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.web.view.BaseResp;

@RestController
@RequestMapping(value = "/mimosa/duration/assetPool/fee/setting", produces = "application/json;charset=utf-8")
public class AssetPoolFeeSettingController extends BaseController {

	@Autowired
	private AssetPoolFeeSettingService assetPoolFeeSettingService;

	@RequestMapping(value = "save", name = "费金管理 - 新增费金规则", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseResp> save(@RequestBody SaveAssetPoolFeeSettingForm form) {
		this.assetPoolFeeSettingService.save(form);
		return new ResponseEntity<BaseResp>(new BaseResp(), HttpStatus.OK);
	}

	@RequestMapping(value = "load", name = "费金管理 - 获取费金列表", method = {RequestMethod.POST, RequestMethod.GET})
	@ResponseBody
	public ResponseEntity<List<AssetPoolFeeSettingResp>> load(@RequestParam String assetPoolOid) {
		List<AssetPoolFeeSettingResp> list = this.assetPoolFeeSettingService.load(assetPoolOid);
		return new ResponseEntity<List<AssetPoolFeeSettingResp>>(list, HttpStatus.OK);
	}

}
