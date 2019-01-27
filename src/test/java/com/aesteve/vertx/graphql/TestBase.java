package com.aesteve.vertx.graphql;

import com.aesteve.vertx.graphql.mock.Todo;
import com.aesteve.vertx.graphql.mock.Todos;
import com.github.aesteve.vertx.graphql.GraphQLRouter;
import com.github.aesteve.vertx.graphql.AbstractVertxDataFetcher;
import com.github.aesteve.vertx.graphql.VertxSchemaDefinition;
import graphql.GraphQL;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static com.aesteve.vertx.graphql.mock.Todos.todos;

@ExtendWith(VertxExtension.class)
abstract class TestBase {

    private static final int GRAPHQL_SERVER_PORT = 9191;
    private static final String GRAPHQL_SERVER_ENDPOINT = "/graphql";

    protected WebClient client;
    protected String todoListJson;

    @BeforeEach
    void setupGraphQlServer(Vertx vertx, VertxTestContext ctx) throws Exception {
        todoListJson = Json.mapper.writeValueAsString(todos);
        client = WebClient.create(vertx, new WebClientOptions().setDefaultHost("localhost").setDefaultPort(GRAPHQL_SERVER_PORT));
        VertxSchemaDefinition.fromFile(vertx, "todos.graphqls")
                .setHandler(ctx.succeeding(registry -> {
                    GraphQLSchema schema = new SchemaGenerator().makeExecutableSchema(registry, buildDataFetchers(vertx));
                    GraphQL graphQL = GraphQL.newGraphQL(schema).build();
                    vertx.createHttpServer()
                            .requestHandler(GraphQLRouter.create(vertx, graphQL, GRAPHQL_SERVER_ENDPOINT))
                            .listen(GRAPHQL_SERVER_PORT, ctx.completing());
                }));
    }

    private RuntimeWiring buildDataFetchers(Vertx vertx) {
        return RuntimeWiring
                .newRuntimeWiring()
                .type("QueryType", builder ->
                    builder.dataFetcher("todos", new AbstractVertxDataFetcher<List<Todo>>(vertx) {
                        @Override
                        public Future<List<Todo>> fetch(DataFetchingEnvironment environment) {
                            return Todos.allTodos();
                        }
                    }).dataFetcher("todoByName", new AbstractVertxDataFetcher<Todo>(vertx) {
                        @Override
                        public Future<Todo> fetch(DataFetchingEnvironment environment) {
                            return Todos.findByName(environment.getArgument("name"));
                        }
                    }).dataFetcher("completedTodos", new AbstractVertxDataFetcher(vertx) {
                        @Override
                        public Future<List<Todo>> fetch(DataFetchingEnvironment environment) {
                            return Todos.completedTodos();
                        }
                    })
                )
                .build();
    }

}
