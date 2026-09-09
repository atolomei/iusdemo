package io.demo.web.security;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.wicketstuff.annotation.mount.MountPath;

import io.demo.Logger;
import io.demo.email.EmailService;
import io.demo.email.EmailTemplateService;
import io.demo.model.PersistentToken;
import io.demo.model.User;
import io.demo.model.db.service.PersistentTokenDBService;
import io.demo.model.db.service.UserDBService;
import io.wktui.error.AlertPanel;
import io.wktui.error.ErrorPanel;
import io.wktui.form.Form;
import io.wktui.form.FormState;
import io.wktui.form.button.SubmitButton;
import io.wktui.form.field.Field;
import io.wktui.form.field.TextField;
import wktui.base.InvisiblePanel;
import wktui.bootstrap.Bootstrap;

/**
 * <p>
 * Forgot Password page. The user enters their email / username / phone and the
 * system sends an email with password reset instructions.
 * </p>
 *
 * @author atolomei@novamens.com (Alejandro Tolomei)
 */
@MountPath("/forgot")
public class ForgotPasswordPage extends WebPage {

	private static final long serialVersionUID = 1L;

	static private Logger logger = Logger.getLogger(ForgotPasswordPage.class.getName());

	private static final SecureRandom RANDOM = new SecureRandom();

	private Form<User> form;

	private String username = "";

	private TextField<String> userNameField;

	@SpringBean
	private UserDBService userDBService;

	@SpringBean
	private PersistentTokenDBService persistentTokenDBService;

	@SpringBean
	private EmailService emailService;

	@SpringBean
	private EmailTemplateService emailTemplateService;

	public ForgotPasswordPage() {
		super();
	}

	public ForgotPasswordPage(PageParameters parameters) {
		super(parameters);
		if (getPageParameters() != null && getPageParameters().get("username") != null)
			username = getPageParameters().get("username").toString("");
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(Bootstrap.getCssResourceReference()));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new Image("miniLogo", new PackageResourceReference(ForgotPasswordPage.class, "kbee.png")));

		add(new InvisiblePanel("alert"));

		form = new Form<User>("forgotForm") {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit() {
			}

			@Override
			protected void onError() {
			}
		};

		userNameField = new TextField<String>("username", new PropertyModel<String>(this, "username"), getLabel("username-email-phone"));
		userNameField.setTitleCss("row mb-1");
		userNameField.setCss("text-center text-lg-center text-md-center text-sm-center text-xl-center textl-xxl-center form-control bg-dark text-light");

		SubmitButton<User> buttons = new SubmitButton<User>("buttons-bottom", getForm()) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(AjaxRequestTarget target) {

				if (username == null || username.trim().isEmpty()) {
					ForgotPasswordPage.this.addOrReplace(new AlertPanel<Void>("alert", AlertPanel.WARNING, null, ForgotPasswordPage.this.getLabel("enter-username-email-phone")));
					target.add(ForgotPasswordPage.this);
					return;
				}

				// lookup user by username / email / phone
				Optional<User> ou = getUserDBService().findByUsernameOrEmailOrPhone(username);

				if (ou.isPresent()) {

					User u = ou.get();

					String email = u.getEmail();
					String personName = u.getDisplayname();

					if (email != null && email.trim().length() > 0) {

						String tokenValue = nextSecureToken();

						PersistentToken token = getPersistentTokenDBService().create(u.getId().toString(), User.class.getSimpleName(), tokenValue, OffsetDateTime.now().plusDays(1));

						Long tid = null;

						try {
							tid = token.getId();

							String subject = ForgotPasswordPage.this.getLabel("password-reset-subject").getObject();
							String url = getServerUrl() + "/password-reset/" + u.getId().toString() + "-" + tokenValue + "-" + getLocale().getLanguage();

							String text = getEmailTemplateService().render(EmailTemplateService.PASSWORD_RESET, Map.of("application", "demo", "personName", personName, "resetLink", url));

							logger.debug("Sending email to -> " + email);

							String sendEmail = getEmailService().sendHTML(email, subject, text);

							logger.debug("Email sent response -> " + sendEmail);

						} catch (Exception e) {
							if (tid != null)
								getPersistentTokenDBService().findById(tid).ifPresent(t -> getPersistentTokenDBService().delete(t));
							ForgotPasswordPage.this.addOrReplace(new ErrorPanel("alert", e));
							logger.error(e);
							target.add(ForgotPasswordPage.this);
							return;
						}

						String css = "col-xxl-12 col-xl-12 col-lg-12 col-md-12 col-sm-12 text-center text-lg-center text-xl-center text-xxl-center";
						AlertPanel<Void> s = new AlertPanel<Void>("alert", AlertPanel.SUCCESS, null, ForgotPasswordPage.this.getLabel("email-sent"));
						s.setAlertTextContainerCss(css);
						ForgotPasswordPage.this.addOrReplace(s);

					} else {
						ForgotPasswordPage.this.addOrReplace(new AlertPanel<Void>("alert", AlertPanel.DANGER, null, ForgotPasswordPage.this.getLabel("no-email-associated")));
					}
				} else {
					ForgotPasswordPage.this.addOrReplace(new AlertPanel<Void>("alert", AlertPanel.DANGER, null, ForgotPasswordPage.this.getLabel("no-user-found")));
				}

				target.add(ForgotPasswordPage.this);
			}

			public IModel<String> getLabel() {
				return ForgotPasswordPage.this.getLabel("send");
			}

			protected String getSaveCss() {
				return "btn text-light border-light btn-lg";
			}
		};
		form.add(buttons);
		form.add(userNameField);
		add(form);

		edit();
	}

	protected StringResourceModel getLabel(String key) {
		return new StringResourceModel(key, this);
	}

	private String nextSecureToken() {
		byte[] bytes = new byte[32];
		RANDOM.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private String getServerUrl() {
		String protocol = ((WebRequest) RequestCycle.get().getRequest()).getUrl().getProtocol();
		String host = ((WebRequest) RequestCycle.get().getRequest()).getUrl().getHost();
		Integer iport = ((WebRequest) RequestCycle.get().getRequest()).getUrl().getPort();
		String port = (iport == null || iport.equals(80) || iport.equals(443)) ? "" : (":" + iport.toString());
		return protocol + "://" + host + port;
	}

	private Form<User> getForm() {
		return form;
	}

	private UserDBService getUserDBService() {
		return userDBService;
	}

	private PersistentTokenDBService getPersistentTokenDBService() {
		return persistentTokenDBService;
	}

	private EmailService getEmailService() {
		return emailService;
	}

	private EmailTemplateService getEmailTemplateService() {
		return emailTemplateService;
	}

	public void edit() {
		getForm().setFormState(FormState.EDIT);
		getForm().visitChildren(Field.class, new IVisitor<Field<?>, Void>() {
			@Override
			public void component(Field<?> field, IVisit<Void> visit) {
				field.editOn();
			}
		});
	}
}
