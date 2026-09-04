package io.demo.web.home;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import io.wktui.editor.Editor;
import io.wktui.form.Form;
import io.wktui.form.FormState;
import io.wktui.form.field.Field;

public class ObjectEditor<T> extends ObjectModelPanel<T> implements Editor<T> {

	private static final long serialVersionUID = 1L;

	static public final List<Boolean> b_list = new ArrayList<Boolean>();

	static {
		b_list.add(Boolean.TRUE);
		b_list.add(Boolean.FALSE);
	}

 	static public final List<Locale> locales = new ArrayList<Locale>();

	static {
		locales.add(Locale.ENGLISH);
		locales.add(Locale.forLanguageTag("es"));
		locales.add(Locale.forLanguageTag("pt-BR"));
	}

	private Form<T> form;
	private boolean readonly = false;
	private List<String> updatedParts = new ArrayList<String>();

	public ObjectEditor(String id, IModel<T> model) {
		super(id, model);
		super.setOutputMarkupId(true);
	}


	@Override
	public boolean isReadOnly() {
		return readonly;
	}


	public void cancel(AjaxRequestTarget target) {

		getForm().setFormState(FormState.VIEW);

		getForm().visitChildren(Field.class, new IVisitor<Field<?>, Void>() {
			@Override
			public void component(Field<?> field, IVisit<Void> visit) {
				field.editOff();
			}
		});
		target.add(this);
	}

	public void edit() {
		getForm().setFormState(FormState.EDIT);
		getForm().visitChildren(Field.class, new IVisitor<Field<?>, Void>() {
			@Override
			public void component(Field<?> field, IVisit<Void> visit) {
				field.editOn();
			}
		});
	}

	public void edit(final AjaxRequestTarget target) {
		getForm().setFormState(FormState.EDIT);
		getForm().visitChildren(Field.class, new IVisitor<Field<?>, Void>() {
			@Override
			public void component(Field<?> field, IVisit<Void> visit) {
				field.editOn();
			}
		});
		target.add(this);
	}

	public void updateModel() {
		getForm().visitChildren(Field.class, new IVisitor<Field<?>, Void>() {
			@Override
			public void component(Field<?> field, IVisit<Void> visit) {
				field.updateModel();
			}
		});

	}

	public void submit(final AjaxRequestTarget target) {
		getForm().setFormState(FormState.VIEW);
		getForm().visitChildren(Field.class, new IVisitor<Field<?>, Void>() {
			@Override
			public void component(Field<?> field, IVisit<Void> visit) {
				field.editOff();
			}
		});
		target.add(this);
	}

	public boolean isEditionEnabled() {
		return getForm().getFormState() == FormState.EDIT;
	}

	public Form<T> getForm() {
		return this.form;
	}

	public void setForm(Form<T> form) {
		this.form = form;
	}

	public void reset() {
		updatedParts.clear();
	}

	public void setReadOnly(boolean re) {
		this.readonly = re;
	}

	public T getModelObject() {
		return getModel().getObject();
	}

	public void setUpdatedPart(String updatedPart) {

		if (updatedPart == null)
			return;

		if (updatedPart.length() == 0)
			return;

		if (!updatedParts.contains(updatedPart))
			updatedParts.add(updatedPart);
	}

	public List<String> getUpdatedParts() {
		return updatedParts;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		if (getModel() != null)
			getModel().detach();

		if (getForm() != null)
			getForm().detach();

		super.onDetach();
	}

	public void update(AjaxRequestTarget target) {
	}

	//public String formatFileSize(long size) {
	//	return NumberFormatter.formatFileSize(size);
	//}


	/** ------------------------ **/

	protected Locale getUserLocale() {
		return Locale.getDefault();
	}


	protected String normalizeFileName(String name) {
		String str = name.replaceAll("[^\\x00-\\x7F]|[\\s]+", "-").toLowerCase().trim();
		str = str.replace("'", "");
		str = str.replace(".", "");
		if (str.length() < 100)
			return str;
		return str.substring(0, 100);
	}

	protected IModel<String> getLabel(String key) {
		return new StringResourceModel(key, this, null);
	}

	protected String getLabelString(String key) {
		return getLabel(key).getObject();
	}

	protected String getLabelString(String key, String... parameter) {
		return getLabel(key, parameter).getObject();
	}

	protected IModel<String> getLabel(String key, String... parameter) {
		StringResourceModel model = new StringResourceModel(key, this, null);
		model.setParameters((Object[]) parameter);
		return model;
	}

}
