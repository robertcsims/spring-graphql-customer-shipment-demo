package com.example.graphql.client;

import java.time.Instant;
import java.util.List;

public record GraphQlClientDemoReport(
        boolean allPassed,
        int passed,
        int total,
        String clientType,
        String operationsSource,
        Instant verifiedAt,
        List<GraphQlClientOperationResult> operations
) {
}