package com.guohuai.ams.dict;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DictResp {

	public DictResp(Dict dict) {
		this.id = dict.getOid();
		this.text = dict.getName();
		this.children = new ArrayList<DictResp>();
	}

	private String id;
	private String text;

	private List<DictResp> children;

}
