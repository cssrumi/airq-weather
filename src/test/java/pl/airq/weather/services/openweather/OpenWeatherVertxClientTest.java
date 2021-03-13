package pl.airq.weather.services.openweather;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;
import pl.airq.common.domain.station.StationQuery;
import pl.airq.common.store.Store;
import pl.airq.common.vo.StationLocation;
import pl.airq.weather.domain.WeatherStateManager;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTestResource(OpenWeatherApiMockResource.class)
@QuarkusTest
class OpenWeatherVertxClientTest {

    private static final String URI = "/data/2.5/weather";
    private final WireMockServer server = OpenWeatherApiMockResource.server();

    @InjectMock
    WeatherStateManager stateManager;
    @InjectMock
    StationQuery stationQuery;
    @Inject
    Store<String, StationLocation> locationStore;

    @ConfigProperty(name = "weather.open-weather.api-key")
    String apiKey;

    @Inject
    OpenWeatherVertxClient client;

    @Inject
    ObjectMapper mapper;

    @Test
    void getByCoordinates() throws JsonProcessingException {
        String validResponseScenario = "VALID_RESPONSE";
        String lon = "5";
        String lat = "6";
        OpenWeatherCurrentInfo expectedResponse = apiResponse(lon, lat);
        stubFor(get(urlPathEqualTo(URI)).withQueryParam("lon", equalTo(lon))
                                        .withQueryParam("lat", equalTo(lat))
                                        .withQueryParam("appid", equalTo(apiKey))
                                        .inScenario(Scenario.STARTED)
                                        .willSetStateTo(validResponseScenario)
                                        .willReturn(aResponse().withStatus(500)));
        stubFor(get(urlPathEqualTo(URI)).withQueryParam("lon", equalTo(lon))
                                        .withQueryParam("lat", equalTo(lat))
                                        .withQueryParam("appid", equalTo(apiKey))
                                        .inScenario(validResponseScenario)
                                        .willReturn(aResponse().withBody(mapper.writeValueAsString(expectedResponse))));

        final OpenWeatherCurrentInfo result = client.getByCoordinates(lon, lat, apiKey).await().atMost(Duration.ofSeconds(1));

        assertThat(result).isNotNull();
        assertThat(result.coord).isNotNull();
        assertThat(result.coord.lat).isEqualTo(expectedResponse.coord.lat);
        assertThat(result.coord.lon).isEqualTo(expectedResponse.coord.lon);
        assertThat(result.main).isNotNull();
        assertThat(result.main.humidity).isEqualTo(expectedResponse.main.humidity);
        assertThat(result.main.pressure).isEqualTo(expectedResponse.main.pressure);
        assertThat(result.main.temp).isEqualTo(expectedResponse.main.temp);
        assertThat(result.wind).isNotNull();
        assertThat(result.wind.deg).isEqualTo(expectedResponse.wind.deg);
        assertThat(result.wind.speed).isEqualTo(expectedResponse.wind.speed);
    }

    @Test
    void getByCityAndCountry() {
    }

    private OpenWeatherCurrentInfo apiResponse(String lon, String lat) {
        final OpenWeatherCurrentInfo dto = new OpenWeatherCurrentInfo();
        dto.dt = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        dto.main = new OpenWeatherCurrentInfo.Main();
        dto.main.humidity = 5.0f;
        dto.main.pressure = 5.0f;
        dto.main.temp = 5.0f;
        dto.wind = new OpenWeatherCurrentInfo.Wind();
        dto.wind.deg = 5.0f;
        dto.wind.speed = 5.0f;
        dto.coord = new OpenWeatherCurrentInfo.Coordinates();
        dto.coord.lat = lat;
        dto.coord.lon = lon;
        return dto;
    }
}
