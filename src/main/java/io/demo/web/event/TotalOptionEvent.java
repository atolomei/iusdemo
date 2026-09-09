package io.demo.web.event;

import org.apache.wicket.ajax.AjaxRequestTarget;

import io.demo.results.TotalOption;
import io.wktui.event.SimpleAjaxWicketEvent;

/**
 * Fired by the ToolbarResults when the user changes the max results selector.
 */
public class TotalOptionEvent extends SimpleAjaxWicketEvent {

	private final TotalOption option;

	public TotalOptionEvent(TotalOption option, AjaxRequestTarget target) {
		super(null, target, null);
		this.option = option;
	}

	public TotalOption getOption() {
		return this.option;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{ \"option\": \"" + getOption() + "\"}";
	}
}
