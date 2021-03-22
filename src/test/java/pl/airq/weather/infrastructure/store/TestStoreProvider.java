package pl.airq.weather.infrastructure.store;

import io.quarkus.test.Mock;
import io.smallrye.mutiny.Uni;
import java.util.Collections;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import org.mockito.Mockito;
import pl.airq.common.store.Store;
import pl.airq.common.store.key.TLKey;
import pl.airq.common.vo.StationLocation;
import pl.airq.weather.domain.WeatherInfo;

import static org.mockito.Mockito.when;

@Dependent
public class TestStoreProvider {

    @Produces
    @Mock
    Store<String, StationLocation> locationStore() {
        Store<String, StationLocation> locationStore = (Store<String, StationLocation>) Mockito.mock(Store.class);
        when(locationStore.pullMap()).thenReturn(Uni.createFrom().item(Collections::emptyMap));
        return locationStore;
    }

    @Produces
    @Mock
    Store<TLKey, WeatherInfo> weatherInfoStore() {
        return (Store<TLKey, WeatherInfo>) Mockito.mock(Store.class);
    }

}
