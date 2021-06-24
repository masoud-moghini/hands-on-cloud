package se.magnus.microservices.core.product.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.product.ProductService;
import se.magnus.microservices.core.product.persistance.ProductEntity;
import se.magnus.microservices.core.product.persistance.ProductRepository;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;

@RestController
public class ProductServiceImpl implements ProductService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ServiceUtil serviceUtil;
    private ProductRepository repository;
    private final ProductMapper mapper;
    @Autowired
    public ProductServiceImpl(
            ServiceUtil serviceUtil,
            ProductRepository productRepository,
            ProductMapper mapper
    ) {
        this.serviceUtil = serviceUtil;
        this.repository = productRepository;
        this.mapper = mapper;
    }

    @Override
    public Mono<Product> getProduct(int productId) {
        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);
        Mono<Product> entity =
                repository.findByProductId(productId).switchIfEmpty(Mono.error(
                        new NotFoundException("No product found for productId: " + productId)
                ))
                .log()
                .map(e->mapper.entityToApi(e))
                .map(e->{e.setServiceAddress(serviceUtil.getServiceAddress());return e;});
        return entity;
    }

    @Override
    public Product createProduct(Product body) {
        try{
            LOG.debug("*******************************************");
            ProductEntity entity = mapper.apiToEntity(body);
            Mono<Product> newEntity = repository.save(entity).log().onErrorMap(
                    DuplicateKeyException.class,
                    ex->new InvalidInputException("Duplicate key, Product Id: " + body.getProductId())
            )
            .map(e->mapper.entityToApi(e));
            return newEntity.block();
        }catch (DuplicateKeyException ex){
            LOG.debug("=====================================");

            throw new InvalidInputException("Duplicate key, Product Id: " +
                    body.getProductId());
        }

    }

    @Override
    public void deleteProduct(int productId) {
        LOG.debug("deleteProduct: tries to delete an entity with productId: {}", productId);
        repository.findByProductId(productId).map(p-> repository.delete(p)).flatMap(e->e).block();
    }
}