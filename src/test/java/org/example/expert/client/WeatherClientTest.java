package org.example.expert.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.example.expert.client.dto.WeatherDto;
import org.example.expert.domain.common.exception.ServerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class WeatherClientTest {
    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Mock
    private RestTemplate restTemplate;

    private WeatherClient weatherClient;

    private String currentDate;

    @BeforeEach
    void setUp() {
        given(restTemplateBuilder.build()).willReturn(restTemplate);
        weatherClient = new WeatherClient(restTemplateBuilder);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        currentDate = LocalDate.now().format(formatter);
    }

    @Nested
    class 오늘의_날씨_가져오기_테스트 {
        @Test
        void 오늘의_날씨를_가져온다() {
            // given
            WeatherDto weatherDto = new WeatherDto(currentDate, "weather");
            WeatherDto[] weatherDtos = new WeatherDto[] {weatherDto};
            ResponseEntity<WeatherDto[]> resps = ResponseEntity.ok(weatherDtos);

            given(restTemplate.getForEntity(any(URI.class), eq(WeatherDto[].class)))
                    .willReturn(resps);

            // when
            String weather = weatherClient.getTodayWeather();

            // then
            assertNotNull(weather);
            assertEquals("weather", weather);
        }

        @Test
        void API요청이_실패하면_실패한다() {
            // given
            WeatherDto weatherDto = new WeatherDto(currentDate, "weather");
            WeatherDto[] weatherDtos = new WeatherDto[] {weatherDto};
            ResponseEntity<WeatherDto[]> resps =
                    ResponseEntity.status(HttpStatus.NOT_FOUND).body(weatherDtos);

            given(restTemplate.getForEntity(any(URI.class), eq(WeatherDto[].class)))
                    .willReturn(resps);

            // when & then
            assertThrows(ServerException.class, () -> weatherClient.getTodayWeather());
        }

        @Test
        void 날씨데이터가_없으면_실패한다() {
            // given
            WeatherDto[] weatherDtos = new WeatherDto[] {};
            ResponseEntity<WeatherDto[]> resps = ResponseEntity.ok(weatherDtos);

            given(restTemplate.getForEntity(any(URI.class), eq(WeatherDto[].class)))
                    .willReturn(resps);

            // when & then
            assertThrows(ServerException.class, () -> weatherClient.getTodayWeather());
        }

        @Test
        void 오늘자날씨데이터가_없으면_실패한다() {
            // given
            WeatherDto weatherDto = new WeatherDto("LOL", "weather");
            WeatherDto[] weatherDtos = new WeatherDto[] {weatherDto};
            ResponseEntity<WeatherDto[]> resps = ResponseEntity.ok(weatherDtos);

            given(restTemplate.getForEntity(any(URI.class), eq(WeatherDto[].class)))
                    .willReturn(resps);

            // when & then
            assertThrows(ServerException.class, () -> weatherClient.getTodayWeather());
        }
    }
}
