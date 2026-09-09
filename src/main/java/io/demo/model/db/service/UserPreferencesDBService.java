package io.demo.model.db.service;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import io.demo.Logger;
import io.demo.model.User;
import io.demo.model.UserPreferences;
import io.demo.service.Settings;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;

@Service
public class UserPreferencesDBService extends DBService<UserPreferences, Long> {

	@SuppressWarnings("unused")
	static private Logger logger = Logger.getLogger(UserPreferencesDBService.class.getName());

	public UserPreferencesDBService(CrudRepository<UserPreferences, Long> repository, Settings settings) {
		super(repository, settings);
	}

	 

	@Transactional
	public Optional<UserPreferences> getByUser(User user) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<UserPreferences> cq = cb.createQuery(UserPreferences.class);
		Root<UserPreferences> root = cq.from(UserPreferences.class);
		cq.select(root).where(cb.equal(root.get("luser"), user));
		TypedQuery<UserPreferences> q = getEntityManager().createQuery(cq);
		List<UserPreferences> list = q.getResultList();
		return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
	}

	@Override
	public String toJSON() {
		return null;
	}

	@Override
	protected Class<UserPreferences> getEntityClass() {
		return UserPreferences.class;
	}



	@PostConstruct
	protected void onInitialize() {
		super.register(getEntityClass(), this);
	}
	

}
