package pl.airq.weather.infrastructure.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.redis.client.reactive.ReactiveRedisClient;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.redis.client.Response;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.airq.common.domain.exception.DeserializationException;
import pl.airq.common.domain.exception.SerializationException;
import pl.airq.common.process.MutinyUtils;
import pl.airq.common.store.key.TLKey;
import pl.airq.weather.domain.WeatherCache;
import pl.airq.weather.domain.WeatherInfo;

@ApplicationScoped
public class RedisWeatherCache implements WeatherCache<TLKey> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisWeatherCache.class);
    private static final String EXPIRE_IN_CONFIG = "weather.cache.expire.in";
    private static final String EXPIRE_TIME_UNIT_CONFIG = "weather.cache.expire.timeUnit";
    private final ReactiveRedisClient reactiveRedisClient;
    private final String expireInSeconds;
    private final ObjectMapper mapper;

    @Inject
    public RedisWeatherCache(ReactiveRedisClient reactiveRedisClient,
                             @ConfigProperty(name = EXPIRE_IN_CONFIG) Long expireIn,
                             @ConfigProperty(name = EXPIRE_TIME_UNIT_CONFIG) ChronoUnit expireTimeUnit,
                             ObjectMapper mapper) {
        this.reactiveRedisClient = reactiveRedisClient;
        this.expireInSeconds = Long.toString(Duration.of(expireIn, expireTimeUnit).toSeconds());
        this.mapper = mapper;
    }

    @PostConstruct
    void logConfig() {
        LOGGER.info("{} initialized. Cache expire set at: {} seconds", RedisWeatherCache.class.getSimpleName(), expireInSeconds);
    }

    @Override
    public Uni<Void> delete(TLKey key) {
        return reactiveRedisClient.del(List.of(key.value()))
                                  .onItem().transformToUni(MutinyUtils::ignoreUniResult);
    }

    @Override
    public Uni<Boolean> put(TLKey key, WeatherInfo value) {
        return reactiveRedisClient.setex(key.value(), expireInSeconds, serializeValue(value))
                                  .invoke(ignore -> LOGGER.info("Set key: {}, value: {}", key.value(), value))
                                  .map(ignore -> Boolean.TRUE)
                                  .onFailure().recoverWithItem(Boolean.FALSE);
    }

    @Override
    public Uni<WeatherInfo> get(TLKey key) {
        return reactiveRedisClient.get(key.value())
                                  .onItem().transform(Response::toString)
                                  .onItem().transform(this::deserializeValue);
    }

    private String serializeValue(WeatherInfo value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            LOGGER.warn("Unable to serialize {}", value);
            throw new SerializationException(String.format("Unable to serialize %s", value), e);
        }
    }

    private WeatherInfo deserializeValue(String rawValue) {
        try {
            return mapper.readValue(rawValue, WeatherInfo.class);
        } catch (JsonProcessingException e) {
            LOGGER.warn("Unable to deserialize {}", rawValue);
            throw new DeserializationException(String.format("Unable to deserialize %s", rawValue));
        }
    }
}
