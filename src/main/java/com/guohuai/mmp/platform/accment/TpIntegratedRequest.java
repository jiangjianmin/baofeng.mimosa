package com.guohuai.mmp.platform.accment;

import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class TpIntegratedRequest {
	private List<TransPublisherRequest> tpList;
}
