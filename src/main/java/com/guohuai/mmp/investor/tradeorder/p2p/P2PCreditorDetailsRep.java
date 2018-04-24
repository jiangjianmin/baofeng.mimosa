package com.guohuai.mmp.investor.tradeorder.p2p;

import com.guohuai.component.web.view.BaseRep;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@lombok.Data
@NoArgsConstructor
public class P2PCreditorDetailsRep extends BaseRep{

	private int page = 0;
	private int size = 0;
	private int total = 0;
    private List<P2PCreditorDetail> rows = new ArrayList<>();

}
