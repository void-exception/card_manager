package com.cardmanager.card.Model.Card;

import com.cardmanager.card.Security.Model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends CrudRepository<Card, Long> {
    List<Card> findAllByUser(User user);

    Optional<Card> findByCardNumber(String cardNumber);

    void deleteByUser(User user);

    Page<Card> findAllByStatus(CardStatus status, Pageable pageable);

    Page<Card> findAll(Pageable pageable);
}
