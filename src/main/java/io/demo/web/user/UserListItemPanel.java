package io.demo.web.user;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import io.demo.model.User;
import wktui.base.InvisiblePanel;
import wktui.base.ModelPanel;

/**
 * List item of a {@link User}: "lastname, firstname (username)" plus a
 * contextual menu.
 */
public abstract class UserListItemPanel extends ModelPanel<User> {

	private static final long serialVersionUID = 1L;

	public UserListItemPanel(String id, IModel<User> model) {
		super(id, model);
		setOutputMarkupId(true);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		Link<User> link = new Link<User>("link", getModel()) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				UserListItemPanel.this.onClick();
			}
		};
		add(link);

		Label label = new Label("label", getTitle());
		label.setEscapeModelStrings(false);
		link.add(label);

		WebMarkupContainer menu = getMenu("menu");
		if (menu == null)
			menu = new InvisiblePanel("menu");
		add(menu);
	}

	public IModel<String> getTitle() {

		User user = getModel().getObject();

		StringBuilder sb = new StringBuilder();

		if (user.getLastName() != null)
			sb.append(user.getLastName());

		if (user.getFirstName() != null) {
			if (sb.length() > 0)
				sb.append(", ");
			sb.append(user.getFirstName());
		}

		if (sb.length() == 0 && user.getDisplayname() != null)
			sb.append(user.getDisplayname());

		sb.append(" <span class=\"text-secondary\">( " + (user.getName() != null ? user.getName() : "") + " )</span>");

		return Model.of(sb.toString());
	}

	protected abstract void onClick();

	protected abstract WebMarkupContainer getMenu(String id);
}
