package io.demo.web.event;

import org.apache.wicket.ajax.AjaxRequestTarget;

import io.demo.results.DateRange;
import io.wktui.event.SimpleAjaxWicketEvent;

/**
 * Fired by the ToolbarResults when the user changes the date range selector.
 */
public class DateRangeEvent extends SimpleAjaxWicketEvent {

	private final DateRange option;

	public DateRangeEvent(DateRange option, AjaxRequestTarget target) {
		super(null, target, null);
		this.option = option;
	}

	public DateRange getOption() {
		return this.option;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{ \"option\": \"" + getOption() + "\"}";
	}
}
