package pl.airq.weather.config;

import io.quarkus.arc.config.ConfigProperties;
import java.time.temporal.ChronoUnit;
import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import pl.airq.common.config.properties.ExpireIn;
import pl.airq.common.config.properties.RestService;

@ConfigProperties(prefix = "weather")
public class WeatherProperties {

    @NotNull
    private Store store;

    @NotNull
    private Round round;

    @NotNull
    private WeatherProperties.OpenWeather openWeather;

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public Round getRound() {
        return round;
    }

    public void setRound(Round round) {
        this.round = round;
    }

    public OpenWeather getOpenWeather() {
        return openWeather;
    }

    public void setOpenWeather(OpenWeather openWeather) {
        this.openWeather = openWeather;
    }

    public static class Store {

        @NotNull
        private ExpireIn expire;

        public ExpireIn getExpire() {
            return expire;
        }

        public void setExpire(ExpireIn expire) {
            this.expire = expire;
        }
    }

    public static class Round {

        @NotNull
        private ChronoUnit toUnit;

        public ChronoUnit getToUnit() {
            return toUnit;
        }

        public void setToUnit(ChronoUnit toUnit) {
            this.toUnit = toUnit;
        }
    }

    public static class OpenWeather extends RestService {

        @Nullable
        private String apiKey;

        @Nullable
        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(@Nullable String apiKey) {
            this.apiKey = apiKey;
        }
    }
}
