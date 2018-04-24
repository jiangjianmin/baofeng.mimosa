package com.guohuai.ams.product.dataTransmission;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.guohuai.ams.product.Product;

public interface ProductDataTransmissionDao extends JpaRepository<Product, String>, JpaSpecificationExecutor<Product> {
	/**
	 * 
	 * @author yihonglei
	 * @Title: getProductInfo
	 * @Description: 查询募集期结束，有投资成功记录并且募集结束日期小于当前系统时间的定期产品列表
	 * @param startTime
	 * @param endTime
	 * @param fullName
	 * @param pageRow
	 * @param row
	 * @return List<Object[]>
	 * @date 2017年5月23日 下午9:02:11
	 * @since  1.0.0
	 */
	@Query(value = " SELECT "
			+ " gp.oid oid, " // 产品id
			+ " gp.code code, " // 产品代码
			+ " gp.fullName fullName, " // 产品全称
			+ " gp.raisedTotalNumber raisedTotalNumber, " // 产品发行金额
			+ " gp.collectedVolume collectedVolume, " // 实际募集金额
			+ " gp.expAror expAror, " // 产品利率(eg:0.08)
			+ " gp.durationPeriodDays durationPeriodDays, "// 期限(天)--存续期天数
			+ " gp.raiseStartDate raiseStartDate, " // 募集开始日期
			+ " gp.raiseEndDate raiseEndDate, " // 募集结束日期
			+ " gp.durationPeriodEndDate durationPeriodEndDate, " // 到期日--存续期结束日期
			+ " DATE_ADD(gp.raiseEndDate,INTERVAL gp.interestsFirstDays DAY) interestsFirstDays " // 起息日--募集期结束后的多少日
			+ " FROM t_gam_product gp LEFT JOIN t_money_publisher_baseaccount mpb ON gp.spvOid = mpb.oid "
			+ " LEFT JOIN t_money_corporate mc ON mpb.corperateOid = mc.oid "
			+ " WHERE DATE_FORMAT(SYSDATE(),'%Y-%m-%d') > DATE_FORMAT(gp.raiseEndDate,'%Y-%m-%d') "
			+ " AND gp.state IN ('RAISEEND','DURATIONING','DURATIONEND','CLEARING','CLEARED') "
			+ " AND IF(?1='' or ?1 IS NULL,1=1,gp.raiseEndDate >= ?1) "
			+ " AND IF(?2='' or ?2 IS NULL,1=1,gp.raiseEndDate <= ?2) "
			+ " AND IF(?3='' or ?3 IS NULL,1=1,gp.fullName LIKE ?3) "
			+ " AND mc.name LIKE ?4 "
			+ " ORDER BY raiseEndDate DESC LIMIT ?5,?6",nativeQuery=true)
	public List<Object[]> getProductInfo(
			String startTime,String endTime,String fullName,String publisher,
			int pageRow, int row);
	
	@Query(value = " SELECT "
			+ " COUNT(1) "
			+ " FROM t_gam_product gp LEFT JOIN t_money_publisher_baseaccount mpb ON gp.spvOid = mpb.oid "
			+ " LEFT JOIN t_money_corporate mc ON mpb.corperateOid = mc.oid "
			+ " WHERE DATE_FORMAT(SYSDATE(),'%Y-%m-%d') > DATE_FORMAT(gp.raiseEndDate,'%Y-%m-%d') "
			+ " AND gp.state IN ('RAISEEND','DURATIONING','DURATIONEND','CLEARING','CLEARED') "
			+ " AND IF(?1='' or ?1 IS NULL,1=1,gp.raiseEndDate >= ?1) "
			+ " AND IF(?2='' or ?2 IS NULL,1=1,gp.raiseEndDate <= ?2) "
			+ " AND IF(?3='' or ?3 IS NULL,1=1,gp.fullName LIKE ?3) "
			+ " AND mc.name LIKE ?4 ",nativeQuery=true)
	public int getProductInfoCount(String startTime,String endTime,String fullName,String publisher);
	
	/**
	 * 
	 * @author yihonglei
	 * @Title: getProductInvestorInfo
	 * @Description:根据产品Id查询某个产品对应投资人投资信息列表
	 * @param productOid
	 * @param pageRow
	 * @param row
	 * @return List<Object[]>
	 * @date 2017年5月24日 上午12:35:01
	 * @since  1.0.0
	 */
	@Query(value = " SELECT "
			+ " mit.orderCode orderCode, " // 交易代码
			+ " mit.orderTime orderTime, " // 交易时间
			+ " gp.code code, " // 产品代码
			+ " mit.orderAmount orderAmount, " // 认购金额(元)
			+ " mib.realName realName, " // 客户姓名
			+ " mib.idNum idNum, " // 证件号码
			+ " mib.phoneNum phoneNum, " // 手机号
			+ " gp.expAror expAror " // 产品利率
			+ " FROM t_gam_product gp "
			+ " LEFT JOIN t_money_investor_tradeorder mit ON gp.oid = mit.productOid "
			+ " LEFT JOIN t_money_investor_baseaccount mib ON mit.investorOid = mib.oid "
			+ " WHERE gp.oid = ?1 AND mit.orderStatus = 'confirmed' "
			+ " ORDER BY mit.orderTime LIMIT ?2,?3 ",nativeQuery=true)
	public List<Object[]> getProductInvestorInfo(String productOid,int pageRow, int row);
	
	@Query(value = " SELECT "
			+ " COUNT(1) "
			+ " FROM t_gam_product gp "
			+ " LEFT JOIN t_money_investor_tradeorder mit ON gp.oid = mit.productOid "
			+ " LEFT JOIN t_money_investor_baseaccount mib ON mit.investorOid = mib.oid "
			+ " WHERE gp.oid = ?1 AND mit.orderStatus = 'confirmed' ",nativeQuery=true)
	public int getProductInvestorInfoCount(String productOid);
	
	
	@Query(value = " SELECT "
			+ " createTime, "
			+ " operateOid "
			+ " FROM t_gam_product_upload_record "
			+ " WHERE productOid = ?1 "
			+ " ORDER BY createTime DESC",nativeQuery=true)
	public List<Object[]> getProductDataTransRecord(String productOid);
	
	@Query(value = " SELECT "
			+ " COUNT(1) "
			+ " FROM t_gam_product_upload_record "
			+ " WHERE productOid = ?1 ",nativeQuery=true)
	public int getProductDataTransRecordCount(String productOid);
	
	// ***************************数据上传相关*************************************
	/**
	 * 
	 * @author yihonglei
	 * @Title: saveProductDataTransRecord
	 * @Description:保存传输记录
	 * @param productOid
	 * @param operateOid
	 * @return int
	 * @date 2017年5月25日 下午3:43:44
	 * @since  1.0.0
	 */
	@Modifying
	@Query(value = " INSERT INTO t_gam_product_upload_record (oid,productOid,operateOid,createTime) VALUES  "
			+ " (REPLACE(UUID(),'-',''),?1,?2,SYSDATE()) ",nativeQuery=true)
	public int saveProductDataTransRecord(String productOid,String operateOid);
	
	/**
	 * 
	 * @author yihonglei
	 * @Title: getProductDataTransRecordLastTime
	 * @Description:获取最后一次上传的时间
	 * @return String
	 * @date 2017年5月25日 上午12:43:41
	 * @since  1.0.0
	 */
	@Query(value = " SELECT "
			+ " MAX(DATE_FORMAT(createTime,'%Y-%m-%d')) "
			+ " FROM t_gam_product_upload_record  ",nativeQuery=true)
	public String getProductDataTransRecordLastTime();
	
	/**
	 * 
	 * @author yihonglei
	 * @Title: getBaseAssetCodes
	 * @Description:资产池对应的基础资产编号
	 * @param lastTime
	 * @return String[]
	 * @date 2017年5月25日 上午12:41:06
	 * @since  1.0.0
	 */
	@Query(value = " SELECT "
			+ "   ga.baseAssetCode baseAssetCode "
			+ " FROM t_gam_assetpool ga LEFT JOIN t_gam_product gp ON ga.oid = gp.assetPoolOid "
			+ " LEFT JOIN t_money_publisher_baseaccount mpb ON gp.spvOid = mpb.oid "
			+ " LEFT JOIN t_money_corporate mc ON mpb.corperateOid = mc.oid "
			+ " LEFT JOIN t_money_investor_tradeorder mit ON gp.oid = mit.productOid "
			+ " WHERE DATE_FORMAT(SYSDATE(),'%Y-%m-%d') > DATE_FORMAT(gp.raiseEndDate,'%Y-%m-%d') "
			+ " AND gp.state IN ('RAISEEND','DURATIONING','DURATIONEND','CLEARING','CLEARED') "
			+ " AND mit.orderStatus = 'confirmed' "
			+ " AND DATE_FORMAT(gp.raiseEndDate,'%Y-%m-%d') >= DATE_FORMAT(?1,'%Y-%m-%d') "
			+ " AND mc.name LIKE ?2 "
			+ " GROUP BY ga.baseAssetCode ",nativeQuery=true)
	public String[] getBaseAssetCodes(String lastUploadDate,String publisher);
	/**
	 * 
	 * @author yihonglei
	 * @Title: getRaiseEndDates
	 * @Description:某个基础资产编号下对应满足上传条件的所有产品募集结束时间
	 * @param lastTime
	 * @param baseAssetCode
	 * @return String[]
	 * @date 2017年5月25日 上午12:40:50
	 * @since  1.0.0
	 */
	@Query(value = " SELECT "
			+ "   gp.raiseEndDate raiseEndDate "
			+ " FROM t_gam_assetpool ga LEFT JOIN t_gam_product gp ON ga.oid = gp.assetPoolOid "
			+ " LEFT JOIN t_money_investor_tradeorder mit ON gp.oid = mit.productOid "
			+ " WHERE DATE_FORMAT(SYSDATE(),'%Y-%m-%d') > DATE_FORMAT(gp.raiseEndDate,'%Y-%m-%d') "
			+ " AND gp.state IN ('RAISEEND','DURATIONING','DURATIONEND','CLEARING','CLEARED') "
			+ " AND mit.orderStatus = 'confirmed' "
			+ " AND DATE_FORMAT(gp.raiseEndDate,'%Y-%m-%d') >= DATE_FORMAT(?1,'%Y-%m-%d') "
			+ " AND ga.baseAssetCode = ?2 "
			+ " GROUP BY gp.raiseEndDate "
			+ " ORDER BY gp.raiseEndDate ",nativeQuery=true)
	public String[] getRaiseEndDates(String lastUploadDate,String baseAssetCode);
	/**
	 * 
	 * @author yihonglei
	 * @Title: getProductUploadInfo
	 * @Description:根据基础资产编号和募集结束时间，查询出某个募集结束日对应的所有产品列表信息
	 * @param raiseEndDate
	 * @param baseAssetCode
	 * @return List<Object[]>
	 * @date 2017年5月25日 上午12:40:30
	 * @since  1.0.0
	 */
	@Query(value = " SELECT "
			+ " DISTINCT "
			+ " gp.oid oid, "
			+ " gp.code code, "
			+ " gp.fullName fullName, "
			+ " gp.raisedTotalNumber raisedTotalNumber, "
			+ " gp.collectedVolume collectedVolume, "
			+ " gp.expAror expAror, "
			+ " gp.durationPeriodDays durationPeriodDays, "
			+ " DATE_FORMAT(gp.raiseStartDate,'%Y%m%d') raiseStartDate, "
			+ " DATE_FORMAT(gp.raiseEndDate,'%Y%m%d') raiseEndDate, "
			+ " DATE_FORMAT(gp.durationPeriodEndDate,'%Y%m%d') durationPeriodEndDate, "
			+ " DATE_FORMAT(DATE_ADD(gp.raiseEndDate,INTERVAL gp.interestsFirstDays DAY),'%Y%m%d') interestsFirstDays, "
			+ " ga.baseAssetCode baseAssetCode "
			+ " FROM t_gam_assetpool ga LEFT JOIN t_gam_product gp ON ga.oid = gp.assetPoolOid "
			+ " LEFT JOIN t_money_investor_tradeorder mit ON gp.oid = mit.productOid "
			+ " WHERE DATE_FORMAT(SYSDATE(),'%Y-%m-%d') > DATE_FORMAT(gp.raiseEndDate,'%Y-%m-%d') "
			+ " AND gp.state IN ('RAISEEND','DURATIONING','DURATIONEND','CLEARING','CLEARED') "
			+ " AND mit.orderStatus = 'confirmed' "
			+ " AND gp.raiseEndDate = ?1 "
			+ " AND ga.baseAssetCode = ?2 "
			+ " ORDER BY gp.raiseEndDate ",nativeQuery=true)
	public List<Object[]> getProductUploadInfo(String raiseEndDate,String baseAssetCode);
	
	/**
	 * 
	 * @author yihonglei
	 * @Title: getProductUploadInvestorInfo
	 * @Description:根据产品Id获取上传时对应投资信息
	 * @param productOid
	 * @param pageRow
	 * @param row
	 * @return List<Object[]>
	 * @date 2017年5月24日 上午1:44:19
	 * @since  1.0.0
	 */
	@Query(value = " SELECT "
			+ " DISTINCT "
			+ " mit.orderCode orderCode, "
			+ " DATE_FORMAT(mit.orderTime,'%Y%m%d%H%i%s') orderTime, "
			+ " gp.code code, "
			+ " mit.orderAmount orderAmount, "
			+ " mib.realName realName, "
			+ " mib.idNum idNum, "
			+ " mib.phoneNum phoneNum, "
			+ " gp.expAror expAror, "
			+ " DATE_FORMAT(gp.raiseEndDate,'%Y%m%d') raiseEndDate "
			+ " FROM t_gam_product gp "
			+ " LEFT JOIN t_money_investor_tradeorder mit "
			+ " ON gp.oid = mit.productOid "
			+ " LEFT JOIN t_money_investor_baseaccount mib "
			+ " ON mit.investorOid = mib.oid "
			+ " WHERE gp.oid = ?1 AND mit.orderStatus = 'confirmed' "
			+ " ORDER BY mit.orderTime ",nativeQuery=true)
	public List<Object[]> getProductUploadInvestorInfo(String productOid);
	
}
