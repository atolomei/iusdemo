package io.demo.web.event;

import org.apache.wicket.ajax.AjaxRequestTarget;

import io.demo.results.QueryLevelOption;
import io.wktui.event.SimpleAjaxWicketEvent;

/**
 * Fired by the ToolbarResults when the user changes the query level selector.
 * A null option means "TODOS".
 */
public class QueryLevelOptionEvent extends SimpleAjaxWicketEvent {

	private final QueryLevelOption option;

	public QueryLevelOptionEvent(QueryLevelOption option, AjaxRequestTarget target) {
		super(null, target, null);
		this.option = option;
	}

	/** @return the selected level, or null for "TODOS" */
	public QueryLevelOption getOption() {
		return this.option;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{ \"option\": \"" + getOption() + "\"}";
	}
}
