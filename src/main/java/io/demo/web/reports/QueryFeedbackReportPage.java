package io.demo.web.reports;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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

import io.demo.Logger;
import io.demo.model.Query;
import io.demo.model.QueryFeedback;
import io.demo.model.QueryFeedbackGrade;
import io.demo.model.Role;
import io.demo.model.User;
import io.demo.model.db.service.DBService;
import io.demo.model.db.service.QueryDBService;
import io.demo.model.db.service.QueryFeedbackDBService;
import io.demo.model.db.service.UserDBService;
import io.demo.service.ServiceLocator;
import io.demo.web.page.DemoBasePage;
import io.demo.web.panel.SimpleHeaderPanel;
import io.wktui.error.ErrorPanel;
import io.wktui.nav.breadcrumb.BreadCrumb;
import io.wktui.struct.list.ListPanel;
import io.wktui.struct.list.ListPanelMode;

/**
 * Report of the most recent {@link QueryFeedback}s. For each feedback the
 * title is "username - (query id) - first 52 letters of the query - grade";
 * the expanded panel shows user, date, query, grade and info.
 */
@MountPath("/reports/queryfeedback")
public class QueryFeedbackReportPage extends DemoBasePage {

	private static final long serialVersionUID = 1L;

	static private Logger logger = Logger.getLogger(QueryFeedbackReportPage.class.getName());

	static final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

	private static final int MAX_FEEDBACKS = 1000;
	private static final int MAX_QUERY_LETTERS = 148;

	private List<FeedbackRow> rows;

	public QueryFeedbackReportPage(PageParameters parameters) {
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

	
	
	@Override
	public void onInitialize() {
		super.onInitialize();

		loadRows();

		WebMarkupContainer feedbackContainer = new WebMarkupContainer("feedbackContainer");
		feedbackContainer.setOutputMarkupId(true);
		add(feedbackContainer);

		ListPanel<FeedbackRow> feedbackListPanel = new ListPanel<FeedbackRow>("feedbackList") {
			private static final long serialVersionUID = 1L;

			@Override
			public IModel<String> getItemLabel(IModel<FeedbackRow> model) {
				return Model.of(model.getObject().getTitle());
			}

			@Override
			public List<IModel<FeedbackRow>> getItems() {
				List<IModel<FeedbackRow>> models = new ArrayList<>();
				for (FeedbackRow row : rows)
					models.add(Model.of(row));
				return models;
			}

			@Override
			protected WebMarkupContainer getListItemExpandedPanel(IModel<FeedbackRow> model, ListPanelMode mode) {
				return new QueryFeedbackExpandedPanel("expanded-panel", model);
			}
		};
		feedbackListPanel.setHasExpander(true);
		feedbackListPanel.setLiveSearch(false);
		feedbackListPanel.setSettings(false);
		feedbackContainer.add(feedbackListPanel);
	}

	protected Panel createHeaderPanel() {
		try {
			BreadCrumb<Void> bc = createBreadCrumb();
			bc.addElement(new io.wktui.nav.breadcrumb.BCElement(Model.of("evaluaciones")));

			SimpleHeaderPanel ph = new SimpleHeaderPanel("page-header", getOptionalSessionUserModel());
			ph.setBreadCrumb(bc);
			return ph;

		} catch (Exception e) {
			logger.error(e);
			return new ErrorPanel("page-header", e);
		}
	}

	private void loadRows() {
		rows = new ArrayList<>();
		try {
			ZoneId zoneId = getZoneId();
			for (QueryFeedback f : getQueryFeedbackDBService().getRecent(MAX_FEEDBACKS)) {

				
				// problem with lazy loading: f.getLastModifiedUser() is null, so we need to load the user from the database
				User user =  getUserDBService().findById(f.getLastModifiedUser().getId()).orElse(null);
				String username = (user != null) ? user.getName() : "[null]";


				// problem with lazy loading: f.getQuery() is null, so we need to load the query from the database
				Query query =  getQueryDBService().findById(f.getQuery().getId()).orElse(null); 
						
				Long queryId = (query != null) ? query.getId() : null;
				String queryText = (query != null && query.getQuery() != null) ? query.getQuery() : "";

				QueryFeedbackGrade grade = f.getGradeEnum();
				String gradeName = (grade != null) ? grade.getDisplayName() : "sin calificación";

				String ts = (f.getCreated() != null) ? df.format(f.getCreated().atZoneSameInstant(zoneId)) : "";

				rows.add(new FeedbackRow(username, queryId, queryText, gradeName, f.getInfo(), ts));
			}
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

	

	
	@Override
	protected String getToolbarTitle() {
		return "Buscador Juridico";
	}

	/** row of the report: user, query, grade, info and date */
	public static class FeedbackRow implements java.io.Serializable {
		private static final long serialVersionUID = 1L;

		private final String username;
		private final Long queryId;
		private final String query;
		private final String grade;
		private final String info;
		private final String timestamp;

		FeedbackRow(String username, Long queryId, String query, String grade, String info, String timestamp) {
			this.username = username;
			this.queryId = queryId;
			this.query = query;
			this.grade = grade;
			this.info = info;
			this.timestamp = timestamp;
		}

		/** "username - (query id) - first 52 letters of the query - grade" */
		public String getTitle() {
			String q = (query != null && query.length() > MAX_QUERY_LETTERS) ? query.substring(0, MAX_QUERY_LETTERS) + "…" : query;
			return username + " - (" + (queryId != null ? queryId : "-") + ") - " + q;
		}

		public String getUsername() {
			return username;
		}

		public Long getQueryId() {
			return queryId;
		}

		public String getQuery() {
			return query;
		}

		public String getGrade() {
			return grade;
		}

		public String getInfo() {
			return info;
		}

		public String getTimestamp() {
			return timestamp;
		}
	}

	/** expanded panel: user, date, query, grade and info */
	public static class QueryFeedbackExpandedPanel extends org.apache.wicket.markup.html.panel.Panel {

		private static final long serialVersionUID = 1L;

		public QueryFeedbackExpandedPanel(String id, IModel<FeedbackRow> model) {
			super(id, model);

			FeedbackRow row = model.getObject();
			add(new Label("user", row.getUsername()));
			add(new Label("date", row.getTimestamp()));
			add(new Label("query", row.getQuery()));
			add(new Label("id",  row.getQueryId()));
			
			add(new Label("grade", row.getGrade()));
			add(new Label("info", row.getInfo() != null ? row.getInfo() : ""));
			
		}
	}
}
