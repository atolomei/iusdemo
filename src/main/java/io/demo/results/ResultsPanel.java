package io.demo.results;

import java.io.File;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.math3.analysis.solvers.RiddersSolver;
import org.apache.wicket.AttributeModifier;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.resource.FileResourceStream;

import io.demo.Logger;
import io.demo.model.Query;
import io.demo.model.RAGDocumento;
import io.demo.model.RAGSentencia;
import io.demo.service.ServiceLocator;
import io.demo.service.LegalSearchService;
import io.demo.service.Settings;
import io.demo.service.TestQueriesService;
import io.demo.service.UserSettingsService;
import io.demo.service.rag.KbeeRAGClient;
import io.demo.web.home.AnalysisPanel;
import io.demo.web.panel.DemoObjectListItemPanel;
import io.demo.web.panel.ObjectModel;
import io.demo.web.event.DateRangeEvent;
import io.demo.web.event.OrderOptionEvent;
import io.demo.web.event.QueryAnalysisEvent;
import io.demo.web.event.SubjectOptionEvent;
import io.demo.web.event.TotalOptionEvent;
import io.wktui.error.AlertPanel;
import io.wktui.error.ErrorPanel;
import io.wktui.struct.list.ListPanel;
import io.wktui.struct.list.ListPanelMode;
import wktui.base.BasePanel;
import wktui.base.DummyBlockPanel;
import wktui.base.InvisiblePanel;
import wktui.base.UIEventListener;

public class ResultsPanel<T extends RAGDocumento> extends BasePanel {

	private static final long serialVersionUID = 1L;

	static private Logger logger = Logger.getLogger( ResultsPanel.class.getName());
	
	private List<IModel<T>> list;
	private ListPanel<T> panel;

	 
	private WebMarkupContainer contentsContainerContainer;
	private WebMarkupContainer errorContainer;
	private WebMarkupContainer listToolbarContainer;
	private WebMarkupContainer helpContainer;

	
	private ToolbarResults toolbar;

	/** container of the query general analysis panel (InvisiblePanel by default) */
	private WebMarkupContainer generalAnalysisContainer;

	
	private Panel errorPanel;

	private boolean b_expand = false;
	private boolean isHelpVisible = false;

	/** the query whose RagResponse was saved to disk by {@link KbeeRAGClient} */
	//private String query;

	private IModel<Query> queryModel = null;
	
	
	 private DateRange dateRange = null;
	 private SubjectOption subjectOption=null;
	 private OrderOption orderOption=null;
	 private TotalOption totalOption = TotalOption.getDefault();
	 private ReasoningEffortOption reasoningEffortOption = ReasoningEffortOption.getDefault();
	 
	 
	
	/**
	 * 
	 * 
	 * @param id
	 */
	public ResultsPanel(String id) {
		super(id);
	}

	
	public ResultsPanel(String id, List<IModel<T>> list) {
		super(id);
		this.list = list;
	}
	 
 
	@Override
	public void onDetach() {
		super.onDetach();
		
		if (list!=null)	{
			list.forEach(i-> i.detach());
		}
		
		if (queryModel != null)
			queryModel.detach();
		
		
	}

 
	/** selections received from the SearchForm (displayed read-only in the toolbar) */
	public void setDateRange(DateRange dateRange) {
		this.dateRange = dateRange;
	}

	public void setSubjectOption(SubjectOption subjectOption) {
		this.subjectOption = subjectOption;
	}

	public void setTotalOption(TotalOption totalOption) {
		if (totalOption != null)
			this.totalOption = totalOption;
	}

	/** reasoning effort selected in the SearchForm, used in the calls to the LegalSearchService API */
	public void setReasoningEffortOption(ReasoningEffortOption reasoningEffortOption) {
		if (reasoningEffortOption != null)
			this.reasoningEffortOption = reasoningEffortOption;
	}

	public ReasoningEffortOption getReasoningEffortOption() {
		return this.reasoningEffortOption;
	}
	
	public void setHelpVisible(boolean b) {
		this.isHelpVisible = b;
	}
	
	protected void loadList() { 
		if (list == null)
			list = new ArrayList<IModel<T>>();
	}
	
	
	public void setList(List<IModel<T>> list) {
		this.list = list;
		
	}
	
	protected List<IModel<T>> getList() {
		//return this.list;
		return applyUserSettings(this.list);
	}
	

	/**
	 * Applies the user's preferences to the result list: sorting by date
	 * (most recent first) and limiting to the max number of results.
	 */
	protected List<IModel<T>> applyUserSettings(List<IModel<T>> source) {

		if (source == null)
			return new ArrayList<IModel<T>>();
 

		List<IModel<T>> result = new ArrayList<IModel<T>>(source);

		// filter by date range
		if (this.dateRange != null && this.dateRange != DateRange.ALL) {
			OffsetDateTime from = this.dateRange.getFrom(ZoneId.systemDefault());
			if (from != null) {
				result.removeIf(m -> {
					if (!(m.getObject() instanceof RAGSentencia))
						return false;
					OffsetDateTime fecha = ((RAGSentencia) m.getObject()).getFecha();
					return (fecha == null) || fecha.isBefore(from);
				});
			}
		}

		// filter by subject (materia). Documents with a null subject are only
		// included when the selected option is SubjectOption.TODOS
		if (this.subjectOption != null && this.subjectOption != SubjectOption.TODOS) {
			result.removeIf(m -> {
				if (!(m.getObject() instanceof RAGSentencia))
					return false;
				String subject = ((RAGSentencia) m.getObject()).getSubject();
				return (subject == null) || !subject.equalsIgnoreCase(subjectOption.getLabel());
			});
		}

		// sort by date, most recent first
		if (this.orderOption==OrderOption.MAS_RECIENTES) {
			result.sort(Comparator.comparing(
					(IModel<T> m) -> (m.getObject() instanceof RAGSentencia) ? ((RAGSentencia) m.getObject()).getFecha() : null,
					Comparator.nullsLast(Comparator.reverseOrder())));
		}
		else if	(this.orderOption==OrderOption.MAS_RELEVANTES) {
			result.sort(Comparator.comparing(
					(IModel<T> m) -> (m.getObject() instanceof RAGSentencia) ? ((RAGSentencia) m.getObject()).getScore() : null,
					Comparator.nullsLast(Comparator.reverseOrder())));
		}
		/**
		else if	(this.orderOption==OrderOption.MAYOR_COINCIDENCIA) {
			result.sort(Comparator.comparing(
					(IModel<T> m) -> (m.getObject() instanceof RAGSentencia) ? ((RAGSentencia) m.getObject()).getSemanticProximity() : null,
					Comparator.nullsLast(Comparator.naturalOrder())));
		}**/
		 
	
		// keep only the first N results
		int max = this.totalOption.getMax();
		if (max > 0 && result.size() > max)
			result = new ArrayList<IModel<T>>(result.subList(0, max));
		
		return result;
	}

	protected UserSettingsService getUserSettingsService() {
		return (UserSettingsService) ServiceLocator.getInstance().getBean(UserSettingsService.class);
	}

	protected TestQueriesService getTestQueriesService() {
		return (TestQueriesService) ServiceLocator.getInstance().getBean(TestQueriesService.class);
	}
	
	protected KbeeRAGClient getRAGClient() {
		return (KbeeRAGClient) ServiceLocator.getInstance().getBean(KbeeRAGClient.class);
	}

	/**
	 * Domain {@link io.demo.model.User} of the current session (resolved from
	 * Spring Security and cached in the Wicket session). Pass this to the
	 * services layer, which must never see Wicket types.
	 */
	protected java.util.Optional<io.demo.model.User> getSessionUser() {
		return io.demo.web.WebSessionUser.get();
	}

	/** Session user, required (throws if nobody is signed in). */
	protected io.demo.model.User requireSessionUser() {
		return io.demo.web.WebSessionUser.require();
	}

	/** Id of the current web session, to pass to the services layer. */
	protected String getSessionId() {
		return io.demo.web.WebSessionUser.getSessionId();
	}
	
	public void onBeforeRender() {
		super.onBeforeRender();
	}
	
	@Override
	public void addListeners() {
		super.addListeners();

		add(new UIEventListener<OrderOptionEvent>() {
			private static final long serialVersionUID = 1L;

			@Override
			public void onEvent(OrderOptionEvent event) {
				ResultsPanel.this.orderOption = event.getOption();
				refresh(event.getTarget());
			}
		});

		add(new UIEventListener<DateRangeEvent>() {
			private static final long serialVersionUID = 1L;

			@Override
			public void onEvent(DateRangeEvent event) {
				ResultsPanel.this.dateRange = event.getOption();
				refresh(event.getTarget());
			}
		});

		add(new UIEventListener<SubjectOptionEvent>() {
			private static final long serialVersionUID = 1L;

			@Override
			public void onEvent(SubjectOptionEvent event) {
				ResultsPanel.this.subjectOption = event.getOption();
				refresh(event.getTarget());
			}
		});

		add(new UIEventListener<TotalOptionEvent>() {
			private static final long serialVersionUID = 1L;

			@Override
			public void onEvent(TotalOptionEvent event) {
				ResultsPanel.this.totalOption = event.getOption();
				refresh(event.getTarget());
			}
		});

		add(new UIEventListener<QueryAnalysisEvent>() {
			private static final long serialVersionUID = 1L;

			@Override
			public void onEvent(QueryAnalysisEvent event) {
				ResultsPanel.this.showGeneralAnalysis(event.getTarget());
			}
		});
	}

	/**
	 * Displays the {@link GeneralAnalysisPanel} with the query general analysis,
	 * replacing the default {@link InvisiblePanel}. The analysis is obtained from
	 * the {@link LegalSearchService} (saved with the query, or requested to the
	 * RAG server).
	 */
	protected void showGeneralAnalysis(org.apache.wicket.ajax.AjaxRequestTarget target) {

		try {
			Query query = getQueryModel().getObject();

			// obtain (and save with the query) the general analysis; by default
			// the same llm used for the query
			getLegalSearchService().queryAnalysis(query, null);

			GeneralAnalysisPanel panel = new GeneralAnalysisPanel("generalanalysis", getQueryModel()) {
				private static final long serialVersionUID = 1L;

				@Override
				protected void onClose(org.apache.wicket.ajax.AjaxRequestTarget target) {
					generalAnalysisContainer.addOrReplace(new InvisiblePanel("generalanalysis"));
					if (target != null)
						target.add(generalAnalysisContainer);
				}
			};

			generalAnalysisContainer.addOrReplace(panel);

		} catch (Exception e) {
			logger.error(e);
			generalAnalysisContainer.addOrReplace(new ErrorPanel("generalanalysis", e));
		}

		if (target != null)
			target.add(generalAnalysisContainer);
	}

	protected LegalSearchService getLegalSearchService() {
		return (LegalSearchService) ServiceLocator.getInstance().getBean(LegalSearchService.class);
	}

	/**
	 * Refreshes the results (list panel and toolbar total) after a
	 * selection change in the toolbar.
	 */
	protected void refresh(org.apache.wicket.ajax.AjaxRequestTarget target) {
		if (this.toolbar != null)
			this.toolbar.setTotal(Integer.valueOf(getList().size()));
		if (target != null)
			target.add(contentsContainerContainer);
	}
	
	
	public void onInitialize() {
		super.onInitialize();
	
		contentsContainerContainer = new WebMarkupContainer("contentsContainer");
		contentsContainerContainer.setOutputMarkupId(true);
		add(contentsContainerContainer);
		
		AlertPanel<Void> q=new AlertPanel<Void>("query", AlertPanel.PRIMARY, Model.of(queryModel.getObject().getInfo()));
		contentsContainerContainer .add(q);
		
		
		errorContainer = new WebMarkupContainer("errorContainer") {
			private static final long serialVersionUID = 1L;

			public boolean isVisible() {
				return false;
				//return (getErrorPanel() != null) && getErrorPanel().isVisible();
			}
		};
	
		errorContainer.setOutputMarkupId(true);
		errorContainer.add(new InvisiblePanel("error"));
		contentsContainerContainer.add(errorContainer);

		// link that opens the RagResponse json file saved in the work
		// directory by the KbeeRAGClient for the current query
	
		
		/**Link<Void> ragResponseLink = new Link<Void>("ragresponse") {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				String q = getQuery();
				return (q != null) && getRAGClient().getResponseFile(q).exists();
			}

			@Override
			public void onClick() {
				File file = getRAGClient().getResponseFile(getQuery());
				FileResourceStream stream = new FileResourceStream(new org.apache.wicket.util.file.File(file));
				ResourceStreamRequestHandler handler = new ResourceStreamRequestHandler(stream, file.getName());
				handler.setContentDisposition(ContentDisposition.INLINE);
				getRequestCycle().scheduleRequestHandlerAfterCurrent(handler);
			}
		};
		ragResponseLink.add(new Label("name", new Model<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getObject() {
				String q = getQuery();
				return (q != null) ? getRAGClient().getResponseFile(q).getName() : "";
			}
		}));
		contentsContainerContainer.add(ragResponseLink);
	**/
	
		
		try {

			loadList();

			
			
			UserSettingsService settings = getUserSettingsService();
			if (settings.isRecentFirst())
				this.orderOption = OrderOption.MAS_RECIENTES;
			else
				this.orderOption = OrderOption.MAS_RELEVANTES;

			// keep the values received from the SearchForm; fall back to defaults
			if (this.dateRange == null)
				this.dateRange = DateRange.ALL;

			if (this.subjectOption == null)
				this.subjectOption = SubjectOption.TODOS;

			if (this.reasoningEffortOption == null)
				this.reasoningEffortOption = ReasoningEffortOption.getDefault();
			
			
			this.toolbar = new ToolbarResults("toolbar", null);
			this.toolbar.setOrderOption(this.orderOption);
			this.toolbar.setDateRange(this.dateRange);
			this.toolbar.setSubjectOption(this.subjectOption);
			this.toolbar.setTotalOption(this.totalOption);
			this.toolbar.setReasoningEffortOption(this.reasoningEffortOption);
			
			
			List<IModel<T>> li=getList();
			
			this.toolbar.setTotal(Integer.valueOf(li.size()));
			contentsContainerContainer.add(this.toolbar);
			
			// general analysis of the query: an InvisiblePanel until the
			// user clicks the "Análisis" button in the toolbar
			generalAnalysisContainer = new WebMarkupContainer("generalAnalysisContainer");
			generalAnalysisContainer.setOutputMarkupId(true);
			generalAnalysisContainer.add(new InvisiblePanel("generalanalysis"));
			contentsContainerContainer.add(generalAnalysisContainer);
			
			
			
			this.panel = new ListPanel<T>("contents") {

				private static final long serialVersionUID = 1L;

				
				
				public String getToolbarCss() {
					return " w-100 border rounded float-start pt-2 pb-2 ps-3 pe-3 mb-2";
				}
				
				
				@Override
				public List<IModel<T>> getItems() {
					return ResultsPanel.this.getList();
				}

				@Override
				public Integer getTotalItems() {
					return Integer.valueOf(ResultsPanel.this.getList().size());
				}

				@Override
				protected Panel getListItemExpandedPanel(IModel<T> model, ListPanelMode mode) {
					return  ResultsPanel.this.getObjectListItemExpandedPanel(model, mode);
				}

			 
				@Override
				protected Panel getListItemPanel(IModel<T> model, ListPanelMode mode) {

					DemoObjectListItemPanel<T> panel = new DemoObjectListItemPanel<T>("row-element", model, mode) {

						private static final long serialVersionUID = 1L;

						@Override
						public WebMarkupContainer getObjectMenu() {
							return ResultsPanel.this.getObjectMenu(getModel());
						}

						@Override
						public void onClick() {
							ResultsPanel.this.onClick(getModel());
						}

						public IModel<String> getInfo() {
							return ResultsPanel.this.getObjectInfo(getModel());
						}

						@Override
						public IModel<String> getObjectTitle() {
							return ResultsPanel.this.getObjectTitle(getModel());
						}

						@Override
						public  String getTitleIcon() {
							return ResultsPanel.this.getObjectTitleIcon(getModel());
						}

						protected IModel<String> getObjectSubtitle() {
							//if (getMode() == ListPanelMode.TITLE)
							//	return null;
							return ResultsPanel.this.getObjectSubtitle(getModel());
						}

						@Override
						public String getImageSrc() {
							return ResultsPanel.this.getObjectImageSrc(getModel());
						}

					};
					return panel;
					
				 }

				protected void onClick(IModel<T> model) {
				//	ResultsPanel.this.onClick(model);
					logger.debug("onClick: " + model.getObject().toString());
				 }

				//@Override
				//public IModel<String> getItemLabel(IModel<T> model) {
				//	return ResultsPanel.this.getObjectTitle(model);
				//}
			};

			this.panel.setBorder(false);
			
			this.panel.setHasExpander(true);
			this.panel.setSettings(false);
			this.panel.setToolbarVisible(false);
			this.panel.setPageSize(this.totalOption.getMax());
			
				
			//this.panel.setTitle(getListPanelLabel());
			this.panel.setListPanelMode(ListPanelMode.TITLE_TEXT);

			contentsContainerContainer.add(this.panel);

			// feedback editor: the user evaluates the query results
			contentsContainerContainer.add(new QueryFeedbackEditor("feedback", getQueryModel()));

		} catch (Exception e) {
			logger.error(e);
			contentsContainerContainer.addOrReplace(new ErrorPanel("contents", e));
			if (contentsContainerContainer.get("feedback") == null)
				contentsContainerContainer.add(new InvisiblePanel("feedback"));
		}
		
 
		 
	 
	}

	protected Panel getObjectListItemExpandedPanel(IModel<T> model, ListPanelMode mode) {
		// displays the Analysis and Quotes of the document according to the
		// user's preferences (UserSettingsService)
		return new AnalysisPanel<T>("expanded-panel", model, getQueryModel());
	}

	private IModel<Query> getQueryModel() {
		// TODO Auto-generated method stub
		return this.queryModel;
	}


	protected String getObjectTitleIcon(IModel<T> model) {
		// TODO Auto-generated method stub
		return null;
	}

	protected IModel<String> getObjectTitle(IModel<T> model) {
	
		String title = model.getObject().getTitle();
		
		
		if (model.getObject().getPjsfDocumentId()!=null) {
			String id=	model.getObject().getPjsfDocumentId();	
			boolean  b= getTestQueriesService().check(getQueryModel().getObject().getQuery(), id);
			return Model.of( title + (b ? " <span class=\"badge bg-warning\">#</span>" : "") );
			
		}
		else {
			
			return new Model<String>(model.getObject().getTitle());
		}
		
	}


	
	public Settings getSettingsService() {
		return (Settings) ServiceLocator.getInstance().getBean(Settings.class);
	}

	
	protected void onClick(IModel<T> model) {
		
		String base=  getSettingsService().getBasePJSFUrl();
		
		//t"https://portal.justiciasantafe.gov.ar/bdj/index.php?pg=bus&m=busqueda&c=busqueda&a=get&id="; //53492
		
		if (model.getObject().getPjsfDocumentId()!=null) {
			//logger.debug(base+model.getObject().getPjsfDocumentId());
			setResponsePage(new RedirectPage(base+model.getObject().getPjsfDocumentId()));
			
		}
		else {
			logger.error("ragDocumentId is null for " + model.getObject().getTitle());
		}
		
	}

	

	protected String getObjectImageSrc(IModel<T> model) {
		// TODO Auto-generated method stub
		return null;
	}

	protected IModel<String> getObjectSubtitle(IModel<T> model) {
		//logger.debug("getObjectSubtitle: " + model.getObject().getSubtitle());
		return Model.of( model.getObject().getSubtitle() );
	}

	protected IModel<String> getObjectInfo(IModel<T> model) {
		// TODO Auto-generated method stub
		return null;
	}

	protected WebMarkupContainer getObjectMenu(IModel<T> model) {
		// TODO Auto-generated method stub
		return null;
	}


	public void setQueryModel(ObjectModel<Query> objectModel) {

			this.queryModel = objectModel;
			
	}
	
	
	
}
