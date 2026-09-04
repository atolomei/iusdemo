package io.demo.model.db.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.demo.model.DemoObjectMapper;
import io.demo.service.BaseService;
import io.demo.service.Settings;
import io.demo.service.SystemService;
import tools.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;

/**
 * 
 * @param <T> Type
 * @param <M> ModelType
 * @param <I> Type Index
 */

public abstract class BaseDBService<T, I> extends BaseService implements SystemService {

	@JsonIgnore
	static final private ObjectMapper mapper = new DemoObjectMapper();
 

	@JsonIgnore
	@Autowired
	private final CrudRepository<T, I> repository;

	@JsonIgnore
	@PersistenceContext
	private EntityManager entityManager;

	private static Map<Class<?>, BaseDBService<?, Long>> map = new HashMap<Class<?>, BaseDBService<?, Long>>();

	public static void register(Class<?> entityClass, BaseDBService<?, Long> dbService) {
		map.put(entityClass, dbService);
	}

	public static BaseDBService<?, Long> getDBService(Class<?> entityClass) {
		return map.get(entityClass);
	}

	public BaseDBService(CrudRepository<T, I> repository, Settings settings) {
		super(settings);
		this.repository = repository;
	}

	@Transactional
	public <S extends T> S save(S entity) {
		return repository.save(entity);
	}

	@Transactional
	public Optional<T> findById(I id) {
		return repository.findById(id);
	}

	@Transactional
	public boolean existsById(I id) {
		return repository.existsById(id);
	}

	@Transactional
	public Iterable<T> findAll() {
		return repository.findAll();
	}

	@Transactional
	public void delete(T c) {
		repository.delete(c);
	}

	@Transactional
	public void deleteById(I id) {
		repository.deleteById(id);
	}

	@Transactional
	public Iterable<T> findAllSorted() {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(getEntityClass());
		Root<T> root = cq.from(getEntityClass());
		cq.orderBy(cb.asc(cb.lower(root.get("name"))));

		return getEntityManager().createQuery(cq).getResultList();
	}

	@Transactional
	public List<T> getByName(String name) {
		return createNameQuery(name, false).getResultList();
	}

	@Transactional
	public List<T> getNameLike(String name) {
		return createNameQuery(name, true).getResultList();
	}

	public TypedQuery<T> createNameQuery(String name) {
		return createNameQuery(name, false);
	}

	public TypedQuery<T> createNameQuery(String name, boolean isLike) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(getEntityClass());
		Root<T> root = cq.from(getEntityClass());

		if (isLike)
			cq.select(root).where(cb.like(cb.lower(root.get(getNameColumn())), "%" + name.toLowerCase() + "%"));
		else
			cq.select(root).where(cb.equal(cb.lower(root.get(getNameColumn())), name.toLowerCase()));

		return getEntityManager().createQuery(cq);
	}

	public EntityManager getEntityManager() {
		return entityManager;
	}

	public CrudRepository<T, I> getRepository() {
		return repository;
	}

	protected abstract Class<T> getEntityClass();

	protected String getNameColumn() {
		return "name";
	}

	public String normalize(String name) {
		return this.getEntityClass().getSimpleName().toLowerCase() + "-" + name.toLowerCase().trim();
	}

	protected String nameKey(String name) {
		return name.toLowerCase().replaceAll("[^a-z0-9]+", "-") // Replace non-ASCII alphanumerics with hyphen
				.replaceAll("(^-+|-+$)", ""); // Trim leading/trailing hyphens
	}

}
