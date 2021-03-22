package pl.airq.weather.infrastructure.services.openweather;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.Map;

public class OpenWeatherApiMockResource implements QuarkusTestResourceLifecycleManager {

    private static final String OPEN_WEATHER_HOST_CONFIG = "weather.open-weather.host";
    private static final String OPEN_WEATHER_PORT_CONFIG = "weather.open-weather.port";
    private static final String OPEN_WEATHER_SSL_CONFIG = "weather.open-weather.ssl";
    private static final String OPEN_WEATHER_API_KEY_CONFIG = "weather.open-weather.api-key";

    private static final WireMockServer WIRE_MOCK_SERVER = new WireMockServer(WireMockConfiguration.wireMockConfig()
                                                                                                   .notifier(new ConsoleNotifier(true)));


    @Override
    public Map<String, String> start() {
        WIRE_MOCK_SERVER.start();
        return Map.of(
                OPEN_WEATHER_HOST_CONFIG, "localhost",
                OPEN_WEATHER_PORT_CONFIG, Integer.toString(WIRE_MOCK_SERVER.port()),
                OPEN_WEATHER_SSL_CONFIG, Boolean.toString(WIRE_MOCK_SERVER.baseUrl().contains("https")),
                OPEN_WEATHER_API_KEY_CONFIG, "APIKEY"
        );
    }

    @Override
    public void stop() {
        WIRE_MOCK_SERVER.stop();
    }

    public static WireMockServer server() {
        return WIRE_MOCK_SERVER;
    }
}
