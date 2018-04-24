package com.guohuai.mmp.publisher.hold;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class MyTnClientRep<E> implements Serializable {
	private static int DEFAULT_PAGE_SIZE = 20; // 默认分页记录数
    private List<E> rows;// 数据记录List
    private int total; // 总记录条数
    private int page; // 当前页码
    private int row; // 单页记录条数
    private int totalPage; // 总页码数
    private String condition; // 分页条件
    private String orderby; // 排序值
    private int ascORdesc; // 升序降序 0 升序 1 降序
    private BigDecimal tnCapitalAmount = BigDecimal.ZERO; // 定期总资产
    private BigDecimal totalIncomeAmount = BigDecimal.ZERO; // 定期累计收益
    private BigDecimal totalExpectIncomeAmount = BigDecimal.ZERO; // 预期总收益
    
    /**
     * 
     * @author yihonglei
     * @CreateInstance: PageVo 
     * @Description: 构造器赋初始值
     * @date 2017年5月10日 下午6:04:54
     */
    public MyTnClientRep() {
    	total = -1;
    	totalPage = -1;
    	row = DEFAULT_PAGE_SIZE;
    	page = -1;
    }
    /**
     * 
     * @author yihonglei
     * @Title: reTotalPage
     * @Description:计算总页码数
     * @return void
     * @date 2017年5月13日 下午8:09:47
     * @since  1.0.0
     */
    public void reTotalPage() {
        if (row * (page - 1) > total) {
        	setPage((int) Math.ceil((double) total / (double) row));
        }
        if (row == 0) {
        	setRow(DEFAULT_PAGE_SIZE);
        }
        setTotalPage((int) Math.ceil((double) total / (double) row));
    }
}
