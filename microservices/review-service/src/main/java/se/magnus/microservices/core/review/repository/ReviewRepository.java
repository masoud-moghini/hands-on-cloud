package se.magnus.microservices.core.review.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import se.magnus.microservices.core.review.persistance.ReviewEntity;

import java.util.List;

public interface ReviewRepository extends CrudRepository<ReviewEntity,String> {
    @Transactional(readOnly = true)
    List<ReviewEntity> findByProductId(int ProductId);
}
