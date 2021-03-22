package pl.airq.weather.domain;

import io.smallrye.mutiny.Uni;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import pl.airq.common.exception.ResourceNotFoundException;
import pl.airq.common.process.AppEventBus;
import pl.airq.common.process.failure.FailureFactory;
import pl.airq.common.store.Store;
import pl.airq.common.store.key.TLKey;
import pl.airq.common.vo.StationLocation;
import pl.airq.weather.domain.dto.WeatherInfoRequest;

@ApplicationScoped
public class WeatherQuery {

    private final Store<String, StationLocation> locationStore;
    private final Store<TLKey, WeatherInfo> weatherInfoStore;
    private final AppEventBus eventBus;

    @Inject
    public WeatherQuery(Store<String, StationLocation> locationStore,
                        Store<TLKey, WeatherInfo> weatherInfoStore,
                        AppEventBus eventBus) {
        this.locationStore = locationStore;
        this.weatherInfoStore = weatherInfoStore;
        this.eventBus = eventBus;
    }

    public Uni<WeatherInfo> find(WeatherInfoRequest request) {
        return locationStore.get(request.stationId)
                            .onItem().ifNull().failWith(new ResourceNotFoundException("Station not found."))
                            .onItem().transform(location -> TLKey.from(timestamp(request), location))
                            .onItem().transformToUni(weatherInfoStore::get)
                            .onItem().ifNull().failWith(new ResourceNotFoundException("Weather information not found."))
                            .onFailure().invoke(throwable -> eventBus.publish(FailureFactory.fromThrowable(throwable)));
    }

    private OffsetDateTime timestamp(WeatherInfoRequest request) {
        return OffsetDateTime.ofInstant(Instant.ofEpochSecond(request.timestamp), ZoneOffset.systemDefault());
    }
}
