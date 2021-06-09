package se.magnus.microservices.core.product.services;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import se.magnus.api.core.product.Product;
import se.magnus.microservices.core.product.persistance.ProductEntity;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mappings({
            @Mapping(target = "serviceAddress",ignore = true)
    })
    public Product entityToApi(ProductEntity entity);

    @Mappings({
            @Mapping(target = "id",ignore = true),
            @Mapping(target = "version",ignore = true)
    })
    public ProductEntity apiToEntity(Product product);
}
