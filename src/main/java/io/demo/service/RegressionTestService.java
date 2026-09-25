package io.demo.service;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import io.demo.Logger;
import io.demo.model.RAGSentencia;
import io.demo.results.ReasoningEffortOption;

/**
 * Executes a regression test with the queries loaded by
 * {@link TestQueriesService}.
 * <p>
 * For each test query (and each selected {@link ReasoningEffortOption}) the
 * search is executed via {@link LegalSearchService} (cache disabled) and the
 * result is evaluated against the expected {@code pjsfDocumentId}. Results
 * are logged to the console and written as a CSV file named
 * {@code regression-text-<timestamp>.txt} in the directory provided by
 * {@link Settings#getRegressionTestDir()}.
 * </p>
 * CSV columns:
 * <ol>
 * <li>Query</li>
 * <li>LLM</li>
 * <li>topK used</li>
 * <li>Expected result documentId</li>
 * <li>SUCCESS (expected result in the results) [YES / NO]</li>
 * <li>#RELEVANCE (order of the expected result, sorted by relevance)</li>
 * <li>#DATE (order of the expected result, sorted by date, newest first)</li>
 * <li>DURATION (SECS)</li>
 * </ol>
 */
@Service
public class RegressionTestService extends BaseService {

	static private Logger logger = Logger.getLogger(RegressionTestService.class.getName());

	static final private String CSV_HEADER = "QUERY,LLM,TOPK,EXPECTED_DOCUMENT_ID,SUCCESS,#RELEVANCE,#DATE,DURATION_SECS";

	static final private String SESSION_ID = "regression-test";

	private final TestQueriesService testQueriesService;
	private final LegalSearchService legalSearchService;

	public RegressionTestService(Settings settings, TestQueriesService testQueriesService, LegalSearchService legalSearchService) {
		super(settings);
		this.testQueriesService = testQueriesService;
		this.legalSearchService = legalSearchService;
	}

	/**
	 * Runs the regression test and returns the generated CSV file.
	 */
	public File run() {

		Map<String, String> queries = this.testQueriesService.getTestQueries();

		File file = createResultsFile();

		String llm = getSettings().getRagLlm();

		int total = 0;
		int passed = 0;

		logger.info("regression test started -> " + queries.size() + " queries | llm: " + llm);

		try (PrintWriter out = new PrintWriter(file, StandardCharsets.UTF_8)) {

			out.println(CSV_HEADER);
			logger.info(CSV_HEADER);

			for (Map.Entry<String, String> entry : queries.entrySet()) {

				String query = entry.getKey();
				String expectedDocumentId = entry.getValue();

				for (ReasoningEffortOption effort : getEfforts()) {

					String row = execute(query, expectedDocumentId, llm, effort);

					out.println(row);
					out.flush();
					logger.info(row);

					total++;
					if (row.contains(",YES,"))
						passed++;
				}
			}

		} catch (IOException e) {
			throw new RuntimeException("Failed to write regression test results file: " + file.getAbsolutePath(), e);
		}

		logger.info("regression test completed -> total: " + total + " | passed: " + passed + " | failed: " + (total - passed));
		logger.info("results file -> " + file.getAbsolutePath());

		return file;
	}

	/**
	 * Executes one query and returns the CSV row.
	 */
	protected String execute(String query, String expectedDocumentId, String llm, ReasoningEffortOption effort) {

		long start = System.nanoTime();

		List<RAGSentencia> results;

		try {
			
			OffsetDateTime startDate = OffsetDateTime.now().minusYears(5);
			OffsetDateTime endDate = null;
			
			
			results = this.legalSearchService.search(query, startDate, null, null, null, SESSION_ID, false, null, null, effort);
		} catch (Exception e) {
			logger.error(e);
			double secs = (System.nanoTime() - start) / 1_000_000_000.0;
			return csvRow(query, llm, effort.getTopK(), expectedDocumentId, false, -1, -1, secs, "ERROR: " + e.getClass().getSimpleName());
		}

		double secs = (System.nanoTime() - start) / 1_000_000_000.0;

		boolean success = false;

		if (results != null)
			for (RAGSentencia s : results)
				if (s.getPjsfDocumentId() != null && this.testQueriesService.check(query, s.getPjsfDocumentId())) {
					success = true;
					break;
				}

		int relevanceOrder = order(results, expectedDocumentId,
				Comparator.comparing(RAGSentencia::getScore, Comparator.nullsLast(Comparator.reverseOrder())));

		int dateOrder = order(results, expectedDocumentId,
				Comparator.comparing(RAGSentencia::getFecha, Comparator.nullsLast(Comparator.reverseOrder())));

		return csvRow(query, llm, effort.getTopK(), expectedDocumentId, success, relevanceOrder, dateOrder, secs, null);
	}

	/**
	 * 1-based position of the expected document in the results sorted with the
	 * given comparator, or {@code -1} if not present.
	 */
	protected int order(List<RAGSentencia> results, String expectedDocumentId, Comparator<RAGSentencia> comparator) {

		if (results == null || expectedDocumentId == null)
			return -1;

		List<RAGSentencia> sorted = new ArrayList<>(results);
		sorted.sort(comparator);

		for (int i = 0; i < sorted.size(); i++)
			if (expectedDocumentId.equals(sorted.get(i).getPjsfDocumentId()))
				return i + 1;

		return -1;
	}

	protected String csvRow(String query, String llm, int topK, String expectedDocumentId, boolean success, int relevanceOrder, int dateOrder, double durationSecs, String error) {

		StringBuilder str = new StringBuilder();

		str.append(escape(query)).append(",");
		str.append(escape(llm)).append(",");
		str.append(topK).append(",");
		str.append(escape(expectedDocumentId)).append(",");
		str.append(success ? "YES" : "NO").append(",");
		str.append(relevanceOrder > 0 ? String.valueOf(relevanceOrder) : "-").append(",");
		str.append(dateOrder > 0 ? String.valueOf(dateOrder) : "-").append(",");
		str.append(String.format(java.util.Locale.US, "%.2f", Double.valueOf(durationSecs)));

		if (error != null)
			str.append(",").append(escape(error));

		return str.toString();
	}

	/** Escapes a CSV value (quotes values containing comma, quote or newline). */
	protected String escape(String value) {
		if (value == null)
			return "";
		if (value.contains(",") || value.contains("\"") || value.contains("\n"))
			return "\"" + value.replace("\"", "\"\"") + "\"";
		return value;
	}

	/** Reasoning effort options to test (from Settings, empty -> all). */
	protected List<ReasoningEffortOption> getEfforts() {

		String str = getSettings().getRegressionTestEfforts();

		List<ReasoningEffortOption> list = new ArrayList<>();

		if (str == null || str.isBlank()) {
			for (ReasoningEffortOption o : ReasoningEffortOption.values())
				list.add(o);
			return list;
		}

		for (String key : str.split(","))
			if (!key.isBlank())
				list.add(ReasoningEffortOption.fromKey(key.trim()));

		return list;
	}

	/** Creates the results file "regression-text-yyyyMMdd-HHmmss.txt". */
	protected File createResultsFile() {

		File dir = new File(getSettings().getRegressionTestDir());

		try {
			FileUtils.forceMkdir(dir);
		} catch (IOException e) {
			throw new RuntimeException("Failed to create regression test directory: " + dir.getAbsolutePath(), e);
		}

		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));

		return new File(dir, "regression-text-" + timestamp + ".txt");
	}
}
