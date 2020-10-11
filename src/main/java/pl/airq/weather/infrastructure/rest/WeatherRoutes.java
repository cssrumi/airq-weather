package pl.airq.weather.infrastructure.rest;

import io.quarkus.vertx.web.Body;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpMethod;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import pl.airq.weather.domain.WeatherQuery;

@RouteBase(path = "/v2/weather", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
public class WeatherRoutes {

    private final WeatherQuery weatherQuery;

    @Inject
    public WeatherRoutes(WeatherQuery weatherQuery) {
        this.weatherQuery = weatherQuery;
    }

    @Route(path = "/info", methods = HttpMethod.POST)
    Uni<WeatherInfoResponse> getWeatherInfo(@Body WeatherInfoRequest request) {
        return weatherQuery.find(request).map(WeatherInfoResponse::new);
    }
}
