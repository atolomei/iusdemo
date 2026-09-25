package io.demo.model.db.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import io.demo.Logger;
import io.demo.model.PJSFSentencia;
import io.demo.service.Settings;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;

/**
 * Database operations for {@link PJSFSentencia}.
 */
@Service
public class PJSFSentenciaDBService extends DBService<PJSFSentencia, Long> {

	@SuppressWarnings("unused")
	static private Logger logger = Logger.getLogger(PJSFSentenciaDBService.class.getName());

	public PJSFSentenciaDBService(CrudRepository<PJSFSentencia, Long> repository, Settings settings) {
		super(repository, settings);
	}

	/**
	 * Returns the sentencia for the given PJSF document id (sid), if stored.
	 */
	@Transactional
	public Optional<PJSFSentencia> getByDocumentoId(String documentoId) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<PJSFSentencia> cq = cb.createQuery(PJSFSentencia.class);
		Root<PJSFSentencia> root = cq.from(PJSFSentencia.class);
		cq.select(root).where(cb.equal(root.get("documentoId"), documentoId));
		List<PJSFSentencia> list = getEntityManager().createQuery(cq).setMaxResults(1).getResultList();
		return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
	}

	@Transactional
	public boolean existsByDocumentoId(String documentoId) {
		return getByDocumentoId(documentoId).isPresent();
	}

	@Override
	public String toJSON() {
		return null;
	}

	@Override
	protected Class<PJSFSentencia> getEntityClass() {
		return PJSFSentencia.class;
	}

	@PostConstruct
	protected void onInitialize() {
		super.register(getEntityClass(), this);
	}

}
