package io.demo.model.db.service;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import io.demo.Logger;
import io.demo.model.Query;
import io.demo.service.Settings;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;

@Service
public class QueryDBService extends DBService<Query, Long> {

	@SuppressWarnings("unused")
	static private Logger logger = Logger.getLogger(QueryDBService.class.getName());

	public QueryDBService(CrudRepository<Query, Long> repository, Settings settings) {
		super(repository, settings);
	}

	@Override
	public <S extends Query> S save(S query) {
		logger.debug("saving query -> reasoningEffortLevel: "
				+ io.demo.results.ReasoningEffortOption.fromOrdinal(query.getReasoningEffortOption()).getKey()
				+ " (" + query.getReasoningEffortOption() + ") | " + query.toString());
		return super.save(query);
	}

	
	@Transactional
	public Optional<Query> findWithDeps(Long id) {
	
	
		Optional<Query> o = super.findById(id);

		if (o.isEmpty())
			return o;

		Query a = o.get();

		// Read all lazy proxy IDs while entity is still attached
		Long userId = a.getLastModifiedUser() != null ? a.getLastModifiedUser().getId() : null;

		// Detach to prevent dirty-checking from triggering @PostUpdate
		getEntityManager().detach(a);
	
		if (userId != null)
			a.setLastModifiedUser(getUserDBService().findById(userId).get());

		a.setDependencies(true);
	
		return Optional.of(a);
	
	}
	
	
	public Optional<Query> findByKey(String key) {
	
	
	    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Query> cq = cb.createQuery(Query.class);
        Root<Query> root = cq.from(Query.class);
        cq.select(root).where(cb.equal(root.get("queryKey"), key));
        List<Query> list = getEntityManager().createQuery(cq).getResultList();
        return list.stream().findFirst();
	
	
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
	 * Returns the most recent queries, ordered by most recent first.
	 *
	 * @param maxResults maximum number of queries to return
	 */
	@Transactional
	public List<Query> getRecent(int maxResults, OffsetDateTime from, OffsetDateTime to) {

		
		/**
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<Query> cq = cb.createQuery(Query.class);
		Root<Query> root = cq.from(Query.class);
		cq.select(root);
		cq.orderBy(cb.desc(root.get("created")));
		return getEntityManager().createQuery(cq).setMaxResults(maxResults).getResultList();
		**/
		
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<Query> cq = cb.createQuery(Query.class);
		Root<Query> root = cq.from(Query.class);

		
		Predicate p;

		
		if (from == null && to == null) {
			p = cb.conjunction(); // No date filter
		} else if (from != null && to == null) {
			p = cb.greaterThanOrEqualTo(root.get("created"), from);
		} else if (from == null && to != null) {
			p = cb.lessThanOrEqualTo(root.get("created"), to);
		} else
		p = cb.and(cb.greaterThanOrEqualTo(root.get("created"), from), 
				   cb.lessThanOrEqualTo(root.get("created"), to));

		
		cq.select(root).distinct(true).where(p);
		cq.orderBy(cb.desc(root.get("created")));

		return getEntityManager().createQuery(cq).setMaxResults(500).getResultList();
		
		
		
		
		
		
		
		
		
		
		
		
		
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

	/**
	 * Returns the most recent {@link Query} with the given query key (hash of
	 * text + date range + subject + reasoning effort), or null if none was logged.
	 */
	@Transactional
	public Query getMostRecentByKey(String queryKey) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<Query> cq = cb.createQuery(Query.class);
		Root<Query> root = cq.from(Query.class);
		cq.select(root).where(cb.equal(root.get("queryKey"), queryKey));
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
