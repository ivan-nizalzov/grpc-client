package dev.nizalzov.gateway;


import io.tarantool.driver.api.TarantoolClientFactory;

import static io.tarantool.driver.api.connection.TarantoolConnectionSelectionStrategyType.PARALLEL_ROUND_ROBIN;

public class Test {
    public static void main(String[] args) {
        try {
            var client = TarantoolClientFactory.createClient()
                    .withAddress("localhost", 3301)
                    .withCredentials("tarantool", "tarantool")
                    .withConnectionSelectionStrategy(PARALLEL_ROUND_ROBIN)
                    .withRetryingByNumberOfAttempts(
                            5,
                            builder -> builder
                                    .withRequestTimeout(100)
                    )
                    .build();


            System.out.println();
            System.out.println("Is client instance of TarantoolClient? Answer: " + (client instanceof io.tarantool.driver.api.TarantoolClient));
            System.out.println("Tarantool version: " + client.getVersion());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
