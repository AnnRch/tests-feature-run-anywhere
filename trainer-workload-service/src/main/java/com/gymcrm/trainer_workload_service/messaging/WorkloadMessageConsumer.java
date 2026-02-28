package com.gymcrm.trainer_workload_service.messaging;

import com.gymcrm.trainer_workload_service.dto.TrainerWorkloadRequest;
import com.gymcrm.trainer_workload_service.service.WorkloadService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkloadMessageConsumer {

    private final WorkloadService workloadService;
    private final SqsTemplate sqsTemplate;

    @Value("${trainer.workload.dlq}")
    private String dlqName;


    @SqsListener("${trainer.workload.queue}")
    public void consume(TrainerWorkloadRequest request) {
            log.info("Received workload message: {}", request);
            workloadService.saveTrainerData(request);
    }

    @SqsListener("${trainer.create.workload.queue}")
    public void consumeTrainerCreate(TrainerWorkloadRequest request) {
        log.info("Received workload message about trainer creation: {}", request);
        workloadService.createTrainerLogic(request);
    }

    @SqsListener("${trainer.delete.workload.queue}")
    public void consumeTrainerDelete(TrainerWorkloadRequest request) {
        log.info("Received workload message about trainer deletion: {}", request);
        workloadService.deleteTrainer(request.getUsername());
    }

    @SqsListener("${trainer.workload.dlq}")
    public void consumeDlq(TrainerWorkloadRequest request) {
        log.warn("Message reached the AWS DLQ: {}", request);
    }
}
