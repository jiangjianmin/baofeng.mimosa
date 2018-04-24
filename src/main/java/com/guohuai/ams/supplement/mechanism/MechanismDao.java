package com.guohuai.ams.supplement.mechanism;

import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface MechanismDao extends JpaRepository<Mechanism, Serializable>, JpaSpecificationExecutor<Mechanism>{

}
