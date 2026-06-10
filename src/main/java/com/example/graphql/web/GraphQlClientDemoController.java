package com.example.graphql.web;

import com.example.graphql.client.GraphQlClientDemoReport;
import com.example.graphql.client.GraphQlClientDemoVerifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo")
public class GraphQlClientDemoController {

    private final GraphQlClientDemoVerifier verifier;

    public GraphQlClientDemoController(GraphQlClientDemoVerifier verifier) {
        this.verifier = verifier;
    }

    @GetMapping("/graphql-client")
    public GraphQlClientDemoReport status() {
        return verifier.getLastReport();
    }

    @PostMapping("/graphql-client/verify")
    public GraphQlClientDemoReport verifyNow() {
        return verifier.runVerification();
    }
}