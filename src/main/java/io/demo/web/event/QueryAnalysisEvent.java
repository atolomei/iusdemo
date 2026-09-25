package io.demo.web.event;

import org.apache.wicket.ajax.AjaxRequestTarget;

import io.wktui.event.SimpleAjaxWicketEvent;

/**
 * Fired by the ToolbarResults when the user clicks the "Análisis" button to
 * display the general analysis of the query.
 */
public class QueryAnalysisEvent extends SimpleAjaxWicketEvent {

	public QueryAnalysisEvent(AjaxRequestTarget target) {
		super(null, target, null);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{}";
	}
}
