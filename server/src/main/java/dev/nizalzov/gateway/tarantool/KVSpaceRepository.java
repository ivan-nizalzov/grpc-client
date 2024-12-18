package dev.nizalzov.gateway.tarantool;

import dev.nizalzov.gateway.exception.TarantoolRepoException;
import io.tarantool.driver.api.TarantoolClient;
import io.tarantool.driver.api.TarantoolResult;
import io.tarantool.driver.api.conditions.Conditions;
import io.tarantool.driver.api.space.TarantoolSpaceOperations;
import io.tarantool.driver.api.tuple.TarantoolTuple;
import io.tarantool.driver.exceptions.TarantoolClientException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
public class KVSpaceRepository extends AbstractSpaceRepository {

    public KVSpaceRepository() {
        super(SpaceNames.KV);
    }

    @Override
    public TarantoolResult<TarantoolTuple> insert(TarantoolTuple tarantoolTuple) {
        try (TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> client = tarantoolConfig.makeClient()) {
            return getSpace(client)
                    .insert(tarantoolTuple)
                    .get();
        } catch (Exception e) {
            throw new TarantoolRepoException(e.getMessage());
        }
    }

    @Override
    public TarantoolResult<TarantoolTuple> select(Conditions conditions) {
        try (TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> client = tarantoolConfig.makeClient()) {
            return getSpace(client)
                    .select(conditions)
                    .get();
        } catch (Exception e) {
            throw new TarantoolRepoException(e.getMessage());
        }
    }

    @Override
    public TarantoolResult<TarantoolTuple> selectMany(Conditions conditions) {
        try (TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> client = tarantoolConfig.makeClient()) {
            return getSpace(client)
                    .select(conditions)
                    .get();
        } catch (Exception e) {
            throw new TarantoolRepoException(e.getMessage());
        }
    }

    @Override
    public void delete(Conditions conditions) {
        try (TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> client = tarantoolConfig.makeClient()) {
            getSpace(client)
                    .delete(conditions)
                    .get();
        } catch (Exception e) {
            throw new TarantoolRepoException(e.getMessage());
        }
    }

    @Override
    public Integer countTuples() {
        try (TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> client = tarantoolConfig.makeClient()) {
            List<?> list = client.call("count_tuples", Integer.class)
                    .get();
            return (Integer) list.get(0);
        } catch (Exception e) {
            throw new TarantoolRepoException(e.getMessage());
        }
    }

    public enum Fields {
        KEY,
        VALUE
    }
}
