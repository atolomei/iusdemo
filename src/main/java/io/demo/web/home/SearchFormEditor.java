package io.demo.web.home;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

import io.wktui.form.Form;
import io.wktui.form.FormState;
import io.wktui.form.button.EditButtons;
import io.wktui.form.button.SubmitButton;
import io.wktui.form.field.BooleanField;
import io.wktui.form.field.ChoiceField;
import io.wktui.form.field.Field;

import io.wktui.form.field.OffsetDateTimeField;
import io.wktui.form.field.StaticTextField;
import io.wktui.form.field.TextAreaField;
import io.wktui.form.field.TextField;
import io.wktui.nav.toolbar.AjaxButtonToolbarItem;
import io.wktui.nav.toolbar.ToolbarItem;
import io.wktui.nav.toolbar.ToolbarItem.Align;
import io.wktui.struct.list.ListPanel;
import io.demo.Logger;
import io.demo.service.QueryHistoryService;
import io.demo.service.ServiceLocator;
import io.demo.service.UserSettingsService;
import io.demo.web.event.SearchEvent;
import io.wktui.editor.ObjectUpdateEvent;
import io.wktui.error.AlertPanel;
import io.wktui.error.SimpleAlertRow;
import io.wktui.event.MenuAjaxEvent;
import wktui.base.InvisiblePanel;

public class SearchFormEditor extends ObjectEditor<String>   {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	static private Logger logger = Logger.getLogger(SearchFormEditor.class.getName());

	private TextAreaField<String> textField;
	
	private boolean submitted = false;

	private String text;
	
	
	ListPanel<String> historyPanel;

	/** Container of the history section, collapsed by default. */
	private WebMarkupContainer historyContainer;

	/** Whether the history section is expanded. Collapsed by default. */
	private boolean historyVisible = false;


	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public SearchFormEditor(String id) {
		this(id, Model.of(new String()));
	}

	public SearchFormEditor(String id, IModel<String> model) {
		super(id, model);
		this.setOutputMarkupId(true);
	}

	@Override
	public void onDetach() {
		super.onDetach();

	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		//setUpModel();

		this.historyContainer = new WebMarkupContainer("historyContainer") {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return historyVisible;
			}
		};
		this.historyContainer.setOutputMarkupPlaceholderTag(true);
		add(this.historyContainer);

		historyPanel = new ListPanel<String>("history",  getHistoryModel()) {

			@Override
			protected String getListGroupItemCss() {
				return "list-group-item  border-0";
			}
		};

		
		historyPanel.setHasExpander(false);	

		historyPanel.setItemMenu(false);	
		historyPanel.setSettings(false);
		historyPanel.setToolbarVisible(false);
		
		this.historyContainer.add(historyPanel);
		
		
		
		add(new InvisiblePanel("error"));
		add(new InvisiblePanel("success"));

		Form<String> form = new Form<String>("searchForm", getModel());

		form.setOutputMarkupId(true);
		add(form);
		setForm(form);

		textField = new TextAreaField<String>("text", getTextModel(), getLabel("text"), 9);
		
		getForm().add(textField);

		 

		SubmitButton<String> sm = new SubmitButton<String>("send", getModel(), getForm()) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(AjaxRequestTarget target) {
				SearchFormEditor.this.onSave(target);
			}

			@Override
			public boolean isVisible() {
				return !submitted;
			}

			@Override
			public boolean isEnabled() {
				return true;
			}

			public IModel<String> getLabel() {
				return getLabel("submit");
			}

			@Override
			public String getRowCss() {
				return "d-inline-block float-left";
			}

			@Override
			public String getColCss() {
				return "d-inline-block float-left";
			}

			@Override
			public String getSaveCss() {
				return "btn btn-primary btn-md";
			}
		};

		getForm().add(sm);

		// link to expand/collapse the recent queries panel
		AjaxLink<Void> toggleHistory = new AjaxLink<Void>("toggleHistory") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				historyVisible = !historyVisible;
				if (historyVisible)
					refreshHistoryPanel();
				target.add(SearchFormEditor.this);
			}
		};
		toggleHistory.add(new Label("toggleHistoryLabel", new Model<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getObject() {
				return historyVisible ? "cerrar consultas recientes" : "ver consultas recientes";
			}
		}));
		getForm().add(toggleHistory);
		
		edit();

		form.updateModel();
		form.updateReload();
		form.visitChildren(Field.class, new IVisitor<Field<?>, Void>() {
			@Override
			public void component(Field<?> field, IVisit<Void> visit) {
				field.editOn();
			}
		});

	 ;

	}

	private List<IModel<String>> getHistoryModel() {
		
		List<IModel<String>> models = new ArrayList<>();
		
		// limit the history to the max configured by the user
		int max = getUserSettingsService().getMaxHistory();

		for (String query : getQueryHistoryService().getHistory()) {
			if (max > 0 && models.size() >= max)
				break;
			models.add(Model.of(query));
		}
		
		return models;
	}

	protected QueryHistoryService getQueryHistoryService() {
		return (QueryHistoryService) ServiceLocator.getInstance().getBean(QueryHistoryService.class);
	}

	protected UserSettingsService getUserSettingsService() {
		return (UserSettingsService) ServiceLocator.getInstance().getBean(UserSettingsService.class);
	}

	

	private IModel<String> getTextModel() {
		return new PropertyModel<String>(this, "text");
	}

	
	@SuppressWarnings("unused")
	private void setTextModel(IModel<String> model) {
		this.textField.setModel(model);
	}
	
	
/**	protected void setUpModel() {
		try {
			setModel(new ObjectModel<Candidate>(getCandidateDBService().findWithDeps(getModel().getObject().getId()).get()));
		} catch (Exception e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}

**/
	
	protected void onSave(AjaxRequestTarget target) {

		try {

			//getForm().setFormState(FormState.VIEW);
			getForm().updateReload();

			target.add(this);

			// fire the SearchEvent; DemoHomePage listens to it, executes the
			// search, updates the user's history and refreshes the results
			fireScanAll(new SearchEvent(getText(), target));

			refreshHistoryPanel();

		} catch (Exception e) {
			addOrReplace(new SimpleAlertRow<Void>("error", e));
		}
		target.add(this);
	}

	/** Rebuilds the history panel so it reflects the current query history. */
	public void refreshHistoryPanel() {
		
		ListPanel<String> panel = new ListPanel<String>("history", getHistoryModel()) {

			@Override
			protected String getListGroupItemCss() {
				return "list-group-item  border-0";
			}
		};
		panel.setHasExpander(false);
		panel.setItemMenu(false);
		panel.setSettings(false);
		panel.setToolbarVisible(false);
		historyPanel = panel;
		this.historyContainer.addOrReplace(historyPanel);
	}

	protected void onCancel(AjaxRequestTarget target) {
		getForm().setFormState(FormState.VIEW);
		target.add(getForm());
	}

	protected void onEdit(AjaxRequestTarget target) {
		super.edit(target);
		target.add(this);
	}

}
