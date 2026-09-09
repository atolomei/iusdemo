package io.demo.web.event;

import org.apache.wicket.ajax.AjaxRequestTarget;

import io.demo.results.OrderOption;
import io.wktui.event.SimpleAjaxWicketEvent;

/**
 * Fired by the ToolbarResults when the user changes the order selector.
 */
public class OrderOptionEvent extends SimpleAjaxWicketEvent {

	private final OrderOption option;

	public OrderOptionEvent(OrderOption option, AjaxRequestTarget target) {
		super(null, target, null);
		this.option = option;
	}

	public OrderOption getOption() {
		return this.option;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{ \"option\": \"" + getOption() + "\"}";
	}
}
