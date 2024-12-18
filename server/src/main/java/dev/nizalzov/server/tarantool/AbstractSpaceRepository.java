package dev.nizalzov.server.tarantool;

import io.tarantool.driver.api.TarantoolClient;
import io.tarantool.driver.api.TarantoolResult;
import io.tarantool.driver.api.conditions.Conditions;
import io.tarantool.driver.api.space.TarantoolSpaceOperations;
import io.tarantool.driver.api.tuple.TarantoolTuple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public abstract class AbstractSpaceRepository {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Autowired
    protected TarantoolConfig tarantoolConfig;

    private final String SPACE_NAME;

    public AbstractSpaceRepository(String spaceName) {
        this.SPACE_NAME = spaceName;
    }

    public abstract TarantoolResult<TarantoolTuple> insert(TarantoolTuple tarantoolTuple);

    public abstract TarantoolResult<TarantoolTuple> select(Conditions conditions);

    public abstract List<List<?>> selectMany(String keySince, String keyTo);

    public abstract void delete(Conditions conditions);

    public abstract Integer countTuples();

    public TarantoolSpaceOperations<TarantoolTuple, TarantoolResult<TarantoolTuple>> getSpace(
            TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> client
    ) {
        return client.space(SPACE_NAME);
    }
}
