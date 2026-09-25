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
import org.apache.wicket.request.mapper.parameter.PageParameters;
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
import io.demo.model.db.service.QueryDBService;
import io.demo.results.DateRange;
import io.demo.results.ReasoningEffortOption;
import io.demo.results.SubjectOption;
import io.demo.service.QueryHistoryService;
import io.demo.service.QueryLogService;
import io.demo.service.ServiceLocator;
import io.demo.service.UserSettingsService;
import io.demo.service.rag.KbeeRAGClient;
import io.demo.web.event.SearchEvent;
import io.wktui.editor.ObjectUpdateEvent;
import io.wktui.error.AlertPanel;
import io.wktui.error.SimpleAlertRow;
import io.wktui.event.MenuAjaxEvent;
import wktui.base.InvisiblePanel;

public class SearchFormEditor extends ObjectEditor<String> {

	private static final long serialVersionUID = 1L;

	static private Logger logger = Logger.getLogger(SearchFormEditor.class.getName());

	private TextAreaField<String> textField;

	private boolean submitted = false;

	private String text;

	private ListPanel<io.demo.model.Query> historyPanel;

	/** Container of the history section, collapsed by default. */
	private WebMarkupContainer historyContainer;

	/** Whether the history section is expanded. Collapsed by default. */
	private boolean historyVisible = false;

	/** toolbar selections passed to the ResultsPanel with the SearchEvent */
	private DateRange dateRange = DateRange.getDefault();
	private SubjectOption subjectOption = SubjectOption.getDefault();
	private ReasoningEffortOption reasoningEffortOption = ReasoningEffortOption.getDefault();


	
	public SearchFormEditor(String id) {
		this(id, Model.of(new String()));
	}

	public SearchFormEditor(String id, IModel<String> model) {
		super(id, model);
		this.setOutputMarkupId(true);
	}
	
	
	
	public DateRange getDateRange() {
		return dateRange;
	}

	public void setDateRange(DateRange dateRange) {
		this.dateRange = dateRange;
	}

	public SubjectOption getSubjectOption() {
		return subjectOption;
	}

	public void setSubjectOption(SubjectOption subjectOption) {
		this.subjectOption = subjectOption;
	}

	public ReasoningEffortOption getReasoningEffortOption() {
		return reasoningEffortOption;
	}

	public void setReasoningEffortOption(ReasoningEffortOption reasoningEffortOption) {
		this.reasoningEffortOption = reasoningEffortOption;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}


	@Override
	public void onDetach() {
		super.onDetach();

	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		// setUpModel();

		this.historyContainer = new WebMarkupContainer("historyContainer") {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return historyVisible;
			}
		};
		this.historyContainer.setOutputMarkupPlaceholderTag(true);
		add(this.historyContainer);

		historyPanel = new ListPanel<io.demo.model.Query>("history", getHistoryModel()) {

			@Override
			public IModel<String> getItemLabel(IModel<io.demo.model.Query> model) {
				return Model.of(model.getObject().getQuery());
			}

			@Override
			public void onClick(IModel<io.demo.model.Query> model) {
				SearchFormEditor.this.onSearchHistory(model);
			}

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

		// --- toolbar: Período / Materia / Máx (above the "Consulta" field) ---

		org.apache.wicket.markup.html.form.DropDownChoice<DateRange> dates = new org.apache.wicket.markup.html.form.DropDownChoice<DateRange>("daterange", new PropertyModel<DateRange>(this, "dateRange"),
				java.util.Arrays.asList(DateRange.values()), new org.apache.wicket.markup.html.form.ChoiceRenderer<DateRange>("label"));
		getForm().add(dates);

		org.apache.wicket.markup.html.form.DropDownChoice<SubjectOption> subject = new org.apache.wicket.markup.html.form.DropDownChoice<SubjectOption>("subject", new PropertyModel<SubjectOption>(this, "subjectOption"),
				java.util.Arrays.asList(SubjectOption.values()), new org.apache.wicket.markup.html.form.ChoiceRenderer<SubjectOption>("label"));
		getForm().add(subject);

		org.apache.wicket.markup.html.form.DropDownChoice<ReasoningEffortOption> effort = new org.apache.wicket.markup.html.form.DropDownChoice<ReasoningEffortOption>("reasoningeffort", new PropertyModel<ReasoningEffortOption>(this, "reasoningEffortOption"),
				java.util.Arrays.asList(ReasoningEffortOption.values()), new org.apache.wicket.markup.html.form.ChoiceRenderer<ReasoningEffortOption>("label"));
		getForm().add(effort);

		// --- alert displayed at the bottom when the RAG server is not accessible ---
		add(new AlertPanel<Void>("ragAlert", AlertPanel.DANGER, getLabel("rag-not-available")) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return !getRAGClient().isAvailable();
			}
		});

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

	private List<IModel<io.demo.model.Query>> getHistoryModel() {

		List<IModel<io.demo.model.Query>> models = new ArrayList<>();

		// limit the history to the max configured by the user
		int max = getUserSettingsService().getMaxHistory();

		// use detachable models keyed by id so the JPA entities (with their
		// results JSON and lazy proxies) are never serialized into the session
		getQueryDBService().getRecent(max).forEach(query -> {
			models.add(new QueryModel(query));
		});

		return models;
	}

	/**
	 * LoadableDetachableModel for a {@link io.demo.model.Query}: only the id is
	 * kept across requests; the entity is reloaded from the database on demand
	 * and released by Wicket's detach mechanism at the end of the request.
	 */
	private static class QueryModel extends org.apache.wicket.model.LoadableDetachableModel<io.demo.model.Query> {

		private static final long serialVersionUID = 1L;

		private final Long id;

		QueryModel(io.demo.model.Query query) {
			super(query);
			this.id = query.getId();
		}

		@Override
		protected io.demo.model.Query load() {
			QueryDBService service = (QueryDBService) ServiceLocator.getInstance().getBean(QueryDBService.class);
			return service.findById(id).orElse(null);
		}
	}

	private IModel<String> getTextModel() {
		return new PropertyModel<String>(this, "text");
	}

	protected KbeeRAGClient getRAGClient() {
		return (KbeeRAGClient) ServiceLocator.getInstance().getBean(KbeeRAGClient.class);
	}

	@SuppressWarnings("unused")
	private void setTextModel(IModel<String> model) {
		this.textField.setModel(model);
	}

	/**
	 * protected void setUpModel() { try { setModel(new
	 * ObjectModel<Candidate>(getCandidateDBService().findWithDeps(getModel().getObject().getId()).get()));
	 * } catch (Exception e) { logger.error(e); throw new RuntimeException(e); } }
	 * 
	 **/

	protected void onSave(AjaxRequestTarget target) {

		try {

			// getForm().setFormState(FormState.VIEW);
			getForm().updateReload();

			target.add(this);

			if (getText() == null || getText().trim().isEmpty()) {
				addOrReplace(new SimpleAlertRow<String>("error", Model.of("Debe ingresar un texto para buscar")));
				target.add(this);
				return;
			}

			// fire the SearchEvent; DemoHomePage listens to it, executes the
			// search, updates the user's history and refreshes the results
			fireScanAll(new SearchEvent(getText(), getDateRange(), getSubjectOption(), getReasoningEffortOption(), target));

			refreshHistoryPanel();

		} catch (Exception e) {
			logger.error(e);
			addOrReplace(new SimpleAlertRow<Void>("error", e));
		}
		target.add(this);
	}

	/** Rebuilds the history panel so it reflects the current query history. */
	public void refreshHistoryPanel() {

		ListPanel<io.demo.model.Query> panel = new ListPanel<io.demo.model.Query>("history", getHistoryModel()) {

			@Override
			public IModel<String> getItemLabel(IModel<io.demo.model.Query> model) {
				return Model.of(model.getObject().getQuery());
			}

			@Override
			public void onClick(IModel<io.demo.model.Query> model) {
				SearchFormEditor.this.onSearchHistory(model);
			}

			//@Override
			//protected String getListGroupItemCss() {
			//	return "list-group-item  border-0";
			//}
		};
		
		
		panel.setBorder(true);
		panel.setHasExpander(false);
		panel.setItemMenu(false);
		panel.setSettings(false);
		panel.setToolbarVisible(false);
		historyPanel = panel;
		this.historyContainer.addOrReplace(historyPanel);
	}

	protected void onSearchHistory(IModel<io.demo.model.Query> model) {

		io.demo.model.Query logged = model.getObject();

		PageParameters params = new PageParameters();
		params.add("query", logged.getQuery());

		// pass the toolbar filters saved with the query so DemoHomePage can
		// restore the selectors and re-run the search with the same options
		params.add("dateRangeOption", String.valueOf(logged.getDateRangeOption()));
		params.add("subjectOption", String.valueOf(logged.getSubjectOption()));
		params.add("reasoningEffortOption", String.valueOf(logged.getReasoningEffortOption()));

		
		logger.debug(model.getObject().toString());;
		
		setResponsePage(new DemoHomePage(params));

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
