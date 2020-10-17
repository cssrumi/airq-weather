package pl.airq.weather.config;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.airq.common.infrastructure.route.RoutesRequestLogger;

@Dependent
public class RoutesConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoutesConfig.class);

    void configureRequestLogger(@Observes Router router) {
        router.route().order(0).handler(BodyHandler.create());
        router.route().order(1).path("/*").handler(RoutesRequestLogger.create());
        LOGGER.info("Router logger configured");
    }
}
