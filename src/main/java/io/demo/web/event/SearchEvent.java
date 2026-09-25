package io.demo.web.event;

import org.apache.wicket.ajax.AjaxRequestTarget;

import io.demo.results.DateRange;
import io.demo.results.ReasoningEffortOption;
import io.demo.results.SubjectOption;
import io.wktui.event.SimpleAjaxWicketEvent;

/**
 * WicketAjaxEvent fired by the SearchFormEditor when the user submits a
 * search. Carries the text to search, the search options (date range,
 * subject, reasoning effort) and the {@link AjaxRequestTarget}.
 */
public class SearchEvent extends SimpleAjaxWicketEvent {

	private final String searchText;

	private final DateRange dateRange;
	private final SubjectOption subjectOption;
	private final ReasoningEffortOption reasoningEffortOption;

	public SearchEvent(String searchText, AjaxRequestTarget target) {
		this(searchText, null, null, null, target);
	}

	public SearchEvent(String searchText, DateRange dateRange, SubjectOption subjectOption, ReasoningEffortOption reasoningEffortOption, AjaxRequestTarget target) {
		super(null, target, null);
		this.searchText = searchText;
		this.dateRange = dateRange;
		this.subjectOption = subjectOption;
		this.reasoningEffortOption = reasoningEffortOption;
	}

	public String getSearchText() {
		return this.searchText;
	}

	public DateRange getDateRange() {
		return this.dateRange;
	}

	public SubjectOption getSubjectOption() {
		return this.subjectOption;
	}

	public ReasoningEffortOption getReasoningEffortOption() {
		return this.reasoningEffortOption;
	}

	@Override
	public String toString() {

		return getClass().getSimpleName()
				+ "{ \"searchText\": \"" + getSearchText() + "\""
				+ ", \"dateRange\": \"" + (getDateRange() != null ? getDateRange().getLabel() : "null") + "\""
				+ ", \"reasoningEffortOption\": \"" + (getReasoningEffortOption() != null ? getReasoningEffortOption().getLabel() : "null") + "\""
				+ ", \"subjectOption\": \"" + (getSubjectOption() != null ? getSubjectOption().getLabel() : "null") + "\" }";
	}

}