package io.demo.service.rag;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.List;

import org.springframework.stereotype.Service;


import io.demo.Logger;
import io.demo.model.DemoObjectMapper;
import io.demo.model.Query;
import io.demo.service.Settings;
import tools.jackson.databind.ObjectMapper;

/**
 * <p>
 * Client for the Kbee RAG Server prototype (JSON REST API published by
 * {@code RagController} on the kbee-solr server).
 * </p>
 * <p>
 * Sample request:
 * </p>
 * 
 * <pre>
 * curl -X POST http://localhost:8081/api/rag/answer \
 *   -H "Content-Type: application/json" \
 *   -d '{ "question": "...", "topK": 15 }'
 * </pre>
 * 
 * @author atolomei@novamens.com (Alejandro Tolomei)
 */
@Service
public class KbeeRAGClient {

    static private Logger logger = Logger.getLogger(KbeeRAGClient.class.getName());

    private static final String ANSWER_ENDPOINT = "/api/rag/answer";

    private static final String DOCUMENT_ANALYSIS_ENDPOINT = "/api/rag/document-analysis";

    private static final String QUERY_ANALYSIS_ENDPOINT = "/api/rag/queryanalysis";

    private final Settings settings;

    private final HttpClient httpClient;

    private final ObjectMapper mapper;

    public KbeeRAGClient(Settings settings) {
        this.settings = settings;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        this.mapper = new DemoObjectMapper();
        //this.mapper.registerModule(new JavaTimeModule());
        //this.mapper.findAndRegisterModules();
        //this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Connects to the Kbee RAG Server prototype, sends the question and parses the
     * JSON response into a {@link RagResponse} record.
     * <p>
     * If debug logging is enabled, the response is also saved to disk in the work
     * directory, under a file named with a hash of the question ({@code <hash>.json}),
     * so the server output can be analyzed.
     * </p>
     * 
     * @param question the question to send to the RAG server
     * @return the parsed {@link RagResponse}
     */
    public RagResponse executeQuery(String question) {
        return executeQuery(question, null);
    }

    /**
     * Same as {@link #executeQuery(String)}, but allows passing optional
     * filter parameters (e.g. {@code fromDate}, {@code toDate}) that are
     * forwarded to the server in the {@link RagRequest}.
     */
    public RagResponse executeQuery(String question, java.util.Map<String, String> parameters) {
        return executeQuery(question, parameters, 0);
    }

    /**
     * Same as {@link #executeQuery(String, java.util.Map)}, but allows requesting a
     * specific number of results (topK). When {@code topK <= 0} the default from
     * {@link io.demo.service.Settings#getRagTopK()} is used.
     */
    public RagResponse executeQuery(String question, java.util.Map<String, String> parameters, int topK) {
        return executeQuery(question, parameters, topK, null);
    }

    /**
     * Same as {@link #executeQuery(String, java.util.Map, int)}, but also passes the
     * {@link io.demo.results.ReasoningEffortOption} (both topK and key) to the RAG server.
     * When {@code reasoningEffort} is null the server defaults to "low".
     */
    public RagResponse executeQuery(String question, java.util.Map<String, String> parameters, int topK,
            io.demo.results.ReasoningEffortOption reasoningEffort) {

        try {
        	
        	int rerankTopK = (topK > 0) ? topK : ((reasoningEffort != null) ? reasoningEffort.getTopK() : settings.getRagTopK());
        	if (question.trim().toLowerCase().contains("suprema")) {
            	rerankTopK = Math.max(rerankTopK, 40);
            }
        	
        	String reasoningEffortKey = (reasoningEffort != null) ? reasoningEffort.getKey() : null;

            String requestBody = mapper.writeValueAsString(new RagRequest(question, parameters, rerankTopK, settings.getRagLlm(), reasoningEffortKey));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(getAnswerUrl()))
                    .timeout(Duration.ofMinutes( settings.getSearchTimeOutMinutes()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", basicAuth())
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

          
            logger.debug("Sending request to Kbee RAG Server -> " + requestBody.toString());
            
            long startTime = System.currentTimeMillis();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            logger.debug("Round-trip time to Kbee RAG Server -> " + (System.currentTimeMillis() - startTime) + " ms");
            
            if (response.statusCode() != 200) {
            
            	logger.error("Kbee RAG Server returned HTTP " + response.statusCode() + " | " + getAnswerUrl() + " | response body: " + response.body());
            	throw new RuntimeException("Kbee RAG Server returned HTTP " + response.statusCode() + " | " + getAnswerUrl() + "  <br/> response body: " + response.body());
            }
            
            logger.debug("Kbee RAG Server response: " + response.body());
            
            RagResponse ragResponse = mapper.readValue(response.body(), RagResponse.class);

        //    if (logger.isDebugEnabled())
                saveToDisk(question, ragResponse);

            return ragResponse;

        } catch (RuntimeException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while calling Kbee RAG Server | " + getAnswerUrl(), e);
        } catch (Exception e) {
        	logger.error(e);      	
            throw new RuntimeException("Error calling Kbee RAG Server | " + getAnswerUrl(), e);
        }
    }

    /**
     * Calls the Kbee RAG Server {@code /api/rag/document-analysis} endpoint and
     * parses the JSON response into a {@link DocumentAnalysisResponse} record.
     * 
     * @param ragDocumentId id of the document (segment) in the RAG index
     * @param query         the question to answer about the document
     * @return the parsed {@link DocumentAnalysisResponse}
     */
    public DocumentAnalysisResponse analyzeDocument(String ragDocumentId, Query query) {

        try {
            String requestBody = mapper.writeValueAsString(new DocumentAnalysisRequestPayload(ragDocumentId, query.getQuery(), settings.getRagLlm()));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(getDocumentAnalysisUrl()))
                    .timeout(Duration.ofMinutes( settings.getSearchTimeOutMinutes()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", basicAuth())
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            long startTime = System.currentTimeMillis();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            logger.debug("Round-trip time to Kbee RAG Server (document-analysis): " + (System.currentTimeMillis() - startTime) + " ms");

            if (response.statusCode() != 200)
                throw new RuntimeException("Kbee RAG Server returned HTTP " + response.statusCode() + " | " + getDocumentAnalysisUrl());

            logger.debug("Kbee RAG Server response: " + response.body());

            return mapper.readValue(response.body(), DocumentAnalysisResponse.class);

        } catch (RuntimeException e) {
        	logger.error(e);
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while calling Kbee RAG Server | " + getDocumentAnalysisUrl(), e);
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("Error calling Kbee RAG Server | " + getDocumentAnalysisUrl(), e);
        }
    }

    /**
     * Calls the Kbee RAG Server {@code /api/rag/queryanalysis} endpoint and
     * parses the JSON response into a {@link QueryAnalysis} record.
     *
     * @param serverQueryId the server's id of the query
     * @param llm           the llm to use for the analysis
     * @return the parsed {@link QueryAnalysis}
     */
    public QueryAnalysis queryAnalysis(String serverQueryId, String llm) {

        try {
            String requestBody = mapper.writeValueAsString(new QueryAnalysisRequestPayload(serverQueryId, llm));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(getQueryAnalysisUrl()))
                    .timeout(Duration.ofMinutes(settings.getSearchTimeOutMinutes()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", basicAuth())
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            long startTime = System.currentTimeMillis();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            logger.debug("Round-trip time to Kbee RAG Server (queryanalysis): " + (System.currentTimeMillis() - startTime) + " ms");

            if (response.statusCode() != 200)
                throw new RuntimeException("Kbee RAG Server returned HTTP " + response.statusCode() + " | " + getQueryAnalysisUrl());

            logger.debug("Kbee RAG Server response: " + response.body());

            return mapper.readValue(response.body(), QueryAnalysis.class);

        } catch (RuntimeException e) {
            logger.error(e);
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while calling Kbee RAG Server | " + getQueryAnalysisUrl(), e);
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("Error calling Kbee RAG Server | " + getQueryAnalysisUrl(), e);
        }
    }

    /**
     * Returns {@code true} if the Kbee RAG Server can be reached (a TCP
     * connection to its host/port succeeds within a short timeout).
     */
    public boolean isAvailable() {
        try {
            URI uri = URI.create(settings.getRagServerUrl() + ":" + settings.getRagServerPort());
            String host = uri.getHost() != null ? uri.getHost() : settings.getRagServerUrl();
            int port = uri.getPort() > 0 ? uri.getPort() : settings.getRagServerPort();
            try (java.net.Socket socket = new java.net.Socket()) {
                socket.connect(new java.net.InetSocketAddress(host, port), 1500);
                return true;
            }
        } catch (Exception e) {
            logger.debug("Kbee RAG Server not reachable -> " + e.getClass().getSimpleName() + " | " + e.getMessage());
            return false;
        }
    }

    /**
     * Returns the file in the work directory where the {@link RagResponse} for
     * the given question is saved by {@link #executeQuery(String)}.
     */
    public File getResponseFile(String question) {
        try {
            return new File(settings.getWorkDir(), hash(question) + ".json");
        } catch (Exception e) {
            throw new RuntimeException("could not compute response file for question -> " + question, e);
        }
    }

    private String getAnswerUrl() {
        return settings.getRagServerUrl() + ":" + settings.getRagServerPort() + ANSWER_ENDPOINT;
    }

    /** HTTP Basic Authorization header value from settings (kbee.rag.user / kbee.rag.password). */
    private String basicAuth() {
        String credentials = settings.getRagUser() + ":" + settings.getRagPassword();
        return "Basic " + java.util.Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    private String getDocumentAnalysisUrl() {
        return settings.getRagServerUrl() + ":" + settings.getRagServerPort() + DOCUMENT_ANALYSIS_ENDPOINT;
    }

    private String getQueryAnalysisUrl() {
        return settings.getRagServerUrl() + ":" + settings.getRagServerPort() + QUERY_ANALYSIS_ENDPOINT;
    }

    
    public List<RagResponse> getResponseHistory() {

    
    	try {
			File workDir = new File( settings.getWorkDir() );
			File[] files = workDir.listFiles((dir, name) -> name.endsWith(".json"));
			if (files == null) {
				logger.warn("No response history found in work directory: " + workDir.getAbsolutePath());
				return List.of();
			}
			return List.of(files).stream()
					.map(file -> {
						try {
							return mapper.readValue(file, RagResponse.class);
						} catch (Exception e) {
							logger.error(e, "could not read RagResponse from file -> " + file.getAbsolutePath());
							return null;
						}
					})
					.filter(ragResponse -> ragResponse != null)
					.toList();
		} catch (Exception e) {
			throw new RuntimeException("could not get response history", e);
		}
    	
    	    	
    	
    
    }
    /**
     * Saves the {@link RagResponse} as pretty-printed JSON into the work
     * directory, with the file name being the SHA-256 hash of the question.
     */
    private void saveToDisk(String question, RagResponse ragResponse) {
        try {
            File file = new File(settings.getWorkDir(), hash(question) + ".json");
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, ragResponse);
            logger.debug("RagResponse saved -> " + file.getAbsolutePath());
        } catch (Exception e) {
            logger.error(e, "could not save RagResponse to disk");
        }
    }

    private static String hash(String text) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    /** Payload matching the server side {@code DocumentAnalysisRequest} record. */
    private record DocumentAnalysisRequestPayload(String documentId, String question, String llm) {
    }

    /** Payload matching the server side {@code QueryAnalysisRequest} record. */
    private record QueryAnalysisRequestPayload(String queryId, String llm) {
    }
}
