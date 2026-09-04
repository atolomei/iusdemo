package io.demo.web.page;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import io.demo.model.User;
import io.wktui.nav.breadcrumb.BreadCrumb;
import wktui.base.ModelPanel;

public class SimpleHeaderPanel extends ModelPanel<User> {

	public SimpleHeaderPanel(String id, IModel<User> model) {
		super(id, model);
		// TODO Auto-generated constructor stub
	}

	private static final long serialVersionUID = 1L;

	
	public void onInitialze() {
		super.onInitialize();

		
		
		
		
		
	}


	public void setBreadCrumb(Panel bc) {
		if (!bc.getId().equals("breadcrumb"))
			throw new IllegalArgumentException(" id must be breadcrumb -> " + bc.getId());
		addOrReplace(bc);
	}



}
