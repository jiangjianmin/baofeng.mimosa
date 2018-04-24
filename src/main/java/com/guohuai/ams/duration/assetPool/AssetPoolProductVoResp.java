package com.guohuai.ams.duration.assetPool;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class AssetPoolProductVoResp {
	
	private List<AssetPoolProductVo> rows = new ArrayList<>();
	private int page = 0;
	private int row = 0;
	private int totalPage = 0;
	private long total = 0;
	private BigDecimal totalCollectedVolume;
	private BigDecimal totalNoPayInvest;
	private BigDecimal totalPayInvest;
	private BigDecimal totalCash;
	private BigDecimal totalInvest;
	private BigDecimal totalIncome;

}
