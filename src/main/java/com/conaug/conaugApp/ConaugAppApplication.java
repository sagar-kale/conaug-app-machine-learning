package com.conaug.conaugApp;

import com.conaug.conaugApp.service.ParseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
@Slf4j
public class ConaugAppApplication {

    @Autowired
    private ParseService parseService;

    @PostConstruct
    public void init() {
        log.info("Training model for sentiment analysis....");
        parseService.trainModel();

    }

    public static void main(String[] args) {
        SpringApplication.run(ConaugAppApplication.class, args);
    }

}
