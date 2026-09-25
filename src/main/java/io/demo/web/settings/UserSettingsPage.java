package io.demo.web.settings;

import java.util.Optional;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

import io.demo.Logger;
import io.demo.model.User;
import io.demo.web.page.BasePage;
import io.demo.web.page.DemoBasePage;
import io.demo.web.panel.GlobalTopPanel;
import io.demo.web.panel.SimpleHeaderPanel;
import io.demo.web.reports.ReportQueriesPage;
import io.wktui.error.ErrorPanel;
import io.wktui.nav.breadcrumb.BreadCrumb;

/**
 * Page to edit the user's preferences ({@code UserSettingsService}).
 */
@MountPath("/usersettings")
public class UserSettingsPage extends DemoBasePage {

	private static final long serialVersionUID = 1L;

	static private Logger logger = Logger.getLogger(UserSettingsPage.class.getName());
	
	public UserSettingsPage() {
		this(new PageParameters());
	}

	public UserSettingsPage(PageParameters parameters) {
		super(parameters);
	}

	public boolean canAccess(Optional<User> ouser) {
		return ouser.isPresent();
	} 
	
	
	@Override
	protected String getToolbarTitle() {
		return "Buscador Juridico";
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		add(new UserSettingsEditor("settings"));
	
	
	
	
	}

	protected Panel createHeaderPanel() {
		try {

			BreadCrumb<Void> bc = createBreadCrumb();
			bc.addElement(new io.wktui.nav.breadcrumb.BCElement( Model.of("mis preferencias")));

			SimpleHeaderPanel ph = new SimpleHeaderPanel("page-header", getOptionalSessionUserModel());
			ph.setBreadCrumb(bc);

			// bc.addElement(new BCElement(new
			// Model<String>(getModel().getObject().getDisplayname())));
			// JumboPageHeaderPanel<Candidate> ph = new
			// JumboPageHeaderPanel<Candidate>("page-header", getModel(), new
			// Model<String>(getModel().getObject().getDisplayname()));
			// ph.setHeaderCss("mb-0 pb-2 border-none");
			// ph.setIcon(Candidate.getIcon());
			// ph.setBreadCrumb(bc);
			// ph.setContext(getLabel("candidate"));
			// return (ph);

			return ph;

		} catch (Exception e) {
			logger.error(e);
			return new ErrorPanel("page-header", e);
		}
	}

	@Override
	protected void addListeners() {
		// TODO Auto-generated method stub
		
	}

}
