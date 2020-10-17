package pl.airq.weather.store;

import io.quarkus.runtime.StartupEvent;
import java.time.Duration;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.airq.common.store.Store;
import pl.airq.common.vo.StationLocation;

@ApplicationScoped
public class StoreLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreLoader.class);
    private final Store<String, StationLocation> locationStore;
    private final Event<StoreReady> storeReadyEvent;

    @Inject
    StoreLoader(Store<String, StationLocation> locationStore, Event<StoreReady> storeReadyEvent) {
        this.locationStore = locationStore;
        this.storeReadyEvent = storeReadyEvent;
    }

    void load(@Observes StartupEvent event) {
        final int loadedEntries = locationStore.pullMap().await().atMost(Duration.ofSeconds(60)).size();

        LOGGER.info("{} entries loaded to location store.", loadedEntries);
        storeReadyEvent.fireAsync(new StoreReady());
    }

}
