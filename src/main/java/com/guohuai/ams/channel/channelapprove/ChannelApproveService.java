package com.guohuai.ams.channel.channelapprove;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.guohuai.ams.channel.Channel;
import com.guohuai.ams.channel.ChannelService;
import com.guohuai.ams.channel.channelapprove.ChannelApproveQueryRep.ChannelApproveQueryRepBuilder;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.Clock;
import com.guohuai.component.web.view.BaseResp;
import com.guohuai.component.web.view.PageResp;
import com.guohuai.operate.api.AdminSdk;

@Service
@Transactional
public class ChannelApproveService {

	@Autowired
	ChannelApproveDao daoChannelApprove;
	@Autowired
	ChannelService serviceChannel;
	@Autowired
	private AdminSdk adminSdk;
	
	@Transactional
	public PageResp<ChannelApproveQueryRep> channelApproveQuery(Specification<ChannelApprove> spec, Pageable pageable) {		
		Page<ChannelApprove> enchapproves = this.daoChannelApprove.findAll(spec, pageable);
		PageResp<ChannelApproveQueryRep> pageResp = new PageResp<ChannelApproveQueryRep>();
		List<ChannelApproveQueryRep> list = new ArrayList<ChannelApproveQueryRep>();
		for (ChannelApprove en : enchapproves) {
			String requester = adminSdk.getAdmin(en.getRequester()).getName();
			String approveMan = adminSdk.getAdmin(en.getApproveMan()).getName();
			ChannelApproveQueryRep rep = new ChannelApproveQueryRepBuilder().oid(en.getOid())
					.channelOid(en.getChannel().getOid())
					.channelName(en.getChannelName()).channelApprovelCode(en.getChannelApproveCode())
					.requestType(en.getRequestType()).requester(requester)
					.approvelMan(approveMan).approvelResult(en.getApproveStatus())
					.remark(en.getRemark()).requestTime(en.getRequestTime())
					.updateTime(en.getUpdateTime()).build();
			list.add(rep);
		}
		pageResp.setTotal(enchapproves.getTotalElements());	
		pageResp.setRows(list);
		return pageResp;
	}
	
	/**
	 * 获取渠道审批实体
	 * @param oid
	 * @return
	 */
	public ChannelApprove getOne(String oid){
		ChannelApprove en = this.daoChannelApprove.findOne(oid);
		if(en==null){
			//error.define[70002]=平台-渠道审批不存在!(CODE:70002)
			throw AMPException.getException(70002);
		}
		return en;
	}
	
	/**
	 * 渠道申请处理
	 * @param req
	 * @param operator
	 */
	@Transactional
	public BaseResp dealApply(ChannelApproveOpeReq req, String operator){
		BaseResp rep = new BaseResp();
		Timestamp now = new Timestamp(Clock.DEFAULT.getCurrentTimeInMillis());
		ChannelApprove en = this.getOne(req.getApprOid());
		Channel channel = en.getChannel();
		if(channel==null){
			//error.define[70003]=平台-渠道审批对应的平台-渠道不存在!(CODE:70003)
			throw AMPException.getException(70003);
		}
		//审批人
		en.setApproveMan(operator);
		en.setApproveStatus(req.getApprResult());
		
		if(ChannelApprove.CHANAPPROVE_APPROVERESULT_PASS.equals(req.getApprResult())){
			//审批通过
			channel.setApproveStatus(Channel.CHANNEL_APPROVESTATUS_PASS);
			//通过后将渠道状态设置为申请时的状态
			if(ChannelApprove.CHANAPPROVE_REQUESTTYPE_ON.equals(en.getRequestType())){
				channel.setChannelStatus(Channel.CHANNEL_STATUS_ON);
			}else if(ChannelApprove.CHANAPPROVE_REQUESTTYPE_OFF.equals(en.getRequestType())){
				channel.setChannelStatus(Channel.CHANNEL_STATUS_OFF);
			}			
		}else if(ChannelApprove.CHANAPPROVE_APPROVERESULT_REFUSED.equals(req.getApprResult())){
			//审批驳回
			channel.setApproveStatus(Channel.CHANNEL_APPROVESTATUS_REFUSED);
		}
		en.setRemark(req.getRemark());
		en.setUpdateTime(now);
		//更新渠道审批
		this.daoChannelApprove.save(en);		
		channel.setUpdateTime(now);
		//更新渠道
		this.serviceChannel.save(channel);
		return rep;
	}
	
	/**
	 * 保存渠道审批
	 * @param en
	 * @return
	 */
	public ChannelApprove saveChanApprEntity(ChannelApprove en){
		return this.daoChannelApprove.save(en);
	}
	
	/**
	 * 根据渠道获取渠道审批列表
	 * @param channel
	 * @return
	 */
	public List<ChannelApprove> findByChannel(Channel channel){
		return this.daoChannelApprove.getByChannel(channel);
	}
	
	/**
	 * 根据渠道，获取未审核的审核渠道记录
	 * @param chen
	 * @return
	 */
	public List<ChannelApprove> findByChannelAndApprovelResult(Channel chen){
		return this.daoChannelApprove.findByChannelAndApproveStatus(chen, ChannelApprove.CHANAPPROVE_APPROVESTATUS_toApprove);
	}
}
