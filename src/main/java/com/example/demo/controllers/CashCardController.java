package com.example.demo.controllers;

import com.example.demo.model.CashCard;
import com.example.demo.repositories.CashCardRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/cashcards")
public class CashCardController {
    private CashCardRepository cashCardRepository;

    /*
    Dit is de voorkeur injection methode, beter dan Autowired

    Spring's Auto Configuration is utilizing its dependency injection (DI) framework, specifically constructor injection, to supply CashCardController with the correct implementation of CashCardRepository at runtime.
     */
    public CashCardController(CashCardRepository cashCardRepository) {
        this.cashCardRepository = cashCardRepository;
    }

    /*
    @PathVariable makes Spring Web aware of the requestedId supplied in the HTTP request.
    Now it’s available for us to use in our handler method.
     */
    @GetMapping("/{requestedId}")
    public ResponseEntity<CashCard> findById(@PathVariable Long requestedId) {
        Optional<CashCard> cashCardOptional = cashCardRepository.findById(requestedId);
        if (cashCardOptional.isPresent()) {
            return ResponseEntity.ok(cashCardOptional.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

/*
met security:
 */
//    @GetMapping("/{requestedId}")
//    public ResponseEntity<CashCard> findById(@PathVariable Long requestedId, Principal principal) {
//        CashCard cashCard = findCashCard(requestedId, principal);
//
//    @GetMapping()
//    public ResponseEntity<Iterable<CashCard>> findAll() {
//        return ResponseEntity.ok(cashCardRepository.findAll());
//    }

    /*
    NB ook repos aanpassen: PagingAndSortingRepository<CashCard, Long>

The URI we are requesting contains both pagination and sorting information: /cashcards?page=0&size=1&sort=amount,desc

page=0: Get the first page. Page indexes start at 0.
size=1: Each page has size 1.
sort=amount,desc

/cashcards/paged?page=0&size=1&sort=amount,desc

Spring provides the default page and size values (they are 0 and 20, respectively).
     */
    @GetMapping("/paged")
    public ResponseEntity<List<CashCard>> findAll(Pageable pageable) {
        Page<CashCard> page = cashCardRepository.findAll(
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSort()
//                        pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount"))
                ));
        return ResponseEntity.ok(page.getContent());
    }

    /*
    Let op UriComponentsBuilder ucb: injected door spring: deze helpt om de waarde van de id te achter halen en dez ein de location header te zetten conform de REST voorschrift

    According to the official specification:

the origin server SHOULD send a 201 (Created) response ...

We now expect the HTTP response status code to be 201 CREATED, which is semantically correct if our API creates a new CashCard from our request.

URI locationOfNewCashCard = createResponse.getHeaders().getLocation();
The official spec continue to state the following:

send a 201 (Created) response containing a Location header field that provides an identifier for the primary resource created ...

In other words, when a POST request results in the successful creation of a resource, such as a new CashCard, the response should include information for how to retrieve that resource. We'll do this by supplying a URI in a Response Header named "Location".

Note that URI is indeed the correct entity here and not a URL; a URL is a type of URI, while a URI is more generic.

createCashCard(@RequestBody CashCard newCashCardRequest, ...)
Unlike the GET we added earlier, the POST expects a request "body". This contains the data submitted to the API. Spring Web will deserialize the data into a CashCard for us.

URI locationOfNewCashCard = ucb
   .path("cashcards/{id}")
   .buildAndExpand(savedCashCard.id())
   .toUri();
This is constructing a URI to the newly created CashCard. This is the URI that the caller can then use to GET the newly-created CashCard.

Note that savedCashCard.id is used as the identifier, which matches the GET endpoint's specification of cashcards/<CashCard.id>.

Where did UriComponentsBuilder come from?

We were able to add UriComponentsBuilder ucb as a method argument to this POST handler method and it was automatically passed in. How so? It was injected from our now-familiar friend, Spring's IoC Container. Thanks, Spring Web!

return ResponseEntity.created(locationOfNewCashCard).build();
Finally, we return 201 CREATED with the correct Location header.

     */
    @PostMapping
    private ResponseEntity<Void> createCashCard(@RequestBody CashCard newCashCardRequest, UriComponentsBuilder ucb) {
        CashCard savedCashCard = cashCardRepository.save(newCashCardRequest);
        URI locationOfNewCashCard = ucb
                .path("cashcards/{id}")
                .buildAndExpand(savedCashCard.id())
                .toUri();
        return ResponseEntity.created(locationOfNewCashCard).build();
    }

    /*
    PUT voor update
     */
    @PutMapping("/{requestedId}")
    private ResponseEntity<Void> putCashCard(@PathVariable Long requestedId, @RequestBody CashCard cashCardUpdate) {
        CashCard cashCard = cashCardRepository.findByIdAndOwner(requestedId, "sarah1");
        if (cashCard != null) {
            CashCard updatedCashCard = new CashCard(cashCard.id(), cashCardUpdate.amount(), "sarah1");
            cashCardRepository.save(updatedCashCard);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.noContent().build();
    }

//    /*
//    met security:
//     */
//    @GetMapping("/{requestedId}")
//    public ResponseEntity<CashCard> findById(@PathVariable Long requestedId, Principal principal) {
//        CashCard cashCard = cashCardRepository.findByIdAndOwner(requestedId, principal.getName());
//        if (cashCard != null) {
//            return ResponseEntity.ok(cashCard);
//        } else {
//            return ResponseEntity.notFound().build();
//        }
//    }

}