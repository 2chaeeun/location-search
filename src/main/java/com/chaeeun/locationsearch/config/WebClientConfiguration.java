package com.chaeeun.locationsearch.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfiguration {

    @Value("${kakao-api-key}")
    private String kakaoApiKey;

    @Value("${X-Naver-Client-Id}")
    private String naverClientId;

    @Value("${X-Naver-Client-Secret}")
    private String naverClientSecret;

    @Bean(value = "kakaoWebClient")
    public WebClient kakaoWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1500)
                .responseTimeout(Duration.ofMillis(1500))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(1500, TimeUnit.MILLISECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(1500, TimeUnit.MILLISECONDS)));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl("https://dapi.kakao.com")
                .defaultHeader("Authorization", kakaoApiKey)
                .build();
    }

    @Bean(value = "naverWebClient")
    public WebClient naverWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1500)
                .responseTimeout(Duration.ofMillis(1500))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(1500, TimeUnit.MILLISECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(1500, TimeUnit.MILLISECONDS)));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl("https://openapi.naver.com")
                .defaultHeader("X-Naver-Client-Id", naverClientId)
                .defaultHeader("X-Naver-Client-Secret", naverClientSecret)
                .build();
    }
}
