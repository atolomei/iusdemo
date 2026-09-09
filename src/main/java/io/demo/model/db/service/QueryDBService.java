package io.demo.model.db.service;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.List;

import io.demo.Logger;
import io.demo.model.Query;
import io.demo.service.Settings;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;

@Service
public class QueryDBService extends DBService<Query, Long> {

	@SuppressWarnings("unused")
	static private Logger logger = Logger.getLogger(QueryDBService.class.getName());

	public QueryDBService(CrudRepository<Query, Long> repository, Settings settings) {
		super(repository, settings);
	}

 

	@Transactional
	public List<Query> getBySessionId(String sessionId) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<Query> cq = cb.createQuery(Query.class);
		Root<Query> root = cq.from(Query.class);
		cq.select(root).where(cb.equal(root.get("session_id"), sessionId));
		TypedQuery<Query> q = getEntityManager().createQuery(cq);
		return q.getResultList();
	}

	/**
	 * Returns the most recent queries, ordered by most recent first.
	 *
	 * @param maxResults maximum number of queries to return
	 */
	@Transactional
	public List<Query> getRecent(int maxResults) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<Query> cq = cb.createQuery(Query.class);
		Root<Query> root = cq.from(Query.class);
		cq.select(root);
		cq.orderBy(cb.desc(root.get("created")));
		return getEntityManager().createQuery(cq).setMaxResults(maxResults).getResultList();
	}

	/**
	 * Returns the most recent {@link Query} with the given query text, or null
	 * if none was logged.
	 */
	@Transactional
	public Query getMostRecentByText(String queryText) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<Query> cq = cb.createQuery(Query.class);
		Root<Query> root = cq.from(Query.class);
		cq.select(root).where(cb.equal(root.get("query"), queryText));
		cq.orderBy(cb.desc(root.get("created")));
		List<Query> list = getEntityManager().createQuery(cq).setMaxResults(1).getResultList();
		return list.isEmpty() ? null : list.get(0);
	}

	@Override
	public String toJSON() {
		return null;
	}

	@Override
	protected Class<Query> getEntityClass() {
		return Query.class;
	}
	
	@PostConstruct
	protected void onInitialize() {
		super.register(getEntityClass(), this);
	}
	
	
}
