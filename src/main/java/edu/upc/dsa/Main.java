package edu.upc.dsa;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;
import java.util.concurrent.CountDownLatch;

public class Main {
	public static final String BASE_URI = "http://localhost:8080/dsaApp/";
	private static final Logger logger = Logger.getLogger(Main.class);

	public static HttpServer startServer() {
		logger.info("startServer start: baseUri=" + BASE_URI);
		ResourceConfig config = new ResourceConfig().packages("edu.upc.dsa.services");
		config.register(ApiListingResource.class);
		config.register(SwaggerSerializers.class);

		BeanConfig swaggerConfig = new BeanConfig();
		swaggerConfig.setHost("localhost:8080");
		swaggerConfig.setBasePath("/dsaApp");
		swaggerConfig.setResourcePackage("edu.upc.dsa.services");
		swaggerConfig.setTitle("Drone Fleet API");
		swaggerConfig.setDescription("REST API for managing the drone fleet");
		swaggerConfig.setVersion("1.0.0");
		swaggerConfig.setScan(true);

		HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), config);
		StaticHttpHandler staticHandler = new StaticHttpHandler("./public/");
		server.getServerConfiguration().addHttpHandler(staticHandler, "/");

		StaticHttpHandler swaggerAssets = new StaticHttpHandler(
				"./target/swagger-ui/META-INF/resources/webjars/swagger-ui/3.52.5/");
		server.getServerConfiguration().addHttpHandler(swaggerAssets, "/swagger-ui");
		logger.info("startServer end: baseUri=" + BASE_URI);
		return server;
	}

	public static void main(String[] args) throws InterruptedException {
		final HttpServer server = startServer();
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				server.shutdownNow();
			}
		}));
		logger.info("Drone fleet REST service listening at " + BASE_URI);
		new CountDownLatch(1).await();
	}
}