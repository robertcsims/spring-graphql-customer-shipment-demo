package com.example.graphql.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Exercises every typed client operation against the live /graphql endpoint.
 * Startup verification is controlled by {@code app.graphql.client.verify-on-startup}.
 */
@Component
public class GraphQlClientDemoVerifier {

    private static final Logger log = LoggerFactory.getLogger(GraphQlClientDemoVerifier.class);

    private final CustomerShipmentGraphQlDemoService demoService;
    private final boolean verifyOnStartup;
    private volatile GraphQlClientDemoReport lastReport;

    public GraphQlClientDemoVerifier(
            CustomerShipmentGraphQlDemoService demoService,
            @Value("${app.graphql.client.verify-on-startup:true}") boolean verifyOnStartup) {
        this.demoService = demoService;
        this.verifyOnStartup = verifyOnStartup;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void verifyOnStartup() {
        if (!verifyOnStartup) {
            return;
        }
        lastReport = runVerification();
    }

    public GraphQlClientDemoReport runVerification() {
        GraphQlClientDemoReport report = demoService.runAllDemoOperations();
        lastReport = report;
        if (report.allPassed()) {
            log.info("Typed GraphQL Java client verification passed ({}/{} operations)",
                    report.passed(), report.total());
        } else {
            log.warn("Typed GraphQL Java client verification incomplete ({}/{} operations): {}",
                    report.passed(), report.total(), report.operations());
        }
        return report;
    }

    public GraphQlClientDemoReport getLastReport() {
        GraphQlClientDemoReport report = lastReport;
        return report != null ? report : runVerification();
    }
}