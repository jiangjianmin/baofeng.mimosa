package com.guohuai.mmp.investor.tradeorder.cycleProduct;

import com.guohuai.cardvo.util.CardVoUtil;
import com.guohuai.component.util.StringUtil;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class BookingOrderEMDao {

    /**
     * 绑定jpa EntityManager
     */
    @PersistenceContext
    private EntityManager em;

    /**
     * 获取预约单列表
     *
     * @param req
     * @return
     */
    public List<Map<String, Object>> getBookingOrderList(BookingOrderReq req) {
        String sql = " SELECT " +
                "  a.phone as phone, " +
                "  b.orderCode as bookingOrderCode, " +
                "  b.orderAmount as bookingAmount, " +
                "  b.orderType as payType, " +
                "  b.orderStatus as bookingStatus, " +
                "  b.orderTime as bookingTime, " +
                "  a.investProductName as productName, " +
                "  a.investOrderCode as orderCode, " +
                "  a.investAmount as orderAmount " +
                " FROM t_money_investor_opencycle_tradeorder_relation a, t_money_investor_tradeorder b " +
                " WHERE a.sourceOrderCode = b.orderCode AND a.orderType = 'booking' ";
        Map<String, Object> params = new HashMap<>();
        if (!StringUtil.isEmpty(req.getPayType())) {
            sql += " AND a.orderType = :orderType ";
            params.put("orderType", req.getPayType());
        }
        if (!StringUtil.isEmpty(req.getOrderStatus())) {
            sql += " AND b.orderStatus = :orderStatus ";
            params.put("orderStatus", req.getOrderStatus());
        }
        if (!StringUtil.isEmpty(req.getOrderTimeBegin()) && !StringUtil.isEmpty(req.getOrderTimeEnd())) {
            sql += " AND b.orderTime BETWEEN str_to_date(:startTime, '%Y-%m-%d %H:%i:%s') AND str_to_date(:endTime, '%Y-%m-%d %H:%i:%s') ";
            params.put("startTime", req.getOrderTimeBegin());
            params.put("endTime", req.getOrderTimeEnd());
        }
        if (!StringUtil.isEmpty(req.getProductName())) {
            sql += " AND a.investProductName like :productName";
            params.put("productName", '%' + req.getProductName() + '%');
        }
        if (!StringUtil.isEmpty(req.getPhone())) {
            sql += " AND a.phone like :phone";
            params.put("phone", '%' + req.getPhone() + '%');
        }
        sql += " ORDER BY b.orderTime DESC ";
        sql += " LIMIT " + (req.getPage() - 1) * req.getSize() + ", " + req.getSize();

        Query emQuery = em.createNativeQuery(sql);
        params.forEach(emQuery::setParameter);

        return CardVoUtil.query2Map(emQuery);
    }

    /**
     * 获取预约单列表总数
     *
     * @param req
     * @return
     */
    public int getBookingOrderListCount(BookingOrderReq req) {
        String sql = " SELECT count(1) " +
                " FROM t_money_investor_opencycle_tradeorder_relation a, t_money_investor_tradeorder b " +
                " WHERE a.sourceOrderCode = b.orderCode AND a.orderType = 'booking' ";
        Map<String, Object> params = new HashMap<>();
        if (!StringUtil.isEmpty(req.getPayType())) {
            sql += " AND a.orderType = :orderType ";
            params.put("orderType", req.getPayType());
        }
        if (!StringUtil.isEmpty(req.getOrderStatus())) {
            sql += " AND b.orderStatus = :orderStatus ";
            params.put("orderStatus", req.getOrderStatus());
        }
        if (!StringUtil.isEmpty(req.getOrderTimeBegin()) && !StringUtil.isEmpty(req.getOrderTimeEnd())) {
            sql += " AND b.orderTime BETWEEN str_to_date(:startTime, '%Y-%m-%d %H:%i:%s') AND str_to_date(:endTime, '%Y-%m-%d %H:%i:%s') ";
            params.put("startTime", req.getOrderTimeBegin());
            params.put("endTime", req.getOrderTimeEnd());
        }
        if (!StringUtil.isEmpty(req.getProductName())) {
            sql += " AND a.investProductName like :productName";
            params.put("productName", '%' + req.getProductName() + '%');
        }
        if (!StringUtil.isEmpty(req.getPhone())) {
            sql += " AND a.phone like :phone";
            params.put("phone", '%' + req.getPhone() + '%');
        }

        Query emQuery = em.createNativeQuery(sql);
        params.forEach(emQuery::setParameter);

        return CardVoUtil.countNum(emQuery).intValue();
    }
}
