package com.guohuai.component.persist;

import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.UUIDHexGenerator;
import org.hibernate.type.Type;

import java.io.Serializable;
import java.util.Optional;
import java.util.Properties;

/**
 * 自定义主键生成器
 *com.guohuai.mmp.investor.moniCycle.AssignOrUUIDGenerator
 * @author yujianlong
 * @create 2018-03-31 16:16
 **/
public class AssignOrUUIDGenerator extends UUIDHexGenerator implements Configurable,IdentifierGenerator {
	private String entityName;
	@Override
	public void configure(Type type, Properties params, Dialect d) throws MappingException {
		entityName = params.getProperty(ENTITY_NAME);
		if ( entityName == null ) {
			throw new MappingException("no entity name");
		}
		super.configure(type, params, d);
	}

	@Override
	public Serializable generate(SessionImplementor session, Object obj) throws HibernateException {
		if (obj instanceof UUID) {
			String op_AssignedId = Optional.ofNullable(obj).map(o -> (UUID) o).map(UUID::getAssignedId).orElse("");
			if (StringUtils.isNotEmpty(op_AssignedId)){
				return op_AssignedId;
			}
		}
		return super.generate(session, obj);

	}
}
