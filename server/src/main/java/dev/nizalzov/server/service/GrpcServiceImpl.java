package dev.nizalzov.server.service;

import com.google.protobuf.ByteString;
import dev.nizalzov.server.exception.GrpcServiceException;
import dev.nizalzov.server.tarantool.KVSpaceRepository;
import dev.nizalzov.grpc.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.tarantool.driver.api.TarantoolResult;
import io.tarantool.driver.api.conditions.Conditions;
import io.tarantool.driver.api.tuple.DefaultTarantoolTupleFactory;
import io.tarantool.driver.api.tuple.TarantoolTuple;
import io.tarantool.driver.api.tuple.TarantoolTupleFactory;
import io.tarantool.driver.mappers.factories.DefaultMessagePackMapperFactory;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;

@GrpcService
public class GrpcServiceImpl extends CustomServiceGrpc.CustomServiceImplBase {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final DefaultMessagePackMapperFactory mapperFactory = DefaultMessagePackMapperFactory.getInstance();
    private final TarantoolTupleFactory tupleFactory = new DefaultTarantoolTupleFactory(mapperFactory.defaultComplexTypesMapper());

    private final KVSpaceRepository kvSpaceRepository;

    public GrpcServiceImpl(KVSpaceRepository kvSpaceRepository) {
        this.kvSpaceRepository = kvSpaceRepository;
    }

    @Override
    public void put(PutRequest request, StreamObserver<PutResponse> responseObserver) {
        try {
            if (request.getKey().isEmpty()) {
                throw new GrpcServiceException("Key cannot be empty.");
            }

            Conditions conditions = makeSelectConditions(request.getKey());
            TarantoolResult<TarantoolTuple> selectedTuple = kvSpaceRepository.select(conditions);

            if (selectedTuple.isEmpty()) {
                TarantoolTuple tarantoolTuple = makeTuple(request.getKey(), request.getValue().toByteArray());
                TarantoolResult<TarantoolTuple> insertedTuple = kvSpaceRepository.insert(tarantoolTuple);

                if (!insertedTuple.isEmpty()) {
                    logger.info("Successfully inserted value into Tarantool for key '{}'", request.getKey());
                    responseObserver.onNext(
                            PutResponse.newBuilder()
                                    .setSuccess(true)
                                    .build()
                    );
                }
            } else {
                logger.error("Key '{}' already exists", request.getKey());
                responseObserver.onError(
                        Status.ALREADY_EXISTS
                                .withDescription(String.format("Key '%s' already exists", request.getKey()))
                                .asRuntimeException()
                );
            }
        } catch (GrpcServiceException e) {
            logger.error("Failed to put value with key '{}': {}", request.getKey(), e.getMessage());

            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription(String.format("Failed to put value with key '%s'", request.getKey()))
                            .augmentDescription("Key: " + request.getKey())
                            .withCause(e)
                            .asRuntimeException());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void get(GetRequest request, StreamObserver<GetResponse> responseObserver) {
        try {
            if (request.getKey().isEmpty()) {
                throw new GrpcServiceException("Key cannot be empty.");
            }

            Conditions conditions = makeSelectConditions(request.getKey());
            TarantoolResult<TarantoolTuple> selectedTuple = kvSpaceRepository.select(conditions);

            if (selectedTuple.isEmpty()) {
                logger.error("Key '{}' not found", request.getKey());
                responseObserver.onError(
                        Status.NOT_FOUND
                                .withDescription(String.format("Key '%s' not found", request.getKey()))
                                .asRuntimeException()
                );
            } else {
                logger.info("Successfully selected value from Tarantool for key '{}'", request.getKey());
                responseObserver.onNext(
                        GetResponse.newBuilder()
                                .setValue(ByteString.copyFrom(
                                        selectedTuple
                                                .get(0)
                                                .getByteArray(KVSpaceRepository.Fields.VALUE.getName()))
                                )
                                .build()
                );
            }
        } catch (GrpcServiceException e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription(String.format("Failed to get value for key '%s'", request.getKey()))
                    .asRuntimeException());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void delete(DeleteRequest request, StreamObserver<DeleteResponse> responseObserver) {
        try {
            Conditions conditions = makeSelectConditions(request.getKey());
            TarantoolResult<TarantoolTuple> selectedTuple = kvSpaceRepository.select(conditions);

            if (selectedTuple.isEmpty()) {
                logger.error("Key '{}' not found", request.getKey());
                responseObserver.onError(
                        Status.NOT_FOUND
                                .withDescription(String.format("Key '%s' not found", request.getKey()))
                                .asRuntimeException()
                );
            } else {
                kvSpaceRepository.delete(conditions);
                logger.info("Successfully deleted value from Tarantool for key '{}'", request.getKey());
                responseObserver.onNext(
                        DeleteResponse.newBuilder()
                                .setSuccess(true)
                                .build()
                );
            }
        } catch (GrpcServiceException e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription(String.format("Failed to delete value for key '%s'", request.getKey()))
                    .asRuntimeException());
        }
        responseObserver.onCompleted();
    }

    // Limit and offset are not used in this implementation
    @Override
    public void range(RangeRequest request, StreamObserver<RangeResponse> responseObserver) {
        try {
            Conditions conditionsSince = makeSelectConditions(request.getKeySince());
            Conditions conditionsTo = makeSelectConditions(request.getKeyTo());

            TarantoolResult<TarantoolTuple> selectedSince = kvSpaceRepository.select(conditionsSince);
            TarantoolResult<TarantoolTuple> selectedTo = kvSpaceRepository.select(conditionsTo);

            if (selectedSince.isEmpty() || selectedTo.isEmpty()) {
                logger.error("Keys '{}' or '{}' not found", request.getKeySince(), request.getKeyTo());
                responseObserver.onError(
                        Status.NOT_FOUND
                                .withDescription(String.format("Keys '%s' or '%s' not found", request.getKeySince(), request.getKeyTo()))
                                .asRuntimeException()
                );
            } else {
                List<List<?>> result = kvSpaceRepository.selectMany(request.getKeySince(), request.getKeyTo());
                List<?> tuples = result.get(0);

                RangeResponse.Builder responseBuilder = RangeResponse.newBuilder();
                tuples.forEach(row -> {
                    List<?> tupleList = (List<?>) row;
                    String key = (String) tupleList.get(0);
                    byte[] value = (byte[]) tupleList.get(1);

                    responseBuilder.addPairs(
                            KeyValuePair.newBuilder()
                                    .setKey(key)
                                    .setValue(ByteString.copyFrom(value))
                                    .build()
                    );
                });

                responseObserver.onNext(responseBuilder.build());
            }
        } catch (GrpcServiceException e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription(String.format("Failed to get range for keys '%s' to '%s'", request.getKeySince(), request.getKeyTo()))
                    .asRuntimeException());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void count(CountRequest request, StreamObserver<CountResponse> responseObserver) {
        try {
            Integer allTuplesCount = kvSpaceRepository.countTuples();
            responseObserver.onNext(
                    CountResponse.newBuilder()
                            .setCount(allTuplesCount)
                            .build()
            );
        } catch (GrpcServiceException e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to count tuples")
                    .asRuntimeException());
        }
        responseObserver.onCompleted();
    }

    private TarantoolTuple makeTuple(String key, byte[] value) {
        return tupleFactory.create(Arrays.asList(key, value));
    }

    private Conditions makeSelectConditions(String key) {
        return Conditions.equals(KVSpaceRepository.Fields.KEY.getName(), key);
    }
}
