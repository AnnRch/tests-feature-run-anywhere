package com.gymcrm.trainer_workload_service.repository.impl;

import com.gymcrm.gym_crm_spring.dto.workload.TrainerWorkloadRequest;
import com.gymcrm.trainer_workload_service.entity.MonthSummary;
import com.gymcrm.trainer_workload_service.entity.TrainerWorkload;
import com.gymcrm.trainer_workload_service.entity.YearSummary;
import com.gymcrm.trainer_workload_service.repository.CommandRepository;
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
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.util.ArrayList;
import java.util.Map;

@Repository
@Slf4j
@RequiredArgsConstructor
public class CommandRepositoryImpl implements CommandRepository {

    private final DynamoDbTemplate dynamoDbTemplate;
    private final DynamoDbTable<TrainerWorkload> trainerTable;

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

        Key key = Key.builder().partitionValue(trainerWorkload.getUsername()).build();
        TrainerWorkload existing = trainerTable.getItem(key);

        if (existing == null) {
            if (trainerWorkload.getYears() == null) {
                trainerWorkload.setYears(new ArrayList<>());
            }
            // USE trainerTable HERE
            trainerTable.putItem(trainerWorkload);
            log.info("Created new trainer workload for {}", trainerWorkload.getUsername());
        }
    }

    @Override
    public void updateTrainerYearMonthDuration(TrainerWorkloadRequest request) {
        String username = request.getUsername();
        int year = request.getTrainingDate().getYear();
        String month = request.getTrainingDate().getMonth().name();
        int duration = calculateDuration(request);

        // CHANGE: Use trainerTable.getItem() instead of template.load()
        Key key = Key.builder().partitionValue(username).build();
        TrainerWorkload workload = trainerTable.getItem(key);

        if (workload == null) {
            log.info("Trainer {} not found, creating skeleton workload for update.", request.getUsername());
            workload = new TrainerWorkload();
            workload.setUsername(request.getUsername());
            workload.setYears(new ArrayList<>());
        }

        // ... (Your existing logic for updating year/month lists remains the same) ...
        TrainerWorkload finalWorkload = workload;
        YearSummary yearSummary = workload.getYears().stream()
                .filter(y -> y.getYear() == year)
                .findFirst()
                .orElseGet(() -> {
                    YearSummary newYear = new YearSummary(year, new ArrayList<>());
                    finalWorkload.getYears().add(newYear);
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

        // CHANGE: Use trainerTable.putItem() instead of template.save()
        trainerTable.putItem(workload);
    }

    @Override
    public void deleteByUsername(String username) {
        Key key = Key.builder()
                .partitionValue(username)
                .build();

        // CHANGE: Use trainerTable.getItem()
        TrainerWorkload workload = trainerTable.getItem(key);

        if (workload != null) {
            // CHANGE: Use trainerTable.deleteItem()
            trainerTable.deleteItem(key);
            log.info("Deleted workload for trainer: {}", username);
        } else {
            log.warn("Trainer workload not found for deletion: {}", username);
        }
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
