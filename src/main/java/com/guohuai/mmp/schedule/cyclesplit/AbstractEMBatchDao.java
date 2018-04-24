package com.guohuai.mmp.schedule.cyclesplit;

import com.guohuai.cardvo.util.CardVoUtil;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;


/**
 * 支持em的查询以及批量处理的dao抽象类
 * 子类需继承
 *
 * @author yujianlong
 */
public abstract class AbstractEMBatchDao<T> {

	/**
	 * 绑定jpa EntityManager
	 */
	@PersistenceContext
	protected EntityManager em;

	/**
	 *
	 *批量插入的方法
	 * @author yujianlong
	 * @date 2018/4/1 16:28
	 * @param [list]
	 * @return void
	 */
	@Transactional
	public void batchInsert(List<T> list) {
		for (int i = 0; i < list.size(); i++) {
			em.persist(list.get(i));
			if (i % 200 == 0) {
				em.flush();
//				em.clear();
			}
		}
	}
	/**
	 *
	 *批量更新的方法
	 * @author yujianlong
	 * @date 2018/4/1 16:29
	 * @param [list]
	 * @return void
	 */
	@Transactional
	public void batchUpdate(List<T> list) {
		for (int i = 0; i < list.size(); i++) {
			em.merge(list.get(i));
			if (i % 200 == 0) {
				em.flush();
//				em.clear();
			}
		}
	}

}




