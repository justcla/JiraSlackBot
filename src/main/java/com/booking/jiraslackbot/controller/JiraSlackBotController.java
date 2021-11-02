package com.booking.jiraslackbot.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class JiraSlackBotController {

    @GetMapping("/")
    public String index(@RequestParam(required=false) Map<String,String> reqParams) {
        String message = reqParams.get("message");
        System.out.println("Received message = " + message);
        String operation = message.substring(0, message.indexOf(' '));
        return "Operation is: " + operation;
    }

}