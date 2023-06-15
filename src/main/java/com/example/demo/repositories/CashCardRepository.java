package com.example.demo.repositories;

import com.example.demo.model.CashCard;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

/*
PagingAndSortingRepository<CashCard, Long> maakt paging en sorting mogelijk
 */

public interface CashCardRepository extends CrudRepository<CashCard, Long>, PagingAndSortingRepository<CashCard, Long> {
}
