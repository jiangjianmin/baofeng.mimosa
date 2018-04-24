package com.guohuai.ams.activityModel.ttzActivity;

import com.guohuai.component.web.view.BaseRep;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.sql.Date;

@lombok.Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TtzActivityForm extends BaseRep{

    private String title;
    private String bannerImageUrl;
    private String ruleImageUrl;
    private String activityTimeBegin;
    private String activityTimeEnd;
    private String productLabel;
}
