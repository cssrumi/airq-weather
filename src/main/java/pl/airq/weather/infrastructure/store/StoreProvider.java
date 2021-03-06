package pl.airq.weather.infrastructure.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.redis.client.reactive.ReactiveRedisClient;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import pl.airq.common.domain.station.StationQuery;
import pl.airq.common.process.AppEventBus;
import pl.airq.common.infrastructure.store.Store;
import pl.airq.common.infrastructure.store.StoreBuilder;
import pl.airq.common.infrastructure.store.key.TLKey;
import pl.airq.common.domain.vo.StationLocation;
import pl.airq.weather.config.WeatherProperties;
import pl.airq.weather.domain.WeatherInfo;

@Dependent
public class StoreProvider {

    private final WeatherProperties properties;

    @Inject
    public StoreProvider(WeatherProperties properties) {
        this.properties = properties;
    }

    @Produces
    @ApplicationScoped
    Store<String, StationLocation> locationStore(StationQuery stationQuery) {
        return new StoreBuilder<String, StationLocation>().withInMemoryOnTop()
                                                          .withFallback(new LocationFallbackLayer(stationQuery))
                                                          .build();
    }

    @Produces
    @ApplicationScoped
    Store<TLKey, WeatherInfo> weatherInfoStore(ReactiveRedisClient redis, AppEventBus bus, ObjectMapper mapper) {
        final WeatherInfoRedisLayer weatherInfoRedisLayer = new WeatherInfoRedisLayer(
                redis, bus, mapper, properties.getStore().getExpire().duration()
        );
        return new StoreBuilder<TLKey, WeatherInfo>().withGuavaCacheOnTop(properties.getStore().getExpire().duration())
                                                     .withLayer(weatherInfoRedisLayer)
                                                     .build();
    }

}
