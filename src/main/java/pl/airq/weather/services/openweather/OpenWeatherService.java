package pl.airq.weather.services.openweather;

import io.smallrye.mutiny.Uni;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import pl.airq.weather.domain.CurrentWeatherService;
import pl.airq.weather.domain.WeatherInfo;

@ApplicationScoped
class OpenWeatherService implements CurrentWeatherService {

    private final String apiKey;
    private final OpenWeatherVertxClient client;
    private final OpenWeatherConverter converter;

    @Inject
    public OpenWeatherService(@ConfigProperty(name = "weather.open-weather.api-key") String apiKey,
                              OpenWeatherVertxClient client) {
        this.apiKey = apiKey;
        this.client = client;
        this.converter = new OpenWeatherConverter();
    }

    @Override
    public Uni<WeatherInfo> getCurrentWeatherInfoByCityAndCountry(String city, String country) {
        return client.getByCityAndCountry(city + "," + country, apiKey)
                     .map(converter::toCurrentWeatherInfo);
    }

    @Override
    public Uni<WeatherInfo> getCurrentWeatherInfoByCityFromPoland(String city) {
        return client.getByCityAndCountry(city + ",PL", apiKey)
                     .map(converter::toCurrentWeatherInfo);
    }

    @Override
    public Uni<WeatherInfo> getCurrentWeatherInfoByCoordinates(String longitude, String latitude) {
        return client.getByCoordinates(longitude, latitude, apiKey)
                     .map(converter::toCurrentWeatherInfo);
    }
}
