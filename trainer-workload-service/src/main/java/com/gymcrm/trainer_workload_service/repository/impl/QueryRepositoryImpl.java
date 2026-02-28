package com.gymcrm.trainer_workload_service.repository.impl;

import com.gymcrm.trainer_workload_service.entity.TrainerWorkload;
import com.gymcrm.trainer_workload_service.repository.QueryRepository;
import io.awspring.cloud.dynamodb.DynamoDbTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.Key;

@Repository
@RequiredArgsConstructor
public class QueryRepositoryImpl implements QueryRepository {

    private final DynamoDbTemplate dynamoDbTemplate;

    @Override
    public TrainerWorkload findByUsername(String username) {
        return dynamoDbTemplate.load(
                Key.builder()
                        .partitionValue(username)
                        .build(),
                TrainerWorkload.class
        );
    }
}
