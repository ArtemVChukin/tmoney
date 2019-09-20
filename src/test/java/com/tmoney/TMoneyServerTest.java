package com.tmoney;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.post;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;

public class TMoneyServerTest {
    private static final String ACCOUNTS = "api/accounts";
    private static final String ACCOUNT = "api/accounts/{number}";
    private static final String TRANSACTIONS = "api/transactions";
    private static final String TRANSACTION = "api/transactions/{id}";
    private static final String JSON_SHORT_ACCOUNT = "\"name\":\"%s\",\"balance\":%s";
    private static final String JSON_FULL_ACCOUNT = "{\"number\":\"%s\",\"name\":\"%s\",\"balance\":%s}";
    private static final String JSON_SHORT_TRANSACTION = "\"debit\":\"%s\",\"credit\":\"%s\",\"amount\":%s";
    private static final String JSON_FULL_TRANSACTION = "{\"id\":%s,\"debit\":\"%s\",\"credit\":\"%s\",\"amount\":%s}";

    @BeforeClass
    public static void setUpClass() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8081;
        RestAssured.basePath = "/";
        TMoneyServer.main(new String[]{});
    }

    @After
    public void tearDown() {
        post("reset");
    }

    @Test
    public void postAccount() {
        createAccount("blank name", "12.00");
    }

    @Test
    public void postSameAccount() {
        String account = createAccount("blank name", "12.00");
        String formatJSON = String.format(JSON_FULL_ACCOUNT, account, "name", "123");
        given().contentType(ContentType.JSON).accept(ContentType.JSON)
                .body(formatJSON)
                .when().post(ACCOUNTS)
                .then()
                .statusCode(HttpStatus.UNPROCESSABLE_ENTITY_422);
    }

    @Test
    public void postInvalidAccount() {
        String formatJSON = String.format(JSON_SHORT_ACCOUNT, "valid name", "invalid balance");
        System.out.println(given().contentType(ContentType.JSON).accept(ContentType.JSON)
                .body("{" + formatJSON + "}")
                .when().post(ACCOUNTS)
                .then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR_500).extract().body().asString());
    }

    private String createAccount(String name, String balance) {
        String formatJSON = String.format(JSON_SHORT_ACCOUNT, name, balance);
        return given().contentType(ContentType.JSON).accept(ContentType.JSON)
                .body("{" + formatJSON + "}")
                .when().post(ACCOUNTS)
                .then()
                .statusCode(HttpStatus.OK_200)
                .and().assertThat()
                .body(containsString(formatJSON))
                .body(containsString("\"number\":"))
                .extract().body().jsonPath().getString("number");
    }

    @Test
    public void getAccount() {
        String name = "new name";
        String balance = "1.50";
        String postAccountNumber = createAccount(name, balance);
        String getAccountNumber = given().contentType(ContentType.JSON).accept(ContentType.JSON)
                .pathParams("number", postAccountNumber)
                .when().get(ACCOUNT)
                .then()
                .statusCode(HttpStatus.OK_200)
                .and().assertThat()
                .body(containsString(String.format(JSON_SHORT_ACCOUNT, name, balance)))
                .body(containsString("\"number\":"))
                .extract().body().jsonPath().getString("number");
        assertEquals(postAccountNumber, getAccountNumber);
    }

    @Test
    public void getNullAccount() {
        given().contentType(ContentType.JSON).accept(ContentType.JSON)
                .pathParams("number", "non exists number")
                .when().get(ACCOUNT)
                .then()
                .statusCode(HttpStatus.NOT_FOUND_404);
    }

    @Test
    public void getAccounts() {
        String name0 = "new name";
        String balance0 = "1.50";
        String name1 = "second name";
        String balance1 = "1500";
        String name2 = "brand new name";
        String balance2 = "200";
        List<String> accounts = Arrays.asList(
                createAccount(name0, balance0),
                createAccount(name1, balance1),
                createAccount(name2, balance2)
        );
        int size = given().contentType(ContentType.JSON).accept(ContentType.JSON)
                .when().get(ACCOUNTS)
                .then()
                .statusCode(HttpStatus.OK_200)
                .and().assertThat()
                .body(containsString(String.format(JSON_FULL_ACCOUNT, accounts.get(0), name0, balance0)))
                .body(containsString(String.format(JSON_FULL_ACCOUNT, accounts.get(1), name1, balance1)))
                .body(containsString(String.format(JSON_FULL_ACCOUNT, accounts.get(2), name2, balance2)))
                .extract().body().as(List.class).size();
        assertEquals(3, size);

        size = given().contentType(ContentType.JSON).accept(ContentType.JSON)
                .queryParams("name", name2)
                .when().get(ACCOUNTS)
                .then()
                .statusCode(HttpStatus.OK_200)
                .and().assertThat()
                .body(containsString(String.format(JSON_FULL_ACCOUNT, accounts.get(2), name2, balance2)))
                .extract().body().as(List.class).size();
        assertEquals(1, size);
    }

    @Test
    public void updateAccount() {
        String postAccountNumber = createAccount("name", "45");
        String newName = "new name";
        String newBalance = "888";
        given().contentType(ContentType.JSON).accept(ContentType.JSON)
                .body(String.format(JSON_FULL_ACCOUNT, postAccountNumber, newName, newBalance))
                .when().put(ACCOUNTS)
                .then()
                .statusCode(HttpStatus.NO_CONTENT_204);
        given().contentType(ContentType.JSON).accept(ContentType.JSON)
                .pathParams("number", postAccountNumber)
                .when().get(ACCOUNT)
                .then()
                .statusCode(HttpStatus.OK_200)
                .and().assertThat()
                .body(containsString(String.format(JSON_FULL_ACCOUNT, postAccountNumber, newName, newBalance)));
    }

    @Test
    public void updateNullAccount() {
        given().contentType(ContentType.JSON).accept(ContentType.JSON)
                .body(String.format(JSON_FULL_ACCOUNT, "not exists", "new name", "555"))
                .when().put(ACCOUNTS)
                .then()
                .statusCode(HttpStatus.NOT_FOUND_404);
    }

    @Test
    public void updateInvalidAccount() {
        given().contentType(ContentType.JSON).accept(ContentType.JSON)
                .body(String.format(JSON_FULL_ACCOUNT, "not exists", "new name", "true"))
                .when().put(ACCOUNTS)
                .then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }

    @Test
    public void deleteAccount() {
        String postAccountNumber = createAccount("name", "45");
        given().contentType(ContentType.JSON).accept(ContentType.JSON)
                .pathParams("number", postAccountNumber)
                .when().delete(ACCOUNT)
                .then()
                .statusCode(HttpStatus.NO_CONTENT_204);

        given().contentType(ContentType.JSON).accept(ContentType.JSON)
                .pathParams("number", postAccountNumber)
                .when().get(ACCOUNT)
                .then()
                .statusCode(HttpStatus.NOT_FOUND_404);
    }

    @Test
    public void deleteNullAccount() {
        given().contentType(ContentType.JSON).accept(ContentType.JSON)
                .pathParams("number", "not real number")
                .when().delete(ACCOUNT)
                .then()
                .statusCode(HttpStatus.NOT_FOUND_404);
    }

    @Test
    public void postTransaction() {
        String debit = createAccount("debit account", "500.00");
        String credit = createAccount("credit account", "200");
        createTransaction(debit, credit, "300");
    }

    @Test
    public void postNotEnoughMoneyTransaction() {
        String debit = createAccount("debit account", "200.00");
        String credit = createAccount("credit account", "100");
        String formatJSON = String.format(JSON_SHORT_TRANSACTION, debit, credit, "500");
        given().contentType(ContentType.JSON).accept(ContentType.JSON)
                .body("{" + formatJSON + "}")
                .when().post(TRANSACTIONS)
                .then()
                .statusCode(HttpStatus.UNPROCESSABLE_ENTITY_422);
    }

    @Test
    public void postNotExistingAccountTransaction() {
        String realAccount = createAccount("debit account", "200.00");
        String formatJSON = String.format(JSON_SHORT_TRANSACTION, realAccount, "not real account", "10");
        given().contentType(ContentType.JSON).accept(ContentType.JSON)
                .body("{" + formatJSON + "}")
                .when().post(TRANSACTIONS)
                .then()
                .statusCode(HttpStatus.NOT_FOUND_404);
        formatJSON = String.format(JSON_SHORT_TRANSACTION, "not real account", realAccount, "10");
        given().contentType(ContentType.JSON).accept(ContentType.JSON)
                .body("{" + formatJSON + "}")
                .when().post(TRANSACTIONS)
                .then()
                .statusCode(HttpStatus.NOT_FOUND_404);
    }

    @Test
    public void getTransaction() {
        String debit = createAccount("debit account", "200.00");
        String credit = createAccount("credit account", "100");
        String amount = "55";
        Long transactionId = createTransaction(debit, credit, amount);
        Long getTransactionId = given().contentType(ContentType.JSON).accept(ContentType.JSON)
                .pathParams("id", transactionId)
                .when().get(TRANSACTION)
                .then()
                .statusCode(HttpStatus.OK_200)
                .and().assertThat()
                .body(containsString(String.format(JSON_FULL_TRANSACTION, transactionId, debit, credit, amount)))
                .extract().body().jsonPath().getLong("id");
        assertEquals(transactionId, getTransactionId);
    }

    @Test
    public void getNullTransaction() {
        given().contentType(ContentType.JSON).accept(ContentType.JSON)
                .pathParams("id", "123")
                .when().get(TRANSACTION)
                .then()
                .statusCode(HttpStatus.NOT_FOUND_404);
    }

    @Test
    public void getExceptionTransaction() {
        given().contentType(ContentType.JSON).accept(ContentType.JSON)
                .pathParams("id", "not even a number")
                .when().get(TRANSACTION)
                .then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }

    @Test
    public void getTransactions() {
        String account1 = createAccount("big account", "200.00");
        String account2 = createAccount("empty account", "0");
        String account3 = createAccount("huge account", "1000");
        String amount0 = "100";
        String amount1 = "500";
        String amount2 = "20";
        List<Long> transactions = Arrays.asList(
                createTransaction(account1, account2, amount0),
                createTransaction(account3, account2, amount1),
                createTransaction(account3, account1, amount2)
        );
        int size = given().contentType(ContentType.JSON).accept(ContentType.JSON)
                .when().get(TRANSACTIONS)
                .then()
                .statusCode(HttpStatus.OK_200)
                .and().assertThat()
                .body(containsString(String.format(JSON_FULL_TRANSACTION, transactions.get(0), account1, account2, amount0)))
                .body(containsString(String.format(JSON_FULL_TRANSACTION, transactions.get(1), account3, account2, amount1)))
                .body(containsString(String.format(JSON_FULL_TRANSACTION, transactions.get(2), account3, account1, amount2)))
                .extract().body().as(List.class).size();
        assertEquals(3, size);

        size = given().contentType(ContentType.JSON).accept(ContentType.JSON)
                .queryParam("account", account3)
                .when().get(TRANSACTIONS)
                .then()
                .statusCode(HttpStatus.OK_200)
                .and().assertThat()
                .body(containsString(String.format(JSON_FULL_TRANSACTION, transactions.get(1), account3, account2, amount1)))
                .body(containsString(String.format(JSON_FULL_TRANSACTION, transactions.get(2), account3, account1, amount2)))
                .extract().body().as(List.class).size();
        assertEquals(2, size);
    }

    private Long createTransaction(String debit, String credit, String amount) {
        String formatJSON = String.format(JSON_SHORT_TRANSACTION, debit, credit, amount);
        return given().contentType(ContentType.JSON).accept(ContentType.JSON)
                .body("{" + formatJSON + "}")
                .when().post(TRANSACTIONS)
                .then()
                .statusCode(HttpStatus.OK_200)
                .and().assertThat()
                .body(containsString(formatJSON))
                .body(containsString("\"id\":"))
                .extract().body().jsonPath().getLong("id");
    }
}
