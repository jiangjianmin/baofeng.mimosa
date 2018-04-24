package com.guohuai.ams.duration.assetPool.chargefee;

import java.util.List;

import lombok.Data;

@Data
public class SaveAssetPoolFeeSettingForm {

	private String assetPoolOid;

	private List<SaveAssetPoolFeeSettingPojo> feeSettings;

	@Data
	public static class SaveAssetPoolFeeSettingPojo {

		private String startAmount;
		private String endAmount;
		private String feeRatio;

	}

}
