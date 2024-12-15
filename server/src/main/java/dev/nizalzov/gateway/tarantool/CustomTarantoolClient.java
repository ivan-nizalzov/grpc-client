package dev.nizalzov.gateway.tarantool;

import dev.nizalzov.gateway.exception.TarantoolConfigException;
import io.tarantool.driver.api.*;
import io.tarantool.driver.api.tuple.TarantoolTuple;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static io.tarantool.driver.api.connection.TarantoolConnectionSelectionStrategyType.PARALLEL_ROUND_ROBIN;

@Component
public class CustomTarantoolClient {
    private final Logger logger = LogManager.getLogger(this.getClass());

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

    private TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> client;

    @PostConstruct
    void init() {
        client = makeClient();
    }

    public TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> makeClient() {

        logger.info("Connecting to Tarantool at {}:{}", HOST, PORT);

        if (USERNAME == null || PASSWORD == null) {
            throw new TarantoolConfigException(
                    "Tarantool's username and password aren't provided.");
        }

        if (HOST == null || PORT == null) {
            logger.warn("Tarantool's host and port aren't provided. Using default values: localhost:3301");
            HOST = "localhost";
            PORT = 3301;
        }

        return TarantoolClientFactory.createClient()
                .withAddress(HOST, PORT)
                .withCredentials(USERNAME, PASSWORD)
                .withConnections(10)
                .withConnectionSelectionStrategy(PARALLEL_ROUND_ROBIN)
                .withRetryingByNumberOfAttempts(
                        RETRIES,
                        builder -> builder
                                .withRequestTimeout(TIMEOUT_MS)
                )
                .build();
    }

    public TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> getClient() {
        return client;
    }
}
