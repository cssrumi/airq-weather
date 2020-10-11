package pl.airq.weather.domain;

import io.quarkus.runtime.configuration.ConfigurationException;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Multi;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.airq.common.domain.station.Station;
import pl.airq.common.domain.station.StationQuery;
import pl.airq.common.store.key.TLKey;
import pl.airq.common.vo.StationLocation;

@ApplicationScoped
public class CurrentWeatherServiceFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(CurrentWeatherServiceFacade.class);
    private final ChronoUnit roundToUnit;
    private final CurrentWeatherService weatherService;
    private final StationQuery stationQuery;
    private final WeatherCache<TLKey> weatherCache;
    private final ExecutorService executor;

    @Inject
    public CurrentWeatherServiceFacade(@ConfigProperty(name = "weather.round.toUnit") ChronoUnit roundToUnit,
                                       CurrentWeatherService weatherService,
                                       StationQuery stationQuery,
                                       WeatherCache<TLKey> weatherCache) {
        this.roundToUnit = roundToUnit;
        this.weatherService = weatherService;
        this.stationQuery = stationQuery;
        this.weatherCache = weatherCache;
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @PostConstruct
    void validate() {
        if (roundToUnit == null) {
            throw new InvalidConfigurationException("Round configuration is empty.");
        }
        if (roundToUnit != ChronoUnit.HOURS && roundToUnit != ChronoUnit.MINUTES && roundToUnit != ChronoUnit.SECONDS) {
            throw new InvalidConfigurationException("Round configuration is invalid: " + roundToUnit);
        }
    }

    @Scheduled(cron = "{weather.pull.cron}")
    void pullWeatherDataIntoCache() {
        final OffsetDateTime timestamp = roundCurrentTimestamp();
        LOGGER.info("Pulling weather with '{}' timestamp", timestamp);
        int size = stationQuery.findAll().emitOn(executor)
                               .toMulti()
                               .flatMap(stations -> Multi.createFrom().iterable(stations))
                               .flatMap(station -> findCurrentWeatherInfo(station)
                                       .flatMap(weatherInfo -> putWeatherInfoIntoCache(weatherInfo, station.location, timestamp)))
                               .collectItems().asList()
                               .await().atMost(Duration.ofMinutes(1))
                               .size();
        LOGGER.info("Weather info for {} stations have been put into cache", size);
    }

    private Multi<WeatherInfo> findCurrentWeatherInfo(Station station) {
        return weatherService.getCurrentWeatherInfoByCoordinates(station.location.getLon().toString(), station.location.getLat().toString())
                             .emitOn(executor).toMulti();
    }

    private Multi<Boolean> putWeatherInfoIntoCache(WeatherInfo weatherInfo, StationLocation location, OffsetDateTime timestamp) {
        return weatherCache.put(TLKey.from(timestamp, location), weatherInfo).toMulti();
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
