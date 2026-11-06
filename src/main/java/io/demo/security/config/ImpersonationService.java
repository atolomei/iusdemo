package io.demo.security.config;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import io.demo.Logger;
import io.demo.model.User;
import io.demo.model.db.service.UserDBService;
import io.demo.service.BaseService;
import io.demo.service.ServiceLocator;
import io.demo.service.Settings;
import io.demo.service.SystemService;
import io.demo.web.WebSessionUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * <p>
 * Allows the {@code root} user to sign in as another user (impersonation),
 * without knowing that user's password. Restricted to the {@code root} user
 * only (see {@link #impersonate(String, HttpServletRequest, HttpServletResponse)}).
 * </p>
 *
 * @author atolomei@novamens.com (Alejandro Tolomei)
 */
@Service
public class ImpersonationService extends BaseService implements SystemService {

	static private Logger logger = Logger.getLogger(ImpersonationService.class.getName());

	private final UserDetailsService userDetailsService;

	public ImpersonationService(Settings settings, UserDetailsService userDetailsService) {
		super(settings);
		this.userDetailsService = userDetailsService;
	}

	/**
	 * Replaces the current {@link SecurityContext} authentication with one for
	 * {@code targetUsername}, so that the rest of the request/session proceeds
	 * as if that user had signed in. Only the {@code root} user is allowed to
	 * impersonate.
	 *
	 * @throws SecurityException if the currently authenticated user is not
	 *                            {@code root}.
	 */
	public void impersonate(String targetUsername, HttpServletRequest request, HttpServletResponse response) {

		Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
		String adminIdentifier = currentAuth != null ? currentAuth.getName() : null;

		if (adminIdentifier == null || !"root".equals(adminIdentifier)) {
			logger.warn("Impersonation denied: caller is not root -> " + adminIdentifier);
			throw new SecurityException("Only the root user can impersonate another user");
		}

		// load target user
		UserDetails targetUser = userDetailsService.loadUserByUsername(targetUsername);

		// create new authentication
		Authentication newAuth = new UsernamePasswordAuthenticationToken(targetUser, targetUser.getPassword(), targetUser.getAuthorities());

		// replace authentication
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(newAuth);
		SecurityContextHolder.setContext(context);

		// persist in session
		new HttpSessionSecurityContextRepository().saveContext(context, request, response);

		// invalidate the cached Wicket-session user so it is re-resolved from the
		// new SecurityContext on next access
		WebSessionUser.invalidate();

		logger.debug("IMPERSONATION: root -> " + targetUsername);
	}

	public boolean isRoot(User u) {
		return u != null && u.getName() != null && u.getName().equals("root");
	}

	protected UserDBService getUserDBService() {
		return (UserDBService) ServiceLocator.getInstance().getBean(UserDBService.class);
	}

	@Override
	public String toJSON() {
		return null;
	}
}
