package io.demo.web;

import java.util.Optional;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Session;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import io.demo.Logger;
import io.demo.model.User;
import io.demo.model.db.service.UserDBService;
import io.demo.service.ServiceLocator;

/**
 * Web-layer helper that resolves the domain {@link User} of the current
 * session from the Spring Security context and caches it in the Wicket
 * session metadata.
 * <p>
 * This class is the single bridge between the Wicket/Spring Security web
 * layer and the domain model: pages and panels obtain the {@link User} (a
 * pure domain object) and the session id here, and pass them to the
 * services layer as plain arguments. Services never see anything related
 * to Wicket or Spring Security.
 * </p>
 *
 * <pre>
 * // in any Panel or Page:
 * Optional&lt;User&gt; user = WebSessionUser.get();
 * String sessionId = WebSessionUser.getSessionId();
 * service.put(docId, query, result, user.orElseThrow(), sessionId);
 * </pre>
 */
public final class WebSessionUser {

	static private Logger logger = Logger.getLogger(WebSessionUser.class.getName());

	/**
	 * Session metadata key caching the domain user's id for the session.
	 * Only the id (a {@link Long}) is stored in the session: the entity
	 * itself is not {@code Serializable} and is reloaded via the
	 * {@link UserDBService} on each access.
	 */
	private static final MetaDataKey<Long> SESSION_USER_ID = new MetaDataKey<Long>() {
		private static final long serialVersionUID = 1L;
	};

	private WebSessionUser() {
	}

	/**
	 * Returns the domain {@link User} of the current session, resolving it
	 * from the Spring Security {@link Authentication} on first access and
	 * caching its id in the Wicket session afterwards.
	 */
	public static Optional<User> get() {

		Session session = Session.get();

		Long cachedId = session.getMetaData(SESSION_USER_ID);
		if (cachedId != null) {
			Optional<User> user = getUserDBService().findById(cachedId);
			if (user.isPresent())
				return user;
			// user removed since it was cached -> fall through and re-resolve
			session.setMetaData(SESSION_USER_ID, null);
		}

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth == null || !auth.isAuthenticated())
			return Optional.empty();

		if (auth instanceof AnonymousAuthenticationToken)
			return Optional.empty();

		if ("anonymousUser".equals(auth.getName()))
			return Optional.empty();

		Optional<User> o_user = getUserDBService().findByUsernameOrEmailOrPhone(auth.getName());

		if (o_user.isEmpty()) {
			logger.error("Authenticated principal not found in database -> " + auth.getName());
			return Optional.empty();
		}

		session.setMetaData(SESSION_USER_ID, o_user.get().getId());
		return o_user;
	}

	private static UserDBService getUserDBService() {
		return (UserDBService) ServiceLocator.getInstance().getBean(UserDBService.class);
	}

	/**
	 * Returns the user of the current session or throws if nobody is
	 * signed in. Use when the caller requires an authenticated user.
	 */
	public static User require() {
		return get().orElseThrow(() -> new IllegalStateException("no authenticated user in session"));
	}

	/**
	 * Returns the id of the current Wicket session, binding it first if it
	 * is still temporary (a temporary session has no id yet).
	 */
	public static String getSessionId() {
		Session session = Session.get();
		if (session.isTemporary())
			session.bind();
		return session.getId();
	}

	/** Removes the cached user id (e.g. after profile changes or sign-out). */
	public static void invalidate() {
		Session.get().setMetaData(SESSION_USER_ID, null);
	}
}
