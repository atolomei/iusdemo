package io.demo.web.home;

import java.net.ConnectException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

import com.giffing.wicket.spring.boot.context.scan.WicketHomePage;

import io.demo.Logger;
import io.demo.model.Query;
import io.demo.model.RAGSentencia;
import io.demo.model.Role;
import io.demo.model.User;
import io.demo.results.ResultsPanel;
import io.demo.web.event.SearchEvent;
import io.demo.web.page.BasePage;
import io.demo.web.page.DemoBasePage;
import io.demo.web.panel.GlobalFooterPanel;
import io.demo.web.panel.GlobalTopPanel;
import io.demo.web.panel.ObjectModel;
import io.demo.web.panel.SimpleHeaderPanel;
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
 * 
 * User cache de consultas -> yes/no, deshabilitar el cache de consultas de
 * LegalSearchService Limpiar cache de consultas -> boton ajax -> reset de cache
 * de consultas de LegalSearchService
 * 
 * Maximo de resultados -> 1, 2, 5, 10, 20 ,30 selector, para ser usado por
 * resultsPanel para mostrar solo los primeros N resultados Ordenar el resultset
 * por fecha (mas reciente primero) -> checkbox, para ser usado por resultsPanel
 * para ordenar el resultset antes de mostrarlo
 * 
 * 
 * 
 *
 *
 *
 *
 * kbee-solr-api │ ├── Ollama :11434 │ └── qwen3-embedding:0.6b │ ├── Solr │ └──
 * Qwen Reranker :8000 └── Qwen3-Reranker-4B
 * 
 * 
 * 
 * 
 * con batch_size=8.
 *
 *
 *
 *
 * 
 * 
 * ----------
 *
 *
 *
 *
 * Terminal 1 — Reranker:
    cd ~/eclipse-workspace/qwenn-reranker 
    source .venv/bin/activate 
    python -m uvicorn app:app --host 127.0.0.1 --port 8001
 * 
 * 
 * Terminal 2 — comprobar: 
 * curl http://127.0.0.1:8001/health
 *
 *
 * 
 * ollama list
 *
 *
 *
 *
 *
 *
 *
 *
 * http://127.0.0.1:11434
 *
 * Qwen3-Embedding 0.6B │ ├── Ollama.app ├── :11434 ├── M4 Max / Metal └── 1024
 * dimensiones
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
 * 
 * -----
 *
 *
 * Comandos que realmente necesitás recordar Terminal 1 — Reranker: cd
 * ~/eclipse-workspace/qwenn-reranker source .venv/bin/activate python -m
 * uvicorn app:app --host 127.0.0.1 --port 8001 Terminal 2 — comprobar: curl
 * http://127.0.0.1:8001/health Ollama: ollama list
 * 
 * 
 * 
 * 
 * 
 * Embedding → http://127.0.0.1:11434 Reranker → http://127.0.0.1:8001 Solr → tu
 * instancia habitual
 *
 * 
 * 
 * 
 */
@WicketHomePage
@MountPath("/home")
public class DemoHomePage extends DemoBasePage {

	private static final long serialVersionUID = 1L;

	static private Logger logger = Logger.getLogger(DemoHomePage.class.getName());

	private SearchFormEditor searchFormEditor;
	private ResultsPanel<RAGSentencia> resultsPanel;

	private GlobalFooterPanel footerPanel;

	/**
	 * Container of the SearchFormEditor and the results panel (Ajax refresh
	 * target).
	 */
	private WebMarkupContainer container;

	private Panel pageHeader;

	
	
	public DemoHomePage() {
		this(new PageParameters());
	}

	
	public boolean canAccess(Optional<User> ouser) {

		if (ouser.isEmpty())
			return false;
		
		
		
		Role role = ouser.get().getRole();
		
		if (role==null)
			return false;
		
	
		return true;
	
	} 


	
	public DemoHomePage(PageParameters parameters) {
		super(parameters);
	}

	@Override
	public void addListeners() {
		
		add(new UIEventListener<SearchEvent>() {
			private static final long serialVersionUID = 1L;

			@Override
			public void onEvent(SearchEvent event) {

				logger.debug("search event received: " + event.toString());
				
				// execute the search and replace the results panel
				executeSearch(event.getSearchText(), event.getDateRange(), event.getSubjectOption(), event.getReasoningEffortOption());

				// update the user's query history
				//getQueryHistoryService().record(event.getSearchText());

				// add the container to the target so the replaced results panel is refreshed
				event.getTarget().add(getContainer());
			}
		});
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

	protected Panel createHeaderPanel() {
		try {

			BreadCrumb<Void> bc = createBreadCrumb();
			// bc.addElement(new HREFBCElement("/home", Model.of("home")));

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

	public void onInitialize() {
		super.onInitialize();

		container = new WebMarkupContainer("container");
		container.setOutputMarkupId(true);
		add(container);

		searchFormEditor = new SearchFormEditor("searchform");
		container.add(searchFormEditor);

		String query = getPageParameters().get("query").toOptionalString();

		if (query != null && !query.isEmpty()) {

			// restore the toolbar filters from the page parameters (query history)
			io.demo.results.DateRange dateRange = io.demo.results.DateRange
					.fromOrdinal(getPageParameters().get("dateRangeOption").toInt(io.demo.results.DateRange.getDefault().ordinal()));
		
			io.demo.results.SubjectOption subjectOption = io.demo.results.SubjectOption
					.fromOrdinal(getPageParameters().get("subjectOption").toInt(io.demo.results.SubjectOption.getDefault().ordinal()));
			
			io.demo.results.ReasoningEffortOption reasoningEffortOption = io.demo.results.ReasoningEffortOption
					.fromOrdinal(getPageParameters().get("reasoningEffortOption").toInt(io.demo.results.ReasoningEffortOption.getDefault().ordinal()));

			// set the toolbar selectors to the restored values
			searchFormEditor.setDateRange(dateRange);
			searchFormEditor.setSubjectOption(subjectOption);
			searchFormEditor.setReasoningEffortOption(reasoningEffortOption);
			searchFormEditor.setText(query);

			executeSearch(query, dateRange, subjectOption, reasoningEffortOption);

		} else {
			container.add(new InvisiblePanel("results"));
		}

	}

	public WebMarkupContainer getContainer() {
		return this.container;
	}

	public void executeSearch(String searchText) {
		executeSearch(searchText, null, null, null);
	}

	public void executeSearch(String searchText, io.demo.results.DateRange dateRange, io.demo.results.SubjectOption subjectOption, io.demo.results.ReasoningEffortOption reasoningEffortOption) {

		List<IModel<RAGSentencia>> list = new ArrayList<IModel<RAGSentencia>>();

		// honor the user's preference on using the query cache
		boolean useCache = getSettingsService().isUseCacheQueries();
		
		
		//if  (searchText == null || searchText.isEmpty()) {
			//searchText="Precedente sobre si puede rechazarse una demanda laboral por incapacidad cuando no se acredita la incapacidad con prueba médica";
	
		//}

		try {
			ZoneId zoneId = ZoneId.systemDefault();
			OffsetDateTime fromDate = dateRange != null ? dateRange.getFrom(zoneId) : null;
			String subject= subjectOption !=null ? subjectOption.getLabel() : null;
			
			getLegalSearchService().search(searchText, fromDate,  null, subject, getSessionUser().get(), getSessionId().orElse("no-session"), useCache, dateRange, subjectOption, 
					reasoningEffortOption).forEach(item -> list.add(new Model<RAGSentencia>(item)));
			
		} catch (Exception e) {
			// the RAG server could not be reached (and the query was not cached):
			// display an error panel in place of the results so the application
			// keeps working with cached queries/contents only
			logger.error(e, "search failed (RAG server unavailable?)");
			
			
			if (e.getCause()!=null && e.getCause() instanceof ConnectException) {
				logger.error("RAG server connection refused. Check if the RAG server is running.");
				getContainer().addOrReplace(new ErrorPanel("results", Model.of("No se pudo ejecutar la búsqueda. Error de conexión al servidor de inferencias ("+e.getCause().getClass().getSimpleName()+")" )));
				
			}
			else {
				getContainer().addOrReplace(new ErrorPanel("results", e));
			}
			return;
		}

		resultsPanel = new ResultsPanel<RAGSentencia>("results", list);
		
		
		String key = getQueryLogService().queryKey(searchText, dateRange, subjectOption, reasoningEffortOption);
				
		Optional<Query> o = getQueryDBService().findByKey(key);
		
		if (o.isPresent()) {
			Query q = o.get();
			resultsPanel.setQueryModel( new ObjectModel<Query>(q));
		}
		
		resultsPanel.setDateRange(dateRange);
		resultsPanel.setSubjectOption(subjectOption);
		resultsPanel.setTotalOption(io.demo.results.TotalOption.getDefault());
		resultsPanel.setReasoningEffortOption(reasoningEffortOption);

		getContainer().addOrReplace(resultsPanel);
	}
	


	


	private Optional<String> getSessionId() {
		try {
			if (org.apache.wicket.Session.exists())
				return Optional.of( org.apache.wicket.Session.get().getId() );
		} catch (Exception e) {
			logger.debug("no wicket session available");
		}
		return Optional.empty();
	}
	

}
