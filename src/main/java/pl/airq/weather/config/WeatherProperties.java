package pl.airq.weather.config;

import io.quarkus.arc.config.ConfigProperties;
import java.time.temporal.ChronoUnit;
import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import pl.airq.common.config.properties.ExpireIn;

@ConfigProperties(prefix = "weather")
public class WeatherProperties {

    @NotNull
    private Store store;

    @NotNull
    private Round round;

    @NotNull
    private RestService openWeather;

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

    public RestService getOpenWeather() {
        return openWeather;
    }

    public void setOpenWeather(RestService openWeather) {
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

    public static class RestService {

        @NotEmpty
        private String host;

        @Positive
        private Integer port = 443;

        @NotNull
        private Boolean ssl = true;

        @Nullable
        private String apiKey;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public Boolean getSsl() {
            return ssl;
        }

        public void setSsl(Boolean ssl) {
            this.ssl = ssl;
        }

        @Nullable
        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(@Nullable String apiKey) {
            this.apiKey = apiKey;
        }
    }
}
