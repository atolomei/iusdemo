package io.demo.web.panel;

import java.util.Optional;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import io.demo.model.User;
import io.wktui.nav.breadcrumb.BreadCrumb;
import wktui.base.ModelPanel;
import wktui.base.OptionalModelPanel;

public class SimpleHeaderPanel extends OptionalModelPanel<User> {

	public SimpleHeaderPanel(String id, Optional<IModel<User>> omodel) {
		super(id, omodel);
		
	}

	private static final long serialVersionUID = 1L;

	
	@Override
	public void onInitialize() {
		super.onInitialize();
	}


	public void setBreadCrumb(Panel bc) {
		if (!bc.getId().equals("breadcrumb"))
			throw new IllegalArgumentException(" id must be breadcrumb -> " + bc.getId());
		addOrReplace(bc);
	}



}
