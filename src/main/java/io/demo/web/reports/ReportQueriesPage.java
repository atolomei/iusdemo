package io.demo.web.reports;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

import io.demo.Logger;
import io.demo.model.Query;
import io.demo.model.Role;
import io.demo.model.Stat;
import io.demo.model.User;
import io.demo.model.db.service.QueryDBService;
import io.demo.service.ServiceLocator;
import io.demo.web.home.DemoHomePage;
import io.demo.web.page.BasePage;
import io.demo.web.page.DemoBasePage;
import io.demo.web.panel.ObjectModel;
import io.demo.web.panel.PageHeaderPanel;
import io.demo.web.panel.SimpleHeaderPanel;
import io.wktui.error.ErrorPanel;
import io.wktui.nav.breadcrumb.BreadCrumb;
import io.wktui.struct.list.ListPanel;
import io.wktui.struct.list.ListPanelMode;

/**
 * Report of the 1000 most recent user queries.
 */
@MountPath("/reports/queries")
public class ReportQueriesPage extends DemoBasePage {

	private static final long serialVersionUID = 1L;

	static private Logger logger = Logger.getLogger(ReportQueriesPage.class.getName());

	static final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

	private static final int MAX_QUERIES = 10000;

	private List<IModel<Query>> queries;
	
	private DateRange selectedRange = DateRange.LAST_7_DAYS;

	private WebMarkupContainer signinContainer;
	

	public ReportQueriesPage(PageParameters parameters) {
		super(parameters);
	}

	
	@Override
	public boolean canAccess(Optional<User> ouser) {
		
		if (ouser.isEmpty())
			return false;
		
		
		
		Role role = ouser.get().getRole();
		
		if (role==null)
			return false;
		
	
		return true;
		
		
	} 
	
	@Override
	public void onInitialize() {
		super.onInitialize();

		loadRows();
		
		DropDownChoice<DateRange> rangeSelector = new DropDownChoice<DateRange>("queriesRangeSelector", new PropertyModel<DateRange>(this, "selectedRange"), Arrays.asList(DateRange.values()), new IChoiceRenderer<DateRange>() {
			private static final long serialVersionUID = 1L;

			@Override
			public Object getDisplayValue(DateRange object) {
				return getString(object.getKey(), null, object.getKey());
			}

			@Override
			public String getIdValue(DateRange object, int index) {
				return object.name();
			}

			@Override
			public DateRange getObject(String id, IModel<? extends List<? extends DateRange>> choices) {
				return DateRange.valueOf(id);
			}
		});

		signinContainer = new WebMarkupContainer("queriesContainer");
		signinContainer.setOutputMarkupId(true);
		add(signinContainer);

		rangeSelector.add(new AjaxFormComponentUpdatingBehavior("change") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				loadRows();
				target.add(signinContainer);
			}
		});
		add(rangeSelector);
		

		ListPanel<Query> queryListPanel = new ListPanel<Query>("queryList") {
			private static final long serialVersionUID = 1L;

			@Override
			public IModel<String> getItemLabel(IModel<Query> model) {
				return Model.of(model.getObject().getQuery());
			}

			@Override
			public List<IModel<Query>> getItems() {
				return queries;
			}

			@Override
			protected void onClick(IModel<Query> model) {
				PageParameters parameters = new PageParameters();
				parameters.add("query", model.getObject().getQuery());
				setResponsePage(new DemoHomePage(parameters));
			}

			@Override
			protected WebMarkupContainer getListItemExpandedPanel(IModel<Query> model, ListPanelMode mode) {
				return new QueryReportExpandedPanel("expanded-panel", model);
			}
		};
		queryListPanel.setHasExpander(true);
		queryListPanel.setLiveSearch(true);
		queryListPanel.setSettings(true);
		signinContainer.add(queryListPanel);
	}
	

	/**
	private void buildSigninList() {

		ZoneId zoneId = getZoneId();
		OffsetDateTime from = selectedRange.getFrom(zoneId);
		OffsetDateTime to = selectedRange.getTo(zoneId);

		List<Stat> signins = getStatDBService().getRecentSignins(from, to);

		rows = new ArrayList<>();
		for (Stat v : signins) {
			String name = displayName(v.getUser());
			String ts = v.getTimestamp() != null ? df.format(v.getTimestamp().atZoneSameInstant(zoneId)) : "";
			rows.add(new VisitRow(name, ts));
		}

		signinListPanel = new ListPanel<VisitRow>("signinList") {
			private static final long serialVersionUID = 1L;

			@Override
			public IModel<String> getItemLabel(IModel<VisitRow> model) {
				VisitRow row = model.getObject();
				return Model.of("<span class=\"float-start\">" + row.getName() + "</span><span class=\"float-end\">" + row.getTimestamp() + "</span>");
			}

			@Override
			public List<IModel<VisitRow>> getItems() {
				List<IModel<VisitRow>> models = new ArrayList<>();
				for (VisitRow row : rows)
					models.add(Model.of(row));
				return models;
			}
		};
		signinListPanel.setHasExpander(false);
		signinListPanel.setLiveSearch(false);
		signinListPanel.setSettings(false);
		signinContainer.addOrReplace(signinListPanel);
	}
	**/
	
	protected Panel createHeaderPanel() {
		try {

			BreadCrumb<Void> bc = createBreadCrumb();
			bc.addElement(new io.wktui.nav.breadcrumb.BCElement( Model.of("Consultas")));

			// bc.addElement(new HREFBCElement("/home", Model.of("home")));

			//SimpleHeaderPanel ph = new SimpleHeaderPanel("page-header", getOptionalSessionUserModel());
			
			PageHeaderPanel<User> ph = new PageHeaderPanel<User>("page-header", getSessionUserModel(), Model.of("Consultas"));
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
	

	private void loadRows() {
		this.queries = new ArrayList<IModel<Query>>();
		try {
			

			ZoneId zoneId = getZoneId();
			
			OffsetDateTime from = selectedRange.getFrom(zoneId);
			OffsetDateTime to = selectedRange.getTo(zoneId);

			List<Query> list = getQueryDBService().getRecent(MAX_QUERIES, from, to);

			
			
			for (Query q : list)
				queries.add(new ObjectModel<Query>(q));
		} catch (Exception e) {
			logger.error(e);
		}
	}

	private ZoneId getZoneId() {
		try {
			if (getSessionUser().isPresent() && getSessionUser().get().getZoneId() != null)
				return ZoneId.of(getSessionUser().get().getZoneId());
		} catch (Exception e) {
			logger.error(e);
		}
		return ZoneId.systemDefault();
	}

	public QueryDBService getQueryDBService() {
		return (QueryDBService) ServiceLocator.getInstance().getBean(QueryDBService.class);
	}

	@Override
	protected String getToolbarTitle() {
		return "Buscador Juridico";
	}

	@Override
	public void onDetach() {
		super.onDetach();
		if (queries != null)
			queries.forEach(m -> m.detach());
	}


	@Override
	protected void addListeners() {
		// TODO Auto-generated method stub
		
	}
}