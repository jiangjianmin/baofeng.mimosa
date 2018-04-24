package com.guohuai.ams.activityModel.ttzActivity;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "T_GAM_ACTIVITY_TTZ_RATIO")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
public class TtzActivityRatioEntity {

    @Id
    private String id;
    private BigDecimal minAmount;
    private BigDecimal ratio;

}
