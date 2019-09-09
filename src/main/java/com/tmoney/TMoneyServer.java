package com.tmoney;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.eclipse.jetty.http.HttpStatus.*;
import static spark.Spark.*;

public class TMoneyServer {
    private static final ObjectMapper json = new ObjectMapper();

    public static void main(final String[] args) {
        AccountService accountService = new AccountService();
        TransactionService transactionService = new TransactionService(accountService);
        port(8081);
        before("*", "application/json");
        after((req, res) -> res.type("application/json"));

        path("/api", () -> {
            path("/accounts", () -> {
                post("", (request, response) ->
                        json.writeValueAsString(accountService.add(json.readValue(request.body(), Account.class))));
                put("", (request, response) -> {
                    accountService.change(json.readValue(request.body(), Account.class));
                    response.status(NO_CONTENT_204);
                    return "";
                });
                delete("/:number", (request, response) -> {
                    accountService.delete(request.params("number"));
                    response.status(NO_CONTENT_204);
                    return "";
                });
                get("", (request, response) ->
                        json.writeValueAsString(accountService.getByName(request.queryParams("name"))));
                get("/:number", (request, response) ->
                        json.writeValueAsString(accountService.get(request.params("number"))));
            });

            path("/transactions", () -> {
                post("", (request, response) ->
                        json.writeValueAsString(transactionService.add(json.readValue(request.body(), Transaction.class))));
                get("", (request, response) ->
                        json.writeValueAsString(transactionService.getByAccount(request.queryParams("account"))));
                get("/:id", (request, response) ->
                        json.writeValueAsString(transactionService.get(Long.valueOf(request.params("id")))));
            });
        });

        exception(NullPointerException.class, (exception, request, response) -> {
            response.status(NOT_FOUND_404);
            response.body(String.format("{\"message\":\"%s\"}", exception.getMessage()));
        });

        exception(TransactionService.InsufficientFundsException.class, (exception, request, response) -> {
            response.status(UNPROCESSABLE_ENTITY_422);
            response.body(String.format("{\"message\":\"%s\"}", exception.getMessage()));
        });

        exception(AccountService.AccountAlreadyOpenedException.class, (exception, request, response) -> {
            response.status(UNPROCESSABLE_ENTITY_422);
            response.body(String.format("{\"message\":\"%s\"}", exception.getMessage()));
        });

        post("/reset", ((request, response) -> {
            transactionService.reset();
            accountService.reset();
            return 0;
        }));
    }

}
