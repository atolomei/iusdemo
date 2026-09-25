package io.demo.web.page;

import java.util.Optional;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

import io.demo.model.Role;
import io.demo.model.User;

/**
 * Comunidad page of the demo del buscador juridico kbee. No login required.
 */
@MountPath("/comunidad")
public class ComunidadPage extends BasePage {

    private static final long serialVersionUID = 1L;

    public ComunidadPage(PageParameters parameters) {
        super(parameters);
        add(new Label("pageName", Model.of(ComunidadPage.class.getSimpleName())));
    }

 

    
    @Override
    protected String getToolbarTitle() {
        return "Buscador Juridico";
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




	@Override
	protected void addListeners() {
		// TODO Auto-generated method stub
		
	} 
}
