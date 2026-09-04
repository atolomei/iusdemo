package io.demo.web.page;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import io.demo.model.User;
import io.wktui.nav.menu.NavDropDownMenu;
import wktui.base.ModelPanel;

public class UserGlobalTopPanel extends ModelPanel<User> {

	public UserGlobalTopPanel(String id, IModel<User> model) {
		super(id, model);
	}

	/**
	private NavDropDownMenu<Void> getMenu() {

		NavDropDownMenu<Void> menu = new NavDropDownMenu<Void>("userMenu");

		//menu.setMenuAlignEnd(true);

		// menu.setIconCss("d-block-inline fa-2x fa-duotone fa-solid fa-user ps-2
		// pe-2");
		// menu.setIconCss("d-block-inline fa-user ps-2 pe-2");

		Optional<Person> o = getPersonDBService().getByUser(getModel().getObject());

		menu.setTitle(new Model<String>(getModel().getObject().getDisplayname()));

		if (o.isPresent())
			menu.setSubtitle(Model.of(o.get().getFirstLastname()));

		menu.addItem(new io.wktui.nav.menu.MenuItemFactory<Void>() {

			private static final long serialVersionUID = 1L;

			@Override
			public MenuItemPanel<Void> getItem(String id) {

				return new LinkMenuItem<Void>(id) {

					private static final long serialVersionUID = 1L;

					@Override
					public void onClick() {
						setResponsePage(new UserPage(UserGlobalTopPanel.this.getModel(), true));
					}

					@Override
					public IModel<String> getLabel() {
						return getLabel("account");
					}

					@Override
					public String getBeforeClick() {
						return null;
					}
				};
			}
		});

		menu.addItem(new io.wktui.nav.menu.MenuItemFactory<Void>() {

			private static final long serialVersionUID = 1L;

			@Override
			public MenuItemPanel<Void> getItem(String id) {

				return new LinkMenuItem<Void>(id) {

					private static final long serialVersionUID = 1L;

					@Override
					public void onClick() {
						Optional<Person> o = getPersonDBService().getByUser(UserGlobalTopPanel.this.getModel().getObject());
						if (o.isPresent())
							setResponsePage(new PersonPage(new ObjectModel<Person>(o.get())));
						else
							setResponsePage(new ErrorPage(Model.of("not found")));
					}

					@Override
					public IModel<String> getLabel() {
						return getLabel("personal-info");
					}

					@Override
					public String getBeforeClick() {
						return null;
					}
				};
			}
		});

		menu.addItem(new io.wktui.nav.menu.MenuItemFactory<Void>() {
			private static final long serialVersionUID = 1L;

			@Override
			public MenuItemPanel<Void> getItem(String id) {
				return new io.wktui.nav.menu.SeparatorMenuItem<Void>(id);
			}
		});

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

					@Override
					public String getBeforeClick() {
						return null;
					}
				};
			}
		});

		return menu;
	}
	
	*/

	/**protected PersonDBService getPersonDBService() {
		return (PersonDBService) ServiceLocator.getInstance().getBean(PersonDBService.class);
	}*/
	
}
