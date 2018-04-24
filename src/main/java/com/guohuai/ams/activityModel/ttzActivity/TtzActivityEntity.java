package com.guohuai.ams.activityModel.ttzActivity;

import com.guohuai.component.persist.UUID;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Date;

@Entity
@Table(name = "T_GAM_ACTIVITY_TTZ")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
public class TtzActivityEntity extends UUID implements Serializable {

    private static final long serialVersionUID = -893680553761472538L;

    private String title;

    private String bannerImageUrl;

    private String ruleImageUrl;

    private String activityTimeBegin;

    private String activityTimeEnd;

    private String productLabel;

}
