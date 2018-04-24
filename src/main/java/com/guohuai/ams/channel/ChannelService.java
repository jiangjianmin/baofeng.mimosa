package com.guohuai.ams.channel;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.guohuai.ams.channel.ChannelRemarksRep.ChannelRemarksRepBuilder;
import com.guohuai.ams.channel.channelapprove.ChannelApprove;
import com.guohuai.ams.channel.channelapprove.ChannelApproveDao;
import com.guohuai.ams.channel.channelapprove.ChannelApproveService;
import com.guohuai.ams.label.LabelEntity;
import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.productChannel.ProductChannel;
import com.guohuai.ams.product.productChannel.ProductChannelDao;
import com.guohuai.basic.common.SeqGenerator;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.Clock;
import com.guohuai.component.util.StringUtil;
import com.guohuai.component.web.view.BaseResp;
import com.guohuai.component.web.view.PageResp;
import com.guohuai.mmp.lx.serfee.SerFeeQueryRep;
import com.guohuai.mmp.lx.serfee.SerFeeService;
import com.guohuai.mmp.sys.CodeConstants;

@Service
@Transactional
public class ChannelService {

	@Autowired
	ChannelDao daoChannel;
	@Autowired
	ChannelApproveService serviceChannelApprove;
	@Autowired
	SerFeeService serFeeService;
	@Autowired
	private SeqGenerator seqGenerator;
	@Autowired
	private ProductChannelDao productChannelDao;
	@Autowired
	private ChannelApproveDao cannelApproveDao;

	@Transactional
	public PageResp<ChannelQueryRep> channelQuery(Specification<Channel> spec, Pageable pageable) {
		Page<Channel> enchs = this.daoChannel.findAll(spec, pageable);
		PageResp<ChannelQueryRep> pageResp = new PageResp<ChannelQueryRep>();
		List<ChannelQueryRep> list = new ArrayList<ChannelQueryRep>();
		
		if (enchs != null && enchs.getContent() != null && enchs.getTotalElements() > 0) {
			for (Channel ench : enchs) {
				ChannelQueryRep r = new ChannelQueryRep(ench);
				List<ChannelApprove> cas = queryRremarks(ench.getOid());
				if(cas!=null) {
					r.setCommentNum(cas.size());
				}
				list.add(r);
			}
		}
		pageResp.setTotal(enchs.getTotalElements());
		pageResp.setRows(list);
		return pageResp;
	}

	/**
	 * 获取渠道实体
	 * 
	 * @param oid
	 * @return
	 */
	public Channel getOne(String oid) {
		Channel en = this.daoChannel.findOne(oid);
		if (en == null) {
			// error.define[70001]=平台-渠道不存在!(CODE:70001)
			throw AMPException.getException(70001);
		}
		return en;
	}

	/**
	 * 保存渠道
	 * 
	 * @param en
	 * @return
	 */
	public Channel save(Channel en) {
		return this.daoChannel.save(en);
	}

	/**
	 * 获取渠道详情
	 * 
	 * @param oid
	 * @return
	 */
	@Transactional
	public ChannelInfoRep getChannelInfo(String oid) {
		Channel en = this.getOne(oid);
		ChannelInfoRep rep = new ChannelInfoRep();
		rep.setOid(en.getOid());
		rep.setCid(en.getCid());
		rep.setCkey(en.getCkey());
		rep.setChannelName(en.getChannelName());
		rep.setChannelId(en.getChannelId());
		rep.setChannelFee(en.getChannelFee().multiply(new BigDecimal("100")));
		rep.setJoinType(en.getJoinType());
		rep.setPartner(en.getPartner());
		rep.setChannelContactName(en.getChannelContactName());
		rep.setChannelEmail(en.getChannelEmail());
		rep.setChannelPhone(en.getChannelPhone());
		rep.setChannelStatus(en.getChannelStatus());
		rep.setApprovelStatus(en.getApproveStatus());
		rep.setDeleteStatus(en.getDeleteStatus());
		SerFeeQueryRep sqr = serFeeService.channelSumAccruedFee(oid);
		rep.setAccruedFeeTotal(sqr.getAccruedSumFee());
		rep.setPayFeeTotal(sqr.getPayPlatformSumFee());
		rep.setPayCouFeeTotal(sqr.getPayPlatformCouSumFee());
		return rep;
	}

	/**
	 * 渠道的开启停用申请
	 * 
	 * @param oid
	 *            渠道Oid
	 * @param requestType
	 *            申请类型
	 * @param operator
	 * @return
	 */
	@Transactional
	public BaseResp setApply(String oid, String requestType, String operator) {
		BaseResp rep = new BaseResp();
		Timestamp now = new Timestamp(Clock.DEFAULT.getCurrentTimeInMillis());
		Channel chan = this.getOne(oid);
		// 查看审批表是否有未审核的记录
		List<ChannelApprove> lists = serviceChannelApprove.findByChannelAndApprovelResult(chan);
		if (lists != null && lists.size() > 0) {
			// error.define[70004]=已经存在一条未审批的记录，无法再次操作!(CODE:70004)
			throw AMPException.getException(70004);
		}
		ChannelApprove channelApprove = new ChannelApprove();
		channelApprove.setChannel(chan);
		channelApprove.setChannelName(chan.getChannelName());
		String channelApproveCode = this.seqGenerator.next(CodeConstants.channelApproveCode);
		channelApprove.setChannelApproveCode(channelApproveCode);
		channelApprove.setRequestType(requestType);
		// 申请人
		channelApprove.setRequester(operator);
		channelApprove.setRequestTime(now);
		// 审批表 标记为待审批
		channelApprove.setApproveStatus(ChannelApprove.CHANAPPROVE_APPROVESTATUS_toApprove);
		channelApprove.setCreateTime(now);
		channelApprove.setUpdateTime(now);
		this.serviceChannelApprove.saveChanApprEntity(channelApprove);
		// 待审批
		chan.setApproveStatus(Channel.CHANNEL_APPROVESTATUS_toApprove);
		chan.setUpdateTime(now);
		this.daoChannel.save(chan);
		return rep;
	}

	/**
	 * 新增和修改渠道
	 * 
	 * @param req
	 * @return
	 */
	public Channel addChannel(ChannelAddReq req) {
		Timestamp now = new Timestamp(Clock.DEFAULT.getCurrentTimeInMillis());
		Channel channel = null;
		if (req.getOid() != null && !"".equals(req.getOid())) {
			channel = this.getOne(req.getOid());
		} else {
			channel = new Channel();
			channel.setCreateTime(now);
		}
		// 此处channelId以后修改成想要的格式
		channel.setCid(req.getCid());
		channel.setCkey(req.getCkey());
		channel.setChannelId(StringUtil.uuid());
		channel.setChannelName(req.getChannelName());
		channel.setJoinType(req.getJoinType());
		channel.setChannelFee(null == req.getChannelFee() ? BigDecimal.ZERO : req.getChannelFee().divide(new BigDecimal("100")));
		channel.setPartner(req.getPartner());
		channel.setChannelContactName(req.getChannelContactName());
		channel.setChannelEmail(req.getChannelEmail());
		channel.setChannelPhone(req.getChannelPhone());
		channel.setChannelStatus(Channel.CHANNEL_STATUS_OFF);
		// 删除状态-正常
		channel.setDeleteStatus(Channel.CHANNEL_DELESTATUS_NO);
		channel.setUpdateTime(now);
		channel = this.daoChannel.save(channel);
		return channel;
	}

	/**
	 * 删除渠道
	 * 
	 * @param oid
	 * @return
	 */
	public BaseResp delChannel(final String oid) {
		BaseResp rep = new BaseResp();
		
		Specification<ProductChannel> spec = new Specification<ProductChannel>() {
			@Override
			public Predicate toPredicate(Root<ProductChannel> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				
				return cb.and(
						cb.equal(root.get("marketState").as(String.class), ProductChannel.MARKET_STATE_Onshelf),//产品在渠道上上架状态
						cb.equal(root.get("channel").get("oid").as(String.class), oid),//产品选择某个渠道
						cb.equal(root.get("product").get("isDeleted").as(String.class), Product.NO)//产品未删除
					);
			}
		};
		spec = Specifications.where(spec);
		List<ProductChannel> pcs = this.productChannelDao.findAll(spec);
		if(pcs!=null && pcs.size()>0) {
			throw AMPException.getException("该渠道有上架的产品，请先全部下架");
		}
		Timestamp now = new Timestamp(Clock.DEFAULT.getCurrentTimeInMillis());
		// 判断渠道是否存在
		Channel en = this.getOne(oid);
		en.setDeleteStatus(Channel.CHANNEL_DELESTATUS_YES);
		en.setUpdateTime(now);
		this.daoChannel.save(en);
		return rep;
	}

	/**
	 * 根据渠道获取审批表中的审核意见列表
	 * 
	 * @param oid
	 * @return
	 */
	@Transactional
	public PageResp<ChannelRemarksRep> remarksQuery(String oid) {
		PageResp<ChannelRemarksRep> pageResp = new PageResp<ChannelRemarksRep>();
		List<ChannelRemarksRep> listReps = new ArrayList<ChannelRemarksRep>();
		
		List<ChannelApprove> list = queryRremarks(oid);
		for (ChannelApprove en : list) {
			ChannelRemarksRep rep = new ChannelRemarksRepBuilder().remark(en.getRemark()).time(en.getUpdateTime()).build();
			listReps.add(rep);
		}
		pageResp.setRows(listReps);
		return pageResp;
	}
	
	private List<ChannelApprove> queryRremarks(final String oid) {
		Specification<ChannelApprove> caspec = new Specification<ChannelApprove>() {
			@Override
			public Predicate toPredicate(Root<ChannelApprove> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				Predicate c = cb.equal(root.get("channel").get("oid").as(String.class), oid);
				
				In<String> status = cb.in(root.get("approveStatus").as(String.class));
				status.value(ChannelApprove.CHANAPPROVE_APPROVERESULT_PASS).value(ChannelApprove.CHANAPPROVE_APPROVERESULT_REFUSED);
				return cb.and(c, status);
			}
		};
		caspec = Specifications.where(caspec);
		
		List<ChannelApprove> list = cannelApproveDao.findAll(caspec);
		return list;
	}

	/**
	 * 获取一个渠道信息
	 * 
	 * @return
	 */
	public ChannelInfoRep getOneChannel() {
		List<Channel> list = this.daoChannel.findByDeleteStatus(Channel.CHANNEL_DELESTATUS_NO);
		ChannelInfoRep rep = new ChannelInfoRep();
		if (list != null && list.size() > 0) {
			rep = this.getChannelInfo(list.get(0).getOid());
		}
		return rep;
	}

	public List<ChannelOptions> getOptions() {
		List<Channel> channel = this.daoChannel.findByApproveStatus(Channel.CHANNEL_APPROVESTATUS_PASS);
		List<ChannelOptions> ops = new ArrayList<ChannelOptions>();
		if (null != channel && channel.size() > 0) {
			for (Channel c : channel) {
				ops.add(new ChannelOptions(c));
			}
		}
		return ops;
	}

	public Channel getChannel(String cid, String ckey) {
		Channel c = this.daoChannel.findByCidAndCkey(cid, ckey);
		return c;
	}

	public Channel findByCid(String cid) {
		Channel channel = this.daoChannel.findByCid(cid);
		if (null == channel) {
			// error.define[30023]=渠道不存在(CODE:30023)
			throw new AMPException(30023);
		}
		return channel;
	}
	
	public Channel findOneByCid(String cid) {
		return this.daoChannel.findByCid(cid);
	}
	
	@Transactional
	public long validateSingle(final String attrName, final String value, final String oid) {

		Specification<Channel> spec = new Specification<Channel>() {
			@Override
			public Predicate toPredicate(Root<Channel> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				if (StringUtil.isEmpty(oid)) {
					return cb.and(cb.equal(root.get("deleteStatus").as(String.class), Channel.CHANNEL_DELESTATUS_NO), cb.equal(root.get(attrName).as(String.class), value));
				} else {
					return cb.and(cb.equal(root.get("deleteStatus").as(String.class), Channel.CHANNEL_DELESTATUS_NO), cb.equal(root.get(attrName).as(String.class), value),
							cb.notEqual(root.get("oid").as(String.class), oid));
				}
			}
		};
		spec = Specifications.where(spec);

		return this.daoChannel.count(spec);
	}
	
	
	public List<JSONObject> getChannelNames() {
		Specification<Channel> spec = new Specification<Channel>() {
			@Override
			public Predicate toPredicate(Root<Channel> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.and(cb.equal(root.get("channelStatus").as(String.class), Channel.CHANNEL_STATUS_ON), cb.equal(root.get("approveStatus").as(String.class), Channel.CHANNEL_APPROVESTATUS_PASS), 
						cb.equal(root.get("deleteStatus").as(String.class), Channel.CHANNEL_DELESTATUS_NO));
			}
		};
		spec = Specifications.where(spec);

		List<Channel> channels = this.daoChannel.findAll(Specifications.where(spec));

		List<JSONObject> channelNames = new ArrayList<JSONObject>();
		if (channels != null && channels.size() > 0) {
			JSONObject channelName = null;
			for (Channel channel : channels) {
				channelName = new JSONObject();
				channelName.put("oid", channel.getOid());
				channelName.put("name", channel.getChannelName());
				channelName.put("code", channel.getChannelCode());
				channelNames.add(channelName);
			}
		}
		return channelNames;
	}
	
}
