package io.demo.web.settings;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

import io.demo.web.page.BasePage;
import io.demo.web.page.GlobalTopPanel;

/**
 * Page to edit the user's preferences ({@code UserSettingsService}).
 */
@MountPath("/usersettings")
public class UserSettingsPage extends BasePage {

	private static final long serialVersionUID = 1L;

	public UserSettingsPage() {
		this(new PageParameters());
	}

	public UserSettingsPage(PageParameters parameters) {
		super(parameters);
	}

	@Override
	protected String getToolbarTitle() {
		return "Buscador Juridico";
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		add(new GlobalTopPanel("top-panel", null));
		add(new UserSettingsEditor("settings"));
	}
}
