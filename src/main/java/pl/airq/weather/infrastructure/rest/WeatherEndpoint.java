package pl.airq.weather.infrastructure.rest;

import io.smallrye.mutiny.Uni;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.airq.weather.domain.WeatherQuery;

@Path("/v1/weather")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WeatherEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherEndpoint.class);
    private final WeatherQuery weatherQuery;

    @Inject
    public WeatherEndpoint(WeatherQuery weatherQuery) {
        this.weatherQuery = weatherQuery;
    }

    @POST
    @Path("/info")
    public Uni<Response> getWeatherInfo(@NotNull @Valid WeatherInfoRequest request) {
        return weatherQuery.find(request)
                           .onItem().transform(info -> Response.ok(info).build());
    }
}
