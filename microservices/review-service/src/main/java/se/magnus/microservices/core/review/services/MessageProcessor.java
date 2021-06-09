package se.magnus.microservices.core.review.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import se.magnus.api.core.review.Review;
import se.magnus.api.core.review.ReviewService;
import se.magnus.api.event.Event;
import se.magnus.util.exceptions.EventProcessingException;

@EnableBinding(Sink.class)
public class MessageProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessor.class);

    private final ReviewService reviweService;

    public MessageProcessor(ReviewService reviweService) {
        this.reviweService = reviweService;
    }


    @StreamListener(Sink.INPUT)
    private void process(Event<Integer, Review> reviewEvent){
        switch (reviewEvent.getEventType()){
            case CREATE:
                Review product = reviewEvent.getData();
                reviweService.createReview(product);
                break;
            case DELETE:
                int reviewId = reviewEvent.getKey();
                reviweService.deleteReviews(reviewId);
                break;
            default:
                String errorMessage = "Incorrect event type: " + reviewEvent.getEventType() + ", expected a CREATE or DELETE event";
                LOG.warn(errorMessage);
                throw new EventProcessingException(errorMessage);

        }
    }
}
