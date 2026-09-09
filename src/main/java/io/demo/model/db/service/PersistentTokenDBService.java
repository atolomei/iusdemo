package io.demo.model.db.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import io.demo.Logger;
import io.demo.model.PersistentToken;
import io.demo.model.db.PersistentTokenRepository;
import io.demo.service.Settings;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;

@Service
public class PersistentTokenDBService extends BaseDBService<PersistentToken, Long> {

	@SuppressWarnings("unused")
	static private Logger logger = Logger.getLogger(PersistentTokenDBService.class.getName());

	@PersistenceContext
	private EntityManager entityManager;

	public PersistentTokenDBService(CrudRepository<PersistentToken, Long> repository, Settings settings) {
		super(repository, settings);
	}

	@PostConstruct
	public void init() {
		BaseDBService.register(PersistentToken.class, this);
	}

	@Override
	protected Class<PersistentToken> getEntityClass() {
		return PersistentToken.class;
	}

	// ---------------------------------------------------------------
	// Create
	// ---------------------------------------------------------------

	/**
	 * Creates and persists a new {@link PersistentToken}.
	 *
	 * @param entity  the entity name (e.g. class name) this token is linked to
	 * @param object  the id or key of the object this token applies to
	 * @param token   the token value
	 * @param expires expiry date/time
	 * @return the persisted token
	 */
	@Transactional
	public PersistentToken create(String entityId, String object, String token, OffsetDateTime expires) {
		PersistentToken t = new PersistentToken();
		t.setEntity(entityId);
		t.setEntityClass(object);
		t.setToken(token);
		t.setCreated(OffsetDateTime.now());
		t.setExpires(expires);
		return save(t);
	}

	// ---------------------------------------------------------------
	// Finders
	// ---------------------------------------------------------------

	@Transactional
	public Optional<PersistentToken> findById(Long id) {
		return getRepository().findById(id);
	}

	@Transactional
	public Iterable<PersistentToken> findByToken(String token) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<PersistentToken> cq = cb.createQuery(getEntityClass());
		Root<PersistentToken> root = cq.from(getEntityClass());
		Predicate p1 = cb.equal(root.get("token"), token);
		cq.select(root).where(p1);
		return getEntityManager().createQuery(cq).getResultList();
	}

	@Transactional
	public List<PersistentToken> findByEntity(String entity) {
		return ((PersistentTokenRepository) getRepository()).findByEntity(entity);
	}

	@Transactional
	public List<PersistentToken> findByEntityAndObject(String entity, String object) {
		return ((PersistentTokenRepository) getRepository()).findByEntityAndObject(entity, object);
	}

	@Transactional
	public List<PersistentToken> findByEntityObjectAndToken(String entity, String object, String token) {
		return ((PersistentTokenRepository) getRepository()).findByEntityObjectAndToken(entity, object, token);
	}

	// ---------------------------------------------------------------
	// Validate / expire
	// ---------------------------------------------------------------

	/**
	 * Returns {@code true} if a matching token exists and has not yet expired.
	 */
	@Transactional
	public boolean isValid(String entity, String object, String token) {
		List<PersistentToken> results = findByEntityObjectAndToken(entity, object, token);
		if (results == null || results.isEmpty())
			return false;
		OffsetDateTime now = OffsetDateTime.now();
		return results.stream().anyMatch(t -> t.getExpires() != null && t.getExpires().isAfter(now));
	}

	/**
	 * Deletes all expired tokens.
	 *
	 * @return number of deleted rows
	 */
	@Transactional
	public int deleteExpired() {
		return getEntityManager().createQuery("DELETE FROM PersistentToken t WHERE t.expires < :now").setParameter("now", OffsetDateTime.now()).executeUpdate();
	}

	/**
	 * Deletes all tokens linked to a given entity/object pair.
	 */
	@Transactional
	public int deleteByEntityAndObject(String entity, String object) {
		return getEntityManager().createQuery("DELETE FROM PersistentToken t WHERE t.entity = :entity AND t.object = :object").setParameter("entity", entity).setParameter("object", object).executeUpdate();
	}

	@Override
	public String toJSON() {
		return this.getClass().getName();
	}
}
