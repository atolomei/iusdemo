package io.demo.web.event;

import org.apache.wicket.ajax.AjaxRequestTarget;

import io.demo.results.SubjectOption;
import io.wktui.event.SimpleAjaxWicketEvent;

/**
 * Fired by the ToolbarResults when the user changes the subject selector.
 */
public class SubjectOptionEvent extends SimpleAjaxWicketEvent {

	private final SubjectOption option;

	public SubjectOptionEvent(SubjectOption option, AjaxRequestTarget target) {
		super(null, target, null);
		this.option = option;
	}

	public SubjectOption getOption() {
		return this.option;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{ \"option\": \"" + getOption() + "\"}";
	}
}
