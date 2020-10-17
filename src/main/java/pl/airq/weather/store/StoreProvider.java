package pl.airq.weather.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.redis.client.reactive.ReactiveRedisClient;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import pl.airq.common.domain.station.StationQuery;
import pl.airq.common.process.AppEventBus;
import pl.airq.common.store.Store;
import pl.airq.common.store.StoreBuilder;
import pl.airq.common.store.key.TLKey;
import pl.airq.common.vo.StationLocation;
import pl.airq.weather.domain.WeatherInfo;

@Dependent
public class StoreProvider {

    private static final String EXPIRE_IN_CONFIG = "weather.store.expire.in";
    private static final String EXPIRE_TIME_UNIT_CONFIG = "weather.store.expire.timeUnit";

    @Produces
    @ApplicationScoped
    Store<String, StationLocation> locationStore(StationQuery stationQuery) {
        return new StoreBuilder<String, StationLocation>().withGuavaCacheOnTop(Duration.ofDays(1))
                                                          .withFallback(new LocationFallbackLayer(stationQuery))
                                                          .build();
    }

    @Produces
    @ApplicationScoped
    Store<TLKey, WeatherInfo> weatherInfoStore(ReactiveRedisClient redis, AppEventBus bus, ObjectMapper mapper,
                                               @ConfigProperty(name = EXPIRE_IN_CONFIG) Long expireIn,
                                               @ConfigProperty(name = EXPIRE_TIME_UNIT_CONFIG) ChronoUnit expireTimeUnit) {
        final WeatherInfoRedisLayer weatherInfoRedisLayer = new WeatherInfoRedisLayer(redis, bus, mapper, expireIn, expireTimeUnit);
        return new StoreBuilder<TLKey, WeatherInfo>().withGuavaCacheOnTop(Duration.of(expireIn, expireTimeUnit))
                                                     .withLayer(weatherInfoRedisLayer)
                                                     .build();
    }

}
