package se.magnus.microservices.core.product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(basePackageClasses = ProductServiceApplication.class)

@ComponentScan("se.magnus")
public class ProductServiceApplication {

	public static void main(String[] args) {
		Logger log = LoggerFactory.getLogger(ProductServiceApplication.class);
		ConfigurableApplicationContext ctx =  SpringApplication.run(ProductServiceApplication.class, args);
		String MongodbHost = ctx.getEnvironment().getProperty("spring.data.mongodb.host");
		String MongodbPort = ctx.getEnvironment().getProperty("spring.data.mongodb.port");
		log.info("connecting to host : "+MongodbHost +" and port : "+ MongodbPort);

	}
}
