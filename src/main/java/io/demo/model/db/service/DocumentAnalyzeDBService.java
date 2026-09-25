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

	/**
	 * Returns the most recent {@link DocumentAnalyze} for the given RAG
	 * document id and question, or null if none was logged.
	 */
	@Transactional
	public DocumentAnalyze getMostRecent(String ragDocumentId, String question) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<DocumentAnalyze> cq = cb.createQuery(DocumentAnalyze.class);
		Root<DocumentAnalyze> root = cq.from(DocumentAnalyze.class);
		cq.select(root).where(cb.and(
				cb.equal(root.get("ragDocumentId"), ragDocumentId),
				cb.equal(root.get("question"), question)));
		cq.orderBy(cb.desc(root.get("created")));
		List<DocumentAnalyze> list = getEntityManager().createQuery(cq).setMaxResults(1).getResultList();
		return list.isEmpty() ? null : list.get(0);
	}

	@Override
	public String toJSON() {
		return null;
	}

	@Override
	protected Class<DocumentAnalyze> getEntityClass() {
		return DocumentAnalyze.class;
	}
	
	@PostConstruct
	protected void onInitialize() {
		super.register(getEntityClass(), this);
	}
	
	
}
