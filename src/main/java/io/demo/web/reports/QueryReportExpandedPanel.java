package io.demo.web.reports;

import java.util.Optional;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.util.resource.StringResourceStream;

import io.demo.model.Query;
import io.demo.model.User;
import io.demo.model.db.service.QueryDBService;
import io.demo.service.DateTimeService;
import io.demo.service.ServiceLocator;
import wktui.base.ModelPanel;

/**
 * Expanded panel with the details of a {@link Query}: id, query text,
 * duration, last modified, user, and a link to open the JSON results in a new
 * tab.
 */
public class QueryReportExpandedPanel extends ModelPanel<Query> {

	private static final long serialVersionUID = 1L;

	public QueryReportExpandedPanel(String id, IModel<Query> model) {
		super(id, model);
		setOutputMarkupId(true);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		Optional<Query> oquery = getQueryDBService().findWithDeps(getModel().getObject().getId());

		
		if (oquery.isEmpty()) {
			add(new Label("id", "-"));
			add(new Label("query", "-"));
			add(new Label("duration", "-"));
			add(new Label("lastModified", "-"));
			add(new Label("user", "-"));
		
			add(new Label("dateRange", "" ));
			add(new Label("subject", "-"));
			add(new Label("reasoningEffort", "-"));
			
			
			Link<Query> resultsLink = new Link<Query>("resultsLink", getModel()) {
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick() {
				}
			};
			resultsLink.setEnabled(false);
			add(resultsLink);
			return;
		}
		
		
		
		Query query = oquery.get();
		
		add(new Label("id", query.getId() != null ? query.getId().toString() : "-"));
		add(new Label("query", query.getQuery() != null ? query.getQuery() : "-"));
		add(new Label("duration", formatDuration(query.getDurationMillisecs())));
		add(new Label("lastModified", query.getLastModified() != null ? getDateTimeService().format(query.getLastModified()) : "-"));

		add(new Label("dateRange", io.demo.results.DateRange.fromOrdinal(query.getDateRangeOption()).getLabel()));
		add(new Label("subject",  io.demo.results.SubjectOption.fromOrdinal(query.getSubjectOption())));
		add(new Label("reasoningEffort", io.demo.results.ReasoningEffortOption.fromOrdinal(query.getReasoningEffortOption())));
		
		
		
		
		
		
		User user = query.getLastModifiedUser();
		add(new Label("user", (user != null && user.getUsername() != null) ? user.getUsername() : "-"));

		Link<Query> resultsLink = new Link<Query>("resultsLink", getModel()) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				String json = getModelObject().getResults() != null ? getModelObject().getResults() : "{}";
				getRequestCycle().scheduleRequestHandlerAfterCurrent(new ResourceStreamRequestHandler(new StringResourceStream(json, "application/json")));
			}

			@Override
			public boolean isEnabled() {
				return getModelObject().getResults() != null;
			}
		};
		resultsLink.add(AttributeModifier.replace("target", "_blank"));
		add(resultsLink);
	}

	protected String formatDuration(long millisecs) {
		if (millisecs < 1000)
			return millisecs + " ms";
		return String.format("%.2f s", millisecs / 1000.0);
	}

	protected DateTimeService getDateTimeService() {
		return (DateTimeService) ServiceLocator.getInstance().getBean(DateTimeService.class);
	}
	
	public QueryDBService getQueryDBService() {
		return (QueryDBService) ServiceLocator.getInstance().getBean(QueryDBService.class);
	}

}
