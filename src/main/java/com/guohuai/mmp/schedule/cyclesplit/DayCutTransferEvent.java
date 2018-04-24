package com.guohuai.mmp.schedule.cyclesplit;

import com.guohuai.ams.product.Product;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 日切事件传输对象
 *
 * @author yujianlong
 * @create 2018-03-28 20:12
 **/
public class DayCutTransferEvent {
	private int page;
	BigDecimal availableAmount;
	private Product product;
	private List<String> holdOids;
	private ConcurrentLinkedDeque<BigDecimal> sumAmount;
	private Map<String, String> tmpMap = new HashMap<>();

	public DayCutTransferEvent() {

	}

	public DayCutTransferEvent(int page, BigDecimal availableAmount, Product product, List<String> holdOids, ConcurrentLinkedDeque<BigDecimal> sumAmount) {
		this.page = page;
		this.availableAmount = availableAmount;
		this.product = product;
		this.holdOids = holdOids;
		this.sumAmount = sumAmount;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public BigDecimal getAvailableAmount() {
		return availableAmount;
	}

	public void setAvailableAmount(BigDecimal availableAmount) {
		this.availableAmount = availableAmount;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public List<String> getHoldOids() {
		return holdOids;
	}

	public void setHoldOids(List<String> holdOids) {
		this.holdOids = holdOids;
	}

	public ConcurrentLinkedDeque<BigDecimal> getSumAmount() {
		return sumAmount;
	}

	public void setSumAmount(ConcurrentLinkedDeque<BigDecimal> sumAmount) {
		this.sumAmount = sumAmount;
	}


	public Map<String, String> getTmpMap() {
		return tmpMap;
	}

	public void setTmpMap(Map<String, String> tmpMap) {
		this.tmpMap = tmpMap;
	}

	@Override
	public String toString() {
		return "DayCutTransferEvent{" +
				"page=" + page +
				", availableAmount=" + availableAmount +
				", product=" + product +
				", holdOids=" + holdOids +
				'}';
	}
}
