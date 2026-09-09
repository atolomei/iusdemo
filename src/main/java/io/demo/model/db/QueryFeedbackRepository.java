package io.demo.model.db;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import io.demo.model.QueryFeedback;

@Repository
public interface QueryFeedbackRepository extends CrudRepository<QueryFeedback, Long> {

}
