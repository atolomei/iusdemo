package io.demo.web.user;

import java.util.Optional;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import io.demo.Logger;
import io.demo.model.Role;
import io.demo.model.User;
import io.demo.model.db.service.UserDBService;
import io.demo.service.ServiceLocator;
import io.demo.web.home.ObjectEditor;
import io.demo.web.page.BasePage;
import io.wktui.error.SimpleAlertRow;
import io.wktui.form.Form;
import io.wktui.form.FormState;
import io.wktui.form.button.EditButtons;
import io.wktui.form.field.StaticTextField;
import io.wktui.form.field.TextField;
import wktui.base.InvisiblePanel;

/**
 * Editor to change a {@link User}'s password.
 */
public class UserPasswordEditor extends ObjectEditor<User> {

	private static final long serialVersionUID = 1L;

	static private Logger logger = Logger.getLogger(UserPasswordEditor.class.getName());

	private StaticTextField<String> usernameField;
	private TextField<String> passwordField;

	private String newPassword;

	public UserPasswordEditor(String id, IModel<User> model) {
		super(id, model);
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public boolean hasWritePermission() {

		Optional<User> ouser = getSessionUser();

		if (ouser.isEmpty())
			return false;

		
		if (isRoot())
			return true;
		
		if (ouser.get().getId().equals(getModel().getObject().getId()))
			return true;

		Role role = ouser.get().getRole();
		return role == Role.SYSADMIN || role == Role.ADMIN;
	}

	protected boolean isRoot() {
		return getUserDBService().isRoot(getModel().getObject());
	}

	
	@Override
	public void onInitialize() {
		super.onInitialize();

		add(new InvisiblePanel("error"));

		this.newPassword = "";

		Form<User> form = new Form<User>("passwordForm", getModel());
		add(form);
		setForm(form);

		form.setFormState(FormState.VIEW);

		this.usernameField = new StaticTextField<String>("username", new PropertyModel<String>(getModel(), "name"), getLabel("username"));

		this.passwordField = new TextField<String>("password", new PropertyModel<String>(UserPasswordEditor.this, "newPassword"), getLabel("new-password")) {

			private static final long serialVersionUID = 1L;

			public boolean isVisible() {
				return getForm().getFormState() == FormState.EDIT;
			}

			public boolean isEnabled() {
				if (!hasWritePermission())
					return false;
				return getForm().getFormState() == FormState.EDIT;
			}
		};

		form.add(usernameField);
		form.add(passwordField);

		EditButtons<User> buttons = new EditButtons<User>("buttons", form, getModel()) {

			private static final long serialVersionUID = 1L;

			public void onEdit(AjaxRequestTarget target) {
				UserPasswordEditor.this.onEdit(target);
			}

			public void onCancel(AjaxRequestTarget target) {
				UserPasswordEditor.this.onCancel(target);
			}

			public void onSave(AjaxRequestTarget target) {
				UserPasswordEditor.this.onSave(target);
			}

			protected String getSaveClass() {
				return "btn btn-primary btn-sm";
			}

			protected String getCancelClass() {
				return "btn btn-sm btn-outline-primary";
			}

			@Override
			public boolean isVisible() {
				if (!hasWritePermission())
					return false;
				return getForm().getFormState() == FormState.EDIT;
			}
		};

		form.add(buttons);
	}

	public void onEdit(AjaxRequestTarget target) {
		super.edit(target);
	}

	protected void onCancel(AjaxRequestTarget target) {
		super.cancel(target);
	}

	protected void onSave(AjaxRequestTarget target) {

		try {
			updateModel();

			if (getNewPassword() != null && !getNewPassword().trim().isEmpty()) {
				getUserDBService().updatePassword(getModel().getObject(), getNewPassword(), getSessionUser().orElse(null));
			}

			getForm().setFormState(FormState.VIEW);
			submit(target);

		} catch (Exception e) {
			addOrReplace(new SimpleAlertRow<Void>("error", e));
			logger.error(e);
		}
		target.add(this);
	}

	protected Optional<User> getSessionUser() {
		if (getPage() instanceof BasePage)
			return ((BasePage) getPage()).getSessionUser();
		return Optional.empty();
	}

	protected UserDBService getUserDBService() {
		return (UserDBService) ServiceLocator.getInstance().getBean(UserDBService.class);
	}
}
