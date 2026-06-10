package com.example.graphql.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLRequest;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLResult;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.function.Supplier;

/**
 * Thin transport wrapper around the codegen {@link GraphQLRequest} types.
 * Operations and response projections live in {@code com.example.graphql.client.generated}.
 */
public class CustomerShipmentGraphQlClient {

    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper;
    private final Supplier<String> baseUrlSupplier;
    private volatile RestClient restClient;

    public CustomerShipmentGraphQlClient(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            Supplier<String> baseUrlSupplier) {
        this.restClientBuilder = restClientBuilder;
        this.objectMapper = objectMapper;
        this.baseUrlSupplier = baseUrlSupplier;
    }

    public <T extends GraphQLResult<?>> T execute(GraphQLRequest request, Class<T> responseType) {
        String responseBody = restClient().post()
                .uri("/graphql")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request.toHttpJsonBody())
                .retrieve()
                .body(String.class);

        try {
            T result = objectMapper.readValue(responseBody, responseType);
            if (result.hasErrors()) {
                throw new GraphQlClientException("GraphQL errors: " + result.getErrors());
            }
            return result;
        } catch (GraphQlClientException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new GraphQlClientException("Failed to parse GraphQL response", ex);
        }
    }

    private RestClient restClient() {
        RestClient client = restClient;
        if (client == null) {
            synchronized (this) {
                client = restClient;
                if (client == null) {
                    client = restClientBuilder.baseUrl(baseUrlSupplier.get()).build();
                    restClient = client;
                }
            }
        }
        return client;
    }
}