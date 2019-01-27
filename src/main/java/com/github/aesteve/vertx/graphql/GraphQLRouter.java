package com.github.aesteve.vertx.graphql;

import com.fasterxml.jackson.core.JsonProcessingException;
import graphql.ExecutionInput;
import graphql.GraphQL;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class GraphQLRouter {

    private static final Logger LOG = LoggerFactory.getLogger(GraphQLRouter.class);
    private static final String JSON_CONTENT_TYPE = "application/json";

    public static Router create(Vertx vertx, GraphQL graphQL, String graphQLEndpoint) {
        Router r = Router.router(vertx);
        r.get(graphQLEndpoint)
                .produces(JSON_CONTENT_TYPE)
                .handler(rc -> {
                    String query = rc.request().getParam("query");
                    if (query == null) {
                        rc.response().setStatusCode(400).end(error("Query parameter is mandatory"));
                        return;
                    }
                    executeQuery(rc, graphQL, query, null);
                });
        r.post(graphQLEndpoint)
                .produces(JSON_CONTENT_TYPE)
                .consumes(JSON_CONTENT_TYPE)
                .handler(BodyHandler.create());
        r.post(graphQLEndpoint).handler(rc -> {
            HttpServerResponse resp = rc.response();
            JsonObject body;
            try {
                body = rc.getBodyAsJson();
            } catch (Exception e) {
                resp.setStatusCode(400).end(error("Could not read request body as JSON"));
                return;
            }
            String query = body.getString("query");
            if (query == null) {
                resp.setStatusCode(400).end(error("No 'query' specified in the request body"));
                return;
            }
            Map<String, Object> variables = new HashMap<>();
            try {
                JsonObject jsonVariables = body.getJsonObject("variables");
                if (jsonVariables != null) {
                    variables = jsonVariables.getMap();
                }
            } catch(Exception e) {
                resp.setStatusCode(400).end(error("Invalid json 'variables' node supplied in request body"));
            }
            executeQuery(rc, graphQL, query, variables);
        });
        return r;
    }

    private static void executeQuery(RoutingContext rc, GraphQL graphQL, String query, Map<String, Object> variables) {
        graphQL.executeAsync(
                ExecutionInput.newExecutionInput()
                        .query(query)
                        .variables(variables)
                        .build()
        ).whenComplete((result, error) -> {
            if (error != null) {
                LOG.error("Failed executing graphql query", error);
                rc.response().setStatusCode(500).end(error(error.getMessage()));
                return;
            }
            try {
                rc.response().setStatusCode(200).end(Json.mapper.writeValueAsString(result));
            } catch (JsonProcessingException e) {
                LOG.error("Failed writing graphql response as JSON", e);
                rc.response().setStatusCode(500).end(error("Could not send response as JSON. " + e.getMessage()));
            }
        });
    }

    private static String error(String reason) {
        return new JsonObject()
                .put("error", "reason")
                .putNull("data")
                .encode();
    }

}
