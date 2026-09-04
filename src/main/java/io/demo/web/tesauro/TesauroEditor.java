package io.demo.web.tesauro;

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
import io.demo.Logger;
import io.demo.web.home.ObjectEditor;
import io.wktui.editor.ObjectUpdateEvent;
import io.wktui.error.AlertPanel;
import io.wktui.error.SimpleAlertRow;
import io.wktui.event.MenuAjaxEvent;
import wktui.base.InvisiblePanel;

public class TesauroEditor extends ObjectEditor<String>   {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	static private Logger logger = Logger.getLogger(TesauroEditor.class.getName());

	private TextField<String> textField;
	private boolean submitted = false;

	private String text;
	
	
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public TesauroEditor(String id) {
		this(id, Model.of(new String()));
	}

	public TesauroEditor(String id, IModel<String> model) {
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

		add(new InvisiblePanel("error"));
		add(new InvisiblePanel("success"));

		Form<String> form = new Form<String>("searchForm", getModel());

		form.setOutputMarkupId(true);
		add(form);
		setForm(form);

		textField = new TextField<String>("text", getTextModel(), getLabel("text"));
		
		getForm().add(textField);

		 

		SubmitButton<String> sm = new SubmitButton<String>("send", getModel(), getForm()) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(AjaxRequestTarget target) {
				TesauroEditor.this.onSave(target);
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
				return "btn btn-primary btn-lg";
			}
		};

		getForm().add(sm);
		
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

			//getCandidateDBService().save(getModelObject(), String.join(", ", getUpdatedParts()), getRootUser());

			
			getForm().setFormState(FormState.VIEW);
			getForm().updateReload();

			addOrReplace(new AlertPanel<Void>("success", AlertPanel.SUCCESS, getLabel("submitted-ok")));
			target.add(this);


			fireScanAll(new ObjectUpdateEvent(target));

		} catch (Exception e) {
			addOrReplace(new SimpleAlertRow<Void>("error", e));
		}
		target.add(this);
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
