package com.gymcrm.trainer_workload_service.service;

import com.gymcrm.gym_crm_spring.dto.workload.ActionType;
import com.gymcrm.gym_crm_spring.dto.workload.TrainerWorkloadRequest;
import com.gymcrm.trainer_workload_service.entity.TrainerWorkload;
import com.gymcrm.trainer_workload_service.repository.impl.CommandRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
//import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDate;
import java.util.List;

//import static com.mongodb.assertions.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Test for WorkloadService.
 * 
 * Cross-platform support:
 * - Uses Testcontainers when enabled (testcontainers.enabled=true) and Docker is available
 * - Falls back to docker-compose MongoDB when Testcontainers is disabled
 * 
 * MongoDB connection is configured in application-test.properties
 * To use Testcontainers: set testcontainers.enabled=true and ensure Docker is running
 * To use docker-compose: set testcontainers.enabled=false and ensure docker-compose containers are running
 */
@DataMongoTest
@ActiveProfiles("test")
@ContextConfiguration(classes = com.gymcrm.trainer_workload_service.TrainerWorkloadServiceApplication.class)
@Import({CommandRepositoryImpl.class, WorkloadService.class})
public class WorkloadServiceTest {

    // Testcontainers is configured via MongoTestConfig.java
    // When testcontainers.enabled=false, uses MongoDB from docker-compose (port 27017)

//    @Autowired
//    private WorkloadService workloadService;
//
//    @Autowired
//    private MongoTemplate mongoTemplate;
//
//    @MockBean
//    private org.springframework.jms.core.JmsTemplate jmsTemplate;
//
//    @MockBean
//    private com.gymcrm.trainer_workload_service.messaging.WorkloadMessageConsumer workloadMessageConsumer;
//
//    @BeforeEach
//    void cleanUp() {
//        mongoTemplate.dropCollection(TrainerWorkload.class);
//    }
//
//    @Test
//    void createTrainerIfNotExistsTest() {
//        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
//                .username("Lilia.Levada")
//                .firstName("Lilia")
//                .lastName("Levada")
//                .isActive(true)
//                .build();
//
//        workloadService.createTrainerLogic(request);
//        workloadService.createTrainerLogic(request);
//
//        List<TrainerWorkload> results = mongoTemplate.findAll(TrainerWorkload.class);
//        assertEquals(1, results.size());
//        assertEquals("ACTIVE", results.get(0).getStatus());
//    }
//
//    @Test
//    void saveTrainerDataTestAddActionCase1() {
//        String username = "Lilia.Levada";
//        workloadService.createTrainerLogic(TrainerWorkloadRequest.builder()
//                .username(username).firstName("Lilia").lastName("Levada").isActive(true).build());
//
//        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
//                .username(username)
//                .trainingDate(LocalDate.of(2026, 5, 20))
//                .trainingDuration(90)
//                .actionType(ActionType.ADD)
//                .build();
//
//        workloadService.saveTrainerData(request);
//
//        TrainerWorkload result = mongoTemplate.findById(username, TrainerWorkload.class);
//        assertNotNull(result);
//        assertEquals(2026, result.getYears().get(0).getYear());
//        assertEquals("MAY", result.getYears().get(0).getMonths().get(0).getMonth());
//        assertEquals(90, result.getYears().get(0).getMonths().get(0).getTrainingSummaryDuration());
//    }
//
//    @Test
//    void saveTrainerDataTestAddActionCase2() {
//        String username = "Lilia.Levada";
//        workloadService.createTrainerLogic(TrainerWorkloadRequest.builder()
//                .username(username)
//                .firstName("Lilia")
//                .lastName("Levada")
//                .isActive(true)
//                .build());
//
//        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
//                .username(username)
//                .trainingDate(LocalDate.of(2026, 1, 1))
//                .trainingDuration(60)
//                .actionType(ActionType.ADD)
//                .build();
//
//        workloadService.saveTrainerData(request);
//        workloadService.saveTrainerData(request);
//
//        TrainerWorkload result = mongoTemplate.findById(username, TrainerWorkload.class);
//        assertEquals(120, result.getYears().get(0).getMonths().get(0).getTrainingSummaryDuration());
//    }
//
//    @Test
//    void deleteTrainerTest() {
//        String username = "Lilia.Levada";
//        workloadService.createTrainerLogic(TrainerWorkloadRequest.builder()
//                .username(username)
//                .firstName("Lilia")
//                .lastName("Levada")
//                .isActive(true)
//                .build());
//
//        workloadService.deleteTrainer(username);
//
//        TrainerWorkload result = mongoTemplate.findById(username, TrainerWorkload.class);
//        assertNull(result, "The document should no longer exist in the database.");
//    }
//
//    @Test
//    void saveTrainerDataDeleteAction() {
//        String username = "Lilia.Levada";
//        workloadService.createTrainerLogic(TrainerWorkloadRequest.builder()
//                .username(username).firstName("Lilia").lastName("Levada").isActive(true).build());
//
//        workloadService.saveTrainerData(TrainerWorkloadRequest.builder()
//                .username(username)
//                .trainingDate(LocalDate.of(2026, 2, 10))
//                .trainingDuration(100)
//                .actionType(ActionType.ADD)
//                .build());
//
//        TrainerWorkloadRequest deleteRequest = TrainerWorkloadRequest.builder()
//                .username(username)
//                .trainingDate(LocalDate.of(2026, 2, 10))
//                .trainingDuration(40)
//                .actionType(ActionType.DELETE)
//                .build();
//        workloadService.saveTrainerData(deleteRequest);
//
//        TrainerWorkload result = mongoTemplate.findById(username, TrainerWorkload.class);
//        assertNotNull(result);
//        int finalDuration = result.getYears().get(0).getMonths().get(0).getTrainingSummaryDuration();
//        assertEquals(60, finalDuration, "Duration should have been subtracted.");
//    }
}
