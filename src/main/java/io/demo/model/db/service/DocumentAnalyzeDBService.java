package io.demo.model.db.service;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.List;

import io.demo.Logger;
import io.demo.model.DocumentAnalyze;
import io.demo.model.Query;
import io.demo.service.Settings;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;

@Service
public class DocumentAnalyzeDBService extends DBService<DocumentAnalyze, Long> {

	@SuppressWarnings("unused")
	static private Logger logger = Logger.getLogger(DocumentAnalyzeDBService.class.getName());

	public DocumentAnalyzeDBService(CrudRepository<DocumentAnalyze, Long> repository, Settings settings) {
		super(repository, settings);
	}

	@PostConstruct
	protected void onInitialize() {
		DBService.register(DocumentAnalyze.class, this);
	}

	@Transactional
	public List<DocumentAnalyze> getByQuery(Query query) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<DocumentAnalyze> cq = cb.createQuery(DocumentAnalyze.class);
		Root<DocumentAnalyze> root = cq.from(DocumentAnalyze.class);
		cq.select(root).where(cb.equal(root.get("query"), query));
		TypedQuery<DocumentAnalyze> q = getEntityManager().createQuery(cq);
		return q.getResultList();
	}

	@Transactional
	public List<DocumentAnalyze> getBySessionId(String sessionId) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<DocumentAnalyze> cq = cb.createQuery(DocumentAnalyze.class);
		Root<DocumentAnalyze> root = cq.from(DocumentAnalyze.class);
		cq.select(root).where(cb.equal(root.get("session_id"), sessionId));
		TypedQuery<DocumentAnalyze> q = getEntityManager().createQuery(cq);
		return q.getResultList();
	}

	@Override
	public String toJSON() {
		return null;
	}

	@Override
	protected Class<DocumentAnalyze> getEntityClass() {
		return DocumentAnalyze.class;
	}
}
