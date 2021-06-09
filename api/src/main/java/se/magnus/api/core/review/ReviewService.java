package se.magnus.api.core.review;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

public interface ReviewService {

    /**
     * Sample usage: curl $HOST:$PORT/review?productId=1
     *
     * @param productId
     * @return
     */
    @PostMapping(
            value    = "/review",
            consumes = "application/json",
            produces = "application/json")
    Review createReview(@RequestBody Review body);

    /**
     * Sample usage: curl $HOST:$PORT/review?productId=1
     *
     * @param productId
     * @return
     */
    @GetMapping(
            value    = "/review",
            produces = "application/json")
    Flux<Review> getReviews(@RequestParam(value = "productId", required = true) int productId);

    /**
     * Sample usage:
     *
     * curl -X DELETE $HOST:$PORT/review?productId=1
     *
     * @param productId
     */
    @DeleteMapping(value = "/review")
    void deleteReviews(@RequestParam(value = "productId", required = true)  int productId);
}