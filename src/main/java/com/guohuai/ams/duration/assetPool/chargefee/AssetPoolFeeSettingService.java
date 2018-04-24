package com.guohuai.ams.duration.assetPool.chargefee;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.ams.duration.assetPool.AssetPoolEntity;
import com.guohuai.ams.duration.assetPool.AssetPoolService;
import com.guohuai.ams.duration.assetPool.chargefee.SaveAssetPoolFeeSettingForm.SaveAssetPoolFeeSettingPojo;
import com.guohuai.ams.duration.assetPool.history.HistoryValuationEntity;
import com.guohuai.ams.duration.assetPool.history.HistoryValuationService;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.StringUtil;

@Service
public class AssetPoolFeeSettingService {

	@Autowired
	private AssetPoolFeeSettingDao assetPoolFeeSettingDao;

	@Autowired
	private AssetPoolService assetPoolService;
	
	@Autowired
	private HistoryValuationService historyValuationService;

	@Transactional
	public List<AssetPoolFeeSetting> save(SaveAssetPoolFeeSettingForm form) {

		AssetPoolEntity pool = this.assetPoolService.getByOid(form.getAssetPoolOid());

		if (null == pool) {
			throw new AMPException("错误的资产池oid");
		}

		this.assetPoolFeeSettingDao.deleteByAssetPool(pool);

		List<AssetPoolFeeSetting> list = new ArrayList<AssetPoolFeeSetting>();

		if (null != form.getFeeSettings() && form.getFeeSettings().size() > 0) {
			for (SaveAssetPoolFeeSettingPojo set : form.getFeeSettings()) {
				AssetPoolFeeSetting s = new AssetPoolFeeSetting();
				s.setOid(StringUtil.uuid());
				s.setAssetPool(pool);
				s.setStartAmount(StringUtil.isEmpty(set.getStartAmount()) ? null : new BigDecimal(set.getStartAmount()).multiply(new BigDecimal("10000")));
				s.setEndAmount(StringUtil.isEmpty(set.getEndAmount()) ? null : new BigDecimal(set.getEndAmount()).multiply(new BigDecimal("10000")));
				s.setFeeRatio(new BigDecimal(set.getFeeRatio()).divide(new BigDecimal("100")));
				s = this.assetPoolFeeSettingDao.save(s);
				list.add(s);
			}
		}
		return list;
	}

	@Transactional
	public List<AssetPoolFeeSettingResp> load(String assetPoolOid) {
		List<AssetPoolFeeSettingResp> list = new ArrayList<AssetPoolFeeSettingResp>();
		List<AssetPoolFeeSetting> ss = this.assetPoolFeeSettingDao.findByAssetPoolOid(assetPoolOid);
		if (null != ss && ss.size() > 0) {
			for (AssetPoolFeeSetting s : ss) {
				list.add(new AssetPoolFeeSettingResp(s));
			}
		}
		return list;
	}
	
	@Transactional
	public BigDecimal feeCalac(String assetPoolOid, Date baseDate) {
		HistoryValuationEntity his = this.historyValuationService.findByAssetPoolAndDate(assetPoolOid, baseDate);
		if (null == his) {
			return BigDecimal.ZERO;
		}
		return his.getFeeValue();
	}

	@Transactional
	public BigDecimal feeCalc(String assetPoolOid, BigDecimal scale, int basic) {

		List<AssetPoolFeeSetting> settings = this.assetPoolFeeSettingDao.findByAssetPoolOid(assetPoolOid);
		// List<AssetPoolFeeSetting> settings = new ArrayList<AssetPoolFeeSetting>();
		// settings.add(new AssetPoolFeeSetting("", pool, new BigDecimal("0"), new BigDecimal("1000000"), new BigDecimal("0.01")));
		// settings.add(new AssetPoolFeeSetting("", pool, new BigDecimal("1000000"), new BigDecimal("1500000"), new BigDecimal("0.11")));
		// settings.add(new AssetPoolFeeSetting("", pool, new BigDecimal("1500000"), new BigDecimal("3000000"), new BigDecimal("0.21")));
		// settings.add(new AssetPoolFeeSetting("", pool, new BigDecimal("3000000"), new BigDecimal("4000000"), new BigDecimal("0.31")));
		// settings.add(new AssetPoolFeeSetting("", pool, new BigDecimal("4000000"), new BigDecimal("5000000"), new BigDecimal("0.36")));
		// settings.add(new AssetPoolFeeSetting("", pool, new BigDecimal("5000000"), null, new BigDecimal("0.01")));

		if (null == settings || settings.size() == 0) {
			return BigDecimal.ZERO;
		}

		if (scale.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO;
		}

		BigDecimal fee = BigDecimal.ZERO;

		for (AssetPoolFeeSetting set : settings) {
			BigDecimal base = BigDecimal.ZERO;
			if (null == set.getStartAmount() && null != set.getEndAmount()) {
				base = scale.compareTo(set.getEndAmount()) > 0 ? set.getEndAmount() : scale;
			}

			if (null != set.getStartAmount() && null != set.getEndAmount()) {
				BigDecimal wipe = scale.subtract(set.getStartAmount());
				if (wipe.compareTo(BigDecimal.ZERO) <= 0) {
					base = BigDecimal.ZERO;
				} else {
					BigDecimal subtract = set.getEndAmount().subtract(set.getStartAmount());
					base = wipe.compareTo(subtract) > 0 ? subtract : wipe;
				}
			}

			if (null != set.getStartAmount() && null == set.getEndAmount()) {
				BigDecimal wipe = scale.subtract(set.getStartAmount());
				base = wipe.compareTo(BigDecimal.ZERO) > 0 ? wipe : BigDecimal.ZERO;
			}

			BigDecimal charge = BigDecimal.ZERO;
			if (base.compareTo(BigDecimal.ZERO) > 0) {
				charge = base.multiply(set.getFeeRatio()).divide(new BigDecimal(basic), 4, RoundingMode.HALF_UP);
			}

			if (charge.compareTo(BigDecimal.ZERO) > 0) {
				fee = fee.add(charge);
			}

		}

		fee = fee.setScale(2, RoundingMode.HALF_UP);

		return fee;
	}

}
