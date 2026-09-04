package io.demo.web.page;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import io.demo.model.User;
import wktui.base.ModelPanel;



public class GlobalTopPanel extends ModelPanel<User> {

	
	
	private Panel userGlobalTopPanel;

	
	
	public GlobalTopPanel(String id, IModel<User> model) {
		super(id, model);
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
	}
	

	@Override
	public void onInitialize() {
		super.onInitialize();
	
	
	
		if (getModel() != null) {
		//	this.userGlobalTopPanel = new UserGlobalTopPanel("userGlobalTopPanel", getModel());
		//	HelpDropDownMenu help = new HelpDropDownMenu("help", getModel());
		//	add(help);

		} else {
		//	this.userGlobalTopPanel = new InvisiblePanel("userGlobalTopPanel");
		//	add(new InvisiblePanel("help"));

		}

		// add(this.userGlobalTopPanel);
		
		
		this.userGlobalTopPanel = new UserGlobalTopPanel("userGlobalTopPanel", getModel());
		add(this.userGlobalTopPanel);
		
		
	
	}
	

}
