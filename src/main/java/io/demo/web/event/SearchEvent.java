package io.demo.web.event;

import org.apache.wicket.ajax.AjaxRequestTarget;

import io.wktui.event.SimpleAjaxWicketEvent;

/**
 * WicketAjaxEvent fired by the SearchFormEditor when the user submits a
 * search. Carries the text to search and the {@link AjaxRequestTarget}.
 */
public class SearchEvent extends SimpleAjaxWicketEvent {

	private final String searchText;

	public SearchEvent(String searchText, AjaxRequestTarget target) {
		super(null, target, null);
		this.searchText = searchText;
	}

	public String getSearchText() {
		return this.searchText;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{ \"searchText\": \"" + getSearchText() + "\"}";
	}
}
