package io.demo.model.db.service;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import io.demo.Logger;
import io.demo.model.Query;
import io.demo.model.QueryFeedback;
import io.demo.service.Settings;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;

/**
 * CRUD operations and queries for {@link QueryFeedback}.
 */
@Service
public class QueryFeedbackDBService extends DBService<QueryFeedback, Long> {

 
	static private Logger logger = Logger.getLogger(QueryFeedbackDBService.class.getName());

	public QueryFeedbackDBService(CrudRepository<QueryFeedback, Long> repository, Settings settings) {
		super(repository, settings);
	}

	@PostConstruct
	protected void onInitialize() {
		DBService.register(QueryFeedback.class, this);
	}

	/** Returns all the feedbacks of the given query. */
	@Transactional
	public List<QueryFeedback> getByQuery(Query query) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<QueryFeedback> cq = cb.createQuery(QueryFeedback.class);
		Root<QueryFeedback> root = cq.from(QueryFeedback.class);
		cq.select(root).where(cb.equal(root.get("query"), query));
		return getEntityManager().createQuery(cq).getResultList();
	}

	/** Returns the most recent feedbacks, ordered by most recent first. */
	@Transactional
	public List<QueryFeedback> getRecent(int maxResults) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<QueryFeedback> cq = cb.createQuery(QueryFeedback.class);
		Root<QueryFeedback> root = cq.from(QueryFeedback.class);
		cq.select(root);
		cq.orderBy(cb.desc(root.get("created")));
		
		List<QueryFeedback> list = getEntityManager().createQuery(cq).setMaxResults(maxResults).getResultList();
	
		
		
		logger.debug(list.size() + " feedbacks retrieved, maxResults=" + maxResults);
		
		return list;
	}
	

	/** Returns the feedbacks with the given grade. */
	@Transactional
	public List<QueryFeedback> getByGrade(int grade) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<QueryFeedback> cq = cb.createQuery(QueryFeedback.class);
		Root<QueryFeedback> root = cq.from(QueryFeedback.class);
		cq.select(root).where(cb.equal(root.get("grade"), grade));
		return getEntityManager().createQuery(cq).getResultList();
	}

	@Override
	public String toJSON() {
		return null;
	}

	@Override
	protected Class<QueryFeedback> getEntityClass() {
		return QueryFeedback.class;
	}
}
