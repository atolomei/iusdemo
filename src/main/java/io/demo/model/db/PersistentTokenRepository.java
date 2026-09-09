package io.demo.model.db;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.demo.model.PersistentToken;

 

/**
 *  <S extends T> S save(S entity);
 *  <S extends T> Iterable<S> saveAll(Iterable<S> entities);
 *  Optional<T> findById(ID id);
 *  boolean existsById(ID id);
 *  Iterable<T> findAll();
 *  Iterable<T> findAllById(Iterable<ID> ids);
 *  long count();
 *  void deleteById(ID id);
 *  void delete(T entity);
 *  void deleteAllById(Iterable<? extends ID> ids);
 *  void deleteAll(Iterable<? extends T> entities);
 *  void deleteAll();
 */
@Repository
public interface PersistentTokenRepository extends CrudRepository<PersistentToken, Long> {

	List<PersistentToken> findByToken(String token);

	List<PersistentToken> findByEntity(String entity);

	List<PersistentToken> findByEntityAndObject(String entity, String object);

	@Query("SELECT t FROM PersistentToken t WHERE t.entity = :entity AND t.object = :object AND t.token = :token")
	List<PersistentToken> findByEntityObjectAndToken(@Param("entity") String entity,
	                                                  @Param("object") String object,
	                                                  @Param("token") String token);

}
