package pl.airq.weather.domain;

import io.smallrye.mutiny.Uni;

public interface CurrentWeatherService {

    Uni<WeatherInfo> getCurrentWeatherInfoByCityFromPoland(String city);

    Uni<WeatherInfo> getCurrentWeatherInfoByCityAndCountry(String city, String country);

    Uni<WeatherInfo> getCurrentWeatherInfoByCoordinates(String longitude, String latitude);

}
