package com.ime.lockmanager.reservation.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ime.lockmanager.locker.application.port.in.req.LockerRegisterRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SqsTransferListener {
    private final ObjectMapper mapper;

    @SqsListener(value = "${cloud.aws.sqs.queue-name}")
    public void messageListener(String message) throws JsonProcessingException {
        LockerRegisterRequestDto requestDto = mapper.readValue(message, LockerRegisterRequestDto.class);
        System.out.println("Listener: " + requestDto.getMajorId());
    }
}
