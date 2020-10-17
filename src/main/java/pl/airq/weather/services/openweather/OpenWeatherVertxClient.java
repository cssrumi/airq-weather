package pl.airq.weather.services.openweather;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class OpenWeatherVertxClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenWeatherVertxClient.class);
    private static final String OPEN_WEATHER_HOST_CONFIG = "weather.open-weather.host";
    private static final String OPEN_WEATHER_PORT_CONFIG = "weather.open-weather.port";
    private static final String OPEN_WEATHER_SSL_CONFIG = "weather.open-weather.ssl";

    private final ObjectMapper mapper;
    private final WebClient client;

    @Inject
    OpenWeatherVertxClient(Vertx vertx, ObjectMapper mapper,
                           @ConfigProperty(name = OPEN_WEATHER_HOST_CONFIG) String host,
                           @ConfigProperty(name = OPEN_WEATHER_PORT_CONFIG, defaultValue = "443") Integer port,
                           @ConfigProperty(name = OPEN_WEATHER_SSL_CONFIG, defaultValue = "true") Boolean ssl) {
        this.mapper = mapper;
        this.client = WebClient.create(vertx, new WebClientOptions().setDefaultHost(host)
                                                                    .setDefaultPort(port)
                                                                    .setSsl(ssl)
                                                                    .setTrustAll(true));
    }

    Uni<OpenWeatherCurrentInfo> getByCoordinates(String longitude, String latitude, String apiKey) {
        return client.get("/data/2.5/weather")
                     .addQueryParam("lon", longitude)
                     .addQueryParam("lat", latitude)
                     .addQueryParam("appid", apiKey)
                     .send()
                     .map(this::deserialize);
    }

    Uni<OpenWeatherCurrentInfo> getByCityAndCountry(String cityCommaCountry, String apiKey) {
        return client.get("/data/2.5/weather")
                     .addQueryParam("q", cityCommaCountry)
                     .addQueryParam("appid", apiKey)
                     .send()
                     .map(this::deserialize);
    }

    private OpenWeatherCurrentInfo deserialize(HttpResponse<?> response) {
        final Response.Status status = Response.Status.fromStatusCode(response.statusCode());
        if (!status.getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            LOGGER.warn("Unhandled status: {}", status);
            throw new RuntimeException();//todo: create valid exception
        }
        try {
            return mapper.readValue(response.bodyAsString(), OpenWeatherCurrentInfo.class);
        } catch (JsonProcessingException e) {
            LOGGER.warn("Unable to deserialize {}. Raw msg: {}", OpenWeatherCurrentInfo.class.getSimpleName(), response.toString());
            throw new RuntimeException();//todo: create valid exception
        }
    }
}
