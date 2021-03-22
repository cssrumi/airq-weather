package pl.airq.weather.infrastructure.store;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import pl.airq.common.domain.station.StationQuery;
import pl.airq.common.store.layer.FallbackLayer;
import pl.airq.common.vo.StationId;
import pl.airq.common.vo.StationLocation;

class LocationFallbackLayer extends FallbackLayer<String, StationLocation> {

    private final StationQuery query;

    LocationFallbackLayer(StationQuery query) {
        this.query = query;
    }

    @Override
    public Uni<StationLocation> get(String key) {
        return query.findById(StationId.from(key))
                    .map(station -> station.location);
    }

    @Override
    public Uni<Set<StationLocation>> getAll() {
        return query.findAll()
                    .onItem().transformToMulti(Multi.createFrom()::iterable)
                    .map(station -> station.location)
                    .collectItems().with(Collectors.toSet());
    }

    @Override
    public Uni<Set<StationLocation>> getAll(Predicate<StationLocation> valuePredicate) {
        return query.findAll()
                    .onItem().transformToMulti(Multi.createFrom()::iterable)
                    .map(station -> station.location)
                    .filter(valuePredicate)
                    .collectItems().with(Collectors.toSet());
    }

    @Override
    public Uni<Map<String, StationLocation>> getMap() {
        return query.findAll()
                    .onItem().transformToMulti(Multi.createFrom()::iterable)
                    .collectItems().asMap(station -> station.id.value(), station -> station.location);
    }
}
