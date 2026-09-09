package io.demo.web.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

import io.demo.Logger;
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
import io.wktui.nav.breadcrumb.HREFBCElement;
import io.wktui.nav.toolbar.AjaxButtonToolbarItem;
import io.wktui.nav.toolbar.Toolbar;
import io.wktui.nav.toolbar.ToolbarItem;
import io.wktui.nav.toolbar.ToolbarItem.Align;

/**
 * Page to view/edit a {@link User}. Mounted on /user/${id}
 */
@MountPath("/user/${id}")
public class UserPage extends DemoBasePage {

	private static final long serialVersionUID = 1L;

	static private Logger logger = Logger.getLogger(UserPage.class.getName());

	private IModel<User> model;
	private UserEditor editor;
	private UserPasswordEditor passwordEditor;

	public UserPage(PageParameters parameters) {
		super(parameters);

		Long id = parameters.get("id").toOptionalLong();
		if (id != null) {
			Optional<User> o_user = getUserDBService().findById(id);
			if (o_user.isPresent())
				this.model = new ObjectModel<User>(o_user.get());
		}
	}

	public UserPage(IModel<User> model) {
		super(new PageParameters());
		this.model = model;
	}

	public IModel<User> getModel() {
		return model;
	}

	@Override
	public boolean canAccess(Optional<User> ouser) {

		if (ouser.isEmpty())
			return false;

		if (model == null)
			return false;

		// a user can always see his own page
		if (ouser.get().getId() != null && ouser.get().getId().equals(model.getObject().getId()))
			return true;

		Role role = ouser.get().getRole();
		if (role == null)
			return false;

		return role == Role.SYSADMIN || role == Role.ADMIN;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		if (model == null) {
			add(new ErrorPanel("toolbar", null, Model.of("User not found")));
			add(new ErrorPanel("editor", null, Model.of("User not found")));
			add(new ErrorPanel("passwordEditor", null, Model.of("User not found")));
			return;
		}

		this.editor = new UserEditor("editor", model);
		add(editor);

		this.passwordEditor = new UserPasswordEditor("passwordEditor", model);
		add(passwordEditor);

		List<ToolbarItem> items = new ArrayList<ToolbarItem>();

		AjaxButtonToolbarItem<User> edit = new AjaxButtonToolbarItem<User>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onCick(AjaxRequestTarget target) {
				UserPage.this.editor.onEdit(target);
			}

			@Override
			public IModel<String> getButtonLabel() {
				return getLabel("edit");
			}

			@Override
			public boolean isVisible() {
				return editor.hasWritePermission();
			}
		};
		edit.setAlign(Align.TOP_LEFT);
		items.add(edit);

		add(new Toolbar("toolbar", items));
	}

	@Override
	protected Panel createHeaderPanel() {
		try {
			BreadCrumb<Void> bc = createBreadCrumb();
			bc.addElement(new HREFBCElement("/users", getLabel("users")));

			String title = model != null ? model.getObject().getDisplayname() : "";
			bc.addElement(new BCElement(Model.of(title)));

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
		if (model != null)
			model.detach();
	}

	public UserDBService getUserDBService() {
		return (UserDBService) ServiceLocator.getInstance().getBean(UserDBService.class);
	}
}
