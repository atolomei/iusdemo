package io.demo.web.panel;

import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.PackageResourceReference;

import org.apache.wicket.model.Model;

import io.demo.model.User;
import io.wktui.nav.menu.MainMenu;
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
		
	
		
		// logo rendered from the pjsf.png file packaged next to this class
		Image logo = new Image("logo", new PackageResourceReference(GlobalTopPanel.class, "pjsf.png"));
		add(logo);

		// global hamburger main menu
		MainMenu mainMenu = new MainMenu("mainMenu");
		mainMenu.addLink(Model.of("Portada"), "/home");
		mainMenu.addLink(Model.of("Usuarios"), "/users");
	
		//mainMenu.addLink(Model.of("Configuration"), "#");
		mainMenu.addTitle(Model.of("Reportes"));
		mainMenu.addLink(Model.of("Visitas"), "/reports/visits");
		mainMenu.addLink(Model.of("Consultas"), "/reports/queries");
		mainMenu.addLink(Model.of("Evaluaciones"), "/reports/queryfeedback");
		
		add(mainMenu);

		this.userGlobalTopPanel = new UserGlobalTopPanel("userGlobalTopPanel", getModel());
		add(this.userGlobalTopPanel);
		
		
	
	}
	

}
