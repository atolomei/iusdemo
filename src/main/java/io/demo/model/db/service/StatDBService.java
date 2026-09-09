package io.demo.model.db.service;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

import io.demo.Logger;
import io.demo.model.Stat;
import io.demo.model.User;
import io.demo.service.Settings;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
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

	/**
	 * Logs a sign-in for the given user.
	 */
	@Transactional
	public Stat logSignin(User user, String sessionId, String userAgent) {
		Stat stat = new Stat();
		stat.setAction(Stat.ACTION_SIGNIN);
		stat.setPageId(Stat.ACTION_SIGNIN);
		stat.setSessionId(sessionId);
		stat.setUserAgent(userAgent);
		stat.setUser(user);
		stat.setTimestamp(OffsetDateTime.now());
		return save(stat);
	}

	/**
	 * Returns the most recent SIGNIN stats within the given time range,
	 * ordered by most recent first.
	 *
	 * @param from inclusive lower bound, or {@code null} for no lower bound
	 * @param to   inclusive upper bound, or {@code null} for no upper bound
	 */
	@Transactional
	public List<Stat> getRecentSignins(OffsetDateTime from, OffsetDateTime to) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<Stat> cq = cb.createQuery(Stat.class);
		Root<Stat> root = cq.from(Stat.class);

		// Eagerly fetch user within the transaction to avoid LazyInitializationException
		root.fetch("user", JoinType.LEFT);

		Predicate p = cb.equal(root.get("action"), Stat.ACTION_SIGNIN);

		if (from != null)
			p = cb.and(p, cb.greaterThanOrEqualTo(root.get("timestamp"), from));

		if (to != null)
			p = cb.and(p, cb.lessThanOrEqualTo(root.get("timestamp"), to));

		cq.select(root).distinct(true).where(p);
		cq.orderBy(cb.desc(root.get("timestamp")));

		return getEntityManager().createQuery(cq).setMaxResults(500).getResultList();
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
