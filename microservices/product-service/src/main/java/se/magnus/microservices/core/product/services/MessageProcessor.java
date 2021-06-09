package se.magnus.microservices.core.product.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.product.ProductService;
import se.magnus.api.event.Event;
import se.magnus.util.exceptions.EventProcessingException;

@EnableBinding(Sink.class)
public class MessageProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessor.class);

    private final ProductService productService;

    public MessageProcessor(ProductService productService) {
        this.productService = productService;
    }


    @StreamListener(Sink.INPUT)
    private void process(Event<Integer, Product> productEvent){
        switch (productEvent.getEventType()){
            case CREATE:
                Product product = productEvent.getData();
                productService.createProduct(product);
                break;
            case DELETE:
                int productId = productEvent.getKey();
                productService.deleteProduct(productId);
                break;
            default:
                String errorMessage = "Incorrect event type: " + productEvent.getEventType() + ", expected a CREATE or DELETE event";
                LOG.warn(errorMessage);
                throw new EventProcessingException(errorMessage);

        }
    }
}
