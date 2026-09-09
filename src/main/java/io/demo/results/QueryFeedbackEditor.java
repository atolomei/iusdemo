package io.demo.results;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import io.demo.Logger;
import io.demo.model.Query;
import io.demo.model.QueryFeedback;
import io.demo.model.QueryFeedbackGrade;
import io.demo.model.User;
import io.demo.model.db.service.QueryDBService;
import io.demo.model.db.service.QueryFeedbackDBService;
import io.demo.service.ServiceLocator;
import io.demo.web.page.BasePage;
import wktui.base.BasePanel;

/**
 * Editor for the user to evaluate a query ({@link QueryFeedback}): an Ajax
 * link "Evaluar consulta" opens a small form where the user selects a
 * {@link QueryFeedbackGrade} and optionally enters a comment. On save the
 * feedback is stored in the database and a thank-you alert is displayed.
 */
public class QueryFeedbackEditor extends BasePanel {

	private static final long serialVersionUID = 1L;

	static private Logger logger = Logger.getLogger(QueryFeedbackEditor.class.getName());

	static private final String THANKS_MESSAGE = "Su evaluación ha sido enviada. Muchas gracias por ayudarnos a mejorar el buscador.";

	private enum State {
		CLOSED, OPEN, SAVED
	}

	private State state = State.CLOSED;

	/** the query text whose results the user is evaluating */
	private final String query;

	private QueryFeedbackGrade grade;
	private String info;

	private WebMarkupContainer container;
	private WebMarkupContainer lookupContainer;
	private WebMarkupContainer formContainer;
	private WebMarkupContainer successContainer;
	private Label errorLabel;

	public QueryFeedbackEditor(String id, String query) {
		super(id);
		this.query = query;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		container = new WebMarkupContainer("feedbackContainer");
		container.setOutputMarkupId(true);
		add(container);

		// --- link "Evaluar consulta" ------------------------------------
		lookupContainer = new WebMarkupContainer("feedbackLookupContainer") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(state == State.CLOSED);
			}
		};
		lookupContainer.setOutputMarkupPlaceholderTag(true);
		container.add(lookupContainer);

		lookupContainer.add(new AjaxLink<Void>("evaluate") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				state = State.OPEN;
				target.add(container);
			}
		});

		// --- form -------------------------------------------------------
		formContainer = new WebMarkupContainer("feedbackFormContainer") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(state == State.OPEN);
			}
		};
		formContainer.setOutputMarkupPlaceholderTag(true);
		container.add(formContainer);

		Form<Void> form = new Form<Void>("feedbackForm");
		formContainer.add(form);

		List<QueryFeedbackGrade> grades = Arrays.asList(QueryFeedbackGrade.values());

		DropDownChoice<QueryFeedbackGrade> gradeChoice = new DropDownChoice<QueryFeedbackGrade>("grade",
				new PropertyModel<QueryFeedbackGrade>(this, "grade"), grades, new IChoiceRenderer<QueryFeedbackGrade>() {
					private static final long serialVersionUID = 1L;

					@Override
					public Object getDisplayValue(QueryFeedbackGrade object) {
						return object.getDisplayName();
					}

					@Override
					public String getIdValue(QueryFeedbackGrade object, int index) {
						return object.name();
					}

					@Override
					public QueryFeedbackGrade getObject(String id, IModel<? extends List<? extends QueryFeedbackGrade>> choices) {
						return QueryFeedbackGrade.valueOf(id);
					}
				});
		gradeChoice.setRequired(true);
		form.add(gradeChoice);

		TextArea<String> infoArea = new TextArea<String>("info", new PropertyModel<String>(this, "info"));
		form.add(infoArea);

		form.add(new AjaxButton("save") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				onSave(target);
			}

			@Override
			protected void onError(AjaxRequestTarget target) {
				target.add(container);
			}
		});

		form.add(new AjaxLink<Void>("cancel") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				state = State.CLOSED;
				target.add(container);
			}
		});

		errorLabel = new Label("error", Model.of("")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getDefaultModelObject() != null && !getDefaultModelObject().toString().isEmpty());
			}
		};
		errorLabel.setOutputMarkupPlaceholderTag(true);
		formContainer.add(errorLabel);

		// --- success alert ------------------------------------------------
		successContainer = new WebMarkupContainer("feedbackSuccessContainer") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(state == State.SAVED);
			}
		};
		successContainer.setOutputMarkupPlaceholderTag(true);
		successContainer.add(new Label("message", THANKS_MESSAGE));
		container.add(successContainer);
	}

	protected void onSave(AjaxRequestTarget target) {
		try {
			QueryFeedback feedback = new QueryFeedback();

			Query queryEntity = getQueryDBService().getMostRecentByText(query);
			if (queryEntity == null)
				throw new IllegalStateException("no se encontró la consulta -> " + query);

			User user = getSessionUser();
			if (user == null)
				throw new IllegalStateException("no hay un usuario en la sesión");

			feedback.setQuery(queryEntity);
			feedback.setGradeEnum(grade);
			feedback.setInfo(info);
			feedback.setCreated(OffsetDateTime.now());
			feedback.setLastModified(OffsetDateTime.now());
			feedback.setLastModifiedUser(user);

			getQueryFeedbackDBService().save(feedback, user);

			state = State.SAVED;
			errorLabel.setDefaultModelObject("");

		} catch (Exception e) {
			logger.error(e);
			errorLabel.setDefaultModelObject("Error al guardar la evaluación: " + e.getMessage());
		}
		target.add(container);
	}

	protected User getSessionUser() {
		if (getPage() instanceof BasePage)
			return ((BasePage) getPage()).getSessionUser().orElse(null);
		return null;
	}

	protected QueryDBService getQueryDBService() {
		return (QueryDBService) ServiceLocator.getInstance().getBean(QueryDBService.class);
	}

	protected QueryFeedbackDBService getQueryFeedbackDBService() {
		return (QueryFeedbackDBService) ServiceLocator.getInstance().getBean(QueryFeedbackDBService.class);
	}
}
