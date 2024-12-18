package dev.nizalzov.gateway.tarantool;

import io.tarantool.driver.api.TarantoolClient;
import io.tarantool.driver.api.TarantoolResult;
import io.tarantool.driver.api.conditions.Conditions;
import io.tarantool.driver.api.space.TarantoolSpaceOperations;
import io.tarantool.driver.api.tuple.TarantoolTuple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractSpaceRepository {
    private final Logger logger = LogManager.getLogger(this.getClass());

//    private final TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient;
//    private final TarantoolSpaceOperations<TarantoolTuple, TarantoolResult<TarantoolTuple>> space;

//    public AbstractSpaceClient(TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient,
//                               String spaceName) {
//        this.tarantoolClient = tarantoolClient;
//        this.space = tarantoolClient.space(spaceName);
//    }

    @Autowired
    protected TarantoolConfig tarantoolConfig;

    private final String SPACE_NAME;

    public AbstractSpaceRepository(String spaceName) {
        this.SPACE_NAME = spaceName;
    }

    public abstract TarantoolResult<TarantoolTuple> insert(TarantoolTuple tarantoolTuple);

    public abstract TarantoolResult<TarantoolTuple> select(Conditions conditions);

    public abstract TarantoolResult<TarantoolTuple> selectMany(Conditions conditions);

    public abstract void delete(Conditions conditions);

    public abstract Integer countTuples();

    public TarantoolSpaceOperations<TarantoolTuple, TarantoolResult<TarantoolTuple>> getSpace(
            TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> client
    ) {
        return client.space(SPACE_NAME);
    }
}
