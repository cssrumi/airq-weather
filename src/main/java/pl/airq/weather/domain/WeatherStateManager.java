package pl.airq.weather.domain;

import com.google.common.base.Preconditions;
import io.quarkus.runtime.configuration.ConfigurationException;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Multi;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.airq.common.store.Store;
import pl.airq.common.store.key.TLKey;
import pl.airq.common.vo.StationLocation;
import pl.airq.weather.config.WeatherProperties;
import pl.airq.weather.store.StoreReady;

@ApplicationScoped
public class WeatherStateManager {

    private static final Set<ChronoUnit> VALID_CHRONO_UNITS = Set.of(ChronoUnit.HOURS, ChronoUnit.MINUTES, ChronoUnit.SECONDS);
    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherStateManager.class);
    private final AtomicBoolean isStoreReady = new AtomicBoolean(false);
    private final ChronoUnit roundToUnit;
    private final CurrentWeatherService weatherService;
    private final Store<String, StationLocation> locationStore;
    private final Store<TLKey, WeatherInfo> weatherStore;
    private final ExecutorService executor;

    @Inject
    public WeatherStateManager(WeatherProperties properties,
                               CurrentWeatherService weatherService,
                               Store<String, StationLocation> locationStore,
                               Store<TLKey, WeatherInfo> weatherStore) {
        Preconditions.checkArgument(VALID_CHRONO_UNITS.contains(properties.getRound().getToUnit()),
                "Round configuration is invalid. Allowed values: " + VALID_CHRONO_UNITS);
        this.roundToUnit = properties.getRound().getToUnit();
        this.weatherService = weatherService;
        this.locationStore = locationStore;
        this.weatherStore = weatherStore;
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    void storeReady(@ObservesAsync StoreReady storeReady) {
        isStoreReady.set(true);
        LOGGER.info("{} handled. {} started...", storeReady.getClass().getSimpleName(), WeatherStateManager.class.getSimpleName());
    }

    @Scheduled(cron = "{weather.pull.cron}")
    void invokePull() {
        if (isStoreReady.get()) {
            pullWeatherDataIntoStore();
        }
    }

    void pullWeatherDataIntoStore() {
        final OffsetDateTime timestamp = roundCurrentTimestamp();
        LOGGER.info("Pulling weather with '{}' timestamp", timestamp);
        int size = locationStore.getAll()
                                .emitOn(executor)
                                .onItem().transformToMulti(Multi.createFrom()::iterable)
                                .flatMap(location -> findAndUpsertWeatherInfo(location, timestamp))
                                .collectItems().asList()
                                .await().atMost(Duration.ofMinutes(1))
                                .size();

        LOGGER.info("Weather info for {} stations have been put into cache", size);
    }

    private Multi<WeatherInfo> findCurrentWeatherInfo(StationLocation location) {
        return weatherService.getCurrentWeatherInfoByCoordinates(location.getLon().toString(), location.getLat().toString())
                             .emitOn(executor).toMulti();
    }

    private Multi<WeatherInfo> findAndUpsertWeatherInfo(StationLocation location, OffsetDateTime timestamp) {
        return findCurrentWeatherInfo(location).flatMap(weatherInfo -> weatherStore.upsert(TLKey.from(timestamp, location), weatherInfo).toMulti());
    }

    private OffsetDateTime roundCurrentTimestamp() {
        OffsetDateTime now = OffsetDateTime.now();
        switch (roundToUnit) {
            case HOURS:
                now = now.minusMinutes(now.getMinute());
            case MINUTES:
                now = now.minusSeconds(now.getSecond());
            case SECONDS:
                return now;
            default:
                throw new ConfigurationException("Invalid round configuration: " + roundToUnit);
        }
    }
}
