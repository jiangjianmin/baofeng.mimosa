package com.guohuai.mmp.platform.publisher.offset;

import com.guohuai.component.web.view.BaseRep;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class OffsetCloseRep extends BaseRep {
	private String retHtml;
	private String offsetOid;
	private String type;
}
