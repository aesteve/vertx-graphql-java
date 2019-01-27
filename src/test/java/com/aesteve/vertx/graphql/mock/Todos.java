package com.aesteve.vertx.graphql.mock;

import io.vertx.core.Future;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class Todos {

    public final static List<Todo> todos = new ArrayList<>();

    static {
        Todo todo1 = new Todo();
        todo1.setName("todo1");
        todo1.setDescription("Description of todo1");
        todo1.setCompleted(false);
        todos.add(todo1);
        Todo todo2 = new Todo();
        todo2.setName("todo2");
        todo2.setDescription("Description of todo2");
        todo2.setCompleted(true);
        todos.add(todo2);
    }

    public static Future<List<Todo>> allTodos() {
        return Future.succeededFuture(todos);
    }

    public static Future<Todo> findByName(String name) {
        return Future.succeededFuture(
                todos.stream()
                        .filter(t -> t.getName().equals(name))
                        .findFirst()
                        .orElse(null)
        );
    }

    public static Future<List<Todo>> completedTodos() {
        return Future.succeededFuture(
                        todos.stream()
                        .filter(Todo::isCompleted)
                        .collect(toList())
        );
    }
}
