package pl.airq.weather.infrastructure.services.openweather;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.airq.weather.config.WeatherProperties;

@ApplicationScoped
public class OpenWeatherVertxClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenWeatherVertxClient.class);

    private final ObjectMapper mapper;
    private final WebClient client;

    @Inject
    OpenWeatherVertxClient(WeatherProperties properties, Vertx vertx, ObjectMapper mapper) {
        this.mapper = mapper;
        this.client = WebClient.create(vertx, new WebClientOptions().setDefaultHost(properties.getOpenWeather().getHost())
                                                                    .setDefaultPort(properties.getOpenWeather().getPort())
                                                                    .setSsl(properties.getOpenWeather().getSsl())
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
