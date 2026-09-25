package io.demo.web.security;

import java.util.Locale;
import java.util.Optional;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.wicketstuff.annotation.mount.MountPath;

import io.demo.App;
import io.demo.Logger;
import io.demo.model.PersistentToken;
import io.demo.model.User;
import io.demo.model.db.service.PersistentTokenDBService;
import io.demo.model.db.service.UserDBService;
import io.demo.web.page.BasePage;
import io.wktui.error.AlertPanel;
import wktui.base.InvisiblePanel;
import wktui.bootstrap.Bootstrap;

/**
 * <p>
 * Password reset page. The user arrives here via the link sent by email from
 * the {@link ForgotPasswordPage}. The URL contains a token of the form
 * {@code {userid}-{token}-{lang}}; if the token is valid (exists and is not
 * expired) a {@link PasswordResetEditor} is displayed so the user can enter a
 * new password.
 * </p>
 *
 * @author atolomei@novamens.com (Alejandro Tolomei)
 */
@MountPath("/password-reset/${token}")
public class PasswordResetPage extends BasePage {

	private static final long serialVersionUID = 1L;

	static private Logger logger = Logger.getLogger(PasswordResetPage.class.getName());

	@SpringBean
	private UserDBService userDBService;

	@SpringBean
	private PersistentTokenDBService persistentTokenDBService;

	private String token;
	private String userid;
	private String tokenstr;
	private String lang;

	public PasswordResetPage() {
		this(new PageParameters());
	}

	public PasswordResetPage(PageParameters parameters) {
		super(parameters);

		token = getPageParameters().get("token").toString();

		if (token == null || token.length() == 0)
			return;

		String arr[] = token.split("-");

		if (arr.length != 3)
			return;

		userid = arr[0];
		tokenstr = arr[1];
		lang = arr[2];

		if (lang == null)
			this.lang = Locale.getDefault().getLanguage();

		getSession().setLocale(Locale.forLanguageTag(lang));
	}

	@Override
	public Locale getLocale() {
		if (lang == null)
			return Locale.getDefault();
		return Locale.forLanguageTag(lang);
	}

	@Override
	public boolean canAccess(Optional<User> user) {
		// public page: accessed from the reset link sent by email
		return true;
	}

	@Override
	protected void addListeners() {
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(Bootstrap.getCssResourceReference()));
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		add(new Image("miniLogo", new PackageResourceReference(App.class, "pjsf.png")));

		add(new InvisiblePanel("editor"));
		add(new InvisiblePanel("error"));

		if (token == null || token.length() == 0) {
			addOrReplace(new AlertPanel<Void>("error", AlertPanel.DANGER, new StringResourceModel("invalidtoken", this, null)));
			return;
		}

		if (userid == null || tokenstr == null || lang == null) {
			addOrReplace(new AlertPanel<Void>("error", AlertPanel.DANGER, new StringResourceModel("invalidtoken", this, null)));
			return;
		}

		try {

			Iterable<PersistentToken> opt = getPersistentTokenDBService().findByToken(tokenstr);

			if (opt == null) {
				addOrReplace(new AlertPanel<Void>("error", AlertPanel.DANGER, new StringResourceModel("invalidtoken", this, null)));
				return;
			}

			for (PersistentToken t : opt) {
				if (t.getEntityClass().equals(User.class.getSimpleName()) && t.getEntity().equals(userid)) {

					// check that the token has not expired
					if (t.getExpires() != null && t.getExpires().isBefore(java.time.OffsetDateTime.now())) {
						addOrReplace(new AlertPanel<Void>("error", AlertPanel.DANGER, new StringResourceModel("expiredtoken", this, null)));
						return;
					}

					User u = getUserDBService().findById(Long.parseLong(userid)).orElse(null);

					if (u != null) {
						addOrReplace(new PasswordResetEditor("editor", new io.demo.web.panel.ObjectModel<User>(u), t.getId()));
						return;
					}
				}
			}

		} catch (Exception e) {
			logger.error(e);
		}

		addOrReplace(new AlertPanel<Void>("error", AlertPanel.DANGER, new StringResourceModel("invalidtoken", this, null)));
	}

	public UserDBService getUserDBService() {
		return userDBService;
	}

	private PersistentTokenDBService getPersistentTokenDBService() {
		return persistentTokenDBService;
	}
}
