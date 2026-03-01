package com.gymcrm.trainer_workload_service.entity;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
//import org.springframework.data.annotation.Id;
//import org.springframework.data.mongodb.core.index.CompoundIndex;
//import org.springframework.data.mongodb.core.index.CompoundIndexes;
//import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@DynamoDbBean
public class TrainerWorkload {
    private String username;
    private String firstName;
    private String lastName;
    private String status;
    private List<YearSummary> years;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("trainerName")
    public String getUsername(){
        return username;
    }
}