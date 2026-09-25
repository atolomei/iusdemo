package io.demo.web.user;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import io.demo.model.User;
import wktui.base.ModelPanel;

/**
 * Expanded panel with the info of a {@link User}: name, username, email,
 * phone, role.
 */
public class UserExpandedPanel extends ModelPanel<User> {

	private static final long serialVersionUID = 1L;

	public UserExpandedPanel(String id, IModel<User> model) {
		super(id, model);
		setOutputMarkupId(true);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		User user = getModel().getObject();

		StringBuilder name = new StringBuilder();
		if (user.getLastName() != null)
			name.append(user.getLastName());
		if (user.getFirstName() != null) {
			if (name.length() > 0)
				name.append(", ");
			name.append(user.getFirstName());
		}
		if (name.length() == 0 && user.getDisplayname() != null)
			name.append(user.getDisplayname());

		add(new Label("name", name.length() > 0 ? name.toString() : "-"));
		add(new Label("username", user.getUsername() != null ? user.getUsername() : "-"));
		add(new Label("email", user.getEmail() != null ? user.getEmail() : "-"));
		add(new Label("phone", user.getPhone() != null ? user.getPhone() : "-"));
		add(new Label("role", user.getRole() != null ? user.getRole().toString() : "-"));
	}
}
