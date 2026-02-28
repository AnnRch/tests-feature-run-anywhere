package com.gymcrm.trainer_workload_service.entity;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@DynamoDbBean
public class YearSummary {
    private int year;
    private List<MonthSummary> months;
}
