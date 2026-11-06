package io.demo.web.event;

import org.apache.wicket.ajax.AjaxRequestTarget;

import io.wktui.event.SimpleAjaxWicketEvent;

/**
 * Fired by the ToolbarResults when the user clicks the "Enviar por email"
 * button to send the query and the list of results by email.
 */
public class SendResultsEmailEvent extends SimpleAjaxWicketEvent {

	public SendResultsEmailEvent(AjaxRequestTarget target) {
		super(null, target, null);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{}";
	}
}
