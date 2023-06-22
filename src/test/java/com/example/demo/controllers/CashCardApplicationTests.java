package com.example.demo.controllers;
import com.example.demo.model.CashCard;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;

/*
This lab is a continuous example of the Red, Green, Refactor development loop we learned about in a previous lesson.

As we refactor, our tests will periodically fail when we run them. We'll know we've successfully removed all hard-coded
data from our Controller and "migrated" that data (and data management) to a database-backed Repository when our tests pass again.
 */

/*
The reason is that one of the other tests is interfering with our new test by creating a new Cash Card. @DirtiesContext fixes this problem by causing Spring to start with a clean slate, as if those other tests hadn't been run.
shouldCreateANewCashCard() maakt een nieuwe cashcard aan
fix;
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CashCardApplicationTests {

    //Autowired in principe alleen in tests gebruiken
    @Autowired
    TestRestTemplate restTemplate;

    /*
    Let op de methode om de response uit te parsen en daar velden zoals id en amount uit te halen

    DocumentContext documentContext = JsonPath.parse(response.getBody());
This converts the response String into a JSON-aware object with lots of helper methods.
     */
    @Test
    void shouldReturnACashCardWhenDataIsSaved() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .getForEntity("/cashcards/99", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        Number id = documentContext.read("$.id");
        assertThat(id).isEqualTo(99);

        Double amount = documentContext.read("$.amount");
        assertThat(amount).isEqualTo(123.45);
    }

    @Test
    void shouldNotReturnACashCardWithAnUnknownId() {
        ResponseEntity<String> response = restTemplate.getForEntity("/cashcards/1000", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isBlank();
    }

    /*
    Although you can use @DirtiesContext to work around inter-test interaction, you shouldn't use it indiscriminately; you should have a good reason. Our reason here is to clean up after creating a new Cash Card.

Leave DirtiesContext commented out at the class level, and uncomment it on the method which creates a new Cash Card:
     */
    @Test
    @DirtiesContext
    void shouldCreateANewCashCard() {
        CashCard newCashCard = new CashCard(null, 250.00, "sarah1");
        ResponseEntity<Void> createResponse = restTemplate.postForEntity("/cashcards", newCashCard, Void.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        URI locationOfNewCashCard = createResponse.getHeaders().getLocation();
        ResponseEntity<String> getResponse = restTemplate.getForEntity(locationOfNewCashCard, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
        Number id = documentContext.read("$.id");
        Double amount = documentContext.read("$.amount");

        assertThat(id).isNotNull();
        assertThat(amount).isEqualTo(250.00);
    }


    /*
    documentContext.read("$.length()");
...
documentContext.read("$..id");
...
documentContext.read("$..amount");
Check out these new JsonPath expressions!

documentContext.read("$.length()") calculates the length of the array.

.read("$..id") retrieves the list of all id values returned, while .read("$..amount") collects all amounts returned.

To learn more about JsonPath, a good place to start is here in the JsonPath documentation:
https://github.com/json-path/JsonPath

     */
    @Test
    void shouldReturnAllCashCardsWhenListIsRequested() {
        ResponseEntity<String> response = restTemplate.getForEntity("/cashcards", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        int cashCardCount = documentContext.read("$.length()");
        assertThat(cashCardCount).isEqualTo(3);

        JSONArray ids = documentContext.read("$..id");
        assertThat(ids).containsExactlyInAnyOrder(99, 100, 101);

        JSONArray amounts = documentContext.read("$..amount");
        assertThat(amounts).containsExactlyInAnyOrder(123.45, 1.0, 150.00);
    }

    /*
    The URI we are requesting contains both pagination and sorting information: /cashcards?page=0&size=1&sort=amount,desc

page=0: Get the first page. Page indexes start at 0.
size=1: Each page has size 1.
sort=amount,desc
The extraction of data (using more JSONPath!) and accompanying assertions expect that the returned Cash Card is the $150.00 one.
     */
    @Test
    void shouldReturnASortedPageOfCashCards() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .getForEntity("/cashcards/paged?page=0&size=1&sort=amount,desc", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        JSONArray read = documentContext.read("$[*]");
        assertThat(read.size()).isEqualTo(1);

        double amount = documentContext.read("$[0].amount");
        assertThat(amount).isEqualTo(150.00);
    }

    @Test
    void shouldNotReturnACashCardWhenUsingBadCredentials() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("BAD-USER", "abc123")
                .getForEntity("/cashcards/99", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        response = restTemplate
                .withBasicAuth("sarah1", "BAD-PASSWORD")
                .getForEntity("/cashcards/99", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRejectUsersWhoAreNotCardOwners() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("hank-owns-no-cards", "qrs456")
                .getForEntity("/cashcards/99", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldNotAllowAccessToCashCardsTheyDoNotOwn() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .getForEntity("/cashcards/102", String.class); // kumar2's data
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}