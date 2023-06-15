package com.example.demo.controllers;

import com.example.demo.model.CashCard;
import com.example.demo.repositories.CashCardRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
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

    @GetMapping()
    public ResponseEntity<Iterable<CashCard>> findAll() {
        return ResponseEntity.ok(cashCardRepository.findAll());
    }

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
}