package com.gitagyan.gita.controllers;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@CrossOrigin
@RestController
public class GitaGyanController {
    @Autowired
    ChatClient chatClient;
    @Autowired
    PgVectorStore pgVectorStore;
    @Value("classpath:SystemPrompt.st")
    String systempropmt;

    @GetMapping("/gitagyan")
    public Map<String,String> gitagyan(@RequestParam String question){

        ChatResponse chatResponse= this.chatClient.prompt()
                .system(systempropmt)
                .user(question).
                advisors(new QuestionAnswerAdvisor(pgVectorStore, SearchRequest.query(question).withTopK(4)))
                .call()
                .chatResponse();
        return Map.of("response",chatResponse.getResult().getOutput().getContent());
    }
}
