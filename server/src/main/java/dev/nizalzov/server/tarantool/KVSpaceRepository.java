package dev.nizalzov.server.tarantool;

import dev.nizalzov.server.exception.TarantoolRepoException;
import io.tarantool.driver.api.TarantoolClient;
import io.tarantool.driver.api.TarantoolResult;
import io.tarantool.driver.api.conditions.Conditions;
import io.tarantool.driver.api.tuple.TarantoolTuple;
import org.springframework.stereotype.Component;

import java.util.List;

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
    public List<List<?>> selectMany(String keySince, String keyTo) {
        try (TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> client = tarantoolConfig.makeClient()) {
            List<?> rangeTuples = client.call("range_tuples", keySince, keyTo).get();
            return (List<List<?>>) rangeTuples;
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
            List<?> list = client.call("count_tuples").get();
            return ((Number) list.get(0)).intValue();
        } catch (Exception e) {
            throw new TarantoolRepoException(e.getMessage());
        }
    }

    public enum Fields {
        KEY,
        VALUE;

        private String name;

        Fields() {
            this.name = this.name().toLowerCase();
        }

        public String getName() {
            return name;
        }
    }
}
