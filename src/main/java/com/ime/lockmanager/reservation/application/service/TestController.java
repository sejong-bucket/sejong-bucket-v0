package com.ime.lockmanager.reservation.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class TestController {
    private final TestService testService;

    @PostMapping("/test")
    public void test(){
        testService.test("test");
    }
}
