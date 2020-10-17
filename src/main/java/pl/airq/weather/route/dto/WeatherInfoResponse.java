package pl.airq.weather.route.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import pl.airq.weather.domain.WeatherInfo;

@RegisterForReflection
public class WeatherInfoResponse {

    public final Long timestamp;
    public final WeatherInfo weatherInfo;

    public WeatherInfoResponse(Long timestamp, WeatherInfo weatherInfo) {
        this.timestamp = timestamp;
        this.weatherInfo = weatherInfo;
    }
}
