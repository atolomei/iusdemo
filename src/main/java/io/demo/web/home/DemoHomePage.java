package io.demo.web.home;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

import com.giffing.wicket.spring.boot.context.scan.WicketHomePage;

import io.demo.Logger;
import io.demo.model.Sentencia;
import io.demo.results.ResultsPanel;
import io.demo.web.event.SearchEvent;
import io.demo.web.page.BasePage;
import io.demo.web.page.GlobalFooterPanel;
import io.demo.web.page.GlobalTopPanel;
import io.demo.web.page.SimpleHeaderPanel;
import io.wktui.error.ErrorPanel;
import io.wktui.nav.breadcrumb.BreadCrumb;
import io.wktui.nav.breadcrumb.HREFBCElement;
import wktui.base.DummyBlockPanel;
import wktui.base.InvisiblePanel;
import wktui.base.UIEventListener;

/**
 * Home page of the demo del buscador juridico kbee. No login required.

 *
 *
 * User cache de consultas -> yes/no, deshabilitar el cache de consultas de LegalSearchService 
 * Limpiar cache de consultas -> boton ajax -> reset de cache de consultas de LegalSearchService
 * 
 * Maximo de resultados -> 1, 2, 5, 10, 20 ,30  selector, para ser usado por resultsPanel para mostrar solo los primeros N resultados
 * Ordenar el resultset por fecha (mas reciente primero) -> checkbox, para ser usado por resultsPanel para ordenar el resultset antes de mostrarlo
 * 
 * 
 * 
 *
 *
 *
 *
 *kbee-solr-api
     │
     ├── Ollama :11434
     │     └── qwen3-embedding:0.6b
     │
     ├── Solr
     │
     └── Qwen Reranker :8000
           └── Qwen3-Reranker-4B
           
  
           
           
           con batch_size=8.
           
           
 *
 *
 *
 *
 *----------
 *
 *
 *
 *
 *Terminal 1 — Reranker:
 *cd ~/eclipse-workspace/qwenn-reranker
source .venv/bin/activate
python -m uvicorn app:app --host 127.0.0.1 --port 8001


Terminal 2 — comprobar:
curl http://127.0.0.1:8001/health
 
 *
 *
 *ollama list
 *
 *
 *
 *
 *
 *
 *
 *
 *http://127.0.0.1:11434
 *
 *Qwen3-Embedding 0.6B
       │
       ├── Ollama.app
       ├── :11434
       ├── M4 Max / Metal
       └── 1024 dimensiones
 
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *-----
 *
 *
 *Comandos que realmente necesitás recordar
Terminal 1 — Reranker:
cd ~/eclipse-workspace/qwenn-reranker
source .venv/bin/activate
python -m uvicorn app:app --host 127.0.0.1 --port 8001
Terminal 2 — comprobar:
curl http://127.0.0.1:8001/health
Ollama:
ollama list





Embedding → http://127.0.0.1:11434
Reranker  → http://127.0.0.1:8001
Solr      → tu instancia habitual



 *
 */
@WicketHomePage
@MountPath("/home")
public class DemoHomePage extends BasePage {

    private static final long serialVersionUID = 1L;
    
    
    static private Logger logger = Logger.getLogger(DemoHomePage.class.getName());
	
    private SearchFormEditor searchFormEditor;
    private ResultsPanel<Sentencia> resultsPanel;
    
    private GlobalTopPanel globalTopPanel;
    private GlobalFooterPanel footerPanel;
    
    /** Container of the SearchFormEditor and the results panel (Ajax refresh target). */
    private  WebMarkupContainer container;
    
    private Panel pageHeader;
    
    public DemoHomePage() {
        this(new PageParameters());
    }
    
    @Override
    public void addListeners() {
    	super.addListeners();
    	
    	add(new UIEventListener<SearchEvent>() {
    		private static final long serialVersionUID = 1L;

			@Override
			public void onEvent(SearchEvent event) {
				
				// execute the search and replace the results panel
				executeSearch(event.getSearchText());
				
				// update the user's query history
				getQueryHistoryService().record(event.getSearchText());
				
				// add the container to the target so the replaced results panel is refreshed
				event.getTarget().add(getContainer());
			}
		});
    }
	
    public DemoHomePage(PageParameters parameters) {
        super(parameters);
    }

    @Override
    protected String getToolbarTitle() {
        return "Buscador Juridico";
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
    
    
    private Panel createHeaderPanel() {
    	try {
			
    		BreadCrumb<Void> bc = createBreadCrumb();
			bc.addElement(new HREFBCElement("/home",  Model.of("home")));

			SimpleHeaderPanel ph = new SimpleHeaderPanel("page-header",getSessionUserModel());
			ph.setBreadCrumb(bc);
			
			
			
			// bc.addElement(new BCElement(new Model<String>(getModel().getObject().getDisplayname())));
			//JumboPageHeaderPanel<Candidate> ph = new JumboPageHeaderPanel<Candidate>("page-header", getModel(), new Model<String>(getModel().getObject().getDisplayname()));
			//ph.setHeaderCss("mb-0 pb-2 border-none");
			//ph.setIcon(Candidate.getIcon());
			//ph.setBreadCrumb(bc);
			//ph.setContext(getLabel("candidate"));
			//return (ph);
			
			return bc;
			
			
		} catch (Exception e) {
			logger.error(e);
			return new ErrorPanel("page-header", e);
		}
	}

	public void onInitialize() {
		super.onInitialize();

		globalTopPanel=new GlobalTopPanel("top-panel", null);
		add ( globalTopPanel );
		
		
		footerPanel=new GlobalFooterPanel("footer-panel", null);
		add (footerPanel );
		
		
	
		container = new WebMarkupContainer("container");
		container.setOutputMarkupId(true);
		add(container);

		searchFormEditor = new SearchFormEditor("searchform");
		container.add(searchFormEditor);

		container.add(new InvisiblePanel("results"));
	}
    
    public WebMarkupContainer getContainer() {
    	return this.container;
    }
    
    public void executeSearch(String searchText) {
	
    	List<IModel<Sentencia>> list =  new ArrayList<IModel<Sentencia>>();
		
    	// honor the user's preference on using the query cache
    	boolean useCache = getUserSettingsService().isUseQueryCache();

    	getLegalSearchService().search(searchText, useCache).forEach(item-> list.add( new Model<Sentencia>(item)));

    	resultsPanel = new ResultsPanel<Sentencia>("results", list);
    	resultsPanel.setQuery(searchText);
		
    	getContainer().addOrReplace ( resultsPanel );
    }
    
}
