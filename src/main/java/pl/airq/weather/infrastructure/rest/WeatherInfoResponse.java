package pl.airq.weather.infrastructure.rest;

import io.quarkus.runtime.annotations.RegisterForReflection;
import pl.airq.weather.domain.WeatherInfo;

@RegisterForReflection
public class WeatherInfoResponse {

    public final WeatherInfo weatherInfo;

    public WeatherInfoResponse(WeatherInfo weatherInfo) {
        this.weatherInfo = weatherInfo;
    }
}
