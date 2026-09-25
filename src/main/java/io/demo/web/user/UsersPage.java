package io.demo.web.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

import io.demo.Logger;
import io.demo.model.ObjectState;
import io.demo.model.Role;
import io.demo.model.User;
import io.demo.model.db.service.UserDBService;
import io.demo.service.ServiceLocator;
import io.demo.web.page.DemoBasePage;
import io.demo.web.panel.ObjectModel;
import io.demo.web.panel.SimpleHeaderPanel;
import io.wktui.error.ErrorPanel;
import io.wktui.nav.breadcrumb.BCElement;
import io.wktui.nav.breadcrumb.BreadCrumb;
import io.wktui.nav.menu.AjaxLinkMenuItem;
import io.wktui.nav.menu.LinkMenuItem;
import io.wktui.nav.menu.MenuItemPanel;
import io.wktui.nav.menu.NavDropDownMenu;
import io.wktui.nav.toolbar.ButtonCreateToolbarItem;
import io.wktui.nav.toolbar.Toolbar;
import io.wktui.nav.toolbar.ToolbarItem;
import io.wktui.nav.toolbar.ToolbarItem.Align;
import io.wktui.struct.list.ListPanel;

/**
 * Page with the list of {@link User}. Mounted on /users.
 * Access: SysAdmin or Admin.
 */
@MountPath("/users")
public class UsersPage extends DemoBasePage {

	private static final long serialVersionUID = 1L;

	static private Logger logger = Logger.getLogger(UsersPage.class.getName());

	private List<IModel<User>> list;
	private ListPanel<User> listPanel;

	public UsersPage() {
		this(new PageParameters());
	}

	public UsersPage(PageParameters parameters) {
		super(parameters);
	}

	@Override
	public boolean canAccess(Optional<User> ouser) {

		if (ouser.isEmpty())
			return false;

		Role role = ouser.get().getRole();
		if (role == null)
			return false;

		return role == Role.SYSADMIN || role == Role.ADMIN;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		this.list = new ArrayList<IModel<User>>();
		getUserDBService().findAllSorted(ObjectState.EDITION, ObjectState.PUBLISHED).forEach(u -> list.add(new ObjectModel<User>(u)));

		// toolbar with a "create" button
		List<ToolbarItem> items = new ArrayList<ToolbarItem>();
		ButtonCreateToolbarItem<User> create = new ButtonCreateToolbarItem<User>("item") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onClick() {
				UsersPage.this.onCreate();
			}
		};
		create.setAlign(Align.TOP_LEFT);
		items.add(create);
		add(new Toolbar("toolbar", items));

		// list of users
		this.listPanel = new ListPanel<User>("list", list) {
			private static final long serialVersionUID = 1L;

			@Override
			protected Panel getListItemPanel(IModel<User> model, io.wktui.struct.list.ListPanelMode mode) {
				return UsersPage.this.getListItemPanel(model);
			}

			@Override
			public IModel<String> getItemLabel(IModel<User> model) {
				return Model.of(model.getObject().getDisplayname() + " " + (model.getObject().getName() != null ? model.getObject().getName() : ""));
			}

			@Override
			protected WebMarkupContainer getListItemExpandedPanel(IModel<User> model, io.wktui.struct.list.ListPanelMode mode) {
				return new UserExpandedPanel("expanded-panel", model);
			}
		};
		listPanel.setHasExpander(true);
		add(listPanel);
	}

	protected Panel getListItemPanel(IModel<User> model) {

		return new UserListItemPanel("row-element", model) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onClick() {
				setResponsePage(new UserPage(getModel()));
			}

			@Override
			protected WebMarkupContainer getMenu(String id) {
				return UsersPage.this.getObjectMenu(id, getModel());
			}
		};
	}

	protected WebMarkupContainer getObjectMenu(String id, IModel<User> model) {

		NavDropDownMenu<User> menu = new NavDropDownMenu<User>(id, model, null);
		menu.setOutputMarkupId(true);

		menu.setTitleCss("d-block-inline d-sm-block-inline d-md-block-inline d-lg-none d-xl-none d-xxl-none ps-1 pe-1");
		menu.setIconCss("fa-solid fa-ellipsis d-block-inline d-sm-block-inline d-md-block-inline d-lg-block-inline d-xl-block-inline d-xxl-block-inline ps-1 pe-1");

		// Abrir
		menu.addItem(new io.wktui.nav.menu.MenuItemFactory<User>() {
			private static final long serialVersionUID = 1L;

			@Override
			public MenuItemPanel<User> getItem(String id) {
				return new LinkMenuItem<User>(id, model) {
					private static final long serialVersionUID = 1L;

					@Override
					public void onClick() {
						setResponsePage(new UserPage(model));
					}

					@Override
					public IModel<String> getLabel() {
						return UsersPage.this.getLabel("open");
					}
				};
			}
		});

		// Borrar
		menu.addItem(new io.wktui.nav.menu.MenuItemFactory<User>() {
			private static final long serialVersionUID = 1L;

			@Override
			public MenuItemPanel<User> getItem(String id) {
				return new AjaxLinkMenuItem<User>(id, model) {
					private static final long serialVersionUID = 1L;

					@Override
					public void onClick(AjaxRequestTarget target) {
						try {
							getUserDBService().markAsDeleted(getModel().getObject(), getSessionUser().get());
							list.removeIf(m -> m.getObject().getId().equals(getModel().getObject().getId()));
							target.add(listPanel);
						} catch (Exception e) {
							logger.error(e);
						}
					}

					@Override
					public IModel<String> getLabel() {
						return UsersPage.this.getLabel("delete");
					}
				};
			}
		});

		menu.addItem(new io.wktui.nav.menu.MenuItemFactory<User>() {
			private static final long serialVersionUID = 1L;

			@Override
			public MenuItemPanel<User> getItem(String id) {
				return new io.wktui.nav.menu.SeparatorMenuItem<User>(id);
			}
		});

		// Enviar correo de Bienvenida (not implemented yet)
		menu.addItem(new io.wktui.nav.menu.MenuItemFactory<User>() {
			private static final long serialVersionUID = 1L;

			@Override
			public MenuItemPanel<User> getItem(String id) {
				return new AjaxLinkMenuItem<User>(id, model) {
					private static final long serialVersionUID = 1L;

					@Override
					public void onClick(AjaxRequestTarget target) {
						// not implemented yet
						logger.debug("send welcome email -> not implemented yet");
					}

					@Override
					public IModel<String> getLabel() {
						return UsersPage.this.getLabel("send-welcome-email");
					}
				};
			}
		});

		// Ingresar como este usuario (not implemented yet)
		menu.addItem(new io.wktui.nav.menu.MenuItemFactory<User>() {
			private static final long serialVersionUID = 1L;

			@Override
			public MenuItemPanel<User> getItem(String id) {
				return new AjaxLinkMenuItem<User>(id, model) {
					private static final long serialVersionUID = 1L;

					@Override
					public void onClick(AjaxRequestTarget target) {
						// not implemented yet
						logger.debug("impersonate -> not implemented yet");
					}

					@Override
					public IModel<String> getLabel() {
						return UsersPage.this.getLabel("impersonate");
					}
				};
			}
		});

		return menu;
	}

	protected void onCreate() {
		try {
			User user = getUserDBService().create("new", getSessionUser().get());
			setResponsePage(new UserPage(new ObjectModel<User>(user)));
		} catch (Exception e) {
			logger.error(e);
		}
	}

	@Override
	protected Panel createHeaderPanel() {
		try {
			BreadCrumb<Void> bc = createBreadCrumb();
			bc.addElement(new BCElement(getLabel("users")));

			SimpleHeaderPanel ph = new SimpleHeaderPanel("page-header", getOptionalSessionUserModel());
			ph.setBreadCrumb(bc);
			return ph;

		} catch (Exception e) {
			logger.error(e);
			return new ErrorPanel("page-header", e);
		}
	}

	@Override
	protected String getToolbarTitle() {
		return "Buscador Juridico";
	}

	@Override
	public void onDetach() {
		super.onDetach();
		if (list != null)
			list.forEach(m -> m.detach());
	}

	public UserDBService getUserDBService() {
		return (UserDBService) ServiceLocator.getInstance().getBean(UserDBService.class);
	}

	@Override
	protected void addListeners() {
		// TODO Auto-generated method stub
		
	}
}
