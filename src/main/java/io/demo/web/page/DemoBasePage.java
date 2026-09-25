package io.demo.web.page;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.demo.Logger;
import io.demo.web.panel.GlobalTopPanel;
import io.wktui.error.ErrorPanel;
import io.wktui.nav.breadcrumb.BreadCrumb;
import wktui.base.InvisiblePanel;

public abstract class DemoBasePage extends BasePage {
	
	private static final long serialVersionUID = 1L;

	static private Logger logger = Logger.getLogger( DemoBasePage.class.getName());
	
	private Panel pageHeader;
	

	private GlobalTopPanel globalTopPanel;

	public DemoBasePage() {
		this(new PageParameters());
	}

	public DemoBasePage(PageParameters parameters) {
		super(parameters);

	}


	public void onInitialize() {
		super.onInitialize();
	
		
		try {
		
			globalTopPanel = new GlobalTopPanel("top-panel", getSessionUserModel());
			add(globalTopPanel);

		} catch (Exception e) {
				addOrReplace(new ErrorPanel("top-panel", e));
		}
		
		
		
		try {
	
			initHeaderPanel();
			
		} finally {
			
			if (get("page-header") == null) {
				addOrReplace(new InvisiblePanel("page-header"));
			}
		}
	
	}
	
	
	
	
	
	protected void initHeaderPanel() {
			try {
				Panel panel = createHeaderPanel();
				if (panel == null)
					pageHeader = new InvisiblePanel("page-header");
				else
					pageHeader = panel;

				addOrReplace(pageHeader);

			} catch (Exception e) {
				logger.error(e);
				addOrReplace(new ErrorPanel("page-header", e));
			}
		}

	protected abstract Panel createHeaderPanel();
	
	
 	
}

