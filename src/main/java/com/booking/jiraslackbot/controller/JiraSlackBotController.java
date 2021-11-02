package com.booking.jiraslackbot.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

//
// Slack App token: xapp-1-A02H428J36W-2693004651296-05892563c021ce1624e82237f78c682823fa8e0a04f18f370beeb6b6b5d20888
//

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