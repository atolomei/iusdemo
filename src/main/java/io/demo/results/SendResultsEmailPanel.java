package io.demo.results;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.EmailTextField;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.validator.EmailAddressValidator;

import io.demo.Logger;
import io.demo.email.EmailService;
import io.demo.service.ServiceLocator;
import wktui.base.BasePanel;

/**
 * Panel opened from the {@link ToolbarResults} "Enviar por email" button
 * (same pattern as {@link GeneralAnalysisPanel}): a form with a static
 * "Enviado por" field (the session user), an {@link EmailTextField} for the
 * recipient's email address, an optional "Comentarios" {@link TextArea} and
 * a button to send the query and the list of results (built by
 * {@link ResultsPanel}) using the {@link EmailService}. The first (most
 * relevant) result also includes the "\u00bfPor qu\u00e9 es relevante?" analysis
 * text. On success a "el mail fue enviado" message is shown together with a
 * button to close the panel.
 */
public class SendResultsEmailPanel extends BasePanel {

	private static final long serialVersionUID = 1L;

	static private Logger logger = Logger.getLogger(SendResultsEmailPanel.class.getName());

	private enum State {
		FORM, SENT
	}

	private State state = State.FORM;

	/** bound to the "to" TextField */
	private String to;

	/** bound to the "comments" TextArea (optional, included in the email) */
	private String comments;

	private String error;

	// --- email content, supplied by ResultsPanel ----------------------
	private final String query;
	private final String dateRangeLabel;
	private final String subjectLabel;
	private final String reasoningLevelLabel;
	private final String orderLabel;
	/** display name of the session user who sends the email (not editable) */
	private final String sentBy;
	private final List<EmailResultItem> items;

	private WebMarkupContainer container;
	private WebMarkupContainer formContainer;
	private WebMarkupContainer successContainer;
	private Label errorLabel;

	public SendResultsEmailPanel(String id, String query, String dateRangeLabel, String subjectLabel, String reasoningLevelLabel, String orderLabel, String sentBy, List<EmailResultItem> items) {
		super(id);
		this.query = query;
		this.dateRangeLabel = dateRangeLabel;
		this.subjectLabel = subjectLabel;
		this.reasoningLevelLabel = reasoningLevelLabel;
		this.orderLabel = orderLabel;
		this.sentBy = sentBy;
		this.items = items;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		container = new WebMarkupContainer("sendEmailContainer");
		container.setOutputMarkupId(true);
		add(container);

		// --- form ---------------------------------------------------------
		formContainer = new WebMarkupContainer("sendEmailFormContainer") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(state == State.FORM);
			}
		};
		formContainer.setOutputMarkupPlaceholderTag(true);
		container.add(formContainer);

		Form<Void> form = new Form<Void>("sendEmailForm");
		formContainer.add(form);

		// "Enviado por": static, not editable
		//form.add(new Label("sentBy", Model.of(sentBy != null ? sentBy : "")));

		EmailTextField toField = new EmailTextField("to", new PropertyModel<String>(this, "to"));
		toField.setRequired(true);
		toField.add(EmailAddressValidator.getInstance());
		form.add(toField);

		// "Comentarios": editable, optional
		TextArea<String> commentsField = new TextArea<String>("comments", new PropertyModel<String>(this, "comments"));
		commentsField.setRequired(false);
		form.add(commentsField);

		form.add(new AjaxButton("send") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				onSend(target);
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
				SendResultsEmailPanel.this.onClose(target);
			}
		});

		errorLabel = new Label("error", new PropertyModel<String>(this, "error")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(error != null && !error.isEmpty());
			}
		};
		errorLabel.setOutputMarkupPlaceholderTag(true);
		formContainer.add(errorLabel);

		// --- success -------------------------------------------------------
		successContainer = new WebMarkupContainer("sendEmailSuccessContainer") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(state == State.SENT);
			}
		};
		successContainer.setOutputMarkupPlaceholderTag(true);
		successContainer.add(new Label("message", "El mail fue enviado"));
		successContainer.add(new AjaxLink<Void>("close") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				SendResultsEmailPanel.this.onClose(target);
			}
		});
		container.add(successContainer);
	}

	protected void onSend(AjaxRequestTarget target) {
		try {
			String subject = "KBEE. " + (query != null ? query : "");

			if (subject.length()>72)
				subject=subject.substring(0,72)+"...";
					
			getEmailService().sendHTML(to, subject, buildBody());

			state = State.SENT;
			error = null;

		} catch (Exception e) {
			logger.error(e);
			error = "Error al enviar el mail: " + e.getMessage();
		}
		target.add(container);
	}

	/** Builds the HTML body: query/filters header and the list of results. */
	protected String buildBody() {

		StringBuilder sb = new StringBuilder();

		sb.append("<div style=\"font-family: Arial, Helvetica, sans-serif; font-size: 14px;\">");

		if (sentBy != null && !sentBy.isBlank())
			sb.append("<p><b>Enviado por:</b> ").append(nullToEmpty(sentBy)).append("</p>");

		sb.append("<p><b>Consulta:</b> ").append(nullToEmpty(query)).append("</p>");
		sb.append("<p><b>Per\u00edodo:</b> ").append(nullToEmpty(dateRangeLabel)).append("</p>");
		sb.append("<p><b>Materia:</b> ").append(nullToEmpty(subjectLabel)).append("</p>");
		sb.append("<p><b>Nivel de an\u00e1lisis:</b> ").append(nullToEmpty(reasoningLevelLabel)).append("</p>");
		sb.append("<p><b>Ordenar por:</b> ").append(nullToEmpty(orderLabel)).append("</p>");

		if (comments != null && !comments.isBlank())
			sb.append("<p><b>Comentarios:</b> ").append(nullToEmpty(comments).replace("\n", "<br/>")).append("</p>");

		sb.append("<hr/>");

		sb.append("<ol style=\"padding-left: 20px;\">");
		if (items != null) {
			for (EmailResultItem item : items) {
				sb.append("<li style=\"margin-bottom: 14px;\">");

				if (item.getLink() != null && !item.getLink().isEmpty())
					sb.append("<a href=\"").append(item.getLink()).append("\">").append(nullToEmpty(item.getTitle())).append("</a>");
				else
					sb.append(nullToEmpty(item.getTitle()));

				if (item.getSubtitle() != null && !item.getSubtitle().isEmpty())
					sb.append("<br/><span style=\"font-size: 0.9em; color: #555;\">").append(item.getSubtitle()).append("</span>");

			/**	if (item.getWhyRelevant() != null && !item.getWhyRelevant().isEmpty()) {
					sb.append("<div style=\"margin-top: 8px;\">");
					sb.append("<b>\u00bfPor qu\u00e9 es relevante?</b>");
					sb.append("<div style=\"font-size: 0.9em;\">").append(item.getWhyRelevant()).append("</div>");
					sb.append("</div>");
				}
				**/

				sb.append("</li>");
			}
		}
		sb.append("</ol>");

		sb.append("</div>");

		return sb.toString();
	}

	private String nullToEmpty(String s) {
		return (s != null) ? s : "";
	}

	protected EmailService getEmailService() {
		return (EmailService) ServiceLocator.getInstance().getBean(EmailService.class);
	}

	/** called when the user cancels or closes the panel after sending; overridden by the container */
	protected void onClose(AjaxRequestTarget target) {
	}
}
