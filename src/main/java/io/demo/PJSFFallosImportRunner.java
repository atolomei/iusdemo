package io.demo;

import java.io.File;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import io.demo.model.User;
import io.demo.model.db.service.PJSFSentenciaDBService;
import io.demo.service.PJSFSentenciaParserService;
import io.demo.service.Settings;

/**
 * One-shot startup import of the PJSF fallos downloaded to the file system
 * ({@code fallo-<sid>.txt} files in the {@code pjsf.fallos.dir} directory) into
 * the database cache of {@link io.demo.model.PJSFSentencia}.
 * <p>
 * Activated with the JVM argument {@code -Dimportfallos=true}. The import is
 * idempotent: fallos already stored in the database are skipped, so it is safe
 * to run on every startup.
 * </p>
 */
@Component
@Order(20)
@ConditionalOnProperty(name = "importfallos", havingValue = "true")
public class PJSFFallosImportRunner implements ApplicationRunner {

	static private Logger logger = Logger.getLogger(PJSFFallosImportRunner.class.getName());
	static private Logger startupLogger = Logger.getLogger("StartupLogger");

	private final Settings settings;
	private final PJSFSentenciaDBService dbService;
	private final PJSFSentenciaParserService parserService;

	public PJSFFallosImportRunner(Settings settings, PJSFSentenciaDBService dbService, PJSFSentenciaParserService parserService) {
		this.settings = settings;
		this.dbService = dbService;
		this.parserService = parserService;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {

		File dir = new File(settings.getFallosDir());
		startupLogger.info("Importing PJSF fallos from -> " + dir.getAbsolutePath());

		try {
			User root = dbService.getUserDBService().findRoot();
			int imported = dbService.importFromFileSystem(dir, parserService, root);
			startupLogger.info("PJSF fallos import -> " + imported + " fallo(s) imported into the database cache");
		} catch (Exception e) {
			logger.error(e, "Error importing PJSF fallos from " + dir.getAbsolutePath());
		}
	}
}
