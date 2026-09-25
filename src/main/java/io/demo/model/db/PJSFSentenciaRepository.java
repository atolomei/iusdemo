package io.demo.model.db;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import io.demo.model.PJSFSentencia;

/**
 * CRUD repository for {@link PJSFSentencia}.
 */
@Repository
public interface PJSFSentenciaRepository extends CrudRepository<PJSFSentencia, Long> {

}
