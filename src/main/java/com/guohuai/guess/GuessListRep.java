package com.guohuai.guess;

import java.util.ArrayList;
import java.util.List;

import com.guohuai.component.web.view.BaseRep;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@lombok.Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuessListRep extends BaseRep{
	
	List<GuessRep> guessList = new ArrayList<GuessRep>();

}
