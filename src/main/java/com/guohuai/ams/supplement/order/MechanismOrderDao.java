package com.guohuai.ams.supplement.order;

import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface MechanismOrderDao extends JpaRepository<MechanismOrder, Serializable>, JpaSpecificationExecutor<MechanismOrder>{

}
