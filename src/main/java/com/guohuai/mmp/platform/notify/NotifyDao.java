package com.guohuai.mmp.platform.notify;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface NotifyDao extends JpaRepository<NotifyEntity, String>, JpaSpecificationExecutor<NotifyEntity> {
}
