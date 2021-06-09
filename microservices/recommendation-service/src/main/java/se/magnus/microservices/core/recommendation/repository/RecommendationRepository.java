package se.magnus.microservices.core.recommendation.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import se.magnus.microservices.core.recommendation.persistence.RecommendationEntity;

import java.util.List;

public interface RecommendationRepository extends ReactiveCrudRepository<RecommendationEntity,String> {

    Flux<RecommendationEntity> findByProductId(int productId);

}
