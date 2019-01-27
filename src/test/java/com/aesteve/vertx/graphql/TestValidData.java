package com.aesteve.vertx.graphql;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(VertxExtension.class)
class TestValidData extends TestBase {

    @Test
    @Timeout(value = 2, timeUnit = TimeUnit.SECONDS)
    void request_all_todo_names_using_get(VertxTestContext ctx) {
        String query = "{ todos { name } }";
        client.get("/graphql")
                .addQueryParam("query", query)
                .send(ctx.succeeding(resp -> {
                    ctx.verify(() -> {
                        assertEquals(200, resp.statusCode());
                        JsonObject json = resp.bodyAsJsonObject();
                        assertTrue(json.getJsonArray("errors").isEmpty());
                        JsonObject data = json.getJsonObject("data");
                        assertNotNull(data);
                        JsonArray todos = data.getJsonArray("todos");
                        todos.forEach(todo -> {
                            assertNotNull(((JsonObject)todo).getString("name"));
                        });
                        ctx.completeNow();
                    });
                }));
    }


    @Test
    @Timeout(value = 2, timeUnit = TimeUnit.SECONDS)
    void request_one_todo_completed_and_description_using_post(VertxTestContext ctx) {
        String query = "query { todoByName(name: \"todo2\") { description completed } }";
        JsonObject payload = new JsonObject().put("query", query);
        client.post("/graphql")
                .addQueryParam("query", query)
                .sendJsonObject(payload, ctx.succeeding(resp -> {
                    ctx.verify(() -> {
                        assertEquals(200, resp.statusCode());
                        JsonObject json = resp.bodyAsJsonObject();
                        assertTrue(json.getJsonArray("errors").isEmpty());
                        JsonObject data = json.getJsonObject("data");
                        assertNotNull(data);
                        JsonObject todo = data.getJsonObject("todoByName");
                        assertTrue(todo.getBoolean("completed"));
                        assertEquals("Description of todo2", todo.getString("description"));
                        ctx.completeNow();
                    });
                }));
    }


}
