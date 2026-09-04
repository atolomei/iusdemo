package io.demo.web.home;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.model.IModel;

import io.demo.Logger;
import io.demo.model.Identifiable;
import wktui.base.ModelPanel;

public class ObjectModelPanel<T> extends ModelPanel<T> {

	private static final long serialVersionUID = 1L;

	static private Logger logger = Logger.getLogger(ObjectModelPanel.class.getName());


	public ObjectModelPanel(String id, IModel<T> model) {
		super(id, model);
	}

	public void onDetach() {
		super.onDetach();

	}

	protected <S extends Identifiable> List<IModel<S>> iFilter(List<IModel<S>> initialList, String filter) {
		List<IModel<S>> list = new ArrayList<IModel<S>>();
		final String str = filter.trim().toLowerCase();
		initialList.forEach(s -> {
			if (s.getObject().getName().toLowerCase().contains(str)) {
				list.add(s);
			}
		});
		return list;
	}

	
	public Locale getLocale() {
		Locale locale = super.getLocale();
		
		if (locale == null) {
			logger.error("locale is null, using default locale -> " + Locale.getDefault().toString());
			locale = Locale.getDefault();	
		}
		return locale;
		
		// WebRequest request = (WebRequest) RequestCycle.get().getRequest();
		// return request.getLocale();
	
	}
 
	 
}
