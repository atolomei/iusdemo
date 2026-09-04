package io.demo.model.db.service;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.List;

import io.demo.Logger;
import io.demo.model.Stat;
import io.demo.service.Settings;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;

/**
 * Stat is not a {@link io.demo.model.DemoDBObject} (no state / lastmodifieduser),
 * so this service extends {@link BaseDBService} directly.
 */
@Service
public class StatDBService extends BaseDBService<Stat, Long> {

	@SuppressWarnings("unused")
	static private Logger logger = Logger.getLogger(StatDBService.class.getName());

	public StatDBService(CrudRepository<Stat, Long> repository, Settings settings) {
		super(repository, settings);
	}

	@Transactional
	public List<Stat> getBySessionId(String sessionId) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<Stat> cq = cb.createQuery(Stat.class);
		Root<Stat> root = cq.from(Stat.class);
		cq.select(root).where(cb.equal(root.get("session_id"), sessionId));
		TypedQuery<Stat> q = getEntityManager().createQuery(cq);
		return q.getResultList();
	}

	@Transactional
	public List<Stat> getByPageId(String pageId) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<Stat> cq = cb.createQuery(Stat.class);
		Root<Stat> root = cq.from(Stat.class);
		cq.select(root).where(cb.equal(root.get("page_id"), pageId));
		TypedQuery<Stat> q = getEntityManager().createQuery(cq);
		return q.getResultList();
	}

	@Override
	public String toJSON() {
		return null;
	}

	@Override
	protected Class<Stat> getEntityClass() {
		return Stat.class;
	}
}
