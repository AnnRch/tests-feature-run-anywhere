package com.gymcrm.gym_crm_spring.messaging;

import com.gymcrm.gym_crm_spring.dto.workload.TrainerWorkloadRequest;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
//import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkloadMessageProducer {

//    private final JmsTemplate jmsTemplate;
    private final SqsTemplate sqsTemplate;

    @Value("${trainer.workload.queue}")
    private String queueName;

    @Value("${trainer.create.workload.queue}")
    private String createQueueName;

    @Value("${trainer.delete.workload.queue}")
    private String deleteQueueName;

//    public void sendWorkloadUpdate(TrainerWorkloadRequest request) {
//        log.info("Sending workload message to queue={} payload={}", queueName, request);
//        sqsTemplate.convertAndSend(queueName, request, message -> {
//            message.setStringProperty("_type", JmsTypes.TRAINER_WORKLOAD_V1);
//            return message;
//        });
//    }

//    public void sendWorkloadCreateTrainer(TrainerWorkloadRequest request){
//        log.info("Sending workload message for trainer creation to queue={} payload={}", "trainer.create.workload.queue", request);
//        jmsTemplate.convertAndSend("trainer.create.workload.queue", request, message -> {
//            message.setStringProperty("_type", JmsTypes.TRAINER_WORKLOAD_V1);
//            return message;
//        });
//    }

//    public void sendWorkloadDeleteTrainer(TrainerWorkloadRequest request){
//        log.info("Sending workload message for trainer deletion to queue={} payload={}", "trainer.create.workload.queue", request);
//        jmsTemplate.convertAndSend("trainer.delete.workload.queue", request, message -> {
//            message.setStringProperty("_type", JmsTypes.TRAINER_WORKLOAD_V1);
//            return message;
//        });
//    }

    public void sendWorkloadUpdate(TrainerWorkloadRequest request) {
        sendToSqs(queueName, request);
    }

    public void sendWorkloadCreateTrainer(TrainerWorkloadRequest request) {
        sendToSqs(createQueueName, request);
    }

    public void sendWorkloadDeleteTrainer(TrainerWorkloadRequest request) {
        sendToSqs(deleteQueueName, request);
    }

    private void sendToSqs(String queue, TrainerWorkloadRequest payload) {
        log.info("Sending workload message to queue={} payload={}", queue, payload);

        sqsTemplate.send(queue, MessageBuilder
                .withPayload(payload)
                // AWS SQS uses Message Attributes for what JMS calls "Properties"
                .setHeader("_type", JmsTypes.TRAINER_WORKLOAD_V1)
                .build());
    }
}
