package pl.airq.weather.rest.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import javax.validation.constraints.NotNull;

@RegisterForReflection
public class WeatherInfoRequest {

    @NotNull
    public Long timestamp;
    @NotNull
    public String stationId;

}
