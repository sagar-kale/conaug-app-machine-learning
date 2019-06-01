package com.conaug.conaugApp.controller;

import com.conaug.conaugApp.model.Feedback;
import com.conaug.conaugApp.model.Response;
import com.conaug.conaugApp.service.ParseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping(path = "/conaug")
@Slf4j
public class ParserController {
    @Autowired
    private ParseService parseService;

    @GetMapping(value = "/parse", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Response parseSentence(@RequestBody Feedback feedback) {
        log.info("under parse method");
        log.info("data :: {}", feedback.getData());
        return parseService.parseData(feedback);
    }

    @GetMapping(value = "/analyze", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Response analyzeSentiments(@RequestBody Feedback feedback) {
        log.info("under analyzeSentiments method");
        log.info("data :: {}", feedback.getData());
        return parseService.identifyOrg(feedback);
    }

}
