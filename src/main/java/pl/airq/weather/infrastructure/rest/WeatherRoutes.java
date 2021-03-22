package pl.airq.weather.infrastructure.rest;

import io.quarkus.vertx.web.Body;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import pl.airq.weather.domain.WeatherQuery;
import pl.airq.weather.domain.dto.WeatherInfoRequest;
import pl.airq.weather.domain.dto.WeatherInfoResponse;

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

    @Route(path = "/test", methods = HttpMethod.GET, consumes = MediaType.WILDCARD)
    void throwRuntimeEx(RoutingContext context) {
        context.fail(new RuntimeException("MY EX"));
    }
}
