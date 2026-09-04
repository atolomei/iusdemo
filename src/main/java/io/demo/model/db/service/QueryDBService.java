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

	@PostConstruct
	protected void onInitialize() {
		DBService.register(Query.class, this);
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

	@Override
	public String toJSON() {
		return null;
	}

	@Override
	protected Class<Query> getEntityClass() {
		return Query.class;
	}
}
