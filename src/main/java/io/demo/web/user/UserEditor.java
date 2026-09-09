package io.demo.web.user;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.util.ListModel;

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
import io.wktui.form.field.ChoiceField;
import io.wktui.form.field.TextField;
import wktui.base.InvisiblePanel;

/**
 * Editor of a {@link User}: name, lastname, username, email, phone and role.
 */
public class UserEditor extends ObjectEditor<User> {

	private static final long serialVersionUID = 1L;

	static private Logger logger = Logger.getLogger(UserEditor.class.getName());

	private TextField<String> firstNameField;
	private TextField<String> lastNameField;
	private TextField<String> usernameField;
	private TextField<String> emailField;
	private TextField<String> phoneField;
	private ChoiceField<Role> roleField;

	public UserEditor(String id, IModel<User> model) {
		super(id, model);
	}

	public boolean hasWritePermission() {

		Optional<User> ouser = getSessionUser();

		if (ouser.isEmpty())
			return false;

		// session user is editing himself
		if (ouser.get().getId().equals(getModel().getObject().getId()))
			return true;

		Role role = ouser.get().getRole();
		return role == Role.SYSADMIN || role == Role.ADMIN;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		add(new InvisiblePanel("error"));

		Form<User> form = new Form<User>("userForm", getModel());
		add(form);
		setForm(form);

		this.firstNameField = new TextField<String>("firstName", new PropertyModel<String>(getModel(), "firstName"), getLabel("firstName"));
		this.lastNameField = new TextField<String>("lastName", new PropertyModel<String>(getModel(), "lasttName"), getLabel("lastName"));
		this.usernameField = new TextField<String>("username", new PropertyModel<String>(getModel(), "name"), getLabel("username"));
		this.emailField = new TextField<String>("email", new PropertyModel<String>(getModel(), "email"), getLabel("email"));
		this.phoneField = new TextField<String>("phone", new PropertyModel<String>(getModel(), "phone"), getLabel("phone"));

		this.roleField = new ChoiceField<Role>("role", new PropertyModel<Role>(getModel(), "role"), getLabel("role")) {
			private static final long serialVersionUID = 1L;

			@Override
			public IModel<List<Role>> getChoices() {
				return new ListModel<Role>(Arrays.asList(Role.values()));
			}
		};

		// root's username can not be changed
		if (getModel().getObject().getName() != null && getModel().getObject().getName().equals("root"))
			this.usernameField.setReadOnly(true);

		form.add(firstNameField);
		form.add(lastNameField);
		form.add(usernameField);
		form.add(emailField);
		form.add(phoneField);
		form.add(roleField);

		form.setFormState(FormState.VIEW);

		EditButtons<User> buttons = new EditButtons<User>("buttons", form, getModel()) {

			private static final long serialVersionUID = 1L;

			public void onEdit(AjaxRequestTarget target) {
				UserEditor.this.onEdit(target);
			}

			public void onCancel(AjaxRequestTarget target) {
				UserEditor.this.onCancel(target);
			}

			public void onSave(AjaxRequestTarget target) {
				UserEditor.this.onSave(target);
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
			getForm().setFormState(FormState.VIEW);
			submit(target);

			User user = getModelObject();
			user.setEmail(User.normalizeEmail(user.getEmail()));
			user.setPhone(User.normalizePhone(user.getPhone()));

			getUserDBService().save(user, getSessionUser().orElse(null));
			getForm().updateReload();

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
