package io.demo.model.db.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.demo.Logger;
import io.demo.model.User;
import io.demo.service.Settings;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.ParameterExpression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;

@Service
public class UserDBService extends DBService<User, Long> {

	static private Logger logger = Logger.getLogger(UserDBService.class.getName());

	@Autowired
	@PersistenceContext
	private EntityManager entityManager;


	public UserDBService(CrudRepository<User, Long> repository, Settings settings) {
		super(repository, settings);
	}


	
	@Override
	public String toJSON() {
		return null;
	}


	@Transactional
	public User findRoot() {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<User> criteria = cb.createQuery(getEntityClass());
		Root<User> root = criteria.from(getEntityClass());

		ParameterExpression<String> nameParam = cb.parameter(String.class);
		criteria.select(root).where(cb.equal(root.get(getNameColumn()), nameParam));

		TypedQuery<User> query = entityManager.createQuery(criteria);
		query.setHint("org.hibernate.cacheable", true);
		query.setFlushMode(FlushModeType.COMMIT);
		query.setParameter(nameParam, "root");
		List<User> users = query.getResultList();
		if (users != null && !users.isEmpty()) {
			return users.get(0);
		}
		throw new RuntimeException("Database does not have user with name=='root'");
	}

	
	@Override
	protected Class<User> getEntityClass() {
		return User.class;
	}



	@Transactional
	public Optional<User> findByUsername(String username) {

		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<User> criteria = cb.createQuery(getEntityClass());
		Root<User> root = criteria.from(getEntityClass());

		ParameterExpression<String> nameParam = cb.parameter(String.class);
		criteria.select(root).where(cb.equal(root.get(getNameColumn()), nameParam));

		TypedQuery<User> query = entityManager.createQuery(criteria);
		query.setHint("org.hibernate.cacheable", true);
		query.setFlushMode(FlushModeType.COMMIT);
		query.setParameter(nameParam, username);
		List<User> users = query.getResultList();
		if ((users != null) && (!users.isEmpty())) {
			return Optional.of(users.get(0));
		}
		return Optional.empty();
	}


}
