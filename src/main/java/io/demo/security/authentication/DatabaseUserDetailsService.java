package io.demo.security.authentication;

import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import io.demo.Logger;
import io.demo.model.User;
import io.demo.model.db.service.UserDBService;

/**
 * <p>
 * {@link UserDetailsService} backed by the application database. Users can
 * sign in with their <b>username</b>, <b>email</b> or <b>phone number</b>.
 * </p>
 *
 * @author atolomei@novamens.com (Alejandro Tolomei)
 */
@Service
public class DatabaseUserDetailsService implements UserDetailsService {

	static private Logger logger = Logger.getLogger(DatabaseUserDetailsService.class.getName());

	private final UserDBService userDBService;

	public DatabaseUserDetailsService(UserDBService userDBService) {
		this.userDBService = userDBService;
	}

	private boolean looksLikeEmail(String v) {
		return v.contains("@");
	}

	private boolean looksLikePhone(String v) {
		return v.matches("^[0-9+()\\-\\s]{6,20}$");
	}

	@Override
	public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {

		Optional<User> oUser;

		if (looksLikeEmail(identifier)) {
			oUser = getUserDBService().findByEmail(User.normalizeEmail(identifier));
		} else if (looksLikePhone(identifier)) {
			oUser = getUserDBService().findByPhone(User.normalizePhone(identifier));
		} else {
			oUser = getUserDBService().findByUsernameOrEmailOrPhone(identifier);
		}

		if (oUser.isEmpty())
			throw new UsernameNotFoundException("User not found for identifier -> " + identifier);

		User user = oUser.get();

		logger.debug("DB User -> " + user.getUsername() + " | identifier -> " + identifier);

		return new org.springframework.security.core.userdetails.User(
				user.getUsername(),
				user.getPassword(),
				user.getRolesAsString().stream()
						.map(SimpleGrantedAuthority::new)
						.collect(Collectors.toSet()));
	}

	private UserDBService getUserDBService() {
		return this.userDBService;
	}
}
