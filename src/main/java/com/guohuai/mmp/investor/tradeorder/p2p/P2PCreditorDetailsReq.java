package com.guohuai.mmp.investor.tradeorder.p2p;

import com.guohuai.component.web.view.BaseRep;
import com.guohuai.component.web.view.BaseReq;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@lombok.Data
@NoArgsConstructor
public class P2PCreditorDetailsReq extends BaseReq{

	private String orderOid;

	private int page = 1;

	private int size = 10;

}
