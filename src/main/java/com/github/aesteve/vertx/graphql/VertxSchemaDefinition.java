package com.github.aesteve.vertx.graphql;

import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;

import java.nio.charset.StandardCharsets;

public class VertxSchemaDefinition {

    public static Future<TypeDefinitionRegistry> fromFile(Vertx vertx, String fileName) {
        Future<Buffer> fut = Future.future();
        vertx.fileSystem()
                .readFile(fileName, fut.completer());
        return fut.map(buff ->
                new SchemaParser().parse(buff.toString(StandardCharsets.UTF_8))
        );
    }


}
