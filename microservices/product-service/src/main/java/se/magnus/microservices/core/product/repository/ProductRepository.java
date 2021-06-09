package se.magnus.microservices.core.product.repository;


import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import se.magnus.microservices.core.product.persistance.ProductEntity;

import java.util.Optional;

public interface ProductRepository extends ReactiveCrudRepository<ProductEntity,String> {
    Mono<ProductEntity> findByProductId(int productId);
}
