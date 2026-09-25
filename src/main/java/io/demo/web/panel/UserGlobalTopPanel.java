package io.demo.web.panel;

import java.util.List;

import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.PackageResourceReference;

import io.demo.App;
import io.demo.model.User;
import io.demo.web.user.UserPage;
import io.demo.web.user.UserPasswordPage;
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

	
	static List<PackageResourceReference> flowers = new java.util.ArrayList<PackageResourceReference>();
	
	static {
		flowers.add(new PackageResourceReference(App.class, "flower1.jpg"));
		flowers.add(new PackageResourceReference(App.class, "flower2.png"));
		flowers.add(new PackageResourceReference(App.class, "flower3.jpeg"));
		flowers.add(new PackageResourceReference(App.class, "flower4.jpeg"));
		flowers.add(new PackageResourceReference(App.class, "flower5.jpeg"));
		 
	}
	
	
	
	public UserGlobalTopPanel(String id, IModel<User> model) {
		super(id, model);
		setOutputMarkupId(true);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		add(getMenu());
		// default image, instead of the AvatarService or the user's photo
		add(new Image("image", flowers.get((getModel().getObject().getId().intValue()) % flowers.size()) ));
	}

	private NavDropDownMenu<Void> getMenu() {

		NavDropDownMenu<Void> menu = new NavDropDownMenu<Void>("userMenu");

		if (getModel() != null && getModel().getObject() != null) {

			
			String title = getModel().getObject().getDisplayname();
			
			menu.setTitle(Model.of(title));
		
			if (title!=null &&title.equals(getModel().getObject().getName()))
				menu.setSubtitle(Model.of(""));
			else
				menu.setSubtitle(Model.of(getModel().getObject().getName()));
			
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
						setResponsePage(new UserPage(UserGlobalTopPanel.this.getModel(), true ));
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
						setResponsePage(new UserPasswordPage( UserGlobalTopPanel.this.getModel()) );
					}

					@Override
					public IModel<String> getLabel() {
						return getLabel("my-password");
					}
				};
			}
		});
		
		// My Preferences
	/**	menu.addItem(new io.wktui.nav.menu.MenuItemFactory<Void>() {
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
**/

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