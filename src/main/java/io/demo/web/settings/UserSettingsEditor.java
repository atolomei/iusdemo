package io.demo.web.settings;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

import io.demo.Logger;
import io.demo.service.DocumentAnalyzeCacheService;
import io.demo.service.QueryCacheService;
import io.demo.service.ServiceLocator;
import io.demo.service.UserSettingsService;
import io.demo.web.home.ObjectEditor;
import io.wktui.error.AlertPanel;
import io.wktui.error.SimpleAlertRow;
import io.wktui.form.Form;
import io.wktui.form.button.SubmitButton;
import io.wktui.form.field.BooleanField;
import io.wktui.form.field.ChoiceField;
import io.wktui.form.field.Field;
import wktui.base.InvisiblePanel;

/**
 * Editor of the session-scoped {@link UserSettingsService} (wktui form).
 */
public class UserSettingsEditor extends ObjectEditor<UserSettingsService> {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	static private Logger logger = Logger.getLogger(UserSettingsEditor.class.getName());

	static private final List<Integer> MAX_RESULTS_CHOICES = List.of(1, 2, 3, 5, 10, 20, 30);
	static private final List<Integer> MAX_HISTORY_CHOICES = List.of(1, 2, 3, 5, 10, 20);
	static private final List<Integer> MAX_QUOTES_CHOICES = List.of(1, 2, 3, 4, 5);

	private BooleanField useQueryCacheField;
	private ChoiceField<Integer> maxSearchResultsField;

	private BooleanField recentFirstField;
	private ChoiceField<Integer> maxHistoryField;

	private BooleanField showAnalysisField;
	private BooleanField showQuotesField;
	private ChoiceField<Integer> maxQuotesField;

	public UserSettingsEditor(String id) {
		super(id, new LoadableDetachableModel<UserSettingsService>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected UserSettingsService load() {
				return (UserSettingsService) ServiceLocator.getInstance().getBean(UserSettingsService.class);
			}
		});
		this.setOutputMarkupId(true);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		add(new InvisiblePanel("error"));
		add(new InvisiblePanel("success"));

		Form<UserSettingsService> form = new Form<UserSettingsService>("settingsForm", getModel());
		form.setOutputMarkupId(true);
		add(form);
		setForm(form);

		useQueryCacheField = new BooleanField("useQueryCache", new PropertyModel<Boolean>(getModel(), "useQueryCache"), getLabel("useQueryCache"));

		maxSearchResultsField = new ChoiceField<Integer>("maxSearchResults", new PropertyModel<Integer>(getModel(), "maxSearchResults"), getLabel("maxSearchResults")) {
			private static final long serialVersionUID = 1L;

			@Override
			public IModel<List<Integer>> getChoices() {
				return new ListModel<Integer>(new ArrayList<Integer>(MAX_RESULTS_CHOICES));
			}
		};

		recentFirstField = new BooleanField("recentFirst", new PropertyModel<Boolean>(getModel(), "recentFirst"), getLabel("recentFirst"));

		maxHistoryField = new ChoiceField<Integer>("maxHistory", new PropertyModel<Integer>(getModel(), "maxHistory"), getLabel("maxHistory")) {
			private static final long serialVersionUID = 1L;

			@Override
			public IModel<List<Integer>> getChoices() {
				return new ListModel<Integer>(new ArrayList<Integer>(MAX_HISTORY_CHOICES));
			}
		};

		showAnalysisField = new BooleanField("showAnalysis", new PropertyModel<Boolean>(getModel(), "showAnalysis"), getLabel("showAnalysis"));
		showQuotesField = new BooleanField("showQuotes", new PropertyModel<Boolean>(getModel(), "showQuotes"), getLabel("showQuotes"));

		maxQuotesField = new ChoiceField<Integer>("maxQuotes", new PropertyModel<Integer>(getModel(), "maxQuotes"), getLabel("maxQuotes")) {
			private static final long serialVersionUID = 1L;

			@Override
			public IModel<List<Integer>> getChoices() {
				return new ListModel<Integer>(new ArrayList<Integer>(MAX_QUOTES_CHOICES));
			}
		};

		getForm().add(useQueryCacheField);
		getForm().add(maxSearchResultsField);
		getForm().add(recentFirstField);
		getForm().add(maxHistoryField);
		getForm().add(showAnalysisField);
		getForm().add(showQuotesField);
		getForm().add(maxQuotesField);

		SubmitButton<UserSettingsService> save = new SubmitButton<UserSettingsService>("save", getModel(), getForm()) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(AjaxRequestTarget target) {
				UserSettingsEditor.this.onSave(target);
			}

			public IModel<String> getLabel() {
				return UserSettingsEditor.this.getLabel("save");
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

		getForm().add(save);

		// button to clean up the query and document analysis caches
		AjaxLink<Void> cleanCaches = new AjaxLink<Void>("cleanCaches") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				UserSettingsEditor.this.onCleanCaches(target);
			}
		};
		cleanCaches.add(new Label("cleanCachesLabel", getLabel("cleanCaches")));
		add(cleanCaches);

		// the settings form is always editable
		edit();
		form.updateModel();
		form.updateReload();
		form.visitChildren(Field.class, new IVisitor<Field<?>, Void>() {
			@Override
			public void component(Field<?> field, IVisit<Void> visit) {
				field.editOn();
			}
		});
	}

	protected void onSave(AjaxRequestTarget target) {
		try {
			// the fields update the session-scoped UserSettingsService directly
			getForm().updateReload();
			addOrReplace(new AlertPanel<Void>("success", AlertPanel.SUCCESS, getLabel("saved-ok")));
		} catch (Exception e) {
			addOrReplace(new SimpleAlertRow<Void>("error", e));
		}
		target.add(this);
	}

	/** Empties the query and document analysis caches (memory and disk). */
	protected void onCleanCaches(AjaxRequestTarget target) {
		try {
			getQueryCacheService().cleanUp();
			getDocumentAnalyzeCacheService().cleanUp();
			addOrReplace(new AlertPanel<Void>("success", AlertPanel.SUCCESS, getLabel("caches-cleaned-ok")));
		} catch (Exception e) {
			addOrReplace(new SimpleAlertRow<Void>("error", e));
		}
		target.add(this);
	}

	protected QueryCacheService getQueryCacheService() {
		return (QueryCacheService) ServiceLocator.getInstance().getBean(QueryCacheService.class);
	}

	protected DocumentAnalyzeCacheService getDocumentAnalyzeCacheService() {
		return (DocumentAnalyzeCacheService) ServiceLocator.getInstance().getBean(DocumentAnalyzeCacheService.class);
	}
}
