package dev.nizalzov.gateway.tarantool;

import dev.nizalzov.gateway.exception.TarantoolRepoException;
import io.tarantool.driver.api.TarantoolClient;
import io.tarantool.driver.api.TarantoolResult;
import io.tarantool.driver.api.conditions.Conditions;
import io.tarantool.driver.api.tuple.TarantoolTuple;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
public class KVSpaceClient extends AbstractSpaceClient {

    public KVSpaceClient(TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient) {
        super(tarantoolClient, SpaceNames.KV);
    }

    @Override
    public TarantoolResult<TarantoolTuple> insert(TarantoolTuple tarantoolTuple) {
        try {
            return getSpace().insert(tarantoolTuple).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new TarantoolRepoException(e.getMessage());
        }
    }

    @Override
    public TarantoolResult<TarantoolTuple> select(Conditions conditions) {
        try {
            return getSpace().select(conditions).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TarantoolResult<TarantoolTuple> delete(Conditions conditions) {
        return null;
    }

    public enum Fields {
        KEY,
        VALUE
    }
}
