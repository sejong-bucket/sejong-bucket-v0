package com.ime.lockmanager.reservation.application.service;

import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ime.lockmanager.locker.application.port.in.req.LockerRegisterRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SqsTransferListener {
    private final ObjectMapper mapper;
    private final ReservationCommandService reservationCommandService;
//    private final TestService testService;
    /*@Value("${cloud.aws.sqs.queue-name}")
    private String queueUrl;*/
    @SqsListener(value = "${cloud.aws.sqs.queue-name}")
    public void messageListener(/*AmazonSQSClient sqsClient,*/String message) throws Exception {
//        ReceiveMessageRequest receiveRequest = new ReceiveMessageRequest()
//                .withQueueUrl(queueUrl)
//                .withMaxNumberOfMessages(1)
//                .withWaitTimeSeconds(20);
//                .queueUrl(queueUrl)
//                .waitTimeSeconds(20)
//                .build();
//        ReceiveMessageResult receiveMessageResult = sqsClient.receiveMessage(receiveRequest);
//        List<Message> messages = receiveMessageResult.getMessages();
//        for (Message message1 : messages) {
//        LockerRegisterRequestDto requestDto = mapper.readValue(message, LockerRegisterRequestDto.class);
//        System.out.println("Listener: " + requestDto.getMajorId());
//        }
        LockerRegisterRequestDto requestDto = mapper.readValue(message, LockerRegisterRequestDto.class);
        reservationCommandService.reserveForUser(requestDto);
//        System.out.println("끝");
//        testService.test(message);
    }
}
