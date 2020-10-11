package pl.airq.weather.domain;

public interface WeatherConverter<T> {

    WeatherInfo toCurrentWeatherInfo(T dto);
}
