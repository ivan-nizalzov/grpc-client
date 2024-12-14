package dev.nizalzov.gateway.config;

import io.tarantool.driver.api.*;
import io.tarantool.driver.api.tuple.TarantoolTuple;
import io.tarantool.driver.auth.SimpleTarantoolCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static io.tarantool.driver.api.connection.TarantoolConnectionSelectionStrategyType.PARALLEL_ROUND_ROBIN;

@Configuration
public class TarantoolConfig {

    @Value("${spring.tarantool.host}")
    private String HOST;

    @Value("${spring.tarantool.port}")
    private Integer PORT;

    @Value("${spring.tarantool.username}")
    private String USERNAME;

    @Value("${spring.tarantool.password}")
    private String PASSWORD;

    @Value("${spring.tarantool.connection.retries}")
    private Integer RETRIES;

    @Value("${spring.tarantool.connection.timeout}")
    private Integer TIMEOUT_MS;

    @Bean
    public TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient() {

        return TarantoolClientFactory.createClient()
                .withAddress(new TarantoolServerAddress(HOST, PORT).getSocketAddress())
                .withCredentials(new SimpleTarantoolCredentials(USERNAME, PASSWORD))
                .withConnectionSelectionStrategy(PARALLEL_ROUND_ROBIN)
                .withRetryingByNumberOfAttempts(
                        RETRIES,
                        builder -> builder
                                .withRequestTimeout(TIMEOUT_MS)
                )
                .build();
    }
}
