package se.magnus.microservices.core.review;

import io.netty.util.internal.logging.InternalLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.Executors;

@SpringBootApplication
@ComponentScan("se.magnus")
public class ReviewServiceApplication {

	private final Integer connectionPoolSize;
	private Logger LOG = LoggerFactory.getLogger(ReviewServiceApplication.class);

	public ReviewServiceApplication(
			@Value("${spring.datasource.maximum-pool-size:10}") Integer connectionPoolSize
	){
		this.connectionPoolSize = connectionPoolSize;
	}

	@Bean
	public Scheduler jdbcScheduler(){
		LOG.info("Creates a jdbcScheduler with connectionPoolSize = " +
				connectionPoolSize);
		return Schedulers.fromExecutor(Executors.newFixedThreadPool(this.connectionPoolSize));
	}

	public static void main(String[] args) {
		SpringApplication.run(ReviewServiceApplication.class, args);
	}
}
