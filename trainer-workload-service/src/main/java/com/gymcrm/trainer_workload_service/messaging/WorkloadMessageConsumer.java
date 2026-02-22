package com.gymcrm.trainer_workload_service.messaging;

import com.gymcrm.trainer_workload_service.dto.TrainerWorkloadRequest;
import com.gymcrm.trainer_workload_service.service.WorkloadService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
//import org.springframework.jms.annotation.JmsListener;
//import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkloadMessageConsumer {

    private final WorkloadService workloadService;
//    private final JmsTemplate jmsTemplate;
    private final SqsTemplate sqsTemplate;

    @Value("${trainer.workload.dlq}")
    private String dlqName;


//    @JmsListener(destination = "${trainer.workload.queue}")
    @SqsListener("${trainer.workload.queue}")
    public void consume(TrainerWorkloadRequest request) {
//        try {
            log.info("Received workload message: {}", request);
            workloadService.saveTrainerData(request);
//        } catch (Exception e) {
//            log.error("Failed to process workload message, sending to DLQ", e);
//
//            jmsTemplate.convertAndSend(dlqName, request, message -> {
//                message.setStringProperty("_type", JmsTypes.TRAINER_WORKLOAD_V1);
//                return message;
//            });
//        }
    }

    @SqsListener("${trainer.create.workload.queue}")
    public void consumeTrainerCreate(TrainerWorkloadRequest request) {
        log.info("Received workload message about trainer creation: {}", request);
        workloadService.createTrainerLogic(request);
    }

//    @JmsListener(destination = "${trainer.create.workload.queue}")
//    public void consumeTrainerCreate(TrainerWorkloadRequest request){
//        try {
//            log.info("Received workload message about trainer creation: {}", request);
//            workloadService.createTrainerLogic(request);
//        } catch (Exception e) {
//            log.error("Failed to process workload message, sending to DLQ", e);
//
//            jmsTemplate.convertAndSend(dlqName, request, message -> {
//                message.setStringProperty("_type", JmsTypes.TRAINER_WORKLOAD_V1);
//                return message;
//            });
//        }
//    }

    @SqsListener("${trainer.delete.workload.queue}")
    public void consumeTrainerDelete(TrainerWorkloadRequest request) {
        log.info("Received workload message about trainer deletion: {}", request);
        // Ensure request.getTrainerUsername() matches your DTO field name
        workloadService.deleteTrainer(request.getUsername());
    }

//    @JmsListener(destination = "${trainer.delete.workload.queue}")
//    public void consumeTrainerDelete(TrainerWorkloadRequest request){
//        try {
//            log.info("Received workload message about trainer deletion: {}", request);
//            workloadService.deleteTrainer(request.getUsername());
//        } catch (Exception e) {
//            log.error("Failed to process workload message, sending to DLQ", e);
//
//            jmsTemplate.convertAndSend(dlqName, request, message -> {
//                message.setStringProperty("_type", JmsTypes.TRAINER_WORKLOAD_V1);
//                return message;
//            });
//        }
//    }

    @SqsListener("${trainer.workload.dlq}")
    public void consumeDlq(TrainerWorkloadRequest request) {
        log.warn("Message reached the AWS DLQ: {}", request);
    }

//    @JmsListener(destination = "${trainer.workload.dlq}")
//    public void consumeDlq(TrainerWorkloadRequest request) {
//        log.warn("Message in DLQ: {}", request);
//    }
}
