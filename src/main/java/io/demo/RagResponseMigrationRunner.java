package io.demo;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import io.demo.model.Query;
import io.demo.model.db.service.QueryDBService;
import io.demo.service.Settings;
import io.demo.service.rag.RagResponseMigrator;

/**
 * One-shot startup migration of legacy {@link io.demo.service.rag.RagResponse}
 * JSON (flat {@code sources}) to the new server format
 * ({@code {"selected": {...}, "contextSegments": []}}).
 * <p>
 * Migrates both the response files saved in the work directory and the
 * {@code results} column of the {@code query} table. The migration is
 * idempotent: entries already in the new format are left untouched, so it is
 * safe to run on every startup.
 * </p>
 */
@Component
@Order(10)
@ConditionalOnProperty(name = "migratejson", havingValue = "true")
public class RagResponseMigrationRunner implements ApplicationRunner {

	static private Logger logger = Logger.getLogger(RagResponseMigrationRunner.class.getName());
	static private Logger startupLogger = Logger.getLogger("StartupLogger");

	private final Settings settings;
	private final QueryDBService queryDBService;

	public RagResponseMigrationRunner(Settings settings, QueryDBService queryDBService) {
		this.settings = settings;
		this.queryDBService = queryDBService;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {

		RagResponseMigrator migrator = new RagResponseMigrator();

		// 1. response files saved in the work directory
		int files = migrator.migrateWorkDir(settings);

		// 2. results column of the query table
		int rows = 0;
		for (Query query : queryDBService.findAll()) {
			try {
				String migrated = migrator.migrateJson(query.getResults());
				if (migrated != null) {
					query.setResults(migrated);
					queryDBService.save(query);
					rows++;
				}
			} catch (Exception e) {
				logger.error(e, "could not migrate query -> " + query.getId());
			}
		}

		if (files > 0 || rows > 0)
			startupLogger.info("RagResponse migration -> " + files + " file(s), " + rows + " query row(s) migrated to the new format");
	}
}
