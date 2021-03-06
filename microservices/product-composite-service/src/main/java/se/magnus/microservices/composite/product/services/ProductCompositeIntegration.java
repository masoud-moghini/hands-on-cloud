package se.magnus.microservices.composite.product.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.product.ProductService;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.recommendation.RecommendationService;
import se.magnus.api.core.review.Review;
import se.magnus.api.core.review.ReviewService;
import se.magnus.api.event.Event;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.HttpErrorInfo;

import java.io.IOException;

import static reactor.core.publisher.Flux.empty;



@EnableBinding(ProductCompositeIntegration.MessageSources.class)
@Component
public class ProductCompositeIntegration implements RecommendationService ,ProductService,ReviewService  {

    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper mapper;

    private final String productServiceUrl = "http://product/";
    private final String recommendationServiceUrl = "http://recommendation/";
    private final String reviewServiceUrl = "http://review/";

    private  WebClient webClient;
    private MessageSources messageSources;
    public interface MessageSources{
        String OUTPUT_PRODUCTS = "output-products";
        String OUTPUT_RECOMMENDATIONS = "output-recommendations";
        String OUTPUT_REVIEWS = "output-reviews";

        @Output(OUTPUT_PRODUCTS)
        MessageChannel ouputProducts();


        @Output(OUTPUT_REVIEWS)
        MessageChannel outputReviews();

        @Output(OUTPUT_RECOMMENDATIONS)
        MessageChannel outputRecommendations();

    }

    @Autowired
    public ProductCompositeIntegration(
        WebClient.Builder webClient,
        ObjectMapper mapper,
        MessageSources messageSources){


        this.webClientBuilder = webClient;
        this.mapper = mapper;
        this.messageSources = messageSources;

    }

    public Mono<Health> getProductHealth() {return  getHealth(productServiceUrl);}

    public Mono<Health> getRecommendationHealth() {
        return getHealth(recommendationServiceUrl);
    }

    public Mono<Health> getReviewHealth() {
        return getHealth(reviewServiceUrl);
    }

    private Mono<Health> getHealth(String url) {
        url += "/actuator/health";
        LOG.debug("Will call the Health API on URL: {}", url);
        return getWebClient().get().uri(url).retrieve().bodyToMono(String.class)
                .map(s -> new Health.Builder().up().build())
                .onErrorResume(ex -> Mono.just(new Health.Builder().down(ex).build()))
                .log();
    }

    private Throwable handleException(Throwable ex) {

        if (!(ex instanceof WebClientResponseException)) {
            LOG.warn("Got a unexpected error: {}, will rethrow it", ex.toString());
            return ex;
        }

        WebClientResponseException wcre = (WebClientResponseException)ex;

        switch (wcre.getStatusCode()) {

            case NOT_FOUND:
                return new NotFoundException(getErrorMessage(wcre));

            case UNPROCESSABLE_ENTITY :
                return new InvalidInputException(getErrorMessage(wcre));

            default:
                LOG.warn("Got a unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
                LOG.warn("Error body: {}", wcre.getResponseBodyAsString());
                return ex;
        }
    }

    public Mono<Product> getProduct(int productId) {

        String url = productServiceUrl +"/product/"+ productId;
        LOG.debug("Will call getProduct API on URL: {}", url);

        Mono<Product> product = getWebClient().get().uri(url).retrieve().bodyToMono(Product.class).log()
                .onErrorMap(WebClientResponseException.class, ex -> handleHttpClientException(ex));

        return product;

    }

    private WebClient getWebClient() {
        if (webClient == null){
            webClient = webClientBuilder.build();
        }
        return this.webClient;
    }

    @Override
    public Product createProduct(Product body) {
        this.messageSources.ouputProducts().send(MessageBuilder.withPayload(new Event(Event.Type.CREATE,body.getProductId(),body)).build());
        return body;
    }

    @Override
    public void deleteProduct(int productId) {
        this.messageSources.ouputProducts().send(MessageBuilder.withPayload(new Event(Event.Type.DELETE,productId,null)).build());
    }

    private String getErrorMessage(WebClientResponseException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioex) {
            return ex.getMessage();
        }
    }

    @Override
    public Recommendation createRecommendation(Recommendation body) {
        this.messageSources.outputRecommendations().send(MessageBuilder.withPayload(new Event(Event.Type.CREATE,body.getRecommendationId(),body)).build());
        return body;
    }

    public Flux<Recommendation> getRecommendations(int productId) {
        String url = recommendationServiceUrl + "?productId=" + productId;
        LOG.debug("Will call the getRecommendations API on URL: {}", url);
        Flux<Recommendation> recommendations = getWebClient().get().uri(url).retrieve().bodyToFlux(Recommendation.class)
                .log()
                .onErrorMap(WebClientResponseException.class,ex->handleHttpClientException(ex));

        LOG.debug("Found {} recommendations for a product with id: {}", recommendations.count(), productId);
        return recommendations;
    }

    @Override
    public void deleteRecommendations(int productId) {
        this.messageSources.outputRecommendations().send(MessageBuilder.withPayload(new Event(Event.Type.DELETE,productId,null)).build());
    }

    @Override
    public Review createReview(Review body) {
        this.messageSources.outputReviews().send(MessageBuilder.withPayload(new Event(Event.Type.CREATE,body.getReviewId(),body)).build());
        return body;
    }

    public Flux<Review> getReviews(int productId) {
            String url = reviewServiceUrl + productId;
            LOG.debug("Will call getReviews API on URL: {}", url);
            Flux<Review> reviews = getWebClient().get().uri(url).retrieve().bodyToFlux(Review.class).onErrorResume(error -> empty());

            LOG.debug("Found {} reviews for a product with id: {}", reviews.count(), productId);
            return reviews;
    }

    @Override
    public void deleteReviews(int productId) {
        this.messageSources.outputReviews().send(MessageBuilder.withPayload(new Event(Event.Type.DELETE,productId,null)).build());
    }

    private RuntimeException handleHttpClientException(WebClientResponseException ex) {
        switch (ex.getStatusCode()) {

            case NOT_FOUND:
                return new NotFoundException(getErrorMessage(ex));

            case UNPROCESSABLE_ENTITY :
                return new InvalidInputException(getErrorMessage(ex));

            default:
                LOG.warn("Got a unexpected HTTP error: {}, will rethrow it", ex.getStatusCode());
                LOG.warn("Error body: {}", ex.getResponseBodyAsString());
                return ex;
        }
    }

}
