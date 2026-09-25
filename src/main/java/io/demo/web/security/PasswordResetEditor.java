package io.demo.web.security;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

import io.demo.Logger;
import io.demo.model.User;
import io.demo.model.db.service.PersistentTokenDBService;
import io.demo.model.db.service.UserDBService;
import io.demo.service.ServiceLocator;
import io.demo.web.home.ObjectEditor;
import io.wktui.error.AlertPanel;
import io.wktui.error.SimpleAlertRow;
import io.wktui.form.Form;
import io.wktui.form.FormState;
import io.wktui.form.button.SubmitButton;
import io.wktui.form.field.Field;
import io.wktui.form.field.TextField;
import wktui.base.InvisiblePanel;

/**
 * Editor used by {@link PasswordResetPage} to set a new password for the
 * {@link User} identified by a valid {@link io.demo.model.PersistentToken}.
 * Modeled after the DellemuseServerApp PasswordResetEditor.
 */
public class PasswordResetEditor extends ObjectEditor<User> {

	private static final long serialVersionUID = 1L;

	static private Logger logger = Logger.getLogger(PasswordResetEditor.class.getName());

	/** the token consumed (deleted) once the password is reset */
	private Long persistentTokenId;

	private TextField<String> passwordField;

	private String newPassword;

	public PasswordResetEditor(String id, IModel<User> model, Long persistentTokenId) {
		super(id, model);
		this.persistentTokenId = persistentTokenId;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public boolean hasWritePermission() {
		return getModel() != null && getModel().getObject() != null;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		add(new InvisiblePanel("error"));
		add(new InvisiblePanel("success"));

		Form<User> form = new Form<User>("passwordForm", getModel());
		add(form);
		setForm(form);

		Label notice = new Label("notice",
				new StringResourceModel("notice", this).setParameters(getModel().getObject().getDisplayname(), getModel().getObject().getUsername()));
		notice.setEscapeModelStrings(false);
		add(notice);

		this.newPassword = "";

		form.setFormState(FormState.EDIT);

		this.passwordField = new TextField<String>("password", new PropertyModel<String>(PasswordResetEditor.this, "newPassword"), getLabel("new-password")) {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return getForm().getFormState() == FormState.EDIT;
			}

			@Override
			public boolean isEnabled() {
				if (!hasWritePermission())
					return false;
				return getForm().getFormState() == FormState.EDIT;
			}
		};
		form.add(passwordField);

		SubmitButton<User> sm = new SubmitButton<User>("send", getModel(), getForm()) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(AjaxRequestTarget target) {
				PasswordResetEditor.this.onSave(target);
			}

			@Override
			public boolean isEnabled() {
				return true;
			}

			public IModel<String> getLabel() {
				return PasswordResetEditor.this.getLabel("submit");
			}

			@Override
			public String getRowCss() {
				return "d-inline-block float-left";
			}

			@Override
			public String getColCss() {
				return "d-inline-block float-left";
			}

			@Override
			public String getSaveCss() {
				return "btn btn-primary btn-lg";
			}

			@Override
			public boolean isVisible() {
				return getForm().getFormState() == FormState.EDIT;
			}
		};

		getForm().add(sm);

		form.updateModel();
		form.updateReload();
		form.visitChildren(Field.class, new IVisitor<Field<?>, Void>() {
			@Override
			public void component(Field<?> field, IVisit<Void> visit) {
				field.editOn();
			}
		});
	}

	protected void onSave(AjaxRequestTarget target) {

		try {
			updateModel();

			if (getNewPassword() == null || getNewPassword().trim().isEmpty()) {
				addOrReplace(new SimpleAlertRow<String>("error", getLabel("enter-password")));
				target.add(this);
				return;
			}

			getUserDBService().updatePassword(getModel().getObject(), getNewPassword(), null);

			// consume the token so the reset link cannot be reused
			if (this.persistentTokenId != null)
				getPersistentTokenDBService().deleteById(this.persistentTokenId);

			getForm().setFormState(FormState.VIEW);

			addOrReplace(new SimpleAlertRow<String>("success",
					new StringResourceModel("password-reset-success", this).setParameters(getModel().getObject().getDisplayname(), getModel().getObject().getUsername()),
					AlertPanel.INFO));

			target.add(this);

		} catch (Exception e) {
			addOrReplace(new SimpleAlertRow<Void>("error", e));
			logger.error(e);
			target.add(this);
		}
	}

	protected void onCancel(AjaxRequestTarget target) {
		getForm().setFormState(FormState.VIEW);
		target.add(this);
	}

	protected UserDBService getUserDBService() {
		return (UserDBService) ServiceLocator.getInstance().getBean(UserDBService.class);
	}

	protected PersistentTokenDBService getPersistentTokenDBService() {
		return (PersistentTokenDBService) ServiceLocator.getInstance().getBean(PersistentTokenDBService.class);
	}
}
