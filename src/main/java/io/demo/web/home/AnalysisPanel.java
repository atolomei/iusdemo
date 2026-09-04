package io.demo.web.home;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import io.demo.Logger;
import io.demo.model.Documento;
import io.demo.service.LegalSearchService;
import io.demo.service.ServiceLocator;
import io.demo.service.UserSettingsService;
import io.demo.service.rag.DocumentAnalysisResponse;
import io.demo.service.rag.RAGDocumentAnalysisConverter;
import wktui.base.InvisiblePanel;
import wktui.base.ModelPanel;

/**
 * Expanded panel of a search result: displays the Analysis, the
 * jurisprudential citations (fallos), the normative citations (normas) and the
 * Quotes of the {@link Documento}, according to the user's preferences
 * ({@link UserSettingsService}).
 */
public class AnalysisPanel<T extends Documento> extends ModelPanel<T> {

	private static final long serialVersionUID = 1L;

	static private Logger logger = Logger.getLogger(AnalysisPanel.class.getName());
	

	
	private static final String NO_INFO = "sin información";

	private WebMarkupContainer analysisContainer;
	private WebMarkupContainer fallosContainer;
	private WebMarkupContainer normasContainer;

	/** the query used to analyze the document (RAG document analysis) */
	private String query;

	public AnalysisPanel(String id, IModel<T> model) {
		this(id, model, null);
	}

	public AnalysisPanel(String id, IModel<T> model, String query) {
		super(id, model);
		this.query = query;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		// --- Analysis ---------------------------------------------------

		// visible only if the user enabled "show analysis"
		analysisContainer = new WebMarkupContainer("analysisContainer") {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return getUserSettingsService().isShowAnalysis();
			}
		};
		analysisContainer.setOutputMarkupPlaceholderTag(true);
		add(analysisContainer);

		
		try {
			Label analysis = new Label("analysis", getAnalysisModel()) {
				private static final long serialVersionUID = 1L;
	
				@Override
				public boolean isVisible() {
					return hasAnalysis();
				}
			};
			analysis.setEscapeModelStrings(false);
			analysisContainer.add(analysis);
		} catch (Exception e) {
			logger.error(e);
			Label error = new Label("analysis", "Error al obtener el análisis del documento: " + e.getMessage());
			error.setEscapeModelStrings(false);
			analysisContainer.add(error);
		}
		
		
		
		Label noInfoAnalysis = new Label("noInfoAnalysis", NO_INFO) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return !hasAnalysis();
			}
		};
		analysisContainer.add(noInfoAnalysis);

		// --- Citas jurisprudenciales (fallos) ----------------------------

		fallosContainer = addQuoteSection("fallosContainer", "fallos", "fallo-text", "noFallosQuotes", getFallosModel());

		// --- Citas normativas (normas) ------------------------------------

		normasContainer = addQuoteSection("normasContainer", "normas", "norma-text", "noNormasQuotes", getNormasModel());

		add(new InvisiblePanel("error"));
	}

	/**
	 * Builds a citation section: an outer container (visible only if the user
	 * enabled "show quotes"), a &lt;ul&gt; container with a ListView of items
	 * ("quote" is the repeating &lt;li&gt;) and a "no info" label shown when
	 * the list is empty.
	 */
	protected WebMarkupContainer addQuoteSection(String containerId, String listId, String textId, String noInfoId, final IModel<List<String>> listModel) {

		WebMarkupContainer container = new WebMarkupContainer(containerId) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return getUserSettingsService().isShowAnalysis();
			}
		};
		container.setOutputMarkupPlaceholderTag(true);
		add(container);

		// the <ul> is a container, the <li> is the repeating element
		WebMarkupContainer listContainer = new WebMarkupContainer(listId) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return !listModel.getObject().isEmpty();
			}
		};
		container.add(listContainer);

		listContainer.add(new ListView<String>("quote", listModel) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<String> item) {
				Label label = new Label(textId, item.getModel());
				label.setEscapeModelStrings(false);
				item.add(label);
			}
		});

		Label noInfo = new Label(noInfoId, NO_INFO) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return listModel.getObject().isEmpty();
			}
		};
		container.add(noInfo);

		return container;
	}

	/**
	 * Converter of the RAG document analysis response, shared by the analysis
	 * label and the citation ListViews. Detachable so it is not serialized.
	 */
	private final IModel<RAGDocumentAnalysisConverter> converterModel = new LoadableDetachableModel<RAGDocumentAnalysisConverter>() {
		private static final long serialVersionUID = 1L;

		@Override
		protected RAGDocumentAnalysisConverter load() {
			T documento = AnalysisPanel.this.getModel().getObject();
			String ragDocumentId = documento.getRagDocumentId();

			if (ragDocumentId != null && !ragDocumentId.isBlank() && query != null && !query.isBlank()) {
				DocumentAnalysisResponse response = getLegalSearchService().analyzeDocumentResponse(ragDocumentId, query);
				return new RAGDocumentAnalysisConverter(response);
			}
			return new RAGDocumentAnalysisConverter((String) null);
		}
	};

	/**
	 * Analysis of the document: if the document has a RAG document id and a query
	 * was provided, the answer is obtained from
	 * {@link LegalSearchService#analyzeDocumentResponse(String, String)} (cached)
	 * and parsed by a {@link RAGDocumentAnalysisConverter}.
	 */
	protected IModel<String> getAnalysisModel() {
		return new LoadableDetachableModel<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected String load() {
				return converterModel.getObject().getAnalysis();
			}
		};
	}

	/** Jurisprudential citations parsed from the RAG document analysis. */
	protected IModel<List<String>> getFallosModel() {
		return new LoadableDetachableModel<List<String>>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected List<String> load() {
				return limit(converterModel.getObject().getCitasJurisprudencia());
			}
		};
	}

	/** Normative citations parsed from the RAG document analysis. */
	protected IModel<List<String>> getNormasModel() {
		return new LoadableDetachableModel<List<String>>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected List<String> load() {
				return limit(converterModel.getObject().getCitasNormas());
			}
		};
	}

	/** Limits the list to the user's maxQuotes. */
	protected List<String> limit(List<String> quotes) {
		if (quotes == null)
			return new ArrayList<String>();
		int max = Math.max(0, getUserSettingsService().getMaxQuotes());
		List<String> result = new ArrayList<String>();
		quotes.stream().limit(max).forEach(result::add);
		return result;
	}

	protected boolean hasAnalysis() {
		String analysis = converterModel.getObject().getAnalysis();
		return analysis != null && !analysis.isBlank();
	}

	@Override
	public void onDetach() {
		converterModel.detach();
		super.onDetach();
	}

	protected LegalSearchService getLegalSearchService() {
		return (LegalSearchService) ServiceLocator.getInstance().getBean(LegalSearchService.class);
	}

	protected UserSettingsService getUserSettingsService() {
		return (UserSettingsService) ServiceLocator.getInstance().getBean(UserSettingsService.class);
	}
}