package com.gymcrm.trainer_workload_service.repository.impl;

import com.gymcrm.trainer_workload_service.dto.TrainerWorkloadRequest;
import com.gymcrm.trainer_workload_service.entity.MonthSummary;
import com.gymcrm.trainer_workload_service.entity.TrainerWorkload;
import com.gymcrm.trainer_workload_service.entity.YearSummary;
import com.gymcrm.trainer_workload_service.repository.CommandRepository;
import com.gymcrm.trainer_workload_service.repository.QueryRepository;
//import com.mongodb.client.result.DeleteResult;
//import com.mongodb.client.result.UpdateResult;
import io.awspring.cloud.dynamodb.DynamoDbTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.query.Criteria;
//import org.springframework.data.mongodb.core.query.Query;
//import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.util.ArrayList;
import java.util.Map;

@Repository
@Slf4j
@RequiredArgsConstructor
public class CommandRepositoryImpl implements CommandRepository {

    private final DynamoDbTemplate dynamoDbTemplate;

    private final Map<Integer, String> monthsMap = Map.ofEntries(
            Map.entry(1, "JANUARY"),
            Map.entry(2, "FEBRUARY"),
            Map.entry(3, "MARCH"),
            Map.entry(4, "APRIL"),
            Map.entry(5, "MAY"),
            Map.entry(6, "JUNE"),
            Map.entry(7, "JULY"),
            Map.entry(8, "AUGUST"),
            Map.entry(9, "SEPTEMBER"),
            Map.entry(10, "OCTOBER"),
            Map.entry(11, "NOVEMBER"),
            Map.entry(12, "DECEMBER")
    );

    @Override
    public void createTrainerIfNotExists(TrainerWorkload trainerWorkload) {
        TrainerWorkload existing = dynamoDbTemplate.load(
                Key.builder().partitionValue(trainerWorkload.getUsername()).build(),
                TrainerWorkload.class);

        if (existing == null) {
            if (trainerWorkload.getYears() == null) {
                trainerWorkload.setYears(new ArrayList<>());
            }
            dynamoDbTemplate.save(trainerWorkload);
            log.info("Created new trainer workload for {}", trainerWorkload.getUsername());

        }
    }

    @Override
    public void updateTrainerYearMonthDuration(TrainerWorkloadRequest request) {
        String username = request.getUsername();
        int year = request.getTrainingDate().getYear();
        String month = request.getTrainingDate().getMonth().name();
        int duration = calculateDuration(request);

        TrainerWorkload workload = dynamoDbTemplate.load(
                Key.builder().partitionValue(username).build(),
                TrainerWorkload.class);

        if (workload == null) {
            log.warn("Trainer {} not found, cannot update duration", username);
            return;
        }

        YearSummary yearSummary = workload.getYears().stream()
                .filter(y -> y.getYear() == year)
                .findFirst()
                .orElseGet(() -> {
                    YearSummary newYear = new YearSummary(year, new ArrayList<>());
                    workload.getYears().add(newYear);
                    return newYear;
                });

        MonthSummary monthSummary = yearSummary.getMonths().stream()
                .filter(m -> m.getMonth().equalsIgnoreCase(month))
                .findFirst()
                .orElseGet(() -> {
                    MonthSummary newMonth = new MonthSummary(month, 0);
                    yearSummary.getMonths().add(newMonth);
                    return newMonth;
                });

        monthSummary.setTrainingSummaryDuration(monthSummary.getTrainingSummaryDuration() + duration);
        dynamoDbTemplate.save(workload);
    }

    @Override
    public void deleteByUsername(String username) {

        TrainerWorkload workload = new TrainerWorkload();
        workload.setUsername(username);

        dynamoDbTemplate.delete(workload);
        log.info("Deleted workload for trainer: {}", username);
    }


    private int calculateDuration(TrainerWorkloadRequest dto){
        if (dto.getActionType() == null) {
            return 0;
        }
        return switch (dto.getActionType().name()) {
            case "ADD" -> dto.getTrainingDuration();
            case "DELETE" -> -dto.getTrainingDuration();
            default -> 0;
        };
    }
}
