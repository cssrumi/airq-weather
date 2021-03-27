package pl.airq.weather.infrastructure.store;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import io.quarkus.redis.client.reactive.ReactiveRedisClient;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.redis.client.Response;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.airq.common.exception.DeserializationException;
import pl.airq.common.exception.SerializationException;
import pl.airq.common.process.AppEventBus;
import pl.airq.common.process.MutinyUtils;
import pl.airq.common.process.failure.FailureFactory;
import pl.airq.common.infrastructure.store.key.TLKey;
import pl.airq.common.infrastructure.store.layer.StoreLayer;
import pl.airq.weather.domain.WeatherInfo;

public class WeatherInfoRedisLayer implements StoreLayer<TLKey, WeatherInfo> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherInfoRedisLayer.class);
    private final ReactiveRedisClient reactiveRedisClient;
    private final AppEventBus bus;
    private final ObjectMapper mapper;
    private final String expireInSeconds;

    public WeatherInfoRedisLayer(ReactiveRedisClient reactiveRedisClient, AppEventBus bus, ObjectMapper mapper, Duration expireIn) {
        this.reactiveRedisClient = reactiveRedisClient;
        this.bus = bus;
        this.mapper = mapper;
        this.expireInSeconds = Long.toString(expireIn.toSeconds());
    }

    @PostConstruct
    void logConfig() {
        LOGGER.info("{} initialized. Cache expire set at: {} seconds", WeatherInfoRedisLayer.class.getSimpleName(), expireInSeconds);
    }

    @Override
    public Uni<WeatherInfo> get(TLKey key) {
        return reactiveRedisClient.get(key.value())
                                  .onItem().ifNotNull().transform(Response::toString)
                                  .onItem().ifNotNull().transform(this::deserializeValue);
    }

    protected Uni<Set<WeatherInfo>> getAllFrom(List<String> rawKeys) {
        return Iterables.isEmpty(rawKeys) ? Uni.createFrom().item(Set.of()) : reactiveRedisClient.mget(rawKeys)
                                                                                                 .map(Response::toString)
                                                                                                 .map(this::deserializeSet);
    }

    @Override
    public Uni<Set<WeatherInfo>> getAll() {
        return getRawKeys().collectItems().asList()
                           .flatMap(this::getAllFrom);
    }

    @Override
    public Uni<Set<WeatherInfo>> getAll(Predicate<WeatherInfo> predicate) {
        return getAll().onItem().transformToMulti(Multi.createFrom()::iterable)
                       .filter(predicate)
                       .collectItems().with(Collectors.toSet());
    }

    protected Multi<String> getRawKeys() {
        return reactiveRedisClient.keys("*")
                                  .onItem().transformToMulti(Multi.createFrom()::iterable)
                                  .map(Response::toString);
    }

    protected Uni<Map<TLKey, WeatherInfo>> getMapFromRawKeys(List<String> rawKeys) {
        return reactiveRedisClient.mget(rawKeys)
                                  .map(Response::toString)
                                  .map(this::deserializeList)
                                  .map(values -> IntStream.range(0, rawKeys.size()).boxed()
                                                          .collect(Collectors.toMap(i -> TLKey.from(rawKeys.get(i)), values::get)));
    }

    @Override
    public Uni<Map<TLKey, WeatherInfo>> getMap() {
        return getRawKeys().collectItems().asList()
                           .flatMap(this::getMapFromRawKeys);
    }

    @Override
    public Uni<Map<TLKey, WeatherInfo>> getMap(Predicate<TLKey> keyPredicate) {
        return getRawKeys().filter(rawKey -> keyPredicate.test(TLKey.from(rawKey)))
                           .collectItems().asList()
                           .flatMap(this::getMapFromRawKeys);
    }

    @Override
    public Uni<WeatherInfo> upsert(TLKey key, WeatherInfo value) {
        return reactiveRedisClient.setex(key.value(), expireInSeconds, serializeValue(value))
                                  .invoke(ignore -> LOGGER.debug("Set key: {}, value: {}", key.value(), value))
                                  .onFailure().invoke(throwable -> bus.publish(FailureFactory.fromThrowable(throwable)))
                                  .map(ignore -> value);
    }

    @Override
    public Uni<Void> delete(TLKey key) {
        return reactiveRedisClient.del(List.of(key.value()))
                                  .onItem().transformToUni(MutinyUtils::ignoreUniResult);
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

    private Set<WeatherInfo> deserializeSet(String rawSet) {
        try {
            return Sets.newHashSet(mapper.readValue(rawSet, WeatherInfo[].class));
        } catch (JsonProcessingException e) {
            LOGGER.warn("Unable to deserialize set {}", rawSet);
            throw new DeserializationException(String.format("Unable to deserialize set %s", rawSet));
        }
    }

    private List<WeatherInfo> deserializeList(String rawList) {
        try {
            return Arrays.asList(mapper.readValue(rawList, WeatherInfo[].class));
        } catch (JsonProcessingException e) {
            LOGGER.warn("Unable to deserialize list {}", rawList);
            throw new DeserializationException(String.format("Unable to deserialize list %s", rawList));
        }
    }
}
