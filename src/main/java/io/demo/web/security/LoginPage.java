package io.demo.web.security;

import java.util.Optional;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.wicketstuff.annotation.mount.MountPath;

import com.giffing.wicket.spring.boot.context.scan.WicketSignInPage;
import com.giffing.wicket.spring.boot.starter.configuration.extensions.external.spring.security.SecureWebSession;

import io.demo.Logger;
import io.demo.model.User;
import io.wktui.error.AlertPanel;
import io.wktui.form.Form;
import io.wktui.form.FormState;
import io.wktui.form.button.SubmitButton;
import io.wktui.form.field.Field;
import io.wktui.form.field.PasswordField;
import io.wktui.form.field.TextField;
import jakarta.servlet.http.HttpServletRequest;
import wktui.base.InvisiblePanel;
import wktui.bootstrap.Bootstrap;

/**
 * <p>
 * Sign in page. Users can sign in with their username, email or phone number.
 * </p>
 *
 * @author atolomei@novamens.com (Alejandro Tolomei)
 */
@WicketSignInPage
@MountPath("/signin")
public class LoginPage extends WebPage {

	private static final long serialVersionUID = 1L;

	static private Logger logger = Logger.getLogger(LoginPage.class.getName());

	private WebMarkupContainer alertContainer;

	private Form<User> form;

	private String username = "";
	private String password = "";

	private TextField<String> usernameField;
	private PasswordField passwordField;

	private boolean isError = false;

	public LoginPage() {
		super();
	}

	public LoginPage(PageParameters parameters) {
		super(parameters);
		if (getPageParameters() != null) {
			if (getPageParameters().get("error") != null) {
				StringValue s = getPageParameters().get("error");
				if (!s.isEmpty())
					isError = true;
			}
			if (getPageParameters().get("username") != null) {
				StringValue s = getPageParameters().get("username");
				if (!s.isEmpty())
					username = s.toString();
			}
		}
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(Bootstrap.getCssResourceReference()));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		if (getSession().isTemporary())
			getSession().bind();

		add(new Image("miniLogo", new PackageResourceReference(LoginPage.class, "kbee.png")));

		alertContainer = new WebMarkupContainer("alertContainer");
		add(alertContainer);

		if (isError()) {
			AlertPanel<Void> alert = new AlertPanel<Void>("alert", AlertPanel.DANGER, null, getLabel("username-password-invalid"));
			alert.setAlertTextContainerCss("col-xxl-12 col-xl-12 col-lg-12 col-md-12 col-sm-12 text-center");
			alertContainer.add(alert);
		} else {
			alertContainer.setVisible(false);
			alertContainer.add(new InvisiblePanel("alert"));
		}

		form = new Form<User>("loginForm") {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onInitialize() {
				super.onInitialize();
				if (getSession().isTemporary())
					getSession().bind();
			}

			@Override
			protected void onSubmit() {

				SecureWebSession session = (SecureWebSession) getSession();

				if (!session.signIn(username, password)) {
					ServletWebRequest servletWebRequest = (ServletWebRequest) RequestCycle.get().getRequest();
					HttpServletRequest httpRequest = (HttpServletRequest) servletWebRequest.getContainerRequest();
					String ipAddress = httpRequest.getRemoteAddr();
					logger.warn("Invalid username or password -> u." + username + " ip." + ipAddress);
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
					}
					getPageParameters().set("error", "Invalid username or password");
					setResponsePage(new LoginPage(getPageParameters()));
					return;
				}

				logger.debug("successful login -> " + username);

				// VERY IMPORTANT -> bind Wicket session immediately
				getSession().bind();

				// Force session creation at container level
				ServletWebRequest servletRequest = (ServletWebRequest) RequestCycle.get().getRequest();
				HttpServletRequest request = (HttpServletRequest) servletRequest.getContainerRequest();
				request.getSession(true);

				// Continue original destination OR go home
				continueToOriginalDestination();
				setResponsePage(getApplication().getHomePage());
			}

			@Override
			protected void onError() {
				getPageParameters().set("error", "Invalid username or password");
				setResponsePage(new LoginPage(getPageParameters()));
			}
		};

		usernameField = new TextField<String>("usernameoremail", new PropertyModel<String>(this, "username"), getLabel("username-email-phone"));
		passwordField = new PasswordField("password", new PropertyModel<String>(this, "password"), getLabel("password"));
		usernameField.setTitleCss("row mb-1");
		usernameField.setCss("text-center text-lg-center text-md-center text-sm-center text-xl-center textl-xxl-center form-control bg-dark text-light");
		passwordField.setCss("text-center text-lg-center text-md-center text-sm-center text-xl-center textl-xxl-center form-control bg-dark text-light");
		passwordField.setTitleCss("row mb-1");

		SubmitButton<User> buttons = new SubmitButton<User>("buttons-bottom", getForm()) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(AjaxRequestTarget target) {
			}

			public IModel<String> getLabel() {
				return LoginPage.this.getLabel("signin");
			}

			protected String getSaveCss() {
				return "btn text-light border-light btn-lg";
			}
		};
		form.add(buttons);
		form.add(usernameField);
		form.add(passwordField);
		add(form);
		edit();
	}

	protected StringResourceModel getLabel(String key) {
		return new StringResourceModel(key, this);
	}

	private boolean isError() {
		return this.isError;
	}

	private Form<User> getForm() {
		return form;
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
