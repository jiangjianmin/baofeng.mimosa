package com.guohuai.ams.activityModel.ttzActivity;

import com.guohuai.component.exception.AMPException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
public class TtzActivityRatioRangeCache {

    private List<TtzActivityRatio> ratioCache = new ArrayList<>();

    @Autowired
    private TtzActivityRatioDao ttzActivityRatioDao;

    @PostConstruct
    public void initRatioCache() {
        ratioCache.clear();
        List<TtzActivityRatioEntity> entities = ttzActivityRatioDao.findAll();
        for (TtzActivityRatioEntity entity : entities) {
            TtzActivityRatio ratio = new TtzActivityRatio();
            ratio.setAmount(entity.getMinAmount());
            ratio.setRatio(entity.getRatio());
            ratioCache.add(ratio);
        }
        ratioCache.sort(Comparator.comparing(TtzActivityRatio::getAmount));
        log.info("【团团赚活动】初始化购买总额、利率对应关系成功！{}", ratioCache);
    }

    public List<TtzActivityRatio> getRatioCache() {
        if (ratioCache == null || ratioCache.size() < 1) {
            throw new AMPException("团团赚购买额利率对应关系不存在!");
        }
        return ratioCache;
    }

}
