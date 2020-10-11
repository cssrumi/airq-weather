package pl.airq.weather.domain;

import io.smallrye.mutiny.Uni;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import pl.airq.common.domain.exception.ResourceNotFoundException;
import pl.airq.common.domain.station.StationQuery;
import pl.airq.common.store.key.TLKey;
import pl.airq.common.vo.StationId;
import pl.airq.weather.infrastructure.rest.WeatherInfoRequest;

@ApplicationScoped
public class WeatherQuery {

    private final WeatherCache<TLKey> weatherCache;
    private final StationQuery stationQuery;

    @Inject
    public WeatherQuery(WeatherCache<TLKey> weatherCache, StationQuery stationQuery) {
        this.weatherCache = weatherCache;
        this.stationQuery = stationQuery;
    }

    public Uni<WeatherInfo> find(WeatherInfoRequest request) {
        return stationQuery.findById(StationId.from(request.stationId))
                           .onItem().ifNull().failWith(new ResourceNotFoundException("Station not found."))
                           .onItem().transform(station -> station.location)
                           .onItem().transform(location -> TLKey.from(timestamp(request), location))
                           .onItem().transformToUni(weatherCache::get)
                           .onItem().ifNull().failWith(new ResourceNotFoundException("Weather information not found."));
    }

    private OffsetDateTime timestamp(WeatherInfoRequest request) {
        return OffsetDateTime.ofInstant(Instant.ofEpochSecond(request.timestamp), ZoneOffset.systemDefault());
    }
}
