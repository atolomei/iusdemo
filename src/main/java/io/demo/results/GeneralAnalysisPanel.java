package io.demo.results;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import io.demo.model.Query;
import wktui.base.BasePanel;

/**
 * Panel displaying the general analysis of the query (see
 * {@code LegalSearchService.queryAnalysis}): a title, the analysis text
 * received from the server, the LLM used, and a link to close the panel.
 */
public class GeneralAnalysisPanel extends BasePanel {

	private static final long serialVersionUID = 1L;

	private IModel<Query> queryModel;

	public GeneralAnalysisPanel(String id, IModel<Query> queryModel) {
		super(id);
		this.queryModel = queryModel;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		add(new Label("title", "Análisis General"));

		add(new Label("analysis", new Model<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getObject() {
				Query query = queryModel.getObject();
				return (query != null && query.getAnalysisText() != null) ? query.getAnalysisText() : "";
			}
		}));

		add(new Label("llm", new Model<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getObject() {
				Query query = queryModel.getObject();
				return (query != null && query.getAnalysisLlm() != null) ? query.getAnalysisLlm() : "";
			}
		}));

		add(new AjaxLink<Void>("close") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				GeneralAnalysisPanel.this.onClose(target);
			}
		});
	}

	/** called when the user clicks the "cerrar" link; overridden by the container */
	protected void onClose(AjaxRequestTarget target) {
	}

	@Override
	public void onDetach() {
		super.onDetach();
		if (queryModel != null)
			queryModel.detach();
	}
}
