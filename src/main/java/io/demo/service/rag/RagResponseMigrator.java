package io.demo.service.rag;

import java.io.File;

import io.demo.Logger;
import io.demo.model.DemoObjectMapper;
import io.demo.service.Settings;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * One-shot migration of legacy {@link RagResponse} JSON (flat {@code sources}
 * entries such as {@code {"documentId": ..., "score": ...}}) to the new
 * format ({@code {"selected": {...}, "contextSegments": []}}).
 * <p>
 * The migration is idempotent: files/rows already in the new format are left
 * untouched. It operates on the JSON tree model, so it does not depend on the
 * current DTO records.
 * </p>
 */
public class RagResponseMigrator {

	static private Logger logger = Logger.getLogger(RagResponseMigrator.class.getName());

	private final ObjectMapper mapper = new DemoObjectMapper();

	/**
	 * Migrates every {@code *.json} file in the work directory.
	 *
	 * @return the number of files migrated
	 */
	public int migrateWorkDir(Settings settings) {

		File workDir = new File(settings.getWorkDir());
		File[] files = workDir.listFiles((dir, name) -> name.endsWith(".json"));

		if (files == null)
			return 0;

		int migrated = 0;

		for (File file : files) {
			try {
				JsonNode tree = mapper.readTree(file);

				if (!(tree instanceof ObjectNode root))
					continue;

				if (migrateResponse(root)) {
					mapper.writerWithDefaultPrettyPrinter().writeValue(file, root);
					logger.info("migrated -> " + file.getName());
					migrated++;
				}
			} catch (Exception e) {
				logger.error(e, "could not migrate -> " + file.getAbsolutePath());
			}
		}

		return migrated;
	}

	/**
	 * Migrates a legacy RagResponse JSON string to the new format.
	 *
	 * @return the migrated JSON, or {@code null} if the input was not a legacy
	 *         RagResponse (already migrated, plain text, etc.)
	 */
	public String migrateJson(String json) {
		if (json == null || !json.trim().startsWith("{"))
			return null;
		try {
			JsonNode tree = mapper.readTree(json);
			if (!(tree instanceof ObjectNode root))
				return null;
			return migrateResponse(root) ? mapper.writeValueAsString(root) : null;
		} catch (Exception e) {
			logger.debug("not a valid RagResponse JSON, skipping: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Converts a RagResponse tree in place.
	 *
	 * @return {@code true} if the node was in the legacy format and was converted
	 */
	public boolean migrateResponse(ObjectNode root) {

		JsonNode sources = root.get("sources");

		if (sources == null || !sources.isArray())
			return false;

		boolean changed = false;
		ArrayNode migrated = mapper.createArrayNode();

		for (JsonNode source : sources) {

			if (source.has("selected")) {
				// already new format
				migrated.add(source);
			} else {
				ObjectNode wrapper = mapper.createObjectNode();
				wrapper.set("selected", source);
				wrapper.set("contextSegments", mapper.createArrayNode());
				migrated.add(wrapper);
				changed = true;
			}
		}

		if (changed)
			root.set("sources", migrated);

		if (!root.has("answer")) {
			root.putNull("answer");
			changed = true;
		}

		return changed;
	}
}
