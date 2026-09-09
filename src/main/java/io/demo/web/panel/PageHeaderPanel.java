package io.demo.web.panel;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import io.demo.model.DemoObject;
import io.wktui.nav.breadcrumb.BreadCrumb;
import io.wktui.nav.menu.DropDownMenu;
import io.wktui.nav.menu.LinkMenuItem;
import io.wktui.nav.menu.MenuItemPanel;
import io.wktui.nav.menu.NavBar;
import io.wktui.nav.menu.NavDropDownMenu;
import io.wktui.nav.menu.SeparatorMenuItem;
import wktui.base.BasePanel;
import wktui.base.DummyBlockPanel;
import wktui.base.InvisiblePanel;
import wktui.base.LabelPanel;
import wktui.base.ModelPanel;


public class PageHeaderPanel<T> extends ModelPanel<T> {

	private static final long serialVersionUID = 1L;

	private IModel<String> title;
	private IModel<String> tagLine;
	
	public PageHeaderPanel(String id) {
		this(id, null, null);
	}

	public PageHeaderPanel(String id, IModel<T> model) {
	       this(id, model, null);
	}

	public PageHeaderPanel(String id, IModel<T> model, IModel<String> title) {
		super(id, model);
		this.title=title; 
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		Label title = new Label("title", getTitle());
		add(title);
	}
	

    @Override
    public void onBeforeRender() {
        super.onBeforeRender();
    
        if (get("breadcrumb")==null)
            addOrReplace(new InvisiblePanel("breadcrumb"));
        
        if (getTagline()!=null) {
            WebMarkupContainer taglineContainer =  new WebMarkupContainer("taglineContainer");
            addOrReplace(taglineContainer);
            taglineContainer.add((new Label("tagline", getTagline())).setEscapeModelStrings(false));
        }
        else {
            addOrReplace( new InvisiblePanel("taglineContainer"));
        }
    }
	
    public void setBreadCrumb(Panel bc) {
        if (!bc.getId().equals("breadcrumb"))
            throw new IllegalArgumentException(" id must be breadcrumb -> " + bc.getId());
        addOrReplace(bc);
    }
	
	
    public IModel<String> getTagline() {
        return this.tagLine;
    }
	
    public void setTagline(IModel<String> tag) {
        this.tagLine=tag;
    }

    
    private IModel<String> getTitle() {
    
    	if (this.title!=null)
    		return this.title;
    	
    	if (getModel()!=null) {
    		if (getModel().getObject() instanceof DemoObject) {
    			return new Model<String>( ((DemoObject) getModel().getObject()).getTitle() );
    		}
    	}
    	return new Model<String>( getClass().getSimpleName());  
      
    }
}
