package io.demo.model.db.service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import io.demo.Logger;
import io.demo.model.ObjectState;
import io.demo.model.PJSFSentencia;
import io.demo.model.User;
import io.demo.service.PJSFSentenciaParserService;
import io.demo.service.Settings;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;

/**
 * Database operations for {@link PJSFSentencia}.
 */
@Service
public class PJSFSentenciaDBService extends DBService<PJSFSentencia, Long> {

	static private Logger logger = Logger.getLogger(PJSFSentenciaDBService.class.getName());

	/** file name convention of the downloaded fallos: fallo-<sid>.txt */
	private static final String FALLO_FILE_PREFIX = "fallo-";
	private static final String FALLO_FILE_SUFFIX = ".txt";

	public PJSFSentenciaDBService(CrudRepository<PJSFSentencia, Long> repository, Settings settings) {
		super(repository, settings);
	}

	/**
	 * Returns the sentencia for the given PJSF document id (sid), if stored.
	 */
	@Transactional
	public Optional<PJSFSentencia> getByDocumentoId(String documentoId) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<PJSFSentencia> cq = cb.createQuery(PJSFSentencia.class);
		Root<PJSFSentencia> root = cq.from(PJSFSentencia.class);
		cq.select(root).where(cb.equal(root.get("documentoId"), documentoId));
		List<PJSFSentencia> list = getEntityManager().createQuery(cq).setMaxResults(1).getResultList();
		return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
	}

	@Transactional
	public boolean existsByDocumentoId(String documentoId) {
		return getByDocumentoId(documentoId).isPresent();
	}

	/**
	 * Walks the given directory and imports into the database every fallo file
	 * ({@code fallo-<sid>.txt}) that is not already stored. The sid is taken
	 * from the file name. Files containing the PJSF "fallo no existe" error
	 * page are skipped (logged as warning). Idempotent: files whose sid is
	 * already in the database are skipped.
	 *
	 * @return the number of fallos imported
	 */
	public int importFromFileSystem(File dir, PJSFSentenciaParserService parser, User user) {

		if (dir == null || !dir.isDirectory()) {
			logger.warn("Fallos directory does not exist -> " + (dir != null ? dir.getAbsolutePath() : "null"));
			return 0;
		}

		File[] files = dir.listFiles((d, name) -> name.startsWith(FALLO_FILE_PREFIX) && name.endsWith(FALLO_FILE_SUFFIX));
		if (files == null || files.length == 0) {
			logger.info("No fallo files found in -> " + dir.getAbsolutePath());
			return 0;
		}

		int imported = 0;
		int skipped = 0;
		int invalid = 0;

		for (File file : files) {

			String name = file.getName();
			String sid = name.substring(FALLO_FILE_PREFIX.length(), name.length() - FALLO_FILE_SUFFIX.length());

			if (sid.isBlank()) {
				logger.warn("Can not extract sid from file name -> " + name);
				invalid++;
				continue;
			}

			try {
				if (existsByDocumentoId(sid)) {
					skipped++;
					continue;
				}

				String text = Files.readString(file.toPath(), StandardCharsets.UTF_8);
				PJSFSentencia sentencia = parser.parseFile(sid, text);

				if (sentencia == null) {
					logger.warn("File is not a valid fallo (fallo no existe) -> " + name);
					invalid++;
					continue;
				}

				sentencia.setCreated(OffsetDateTime.now());
				sentencia.setState(ObjectState.PUBLISHED);
				sentencia.setLastModifiedUser(user);
				save(sentencia, user);
				
				logger.debug("Imported fallo file -> " + name + ", sid=" + sid);
				
				imported++;

			} catch (Exception e) {
				logger.error(e, "Error importing fallo file -> " + name);
				invalid++;
			}
		}

		logger.info("Fallos import from " + dir.getAbsolutePath() + " -> imported: " + imported + ", skipped (already stored): " + skipped + ", invalid/errors: " + invalid);
		return imported;
	}

	@Override
	public String toJSON() {
		return null;
	}

	@Override
	protected Class<PJSFSentencia> getEntityClass() {
		return PJSFSentencia.class;
	}

	@PostConstruct
	protected void onInitialize() {
		super.register(getEntityClass(), this);
	}

}
