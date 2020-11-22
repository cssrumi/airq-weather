package pl.airq.weather.rest;

import io.quarkus.vertx.web.Body;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpMethod;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.airq.weather.domain.WeatherQuery;
import pl.airq.weather.rest.dto.WeatherInfoRequest;
import pl.airq.weather.rest.dto.WeatherInfoResponse;

@RouteBase(path = "/v1/weather", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
public class WeatherRoutes {

    private final WeatherQuery weatherQuery;

    @Inject
    public WeatherRoutes(WeatherQuery weatherQuery) {
        this.weatherQuery = weatherQuery;
    }

    @Route(path = "/info", methods = HttpMethod.POST)
    Uni<WeatherInfoResponse> getWeatherInfo(@Body WeatherInfoRequest request) {
        return weatherQuery.find(request).map(weatherInfo -> new WeatherInfoResponse(request.timestamp, weatherInfo));
    }
}
