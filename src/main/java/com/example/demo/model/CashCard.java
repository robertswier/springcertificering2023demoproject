package com.example.demo.model;

import org.springframework.data.annotation.Id;

/*
CrudRepository<CashCard, Long> we indicate that the CashCard's ID is Long. However, we still need to tell Spring Data
which field is the ID: @Id
Dit lost de volgend emelding op: IllegalStateException: Required identifier property not found for class com.example.demo.model.CashCard
 */
public record CashCard(@Id Long id, Double amount, String owner) {
}
