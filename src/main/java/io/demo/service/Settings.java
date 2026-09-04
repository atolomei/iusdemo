package io.demo.service;

import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;import com.github.benmanes.caffeine.cache.Caffeine;

import io.demo.Logger;
import jakarta.annotation.PostConstruct;
 ;

/**
 * <p>
 * Server configuration defined in file {@code application.properties}
 * </p>
 * 
 * @author atolomei@novamens.com (Alejandro Tolomei)
 * 
 */
@Configuration
public class Settings {

	
	@SuppressWarnings("unused")
	static private Logger logger = Logger.getLogger(Settings.class.getName());

	private static final OffsetDateTime systemStarted = OffsetDateTime.now();

	/** APP ----------------------------------------------------------------- */

	@Value("${app.name:book}")
	protected String appName;

	@Value("${server.port:8080}")
	protected int port;

	/** WORK DIRECTORIES ---------------------------------------------------- */

	@Value("${work.dir:work}")
	protected String workDir;

	@Value("${output.dir:output}")
	protected String outputDir;

	@Value("${templates.dir:templates}")
	protected String templatesDir;

	@Value("${fonts.dir:fonts}")
	protected String fontsDir;

	@Value("${input.dir:input}")
	protected String inputDir;

	/** BOOK DIRECTORIES ---------------------------------------------------- */

	/** Directory containing the source PDF file(s) to process. */
	@Value("${book.src:work/src}")
	protected String bookSrcDir;

	/** Directory where per-page debug/inspection files are written. */
	@Value("${book.pdf.debug:work/debug}")
	protected String bookPdfDebugDir;

	/** Directory where the extracted JSON representation of the PDF is stored. */
	@Value("${book.pdf.parse:work/parse}")
	protected String bookPdfParseDir;

	/** Directory where images extracted from the PDF are saved. */
	@Value("${book.pdf.parse.images:work/parse/images}")
	protected String bookPdfParseImagesDir;

	/** Directory where the generated Markdown file is written. */
	@Value("${book.markdown:work/markdown}")
	protected String bookMarkdownDir;

	/** Directory where the final HTML output is written. */
	@Value("${book.html:work/html}")
	protected String bookHtmlDir;

	/** Source directory for the HTML site (templates, css, js masters). */
	@Value("${book.html.source:html-source}")
	protected String bookHtmlSourceDir;

	/** Output directory for the generated interactive HTML site. */
	@Value("${book.html.site:html}")
	protected String bookHtmlSiteDir;

	/** TRANSLATION --------------------------------------------------------- */

	@Value("${book.translate.enabled:false}")
	private boolean translateEnabled;

	@Value("${book.translate.target-lang:en}")
	private String translateTargetLang;

	/**
	 * Path to Google service-account JSON credentials. If empty, Application
	 * Default Credentials (gcloud/env) are used.
	 */
	@Value("${google.translate.credentials:}")
	private String googleTranslateAuthPath;

	/** PDF SETTINGS -------------------------------------------------------- */

	@Value("${pdf.page.width:612}")
	protected float pageWidth;

	@Value("${pdf.page.height:792}")
	protected float pageHeight;

	@Value("${pdf.margin.top:72}")
	protected float marginTop;

	@Value("${pdf.margin.bottom:72}")
	protected float marginBottom;

	@Value("${pdf.margin.left:72}")
	protected float marginLeft;

	@Value("${pdf.margin.right:72}")
	protected float marginRight;

	@Value("${pdf.font.size.body:12}")
	protected float fontSizeBody;

	@Value("${pdf.font.size.title:24}")
	protected float fontSizeTitle;

	@Value("${pdf.font.size.chapter:18}")
	protected float fontSizeChapter;

	@Value("${pdf.font.size.section:14}")
	protected float fontSizeSection;

	@Value("${pdf.font.size.footer:9}")
	protected float fontSizeFooter;

	@Value("${pdf.line.spacing:1.5}")
	protected float lineSpacing;

	/** KBEE RAG SERVER ------------------------------------------------------ */

	@Value("${kbee.rag.url:http://localhost}")
	protected String ragServerUrl;

	@Value("${kbee.rag.port:8081}")
	protected int ragServerPort;

	@Value("${kbee.rag.topk:15}")
	protected int ragTopK;

	/** CACHE DIRECTORIES ---------------------------------------------------- */

	/** Directory where the query cache is persisted. */
	@Value("${cache.query.dir:work/cache/query}")
	protected String queryCacheDir;

	/** Directory where the document analyze cache is persisted. */
	@Value("${cache.documentanalyze.dir:work/cache/documentanalyze}")
	protected String documentAnalyzeCacheDir;

	/** Directory containing the test queries JSON files. */
	@Value("${testqueries.dir:work/testqueries}")
	protected String testQueriesDir;

	/** DATABASE -------------------------------------------------------------- */

	@Value("${spring.datasource.url:jdbc:postgresql://localhost:5432/demo}")
	protected String databaseUrl;

	@Value("${spring.datasource.username:postgres}")
	protected String databaseUsername;

	@Value("${spring.datasource.password:}")
	protected String databasePassword;

	@Value("${spring.datasource.driver-class-name:org.postgresql.Driver}")
	protected String databaseDriver;

	/** LOCALE / TIMEZONE --------------------------------------------------- */

	@Value("${server.locale:en}")
	protected String localeStr;

	@Value("${server.zoneid:UTC}")
	protected String zoneid;

	/** ---------------------------------------------------------------------- */

	public Settings() {
	}

	@PostConstruct
	protected void onInitialize() {
		checkDirs();
	}

	/** Getters -------------------------------------------------------------- */

	public OffsetDateTime getSystemStartTime() {
		return systemStarted;
	}

	public String getAppName() {
		return appName;
	}

	public int getPort() {
		return port;
	}

	public String getWorkDir() {
		return workDir;
	}

	public String getOutputDir() {
		return outputDir;
	}

	public String getTemplatesDir() {
		return templatesDir;
	}

	public String getFontsDir() {
		return fontsDir;
	}

	public String getInputDir() {
		return inputDir;
	}

	public String getBookSrcDir() {
		return bookSrcDir;
	}

	public String getBookPdfDebugDir() {
		return bookPdfDebugDir;
	}

	public String getBookPdfParseDir() {
		return bookPdfParseDir;
	}

	public String getBookPdfParseImagesDir() {
		return bookPdfParseImagesDir;
	}

	public String getBookMarkdownDir() {
		return bookMarkdownDir;
	}

	public String getBookHtmlDir() {
		return bookHtmlDir;
	}

	public String getBookHtmlSourceDir() {
		return bookHtmlSourceDir;
	}

	public String getBookHtmlSiteDir() {
		return bookHtmlSiteDir;
	}

	public boolean isTranslateEnabled() {
		return translateEnabled;
	}

	public String getTranslateTargetLang() {
		return translateTargetLang;
	}

	public String getGoogleTranslateAuthPath() {
		return googleTranslateAuthPath;
	}

	public float getPageWidth() {
		return pageWidth;
	}

	public float getPageHeight() {
		return pageHeight;
	}

	public float getMarginTop() {
		return marginTop;
	}

	public float getMarginBottom() {
		return marginBottom;
	}

	public float getMarginLeft() {
		return marginLeft;
	}

	public float getMarginRight() {
		return marginRight;
	}

	public float getFontSizeBody() {
		return fontSizeBody;
	}

	public float getFontSizeTitle() {
		return fontSizeTitle;
	}

	public float getFontSizeChapter() {
		return fontSizeChapter;
	}

	public float getFontSizeSection() {
		return fontSizeSection;
	}

	public float getFontSizeFooter() {
		return fontSizeFooter;
	}

	public float getLineSpacing() {
		return lineSpacing;
	}

	public String getRagServerUrl() {
		return ragServerUrl;
	}

	public int getRagServerPort() {
		return ragServerPort;
	}

	public int getRagTopK() {
		return ragTopK;
	}

	public String getDatabaseUrl() {
		return databaseUrl;
	}

	public String getDatabaseUsername() {
		return databaseUsername;
	}

	public String getDatabasePassword() {
		return databasePassword;
	}

	public String getDatabaseDriver() {
		return databaseDriver;
	}

	public Locale getDefaultLocale() {
		return Locale.forLanguageTag(localeStr);
	}

	public ZoneId getDefaultZoneId() {
		return ZoneId.of(zoneid);
	}

	/**
	 * @return usable content width (page width minus left and right margins)
	 */
	public float getContentWidth() {
		return pageWidth - marginLeft - marginRight;
	}

	/**
	 * @return usable content height (page height minus top and bottom margins)
	 */
	public float getContentHeight() {
		return pageHeight - marginTop - marginBottom;
	}

	/** Directory bootstrap -------------------------------------------------- */

	private void checkDirs() {

		for (String path : new String[] {
				workDir,
				outputDir,
				inputDir,
				bookSrcDir,
				bookPdfDebugDir,
				bookPdfParseDir,
				bookPdfParseImagesDir,
				bookMarkdownDir,
				bookHtmlDir,
				queryCacheDir,
				documentAnalyzeCacheDir,
				testQueriesDir
		}) {
			try {
				File dir = new File(path);
				if (!dir.exists() || !dir.isDirectory())
					FileUtils.forceMkdir(dir);
			} catch (IOException e) {
				throw new RuntimeException("Failed to create directory: " + path, e);
			}
		}
	}

	public int getQueryCacheInitialCapacity() {
		return 100;
	}


	public long getCacheQueryDurationMinutes() {
		return 60*24*30;
	}

	public long getQueryCacheMaxCapacity() {
		return 10000;
	}

	/** Directory where the query cache is persisted ("query"). */
	public String getQueryCacheDir() {
		return queryCacheDir;
	}

	/** Directory where the document analyze cache is persisted ("documentanalyze"). */
	public String getDocumentAnalyzeCacheDir() {
		return documentAnalyzeCacheDir;
	}

	/** Directory containing the test queries JSON files ("testqueries"). */
	public String getTestQueriesDir() {
		return testQueriesDir;
	}
}
