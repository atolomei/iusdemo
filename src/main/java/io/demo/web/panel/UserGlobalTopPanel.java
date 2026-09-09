package io.demo.web.panel;

import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.PackageResourceReference;

import io.demo.model.User;
import io.wktui.nav.menu.LinkMenuItem;
import io.wktui.nav.menu.MenuItemPanel;
import io.wktui.nav.menu.NavDropDownMenu;
import wktui.base.ModelPanel;

/**
 * User area of the global top panel: drop-down menu (My Account, My
 * Preferences, Sign out) and the user's avatar.
 *
 * The avatar is the default image flower1.jpg packaged next to this class
 * (instead of the AvatarService or the user's photo).
 */
public class UserGlobalTopPanel extends ModelPanel<User> {

	private static final long serialVersionUID = 1L;

	public UserGlobalTopPanel(String id, IModel<User> model) {
		super(id, model);
		setOutputMarkupId(true);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		add(getMenu());

		// default image, instead of the AvatarService or the user's photo
		add(new Image("image", new PackageResourceReference(UserGlobalTopPanel.class, "flower1.jpg")));
	}

	private NavDropDownMenu<Void> getMenu() {

		NavDropDownMenu<Void> menu = new NavDropDownMenu<Void>("userMenu");

		if (getModel() != null && getModel().getObject() != null) {
			menu.setTitle(Model.of(getModel().getObject().getUsername()));
			menu.setSubtitle(Model.of(getModel().getObject().getEmail()));
			
		}
		else
			menu.setTitle(Model.of("[null]"));
		
		
		
		
		// My Account
		menu.addItem(new io.wktui.nav.menu.MenuItemFactory<Void>() {
			private static final long serialVersionUID = 1L;

			@Override
			public MenuItemPanel<Void> getItem(String id) {
				return new LinkMenuItem<Void>(id) {
					private static final long serialVersionUID = 1L;

					@Override
					public void onClick() {
						setResponsePage(new RedirectPage("/myaccount"));
					}

					@Override
					public IModel<String> getLabel() {
						return getLabel("my-account");
					}
				};
			}
		});

		// My Preferences
		menu.addItem(new io.wktui.nav.menu.MenuItemFactory<Void>() {
			private static final long serialVersionUID = 1L;

			@Override
			public MenuItemPanel<Void> getItem(String id) {
				return new LinkMenuItem<Void>(id) {
					private static final long serialVersionUID = 1L;

					@Override
					public void onClick() {
						setResponsePage(new RedirectPage("/usersettings"));
					}

					@Override
					public IModel<String> getLabel() {
						return getLabel("my-preferences");
					}
				};
			}
		});

		// separator
		menu.addItem(new io.wktui.nav.menu.MenuItemFactory<Void>() {
			private static final long serialVersionUID = 1L;

			@Override
			public MenuItemPanel<Void> getItem(String id) {
				return new io.wktui.nav.menu.SeparatorMenuItem<Void>(id);
			}
		});

		// Sign out
		menu.addItem(new io.wktui.nav.menu.MenuItemFactory<Void>() {
			private static final long serialVersionUID = 1L;

			@Override
			public MenuItemPanel<Void> getItem(String id) {
				return new LinkMenuItem<Void>(id) {
					private static final long serialVersionUID = 1L;

					@Override
					public void onClick() {
						setResponsePage(new RedirectPage("/logout"));
					}

					@Override
					public IModel<String> getLabel() {
						return getLabel("sign-out");
					}
				};
			}
		});

		return menu;
	}
}