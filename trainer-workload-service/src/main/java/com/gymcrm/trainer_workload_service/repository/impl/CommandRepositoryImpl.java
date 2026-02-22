package com.gymcrm.trainer_workload_service.repository.impl;

import com.gymcrm.trainer_workload_service.dto.TrainerWorkloadRequest;
import com.gymcrm.trainer_workload_service.entity.MonthSummary;
import com.gymcrm.trainer_workload_service.entity.TrainerWorkload;
import com.gymcrm.trainer_workload_service.entity.YearSummary;
import com.gymcrm.trainer_workload_service.repository.CommandRepository;
import com.gymcrm.trainer_workload_service.repository.QueryRepository;
//import com.mongodb.client.result.DeleteResult;
//import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.query.Criteria;
//import org.springframework.data.mongodb.core.query.Query;
//import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

//@Repository
@Slf4j
@RequiredArgsConstructor
public class CommandRepositoryImpl implements CommandRepository {

//    private final MongoTemplate mongoTemplate;
//    private final QueryRepository queryRepository;
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
//        Query query = new Query(Criteria.where("username").is(trainerWorkload.getUsername()));
//        Query query = new Query(Criteria.where("_id").is(trainerWorkload.getUsername()));
//
//        Update update = new Update()
////                .setOnInsert("username", trainerWorkload.getUsername())
//                .setOnInsert("_id",trainerWorkload.getUsername())
//                .setOnInsert("firstName", trainerWorkload.getFirstName())
//                .setOnInsert("lastName", trainerWorkload.getLastName())
//                .setOnInsert("status", trainerWorkload.getStatus())
//                .setOnInsert("years", Collections.emptyList());
//
//        mongoTemplate.upsert(query, update, TrainerWorkload.class);

    }

    @Override
    public void updateTrainerYearMonthDuration(TrainerWorkloadRequest request) {
        String username = request.getUsername();
        int year = request.getTrainingDate().getYear();
        String month = request.getTrainingDate().getMonth().name();
        int duration = calculateDuration(request);

//        if (request.getActionType().name().equalsIgnoreCase("DELETE")) {
//            deleteCheck(request);
//        }

//        Query query = new Query(Criteria.where("_id").is(username));
//        Update update = new Update()
//                .inc("years.$[yearIdx].months.$[monthIdx].trainingSummaryDuration", duration)
//                .filterArray(Criteria.where("yearIdx.year").is(year))
//                .filterArray(Criteria.where("monthIdx.month").is(month));
//
//        UpdateResult result = mongoTemplate.updateFirst(query, update, TrainerWorkload.class);
//
//        if (result.getModifiedCount() == 0) {
//            addYearMonthIfNotExist(username, year, month, duration);
//        }
    }

    @Override
    public void deleteByUsername(String username) {
//        Query query = new Query(Criteria.where("username").is(username));
//        DeleteResult deleteResult = mongoTemplate.remove(query, TrainerWorkload.class);
//        log.info("Deleted {} workload document(s) for trainer {}",
//                deleteResult.getDeletedCount(),
//                username
//        );
//        deleteResult.getDeletedCount();
    }

    private void deleteCheck(TrainerWorkloadRequest request){
       int requestedDuration = request.getTrainingDuration();
//       TrainerWorkload workload = queryRepository.findByUsername(request.getUsername());

//       YearSummary yearSummary = workload.getYears().stream()
//               .filter(yearSummary1 ->
//                       yearSummary1.getYear() == request.getTrainingDate().getYear())
//               .findFirst()
//               .orElse(null);

//       if (yearSummary == null) {
//           throw new NullPointerException("nullYearSummary");
//       }
//
//       MonthSummary monthSummary = yearSummary.getMonths().stream()
//               .filter(monthSummary1 ->
//                       monthSummary1.getMonth().equalsIgnoreCase(
//                               monthsMap.get(request.getTrainingDate().getMonthValue())))
//               .findFirst()
//               .orElse(null);

//       if (monthSummary == null){
//           throw new NullPointerException("nullMonthSummary");
//       }
//
//        int currentDuration = monthSummary.getTrainingSummaryDuration();
//        if (requestedDuration > currentDuration) {
//            throw new IllegalArgumentException("Requested duration can't be subtracted");
//        }
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

    private void addYearMonthIfNotExist (String username,int year, String month,int duration) {
//        Query yearQuery = new Query(Criteria.where("_id").is(username).and("years.year").is(year));
//        boolean yearExists = mongoTemplate.exists(yearQuery, TrainerWorkload.class);
//
//        if (yearExists) {
//            mongoTemplate.updateFirst(
//                    yearQuery,
//                    new Update().push("years.$.months", new MonthSummary(month, duration)),
//                    TrainerWorkload.class
//            );
//        } else {
//            YearSummary newYear = YearSummary.builder()
//                    .year(year)
//                    .months(Collections.singletonList(new MonthSummary(month, duration)))
//                    .build();
//
//            mongoTemplate.updateFirst(
//                    new Query(Criteria.where("_id").is(username)),
//                    new Update().push("years", newYear),
//                    TrainerWorkload.class
//            );
//        }
    }
}
