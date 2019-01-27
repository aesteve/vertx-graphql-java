## Vertx + GraphQL-java

A very basic example and a set of utility methods allowing to build a graphql-compliant http server with Vert.x.
The main point is that graphql-java already provides a way to deal with asynchronous responses.
Just a tiny set of utility methods is needed to convert from `Future` to `CompletableFuture`.

On top of that comes a very basic http server, handling GraphQL queries either through POST or GET.

It's a basic example, not a library, since the code could be adapted if you're using RxJava Singles, koroutines, etc.
