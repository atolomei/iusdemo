package io.demo.web.home;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import io.demo.Logger;
import io.demo.model.PJSFSentencia;
import io.demo.model.PJSFSumario;
import io.demo.model.Query;
import io.demo.model.RAGDocumento;
import io.demo.model.User;
import io.demo.service.LegalSearchService;
import io.demo.service.PJSFSentenciaService;
import io.demo.service.ServiceLocator;
import io.demo.service.Settings;
import io.demo.service.UserSettingsService;
import io.demo.service.rag.DocumentAnalysisResponse;
import io.demo.service.rag.RAGDocumentAnalysisConverter;
import io.demo.util.MarkdownUtil;
import wktui.base.InvisiblePanel;
import wktui.base.ModelPanel;

/**
 * Expanded panel of a search result. It has three parts:
 * <ol>
 * <li>The {@link PJSFSentencia}: tribunal and fecha as subtitle, and the
 * sumarios (title with the voces + text).</li>
 * <li>An AjaxLink "Citas" that shows the citas (jurisprudenciales and
 * normativas) obtained from the document analysis.</li>
 * <li>An AjaxLink "Por qué es relevante ?" that shows why this sentencia is
 * relevant for the query.</li>
 * </ol>
 * Since 2 and 3 are time consuming, when the panel is initialized the analysis
 * is prefetched asynchronously via {@link PJSFSentenciaService}; when the links
 * are clicked, a blocking call joins the prefetch (usually already done while
 * the user reads the sumarios).
 */
public class AnalysisPanel<T extends RAGDocumento> extends ModelPanel<T> {

	private static final long serialVersionUID = 1L;

	static private Logger logger = Logger.getLogger(AnalysisPanel.class.getName());

	private static final DateTimeFormatter FECHA_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private State state = State.ANALYSIS_NOT_SHOWN;

	private enum State {
		ANALYSIS_NOT_SHOWN, ANALYSIS_SHOWN;
	}

	private boolean citasShown = false;

	/** unlike citas / analysis, the sumarios start expanded */
	private boolean sumariosShown = true;

	private static final String NO_INFO = "sin información";

	/** max words displayed for each sumario text; longer texts are trimmed with "..." */
	private static final int SUMARIO_MAX_WORDS = 220;

	private WebMarkupContainer analysisContainer;
	private WebMarkupContainer fallosContainer;
	private WebMarkupContainer normasContainer;
	private WebMarkupContainer sumariosContainer;

	private WebMarkupContainer analysisLookupContainer;
	private WebMarkupContainer citasLookupContainer;
	private WebMarkupContainer sumariosLookupContainer;

	private IModel<Query> queryModel;
	

	//public AnalysisPanel(String id, IModel<T> model) {
	//	this(id, model, null);
	//}

	public AnalysisPanel(String id, IModel<T> model, IModel<Query> queryModel) {
		super(id, model);
		this.queryModel = queryModel;
	}
	
	
	public Settings getSettingsService() {
		return (Settings) ServiceLocator.getInstance().getBean(Settings.class);
	}

	

	@Override
	public void onInitialize() {
		super.onInitialize();

		// start the asynchronous prefetch of the document analysis so that,
		// while the user reads the sumarios, the citas / relevance info is
		// already being computed
		prefetchAnalysis();

		
	 
		// --- PJSFSentencia: subtitle (tribunal - fecha) -------------------

		//Label subtitle = new Label("sentenciaSubtitle", getSentenciaSubtitleModel());
		//add(subtitle);

		// --- Sumarios of the PJSFSentencia --------------------------------
		// same expand/close pattern as citas and analysis, but starts open

		sumariosLookupContainer = new WebMarkupContainer("sumariosLookupContainer") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getUserSettingsService().isShowAnalysis() && !sumariosShown);
			}
		};
		sumariosLookupContainer.setOutputMarkupPlaceholderTag(true);

		AjaxLink<Void> showSumarios = new AjaxLink<Void>("showSumarios") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				sumariosShown = true;
				target.add(sumariosLookupContainer);
				target.add(sumariosContainer);
			}
		};
		sumariosLookupContainer.add(showSumarios);
		add(sumariosLookupContainer);

		sumariosContainer = new WebMarkupContainer("sumariosContainer") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getUserSettingsService().isShowAnalysis() && sumariosShown);
			}
		};
		sumariosContainer.setOutputMarkupPlaceholderTag(true);
		add(sumariosContainer);

		final IModel<List<PJSFSumario>> sumariosModel = getSumariosModel();

		WebMarkupContainer sumariosList = new WebMarkupContainer("sumarios") {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return !sumariosModel.getObject().isEmpty();
			}
		};
		sumariosContainer.add(sumariosList);

		sumariosList.add(new ListView<PJSFSumario>("quote", sumariosModel) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<PJSFSumario> item) {
				PJSFSumario sumario = item.getModelObject();
				// the voces are in the title
				Label title = new Label("sumario-title", sumario.getTitle() != null ? sumario.getTitle() : String.join(". ", sumario.getVoces() == null ? new ArrayList<>() : sumario.getVoces()));
				item.add(title);
				Label text = new Label("sumario-text", trimWords(sumario.getTexto(), SUMARIO_MAX_WORDS));
				text.setEscapeModelStrings(false);
				item.add(text);
			}
		});

		Label noSumarios = new Label("noSumariosQuotes", new LoadableDetachableModel<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected String load() {
				sentenciaModel.getObject(); // resolves the model, sets sentenciaError
				return sentenciaError != null ? sentenciaError : NO_INFO;
			}
		}) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return sumariosModel.getObject().isEmpty();
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				sentenciaModel.getObject();
				add(new org.apache.wicket.AttributeModifier("class", sentenciaError != null ? "text-danger" : ""));
			}
		};
		sumariosContainer.add(noSumarios);

		// close link of the sumarios section: hides the sumarios and shows
		// the "Sumarios" lookup link again
		AjaxLink<Void> closeSumarios = new AjaxLink<Void>("closeSumarios") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				sumariosShown = false;
				target.add(sumariosLookupContainer);
				target.add(sumariosContainer);
			}
		};
		sumariosContainer.add(closeSumarios);

		// --- Citas (jurisprudenciales + normativas) ------------------------
		// shown only after the user clicks on "Citas": blocking call to the
		// (prefetched) document analysis

		citasLookupContainer = new WebMarkupContainer("citasLookupContainer") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getUserSettingsService().isShowAnalysis() && !citasShown);
			}
		};
		citasLookupContainer.setOutputMarkupPlaceholderTag(true);

		// working indicator: the link shows a busy spinner while the (possibly
		// blocking) analysis lookup is being resolved
		AjaxLink<Void> showCitas = new IndicatingAjaxLink<Void>("showCitas") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				logger.debug("show citas for document " + AnalysisPanel.this.getModel().getObject().getId());
				citasShown = true;
				target.add(citasLookupContainer);
				target.add(fallosContainer);
				target.add(normasContainer);
			}
		};
		citasLookupContainer.add(showCitas);
		add(citasLookupContainer);

		// --- Citas jurisprudenciales (fallos) ----------------------------

		fallosContainer = addQuoteSection("fallosContainer", "fallos", "fallo-text", "noFallosQuotes", getFallosModel());


		// --- Citas normativas (normas) ------------------------------------

		normasContainer = addQuoteSection("normasContainer", "normas", "norma-text", "noNormasQuotes", getNormasModel());

		// close link of the citas sections: hides the citas and shows the
		// "Citas" lookup link again (same pattern as the analysis close link)
		AjaxLink<Void> closeCitas = new AjaxLink<Void>("closeCitas") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				citasShown = false;
				target.add(citasLookupContainer);
				target.add(fallosContainer);
				target.add(normasContainer);
			}
		};
		normasContainer.add(closeCitas);

		// --- "Por qué es relevante ?" (analysis) ---------------------------

		// visible only if the user enabled "show analysis"
		analysisLookupContainer = new WebMarkupContainer("analysisLookupContainer") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getUserSettingsService().isShowAnalysis() && getState() == State.ANALYSIS_NOT_SHOWN);
			}
		};
		analysisLookupContainer.setOutputMarkupPlaceholderTag(true);

		// working indicator: busy spinner while the analysis is generated
		AjaxLink<Void> showAnalysis = new IndicatingAjaxLink<Void>("show") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {

				logger.debug("show analysis for document " + AnalysisPanel.this.getModel().getObject().getId());

				long start = System.currentTimeMillis();
				generateAnalysis();
				logger.debug("analysis generated in " + (System.currentTimeMillis() - start) + " ms");

				state = State.ANALYSIS_SHOWN;
				target.add(analysisLookupContainer);
				target.add(analysisContainer);
			}
		};
		analysisLookupContainer.add(showAnalysis);
		add(analysisLookupContainer);

		// visible only if the user enabled "show analysis"
		analysisContainer = new WebMarkupContainer("analysisContainer") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getUserSettingsService().isShowAnalysis() && getState() == State.ANALYSIS_SHOWN);
			}
		};
		analysisContainer.setOutputMarkupPlaceholderTag(true);
		add(analysisContainer);

		analysisContainer.add(new InvisiblePanel("analysis"));
		analysisContainer.add(new InvisiblePanel("noInfoAnalysis"));

		// error panel: shown when the sentencia could not be retrieved from the
		// external PJSF server (timeout / server error)
		Label errorLabel = new Label("error", new LoadableDetachableModel<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected String load() {
				sentenciaModel.getObject(); // resolves the model, sets sentenciaError
				return sentenciaError;
			}
		}) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				sentenciaModel.getObject();
				return sentenciaError != null;
			}
		};
		errorLabel.add(new org.apache.wicket.AttributeModifier("class", "text-danger"));
		errorLabel.setOutputMarkupPlaceholderTag(true);
		add(errorLabel);

	}

	/**
	 * Starts the asynchronous prefetch of the document analysis, so that the
	 * blocking calls made when the user clicks "Citas" or "Por qué es relevante ?"
	 * are (usually) already resolved.
	 */
	protected void prefetchAnalysis() {
		try {
			T documento = getModel().getObject();
			String ragDocumentId = documento.getRagDocumentId();
			if (ragDocumentId != null && !ragDocumentId.isBlank() && queryModel != null)
				getPJSFSentenciaService().prefetchAnalysis(ragDocumentId, queryModel.getObject(), getSessionUser().get(), getSession().getId());
		} catch (Exception e) {
			logger.error(e);
		}
	}

	protected void generateAnalysis() {
		
		try {
			
			
			IModel<String> s=getAnalysisModel();
			
			String analysisText=s.getObject();
			
			Label analysis = new Label("analysis", Model.of(analysisText)) {
				private static final long serialVersionUID = 1L;
	
				@Override
				public boolean isVisible() {
					return hasAnalysis();
				}
			};
			analysis.setEscapeModelStrings(false);
			analysisContainer.addOrReplace(analysis);
		} catch (Exception e) {
			logger.error(e);
			Label error = new Label("analysis", "Error al obtener el análisis del documento: " + e.getMessage());
			error.setEscapeModelStrings(false);
			analysisContainer.addOrReplace(error);
		}
		
		
		
		// shown when there is no analysis: "no info", or the error message (in
		// place of the real data) if the RAG server could not be reached
		Label noInfoAnalysis = new Label("noInfoAnalysis", new LoadableDetachableModel<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected String load() {
				hasAnalysis(); // resolves converterModel, sets analysisError
				return analysisError != null ? analysisError : NO_INFO;
			}
		}) {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return !hasAnalysis();
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				hasAnalysis();
				add(new org.apache.wicket.AttributeModifier("class", analysisError != null ? "text-danger" : ""));
			}
		};
		analysisContainer.addOrReplace(noInfoAnalysis);

	AjaxLink<Void> close = new AjaxLink<Void>("close") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
			 
				state = State.ANALYSIS_NOT_SHOWN;
				target.add(analysisLookupContainer);
				target.add(analysisContainer);
			}
		}; 
		
		analysisContainer.addOrReplace(close);

	}

	protected State getState() {
		return state;
	
	}

	// --- PJSFSentencia models ---------------------------------------------

	/**
	 * The {@link PJSFSentencia} of this result: retrieved from the database or, if
	 * not stored, from the web (parsed and stored). Detachable so it is not
	 * serialized.
	 */
	/**
	 * Error message when the PJSF sentencia could not be retrieved (external
	 * server error / timeout); when not null, an error panel is displayed
	 * instead of failing the whole page render.
	 */
	private String sentenciaError;

	private final IModel<PJSFSentencia> sentenciaModel = new LoadableDetachableModel<PJSFSentencia>() {
		private static final long serialVersionUID = 1L;

		@Override
		protected PJSFSentencia load() {
			sentenciaError = null;
			T documento = AnalysisPanel.this.getModel().getObject();
			String sid = documento.getPjsfDocumentId();
			if (sid == null || sid.isBlank())
				return null;
			try {
				Optional<PJSFSentencia> sentencia = getPJSFSentenciaService().getSentencia(sid, getSessionUser().get());
				return sentencia.orElse(null);
			} catch (Exception e) {
				logger.error(e, "could not retrieve the sentencia " + sid + " (external server unavailable?)");
				sentenciaError = "No se pudo obtener el documento (servidor externo no disponible)";
				return null;
			}
		}
	};

	/** Subtitle of the sentencia: tribunal and fecha. */
	protected IModel<String> getSentenciaSubtitleModel() {
		return new LoadableDetachableModel<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected String load() {
				PJSFSentencia sentencia = sentenciaModel.getObject();
				if (sentencia == null)
					return "";
				StringBuilder sb = new StringBuilder();
				if (sentencia.getTribunal() != null)
					sb.append(sentencia.getTribunal());
				if (sentencia.getFecha() != null) {
					if (sb.length() > 0)
						sb.append(" - ");
					sb.append(sentencia.getFecha().format(FECHA_FORMAT));
				}
				return sb.toString();
			}
		};
	}

	/** The sumarios of the sentencia. */
	protected IModel<List<PJSFSumario>> getSumariosModel() {
		return new LoadableDetachableModel<List<PJSFSumario>>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected List<PJSFSumario> load() {
				PJSFSentencia sentencia = sentenciaModel.getObject();
				if (sentencia == null || sentencia.getSumarios() == null)
					return new ArrayList<>();
				return sentencia.getSumarios();
			}
		};
	}

	/**
	 * Builds a citation section: an outer container (visible only if the user
	 * enabled "show quotes" and clicked on "Citas"), a &lt;ul&gt; container with a
	 * ListView of items ("quote" is the repeating &lt;li&gt;) and a "no info" label
	 * shown when the list is empty.
	 */
	protected WebMarkupContainer addQuoteSection(String containerId, String listId, String textId, String noInfoId, final IModel<List<String>> listModel) {

		WebMarkupContainer container = new WebMarkupContainer(containerId) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getUserSettingsService().isShowAnalysis() && citasShown);
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

		// shown when the list is empty: "no info", or the error message (in
		// place of the real data) if the RAG server could not be reached
		Label noInfo = new Label(noInfoId, new LoadableDetachableModel<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected String load() {
				// evaluating the list model resolves converterModel and sets analysisError
				listModel.getObject();
				return analysisError != null ? analysisError : NO_INFO;
			}
		}) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return listModel.getObject().isEmpty();
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				listModel.getObject();
				add(new org.apache.wicket.AttributeModifier("class", analysisError != null ? "text-danger" : ""));
			}
		};
		container.add(noInfo);

		return container;
	}

	/**
	 * Error message when the RAG server could not be reached (or failed); when
	 * not null, an error text is displayed in place of the real data so the
	 * application keeps working with cached queries/contents only.
	 */
	private String analysisError;

	/**
	 * Converter of the RAG document analysis response, shared by the analysis label
	 * and the citation ListViews. Detachable so it is not serialized. The response
	 * is obtained with a blocking call that joins the pending asynchronous prefetch
	 * (started when the panel was initialized). If the RAG server can not be
	 * reached, an empty converter is returned and {@link #analysisError} is set,
	 * so an error message is displayed in place of the real data.
	 */
	private final IModel<RAGDocumentAnalysisConverter> converterModel=new LoadableDetachableModel<RAGDocumentAnalysisConverter>(){private static final long serialVersionUID=1L;

	@Override protected RAGDocumentAnalysisConverter load(){

	analysisError=null;

	T documento=AnalysisPanel.this.getModel().getObject();String ragDocumentId=documento.getRagDocumentId();

	if(ragDocumentId!=null&&!ragDocumentId.isBlank()&&queryModel!=null)
	{
		logger.debug("analyzing document "+documento.getId()+" with RAG document id "+ragDocumentId+" and query: "+queryModel.getObject().getQuery());

	try{
	DocumentAnalysisResponse response=getPJSFSentenciaService().getAnalysis(ragDocumentId,queryModel.getObject(), getSessionUser().get(),getSession().getId());
	return new RAGDocumentAnalysisConverter(response);
	}catch(Exception e){
	logger.error(e,"could not get the document analysis (RAG server unavailable?)");
	analysisError="No se pudo obtener el análisis del documento (servidor no disponible)";
	return new RAGDocumentAnalysisConverter((String)null);
	}

	}return new RAGDocumentAnalysisConverter((String)null);}};

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
				
				String mds=converterModel.getObject().getAnalysis();
				String r=MarkdownUtil.markdownToHtml(mds);
				String m="<p><span style=\"font-size:0.85em;\">Software de inferencia. <i>" +  getSettingsService().getRagLlm() + "</i></span></p>";
						
				return r+m;
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

	/**
	 * Trims the text at {@code maxWords} words, appending "..." if the text is
	 * longer.
	 */
	protected static String trimWords(String text, int maxWords) {
		if (text == null)
			return "";
		String[] words = text.trim().split("\\s+");
		if (words.length <= maxWords)
			return text;
		return String.join(" ", java.util.Arrays.copyOfRange(words, 0, maxWords)) + " ...";
	}

	@Override
	public void onDetach() {
		converterModel.detach();
		sentenciaModel.detach();

		if (queryModel != null)
			queryModel.detach();
		
		super.onDetach();
	}

	
	
	/** Domain{@link io.demo.model.User} of the

	current session (resolved from
			 * Spring Security and cached in the Wicket session). Pass this to the
			 * services layer, which must never see Wicket types.
			 */

	protected java.util.Optional<io.demo.model.User> getSessionUser() {
		return io.demo.web.WebSessionUser.get();
	}

	protected LegalSearchService getLegalSearchService() {
		return (LegalSearchService) ServiceLocator.getInstance().getBean(LegalSearchService.class);
	}

	protected PJSFSentenciaService getPJSFSentenciaService() {
		return (PJSFSentenciaService) ServiceLocator.getInstance().getBean(PJSFSentenciaService.class);
	}

	protected UserSettingsService getUserSettingsService() {
		return (UserSettingsService) ServiceLocator.getInstance().getBean(UserSettingsService.class);
	}
}
