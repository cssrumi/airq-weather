package pl.airq.weather.domain;

import io.smallrye.mutiny.Uni;
import pl.airq.common.store.key.Key;

public interface WeatherCache<K extends Key> {

    Uni<Void> delete(K key);

    Uni<Boolean> put(K key, WeatherInfo value);

    Uni<WeatherInfo> get(K key);

}
