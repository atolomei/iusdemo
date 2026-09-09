package io.demo.model.db.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.demo.Logger;
import io.demo.model.ObjectState;
import io.demo.model.Role;
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

	@Autowired
	@JsonIgnore
	private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;


	public UserDBService(CrudRepository<User, Long> repository, Settings settings) {
		super(repository, settings);
	}


	
	@Override
	public String toJSON() {
		return null;
	}

	/**
	 * Creates a new User with the given username (stored in {@code name}).
	 */
	@Transactional
	public User create(String username, User createdBy) {

		User user = new User();
		user.setName(username);

		user.setShowWelcome(true);
		user.setCreated(OffsetDateTime.now());
		user.setLastModified(OffsetDateTime.now());
		user.setLastModifiedUser(createdBy);
		user.setState(ObjectState.EDITION);
		user.setRole(Role.REGULAR_USER);

		String hash = passwordEncoder.encode("demo");
		user.setPassword(hash);

		return getRepository().save(user);
	}

	/**
	 * Sets a new password for the given user. The password is stored encoded with
	 * the application's {@code PasswordEncoder} (delegating encoder, i.e. hash is
	 * prefixed with {@code {bcrypt}}), which is the format expected by Spring
	 * Security at login time.
	 */
	@Transactional
	public User updatePassword(User user, String rawPassword, User modifiedBy) {
		user.setPassword(passwordEncoder.encode(rawPassword));
		user.setLastModified(OffsetDateTime.now());
		user.setLastModifiedUser(modifiedBy);
		return getRepository().save(user);
	}

	@Transactional
	public String generateUserName(String name, String lastName) {

		if (name == null || name.trim().isEmpty() || lastName == null || lastName.trim().isEmpty()) {
			throw new IllegalArgumentException("Name and last name can not be empty");
		}

		String cleanName = name.toLowerCase().trim();
		String cleanLastName = lastName.toLowerCase().trim().replaceAll("\\s+", "");

		// 1. first letter of name plus lastname
		String username = cleanName.substring(0, 1) + cleanLastName;
		if (findByUsername(username).isEmpty())
			return username;

		// 2. name dot lastname
		username = cleanName + "." + cleanLastName;
		if (findByUsername(username).isEmpty())
			return username;

		// 3. name dot lastname plus a number
		int counter = 1;
		while (true) {
			String numberedUsername = username + counter;
			if (findByUsername(numberedUsername).isEmpty())
				return numberedUsername;
			counter++;
		}
	}

	@Transactional
	@Override
	public void markAsDeleted(User c, User deletedBy) {
		if (c.getName() != null && c.getName().equals("root"))
			throw new IllegalArgumentException("root user can not be deleted");
		super.markAsDeleted(c, deletedBy);
	}

	@Transactional
	@Override
	public void delete(User o, User by) {
		if (o.getName() != null && o.getName().equals("root"))
			throw new IllegalArgumentException("root user can not be deleted");
		super.delete(o, by);
	}

	@Transactional
	public Optional<User> findByUsernameOrEmailOrPhone(String value) {

		Optional<User> user_n = findByUsername(value);
		if (user_n.isPresent())
			return user_n;

		Optional<User> user_e = findByEmail(value);
		if (user_e.isPresent())
			return user_e;

		return findByPhone(value);
	}

	@Transactional
	public Optional<User> findByUsernameOrEmail(String value) {
		Optional<User> user = findByUsername(value);
		if (user.isPresent())
			return user;
		return findByEmail(value);
	}

	@Transactional
	public Optional<User> findByEmail(String email) {
		return findByField("email", User.normalizeEmail(email));
	}

	@Transactional
	public Optional<User> findByPhone(String phone) {
		return findByField("phone", User.normalizePhone(phone));
	}

	private Optional<User> findByField(String field, String value) {

		if (value == null)
			return Optional.empty();

		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<User> criteria = cb.createQuery(getEntityClass());
		Root<User> root = criteria.from(getEntityClass());

		ParameterExpression<String> param = cb.parameter(String.class);
		criteria.select(root).where(cb.equal(root.get(field), param));

		TypedQuery<User> query = entityManager.createQuery(criteria);
		query.setHint("org.hibernate.cacheable", true);
		query.setFlushMode(FlushModeType.COMMIT);
		query.setParameter(param, value);
		List<User> users = query.getResultList();
		if (users != null && !users.isEmpty())
			return Optional.of(users.get(0));
		return Optional.empty();
	}

	@Transactional
	public List<User> findByRole(Role role) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<User> cq = cb.createQuery(getEntityClass());
		Root<User> root = cq.from(getEntityClass());
		cq.select(root).where(cb.equal(root.get("role"), role));
		return getEntityManager().createQuery(cq).getResultList();
	}

	/** All users sorted by lastname, firstname */
	@Transactional
	@Override
	public Iterable<User> findAllSorted() {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<User> cq = cb.createQuery(getEntityClass());
		Root<User> root = cq.from(getEntityClass());
		cq.orderBy(cb.asc(cb.lower(cb.coalesce(root.get("lasttName"), ""))), cb.asc(cb.lower(cb.coalesce(root.get("firstName"), ""))));
		return getEntityManager().createQuery(cq).getResultList();
	}

	@Transactional
	@Override
	public Iterable<User> findAllSorted(ObjectState os) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<User> cq = cb.createQuery(getEntityClass());
		Root<User> root = cq.from(getEntityClass());
		cq.select(root).where(cb.equal(root.get("state"), os));
		cq.orderBy(cb.asc(cb.lower(cb.coalesce(root.get("lasttName"), ""))), cb.asc(cb.lower(cb.coalesce(root.get("firstName"), ""))));
		return getEntityManager().createQuery(cq).getResultList();
	}

	@Transactional
	@Override
	public Iterable<User> findAllSorted(ObjectState os1, ObjectState os2) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<User> cq = cb.createQuery(getEntityClass());
		Root<User> root = cq.from(getEntityClass());
		Predicate p1 = cb.equal(root.get("state"), os1);
		Predicate p2 = cb.equal(root.get("state"), os2);
		cq.select(root).where(cb.or(p1, p2));
		cq.orderBy(cb.asc(cb.lower(cb.coalesce(root.get("lasttName"), ""))), cb.asc(cb.lower(cb.coalesce(root.get("firstName"), ""))));
		return getEntityManager().createQuery(cq).getResultList();
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


	@PostConstruct
	protected void onInitialize() {
		super.register(getEntityClass(), this);
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



	public boolean isRoot(User u) {
		return u.getName() != null && u.getName().equals("root");
	}


}
