package com.guohuai.ams.guess;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductDao;
import com.guohuai.ams.product.ProductDecimalFormat;
import com.guohuai.ams.product.ProductService;
import com.guohuai.ams.productPackage.ProductPackage;
import com.guohuai.ams.productPackage.ProductPackageDao;
import com.guohuai.basic.component.exception.GHException;
import com.guohuai.cache.service.CacheChannelService;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.DecimalUtil;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.component.web.view.BaseResp;
import com.guohuai.component.web.view.PagesRep;
import com.guohuai.guess.GuessDetailForeRep;
import com.guohuai.guess.GuessListRep;
import com.guohuai.guess.GuessRep;
import com.guohuai.guess.ItemRep;
import com.guohuai.guess.MyGuess;
import com.guohuai.guess.MyGuessRep;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderDao;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GuessService {
	
	@Autowired
	private GuessDao guessDao;
	
	@Autowired
	private GuessItemDao guessItemDao;
	
	@Autowired
	private ProductDao productDao;
	
	@Autowired
	private GuessInvestItemDao guessInvestItemDao;
	
//	@Autowired
//	private ProductPackageService productPackageService;
	
	@Autowired
	private ProductPackageDao productPackageDao;
	
	@Autowired
	private ProductService productService;
	
	@Autowired
	private CacheChannelService cacheChannelService;
	
	@Autowired
	private InvestorTradeOrderDao investorTradeOrderDao;
	
	@Transactional
	public BaseResp save(GuessForm guessForm, String loginName) {
		BaseResp rep = new BaseResp();
		GuessEntity guessEntity = GuessEntity.builder()
				.content(guessForm.getContent())
				.guessName(guessForm.getGuessName())
				.guessTitle(guessForm.getGuessTitle())
				.imgPath(guessForm.getImgPath())
				.question(guessForm.getQuestion())
				.remark(guessForm.getRemark())
				.status(GuessEntity.GUESS_STATUS_CREATED)
				.delFlag(GuessEntity.GUESS_UNDEL)
				.createPerson(loginName)
				.createTime(new Date())
				.build();
		GuessEntity saved = guessDao.save(guessEntity);
		List<GuessItemEntity> guessItems = new ArrayList<GuessItemEntity>();
		for(String itemContent : guessForm.getItemContents()){
			GuessItemEntity guessItem = GuessItemEntity.builder()
					.content(itemContent)
					.createTime(new Date())
					.createPerson(loginName)
					.guess(saved)
					.build();
			guessItems.add(guessItem);
		}
		guessItemDao.save(guessItems);
		return rep;
	}

	public PagesRep<GuessQueryRep> query(Specification<GuessEntity> spec, Pageable pageable) {
		PagesRep<GuessQueryRep> pagesRep = new PagesRep<GuessQueryRep>();
		Page<GuessEntity> pages = guessDao.findAll(spec, pageable);
		List<GuessQueryRep> rows = new ArrayList<GuessQueryRep>();
		if (pages != null && pages.getContent() != null && pages.getTotalElements() > 0) {
			for(GuessEntity guess : pages){
				String oid = guess.getOid();
				String guessName =  guess.getGuessName();
				Integer status = guess.getStatus();
				Timestamp createTime = new Timestamp(guess.getCreateTime().getTime());
				String createPerson = guess.getCreatePerson();
				Integer isAllRaiseFail=this.isAllRaiseFailAndHasInterest(guess,"raiseFail");//产品包下的产品是否全部流标（1：是，0：否）
				Integer hasInterest=this.isAllRaiseFailAndHasInterest(guess,"hasInterest");//产品包下的产品是否有派息的（1：是，0：否）
				String title = guess.getGuessTitle();
				String imgPath = guess.getImgPath();
				String content = guess.getContent();
				GuessQueryRep page = new GuessQueryRep(oid,guessName,status,createTime,createPerson,isAllRaiseFail,hasInterest,title,imgPath,content);
				rows.add(page);
			}
			pagesRep.setRows(rows);
		}
		pagesRep.setTotal(pages.getTotalElements());
		return pagesRep;
	}
	/**
	 * 
	 * @author wangqiandi
	 * @Title: guessRecordList
	 * @Description: 竞猜记录
	 * @param req
	 * @return GuessRecordResp<Map<String,Object>>
	 * @date 2017年11月8日 下午2:17:45
	 * @since  1.0.0
	 */
	public GuessRecordResp<Map<String,Object>> guessRecordList(String guessName,String productName,
			String realName,String orderStatus,
			String phoneNum,int page,int row) {
		// 客户端请求响应结果
		GuessRecordResp<Map<String,Object>> rep = new GuessRecordResp<>();
		List<Map<String,Object>> guessRecordList = new ArrayList<Map<String,Object>>();
		// 竞猜记录列表查询
		List<Object[]> objGuessRecordList = this.guessDao.getGuessRecordList(
				guessName, productName, realName,phoneNum,orderStatus,
				(page - 1) * row, row);
		// 竞猜记录总条数查询
		int total = this.guessDao.getGuessRecordCount(guessName, productName, realName,phoneNum,orderStatus);
		// 1. 汇总数据处理(定期总资产,定期累计收益,定期预期收益)
		if (objGuessRecordList.size() > 0) {
			// 2. 定期列表数据处理
			guessRecordList = toObj2MapList(objGuessRecordList);
		}
		// 分页数据处理
		rep.setRows(guessRecordList);
		rep.setTotal(total);
		rep.setRow(row);
		rep.setPage(page);
		rep.reTotalPage();
		
		return rep;
	}
	private List<Map<String,Object>> toObj2MapList(List<Object[]> objGuessRecordList) {
		List<Map<String,Object>> guessRecordList = new ArrayList<Map<String,Object>>();
		try {
			objGuessRecordList.stream().forEach(guessRecord->{
				Map<String,Object> mapData = new HashMap<String,Object>();
				Object[] objArr = guessRecord;
				BigDecimal orderAmount = ObjectUtils.isEmpty(objArr[1])?new BigDecimal(0):new BigDecimal(objArr[1].toString());
				BigDecimal percent = ObjectUtils.isEmpty(objArr[8])?new BigDecimal(0):new BigDecimal(objArr[8].toString());
				BigDecimal guessIncome = ObjectUtils.isEmpty(objArr[9])?new BigDecimal(0):new BigDecimal(objArr[9].toString());;
				String guessIncomeStr;
				String percentStr;
				int m = guessIncome.compareTo(BigDecimal.ZERO);
				if(m == 0) {
					guessIncomeStr = "——";
				}else if(m == 1) {
					guessIncomeStr = "+"+ProductDecimalFormat.format(guessIncome);
				}else {
					guessIncomeStr = ""+ProductDecimalFormat.format(guessIncome);
				}
				//转换成前端需要的展示形式
				if(percent.signum()==0) {
					percentStr = "按照基础收益率";
				}else if(percent.signum()==1){
					percentStr = "加息"+ProductDecimalFormat.format(ProductDecimalFormat.multiply(percent))+"%";
				}else {
					percentStr = "减息"+ProductDecimalFormat.format(ProductDecimalFormat.multiply(percent).abs())+"%";
				}
				mapData.put("orderCode", objArr[0]); // 订单Oid
				mapData.put("orderAmount", ProductDecimalFormat.format(orderAmount)); // 订单金额
				mapData.put("orderStatus", objArr[2]); // 订单状态
				mapData.put("orderTime", objArr[3]); // 订单时间
				mapData.put("productName", objArr[4]); // 产品状态
				mapData.put("guessName", objArr[5]); // 竞猜标题
				mapData.put("guessItemContent", objArr[6]); // 竞猜选项答案
				mapData.put("openDate", objArr[7]); // 预期开奖日期
				mapData.put("percent", percentStr); // 开奖结果
				mapData.put("guessIncomeStr", guessIncomeStr); // 竞猜收益
				mapData.put("userOid", objArr[10]); // 用户id
				mapData.put("realName", objArr[11]); // 真是姓名
				mapData.put("phoneNum", objArr[12]); // 电话号码
				guessRecordList.add(mapData);
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return guessRecordList;
	}
	
	/**判断产品包下的产品是否全部流标和是否有派息的
	 * @param guess
	 * @param flag
	 * @return
	 */
	private Integer isAllRaiseFailAndHasInterest(GuessEntity guess, String flag) {
		
		Integer isAllRaiseFail=1;//产品包下的产品是否全部流标（1：是，0：否）
		Integer hasInterest=0;//产品包下的产品是否有派息的（1：是，0：否）
		ProductPackage productPackage = this.productPackageDao.findByGuessOid(guess.getOid());
		List<Product> products = this.productService.getProductByProductPackage(productPackage);
		//repayInterestStatus：付息状态,state：产品状态
		for(Product p:products){
			String state = p.getState();
			String repayInterestStatus = p.getRepayInterestStatus();
			// 查询产品有无募集失败的订单cashFailed（派息状态为repayed并且无cashFailed的订单说明是正常的派息）
			List<InvestorTradeOrderEntity> cashFailedOrderList = investorTradeOrderDao.getCashFailedOrderByProductOid(p.getOid());
			if(Product.PRODUCT_repayInterestStatus_repayed.equals(repayInterestStatus) && cashFailedOrderList.size() == 0){
				hasInterest = 1;
			}
			if(!Product.STATE_RaiseFail.equals(state)){
				isAllRaiseFail = 0;
			}
			if(hasInterest==1&&isAllRaiseFail==0){
				break;
			}
		}
		if("raiseFail".equals(flag)){
			return isAllRaiseFail;
		}else if("hasInterest".equals(flag)){
			return hasInterest;
		}
		return null;
	}

	@Transactional
	public BaseResp update(GuessForm guessForm, String loginName) {
		BaseResp rep = new BaseResp();
		GuessEntity guess = guessDao.findOne(guessForm.getOid());
		guess.setContent(guessForm.getContent());
		guess.setGuessName(guessForm.getGuessName());
		guess.setGuessTitle(guessForm.getGuessTitle());
		if(StringUtils.isNoneBlank(guessForm.getImgPath())){
			guess.setImgPath(guessForm.getImgPath());
		}
		guess.setQuestion(guessForm.getQuestion());
		guess.setRemark(guessForm.getRemark());
		guess.setUpdatePerson(loginName);
		guess.setUpdateTime(new Date());
		guessDao.save(guess);
		//传过来的选项oids修改对应的选项内容（剩下的选项oids点删除按钮单独删，多出的oids新增进去）
		List<String> itemOids = guessForm.getItemOids();
		List<String> itemContents = guessForm.getItemContents();
		//处理修改
		List<GuessItemEntity> guessItems = guessItemDao.findAll(itemOids);
		for(GuessItemEntity guessItem : guessItems){
			String oid = guessItem.getOid();
			int i = itemOids.indexOf(oid);
			String content = itemContents.get(i);
			guessItem.setContent(content);
		}
		guessItemDao.save(guessItems);
		//TODO:处理新增
		List<GuessItemEntity> guessItemSaves = new ArrayList<GuessItemEntity>();
		for(int i=0;i<itemOids.size()&&i<itemContents.size();i++){
			String itemOid = itemOids.get(i);
			if(StringUtils.isBlank(itemOid)){
				String itemContent = itemContents.get(i);
				GuessItemEntity guessItem = GuessItemEntity.builder()
						.content(itemContent)
						.createTime(new Date())
						.createPerson(loginName)
						.guess(guess)
						.build();
				guessItemSaves.add(guessItem);
			}
		}
		
		guessItemDao.save(guessItemSaves);
		return rep;
	}

	public GuessSeeRep see(String oid) {
		GuessEntity guess = guessDao.findOne(oid);
		List<GuessItemEntity> guessItems = guessItemDao.findByGuessOid(oid);
		List<String> answerList = new ArrayList<String>();
		List<String> answerOidList = new ArrayList<String>();
		for(GuessItemEntity guessItem:guessItems){
			answerList.add(guessItem.getContent());
			answerOidList.add(guessItem.getOid());
		}
		GuessSeeRep rep = new GuessSeeRep(guess,answerList,answerOidList);
		return rep;
	}

	public GuessDetailRep detail(String oid) {
		GuessEntity guess = guessDao.findOne(oid);
		List<GuessItemEntity> guessItems = guessItemDao.findByGuessOid(oid);
		//根据id查询产品包
		ProductPackage productPackage = productPackageDao.findByGuessOid(oid);
		String productPackageName = null;
		if(productPackage!=null){
			productPackageName = productPackage.getName();
		}else{
//			throw new GHException("该竞猜活动没有关联产品包！");
		}
		List<String> answerList = new ArrayList<String>();
		List<BigDecimal> percentList = new ArrayList<BigDecimal>();
		for(GuessItemEntity guessItem:guessItems){
			answerList.add(guessItem.getContent());
			percentList.add(guessItem.getPercent());
		}
		GuessDetailRep rep = new GuessDetailRep(guess,answerList,percentList,productPackageName);
		return rep;
	}
	@Transactional
	public BaseResp del(String oid, String loginName) {
		BaseResp rep = new BaseResp();
		GuessEntity guess = guessDao.findOne(oid);
		guess.setDelFlag(GuessEntity.GUESS_DEL);
		guessDao.save(guess);
		return rep;
	}
	@Transactional
	public BaseResp delItem(String itemOid) {
		BaseResp rep = new BaseResp();
		guessItemDao.delete(itemOid);
		return rep;
	}

	public GuessItemRep item(String oid) {
		GuessItemRep rep = new GuessItemRep();
		List<GuessItemEntity> guessItems = guessItemDao.findByGuessOid(oid);
		for(GuessItemEntity guessItem : guessItems){
			String content = guessItem.getContent();
			rep.getContent().add(content);
			String itemOid = guessItem.getOid();
			rep.getOids().add(itemOid);
			BigDecimal percent = guessItem.getPercent();
			rep.getPercents().add(percent);
		}
		return rep;
	}

	/**开奖
	 * @param req
	 * @return
	 */
	@Transactional
	public BaseRep lottery(LotteryReq req) {
		BaseRep rep = new BaseRep();
		List<String> oids =  req.getOids();
		List<BigDecimal> percents = req.getPercents();
		List<GuessItemEntity> guessItems = guessItemDao.findAll(oids);
		GuessEntity guess = guessItems.get(0).getGuess();
		Product p = productService.getByGuess(guess);
		BigDecimal baseRatio = p.getExpAror();//基本利率
		for(int i=0;i<percents.size()&&i<oids.size();i++){
			GuessItemEntity guessItem = findItemByOid(guessItems,oids.get(i));
			BigDecimal delaPer = percents.get(i);
			delaPer = delaPer.divide(new BigDecimal("100"), 4,RoundingMode.DOWN);//百分数利率转化为小数利率
			guessItem.setPercent(delaPer);
			guessItem.setNetPercent(baseRatio.add(delaPer));
		}
		guessItemDao.save(guessItems);
		return rep;
	}
	
	/**
	* <p>Title: </p>
	* <p>Description:根据奖项oid查奖项实体（内存中） </p>
	* <p>Company: </p> 
	* @param guessItems
	* @param oid
	* @return
	* @author 邱亮
	* @date 2017年9月29日 上午10:39:20
	* @since 1.0.0
	*/
	private GuessItemEntity findItemByOid(List<GuessItemEntity> guessItems,String oid){
		for(GuessItemEntity item:guessItems){
			if(oid.equals(item.getOid())){
				return item;
			}
		}
		return null;
	}

	public GuessListRep list() {
		GuessListRep rep = new GuessListRep();
		List<Object[]> guesses = guessDao.findAllOrderByRaiseStartDate();
		for(Object[] objs : guesses){
			String name = (String) objs[0];
			String title = (String) objs[1];
			String imgPath = (String) objs[2];
			Byte status = (Byte) objs[3];
			String guessId = (String) objs[4];
			String statusDisp = status == 3 ? "0" : (status == 4 ? "1" : "-1");//为-1不展示此活动
			GuessRep guessPep = GuessRep.builder().name(name).title(title).imgPath(imgPath).status(statusDisp).guessId(guessId).build();
			rep.getGuessList().add(guessPep);
		}
		return rep;
	}

	public GuessDetailForeRep detailFore(String guessId,String cid,String ckey) {
		GuessDetailForeRep rep = new GuessDetailForeRep();
		GuessEntity guess = guessDao.findOne(guessId);
		List<GuessItemEntity> guessItems = guessItemDao.findByGuessOid(guessId);
		String name = guess.getGuessName();
		String title = guess.getQuestion();
		String content = guess.getContent();
		Integer status = guess.getStatus();
		String productId = getCurrentProductByGuess(guess).getOid();
		rep.setName(name);
		rep.setTitle(title);
		rep.setContent(content);
		rep.setGuessStatus(String.valueOf(status));
		rep.setProductStatus(getProductStatusInPackageByGuess(guess,cid,ckey));
		rep.setProductId(productId);
		for(GuessItemEntity item : guessItems){
			ItemRep irep = ItemRep.builder()
					.itemId(item.getOid())
					.itemContent(item.getContent())
					.itemPercent(DecimalUtil.zoomOut(item.getPercent(), 100))//将加息利率扩大100倍方便前台展示
					.build();
			rep.getItemList().add(irep);		
		}
		return rep;
	}

	/**获取竞猜活动详情状态（去下注，来袭中）
	 * @param guess
	 * @return
	 */
	private String getProductStatusInPackageByGuess(GuessEntity guess,String cid,String ckey) {
		Product p = getCurrentProductByGuess(guess);//当前产品
		//如果当前产品在该渠道已下架，则前端显示来袭中
		if(!cacheChannelService.checkProductChannel(cid, ckey, p.getOid())){
			return "3";//来袭中
		}
		ProductPackage  productPackage = productPackageDao.findByGuessOid(guess.getOid());
		return getCurrentProductStatusByProductPackage(productPackage);
	}

	/**获取产品包当前产品是可以购买（去下注）还是来袭中（定时任务执行中）
	 * 如果该产品包下的最新的产品已募集满，并且满足再上架新产品条件---》来袭中 ，如果最新产品没募集满 ----》去下注  否则 ----》已售罄
	 * @param productPackage
	 * @return
	 */
	private String getCurrentProductStatusByProductPackage(ProductPackage productPackage) {
		String result = "0";//去下注
		
		int toProductNum = productPackage.getToProductNum();//上架产品数量
		int productCount = productPackage.getProductCount();//产品包包含的产品数量
		// 校验剩余时间是否满足上架产品
		java.util.Date now = new java.util.Date();
		
		String limitTimes = productPackage.getLimitTime();
		Double limitHour = Double.parseDouble(limitTimes);
		long nowTime =  now.getTime();
		long endTime = DateUtil.addDay(productPackage.getRaiseEndDate(), 1).getTime();
		long leftTime = endTime - nowTime;
		long limitTime = (long) (limitHour * 3600 * 1000);
		
		if(checkProductFound(productPackage)){
			if(leftTime >= limitTime){ //剩余时间足够，可以上架此产品包的产品
				// 查询产品包下面已经上架的产品的数量
				if(toProductNum < productCount){//当上架数量小于产品总数时
					//可以上架本产品包的产品
					result = "1";//来袭中
				}else{
					result = "2";//已售罄
					log.info("=====productPackage{}所有产皮包上架完毕，无可用产品{}=====",productPackage.getOid(),productPackage.getToProductNum());
				}
			}else{
				result = "2";//已售罄
				log.info("=====productPackage{}剩余募集时间不满足上架产品需求{}=====",productPackage.getOid(),productPackage.getLimitTime());
			}
		}else{
			log.info("=====productPackage{}下存在募集中的产品,不能新建新的=====",productPackage.getOid());
		}
		return result;
	}
	
	/**TODO:满标定义改变上线后此方法要改
	 * 判断定期产品的产品剩余份额与最小可投金额
	 */
	public boolean checkProductFound(ProductPackage productPackage){
		boolean canFoundProduct = false;
		List<Product> raisingProductFromProductPackages = productDao.raisingProductFromProductPackage(productPackage.getOid());
		if(raisingProductFromProductPackages.size() == 0){
			canFoundProduct = true;
		}else{
			int productFullCount = 0;
			for(Product product : raisingProductFromProductPackages){
//				BigDecimal investMin = null == product.getInvestMin() ? BigDecimal.ZERO : product.getInvestMin();
//				if (product.getCurrentVolume().add(investMin).compareTo(product.getRaisedTotalNumber()) >= 0){
				if (product.getCurrentVolume().compareTo(product.getRaisedTotalNumber()) >= 0){
					productFullCount++;
				}
			}
			if(productFullCount == raisingProductFromProductPackages.size()){
				canFoundProduct = true;
			}
		}
		return canFoundProduct;
	}
	
	/**获取产品包的当前产品
	 * @param guess
	 * @return
	 */
	private Product getCurrentProductByGuess(GuessEntity guess) {
		String guessOid = guess.getOid();
		return productDao.findCurrentProductByGuessOid(guessOid);
	}

	public GuessEntity getByOid(String guessOid) {
		
		return guessDao.findOne(guessOid);
	}
	@Transactional
	public void modiyStatusByOid(String oid ,Integer from ,Integer to) {
		
		int i = guessDao.updateStatus(oid,from,to);
		if(i<=0){
			throw new GHException("修改竞猜活动:"+oid+",状态失败");
		}else{
			log.info("<----------修改竞猜活动：{},状态成功---------->",oid);
		}
	}

	/**修改产品包对应的竞猜活动状态为已上架
	 * @param productPackage
	 */
	@Transactional
	public void onshelfGuess(ProductPackage productPackage) {
		if(hasGuessInProductPackage(productPackage)){
			Integer toProductNum = productPackage.getToProductNum() + 1;
			log.info("<-----------productPackage.getToProductNum():{}----------------->",productPackage.getToProductNum());
			log.info("<-----------toProductNum:{}----------------->",toProductNum);
			if(toProductNum==1){
				String guessOid = productPackage.getGuess().getOid();
				int i = guessDao.updateStatus(guessOid,GuessEntity.GUESS_STATUS_LOCKED,GuessEntity.GUESS_STATUS_ONSHELF);
				if(i<=0){
					throw new GHException("上架竞猜活动:"+guessOid+",失败");
				}else{
					log.info("<-----------上架竞猜活动:{},成功------------->",guessOid);
				}
			}
		}
		
	}

	public boolean hasGuessInProductPackage(ProductPackage productPackage) {
		return productPackage.getRelateGuess()!=null&&productPackage.getRelateGuess()==1;
	}

	/**结束产品包对应的竞猜活动
	 * @param productPackage
	 */
	@Transactional
	public void endGuess(ProductPackage productPackage) {
		if(hasGuessInProductPackage(productPackage)){
			String guessOid = productPackage.getGuess().getOid();
			int i = guessDao.updateStatus(guessOid,GuessEntity.GUESS_STATUS_ONSHELF,GuessEntity.GUESS_STATUS_END);
			if(i<=0){
				throw new GHException("结束竞猜活动:"+guessOid+",失败");
			}else{
				log.info("<-----------结束竞猜活动:{},成功------------->",guessOid);
			}
		}
		
	}

	/**判断产品是否关联竞猜活动
	 * @param product
	 * @return
	 */
	public boolean hasGuessWithProduct(Product product) {
		GuessEntity guess = product.getGuess();
		return guess!=null;
	}

	/**查询已保存和已解锁状态的竞猜活动名称
	 * @param status
	 * @return
	 */
	public GuessNameRep guessNameList(Integer status) {
		GuessNameRep rep = new GuessNameRep();
		List<GuessEntity> guesses = guessDao.findByStatus(status);
		for(GuessEntity guess:guesses){
			GuessName gName = GuessName.builder().oid(guess.getOid()).name(guess.getGuessName()).build();
			rep.getGuessList().add(gName);
		}
		return rep;
	}

	public List<GuessItemEntity> getGuessItemByGuessOid(String oid) {
		List<GuessItemEntity> items = guessItemDao.findByGuessOid(oid);
		return items;
	}

	public GuessNameRep guessNameAllList() {
		GuessNameRep rep = new GuessNameRep();
		List<GuessEntity> guesses = guessDao.findAllNotDel();
		for(GuessEntity guess:guesses){
			GuessName gName = GuessName.builder().oid(guess.getOid()).name(guess.getGuessName()).build();
			rep.getGuessList().add(gName);
		}
		return rep;
	}

	/**
	* <p>Title: 根据产品包查询竞猜活动</p>
	* <p>Description: </p>
	* <p>Company: </p> 
	* @param productPackage
	* @return
	* @author 邱亮
	* @date 2017年7月11日 下午12:04:31
	* @since 1.0.0
	*/
	public GuessEntity getGuessById(ProductPackage productPackage) {
		if(hasGuessInProductPackage(productPackage)){
			return guessDao.findOne(productPackage.getGuess().getOid());
		}
		return null;
	}

	/**
	* <p>Title:如果产品包关联的竞猜活动没有结束则结束改活动 </p>
	* <p>Description: </p>
	* <p>Company: </p> 
	* @param pp
	* @author 邱亮
	* @date 2017年7月11日 下午2:24:49
	* @since 1.0.0
	*/
	@Transactional(value = TxType.REQUIRES_NEW)
	public void endGuessIfNotEnd(ProductPackage pp) {
		GuessEntity guess = getGuessById(pp);
		if(guess!=null){
			if(!GuessEntity.GUESS_STATUS_END.equals(guess.getStatus())){
				endGuess(pp);
				log.debug("<--------产品包:{}募集结束,结束竞猜活动成功------------>",pp.getOid());
			}else{
				log.debug("<--------产品包:{}募集结束,竞猜活动已经结束，不用在此结束活动------------>",pp.getOid());
			}
		}
		
	}

	/**
	* <p>Title: </p>
	* <p>Description: </p>
	* <p>Company: </p> 
	* @param tradeOrderReq
	* @return
	* @author 邱亮
	* @date 2017年7月24日 下午5:30:31
	* @since 1.0.0
	*/
	public boolean hasRelatedGuess(String productOid) {
		Product p = productDao.findProductByOidAndGuessOidIsNotNull(productOid);
		if(p!=null){
			return true;
		}
		return false;
	}

	public MyGuessRep myGuess(String uid) {
		MyGuessRep rep = new MyGuessRep();
		List<Object[]> objs = guessDao.findGuessInfoByUid(uid);
		for(Object[] obj : objs){
			BigDecimal myLotterAnswer = null;
			String repayLoanStatus = (String)obj[8];
			if(Product.PRODUCT_repayLoanStatus_repayed.equals(repayLoanStatus)){
				myLotterAnswer = ((BigDecimal)obj[7]).multiply(new BigDecimal(100));
			}
			MyGuess guess = MyGuess.builder()
					.title((String)obj[0])
					.productName((String)obj[1])
					.expAnnualRate(((BigDecimal)obj[2]).multiply(new BigDecimal(100)))
					.investAmount((BigDecimal)obj[3])
					.investTime((Timestamp)obj[4])
					.expLotteryDate((Date)obj[5])
					.myAnswer((String)obj[6])
					.myLotteryAnswer(myLotterAnswer==null?"--":transferToString(myLotterAnswer))
					.build();
			rep.getList().add(guess);
		}
				
		return rep;
	}

	private String transferToString(BigDecimal myLotterAnswer) {
		String result = "";
		if(myLotterAnswer.compareTo(BigDecimal.ZERO)<0){
			myLotterAnswer = myLotterAnswer.negate();
			result = "已为您减息"+format(myLotterAnswer)+"%";
		}else if(myLotterAnswer.compareTo(BigDecimal.ZERO)==0){
			result = "按照基础收益率";
		}else{
			result = "已为您加息"+format(myLotterAnswer)+"%";
		}
		return result;
	}

	private String format(BigDecimal d) {
		d = d.setScale(2, RoundingMode.DOWN);
		return d.toString();
	}


}
