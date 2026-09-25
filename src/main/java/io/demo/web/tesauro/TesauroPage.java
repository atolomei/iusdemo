package io.demo.web.tesauro;

import java.util.List;
import java.util.Optional;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

import io.demo.model.Role;
import io.demo.model.User;
import io.demo.results.ResultsPanel;
import io.demo.web.home.DemoHomePage;
import io.demo.web.home.SearchFormEditor;
import io.demo.web.page.BasePage;

/**
 * Tesauro page of the demo del buscador juridico kbee. No login required.
 */
@MountPath("/tesauro")
public class TesauroPage extends BasePage {

    private static final long serialVersionUID = 1L;

    
   TesauroEditor editor;
   Label tesauroLabel;
   List<String> tesauroList;
   
    
    
    public TesauroPage(PageParameters parameters) {
        super(parameters);
        
    }
    
    
	@Override
	public boolean canAccess(Optional<User> ouser) {
		
		if (ouser.isEmpty())
			return false;
		
		
		
		Role role = ouser.get().getRole();
		
		if (role==null)
			return false;
		
	
		return role== Role.SYSADMIN || role== Role.ADMIN;
		
		
	} 
	
    public void onInitialize() {
		super.onInitialize();
	
	    add(new Label("pageName", Model.of(DemoHomePage.class.getSimpleName())));
	       
	       
		add(new TesauroEditor("form") );
		
		
		
		
		
		
			
	}
    
    @Override
    protected String getToolbarTitle() {
        return "Buscador Juridico";
    }


	@Override
	protected void addListeners() {
		// TODO Auto-generated method stub
		
	}
    
    
}
