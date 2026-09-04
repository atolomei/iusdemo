package io.demo.results;

import java.io.File;
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
import io.demo.model.Documento;
import io.demo.model.Sentencia;
import io.demo.service.ServiceLocator;
import io.demo.service.TestQueriesService;
import io.demo.service.UserSettingsService;
import io.demo.service.rag.KbeeRAGClient;
import io.demo.web.home.AnalysisPanel;
import io.demo.web.page.DemoObjectListItemPanel;
import io.wktui.error.AlertPanel;
import io.wktui.error.ErrorPanel;
import io.wktui.struct.list.ListPanel;
import io.wktui.struct.list.ListPanelMode;
import wktui.base.BasePanel;
import wktui.base.DummyBlockPanel;
import wktui.base.InvisiblePanel;

public class ResultsPanel<T extends Documento> extends BasePanel {

	private static final long serialVersionUID = 1L;

	static private Logger logger = Logger.getLogger( ResultsPanel.class.getName());
	
	private List<IModel<T>> list;
	private ListPanel<T> panel;

	 
	private WebMarkupContainer contentsContainerContainer;
	private WebMarkupContainer errorContainer;
	private WebMarkupContainer listToolbarContainer;
	private WebMarkupContainer helpContainer;

	
	private ToolbarResults toolbar;
	
	
	private Panel errorPanel;

	private boolean b_expand = false;
	private boolean isHelpVisible = false;

	/** the query whose RagResponse was saved to disk by {@link KbeeRAGClient} */
	private String query;

	public void setQuery(String query) {
		this.query = query;
	}

	public String getQuery() {
		return this.query;
	}
	
	
	public ResultsPanel(String id) {
		super(id);
		 
	}

	public ResultsPanel(String id, List<IModel<T>> list) {
		super(id);
		this.list = list;
		 
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
		return applyUserSettings(this.list);
	}

	/**
	 * Applies the user's preferences to the result list: sorting by date
	 * (most recent first) and limiting to the max number of results.
	 */
	protected List<IModel<T>> applyUserSettings(List<IModel<T>> source) {

		if (source == null)
			return new ArrayList<IModel<T>>();

		UserSettingsService settings = getUserSettingsService();

		List<IModel<T>> result = new ArrayList<IModel<T>>(source);

		// sort by date, most recent first
		if (settings.isRecentFirst()) {
			result.sort(Comparator.comparing(
					(IModel<T> m) -> (m.getObject() instanceof Sentencia) ? ((Sentencia) m.getObject()).getFecha() : null,
					Comparator.nullsLast(Comparator.reverseOrder())));
		}

		// keep only the first N results
		int max = settings.getMaxSearchResults();
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
	
	
	public void onBeforeRender() {
		super.onBeforeRender();
	}
	
	
	public void onInitialize() {
		super.onInitialize();
	
		contentsContainerContainer = new WebMarkupContainer("contentsContainer");
		contentsContainerContainer.setOutputMarkupId(true);
		add(contentsContainerContainer);

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

			
			this.toolbar = new ToolbarResults("toolbar", null);
			add(this.toolbar);
			
			
			
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

			this.panel.setHasExpander(true);
			this.panel.setSettings(true);
			
			//this.panel.setTitle(getListPanelLabel());
			this.panel.setListPanelMode(ListPanelMode.TITLE_TEXT);

			contentsContainerContainer.add(this.panel);

		} catch (Exception e) {
			logger.error(e);
			contentsContainerContainer.addOrReplace(new ErrorPanel("contents", e));
		}
		
 
		 
	 
	}

	protected Panel getObjectListItemExpandedPanel(IModel<T> model, ListPanelMode mode) {
		// displays the Analysis and Quotes of the document according to the
		// user's preferences (UserSettingsService)
	
		
		
		
		return new AnalysisPanel<T>("expanded-panel", model, getQuery());
	}

	protected String getObjectTitleIcon(IModel<T> model) {
		// TODO Auto-generated method stub
		return null;
	}

	protected IModel<String> getObjectTitle(IModel<T> model) {
	
		String title = model.getObject().getTitle();
		
		
		if (model.getObject().getPjsfDocumentId()!=null) {
			String id=	model.getObject().getPjsfDocumentId();	
			boolean  b= getTestQueriesService().check(getQuery(), id);
			return Model.of( title + (b ? " <span class=\"badge bg-warning\">#</span>" : "") );
			
		}
		else {
			
			return new Model<String>(model.getObject().getTitle());
		}
		 
		
		
		
	}


	protected void onClick(IModel<T> model) {

		
		String base= "https://portal.justiciasantafe.gov.ar/bdj/index.php?pg=bus&m=busqueda&c=busqueda&a=get&id="; //53492
	
		
		if (model.getObject().getPjsfDocumentId()!=null) {
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
		logger.debug("getObjectSubtitle: " + model.getObject().getSubtitle());
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
	
	
	
}
