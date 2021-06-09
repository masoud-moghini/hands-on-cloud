package se.magnus.microservices.core.recommendation.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.recommendation.RecommendationService;
import se.magnus.api.event.Event;
import se.magnus.util.exceptions.EventProcessingException;

@EnableBinding(Sink.class)
public class MessageProcessor {

    private RecommendationService recommendationService;
    Logger log = LoggerFactory.getLogger(MessageProcessor.class);

    public MessageProcessor(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @StreamListener(Sink.INPUT)
    private void process(Event<Integer, Recommendation> recommendationEvent){
        switch (recommendationEvent.getEventType()){
            case CREATE:
                Recommendation product = recommendationEvent.getData();
                recommendationService.createRecommendation(product);
                break;
            case DELETE:
                int productId = recommendationEvent.getKey();
                recommendationService.deleteRecommendations(productId);
                break;
            default:
                String errorMessage = "Incorrect event type: " + recommendationEvent.getEventType() + ", expected a CREATE or DELETE event";
                log.warn(errorMessage);
                throw new EventProcessingException(errorMessage);

        }
    }
}
