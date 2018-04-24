package com.guohuai.ams.supplement.mechanism;

import java.util.ArrayList;
import java.util.List;

import com.guohuai.component.web.view.BaseRep;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class MechanismListRep extends BaseRep{
	
	List<MechanismRep> mecs = new ArrayList<MechanismRep>();
}
